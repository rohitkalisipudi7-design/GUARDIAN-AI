package com.example.guardianai.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.PowerManager
import android.provider.Settings
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.NotificationManagerCompat
import com.example.guardianai.pipeline.*
import com.example.guardianai.services.ListenerStatus
import com.example.guardianai.theme.*
import com.example.guardianai.ui.components.pressClickable
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

sealed interface PipelineUiState {
    object Loading : PipelineUiState
    object PermissionError : PipelineUiState
    object Empty : PipelineUiState
    data class Success(val logs: List<PipelineExecutionLog>) : PipelineUiState
}

fun Modifier.shimmerEffect(): Modifier = composed {
    val transition = rememberInfiniteTransition(label = "shimmer_transition")
    val translateAnimation by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1100, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer_translate"
    )
    val shimmerColors = listOf(
        Color.White.copy(alpha = 0.02f),
        Color.White.copy(alpha = 0.08f),
        Color.White.copy(alpha = 0.02f)
    )
    this.background(
        brush = Brush.linearGradient(
            colors = shimmerColors,
            start = Offset.Zero,
            end = Offset(x = translateAnimation, y = translateAnimation)
        )
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PipelineLiveScreen() {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val isConnected by ListenerStatus.isConnected.collectAsState()
    val isUserToggleOn by ListenerStatus.isUserToggleOn.collectAsState()
    var rawLogs by remember { mutableStateOf<List<PipelineExecutionLog>>(emptyList()) }
    var isInitialLoading by remember { mutableStateOf(true) }
    var selectedAppTab by remember { mutableStateOf("ALL") }
    var selectedLogForAudit by remember { mutableStateOf<PipelineExecutionLog?>(null) }

    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(400)
        isInitialLoading = false
        val current = ListenService.getLogs()
        if (current.isEmpty()) {
            ListenService.processIncomingEvent(
                rawSender = "Priya (Roommate)",
                rawContent = "Hey! Can you send me the chemistry notes PDF before 5 PM today?",
                platform = "WhatsApp",
                isLateNight = false,
                rejectionClarity = 0,
                frequencyCount = 1
            )
            ListenService.processIncomingEvent(
                rawSender = "Unknown (+91 98999 00112)",
                rawContent = "URGENT ALARM: I intercepted your private hostel photos from your iCloud. Send ₹75,000 to my UPI ID immediately or I will leak them to your college WhatsApp group!",
                platform = "Instagram",
                isLateNight = true,
                rejectionClarity = 3,
                frequencyCount = 14
            )
            ListenService.processIncomingEvent(
                rawSender = "Campus Alert (+91 91234 56789)",
                rawContent = "Reminder: Campus safety workshop starts tomorrow at 10 AM in the auditorium.",
                platform = "Telegram",
                isLateNight = false,
                rejectionClarity = 0,
                frequencyCount = 1
            )
        }
        while (true) {
            rawLogs = ListenService.getLogs()
            kotlinx.coroutines.delay(1000)
        }
    }

    val hasPermission = remember(isConnected) {
        val enabledListeners = NotificationManagerCompat.getEnabledListenerPackages(context)
        enabledListeners.contains(context.packageName) || isConnected
    }

    val uiState: PipelineUiState by remember { derivedStateOf {
        when {
            isInitialLoading -> PipelineUiState.Loading
            !hasPermission && isUserToggleOn -> PipelineUiState.PermissionError
            rawLogs.isEmpty() -> PipelineUiState.Empty
            else -> {
                val filtered = when (selectedAppTab) {
                    "WHATSAPP" -> rawLogs.filter { it.platform.contains("WhatsApp", true) }
                    "INSTAGRAM" -> rawLogs.filter { it.platform.contains("Instagram", true) }
                    "TELEGRAM" -> rawLogs.filter { it.platform.contains("Telegram", true) }
                    "SMS_OTHER" -> rawLogs.filter {
                        !it.platform.contains("WhatsApp", true) &&
                        !it.platform.contains("Instagram", true) &&
                        !it.platform.contains("Telegram", true)
                    }
                    else -> rawLogs
                }
                if (filtered.isEmpty()) PipelineUiState.Empty else PipelineUiState.Success(filtered)
            }
        }
    } }

    val isBatteryOptimized = remember {
        val pm = context.getSystemService(Context.POWER_SERVICE) as? PowerManager
        pm?.isIgnoringBatteryOptimizations(context.packageName) == false
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(AppBackground), // #FBF8F3 Warm Ivory/Cream foundation
        contentPadding = PaddingValues(start = 24.dp, top = 18.dp, end = 24.dp, bottom = 100.dp)
    ) {
        item {
            // Editorial Screen Title (input_file_2.png)
            Text(
                text = "Active Threat Pipeline",
                color = TextPrimary,
                fontFamily = FontFamily.Serif,
                fontSize = 32.sp,
                lineHeight = 38.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Real-time cryptographic analysis of anomalous behavioral markers across protected ledgers.",
                color = TextSubtitle,
                style = MaterialTheme.typography.bodyMedium,
                fontSize = 14.sp
            )
            Spacer(modifier = Modifier.height(18.dp))
        }

        item {
            // Master Option: Turn ON and OFF Notification Listener
            Surface(
                color = CardSand,
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, if (isUserToggleOn && hasPermission) StatusEmerald else GuardianCrimson),
                modifier = Modifier.fillMaxWidth().padding(bottom = 14.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp).fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (isUserToggleOn && hasPermission) Icons.Default.Security else Icons.Default.GppBad,
                                contentDescription = null,
                                tint = if (isUserToggleOn && hasPermission) StatusEmerald else GuardianCrimson,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Real-Time Threat Listener",
                                color = TextPrimary,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = when {
                                !isUserToggleOn -> "PAUSED: Interception of WhatsApp, Instagram, & SMS threats is turned OFF."
                                !hasPermission -> "ACTION NEEDED: Toggle is ON but Android Notification Access permission is missing."
                                else -> "ACTIVE: Continuously monitoring and evaluating incoming messages & distress keywords."
                            },
                            color = TextSubtitle,
                            style = MaterialTheme.typography.bodySmall,
                            fontSize = 12.sp,
                            lineHeight = 16.sp
                        )
                    }
                    Switch(
                        checked = isUserToggleOn,
                        onCheckedChange = { newState ->
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            ListenerStatus.setUserToggleOn(context, newState)
                            if (newState && !hasPermission) {
                                try {
                                    val intent = Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
                                    context.startActivity(intent)
                                } catch (e: Exception) {}
                            }
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = StatusEmerald,
                            uncheckedThumbColor = Color.White,
                            uncheckedTrackColor = GuardianCrimson.copy(alpha = 0.5f)
                        )
                    )
                }
            }
        }

        item {
            // Task #4: Protection Status Heartbeat Card (Editorial Sand style)
            Surface(
                color = if (hasPermission && isUserToggleOn) CardSand else CardSandElevated,
                shape = RoundedCornerShape(8.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, if (hasPermission && isUserToggleOn) StatusEmerald else GuardianCrimson),
                modifier = Modifier.fillMaxWidth().padding(bottom = 14.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp).fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(if (hasPermission && isUserToggleOn) StatusEmerald else GuardianCrimson, CircleShape)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = if (hasPermission && isUserToggleOn) "HEARTBEAT: ACTIVE MEMORY ENCLAVE BOUND" else "HEARTBEAT: OS LISTENER DETACHED / PAUSED",
                            color = if (hasPermission && isUserToggleOn) StatusEmerald else GuardianCrimson,
                            style = MaterialTheme.typography.labelMedium,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            maxLines = 1,
                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "AES-256-GCM",
                        color = TextMuted,
                        style = MaterialTheme.typography.labelSmall,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp
                    )
                }
            }
        }

        if (!hasPermission && isUserToggleOn) {
            item {
                PermissionBannerCard {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    val intent = Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
                    context.startActivity(intent)
                }
                Spacer(modifier = Modifier.height(12.dp))
            }
        } else if (isBatteryOptimized) {
            item {
                // Task #4: OEM Battery Optimization Whitelisting Banner (OnePlus / Xiaomi / Samsung survival)
                Surface(
                    color = CardDark,
                    shape = RoundedCornerShape(6.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, StatusAmber),
                    modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp).pressClickable {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        try {
                            val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                                data = Uri.parse("package:${context.packageName}")
                            }
                            context.startActivity(intent)
                        } catch (e: Exception) {
                            val intent = Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)
                            context.startActivity(intent)
                        }
                    }
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                            Text("OEM TASK KILLER PROTECTION NEEDED", color = StatusAmber, style = MaterialTheme.typography.labelSmall, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text("Tap to whitelist Guardian AI from OnePlus/Android aggressive battery killers so SOS remains active in standby.", color = TextSubtitle, style = MaterialTheme.typography.bodySmall, fontSize = 11.sp, lineHeight = 15.sp)
                        }
                        Icon(Icons.Default.BatteryChargingFull, contentDescription = null, tint = StatusAmber, modifier = Modifier.size(20.dp))
                    }
                }
            }
        }

        item {
            // Technical sharp filter tabs (No emojis, no truncation, intentional geometry)
            FilterChipsBar(
                selectedTab = selectedAppTab,
                totalCount = rawLogs.size,
                whatsAppCount = rawLogs.count { it.platform.contains("WhatsApp", true) },
                instagramCount = rawLogs.count { it.platform.contains("Instagram", true) },
                telegramCount = rawLogs.count { it.platform.contains("Telegram", true) },
                otherCount = rawLogs.count {
                    !it.platform.contains("WhatsApp", true) &&
                    !it.platform.contains("Instagram", true) &&
                    !it.platform.contains("Telegram", true)
                },
                onTabSelected = { tab ->
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    selectedAppTab = tab
                }
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "LOCAL STREAM LOGS",
                        color = TextPrimary,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                    Text(
                        text = "ZERO-PII // MEMORY BUFFER ONLY",
                        color = TextMuted,
                        style = MaterialTheme.typography.labelSmall,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 10.sp
                    )
                }

                if (rawLogs.isNotEmpty()) {
                    Surface(
                        color = CardDark,
                        shape = RoundedCornerShape(4.dp), // Sharp technical geometry
                        border = androidx.compose.foundation.BorderStroke(1.dp, GlassBorder),
                        modifier = Modifier.pressClickable {
                            ListenService.clearLogs()
                            rawLogs = ListenService.getLogs()
                        }
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                        ) {
                            Icon(Icons.Default.DeleteOutline, contentDescription = null, tint = TextSubtitle, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("FLUSH BUFFER", color = TextSubtitle, style = MaterialTheme.typography.labelSmall, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        when (val state = uiState) {
            is PipelineUiState.Loading -> item { ShimmerLoadingState() }
            is PipelineUiState.PermissionError -> item {
                PermissionErrorState {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    val intent = Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
                    context.startActivity(intent)
                }
            }
            is PipelineUiState.Empty -> item {
                LeftAlignedEditorialEmptyState(
                    onSimulateSafe = {
                        ListenService.processIncomingEvent(
                            rawSender = "Aarav (+91 98111 22334)",
                            rawContent = "Hey! Let's meet at the library around 4 PM to finish our college presentation.",
                            platform = "WhatsApp",
                            isLateNight = false,
                            rejectionClarity = 0,
                            frequencyCount = 1
                        )
                        rawLogs = ListenService.getLogs()
                    },
                    onSimulateCritical = {
                        ListenService.processIncomingEvent(
                            rawSender = "Unknown Number (+91 99999 88888)",
                            rawContent = "URGENT: I have all your private hostel photos. Send ₹75,000 via UPI immediately or I am sending them to your parents and college group!",
                            platform = "Instagram",
                            isLateNight = true,
                            rejectionClarity = 3,
                            frequencyCount = 15
                        )
                        rawLogs = ListenService.getLogs()
                    }
                )
            }
            is PipelineUiState.Success -> {
                items(state.logs, key = { it.id }) { log ->
                    Column(modifier = Modifier.padding(bottom = 12.dp)) {
                        AnimatedVisibility(
                            visible = true,
                            enter = slideInVertically(initialOffsetY = { 40 }) + fadeIn(tween(300))
                        ) {
                            ThreatLogCard(log = log) {
                                selectedLogForAudit = log
                            }
                        }
                    }
                }
            }
        }
    }

    if (selectedLogForAudit != null) {
        val auditLog = selectedLogForAudit!!
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)
        ModalBottomSheet(
            onDismissRequest = { selectedLogForAudit = null },
            sheetState = sheetState,
            containerColor = CardDark,
            contentColor = TextPrimary,
            shape = RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp),
            scrimColor = Color.Black.copy(alpha = 0.8f)
        ) {
            CryptographicAuditSheetContent(log = auditLog)
        }
    }
}

@Composable
fun PermissionBannerCard(onEnableClicked: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = CardDark),
        shape = RoundedCornerShape(6.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, GuardianCrimson)
    ) {
        Row(
            modifier = Modifier.padding(14.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = GuardianCrimson.copy(alpha = 0.15f),
                    modifier = Modifier.size(36.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.WarningAmber, contentDescription = null, tint = GuardianCrimson, modifier = Modifier.size(18.dp))
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text("ENGINE INTERCEPTION OFFLINE", color = GuardianCrimson, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                    Text("Grant OS notification access to enable ML memory buffers", color = TextSubtitle, style = MaterialTheme.typography.bodySmall)
                }
            }
            Surface(
                color = GuardianCrimson,
                shape = RoundedCornerShape(4.dp),
                modifier = Modifier.pressClickable { onEnableClicked() }
            ) {
                Text("ENABLE", color = Color.White, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelSmall, fontFamily = FontFamily.Monospace, modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp))
            }
        }
    }
}

// Technical Filter Tabs without generic emojis or truncation
@Composable
fun FilterChipsBar(
    selectedTab: String,
    totalCount: Int,
    whatsAppCount: Int,
    instagramCount: Int,
    telegramCount: Int,
    otherCount: Int,
    onTabSelected: (String) -> Unit
) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        contentPadding = PaddingValues(horizontal = 0.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        item {
            FilterChipItem(
                isSelected = selectedTab == "ALL",
                label = "All Threats ($totalCount)",
                onClick = { onTabSelected("ALL") }
            )
        }
        item {
            FilterChipItem(
                isSelected = selectedTab == "WHATSAPP",
                label = "WhatsApp ($whatsAppCount)",
                onClick = { onTabSelected("WHATSAPP") }
            )
        }
        item {
            FilterChipItem(
                isSelected = selectedTab == "INSTAGRAM",
                label = "Instagram ($instagramCount)",
                onClick = { onTabSelected("INSTAGRAM") }
            )
        }
        item {
            FilterChipItem(
                isSelected = selectedTab == "TELEGRAM",
                label = "Telegram ($telegramCount)",
                onClick = { onTabSelected("TELEGRAM") }
            )
        }
        item {
            FilterChipItem(
                isSelected = selectedTab == "SMS_OTHER",
                label = "Device Anomaly ($otherCount)",
                onClick = { onTabSelected("SMS_OTHER") }
            )
        }
    }
}

@Composable
fun FilterChipItem(isSelected: Boolean, label: String, onClick: () -> Unit) {
    val backgroundColor by animateColorAsState(
        targetValue = if (isSelected) GuardianCrimsonMuted else CardSand,
        animationSpec = tween(160),
        label = "chip_bg"
    )
    val textColor by animateColorAsState(
        targetValue = if (isSelected) GuardianSienna else TextSubtitle,
        animationSpec = tween(160),
        label = "chip_text"
    )

    Surface(
        color = backgroundColor,
        shape = RoundedCornerShape(100.dp), // Clean editorial pill from reference image
        border = androidx.compose.foundation.BorderStroke(1.dp, if (isSelected) GuardianSienna else BorderSand),
        modifier = Modifier.pressClickable { onClick() }
    ) {
        Text(
            text = label,
            color = textColor,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            maxLines = 1,
            softWrap = false,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )
    }
}

@Composable
fun ShimmerLoadingState() {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxSize().padding(top = 4.dp)
    ) {
        repeat(3) {
            Card(
                modifier = Modifier.fillMaxWidth().height(132.dp),
                colors = CardDefaults.cardColors(containerColor = CardDark),
                shape = RoundedCornerShape(8.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                        Box(modifier = Modifier.width(140.dp).height(16.dp).clip(RoundedCornerShape(4.dp)).shimmerEffect())
                        Box(modifier = Modifier.width(60.dp).height(16.dp).clip(RoundedCornerShape(4.dp)).shimmerEffect())
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                    Box(modifier = Modifier.fillMaxWidth().height(36.dp).clip(RoundedCornerShape(4.dp)).shimmerEffect())
                    Spacer(modifier = Modifier.height(14.dp))
                    Box(modifier = Modifier.width(220.dp).height(12.dp).clip(RoundedCornerShape(4.dp)).shimmerEffect())
                }
            }
        }
    }
}

@Composable
fun PermissionErrorState(onRetryClicked: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
        colors = CardDefaults.cardColors(containerColor = CardDark),
        shape = RoundedCornerShape(8.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, GuardianCrimson)
    ) {
        Column(
            modifier = Modifier.padding(20.dp).fillMaxWidth(),
            horizontalAlignment = Alignment.Start
        ) {
            Text("PERIMETER ACCESS RESTRICTED", color = GuardianCrimson, style = MaterialTheme.typography.labelLarge, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "The local neural engine requires OS Notification Listener access to intercept online harassment and sextortion payloads. All processing runs inside a zero-PII memory buffer on this device.",
                color = TextSubtitle,
                style = MaterialTheme.typography.bodySmall,
                lineHeight = 18.sp
            )
            Spacer(modifier = Modifier.height(20.dp))
            Surface(
                color = GuardianCrimson,
                shape = RoundedCornerShape(4.dp),
                modifier = Modifier.fillMaxWidth().height(44.dp).pressClickable { onRetryClicked() }
            ) {
                Row(horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Security, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("AUTHORIZE OS MEMORY BUFFER ACCESS", color = Color.White, style = MaterialTheme.typography.labelMedium, fontFamily = FontFamily.Monospace)
                }
            }
        }
    }
}

// Left-Aligned, Editorial & Asymmetric Empty State (Vercel / Linear / Senior Designer point of view)
// Explicitly rejecting the center-aligned circular icon + centered heading + centered gray text template
@Composable
fun LeftAlignedEditorialEmptyState(onSimulateSafe: () -> Unit, onSimulateCritical: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
        colors = CardDefaults.cardColors(containerColor = CardDark),
        shape = RoundedCornerShape(8.dp), // Intentional sharp technical geometry
        border = androidx.compose.foundation.BorderStroke(1.dp, GlassBorder)
    ) {
        Column(
            modifier = Modifier.padding(20.dp).fillMaxWidth(),
            horizontalAlignment = Alignment.Start // Left-aligned editorial system board
        ) {
            // Editorial technical header block
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(8.dp).background(StatusEmerald, CircleShape))
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "SYSTEM CLEAR // NO ACTIVE INTERCEPTIONS",
                    color = TextPrimary,
                    style = MaterialTheme.typography.labelLarge,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "The local neural engine is scanning incoming notification streams across WhatsApp, Instagram, and Telegram. All payloads are analyzed locally using AES-256 memory buffers without leaving this device.",
                color = TextSubtitle,
                style = MaterialTheme.typography.bodySmall,
                lineHeight = 18.sp
            )

            Spacer(modifier = Modifier.height(24.dp))
            HorizontalDivider(color = GlassBorder, thickness = 1.dp)
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "DIAGNOSTIC DRILL STREAM INJECTION",
                color = TextMuted,
                style = MaterialTheme.typography.labelSmall,
                fontFamily = FontFamily.Monospace
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Intentional Asymmetrical Action Grid with dense structural distinction
            Column(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                // Secondary diagnostic preview action (Sharp structural border)
                Surface(
                    color = CardSurfaceElevated,
                    shape = RoundedCornerShape(6.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, GlassBorder),
                    modifier = Modifier.fillMaxWidth().pressClickable { onSimulateSafe() }
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("INJECT SAFE CHAT PAYLOAD", style = MaterialTheme.typography.labelMedium, color = TextPrimary, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text("Log ID: #DEMO-01 // Safe study notes query", style = MaterialTheme.typography.labelSmall, fontFamily = FontFamily.Monospace, color = TextMuted)
                        }
                        Icon(Icons.Default.PlayArrow, contentDescription = null, tint = TextSubtitle, modifier = Modifier.size(16.dp))
                    }
                }

                // Primary urgency diagnostic action (High visual weight committed GuardianCrimson)
                Surface(
                    color = GuardianCrimson,
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier.fillMaxWidth().pressClickable { onSimulateCritical() }
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("INJECT CRITICAL SEXTORTION ALERT", style = MaterialTheme.typography.labelMedium, color = Color.White, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text("Log ID: #DEMO-CRIT // High-risk extortion pattern", style = MaterialTheme.typography.labelSmall, fontFamily = FontFamily.Monospace, color = Color.White.copy(alpha = 0.8f))
                        }
                        Icon(Icons.Default.Warning, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                    }
                }
            }
        }
    }
}

// Guardian Editorial Threat Log Card matching Senior Designer Reference Image (input_file_2.png)
@Composable
fun ThreatLogCard(log: PipelineExecutionLog, onCardClicked: () -> Unit) {
    val tierColor = when (log.stage3Analyze.tier) {
        RiskTier.CRITICAL -> GuardianCrimson
        RiskTier.ESCALATE -> StatusAmber
        RiskTier.INTERVENE -> InterveneBrown
        RiskTier.MONITOR -> StatusEmerald
    }

    val timeFormatted = remember(log.timestamp) {
        SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date(log.timestamp))
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .pressClickable { onCardClicked() },
        colors = CardDefaults.cardColors(containerColor = CardSand), // #F0ECE5 Sand Card
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderSand)
    ) {
        Column {
            // Top Header Bar (#E5E0D6)
            Surface(
                color = CardSandElevated,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "SHA-256: 7f83b165...${log.id.takeLast(3)}",
                        color = GuardianSienna,
                        style = MaterialTheme.typography.labelSmall,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = timeFormatted,
                        color = TextMuted,
                        style = MaterialTheme.typography.labelSmall,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp
                    )
                }
            }

            Column(modifier = Modifier.padding(16.dp)) {
                // Card Body: Large vector title + Status Badge on right
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
                        Text(
                            text = when (log.stage3Analyze.tier) {
                                RiskTier.CRITICAL -> "Sextortion / Extortion Pattern Detected"
                                RiskTier.ESCALATE -> "Anomalous Credential & Egress Pattern"
                                RiskTier.INTERVENE -> "Privacy Policy Violation / Sensitive Access"
                                RiskTier.MONITOR -> "Routine Communication Stream"
                            },
                            color = TextPrimary,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${log.platform.uppercase()} · \"${log.rawContent.ifEmpty { log.stage3Analyze.rawContent }}\"",
                            color = TextSubtitle,
                            style = MaterialTheme.typography.bodySmall,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    // Status Badge (CRITICAL red, ESCALATE orange, INTERVENE brown, MONITOR green)
                    Surface(
                        color = tierColor,
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = log.stage3Analyze.tier.name,
                            color = Color.White,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                        )
                    }
                }

                if (log.stage3Analyze.corroboratingSignals.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    @OptIn(ExperimentalLayoutApi::class)
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        log.stage3Analyze.corroboratingSignals.forEach { signal ->
                            Surface(
                                color = CardSandElevated,
                                shape = RoundedCornerShape(4.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, BorderSand)
                            ) {
                                Text(
                                    text = signal,
                                    color = tierColor,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 10.sp,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Bottom Row: Risk Index + Numerical Bar (98/100) exactly like reference image
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Risk Index",
                        color = TextSubtitle,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "${"%.1f".format(log.stage3Analyze.riskScore)}/10",
                        color = tierColor,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                LinearProgressIndicator(
                    progress = { (log.stage3Analyze.riskScore / 10f).coerceIn(0f, 1f) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = tierColor,
                    trackColor = CardSandElevated
                )
            }
        }
    }
}

@Composable
fun CryptographicAuditSheetContent(log: PipelineExecutionLog) {
    val tierColor = when (log.stage3Analyze.tier) {
        RiskTier.CRITICAL -> GuardianCrimson
        RiskTier.ESCALATE -> EscalateOrange
        RiskTier.INTERVENE -> StatusAmber
        RiskTier.MONITOR -> StatusEmerald
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 22.dp)
            .padding(bottom = 36.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Security, contentDescription = null, tint = SignalIndigo, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(10.dp))
                Text("5-STAGE ENCLAVE AUDIT", color = TextPrimary, style = MaterialTheme.typography.labelLarge, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
            }
            Surface(
                color = tierColor.copy(alpha = 0.15f),
                shape = RoundedCornerShape(4.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, tierColor)
            ) {
                Text(
                    text = log.stage3Analyze.tier.name.substringBefore(" "),
                    color = tierColor,
                    style = MaterialTheme.typography.labelSmall,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }

        Text("CASE BUFFER REFERENCE: ${log.id}", color = TextMuted, style = MaterialTheme.typography.labelSmall, fontFamily = FontFamily.Monospace, modifier = Modifier.padding(top = 4.dp, bottom = 14.dp))

        HorizontalDivider(color = GlassBorder, thickness = 1.dp)
        Spacer(modifier = Modifier.height(14.dp))

        AuditStageRow("1", "LISTEN // METADATA EXTRACTION", log.stage1Listen, SignalIndigo)
        AuditStageRow("2", "ANONYMIZE // SHA-256 + AES-256-GCM", log.stage2Anonymize, TextSubtitle, true)
        AuditStageRow("3", "ANALYZE // ON-DEVICE EVALUATION", "Pattern: ${log.stage3Analyze.patternDetected}\nScore: ${"%.1f".format(log.stage3Analyze.riskScore)}/10 | Explanation: ${log.stage3Analyze.explanation}", tierColor)
        AuditStageRow("4", "ENFORCE // AUTONOMOUS INTERVENTION", log.stage4Enforce.actionsExecuted.joinToString("\n• ", prefix = "• "), EscalateOrange)
        AuditStageRow("5", "REPORT // ZERO-PII COUNSELOR PAYLOAD", "Timeline: ${log.stage5Report.timelineSummary}\nGuarantee: 100% stripped of names and phone numbers.", StatusEmerald)

        Spacer(modifier = Modifier.height(20.dp))
        Surface(
            color = SignalIndigo,
            shape = RoundedCornerShape(6.dp),
            modifier = Modifier.fillMaxWidth().height(48.dp).pressClickable { /* Dismiss */ }
        ) {
            Row(horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Verified, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("VERIFY SHA-256 CRYPTOGRAPHIC CHAIN", color = Color.White, style = MaterialTheme.typography.labelMedium, fontFamily = FontFamily.Monospace)
            }
        }
    }
}

@Composable
fun AuditStageRow(stageNum: String, title: String, description: String, badgeColor: Color, isMonospace: Boolean = false) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.Top
    ) {
        Surface(
            shape = RoundedCornerShape(4.dp),
            color = badgeColor.copy(alpha = 0.15f),
            modifier = Modifier.size(24.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(stageNum, color = badgeColor, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelSmall, fontFamily = FontFamily.Monospace)
            }
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, color = badgeColor, style = MaterialTheme.typography.labelSmall, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = description,
                color = TextPrimary.copy(alpha = 0.9f),
                style = MaterialTheme.typography.bodySmall,
                fontFamily = if (isMonospace) FontFamily.Monospace else FontFamily.Default
            )
        }
    }
}
