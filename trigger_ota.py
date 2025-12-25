
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
        "code": 20,                 
        "name": "2.00",             
        "mandatory": True,          
        "changelog": "🔥 MAJOR UPDATE v2.00: Swarm Mode & Localization.\n- Added Swarm Toggle\n- Localized Changelogs Supported",
        "changelog_map": {
            "en": "🔥 MAJOR UPDATE v2.00\n- Swarm Mode Toggle Added\n- Full Localization Support\n- Performance Enhancements",
            "ru": "🔥 ОБНОВЛЕНИЕ v2.00\n- Добавлен режим Swarm\n- Поддержка локализации\n- Улучшение производительности",
            "es": "🔥 ACTUALIZACIÓN v2.00\n- Modo Enjambre Agregado\n- Soporte de Localización",
            "de": "🔥 UPDATE v2.00\n- Swarm-Modus hinzugefügt\n- Lokalisierungsunterstützung"
        },
        "url": "https://github.com/yothanan2/ghost_mobile/releases/download/v2.00/Ghost_v2.00_Release.apk"
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
