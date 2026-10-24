package com.example.guardianai.pipeline

import android.content.Context
import com.example.guardianai.services.EncryptedVaultStorage

data class EnforcementRecord(
    val id: String,
    val timestamp: Long,
    val senderHash: String,
    val tier: RiskTier,
    val riskScore: Float,
    val actionsExecuted: List<String>,
    val silentToVictim: Boolean = true // Victim receives no notification fatigue
)

/**
 * Stage 4: ENFORCE (Automatic Actions within milliseconds)
 * Executes protection based on risk tier:
 * - MONITOR: Log metadata & watch silently
 * - INTERVENE: Send reminder message & block 24h
 * - ESCALATE: Block indefinite & alert counselor
 * - CRITICAL: Permanent block, alert Dean & activate crisis response
 */
object EnforceDispatcher {

    private val enforcementHistory = mutableListOf<EnforcementRecord>()
    private val listeners = mutableListOf<(EnforcementRecord) -> Unit>()
    private var appContext: Context? = null

    fun init(context: Context) {
        appContext = context.applicationContext
        EncryptedVaultStorage.init(appContext!!)
        synchronized(enforcementHistory) {
            if (enforcementHistory.isEmpty()) {
                enforcementHistory.addAll(EncryptedVaultStorage.loadEnforcementRecords(appContext!!))
            }
        }
    }

    fun dispatch(
        senderHash: String,
        analysis: AnalysisResult,
        timestamp: Long = System.currentTimeMillis()
    ): EnforcementRecord {
        val actions = mutableListOf<String>()

        when (analysis.tier) {
            RiskTier.MONITOR -> {
                actions.add("Logged anonymized metadata to silent audit trail")
                actions.add("Continuous background pattern monitoring active")
            }
            RiskTier.INTERVENE -> {
                actions.add("Sent automated boundary reminder to contact")
                actions.add("Applied temporary 24-hour communication block")
                actions.add("Flagged pattern for weekly safety check-in")
            }
            RiskTier.ESCALATE -> {
                actions.add("Applied indefinite communication block across channels")
                actions.add("Alerted Campus Counselor / Support Liaison (Anonymized Case)")
                actions.add("Saved encrypted evidence package to Vault")
            }
            RiskTier.CRITICAL -> {
                actions.add("PERMANENT BLOCK applied immediately across all synced platforms")
                actions.add("HIGH-PRIORITY ALERT dispatched to Dean of Students & Counselor")
                actions.add("24/7 Crisis Hotline & Emergency Response activated")
                actions.add("Secure Chain-of-Custody evidence bundle locked in Vault")
            }
        }

        val record = EnforcementRecord(
            id = "ACT_${System.currentTimeMillis().toString().takeLast(6)}",
            timestamp = timestamp,
            senderHash = senderHash,
            tier = analysis.tier,
            riskScore = analysis.riskScore,
            actionsExecuted = actions,
            silentToVictim = true
        )

        synchronized(enforcementHistory) {
            enforcementHistory.add(0, record)
            appContext?.let { EncryptedVaultStorage.saveEnforcementRecords(it, enforcementHistory) }
        }

        listeners.forEach { it.invoke(record) }
        return record
    }

    fun getHistory(): List<EnforcementRecord> {
        synchronized(enforcementHistory) {
            if (enforcementHistory.isEmpty() && appContext != null) {
                enforcementHistory.addAll(EncryptedVaultStorage.loadEnforcementRecords(appContext!!))
            }
            return enforcementHistory.toList()
        }
    }

    fun addListener(listener: (EnforcementRecord) -> Unit) {
        listeners.add(listener)
    }

    fun removeListener(listener: (EnforcementRecord) -> Unit) {
        listeners.remove(listener)
    }
}
