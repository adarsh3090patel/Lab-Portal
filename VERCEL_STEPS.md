# 🚀 Vercel par Backend Host Karne ka Step-by-Step Guide

Vercel par host karne ke baad aapko **kabhi bhi koi IP address daalne ki zaroorat nahi padegi**. App seedha internet se chalega!

---

## 📋 Step 1: Apne Project ko GitHub par Daalein
Agar aapka project pehle se GitHub par hai toh **Step 2** par jayein. Agar nahi hai:

1. [github.com](https://github.com) par login karein aur ek **New Repository** banayein (jaise: `LabPortal`).
2. Apne laptop me project root folder me ye commands chalayein:
   ```bash
   git add .
   git commit -m "MMYRV Lab Portal full setup"
   git branch -M main
   git remote add origin https://github.com/<AAPKA_GITHUB_USERNAME>/LabPortal.git
   git push -u origin main
   ```

---

## 📋 Step 2: Vercel par Import Karein

1. Browser me [vercel.com](https://vercel.com) kholein aur apne **GitHub account se Login** karein.
2. Dashboard par blue button **"Add New..." ➔ "Project"** par click karein.
3. Apni **`LabPortal`** repository ke samne **"Import"** button dabayein.

---

## 📋 Step 3: Vercel Settings (⚠️ Sabse Zaroori Step)

Project configuration screen par sirf **2 cheezein** karni hain:

### A. Root Directory Set Karein:
* Screen par **"Root Directory"** ke aage **"Edit"** button dabayein.
* Wahan list me se **`backend`** select karein aur **Continue** dabayein.
  *(Ye zaroori hai taaki Vercel sirf Python backend ko host kare).*

### B. Environment Variables Add Karein:
Neeche **"Environment Variables"** section kholein aur ye 2 variables add karein:

1. **Key:** `MONGODB_URI`
   **Value:** `mongodb+srv://labadmin:Adarsh%40123@cluster0.plvgasl.mongodb.net/labportal?retryWrites=true&w=majority`
   *(Add button dabayein)*

2. **Key:** `DB_NAME`
   **Value:** `labportal`
   *(Add button dabayein)*

---

## 📋 Step 4: Deploy Karein! 🚀
* Niche blue button **"Deploy"** par click karein.
* 1-2 minute me Vercel backend live kar dega aur screen par confetti/celebration aayega!

---

## 🎯 Mujhe Kya Dena Hai?

Deploy hote hi Vercel screen par aapko ek **Domain / Link** dikhayega, jaise:
```text
https://labportal-backend.vercel.app
```
*(ya `https://backend-xxxxx.vercel.app`)*

👉 **Aapko bas ye Vercel wala link copy karke mujhe chat me bhejna hai!**

Mai ye link direct Android app ke andar fix kar dunga, jiske baad aapko phone me koi setting ya IP nahi chhedni padegi — app seedha 5G/Wi-Fi par hamesha ke liye connect ho jayega!
