
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
        "code": 32,                 
        "name": "2.12",             
        "mandatory": False,          
        "changelog": "v2.12:\n\n1. CONTINUOUS CHART: Watch the market 24/7, even without active trades!\n2. P/L FIX: Corrected Floating Profit calculation (stripped Bonus).\n3. PERFORMANCE: Removed legacy chart code for smoother UI.",
        "changelog_map": {
            "en": "v2.12:\n\n1. CONTINUOUS CHART: Watch the market 24/7, even without active trades!\n2. P/L FIX: Corrected Floating Profit calculation (stripped Bonus).\n3. PERFORMANCE: Removed legacy chart code for smoother UI.",
            "th": "v2.12:\n\n1. กราฟต่อเนื่อง: ดูตลาดได้ตลอด 24/7 แม้ไม่มีการเทรด!\n2. แก้ไข P/L: แก้ไขการคำนวณกำไรลอยตัว (ไม่รวมโบนัส).\n3. ประสิทธิภาพ: ลบรหัสกราฟเก่าเพื่อ UI ที่ราบรื่นขึ้น.",
            "ru": "v2.12:\n\n1. НЕПРЕРЫВНЫЙ ГРАФИК: Следите за рынком 24/7, даже без активных сделок!\n2. ИСПРАВЛЕНИЕ P/L: Исправлен расчет плавающей прибыли (без бонуса).\n3. ПРОИЗВОДИТЕЛЬНОСТЬ: Удален устаревший код графика для более плавной работы интерфейса.",
            "es": "v2.12:\n\n1. GRÁFICO CONTINUO: ¡Observe el mercado 24/7, incluso sin operaciones activas!\n2. CORRECCIÓN P/L: Cálculo de ganancias flotantes corregido (sin bonificación).\n3. RENDIMIENTO: Código de gráfico heredado eliminado para una interfaz de usuario más fluida."
        },
        "url": "https://github.com/yothanan2/ghost_mobile/releases/download/v2.12/Ghost_v2.12_Release.apk",
        "timestamp": {".sv": "timestamp"}
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
