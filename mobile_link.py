import firebase_admin
from firebase_admin import credentials
from firebase_admin import db
import time
import threading
import random
import os

class GhostUplink:
    def __init__(self, service_account_path='serviceAccountKey.json', database_url=None, bot_id="GHOST_PRIME"):
        if not os.path.exists(service_account_path):
            raise FileNotFoundError(f"Missing {service_account_path}. Please download it from Firebase Console.")
        
        # Initialize Firebase App
        if not firebase_admin._apps:
            cred = credentials.Certificate(service_account_path)
            # Note: User must provide their database URL or we can try to infer/ask
            # For now, we assume the user will edit this or we default to a placeholder
            if not database_url:
                print("WARNING: No DATABASE_URL provided. Please set it in main().")
            
            # Ensure URL ends with /
            if database_url and not database_url.endswith('/'):
                database_url += '/'

            firebase_admin.initialize_app(cred, {
                'databaseURL': database_url
            })
        
        self.bot_id = bot_id
        # UPDATED PATHS for Multi-User Support
        self.vitals_ref = db.reference(f'users/{self.bot_id}/vitals')
        self.commands_ref = db.reference(f'users/{self.bot_id}/commands')
        self.running = True

    def broadcast_vitals(self, equity, balance, rsi, trend, active_trades):
        """Pushes current bot vitals to Firebase."""
        try:
            data = {
                'equity': equity,
                'balance': balance,
                'rsi': rsi,
                'trend': trend,
                'active_trades': active_trades,
                'timestamp': time.time()
            }
            self.vitals_ref.set(data)
            print(f"[UPLINK] Broadcast: Eq=${equity:.2f} Bal=${balance:.2f} RSI={rsi:.1f} Trend={trend} Act={active_trades}")
        except Exception as e:
            print(f"[UPLINK] Error broadcasting vitals: {e}")

    def listen_for_commands(self, callback):
        """Listens for commands from the Android app."""
        def listener(event):
            if event.data:
                # Handle both string "CLOSE_ALL" and dict {"action": "CLOSE_ALL"}
                cmd = event.data
                if isinstance(cmd, dict):
                     # If it's a push ID key -> value struct, take the last one or just values
                     # Simpler: just print raw for now or extract 'action' if possible
                     print(f"[UPLINK] Command Received (Raw): {cmd}")
                     if 'action' in cmd:
                         callback(cmd['action'])
                     else:
                         # Iterate values if it's a list/map of commands
                         for k, v in cmd.items():
                             if isinstance(v, dict) and 'action' in v:
                                 callback(v['action'])
                             elif v == "CLOSE_ALL":
                                 callback(v)
                else:
                    print(f"[UPLINK] Command Received: {cmd}")
                    callback(cmd)
                
                # Optional: Clear after processing
                # self.commands_ref.set({}) # Be careful clearing all

        self.commands_ref.listen(listener)
        print("[UPLINK] Listening for commands on 'ghost_cockpit/commands'...")

    def stop(self):
        self.running = False

# --- Mock Bot Loop for Testing ---
def mock_bot_loop():
    # REPLACE THIS URL with your actual Firebase Realtime Database URL
    DATABASE_URL = "https://ghost-app-2fff8-default-rtdb.europe-west1.firebasedatabase.app/" 
    
    print("--- Ghost Uplink Prototype (V2) ---")
    
    try:
        uplink = GhostUplink('serviceAccountKey.json', DATABASE_URL, bot_id="GHOST_PRIME")
        print(f"--- UPLINK ONLINE (ID: GHOST_PRIME) ---")
    except Exception as e:
        print(f"Initialization Failed: {e}")
        return

    def on_command(cmd):
        if cmd == "CLOSE_ALL":
            print("\n!!! PANIC PROTOCOL INITIATED !!!")
            print("!!! CLOSING ALL POSITIONS !!!\n")
        else:
            print(f"Unknown command: {cmd}")

    # Start Listener
    uplink.listen_for_commands(on_command)

    # Simulation Loop
    balance = 10000.0
    equity = 10000.0
    active_trades = 0
    trend = "SCANNING"
    
    try:
        while True:
            # Simulate changing market conditions
            rsi = random.uniform(30, 70)
            
            # Simulate a trade
            if random.random() > 0.8:
                active_trades = random.randint(0, 3)
            
            if active_trades > 0:
                pnl = random.uniform(-50, 150)
                equity = balance + pnl
                trend = "BULLISH" if rsi > 50 else "BEARISH"
            else:
                equity = balance
                trend = "SCANNING"

            uplink.broadcast_vitals(equity, balance, rsi, trend, active_trades)
            
            time.sleep(1) # Faster updates for fluid UI
    except KeyboardInterrupt:
        print("Stopping Uplink...")

if __name__ == "__main__":
    mock_bot_loop()
