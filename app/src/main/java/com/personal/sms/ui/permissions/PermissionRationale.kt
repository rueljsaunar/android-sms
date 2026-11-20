package com.personal.sms.ui.permissions

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable

/** Explains why the app needs SMS / Contacts / Notification access before requesting. */
@Composable
fun PermissionRationaleDialog(onContinue: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("A few permissions") },
        text = {
            Text(
                "To work as your messaging app, it needs:\n\n" +
                    "• SMS — to send and receive your texts\n" +
                    "• Contacts — to show names and photos instead of numbers\n" +
                    "• Notifications — to alert you to new messages\n\n" +
                    "Everything stays on this device — nothing is uploaded.",
            )
        },
        confirmButton = { TextButton(onClick = onContinue) { Text("Continue") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Not now") } },
    )
}
