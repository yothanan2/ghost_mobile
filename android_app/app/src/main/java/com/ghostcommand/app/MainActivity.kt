package com.ghostcommand.app

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import androidx.activity.ComponentActivity
// Biometric imports removed v2.11
import androidx.activity.compose.setContent
import java.security.MessageDigest

import androidx.compose.foundation.border
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessaging
import android.widget.Toast
import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.app.DownloadManager
import android.content.Intent
import android.net.Uri
import android.os.Environment
import androidx.core.content.FileProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import org.json.JSONObject
import java.io.File

// [v2.09] Ghost Chart Integration
import com.ghostcommand.app.GhostChart
import com.ghostcommand.app.Candle
import com.ghostcommand.app.TradeLines

// --- COLORS ---
// --- COLORS ---
val BgDark = Color(0xFF0A0A0A)
val CardBg = Color(0xFF161616)
val NeonGreen = Color(0xFF00FF9D)
val NeonRed = Color(0xFFFF2A2A)
val NeonBlue = Color(0xFF00D1FF)
val TextDim = Color(0xFFB0B0B0) // [UI FIX] Lightened from 0xFF666666
val Gold = Color(0xFFFFD700)     // For Authority (Boss Mode)
val BarBg = Color(0xFF333333)    // Dark track for progress bars

// --- DATA MODELS ---
data class GhostVitals(
    val equity: Double = 0.0,
    val balance: Double = 0.0,
    val rsi: Double = 50.0,
    val trend: String = "SCANNING",
    val active_trades: Int = 0,
    // [NEW] Flight Deck Metrics (Defaults for safety)
    val adx: Double = 0.0,
    val atr: Double = 0.0,
    val confidence: Double = 0.0,
    val daily_profit: Double = 0.0,
    val daily_target: Double = 60.0,
    val auto_god_mode: Boolean = true,
    // [NEW] V2.01 State Sync
    val swarm_mode: Boolean = false,
    val use_firewall: Boolean = true,
    val whale_tracker: Boolean = true, // SR Filter
    val auto_rev: Boolean = false,
    val risk_percent: Double = 1.5,
    val mode_name: String = "MANUAL", // For Rhythm/Auto Pilot
    // [NEW] Dynamic Currency
    val currency_symbol: String = "",
    val currency: String = "USD"
)

data class TradeRecord(
    val id: String = "",
    val symbol: String = "",
    val type: String = "BUY", // BUY or SELL
    val profit: Double = 0.0,
    val time: String = "", // HH:MM format
    // [NEW] Dynamic Currency
    val currency_symbol: String = "",
    val currency: String = "USD"
)

// --- POPUP MENU / HELPERS ---
fun formatCurrency(amount: Double, symbol: String, code: String): String {
    val sign = if (amount < 0) "-" else ""
    val absAmount = Math.abs(amount)
    
    // Logic: Prefer symbol ("฿", "€", "$"), fallback to code ("THB", "EUR", "USD")
    val label = if (symbol.isNotEmpty()) symbol else code.ifEmpty { "USD" }
    
    // Add space if label is a code (length > 1), otherwise stick it (e.g. $100 vs THB 100)
    val spacer = if (label.length > 1) " " else ""
    
    return "$sign$label$spacer${String.format("%.2f", absAmount)}"
}

data class LogEntry(
    val id: String = "",
    val message: String = ""
)

// [OTA] Version Model
data class RemoteVersion(
    val code: Int = 0,
    val name: String = "",
    val url: String = "",
    val changelog: String = "",
    val mandatory: Boolean = false,
    val changelog_map: Map<String, String> = emptyMap()
)

// --- TRANSLATION MODULE ---
object GhostLingo {
    val EN = mapOf(
        "DASHBOARD" to "DASHBOARD",
        "NEURAL_STREAM" to "TERMINAL", // [v2.08 REBRAND]
        "HISTORY" to "HISTORY",
        "DAILY_VAULT" to "DAILY VAULT",
        "RSI_LABEL" to "RSI (MOMENTUM)",
        "ATR_LABEL" to "ATR (VOLATILITY)",
        "ADX_LABEL" to "ADX (TREND STR)",
        "AUTHORITY_LABEL" to "AI AUTHORITY",
        "FLOAT_PL" to "FLOAT P/L",
        "ACTIVE_TRADES" to "ACTIVE TRADES",
        "MARKET_TREND" to "MARKET TREND",
        "CLOSE_ALL" to "☢️ EMERGENCY CLOSE ALL",
        "LOGOUT" to "LOGOUT",
        "TODAYS_TRADES" to "TODAY'S TRADES",
        "SESSION_LEDGER" to "SESSION LEDGER",
        "NO_TRADES" to "NO TRADES YET",
        "LOGIN_TITLE" to "GHOST RC",
        "LOGIN_SUB" to "SECURE UPLINK V2.0",
        "TARGET_ID" to "TARGET ID",
        "CONNECT_BTN" to "ESTABLISH LINK",
        "LANG_TOGGLE" to "🇺🇸 EN",
        "GOD_MODE_AUTO" to "⚡ GOD MODE: AUTO",
        "GOD_MODE_OFF" to "🛡️ SAFE ESCAPE (OFF)",
        "EQ_LABEL" to "EQ"
    )

    val TH = mapOf(
        "DASHBOARD" to "แดชบอร์ด",
        "NEURAL_STREAM" to "ระบบบันทึก",
        "HISTORY" to "ประวัติ",
        "DAILY_VAULT" to "กำไรวันนี้",
        "RSI_LABEL" to "RSI (โมเมนตัม)",
        "ATR_LABEL" to "ATR (ความผันผวน)",
        "ADX_LABEL" to "ADX (กำลังเทรนด์)",
        "AUTHORITY_LABEL" to "ความมั่นใจ AI",
        "FLOAT_PL" to "กำไรลอยตัว",
        "ACTIVE_TRADES" to "ออเดอร์คงค้าง",
        "MARKET_TREND" to "แนวโน้มตลาด",
        "CLOSE_ALL" to "☢️ ปิดทุกออเดอร์ทันที",
        "LOGOUT" to "ออกระบบ",
        "TODAYS_TRADES" to "รายการเทรดวันนี้",
        "SESSION_LEDGER" to "บัญชีรอบนี้",
        "NO_TRADES" to "ยังไม่มีรายการเทรด",
        "LOGIN_TITLE" to "GHOST RC",
        "LOGIN_SUB" to "ระบบเชื่อมต่อ V2.0",
        "TARGET_ID" to "ระบุ ID ปลายทาง",
        "CONNECT_BTN" to "เริ่มการเชื่อมต่อ",
        "LANG_TOGGLE" to "🇹🇭 TH",
        "GOD_MODE_AUTO" to "⚡ โหมดเทพ: ออโต้",

        "GOD_MODE_OFF" to "🛡️ โหมดปลอดภัย (ปิด)",
        "EQ_LABEL" to "ยอดเงินในบัญชี" // Equity/Balance
    )
}

fun TR(key: String, lang: String): String {
    val map = if (lang == "TH") GhostLingo.TH else GhostLingo.EN
    return map[key] ?: key
}

// --- MAIN ACTIVITY ---
class MainActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // --- GHOST FIX: FORCE PERMISSION POPUP ---
        requestNotificationPermission()
        
        // --- GHOST LINK SUBSCRIPTION ---
        FirebaseMessaging.getInstance().subscribeToTopic("ghost_alerts")
        
        // --- APP SECURITY LOCK REMOVED (v2.11) ---
        setContent { GhostAppEntryPoint() }
    }

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.POST_NOTIFICATIONS), 101)
            }
        }
    }
}

// --- SECURITY HELPERS ---
// --- SECURITY HELPERS (Removed in v2.11) ---
// hashPin kept for legacy support if needed, though PIN is removed from UI.
fun hashPin(pin: String): String {
    val bytes = MessageDigest.getInstance("SHA-256").digest(pin.toByteArray())
    return bytes.joinToString("") { "%02x".format(it) }
}

// --- ENTRY POINT ---
@Composable
fun GhostAppEntryPoint() {
    val context = LocalContext.current
    val prefs = context.getSharedPreferences("ghost_prefs", Context.MODE_PRIVATE)
    var savedId by remember { mutableStateOf(prefs.getString("bot_id", "") ?: "") }
    
    // LANGUAGE STATE
    var lang by remember { mutableStateOf(prefs.getString("lang", "EN") ?: "EN") }
    fun toggleLang() {
        val newLang = if (lang == "EN") "TH" else "EN"
        lang = newLang
        prefs.edit().putString("lang", newLang).apply()
    }
    
    // [v2.09] SOUND NOTIFICATION STATE
    var soundEnabled by remember { mutableStateOf(prefs.getBoolean("sound_enabled", true)) }
    fun toggleSound() {
        soundEnabled = !soundEnabled
        prefs.edit().putBoolean("sound_enabled", soundEnabled).apply()
    }
    
    if (savedId.isNotEmpty()) {
        MainScreen(botId = savedId, lang = lang, onToggleLang = ::toggleLang, onLogout = {
            prefs.edit().remove("bot_id").apply()
            savedId = ""
        })
    } else {
        LoginScreen(lang = lang, onToggleLang = ::toggleLang, onLogin = { newId ->
            prefs.edit().putString("bot_id", newId).apply()
            savedId = newId
        })
    }
}

// --- LOGIN SCREEN (Simplified - No PIN Required) ---
@Composable
fun LoginScreen(lang: String, onToggleLang: () -> Unit, onLogin: (String) -> Unit) {
    var idText by remember { mutableStateOf("") }
    var statusMsg by remember { mutableStateOf("") }
    
    Box(modifier = Modifier.fillMaxSize().background(BgDark)) {
        // Lang Toggle (Top Right)
        Button(
            onClick = onToggleLang, 
            modifier = Modifier.align(Alignment.TopEnd).padding(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF222222)),
            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Text(TR("LANG_TOGGLE", lang), fontSize = 12.sp, color = Color.White)
        }

        Column(
            modifier = Modifier.fillMaxSize().padding(30.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(TR("LOGIN_TITLE", lang), color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
            Text(TR("LOGIN_SUB", lang), color = NeonGreen, fontSize = 12.sp, letterSpacing = 4.sp)
            Spacer(modifier = Modifier.height(30.dp))
            
            // TARGET ID (MT5 ID)
            Text(TR("TARGET_ID", lang), color = TextDim, fontSize = 12.sp, modifier = Modifier.align(Alignment.Start))
            OutlinedTextField(
                value = idText,
                onValueChange = { idText = it },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = NeonGreen,
                    unfocusedBorderColor = Color.Gray,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.Gray
                ),
                modifier = Modifier.fillMaxWidth().padding(vertical = 5.dp),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )

            if (statusMsg.isNotEmpty()) {
                Text(statusMsg, color = NeonRed, fontSize = 12.sp, modifier = Modifier.padding(top = 10.dp))
            }
            
            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = { 
                    if (idText.isNotBlank()) {
                        onLogin(idText)
                    } else {
                        statusMsg = "Please enter your MT5 ID"
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = NeonGreen),
                shape = RoundedCornerShape(4.dp),
                modifier = Modifier.fillMaxWidth().height(50.dp)
            ) {
                Text(TR("CONNECT_BTN", lang), color = Color.Black, fontWeight = FontWeight.Bold)
            }
        }
    }
}

// --- MAIN SCREEN (TABS) ---
@Composable
fun MainScreen(botId: String, lang: String, onToggleLang: () -> Unit, onLogout: () -> Unit) {
    // FORCE EUROPE SERVER
    val database = Firebase.database("https://ghost-app-2fff8-default-rtdb.europe-west1.firebasedatabase.app")
    var selectedTab by remember { mutableStateOf(0) }
    
    // [OTA] UPDATE CHECKER & DOWNLOAD HANDLER
    CheckForUpdates(database, LocalContext.current, lang)

    // [VITALS] Live Telemetry for State Sync
    var vitals by remember { mutableStateOf(GhostVitals()) }
    DisposableEffect(botId) {
        val rules = database.getReference("users/$botId/system/vitals")
        val listener = object : com.google.firebase.database.ValueEventListener {
            override fun onDataChange(s: com.google.firebase.database.DataSnapshot) {
                val v = s.getValue(GhostVitals::class.java)
                if (v != null) vitals = v
            }
            override fun onCancelled(e: com.google.firebase.database.DatabaseError) {}
        }
        rules.addValueEventListener(listener)
        onDispose { rules.removeEventListener(listener) }
    }

    // [v2.09] GHOST CHART State
    var chartPrice by remember { mutableStateOf(0f) }
    var chartCandles by remember { mutableStateOf<List<Candle>>(emptyList()) }
    var chartLines by remember { mutableStateOf<TradeLines?>(null) }
    
    DisposableEffect(botId) {
        val chartRef = database.getReference("users/$botId/live_chart")
        val chartListener = object : ValueEventListener {
            override fun onDataChange(s: DataSnapshot) {
                try {
                    val data = s.value as? Map<String, Any> ?: return
                    
                    chartPrice = (data["price"] as? Number)?.toFloat() ?: 0f
                    
                    // Parse candle data
                    val candleData = data["candle"] as? Map<String, Any>
                    if (candleData != null) {
                        val o = (candleData["o"] as? Number)?.toFloat() ?: 0f
                        val h = (candleData["h"] as? Number)?.toFloat() ?: 0f
                        val l = (candleData["l"] as? Number)?.toFloat() ?: 0f
                        val c = (candleData["c"] as? Number)?.toFloat() ?: 0f
                        val ts = System.currentTimeMillis()
                        
                        // Keep last 50 candles in memory
                        chartCandles = (chartCandles + Candle(o, h, l, c, ts)).takeLast(50)
                    }
                    
                    // Parse trade lines
                    val linesData = data["lines"] as? Map<String, Any>
                    chartLines = if (linesData != null) {
                        TradeLines(
                            entry = (linesData["entry"] as? Number)?.toFloat() ?: 0f,
                            sl = (linesData["sl"] as? Number)?.toFloat() ?: 0f,
                            tp = (linesData["tp"] as? Number)?.toFloat() ?: 0f
                        )
                    } else null
                } catch (e: Exception) {
                    // Silent fail - chart is non-critical
                }
            }
            override fun onCancelled(e: DatabaseError) {}
        }
        chartRef.addValueEventListener(chartListener)
        onDispose { chartRef.removeEventListener(chartListener) }
    }

    Column(modifier = Modifier.fillMaxSize().background(BgDark)) {
        // CONTENT AREA
        Box(modifier = Modifier.weight(1f)) {
            when (selectedTab) {
                0 -> TacticalDashboard(database, botId, onLogout, lang, onToggleLang, chartPrice, chartCandles, chartLines)
                1 -> NeuralStream(database, botId, lang)
                2 -> DailyHistory(database, botId, lang)
                3 -> SettingsScreen(database, botId, lang, vitals)
            }
        }
        
        // BOTTOM NAVIGATION
        Row(
            modifier = Modifier.fillMaxWidth().height(60.dp).background(Color(0xFF111111)).border(1.dp, Color(0xFF222222)),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TabButton(TR("DASHBOARD", lang), selected = selectedTab == 0) { selectedTab = 0 }
            TabButton(TR("NEURAL_STREAM", lang), selected = selectedTab == 1) { selectedTab = 1 }
            TabButton(TR("HISTORY", lang), selected = selectedTab == 2) { selectedTab = 2 }
            // [UI POLISH] Icon for Settings to save space
            IconTabButton(androidx.compose.material.icons.Icons.Default.Settings, selected = selectedTab == 3) { selectedTab = 3 }
        }
    }
}

@Composable
fun TabButton(text: String, selected: Boolean, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(containerColor = if (selected) Color(0xFF222222) else Color.Transparent),
        shape = RoundedCornerShape(8.dp)
    ) {
        Text(text, color = if (selected) NeonGreen else TextDim, fontSize = 12.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun IconTabButton(icon: androidx.compose.ui.graphics.vector.ImageVector, selected: Boolean, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(containerColor = if (selected) Color(0xFF222222) else Color.Transparent),
        shape = RoundedCornerShape(8.dp),
        contentPadding = PaddingValues(0.dp),
        modifier = Modifier.width(60.dp) // Fixed width for icon
    ) {
        Icon(icon, contentDescription = null, tint = if (selected) NeonGreen else TextDim)
    }
}

// --- TAB 1: DASHBOARD ---
@Composable
fun TacticalDashboard(
    database: com.google.firebase.database.FirebaseDatabase, 
    botId: String, 
    onLogout: () -> Unit, 
    lang: String, 
    onToggleLang: () -> Unit,
    chartPrice: Float,
    chartCandles: List<Candle>,
    chartLines: TradeLines?
) {
    val vitalsRef = database.getReference("users/$botId/vitals")
    val cmdRef = database.getReference("users/$botId/commands")
    var vitals by remember { mutableStateOf(GhostVitals()) }
    var connectionState by remember { mutableStateOf("SCANNING...") }

    DisposableEffect(botId) {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                // Safety: Map snapshot to object, use defaults if keys missing
                vitals = snapshot.getValue(GhostVitals::class.java) ?: GhostVitals()
                connectionState = "● ONLINE"
            }
            override fun onCancelled(error: DatabaseError) { connectionState = "⚠️ ERROR" }
        }
        vitalsRef.addValueEventListener(listener)
        onDispose { vitalsRef.removeEventListener(listener) }
    }

    // Logic: Authority Color
    val authThreshold = 55.0
    val isAuthorized = vitals.confidence >= authThreshold
    val authColor = if (isAuthorized) Gold else NeonBlue
    val authLabel = "${vitals.confidence.toInt()}% / ${authThreshold.toInt()}%" + if(isAuthorized) " (GO)" else ""

    // Logic: Vault Color
    val vaultColor = when {
        vitals.daily_profit >= vitals.daily_target -> Gold
        vitals.daily_profit > 0 -> NeonGreen
        else -> NeonRed
    }

    Column(modifier = Modifier.fillMaxSize().padding(20.dp).verticalScroll(rememberScrollState())) {
        
        // 1. HEADER
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Column {
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(TR("LOGIN_TITLE", lang), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                    Spacer(Modifier.width(6.dp))
                    Text("v${BuildConfig.VERSION_NAME}", color = TextDim, fontSize = 12.sp, modifier = Modifier.padding(bottom = 3.dp))
                }
                Text("ID: $botId  |  $connectionState", color = NeonGreen, fontSize = 10.sp)
            }
            Row {
                // Lang Toggle
                Button(
                    onClick = onToggleLang,
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                    modifier = Modifier.height(30.dp),
                    contentPadding = PaddingValues(horizontal = 5.dp)
                ) {
                    Text(TR("LANG_TOGGLE", lang), fontSize = 12.sp, color = NeonGreen)
                }
                Button(onClick = onLogout, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF222222)), modifier = Modifier.height(30.dp), contentPadding = PaddingValues(horizontal = 10.dp)) {
                    Text(TR("LOGOUT", lang), fontSize = 10.sp, color = Color.White)
                }
            }
        }
        Spacer(modifier = Modifier.height(20.dp))

        // 2. DAILY VAULT (Full Width)
        CyberCard {
            Column(Modifier.padding(15.dp)) {
                // [DYNAMIC CURRENCY]
                val currentC = formatCurrency(vitals.daily_profit, vitals.currency_symbol, vitals.currency)
                val targetC = formatCurrency(vitals.daily_target, vitals.currency_symbol, vitals.currency)
                
                FlightGauge(
                    title = "${TR("DAILY_VAULT", lang)} ($currentC / $targetC)", 
                    value = vitals.daily_profit, 
                    max = vitals.daily_target, 
                    color = vaultColor,
                    label = " "
                )
            }
        }
        Spacer(modifier = Modifier.height(12.dp))

        // 3. FLIGHT DECK (2x2 Grid)
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            // Left Column
            Column(Modifier.weight(1f)) {
                // RSI
                CyberCard {
                    Column(Modifier.padding(12.dp)) {
                        val rsiColor = when { vitals.rsi < 30 -> NeonGreen; vitals.rsi > 70 -> NeonRed; else -> Color.Gray }
                        FlightGauge(TR("RSI_LABEL", lang), vitals.rsi, 100.0, rsiColor)
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                // ATR
                CyberCard {
                    Column(Modifier.padding(12.dp)) {
                        FlightGauge(TR("ATR_LABEL", lang), vitals.atr, 2.0, NeonGreen) // Max 2.0 arbitrary scaling
                    }
                }
            }

            // Right Column
            Column(Modifier.weight(1f)) {
                // ADX
                CyberCard {
                    Column(Modifier.padding(12.dp)) {
                        val adxColor = if (vitals.adx > 25) NeonBlue else Color.Gray
                        FlightGauge(TR("ADX_LABEL", lang), vitals.adx, 75.0, adxColor)
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                // AUTHORITY (The Boss)
                CyberCard {
                    Column(Modifier.padding(12.dp)) {
                        FlightGauge(TR("AUTHORITY_LABEL", lang), vitals.confidence, 100.0, authColor, authLabel)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // 4. FLOATING P/L & TRADES
        val floatingPnL = vitals.equity - vitals.balance
        val pnlColor = if (floatingPnL >= 0) NeonGreen else NeonRed
        
        CyberCard {
            Row(Modifier.padding(20.dp).fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column {
                    Text(TR("FLOAT_PL", lang), color = TextDim, fontSize = 12.sp) // [UI FIX] 12sp
                    // [DYNAMIC CURRENCY]
                    val pnlStr = formatCurrency(floatingPnL, vitals.currency_symbol, vitals.currency)
                    val finalPnl = if (floatingPnL >= 0) "+$pnlStr" else pnlStr

                    Text(finalPnl, color = pnlColor, fontSize = 24.sp, fontWeight = FontWeight.Bold)

                    // [RESTORE EQUITY]
                    val eqStr = formatCurrency(vitals.equity, vitals.currency_symbol, vitals.currency)
                    Row {
                        Text("${TR("EQ_LABEL", lang)}: ", color = Color.White, fontSize = 12.sp)
                        Text(eqStr, color = NeonGreen, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(TR("ACTIVE_TRADES", lang), color = TextDim, fontSize = 12.sp) // [UI FIX] 12sp
                    Text("${vitals.active_trades}", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // [v2.09] GHOST CHART (Only show if active trade exists)
        if (chartCandles.isNotEmpty() && chartPrice > 0f) {
            CyberCard {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        "📊 LIVE CHART",
                        style = MaterialTheme.typography.labelSmall,
                        color = NeonGreen,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    GhostChart(
                        price = chartPrice,
                        candles = chartCandles,
                        lines = chartLines,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(20.dp))
        }

        // 5. GOD MODE TOGGLE (Safe Escape)
        val godModeColor = if (vitals.auto_god_mode) Gold else Color.Gray
        val godModeText = if (vitals.auto_god_mode) TR("GOD_MODE_AUTO", lang) else TR("GOD_MODE_OFF", lang)
        
        Button(
            onClick = { 
                // Toggle Logic: Send the OPPOSITE of current state
                val newState = !vitals.auto_god_mode 
                val payload = mapOf(
                    "command" to "UPDATE_CONFIG",
                    "payload" to mapOf("auto_god_mode" to newState),
                    "status" to "PENDING",
                    "timestamp" to System.currentTimeMillis()
                )
                cmdRef.push().setValue(payload)
            },
            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
            modifier = Modifier.fillMaxWidth().height(50.dp).border(1.dp, godModeColor, RoundedCornerShape(8.dp)),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text(godModeText, color = godModeColor, fontSize = 14.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(12.dp))

        // 6. PANIC BUTTON
        Button(
            onClick = { cmdRef.push().setValue(mapOf("action" to "CLOSE_ALL")) },
            colors = ButtonDefaults.buttonColors(containerColor = NeonRed),
            modifier = Modifier.fillMaxWidth().height(50.dp),
            shape = RoundedCornerShape(8.dp)
        ) {
            // [UI FIX] 90% White (0xE6 is approx 90% of 255)
            Text(TR("CLOSE_ALL", lang), color = Color(0xE6FFFFFF), fontSize = 14.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(12.dp))

        // 7. ENGINE CONTROL (Moved from Rhythm Tab)
        val isManual = vitals.mode_name == "MANUAL"
        val isPaused = vitals.mode_name == "PAUSE"
        
        val engineColor = when {
            isPaused -> Gold
            isManual -> NeonGreen
            else -> NeonRed
        }
        
        val engineText = when {
            isPaused -> "OVERRIDE (PAUSED)"
            isManual -> "TURN ON"
            else -> "TURN OFF"
        }

        Button(
            onClick = { 
                if (isManual || isPaused) {
                     // TURN ON (or OVERRIDE) -> Engage Auto-Scheduler
                     // If user hits Override, we can either go to Manual or Auto.
                     // Going to Auto (0000) seems best to "Resume" normal operations if ready.
                     // But user might want Manual. Let's send them to Auto-Scheduler, 
                     // because if they wanted Manual they are already effectively paused.
                     // Actually, user said "Adapt UI to show RESUME or OVERRIDE".
                     // Let's assume hitting it engages the Auto-Scheduler (Resume).
                     val pl = mapOf("recipe" to "0000: Auto-Scheduler (Sync)")
                     cmdRef.push().setValue(mapOf("payload" to pl))
                } else {
                     // TURN OFF -> Manual Mode
                     val pl = mapOf("update_config" to mapOf("MODE_NAME" to "MANUAL"))
                     cmdRef.push().setValue(mapOf("payload" to pl))
                }
            },
            colors = ButtonDefaults.buttonColors(containerColor = engineColor),
            modifier = Modifier.fillMaxWidth().height(50.dp),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text(engineText, color = Color.Black, fontSize = 14.sp, fontWeight = FontWeight.Bold)
        }
        
        // --- 8. GHOST CHART (v2.07 Real-Time) ---
        var chartData by remember { mutableStateOf<Map<String, Any>?>(null) }
        DisposableEffect(Unit) {
            val ref = database.getReference("users/$botId/live_chart")
            val listener = object : ValueEventListener {
                override fun onDataChange(s: DataSnapshot) {
                    chartData = s.value as? Map<String, Any>
                }
                override fun onCancelled(e: DatabaseError) {}
            }
            ref.addValueEventListener(listener)
            onDispose { ref.removeEventListener(listener) }
        }

        if (chartData != null) {
            Spacer(modifier = Modifier.height(16.dp))
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0A0A0A)),
                modifier = Modifier.fillMaxWidth().height(250.dp)
            ) {
                Column(Modifier.padding(8.dp)) {
                    Text("GHOST VISUALIZER", color = NeonGreen, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                    
                    // Parse Data
                    val price = (chartData!!["price"] as? Number)?.toFloat() ?: 0f
                    val cData = chartData!!["candle"] as? Map<String, Any>
                    val lData = chartData!!["lines"] as? Map<String, Any>
                    
                    // Construct formatted objects
                    // For now, simplify: we just pass the raw data or mock a single candle if list not sent?
                    // Let's assume the bot sends a list of history. If not, chart might be empty.
                    // To be safe, we'll pass an empty list if data is missing, so app doesn't crash.
                    val candles = mutableListOf<Candle>() 
                    // (Expand parsing logic here if needed, or update GhostChart to accept raw maps)
                    // For prototype, let's create a single candle from the 'candle' key just to show it flows.
                    if (cData != null) {
                         candles.add(Candle(
                             (cData["o"] as Number).toFloat(),
                             (cData["h"] as Number).toFloat(),
                             (cData["l"] as Number).toFloat(),
                             (cData["c"] as Number).toFloat(),
                             0L
                         ))
                    }
                    val tLines = if (lData != null) {
                        TradeLines(
                            (lData["entry"] as Number).toFloat(),
                            (lData["sl"] as Number).toFloat(),
                            (lData["tp"] as Number).toFloat()
                        )
                    } else null

                    GhostChart(price = price, candles = candles, lines = tLines, modifier = Modifier.fillMaxSize())
                }
            }
        } else {
             Spacer(modifier = Modifier.height(16.dp))
             Text("CHART OFFLINE (NO ACTIVE TRADE)", color = Color.Gray, fontSize = 10.sp, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
        }
        
        // --- 9. NEWS RADAR (v2.07) ---
        // (Simple text ticker for now)
        var news by remember { mutableStateOf<String?>(null) }
        DisposableEffect(Unit) {
            val ref = database.getReference("system/vitals/news")
            val listener = object : ValueEventListener {
                override fun onDataChange(s: DataSnapshot) {
                    val map = s.value as? Map<String, Any>
                    if (map != null) {
                        val event = map["next_event"] as? String
                        val mins = map["time_in_minutes"]
                        if (event != null) news = "⚠️ NEWS: $event in ${mins}m"
                    }
                }
                override fun onCancelled(e: DatabaseError) {}
            }
            ref.addValueEventListener(listener)
            onDispose { ref.removeEventListener(listener) }
        }
        
        if (news != null) {
             Spacer(modifier = Modifier.height(8.dp))
             Text(news!!, color = Gold, fontWeight = FontWeight.Bold, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
        }
    }
}

// --- TAB 2: NEURAL STREAM (LOGS) ---
@Composable
fun NeuralStream(database: com.google.firebase.database.FirebaseDatabase, botId: String, lang: String) {
    val logsRef = database.getReference("users/$botId/logs")
    val logs = remember { mutableStateListOf<LogEntry>() }

    DisposableEffect(botId) {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val newLogs = mutableListOf<LogEntry>()
                for (child in snapshot.children) {
                    try {
                        val msg = child.getValue(String::class.java) ?: ""
                        newLogs.add(LogEntry(child.key ?: "", msg))
                    } catch (e: Exception) {
                        // Skip malformed logs
                    }
                }
                logs.clear()
                logs.addAll(newLogs.reversed())
            }
            override fun onCancelled(error: DatabaseError) {}
        }
        logsRef.limitToLast(50).addValueEventListener(listener)
        onDispose { logsRef.removeEventListener(listener) }
    }

    Column(modifier = Modifier.fillMaxSize().padding(20.dp)) {
        Text(TR("NEURAL_STREAM", lang), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp)
        Text("ID: $botId", color = NeonGreen, fontSize = 10.sp)
        Spacer(modifier = Modifier.height(20.dp))

        LazyColumn {
            items(logs) { log ->
               Text(log.message, color = NeonGreen, fontSize = 12.sp, fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace)
               Divider(color = Color(0xFF222222), thickness = 1.dp)
            }
        }
    }
}

// --- TAB 3: DAILY HISTORY ---
@Composable
fun DailyHistory(database: com.google.firebase.database.FirebaseDatabase, botId: String, lang: String) {
    val historyRef = database.getReference("users/$botId/history")
    val trades = remember { mutableStateListOf<TradeRecord>() }
    var connectionState by remember { mutableStateOf("FETCHING...") }

    DisposableEffect(botId) {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                try {
                    val newTrades = mutableListOf<TradeRecord>()
                    for (child in snapshot.children) {
                        try {
                            val t = child.getValue(TradeRecord::class.java)
                            if (t != null) newTrades.add(t)
                        } catch (e: Exception) { }
                    }
                    trades.clear()
                    // Show latest first
                    trades.addAll(newTrades.reversed())
                    connectionState = "● SYNCED (${trades.size})"
                } catch (e: Exception) {
                    connectionState = "⚠️ ERROR"
                }
            }
            override fun onCancelled(error: DatabaseError) { connectionState = "⚠️ ERROR" }
        }
        historyRef.addValueEventListener(listener)
        onDispose { historyRef.removeEventListener(listener) }
    }

    Column(modifier = Modifier.fillMaxSize().padding(20.dp)) {
         Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text(TR("TODAYS_TRADES", lang), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp)
            Text(connectionState, color = NeonGreen, fontSize = 10.sp)
        }
        Text(TR("SESSION_LEDGER", lang), color = TextDim, fontSize = 12.sp)
        Spacer(modifier = Modifier.height(15.dp))

        if (trades.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(TR("NO_TRADES", lang), color = TextDim, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(trades) { trade ->
                    CyberCard {
                        Row(
                            modifier = Modifier.padding(12.dp).fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // LEft: Symbol & Time
                            Column {
                                Text(trade.symbol.uppercase(), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text(trade.time, color = TextDim, fontSize = 12.sp) // [UI FIX] 12sp
                            }
                            
                            // Center: Type
                            Text(
                                trade.type, 
                                color = if(trade.type == "BUY") NeonGreen else NeonRed, 
                                fontWeight = FontWeight.Bold, 
                                fontSize = 12.sp
                            )
                            
                            // Right: Profit
                            val pnlColor = if (trade.profit >= 0) NeonGreen else NeonRed
                            // [DYNAMIC CURRENCY]
                            val pnlStr = formatCurrency(trade.profit, trade.currency_symbol, trade.currency)
                            val finalPnl = if (trade.profit >= 0) "+$pnlStr" else pnlStr

                            Text(
                                finalPnl, 
                                color = pnlColor, 
                                fontWeight = FontWeight.Bold, 
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

// --- HELPERS ---
@Composable
fun CyberCard(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    Card(colors = CardDefaults.cardColors(containerColor = CardBg), shape = RoundedCornerShape(8.dp), modifier = modifier.border(1.dp, Color(0xFF222222), RoundedCornerShape(8.dp))) { content() }
}

fun safeDouble(snapshot: DataSnapshot, key: String): Double {
    val value = snapshot.child(key).value
    return when (value) {
        is Double -> value
        is Long -> value.toDouble()
        is String -> value.toDoubleOrNull() ?: 0.0
        else -> 0.0
    }
}

@Composable
fun FlightGauge(title: String, value: Double, max: Double, color: Color, label: String? = null) {
    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(title, color = TextDim, fontSize = 12.sp, fontWeight = FontWeight.Bold) // [UI FIX] 12sp
            Text(label ?: String.format("%.1f", value), color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.height(4.dp))
        LinearProgressIndicator(
            progress = (value / max).toFloat().coerceIn(0f, 1f),
            color = color,
            trackColor = BarBg,
            modifier = Modifier.fillMaxWidth().height(6.dp),
            strokeCap = androidx.compose.ui.graphics.StrokeCap.Round
        )
    }
}

// --- OTA UPDATE MANAGER ---
@Composable
fun CheckForUpdates(database: com.google.firebase.database.FirebaseDatabase, context: Context, lang: String) {
    var showDialog by remember { mutableStateOf(false) }
    var remoteVersion by remember { mutableStateOf(RemoteVersion()) }
    val currentVersionCode = try {
        context.packageManager.getPackageInfo(context.packageName, 0).versionCode
    } catch (e: Exception) { 1 }

    DisposableEffect(Unit) {
        val ref = database.getReference("system/version")
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val v = snapshot.getValue(RemoteVersion::class.java)
                if (v != null && v.code > currentVersionCode) {
                    remoteVersion = v
                    showDialog = true
                }
            }
            override fun onCancelled(error: DatabaseError) {}
        }
        ref.addValueEventListener(listener)
        onDispose { ref.removeEventListener(listener) }
    }

    if (showDialog) {
        // Resolve Localized Changelog (Use App Language)
        // Map Keys: "en", "th" (lowercase). App Lang: "EN", "TH" (uppercase).
        val lookupKey = lang.lowercase()
        val displayChangelog = if (remoteVersion.changelog_map.containsKey(lookupKey)) {
            remoteVersion.changelog_map[lookupKey] ?: remoteVersion.changelog
        } else {
            remoteVersion.changelog // Fallback to default
        }

        AlertDialog(
            onDismissRequest = { if (!remoteVersion.mandatory) showDialog = false },
            containerColor = CardBg,
            title = { Text("📡 LINK UPGRADE (${remoteVersion.name})", color = NeonGreen, fontWeight = FontWeight.Bold) },
            text = { 
                Column {
                    Text("New capabilities available. Initialize upgrade sequence?", color = Color.White)
                    if (displayChangelog.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Text("PATCH NOTES:", color = TextDim, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        Text(displayChangelog, color = Color.Gray, fontSize = 12.sp, fontFamily = FontFamily.Monospace)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { 
                        showDialog = false
                        downloadAndInstallUpdate(context, remoteVersion.url, remoteVersion.name) 
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NeonGreen)
                ) { Text("INITIALIZE", color = Color.Black, fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                if (!remoteVersion.mandatory) {
                    TextButton(onClick = { showDialog = false }) { Text("DEFER", color = TextDim) }
                }
            }
        )
    }
}

fun downloadAndInstallUpdate(context: Context, url: String, versionName: String) {
    // 1. Check Permission to Install Packages (Android 8+)
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        if (!context.packageManager.canRequestPackageInstalls()) {
            Toast.makeText(context, "⚠️ Permission Required: Allow Ghost to install updates.", Toast.LENGTH_LONG).show()
            val intent = Intent(android.provider.Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES)
            intent.data = Uri.parse("package:${context.packageName}")
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
            return
        }
    }

    try {
        val fileName = "ghost_update_${System.currentTimeMillis()}.apk"
        val request = DownloadManager.Request(Uri.parse(url))
            .setTitle("Ghost Upgrade $versionName")
            .setDescription("Downloading protocol patch...")
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            .setDestinationInExternalFilesDir(context, Environment.DIRECTORY_DOWNLOADS, fileName)
            .setMimeType("application/vnd.android.package-archive")

        val dm = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        val downloadId = dm.enqueue(request)

        Toast.makeText(context, "⬇️ DOWNLOADING UPDATE... (Stay here)", Toast.LENGTH_LONG).show()

        // 2. Register Receiver for Completion
        val onComplete = object : android.content.BroadcastReceiver() {
            override fun onReceive(ctxt: Context, intent: Intent) {
                 val id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
                 if (id == downloadId) {
                     installApk(ctxt, id, fileName)
                     context.unregisterReceiver(this) // Clean up
                 }
            }
        }
        ContextCompat.registerReceiver(
            context, 
            onComplete, 
            android.content.IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE), 
            ContextCompat.RECEIVER_EXPORTED
        )
        
    } catch (e: Exception) {
        Toast.makeText(context, "❌ Update Failure: ${e.message}", Toast.LENGTH_LONG).show()
    }
}

fun installApk(context: Context, downloadId: Long, fileName: String) {
    try {
        // We know where we put it: specific file in ExternalFilesDir/Download
        val file = File(context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), fileName)
        
        if (file.exists()) {
            val uri = FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
            
            val intent = Intent(Intent.ACTION_VIEW)
            intent.setDataAndType(uri, "application/vnd.android.package-archive")
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            
            Toast.makeText(context, "📦 Launching Installer...", Toast.LENGTH_SHORT).show()
            context.startActivity(intent)
        } else {
             Toast.makeText(context, "❌ File not found after download.", Toast.LENGTH_SHORT).show()
        }
    } catch (e: Exception) {
        Toast.makeText(context, "❌ Install Failure: ${e.message}", Toast.LENGTH_LONG).show()
    }
}

