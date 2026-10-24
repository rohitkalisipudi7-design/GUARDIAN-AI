package com.example.guardianai.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.guardianai.data.CuratedLegalDatabase
import com.example.guardianai.pipeline.*
import com.example.guardianai.theme.*
import com.example.guardianai.ui.components.pressClickable
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class ChatMessage(
    val id: String,
    val isUser: Boolean,
    val text: String,
    val timestamp: Long = System.currentTimeMillis(),
    val language: String = "English",
    val riskScore: Float? = null,
    val riskTier: RiskTier? = null,
    var isFlaggedForReview: Boolean = false
)

@Composable
fun CompanionChatScreen() {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    var selectedLang by remember { mutableStateOf("English") }
    var inputText by remember { mutableStateOf("") }
    val messages = remember { mutableStateListOf<ChatMessage>() }
    
    // Feature Modal States
    var showReportWizard by remember { mutableStateOf(false) }
    var showHelplineDirectory by remember { mutableStateOf(false) }
    var showSettingsDialog by remember { mutableStateOf(false) }
    var geminiApiKeyInput by remember { mutableStateOf("") }

    val screenshotLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            val fileName = uri.lastPathSegment ?: "uploaded_screenshot.png"
            val userText = "Uploaded Screenshot: [$fileName] for threat analysis."
            messages.add(ChatMessage("u_img_${System.currentTimeMillis()}", true, userText, language = selectedLang))
            
            val aiReply = CompanionAiEngine.analyzeScreenshot(userText, selectedLang)
            messages.add(ChatMessage("ai_img_${System.currentTimeMillis()}", false, aiReply, language = selectedLang))
        }
    }

    LaunchedEffect(selectedLang) {
        messages.clear()
        val greeting = when (selectedLang) {
            "Hindi" -> """
                नमस्ते! मैं **Companion — The Cyber Law Guide** हूँ, भारतीय नागरिकों के लिए आपका निजी डिजिटल सुरक्षा एवं कानून परामर्शदाता।
                
                मैं पूरी संवेदनशीलता और गोपनीयता के साथ इन 3 प्रमुख विषयों पर आपका मार्गदर्शन करने के लिए तैयार हूँ:
                • **साइबर हाइजीन (Cyber Hygiene):** मजबूत पासवर्ड, 2FA, ऐप परमिशन और UPI भुगतान सुरक्षा।
                • **भारतीय साइबर कानून (Indian Cyber Laws):** IT Act 2000, DPDP Act 2023, BNS, POCSO Act, CERT-In नियम और RBI के दिशा-निर्देश।
                • **खतरे की पहचान (Threat Detection):** फ़िशिंग संदेश, बैंकिंग स्कैम और स्क्रीनशॉट विश्लेषण।
                
                *(नोट: मैं केवल डिजिटल सुरक्षा और कानून से जुड़े विषयों पर ही मार्गदर्शन करता हूँ।)* नीचे दिए गए किसी विषय को चुनें, या सीधे अपनी समस्या यहाँ टाइप करें!
            """.trimIndent()
            "Telugu" -> """
                నమస్కారం! నేను **Companion — The Cyber Law Guide** — భారతీయ వినియోగదారుల కోసం మీ ప్రత్యేక సైబర్ భద్రతా సహాయకుడిని.
                
                మీ గోప్యతను కాపాడుతూ ఈ 3 అంశాలలో మీకు పూర్తి సహాయం అందించడానికి నేను సిద్ధంగా ఉన్నాను:
                • **సైబర్ పరిశుభ్రత (Cyber Hygiene):** పాస్‌వర్డ్‌లు, 2FA, UPI భద్రత, యాప్ పర్మిషన్లు.
                • **భారతీయ సైబర్ చట్టాలు:** IT Act 2000, DPDP Act 2023, BNS, POCSO, CERT-In, RBI నిబంధనలు.
                • **ముప్పు గుర్తింపు (Threat Detection):** ఫిషింగ్, స్కామ్‌లు, స్క్రీన్‌షాట్ విశ్లేషణ.
                
                *(గమనిక: పరిధికి ఆవల ఉన్న సాధారణ ప్రశ్నలకు నేను స్పందించను.)* క్రింది అంశాలలో ఒకదాన్ని ఎంచుకోండి లేదా మీ ప్రశ్నను టైప్ చేయండి!
            """.trimIndent()
            else -> """
                Hello! I am **Companion — The Cyber Law Guide**, your confidential personal safety and legal awareness advisor built specifically for Indian digital users (`Built from PDR v1.0`).
                
                Whether you are dealing with online harassment, payment fraud, or just want to secure your devices, I am here to guide you across three core pillars:
                • **Cyber Hygiene & Defense:** Passwords, multi-factor authentication (`2FA`), safe browsing, app permission audits, and UPI payment security.
                • **Indian Cyber Laws & Rights:** Plain-language education on the **IT Act 2000, DPDP Act 2023, BNS, POCSO Act**, and emergency mandates under **CERT-In** and **RBI**.
                • **Threat & Screenshot Analysis:** Real-time evaluation of suspicious payment links, KYC SMS alerts, loan app permissions, and social engineering traps (`[Verified: July 2026]`).
                
                *(Note: To ensure strict safety, I decline off-topic queries like coding or general chit-chat.)* Select one of the suggested topics below, tap **📝 Report** to generate a complaint, or simply type what is on your mind!
            """.trimIndent()
        }
        messages.add(ChatMessage("init_${System.currentTimeMillis()}", false, greeting, language = selectedLang))
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppBackground) // #FBF8F3 Warm Ivory/Cream foundation
            .padding(horizontal = 16.dp, vertical = 6.dp)
    ) {
        // 1. Pinned Header & Language Row (Zero collision, zero text wrapping)
        Surface(
            color = AppBackground,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp)) {
                // Top Action Title Bar
                Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Companion · Cyber Law",
                        color = TextPrimary,
                        fontFamily = FontFamily.Serif,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        softWrap = false,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    // Compact Action Buttons Row (Report & Help)
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            color = CardSand,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.pressClickable {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                val sharedPrefs = context.getSharedPreferences("guardian_ai_prefs", Context.MODE_PRIVATE)
                                geminiApiKeyInput = sharedPrefs.getString("gemini_api_key", "") ?: ""
                                showSettingsDialog = true
                            }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Settings, contentDescription = "Settings", tint = TextPrimary, modifier = Modifier.size(16.dp))
                            }
                        }

                        Surface(
                            color = GuardianSienna,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.pressClickable {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                showReportWizard = true
                            }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Article, contentDescription = null, tint = Color.White, modifier = Modifier.size(13.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Report", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold, softWrap = false, maxLines = 1)
                            }
                        }

                        Surface(
                            color = TextPrimary,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.pressClickable {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                showHelplineDirectory = true
                            }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Phone, contentDescription = null, tint = Color.White, modifier = Modifier.size(13.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Help", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold, softWrap = false, maxLines = 1)
                            }
                        }
                    }
                }

                // Language Selector Row + CERT-In Aligned Pill right beside Language
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Lang:", color = TextSubtitle, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        listOf("English", "Hindi", "Telugu").forEach { lang ->
                            val isSelected = selectedLang == lang
                            val bgColor by animateColorAsState(if (isSelected) GuardianSienna else CardSand, tween(160), label = "lang_bg")
                            val textColor by animateColorAsState(if (isSelected) Color.White else TextSubtitle, tween(160), label = "lang_txt")

                            Surface(
                                color = bgColor,
                                shape = RoundedCornerShape(100.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, if (isSelected) GuardianSienna else BorderSand),
                                modifier = Modifier.pressClickable {
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    selectedLang = lang
                                }
                            ) {
                                Text(
                                    text = lang,
                                    color = textColor,
                                    fontSize = 11.5.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    maxLines = 1,
                                    softWrap = false,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }

                    Surface(
                        color = StatusEmerald.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(100.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, StatusEmerald.copy(alpha = 0.4f))
                    ) {
                        Text(
                            text = "CERT-In",
                            color = StatusEmerald,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            softWrap = false,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }
            }
        }

        // 2. Quick Suggested Topics Bar (Scrollable horizontally)
        LazyRow(
            modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                SafetyPromptChip("🛡️ UPI & 2FA Cyber Hygiene") {
                    inputText = when (selectedLang) {
                        "Hindi" -> "RBI दिशानिर्देशों के तहत UPI और 2FA (Two-Factor Authentication) के लिए साइबर हाइजीन के मुख्य नियम क्या हैं?"
                        "Telugu" -> "UPI మరియు 2FA భద్రత కోసం పాటించవలసిన ముఖ్యమైన సైబర్ పరిశుభ్రత నియమాలను వివరించండి."
                        else -> "What are the essential cyber hygiene rules for UPI payments, 2FA hardening, and app permissions under RBI guidelines?"
                    }
                }
            }
            item {
                SafetyPromptChip("⚖️ IT Act & DPDP Act 2023") {
                    inputText = when (selectedLang) {
                        "Hindi" -> "IT Act 2000 की धारा 66/67 के तहत मेरे अधिकार और DPDP Act 2023 / Rules 2025 की स्थिति समझाएं।"
                        "Telugu" -> "భారతీయ IT Act 2000 మరియు DPDP Act 2023 అమలు దశల గురించి నాకు వివరించండి."
                        else -> "Explain my general awareness rights under IT Act 2000 Section 66/67 and the phased rollout status of DPDP Act 2023 / Rules 2025."
                    }
                }
            }
            item {
                SafetyPromptChip("🚨 Active Fraud Golden Hour") {
                    inputText = when (selectedLang) {
                        "Hindi" -> "मेरे खाते से अभी-अभी एक अवैध UPI ट्रांजैक्शन से पैसे कटे हैं! मुझे 'गोल्डन आवर' में क्या करना चाहिए?"
                        "Telugu" -> "నా అకౌంట్ నుండి ఇప్పుడే మోసపూరితంగా డబ్బులు కట్ అయ్యాయి! గోల్డెన్ అవర్‌లో నేను ఏం చేయాలి?"
                        else -> "I just lost ₹50,000 in an active UPI fraud! What immediate steps should I take within the Golden Hour (Helpline 1930) right now?"
                    }
                }
            }
            item {
                SafetyPromptChip("📷 Analyze UPI Collect Screenshot") {
                    inputText = when (selectedLang) {
                        "Hindi" -> "OCR: UPI Collect Request Screen. GPay नोटिफिकेशन: 'आपको ₹10,000 लॉटरी कैशबैक मिला है! राशि पाने के लिए अपना UPI पिन डालें।'"
                        "Telugu" -> "OCR: UPI Collect Request. 'లాటరీ క్యాష్‌బ్యాక్ ₹10,000 పొందడానికి వెంటనే మీ UPI పిన్ ఎంటర్ చేయండి' అని నోటిఫికేషన్ వచ్చింది."
                        else -> "OCR: UPI Collect Request Screen. GPay notification: 'You have won ₹10,000 lottery cashback! Enter your UPI PIN right now to receive money.'"
                    }
                }
            }
            item {
                SafetyPromptChip("📷 Analyze Fake Bank KYC SMS") {
                    inputText = when (selectedLang) {
                        "Hindi" -> "OCR: Banking / KYC Alert SMS. प्रेषक: 'AX-SBIBNK'. संदेश: 'प्रिय ग्राहक, आपका SBI खाता 24 घंटे में बंद हो जाएगा। तुरंत इस लिंक से PAN KYC अपडेट करें: http://sbi-kyc-update.apk'"
                        "Telugu" -> "OCR: Banking KYC SMS. 'మీ SBI ఖాతా 24 గంటల్లో బ్లాక్ అవుతుంది. వెంటనే ఈ లింక్ ద్వారా KYC అప్‌డేట్ చేయండి: http://sbi-kyc-update.apk'"
                        else -> "OCR: Banking / KYC Alert SMS. Sender: 'AX-SBIBNK'. Text: 'Dear Customer, your SBI account is blocked. Update PAN KYC within 24 hours via this link: http://sbi-kyc-update.apk'"
                    }
                }
            }
            item {
                SafetyPromptChip("📷 Analyze App Permission Screen") {
                    inputText = when (selectedLang) {
                        "Hindi" -> "OCR: Android App Permission Screen. 'Instant Loan App' आपके फोन के SMS संदेश, कॉल लॉग और Accessibility Service पढ़ने की अनुमति मांग रहा है।"
                        "Telugu" -> "OCR: Android Permission Screen. 'Instant Loan App' మీ SMSలు, కాల్ హిస్టరీ మరియు Accessibility Service పర్మిషన్ అడుగుతోంది."
                        else -> "OCR: Android App Permission Screen. An 'Instant Loan App' is requesting access to read all SMS messages, call logs, and full Accessibility Service control."
                    }
                }
            }
        }

        // 3. Spacious, Unhindered Conversation Feed
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            contentPadding = PaddingValues(top = 4.dp, bottom = 10.dp)
        ) {
            items(messages, key = { it.id }) { msg ->
                EditorialCounselCard(
                    message = msg,
                    onToggleFlag = {
                        val idx = messages.indexOf(msg)
                        if (idx != -1) {
                            messages[idx] = msg.copy(isFlaggedForReview = !msg.isFlaggedForReview)
                        }
                    }
                )
            }
        }

        // 4. Sleek, Unified Pill Input Bar
        Surface(
            color = CardSand,
            shape = RoundedCornerShape(26.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, BorderSand),
            shadowElevation = 1.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp, bottom = 4.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Camera Button inside pill
                Surface(
                    color = AppBackground,
                    shape = CircleShape,
                    border = androidx.compose.foundation.BorderStroke(1.dp, BorderSand),
                    modifier = Modifier
                        .size(38.dp)
                        .pressClickable {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            screenshotLauncher.launch("image/*")
                        }
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.CameraAlt, contentDescription = "Upload Screenshot", tint = GuardianSienna, modifier = Modifier.size(18.dp))
                    }
                }

                Spacer(modifier = Modifier.width(10.dp))

                // Responsive Single/Multi-line BasicTextField inside pill
                BasicTextField(
                    value = inputText,
                    onValueChange = { inputText = it },
                    textStyle = TextStyle(
                        color = TextPrimary,
                        fontSize = 14.sp,
                        fontFamily = FontFamily.SansSerif,
                        lineHeight = 20.sp
                    ),
                    maxLines = 4,
                    decorationBox = { innerTextField ->
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .padding(vertical = 6.dp),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            if (inputText.isEmpty()) {
                                Text(
                                    text = "Ask about cyber safety or Indian law...",
                                    color = TextSubtitle,
                                    fontSize = 13.5.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            innerTextField()
                        }
                    },
                    modifier = Modifier.weight(1f)
                )

                Spacer(modifier = Modifier.width(8.dp))

                // Send Button inside pill
                Surface(
                    color = if (inputText.isNotBlank()) GuardianSienna else TextSubtitle.copy(alpha = 0.35f),
                    shape = CircleShape,
                    modifier = Modifier
                        .size(38.dp)
                        .pressClickable {
                            if (inputText.isNotBlank()) {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                val userText = inputText
                                inputText = ""
                                messages.add(ChatMessage("u_${System.currentTimeMillis()}", true, userText, language = selectedLang))

                                val metadata = MessageMetadata(
                                    sender = "User_Query",
                                    timestamp = System.currentTimeMillis(),
                                    platform = "Companion Advisor",
                                    content = userText,
                                    isLateNight = false,
                                    rejectionClarity = 0,
                                    frequencyCount = 1
                                )
                                val analysis = AnalyzeEngine.analyze(metadata)
                                val aiReply = CompanionAiEngine.generateIntelligentReply(userText, analysis, selectedLang, messages.toList())
                                messages.add(
                                    ChatMessage(
                                        "ai_${System.currentTimeMillis()}",
                                        false,
                                        aiReply,
                                        language = selectedLang,
                                        riskScore = analysis.riskScore,
                                        riskTier = analysis.tier
                                    )
                                )
                            }
                        }
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send", tint = Color.White, modifier = Modifier.size(17.dp))
                    }
                }
            }
        }
    }

    if (showSettingsDialog) {
        AlertDialog(
            onDismissRequest = { showSettingsDialog = false },
            title = {
                Text(
                    "Companion AI Settings",
                    color = TextPrimary,
                    fontFamily = FontFamily.Serif,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )
            },
            text = {
                Column {
                    Text(
                        "Enter your Gemini API Key to enable generative AI responses and dynamic complaint drafting. Get one from Google AI Studio.",
                        color = TextSubtitle,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = geminiApiKeyInput,
                        onValueChange = { geminiApiKeyInput = it },
                        label = { Text("API Key") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        )
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    val sharedPrefs = context.getSharedPreferences("guardian_ai_prefs", android.content.Context.MODE_PRIVATE)
                    sharedPrefs.edit().putString("gemini_api_key", geminiApiKeyInput).apply()
                    com.example.guardianai.pipeline.GeminiApiClient.setApiKey(geminiApiKeyInput)
                    showSettingsDialog = false
                }) {
                    Text("Save", color = GuardianSienna, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showSettingsDialog = false }) {
                    Text("Cancel", color = TextSubtitle)
                }
            },
            containerColor = CardSand
        )
    }

    // Modal Dialog: Guided Report Wizard (Feature B)
    if (showReportWizard) {
        val vaultLogs = remember { ListenService.getLogs() }
        val draftText = remember(selectedLang) {
            CuratedLegalDatabase.generateGuidedReportDraft(vaultLogs, emptyList(), lang = selectedLang)
        }
        AlertDialog(
            onDismissRequest = { showReportWizard = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Article, contentDescription = null, tint = GuardianSienna)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("📝 Guided Report Wizard", fontFamily = FontFamily.Serif, fontWeight = FontWeight.Bold, fontSize = 20.sp, color = TextPrimary)
                }
            },
            text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    Text(
                        text = "Step-by-step complaint drafting assistant pre-filled from your local Evidence Vault. Review carefully per our 'human-in-the-loop' safety rule before submitting to official police authorities (`cybercrime.gov.in`).",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSubtitle,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                    Surface(
                        color = CardSand,
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, BorderSand)
                    ) {
                        Text(
                            text = draftText,
                            color = TextPrimary,
                            style = MaterialTheme.typography.bodySmall,
                            fontFamily = FontFamily.Monospace,
                            lineHeight = 18.sp,
                            modifier = Modifier.padding(14.dp)
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        val clip = ClipData.newPlainText("Guided Report Draft", draftText)
                        clipboard.setPrimaryClip(clip)
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        showReportWizard = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = GuardianSienna)
                ) {
                    Text("📋 Copy Draft for Portal", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showReportWizard = false }) {
                    Text("Close", color = TextSubtitle)
                }
            },
            containerColor = AppBackground,
            shape = RoundedCornerShape(20.dp)
        )
    }

    // Modal Dialog: Local Legal-Aid & Helpline Directory (Feature B)
    if (showHelplineDirectory) {
        AlertDialog(
            onDismissRequest = { showHelplineDirectory = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Phone, contentDescription = null, tint = StatusEmerald)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("📞 Local Legal Aid & Helplines", fontFamily = FontFamily.Serif, fontWeight = FontWeight.Bold, fontSize = 20.sp, color = TextPrimary)
                }
            },
            text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Verified India-wide national helplines and legal aid resources. Always available 24x7:",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSubtitle
                    )
                    CuratedLegalDatabase.helplines.forEach { helpline ->
                        Surface(
                            color = CardSand,
                            shape = RoundedCornerShape(12.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, BorderSand),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                                    Text(helpline.name, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    Surface(
                                        color = GuardianSienna.copy(alpha = 0.15f),
                                        shape = RoundedCornerShape(6.dp)
                                    ) {
                                        Text(helpline.numberOrUrl, color = GuardianSienna, fontWeight = FontWeight.Bold, fontSize = 12.sp, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                                    }
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(helpline.description, color = TextSubtitle, fontSize = 12.sp, lineHeight = 16.sp)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { showHelplineDirectory = false },
                    colors = ButtonDefaults.buttonColors(containerColor = TextPrimary)
                ) {
                    Text("Got It", color = Color.White)
                }
            },
            containerColor = AppBackground,
            shape = RoundedCornerShape(20.dp)
        )
    }
}

@Composable
fun SafetyPromptChip(label: String, onClick: () -> Unit) {
    val haptic = LocalHapticFeedback.current
    Surface(
        color = CardSand,
        shape = RoundedCornerShape(100.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderSand),
        modifier = Modifier.pressClickable {
            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            onClick()
        }
    ) {
        Text(
            text = label,
            color = TextPrimary,
            fontSize = 11.5.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
        )
    }
}

@Composable
fun EditorialCounselCard(message: ChatMessage, onToggleFlag: () -> Unit = {}) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val timeFormatted = remember(message.timestamp) {
        SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(message.timestamp))
    }

    if (message.isUser) {
        // User Query Bubble (Right-aligned sleek Charcoal pill)
        Column(
            modifier = Modifier.fillMaxWidth().padding(start = 50.dp),
            horizontalAlignment = Alignment.End
        ) {
            Surface(
                color = TextPrimary, // #2C2B29 Deep Charcoal
                shape = RoundedCornerShape(18.dp, 18.dp, 4.dp, 18.dp),
                shadowElevation = 1.dp
            ) {
                Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)) {
                    Text(
                        text = message.text,
                        color = Color.White,
                        fontSize = 14.sp,
                        lineHeight = 20.sp
                    )
                }
            }
            Spacer(modifier = Modifier.height(3.dp))
            Text(timeFormatted, color = TextSubtitle, fontSize = 11.sp)
        }
    } else {
        // AI Companion Bubble (Left-aligned clean Sand/Ivory card)
        Column(
            modifier = Modifier.fillMaxWidth().padding(end = 16.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 5.dp, start = 2.dp)
            ) {
                Surface(
                    shape = CircleShape,
                    color = GuardianSienna,
                    modifier = Modifier.size(18.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.SupportAgent, contentDescription = null, tint = Color.White, modifier = Modifier.size(11.dp))
                    }
                }
                Spacer(modifier = Modifier.width(6.dp))
                Text("Companion · Cyber Law Guide", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                Spacer(modifier = Modifier.width(8.dp))
                if (message.isFlaggedForReview) {
                    Surface(
                        color = GuardianSienna.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text("🚩 Flagged", color = GuardianSienna, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                    }
                }
            }

            Surface(
                color = CardSand, // #F0ECE5 warm natural sand
                shape = RoundedCornerShape(4.dp, 18.dp, 18.dp, 18.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, BorderSand),
                shadowElevation = 1.dp
            ) {
                Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp)) {
                    Text(
                        text = message.text,
                        color = TextPrimary,
                        fontSize = 14.sp,
                        lineHeight = 21.5.sp
                    )
                    
                    Spacer(modifier = Modifier.height(10.dp))
                    // Subtle, non-intrusive action buttons row at bottom right of the card
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(
                            onClick = {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                val clip = ClipData.newPlainText("Companion Advice", message.text)
                                clipboard.setPrimaryClip(clip)
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            },
                            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                            modifier = Modifier.height(24.dp)
                        ) {
                            Icon(Icons.Default.ContentCopy, contentDescription = null, tint = TextSubtitle, modifier = Modifier.size(12.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Copy", color = TextSubtitle, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                        }

                        Spacer(modifier = Modifier.width(4.dp))

                        TextButton(
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                onToggleFlag()
                            },
                            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                            modifier = Modifier.height(24.dp)
                        ) {
                            Icon(Icons.Default.Flag, contentDescription = null, tint = if (message.isFlaggedForReview) GuardianSienna else TextSubtitle, modifier = Modifier.size(12.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                if (message.isFlaggedForReview) "Flagged" else "Flag Citation",
                                color = if (message.isFlaggedForReview) GuardianSienna else TextSubtitle,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(3.dp))
            Text(timeFormatted, color = TextSubtitle, fontSize = 11.sp, modifier = Modifier.padding(start = 4.dp))
        }
    }
}
