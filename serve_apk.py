#!/usr/bin/env python3
import http.server
import os
import socketserver

APK_NAME = "forest-quest.apk"
PORT = 8000

class ApkHandler(http.server.SimpleHTTPRequestHandler):
    def do_GET(self):
        if self.path in ("/", f"/{APK_NAME}"):
            apk_path = os.path.join(os.getcwd(), APK_NAME)
            if not os.path.exists(apk_path):
                self.send_error(404, "APK not found")
                return
            self.send_response(200)
            self.send_header("Content-Type", "application/vnd.android.package-archive")
            self.send_header("Content-Disposition", f"attachment; filename={APK_NAME}")
            fs = os.stat(apk_path)
            self.send_header("Content-Length", str(fs.st_size))
            self.end_headers()
            with open(apk_path, "rb") as fh:
                self.wfile.write(fh.read())
        else:
            self.send_error(404, "Not found")

if __name__ == "__main__":
    with socketserver.TCPServer(("0.0.0.0", PORT), ApkHandler) as httpd:
        print(f"Serving {APK_NAME} on port {PORT}")
        httpd.serve_forever()
