import firebase_admin
from firebase_admin import credentials, db
import sys

# 1. Initialize Firebase
cred = credentials.Certificate("serviceAccountKey.json")
try:
    firebase_admin.get_app()
except ValueError:
    firebase_admin.initialize_app(cred, {
        "databaseURL": "https://ghost-app-2fff8-default-rtdb.europe-west1.firebasedatabase.app/"
    })

# 2. Define the Update Payload
version_data = {
    "code": 6,
    "name": "1.5",
    "url": "https://github.com/yothanan2/ghost_mobile/releases/download/v.1.5/app-debug.apk",
    "changelog": "- Added Visible Version Number to Header\n- System Stability Improvements",
    "mandatory": False
}

# 3. Push to 'system/version'
try:
    ref = db.reference("system/version")
    ref.set(version_data)
    print(f"✅ SUCCESSFULLY updated system/version to v{version_data['name']} (Code: {version_data['code']})")
    print(f"URL: {version_data['url']}")
except Exception as e:
    print(f"❌ Failed to update Firebase: {e}")
    sys.exit(1)
