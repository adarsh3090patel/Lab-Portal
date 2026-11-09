import datetime
from fastapi import APIRouter, HTTPException, status
from bson import ObjectId
from app.database import get_database
from app.models import CourseCreateRequest, GrantPermissionsRequest

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
            "faceEmbedding": s.get("faceEmbedding", []),
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
        is_blocked = s.get("isBlocked", False) or s.get("status") == "blocked"
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
            "faceEmbedding": s.get("faceEmbedding", []),
            "isApproved": True,
            "isBlocked": is_blocked,
            "status": "blocked" if is_blocked else s.get("status", "approved"),
            "allowedEdits": s.get("allowedEdits", {"face": False, "academic": False, "personal": False}),
            "initialEditDone": s.get("initialEditDone", False),
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
            "faceEmbedding": s.get("faceEmbedding", []),
            "isApproved": False,
            "status": "rejected",
            "createdAt": s.get("createdAt", "Recent")
        })
    return result

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

@router.post("/api/admin/approve-student/{user_id}")
def approve_student(user_id: str):
    db = get_database()
    if db is None:
        raise HTTPException(status_code=status.HTTP_503_SERVICE_UNAVAILABLE, detail="Database connection not ready.")
    users_col = db["users"]
    student = find_user_by_identifier(user_id, users_col)
    if not student:
        raise HTTPException(status_code=status.HTTP_404_NOT_FOUND, detail="Student not found.")

    users_col.update_one({"_id": student["_id"]}, {"$set": {"isApproved": True, "status": "approved"}})
    return {"message": "Student approved successfully", "success": True}

@router.post("/api/admin/reject-student/{user_id}")
def reject_student(user_id: str):
    db = get_database()
    if db is None:
        raise HTTPException(status_code=status.HTTP_503_SERVICE_UNAVAILABLE, detail="Database connection not ready.")
    users_col = db["users"]
    student = find_user_by_identifier(user_id, users_col)
    if not student:
        raise HTTPException(status_code=status.HTTP_404_NOT_FOUND, detail="Student not found.")

    users_col.update_one({"_id": student["_id"]}, {"$set": {"isApproved": False, "status": "rejected"}})
    return {"message": "Student registration marked as rejected", "success": True}

@router.delete("/api/admin/delete-student/{user_id}")
def delete_student_permanently(user_id: str):
    db = get_database()
    if db is None:
        raise HTTPException(status_code=status.HTTP_503_SERVICE_UNAVAILABLE, detail="Database connection not ready.")
    users_col = db["users"]
    student = find_user_by_identifier(user_id, users_col)
    if not student:
        raise HTTPException(status_code=status.HTTP_404_NOT_FOUND, detail="Student not found.")

    users_col.delete_one({"_id": student["_id"]})
    return {"message": "Student record permanently deleted", "success": True}

@router.post("/api/admin/student/{user_id}/block")
@router.post("/api/admin/block-student/{user_id}")
def block_student(user_id: str):
    db = get_database()
    if db is None:
        raise HTTPException(status_code=status.HTTP_503_SERVICE_UNAVAILABLE, detail="Database connection not ready.")
    users_col = db["users"]
    student = find_user_by_identifier(user_id, users_col)
    if not student:
        raise HTTPException(status_code=status.HTTP_404_NOT_FOUND, detail="Student not found.")

    obj_id = student["_id"]
    users_col.update_one({"_id": obj_id}, {"$set": {"isBlocked": True, "status": "blocked"}})
    
    # Log blocking in edit history
    db["edit_history"].insert_one({
        "studentId": str(obj_id),
        "studentName": student.get("fullName", ""),
        "rollNo": student.get("rollNo", ""),
        "timestamp": datetime.datetime.now(datetime.timezone.utc).isoformat(),
        "changes": [{"field": "Account Status", "oldValue": "Active / Approved", "newValue": "Blocked by Administrator"}],
        "type": "Admin Action"
    })

    return {"message": "Student account has been blocked successfully.", "success": True, "isBlocked": True}

@router.post("/api/admin/student/{user_id}/unblock")
@router.post("/api/admin/unblock-student/{user_id}")
def unblock_student(user_id: str):
    db = get_database()
    if db is None:
        raise HTTPException(status_code=status.HTTP_503_SERVICE_UNAVAILABLE, detail="Database connection not ready.")
    users_col = db["users"]
    student = find_user_by_identifier(user_id, users_col)
    if not student:
        raise HTTPException(status_code=status.HTTP_404_NOT_FOUND, detail="Student not found.")

    obj_id = student["_id"]
    users_col.update_one({"_id": obj_id}, {"$set": {"isBlocked": False, "status": "approved"}})
    
    # Log unblocking in edit history
    db["edit_history"].insert_one({
        "studentId": str(obj_id),
        "studentName": student.get("fullName", ""),
        "rollNo": student.get("rollNo", ""),
        "timestamp": datetime.datetime.now(datetime.timezone.utc).isoformat(),
        "changes": [{"field": "Account Status", "oldValue": "Blocked", "newValue": "Restored / Unblocked"}],
        "type": "Admin Action"
    })

    return {"message": "Student account has been unblocked successfully.", "success": True, "isBlocked": False}

@router.post("/api/admin/student/{user_id}/grant-permissions")
def grant_update_permissions(user_id: str, req: GrantPermissionsRequest):
    db = get_database()
    if db is None:
        raise HTTPException(status_code=status.HTTP_503_SERVICE_UNAVAILABLE, detail="Database connection not ready.")
    users_col = db["users"]
    student = find_user_by_identifier(user_id, users_col)
    if not student:
        raise HTTPException(status_code=status.HTTP_404_NOT_FOUND, detail="Student not found.")

    obj_id = student["_id"]
    allowed = {
        "face": req.face,
        "academic": req.academic,
        "personal": req.personal
    }

    users_col.update_one({"_id": obj_id}, {"$set": {"allowedEdits": allowed}})
    
    granted_names = []
    if req.face: granted_names.append("Facial Recognition")
    if req.academic: granted_names.append("Academic Information")
    if req.personal: granted_names.append("Contact & Personal Details")
    
    db["edit_history"].insert_one({
        "studentId": str(obj_id),
        "studentName": student.get("fullName", ""),
        "rollNo": student.get("rollNo", ""),
        "timestamp": datetime.datetime.now(datetime.timezone.utc).isoformat(),
        "changes": [{
            "field": "Admin Permissions Granted",
            "oldValue": "Locked",
            "newValue": f"1-time edit granted for: {', '.join(granted_names) if granted_names else 'None'}"
        }],
        "type": "Permission Grant"
    })

    return {
        "message": f"Permissions updated! Student can update: {', '.join(granted_names) if granted_names else 'None'} once.",
        "success": True,
        "allowedEdits": allowed
    }

@router.get("/api/admin/student/{user_id}/edit-history")
def get_student_edit_history(user_id: str):
    db = get_database()
    if db is None:
        return []
    history_col = db["edit_history"]
    users_col = db["users"]
    
    student = find_user_by_identifier(user_id, users_col)
    s_id_str = str(student["_id"]) if student else user_id
    s_roll = student.get("rollNo", "") if student else user_id
    
    # Query by string id, object id, or rollNo
    records = list(history_col.find({
        "$or": [
            {"studentId": s_id_str},
            {"studentId": user_id},
            {"rollNo": s_roll},
            {"rollNo": user_id}
        ]
    }).sort("timestamp", -1))

    result = []
    for r in records:
        result.append({
            "id": str(r["_id"]),
            "studentId": r.get("studentId", ""),
            "studentName": r.get("studentName", ""),
            "rollNo": r.get("rollNo", ""),
            "timestamp": r.get("timestamp", ""),
            "changes": r.get("changes", []),
            "type": r.get("type", "Profile Edit")
        })
    return result


