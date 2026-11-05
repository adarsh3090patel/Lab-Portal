from pydantic import BaseModel
from typing import Optional, List

class UserRegisterRequest(BaseModel):
    fullName: str
    email: str
    rollNo: str
    role: str = "Student"
    password: str
    programme: Optional[str] = "B.Sc. (Computer Science)"
    semester: Optional[str] = "Semester V"
    phone: Optional[str] = "+91 98765 43210"
    profilePhotoUrl: Optional[str] = ""
    faceEnrolled: Optional[bool] = True
    facePhotoUrl: Optional[str] = ""

class CourseCreateRequest(BaseModel):
    name: str
    code: str
    semesters: List[str]
    facultyName: Optional[str] = "Department of Computer Science & Engineering"

class UserLoginRequest(BaseModel):
    email: str
    password: str

class ProfileUpdateRequest(BaseModel):
    fullName: Optional[str] = None
    phone: Optional[str] = None
    department: Optional[str] = None
    programme: Optional[str] = None
    semester: Optional[str] = None
    academicYear: Optional[str] = None
    collegeName: Optional[str] = "Maharishi Mahesh Yogi Ramayan University Ayodhya"
    dob: Optional[str] = None
    bloodGroup: Optional[str] = None
    permanentAddress: Optional[str] = None
    guardianName: Optional[str] = None
    guardianPhone: Optional[str] = None
    profilePhotoUrl: Optional[str] = None
    facePhotoUrl: Optional[str] = None

class UserResponse(BaseModel):
    id: str
    fullName: str
    email: str
    rollNo: str
    role: str
    programme: Optional[str] = "B.Sc. (Computer Science)"
    semester: Optional[str] = "Semester V"
    phone: Optional[str] = "+91 98765 43210"
    profilePhotoUrl: Optional[str] = ""
    faceEnrolled: Optional[bool] = True
    facePhotoUrl: Optional[str] = ""
    profileCompletion: Optional[int] = 80
