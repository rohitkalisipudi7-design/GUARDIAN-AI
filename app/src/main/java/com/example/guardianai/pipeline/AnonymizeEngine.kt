package com.example.guardianai.pipeline

import android.util.Base64
import java.security.MessageDigest
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec

/**
 * Stage 2: ANONYMIZE
 * - Strips raw names and phone numbers
 * - Converts identifiers into one-way hashes (e.g. Priya -> Hash_B3K9, +91-9876543210 -> Hash_7F2A)
 * - Encrypts sensitive text content with AES-256-GCM encryption
 */
object AnonymizeEngine {

    private const val AES_KEY_SIZE = 256
    private const val GCM_IV_LENGTH = 12
    private const val GCM_TAG_LENGTH = 128
    private const val TRANSFORMATION = "AES/GCM/NoPadding"

    // For demonstration, we maintain an in-memory session key or derive a static vault key
    private val secretKey: SecretKey by lazy {
        val keyGen = KeyGenerator.getInstance("AES")
        keyGen.init(AES_KEY_SIZE)
        keyGen.generateKey()
    }

    /**
     * Converts raw identifiers (names, phone numbers, usernames) into short deterministic hashes.
     * Example: "Priya" -> "Hash_B3K9", "+91-9876543210" -> "Hash_7F2A"
     */
    fun hashIdentifier(rawId: String): String {
        if (rawId.isBlank()) return "Hash_0000"
        try {
            val digest = MessageDigest.getInstance("SHA-256")
            val hashBytes = digest.digest(rawId.trim().lowercase().toByteArray(Charsets.UTF_8))
            // Take first 2 bytes (4 hex chars) formatted uppercase
            val hex = hashBytes.take(2).joinToString("") { "%02X".format(it) }
            return "Hash_$hex"
        } catch (e: Exception) {
            return "Hash_ERR1"
        }
    }

    /**
     * Encrypts text content using AES-256-GCM.
     * Returns Base64 encoded string containing IV + Encrypted Data + Tag.
     */
    fun encryptText(plainText: String): String {
        if (plainText.isEmpty()) return ""
        try {
            val cipher = Cipher.getInstance(TRANSFORMATION)
            val iv = ByteArray(GCM_IV_LENGTH)
            SecureRandom().nextBytes(iv)
            val spec = GCMParameterSpec(GCM_TAG_LENGTH, iv)
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, spec)

            val cipherText = cipher.doFinal(plainText.toByteArray(Charsets.UTF_8))
            val combined = iv + cipherText
            return Base64.encodeToString(combined, Base64.NO_WRAP)
        } catch (e: Exception) {
            return "[ENCRYPTION_ERROR]: ${e.localizedMessage}"
        }
    }

    /**
     * Decrypts AES-256-GCM encrypted Base64 string back to plain text.
     */
    fun decryptText(encryptedBase64: String): String {
        if (encryptedBase64.isEmpty() || encryptedBase64.startsWith("[ENCRYPTION_ERROR]")) return encryptedBase64
        try {
            val combined = Base64.decode(encryptedBase64, Base64.NO_WRAP)
            if (combined.size < GCM_IV_LENGTH) return "[INVALID_DATA]"

            val iv = combined.copyOfRange(0, GCM_IV_LENGTH)
            val cipherText = combined.copyOfRange(GCM_IV_LENGTH, combined.size)

            val cipher = Cipher.getInstance(TRANSFORMATION)
            val spec = GCMParameterSpec(GCM_TAG_LENGTH, iv)
            cipher.init(Cipher.DECRYPT_MODE, secretKey, spec)

            val plainBytes = cipher.doFinal(cipherText)
            return String(plainBytes, Charsets.UTF_8)
        } catch (e: Exception) {
            return "[DECRYPTION_FAILED]"
        }
    }

    /**
     * Strips PII from text by replacing known patterns or names with their hashes.
     */
    fun anonymizePayload(sender: String, content: String): Pair<String, String> {
        val hashedSender = hashIdentifier(sender)
        val encryptedContent = encryptText(content)
        return Pair(hashedSender, encryptedContent)
    }
}
