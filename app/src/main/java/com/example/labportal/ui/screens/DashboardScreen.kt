package com.example.labportal.ui.screens

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
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDrawerState
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.labportal.R
import com.example.labportal.data.model.UserData
import com.example.labportal.ui.components.PortalBottomBar
import com.example.labportal.ui.components.PortalTab
import com.example.labportal.ui.components.PortalTopBar
import com.example.labportal.ui.theme.MmyrvBadgeBg
import com.example.labportal.ui.theme.MmyrvBorder
import com.example.labportal.ui.theme.MmyrvPrimary
import com.example.labportal.ui.theme.MmyrvTextDark
import com.example.labportal.ui.theme.MmyrvTextMuted
import kotlinx.coroutines.launch

@Composable
fun DashboardScreen(
    user: UserData,
    onLogoutClick: () -> Unit = {}
) {
    val context = LocalContext.current
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val coroutineScope = rememberCoroutineScope()

    var selectedTab by remember { mutableStateOf(PortalTab.HOME) }
    var showProfileDialog by remember { mutableStateOf(false) }
    var showNotificationDialog by remember { mutableStateOf(false) }
    var showFaceScanDialog by remember { mutableStateOf(false) }
    var showAdminAddDialog by remember { mutableStateOf(false) }
    var showAdminApprovalsPage by remember { mutableStateOf(false) }
    var showAdminCoursesPage by remember { mutableStateOf(false) }

    val isAdmin = user.role.equals("Admin", ignoreCase = true)

    // Admin Student Approvals Fullscreen View
    if (showAdminApprovalsPage && isAdmin) {
        AdminStudentApprovalsScreen(
            onBackClick = { showAdminApprovalsPage = false }
        )
        return
    }

    // Admin Courses & Programmes Fullscreen View
    if (showAdminCoursesPage && isAdmin) {
        AdminCoursesScreen(
            onBackClick = { showAdminCoursesPage = false }
        )
        return
    }

    // Student Facial Recognition Dialog
    if (showFaceScanDialog) {
        FaceScanDialog(
            user = user,
            onDismiss = { showFaceScanDialog = false }
        )
    }

    // Admin Quick Add Action Dialog (100% Vector Icons, Zero Emojis)
    if (showAdminAddDialog) {
        AlertDialog(
            onDismissRequest = { showAdminAddDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_add),
                        contentDescription = null,
                        tint = MmyrvPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Admin Quick Actions",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = MmyrvTextDark
                    )
                }
            },
            text = {
                Column {
                    Text(
                        text = "Select an operation to perform on the university lab portal:",
                        fontSize = 12.sp,
                        color = MmyrvTextMuted
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    AdminQuickActionButton(
                        title = "Schedule New Lab Slot",
                        desc = "Configure batch timings, lab capacity & assign staff",
                        iconRes = R.drawable.ic_add,
                        iconColor = MmyrvPrimary,
                        onClick = {
                            showAdminAddDialog = false
                            Toast.makeText(context, "Opening Slot Scheduler Form...", Toast.LENGTH_SHORT).show()
                        }
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    AdminQuickActionButton(
                        title = "Add Hardware & Equipment",
                        desc = "Log oscilloscopes, trainer kits, or PCs into inventory",
                        iconRes = R.drawable.ic_inventory,
                        iconColor = Color(0xFF10B981),
                        onClick = {
                            showAdminAddDialog = false
                            Toast.makeText(context, "Opening Equipment Inventory Form...", Toast.LENGTH_SHORT).show()
                        }
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    AdminQuickActionButton(
                        title = "Post Lab Announcement",
                        desc = "Broadcast circular to enrolled students & lab assistants",
                        iconRes = R.drawable.ic_campaign,
                        iconColor = Color(0xFFF59E0B),
                        onClick = {
                            showAdminAddDialog = false
                            Toast.makeText(context, "Opening Notice Publisher...", Toast.LENGTH_SHORT).show()
                        }
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showAdminAddDialog = false }) {
                    Text(text = "Close", color = MmyrvTextMuted)
                }
            }
        )
    }

    // Notification Dialog (Clean Vector UI)
    if (showNotificationDialog) {
        AlertDialog(
            onDismissRequest = { showNotificationDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_notifications),
                        contentDescription = null,
                        tint = MmyrvPrimary,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "Lab Notifications", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = MmyrvTextDark)
                }
            },
            text = {
                Column {
                    NotificationItem(
                        title = "Physics Lab Maintenance",
                        time = "10 mins ago",
                        desc = "Lab 3 will remain closed from 2 PM to 4 PM for equipment calibration."
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    NotificationItem(
                        title = "Slot Booking Approved",
                        time = "1 hour ago",
                        desc = "Your computer lab slot for AI Practical has been confirmed."
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showNotificationDialog = false }) {
                    Text(text = "Close", color = MmyrvPrimary)
                }
            }
        )
    }

    // Profile Dialog (Clean Vector UI)
    if (showProfileDialog) {
        AlertDialog(
            onDismissRequest = { showProfileDialog = false },
            title = {
                Text(text = "User Profile", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = MmyrvTextDark)
            },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(if (isAdmin) Color(0xFFFEF3C7) else MmyrvBadgeBg),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(id = if (isAdmin) R.drawable.ic_shield else R.drawable.ic_school),
                            contentDescription = null,
                            tint = if (isAdmin) Color(0xFFD97706) else MmyrvPrimary,
                            modifier = Modifier.size(34.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(text = user.fullName, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = MmyrvTextDark)
                    Text(text = user.email, fontSize = 13.sp, color = MmyrvTextMuted)
                    Spacer(modifier = Modifier.height(6.dp))
                    Box(
                        modifier = Modifier
                            .background(
                                color = if (isAdmin) Color(0xFFFEF3C7) else MmyrvBadgeBg,
                                shape = RoundedCornerShape(8.dp)
                            )
                            .padding(horizontal = 12.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "ROLE: ${user.role.uppercase()}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isAdmin) Color(0xFFD97706) else MmyrvPrimary
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(text = "ID: ${user.rollNo}", fontSize = 12.sp, color = Color(0xFF94A3B8))

                    Spacer(modifier = Modifier.height(20.dp))

                    Button(
                        onClick = {
                            showProfileDialog = false
                            onLogoutClick()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_logout),
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = "Logout", color = Color.White)
                    }
                }
            },
            confirmButton = {}
        )
    }

    // REDESIGNED HIGH-END NAVIGATION DRAWER (SLIDE PANEL)
    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = Color.White,
                modifier = Modifier.width(320.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 18.dp, vertical = 20.dp)
                ) {
                    // 1. University Branding Banner
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.mmyrv_logo),
                            contentDescription = "MMYRV Logo",
                            modifier = Modifier
                                .size(46.dp)
                                .clip(CircleShape)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "MMYRV Lab Portal",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = MmyrvTextDark
                            )
                            Text(
                                text = "Central Laboratory System",
                                fontSize = 11.sp,
                                color = MmyrvTextMuted
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // 2. User Account Status Card
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color(0xFFF8FAFC))
                            .border(BorderStroke(1.dp, MmyrvBorder), RoundedCornerShape(14.dp))
                            .padding(12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(CircleShape)
                                    .background(if (isAdmin) Color(0xFFFEF3C7) else MmyrvBadgeBg),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    painter = painterResource(id = if (isAdmin) R.drawable.ic_shield else R.drawable.ic_school),
                                    contentDescription = null,
                                    tint = if (isAdmin) Color(0xFFD97706) else MmyrvPrimary,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = user.fullName,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.5.sp,
                                    color = MmyrvTextDark,
                                    maxLines = 1
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(6.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFF10B981))
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "${user.role.uppercase()} • ${user.rollNo}",
                                        fontSize = 10.5.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = if (isAdmin) Color(0xFFD97706) else MmyrvPrimary,
                                        maxLines = 1
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // 3. Category: Main Workspace Navigation
                    Text(
                        text = "MAIN NAVIGATION",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF94A3B8),
                        letterSpacing = 0.8.sp,
                        modifier = Modifier.padding(horizontal = 6.dp)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    DrawerMenuItem(
                        title = "Home Dashboard",
                        subtitle = "Overview & metrics",
                        iconRes = R.drawable.ic_home,
                        isSelected = selectedTab == PortalTab.HOME
                    ) {
                        selectedTab = PortalTab.HOME
                        coroutineScope.launch { drawerState.close() }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    DrawerMenuItem(
                        title = if (isAdmin) "Today's Live Labs" else "My Attendance",
                        subtitle = if (isAdmin) "Active sessions & batches" else "Practical presence logs",
                        iconRes = if (isAdmin) R.drawable.ic_calendar_today else R.drawable.ic_attendance,
                        badgeText = if (isAdmin) "LIVE" else "88.5%",
                        isSelected = selectedTab == PortalTab.SECONDARY
                    ) {
                        selectedTab = PortalTab.SECONDARY
                        coroutineScope.launch { drawerState.close() }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    DrawerMenuItem(
                        title = "Resources & Manuals",
                        subtitle = "Syllabus, guides & PDFs",
                        iconRes = R.drawable.ic_resources,
                        isSelected = selectedTab == PortalTab.RESOURCES
                    ) {
                        selectedTab = PortalTab.RESOURCES
                        coroutineScope.launch { drawerState.close() }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    DrawerMenuItem(
                        title = "Settings & Preferences",
                        subtitle = "Notifications & biometric",
                        iconRes = R.drawable.ic_settings,
                        isSelected = selectedTab == PortalTab.SETTINGS
                    ) {
                        selectedTab = PortalTab.SETTINGS
                        coroutineScope.launch { drawerState.close() }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // 4. Category: Portal Services & Support
                    Text(
                        text = "SERVICES & TOOLS",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF94A3B8),
                        letterSpacing = 0.8.sp,
                        modifier = Modifier.padding(horizontal = 6.dp)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    if (isAdmin) {
                        DrawerMenuItem(
                            title = "Student Approvals",
                            subtitle = "Verify pending student accounts",
                            iconRes = R.drawable.ic_badge,
                            badgeText = "NEW",
                            isSelected = showAdminApprovalsPage
                        ) {
                            coroutineScope.launch { drawerState.close() }
                            showAdminApprovalsPage = true
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        DrawerMenuItem(
                            title = "Courses & Programmes",
                            subtitle = "Manage degrees & semesters",
                            iconRes = R.drawable.ic_school,
                            badgeText = "NEW",
                            isSelected = showAdminCoursesPage
                        ) {
                            coroutineScope.launch { drawerState.close() }
                            showAdminCoursesPage = true
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        DrawerMenuItem(
                            title = "Admin Quick Actions",
                            subtitle = "Add slot, inventory, alert",
                            iconRes = R.drawable.ic_add,
                            isSelected = false
                        ) {
                            coroutineScope.launch { drawerState.close() }
                            showAdminAddDialog = true
                        }
                    } else {
                        DrawerMenuItem(
                            title = "Facial Attendance",
                            subtitle = "Biometric camera scan",
                            iconRes = R.drawable.ic_face_recognition,
                            isSelected = false
                        ) {
                            coroutineScope.launch { drawerState.close() }
                            showFaceScanDialog = true
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    DrawerMenuItem(
                        title = "Lab Helpdesk Support",
                        subtitle = "Contact laboratory admin",
                        iconRes = R.drawable.ic_help,
                        isSelected = false
                    ) {
                        coroutineScope.launch { drawerState.close() }
                        Toast.makeText(context, "Support Desk: labsupport@mmyrv.edu.in", Toast.LENGTH_LONG).show()
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    HorizontalDivider(color = MmyrvBorder)
                    Spacer(modifier = Modifier.height(14.dp))

                    // 5. Drawer Footer: Sign Out Button & Version
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFFFEF2F2))
                            .border(BorderStroke(1.dp, Color(0xFFFEE2E2)), RoundedCornerShape(12.dp))
                            .clickable {
                                coroutineScope.launch { drawerState.close() }
                                onLogoutClick()
                            }
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_logout),
                            contentDescription = "Logout",
                            tint = Color(0xFFEF4444),
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Sign Out from Portal",
                            color = Color(0xFFEF4444),
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "MMYRV Portal v1.0.4 Enterprise • Cloud Edition",
                        fontSize = 10.sp,
                        color = Color(0xFF94A3B8),
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                }
            }
        }
    ) {
        Scaffold(
            topBar = {
                // Universal Top Header
                PortalTopBar(
                    onMenuClick = { coroutineScope.launch { drawerState.open() } },
                    onNotificationClick = { showNotificationDialog = true },
                    onProfileClick = { showProfileDialog = true },
                    hasUnreadNotifications = true
                )
            },
            bottomBar = {
                // Floating Curved Bottom Navigation Bar
                PortalBottomBar(
                    selectedTab = selectedTab,
                    onTabSelected = { selectedTab = it },
                    onCenterFabClick = {
                        if (isAdmin) {
                            showAdminAddDialog = true
                        } else {
                            showFaceScanDialog = true
                        }
                    },
                    isAdmin = isAdmin
                )
            },
            containerColor = Color(0xFFF8FAFC)
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                // Dynamic Tab Switching
                when (selectedTab) {
                    PortalTab.HOME -> {
                        if (isAdmin) {
                            AdminDashboardContent(user = user)
                        } else {
                            StudentDashboardContent(user = user)
                        }
                    }
                    PortalTab.SECONDARY -> {
                        if (isAdmin) {
                            TodaysLabContent(user = user)
                        } else {
                            AttendanceContent(user = user)
                        }
                    }
                    PortalTab.RESOURCES -> {
                        ResourcesContent(user = user)
                    }
                    PortalTab.SETTINGS -> {
                        SettingsContent(user = user, onLogoutClick = onLogoutClick)
                    }
                }
            }
        }
    }
}

@Composable
fun DrawerMenuItem(
    title: String,
    iconRes: Int,
    subtitle: String? = null,
    badgeText: String? = null,
    isSelected: Boolean = false,
    onClick: () -> Unit
) {
    val itemBg = if (isSelected) Color(0xFFEEF2FF) else Color.Transparent

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(itemBg)
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (isSelected) MmyrvPrimary else Color(0xFFF1F5F9)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(id = iconRes),
                        contentDescription = title,
                        tint = if (isSelected) Color.White else Color(0xFF475569),
                        modifier = Modifier.size(18.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = title,
                        fontSize = 13.5.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                        color = if (isSelected) MmyrvPrimary else MmyrvTextDark
                    )
                    if (subtitle != null) {
                        Spacer(modifier = Modifier.height(1.dp))
                        Text(
                            text = subtitle,
                            fontSize = 10.5.sp,
                            color = MmyrvTextMuted
                        )
                    }
                }
            }

            if (badgeText != null) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (isSelected) MmyrvPrimary.copy(alpha = 0.15f) else Color(0xFFF1F5F9))
                        .padding(horizontal = 7.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = badgeText,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isSelected) MmyrvPrimary else Color(0xFF64748B)
                    )
                }
            } else if (isSelected) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(MmyrvPrimary)
                )
            }
        }
    }
}

@Composable
fun NotificationItem(
    title: String,
    time: String,
    desc: String
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFF8FAFC), shape = RoundedCornerShape(10.dp))
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = title, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MmyrvTextDark)
            Text(text = time, fontSize = 11.sp, color = Color(0xFF94A3B8))
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = desc, fontSize = 12.sp, color = MmyrvTextMuted, lineHeight = 16.sp)
    }
}

@Composable
fun AdminQuickActionButton(
    title: String,
    desc: String,
    iconRes: Int,
    iconColor: Color = MmyrvPrimary,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFFF8FAFC))
            .border(BorderStroke(1.dp, MmyrvBorder), RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(iconColor.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = iconRes),
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(text = title, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MmyrvTextDark)
                Spacer(modifier = Modifier.height(2.dp))
                Text(text = desc, fontSize = 11.sp, color = MmyrvTextMuted, lineHeight = 15.sp)
            }
        }
    }
}
