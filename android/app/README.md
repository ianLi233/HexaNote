# HexaNote Assistant

Voice-powered intelligence for your personal notes. Powered by Qualcomm Whisper ASR, Ollama, and Weaviate.

## 🚀 Features

- **🎤 Voice-First Interface** - Tap to record questions with high-performance on-device transcription.
- **🧠 RAG Chat** - Context-aware conversations. The AI searches your notes to provide accurate, synthesized answers.
- **🔍 Semantic Search** - Find notes by meaning and concepts rather than just keyword matching.
- **🔊 Text-to-Speech** - Listen to responses and note content for a hands-free experience.
- **✨ Modern UI** - A sleek, "Slate" themed dark interface optimized for clarity and speed.

## 🛠️ Architecture

```
[ Mobile App ] <---(REST)---> [ HexaNote Server ]
      |                             |
      |-- WhisperKit (NPU)          |-- Ollama (LLM)
      |-- TTS Engine                |-- Weaviate (Vector DB)
      |-- Jetpack Compose UI        |-- Semantic Search Logic
```

## 📋 Prerequisites

- **Android Device**: Optimized for Snapdragon 8 Elite (Galaxy S25) using Qualcomm NPU.
- **HexaNote Backend**: A running instance of the HexaNote RAG server.
- **Network**: Connectivity to the backend (via Local IP or Tailscale).

## 📦 APK & Installation

### Build it yourself
1. Open the project in **Android Studio Ladybug+**.
2. Go to **Build** > **Build Bundle(s) / APK(s)** > **Build APK(s)**.
3. Once the build completes, a notification will appear. Click **locate** to find the APK, or find it manually at:
   `app/build/outputs/apk/debug/app-debug.apk`

### Quick Install
If you have a device connected via ADB, you can build and install in one step:
```bash
./gradlew installDebug
```

## ⚙️ Configuration

### 1. Backend URL
Edit `app/src/main/java/com/notesassistant/app/network/HexaNoteRetrofitClient.kt`:
```kotlin
private const val BASE_URL = "http://YOUR_SERVER_IP:8001/api/v1/"
```

### 2. Authentication
The app uses token-based authentication. Ensure your backend is configured to accept requests with the appropriate credentials.

## 📱 Project Structure

- `MainActivity.kt`: Entry point and permission handling.
- `ChatScreen.kt`: The main UI supporting RAG Chat and Semantic Search modes.
- `NotesViewModel.kt`: Core logic for orchestrating ASR, API calls, and UI state.
- `WhisperKitASR.kt`: On-device speech-to-text integration using Qualcomm WhisperKit.
- `HexaNoteApiService.kt`: Retrofit definitions for Chat, Search, and Reindexing.
- `AudioRecorder.kt`: Low-latency audio capture for ASR.
- `TTSManager.kt`: Android Text-To-Speech integration.

## 📡 API Capabilities

The app integrates with several key endpoints:
- `POST /chat/query`: Submit natural language questions for RAG processing.
- `GET /notes/search/semantic`: Perform meaning-based searches across your note library.
- `POST /token`: Handle secure authentication.
- `POST /notes/reindex`: Trigger backend indexing of new/updated notes.

## 🚀 Getting Started

1. Open the project in **Android Studio**.
2. Sync Gradle dependencies.
3. Build and run on a compatible Android device.
4. Grant **Microphone** permissions when prompted.
5. Start asking questions about your notes!

---
**Built for the Snapdragon 8 Elite Generation** 🚀
