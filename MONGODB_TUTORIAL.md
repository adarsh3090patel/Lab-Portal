# 🍃 MongoDB Atlas Setup Step-by-Step Guide

Aapke MongoDB Atlas dashboard ke mutabiq ye aasan steps follow karein:

---

## 📌 Step 1: Cluster ko Resume (Unpause) Karein
Aapke screenshot me `Cluster0` ke paas **PAUSED CLUSTER** dikh raha hai:
1. `Cluster0` ke daayein (right side) me **`...`** (3 dots button) par click karein.
2. **"Resume"** par click karein.
3. *1 se 2 minute intezar karein* jab tak status green hokar active na ho jaye.

---

## 📌 Step 2: IP Address Access Allow Karein (Network Access)
Aapke screen par upar yellow warning banner hai. Vercel aur mobile testing ke liye sabhi IP allow karna zaroori hai:
1. Left sidebar me **SECURITY** section ke andar **"Network Access"** par click karein.
2. Green button **"Add IP Address"** dabayein.
3. **"ALLOW ACCESS FROM ANYWHERE"** button par click karein (ye `0.0.0.0/0` add kar dega).
4. **"Confirm"** dabayein.
> 💡 *Note: Ye isliye zaroori hai taaki jab backend Vercel par host ho ya aapka phone Wi-Fi se connect ho, toh connection block na ho.*

---

## 📌 Step 3: Database Username aur Password Banayein (Database Access)
1. Left sidebar me **"Database Access"** par click karein.
2. Green button **"Add New Database User"** par click karein.
3. **Authentication Method**: `Password` chunein.
4. **Username**: Jaise `labadmin` ya apna naam likhein.
5. **Password**: Ek password daalein (ise yaad ya copy kar lein).
6. **Built-in Role**: `Read and write to any database` selected rehne dein.
7. **"Add User"** button dabayein.

---

## 📌 Step 4: Connection String (URL) Copy Karein
1. Left sidebar me wapas **"Clusters"** (ya Project Overview) par jayein.
2. `Cluster0` ke samne **"Connect"** button par click karein.
3. Popup me **"Drivers"** select karein.
4. Driver me **Python** (version default) chunein.
5. Niche step 3 me aapko ek URL dikhega, jaise:
   ```
   mongodb+srv://<username>:<password>@cluster0.xxxxxx.mongodb.net/?retryWrites=true&w=majority
   ```
6. Is string ko copy kar lein!

---

## 📌 Step 5: Mujhe Kya Dena Hai?

Aapko bas wo copy kiya hua **Connection String** dena hai (jisme aapne apna password fill kiya ho).

Jaise:
```
mongodb+srv://labadmin:MeraPassword123@cluster0.abcde.mongodb.net/?retryWrites=true&w=majority
```

> 🔒 **Tip:** Agar aap chat me password share nahi karna chahte, toh aap direct apne project me is file ko open karke paste kar sakte hain:
> **File:** `d:\Project\LabPortal\backend\.env`
> ```env
> MONGODB_URI=mongodb+srv://<username>:<password>@cluster0.xxxxxx.mongodb.net/?retryWrites=true&w=majority
> DB_NAME=labportal
> PORT=8000
> ```

