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
    private val TAG = "SlycoWallet class"
    private var secureKeyExchangeInstance:SecureKeyExchange
    private var pubKey = "-----BEGIN PUBLIC KEY-----\\nMHYwEAYHKoZIzj0CAQYFK4EEACIDYgAE5O2gBR/viTNfnzlZwg7iT9q1kVYaXwvO\\n/QPggO+ns6Y9ioK+9ppPWFFuvXehvg0rt3lHfIITaEb894ZFR4yqftg8/Jw7gzPz\\nmpnskzEBZ2+hhXnAkceKqA47olKZpFR4\\n-----END PUBLIC KEY-----"
    private var sessionKey:String = ""

    init{
        secureKeyExchangeInstance = SecureKeyExchange()
        sessionKey = secureKeyExchangeInstance.generateSessionKey(pubKey,secureKeyExchangeInstance.generateSecureNonce(),secureKeyExchangeInstance.getGMTTimestamp())
            .toString()
        Log.d (TAG,sessionKey)
    }
}

class SecureKeyExchange {
    private val keyStore = KeyStore.getInstance("AndroidKeyStore").apply {
        load(null)
    }
    private val keyAlias = "DHKeyPair"
    private val CURVE_NAME = "secp384r1"  // Changed to P-384 for better compatibility

    private fun generateKeyPair() {
        val keyPairGenerator = KeyPairGenerator.getInstance(
            KeyProperties.KEY_ALGORITHM_EC,
            "AndroidKeyStore"
        )

        val parameterSpec = KeyGenParameterSpec.Builder(
            keyAlias,
            KeyProperties.PURPOSE_SIGN or KeyProperties.PURPOSE_VERIFY
        ).apply {
            setAlgorithmParameterSpec(ECGenParameterSpec(CURVE_NAME))
            setDigests(KeyProperties.DIGEST_SHA512)
            setUserAuthenticationRequired(false)
        }.build()

        keyPairGenerator.initialize(parameterSpec)
        keyPairGenerator.generateKeyPair()
    }

    fun generateSessionKey(serverPublicKeyBase64: String,nonce:String,timestamp:String): SecretKeySpec {
        try {
            // Clean the PEM formatted key
            val cleanedKey = cleanPEMKey(serverPublicKeyBase64)

            // Create EC parameters
            val parameterSpec = ECGenParameterSpec(CURVE_NAME)

            // Create a key pair generator with the same curve
            val keyPairGenerator = KeyPairGenerator.getInstance("EC")
            keyPairGenerator.initialize(parameterSpec)
            val keyPair = keyPairGenerator.generateKeyPair()

            // Decode server's public key
            val serverPublicKeyBytes = Base64.decode(cleanedKey, Base64.NO_WRAP)
            val keyFactory = KeyFactory.getInstance("EC")

            // Create EC parameters for the server's public key
            val serverPublicKey = keyFactory.generatePublic(
                java.security.spec.X509EncodedKeySpec(serverPublicKeyBytes)
            )

            // Verify the server's public key curve
            if (!verifyPublicKey(serverPublicKey)) {
                throw SecurityException("Invalid server public key")
            }

            // Generate shared secret using ECDH
            val keyAgreement = KeyAgreement.getInstance("ECDH")
            keyAgreement.init(keyPair.private)
            keyAgreement.doPhase(serverPublicKey, true)
            val sharedSecret = keyAgreement.generateSecret()

            // Create the custom shared secret string
            val customSecret = "SLYCO;$timestamp;$nonce;LOGON"

            // Derive final key using PBKDF2
            val secretKeyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA512")
            val keySpec = PBEKeySpec(
                customSecret.toCharArray(),
                sharedSecret,
                100000,
                256
            )

            val sessionKey = secretKeyFactory.generateSecret(keySpec)
            val keyBytes = sessionKey.encoded

// As hex string
            val hexKey = keyBytes.joinToString("") { "%02x".format(it) }
            Log.d("SessionKey", "Hex: $hexKey")

// Or as Base64
            val base64Key = Base64.encodeToString(keyBytes, Base64.NO_WRAP)
            Log.d("SessionKey", "Base64: $base64Key")
            return SecretKeySpec(sessionKey.encoded, "AES")

        } catch (e: Exception) {
            Log.e("SecureKeyExchange", "Error in key generation", e)
            throw e
        }
    }

    private fun cleanPEMKey(pemKey: String): String {
        return pemKey
            .replace("-----BEGIN PUBLIC KEY-----", "")
            .replace("-----END PUBLIC KEY-----", "")
            .replace("\\n", "")
            .replace("\n", "")
            .trim()
    }

    private fun verifyPublicKey(publicKey: PublicKey): Boolean {
        try {
            val ecPublicKey = publicKey as java.security.interfaces.ECPublicKey
            val keySize = ecPublicKey.params.curve.field.fieldSize

            // Verify it's using P-384
            if (keySize != 384) {
                Log.e("KeyVerification", "Invalid key size: $keySize")
                return false
            }

            return true
        } catch (e: Exception) {
            Log.e("KeyVerification", "Key verification failed", e)
            return false
        }
    }

    fun getGMTTimestamp(): String {
        val instant = Instant.now()
        val utcDateTime = instant.atOffset(ZoneOffset.UTC)

        return String.format(
            "%d-%02d-%02dT%02d:%02d:%02d.%03dZ",
            utcDateTime.year,
            utcDateTime.monthValue,
            utcDateTime.dayOfMonth,
            utcDateTime.hour,
            utcDateTime.minute,
            utcDateTime.second,
            utcDateTime.nano / 1_000_000
        )
    }

    fun generateSecureNonce(length: Int = 32): String {
        val bytes = ByteArray(length)
        SecureRandom().nextBytes(bytes)
        return Base64.encodeToString(bytes, Base64.NO_WRAP)
    }
}

