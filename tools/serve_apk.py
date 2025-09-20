#!/usr/bin/env python3
import os
import socketserver
from http import HTTPStatus
from http.server import SimpleHTTPRequestHandler

APK_PATH = os.environ.get("APK_PATH", "/app/ForestCollect-debug.apk")
APK_FILENAME = os.environ.get("APK_FILENAME", "ForestCollect-debug.apk")


class ApkRequestHandler(SimpleHTTPRequestHandler):
    def do_GET(self):
        if self.path not in ("/", "/index.html"):
            self.send_error(HTTPStatus.NOT_FOUND, "Only the root path is available")
            return

        if not os.path.exists(APK_PATH):
            self.send_error(HTTPStatus.INTERNAL_SERVER_ERROR, "APK file is missing on the server")
            return

        file_size = os.path.getsize(APK_PATH)
        self.send_response(HTTPStatus.OK)
        self.send_header("Content-Type", "application/vnd.android.package-archive")
        self.send_header("Content-Disposition", f"attachment; filename=\"{APK_FILENAME}\"")
        self.send_header("Content-Length", str(file_size))
        self.end_headers()

        with open(APK_PATH, "rb") as apk_file:
            while True:
                chunk = apk_file.read(1024 * 64)
                if not chunk:
                    break
                self.wfile.write(chunk)

    def log_message(self, format, *args):
        return


def main():
    port = int(os.environ.get("PORT", "8000"))
    socketserver.TCPServer.allow_reuse_address = True
    with socketserver.TCPServer(("0.0.0.0", port), ApkRequestHandler) as server:
        print(f"APK server ready on port {port}. Serving {APK_PATH} as {APK_FILENAME}.")
        try:
            server.serve_forever()
        except KeyboardInterrupt:
            print("Shutdown requested, terminating server.")


if __name__ == "__main__":
    main()
