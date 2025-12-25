# 📦 Ghost Command: Firebase Setup Checklist

## 1. Create Project
- [ ] Go to [console.firebase.google.com](https://console.firebase.google.com/)
- [ ] Create new project: **"GhostCommand"** (Disable Google Analytics for speed).

## 2. Realtime Database
- [ ] In Console, go to **Build > Realtime Database**.
- [ ] Click **Create Database** (Select closest location, e.g., US or Belgium).
- [ ] **Security Rules:** Start in **Test Mode** (allow read/write for 30 days) OR use:
  ```json
  {
    "rules": {
      ".read": true,
      ".write": true
    }
  }