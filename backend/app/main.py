from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware
from app.database import get_database
from app.routes.auth import router as auth_router
from app.routes.admin import router as admin_router

app = FastAPI(
    title="MMYRV LabPortal API",
    description="Backend API for Maharishi Mahesh Yogi Ramayan University Lab Portal",
    version="1.0.0"
)

# Enable CORS for mobile app & web clients
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# Include Routers
app.include_router(auth_router)
app.include_router(admin_router)

@app.on_event("startup")
def startup_db_client():
    db = get_database()
    if db is not None:
        try:
            import hashlib
            users_col = db["users"]
            def hp(p): return hashlib.sha256(p.encode("utf-8")).hexdigest()
            # Ensure demo Admin
            if not users_col.find_one({"email": "admin@mmyrv.edu.in"}):
                users_col.insert_one({
                    "fullName": "MMYRV Lab Admin",
                    "email": "admin@mmyrv.edu.in",
                    "rollNo": "STAFF-ADM-01",
                    "role": "Admin",
                    "password": hp("admin123"),
                    "isApproved": True
                })
                print("[INIT] Created demo Admin user")
            # Ensure demo Student
            if not users_col.find_one({"email": "student@mmyrv.edu.in"}):
                users_col.insert_one({
                    "fullName": "Student User",
                    "email": "student@mmyrv.edu.in",
                    "rollNo": "MMYRV/2026/042",
                    "role": "Student",
                    "password": hp("student123"),
                    "isApproved": True
                })
                print("[INIT] Created demo Student user")
        except Exception as e:
            print("[INIT] Error auto-seeding demo users:", e)

@app.get("/")
def read_root():
    return {
        "status": "online",
        "service": "MMYRV LabPortal Python API",
        "message": "API is running smoothly"
    }

@app.get("/health")
def health_check():
    db = get_database()
    db_status = "connected" if db is not None else "disconnected (check MONGODB_URI)"
    return {
        "status": "ok",
        "database": db_status
    }
