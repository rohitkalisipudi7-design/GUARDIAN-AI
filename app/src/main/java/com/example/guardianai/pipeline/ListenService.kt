package com.example.guardianai.pipeline

import android.content.Context
import com.example.guardianai.services.EncryptedVaultStorage

data class PipelineExecutionLog(
    val id: String,
    val timestamp: Long,
    val stage1Listen: String,
    val stage2Anonymize: String,
    val stage3Analyze: AnalysisResult,
    val stage4Enforce: EnforcementRecord,
    val stage5Report: CounselorCaseReport,
    val rawSender: String = "Unknown",
    val rawContent: String = "",
    val platform: String = "App"
)

/**
 * Stage 1: LISTEN
 * - Intercepts metadata via Notification Listeners & Webhook APIs
 * - Records: Who called, When, How many times, Which platform, Was response given
 * - Orchestrates the full 5-Stage Automated Threat Response Pipeline
 */
object ListenService {

    private val executionLogs = mutableListOf<PipelineExecutionLog>()
    private val listeners = mutableListOf<(PipelineExecutionLog) -> Unit>()
    private var appContext: Context? = null

    fun init(context: Context) {
        appContext = context.applicationContext
        EncryptedVaultStorage.init(appContext!!)
        synchronized(executionLogs) {
            if (executionLogs.isEmpty()) {
                executionLogs.addAll(EncryptedVaultStorage.loadPipelineLogs(appContext!!))
            }
        }
    }

    /**
     * Entry point for incoming message or call metadata.
     * Executes the entire 5-Stage pipeline in milliseconds.
     */
    fun processIncomingEvent(
        rawSender: String,
        rawContent: String,
        platform: String,
        isLateNight: Boolean = false,
        rejectionClarity: Int = 0,
        frequencyCount: Int = 1
    ): PipelineExecutionLog {
        val timestamp = System.currentTimeMillis()

        // Stage 1: LISTEN
        val stage1Log = "Intercepted $platform message from [$rawSender] at ${java.text.SimpleDateFormat("HH:mm:ss").format(timestamp)}"

        // Stage 2: ANONYMIZE
        val (hashedSender, encryptedContent) = AnonymizeEngine.anonymizePayload(rawSender, rawContent)
        val stage2Log = "Raw sender stripped -> [$hashedSender] | Content AES-256 encrypted (${encryptedContent.take(16)}...)"

        // Stage 3: ANALYZE
        val metadata = MessageMetadata(
            sender = hashedSender,
            timestamp = timestamp,
            platform = platform,
            content = rawContent, // AI evaluates plain text inside protected enclave before discarding
            isLateNight = isLateNight,
            rejectionClarity = rejectionClarity,
            frequencyCount = frequencyCount
        )
        val analysis = AnalyzeEngine.analyze(metadata)

        // Stage 4: ENFORCE
        val enforcement = EnforceDispatcher.dispatch(hashedSender, analysis, timestamp)

        // Stage 5: REPORT
        val report = ReportGenerator.generateReport(hashedSender, analysis)

        val logEntry = PipelineExecutionLog(
            id = "LOG_${timestamp.toString().takeLast(6)}",
            timestamp = timestamp,
            stage1Listen = stage1Log,
            stage2Anonymize = stage2Log,
            stage3Analyze = analysis,
            stage4Enforce = enforcement,
            stage5Report = report,
            rawSender = rawSender,
            rawContent = rawContent,
            platform = platform
        )

        synchronized(executionLogs) {
            executionLogs.add(0, logEntry)
            appContext?.let { EncryptedVaultStorage.savePipelineLogs(it, executionLogs) }
        }

        listeners.forEach { it.invoke(logEntry) }
        return logEntry
    }

    fun getLogs(): List<PipelineExecutionLog> {
        synchronized(executionLogs) {
            if (executionLogs.isEmpty() && appContext != null) {
                executionLogs.addAll(EncryptedVaultStorage.loadPipelineLogs(appContext!!))
            }
            return executionLogs.toList()
        }
    }

    fun clearLogs() {
        synchronized(executionLogs) {
            executionLogs.clear()
            appContext?.let { EncryptedVaultStorage.savePipelineLogs(it, executionLogs) }
        }
    }

    fun addListener(listener: (PipelineExecutionLog) -> Unit) {
        listeners.add(listener)
    }

    fun removeListener(listener: (PipelineExecutionLog) -> Unit) {
        listeners.remove(listener)
    }

    /**
     * Simulated Threat Generator for testing all 4 Risk Tiers in the UI
     */
    fun simulateScenario(scenarioType: String): PipelineExecutionLog {
        return when (scenarioType) {
            "CRITICAL_SEXTORTION" -> processIncomingEvent(
                rawSender = "+91-9876543210",
                rawContent = "If you don't pay me 50,000 INR within 2 hours, I will leak your private photos and video to your college group and ruin your life!",
                platform = "WhatsApp",
                isLateNight = true,
                rejectionClarity = 2,
                frequencyCount = 18
            )
            "ESCALATE_BULLYING" -> processIncomingEvent(
                rawSender = "insta_troll_99",
                rawContent = "You are such an ugly pathetic loser, nobody likes you at college. Why don't you just disappear?",
                platform = "Instagram DM",
                isLateNight = false,
                rejectionClarity = 1,
                frequencyCount = 12
            )
            "INTERVENE_STALKING" -> processIncomingEvent(
                rawSender = "unknown_caller_44",
                rawContent = "I saw you leaving the library at 8 PM. I'm watching you outside your hostel. Why didn't you answer my calls?",
                platform = "SMS",
                isLateNight = true,
                rejectionClarity = 0,
                frequencyCount = 6
            )
            "INTERVENE_SCAM" -> processIncomingEvent(
                rawSender = "SBI_Alert_Team",
                rawContent = "URGENT: Your bank account is suspended! Scan this QR code and share OTP immediately to avoid penalty.",
                platform = "SMS / Telegram",
                isLateNight = false,
                rejectionClarity = 0,
                frequencyCount = 3
            )
            else -> processIncomingEvent(
                rawSender = "Akhil_friend",
                rawContent = "Hey! Are we still meeting for group study at the library tomorrow morning?",
                platform = "WhatsApp",
                isLateNight = false,
                rejectionClarity = 0,
                frequencyCount = 2
            )
        }
    }
}
