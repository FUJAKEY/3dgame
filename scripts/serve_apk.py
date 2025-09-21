#!/usr/bin/env python3
import http.server
import os
import shutil
import socketserver

DEFAULT_APK_CANDIDATES = (
    "app/build/outputs/apk/release/app-release.apk",
    "app/build/outputs/apk/debug/app-debug.apk",
    "app/build/outputs/apk/release/app-release-unsigned.apk",
)


def resolve_apk_path():
    override = os.environ.get("APK_PATH")
    if override:
        return override
    for candidate in DEFAULT_APK_CANDIDATES:
        if os.path.exists(candidate):
            return candidate
    return DEFAULT_APK_CANDIDATES[0]


FILENAME = os.environ.get("APK_NAME", "ForestAdventure.apk")

class ApkRequestHandler(http.server.BaseHTTPRequestHandler):
    def do_GET(self):
        if self.path not in ("/", "/index.html"):
            self.send_error(404, "Not Found")
            return

        apk_path = resolve_apk_path()
        if not os.path.exists(apk_path):
            self.send_error(404, "APK not found")
            return

        self.send_response(200)
        self.send_header("Content-Type", "application/vnd.android.package-archive")
        self.send_header("Content-Disposition", f"attachment; filename={FILENAME}")
        self.send_header("Content-Length", str(os.path.getsize(apk_path)))
        self.end_headers()

        with open(apk_path, "rb") as apk_file:
            shutil.copyfileobj(apk_file, self.wfile)

    def log_message(self, format, *args):
        return


def main():
    port = int(os.environ.get("PORT", "8000"))
    with socketserver.TCPServer(("0.0.0.0", port), ApkRequestHandler) as httpd:
        httpd.serve_forever()


if __name__ == "__main__":
    main()
