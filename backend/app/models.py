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
    faceEnrolled: Optional[bool] = True
    facePhotoUrl: Optional[str] = ""

class CourseCreateRequest(BaseModel):
    name: str
    code: str
    semesters: List[str]

class UserLoginRequest(BaseModel):
    email: str
    password: str

class UserResponse(BaseModel):
    id: str
    fullName: str
    email: str
    rollNo: str
    role: str
    programme: Optional[str] = "B.Sc. (Computer Science)"
    semester: Optional[str] = "Semester V"
    phone: Optional[str] = "+91 98765 43210"
    faceEnrolled: Optional[bool] = True
    profileCompletion: Optional[int] = 80
