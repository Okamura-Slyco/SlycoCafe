package br.com.slyco.slycocafe

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import java.security.KeyPairGenerator
import java.security.KeyStore
import java.security.spec.ECGenParameterSpec
import java.security.PrivateKey
import android.util.Base64
import android.util.Log
import java.security.*
import java.time.Instant
import java.time.ZoneOffset
import javax.crypto.KeyAgreement
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

class SlycoWallet {
    private var secureKeyExchangeInstance:SecureKeyExchange
    init{
        secureKeyExchangeInstance = SecureKeyExchange()

    }
}

class SecureKeyExchange {
    private val keyStore = KeyStore.getInstance("AndroidKeyStore").apply {
        load(null)
    }
    private val keyAlias = "DHKeyPair"
    private var nonce = generateSecureNonce()
    private var timestamp = getGMTTimestamp()

    init {
        if (!keyStore.containsAlias(keyAlias)) {
            generateKeyPair()
        }
    }

    private fun generateKeyPair() {
        val keyPairGenerator = KeyPairGenerator.getInstance(
            KeyProperties.KEY_ALGORITHM_EC,
            "AndroidKeyStore"
        )

        val parameterSpec = KeyGenParameterSpec.Builder(
            keyAlias,
            KeyProperties.PURPOSE_SIGN or KeyProperties.PURPOSE_VERIFY
        ).apply {
            setAlgorithmParameterSpec(ECGenParameterSpec("secp521r1"))
            setDigests(KeyProperties.DIGEST_SHA512)
            setUserAuthenticationRequired(false)
        }.build()

        keyPairGenerator.initialize(parameterSpec)
        keyPairGenerator.generateKeyPair()
    }

    fun getPublicKey(): String {
        val publicKey = keyStore.getCertificate(keyAlias).publicKey
        return Base64.encodeToString(publicKey.encoded, Base64.NO_WRAP)
    }

    fun generateSessionKey(serverPublicKeyBase64: String,nonce:String,timestamp:String): SecretKeySpec {
        // Get stored private key
        val privateKey = keyStore.getKey(keyAlias, null) as PrivateKey

        // Create a regular key pair for ECDH (not in KeyStore)
        val keyPairGenerator = KeyPairGenerator.getInstance("EC")
        keyPairGenerator.initialize(ECGenParameterSpec("secp521r1"))
        val keyPair = keyPairGenerator.generateKeyPair()

        // Decode server's public key
        val serverPublicKeyBytes = Base64.decode(serverPublicKeyBase64, Base64.NO_WRAP)
        val keyFactory = KeyFactory.getInstance("EC")
        val serverPublicKey = keyFactory.generatePublic(
            java.security.spec.X509EncodedKeySpec(serverPublicKeyBytes)
        )

        // Generate shared secret using ECDH
        val keyAgreement = KeyAgreement.getInstance("ECDH")
        keyAgreement.init(keyPair.private)
        keyAgreement.doPhase(serverPublicKey, true)
        val sharedSecret = keyAgreement.generateSecret()

        // Generate nonce
        val nonce = generateSecureNonce()

        // Create the custom shared secret string
        val customSecret = "SLYCO;$timestamp;$nonce;LOGON"

        // Derive final key using PBKDF2
        val secretKeyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA512")
        val keySpec = PBEKeySpec(
            customSecret.toCharArray(),
            sharedSecret,
            100000, // iterations
            256 // key length in bits for AES-256
        )

        val sessionKey = secretKeyFactory.generateSecret(keySpec)
        return SecretKeySpec(sessionKey.encoded, "AES")
    }
    private fun getGMTTimestamp(): String {
        val instant = Instant.now()
        val utcDateTime = instant.atOffset(ZoneOffset.UTC)

        // Match Python's datetime.now(timezone.utc).strftime('%Y-%m-%dT%H:%M:%S.%fZ')[:-3]
        return String.format(
            "%d-%02d-%02dT%02d:%02d:%02d.%03dZ",
            utcDateTime.year,
            utcDateTime.monthValue,
            utcDateTime.dayOfMonth,
            utcDateTime.hour,
            utcDateTime.minute,
            utcDateTime.second,
            utcDateTime.nano / 1_000_000  // Convert nanos to milliseconds to match Python's [:-3] slice
        )
    }

    private fun generateSecureNonce(length: Int = 32): String {
        val bytes = ByteArray(length)
        SecureRandom().nextBytes(bytes)
        return Base64.encodeToString(bytes, Base64.NO_WRAP)
    }

    fun clearKeys() {
        if (keyStore.containsAlias(keyAlias)) {
            keyStore.deleteEntry(keyAlias)
        }
    }

    // Helper function to verify the received public key
    private fun verifyPublicKey(publicKey: PublicKey): Boolean {
        try {
            val ecPublicKey = publicKey as java.security.interfaces.ECPublicKey
            val keySize = ecPublicKey.params.curve.field.fieldSize

            // Verify key size
            if (keySize != 521) {
                throw SecurityException("Invalid key size: $keySize")
            }

            return true
        } catch (e: Exception) {
            Log.e("KeyVerification", "Key verification failed", e)
            return false
        }
    }
}
