package com.example.guardianai.ui.screens

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.net.Uri
import android.provider.ContactsContract
import android.telephony.SmsManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import com.example.guardianai.pipeline.EmergencyLocationProvider
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.guardianai.pipeline.WhatsAppSosDispatcher
import com.example.guardianai.services.EmergencyContactManager
import com.example.guardianai.services.TrustedContact
import com.example.guardianai.theme.*
import com.example.guardianai.ui.components.pressClickable
import kotlinx.coroutines.delay

@Composable
fun EmergencyCenterScreen() {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val contacts by EmergencyContactManager.contacts.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }
    var newName by remember { mutableStateOf("") }
    var newPhone by remember { mutableStateOf("") }
    var newRel by remember { mutableStateOf("Trusted Node") }
    var sosStatusMessage by remember { mutableStateOf("READY TO DISPATCH GPS & SMS ALERT") }
    var liveGpsSummary by remember { mutableStateOf("📍 Checking High-Accuracy GPS...") }
    
    // Task #1 & #3: 6-Second Cancellable SOS Countdown State
    var isCountingDown by remember { mutableStateOf(false) }
    var countdownSeconds by remember { mutableStateOf(6) }

    LaunchedEffect(Unit) {
        EmergencyContactManager.init(context)
        EmergencyLocationProvider.startActiveTracking(context)
        while (true) {
            val bestLoc = EmergencyLocationProvider.latestLocation
            if (bestLoc != null) {
                liveGpsSummary = "📍 Live GPS: Locked (${String.format("%.4f", bestLoc.latitude)}°, ${String.format("%.4f", bestLoc.longitude)}°)"
            } else {
                val hasFine = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                val hasCoarse = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
                if (hasFine || hasCoarse) {
                    liveGpsSummary = "📍 GPS Active: Ready to Acquire High-Accuracy Lock on SOS"
                } else {
                    liveGpsSummary = "⚠️ Location Permission Required for GPS Dispatch"
                }
            }
            kotlinx.coroutines.delay(1000L)
        }
    }

    LaunchedEffect(isCountingDown) {
        if (isCountingDown) {
            countdownSeconds = 6
            while (countdownSeconds > 0 && isCountingDown) {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                delay(1000L)
                countdownSeconds--
            }
            if (isCountingDown && countdownSeconds == 0) {
                isCountingDown = false
                // Execute Dual SMS + WhatsApp Dispatch
                triggerAutomatedSosDispatch(context, contacts) { status ->
                    sosStatusMessage = status
                }
            }
        }
    }

    val contactPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickContact()
    ) { contactUri ->
        contactUri?.let { uri ->
            val cursor = context.contentResolver.query(uri, null, null, null, null)
            cursor?.use {
                if (it.moveToFirst()) {
                    val nameIndex = it.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME)
                    val idIndex = it.getColumnIndex(ContactsContract.Contacts._ID)
                    val name = if (nameIndex >= 0) it.getString(nameIndex) ?: "Contact" else "Contact"
                    val id = if (idIndex >= 0) it.getString(idIndex) else ""

                    val phoneCursor = context.contentResolver.query(
                        ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                        null,
                        "${ContactsContract.CommonDataKinds.Phone.CONTACT_ID} = ?",
                        arrayOf(id),
                        null
                    )
                    phoneCursor?.use { pc ->
                        if (pc.moveToFirst()) {
                            val phoneIndex = pc.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
                            val phone = if (phoneIndex >= 0) pc.getString(phoneIndex) ?: "" else ""
                            if (phone.isNotEmpty()) {
                                EmergencyContactManager.addContact(context, name, phone, "Imported Node")
                                Toast.makeText(context, "Added $name ($phone)", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }
            }
        }
    }

    val contactPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            contactPickerLauncher.launch(null)
        } else {
            Toast.makeText(context, "Contact permission needed to import from phonebook", Toast.LENGTH_SHORT).show()
        }
    }

    val sosPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val smsGranted = permissions[Manifest.permission.SEND_SMS] == true
        if (smsGranted) {
            isCountingDown = true
            sosStatusMessage = "ARMED: 6-SECOND COUNTDOWN INITIATED..."
        } else {
            Toast.makeText(context, "SMS permission required for autonomous dispatch", Toast.LENGTH_LONG).show()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppBackground) // #FBF8F3 Warm Ivory/Cream foundation
            .padding(horizontal = 24.dp, vertical = 18.dp)
    ) {
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(20.dp),
            contentPadding = PaddingValues(bottom = 88.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            // 1. Centered Hero Section (input_file_3.png)
            item {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(top = 10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Live GPS & Telemetry Sensor Status Card
                    Surface(
                        color = CardSand,
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, BorderSand),
                        modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
                    ) {
                        Column(modifier = Modifier.padding(14.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            if (!com.example.guardianai.pipeline.EmergencyLocationProvider.isSystemLocationEnabled(context)) {
                                Surface(
                                    color = StatusAmber.copy(alpha = 0.2f),
                                    shape = RoundedCornerShape(10.dp),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, StatusAmber),
                                    modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp).pressClickable {
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                        com.example.guardianai.pipeline.EmergencyLocationProvider.promptTurnOnSystemLocation(context)
                                    }
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(10.dp)) {
                                        Icon(Icons.Default.LocationOn, contentDescription = null, tint = StatusAmber, modifier = Modifier.size(20.dp))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Column {
                                            Text("⚠️ Device GPS is Turned OFF in Settings!", color = StatusAmber, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                                            Text("Tap here to 1-tap turn on Device Location via Google Play Services so real GPS coordinates can be acquired.", color = TextPrimary, style = MaterialTheme.typography.bodySmall, fontSize = 11.sp)
                                        }
                                    }
                                }
                            }
                            Text(
                                text = liveGpsSummary,
                                color = if (liveGpsSummary.contains("Locked")) StatusEmerald else GuardianSienna,
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center
                            )
                            if (sosStatusMessage != "READY TO DISPATCH GPS & SMS ALERT") {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = sosStatusMessage,
                                    color = TextSubtitle,
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.SemiBold,
                                    textAlign = TextAlign.Center
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Surface(
                                color = CardSandElevated,
                                shape = RoundedCornerShape(100.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, BorderSand),
                                modifier = Modifier.pressClickable {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    if (contacts.isEmpty()) {
                                        Toast.makeText(context, "Please add at least 1 contact first!", Toast.LENGTH_SHORT).show()
                                    } else {
                                        sosStatusMessage = "TEST DISPATCH: LAUNCHING MULTI-CONTACT SMS + WHATSAPP..."
                                        triggerAutomatedSosDispatch(context, contacts) { status ->
                                            sosStatusMessage = status
                                        }
                                    }
                                }
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)) {
                                    Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null, tint = GuardianSienna, modifier = Modifier.size(13.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Simulate Instant Dispatch (Test Location & WhatsApp)", color = GuardianSienna, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                                }
                            }

                            if (sosStatusMessage.isNotEmpty() && contacts.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(12.dp))
                                Surface(
                                    color = StatusEmerald.copy(alpha = 0.15f),
                                    shape = RoundedCornerShape(12.dp),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, StatusEmerald.copy(alpha = 0.4f)),
                                    modifier = Modifier.fillMaxWidth().padding(top = 4.dp)
                                ) {
                                    Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text("🟢 WhatsApp Multi-Node Quick Dispatch", color = StatusEmerald, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text("If Android prevented automatic switching between WhatsApp chats, tap any contact below to open their chat instantly:", color = TextSubtitle, style = MaterialTheme.typography.bodySmall, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                                            contacts.forEachIndexed { idx, c ->
                                                Surface(
                                                    color = StatusEmerald,
                                                    shape = RoundedCornerShape(100.dp),
                                                    modifier = Modifier
                                                        .padding(horizontal = 4.dp, vertical = 4.dp)
                                                        .pressClickable {
                                                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                                            com.example.guardianai.pipeline.EmergencyLocationProvider.getExactLocationAsync(context) { link, _ ->
                                                                val msg = if (link == "UNAVAILABLE") {
                                                                    "🚨 URGENT SOS ALERT from Guardian AI! I am in acute distress and require immediate emergency assistance! Location is currently acquiring. Please try calling me!"
                                                                } else {
                                                                    "🚨 URGENT SOS ALERT from Guardian AI! I am in acute distress and require immediate emergency assistance! Live GPS Coordinates: $link"
                                                                }
                                                                com.example.guardianai.pipeline.WhatsAppSosDispatcher.dispatchToWhatsAppNode(context, c, msg)
                                                            }
                                                        }
                                                ) {
                                                    Text("💬 ${c.name}", color = androidx.compose.ui.graphics.Color.White, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp))
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Circular Hero SOS Button inside rich sienna
                    Surface(
                        color = if (isCountingDown) StatusAmber else GuardianSienna, // #8B3B26
                        shape = CircleShape,
                        shadowElevation = 4.dp,
                        modifier = Modifier
                            .size(190.dp)
                            .pressClickable {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                if (isCountingDown) {
                                    isCountingDown = false
                                    sosStatusMessage = "CANCELLED BY USER: SAFETY OVERRIDE ACTIVE"
                                    Toast.makeText(context, "🛑 SOS Dispatch Cancelled — You are marked safe.", Toast.LENGTH_SHORT).show()
                                } else {
                                    if (contacts.isEmpty()) {
                                        Toast.makeText(context, "Add or import at least one trusted contact first!", Toast.LENGTH_LONG).show()
                                        return@pressClickable
                                    }
                                    val hasSms = ContextCompat.checkSelfPermission(context, Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED
                                    val hasFine = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                                    val hasCoarse = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
                                    if (hasSms && (hasFine || hasCoarse)) {
                                        isCountingDown = true
                                        sosStatusMessage = "ARMED: 6-SECOND COUNTDOWN INITIATED..."
                                    } else {
                                        sosPermissionLauncher.launch(
                                            arrayOf(
                                                Manifest.permission.SEND_SMS,
                                                Manifest.permission.ACCESS_FINE_LOCATION,
                                                Manifest.permission.ACCESS_COARSE_LOCATION
                                            )
                                        )
                                    }
                                }
                            }
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            Icon(
                                imageVector = if (isCountingDown) Icons.Default.Warning else Icons.Default.Emergency,
                                contentDescription = "SOS Emblem",
                                tint = Color.White,
                                modifier = Modifier.size(46.dp)
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = if (isCountingDown) "DISPATCH (${countdownSeconds}s)" else "Activate SOS",
                                color = Color.White,
                                fontFamily = FontFamily.Serif,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = if (isCountingDown) "Tap to Cancel" else "Hold or Tap",
                                color = Color.White.copy(alpha = 0.85f),
                                style = MaterialTheme.typography.labelMedium,
                                fontSize = 13.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))
                    Text(
                        text = "Triggering SOS will silently alert your trusted contacts and local authorities with your location and microphone feed.",
                        color = TextSubtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        lineHeight = 20.sp,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }
            }

            // 2. Trusted Contacts Container (input_file_3.png)
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = CardSand), // #F0ECE5 Sand Card
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, BorderSand)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Trusted Contacts",
                                color = TextPrimary,
                                fontFamily = FontFamily.Serif,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Surface(
                                    color = CardSandElevated,
                                    shape = RoundedCornerShape(100.dp),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, BorderSand),
                                    modifier = Modifier.pressClickable {
                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                        if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
                                            contactPickerLauncher.launch(null)
                                        } else {
                                            contactPermissionLauncher.launch(Manifest.permission.READ_CONTACTS)
                                        }
                                    }
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)) {
                                        Icon(Icons.Default.Contacts, contentDescription = null, tint = GuardianSienna, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Import", color = GuardianSienna, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                                    }
                                }

                                Surface(
                                    color = GuardianSienna,
                                    shape = RoundedCornerShape(100.dp),
                                    modifier = Modifier.pressClickable {
                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                        showAddDialog = true
                                    }
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)) {
                                        Icon(Icons.Default.Add, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Add", color = Color.White, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        if (contacts.isEmpty()) {
                            Surface(
                                color = CardSandElevated,
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text("No Trusted Contacts Added", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text("Add or import nodes above so your distress message, GPS link, and audio recording reach them instantly.", color = TextSubtitle, fontSize = 13.sp)
                                }
                            }
                        } else {
                            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                contacts.forEach { contact ->
                                    Surface(
                                        color = AppBackground, // #FBF8F3 inside Sand Container
                                        shape = RoundedCornerShape(8.dp),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, BorderSand)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(14.dp).fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Surface(
                                                    shape = CircleShape,
                                                    color = CardSandElevated,
                                                    modifier = Modifier.size(38.dp)
                                                ) {
                                                    Box(contentAlignment = Alignment.Center) {
                                                        Icon(Icons.Default.Person, contentDescription = null, tint = GuardianSienna, modifier = Modifier.size(20.dp))
                                                    }
                                                }
                                                Spacer(modifier = Modifier.width(12.dp))
                                                Column {
                                                    Text(
                                                        text = "${contact.name} · ${contact.relationship.replace("Node", "").trim().ifEmpty { "Primary" }}",
                                                        color = TextPrimary,
                                                        style = MaterialTheme.typography.titleSmall,
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 15.sp
                                                    )
                                                    Spacer(modifier = Modifier.height(2.dp))
                                                    Text(contact.phone, color = TextSubtitle, style = MaterialTheme.typography.bodySmall, fontSize = 13.sp)
                                                }
                                            }

                                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                                // Green status dot (input_file_3.png)
                                                Box(modifier = Modifier.size(8.dp).background(StatusEmerald, CircleShape))
                                                
                                                IconButton(
                                                    onClick = {
                                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                                        EmergencyContactManager.removeContact(context, contact.id)
                                                    },
                                                    modifier = Modifier.size(28.dp)
                                                ) {
                                                    Icon(Icons.Default.Close, contentDescription = "Remove", tint = TextMuted, modifier = Modifier.size(16.dp))
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 3. Emergency Helplines Section
            item {
                Text(
                    text = "Emergency Helplines",
                    color = TextPrimary,
                    fontFamily = FontFamily.Serif,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            item {
                HelplineCard(
                    title = "National Emergency Dispatch",
                    number = "112",
                    description = "Universal Indian emergency dispatch across police, ambulance, and fire services.",
                    accentColor = GuardianCrimson
                )
            }
            item {
                HelplineCard(
                    title = "National Women's Safety Helpline",
                    number = "1091",
                    description = "24/7 immediate crisis assistance for harassment, stalking, or physical distress.",
                    accentColor = StatusAmber
                )
            }
            item {
                HelplineCard(
                    title = "National Cyber Crime Helpline",
                    number = "1930",
                    description = "Report financial fraud, online extortion, and unauthorized digital access.",
                    accentColor = GuardianSienna
                )
            }

            // 4. Footer Disclaimer (input_file_3.png)
            item {
                Text(
                    text = "Activating the SOS protocol constitutes an emergency declaration and will transmit sensitive real-time telemetry to your authorized contacts.",
                    color = TextMuted,
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center,
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                    lineHeight = 18.sp,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                )
            }
        }
    }

    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            containerColor = CardSand,
            title = { Text("Add Trusted Contact", color = TextPrimary, fontFamily = FontFamily.Serif, fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    OutlinedTextField(
                        value = newName,
                        onValueChange = { newName = it },
                        label = { Text("Contact Name", color = TextSubtitle) },
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary, focusedBorderColor = GuardianSienna, unfocusedBorderColor = BorderSand),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = newPhone,
                        onValueChange = { newPhone = it },
                        label = { Text("Phone Number (+91...)", color = TextSubtitle) },
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary, focusedBorderColor = GuardianSienna, unfocusedBorderColor = BorderSand),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = newRel,
                        onValueChange = { newRel = it },
                        label = { Text("Role (e.g. Primary / Secondary)", color = TextSubtitle) },
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary, focusedBorderColor = GuardianSienna, unfocusedBorderColor = BorderSand),
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Surface(
                    color = GuardianSienna,
                    shape = RoundedCornerShape(100.dp),
                    modifier = Modifier.pressClickable {
                        if (newName.isNotBlank() && newPhone.isNotBlank()) {
                            EmergencyContactManager.addContact(context, newName, newPhone, newRel)
                            newName = ""
                            newPhone = ""
                            showAddDialog = false
                        }
                    }
                ) {
                    Text("Add Node", color = Color.White, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp))
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) {
                    Text("Cancel", color = TextSubtitle, style = MaterialTheme.typography.labelMedium)
                }
            }
        )
    }
}

@Composable
fun HelplineCard(title: String, number: String, description: String, accentColor: Color) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .pressClickable {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                val dialIntent = Intent(Intent.ACTION_DIAL).apply {
                    data = Uri.parse("tel:$number")
                }
                context.startActivity(dialIntent)
            },
        colors = CardDefaults.cardColors(containerColor = CardSand), // #F0ECE5 Sand Card
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderSand)
    ) {
        Row(
            modifier = Modifier.padding(18.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                Surface(
                    shape = CircleShape,
                    color = accentColor.copy(alpha = 0.12f),
                    modifier = Modifier.size(44.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(number, color = accentColor, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                    }
                }
                Spacer(modifier = Modifier.width(14.dp))
                Column {
                    Text(title, color = TextPrimary, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(description, color = TextSubtitle, style = MaterialTheme.typography.bodySmall, lineHeight = 17.sp, fontSize = 13.sp)
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
            Surface(
                shape = CircleShape,
                color = accentColor,
                modifier = Modifier.size(38.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.Phone, contentDescription = "Dial", tint = Color.White, modifier = Modifier.size(18.dp))
                }
            }
        }
    }
}

private fun triggerAutomatedSosDispatch(context: Context, contacts: List<TrustedContact>, onStatus: (String) -> Unit) {
    if (contacts.isEmpty()) {
        onStatus("FAILED: NO VERIFIED NODES SAVED")
        return
    }

    onStatus("ACQUIRING EXACT SATELLITE & WI-FI GPS LOCK...")
    Toast.makeText(context, "📡 Acquiring exact device location...", Toast.LENGTH_SHORT).show()

    EmergencyLocationProvider.getExactLocationAsync(context) { locationLink, _ ->
        val message = if (locationLink == "UNAVAILABLE") {
            "🚨 URGENT SOS ALERT from Guardian AI! I am in acute distress and require immediate emergency assistance! Location is currently acquiring. Please try calling me!"
        } else {
            "🚨 URGENT SOS ALERT from Guardian AI! I am in acute distress and require immediate emergency assistance! Live GPS Coordinates: $locationLink"
        }
        val smsManager = try {
            context.getSystemService(SmsManager::class.java)
        } catch (e: Exception) {
            @Suppress("DEPRECATION")
            SmsManager.getDefault()
        }

        var successCount = 0
        contacts.forEachIndexed { index, contact ->
            try {
                val parts = smsManager.divideMessage(message)
                if (parts.size > 1) {
                    smsManager.sendMultipartTextMessage(contact.phone, null, parts, null, null)
                } else {
                    smsManager.sendTextMessage(contact.phone, null, message, null, null)
                }
                successCount++
            } catch (e: Exception) {
                try {
                    val smsIntent = Intent(Intent.ACTION_SENDTO).apply {
                        data = Uri.parse("smsto:${contact.phone}")
                        putExtra("sms_body", message)
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    context.startActivity(smsIntent)
                    successCount++
                } catch (e2: Exception) {}
            }
        }

        WhatsAppSosDispatcher.dispatchToAllContactsSequentially(context, contacts, message)

        if (successCount > 0) {
            val locStatus = if (locationLink == "UNAVAILABLE") "PENDING" else locationLink
            onStatus("DISPATCHED EXACT LOCATION ($locStatus) TO ${contacts.size} NODES")
            Toast.makeText(context, "✅ SOS Dispatched with location status: $locStatus", Toast.LENGTH_LONG).show()
        } else {
            WhatsAppSosDispatcher.broadcastViaWhatsApp(context, message)
            val locStatus = if (locationLink == "UNAVAILABLE") "PENDING" else locationLink
            onStatus("DISPATCHED VIA WHATSAPP FALLBACK ($locStatus)")
            Toast.makeText(context, "⚠️ Opened WhatsApp with location status: $locStatus", Toast.LENGTH_LONG).show()
        }

        // Register high-precision follow up if an even higher accuracy lock is acquired moments later
        EmergencyLocationProvider.requestHighPrecisionFollowUp(context) { loc ->
            val liveLink = "https://maps.google.com/?q=${loc.latitude},${loc.longitude}"
            val followUpMsg = "🚨 GUARDIAN AI LIVE GPS UPDATE: Exact high-accuracy coordinates acquired -> $liveLink"
            val smsMgr = try { context.getSystemService(SmsManager::class.java) } catch (e: Exception) { @Suppress("DEPRECATION") SmsManager.getDefault() }
            contacts.forEach { c -> try { smsMgr.sendTextMessage(c.phone, null, followUpMsg, null, null) } catch (ex: Exception) {} }
            WhatsAppSosDispatcher.dispatchToAllContactsSequentially(context, contacts, followUpMsg)
        }
    }
}
