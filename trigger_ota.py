
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
        "code": 27,                 
        "name": "2.07",             
        "mandatory": False,          
        "changelog": "📈 UPDATE v2.07: Ghost Visualizer\n- Ghost Chart: Real-time Price & Lines.\n- News Radar: Economic Event Warnings.\n- Performance Optimizations.",
        "changelog_map": {
            "en": "📈 UPDATE v2.07: Ghost Visualizer\n- Ghost Chart: Real-time Price & Lines.\n- News Radar: Economic Event Warnings.\n- Performance Optimizations.",
            "th": "📈 อัปเดต v2.07: กราฟเรียลไทม์\n- แสดงกราฟราคาและเส้น Entry/SL/TP\n- แจ้งเตือนข่าวเศรษฐกิจ",
            "ru": "📈 ОБНОВЛЕНИЕ v2.07: Визуализация\n- Живой график и новости",
            "es": "📈 ACTUALIZACIÓN v2.07: Visualizador\n- Gráfico en tiempo real y Noticias"
        },
        # USE RELEASES URL (STABLE) INSTEAD OF RAW
        "url": "https://github.com/yothanan2/ghost_mobile/releases/download/v2.07/Ghost_v2.07_Release.apk"
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
