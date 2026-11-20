package com.personal.sms.ui.thread

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DatePicker
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import java.util.Calendar
import java.util.TimeZone

/** Two-step (date then time) scheduling dialog; emits the chosen epoch millis. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScheduleDialog(onDismiss: () -> Unit, onConfirm: (Long) -> Unit) {
    val now = System.currentTimeMillis()
    val dateState = rememberDatePickerState(initialSelectedDateMillis = now)
    val timeState = rememberTimePickerState(is24Hour = false)
    var step by remember { mutableIntStateOf(0) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(shape = RoundedCornerShape(24.dp), color = MaterialTheme.colorScheme.surface) {
            Column(
                modifier = Modifier.padding(16.dp).verticalScroll(rememberScrollState()),
            ) {
                if (step == 0) {
                    DatePicker(state = dateState, title = null)
                    DialogButtons(
                        leadingText = "Cancel",
                        onLeading = onDismiss,
                        trailingText = "Next",
                        trailingEnabled = dateState.selectedDateMillis != null,
                        onTrailing = { step = 1 },
                    )
                } else {
                    Text(
                        "Pick a time",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(start = 8.dp, bottom = 12.dp),
                    )
                    TimePicker(state = timeState)
                    DialogButtons(
                        leadingText = "Back",
                        onLeading = { step = 0 },
                        trailingText = "Schedule",
                        trailingEnabled = true,
                        onTrailing = {
                            val millis = dateState.selectedDateMillis ?: return@DialogButtons
                            onConfirm(combine(millis, timeState.hour, timeState.minute))
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun DialogButtons(
    leadingText: String,
    onLeading: () -> Unit,
    trailingText: String,
    trailingEnabled: Boolean,
    onTrailing: () -> Unit,
) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
        TextButton(onClick = onLeading) { Text(leadingText) }
        TextButton(onClick = onTrailing, enabled = trailingEnabled) { Text(trailingText) }
    }
}

/** DatePicker returns a UTC-midnight millis; rebuild it in the local zone with the time. */
private fun combine(dateUtcMillis: Long, hour: Int, minute: Int): Long {
    val utc = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply { timeInMillis = dateUtcMillis }
    return Calendar.getInstance().apply {
        set(
            utc.get(Calendar.YEAR),
            utc.get(Calendar.MONTH),
            utc.get(Calendar.DAY_OF_MONTH),
            hour,
            minute,
            0,
        )
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis
}
