package com.e.mykotlindemo


import android.annotation.SuppressLint
import android.app.KeyguardManager
import android.content.Context
import android.hardware.fingerprint.FingerprintManager
import android.os.Bundle
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey

class MainActivity : AppCompatActivity() {
    private val KEY_NAME:String="mykey"
    private val TAG:String="FigurePrintAuth"

    private var keyStore: KeyStore? = null
    private var keyGenerator: KeyGenerator? = null
    private val textView: TextView? = null
    private var cryptoObject: FingerprintManager.CryptoObject? = null
    private var fingerprintManager: FingerprintManager? = null


    var message: TextView?=null
    @SuppressLint("NewApi")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        message = findViewById<TextView>(R.id.message)
        val btn = findViewById<Button>(R.id.button)
        val fph = FingerprintHandler(message!!)
        if (!checkFinger()) {
            Log.d(TAG,"checkFinger return"+!checkFinger())
            btn.isEnabled = false
        } else {
            // We are ready to set up the cipher and the key
            Log.d(TAG,"checkFinger return else"+!checkFinger())
            generateKey()
            val cipher = generateCipher()
            cryptoObject = FingerprintManager.CryptoObject(cipher)
            message?.text=getString(R.string.msg);

        }
        btn.setOnClickListener{
            message!!.text="Touch the fingerprint scanner to authorize"
            fph.doAuth(this.fingerprintManager!!, this!!.cryptoObject!!)

        }
    }


    @SuppressLint("NewApi")
    private fun checkFinger(): Boolean {
        // Keyguard Manager
        val keyguardManager = getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
        // Fingerprint Manager
        fingerprintManager = getSystemService(Context.FINGERPRINT_SERVICE) as FingerprintManager
        try {
            // Check if the fingerprint sensor is present
            if (!fingerprintManager!!.isHardwareDetected) {
                // Update the UI with a message
               // message?.text = getString(R.string.fingerprint_not_supported)
                return false
            }
            if (!fingerprintManager!!.hasEnrolledFingerprints()) {
               // message?.text = getString(R.string.no_fingerprint_configured)
                return false
            }
            if (!keyguardManager.isKeyguardSecure) {
              //  message?.text = getString(R.string.secure_lock_not_enabled)
                return false
            }
        } catch (se: SecurityException) {
            se.printStackTrace()
        }

        return true
    }

    @SuppressLint("NewApi")
    private fun generateKey() {

        // Get the reference to the key store
        keyStore = KeyStore.getInstance("AndroidKeyStore")
        // Key generator to generate the key
        keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES,
            "AndroidKeyStore")
        keyStore?.load(null)
        keyGenerator?.init(KeyGenParameterSpec.Builder(KEY_NAME,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
            .setBlockModes(KeyProperties.BLOCK_MODE_CBC)
            .setUserAuthenticationRequired(true)
            .setEncryptionPaddings(
                KeyProperties.ENCRYPTION_PADDING_PKCS7)
            .build())
        keyGenerator?.generateKey()


    }


    private fun generateCipher(): Cipher {

        val cipher = Cipher.getInstance(KeyProperties.KEY_ALGORITHM_AES + "/"
                + KeyProperties.BLOCK_MODE_CBC + "/"
                + KeyProperties.ENCRYPTION_PADDING_PKCS7)
        val key = keyStore?.getKey(KEY_NAME,
            null) as SecretKey
        cipher.init(Cipher.ENCRYPT_MODE, key)
        return cipher
    }
}


