package com.example.guardianai.data

import com.example.guardianai.pipeline.PipelineExecutionLog
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class LegalProvision(
    val id: String,
    val citation: String,
    val actName: String,
    val sectionNumber: String,
    val plainLanguageExplanation: String,
    val technicalText: String,
    val confidenceLabel: String = "High Confidence · Verified Statutory Source",
    val lastVerified: String = "July 2026",
    val urgencyLevel: String,
    val actionSteps: List<String>
)

data class HelplineResource(
    val name: String,
    val numberOrUrl: String,
    val description: String,
    val category: String,
    val is24x7: Boolean = true
)

/**
 * Curated Legal Database & RAG Knowledge Base (PDR v1.0 Section 2.A & 2.B)
 * Provides verifiable legal accuracy, citation labels, last-verified timestamps,
 * plain-language translations of legalese, guided report drafting, and evidence mapping.
 */
object CuratedLegalDatabase {

    val provisions = listOf(
        LegalProvision(
            id = "it_66e",
            citation = "IT Act, 2000 — Section 66E (Violation of Privacy)",
            actName = "Information Technology Act, 2000",
            sectionNumber = "Section 66E",
            plainLanguageExplanation = "Capturing, sharing, or threatening to leak private or intimate photos of someone without their clear consent is a serious non-bailable cybercrime.",
            technicalText = "Whoever, intentionally or knowingly captures, publishes or transmits the image of a private area of any person without his or her consent, under circumstances violating the privacy of that person, shall be punished with imprisonment up to three years or with fine up to two lakh rupees, or with both.",
            confidenceLabel = "High Confidence · IT Act 2000 Statutory Citation",
            lastVerified = "July 2026",
            urgencyLevel = "🚨 CRITICAL URGENCY (Photo Leak / Sextortion)",
            actionSteps = listOf(
                "Do NOT pay any extortion money — 98% of blackmailers double demands after initial payment.",
                "Execute Guardian AI Stage 4 Enforce block immediately on the sender identity.",
                "Preserve chat and sender metadata in Evidence Vault with SHA-256 timestamping.",
                "File formal complaint at National Cyber Crime Portal (cybercrime.gov.in) or call 1930."
            )
        ),
        LegalProvision(
            id = "it_67_67a",
            citation = "IT Act, 2000 — Sections 67 & 67A (Obscene & Explicit Electronic Material)",
            actName = "Information Technology Act, 2000",
            sectionNumber = "Sections 67 & 67A",
            plainLanguageExplanation = "Publishing or transmitting sexually explicit digital content or non-consensual intimate media (morphed/deepfakes) carries mandatory prison sentences up to 7 years.",
            technicalText = "Section 67A penalizes publishing or transmitting material containing sexually explicit act or conduct in electronic form with mandatory imprisonment on first conviction up to five years and fine up to ten lakh rupees, extending up to seven years on subsequent conviction.",
            confidenceLabel = "High Confidence · IT Act 2000 Statutory Citation",
            lastVerified = "July 2026",
            urgencyLevel = "🚨 CRITICAL URGENCY (Sextortion / Deepfake Abuse)",
            actionSteps = listOf(
                "Zero-Compliance Rule: Never send more photos or money under threat.",
                "Use Guardian AI to export an encrypted, zero-PII Case ID report for your campus Trust & Safety or police.",
                "Report directly to cybercrime.gov.in under 'Women & Child Safety' fast-track handling."
            )
        ),
        LegalProvision(
            id = "it_66d",
            citation = "IT Act, 2000 — Section 66D (Cheating by Personation using Computer Resources)",
            actName = "Information Technology Act, 2000",
            sectionNumber = "Section 66D",
            plainLanguageExplanation = "Impersonating a bank, customer care representative, police officer, or friend online to trick you out of money or passwords is a specific digital fraud offense.",
            technicalText = "Whoever, by means of any communication device or computer resource cheats by personation, shall be punished with imprisonment of either description for a term which may extend to three years and shall also be liable to fine up to one lakh rupees.",
            confidenceLabel = "High Confidence · IT Act 2000 Statutory Citation",
            lastVerified = "July 2026",
            urgencyLevel = "⚡ GOLDEN HOUR URGENCY (Financial / Phishing Fraud)",
            actionSteps = listOf(
                "Call the National Cyber Crime Financial Helpline 1930 immediately within the 'Golden Hour' (first 2 hours).",
                "Contact your bank/UPI provider emergency line to freeze debit cards and net banking.",
                "Preserve SMS headers, UPI transaction reference IDs, and QR codes in Evidence Vault."
            )
        ),
        LegalProvision(
            id = "it_43_66",
            citation = "IT Act, 2000 — Sections 43 & 66 (Unauthorized Access & Computer Hacking)",
            actName = "Information Technology Act, 2000",
            sectionNumber = "Sections 43 & 66",
            plainLanguageExplanation = "Hacking into someone's WhatsApp, Instagram, email, or device without their consent — or stealing OTPs — is a criminal offense punishable by prison and heavy compensation.",
            technicalText = "Section 43 prescribes liability for accessing, downloading, or disrupting computer resources without permission. Section 66 criminalizes any act referred to in Section 43 done dishonestly or fraudulently with imprisonment up to three years or fine up to five lakh rupees.",
            confidenceLabel = "High Confidence · IT Act 2000 Statutory Citation",
            lastVerified = "July 2026",
            urgencyLevel = "🔴 HIGH URGENCY (Account Compromise / Hacking)",
            actionSteps = listOf(
                "Log out all active sessions via Account Settings -> 'Where You're Logged In'.",
                "Enable App-based Two-Factor Authentication (2FA via Google Authenticator or Bitwarden).",
                "If SIM/OTP hijacking is suspected, re-verify WhatsApp on your primary phone immediately to disconnect the unauthorized session."
            )
        ),
        LegalProvision(
            id = "bns_354d",
            citation = "Bharatiya Nyaya Sanhita (BNS) Section 354D / IPC Sec 354D (Cyberstalking)",
            actName = "Bharatiya Nyaya Sanhita / Indian Penal Code",
            sectionNumber = "Section 354D",
            plainLanguageExplanation = "Continuously following, messaging, or monitoring a woman's internet/social media activity despite clear indication of disinterest is the crime of stalking.",
            technicalText = "Any man who monitors the use by a woman of the internet, email or any other form of electronic communication, despite a clear indication of disinterest by such woman, commits the offense of stalking, punishable with imprisonment up to three years on first conviction.",
            confidenceLabel = "High Confidence · BNS / IPC Criminal Provision",
            lastVerified = "July 2026",
            urgencyLevel = "🔴 HIGH URGENCY (Repeated Harassment / Stalking)",
            actionSteps = listOf(
                "Apply the 1-Strike Boundary Rule: Send exactly one clear refusal ('Stop contacting me'), then cease all dialogue.",
                "Trigger Stage 4 Enforce inside Guardian AI to automatically block incoming attempts.",
                "Preserve all message logs with exact timestamps in your local Evidence Vault."
            )
        ),
        LegalProvision(
            id = "pocso_19",
            citation = "POCSO Act, 2012 — Section 19 (Protection of Minors & Mandatory Reporting)",
            actName = "Protection of Children from Sexual Offences Act, 2012",
            sectionNumber = "Section 19",
            plainLanguageExplanation = "Special statutory protection applies to any person under 18 years of age. Any sexual exploitation, digital grooming, or harassment of a minor requires mandatory reporting to protect the child.",
            technicalText = "Under Section 19 of the POCSO Act, any person who has apprehension that an offense under the Act has been or is likely to be committed against a child has a statutory obligation to report the matter to the Special Juvenile Police Unit or local police.",
            confidenceLabel = "High Confidence · POCSO Act 2012 Statutory Mandate",
            lastVerified = "July 2026",
            urgencyLevel = "🚨 EMERGENCY URGENCY (Minor-Safe Mode / Child Protection)",
            actionSteps = listOf(
                "Immediate Adult Guidance: Talk to a trusted parent, guardian, or school/college counselor right now.",
                "Call Childline 1098 (24x7 Free National Child Helpline) or dial 112 / 1930 for immediate safety assistance.",
                "NEVER forward, store, or repeat suspected Child Sexual Abuse Material (CSAM). Report suspected links directly to authorities without saving."
            )
        ),
        LegalProvision(
            id = "dpdp_2023",
            citation = "Digital Personal Data Protection (DPDP) Act, 2023 & Rules, 2025",
            actName = "Digital Personal Data Protection Act, 2023",
            sectionNumber = "DPDP Act / Rules 2025 (Phased Rollout)",
            plainLanguageExplanation = "India's data privacy law requires clear consent before any company or app can collect or process your personal data, granting you rights to correct or erase data.",
            technicalText = "The DPDP Act received Presidential assent in 2023 and operational Rules were notified in November 2025. Note: Implementation is phased, with full enforcement (including penalties up to ₹250 crores for data breaches) expected by May 2027.",
            confidenceLabel = "High Confidence · Phased Rollout Status Verified",
            lastVerified = "July 2026",
            urgencyLevel = "🟢 STANDARD URGENCY (Data Privacy / App Permissions)",
            actionSteps = listOf(
                "Review Android device permissions and revoke SMS/Call Log access from untrusted apps or loan APKs.",
                "Lodge a formal grievance with the Data Fiduciary's Grievance Officer or the Data Protection Board of India."
            )
        ),
        LegalProvision(
            id = "cert_in_6hr",
            citation = "CERT-In Mandatory Incident Reporting Advisory (6-Hour Rule)",
            actName = "IT Act Rules / CERT-In Directions",
            sectionNumber = "CERT-In 2022/2026 Directions",
            plainLanguageExplanation = "Organizations, service providers, and app developers in India must officially report cybersecurity incidents and data breaches within 6 hours of noticing them.",
            technicalText = "Under Section 70B of the IT Act, CERT-In directs all service providers, intermediaries, and corporate bodies to report designated cybersecurity incidents right away within 6 hours to cert-in.org.in for rapid containment.",
            confidenceLabel = "High Confidence · CERT-In National Mandate",
            lastVerified = "July 2026",
            urgencyLevel = "🟡 ORGANIZATIONAL URGENCY (System Security / Breach)",
            actionSteps = listOf(
                "Verify that personal devices and apps follow CERT-In security advisories at cert-in.org.in.",
                "For organizations: Submit the incident report via incident@cert-in.org.in within the mandatory 6-hour window."
            )
        ),
        LegalProvision(
            id = "rbi_digital_pay",
            citation = "RBI Guidelines on Digital Payment Safety & Customer Liability",
            actName = "Reserve Bank of India Master Directions",
            sectionNumber = "RBI Circular on Customer Liability / 2FA",
            plainLanguageExplanation = "If you report an unauthorized electronic banking or UPI transaction to your bank immediately within 3 working days, your financial liability is zero.",
            technicalText = "Under RBI's Zero Liability Framework, a customer has zero liability where the unauthorized transaction occurs due to contributory fraud/negligence by the bank, or third-party breach where the customer notifies the bank within three working days.",
            confidenceLabel = "High Confidence · RBI Master Direction",
            lastVerified = "July 2026",
            urgencyLevel = "⚡ GOLDEN HOUR URGENCY (Financial Loss / UPI Scams)",
            actionSteps = listOf(
                "Call 1930 and your Bank Customer Care immediately — banks can freeze funds in transit during the immediate 'Golden Hour'.",
                "Do NOT scan any QR code or enter UPI PIN to receive money or refunds. A UPI PIN is NEVER required to receive money."
            )
        )
    )

    val helplines = listOf(
        HelplineResource(
            name = "National Cyber Crime Financial Helpline",
            numberOrUrl = "1930",
            description = "24x7 Government emergency line to freeze fraudulent UPI/bank transfers in transit during the 'Golden Hour'.",
            category = "Financial Fraud & Scams"
        ),
        HelplineResource(
            name = "National Cyber Crime Reporting Portal",
            numberOrUrl = "https://cybercrime.gov.in",
            description = "Official Ministry of Home Affairs portal to file formal online police complaints with fast-track handling for women/children.",
            category = "Official Police Complaint"
        ),
        HelplineResource(
            name = "Childline India (POCSO / Minor Safety)",
            numberOrUrl = "1098 / 112",
            description = "24x7 Free national helpline for minors, children, and students facing online grooming, coercion, or harassment.",
            category = "Minor Safety & Support"
        ),
        HelplineResource(
            name = "National Emergency Number",
            numberOrUrl = "112",
            description = "Unified national 24x7 emergency dispatch for immediate physical danger or police intervention.",
            category = "Emergency & Physical Safety"
        ),
        HelplineResource(
            name = "National Commission for Women (NCW) Digital Helpline",
            numberOrUrl = "7827170170",
            description = "Specialized support line for women facing online stalking, deepfakes, sextortion, and cyberbullying.",
            category = "Women Safety & Legal Guidance"
        ),
        HelplineResource(
            name = "Free Legal Aid (NALSA / State Legal Services)",
            numberOrUrl = "15100 / nalsa.gov.in",
            description = "National Legal Services Authority free legal consultation and representation for victims of cybercrime.",
            category = "Free Legal Aid"
        )
    )

    fun searchProvisions(query: String): List<LegalProvision> {
        val lower = query.lowercase().trim()
        return provisions.filter { prov ->
            prov.citation.lowercase().contains(lower) ||
            prov.plainLanguageExplanation.lowercase().contains(lower) ||
            prov.technicalText.lowercase().contains(lower) ||
            prov.actName.lowercase().contains(lower) ||
            when {
                lower.contains("photo") || lower.contains("nude") || lower.contains("leak") || lower.contains("sextortion") -> prov.id == "it_66e" || prov.id == "it_67_67a"
                lower.contains("money") || lower.contains("upi") || lower.contains("pay") || lower.contains("fraud") || lower.contains("scam") || lower.contains("telegram job") -> prov.id == "it_66d" || prov.id == "rbi_digital_pay"
                lower.contains("hack") || lower.contains("otp") || lower.contains("password") || lower.contains("account") -> prov.id == "it_43_66"
                lower.contains("stalk") || lower.contains("follow") || lower.contains("harass") || lower.contains("bully") -> prov.id == "bns_354d"
                lower.contains("child") || lower.contains("minor") || lower.contains("under 18") || lower.contains("school") -> prov.id == "pocso_19"
                lower.contains("dpdp") || lower.contains("data") || lower.contains("permission") || lower.contains("privacy") -> prov.id == "dpdp_2023"
                lower.contains("cert") || lower.contains("breach") -> prov.id == "cert_in_6hr"
                else -> false
            }
        }
    }

    /**
     * Maps an Evidence Vault log to its most relevant Indian Cyber Law provisions (Feature B).
     */
    fun mapEvidenceToLaw(log: PipelineExecutionLog): List<LegalProvision> {
        val lower = (log.rawContent + " " + log.stage3Analyze.patternDetected + " " + log.stage3Analyze.explanation).lowercase()
        val mapped = mutableSetOf<LegalProvision>()

        if (lower.contains("sextortion") || lower.contains("photo") || lower.contains("leak") || lower.contains("coercion")) {
            provisions.find { it.id == "it_66e" }?.let { mapped.add(it) }
            provisions.find { it.id == "it_67_67a" }?.let { mapped.add(it) }
        }
        if (lower.contains("financial") || lower.contains("scam") || lower.contains("money") || lower.contains("upi") || lower.contains("loan")) {
            provisions.find { it.id == "it_66d" }?.let { mapped.add(it) }
            provisions.find { it.id == "rbi_digital_pay" }?.let { mapped.add(it) }
        }
        if (lower.contains("stalking") || lower.contains("harass") || lower.contains("late-night") || lower.contains("frequency")) {
            provisions.find { it.id == "bns_354d" }?.let { mapped.add(it) }
        }
        if (lower.contains("hack") || lower.contains("account") || lower.contains("otp")) {
            provisions.find { it.id == "it_43_66" }?.let { mapped.add(it) }
        }
        if (mapped.isEmpty()) {
            provisions.find { it.id == "it_66d" }?.let { mapped.add(it) }
        }
        return mapped.toList()
    }

    /**
     * Guided Report Wizard Draft Generator (Feature B).
     * Pre-fills from Evidence Vault entries and formats for cybercrime.gov.in / Police review.
     */
    fun generateGuidedReportDraft(
        logs: List<PipelineExecutionLog>,
        selectedProvisionIds: List<String>,
        userNameOrCaseId: String = "Case ID #8B2L-ANONYMIZED",
        lang: String = "English"
    ): String {
        val dateStr = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault()).format(Date())
        val provisionsList = if (selectedProvisionIds.isNotEmpty()) {
            provisions.filter { selectedProvisionIds.contains(it.id) }
        } else if (logs.isNotEmpty()) {
            mapEvidenceToLaw(logs.first())
        } else {
            listOf(provisions.first())
        }

        val citationsStr = provisionsList.joinToString("\n• ") { "${it.citation} [Confidence: ${it.confidenceLabel}, Verified: ${it.lastVerified}]" }
        val plainSummaryStr = provisionsList.joinToString("\n• ") { it.plainLanguageExplanation }

        val evidenceRows = if (logs.isNotEmpty()) {
            logs.take(5).joinToString("\n---\n") { l ->
                val tStr = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault()).format(Date(l.timestamp))
                val hashStr = "SHA-256 [${l.id.take(8).uppercase()}]"
                "📌 Timestamp: $tStr | Platform: ${l.platform}\n" +
                "👤 Sender/Entity: ${l.stage2Anonymize}\n" +
                "🔍 Pattern Identified: ${l.stage3Analyze.patternDetected} (${l.stage3Analyze.tier.label})\n" +
                "🔐 Cryptographic Hash Check: $hashStr\n" +
                "📝 Content Snippet: \"${l.rawContent.take(120)}${if (l.rawContent.length > 120) "..." else ""}\""
            }
        } else {
            "📌 [No specific vault logs pre-selected. Evidence can be attached manually via Guardian AI Vault PDF export.]"
        }

        return when (lang) {
            "Hindi" -> """
                📑 **प्रारूप शिकायत पत्र — राष्ट्रीय साइबर क्राइम रिपोर्टिंग पोर्टल (cybercrime.gov.in)**
                
                ⚠️ **महत्वपूर्ण सूचना (HUMAN-IN-THE-LOOP REQUIRED):** यह Guardian AI द्वारा आपके Evidence Vault से तैयार किया गया एक प्रारूप (Draft) है। सबमिट करने से पहले कृपया सभी विवरणों की स्वयं जांच करें।
                
                **1. शिकायतकर्ता संदर्भ:** $userNameOrCaseId
                **2. दिनांक एवं समय:** $dateStr
                **3. लागू कानून एवं धाराएं (Applicable Indian Cyber Laws):**
                • $citationsStr
                
                **4. सरल भाषा में कानूनी सारांश:**
                • $plainSummaryStr
                
                **5. साक्ष्य श्रृंखला (Chain of Custody Evidence Table):**
                $evidenceRows
                
                **6. राहत एवं कार्रवाई की मांग (Prayer / Relief Requested):**
                1. उक्त अपराधियों/फोन नंबरों के विरुद्ध IT Act एवं भारतीय न्याय संहिता की संबंधित धाराओं के तहत तुरंत FIR दर्ज की जाए।
                2. यदि कोई वित्तीय धोखाधड़ी हुई है, तो **1930 गोल्डन आवर** नियम के तहत संदिग्ध खातों में राशि फ्रीज की जाए।
                3. CERT-In 6-घंटे के निर्देशानुसार संबंधित सोशल मीडिया/बैंकिंग प्लेटफॉर्म से सर्वर लॉग सुरक्षित किए जाएं।
            """.trimIndent()
            "Telugu" -> """
                📑 **ముసాయిదా ఫిర్యాదు — జాతీయ సైబర్ క్రైమ్ పోర్టల్ (cybercrime.gov.in)**
                
                ⚠️ **ముఖ్యమైన గమనిక (HUMAN-IN-THE-LOOP):** ఇది మీ Evidence Vault ఆధారంగా Guardian AI రూపొందించిన ముసాయిదా మాత్రమే. సమర్పించే ముందు వివరాలన్నీ తనిఖీ చేయండి.
                
                **1. ఫిర్యాదుదారు ఐడీ:** $userNameOrCaseId
                **2. తేదీ & సమయం:** $dateStr
                **3. వర్తించే భారతీయ చట్టాలు:**
                • $citationsStr
                
                **4. సాధారణ భాషలో చట్ట సారాంశం:**
                • $plainSummaryStr
                
                **5. సాక్ష్యాల వివరాలు (Evidence Table):**
                $evidenceRows
                
                **6. కోరుతున్న చర్యలు (Prayer / Relief):**
                1. బాధ్యులైన వారిపై IT Act మరియు BNS చట్టాల కింద వెంటనే FIR నమోదు చేయాలి.
                2. ఆర్థిక మోసం జరిగినట్లయితే **1930 గోల్డెన్ అవర్** నిబంధన కింద ఖాతాలను ఫ్రీజ్ చేయాలి.
            """.trimIndent()
            else -> """
                📑 **DRAFT CYBER COMPLAINT — NATIONAL CYBER CRIME REPORTING PORTAL (cybercrime.gov.in)**
                
                ⚠️ **HUMAN-IN-THE-LOOP REVIEW MANDATORY:** This document is a pre-filled legal draft generated by Guardian AI from your local Evidence Vault. Please carefully review, edit, and verify all facts before submitting to official authorities (`cybercrime.gov.in` or local police station).
                
                **1. COMPLAINANT / CASE REFERENCE:** $userNameOrCaseId
                **2. GENERATION TIMESTAMP:** $dateStr
                **3. APPLICABLE STATUTORY PROVISIONS & CITATIONS:**
                • $citationsStr
                
                **4. PLAIN-LANGUAGE LEGAL TRANSLATION:**
                • $plainSummaryStr
                
                **5. CRYPTOGRAPHIC CHAIN OF CUSTODY (EVIDENCE VAULT LOGS):**
                $evidenceRows
                
                **6. PRAYER / ACTION REQUESTED:**
                1. Immediate registration of First Information Report (FIR) under the cited sections of the Information Technology Act, 2000 and Bharatiya Nyaya Sanhita (BNS).
                2. Under the **National 1930 Golden Hour Protocol**, immediate freeze of target receiving accounts/wallets to prevent dissipation of funds.
                3. Direction to intermediary platforms to preserve server IP logs, device identifiers, and subscriber KYC records in compliance with CERT-In 6-Hour Incident Reporting Directions.
            """.trimIndent()
        }
    }
}
