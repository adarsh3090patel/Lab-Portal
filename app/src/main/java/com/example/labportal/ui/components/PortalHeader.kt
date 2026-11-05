package com.example.labportal.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.labportal.R
import com.example.labportal.ui.theme.MmyrvPrimary
import com.example.labportal.ui.theme.MmyrvTextDark
import com.example.labportal.ui.theme.MmyrvTextMuted

@Composable
fun PortalHeader(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Circular College Logo with subtle elevation
        Image(
            painter = painterResource(id = R.drawable.mmyrv_logo),
            contentDescription = "MMYRV Logo",
            modifier = Modifier
                .size(92.dp)
                .shadow(elevation = 6.dp, shape = CircleShape)
                .clip(CircleShape)
                .background(Color.White)
        )

        Spacer(modifier = Modifier.height(18.dp))

        // Title: "MMYRV Lab Portal"
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "MMYRV ",
                fontSize = 26.sp,
                fontWeight = FontWeight.ExtraBold,
                color = MmyrvTextDark
            )
            Text(
                text = "Lab Portal",
                fontSize = 26.sp,
                fontWeight = FontWeight.ExtraBold,
                color = MmyrvPrimary
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Subtitle: "Maharishi Mahesh Yogi Ramayan University"
        Text(
            text = "Maharishi Mahesh Yogi Ramayan University",
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = MmyrvTextMuted,
            textAlign = TextAlign.Center
        )
    }
}

