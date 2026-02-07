# Quick Setup Guide - Notes Assistant App

This guide will help you get the app running in 30 minutes.

## Prerequisites Checklist

- [ ] Android Studio installed (Hedgehog 2023.1.1+)
- [ ] Python 3.8+ installed (for backend)
- [ ] Samsung Galaxy S25 with USB debugging enabled OR Android Emulator
- [ ] USB cable (if using real device)

## Step-by-Step Setup

### Part 1: Setup Android Studio (10 minutes)

1. **Download and Install Android Studio**
   - Visit: https://developer.android.com/studio
   - Download for your OS (Windows/Mac/Linux)
   - Install with default settings

2. **Install Required Components**
   - Open Android Studio
   - Go to: Tools → SDK Manager
   - Ensure installed:
     - ✅ Android SDK Platform 34
     - ✅ Android SDK Build-Tools
     - ✅ Android Emulator (if not using real device)

### Part 2: Open the Project (5 minutes)

1. **Extract/Open Project**
   ```
   - Unzip NotesAssistantApp.zip (if zipped)
   - Open Android Studio
   - Click "Open" → Select NotesAssistantApp folder
   ```

2. **Wait for Gradle Sync**
   - Android Studio will download dependencies
   - This may take 3-5 minutes on first run
   - Bottom right will show "Gradle Build Finished"

3. **Fix any sync errors**
   - If prompted to update Gradle, click "Update"
   - If SDK versions missing, click "Install missing platforms"

### Part 3: Setup Backend Server (5 minutes)

1. **Install Flask**
   ```bash
   # Windows
   pip install flask
   
   # Mac/Linux
   pip3 install flask
   ```

2. **Navigate to Project**
   ```bash
   cd NotesAssistantApp
   ```

3. **Start Server**
   ```bash
   # Windows
   python backend_server.py
   
   # Mac/Linux
   python3 backend_server.py
   ```

4. **Verify Server Running**
   - You should see:
   ```
   ==========================================
   Notes Assistant Backend Server
   ==========================================
   Server starting on http://0.0.0.0:5000
   ```

### Part 4: Configure Connection (2 minutes)

#### If using Android Emulator:
- Default config works! (`http://10.0.2.2:5000/`)
- No changes needed

#### If using Real Device (Samsung Galaxy S25):

1. Find your computer's IP address:
   
   **Windows:**
   ```cmd
   ipconfig
   ```
   Look for "IPv4 Address" (e.g., 192.168.1.100)
   
   **Mac:**
   ```bash
   ifconfig | grep "inet "
   ```
   
   **Linux:**
   ```bash
   ip addr show
   ```

2. Edit `RetrofitClient.kt`:
   - Path: `app/src/main/java/com/notesassistant/app/network/RetrofitClient.kt`
   - Change line 18:
   ```kotlin
   private const val BASE_URL = "http://YOUR_IP_HERE:5000/"
   // Example: "http://192.168.1.100:5000/"
   ```

3. Make sure phone and computer are on same WiFi network

### Part 5: Enable USB Debugging (Real Device Only) (3 minutes)

1. **On Samsung Galaxy S25:**
   - Go to Settings → About Phone
   - Tap "Build Number" 7 times
   - You'll see "Developer mode enabled"

2. **Enable USB Debugging:**
   - Settings → Developer Options
   - Toggle "USB Debugging" ON

3. **Connect to Computer:**
   - Plug in USB cable
   - Tap "Allow" on phone when prompted

### Part 6: Run the App (5 minutes)

1. **Select Device**
   - Top toolbar in Android Studio
   - Click device dropdown
   - Select your Samsung Galaxy S25 or Emulator

2. **Click Run**
   - Click green play button (▶️)
   - Or press: Shift + F10 (Windows/Linux) / Control + R (Mac)

3. **Wait for Build**
   - First build takes 2-3 minutes
   - Subsequent builds are faster (30 seconds)

4. **App Launches**
   - App installs on device
   - Opens automatically
   - Requests permissions

5. **Grant Permissions**
   - Camera → Allow
   - Microphone → Allow

### Part 7: Test the App (3 minutes)

#### Test 1: Capture Notes
1. Tap "Open Camera"
2. Point at any text/notes
3. Tap camera button (circle)
4. Should see "Image uploaded successfully!"

#### Test 2: Voice Query
1. Tap "Start Recording"
2. Speak: "What are the key points in my notes?"
3. Tap "Stop Recording"
4. Should see response and hear TTS

## Common Issues & Fixes

### Issue: Gradle sync failed
**Fix:**
- File → Invalidate Caches → Restart
- Or: Update Gradle to latest version when prompted

### Issue: Backend server won't start
**Fix:**
```bash
# Check if port 5000 is already in use
# Windows:
netstat -ano | findstr :5000

# Mac/Linux:
lsof -i :5000

# Kill the process or use a different port
```

### Issue: App can't connect to backend
**Fix:**
1. Check server is running (terminal should show "Running on...")
2. Verify IP address is correct (ping from phone)
3. Disable firewall temporarily
4. Make sure both on same WiFi

### Issue: Camera won't open
**Fix:**
1. Settings → Apps → Notes Assistant → Permissions
2. Enable Camera permission
3. Restart app

### Issue: Build error: "SDK not found"
**Fix:**
- Tools → SDK Manager
- Install Android 13/14 SDK
- Sync project again

## Testing Checklist

After setup, verify:
- [ ] App opens without crashing
- [ ] Camera permission granted
- [ ] Camera opens and captures image
- [ ] Backend receives upload (check server logs)
- [ ] Microphone permission granted
- [ ] Can record audio
- [ ] Response displayed on screen
- [ ] TTS speaks the response

## Next Steps

Once everything works:

1. **Try different queries:**
   - "Summarize my notes"
   - "What are the main concepts?"
   - "Explain the key algorithms"

2. **Customize the app:**
   - Change colors in `Theme.kt`
   - Modify UI in `HomeScreen.kt`
   - Update backend responses in `backend_server.py`

3. **Integrate real services:**
   - Add real Whisper ASR
   - Connect to actual OCR service
   - Integrate OpenAI or Claude API

## Getting Help

If stuck:
1. Check Android Studio Logcat (bottom panel)
2. Check backend server logs (terminal)
3. Review error messages carefully
4. Try Clean Project → Rebuild Project

## Development Tips

- **Hot Reload**: Compose updates live when you edit
- **Logcat**: See all app logs in real-time
- **Breakpoints**: Debug by clicking line numbers
- **Build Variants**: Switch between debug/release

## Files You'll Edit Most

- `HomeScreen.kt` - Main UI
- `backend_server.py` - Backend logic
- `RetrofitClient.kt` - API configuration
- `NotesViewModel.kt` - App state management

## Performance Tips

- Keep backend server running during development
- Use debug builds (faster than release)
- Emulator is slower than real device
- First build is always slowest

## Success!

If you see:
- ✅ Camera captures images
- ✅ Voice queries work
- ✅ Backend responds
- ✅ TTS speaks answers

**Congratulations! Your app is fully functional!** 🎉

Now you can start customizing and adding features.

---

**Estimated Total Time: 30-45 minutes**

Questions? Review the main README.md for more details.
