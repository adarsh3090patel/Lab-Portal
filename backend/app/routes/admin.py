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
        "semesters": ["Semester I", "Semester II", "Semester III", "Semester IV", "Semester V", "Semester VI"],
        "facultyName": "Department of Computer Science & IT"
    },
    {
        "id": "course_bca",
        "name": "BCA (Bachelor of Computer Applications)",
        "code": "BCA",
        "semesters": ["Semester I", "Semester II", "Semester III", "Semester IV", "Semester V", "Semester VI"],
        "facultyName": "Department of Computer Science & IT"
    },
    {
        "id": "course_btech_cse",
        "name": "B.Tech (Computer Science & Engineering)",
        "code": "BTECH-CSE",
        "semesters": ["Semester I", "Semester II", "Semester III", "Semester IV", "Semester V", "Semester VI", "Semester VII", "Semester VIII"],
        "facultyName": "Faculty of Engineering & Technology"
    },
    {
        "id": "course_msc_it",
        "name": "M.Sc. (Information Technology)",
        "code": "MSCIT",
        "semesters": ["Semester I", "Semester II", "Semester III", "Semester IV"],
        "facultyName": "Department of Information Technology"
    },
    {
        "id": "course_mca",
        "name": "MCA (Master of Computer Applications)",
        "code": "MCA",
        "semesters": ["Semester I", "Semester II", "Semester III", "Semester IV"],
        "facultyName": "Faculty of Engineering & Technology"
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
        # Fast bulk insert seed default courses
        docs = []
        for c in DEFAULT_COURSES:
            c_copy = dict(c)
            del c_copy["id"]
            docs.append(c_copy)
        if docs:
            courses_col.insert_many(docs)
        courses = list(courses_col.find())
    
    result = []
    for c in courses:
        result.append({
            "id": str(c["_id"]),
            "name": c.get("name", ""),
            "code": c.get("code", ""),
            "semesters": c.get("semesters", []),
            "facultyName": c.get("facultyName", "Faculty of Engineering & Technology")
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
        "semesters": req.semesters,
        "facultyName": (req.facultyName or "Faculty of Engineering & Technology").strip()
    }
    result = courses_col.insert_one(new_course)
    return {
        "message": "Course created successfully",
        "course": {
            "id": str(result.inserted_id),
            "name": req.name,
            "code": req.code,
            "semesters": req.semesters,
            "facultyName": req.facultyName
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
    students = list(users_col.find({"role": {"$ne": "Admin"}, "status": {"$ne": "rejected"}}).sort("_id", -1))
    
    result = []
    for s in students:
        result.append({
            "id": str(s["_id"]),
            "fullName": s.get("fullName", ""),
            "email": s.get("email", ""),
            "rollNo": s.get("rollNo", ""),
            "enrollmentNo": s.get("enrollmentNo", s.get("rollNo", "")),
            "collegeName": s.get("collegeName", "Maharishi Mahesh Yogi Ramayan University Ayodhya"),
            "role": s.get("role", "Student"),
            "programme": s.get("programme", "B.Sc. (Computer Science)"),
            "semester": s.get("semester", "Semester V"),
            "department": s.get("department", "Computer Science & Engineering"),
            "phone": s.get("phone", "+91 98765 43210"),
            "profilePhotoUrl": s.get("profilePhotoUrl", s.get("facePhotoUrl", "")),
            "faceEnrolled": s.get("faceEnrolled", True),
            "facePhotoUrl": s.get("facePhotoUrl", ""),
            "isApproved": s.get("isApproved", False),
            "status": s.get("status", "pending" if not s.get("isApproved") else "approved"),
            "createdAt": s.get("createdAt", "Recent")
        })
    return result

@router.get("/api/admin/all-students")
def get_all_approved_students():
    db = get_database()
    if db is None:
        raise HTTPException(status_code=status.HTTP_503_SERVICE_UNAVAILABLE, detail="Database connection not ready.")
    users_col = db["users"]
    students = list(users_col.find({"role": {"$ne": "Admin"}, "isApproved": True}).sort("_id", -1))
    
    result = []
    for s in students:
        result.append({
            "id": str(s["_id"]),
            "fullName": s.get("fullName", ""),
            "email": s.get("email", ""),
            "rollNo": s.get("rollNo", ""),
            "enrollmentNo": s.get("enrollmentNo", s.get("rollNo", "")),
            "collegeName": s.get("collegeName", "Maharishi Mahesh Yogi Ramayan University Ayodhya"),
            "role": s.get("role", "Student"),
            "programme": s.get("programme", "B.Sc. (Computer Science)"),
            "semester": s.get("semester", "Semester V"),
            "department": s.get("department", "Computer Science & Engineering"),
            "phone": s.get("phone", "+91 98765 43210"),
            "profilePhotoUrl": s.get("profilePhotoUrl", s.get("facePhotoUrl", "")),
            "faceEnrolled": s.get("faceEnrolled", True),
            "facePhotoUrl": s.get("facePhotoUrl", ""),
            "isApproved": True,
            "status": "approved",
            "createdAt": s.get("createdAt", "Recent")
        })
    return result

@router.get("/api/admin/rejected-students")
def get_rejected_students():
    db = get_database()
    if db is None:
        raise HTTPException(status_code=status.HTTP_503_SERVICE_UNAVAILABLE, detail="Database connection not ready.")
    users_col = db["users"]
    students = list(users_col.find({"role": {"$ne": "Admin"}, "status": "rejected"}).sort("_id", -1))
    
    result = []
    for s in students:
        result.append({
            "id": str(s["_id"]),
            "fullName": s.get("fullName", ""),
            "email": s.get("email", ""),
            "rollNo": s.get("rollNo", ""),
            "enrollmentNo": s.get("enrollmentNo", s.get("rollNo", "")),
            "collegeName": s.get("collegeName", "Maharishi Mahesh Yogi Ramayan University Ayodhya"),
            "role": s.get("role", "Student"),
            "programme": s.get("programme", "B.Sc. (Computer Science)"),
            "semester": s.get("semester", "Semester V"),
            "department": s.get("department", "Computer Science & Engineering"),
            "phone": s.get("phone", "+91 98765 43210"),
            "profilePhotoUrl": s.get("profilePhotoUrl", s.get("facePhotoUrl", "")),
            "faceEnrolled": s.get("faceEnrolled", True),
            "facePhotoUrl": s.get("facePhotoUrl", ""),
            "isApproved": False,
            "status": "rejected",
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

    res = users_col.update_one({"_id": obj_id}, {"$set": {"isApproved": True, "status": "approved"}})
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

    res = users_col.update_one({"_id": obj_id}, {"$set": {"isApproved": False, "status": "rejected"}})
    if res.matched_count == 0:
        raise HTTPException(status_code=status.HTTP_404_NOT_FOUND, detail="Student not found.")
    
    return {"message": "Student registration marked as rejected", "success": True}

@router.delete("/api/admin/delete-student/{user_id}")
def delete_student_permanently(user_id: str):
    db = get_database()
    if db is None:
        raise HTTPException(status_code=status.HTTP_503_SERVICE_UNAVAILABLE, detail="Database connection not ready.")
    users_col = db["users"]
    try:
        obj_id = ObjectId(user_id)
    except Exception:
        raise HTTPException(status_code=status.HTTP_400_BAD_REQUEST, detail="Invalid user ID format.")

    res = users_col.delete_one({"_id": obj_id})
    return {"message": "Student record permanently deleted", "success": True}


