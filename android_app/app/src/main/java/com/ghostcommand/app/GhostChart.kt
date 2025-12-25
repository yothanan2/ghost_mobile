package com.ghostcommand.app

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
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
    modifier: Modifier = Modifier
) {
    if (candles.isEmpty()) return

    val textMeasurer = rememberTextMeasurer()
    val NeonGreen = Color(0xFF39FF14)
    val NeonRed = Color(0xFFFF073A)
    val NeonCyan = Color(0xFF00FFFF)
    val ChartBg = Color(0xFF0A0A0A)
    val Gold = Color(0xFFFFD700)

    // Calculate Scale
    val lowestLine = min(lines?.sl ?: Float.MAX_VALUE, lines?.entry ?: Float.MAX_VALUE)
    val highestLine = max(lines?.tp ?: Float.MIN_VALUE, lines?.entry ?: Float.MIN_VALUE)
    
    val minPrice = min(candles.minOf { it.l }, lowestLine) * 0.999f
    val maxPrice = max(candles.maxOf { it.h }, highestLine) * 1.001f
    
    val priceRange = maxPrice - minPrice

    Canvas(modifier = modifier.background(ChartBg).fillMaxSize().padding(end = 40.dp)) {
        val w = size.width
        val h = size.height
        val candleWidth = w / candles.size
        
        // Safe Range
        val safeRange = if (priceRange == 0f) 1f else priceRange

        // DRAW CANDLES
        candles.forEachIndexed { index, candle ->
            val x = index * candleWidth + (candleWidth * 0.1f)
            
            val yHigh = h - ((candle.h - minPrice) / safeRange * h)
            val yLow = h - ((candle.l - minPrice) / safeRange * h)
            val yOpen = h - ((candle.o - minPrice) / safeRange * h)
            val yClose = h - ((candle.c - minPrice) / safeRange * h)
            
            val color = if (candle.c >= candle.o) NeonGreen else NeonRed

            // Wick
            drawLine(color, Offset(x + candleWidth/2, yHigh), Offset(x + candleWidth/2, yLow), strokeWidth = 2f)
            
            // Body
            val top = min(yOpen, yClose)
            val bottom = max(yOpen, yClose)
            val height = max(1f, bottom - top)
            
            drawRect(color, topLeft = Offset(x, top), size = Size(candleWidth * 0.8f, height))
        }

        // DRAW LINES (Entry/SL/TP)
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
                drawText(
                    textMeasurer = textMeasurer,
                    text = label,
                    topLeft = Offset(w + 5f, y - 20f),
                    style = TextStyle(color = color, fontSize = 10.sp)
                )
            }
        }

        lines?.let {
            drawLevel(it.entry, NeonGreen, "E")
            drawLevel(it.sl, NeonRed, "SL")
            drawLevel(it.tp, NeonCyan, "TP")
        }

        // DRAW LIVE PRICE (Dotted Line + Badge)
        val yPrice = h - ((price - minPrice) / safeRange * h)
        if (yPrice in 0f..h) {
             drawLine(
                color = Color.White,
                start = Offset(0f, yPrice),
                end = Offset(w, yPrice),
                strokeWidth = 2f,
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(5f, 5f))
            )
             drawText(
                textMeasurer = textMeasurer,
                text = String.format("%.2f", price),
                topLeft = Offset(w + 5f, yPrice - 10f),
                style = TextStyle(color = Color.White, fontSize = 10.sp, background = Color.DarkGray)
            )
        }
    }
}
