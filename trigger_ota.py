
import firebase_admin
from firebase_admin import credentials, db
import sys

# DATABASE URL (Europe West 1)
DB_URL = "https://ghost-app-2fff8-default-rtdb.europe-west1.firebasedatabase.app/"

def trigger_update():
    print("🚀 OTA UPDATE TRIGGER: v1.6")
    
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
        "code": 40,
        "name": "2.20",
        "url": "https://github.com/yothanan2/ghost_mobile/releases/download/v2.20/Ghost_v2.20_Release.apk",
        "changelog": "v2.20 CRITICAL FIX:\n\n1. MISSING BALANCE: Fixed critical path error causing Balance/Equity to show $0.00.\n2. UNIFIED DATA: Main Header and Dashboard now read from the same live source.",
        "mandatory": False,
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
