
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
        "code": 29,                 
        "name": "2.09",             
        "mandatory": False,          
        "changelog": "📊🔊 v2.09 Feature Update\n- GHOST CHART: Live chart now shows during trades\n- SOUND TOGGLE: Control notification sounds in Settings",
        "changelog_map": {
            "en": "📊🔊 v2.09 Feature Update\n- GHOST CHART: Live chart now shows during trades\n- SOUND TOGGLE: Control notification sounds in Settings",
            "th": "📊🔊 อัปเดต v2.09\n- แชร์ตสด: แสดงกราฟเมื่อมีเทรด\n- ควบคุมเสียง: ปิด/เปิดเสียงแจ้งเตือนได้",
            "ru": "📊🔊 v2.09\n- ГРАФИК: Живой график в сделках\n- ЗВУК: Управление звуком",
            "es": "📊🔊 v2.09\n- GRÁFICO: Gráfico en vivo durante operaciones\n- SONIDO: Control de notificaciones"
        },
        "url": "https://github.com/yothanan2/ghost_mobile/releases/download/v2.09/Ghost_v2.09_Release.apk"
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
