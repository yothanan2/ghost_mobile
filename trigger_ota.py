
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
    version_data = {
        "code": 31,                 
        "name": "2.11",             
        "mandatory": False,          
        "changelog": "🚫🛡️ v2.11 Security Update\n- GHOST PIN REMOVED: Simplified login process\n- Only MT5 ID required for connection",
        "changelog_map": {
            "en": "🚫🛡️ v2.11 Security Update\n- PIN REMOVED: Simplified login\n- Only MT5 ID required",
            "th": "🚫🛡️ อัปเดต v2.11\n- ลบ PIN: เข้าสู่ระบบง่ายขึ้น\n- ใช้แค่ MT5 ID เท่านั้น",
            "ru": "🚫🛡️ v2.11\n- PIN УДАЛЕН: Живой вход\n- Только MT5 ID",
            "es": "🚫🛡️ v2.11\n- PIN ELIMINADO: Inicio simplificado\n- Solo requiere MT5 ID"
        },
        "url": "https://github.com/yothanan2/ghost_mobile/releases/download/v2.11/Ghost_v2.11_Release.apk"
    }

    # 3. Push to 'system/version' (Correct Path per MainActivity.kt)
    ref = db.reference('system/version')
    ref.set(version_data)
    
    print("\n📡 UPDATE SIGNAL SENT!")
    print(f"   Version: {version_data['name']} (Code {version_data['code']})")
    print(f"   URL: {version_data['url']}")
    print("   Users should see the popup immediately.")

if __name__ == "__main__":
    trigger_update()
