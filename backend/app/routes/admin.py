from fastapi import APIRouter, HTTPException, status
from bson import ObjectId
from app.database import get_database
from app.models import CourseCreateRequest

router = APIRouter(tags=["Admin Operations & Courses"])

DEFAULT_COURSES = [
    {
        "id": "course_bsc_cs",
        "name": "B.Sc. (Computer Science)",
        "code": "BSCCS",
        "semesters": ["Semester I", "Semester II", "Semester III", "Semester IV", "Semester V", "Semester VI"]
    },
    {
        "id": "course_bca",
        "name": "BCA (Bachelor of Computer Applications)",
        "code": "BCA",
        "semesters": ["Semester I", "Semester II", "Semester III", "Semester IV", "Semester V", "Semester VI"]
    },
    {
        "id": "course_btech_cse",
        "name": "B.Tech (Computer Science & Engineering)",
        "code": "BTECH-CSE",
        "semesters": ["Semester I", "Semester II", "Semester III", "Semester IV", "Semester V", "Semester VI", "Semester VII", "Semester VIII"]
    },
    {
        "id": "course_msc_it",
        "name": "M.Sc. (Information Technology)",
        "code": "MSCIT",
        "semesters": ["Semester I", "Semester II", "Semester III", "Semester IV"]
    },
    {
        "id": "course_mca",
        "name": "MCA (Master of Computer Applications)",
        "code": "MCA",
        "semesters": ["Semester I", "Semester II", "Semester III", "Semester IV"]
    }
]

@router.get("/api/courses")
@router.get("/api/admin/courses")
def get_all_courses():
    db = get_database()
    if db is None:
        return DEFAULT_COURSES
    courses_col = db["courses"]
    courses = list(courses_col.find())
    if not courses:
        # Seed default courses
        for c in DEFAULT_COURSES:
            c_copy = dict(c)
            del c_copy["id"]
            courses_col.insert_one(c_copy)
        courses = list(courses_col.find())
    
    result = []
    for c in courses:
        result.append({
            "id": str(c["_id"]),
            "name": c.get("name", ""),
            "code": c.get("code", ""),
            "semesters": c.get("semesters", [])
        })
    return result

@router.post("/api/admin/courses")
def add_new_course(req: CourseCreateRequest):
    db = get_database()
    if db is None:
        raise HTTPException(status_code=status.HTTP_503_SERVICE_UNAVAILABLE, detail="Database connection not ready.")
    courses_col = db["courses"]
    existing = courses_col.find_one({"name": {"$regex": f"^{req.name}$", "$options": "i"}})
    if existing:
        raise HTTPException(status_code=status.HTTP_400_BAD_REQUEST, detail="A course with this name already exists.")
    
    new_course = {
        "name": req.name.strip(),
        "code": req.code.strip().upper(),
        "semesters": req.semesters
    }
    result = courses_col.insert_one(new_course)
    return {
        "message": "Course created successfully",
        "course": {
            "id": str(result.inserted_id),
            "name": req.name,
            "code": req.code,
            "semesters": req.semesters
        }
    }

@router.delete("/api/admin/courses/{course_id}")
def delete_course(course_id: str):
    db = get_database()
    if db is None:
        raise HTTPException(status_code=status.HTTP_503_SERVICE_UNAVAILABLE, detail="Database connection not ready.")
    courses_col = db["courses"]
    try:
        obj_id = ObjectId(course_id)
        res = courses_col.delete_one({"_id": obj_id})
    except Exception:
        res = courses_col.delete_one({"code": course_id})
    
    return {"message": "Course removed successfully", "success": True}

@router.get("/api/admin/students")
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
            "profilePhotoUrl": s.get("profilePhotoUrl", s.get("facePhotoUrl", "")),
            "faceEnrolled": s.get("faceEnrolled", True),
            "facePhotoUrl": s.get("facePhotoUrl", ""),
            "isApproved": s.get("isApproved", False),
            "createdAt": s.get("createdAt", "Recent")
        })
    return result

@router.post("/api/admin/approve-student/{user_id}")
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

@router.post("/api/admin/reject-student/{user_id}")
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

