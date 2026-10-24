package com.example.guardianai.pipeline

data class CounselorCaseReport(
    val caseId: String,
    val riskScore: Float,
    val riskTier: RiskTier,
    val pattern: String,
    val timelineSummary: String,
    val evidenceSummary: List<String>,
    val piiProtectionStatus: String = "ALL PII ENCRYPTED / HASHED (SHA-256 + AES-256)",
    val counselorCanSee: List<String> = listOf("Case ID", "Risk Score & Tier", "Behavioral Pattern", "Escalation Timeline", "Channel Types"),
    val counselorCannotSee: List<String> = listOf("Raw Names", "Phone Numbers / Usernames", "Message Text Content", "GPS Location", "Personal Photos")
)

/**
 * Stage 5: REPORT (Anonymized to Counselor)
 * - Generates dashboard payloads for college counselors or trust & safety teams
 * - STRICT PRIVACY: Counselor CANNOT see names, phone numbers, message content, or location
 * - Victim is completely protected with zero reporting burden
 */
object ReportGenerator {

    private val activeReports = mutableListOf<CounselorCaseReport>()

    init {
        // Pre-populate with realistic demo cases showcasing different risk tiers
        activeReports.add(
            CounselorCaseReport(
                caseId = "Case #9F2K",
                riskScore = 8.8f,
                riskTier = RiskTier.CRITICAL,
                pattern = "Sextortion / Blackmail",
                timelineSummary = "5 days escalation across 3 platforms (Instagram, WhatsApp, SMS)",
                evidenceSummary = listOf(
                    "Multiple late-night communications between 1:00 AM - 4:00 AM",
                    "Continued contact after explicit boundary rejection",
                    "2 encrypted media evidence bundles locked in vault"
                )
            )
        )
        activeReports.add(
            CounselorCaseReport(
                caseId = "Case #4B7X",
                riskScore = 7.2f,
                riskTier = RiskTier.ESCALATE,
                pattern = "Cyberbullying / Harassment",
                timelineSummary = "3 days escalation on Telegram",
                evidenceSummary = listOf(
                    "High contact frequency (>25 messages/24h)",
                    "Abusive language pattern detected by AI Safeguard",
                    "Indefinite block applied automatically"
                )
            )
        )
        activeReports.add(
            CounselorCaseReport(
                caseId = "Case #1M9A",
                riskScore = 5.2f,
                riskTier = RiskTier.INTERVENE,
                pattern = "Stalking / Intimidation",
                timelineSummary = "2 days observation on WhatsApp",
                evidenceSummary = listOf(
                    "Physical location referencing language",
                    "Automated boundary reminder dispatched",
                    "24-hour cooling block active"
                )
            )
        )
    }

    fun generateReport(
        senderHash: String,
        analysis: AnalysisResult,
        timelineDays: Int = 1
    ): CounselorCaseReport {
        val shortHash = senderHash.replace("Hash_", "").take(4)
        val caseId = "Case #${shortHash}"

        val report = CounselorCaseReport(
            caseId = caseId,
            riskScore = analysis.riskScore,
            riskTier = analysis.tier,
            pattern = analysis.patternDetected,
            timelineSummary = "$timelineDays day(s) monitoring & escalation tracking",
            evidenceSummary = analysis.behavioralFeatures.ifEmpty { listOf("Routine monitoring active") }
        )

        synchronized(activeReports) {
            // Update existing or add new
            activeReports.removeAll { it.caseId == caseId }
            activeReports.add(0, report)
        }
        return report
    }

    fun getAllReports(): List<CounselorCaseReport> {
        synchronized(activeReports) {
            return activeReports.toList()
        }
    }
}
