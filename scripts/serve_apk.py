#!/usr/bin/env python3
import http.server
import os
import socketserver
import sys

PORT = 8000


def create_handler(apk_path: str):
    apk_path = os.path.abspath(apk_path)

    class ApkRequestHandler(http.server.BaseHTTPRequestHandler):
        def do_GET(self):
            if self.path not in ("/", "/index.html", f"/{os.path.basename(apk_path)}"):
                self.send_error(404, "Not Found")
                return
            if not os.path.exists(apk_path):
                self.send_error(500, "APK not found")
                return
            self.send_response(200)
            self.send_header("Content-Type", "application/vnd.android.package-archive")
            self.send_header(
                "Content-Disposition",
                f"attachment; filename={os.path.basename(apk_path)}",
            )
            self.send_header("Content-Length", str(os.path.getsize(apk_path)))
            self.end_headers()
            with open(apk_path, "rb") as apk_file:
                self.wfile.write(apk_file.read())

        def log_message(self, format, *args):
            sys.stdout.write("%s - - [%s] %s
" % (self.address_string(), self.log_date_time_string(), format % args))

    return ApkRequestHandler


def main():
    if len(sys.argv) != 2:
        print("Usage: serve_apk.py <apk_path>")
        sys.exit(1)
    apk_path = sys.argv[1]
    handler_cls = create_handler(apk_path)
    with socketserver.TCPServer(("0.0.0.0", PORT), handler_cls) as httpd:
        print(f"Serving {os.path.abspath(apk_path)} on http://0.0.0.0:{PORT}/")
        httpd.serve_forever()


if __name__ == "__main__":
    main()
