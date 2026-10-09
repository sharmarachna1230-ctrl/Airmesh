package com.example.data.p2p

import android.content.Context
import android.content.pm.PackageManager
import android.database.Cursor
import android.provider.ContactsContract
import androidx.core.content.ContextCompat

data class PhoneContact(
    val id: String,
    val name: String,
    val phoneNumber: String
)

class ContactManager(private val context: Context) {

    // Cache of device contacts: Phone Number / Hash -> Contact Name
    private val contactsMap = mutableMapOf<String, PhoneContact>()

    // Seed/Known addresses for Demo/Emulation & offline matching
    private val simulatedKnownPeers = mapOf(
        "F4:34:6A:11:22:33" to PhoneContact("1", "Rahul Sharma", "+91 98765 43210"),
        "C8:2B:96:44:55:66" to PhoneContact("2", "Priya Patel", "+91 98234 56789"),
        "A0:18:7D:77:88:99" to PhoneContact("3", "Vikram Singh", "+91 97123 45678"),
        "E2:71:0C:AA:BB:CC" to PhoneContact("4", "Ananya Verma", "+91 96987 65432"),
        "D4:8A:2F:DD:EE:FF" to PhoneContact("5", "Amit Roy (Relay Node)", "+91 95432 10987")
    )

    fun hasContactPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            android.Manifest.permission.READ_CONTACTS
        ) == PackageManager.PERMISSION_GRANTED
    }

    /**
     * Reads contacts from the system phonebook using ContactsContract
     */
    fun loadContactsFromPhonebook(): List<PhoneContact> {
        val result = mutableListOf<PhoneContact>()

        if (!hasContactPermission()) {
            // Return simulation peers if permission not yet given
            return simulatedKnownPeers.values.toList()
        }

        try {
            val cursor: Cursor? = context.contentResolver.query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                arrayOf(
                    ContactsContract.CommonDataKinds.Phone._ID,
                    ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                    ContactsContract.CommonDataKinds.Phone.NUMBER
                ),
                null,
                null,
                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC"
            )

            cursor?.use { c ->
                val idIdx = c.getColumnIndex(ContactsContract.CommonDataKinds.Phone._ID)
                val nameIdx = c.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
                val numberIdx = c.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)

                while (c.moveToNext()) {
                    val id = if (idIdx >= 0) c.getString(idIdx) else ""
                    val name = if (nameIdx >= 0) c.getString(nameIdx) ?: "Unknown" else "Unknown"
                    val number = if (numberIdx >= 0) c.getString(numberIdx) ?: "" else ""

                    if (name.isNotBlank()) {
                        val contact = PhoneContact(id, name, number)
                        result.add(contact)
                        val cleanNumber = number.replace(Regex("[^0-9+]"), "")
                        contactsMap[cleanNumber] = contact
                    }
                }
            }
        } catch (_: Exception) {
            // Graceful fallback to demo contacts
        }

        return if (result.isEmpty()) simulatedKnownPeers.values.toList() else result
    }

    /**
     * Resolves a peer's Bluetooth device name / MAC address / ID into their real phonebook name
     */
    fun matchPeerWithContact(peerMacOrId: String, peerAdvertisedName: String): Pair<String, String?> {
        // Check simulated map first
        simulatedKnownPeers[peerMacOrId]?.let {
            return Pair(it.name, it.phoneNumber)
        }

        // Check if advertised name contains a known phone number
        for ((number, contact) in contactsMap) {
            if (peerAdvertisedName.contains(number) || peerMacOrId.contains(number.takeLast(4))) {
                return Pair(contact.name, contact.phoneNumber)
            }
        }

        // Check by name prefix match in contacts
        val matched = contactsMap.values.find {
            peerAdvertisedName.contains(it.name, ignoreCase = true)
        }
        if (matched != null) {
            return Pair(matched.name, matched.phoneNumber)
        }

        // Default to cleaned advertised name or friendly nearby title
        val fallback = if (peerAdvertisedName.isNotBlank() && peerAdvertisedName != "Unknown") {
            peerAdvertisedName
        } else {
            "AirMesh Peer (${peerMacOrId.takeLast(5)})"
        }
        return Pair(fallback, null)
    }
}
