
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
        "code": 26,                 
        "name": "2.06",             
        "mandatory": False,          
        "changelog": "👻 UPDATE v2.06: Ghost RC\n- Renamed to 'Ghost RC'.\n- Added Holiday/Pause Support.\n- Visual Tweaks.",
        "changelog_map": {
            "en": "👻 UPDATE v2.06: Ghost RC\n- Renamed to 'Ghost RC'.\n- Added Holiday/Pause Support.\n- Visual Tweaks.",
            "th": "👻 อัปเดต v2.06: Ghost RC\n- เปลี่ยนชื่อแอปเป็น Ghost\n- รองรับโหมดวันหยุด",
            "ru": "👻 ОБНОВЛЕНИЕ v2.06: Ghost RC\n- Новое имя: Ghost\n- Поддержка режима паузы",
            "es": "👻 ACTUALIZACIÓN v2.06: Ghost RC\n- Nuevo nombre: Ghost\n- Soporte para modo pausa"
        },
        "url": "https://github.com/yothanan2/ghost_mobile/raw/master/Ghost_v2.06_Release.apk"
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
