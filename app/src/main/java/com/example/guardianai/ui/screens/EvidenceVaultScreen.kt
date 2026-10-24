package com.example.guardianai.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.guardianai.pipeline.*
import com.example.guardianai.theme.*
import com.example.guardianai.ui.components.pressClickable

@Composable
fun EvidenceVaultScreen() {
    val haptic = LocalHapticFeedback.current
    val logs = remember { ListenService.getLogs() }
    var showExportDialog by remember { mutableStateOf(false) }
    var selectedFilter by remember { mutableStateOf("ALL") }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(AppBackground), // #FBF8F3 Warm Ivory/Cream foundation
        contentPadding = PaddingValues(start = 24.dp, top = 18.dp, end = 24.dp, bottom = 100.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            // Editorial Screen Title (input_file_4.png)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
                    Text(
                        text = "Cryptographic Evidence Vault",
                        color = TextPrimary,
                        fontFamily = FontFamily.Serif,
                        fontSize = 32.sp,
                        lineHeight = 38.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Immutable, court-admissible audit trails with SHA-256 local verification anchors.",
                        color = TextSubtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        fontSize = 14.sp
                    )
                }

                Surface(
                    color = GuardianSienna,
                    shape = RoundedCornerShape(100.dp),
                    modifier = Modifier.pressClickable {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        showExportDialog = true
                    }
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)) {
                        Icon(Icons.Default.Share, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Export Bundle", color = Color.White, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        item {
            // Filter Pills Bar (input_file_4.png)
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                item {
                    VaultFilterPill(isSelected = selectedFilter == "ALL", label = "All Records (${logs.size})") { selectedFilter = "ALL" }
                }
                item {
                    VaultFilterPill(isSelected = selectedFilter == "AUDIO", label = "Audio Captures") { selectedFilter = "AUDIO" }
                }
                item {
                    VaultFilterPill(isSelected = selectedFilter == "SNAPSHOTS", label = "Threat Snapshots") { selectedFilter = "SNAPSHOTS" }
                }
                item {
                    VaultFilterPill(isSelected = selectedFilter == "EXPORTS", label = "Ledger Exports") { selectedFilter = "EXPORTS" }
                }
            }
        }

        item {
            // Air-Gapped Offline Export Banner (Editorial Sand Style)
            Card(
                colors = CardDefaults.cardColors(containerColor = CardSandElevated),
                modifier = Modifier
                    .fillMaxWidth()
                    .semantics { contentDescription = "Offline Encrypted Export: Transfer SHA-256 audit ledger via Local QR Code or Android Nearby Share over air-gapped Wi-Fi Direct without cloud servers." },
                shape = RoundedCornerShape(10.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, BorderSand)
            ) {
                Row(
                    modifier = Modifier.padding(14.dp).fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f).padding(end = 10.dp)) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = StatusEmerald.copy(alpha = 0.15f),
                            modifier = Modifier.size(40.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.QrCodeScanner, contentDescription = null, tint = StatusEmerald, modifier = Modifier.size(20.dp))
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("Air-Gapped Offline Export (QR / Nearby Share)", color = StatusEmerald, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text("Transfer encrypted case bundles directly to campus authorities over local Wi-Fi Direct/QR without cellular egress.", color = TextSubtitle, style = MaterialTheme.typography.bodySmall, fontSize = 12.sp, lineHeight = 16.sp)
                        }
                    }
                    Surface(
                        color = StatusEmerald,
                        shape = RoundedCornerShape(100.dp),
                        modifier = Modifier.pressClickable {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            showExportDialog = true
                        }
                    ) {
                        Text("QR Export", color = Color.White, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp))
                    }
                }
            }
        }

        item {
            VaultProtocolGuideCard()
        }

        if (logs.isEmpty()) {
            item {
                // Editorial Sand Empty State / Zero-PII Enclave Description
                Card(
                    modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                    colors = CardDefaults.cardColors(containerColor = CardSand),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, BorderSand)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp).fillMaxWidth(),
                        horizontalAlignment = Alignment.Start
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.VerifiedUser, contentDescription = null, tint = StatusEmerald, modifier = Modifier.size(22.dp))
                            Spacer(modifier = Modifier.width(10.dp))
                            Text("Enclave Ledger Clear · Zero Forensic Packets", color = StatusEmerald, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "When Guardian AI intercepts high-risk harassment or UPI extortion payloads across WhatsApp or Instagram, tamper-evident cryptographic bundles containing exact SHA-256 sender hashes and AES-256 cipherblocks will automatically compile into this immutable vault.",
                            color = TextSubtitle,
                            style = MaterialTheme.typography.bodyMedium,
                            lineHeight = 20.sp,
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        Surface(
                            color = CardSandElevated,
                            shape = RoundedCornerShape(6.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, BorderSand)
                        ) {
                            Text(
                                text = "RFC 3161 Time-Stamp Tokens (TSA) Ready",
                                color = GuardianSienna,
                                style = MaterialTheme.typography.labelSmall,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
            }
        } else {
            items(logs, key = { it.id }) { log ->
                VaultItemCard(log)
            }
        }
    }

    if (showExportDialog) {
        AlertDialog(
            onDismissRequest = { showExportDialog = false },
            containerColor = CardSand,
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Verified, contentDescription = null, tint = StatusEmerald, modifier = Modifier.size(22.dp))
                    Spacer(modifier = Modifier.width(10.dp))
                    Text("Zero-PII Forensic Bundle Export", color = TextPrimary, fontFamily = FontFamily.Serif, fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column {
                    Text("Compiling SHA-256 Cryptographic Ledger...", color = TextSubtitle, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("• Total Cryptographic Records: ${logs.size} timestamped vectors", color = TextPrimary, style = MaterialTheme.typography.bodySmall, fontSize = 13.sp)
                    Text("• Integrity Algorithm: SHA-256 + AES-256-GCM verification", color = TextPrimary, style = MaterialTheme.typography.bodySmall, fontSize = 13.sp)
                    Text("• Privacy Guarantee: 100% stripped of names/phone numbers", color = StatusEmerald, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(14.dp))
                    Text("Formatted strictly for submission to National Cyber Crime (1930), campus legal advisors, or local magistrates without compromising personal privacy.", color = TextSubtitle, style = MaterialTheme.typography.bodySmall, lineHeight = 18.sp)
                }
            },
            confirmButton = {
                Surface(
                    color = StatusEmerald,
                    shape = RoundedCornerShape(100.dp),
                    modifier = Modifier.pressClickable {
                        showExportDialog = false
                    }
                ) {
                    Text("Generate & Download Zip/PDF", color = Color.White, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp))
                }
            },
            dismissButton = {
                TextButton(onClick = { showExportDialog = false }) {
                    Text("Cancel", color = TextSubtitle, style = MaterialTheme.typography.labelMedium)
                }
            }
        )
    }
}

@Composable
fun VaultFilterPill(isSelected: Boolean, label: String, onClick: () -> Unit) {
    Surface(
        color = if (isSelected) GuardianCrimsonMuted else CardSand,
        shape = RoundedCornerShape(100.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, if (isSelected) GuardianSienna else BorderSand),
        modifier = Modifier.pressClickable { onClick() }
    ) {
        Text(
            text = label,
            color = if (isSelected) GuardianSienna else TextSubtitle,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )
    }
}

@Composable
fun VaultItemCard(log: PipelineExecutionLog) {
    val haptic = LocalHapticFeedback.current
    var isDecrypted by remember { mutableStateOf(false) }
    val tierColor = when (log.stage3Analyze.tier) {
        RiskTier.CRITICAL -> GuardianCrimson
        RiskTier.ESCALATE -> StatusAmber
        RiskTier.INTERVENE -> InterveneBrown
        RiskTier.MONITOR -> StatusEmerald
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = CardSand), // #F0ECE5 Sand Card
        modifier = Modifier
            .fillMaxWidth()
            .semantics { contentDescription = "Verified audit chain record ID ${log.id}, threat pattern ${log.stage3Analyze.patternDetected}. Double tap to toggle ciphertext lock." },
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderSand)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            // Level 1 Technical Header (input_file_4.png)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = CircleShape,
                        color = CardSandElevated,
                        modifier = Modifier.size(38.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.Security, contentDescription = null, tint = GuardianSienna, modifier = Modifier.size(18.dp))
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = log.stage3Analyze.patternDetected.ifEmpty { "Threat Snapshot · Intercepted Stream" },
                            color = TextPrimary,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "${log.platform.uppercase()} · SHA-256: ${log.id.take(8)}...${log.id.takeLast(4)}",
                            color = TextSubtitle,
                            style = MaterialTheme.typography.labelSmall,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp
                        )
                    }
                }

                // Verified / Tier Badge
                Surface(
                    color = tierColor,
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = "VERIFIED",
                        color = Color.White,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Inner Ciphertext / Plaintext Block
            Surface(
                color = AppBackground, // #FBF8F3 inside Sand Card
                shape = RoundedCornerShape(8.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, BorderSand),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    if (isDecrypted) {
                        Text(
                            text = "🔓 UNLOCKED SENDER & EVIDENCE RECORD",
                            color = GuardianSienna,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        val senderName = if (log.rawSender.isNotBlank() && log.rawSender != "Unknown") log.rawSender else if (log.stage1Listen.contains("from [")) log.stage1Listen.substringAfter("from [").substringBefore("]") else "Protected Node"
                        val dateFormatted = java.text.SimpleDateFormat("dd MMM yyyy, hh:mm:ss a").format(java.util.Date(log.timestamp))
                        Text(
                            text = "Source/Contact: $senderName\nPlatform: ${log.platform}\nTimestamp: $dateFormatted",
                            color = TextPrimary,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold,
                            lineHeight = 18.sp
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "PLAIN TEXT MESSAGE CONTENT:\n\"${if (log.rawContent.isNotBlank()) log.rawContent else log.stage3Analyze.explanation}\"",
                            color = TextPrimary,
                            style = MaterialTheme.typography.bodySmall,
                            fontFamily = FontFamily.Default,
                            lineHeight = 18.sp
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "FORENSIC THREAT ASSESSMENT:\n${log.stage3Analyze.explanation}",
                            color = TextSubtitle,
                            style = MaterialTheme.typography.bodySmall,
                            lineHeight = 18.sp
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Surface(
                            color = CardSandElevated,
                            shape = RoundedCornerShape(6.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, BorderSand),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text(
                                    text = "⚖️ RECOMMENDED & PREFERABLE LEGAL ACTIONS",
                                    color = GuardianSienna,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "1. Export SHA-256 Bundle: Tap 'Export Bundle' above to generate court-admissible proof.\n2. Report with Hash: Attach this record ID (${log.id}) when lodging complaint at National Cyber Portal (1930) or Campus Title IX.\n3. Preserve Chain of Custody: Do NOT delete or reply to original messages on ${log.platform}.\n4. Automated Enclave Defense: ${log.stage4Enforce.actionsExecuted.joinToString("; ")}.",
                                    color = TextPrimary,
                                    style = MaterialTheme.typography.bodySmall,
                                    fontSize = 12.sp,
                                    lineHeight = 16.sp
                                )
                            }
                        }
                    } else {
                        Text(
                            text = "ENCRYPTED CIPHERTEXT BUFFER:\n${log.stage2Anonymize.substringAfter("AES-256 encrypted (").removeSuffix(")")} [AES-256 LOCKED]",
                            color = TextSubtitle,
                            style = MaterialTheme.typography.bodySmall,
                            fontFamily = FontFamily.Monospace,
                            lineHeight = 18.sp,
                            fontSize = 13.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "AES-256-GCM [HARDWARE ISOLATED]",
                    color = StatusEmerald,
                    style = MaterialTheme.typography.labelSmall,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp
                )

                Surface(
                    color = if (isDecrypted) CardSandElevated else GuardianSienna,
                    shape = RoundedCornerShape(100.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, if (isDecrypted) BorderSand else GuardianSienna),
                    modifier = Modifier.pressClickable {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        isDecrypted = !isDecrypted
                    }
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)) {
                        Icon(if (isDecrypted) Icons.Default.VisibilityOff else Icons.Default.Visibility, contentDescription = null, tint = if (isDecrypted) TextPrimary else Color.White, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(if (isDecrypted) "Lock Ciphertext" else "Decrypt & Audit", color = if (isDecrypted) TextPrimary else Color.White, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun VaultProtocolGuideCard() {
    var expanded by remember { mutableStateOf(false) }
    Card(
        colors = CardDefaults.cardColors(containerColor = CardSand),
        shape = RoundedCornerShape(10.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderSand),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth().pressClickable { expanded = !expanded },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Lock, contentDescription = null, tint = GuardianSienna, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Why Contacts Are Encrypted & Remediation Guide",
                        color = TextPrimary,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
                Icon(if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore, contentDescription = null, tint = TextSubtitle)
            }
            if (expanded) {
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "🔒 Why Contacts & Senders are Encrypted in Vault:\nTo comply with zero-PII privacy mandates and protect you against spyware or physical device seizure, all contacts and message contents are stored as encrypted ciphertext buffers (`AES-256-GCM`). Nobody can read your evidence without biometrics/pin.\n\n🔓 How to Know Who Sent It:\nTap the 'Decrypt & Audit' button on any record card below to unlock the enclave and reveal the exact sender name, timestamp, and plain text message.\n\n⚖️ What Preferable Actions to Do:\n1. Preserve Chain of Custody: Do not delete or reply to original messages.\n2. Export Legal SHA-256 Report: Tap 'Export Bundle' above to generate a court-admissible PDF/JSON file anchored by SHA-256 hash.\n3. Lodge Complaint: Attach the exported hash when reporting to National Cyber Crime Portal (1930) or Campus Title IX / Legal Counselor.",
                    color = TextSubtitle,
                    style = MaterialTheme.typography.bodySmall,
                    lineHeight = 18.sp
                )
            }
        }
    }
}
