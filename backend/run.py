import uvicorn
import socket

def get_local_ip():
    try:
        s = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
        s.connect(("8.8.8.8", 80))
        ip = s.getsockname()[0]
        s.close()
        return ip
    except Exception:
        return "127.0.0.1"

if __name__ == "__main__":
    local_ip = get_local_ip()
    port = 8000
    print("=" * 60)
    print("🚀 LabPortal Python Backend Server Starting...")
    print(f"💻 Local PC URL:       http://localhost:{port}")
    print(f"📱 Mobile Phone URL:   http://{local_ip}:{port}")
    print(f"📄 Swagger API Docs:   http://localhost:{port}/docs")
    print("=" * 60)
    print("💡 NOTE: Phone aur PC dono same Wi-Fi pe hone chahiye!")
    print("=" * 60)

    uvicorn.run("app.main:app", host="0.0.0.0", port=port, reload=True)

