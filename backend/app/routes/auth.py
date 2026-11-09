import hashlib
from fastapi import APIRouter, HTTPException, status
from bson import ObjectId
from app.database import get_database
from app.models import UserRegisterRequest, UserLoginRequest, ProfileUpdateRequest

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
        "profilePhotoUrl": user_data.profilePhotoUrl or user_data.facePhotoUrl,
        "faceEnrolled": user_data.faceEnrolled,
        "facePhotoUrl": user_data.facePhotoUrl,
        "faceEmbedding": user_data.faceEmbedding or [],
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
            "profilePhotoUrl": user_data.profilePhotoUrl or user_data.facePhotoUrl,
            "faceEnrolled": user_data.faceEnrolled,
            "facePhotoUrl": user_data.facePhotoUrl,
            "faceEmbedding": user_data.faceEmbedding or [],
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
            "collegeName": user.get("collegeName", "Maharishi Mahesh Yogi Ramayan University Ayodhya"),
            "programme": user.get("programme", "B.Sc. (Computer Science)"),
            "semester": user.get("semester", "Semester V"),
            "department": user.get("department", "Computer Science & Engineering"),
            "academicYear": user.get("academicYear", "2024 - 2028"),
            "phone": user.get("phone", "+91 98765 43210"),
            "dob": user.get("dob", ""),
            "bloodGroup": user.get("bloodGroup", ""),
            "permanentAddress": user.get("permanentAddress", ""),
            "guardianName": user.get("guardianName", ""),
            "guardianPhone": user.get("guardianPhone", ""),
            "profilePhotoUrl": user.get("profilePhotoUrl", user.get("facePhotoUrl", "")),
            "faceEnrolled": user.get("faceEnrolled", True),
            "facePhotoUrl": user.get("facePhotoUrl", ""),
            "faceEmbedding": user.get("faceEmbedding", []),
            "profileCompletion": user.get("profileCompletion", 85)
        }
    }

@router.put("/profile/{user_id}")
def update_profile(user_id: str, req: ProfileUpdateRequest):
    db = get_database()
    if db is None:
        raise HTTPException(status_code=status.HTTP_503_SERVICE_UNAVAILABLE, detail="Database connection not ready.")
    users_col = db["users"]
    try:
        obj_id = ObjectId(user_id)
    except Exception:
        raise HTTPException(status_code=status.HTTP_400_BAD_REQUEST, detail="Invalid user ID format.")

    update_fields = {}
    if req.fullName is not None: update_fields["fullName"] = req.fullName
    if req.phone is not None: update_fields["phone"] = req.phone
    if req.department is not None: update_fields["department"] = req.department
    if req.programme is not None: update_fields["programme"] = req.programme
    if req.semester is not None: update_fields["semester"] = req.semester
    if req.academicYear is not None: update_fields["academicYear"] = req.academicYear
    if req.collegeName is not None: update_fields["collegeName"] = req.collegeName
    if req.dob is not None: update_fields["dob"] = req.dob
    if req.bloodGroup is not None: update_fields["bloodGroup"] = req.bloodGroup
    if req.permanentAddress is not None: update_fields["permanentAddress"] = req.permanentAddress
    if req.guardianName is not None: update_fields["guardianName"] = req.guardianName
    if req.guardianPhone is not None: update_fields["guardianPhone"] = req.guardianPhone
    if req.profilePhotoUrl is not None and req.profilePhotoUrl.strip():
        update_fields["profilePhotoUrl"] = req.profilePhotoUrl
    if req.facePhotoUrl is not None and req.facePhotoUrl.strip():
        update_fields["facePhotoUrl"] = req.facePhotoUrl
        update_fields["faceEnrolled"] = True
    if req.faceEmbedding is not None and len(req.faceEmbedding) >= 128:
        update_fields["faceEmbedding"] = req.faceEmbedding
        update_fields["faceEnrolled"] = True

    # If personal details like dob, bloodGroup, permanentAddress are filled, profile hits 100%!
    is_complete = bool(
        (req.dob or "").strip() and
        (req.bloodGroup or "").strip() and
        (req.permanentAddress or "").strip()
    )
    update_fields["profileCompletion"] = 100 if is_complete else 85

    res = users_col.update_one({"_id": obj_id}, {"$set": update_fields})
    updated_user = users_col.find_one({"_id": obj_id})
    if not updated_user:
        raise HTTPException(status_code=status.HTTP_404_NOT_FOUND, detail="User not found.")

    return {
        "message": "Profile updated successfully",
        "user": {
            "id": str(updated_user["_id"]),
            "fullName": updated_user.get("fullName", ""),
            "email": updated_user["email"],
            "rollNo": updated_user.get("rollNo", ""),
            "role": updated_user.get("role", "Student"),
            "collegeName": updated_user.get("collegeName", "Maharishi Mahesh Yogi Ramayan University Ayodhya"),
            "programme": updated_user.get("programme", "B.Sc. (Computer Science)"),
            "semester": updated_user.get("semester", "Semester V"),
            "department": updated_user.get("department", "Computer Science & Engineering"),
            "academicYear": updated_user.get("academicYear", "2024 - 2028"),
            "phone": updated_user.get("phone", "+91 98765 43210"),
            "dob": updated_user.get("dob", ""),
            "bloodGroup": updated_user.get("bloodGroup", ""),
            "permanentAddress": updated_user.get("permanentAddress", ""),
            "guardianName": updated_user.get("guardianName", ""),
            "guardianPhone": updated_user.get("guardianPhone", ""),
            "profilePhotoUrl": updated_user.get("profilePhotoUrl", updated_user.get("facePhotoUrl", "")),
            "faceEnrolled": updated_user.get("faceEnrolled", True),
            "facePhotoUrl": updated_user.get("facePhotoUrl", ""),
            "faceEmbedding": updated_user.get("faceEmbedding", []),
            "profileCompletion": updated_user.get("profileCompletion", 100 if is_complete else 85)
        }
    }

