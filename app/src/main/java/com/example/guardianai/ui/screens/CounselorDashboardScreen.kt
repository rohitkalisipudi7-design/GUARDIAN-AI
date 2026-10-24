package com.example.guardianai.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.guardianai.pipeline.*
import com.example.guardianai.theme.*

@Composable
fun CounselorDashboardScreen() {
    val reports = remember { ReportGenerator.getAllReports() }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(AppBackground), // #FBF8F3 Warm Ivory/Cream foundation
        contentPadding = PaddingValues(start = 24.dp, top = 18.dp, end = 24.dp, bottom = 100.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            // Editorial Header Bar (input_file_3.png)
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 2.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
                    Text(
                        text = "Institutional Dashboard",
                        color = TextPrimary,
                        fontFamily = FontFamily.Serif,
                        fontSize = 32.sp,
                        lineHeight = 38.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Zero-PII anonymized campus threat telemetry & risk clusters.",
                        color = TextSubtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        fontSize = 14.sp
                    )
                }

                Surface(
                    color = StatusEmerald.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(100.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, StatusEmerald)
                ) {
                    Text(
                        text = "Zero-PII Certified",
                        color = StatusEmerald,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                    )
                }
            }
        }

        item {
            // Privacy Protocol Block (Editorial Sand Style)
            Card(
                colors = CardDefaults.cardColors(containerColor = CardSandElevated),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, BorderSand)
            ) {
                Row(modifier = Modifier.padding(18.dp), verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = StatusEmerald.copy(alpha = 0.15f),
                        modifier = Modifier.size(42.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.VerifiedUser, contentDescription = null, tint = StatusEmerald, modifier = Modifier.size(22.dp))
                        }
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column {
                        Text("Strict Victim Privacy Protocol Enforced", color = StatusEmerald, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "• Visible to Counselors: Case ID, Risk Score (0.0–10.0), Threat Pattern, Timeline Summary, and Platform Channels.\n• Stripped from Ledger: Names, Phone Numbers, Message Content, GPS Coordinates, and Media Assets.\n• Guarantee: Institutional protection without fear of identity disclosure or perpetrator retaliation.",
                            color = TextSubtitle,
                            style = MaterialTheme.typography.bodySmall,
                            lineHeight = 18.sp,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }

        item {
            Text(
                text = "Active Anonymized Escalation Cases (${reports.size}):",
                color = TextSubtitle,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold
            )
        }

        if (reports.isEmpty()) {
            item {
                // Editorial Sand Empty State
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
                            Icon(Icons.Default.AssignmentTurnedIn, contentDescription = null, tint = StatusEmerald, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(10.dp))
                            Text("No Active Escalation Cases in Enclave", color = StatusEmerald, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "When Guardian AI's Stage 3 ML evaluation assigns an ESCALATE or CRITICAL rating to an incoming notification stream across social platforms, an anonymized case report will instantly publish into this institutional dashboard.",
                            color = TextSubtitle,
                            style = MaterialTheme.typography.bodyMedium,
                            lineHeight = 20.sp,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        } else {
            items(reports, key = { it.caseId }) { report ->
                CounselorCaseCard(report)
            }
        }
    }
}

@Composable
fun CounselorCaseCard(report: CounselorCaseReport) {
    val tierColor = when (report.riskTier) {
        RiskTier.CRITICAL -> GuardianCrimson
        RiskTier.ESCALATE -> StatusAmber
        RiskTier.INTERVENE -> InterveneBrown
        RiskTier.MONITOR -> StatusEmerald
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = CardSand),
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderSand)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            // Level 1 Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = CircleShape,
                        color = CardSandElevated,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.FolderSpecial, contentDescription = null, tint = GuardianSienna, modifier = Modifier.size(18.dp))
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Text("Report // ${report.caseId}", color = TextPrimary, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                }
                Surface(
                    color = tierColor,
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = "SCORE: ${"%.1f".format(report.riskScore)}/10 [${report.riskTier.name.substringBefore(" ")}]",
                        color = Color.White,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Inside Sand Card block for Threat Pattern
            Surface(
                color = AppBackground,
                shape = RoundedCornerShape(8.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, BorderSand),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text("Detected Pattern Vector:", color = GuardianSienna, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(report.pattern, color = TextPrimary, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Escalation Timeline Summary:", color = TextSubtitle, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(report.timelineSummary, color = TextPrimary, style = MaterialTheme.typography.bodySmall, lineHeight = 18.sp, fontSize = 13.sp)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text("Anonymized Evidence Indicators:", color = TextSubtitle, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, fontSize = 11.sp)
            Spacer(modifier = Modifier.height(6.dp))
            report.evidenceSummary.forEach { ev ->
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 3.dp)) {
                    Surface(
                        shape = CircleShape,
                        color = StatusEmerald.copy(alpha = 0.15f),
                        modifier = Modifier.size(16.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.Check, contentDescription = null, tint = StatusEmerald, modifier = Modifier.size(11.dp))
                        }
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(ev, color = TextPrimary, style = MaterialTheme.typography.bodySmall, fontSize = 13.sp)
                }
            }
        }
    }
}
