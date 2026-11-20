package com.personal.sms

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import com.personal.sms.util.recipientsFromUri

const val EXTRA_COMPOSE_ADDRESS = "com.personal.sms.COMPOSE_ADDRESS"

/**
 * Entry point for the sms/smsto "sendto" intent (part of the default-SMS contract).
 * Forwards the recipient to [MainActivity], which routes to the thread/composer.
 */
class ComposeSmsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val address = recipientsFromUri(intent.data).firstOrNull()
        startActivity(
            Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                if (address != null) putExtra(EXTRA_COMPOSE_ADDRESS, address)
            },
        )
        finish()
    }
}
