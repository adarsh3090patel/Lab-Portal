import os
import hashlib
from pymongo import MongoClient
from dotenv import load_dotenv

load_dotenv()

MONGODB_URI = os.getenv("MONGODB_URI", "")
DB_NAME = os.getenv("DB_NAME", "labportal")

def hash_password(password: str) -> str:
    return hashlib.sha256(password.encode("utf-8")).hexdigest()

demo_users = [
    {
        "fullName": "MMYRV Lab Admin",
        "email": "admin@mmyrv.edu.in",
        "rollNo": "STAFF-ADM-01",
        "role": "Admin",
        "password": hash_password("admin123")
    },
    {
        "fullName": "Student User",
        "email": "student@mmyrv.edu.in",
        "rollNo": "MMYRV/2026/042",
        "role": "Student",
        "password": hash_password("student123")
    }
]

def seed_database():
    if not MONGODB_URI or "<db_username>" in MONGODB_URI:
        print("=" * 60)
        print("⚠️ [NOTE]: backend/.env me apna MongoDB username aur password dalein.")
        print("Example: mongodb+srv://adarsh:myPassword123@cluster0.plvgasl.mongodb.net/labportal...")
        print("=" * 60)
        return

    try:
        client = MongoClient(MONGODB_URI, serverSelectionTimeoutMS=5000)
        db = client[DB_NAME]
        users_col = db["users"]

        for user in demo_users:
            existing = users_col.find_one({"email": user["email"]})
            if existing:
                users_col.update_one(
                    {"email": user["email"]},
                    {"$set": user}
                )
                print(f"✅ Updated user: {user['email']} (Role: {user['role']})")
            else:
                users_col.insert_one(user)
                print(f"🎉 Created user: {user['email']} (Role: {user['role']})")

        print("=" * 60)
        print("✨ Demo users seeded successfully into MongoDB Atlas!")
        print("1. Admin:   admin@mmyrv.edu.in   | Password: admin123")
        print("2. Student: student@mmyrv.edu.in | Password: student123")
        print("=" * 60)

    except Exception as e:
        print(f"❌ Error connecting/seeding MongoDB: {e}")

if __name__ == "__main__":
    seed_database()

