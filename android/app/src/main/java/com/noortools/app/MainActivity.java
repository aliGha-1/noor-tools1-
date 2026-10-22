package com.noortools.app;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.webkit.JavascriptInterface;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;
import java.io.OutputStream;
import java.util.Base64;

public class MainActivity extends Activity {
    private WebView webView;
    private ValueCallback<Uri[]> filePathCallback;
    private static final int FILE_CHOOSER_REQUEST = 1201;

    @Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setStatusBarColor(android.graphics.Color.rgb(12,18,32));
        getWindow().setNavigationBarColor(android.graphics.Color.rgb(12,18,32));
        webView = new WebView(this);
        setContentView(webView);
        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setDatabaseEnabled(true);
        settings.setAllowFileAccess(true);
        settings.setAllowContentAccess(true);
        settings.setSupportZoom(false);
        settings.setBuiltInZoomControls(false);
        settings.setDisplayZoomControls(false);
        webView.addJavascriptInterface(new LocalBridge(), "NoorAndroid");
        webView.setWebViewClient(new WebViewClient() {
            @Override public boolean shouldOverrideUrlLoading(WebView view, String url) {
                // Keep all navigation inside the local app; no network is required.
                return !url.startsWith("file:///android_asset/");
            }
            @Override public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                // Send CSV and JSON backup files to Android's Downloads folder.
                String js = "(function(){window.downloadBlob=async function(blob,name){try{const buf=await blob.arrayBuffer();const bytes=new Uint8Array(buf);let bin='';const step=0x8000;for(let i=0;i<bytes.length;i+=step){bin+=String.fromCharCode.apply(null,bytes.subarray(i,Math.min(i+step,bytes.length)));}NoorAndroid.saveFile(name,btoa(bin));}catch(e){alert('تعذر حفظ الملف. حاول مرة أخرى.');}};})();";
                view.evaluateJavascript(js, null);
            }
        });
        webView.setWebChromeClient(new WebChromeClient() {
            @Override public boolean onShowFileChooser(WebView view, ValueCallback<Uri[]> callback, FileChooserParams params) {
                if (filePathCallback != null) filePathCallback.onReceiveValue(null);
                filePathCallback = callback;
                Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setType("application/json");
                try { startActivityForResult(intent, FILE_CHOOSER_REQUEST); }
                catch (Exception e) { filePathCallback = null; Toast.makeText(MainActivity.this, "تعذر فتح مدير الملفات", Toast.LENGTH_LONG).show(); return false; }
                return true;
            }
        });
        if (savedInstanceState == null) webView.loadUrl("file:///android_asset/www/index.html");
        else webView.restoreState(savedInstanceState);
    }

    private class LocalBridge {
        @JavascriptInterface public void saveFile(String fileName, String base64Data) {
            runOnUiThread(() -> {
                try {
                    String safeName = fileName == null ? "noor-tools-backup.json" : fileName.replaceAll("[^a-zA-Z0-9._-]", "_");
                    byte[] bytes;
                    if (Build.VERSION.SDK_INT >= 26) bytes = Base64.getDecoder().decode(base64Data);
                    else bytes = android.util.Base64.decode(base64Data, android.util.Base64.DEFAULT);
                    ContentValues values = new ContentValues();
                    values.put(MediaStore.Downloads.DISPLAY_NAME, safeName);
                    values.put(MediaStore.Downloads.MIME_TYPE, safeName.endsWith(".csv") ? "text/csv" : "application/json");
                    if (Build.VERSION.SDK_INT >= 29) values.put(MediaStore.Downloads.IS_PENDING, 1);
                    Uri collection = MediaStore.Downloads.EXTERNAL_CONTENT_URI;
                    Uri uri = getContentResolver().insert(collection, values);
                    if (uri == null) throw new Exception("No file URI");
                    try (OutputStream out = getContentResolver().openOutputStream(uri)) { if (out == null) throw new Exception("No output stream"); out.write(bytes); }
                    if (Build.VERSION.SDK_INT >= 29) {
                        ContentValues done = new ContentValues(); done.put(MediaStore.Downloads.IS_PENDING, 0);
                        getContentResolver().update(uri, done, null, null);
                    }
                    Toast.makeText(MainActivity.this, "تم حفظ الملف في مجلد التنزيلات", Toast.LENGTH_LONG).show();
                } catch (Exception e) {
                    Toast.makeText(MainActivity.this, "تعذر حفظ الملف: " + e.getMessage(), Toast.LENGTH_LONG).show();
                }
            });
        }
    }

    @Override protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == FILE_CHOOSER_REQUEST && filePathCallback != null) {
            Uri[] result = (resultCode == RESULT_OK && data != null && data.getData() != null) ? new Uri[]{data.getData()} : null;
            filePathCallback.onReceiveValue(result);
            filePathCallback = null;
        }
    }

    @Override protected void onSaveInstanceState(Bundle outState) { super.onSaveInstanceState(outState); if (webView != null) webView.saveState(outState); }
    @Override public void onBackPressed() { if (webView != null && webView.canGoBack()) webView.goBack(); else super.onBackPressed(); }
    @Override protected void onDestroy() { if (webView != null) webView.destroy(); super.onDestroy(); }
}
