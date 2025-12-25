package com.ghostcommand.app

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

@Composable
fun SettingsScreen(database: FirebaseDatabase, botId: String, lang: String) {
    val cmdRef = database.getReference("users/$botId/commands")
    var selectedTab by remember { mutableStateOf(0) } // 0=STRATEGY, 1=RISK, 2=SCHEDULER

    Column(modifier = Modifier.fillMaxSize().padding(15.dp)) {
        
        Text("COMMAND PROTOCOLS", color = Color.Gray, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(10.dp))

        // --- SUB-TABS ---
        Row(modifier = Modifier.fillMaxWidth().height(40.dp)) {
            SubTabBtn("STRATEGY", selectedTab == 0) { selectedTab = 0 }
            Spacer(Modifier.width(8.dp))
            SubTabBtn("RISK VAULT", selectedTab == 1) { selectedTab = 1 }
            Spacer(Modifier.width(8.dp))
            SubTabBtn("RHYTHM", selectedTab == 2) { selectedTab = 2 }
        }
        
        Spacer(Modifier.height(20.dp))

        // --- CONTENT AREA ---
        when(selectedTab) {
            0 -> StrategyConfig(cmdRef, lang)
            1 -> RiskVault(cmdRef, lang)
            2 -> SchedulerConfig(cmdRef, lang)
        }
    }
}

@Composable
fun SubTabBtn(text: String, selected: Boolean, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (selected) NeonGreen else Color(0xFF222222)
        ),
        shape = RoundedCornerShape(50),
        contentPadding = PaddingValues(horizontal = 15.dp, vertical = 0.dp),
        modifier = Modifier.wrapContentWidth()
    ) {
        Text(text, color = if (selected) Color.Black else Color.Gray, fontSize = 11.sp, fontWeight = FontWeight.Bold)
    }
}

// --- TAB 1: STRATEGY (RECIPES) ---
@Composable
fun StrategyConfig(cmdRef: DatabaseReference, lang: String) {
    var recipeInput by remember { mutableStateOf("") }
    
    CyberCard {
        Column(Modifier.padding(15.dp)) {
            Text("ACTIVE RECIPE INJECTION", color = NeonGreen, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(10.dp))
            Text("Enter the exact recipe ID to hot-swap the strategy engine.", color = TextDim, fontSize = 12.sp)
            Spacer(Modifier.height(15.dp))
            
            OutlinedTextField(
                value = recipeInput,
                onValueChange = { recipeInput = it },
                label = { Text("Recipe ID (e.g. SCALPER_V2)", color = Color.Gray) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = NeonGreen,
                    unfocusedBorderColor = Color.Gray,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                ),
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(Modifier.height(15.dp))
            
            Button(
                onClick = { 
                    if (recipeInput.isNotEmpty()) {
                        sendRemoteCmd(cmdRef, mapOf("recipe" to recipeInput))
                        recipeInput = "" // Clear
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Gold),
                modifier = Modifier.fillMaxWidth().height(50.dp)
            ) {
                Text("INJECT RECIPE", color = Color.Black, fontWeight = FontWeight.Bold)
            }
        }
    }
}

// --- TAB 2: RISK VAULT ---
@Composable
fun RiskVault(cmdRef: DatabaseReference, lang: String) {
    var targetInput by remember { mutableStateOf("") }
    var firewallState by remember { mutableStateOf(false) } // This should be synced really, but stateless for now
    
    // Toggles
    CyberCard {
        Column(Modifier.padding(15.dp)) {
            Text("SAFETY OVERRIDES", color = NeonGreen, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(15.dp))
            
            RemoteToggleRow("🔥 FIREWALL", cmdRef, "use_firewall")
            Spacer(Modifier.height(10.dp))
            RemoteToggleRow("🐝 SWARM MODE", cmdRef, "swarm_mode")
            Spacer(Modifier.height(10.dp))
            RemoteToggleRow("🐋 WHALE RADAR", cmdRef, "use_sr_filter")
            Spacer(Modifier.height(10.dp))
            RemoteToggleRow("🔄 AUTO-REVERSAL", cmdRef, "use_adaptive_reversal")
        }
    }
    
    Spacer(Modifier.height(20.dp))
    
    // Daily Target
    CyberCard {
        Column(Modifier.padding(15.dp)) {
            Text("DAILY PROFIT TARGET", color = Gold, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(10.dp))
            
            OutlinedTextField(
                value = targetInput,
                onValueChange = { targetInput = it },
                label = { Text("Target Amount ($)", color = Color.Gray) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Gold,
                    unfocusedBorderColor = Color.Gray,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                ),
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(Modifier.height(15.dp))
            Button(
                onClick = { 
                    if (targetInput.isNotEmpty()) {
                        sendRemoteCmd(cmdRef, mapOf("daily_target" to targetInput.toFloatOrNull()))
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = NeonGreen),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("UPDATE TARGET", color = Color.Black, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun RemoteToggleRow(label: String, cmdRef: DatabaseReference, key: String) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Text(label, color = Color.White, fontSize = 14.sp)
        Row {
            Button(
                onClick = { sendRemoteCmd(cmdRef, mapOf(key to true)) },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF112211)),
                modifier = Modifier.height(30.dp),
                contentPadding = PaddingValues(0.dp)
            ) { Text("ON", color = NeonGreen, fontSize = 10.sp) }
            
            Spacer(Modifier.width(5.dp))
            
            Button(
                onClick = { sendRemoteCmd(cmdRef, mapOf(key to false)) },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF221111)),
                modifier = Modifier.height(30.dp),
                contentPadding = PaddingValues(0.dp)
            ) { Text("OFF", color = NeonRed, fontSize = 10.sp) }
        }
    }
}


// --- TAB 3: SCHEDULER ---
@Composable
fun SchedulerConfig(cmdRef: DatabaseReference, lang: String) {
    CyberCard {
        Column(Modifier.padding(15.dp)) {
            Text("BATTLE RHYTHM (SCHEDULER)", color = NeonBlue, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(10.dp))
            Text("Enable or Disable the Auto-Scheduler engine.", color = TextDim, fontSize = 12.sp)
            Spacer(Modifier.height(20.dp))
            
            // Re-using the toggle row logic logic but custom for scheduler mode concept
            // Assuming we map Scheduler "ON" to MODE_NAME="AUTO-SCHEDULER" or similar
            // But for now, we'll just use a generic config key if we had one.
            // Let's use deep config update for MODE_NAME
            
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Button(
                    onClick = { 
                         // Deep Config Update to set Mode
                         val pl = mapOf("update_config" to mapOf("MODE_NAME" to "AUTO-SCHEDULER"))
                         sendRemoteCmd(cmdRef, pl)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NeonBlue),
                    modifier = Modifier.weight(1f)
                ) { Text("ENABLE AUTO", color = Color.Black) }
                
                Spacer(Modifier.width(10.dp))
                
                Button(
                    onClick = { 
                         // Deep Config Update to set Mode to Manual
                         val pl = mapOf("update_config" to mapOf("MODE_NAME" to "MANUAL"))
                         sendRemoteCmd(cmdRef, pl)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF333333)),
                    modifier = Modifier.weight(1f)
                ) { Text("MANUAL MODE", color = Color.White) }
            }
            
            Spacer(Modifier.height(30.dp))
            Text("SYSTEM OVERRIDES", color = NeonRed, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(10.dp))
            
            Button(
                onClick = { sendRemoteCmd(cmdRef, mapOf("action" to "RESTART")) },
                colors = ButtonDefaults.buttonColors(containerColor = NeonRed),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("⚠️ FORCE REBOOT SYSTEM", color = Color.White, fontWeight = FontWeight.Bold)
            }
        }
    }
}

// --- HELPER COMPONENT IF NOT ACCESSIBLE ---
// Just in case CyberCard isn't public, we'll assume it is. 
// If it fails to compile, I'll copy it here.

fun sendRemoteCmd(ref: DatabaseReference, payload: Map<String, Any?>) {
    ref.setValue(mapOf("payload" to payload))
}
