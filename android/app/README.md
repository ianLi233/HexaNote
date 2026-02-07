# HexaNote Voice Assistant

Voice-powered note querying system for your HexaNote server using Qualcomm Whisper ASR.

## What It Does

1. **🎤 Record** - Tap to record your question
2. **🔊 Transcribe** - Whisper converts speech to text  
3. **📡 Query** - Sends to your HexaNote RAG server
4. **💬 Respond** - Gets AI-generated answer from your notes
5. **🔈 Speak** - TTS reads the answer aloud
6. **📱 Display** - Shows transcript and response on screen

## Quick Start

### 1. Configure Server

Edit `app/src/main/java/com/notesassistant/app/network/HexaNoteRetrofitClient.kt`:

```kotlin
private const val BASE_URL = "http://YOUR_SERVER_IP:8000/"
```

- **Emulator**: `http://10.0.2.2:8000/`
- **Real Device**: `http://192.168.1.XXX:8000/`

### 2. Run

1. Open in Android Studio
2. Sync Gradle
3. Run on Samsung Galaxy S25
4. Grant microphone permission
5. Tap mic button and ask a question!

## Current Status

✅ **Working:**
- Audio recording
- TTS playback
- HexaNote server communication
- UI with transcript and response display
- Session management

🔶 **Mock Implementation:**
- Whisper ASR (returns test transcripts)

## Integrating Real Whisper

See `INTEGRATION_GUIDE.md` for complete instructions on integrating:
- WhisperKit (optimized for Snapdragon 8 Elite)
- WhisperCpp (alternative)
- Google Cloud Speech-to-Text

## Architecture

```
User speaks → AudioRecorder → WhisperASR
                                  ↓
                           (Transcript)
                                  ↓
                         HexaNote /api/v1/chat/query
                                  ↓
                           (AI Response)
                                  ↓
                    TTSManager + UI Display
```

## Files

- `MainActivity.kt` - Entry point, permission handling
- `NotesViewModel.kt` - Orchestrates ASR → Server → TTS flow
- `HomeScreen.kt` - Voice-focused UI
- `HexaNoteApiService.kt` - API definitions
- `WhisperKitASR.kt` - Speech-to-text (template for Whisper integration)
- `TTSManager.kt` - Text-to-speech
- `AudioRecorder.kt` - Audio capture

## API Used

**Endpoint:** `POST /api/v1/chat/query`

**Request:**
```json
{
  "message": "What are my notes about ML?",
  "session_id": "optional-uuid",
  "limit": 5
}
```

**Response:**
```json
{
  "message": "Based on your notes, machine learning...",
  "session_id": "session-uuid",
  "created_at": "..."
}
```

## Testing

### Without Real Whisper

The app generates mock transcripts based on audio length:
- Short recording: "What are my notes about machine learning?"
- Medium: "Can you summarize the key concepts?"
- Long: "Tell me about neural networks..."

This lets you test the full flow except actual ASR.

### Server Connection Test

```bash
curl http://YOUR_SERVER:8000/api/v1/health
```

Should return:
```json
{"status": "healthy", "version": "1.0.0"}
```

## Requirements

- Android 8.0+ (API 26)
- Microphone permission
- Network connection to HexaNote server
- Samsung Galaxy S25 (or any Android device)

## Performance

Optimized for Snapdragon 8 Elite:
- Efficient coroutine-based async
- Minimal UI overhead
- Ready for NPU-accelerated Whisper

## Troubleshooting

**Server connection fails:**
1. Check server is running
2. Verify IP address in `BASE_URL`
3. Check firewall allows port 8000
4. Ensure same WiFi network (for real device)

**TTS not working:**
1. Check volume
2. Verify TTS engine installed
3. Review Logcat for errors

**Recording fails:**
1. Grant microphone permission
2. No other app using mic
3. Check Logcat for errors

## Next Steps

1. Test with mock Whisper
2. Verify server communication
3. Integrate real Whisper (see INTEGRATION_GUIDE.md)
4. Optimize for your use case

## Documentation

- `INTEGRATION_GUIDE.md` - Complete setup and Whisper integration
- `API_DOCUMENTATION.md` - (coming soon) Full API reference
- Inline code comments - Detailed explanations

## License

MIT

---

**Built for Samsung Galaxy S25 with Snapdragon 8 Elite** 🚀
