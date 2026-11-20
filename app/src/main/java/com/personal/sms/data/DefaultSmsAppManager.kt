package com.personal.sms.data

import android.app.role.RoleManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Telephony

/** Checks and requests the system default-SMS-app role. */
class DefaultSmsAppManager(private val context: Context) {

    fun isDefault(): Boolean =
        Telephony.Sms.getDefaultSmsPackage(context) == context.packageName

    /** Intent that prompts the user to make this app the default SMS app. */
    fun requestDefaultIntent(): Intent =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val roleManager = context.getSystemService(RoleManager::class.java)
            roleManager.createRequestRoleIntent(RoleManager.ROLE_SMS)
        } else {
            @Suppress("DEPRECATION")
            Intent(Telephony.Sms.Intents.ACTION_CHANGE_DEFAULT)
                .putExtra(Telephony.Sms.Intents.EXTRA_PACKAGE_NAME, context.packageName)
        }
}
