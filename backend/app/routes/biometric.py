import datetime
from fastapi import APIRouter, HTTPException, status
from bson import ObjectId
from app.database import get_database
from app.models import BiometricEnrollRequest, AttendanceVerificationRequest, BiometricRevokeRequest

router = APIRouter(prefix="/api/biometric", tags=["AI Biometric & Attendance"])

@router.post("/enroll")
def enroll_biometric_template(req: BiometricEnrollRequest):
    db = get_database()
    if db is None:
        raise HTTPException(
            status_code=status.HTTP_503_SERVICE_UNAVAILABLE,
            detail="Database connection not ready."
        )

    users_col = db["users"]

    # Validate embedding dimensions (MobileFaceNet produces 192 features)
    if not req.faceEmbedding or len(req.faceEmbedding) < 128:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail=f"Invalid biometric embedding dimension. Expected 192 features, got {len(req.faceEmbedding) if req.faceEmbedding else 0}."
        )

    # Locate user by ObjectId or Roll Number
    query = None
    try:
        query = {"_id": ObjectId(req.userId)}
    except Exception:
        query = {"rollNo": req.userId}

    user = users_col.find_one(query)
    if not user:
        user = users_col.find_one({"rollNo": req.rollNo})

    if not user:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail=f"User with identifier '{req.userId}' or roll '{req.rollNo}' not found."
        )

    update_doc = {
        "faceEnrolled": True,
        "faceEmbedding": req.faceEmbedding,
        "biometricEnrolledAt": datetime.datetime.utcnow().isoformat()
    }
    if req.facePhotoUrl:
        update_doc["facePhotoUrl"] = req.facePhotoUrl

    users_col.update_one({"_id": user["_id"]}, {"$set": update_doc})

    return {
        "success": True,
        "message": f"Biometric 192D facial embedding enrolled successfully for {user.get('fullName', 'Student')}.",
        "userId": str(user["_id"]),
        "dimensions": len(req.faceEmbedding)
    }

@router.get("/template/{user_id}")
def get_biometric_template(user_id: str):
    db = get_database()
    if db is None:
        raise HTTPException(
            status_code=status.HTTP_503_SERVICE_UNAVAILABLE,
            detail="Database connection not ready."
        )

    users_col = db["users"]
    try:
        user = users_col.find_one({"_id": ObjectId(user_id)})
    except Exception:
        user = users_col.find_one({"rollNo": user_id})

    if not user:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="User not found."
        )

    embedding = user.get("faceEmbedding")
    if not embedding:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="No biometric face template enrolled for this user."
        )

    return {
        "success": True,
        "userId": str(user["_id"]),
        "rollNo": user.get("rollNo", ""),
        "fullName": user.get("fullName", ""),
        "faceEnrolled": True,
        "faceEmbedding": embedding
    }

@router.post("/verify-attendance")
def verify_and_record_attendance(req: AttendanceVerificationRequest):
    db = get_database()
    if db is None:
        raise HTTPException(
            status_code=status.HTTP_503_SERVICE_UNAVAILABLE,
            detail="Database connection not ready."
        )

    users_col = db["users"]
    attendance_col = db["attendance"]

    # 1. Locate student
    try:
        student = users_col.find_one({"_id": ObjectId(req.studentId)})
    except Exception:
        student = users_col.find_one({"rollNo": req.rollNo})

    if not student:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="Student not found in registered university database."
        )

    # 2. Check approval & enrollment status
    if student.get("isApproved") is False:
        raise HTTPException(
            status_code=status.HTTP_403_FORBIDDEN,
            detail="Student account is pending administrator approval. Attendance cannot be recorded."
        )

    # 3. Check matching confidence threshold (Strict MobileFaceNet threshold: >= 0.70)
    if req.similarityScore < 0.70:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail=f"Biometric similarity score ({int(req.similarityScore * 100)}%) is below the security threshold (70%). Identity match rejected."
        )

    # 4. Anti-Duplicate Check: Prevent multiple attendance records for the same subject on the same day
    today_date = datetime.datetime.utcnow().strftime("%Y-%m-%d")
    existing_record = attendance_col.find_one({
        "studentId": str(student["_id"]),
        "subject": req.subject,
        "date": today_date
    })

    if existing_record:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail=f"Attendance already recorded today for {req.subject} at {existing_record.get('timestamp')}. Duplicate attendance is rejected."
        )

    # 5. Server-Authoritative Timestamp
    now_utc = datetime.datetime.utcnow()
    record = {
        "studentId": str(student["_id"]),
        "rollNo": student.get("rollNo", req.rollNo),
        "studentName": student.get("fullName", "Student"),
        "programme": student.get("programme", "B.Sc. (Computer Science)"),
        "semester": student.get("semester", "Semester V"),
        "subject": req.subject,
        "date": today_date,
        "timestamp": now_utc.isoformat(),
        "status": "Present",
        "verificationMethod": "AI_MOBILEFACENET_TFLITE",
        "similarityScore": round(float(req.similarityScore), 4),
        "livenessScore": round(float(req.livenessScore), 4)
    }

    insert_result = attendance_col.insert_one(record)
    record["id"] = str(insert_result.inserted_id)
    del record["_id"]

    return {
        "success": True,
        "message": f"Biometric Attendance Verified: {student.get('fullName')} marked Present in {req.subject}.",
        "attendance": record
    }

@router.get("/attendance/student/{student_id}")
def get_student_attendance_history(student_id: str):
    db = get_database()
    if db is None:
        raise HTTPException(
            status_code=status.HTTP_503_SERVICE_UNAVAILABLE,
            detail="Database connection not ready."
        )

    attendance_col = db["attendance"]

    # Match by studentId or rollNo
    query = {"$or": [{"studentId": student_id}, {"rollNo": student_id}]}
    records = list(attendance_col.find(query).sort("timestamp", -1))

    results = []
    for r in records:
        results.append({
            "id": str(r["_id"]),
            "studentId": r.get("studentId", ""),
            "rollNo": r.get("rollNo", ""),
            "studentName": r.get("studentName", ""),
            "subject": r.get("subject", ""),
            "date": r.get("date", ""),
            "timestamp": r.get("timestamp", ""),
            "status": r.get("status", "Present"),
            "similarityScore": r.get("similarityScore", 0.0)
        })

    return {
        "success": True,
        "count": len(results),
        "records": results
    }

@router.post("/revoke/{user_id}")
def revoke_biometric_enrollment(user_id: str):
    db = get_database()
    if db is None:
        raise HTTPException(
            status_code=status.HTTP_503_SERVICE_UNAVAILABLE,
            detail="Database connection not ready."
        )

    users_col = db["users"]
    try:
        user = users_col.find_one({"_id": ObjectId(user_id)})
    except Exception:
        user = users_col.find_one({"rollNo": user_id})

    if not user:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="User not found."
        )

    users_col.update_one(
        {"_id": user["_id"]},
        {"$set": {"faceEnrolled": False, "faceEmbedding": None}}
    )

    return {
        "success": True,
        "message": f"Biometric enrollment revoked for {user.get('fullName', 'User')}. Re-enrollment required."
    }

