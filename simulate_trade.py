
import firebase_admin
from firebase_admin import credentials, db
import time
import math
import random
import sys

# --- CONFIG ---
# --- CONFIG ---
SERVICE_ACCOUNT_FILE = "g:/My Drive/Ghost_Bot_Sync/firebase_service_account.json"
DATABASE_URL = "https://ghost-app-2fff8-default-rtdb.europe-west1.firebasedatabase.app/"

def init_firebase():
    if not firebase_admin._apps:
        cred = credentials.Certificate(SERVICE_ACCOUNT_FILE)
        firebase_admin.initialize_app(cred, {
            'databaseURL': DATABASE_URL
        })
    print("🔥 Firebase Connected!")

def simulate_market(bot_id):
    print(f"🚀 Launching Simulation for Bot ID: {bot_id}")
    print("Press CTRL+C to stop.")

    ref_chart = db.reference(f"users/{bot_id}/live_chart")
    ref_cmd = db.reference(f"users/{bot_id}/commands")

    # Clear previous commands
    ref_cmd.delete()

    # Base Price (Gold-ish)
    base_price = 2500.0
    
    # Trade Lines
    entry = base_price
    tp = base_price + 5.0
    sl = base_price - 3.0
    
    # Push initial Trade Lines
    print(f"✅ Trade Active: Entry={entry}, TP={tp}, SL={sl}")
    lines_data = {
        "entry": entry,
        "tp": tp,
        "sl": sl
    }

    # Simulation Loop
    step = 0
    while True:
        # Check for commands (POLLING)
        cmds = ref_cmd.get()
        if cmds:
            print(f"🛑 COMMANDS DETECTED: {cmds}")
            # Identify if any command is CLOSE_ALL
            should_close = False
            
            if isinstance(cmds, dict):
                for key, val in cmds.items():
                    if isinstance(val, dict) and val.get("action") == "CLOSE_ALL":
                        should_close = True
                        break
            
            if should_close:
                print("✅ APP REQUESTED CLOSE! Clearing lines...")
                ref_chart.child("lines").delete()
                ref_cmd.delete() # Clear commands
                print("🎉 TRADE CLOSED.")
                sys.exit(0)

        # Oscillate price to hit TP and SL
        # Sine wave to go up to TP, down to SL, repeat
        oscillation = math.sin(step * 0.1) * 6.0 # +/- 6.0 range (covers TP+5 and SL-3)
        current_price = base_price + oscillation

        # Generate Fake Candles (just for visual filler)
        candles = []
        for i in range(10):
            c_base = base_price + (i * 0.5)
            candles.append({
                "o": c_base,
                "h": c_base + 1.0,
                "l": c_base - 1.0,
                "c": c_base + 0.5,
                "ts": int(time.time()) - (10-i)*60
            })
        
        # Determine Status
        status = "NEUTRAL"
        if current_price >= tp: status = "WIN (GREEN BLINK)"
        if current_price <= sl: status = "LOSS (RED BLINK)"

        print(f"Time: {step} | Price: {current_price:.2f} | Status: {status}")

        payload = {
            "price": current_price,
            "candles": candles,
            "lines": lines_data, # Re-send lines to keep them active until close
            "timestamp": {".sv": "timestamp"}
        }

        try:
             ref_chart.update(payload)
        except Exception as e:
            pass # Ignore write errors on exit

        time.sleep(1.0) # 1 sec update rate
        step += 1

if __name__ == "__main__":
    init_firebase()
    print("\n--- GHOST SIMULATOR ---")
    
    if len(sys.argv) > 1:
        bot_id = sys.argv[1]
        print(f"🆔 Bot ID from Args: {bot_id}")
    else:
        bot_id = input("Enter your MT5 ID (Bot ID): ").strip()
        
    if bot_id:
        simulate_market(bot_id)
    else:
        print("Invalid ID.")
