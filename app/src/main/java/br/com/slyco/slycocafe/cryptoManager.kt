package br.com.slyco.slycocafe

import android.os.Build
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import androidx.annotation.RequiresApi
import java.io.InputStream
import java.io.OutputStream
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.IvParameterSpec

data class cryptoDataDC(
    val ivSize: Int,
    val iv: ByteArray,
    val encryptedDataSize:Int,
    val encryptedData:ByteArray
)

@RequiresApi(Build.VERSION_CODES.M)
class CryptoManager {



    private val keyStore = KeyStore.getInstance("AndroidKeyStore").apply {
        load(null)
    }

    private val encryptCipher get() = Cipher.getInstance(TRANSFORMATION).apply {
        init(Cipher.ENCRYPT_MODE, getKey())
    }

    private fun getDecryptCipherForIv(iv: ByteArray): Cipher {
        return Cipher.getInstance(TRANSFORMATION).apply {
            init(Cipher.DECRYPT_MODE, getKey(), IvParameterSpec(iv))
        }
    }

    fun getKey(): SecretKey {
        val existingKey = keyStore.getEntry(SECRET_ALIAS, null) as? KeyStore.SecretKeyEntry
        return existingKey?.secretKey ?: createKey()
    }

    private fun createKey(): SecretKey {
        return KeyGenerator.getInstance(ALGORITHM).apply {
            init(
                KeyGenParameterSpec.Builder(
                    SECRET_ALIAS,
                    KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
                )
                    .setBlockModes(BLOCK_MODE)
                    .setEncryptionPaddings(PADDING)
                    .setUserAuthenticationRequired(false)
                    .setRandomizedEncryptionRequired(true)
                    .build()
            )
        }.generateKey()
    }

    fun encrypt(bytes: ByteArray): cryptoDataDC {
        val encryptedBytes = encryptCipher.doFinal(bytes)
        val retVal = cryptoDataDC(
            encryptCipher.iv.size,
            encryptCipher.iv,
            encryptedBytes.size,
            encryptedBytes
        )

        return retVal
    }

    fun decrypt(inputEncyptData: cryptoDataDC): ByteArray {
        return getDecryptCipherForIv(inputEncyptData.iv).doFinal(inputEncyptData.encryptedData)
    }

    companion object {
        private const val ALGORITHM = KeyProperties.KEY_ALGORITHM_AES
        private const val BLOCK_MODE = KeyProperties.BLOCK_MODE_CBC
        private const val PADDING = KeyProperties.ENCRYPTION_PADDING_PKCS7
        private const val TRANSFORMATION = "$ALGORITHM/$BLOCK_MODE/$PADDING"
        private const val SECRET_ALIAS = "slyco-api-secret"
    }

}