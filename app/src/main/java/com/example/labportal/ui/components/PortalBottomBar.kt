package com.example.labportal.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.labportal.R
import com.example.labportal.ui.theme.MmyrvBadgeBg
import com.example.labportal.ui.theme.MmyrvBorder
import com.example.labportal.ui.theme.MmyrvPrimary
import com.example.labportal.ui.theme.MmyrvTextDark
import com.example.labportal.ui.theme.MmyrvTextMuted

enum class PortalTab {
    HOME,
    SECONDARY, // Attendance (Student) or Today's (Admin)
    RESOURCES,
    SETTINGS
}

@Composable
fun PortalBottomBar(
    selectedTab: PortalTab,
    onTabSelected: (PortalTab) -> Unit,
    onCenterFabClick: () -> Unit,
    isAdmin: Boolean = false,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        contentAlignment = Alignment.BottomCenter
    ) {
        // Floating Rounded Pill Bar
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(68.dp),
            shape = RoundedCornerShape(36.dp),
            color = Color.White,
            shadowElevation = 10.dp,
            border = BorderStroke(1.dp, MmyrvBorder)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                // 1. Home Tab
                NavItem(
                    title = "Home",
                    iconRes = R.drawable.ic_home,
                    isSelected = selectedTab == PortalTab.HOME,
                    onClick = { onTabSelected(PortalTab.HOME) }
                )

                // 2. Attendance (Student) or Today's (Admin)
                NavItem(
                    title = if (isAdmin) "Today's" else "Attendance",
                    iconRes = if (isAdmin) R.drawable.ic_calendar_today else R.drawable.ic_attendance,
                    isSelected = selectedTab == PortalTab.SECONDARY,
                    onClick = { onTabSelected(PortalTab.SECONDARY) }
                )

                // Gap for the elevated center floating button
                Spacer(modifier = Modifier.size(52.dp))

                // 4. Resources
                NavItem(
                    title = "Resources",
                    iconRes = R.drawable.ic_resources,
                    isSelected = selectedTab == PortalTab.RESOURCES,
                    onClick = { onTabSelected(PortalTab.RESOURCES) }
                )

                // 5. Settings
                NavItem(
                    title = "Settings",
                    iconRes = R.drawable.ic_settings,
                    isSelected = selectedTab == PortalTab.SETTINGS,
                    onClick = { onTabSelected(PortalTab.SETTINGS) }
                )
            }
        }

        // 3. Elevated Center Floating Action Button (FAB)
        Box(
            modifier = Modifier
                .offset(y = (-18).dp)
                .size(62.dp)
                .shadow(elevation = 12.dp, shape = CircleShape)
                .clip(CircleShape)
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF3B82F6),
                            Color(0xFF2563EB)
                        )
                    )
                )
                .border(width = 3.dp, color = Color.White, shape = CircleShape)
                .clickable { onCenterFabClick() },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(
                    id = if (isAdmin) R.drawable.ic_add else R.drawable.ic_face_recognition
                ),
                contentDescription = if (isAdmin) "Add Lab/Slot" else "Facial Recognition Attendance",
                tint = Color.White,
                modifier = Modifier.size(if (isAdmin) 28.dp else 30.dp)
            )
        }
    }
}

@Composable
private fun NavItem(
    title: String,
    iconRes: Int,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }

    val iconTint by animateColorAsState(
        targetValue = if (isSelected) MmyrvPrimary else MmyrvTextMuted,
        label = "iconTint"
    )

    val textColor by animateColorAsState(
        targetValue = if (isSelected) MmyrvPrimary else MmyrvTextMuted,
        label = "textColor"
    )

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(18.dp))
            .background(if (isSelected) MmyrvBadgeBg else Color.Transparent)
            .clickable(
                interactionSource = interactionSource,
                indication = null
            ) { onClick() }
            .padding(horizontal = 12.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                painter = painterResource(id = iconRes),
                contentDescription = title,
                tint = iconTint,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = title,
                fontSize = 11.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = textColor
            )
        }
    }
}

