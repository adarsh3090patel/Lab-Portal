import hashlib
from fastapi import APIRouter, HTTPException, status
from app.database import get_database
from app.models import UserRegisterRequest, UserLoginRequest

router = APIRouter(prefix="/api/auth", tags=["Authentication"])

def hash_password(password: str) -> str:
    return hashlib.sha256(password.encode("utf-8")).hexdigest()

@router.post("/register", status_code=status.HTTP_201_CREATED)
def register(user_data: UserRegisterRequest):
    db = get_database()
    if db is None:
        raise HTTPException(
            status_code=status.HTTP_503_SERVICE_UNAVAILABLE,
            detail="Database connection not ready. Check MongoDB URI."
        )

    users_col = db["users"]

    # Check if user already exists
    existing = users_col.find_one({"email": user_data.email.lower()})
    if existing:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail="An account with this email already exists."
        )

    # Insert new user (Admins are pre-approved, students require admin approval)
    is_admin = user_data.role.lower() == "admin"
    new_user = {
        "fullName": user_data.fullName,
        "email": user_data.email.lower(),
        "rollNo": user_data.rollNo,
        "role": user_data.role,
        "programme": user_data.programme or "B.Sc. (Computer Science)",
        "semester": user_data.semester or "Semester V",
        "phone": user_data.phone or "+91 98765 43210",
        "faceEnrolled": user_data.faceEnrolled,
        "facePhotoUrl": user_data.facePhotoUrl,
        "password": hash_password(user_data.password),
        "isApproved": True if is_admin else False,
        "profileCompletion": 85
    }

    result = users_col.insert_one(new_user)

    return {
        "message": "User registered successfully",
        "isApproved": True if is_admin else False,
        "user": {
            "id": str(result.inserted_id),
            "fullName": user_data.fullName,
            "email": user_data.email.lower(),
            "rollNo": user_data.rollNo,
            "role": user_data.role,
            "programme": user_data.programme or "B.Sc. (Computer Science)",
            "semester": user_data.semester or "Semester V",
            "phone": user_data.phone or "+91 98765 43210",
            "faceEnrolled": user_data.faceEnrolled,
            "facePhotoUrl": user_data.facePhotoUrl,
            "profileCompletion": 85
        }
    }

@router.post("/login")
def login(credentials: UserLoginRequest):
    db = get_database()
    if db is None:
        raise HTTPException(
            status_code=status.HTTP_503_SERVICE_UNAVAILABLE,
            detail="Database connection not ready. Check MongoDB URI."
        )

    users_col = db["users"]

    user = users_col.find_one({
        "email": credentials.email.lower(),
        "password": hash_password(credentials.password)
    })

    if not user:
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail="Invalid email or password."
        )

    # Check approval status for non-admin accounts
    is_admin = user.get("role", "Student").lower() == "admin"
    if not is_admin and user.get("isApproved") is False:
        raise HTTPException(
            status_code=status.HTTP_403_FORBIDDEN,
            detail="Your account is pending admin approval. You will be able to log in once the lab administrator approves your registration."
        )

    return {
        "message": "Login successful",
        "user": {
            "id": str(user["_id"]),
            "fullName": user.get("fullName", ""),
            "email": user["email"],
            "rollNo": user.get("rollNo", ""),
            "role": user.get("role", "Student"),
            "programme": user.get("programme", "B.Sc. (Computer Science)"),
            "semester": user.get("semester", "Semester V"),
            "phone": user.get("phone", "+91 98765 43210"),
            "faceEnrolled": user.get("faceEnrolled", True),
            "facePhotoUrl": user.get("facePhotoUrl", ""),
            "profileCompletion": user.get("profileCompletion", 85)
        }
    }

