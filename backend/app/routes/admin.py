from fastapi import APIRouter, HTTPException, status
from bson import ObjectId
from app.database import get_database

router = APIRouter(prefix="/api/admin", tags=["Admin Operations"])

@router.get("/students")
def get_all_students_for_approval():
    db = get_database()
    if db is None:
        raise HTTPException(
            status_code=status.HTTP_503_SERVICE_UNAVAILABLE,
            detail="Database connection not ready."
        )
    users_col = db["users"]
    students = list(users_col.find({"role": {"$ne": "Admin"}}).sort("_id", -1))
    
    result = []
    for s in students:
        result.append({
            "id": str(s["_id"]),
            "fullName": s.get("fullName", ""),
            "email": s.get("email", ""),
            "rollNo": s.get("rollNo", ""),
            "role": s.get("role", "Student"),
            "programme": s.get("programme", "B.Sc. (Computer Science)"),
            "semester": s.get("semester", "Semester V"),
            "phone": s.get("phone", "+91 98765 43210"),
            "faceEnrolled": s.get("faceEnrolled", True),
            "facePhotoUrl": s.get("facePhotoUrl", ""),
            "isApproved": s.get("isApproved", False),
            "createdAt": s.get("createdAt", "Recent")
        })
    return result

@router.post("/approve-student/{user_id}")
def approve_student(user_id: str):
    db = get_database()
    if db is None:
        raise HTTPException(status_code=status.HTTP_503_SERVICE_UNAVAILABLE, detail="Database connection not ready.")
    users_col = db["users"]
    try:
        obj_id = ObjectId(user_id)
    except Exception:
        raise HTTPException(status_code=status.HTTP_400_BAD_REQUEST, detail="Invalid user ID format.")

    res = users_col.update_one({"_id": obj_id}, {"$set": {"isApproved": True}})
    if res.matched_count == 0:
        raise HTTPException(status_code=status.HTTP_404_NOT_FOUND, detail="Student not found.")
    
    return {"message": "Student approved successfully", "success": True}

@router.post("/reject-student/{user_id}")
def reject_student(user_id: str):
    db = get_database()
    if db is None:
        raise HTTPException(status_code=status.HTTP_503_SERVICE_UNAVAILABLE, detail="Database connection not ready.")
    users_col = db["users"]
    try:
        obj_id = ObjectId(user_id)
    except Exception:
        raise HTTPException(status_code=status.HTTP_400_BAD_REQUEST, detail="Invalid user ID format.")

    res = users_col.delete_one({"_id": obj_id})
    if res.deleted_count == 0:
        raise HTTPException(status_code=status.HTTP_404_NOT_FOUND, detail="Student not found.")
    
    return {"message": "Student registration rejected and removed", "success": True}

