package com.personal.sms.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

/** M3 shape scale; rows/cards land on the 18dp "large" radius from the spec. */
val AppShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(18.dp),
    extraLarge = RoundedCornerShape(28.dp),
)

/** Cards and conversation rows: 18dp all around. */
val CardShape18 = RoundedCornerShape(18.dp)

// Chat bubbles: ~20dp radius with a 6dp "tail" corner on the sender's bottom side.
/** Sent (right-aligned) bubble — tail at bottom-end. */
val BubbleShapeSent = RoundedCornerShape(
    topStart = 20.dp,
    topEnd = 20.dp,
    bottomEnd = 6.dp,
    bottomStart = 20.dp,
)

/** Received (left-aligned) bubble — tail at bottom-start. */
val BubbleShapeReceived = RoundedCornerShape(
    topStart = 20.dp,
    topEnd = 20.dp,
    bottomEnd = 20.dp,
    bottomStart = 6.dp,
)
