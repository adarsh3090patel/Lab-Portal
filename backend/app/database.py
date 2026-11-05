import os
from pymongo import MongoClient
from dotenv import load_dotenv

load_dotenv()

MONGODB_URI = os.getenv("MONGODB_URI", "")
DB_NAME = os.getenv("DB_NAME", "labportal")

client = None
db = None

def get_database():
    global client, db
    if db is not None:
        return db

    if not MONGODB_URI or "<db_username>" in MONGODB_URI:
        print("=" * 60)
        print("⚠️ [ACTION REQUIRED] backend/.env me apna MongoDB Atlas credentials daalein!")
        print("Replace <db_username> and <db_password> with your MongoDB user & password.")
        print("=" * 60)
        return None

    try:
        client = MongoClient(MONGODB_URI, serverSelectionTimeoutMS=5000)
        # Verify connection
        client.admin.command('ping')
        db = client[DB_NAME]
        print(f"[SUCCESS] Connected to MongoDB Atlas database: {DB_NAME}")
        return db
    except Exception as e:
        print(f"[ERROR] Could not connect to MongoDB Atlas: {e}")
        return None

