package com.example.guardianai.pipeline

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.widget.Toast
import com.example.guardianai.services.TrustedContact

/**
 * Task #1: WhatsApp Distress Telephony Dispatcher
 * Allows zero-latency distress messaging to emergency contacts via WhatsApp API deep links
 * and direct ACTION_SEND intents.
 */
object WhatsAppSosDispatcher {

    fun isWhatsAppInstalled(context: Context): Boolean {
        return try {
            context.packageManager.getPackageInfo("com.whatsapp", PackageManager.GET_ACTIVITIES)
            true
        } catch (e: Exception) {
            try {
                context.packageManager.getPackageInfo("com.whatsapp.w4b", PackageManager.GET_ACTIVITIES)
                true
            } catch (e2: Exception) {
                false
            }
        }
    }

    fun cleanPhoneForWhatsApp(phone: String): String {
        // Remove spaces, dashes, parentheses
        val digits = phone.replace(Regex("[^0-9+]"), "")
        // If it starts with +, strip it for api.whatsapp.com
        return if (digits.startsWith("+")) {
            digits.substring(1)
        } else if (digits.length == 10 && !digits.startsWith("91")) {
            // Default India prefix if missing and 10 digits
            "91$digits"
        } else {
            digits
        }
    }

    /**
     * Launches direct 1-tap WhatsApp pre-filled distress message to the given trusted contact node.
     */
    fun dispatchToWhatsAppNode(context: Context, contact: TrustedContact, distressMessage: String) {
        val cleanNumber = cleanPhoneForWhatsApp(contact.phone)
        if (cleanNumber.isEmpty()) {
            Toast.makeText(context, "Invalid phone number for ${contact.name}", Toast.LENGTH_SHORT).show()
            return
        }

        try {
            val encodedMsg = Uri.encode(distressMessage)
            // 1. Try direct whatsapp:// protocol first as it opens inside WhatsApp without browser prompt
            val uri = Uri.parse("whatsapp://send?phone=$cleanNumber&text=$encodedMsg")
            val intent = Intent(Intent.ACTION_VIEW, uri).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            try {
                val encodedMsg = Uri.encode(distressMessage)
                // 2. Fallback to https://api.whatsapp.com/send which works via browser or app
                val uri = Uri.parse("https://api.whatsapp.com/send?phone=$cleanNumber&text=$encodedMsg")
                val intent = Intent(Intent.ACTION_VIEW, uri).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(intent)
            } catch (e2: Exception) {
                // 3. Fallback to direct ACTION_SEND if deep link fails
                broadcastViaWhatsApp(context, distressMessage)
            }
        }
    }

    /**
     * Dispatches sequentially to ALL contacts on WhatsApp.
     * Since Android blocks background startActivity when WhatsApp opens in foreground,
     * we dispatch the first contact immediately, and for subsequent contacts we use both
     * background-allowed flags and provide direct notification/toast cues.
     */
    fun dispatchToAllContactsSequentially(context: Context, contacts: List<TrustedContact>, message: String) {
        if (contacts.isEmpty()) return

        // 1. Dispatch first contact immediately
        dispatchToWhatsAppNode(context, contacts[0], message)

        // 2. For subsequent contacts, schedule with 4.5s intervals and bring task to front / allow background launch
        if (contacts.size > 1) {
            val handler = android.os.Handler(android.os.Looper.getMainLooper())
            for (i in 1 until contacts.size) {
                val contact = contacts[i]
                handler.postDelayed({
                    try {
                        Toast.makeText(context, "🟢 WhatsApp SOS -> ${contact.name} (${i + 1}/${contacts.size}). Tap Send in WhatsApp!", Toast.LENGTH_LONG).show()
                        val cleanNumber = cleanPhoneForWhatsApp(contact.phone)
                        if (cleanNumber.isNotEmpty()) {
                            val encodedMsg = Uri.encode(message)
                            val uri = Uri.parse("whatsapp://send?phone=$cleanNumber&text=$encodedMsg")
                            val intent = Intent(Intent.ACTION_VIEW, uri).apply {
                                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                            }
                            // Attempt direct launch or activity start
                            context.startActivity(intent)
                        }
                    } catch (e: Exception) {
                        Toast.makeText(context, "⚠️ Please tap ${contact.name} in Guardian AI to complete WhatsApp dispatch", Toast.LENGTH_LONG).show()
                    }
                }, i * 4500L)
            }
        }
    }

    /**
     * Broadcast distress payload via general WhatsApp share (fallback if specific contact link fails or node unverified)
     */
    fun broadcastViaWhatsApp(context: Context, distressMessage: String) {
        try {
            val sendIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, distressMessage)
                setPackage("com.whatsapp")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(sendIntent)
        } catch (e: Exception) {
            try {
                val sendIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TEXT, distressMessage)
                    setPackage("com.whatsapp.w4b")
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(sendIntent)
            } catch (e2: Exception) {
                try {
                    val sendIntent = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_TEXT, distressMessage)
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    context.startActivity(Intent.createChooser(sendIntent, "Send Emergency Alert").apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    })
                } catch (e3: Exception) {
                    Toast.makeText(context, "Could not open messaging app", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
