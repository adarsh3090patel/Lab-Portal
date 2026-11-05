from pydantic import BaseModel
from typing import Optional

class UserRegisterRequest(BaseModel):
    fullName: str
    email: str
    rollNo: str
    role: str = "Student"
    password: str

class UserLoginRequest(BaseModel):
    email: str
    password: str

class UserResponse(BaseModel):
    id: str
    fullName: str
    email: str
    rollNo: str
    role: str
