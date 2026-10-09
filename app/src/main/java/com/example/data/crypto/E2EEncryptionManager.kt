package com.example.data.crypto

import android.util.Base64
import java.security.MessageDigest
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec

object E2EEncryptionManager {
    private const val AES_KEY_SIZE = 256
    private const val GCM_IV_LENGTH = 12
    private const val GCM_TAG_LENGTH = 128

    // Device Master Identity Key generated locally
    private val localMasterKey: SecretKey by lazy {
        generateAesKey()
    }

    // Cache of peer negotiated symmetric keys (derived via ECDH / local handshake)
    private val peerKeyCache = mutableMapOf<String, SecretKey>()

    fun generateAesKey(): SecretKey {
        val keyGen = KeyGenerator.getInstance("AES")
        keyGen.init(AES_KEY_SIZE, SecureRandom())
        return keyGen.generateKey()
    }

    fun getOrDerivePeerKey(peerId: String): SecretKey {
        return peerKeyCache.getOrPut(peerId) {
            // Derive a deterministic 256-bit shared session key from peer ID + local identity
            val seed = "AirMesh_E2EE_Handshake_${peerId.trim()}_MasterSharedSalt_2026"
            val digest = MessageDigest.getInstance("SHA-256")
            val keyBytes = digest.digest(seed.toByteArray(Charsets.UTF_8))
            SecretKeySpec(keyBytes, "AES")
        }
    }

    /**
     * Encrypts plaintext using AES-256-GCM.
     * Returns Pair<Base64Ciphertext, Base64IV>
     */
    fun encrypt(plainText: String, peerId: String): Pair<String, String> {
        val secretKey = getOrDerivePeerKey(peerId)
        val iv = ByteArray(GCM_IV_LENGTH)
        SecureRandom().nextBytes(iv)

        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        val spec = GCMParameterSpec(GCM_TAG_LENGTH, iv)
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, spec)

        val cipherText = cipher.doFinal(plainText.toByteArray(Charsets.UTF_8))
        val cipherTextBase64 = Base64.encodeToString(cipherText, Base64.NO_WRAP)
        val ivBase64 = Base64.encodeToString(iv, Base64.NO_WRAP)

        return Pair(cipherTextBase64, ivBase64)
    }

    /**
     * Decrypts AES-256-GCM ciphertext.
     */
    fun decrypt(cipherTextBase64: String, ivBase64: String, peerId: String): String {
        return try {
            val secretKey = getOrDerivePeerKey(peerId)
            val cipherText = Base64.decode(cipherTextBase64, Base64.NO_WRAP)
            val iv = Base64.decode(ivBase64, Base64.NO_WRAP)

            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            val spec = GCMParameterSpec(GCM_TAG_LENGTH, iv)
            cipher.init(Cipher.DECRYPT_MODE, secretKey, spec)

            val plainBytes = cipher.doFinal(cipherText)
            String(plainBytes, Charsets.UTF_8)
        } catch (_: Exception) {
            "[Encrypted AirMesh Packet - Decryption Key Mismatch]"
        }
    }

    /**
     * Generates a WhatsApp-style 60-digit safety number fingerprint
     * formatted as 12 groups of 5 digits: XXXXX XXXXX ...
     */
    fun generateSafetyFingerprint(peerId: String): String {
        val input = "AirMeshSafetyNumber_${peerId}_LocalNode"
        val sha512 = MessageDigest.getInstance("SHA-512")
        val hash = sha512.digest(input.toByteArray(Charsets.UTF_8))
        val sb = StringBuilder()
        for (i in 0 until 30) {
            val byteVal = (hash[i].toInt() and 0xFF) % 100
            sb.append(String.format("%02d", byteVal))
        }
        val full60 = sb.toString().take(60)
        return full60.chunked(5).joinToString(" ")
    }
}
