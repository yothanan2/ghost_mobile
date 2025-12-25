
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
        "code": 24,                 
        "name": "2.04",             
        "mandatory": False,          
        "changelog": "🔧 HOTFIX v2.04: Notification Label Fix.\n- Fixed 'v1.4' showing in download manager.\n- Future updates will show correct version.",
        "changelog_map": {
            "en": "🔧 HOTFIX v2.04: Notification Label Fix.\n- Fixed 'v1.4' showing in download manager.\n- Future updates will show correct version.",
            "th": "🔧 แก้ไข v2.04: แก้ชื่อการแจ้งเตือน\n- แก้ไขที่แสดงผิดเป็น 'v1.4'",
            "ru": "🔧 ИСПРАВЛЕНИЕ v2.04: Исправление ярлыка\n- Исправлено отображение 'v1.4'",
            "es": "🔧 CORRECCIÓN v2.04: Etiqueta de Notificación\n- Se corrigió 'v1.4' en la descarga"
        },
        "url": "https://github.com/yothanan2/ghost_mobile/raw/master/Ghost_v2.04_Release.apk"
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
