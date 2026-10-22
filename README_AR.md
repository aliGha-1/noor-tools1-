# NOOR TOOLS — GitHub Ready

هذه الحزمة جاهزة للرفع إلى مستودع GitHub واحد، وتشمل:
- موقع ويب ثابت داخل مجلد `site/`، مناسب لـ GitHub Pages.
- ملفات PWA: `manifest.json` و`sw.js` و`icon.svg`.
- مشروع Android داخل مجلد `android/`.
- GitHub Actions لنشر الموقع تلقائيًا وبناء APK تجريبي تلقائيًا.

## أسهل طريقة من كمبيوتر ويندوز
1. فك ضغط الحزمة.
2. افتح مجلد `NOOR_TOOLS_GITHUB_READY`.
3. افتح Terminal / PowerShell داخل المجلد.
4. استبدل `YOUR-USERNAME` باسم حساب GitHub الحقيقي، ثم نفّذ الأوامر:

```bash
git init
git branch -M main
git add .
git commit -m "Upload NOOR TOOLS website and Android project"
git remote add origin https://github.com/YOUR-USERNAME/noor-tools.git
git push -u origin main
```

يجب أن تكون قد أنشأت مستودعًا فارغًا اسمه `noor-tools` على GitHub. إذا كان المستودع باسم مختلف، غيّر الرابط في أمر `git remote add origin`.

## نشر الموقع
بعد الرفع:
1. افتح المستودع على GitHub ثم Settings → Pages.
2. في Build and deployment، اختر **GitHub Actions** كمصدر.
3. افتح تبويب Actions وانتظر نجاح `Deploy Noor Tools website`.
4. سيظهر رابط الموقع في Settings → Pages أو في نتيجة عملية النشر.

## بناء APK تلقائيًا
1. من المستودع افتح Actions.
2. اختر `Build Noor Tools APK` ثم `Run workflow`، أو ادفع تحديثًا داخل مجلد `android/`.
3. بعد نجاح العملية، افتح صفحة العملية وانزل إلى Artifacts.
4. نزّل `Noor-Tools-debug-APK.zip` وفك ضغطه؛ بداخله `app-debug.apk`.

هذه نسخة Debug للتجربة وليست APK موقّعًا للنشر التجاري. لا ترفع مفاتيح التوقيع أو كلمات السر إلى مستودع عام.

## ملاحظات مهمة
- اجعل المستودع Private إن كانت بيانات المشروع أو تفاصيله لا تريد نشرها. الموقع المنشور عبر GitHub Pages يكون عامًا عادةً، حتى لو كانت سياسة المستودع تختلف حسب نوع الحساب والإعدادات؛ لا تضع بيانات عملاء حقيقية في ملفات الموقع.
- بيانات الموقع محفوظة محليًا في المتصفح، ولا تتزامن تلقائيًا بين الهاتف والكمبيوتر. امسح بيانات المتصفح فقط بعد أخذ نسخة احتياطية.
- العمل دون إنترنت في المتصفح يحتاج فتح الموقع عبر HTTPS مرة أولى ونجاح تخزين ملفات Service Worker. اختبر وضع الطيران قبل الاعتماد عليه.
- ملف APK يضم نسخة من الواجهة داخل التطبيق؛ بياناته محلية على الجهاز ولا تنتقل تلقائيًا إلى نسخة الويب.
- كلمة مرور الواجهة المحلية ليست نظام أمان قويًا. هذه نسخة أولية ويجب اختبارها قبل استخدامها في حسابات المحل الحقيقية.
