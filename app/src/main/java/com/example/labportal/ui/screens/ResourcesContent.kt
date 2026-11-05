package com.example.labportal.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
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
import com.example.labportal.data.model.UserData
import com.example.labportal.ui.theme.MmyrvBorder
import com.example.labportal.ui.theme.MmyrvPrimary
import com.example.labportal.ui.theme.MmyrvTextDark
import com.example.labportal.ui.theme.MmyrvTextMuted

@Composable
fun ResourcesContent(
    user: UserData
) {
    val isAdmin = user.role.equals("Admin", ignoreCase = true)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text(
            text = if (isAdmin) "Lab Inventory & Documents" else "Study Resources & Lab Manuals",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = MmyrvTextDark
        )
        Text(
            text = if (isAdmin) "Central university lab hardware and equipment files" else "Official MMYRV experiment guides and manuals",
            fontSize = 12.sp,
            color = MmyrvTextMuted
        )

        Spacer(modifier = Modifier.height(16.dp))

        ResourceItemCard(
            title = "Data Structures & Algorithms Practical Manual",
            category = "CSE Lab 1",
            fileSize = "4.2 MB PDF",
            badgeColor = MmyrvPrimary
        )

        Spacer(modifier = Modifier.height(10.dp))

        ResourceItemCard(
            title = "Digital Signal Processing Lab Manual 2026",
            category = "ECE Lab 3",
            fileSize = "6.8 MB PDF",
            badgeColor = Color(0xFF10B981)
        )

        Spacer(modifier = Modifier.height(10.dp))

        ResourceItemCard(
            title = "Microbiology & Bio-Lab Safety Protocols",
            category = "BioTech Lab",
            fileSize = "2.1 MB PDF",
            badgeColor = Color(0xFFF59E0B)
        )

        Spacer(modifier = Modifier.height(10.dp))

        ResourceItemCard(
            title = "Chemistry Lab Reagent Preparation Guidelines",
            category = "Chemistry Lab",
            fileSize = "3.5 MB PDF",
            badgeColor = Color(0xFF8B5CF6)
        )
    }
}

@Composable
fun ResourceItemCard(
    title: String,
    category: String,
    fileSize: String,
    badgeColor: Color
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, MmyrvBorder)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(badgeColor.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_resources),
                        contentDescription = null,
                        tint = badgeColor,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(text = title, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MmyrvTextDark, maxLines = 1)
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(text = "$category • $fileSize", fontSize = 11.sp, color = MmyrvTextMuted)
                }
            }
            Button(
                onClick = {},
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF1F5F9)),
                modifier = Modifier.height(34.dp)
            ) {
                Text(text = "View", color = MmyrvTextDark, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

