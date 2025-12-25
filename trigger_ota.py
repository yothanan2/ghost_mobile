
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
        "code": 28,                 
        "name": "2.08",             
        "mandatory": True,          
        "changelog": "🛡️ SECURITY UPDATE v2.08: Zero Trust\n- GHOST PIN: Login Authentication.\n- BIOMETRIC LOCK: App-Level Protection.\n- TERMINAL: Renamed from Sys Logs.",
        "changelog_map": {
            "en": "🛡️ SECURITY UPDATE v2.08: Zero Trust\n- GHOST PIN: Login Authentication.\n- BIOMETRIC LOCK: App-Level Protection.\n- TERMINAL: Renamed from Sys Logs.",
            "th": "🛡️ อัปเดตความปลอดภัย v2.08\n- รหัสผี: ระบบยืนยันตัวตน\n- ล็อกด้วยลายนิ้วมือ\n- เปลี่ยนชื่อ Sys Logs เป็น Terminal",
            "ru": "🛡️ БЕЗОПАСНОСТЬ v2.08\n- GHOST PIN: Авторизация\n- БИОМЕТРИЯ: Защита приложения",
            "es": "🛡️ SEGURIDAD v2.08\n- PIN FANTASMA: Autenticación\n- BLOQUEO BIOMÉTRICO: Protección de App"
        },
        # USE RELEASES URL (STABLE) INSTEAD OF RAW
        "url": "https://github.com/yothanan2/ghost_mobile/releases/download/v2.08/Ghost_v2.08_Release.apk"
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
