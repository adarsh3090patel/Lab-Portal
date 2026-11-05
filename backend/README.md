# LabPortal Python Backend (FastAPI + MongoDB Atlas)

Vercel-ready FastAPI backend with support for local mobile app testing over Wi-Fi.

---

## 🛠️ Local Testing Setup (Phone & PC)

### 1. Requirements Install Karein
Terminal me `backend` folder me jayein:
```bash
cd backend
pip install -r requirements.txt
```

### 2. `.env` File Banayein
`backend/.env` file banayein aur apna MongoDB Atlas URI daalein:
```env
MONGODB_URI=mongodb+srv://<username>:<password>@cluster0.mongodb.net/?retryWrites=true&w=majority
DB_NAME=labportal
PORT=8000
```

### 3. Server Start Karein
```bash
python run.py
```
> Server automatically aapke PC ka Local IP display karega (jaise `http://192.168.1.15:8000`), jo hum Android app me daalenge.

---

## 📱 Mobile Phone Testing Instructions
1. Aapka **Laptop** aur **Android Phone** dono **same Wi-Fi hotspot / network** se jude hone chahiye.
2. Android app me `BASE_URL` ko `http://<AAPKA_PC_IP>:8000/` set karenge.

---

## 🌐 Vercel Deployment (Baad ke liye)
1. GitHub par repo push karein.
2. Vercel dashboard par project import karein.
3. **Root Directory**: `backend` select karein.
4. **Environment Variables** me `MONGODB_URI` add karein.
5. Deploy karein! Vercel automatically `api/index.py` ko Serverless function bana dega.

