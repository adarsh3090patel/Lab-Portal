package com.example.labportal.ui.screens

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
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
import androidx.compose.material3.IconButton
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
    var showEditProfileDialog by remember { mutableStateOf(false) }
    var isSaving by remember { mutableStateOf(false) }
    var showLiveFaceScanner by remember { mutableStateOf(false) }
    var showFullBiometricDialog by remember { mutableStateOf(false) }
    var showFullProfilePhotoDialog by remember { mutableStateOf(false) }
    var showAvatarPickerOptions by remember { mutableStateOf(false) }

    // Separate State: 1. Display Profile Avatar & 2. Official Biometric Face Template
    var currentProfilePhoto by remember {
        mutableStateOf(user.profilePhotoUrl.ifBlank { user.facePhotoUrl })
    }
    var currentBiometricPhoto by remember {
        mutableStateOf(user.facePhotoUrl)
    }

    var fullNameInput by remember { mutableStateOf(user.fullName) }
    var phoneInput by remember { mutableStateOf(user.phone) }
    var dobInput by remember { mutableStateOf(user.dob) }
    var bloodGroupInput by remember { mutableStateOf(user.bloodGroup) }
    var addressInput by remember { mutableStateOf(user.permanentAddress) }
    var guardianNameInput by remember { mutableStateOf(user.guardianName) }
    var guardianPhoneInput by remember { mutableStateOf(user.guardianPhone) }

    // Bitmaps for rendering
    val profileImageBitmap = remember(currentProfilePhoto) {
        FaceDetectionHelper.base64ToImageBitmap(currentProfilePhoto)
    }
    val biometricImageBitmap = remember(currentBiometricPhoto) {
        FaceDetectionHelper.base64ToImageBitmap(currentBiometricPhoto)
    }

    // Persist updates to MongoDB via backend API
    fun saveProfileToDatabase(
        newProfilePhoto: String? = null,
        newBiometricPhoto: String? = null,
        newEmbedding: List<Float>? = null,
        onSuccess: (() -> Unit)? = null
    ) {
        val targetProfile = newProfilePhoto ?: currentProfilePhoto
        val targetBiometric = newBiometricPhoto ?: currentBiometricPhoto
        val userId = if (user.id.isNotBlank()) user.id else user.rollNo

        isSaving = true
        coroutineScope.launch {
            try {
                val req = ProfileUpdateRequest(
                    fullName = fullNameInput.trim().ifBlank { user.fullName },
                    phone = phoneInput.trim(),
                    dob = dobInput.trim(),
                    bloodGroup = bloodGroupInput.trim(),
                    permanentAddress = addressInput.trim(),
                    guardianName = guardianNameInput.trim(),
                    guardianPhone = guardianPhoneInput.trim(),
                    profilePhotoUrl = if (targetProfile.isNotBlank()) targetProfile else null,
                    facePhotoUrl = if (targetBiometric.isNotBlank()) targetBiometric else null,
                    faceEmbedding = newEmbedding
                )

                val response = NetworkClient.apiService.updateProfile(userId, req)
                isSaving = false

                if (response.isSuccessful && response.body() != null) {
                    val updatedDto = response.body()!!.user
                    val updatedUser = user.copy(
                        fullName = updatedDto.fullName,
                        phone = updatedDto.phone,
                        dob = updatedDto.dob,
                        bloodGroup = updatedDto.bloodGroup,
                        permanentAddress = updatedDto.permanentAddress,
                        guardianName = updatedDto.guardianName,
                        guardianPhone = updatedDto.guardianPhone,
                        profilePhotoUrl = updatedDto.profilePhotoUrl,
                        facePhotoUrl = updatedDto.facePhotoUrl,
                        faceEmbedding = updatedDto.faceEmbedding ?: (newEmbedding ?: user.faceEmbedding),
                        profileCompletion = updatedDto.profileCompletion
                    )
                    currentProfilePhoto = updatedDto.profilePhotoUrl
                    currentBiometricPhoto = updatedDto.facePhotoUrl
                    fullNameInput = updatedDto.fullName
                    onProfileUpdated(updatedUser)
                    isEditMode = false
                    showEditProfileDialog = false
                    onSuccess?.invoke()
                    Toast.makeText(
                        context,
                        if (updatedDto.profileCompletion >= 100)
                            "Profile 100% complete! Database updated successfully."
                        else
                            "Profile saved to database successfully.",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    Toast.makeText(context, "Failed to update profile in database", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                isSaving = false
                val isComplete = dobInput.isNotBlank() && bloodGroupInput.isNotBlank() && addressInput.isNotBlank()
                val updatedUser = user.copy(
                    fullName = fullNameInput.trim().ifBlank { user.fullName },
                    phone = phoneInput.trim(),
                    dob = dobInput.trim(),
                    bloodGroup = bloodGroupInput.trim(),
                    permanentAddress = addressInput.trim(),
                    guardianName = guardianNameInput.trim(),
                    guardianPhone = guardianPhoneInput.trim(),
                    profilePhotoUrl = targetProfile,
                    facePhotoUrl = targetBiometric,
                    profileCompletion = if (isComplete) 100 else 85
                )
                onProfileUpdated(updatedUser)
                isEditMode = false
                showEditProfileDialog = false
                onSuccess?.invoke()
                Toast.makeText(context, "Saved offline.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Avatar pickers (Separate from facial biometric scanner)
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            try {
                val inputStream = context.contentResolver.openInputStream(uri)
                val bitmap = BitmapFactory.decodeStream(inputStream)
                if (bitmap != null) {
                    val base64 = FaceDetectionHelper.bitmapToBase64(bitmap)
                    currentProfilePhoto = base64
                    saveProfileToDatabase(newProfilePhoto = base64)
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Could not load image", Toast.LENGTH_SHORT).show()
            }
        }
    }

    val cameraPhotoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        if (bitmap != null) {
            val base64 = FaceDetectionHelper.bitmapToBase64(bitmap)
            currentProfilePhoto = base64
            saveProfileToDatabase(newProfilePhoto = base64)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            // Generous spacing so camera punch-hole / dynamic island never overlaps the top bar
            Spacer(modifier = Modifier.height(28.dp))

            // 1. Top Bar: Back Button, Title/Subtitle, Edit Profile Button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
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

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 12.dp)
                ) {
                    Text(
                        text = "Student Profile",
                        fontSize = 18.sp,
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

                // Edit Profile Button (Opens interactive edit dialog with all contact/personal fields)
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = Color(0xFFEEF2FF),
                    border = BorderStroke(1.dp, Color(0xFFC7D2FE)),
                    modifier = Modifier.clickable {
                        showEditProfileDialog = true
                    }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_edit),
                            contentDescription = "Edit Profile",
                            tint = MmyrvPrimary,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Edit Profile",
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = AppFontFamily,
                            color = MmyrvPrimary
                        )
                    }
                }
            }

            // Edit Mode Alert Banner (when edit mode active)
            if (isEditMode) {
                Spacer(modifier = Modifier.height(10.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFEEF2FF)),
                    border = BorderStroke(1.dp, Color(0xFFC7D2FE))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_edit),
                            contentDescription = null,
                            tint = MmyrvPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Editing Mode Active • Modify fields and tap Save.",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            fontFamily = AppFontFamily,
                            color = MmyrvPrimary,
                            modifier = Modifier.weight(1f)
                        )
                        TextButton(onClick = { isEditMode = false }) {
                            Text("Done", fontWeight = FontWeight.Bold, color = MmyrvPrimary, fontSize = 12.sp)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 2. Student Hero Card: Profile Photo Avatar + Information
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
                    // Profile Photo Avatar with Change Badge
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
                            if (profileImageBitmap != null) {
                                Image(
                                    bitmap = profileImageBitmap,
                                    contentDescription = "Profile Photo",
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

                        // Camera Badge to change Profile Photo
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(MmyrvPrimary)
                                .border(BorderStroke(2.dp, Color.White), CircleShape)
                                .clickable { showAvatarPickerOptions = true },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_camera),
                                contentDescription = "Change Profile Picture",
                                tint = Color.White,
                                modifier = Modifier.size(17.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = fullNameInput.ifBlank { user.fullName },
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = AppFontFamily,
                        color = MmyrvTextDark,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(3.dp))

                    Text(
                        text = "${user.programme} • ${user.semester}",
                        fontSize = 13.5.sp,
                        fontFamily = AppFontFamily,
                        color = MmyrvTextMuted,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Active Student Badge
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

                    // Dual Buttons: View Photo & Change Photo (Matches original clean layout)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                if (currentProfilePhoto.isNotBlank()) {
                                    showFullProfilePhotoDialog = true
                                } else {
                                    showAvatarPickerOptions = true
                                }
                            },
                            modifier = Modifier
                                .weight(1f)
                                .defaultMinSize(minHeight = 44.dp),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, Color(0xFFC7D2FE)),
                            colors = ButtonDefaults.outlinedButtonColors(containerColor = Color.White),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_visibility),
                                contentDescription = null,
                                tint = MmyrvPrimary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "View Photo",
                                fontSize = 12.5.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = AppFontFamily,
                                color = MmyrvPrimary,
                                maxLines = 1
                            )
                        }

                        Button(
                            onClick = { showAvatarPickerOptions = true },
                            modifier = Modifier
                                .weight(1f)
                                .defaultMinSize(minHeight = 44.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MmyrvPrimary),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_camera),
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Change Photo",
                                fontSize = 12.5.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = AppFontFamily,
                                color = Color.White,
                                maxLines = 1
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(22.dp))

            // 3. Dedicated Screen-Lock Biometric Facial Template Section (Separate from profile avatar)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_face_recognition),
                    contentDescription = null,
                    tint = MmyrvTextDark,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Screen-Lock Facial Biometric Record",
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
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Biometric Template Thumbnail
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(Color(0xFF0F172A))
                                .border(1.5.dp, Color(0xFF10B981), RoundedCornerShape(14.dp))
                                .clickable {
                                    if (biometricImageBitmap != null) {
                                        showFullBiometricDialog = true
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            if (biometricImageBitmap != null) {
                                Image(
                                    bitmap = biometricImageBitmap,
                                    contentDescription = "Biometric Face Template",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_face_recognition),
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "3D Landmark Face Key",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = AppFontFamily,
                                    color = MmyrvTextDark
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = Color(0xFFDCFCE7)
                                ) {
                                    Text(
                                        text = "STRICT LOCK",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF15803D),
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(3.dp))
                            Text(
                                text = "Enforced 0° straight gaze, eye-open liveness check & multi-point facial geometric template.",
                                fontSize = 11.5.sp,
                                fontFamily = AppFontFamily,
                                color = MmyrvTextMuted,
                                lineHeight = 15.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = { showFullBiometricDialog = true },
                            modifier = Modifier
                                .weight(1f)
                                .defaultMinSize(minHeight = 42.dp),
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 6.dp)
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_visibility),
                                contentDescription = null,
                                tint = MmyrvTextDark,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "View Biometric",
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = AppFontFamily,
                                color = MmyrvTextDark,
                                maxLines = 1
                            )
                        }

                        Button(
                            onClick = { showLiveFaceScanner = true },
                            modifier = Modifier
                                .weight(1.1f)
                                .defaultMinSize(minHeight = 42.dp),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 6.dp)
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_camera),
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Re-Enroll Face",
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = AppFontFamily,
                                color = Color.White,
                                maxLines = 1
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(22.dp))

            // 4. Section: Academic Information
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

            // 5. Section: Contact & Personal Details
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp, vertical = 2.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
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

                TextButton(onClick = { showEditProfileDialog = true }) {
                    Icon(painter = painterResource(id = R.drawable.ic_edit), contentDescription = null, tint = MmyrvPrimary, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Edit Details", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MmyrvPrimary)
                }
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
                    ProfileRecordRow(
                        iconRes = R.drawable.ic_email,
                        label = "Email Address",
                        value = user.email
                    )
                    HorizontalDivider(color = Color(0xFFF1F5F9), thickness = 1.dp)

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
                            value = if (phoneInput.isNotBlank()) phoneInput else if (user.phone.isNotBlank()) user.phone else "Not provided"
                        )
                    }
                    HorizontalDivider(color = Color(0xFFF1F5F9), thickness = 1.dp)

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
                            value = if (dobInput.isNotBlank()) dobInput else if (user.dob.isNotBlank()) user.dob else "Not provided (tap Edit Details)"
                        )
                    }
                    HorizontalDivider(color = Color(0xFFF1F5F9), thickness = 1.dp)

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
                            value = if (bloodGroupInput.isNotBlank()) bloodGroupInput else if (user.bloodGroup.isNotBlank()) user.bloodGroup else "Not provided (tap Edit Details)"
                        )
                    }
                    HorizontalDivider(color = Color(0xFFF1F5F9), thickness = 1.dp)

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
                            value = if (addressInput.isNotBlank()) addressInput else if (user.permanentAddress.isNotBlank()) user.permanentAddress else "Not provided (tap Edit Details)"
                        )
                    }
                    HorizontalDivider(color = Color(0xFFF1F5F9), thickness = 1.dp)

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
                        val gName = guardianNameInput.ifBlank { user.guardianName }
                        val gPhone = guardianPhoneInput.ifBlank { user.guardianPhone }
                        ProfileRecordRow(
                            iconRes = R.drawable.ic_group,
                            label = "Guardian / Parent Details",
                            value = if (gName.isNotBlank()) "$gName • $gPhone" else "Not provided (tap Edit Details)"
                        )
                    }
                }
            }

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

        // 6. Interactive Edit Profile Modal Dialog (Immediately pops up on tapping Edit Profile)
        if (showEditProfileDialog) {
            Dialog(onDismissRequest = { showEditProfileDialog = false }) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth(0.95f)
                        .clip(RoundedCornerShape(24.dp)),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState())
                            .padding(22.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFFEEF2FF)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        painter = painterResource(id = R.drawable.ic_edit),
                                        contentDescription = null,
                                        tint = MmyrvPrimary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "Edit Profile Information",
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = AppFontFamily,
                                        fontSize = 16.sp,
                                        color = MmyrvTextDark
                                    )
                                    Text(
                                        text = "Saved directly to database",
                                        fontSize = 11.sp,
                                        fontFamily = AppFontFamily,
                                        color = MmyrvTextMuted
                                    )
                                }
                            }

                            IconButton(
                                onClick = { showEditProfileDialog = false },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_close),
                                    contentDescription = "Close",
                                    tint = MmyrvTextMuted,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Full Name
                        EditableRecordField(
                            iconRes = R.drawable.ic_person,
                            label = "Full Name",
                            value = fullNameInput,
                            onValueChange = { fullNameInput = it },
                            placeholder = "e.g. Adarsh Kumar"
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Phone Number
                        EditableRecordField(
                            iconRes = R.drawable.ic_phone,
                            label = "Phone Number",
                            value = phoneInput,
                            onValueChange = { phoneInput = it },
                            placeholder = "+91 98765 43210"
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // DOB
                        EditableRecordField(
                            iconRes = R.drawable.ic_calendar_today,
                            label = "Date of Birth (DOB)",
                            value = dobInput,
                            onValueChange = { dobInput = it },
                            placeholder = "DD/MM/YYYY (e.g. 15/08/2004)"
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Blood Group
                        EditableRecordField(
                            iconRes = R.drawable.ic_water_drop,
                            label = "Blood Group",
                            value = bloodGroupInput,
                            onValueChange = { bloodGroupInput = it },
                            placeholder = "e.g. B+, O+, A+, AB+"
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Permanent Address
                        EditableRecordField(
                            iconRes = R.drawable.ic_location,
                            label = "Permanent Address",
                            value = addressInput,
                            onValueChange = { addressInput = it },
                            placeholder = "Ayodhya, Uttar Pradesh, 224001"
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Guardian Name
                        EditableRecordField(
                            iconRes = R.drawable.ic_group,
                            label = "Guardian / Parent Name",
                            value = guardianNameInput,
                            onValueChange = { guardianNameInput = it },
                            placeholder = "Parent Full Name"
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Guardian Phone
                        EditableRecordField(
                            iconRes = R.drawable.ic_phone,
                            label = "Guardian Phone Number",
                            value = guardianPhoneInput,
                            onValueChange = { guardianPhoneInput = it },
                            placeholder = "+91 98765 00000"
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedButton(
                                onClick = { showEditProfileDialog = false },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(44.dp),
                                shape = RoundedCornerShape(10.dp),
                                border = BorderStroke(1.dp, Color(0xFFCBD5E1))
                            ) {
                                Text("Cancel", color = MmyrvTextDark, fontFamily = AppFontFamily)
                            }

                            Button(
                                onClick = {
                                    saveProfileToDatabase(onSuccess = { showEditProfileDialog = false })
                                },
                                modifier = Modifier
                                    .weight(1.4f)
                                    .height(44.dp),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = MmyrvPrimary),
                                enabled = !isSaving
                            ) {
                                if (isSaving) {
                                    CircularProgressIndicator(
                                        color = Color.White,
                                        strokeWidth = 2.dp,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Saving...", color = Color.White, fontWeight = FontWeight.Bold, fontFamily = AppFontFamily)
                                } else {
                                    Icon(
                                        painter = painterResource(id = R.drawable.ic_check_circle),
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Save Changes", color = Color.White, fontWeight = FontWeight.Bold, fontFamily = AppFontFamily)
                                }
                            }
                        }
                    }
                }
            }
        }

        // 7. Avatar Selection Dialog (Gallery / Camera)
        if (showAvatarPickerOptions) {
            AlertDialog(
                onDismissRequest = { showAvatarPickerOptions = false },
                title = {
                    Text(
                        text = "Change Profile Picture",
                        fontWeight = FontWeight.Bold,
                        fontFamily = AppFontFamily,
                        fontSize = 17.sp,
                        color = MmyrvTextDark
                    )
                },
                text = {
                    Column {
                        Text(
                            text = "Your profile picture is displayed on your student ID card and dashboard. It is separate from your biometric facial template.",
                            fontSize = 12.sp,
                            fontFamily = AppFontFamily,
                            color = MmyrvTextMuted
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = {
                                showAvatarPickerOptions = false
                                cameraPhotoLauncher.launch(null)
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = MmyrvPrimary),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(painter = painterResource(id = R.drawable.ic_camera), contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Take Selfie / Photo", fontFamily = AppFontFamily, fontWeight = FontWeight.Bold)
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedButton(
                            onClick = {
                                showAvatarPickerOptions = false
                                galleryLauncher.launch("image/*")
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(painter = painterResource(id = R.drawable.ic_resources), contentDescription = null, tint = MmyrvPrimary, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Choose from Gallery", fontFamily = AppFontFamily, fontWeight = FontWeight.Bold, color = MmyrvPrimary)
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showAvatarPickerOptions = false }) {
                        Text("Cancel", fontFamily = AppFontFamily, color = MmyrvTextMuted)
                    }
                }
            )
        }

        // 8. High-Resolution Profile Photo Inspection Modal
        if (showFullProfilePhotoDialog) {
            Dialog(onDismissRequest = { showFullProfilePhotoDialog = false }) {
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
                                text = "Profile Photo",
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
                                    .clickable { showFullProfilePhotoDialog = false },
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
                                .background(Color(0xFFF1F5F9))
                                .border(BorderStroke(1.5.dp, Color(0xFFC7D2FE)), RoundedCornerShape(20.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            if (profileImageBitmap != null) {
                                Image(
                                    bitmap = profileImageBitmap,
                                    contentDescription = "Profile Photo",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_person),
                                    contentDescription = null,
                                    tint = MmyrvTextMuted,
                                    modifier = Modifier.size(90.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = fullNameInput.ifBlank { user.fullName },
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = AppFontFamily,
                            color = MmyrvTextDark
                        )
                        Text(
                            text = "${user.programme} • Roll: ${user.rollNo}",
                            fontSize = 12.5.sp,
                            fontFamily = AppFontFamily,
                            color = MmyrvTextMuted
                        )

                        Spacer(modifier = Modifier.height(18.dp))

                        Button(
                            onClick = {
                                showFullProfilePhotoDialog = false
                                showAvatarPickerOptions = true
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(44.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MmyrvPrimary)
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_camera),
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Change Photo",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontFamily = AppFontFamily
                            )
                        }
                    }
                }
            }
        }

        // 9. High-Resolution Biometric Template Inspection Modal
        if (showFullBiometricDialog) {
            Dialog(onDismissRequest = { showFullBiometricDialog = false }) {
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
                                text = "Screen-Lock Biometric Template",
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
                                    .clickable { showFullBiometricDialog = false },
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
                                .background(Color(0xFF0F172A))
                                .border(BorderStroke(2.dp, Color(0xFF10B981)), RoundedCornerShape(20.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            if (biometricImageBitmap != null) {
                                Image(
                                    bitmap = biometricImageBitmap,
                                    contentDescription = "High-Res Biometric Face",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_face_recognition),
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(90.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = fullNameInput.ifBlank { user.fullName },
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = AppFontFamily,
                            color = MmyrvTextDark
                        )
                        Text(
                            text = "Enrolled Biometric ID • Roll: ${user.rollNo}",
                            fontSize = 12.5.sp,
                            fontFamily = AppFontFamily,
                            color = MmyrvTextMuted
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFFDCFCE7),
                            border = BorderStroke(1.dp, Color(0xFFA7F3D0))
                        ) {
                            Text(
                                text = "Screen-Lock Strictness Active • 3D Landmarks Enrolled",
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF15803D),
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        Button(
                            onClick = { showFullBiometricDialog = false },
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

        // 9. Screen-Lock Live Front Camera Biometric Scanner Modal
        if (showLiveFaceScanner) {
            LiveFaceScannerModal(
                title = "Screen-Lock Biometric Scanner",
                subtitle = "AI MobileFaceNet 3D Landmark & Liveness Check",
                registeredBase64 = currentBiometricPhoto,
                enrolledEmbedding = user.faceEmbedding.ifEmpty { null },
                expectedStudentName = fullNameInput.ifBlank { user.fullName },
                expectedRollNo = user.rollNo,
                onFaceVerified = { _, base64, embedding, _ ->
                    showLiveFaceScanner = false
                    currentBiometricPhoto = base64
                    saveProfileToDatabase(newBiometricPhoto = base64, newEmbedding = embedding.toList())
                },
                onFaceCaptured = { _, base64 ->
                    showLiveFaceScanner = false
                    currentBiometricPhoto = base64
                    saveProfileToDatabase(newBiometricPhoto = base64)
                },
                onDismiss = { showLiveFaceScanner = false }
            )
        }
    }
}

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
            .padding(vertical = 4.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                painter = painterResource(id = iconRes),
                contentDescription = null,
                tint = MmyrvPrimary,
                modifier = Modifier.size(15.dp)
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
        Spacer(modifier = Modifier.height(5.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = { Text(placeholder, fontSize = 12.5.sp, color = Color(0xFF94A3B8), fontFamily = AppFontFamily) },
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
