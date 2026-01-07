
import firebase_admin
from firebase_admin import credentials, db
import sys

# DATABASE URL (Europe West 1)
DB_URL = "https://ghost-app-2fff8-default-rtdb.europe-west1.firebasedatabase.app/"

def trigger_update():
    print("🚀 OTA UPDATE TRIGGER: v4.02")
    
    # 1. Initialize Firebase
    try:
        cred = credentials.Certificate("serviceAccountKey.json")
        firebase_admin.initialize_app(cred, {'databaseURL': DB_URL})
        print("✅ Firebase Connected")
    except Exception as e:
        print(f"❌ Connection Error: {e}")
        return

    # 2. Define Update Payload
    update_data = {
        "code": 45,
        "name": "4.02",
        "url": "https://github.com/yothanan2/ghost_mobile/releases/download/v4.02/Ghost_v4.02_Release.apk",
        "changelog": "🔹 PROTCOL 4.02: TACTICAL P/L GAUGES\n- Fleet Rig now shows Active P/L per symbol\n- Real-time Profit/Loss Visualization\n- Removed redundant technical indicators",
        "mandatory": False,
        "changelog_map": {
            "en": "🔹 TACTICAL P/L GAUGES\n- Fleet Rig now shows real-time P/L for active trades.",
            "th": "🔹 ระบบเกจกำไร/ขาดทุน (P/L)\n- แสดงกำไร/ขาดทุนแบบเรียลไทม์ใน Fleet Rig รายคู่เงิน"
        },
        "timestamp": {".sv": "timestamp"}
    }

    # 3. Push to 'system/version' (Correct Path per MainActivity.kt)
    ref = db.reference('system/version')
    ref.set(update_data)
    
    print("\n📡 UPDATE SIGNAL SENT!")
    print(f"   Version: {update_data['name']} (Code {update_data['code']})")
    print(f"   URL: {update_data['url']}")
    print("   Users should see the popup immediately.")

if __name__ == "__main__":
    trigger_update()
