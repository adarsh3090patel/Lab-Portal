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

    # Insert new user
    new_user = {
        "fullName": user_data.fullName,
        "email": user_data.email.lower(),
        "rollNo": user_data.rollNo,
        "role": user_data.role,
        "password": hash_password(user_data.password)
    }

    result = users_col.insert_one(new_user)

    return {
        "message": "User registered successfully",
        "user": {
            "id": str(result.inserted_id),
            "fullName": user_data.fullName,
            "email": user_data.email.lower(),
            "rollNo": user_data.rollNo,
            "role": user_data.role
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

    return {
        "message": "Login successful",
        "user": {
            "id": str(user["_id"]),
            "fullName": user.get("fullName", ""),
            "email": user["email"],
            "rollNo": user.get("rollNo", ""),
            "role": user.get("role", "Student")
        }
    }

