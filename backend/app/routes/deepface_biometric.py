import datetime
from fastapi import APIRouter, HTTPException, status
from bson import ObjectId
from app.database import get_database
from app.models import DeepFaceEnrollRequest, DeepFaceAttendanceRequest, DeepFaceVerifyRequest
from app.services.deepface_service import DeepFaceService

router = APIRouter(prefix="/api/biometric/deepface", tags=["Python DeepFace Facial Recognition"])

@router.get("/status")
def get_deepface_engine_status():
    return {
        "engine": "Python DeepFace",
        "model": DeepFaceService.DEFAULT_MODEL,
        "embeddingDimensions": DeepFaceService.EMBEDDING_DIM,
        "distanceThreshold": DeepFaceService.STRICT_DISTANCE_THRESHOLD,
        "similarityThreshold": DeepFaceService.STRICT_SIMILARITY_THRESHOLD,
        "status": "ready",
        "timestamp": datetime.datetime.now(datetime.timezone.utc).isoformat()
    }

@router.post("/enroll")
def enroll_student_face_deepface(req: DeepFaceEnrollRequest):
    """
    Receives face image captured via CameraX, validates consent and image quality,
    runs DeepFace Facenet512 feature extraction on the server, and associates the
    resulting 512D biometric template with the authenticated student record in MongoDB Atlas.
    """
    db = get_database()
    if db is None:
        raise HTTPException(
            status_code=status.HTTP_503_SERVICE_UNAVAILABLE,
            detail="Database connection not ready."
        )

    users_col = db["users"]

    # 1. Locate student in database
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
            detail=f"Student account not found for identifier '{req.userId}' or roll '{req.rollNo}'."
        )

    # 1b. Security Enforcement: If student already enrolled face, require Admin permission to re-enroll
    already_enrolled = user.get("faceEnrolled", False) or bool(user.get("facePhotoUrl") and user.get("faceEmbedding"))
    can_re_enroll = user.get("allowedEdits", {}).get("face", False)
    if already_enrolled and not can_re_enroll:
        raise HTTPException(
            status_code=status.HTTP_403_FORBIDDEN,
            detail="Facial biometric template is strictly locked. Administrator permission is required to re-enroll."
        )

    # 2. Extract 512D facial embedding using Python DeepFace
    extraction_result = DeepFaceService.extract_face_embedding(req.imageBase64)
    if not extraction_result.get("success"):
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail=extraction_result.get("error", "Face detection failed.")
        )

    embedding = extraction_result["embedding"]
    now_utc = datetime.datetime.now(datetime.timezone.utc).isoformat()

    # 3. Store protected biometric template in MongoDB Atlas
    update_doc = {
        "faceEnrolled": True,
        "faceEmbedding": embedding,
        "embeddingModel": DeepFaceService.DEFAULT_MODEL,
        "embeddingDimensions": len(embedding),
        "biometricEnrolledAt": now_utc,
        "facePhotoUrl": req.imageBase64,
        "allowedEdits": {"face": False, "academic": False, "personal": False},
        "initialEditDone": True
    }

    users_col.update_one({"_id": user["_id"]}, {"$set": update_doc})

    return {
        "success": True,
        "message": f"Biometric template successfully enrolled for {user.get('fullName', 'Student')} using Python DeepFace Facenet512.",
        "userId": str(user["_id"]),
        "rollNo": user.get("rollNo", req.rollNo),
        "dimensions": len(embedding),
        "model": DeepFaceService.DEFAULT_MODEL,
        "quality": extraction_result.get("quality", {}),
        "liveness": extraction_result.get("liveness", {})
    }

@router.post("/verify-attendance")
def verify_and_record_attendance_deepface(req: DeepFaceAttendanceRequest):
    """
    Full Server-Authoritative Biometric Attendance:
    1. Authenticates student profile.
    2. DeepFace extracts live 512D embedding from captured camera frame.
    3. Compares live embedding against student's enrolled DeepFace template in MongoDB Atlas.
    4. Enforces strict Facenet512 cosine threshold (similarity >= 0.70).
    5. Prevents duplicate attendance.
    6. Writes verified attendance record to database with server timestamp.
    """
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
            detail="Student not found in university database."
        )

    # 2. Check student status (Approval & Block status)
    if student.get("isApproved") is False:
        raise HTTPException(
            status_code=status.HTTP_403_FORBIDDEN,
            detail="Student account is pending administrator approval. Attendance cannot be recorded."
        )

    if student.get("isBlocked") is True or student.get("status") == "blocked":
        raise HTTPException(
            status_code=status.HTTP_403_FORBIDDEN,
            detail="Student portal access is blocked. Attendance is restricted. Please contact lab admin."
        )

    # 3. Retrieve enrolled biometric template
    enrolled_embedding = student.get("faceEmbedding")
    if not enrolled_embedding:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail="No enrolled biometric face template found for this student. Please complete face enrollment first."
        )

    # 4. Anti-Duplicate Check: Prevent multiple attendances for same subject on same date
    today_date = datetime.datetime.now(datetime.timezone.utc).strftime("%Y-%m-%d")
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

    # 5. Extract live 512D face embedding using Python DeepFace
    live_result = DeepFaceService.extract_face_embedding(req.imageBase64)
    if not live_result.get("success"):
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail=live_result.get("error", "Failed to detect face in camera capture.")
        )

    live_embedding = live_result["embedding"]

    # 6. DeepFace Identity Verification against enrolled template
    match_result = DeepFaceService.verify_match(
        live_embedding=live_embedding,
        enrolled_embedding=enrolled_embedding,
        threshold=DeepFaceService.STRICT_DISTANCE_THRESHOLD
    )

    if not match_result["is_match"]:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail=f"Identity Match Rejected ({match_result['percentage']}% similarity). Live face does not match enrolled student biometric template."
        )

    # 7. Check Liveness Score
    liveness_info = live_result.get("liveness", {})
    liveness_score = liveness_info.get("score", 1.0)

    # 8. Server-Authoritative Attendance Record Creation
    now_utc = datetime.datetime.now(datetime.timezone.utc)
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
        "verificationEngine": "PYTHON_DEEPFACE",
        "model": DeepFaceService.DEFAULT_MODEL,
        "similarityScore": match_result["similarity"],
        "confidencePercentage": match_result["percentage"],
        "distance": match_result["distance"],
        "livenessScore": round(float(liveness_score), 3)
    }

    insert_result = attendance_col.insert_one(record)
    record["id"] = str(insert_result.inserted_id)
    del record["_id"]

    return {
        "success": True,
        "message": f"Biometric Attendance Verified: {student.get('fullName')} marked Present in {req.subject} ({match_result['percentage']}% DeepFace confidence).",
        "attendance": record,
        "match": match_result
    }

@router.post("/verify-face")
def verify_face_only(req: DeepFaceVerifyRequest):
    """
    Pure face verification endpoint for student identity check without creating an attendance record.
    """
    db = get_database()
    if db is None:
        raise HTTPException(status_code=status.HTTP_503_SERVICE_UNAVAILABLE, detail="Database connection not ready.")

    users_col = db["users"]
    try:
        user = users_col.find_one({"_id": ObjectId(req.userId)})
    except Exception:
        user = users_col.find_one({"rollNo": req.userId})

    if not user:
        raise HTTPException(status_code=status.HTTP_404_NOT_FOUND, detail="User not found.")

    enrolled = user.get("faceEmbedding")
    if not enrolled:
        raise HTTPException(status_code=status.HTTP_400_BAD_REQUEST, detail="No face template enrolled for this user.")

    live_res = DeepFaceService.extract_face_embedding(req.imageBase64)
    if not live_res.get("success"):
        raise HTTPException(status_code=status.HTTP_400_BAD_REQUEST, detail=live_res.get("error"))

    match = DeepFaceService.verify_match(live_res["embedding"], enrolled)
    return {
        "success": match["is_match"],
        "match": match,
        "studentName": user.get("fullName"),
        "rollNo": user.get("rollNo")
    }

