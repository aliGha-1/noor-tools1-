/* Noor Tools offline app shell. Bump CACHE_NAME whenever app files change. */
const CACHE_NAME = "noor-tools-offline-v3";
const APP_SHELL = ["./", "./index.html", "./manifest.json", "./icon.svg"];

self.addEventListener("install", (event) => {
  event.waitUntil((async () => {
    const cache = await caches.open(CACHE_NAME);
    await cache.addAll(APP_SHELL);
    await self.skipWaiting();
  })());
});

self.addEventListener("activate", (event) => {
  event.waitUntil((async () => {
    const keys = await caches.keys();
    await Promise.all(keys.filter((key) => key.startsWith("noor-tools-") && key !== CACHE_NAME)
      .map((key) => caches.delete(key)));
    await self.clients.claim();
  })());
});

self.addEventListener("fetch", (event) => {
  const request = event.request;
  if (request.method !== "GET") return;
  const url = new URL(request.url);
  if (url.origin !== self.location.origin) return;

  event.respondWith((async () => {
    const cache = await caches.open(CACHE_NAME);
    const cached = await cache.match(request) || await cache.match("./index.html");

    // The app shell is served from cache first so it opens without internet.
    if (cached) {
      event.waitUntil(fetch(request).then((response) => {
        if (response && response.ok) cache.put(request, response.clone());
      }).catch(() => {}));
      return cached;
    }

    try {
      const response = await fetch(request);
      if (response && response.ok) cache.put(request, response.clone());
      return response;
    } catch (error) {
      if (request.mode === "navigate") {
        const fallback = await cache.match("./index.html");
        if (fallback) return fallback;
      }
      return new Response("أنت غير متصل بالإنترنت. افتح نور تولز بعد تثبيته أول مرة.", {
        status: 503, headers: { "Content-Type": "text/plain; charset=utf-8" }
      });
    }
  })());
});
