import firebase_admin
from firebase_admin import credentials
from firebase_admin import db
import time
import threading
import random
import os

# --- CONFIGURATION ---
# Set this to match the ID you use in the Android App Login Screen
BOT_ID = "27556817" 
# Your Firebase URL (Must match the one in the App)
DATABASE_URL = "https://ghost-app-2fff8-default-rtdb.europe-west1.firebasedatabase.app/"

class GhostUplink:
    def __init__(self, service_account_path='serviceAccountKey.json', database_url=DATABASE_URL, bot_id=BOT_ID):
        if not os.path.exists(service_account_path):
            raise FileNotFoundError(f"Missing {service_account_path}. Please download it from Firebase Console.")
        
        # Initialize Firebase App
        if not firebase_admin._apps:
            cred = credentials.Certificate(service_account_path)
            
            # Ensure URL ends with /
            if database_url and not database_url.endswith('/'):
                database_url += '/'

            firebase_admin.initialize_app(cred, {
                'databaseURL': database_url
            })
        
        self.bot_id = bot_id
        # V3 PATHS: users/{bot_id}/...
        self.vitals_ref = db.reference(f'users/{self.bot_id}/vitals')
        self.commands_ref = db.reference(f'users/{self.bot_id}/commands')
        self.running = True

    def broadcast_vitals(self, equity, balance, rsi, trend="SCANNING", active_trades=0, adx=0.0, atr=0.0, confidence=0.0, daily_profit=0.0, daily_target=60.0):
        """Pushes current bot vitals to Firebase (V3 Schema)."""
        try:
            data = {
                'equity': float(equity),
                'balance': float(balance),
                'rsi': float(rsi),
                'trend': str(trend),
                'active_trades': int(active_trades),
                # New Flight Deck Fields
                'adx': float(adx),
                'atr': float(atr),
                'confidence': float(confidence),
                'daily_profit': float(daily_profit),
                'daily_target': float(daily_target),
                'timestamp': time.time()
            }
            self.vitals_ref.set(data)
            print(f"[UPLINK] Vitals Updated: Eq=${equity:.2f} Profit=${daily_profit:.2f}")
        except Exception as e:
            print(f"[UPLINK] Error broadcasting vitals: {e}")

    def broadcast_trade(self, symbol, order_type, profit, trade_time=None):
        """Pushes a completed trade to the history log."""
        try:
            if trade_time is None:
                trade_time = time.strftime('%H:%M')
            
            payload = {
                'symbol': str(symbol),
                'type': str(order_type), # BUY or SELL
                'profit': float(profit),
                'time': str(trade_time),
                'timestamp': time.time()
            }
            # Push generates a unique ID, which we map to 'id' if needed or just iterate keys
            self.vitals_ref.parent.child('history').push(payload)
            print(f"[UPLINK] Trade Logged: {symbol} {order_type} ${profit:.2f}")
        except Exception as e:
            print(f"[UPLINK] Error logging trade: {e}")

    def listen_for_commands(self, callback):
        """Listens for commands from the Android app."""
        def listener(event):
            if event.data:
                # Handle both string "CLOSE_ALL" and dict {"action": "CLOSE_ALL"}
                cmd = event.data
                if isinstance(cmd, dict):
                     print(f"[UPLINK] Command Received (Raw): {cmd}")
                     if 'action' in cmd:
                         callback(cmd['action'])
                     else:
                         for k, v in cmd.items():
                             if isinstance(v, dict) and 'action' in v:
                                 callback(v['action'])
                             elif v == "CLOSE_ALL":
                                 callback(v)
                else:
                    print(f"[UPLINK] Command Received: {cmd}")
                    callback(cmd)
                
        self.commands_ref.listen(listener)
        print(f"[UPLINK] Listening for commands on 'users/{self.bot_id}/commands'...")

    def broadcast_log(self, message):
        """Pushes a log message to the Neural Stream."""
        try:
            # push() creates a unique timestamp-based key
            self.commands_ref.parent.child('logs').push(f"[{time.strftime('%Y-%m-%d %H:%M:%S')}] {message}")
            print(f"[UPLINK] Log sent: {message}")
        except Exception as e:
            print(f"[UPLINK] Error sending log: {e}")

    def stop(self):
        self.running = False


# --- MOCK LOOP (Delete this when integrating with real bot) ---
def mock_bot_loop():
    print(f"--- Ghost Uplink Prototype (V3) ---")
    print(f"--- ID: {BOT_ID} ---")
    
    try:
        uplink = GhostUplink() # Uses defaults from top of file
    except Exception as e:
        print(f"Initialization Failed: {e}")
        return

    def on_command(cmd):
        if cmd == "CLOSE_ALL":
            print("\n!!! PANIC PROTOCOL INITIATED !!!")
            print("!!! CLOSING ALL POSITIONS !!!\n")
            uplink.broadcast_log("⚠️ PANIC PROTOCOL EXECUTED")
        else:
            print(f"Unknown command: {cmd}")

    uplink.listen_for_commands(on_command)
    
    equity = 10000.0
    balance = 10000.0
    
    try:
        while True:
            # Simulate
            time.sleep(2)
            uplink.broadcast_log(f"Market Scan: RSI {random.randint(30,70)}")
            uplink.broadcast_vitals(equity, balance, 50, "SCANNING", 0)
    except KeyboardInterrupt:
        pass

if __name__ == "__main__":
    mock_bot_loop()
