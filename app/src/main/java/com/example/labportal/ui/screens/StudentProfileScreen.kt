package com.example.labportal.ui.screens

import android.graphics.Bitmap
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.labportal.R
import com.example.labportal.data.model.UserData
import com.example.labportal.network.NetworkClient
import com.example.labportal.network.ProfileUpdateRequest
import com.example.labportal.ui.components.LiveFaceScannerModal
import com.example.labportal.ui.theme.AppFontFamily
import com.example.labportal.ui.theme.MmyrvBorder
import com.example.labportal.ui.theme.MmyrvPrimary
import com.example.labportal.ui.theme.MmyrvTextDark
import com.example.labportal.ui.theme.MmyrvTextMuted
import com.example.labportal.util.FaceDetectionHelper
import kotlinx.coroutines.launch

@Composable
fun StudentProfileScreen(
    user: UserData,
    onBackClick: () -> Unit,
    onProfileUpdated: (UserData) -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var isEditMode by remember { mutableStateOf(false) }
    var isSaving by remember { mutableStateOf(false) }
    var showLiveFaceScanner by remember { mutableStateOf(false) }
    var showFullPhotoDialog by remember { mutableStateOf(false) }

    // Editable form state initialized from current user
    var currentPhotoBase64 by remember { mutableStateOf(user.facePhotoUrl) }
    var phoneInput by remember { mutableStateOf(user.phone) }
    var dobInput by remember { mutableStateOf(user.dob) }
    var bloodGroupInput by remember { mutableStateOf(user.bloodGroup) }
    var addressInput by remember { mutableStateOf(user.permanentAddress) }
    var guardianNameInput by remember { mutableStateOf(user.guardianName) }
    var guardianPhoneInput by remember { mutableStateOf(user.guardianPhone) }

    // Face bitmap for preview
    val faceImageBitmap = remember(currentPhotoBase64) {
        FaceDetectionHelper.base64ToImageBitmap(currentPhotoBase64)
    }

    // Function to persist updates to MongoDB via backend
    fun saveProfileToDatabase(newPhoto: String? = null) {
        val targetPhoto = newPhoto ?: currentPhotoBase64
        val userId = if (user.id.isNotBlank()) user.id else user.rollNo

        isSaving = true
        coroutineScope.launch {
            try {
                val req = ProfileUpdateRequest(
                    phone = phoneInput.trim(),
                    dob = dobInput.trim(),
                    bloodGroup = bloodGroupInput.trim(),
                    permanentAddress = addressInput.trim(),
                    guardianName = guardianNameInput.trim(),
                    guardianPhone = guardianPhoneInput.trim(),
                    facePhotoUrl = if (targetPhoto.isNotBlank()) targetPhoto else null
                )

                val response = NetworkClient.apiService.updateProfile(userId, req)
                isSaving = false

                if (response.isSuccessful && response.body() != null) {
                    val updatedDto = response.body()!!.user
                    val updatedUser = user.copy(
                        phone = updatedDto.phone,
                        dob = updatedDto.dob,
                        bloodGroup = updatedDto.bloodGroup,
                        permanentAddress = updatedDto.permanentAddress,
                        guardianName = updatedDto.guardianName,
                        guardianPhone = updatedDto.guardianPhone,
                        facePhotoUrl = updatedDto.facePhotoUrl,
                        profileCompletion = updatedDto.profileCompletion
                    )
                    currentPhotoBase64 = updatedDto.facePhotoUrl
                    onProfileUpdated(updatedUser)
                    isEditMode = false
                    Toast.makeText(
                        context,
                        if (updatedDto.profileCompletion >= 100)
                            "Profile 100% complete! Database updated successfully."
                        else
                            "Profile updated in database.",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    Toast.makeText(context, "Failed to save profile to database", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                isSaving = false
                // Local fallback update so user isn't blocked if offline
                val isComplete = dobInput.isNotBlank() && bloodGroupInput.isNotBlank() && addressInput.isNotBlank()
                val updatedUser = user.copy(
                    phone = phoneInput.trim(),
                    dob = dobInput.trim(),
                    bloodGroup = bloodGroupInput.trim(),
                    permanentAddress = addressInput.trim(),
                    guardianName = guardianNameInput.trim(),
                    guardianPhone = guardianPhoneInput.trim(),
                    facePhotoUrl = targetPhoto,
                    profileCompletion = if (isComplete) 100 else 85
                )
                onProfileUpdated(updatedUser)
                isEditMode = false
                Toast.makeText(context, "Saved locally (Offline Mode)", Toast.LENGTH_SHORT).show()
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 18.dp)
        ) {
            // 1. Top Bar: Back Button, Title/Subtitle, Edit Profile Button (Matches uploaded UI)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Back Button
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color.White,
                    border = BorderStroke(1.dp, MmyrvBorder),
                    modifier = Modifier
                        .size(42.dp)
                        .clickable { onBackClick() }
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_arrow_back),
                            contentDescription = "Back",
                            tint = MmyrvTextDark,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                // Title & Subtitle
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 12.dp)
                ) {
                    Text(
                        text = "Student Profile",
                        fontSize = 19.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = AppFontFamily,
                        color = MmyrvTextDark
                    )
                    Text(
                        text = "Official Academic Record",
                        fontSize = 11.5.sp,
                        fontFamily = AppFontFamily,
                        color = MmyrvTextMuted
                    )
                }

                // Edit Profile / Save Pill Button
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = if (isEditMode) MmyrvPrimary else Color(0xFFEEF2FF),
                    border = BorderStroke(1.dp, if (isEditMode) MmyrvPrimary else Color(0xFFC7D2FE)),
                    modifier = Modifier.clickable {
                        if (isEditMode) {
                            saveProfileToDatabase()
                        } else {
                            isEditMode = true
                        }
                    }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (isSaving) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(14.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Saving...",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = AppFontFamily,
                                color = Color.White
                            )
                        } else {
                            Icon(
                                painter = painterResource(id = if (isEditMode) R.drawable.ic_check_circle else R.drawable.ic_edit),
                                contentDescription = "Edit Profile",
                                tint = if (isEditMode) Color.White else MmyrvPrimary,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isEditMode) "Save" else "Edit Profile",
                                fontSize = 12.5.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = AppFontFamily,
                                color = if (isEditMode) Color.White else MmyrvPrimary
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // 2. Student Hero Card (Matches uploaded mockup image exactly)
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp),
                border = BorderStroke(1.dp, MmyrvBorder)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 22.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Centered Circular Avatar with Camera Badge
                    Box(
                        modifier = Modifier.size(108.dp),
                        contentAlignment = Alignment.BottomEnd
                    ) {
                        Box(
                            modifier = Modifier
                                .size(108.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFEEF2FF))
                                .border(BorderStroke(3.dp, Color(0xFFE0E7FF)), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            if (faceImageBitmap != null) {
                                Image(
                                    bitmap = faceImageBitmap,
                                    contentDescription = "Student Biometric Photo",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_person),
                                    contentDescription = "Default Avatar",
                                    tint = MmyrvPrimary,
                                    modifier = Modifier.size(54.dp)
                                )
                            }
                        }

                        // Purple Camera Badge Button
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(MmyrvPrimary)
                                .border(BorderStroke(2.dp, Color.White), CircleShape)
                                .clickable { showLiveFaceScanner = true },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_camera),
                                contentDescription = "Change Biometric Photo",
                                tint = Color.White,
                                modifier = Modifier.size(17.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Student Full Name
                    Text(
                        text = user.fullName,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = AppFontFamily,
                        color = MmyrvTextDark,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(3.dp))

                    // Degree & Semester Subtitle
                    Text(
                        text = "${user.programme} • ${user.semester}",
                        fontSize = 13.5.sp,
                        fontFamily = AppFontFamily,
                        color = MmyrvTextMuted,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Active Student Pill Badge
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = Color(0xFFECFDF5),
                        border = BorderStroke(1.dp, Color(0xFFA7F3D0))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF10B981))
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Active Student • Roll: ${user.rollNo}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = AppFontFamily,
                                color = Color(0xFF047857),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Dual Action Buttons: "View Photo" & "Change Photo"
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // View Photo Button
                        OutlinedButton(
                            onClick = { showFullPhotoDialog = true },
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, Color(0xFFC7D2FE)),
                            colors = ButtonDefaults.outlinedButtonColors(containerColor = Color.White)
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_visibility),
                                contentDescription = "View Photo",
                                tint = MmyrvPrimary,
                                modifier = Modifier.size(17.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "View Photo",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = AppFontFamily,
                                color = MmyrvPrimary
                            )
                        }

                        // Change Photo Button (triggers live camera scanner)
                        Button(
                            onClick = { showLiveFaceScanner = true },
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MmyrvPrimary)
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_camera),
                                contentDescription = "Change Photo",
                                tint = Color.White,
                                modifier = Modifier.size(17.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Change Photo",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = AppFontFamily,
                                color = Color.White
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(22.dp))

            // 3. Section: Academic Information
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_school),
                    contentDescription = null,
                    tint = MmyrvTextDark,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Academic Information",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = AppFontFamily,
                    color = MmyrvTextDark
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, MmyrvBorder)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    ProfileRecordRow(
                        iconRes = R.drawable.ic_school,
                        label = "College / University",
                        value = user.collegeName
                    )
                    HorizontalDivider(color = Color(0xFFF1F5F9), thickness = 1.dp)

                    ProfileRecordRow(
                        iconRes = R.drawable.ic_badge,
                        label = "University Roll Number",
                        value = user.rollNo
                    )
                    HorizontalDivider(color = Color(0xFFF1F5F9), thickness = 1.dp)

                    ProfileRecordRow(
                        iconRes = R.drawable.ic_person,
                        label = "Enrollment Number",
                        value = if (user.enrollmentNo.isNotBlank()) user.enrollmentNo else user.rollNo
                    )
                    HorizontalDivider(color = Color(0xFFF1F5F9), thickness = 1.dp)

                    ProfileRecordRow(
                        iconRes = R.drawable.ic_school,
                        label = "Course & Degree",
                        value = user.programme
                    )
                    HorizontalDivider(color = Color(0xFFF1F5F9), thickness = 1.dp)

                    ProfileRecordRow(
                        iconRes = R.drawable.ic_resources,
                        label = "Department",
                        value = user.department
                    )
                    HorizontalDivider(color = Color(0xFFF1F5F9), thickness = 1.dp)

                    ProfileRecordRow(
                        iconRes = R.drawable.ic_schedule,
                        label = "Semester",
                        value = user.semester
                    )
                    HorizontalDivider(color = Color(0xFFF1F5F9), thickness = 1.dp)

                    ProfileRecordRow(
                        iconRes = R.drawable.ic_calendar_today,
                        label = "Session / Academic Year",
                        value = user.academicYear
                    )
                }
            }

            Spacer(modifier = Modifier.height(22.dp))

            // 4. Section: Contact & Personal Details
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_person),
                    contentDescription = null,
                    tint = MmyrvTextDark,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Contact & Personal Details",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = AppFontFamily,
                    color = MmyrvTextDark
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, MmyrvBorder)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    // Email (Official university email, read-only)
                    ProfileRecordRow(
                        iconRes = R.drawable.ic_email,
                        label = "Email Address",
                        value = user.email
                    )
                    HorizontalDivider(color = Color(0xFFF1F5F9), thickness = 1.dp)

                    // Phone Number
                    if (isEditMode) {
                        EditableRecordField(
                            iconRes = R.drawable.ic_phone,
                            label = "Phone Number",
                            value = phoneInput,
                            onValueChange = { phoneInput = it },
                            placeholder = "+91 98765 43210"
                        )
                    } else {
                        ProfileRecordRow(
                            iconRes = R.drawable.ic_phone,
                            label = "Phone Number",
                            value = if (user.phone.isNotBlank()) user.phone else "Not provided"
                        )
                    }
                    HorizontalDivider(color = Color(0xFFF1F5F9), thickness = 1.dp)

                    // Date of Birth
                    if (isEditMode) {
                        EditableRecordField(
                            iconRes = R.drawable.ic_calendar_today,
                            label = "Date of Birth (DOB)",
                            value = dobInput,
                            onValueChange = { dobInput = it },
                            placeholder = "DD/MM/YYYY (e.g. 15/08/2004)"
                        )
                    } else {
                        ProfileRecordRow(
                            iconRes = R.drawable.ic_calendar_today,
                            label = "Date of Birth (DOB)",
                            value = if (user.dob.isNotBlank()) user.dob else "Not provided (tap Edit Profile)"
                        )
                    }
                    HorizontalDivider(color = Color(0xFFF1F5F9), thickness = 1.dp)

                    // Blood Group
                    if (isEditMode) {
                        EditableRecordField(
                            iconRes = R.drawable.ic_water_drop,
                            label = "Blood Group",
                            value = bloodGroupInput,
                            onValueChange = { bloodGroupInput = it },
                            placeholder = "e.g. B+, O+, A+, AB+"
                        )
                    } else {
                        ProfileRecordRow(
                            iconRes = R.drawable.ic_water_drop,
                            label = "Blood Group",
                            value = if (user.bloodGroup.isNotBlank()) user.bloodGroup else "Not provided (tap Edit Profile)"
                        )
                    }
                    HorizontalDivider(color = Color(0xFFF1F5F9), thickness = 1.dp)

                    // Permanent Address
                    if (isEditMode) {
                        EditableRecordField(
                            iconRes = R.drawable.ic_location,
                            label = "Permanent Address",
                            value = addressInput,
                            onValueChange = { addressInput = it },
                            placeholder = "Ayodhya, Uttar Pradesh, 224001"
                        )
                    } else {
                        ProfileRecordRow(
                            iconRes = R.drawable.ic_location,
                            label = "Permanent Address",
                            value = if (user.permanentAddress.isNotBlank()) user.permanentAddress else "Not provided (tap Edit Profile)"
                        )
                    }
                    HorizontalDivider(color = Color(0xFFF1F5F9), thickness = 1.dp)

                    // Guardian Details
                    if (isEditMode) {
                        EditableRecordField(
                            iconRes = R.drawable.ic_group,
                            label = "Guardian / Parent Name",
                            value = guardianNameInput,
                            onValueChange = { guardianNameInput = it },
                            placeholder = "Parent or Guardian Full Name"
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        EditableRecordField(
                            iconRes = R.drawable.ic_phone,
                            label = "Guardian Phone Number",
                            value = guardianPhoneInput,
                            onValueChange = { guardianPhoneInput = it },
                            placeholder = "+91 98765 00000"
                        )
                    } else {
                        ProfileRecordRow(
                            iconRes = R.drawable.ic_group,
                            label = "Guardian / Parent Details",
                            value = if (user.guardianName.isNotBlank())
                                "${user.guardianName} • ${user.guardianPhone}"
                            else
                                "Not provided (tap Edit Profile)"
                        )
                    }
                }
            }

            // Save Button when in edit mode
            if (isEditMode) {
                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = { saveProfileToDatabase() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MmyrvPrimary)
                ) {
                    if (isSaving) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Saving to Database...", color = Color.White, fontWeight = FontWeight.Bold, fontFamily = AppFontFamily)
                    } else {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_check_circle),
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Save Profile to Database",
                            color = Color.White,
                            fontSize = 14.5.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = AppFontFamily
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))
        }

        // 5. Full-Screen High-Resolution Photo Viewer Modal
        if (showFullPhotoDialog) {
            Dialog(onDismissRequest = { showFullPhotoDialog = false }) {
                Card(
                    modifier = Modifier.fillMaxWidth(0.92f),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Biometric Photo Record",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = AppFontFamily,
                                color = MmyrvTextDark
                            )
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFF1F5F9))
                                    .clickable { showFullPhotoDialog = false },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_close),
                                    contentDescription = "Close",
                                    tint = MmyrvTextMuted,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Box(
                            modifier = Modifier
                                .size(240.dp)
                                .clip(RoundedCornerShape(20.dp))
                                .background(Color(0xFFEEF2FF))
                                .border(BorderStroke(2.dp, Color(0xFFC7D2FE)), RoundedCornerShape(20.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            if (faceImageBitmap != null) {
                                Image(
                                    bitmap = faceImageBitmap,
                                    contentDescription = "High-Res Student Face",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_person),
                                    contentDescription = null,
                                    tint = MmyrvPrimary,
                                    modifier = Modifier.size(90.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = user.fullName,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = AppFontFamily,
                            color = MmyrvTextDark
                        )
                        Text(
                            text = "Roll: ${user.rollNo}",
                            fontSize = 13.sp,
                            fontFamily = AppFontFamily,
                            color = MmyrvTextMuted
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFFECFDF5),
                            border = BorderStroke(1.dp, Color(0xFFA7F3D0))
                        ) {
                            Text(
                                text = "Verified AI Biometric Template",
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF047857),
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        Button(
                            onClick = { showFullPhotoDialog = false },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(44.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MmyrvPrimary)
                        ) {
                            Text(
                                text = "Close",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = AppFontFamily
                            )
                        }
                    }
                }
            }
        }

        // 6. Live In-App Front Camera Facial Scanner Modal
        if (showLiveFaceScanner) {
            LiveFaceScannerModal(
                title = "Update Face Biometric",
                subtitle = "Strict AI Landmark & Liveness Check",
                registeredBase64 = currentPhotoBase64,
                onFaceCaptured = { _, base64 ->
                    showLiveFaceScanner = false
                    currentPhotoBase64 = base64
                    saveProfileToDatabase(newPhoto = base64)
                },
                onDismiss = { showLiveFaceScanner = false }
            )
        }
    }
}

/**
 * Standard Read-Only Record Row with soft blue icon container
 */
@Composable
private fun ProfileRecordRow(
    iconRes: Int,
    label: String,
    value: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 11.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFFEEF2FF)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(id = iconRes),
                contentDescription = label,
                tint = MmyrvPrimary,
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                fontSize = 11.5.sp,
                fontFamily = AppFontFamily,
                color = MmyrvTextMuted,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = value,
                fontSize = 13.5.sp,
                fontWeight = FontWeight.SemiBold,
                fontFamily = AppFontFamily,
                color = MmyrvTextDark,
                lineHeight = 18.sp
            )
        }
    }
}

/**
 * Editable Input Field for Edit Mode
 */
@Composable
private fun EditableRecordField(
    iconRes: Int,
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                painter = painterResource(id = iconRes),
                contentDescription = null,
                tint = MmyrvPrimary,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = label,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                fontFamily = AppFontFamily,
                color = MmyrvTextDark
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = { Text(placeholder, fontSize = 13.sp, color = Color(0xFF94A3B8), fontFamily = AppFontFamily) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(10.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MmyrvPrimary,
                unfocusedBorderColor = Color(0xFFCBD5E1),
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White
            ),
            singleLine = true
        )
    }
}
