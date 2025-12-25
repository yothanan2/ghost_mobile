
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
        "code": 21,                 
        "name": "2.01",             
        "mandatory": True,          
        "changelog": "🔥 UPDATE v2.01: Dynamic Strategy & Visual Sync.\n- Recipes Sync with Bot\n- In-App Strategy Editor\n- Settings Visual Feedback",
        "changelog_map": {
            "en": "🔥 UPDATE v2.01: Dynamic Strategy & Visual Sync.\n- Recipes Sync with Bot\n- In-App Strategy Editor\n- Settings Visual Feedback",
            "th": "🔥 อัปเดต v2.01: กลยุทธ์แบบไดนามิก & การซิงค์สถานะ\n- ซิงค์สูตรกับบอท\n- แก้ไขกลยุทธ์ในแอป\n- การแสดงผลการตั้งค่า",
            "ru": "🔥 ОБНОВЛЕНИЕ v2.01: Динамические Стратегии\n- Синхронизация рецептов\n- Редактор стратегий\n- Визуальная синхронизация",
            "es": "🔥 ACTUALIZACIÓN v2.01: Estrategia Dinámica\n- Sincronización de Recetas\n- Editor de Estrategia\n- Sincronización Visual"
        },
        "url": "https://github.com/yothanan2/ghost_mobile/releases/download/v2.01/Ghost_v2.01_Release.apk"
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
