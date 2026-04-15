package com.alaa.mohamedabdulazim.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alaa.mohamedabdulazim.R
import com.alaa.mohamedabdulazim.ui.theme.IslamicGold
import com.alaa.mohamedabdulazim.ui.theme.IslamicGreen
import com.alaa.mohamedabdulazim.ui.theme.IslamicGreenDark

@Composable
fun FatherHeader(modifier: Modifier = Modifier, compact: Boolean = false) {
    val gradient = Brush.verticalGradient(listOf(IslamicGreenDark, IslamicGreen))
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(gradient, RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp))
            .padding(if (compact) 12.dp else 20.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            if (!compact) {
                Box(
                    modifier = Modifier
                        .size(90.dp)
                        .clip(CircleShape)
                        .border(3.dp, IslamicGold, CircleShape)
                        .background(Color(0xFF2E5E2E), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    // Try to load father's photo; fallback to mosque icon
                    Image(
                        painter = painterResource(R.drawable.father_photo),
                        contentDescription = "صورة الوالد",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
                Spacer(Modifier.height(10.dp))
            }
            Text(
                text = "محمد عبد العظيم الطويل",
                color = IslamicGold,
                fontSize = if (compact) 16.sp else 20.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Text(
                text = "رحمه الله وأسكنه فسيح جناته",
                color = Color.White.copy(alpha = 0.85f),
                fontSize = if (compact) 11.sp else 13.sp,
                textAlign = TextAlign.Center
            )
            if (!compact) {
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "❝ اللهم اغفر له وارحمه وعافه واعف عنه ❞",
                    color = IslamicGold.copy(alpha = 0.8f),
                    fontSize = 11.sp,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}
