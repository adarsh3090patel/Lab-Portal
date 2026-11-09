from pydantic import BaseModel
from typing import Optional, List, Dict, Any

class AllowedEdits(BaseModel):
    face: bool = False
    academic: bool = False
    personal: bool = False
    initialEditDone: bool = False

class GrantPermissionsRequest(BaseModel):
    face: bool = False
    academic: bool = False
    personal: bool = False

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
    faceEmbedding: Optional[List[float]] = None

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
    faceEmbedding: Optional[List[float]] = None

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
    faceEmbedding: Optional[List[float]] = None
    profileCompletion: Optional[int] = 80
    isBlocked: Optional[bool] = False
    status: Optional[str] = "approved"
    allowedEdits: Optional[Dict[str, Any]] = None
    initialEditDone: Optional[bool] = False

class BiometricEnrollRequest(BaseModel):
    userId: str
    rollNo: str
    faceEmbedding: List[float]
    facePhotoUrl: Optional[str] = None

class AttendanceVerificationRequest(BaseModel):
    studentId: str
    rollNo: str
    subject: str = "Central Laboratory Practical"
    similarityScore: float
    livenessScore: float = 1.0
    liveEmbedding: Optional[List[float]] = None

class BiometricRevokeRequest(BaseModel):
    userId: str
    reason: Optional[str] = "Biometric template reset"
