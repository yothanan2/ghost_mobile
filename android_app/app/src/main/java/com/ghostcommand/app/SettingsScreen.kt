package com.ghostcommand.app

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
fun SettingsScreen(database: FirebaseDatabase, botId: String, lang: String, vitals: GhostVitals) {
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
            1 -> RiskVault(cmdRef, lang, vitals)
            2 -> SchedulerConfig(cmdRef, lang)
        }
    }
}

// ... SubTabBtn ... Use Reference

// --- TAB 2: RISK VAULT ---
@Composable
fun RiskVault(cmdRef: DatabaseReference, lang: String, vitals: GhostVitals) {
    var targetInput by remember { mutableStateOf("") }
    
    // Toggles
    CyberCard {
        Column(Modifier.padding(15.dp)) {
            Text("SAFETY OVERRIDES", color = NeonGreen, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(15.dp))
            
            RemoteToggleRow("🔥 FIREWALL", cmdRef, "use_firewall", vitals.use_firewall)
            Spacer(Modifier.height(10.dp))
            RemoteToggleRow("🐝 SWARM MODE", cmdRef, "swarm_mode", vitals.swarm_mode)
            Spacer(Modifier.height(10.dp))
            RemoteToggleRow("🐋 WHALE RADAR", cmdRef, "use_sr_filter", vitals.whale_tracker)
            Spacer(Modifier.height(10.dp))
            RemoteToggleRow("🔄 AUTO-REVERSAL", cmdRef, "use_adaptive_reversal", vitals.auto_rev)
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
            
            // Current Target Helper
            Spacer(Modifier.height(5.dp))
            Text("Current: $${vitals.daily_target}", color = TextDim, fontSize = 10.sp, modifier = Modifier.align(Alignment.End))
        }
    }
}

@Composable
fun RemoteToggleRow(label: String, cmdRef: DatabaseReference, key: String, isActive: Boolean) {
    // OPTIMISTIC UI: Initialize with server state, but allow immediate local override
    var localState by remember(isActive) { mutableStateOf(isActive) }
    
    val onColor = if (localState) Color(0xFF112211) else Color(0xFF111111)
    val onTxt = if (localState) NeonGreen else Color.Gray
    
    val offColor = if (!localState) Color(0xFF221111) else Color(0xFF111111)
    val offTxt = if (!localState) NeonRed else Color.Gray

    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Text(label, color = Color.White, fontSize = 14.sp)
        Row {
            Button(
                onClick = { 
                    localState = true
                    sendRemoteCmd(cmdRef, mapOf(key to true)) 
                },
                colors = ButtonDefaults.buttonColors(containerColor = onColor),
                modifier = Modifier.height(30.dp).border(1.dp, if(localState) NeonGreen else Color.Transparent, RoundedCornerShape(50)),
                contentPadding = PaddingValues(0.dp)
            ) { Text("ON", color = onTxt, fontSize = 10.sp) }
            
            Spacer(Modifier.width(5.dp))
            
            Button(
                onClick = { 
                    localState = false
                    sendRemoteCmd(cmdRef, mapOf(key to false)) 
                },
                colors = ButtonDefaults.buttonColors(containerColor = offColor),
                modifier = Modifier.height(30.dp).border(1.dp, if(!localState) NeonRed else Color.Transparent, RoundedCornerShape(50)),
                contentPadding = PaddingValues(0.dp)
            ) { Text("OFF", color = offTxt, fontSize = 10.sp) }
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

@Composable
fun StrategyConfig(cmdRef: DatabaseReference, lang: String) {
    var expanded by remember { mutableStateOf(false) }
    var selectedRecipe by remember { mutableStateOf("Select Strategy...") }
    var recipesMap by remember { mutableStateOf<Map<String, Any>>(emptyMap()) }
    var isLoading by remember { mutableStateOf(true) }
    
    // Dynamic Form State
    val editState = remember(selectedRecipe) { mutableStateMapOf<String, Any>() }

    // Fetch Recipes
    DisposableEffect(Unit) {
        val ref = cmdRef.parent?.child("system/recipes")
        val listener = object : com.google.firebase.database.ValueEventListener {
            override fun onDataChange(s: com.google.firebase.database.DataSnapshot) {
                val data = s.value as? Map<String, Any> ?: emptyMap()
                recipesMap = data
                isLoading = false
            }
            override fun onCancelled(e: com.google.firebase.database.DatabaseError) {}
        }
        ref?.addValueEventListener(listener)
        onDispose { ref?.removeEventListener(listener) }
    }
    
    // Using simple Column with Scroll since we are inside a Card usually
    CyberCard {
        Column(Modifier.padding(15.dp).verticalScroll(rememberScrollState())) {
            Text("STRATEGY ENGINE (DYNAMIC)", color = NeonGreen, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(10.dp))
            Text(if (isLoading) "Downloading Manifest..." else "${recipesMap.size} Recipes Available", color = TextDim, fontSize = 12.sp)
            Spacer(Modifier.height(15.dp))
            
            // DROPDOWN MENU
            Box(Modifier.fillMaxWidth().wrapContentSize(Alignment.TopStart)) {
                Button(
                    onClick = { expanded = true },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF222222)),
                    modifier = Modifier.fillMaxWidth().height(50.dp).border(1.dp, Color.Gray, RoundedCornerShape(4.dp))
                ) {
                    Text(selectedRecipe, color = Color.White)
                }
                
                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                    modifier = Modifier.background(Color(0xFF222222))
                ) {
                    recipesMap.keys.sorted().forEach { name ->
                        DropdownMenuItem(
                            text = { Text(name, color = Color.White) },
                            onClick = { 
                                selectedRecipe = name
                                // Initialize Edit State
                                editState.clear()
                                val data = recipesMap[name] as? Map<String, Any> ?: emptyMap()
                                data.forEach { (k, v) -> editState[k] = v }
                                expanded = false 
                            }
                        )
                    }
                }
            }
            
            Spacer(Modifier.height(15.dp))
            
            // ACTIONS
            Button(
                onClick = { 
                    if (recipesMap.containsKey(selectedRecipe)) {
                        sendRemoteCmd(cmdRef, mapOf("recipe" to selectedRecipe))
                    }
                },
                enabled = recipesMap.containsKey(selectedRecipe),
                colors = ButtonDefaults.buttonColors(containerColor = Gold),
                modifier = Modifier.fillMaxWidth().height(50.dp)
            ) {
                Text("INJECT & RUN", color = Color.Black, fontWeight = FontWeight.Bold)
            }
            
            // DYNAMIC EDITOR
            if (recipesMap.containsKey(selectedRecipe)) {
                Spacer(Modifier.height(20.dp))
                Divider(color = Color.Gray, thickness = 1.dp)
                Spacer(Modifier.height(10.dp))
                Text("FULL SCALE PARAMETERS", color = NeonBlue, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(10.dp))
                
                val rData = recipesMap[selectedRecipe] as? Map<String, Any> ?: emptyMap()
                
                // Initialize editState if empty (first load fallback)
                if (editState.isEmpty() && rData.isNotEmpty()) {
                    rData.forEach { (k, v) -> editState[k] = v }
                }

                // Filter System Keys
                val systemKeys = listOf("description", "locked", "MAGIC_NUMBER", "TIMEFRAME", "TIMEFRAME_NAME", "SYMBOL", "MODE_TYPE", "GRAVITY_TIMEFRAME")
                
                // Sort keys: Risk items first, then others
                val sortedKeys = rData.keys.filter { !systemKeys.contains(it) }.sortedBy { key ->
                   when {
                       key.contains("RISK") -> "00_$key"
                       key.contains("LOT") -> "01_$key"
                       key.contains("STOP") -> "02_$key"
                       key.contains("TAKE") -> "03_$key"
                       else -> "99_$key"
                   }
                }

                sortedKeys.forEach { key ->
                    val value = editState[key]
                    
                    Column(Modifier.padding(vertical = 5.dp)) {
                        
                        if (value is Boolean) {
                            // SWITCH
                            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(key.replace("_", " "), color = Color.Gray, fontSize = 12.sp)
                                Switch(
                                    checked = value,
                                    onCheckedChange = { editState[key] = it },
                                    colors = SwitchDefaults.colors(checkedThumbColor = NeonGreen, checkedTrackColor = Color(0xFF112211))
                                )
                            }
                        } else {
                            // TEXT / NUMBER FIELD
                            var txtVal by remember(key, value) { mutableStateOf(value.toString()) }
                            OutlinedTextField(
                                value = txtVal,
                                onValueChange = { 
                                    txtVal = it 
                                    // Try to parse back to number if original was number
                                    val orig = rData[key]
                                    if (orig is Number) {
                                        val d = it.toDoubleOrNull()
                                        if (d != null) editState[key] = d
                                    } else {
                                        editState[key] = it
                                    }
                                },
                                label = { Text(key.replace("_", " "), fontSize = 10.sp) },
                                modifier = Modifier.fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = Color.White, 
                                    unfocusedTextColor = Color.White,
                                    focusedBorderColor = Color.Gray,
                                    unfocusedBorderColor = Color(0xFF333333)
                                )
                            )
                        }
                    }
                }
                
                Spacer(Modifier.height(15.dp))
                Button(
                    onClick = {
                        val payload = mapOf("save_recipe" to mapOf("name" to selectedRecipe, "settings" to editState.toMap()))
                        sendRemoteCmd(cmdRef, payload)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF333333)),
                    modifier = Modifier.fillMaxWidth().height(40.dp)
                ) {
                    Text("SAVE CONFIGURATION", color = Color.White)
                }
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
                         // [RHYTHM] Inject the Auto-Pilot Recipe
                         val pl = mapOf("recipe" to "0000: Auto-Scheduler (Sync)")
                         sendRemoteCmd(cmdRef, pl)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NeonBlue),
                    modifier = Modifier.weight(1f)
                ) { Text("ENGAGE AUTO", color = Color.Black, fontWeight = FontWeight.Bold) }
                
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
