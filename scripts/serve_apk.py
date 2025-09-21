#!/usr/bin/env python3
import http.server
import os
import shutil
import socketserver

APK_PATH = os.environ.get("APK_PATH", "app/build/outputs/apk/release/app-release-unsigned.apk")
FILENAME = os.environ.get("APK_NAME", "ForestAdventure.apk")

class ApkRequestHandler(http.server.BaseHTTPRequestHandler):
    def do_GET(self):
        if self.path not in ("/", "/index.html"):
            self.send_error(404, "Not Found")
            return

        if not os.path.exists(APK_PATH):
            self.send_error(404, "APK not found")
            return

        self.send_response(200)
        self.send_header("Content-Type", "application/vnd.android.package-archive")
        self.send_header("Content-Disposition", f"attachment; filename={FILENAME}")
        self.send_header("Content-Length", str(os.path.getsize(APK_PATH)))
        self.end_headers()

        with open(APK_PATH, "rb") as apk_file:
            shutil.copyfileobj(apk_file, self.wfile)

    def log_message(self, format, *args):
        return


def main():
    port = int(os.environ.get("PORT", "8000"))
    with socketserver.TCPServer(("0.0.0.0", port), ApkRequestHandler) as httpd:
        httpd.serve_forever()


if __name__ == "__main__":
    main()
