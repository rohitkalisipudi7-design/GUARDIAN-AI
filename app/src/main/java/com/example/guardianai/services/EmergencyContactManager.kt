package com.example.guardianai.services

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.json.JSONArray
import org.json.JSONObject

data class TrustedContact(
    val id: String,
    val name: String,
    val phone: String,
    val relationship: String
)

object EmergencyContactManager {
    private const val PREFS_NAME = "guardian_trusted_contacts_prefs"
    private const val KEY_CONTACTS = "trusted_contacts_json"

    private val _contacts = MutableStateFlow<List<TrustedContact>>(emptyList())
    val contacts: StateFlow<List<TrustedContact>> = _contacts

    fun init(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val jsonString = prefs.getString(KEY_CONTACTS, null)
        if (jsonString != null) {
            try {
                val list = mutableListOf<TrustedContact>()
                val array = JSONArray(jsonString)
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    list.add(
                        TrustedContact(
                            id = obj.getString("id"),
                            name = obj.getString("name"),
                            phone = obj.getString("phone"),
                            relationship = obj.optString("relationship", "Trusted Contact")
                        )
                    )
                }
                _contacts.value = list
                return
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        // Default initial contacts if none exist
        val initial = listOf(
            TrustedContact("c1", "Mom", "+91 98765 43210", "Parent"),
            TrustedContact("c2", "Priya (Roommate)", "+91 91234 56789", "Hostel Roommate"),
            TrustedContact("c3", "Prof. Mehta", "+91 99887 76655", "Campus Safety Liaison")
        )
        _contacts.value = initial
        saveToPrefs(context, initial)
    }

    fun addContact(context: Context, name: String, phone: String, relationship: String) {
        val current = _contacts.value.toMutableList()
        val newContact = TrustedContact(
            id = "c_${System.currentTimeMillis()}",
            name = name.trim(),
            phone = phone.trim(),
            relationship = relationship.trim().ifEmpty { "Trusted Contact" }
        )
        current.add(newContact)
        _contacts.value = current
        saveToPrefs(context, current)
    }

    fun removeContact(context: Context, contactId: String) {
        val current = _contacts.value.filter { it.id != contactId }
        _contacts.value = current
        saveToPrefs(context, current)
    }

    private fun saveToPrefs(context: Context, list: List<TrustedContact>) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val array = JSONArray()
        list.forEach { c ->
            val obj = JSONObject().apply {
                put("id", c.id)
                put("name", c.name)
                put("phone", c.phone)
                put("relationship", c.relationship)
            }
            array.put(obj)
        }
        prefs.edit().putString(KEY_CONTACTS, array.toString()).apply()
    }
}
