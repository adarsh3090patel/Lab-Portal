import datetime
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
    is_blocked = user.get("isBlocked", False) or user.get("status") == "blocked"

    if not is_admin and is_blocked:
        raise HTTPException(
            status_code=status.HTTP_403_FORBIDDEN,
            detail="ACCOUNT_BLOCKED: Your account has been suspended/blocked by the administrator."
        )

    if not is_admin and user.get("isApproved") is False:
        raise HTTPException(
            status_code=status.HTTP_403_FORBIDDEN,
            detail="Your account is pending admin approval. You will be able to log in once the lab administrator approves your registration."
        )

    allowed_edits = user.get("allowedEdits", {"face": False, "academic": False, "personal": False})

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
            "profileCompletion": user.get("profileCompletion", 85),
            "isBlocked": is_blocked,
            "status": "blocked" if is_blocked else user.get("status", "approved"),
            "allowedEdits": allowed_edits,
            "initialEditDone": user.get("initialEditDone", False)
        }
    }

@router.put("/profile/{user_id}")
def find_user_by_identifier(identifier: str, users_col):
    if not identifier:
        return None
    user = None
    try:
        user = users_col.find_one({"_id": ObjectId(identifier)})
    except Exception:
        pass
    if not user:
        user = users_col.find_one({"rollNo": identifier})
    if not user:
        user = users_col.find_one({"email": identifier.lower()})
    return user

@router.put("/profile/{user_id}")
def update_profile(user_id: str, req: ProfileUpdateRequest):
    db = get_database()
    if db is None:
        raise HTTPException(status_code=status.HTTP_503_SERVICE_UNAVAILABLE, detail="Database connection not ready.")
    users_col = db["users"]
    
    old_user = find_user_by_identifier(user_id, users_col)
    if not old_user:
        raise HTTPException(status_code=status.HTTP_404_NOT_FOUND, detail="User not found.")
    obj_id = old_user["_id"]

    diffs = []
    def check_diff(field_label, old_val, new_val):
        o_clean = str(old_val or "").strip()
        n_clean = str(new_val or "").strip()
        if n_clean and o_clean != n_clean:
            diffs.append({
                "field": field_label,
                "oldValue": o_clean if o_clean else "(Not Set)",
                "newValue": n_clean
            })

    update_fields = {}
    personal_changed = False
    academic_changed = False
    face_changed = False

    if req.fullName is not None:
        check_diff("Full Name", old_user.get("fullName"), req.fullName)
        update_fields["fullName"] = req.fullName
        personal_changed = True
    if req.phone is not None:
        check_diff("Phone", old_user.get("phone"), req.phone)
        update_fields["phone"] = req.phone
        personal_changed = True
    if req.department is not None:
        check_diff("Department", old_user.get("department"), req.department)
        update_fields["department"] = req.department
        academic_changed = True
    if req.programme is not None:
        check_diff("Programme", old_user.get("programme"), req.programme)
        update_fields["programme"] = req.programme
        academic_changed = True
    if req.semester is not None:
        check_diff("Semester", old_user.get("semester"), req.semester)
        update_fields["semester"] = req.semester
        academic_changed = True
    if req.academicYear is not None:
        check_diff("Academic Year", old_user.get("academicYear"), req.academicYear)
        update_fields["academicYear"] = req.academicYear
        academic_changed = True
    if req.collegeName is not None:
        check_diff("College Name", old_user.get("collegeName"), req.collegeName)
        update_fields["collegeName"] = req.collegeName
        academic_changed = True
    if req.dob is not None:
        check_diff("Date of Birth", old_user.get("dob"), req.dob)
        update_fields["dob"] = req.dob
        personal_changed = True
    if req.bloodGroup is not None:
        check_diff("Blood Group", old_user.get("bloodGroup"), req.bloodGroup)
        update_fields["bloodGroup"] = req.bloodGroup
        personal_changed = True
    if req.permanentAddress is not None:
        check_diff("Permanent Address", old_user.get("permanentAddress"), req.permanentAddress)
        update_fields["permanentAddress"] = req.permanentAddress
        personal_changed = True
    if req.guardianName is not None:
        check_diff("Guardian Name", old_user.get("guardianName"), req.guardianName)
        update_fields["guardianName"] = req.guardianName
        personal_changed = True
    if req.guardianPhone is not None:
        check_diff("Guardian Phone", old_user.get("guardianPhone"), req.guardianPhone)
        update_fields["guardianPhone"] = req.guardianPhone
        personal_changed = True
    if req.profilePhotoUrl is not None and req.profilePhotoUrl.strip():
        diffs.append({"field": "Profile Avatar", "oldValue": "Previous Avatar", "newValue": "Updated Profile Photo"})
        update_fields["profilePhotoUrl"] = req.profilePhotoUrl
        personal_changed = True
    if req.facePhotoUrl is not None and req.facePhotoUrl.strip():
        diffs.append({"field": "Facial Biometric Photo", "oldValue": "Previous Biometric Scan", "newValue": "Updated Biometric Face"})
        update_fields["facePhotoUrl"] = req.facePhotoUrl
        update_fields["faceEnrolled"] = True
        face_changed = True
    if req.faceEmbedding is not None and len(req.faceEmbedding) >= 128:
        diffs.append({"field": "Biometric 192D Embedding", "oldValue": "Initial Enrolled Template", "newValue": "New 192D Mathematical Template"})
        update_fields["faceEmbedding"] = req.faceEmbedding
        update_fields["faceEnrolled"] = True
        face_changed = True

    # Check completeness
    is_complete = bool(
        (req.dob or old_user.get("dob") or "").strip() and
        (req.bloodGroup or old_user.get("bloodGroup") or "").strip() and
        (req.permanentAddress or old_user.get("permanentAddress") or "").strip()
    )
    update_fields["profileCompletion"] = 100 if is_complete else 85
    
    # 1-TIME EDIT RULE: Whenever ANY update is submitted by the student,
    # immediately lock all permissions and mark initialEditDone = True!
    # The student gets strictly 1 single edit session per admin approval.
    # It locks automatically so the admin never has to manually lock it.
    is_initial = not old_user.get("initialEditDone", False)
    update_fields["initialEditDone"] = True
    update_fields["allowedEdits"] = {"face": False, "academic": False, "personal": False}

    users_col.update_one({"_id": obj_id}, {"$set": update_fields})
    updated_user = users_col.find_one({"_id": obj_id})
    if not updated_user:
        raise HTTPException(status_code=status.HTTP_404_NOT_FOUND, detail="User not found.")

    # Record in Edit History audit log with exact details
    if diffs:
        db["edit_history"].insert_one({
            "studentId": str(obj_id),
            "studentName": updated_user.get("fullName", ""),
            "rollNo": updated_user.get("rollNo", ""),
            "timestamp": datetime.datetime.now(datetime.timezone.utc).isoformat(),
            "changes": diffs,
            "type": "Initial Profile Setup" if is_initial else "Admin-Authorized 1-Time Update"
        })

    is_blocked = updated_user.get("isBlocked", False) or updated_user.get("status") == "blocked"

    return {
        "message": "Profile updated successfully. Changes locked under university policy.",
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
            "profileCompletion": updated_user.get("profileCompletion", 85),
            "isBlocked": is_blocked,
            "status": "blocked" if is_blocked else updated_user.get("status", "approved"),
            "allowedEdits": {"face": False, "academic": False, "personal": False},
            "initialEditDone": True
        }
    }

@router.get("/status/{user_id}")
def get_user_status(user_id: str):
    db = get_database()
    if db is None:
        raise HTTPException(status_code=status.HTTP_503_SERVICE_UNAVAILABLE, detail="Database connection not ready.")
    users_col = db["users"]
    user = find_user_by_identifier(user_id, users_col)
    if not user:
        raise HTTPException(status_code=status.HTTP_404_NOT_FOUND, detail="User not found.")

    is_blocked = user.get("isBlocked", False) or user.get("status") == "blocked"
    return {
        "isBlocked": is_blocked,
        "isApproved": user.get("isApproved", True),
        "status": "blocked" if is_blocked else user.get("status", "approved"),
        "allowedEdits": user.get("allowedEdits", {"face": False, "academic": False, "personal": False}),
        "initialEditDone": user.get("initialEditDone", False)
    }

