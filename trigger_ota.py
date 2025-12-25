
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
        "code": 25,                 
        "name": "2.05",             
        "mandatory": False,          
        "changelog": "🚀 UPDATE v2.05: Dashboard Evolved.\n- Engine Controls moved to Home Screen.\n- Settings Menu streamlined.\n- UI Enhancements.",
        "changelog_map": {
            "en": "🚀 UPDATE v2.05: Dashboard Evolved.\n- Engine Controls moved to Home Screen.\n- Settings Menu streamlined.\n- UI Enhancements.",
            "th": "🚀 อัปเดต v2.05: ปรับปรุงล่าสุด\n- ย้ายปุ่มควบคุมไปหน้าหลัก\n- จัดระเบียบเมนูตั้งค่า",
            "ru": "🚀 ОБНОВЛЕНИЕ v2.05: Эволюция Панели\n- Управление движком на главном экране",
            "es": "🚀 ACTUALIZACIÓN v2.05: Panel Evolucionado\n- Controles del motor en la pantalla de inicio"
        },
        "url": "https://github.com/yothanan2/ghost_mobile/raw/master/Ghost_v2.05_Release.apk"
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
