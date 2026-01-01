
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
        "code": 42,
        "name": "2.22",
        "url": "https://github.com/yothanan2/ghost_mobile/releases/download/v2.22/Ghost_v2.22_Release.apk",
        "changelog": "v2.22 GHOST SPIRIT PATCH:\n- UI Cleanup: God Mode Removed\n- Readability: White text on command buttons\n- Ghost Pulse: Haptic feedback on tap\n- Visual History: Win/Loss icons (✅/❌)\n- Screen Mastery: Keep Screen On + Brightness Slider\n- OLED Protector: Auto-dimming on idle\n- Full Thai Translation update",
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
