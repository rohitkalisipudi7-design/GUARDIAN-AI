package com.example.guardianai.pipeline

import com.example.guardianai.data.CuratedLegalDatabase
import com.example.guardianai.ui.screens.ChatMessage

/**
 * Dynamic RAG Synthesizer Engine — Triage-First & Calm Guidance
 *
 * Enforces System Prompt Rules:
 * 1. Triage-First Rule: Always ask 1 short 5-option clarifying question for vague harassment messages ("someone is harassing me").
 * 2. Accuracy & Citation Rules: Zero fake verification stamps or invented dates ([Verified: July 2026]).
 * 3. Non-Alarmist Tone: Short, calm, supportive prose without scary legal jargon.
 * 4. Multi-Turn Context Retention once scenario is confirmed.
 */
object DynamicRagSynthesizer {

    fun synthesizeResponse(userText: String, analysis: AnalysisResult?, lang: String, history: List<ChatMessage> = emptyList()): String {
        val lower = userText.lowercase().trim()
        val userTurns = history.filter { it.isUser }.map { it.text }
        val combinedText = (userTurns.joinToString(" ") + " " + lower).lowercase()

        // 1. Check if message is a vague harassment complaint needing triage clarification
        if (isVagueHarassmentMessage(lower, userTurns)) {
            return generateTriageClarification(lang)
        }

        // 2. Check if user is responding to triage options (a, b, c, d, e) or explicitly specified scenario
        val scenario = detectScenario(lower, combinedText)
        if (scenario != null && isExplicitScenarioOrOption(lower, userTurns)) {
            return generateScenarioSpecificGuidance(scenario, lang)
        }

        // 3. Greetings
        if (isGreeting(lower) && userTurns.size <= 1) {
            return generateWarmGreeting(lang)
        }

        // 4. General Help Requests
        if (isGeneralHelpRequest(lower)) {
            return generateGeneralHelpResponse(lang)
        }

        // 5. Identity Query
        if (isIdentityQuery(lower)) {
            return generateIdentityResponse(lang)
        }

        // 6. RAG Legal Query
        return generateLegalRAGResponse(userText, combinedText, lang)
    }

    private fun isVagueHarassmentMessage(lower: String, userTurns: List<String>): Boolean {
        // If user already specified details in previous turns (e.g. "morphed", "money", "doxx"), not vague
        val hasPriorDetails = userTurns.any { turn ->
            val t = turn.lowercase()
            t.contains("morph") || t.contains("nude") || t.contains("photo") || t.contains("upi") ||
            t.contains("money") || t.contains("bank") || t.contains("dox") || t.contains("address")
        }
        if (hasPriorDetails) return false

        // Check if message mentions harassment/trouble/threats without specifying what type
        val specifiesDetails = lower.contains("morph") || lower.contains("photo") || lower.contains("pic") ||
                lower.contains("nude") || lower.contains("money") || lower.contains("upi") || lower.contains("dox") ||
                lower.contains("address") || lower.contains("bank") || lower.contains("scam") || lower.contains("otp") ||
                lower == "a" || lower == "b" || lower == "c" || lower == "d" || lower == "e" || lower.startsWith("option")

        val containsVagueKeyword = lower.contains("harass") || lower.contains("troubl") || lower.contains("bother") ||
                lower.contains("bothering") || lower == "someone is threatening me" || lower == "someone is bothering me"

        return containsVagueKeyword && !specifiesDetails
    }

    private fun isExplicitScenarioOrOption(lower: String, userTurns: List<String>): Boolean {
        if (lower == "a" || lower == "b" || lower == "c" || lower == "d" || lower == "e" || lower.startsWith("option")) return true
        val hasPriorClarification = userTurns.any { isVagueHarassmentMessage(it.lowercase(), emptyList()) }
        return hasPriorClarification || lower.contains("morph") || lower.contains("photo") || lower.contains("upi") || lower.contains("dox")
    }

    private fun generateTriageClarification(lang: String): String {
        return when (lang) {
            "Hindi" -> """
                यह सुनकर दुख हुआ। आपको सही कानूनी कदम बताने के लिए — क्या यह घटना:
                (a) धमकी या अभद्र संदेश है,
                (b) फर्जी/मॉर्फ्ड फोटो या वीडियो है,
                (c) आपकी निजी जानकारी शेयर करना (Doxxing) है,
                (d) पीछा करना/बार-बार संपर्क करना (Stalking) है, या
                (e) कुछ और है?
            """.trimIndent()
            "Telugu" -> """
                మీకు ఇలా జరగడం విచారకరం. సరైన చట్టపరమైన మార్గాన్ని చెప్పడానికి — ఇది:
                (a) బెదిరింపులు లేదా అసభ్యకరమైన మెసేజ్లా,
                (b) నకిలీ/మార్ఫింగ్ చేసిన ఫోటోలు లేదా వీడియోలా,
                (c) మీ వ్యక్తిగత సమాచారాన్ని ఇతరులకు పంచుకోవడమా (Doxxing),
                (d) స్టాకింగ్/రహస్యంగా గమనించడమా, లేదా
                (e) వేరే ఏదైనా నా?
            """.trimIndent()
            else -> """
                Sorry that's happening. To point you to the right steps — is this:
                (a) threats or abusive messages,
                (b) fake/morphed photos or videos,
                (c) someone sharing your private info (doxxing),
                (d) stalking/repeated contact, or
                (e) something else?
            """.trimIndent()
        }
    }

    private fun detectScenario(lower: String, combinedText: String): String? {
        return when {
            lower == "a" || lower.startsWith("option a") || lower.contains("threat") || lower.contains("abusive") -> "THREATS"
            lower == "b" || lower.startsWith("option b") || lower.contains("morph") || lower.contains("fake photo") || lower.contains("nude") -> "MORPHED_MEDIA"
            lower == "c" || lower.startsWith("option c") || lower.contains("dox") || lower.contains("private info") -> "DOXXING"
            lower == "d" || lower.startsWith("option d") || lower.contains("stalk") -> "STALKING"
            lower == "e" || lower.startsWith("option e") || lower.contains("something else") -> "OTHER"
            lower.contains("upi") || lower.contains("money") || lower.contains("bank") -> "FINANCIAL_SCAM"
            else -> null
        }
    }

    private fun generateScenarioSpecificGuidance(scenario: String, lang: String): String {
        return when (scenario) {
            "MORPHED_MEDIA" -> """
                Stay calm and do not comply with extortion demands. Creating or sharing morphed explicit images falls under Section 67A and Section 66E of the IT Act.
                
                **Recommended Action Plan:**
                1. **Preserve Evidence**: Save screenshots of all chat logs, handles, and URLs in your Evidence Vault.
                2. **Prevent Image Spread**: Visit StopNCII.org to generate an on-device digital hash to block image dissemination across platforms.
                3. **File Report**: Submit a formal complaint at **cybercrime.gov.in** under Women & Child Safety, or call **1091** (Women Helpline) / **1930**.
                
                Want me to help you draft the formal complaint text next?
            """.trimIndent()
            "THREATS" -> """
                Sending abusive threats or intimidation online generally falls under harassment provisions of Indian law.
                
                **Recommended Action Plan:**
                1. **Save Screenshots**: Keep clear evidence of the messages and sender profile in your Evidence Vault.
                2. **Cease Contact & Block**: Do not engage or escalate; block the sender immediately.
                3. **Report**: File a complaint on **cybercrime.gov.in** or call **1930** / **112**.
                
                Would you like me to help draft a complaint summary for this?
            """.trimIndent()
            "DOXXING" -> """
                Publishing your private personal information online without consent violates data privacy principles under Indian law.
                
                **Recommended Action Plan:**
                1. **Capture Evidence**: Save screenshots and exact web links showing where your information was posted.
                2. **Request Takedown**: Use the platform's reporting tool to flag the post for immediate privacy violation takedown.
                3. **Report Portal**: Submit a complaint at **cybercrime.gov.in**.
                
                Want me to help you draft the reporting text?
            """.trimIndent()
            "STALKING" -> """
                Repeatedly following or messaging someone online despite clear disinterest generally falls under Section 354D of the Bharatiya Nyaya Sanhita (cyberstalking).
                
                **Recommended Action Plan:**
                1. **One-Strike Refusal**: Send one clear refusal ("Stop contacting me"), then cease all dialogue.
                2. **Save Evidence**: Store all chat timestamps in your Evidence Vault.
                3. **Block & Report**: Block the account and file a report at **cybercrime.gov.in** or call **1091**.
                
                Should we prepare a complaint draft for you?
            """.trimIndent()
            else -> """
                Whatever the situation, start by saving clear screenshots in your Evidence Vault and blocking the sender. You can file a report at **cybercrime.gov.in** or call **1930** for assistance.
            """.trimIndent()
        }
    }

    private fun isGreeting(lower: String): Boolean {
        val greetingWords = listOf("hi", "hello", "hey", "hlo", "namaste", "good morning", "good evening")
        return greetingWords.any { lower == it || lower.startsWith("$it ") || lower.startsWith("$it!") }
    }

    private fun generateWarmGreeting(lang: String): String {
        return "Hello! I am **Guardian AI's Cyber Law Guide**. I help people understand digital safety, Indian cyber laws, and evaluate suspicious messages or harassment. How can I help you today?"
    }

    private fun isGeneralHelpRequest(lower: String): Boolean {
        return lower == "help" || lower == "help me" || lower.startsWith("help me") || lower.contains("i need help")
    }

    private fun generateGeneralHelpResponse(lang: String): String {
        return "I'm here to help. Could you tell me a little about what's going on—is this related to harassment, a suspicious link, payment fraud, or device security?"
    }

    private fun isIdentityQuery(lower: String): Boolean {
        return lower.contains("who are you") || lower.contains("what can you do")
    }

    private fun generateIdentityResponse(lang: String): String {
        return "I am Guardian AI's Cyber Law Guide, a safety companion for Indian users. I provide plain-language legal information regarding the IT Act, DPDP Act, BNS, and POCSO Act, and guide you on filing official complaints."
    }

    private fun generateLegalRAGResponse(userText: String, combinedText: String, lang: String): String {
        val provisions = CuratedLegalDatabase.searchProvisions(combinedText)
        val primaryProv = provisions.firstOrNull() ?: CuratedLegalDatabase.provisions.first { it.id == "it_66e" }

        return """
This situation generally relates to ${primaryProv.actName} (${primaryProv.sectionNumber}). In plain language, ${primaryProv.plainLanguageExplanation.lowercase()}

**Action Steps:**
1. Save clear screenshots of all messages and handles in your Evidence Vault.
2. Block the sender and do not engage.
3. Submit a report at **cybercrime.gov.in** or call **1930** / **1091**.

Want me to help you draft the formal complaint text?
""".trimIndent()
    }
}
