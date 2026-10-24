package com.example.guardianai.pipeline

import com.example.guardianai.data.CuratedLegalDatabase
import com.example.guardianai.ui.screens.ChatMessage
import kotlinx.coroutines.runBlocking

/**
 * Companion AI Engine — Multi-Turn RAG Context & Dynamic Synthesis
 */
object CompanionAiEngine {

    /**
     * Entry point for generating multi-turn aware, RAG-grounded responses.
     */
    fun generateIntelligentReply(userText: String, analysis: AnalysisResult?, lang: String, history: List<ChatMessage> = emptyList()): String {
        val lower = userText.lowercase().trim()

        // 1. HARD GUARDRAIL 1: Anti-Stalkerware Refusal
        if (isCovertMonitoringOrStalkerwareRequest(lower)) {
            return generateAntiStalkerwareRefusal(lang)
        }

        // 2. HARD GUARDRAIL 4: Mandatory CSAM Circuit-Breaker
        if (isSuspectedCsam(lower)) {
            return generateCsamCircuitBreaker(lang)
        }

        // 3. STRICT SCOPE RULE: Check for off-topic queries
        if (isOffTopic(lower)) {
            return generateOffTopicDeclination(lang)
        }

        // 4. SAFETY BOUNDARIES: Check if user pasted sensitive credentials
        if (containsSensitiveCredentials(lower)) {
            return generateCredentialWarningReply(lang)
        }

        // 5. SAFETY BOUNDARIES: Malware creation
        if (isMalwareOrHackingCreationRequest(lower)) {
            return generateMalwareDeclination(lang)
        }

        // 6. Screenshot Analysis
        if (lower.contains("screenshot") || lower.contains("ocr:") || lower.contains("image:") || lower.contains("photo of")) {
            return analyzeScreenshot(userText, lang)
        }

        // 7. Check if Gemini API Key is available for real-time generative LLM synthesis
        if (GeminiApiClient.hasApiKey()) {
            val contextChunks = GuardianSystemPrompt.retrieveContextChunks(userText, history)
            val contextBlock = GuardianSystemPrompt.buildLabeledContextBlock(contextChunks)
            val geminiResponse = runBlocking {
                GeminiApiClient.generateWithGeminiAPI(userText, contextBlock)
            }
            if (!geminiResponse.isNullOrBlank()) {
                return geminiResponse
            }
        }

        // 8. Dynamic RAG Synthesizer with Multi-Turn History
        return DynamicRagSynthesizer.synthesizeResponse(userText, analysis, lang, history)
    }

    private fun isCovertMonitoringOrStalkerwareRequest(lower: String): Boolean {
        return lower.contains("monitor my partner") || lower.contains("spy on my husband") || lower.contains("track my wife") ||
               lower.contains("read someone else's whatsapp") || lower.contains("how to install spyware") || lower.contains("covertly track") ||
               lower.contains("monitor employee phone secretly") || lower.contains("spy on child phone without knowing")
    }

    private fun generateAntiStalkerwareRefusal(lang: String): String {
        return when (lang) {
            "Hindi" -> "एक सुरक्षा परामर्शदाता के रूप में, मैं किसी की अनुमति के बिना फोन पर जासूसी करने में मदद नहीं कर सकता। यह IT Act 2000 की धारा 43/66 और 66E के तहत अपराध है।"
            "Telugu" -> "డిజిటల్ గోప్యతను కాపాడటమే నా బాధ్యత. ఇతరుల ఫోన్లను అనుమతి లేకుండా ట్రాక్ చేయడానికి నేను సహాయం చేయలేను. ఇది IT Act 43/66 ప్రకారం నేరం."
            else -> "As a defensive safety companion, I cannot provide tools or instructions to covertly track or monitor anyone's phone without their consent under Section 43/66/66E of the IT Act, 2000. Would you like to check your own device security?"
        }
    }

    private fun isSuspectedCsam(lower: String): Boolean {
        return lower.contains("csam") || lower.contains("nude minor") || lower.contains("child explicit") ||
               lower.contains("underage explicit") || lower.contains("child abuse photo") || lower.contains("morphed minor")
    }

    private fun generateCsamCircuitBreaker(lang: String): String {
        return "We must pause our content discussion immediately. Possession or distribution of explicit material involving a minor is a non-bailable offense under Section 67B of the IT Act and POCSO Act. Report immediately to cybercrime.gov.in or call 1098 / 1930."
    }

    fun analyzeScreenshot(textOrMetadata: String, lang: String): String {
        val lower = textOrMetadata.lowercase()
        val isUpi = lower.contains("upi") || lower.contains("collect")
        val isKyc = lower.contains("kyc") || lower.contains("bank")
        val type = if (isUpi) "UPI Payment Request" else if (isKyc) "Banking KYC Alert" else "App Permission Screen"
        val verdict = "High-Risk Threat — Likely Phishing (IT Act Section 66D)"
        return "📷 Screenshot Analysis ($type): Marked as $verdict. Do not enter your UPI PIN or download APK files. Save this screenshot inside your Evidence Vault and block the sender."
    }

    private fun isOffTopic(text: String): Boolean {
        val offTopicKeywords = listOf("write a python script", "how to code", "tell me a joke", "recipe for", "weather", "do my homework")
        return offTopicKeywords.any { text.contains(it) }
    }

    private fun generateOffTopicDeclination(lang: String): String {
        return "I am Companion — The Cyber Law Guide. My scope is strictly focused on Indian Cyber Laws (IT Act, DPDP Act, BNS, POCSO), Cyber Hygiene, and Threat Analysis. How can I help you secure your digital life today?"
    }

    private fun containsSensitiveCredentials(text: String): Boolean {
        return text.contains("my otp is") || text.contains("upi pin is") || text.contains("cvv is") || text.contains("password is")
    }

    private fun generateCredentialWarningReply(lang: String): String {
        return "🚨 Safety Shield: Sensitive credentials detected. Never share your OTP or UPI PIN with anyone. Change your PIN immediately in your banking app and call 1930."
    }

    private fun isMalwareOrHackingCreationRequest(text: String): Boolean {
        return text.contains("how to create malware") || text.contains("make a phishing page") || text.contains("hack someone's whatsapp")
    }

    private fun generateMalwareDeclination(lang: String): String {
        return "I cannot provide code or instructions for creating malware or launching cyber attacks under Section 43 and Section 66 of the IT Act, 2000."
    }
}
