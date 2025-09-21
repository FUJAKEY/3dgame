#!/usr/bin/env python3
import http.server
import os
import pathlib
import socketserver

PORT = int(os.environ.get("APK_SERVER_PORT", "8000"))
APK_DIR = pathlib.Path(os.environ.get("APK_DIR", "app/build/outputs/apk/release"))

apk_files = sorted(APK_DIR.glob("*.apk"))
if not apk_files:
    raise SystemExit(f"APK not found in {APK_DIR}. Build the project before starting the server.")

APK_NAME = apk_files[0].name
APK_PATH = apk_files[0]

class ApkRequestHandler(http.server.SimpleHTTPRequestHandler):
    def do_GET(self):
        if self.path in ("/", "/index.html"):
            self.send_response(200)
            self.send_header("Content-Type", "application/vnd.android.package-archive")
            self.send_header("Content-Disposition", f"attachment; filename={APK_NAME}")
            self.send_header("Content-Length", str(APK_PATH.stat().st_size))
            self.end_headers()
            with open(APK_PATH, "rb") as apk_file:
                self.wfile.write(apk_file.read())
        else:
            self.send_response(404)
            self.end_headers()

    def log_message(self, format, *args):
        return

if __name__ == "__main__":
    handler = ApkRequestHandler
    with socketserver.TCPServer(("0.0.0.0", PORT), handler) as httpd:
        print(f"Serving {APK_NAME} on port {PORT}. URL: http://0.0.0.0:{PORT}/")
        httpd.serve_forever()
