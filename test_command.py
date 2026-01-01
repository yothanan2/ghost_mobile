
import firebase_admin
from firebase_admin import credentials, db

# --- CONFIG ---
SERVICE_ACCOUNT_FILE = "g:/My Drive/Ghost_Bot_Sync/firebase_service_account.json"
DATABASE_URL = "https://ghost-app-2fff8-default-rtdb.europe-west1.firebasedatabase.app/"
BOT_ID = "314876338"

def trigger_close():
    if not firebase_admin._apps:
        cred = credentials.Certificate(SERVICE_ACCOUNT_FILE)
        firebase_admin.initialize_app(cred, {'databaseURL': DATABASE_URL})

    ref_cmd = db.reference(f"users/{BOT_ID}/commands")
    print(f"🚀 Pushing CLOSE_ALL to {ref_cmd.path}")
    
    ref_cmd.push().set({"action": "CLOSE_ALL"})
    print("✅ Command Pushed.")

if __name__ == "__main__":
    trigger_close()
