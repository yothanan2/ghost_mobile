package com.ghostcommand.app

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.animation.core.*
import kotlin.math.max
import kotlin.math.min

// Data Models
data class Candle(val o: Float, val h: Float, val l: Float, val c: Float, val ts: Long)
data class TradeLines(val entry: Float, val sl: Float, val tp: Float)

@Composable
fun GhostChart(
    price: Float,
    candles: List<Candle>,
    lines: TradeLines?,
    onClose: () -> Unit, // [NEW] Close Callback
    modifier: Modifier = Modifier
) {
    if (candles.isEmpty()) return

    val textMeasurer = rememberTextMeasurer()
    val NeonGreen = Color(0xFF39FF14)
    val NeonRed = Color(0xFFFF073A)
    val NeonCyan = Color(0xFF00FFFF)
    val ChartBg = Color(0xFF0A0A0A)
    val Gold = Color(0xFFFFD700)

    // --- BLINK LOGIC ---
    // Detect if we should blink (Price crossed TP or SL)
    var blinkColor by remember { mutableStateOf(Color.Transparent) }
    
    if (lines != null) {
        val isLong = lines.tp > lines.entry
        val isWin = if (isLong) price >= lines.tp else price <= lines.tp
        val isLoss = if (isLong) price <= lines.sl else price >= lines.sl
        
        blinkColor = when {
            isWin -> NeonGreen.copy(alpha = 1.0f)
            isLoss -> NeonRed.copy(alpha = 1.0f)
            else -> Color.Transparent
        }
    } else {
        blinkColor = Color.Transparent
    }

    // Animation Pulse
    val infiniteTransition = rememberInfiniteTransition()
    val blinkAlpha by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(500),
            repeatMode = RepeatMode.Reverse
        ),
        label = "blink"
    )
    
    // Effective Color
    // [UI REFINE] Use higher alpha for border visibility
    val activeBlink = if (blinkColor != Color.Transparent) blinkColor.copy(alpha = blinkAlpha) else Color.Transparent

    // --- LAYOUT ---
    // [UI REFINE] Blink is now a Glowing Border
    Box(
        modifier = modifier
            .background(ChartBg)
            .border(width = 3.dp, color = activeBlink, shape = RoundedCornerShape(4.dp))
    ) {
        
        // 2. CHART CANVAS
        val lowestLine = min(lines?.sl ?: Float.MAX_VALUE, lines?.entry ?: Float.MAX_VALUE)
        val highestLine = max(lines?.tp ?: Float.MIN_VALUE, lines?.entry ?: Float.MIN_VALUE)
        
        val minPrice = min(candles.minOf { it.l }, lowestLine) * 0.999f
        val maxPrice = max(candles.maxOf { it.h }, highestLine) * 1.001f
        val priceRange = maxPrice - minPrice

        Canvas(modifier = Modifier.fillMaxSize()) {
            val density = this.density 
            val rightMargin = 40.dp.toPx()
            val w = size.width - rightMargin
            val h = size.height
            
            if (w <= 0f) return@Canvas

            val candleWidth = w / candles.size.toFloat()
            val safeRange = if (priceRange == 0f) 1f else priceRange

            // DRAW CANDLES
            candles.forEachIndexed { index, candle ->
                val x = index * candleWidth + (candleWidth * 0.1f)
                val yHigh = h - ((candle.h - minPrice) / safeRange * h)
                val yLow = h - ((candle.l - minPrice) / safeRange * h)
                val yOpen = h - ((candle.o - minPrice) / safeRange * h)
                val yClose = h - ((candle.c - minPrice) / safeRange * h)
                val color = if (candle.c >= candle.o) NeonGreen else NeonRed

                drawLine(color, Offset(x + candleWidth/2, yHigh), Offset(x + candleWidth/2, yLow), strokeWidth = 2f)
                val top = min(yOpen, yClose)
                val bottom = max(yOpen, yClose)
                val height = max(1f, bottom - top)
                drawRect(color, topLeft = Offset(x, top), size = Size(candleWidth * 0.8f, height))
            }

            // DRAW LINES
            fun drawLevel(level: Float, color: Color, label: String) {
                val y = h - ((level - minPrice) / safeRange * h)
                if (y in 0f..h) {
                    drawLine(
                        color = color,
                        start = Offset(0f, y),
                        end = Offset(w, y),
                        strokeWidth = 3f,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f))
                    )
                    drawText(textMeasurer, label, Offset(w + 5f, y - 20f), TextStyle(color, 10.sp), softWrap = false)
                }
            }
            lines?.let {
                drawLevel(it.entry, NeonGreen, "E")
                drawLevel(it.sl, NeonRed, "SL")
                drawLevel(it.tp, NeonCyan, "TP")
            }

            // DRAW LIVE PRICE
            val yPrice = h - ((price - minPrice) / safeRange * h)
            if (yPrice in 0f..h) {
                 drawLine(
                    color = Color.White, 
                    start = Offset(0f, yPrice), 
                    end = Offset(w, yPrice), 
                    strokeWidth = 2f, 
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(5f, 5f))
                 )
                 drawText(textMeasurer, String.format("%.2f", price), Offset(w + 5f, yPrice - 10f), TextStyle(Color.White, 10.sp, background = Color.DarkGray), softWrap = false)
            }
        }
        
        // 3. OVERLAY: CLOSE BUTTON (Only if active trade lines exist)
        if (lines != null) {
            Button(
                onClick = onClose,
                colors = ButtonDefaults.buttonColors(containerColor = NeonRed.copy(alpha = 0.8f)),
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                modifier = Modifier
                    .align(androidx.compose.ui.Alignment.TopEnd)
                    .padding(8.dp)
                    .height(30.dp),
                shape = RoundedCornerShape(4.dp)
            ) {
                Text("CLOSE", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}
