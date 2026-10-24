package com.example.guardianai.ui.main

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.guardianai.services.ListenerStatus
import com.example.guardianai.theme.*
import com.example.guardianai.ui.components.pressClickable
import com.example.guardianai.ui.screens.*

enum class GuardianTab(val label: String, val icon: ImageVector) {
    PIPELINE("Pipeline", Icons.Default.Security),
    SOS("SOS", Icons.Default.Emergency),
    VAULT("Vault", Icons.Default.Lock),
    COUNSELOR("Companion", Icons.Default.SupportAgent),
    METRICS("Dashboard", Icons.Default.Dashboard)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen() {
    var selectedTab by remember { mutableStateOf(GuardianTab.PIPELINE) }
    val isConnected by ListenerStatus.isConnected.collectAsState()
    val isUserToggleOn by ListenerStatus.isUserToggleOn.collectAsState()
    val haptic = LocalHapticFeedback.current
    val context = androidx.compose.ui.platform.LocalContext.current

    val infiniteTransition = rememberInfiniteTransition(label = "status_pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.25f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dot_scale"
    )
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.35f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dot_alpha"
    )

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = AppBackground, // #FBF8F3 Warm Ivory/Cream foundation
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            // Mobile Editorial Top Bar
            Surface(
                color = AppBackground,
                shadowElevation = 2.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Left: Brand Logo & Status Pill
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Guardian",
                            color = GuardianSienna, // #8B3B26
                            fontFamily = FontFamily.Serif,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        val pillColor = when {
                            !isUserToggleOn -> StatusAmber
                            isConnected -> StatusEmerald
                            else -> GuardianCrimson
                        }
                        val pillText = when {
                            !isUserToggleOn -> "PAUSED"
                            isConnected -> "ACTIVE"
                            else -> "DETACHED"
                        }
                        Surface(
                            color = pillColor.copy(alpha = 0.12f),
                            shape = RoundedCornerShape(100.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, pillColor),
                            modifier = Modifier.pressClickable {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                ListenerStatus.setUserToggleOn(context, !isUserToggleOn)
                            }
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .scale(if (isConnected && isUserToggleOn) pulseScale else 1f)
                                        .alpha(if (isConnected && isUserToggleOn) pulseAlpha else 1f)
                                        .background(pillColor, CircleShape)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = pillText,
                                    color = pillColor,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp
                                )
                            }
                        }
                    }

                    // Right: Quick Action / Profile Icon
                    Surface(
                        shape = CircleShape,
                        color = CardSand,
                        border = androidx.compose.foundation.BorderStroke(1.dp, BorderSand),
                        modifier = Modifier.size(38.dp).pressClickable {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        }
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.AccountCircle, contentDescription = "Profile", tint = GuardianSienna, modifier = Modifier.size(24.dp))
                        }
                    }
                }
            }
        },
        bottomBar = {
            // Editorial Mobile Bottom Navigation Bar
            Surface(
                color = CardSandElevated, // #E5E0D6
                shadowElevation = 8.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    GuardianTab.values().forEach { tab ->
                        val isSelected = selectedTab == tab
                        val pillColor by animateColorAsState(
                            targetValue = if (isSelected) GuardianSienna else Color.Transparent,
                            label = "tab_pill_color"
                        )
                        val contentColor by animateColorAsState(
                            targetValue = if (isSelected) Color.White else TextSubtitle,
                            label = "tab_content_color"
                        )

                        Surface(
                            color = pillColor,
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 2.dp)
                                .pressClickable {
                                    if (selectedTab != tab) {
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                        selectedTab = tab
                                    }
                                }
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center,
                                modifier = Modifier.padding(vertical = 8.dp)
                            ) {
                                Icon(
                                    imageVector = tab.icon,
                                    contentDescription = tab.label,
                                    tint = contentColor,
                                    modifier = Modifier.size(22.dp)
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = tab.label,
                                    color = contentColor,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    fontSize = 11.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (selectedTab) {
                GuardianTab.PIPELINE -> PipelineLiveScreen()
                GuardianTab.SOS -> EmergencyCenterScreen()
                GuardianTab.VAULT -> EvidenceVaultScreen()
                GuardianTab.COUNSELOR -> CompanionChatScreen()
                GuardianTab.METRICS -> CounselorDashboardScreen()
            }
        }
    }
}
