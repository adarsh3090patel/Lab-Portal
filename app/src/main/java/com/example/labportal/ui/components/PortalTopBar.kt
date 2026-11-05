package com.example.labportal.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.labportal.R
import com.example.labportal.ui.theme.MmyrvBorder
import com.example.labportal.ui.theme.MmyrvPrimary
import com.example.labportal.ui.theme.MmyrvTextDark

@Composable
fun PortalTopBar(
    onMenuClick: () -> Unit = {},
    onNotificationClick: () -> Unit = {},
    onProfileClick: () -> Unit = {},
    hasUnreadNotifications: Boolean = true
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .statusBarsPadding()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp)
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Left: Hamburger Menu Icon
            IconButton(
                onClick = onMenuClick,
                modifier = Modifier.size(44.dp)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_menu),
                    contentDescription = "Navigation Menu",
                    tint = MmyrvTextDark,
                    modifier = Modifier.size(24.dp)
                )
            }

            // Center: MMYRV Lab Portal Title
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "MMYRV ",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = MmyrvTextDark
                )
                Text(
                    text = "Lab Portal",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = MmyrvPrimary
                )
            }

            // Right: Notification Icon + Profile Icon
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Notification Bell with unread indicator dot
                Box(
                    contentAlignment = Alignment.Center
                ) {
                    IconButton(
                        onClick = onNotificationClick,
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_notifications),
                            contentDescription = "Notifications",
                            tint = MmyrvTextDark,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    // Red Badge Dot for unread notifications
                    if (hasUnreadNotifications) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .offset(x = 8.dp, y = (-8).dp)
                                .clip(CircleShape)
                                .background(Color(0xFFEF4444))
                        )
                    }
                }

                Spacer(modifier = Modifier.width(4.dp))

                // Profile Avatar Icon
                IconButton(
                    onClick = onProfileClick,
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_account_circle),
                        contentDescription = "Profile",
                        tint = MmyrvPrimary,
                        modifier = Modifier.size(26.dp)
                    )
                }
            }
        }

        // Clean subtle bottom divider
        HorizontalDivider(
            color = MmyrvBorder,
            thickness = 1.dp
        )
    }
}

