package com.personal.sms.ui.components

import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow

/** Renders [text] with the given match [ranges] highlighted in the accent color. */
@Composable
fun HighlightedText(
    text: String,
    ranges: List<IntRange>,
    modifier: Modifier = Modifier,
    style: TextStyle = LocalTextStyle.current,
    maxLines: Int = Int.MAX_VALUE,
) {
    val highlightBg = MaterialTheme.colorScheme.primary.copy(alpha = 0.16f)
    val highlightFg = MaterialTheme.colorScheme.primary
    val baseColor = MaterialTheme.colorScheme.onBackground

    val annotated = remember(text, ranges, highlightBg, highlightFg) {
        buildAnnotatedString {
            append(text)
            ranges.forEach { range ->
                val start = range.first.coerceIn(0, text.length)
                val end = (range.last + 1).coerceIn(start, text.length)
                addStyle(
                    SpanStyle(background = highlightBg, color = highlightFg, fontWeight = FontWeight.SemiBold),
                    start,
                    end,
                )
            }
        }
    }
    Text(
        text = annotated,
        style = style,
        color = baseColor,
        maxLines = maxLines,
        overflow = TextOverflow.Ellipsis,
        modifier = modifier,
    )
}
