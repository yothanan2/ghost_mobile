
import firebase_admin
from firebase_admin import credentials, db
import sys

# DATABASE URL (Europe West 1)
DB_URL = "https://ghost-app-2fff8-default-rtdb.europe-west1.firebasedatabase.app/"

def trigger_update():
    print("🚀 OTA UPDATE TRIGGER: v4.02")
    
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
        "code": 46,
        "name": "4.03",
        "url": "https://github.com/yothanan2/ghost_mobile/releases/download/v4.03/Ghost_v4.03_Release.apk",
        "changelog": "🔹 PROTCOL 4.03: TACTICAL HEARTBEAT\n- Pulsing 'Glow' effect for active trades (Green/Red)\n- Removed redundant RSI/ADX indicators\n- Enhanced P/L visibility with T-PNL progress bars\n- Streamlined Idle states",
        "mandatory": False,
        "changelog_map": {
            "en": "🔹 TACTICAL HEARTBEAT\n- Active trades now pulse with glow based on P/L status.",
            "th": "🔹 ระบบ TACTICAL HEARTBEAT\n- เพิ่มเอฟเฟกต์ไฟกะพริบ (Glow) สำหรับคู่เงินที่ติดออเดอร์ (แดง/เขียว)\n- ตัดข้อมูลอินดิเคเตอร์ที่ไม่จำเป็นออกเพื่อความคล่องตัว"
        },
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
