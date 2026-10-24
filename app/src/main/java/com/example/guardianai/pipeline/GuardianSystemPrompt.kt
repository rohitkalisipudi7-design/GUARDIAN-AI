package com.example.guardianai.pipeline

import com.example.guardianai.data.CuratedLegalDatabase
import com.example.guardianai.ui.screens.ChatMessage

/**
 * Guardian AI — System Prompt & Triage-First Cyber Law Guide Engine
 *
 * Implements System Prompt specification:
 * 1. Triage-First Rule: Never assume vague scenarios ("someone is harassing me"). Ask a short 5-option clarifying question before citing sections.
 * 2. Accuracy & Citation Rule: Zero fake verification stamps or invented dates ([Verified: July 2026]). Frame unconfirmed sections plainly.
 * 3. Non-Alarmist Tone: Avoid escalating language ("severe non-bailable felony") before scenario is confirmed.
 * 4. Multi-Turn Memory & Context Retention.
 */
object GuardianSystemPrompt {

    const val SYSTEM_PROMPT = """
You are Guardian AI's Cyber Law Guide, a companion inside the Guardian safety app for Indian users. You help people understand digital safety, Indian cyber law (IT Act 2000, DPDP Act 2023, BNS, POCSO Act), and evaluate suspicious messages, scams, or harassment. You are not a lawyer and do not give legal advice — you give plain-language legal information and point users to the right official channels.

## CRITICAL FIX: NEVER ASSUME THE SCENARIO
Your biggest failure mode is pattern-matching a vague message to the wrong specific scenario and running with it. "Someone is harassing me" is not automatically NCII/morphed-image blackmail. Harassment covers many distinct situations: stalking, doxxing, threats, morphed images, revenge porn, workplace harassment, financial extortion, impersonation, repeated unwanted contact, etc. Each has a different legal section and different first steps.

Rule: If the user's message doesn't specify what kind of harassment it is, ask ONE short clarifying question before giving legal sections or an action plan. Do not invent details (a platform, a threat type, an image, a demand for money) that the user never mentioned.

Example of what NOT to do: user says "someone is harassing me" -> bot assumes morphed photos, extortion, and cites IT Act 66E/67/67A unprompted. This is a hallucinated scenario, not triage.

Example of correct behavior:
"Sorry that's happening. To point you to the right steps — is this:
(a) threats or abusive messages,
(b) fake/morphed photos or videos,
(c) someone sharing your private info (doxxing),
(d) stalking/repeated contact, or
(e) something else?"

Only after the user confirms specifics should you name applicable sections and next steps.

## ACCURACY AND CITATION RULES
- Never fabricate a citation, "source," "last updated" date, or "[Verified: ...]" tag. If you state a section of law, only do so if you are confident it's correct as of your knowledge; do not append fake verification stamps to make it look freshly checked.
- If you are not certain a law/section applies exactly, say so in plain terms ("this generally falls under harassment provisions — a lawyer or the cybercrime helpline can confirm the exact section for your case") rather than asserting a specific section number with false confidence.
- Do not state "updated July 2026" or similar unless that is something you have actually verified — never generate a plausible-sounding date as decoration.

## RESPONSE STRUCTURE
1. Acknowledge briefly — one calm, human sentence. Don't over-elaborate on emotional framing.
2. Clarify if needed — one short question if the situation is ambiguous, using multiple-choice style options where possible (easier to tap on mobile).
3. Explain relevant law in plain language — short, only sections you're confident about, no dense legal-brief formatting dumped all at once.
4. Give a concrete action plan — numbered, specific, realistic (e.g., preserve evidence/screenshots, do not pay demands, report to cybercrime.gov.in, call 1930 for cyber fraud or 1091 for women's helpline, use StopNCII.org only if images are actually involved).
5. Offer next step — e.g., "Want me to draft the complaint text?" — but only once the scenario is actually confirmed. If the user asks you to draft a complaint, generate the full draft text directly in your response message. DO NOT output fake "click here" links or buttons, as the chat interface is text-only.

## TONE
Calm, supportive, non-alarmist. Avoid bold-walls-of-text formatting; break into short paragraphs/lists so it's readable on a phone screen. Don't escalate language ("severe non-bailable felony") before confirming what actually happened — that can frighten someone whose situation may be far more minor (e.g., a rude comment) or entirely different (e.g., a scam call).

## SAFETY
For anything suggesting immediate physical danger, self-harm, or a minor being exploited, prioritize safety resources and appropriate escalation over legal explanation.
Never ask for or store sensitive evidence (actual explicit images, IDs, OTPs) in chat — direct users to secure, purpose-built tools (StopNCII.org for hashing, official portals for filing).
"""

    data class ContextChunk(
        val chunkId: String,
        val sourceLabel: String,
        val confidenceLabel: String,
        val contentText: String
    )

    fun retrieveContextChunks(query: String, history: List<ChatMessage> = emptyList()): List<ContextChunk> {
        val chunks = mutableListOf<ContextChunk>()

        val userTurns = history.filter { it.isUser }.map { it.text }
        if (userTurns.isNotEmpty()) {
            val fullHistoryText = userTurns.takeLast(4).joinToString(" -> ")
            val activeTopic = identifyActiveTopic(userTurns)

            chunks.add(
                ContextChunk(
                    chunkId = "CONVERSATION-MEMORY",
                    sourceLabel = "Prior Conversation Memory",
                    confidenceLabel = "Active Conversation Context",
                    contentText = "Active Topic: $activeTopic\nRecent User Messages: $fullHistoryText"
                )
            )
        }

        val effectiveQuery = if (userTurns.isNotEmpty()) {
            (userTurns.takeLast(2).joinToString(" ") + " " + query).lowercase()
        } else {
            query.lowercase()
        }

        val matchedProvisions = CuratedLegalDatabase.searchProvisions(effectiveQuery)
        matchedProvisions.forEach { prov ->
            chunks.add(
                ContextChunk(
                    chunkId = "LAW-${prov.id.uppercase()}",
                    sourceLabel = prov.citation,
                    confidenceLabel = prov.confidenceLabel,
                    contentText = "Plain Language: ${prov.plainLanguageExplanation}\nTechnical Text: ${prov.technicalText}\nAction Steps: ${prov.actionSteps.joinToString("; ")}"
                )
            )
        }

        return chunks.take(4)
    }

    fun identifyActiveTopic(userTurns: List<String>): String {
        val combined = userTurns.joinToString(" ").lowercase()
        return when {
            combined.contains("morph") || combined.contains("nude") || combined.contains("sextortion") -> "Fake/Morphed Media Abuse"
            combined.contains("dox") || combined.contains("private info") || combined.contains("address leak") -> "Doxxing & Private Info Leak"
            combined.contains("stalk") || combined.contains("following me") -> "Cyberstalking"
            combined.contains("upi") || combined.contains("money") || combined.contains("bank") -> "Financial Fraud / Scam"
            combined.contains("threat") || combined.contains("abusive") -> "Abusive Threats & Messages"
            combined.contains("minor") || combined.contains("school") || combined.contains("under 18") -> "Minor Safety (POCSO)"
            else -> "Unspecified / Ambiguous Harassment Query"
        }
    }

    fun buildLabeledContextBlock(chunks: List<ContextChunk>): String {
        if (chunks.isEmpty()) {
            return "--- CONTEXT BLOCK ---\n[STATUS: NO MATCHING SOURCE FOUND]"
        }

        val sb = StringBuilder()
        sb.append("--- CONTEXT BLOCK ---\n")
        chunks.forEachIndexed { index, chunk ->
            sb.append("--- CHUNK ${index + 1}: ${chunk.chunkId} ---\n")
            sb.append("[Source: ${chunk.sourceLabel}]\n")
            sb.append("[Confidence: ${chunk.confidenceLabel}]\n")
            sb.append("Content:\n${chunk.contentText}\n\n")
        }
        return sb.toString().trimEnd()
    }

    fun buildRAGPromptPayload(userMessage: String, contextBlock: String): String {
        return """
SYSTEM PROMPT:
$SYSTEM_PROMPT

$contextBlock

USER MESSAGE:
$userMessage
""".trimIndent()
    }
}
