package com.example.labportal.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.labportal.R
import com.example.labportal.data.model.UserData
import com.example.labportal.ui.theme.AppFontFamily
import com.example.labportal.ui.theme.MmyrvBorder
import com.example.labportal.ui.theme.MmyrvPrimary
import com.example.labportal.ui.theme.MmyrvTextDark
import com.example.labportal.ui.theme.MmyrvTextMuted
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

private data class GreetingInfo(
    val greeting: String,
    val iconRes: Int,
    val iconTint: Color,
    val subtitle: String
)

private fun calculateGreeting(isAdmin: Boolean, hour: Int): GreetingInfo {
    return when (hour) {
        in 5..11 -> GreetingInfo(
            greeting = "Good morning",
            iconRes = R.drawable.ic_sun,
            iconTint = Color(0xFFF59E0B),
            subtitle = if (isAdmin) "Ready for today's lab schedule." else "Start your day with productive lab practicals."
        )
        in 12..16 -> GreetingInfo(
            greeting = "Good afternoon",
            iconRes = R.drawable.ic_sun,
            iconTint = Color(0xFFF59E0B),
            subtitle = if (isAdmin) "Active lab practicals are in progress." else "Keep up the momentum and stay focused."
        )
        in 17..21 -> GreetingInfo(
            greeting = "Good evening",
            iconRes = R.drawable.ic_twilight,
            iconTint = Color(0xFFF97316),
            subtitle = if (isAdmin) "Wrap up today's equipment and lab records." else "Review your lab reports and practical notes."
        )
        else -> GreetingInfo(
            greeting = "Good evening",
            iconRes = R.drawable.ic_moon,
            iconTint = Color(0xFF818CF8),
            subtitle = if (isAdmin) "Campus labs are secured for the night." else "Rest well and prepare for tomorrow's labs."
        )
    }
}

@Composable
fun GreetingHeaderSection(
    user: UserData,
    modifier: Modifier = Modifier
) {
    val isAdmin = user.role.equals("Admin", ignoreCase = true)

    // Current hour of day
    val currentHour = remember { Calendar.getInstance().get(Calendar.HOUR_OF_DAY) }
    val greetingInfo = remember(currentHour, isAdmin) {
        calculateGreeting(isAdmin = isAdmin, hour = currentHour)
    }

    // Display Name: "Admin" for admin, or Student's first name for student
    val displayName = remember(user.fullName, isAdmin) {
        if (isAdmin) {
            "Admin"
        } else {
            val first = user.fullName.trim().split(" ").firstOrNull { it.isNotBlank() } ?: "Student"
            first.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
        }
    }

    // Current date formatted dynamically, e.g. "Thu, Nov 5"
    val currentDateText = remember {
        SimpleDateFormat("EEE, MMM d", Locale.ENGLISH).format(Date())
    }

    // Responsive screen size adjustments
    val configuration = LocalConfiguration.current
    val screenWidthDp = configuration.screenWidthDp

    val isSmallScreen = screenWidthDp < 360
    val isMediumScreen = screenWidthDp in 360..400

    val titleFontSize = when {
        isSmallScreen -> 16.sp
        isMediumScreen -> 18.5.sp
        else -> 20.sp
    }

    val subtitleFontSize = when {
        isSmallScreen -> 11.sp
        isMediumScreen -> 12.sp
        else -> 12.5.sp
    }

    val pillFontSize = when {
        isSmallScreen -> 11.sp
        isMediumScreen -> 12.sp
        else -> 12.5.sp
    }

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Left Column: Greeting with Name and dynamic Subtitle
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(end = if (isSmallScreen) 8.dp else 12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = buildAnnotatedString {
                        withStyle(SpanStyle(fontFamily = AppFontFamily, fontWeight = FontWeight.Bold, color = MmyrvTextDark)) {
                            append("${greetingInfo.greeting} , ")
                        }
                        withStyle(SpanStyle(fontFamily = AppFontFamily, fontWeight = FontWeight.Bold, color = MmyrvPrimary)) {
                            append(displayName)
                        }
                    },
                    fontSize = titleFontSize,
                    fontFamily = AppFontFamily,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false)
                )

                Spacer(modifier = Modifier.width(6.dp))

                Icon(
                    painter = painterResource(id = greetingInfo.iconRes),
                    contentDescription = null,
                    tint = greetingInfo.iconTint,
                    modifier = Modifier.size(if (isSmallScreen) 18.dp else 21.dp)
                )
            }

            Spacer(modifier = Modifier.height(3.dp))

            Text(
                text = greetingInfo.subtitle,
                fontSize = subtitleFontSize,
                fontFamily = AppFontFamily,
                color = MmyrvTextMuted,
                fontWeight = FontWeight.Normal,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        // Right Badge: Capsule Date Pill
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = Color.White,
            border = BorderStroke(1.dp, MmyrvBorder),
            shadowElevation = 0.5.dp
        ) {
            Row(
                modifier = Modifier.padding(
                    horizontal = if (isSmallScreen) 10.dp else 14.dp,
                    vertical = if (isSmallScreen) 6.dp else 7.dp
                ),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_calendar_today),
                    contentDescription = "Current Date",
                    tint = MmyrvPrimary,
                    modifier = Modifier.size(if (isSmallScreen) 14.dp else 16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = currentDateText,
                    fontSize = pillFontSize,
                    fontFamily = AppFontFamily,
                    fontWeight = FontWeight.SemiBold,
                    color = MmyrvTextDark
                )
            }
        }
    }
}
