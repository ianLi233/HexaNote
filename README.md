# HexaNote

A privacy-first, AI-powered note-taking application with semantic search and RAG (Retrieval-Augmented Generation). Everything runs 100% locally on your machine — no data ever leaves your device.

## Features

- **Semantic Search** — Find notes by meaning, not just keywords
- **RAG Chat** — Ask questions about your notes and get AI-powered answers with cited sources
- **Voice Input** — On-device speech-to-text via WhisperKit with NPU acceleration (Android)
- **Multi-device Sync** — Real-time sync across devices via WebSocket
- **Local AI** — Powered by Nexa (LLM + Embeddings) and Weaviate (Vector DB)
- **Privacy First** — All data and AI inference stays on your machine

## Architecture

```
┌─────────────────┐
│  Windows Host   │
│  Nexa Server    │  LLM (Llama 3.2) + Embeddings (Jina v2)+ Reranker()
│  port 8883      │
└────────┬────────┘
         │
┌────────▼──────────────────────────────┐
│  Docker (WSL2)                        │
│                                       │
│  ┌─────────────┐  ┌───────────────┐  │
│  │  Backend    │  │  Weaviate     │  │
│  │  FastAPI    │  │  Vector DB    │  │
│  │  port 8001  │  │  port 8080    │  │
│  └─────────────┘  └───────────────┘  │
└───────────────────────────────────────┘
         │
    ┌────┴─────┐
    │          │
 Windows    Android
 Electron   Kotlin
 Client     Client
```

## Quick Start

### Prerequisites

- [Docker Desktop](https://www.docker.com/products/docker-desktop/) with WSL2
- [Nexa CLI](https://github.com/NexaAI/nexa-sdk) installed on the Windows host
- Node.js 18+ (for the Windows client)
- Android Studio (for the Android client)

### 1. Start the Nexa AI server

Please reference the [Backend Readme file](./README.md) for booting up the backend

### 2. Start the backend + Weaviate

```bash
docker compose up -d --build
```

This starts:
- **Weaviate** vector database on port `8080`
- **HexaNote backend** (FastAPI) on port `8001`

### 3. Launch a client

See the [Windows Client](#windows-client) or [Android Client](#android-client) sections below.

---

## Backend

**Tech stack:** Python, FastAPI, SQLAlchemy (SQLite), Weaviate, Nexa

The backend is a REST API that handles note storage, semantic search, RAG chat, and multi-device synchronization.

### Key Endpoints

| Endpoint | Method | Description |
|---|---|---|
| `/api/v1/token` | POST | Authenticate (default password: `hexanote`) |
| `/api/v1/health` | GET | Health check (DB + Weaviate status) |
| `/api/v1/notes` | GET/POST | List or create notes |
| `/api/v1/notes/{id}` | GET/PUT/DELETE | Read, update, or soft-delete a note |
| `/api/v1/notes/search/semantic` | GET | Semantic search across all notes |
| `/api/v1/notes/{id}/search` | GET | Deep search within a single note |
| `/api/v1/notes/reindex` | POST | Reindex all notes in Weaviate |
| `/api/v1/chat/query` | POST | RAG-powered chat query |
| `/api/v1/chat/history` | GET | Get chat conversation history |
| `/api/v1/chat/sessions` | POST | Create a new chat session |
| `/api/v1/sync/ws` | WebSocket | Real-time multi-device sync |

### RAG Pipeline

1. **Semantic search** retrieves relevant note chunks from Weaviate
2. **Re-ranking** filters chunks by confidence (threshold: 0.5)
3. **LLM generation** produces a grounded answer from the top chunks
4. **Deep search** allows follow-up queries within a single note

### Running locally (without Docker)

```bash
cd backend
pip install -r requirements.txt
python main.py
# Server starts on http://0.0.0.0:8000
```

See [API_REFERENCE.md](API_REFERENCE.md) for full API documentation.

---

## Windows Client

**Tech stack:** Electron, React 18, TypeScript, Vite, Tailwind CSS

A desktop application for Windows that provides a full note editor and RAG chat interface.

### Features

- **Note Editor** — Markdown editing with live preview, LaTeX/math rendering (KaTeX), and auto-save
- **Semantic Search** — Search notes by meaning from the sidebar
- **RAG Chat** — Conversational AI interface with session management and source citations
- **File Upload** — Import `.txt` and `.md` files as notes
- **Tag Management** — Organize notes with tags

### Build & Run

```bash
cd windows-client
npm install

# Development
npm run dev

# Build for Windows (ARM64)
npm run build:win
```

---

## Android Client

**Tech stack:** Kotlin, Jetpack Compose, Retrofit, WhisperKit (Qualcomm NPU)

A voice-first mobile client optimized for Snapdragon 8 Elite devices (e.g., Galaxy S25).

### Features

- **Voice Input** — Tap to record, on-device transcription via WhisperKit with NPU acceleration
- **RAG Chat** — Ask questions about your notes with AI-generated answers
- **Semantic Search** — Meaning-based search across all notes
- **Text-to-Speech** — Listen to AI responses and note content hands-free
- **Hardware Optimized** — NPU-accelerated inference for low-latency transcription

### Build & Install

```bash
cd android

# Build and install on connected device
./gradlew installDebug

# Or build APK only
./gradlew assembleDebug
# Output: app/build/outputs/apk/debug/app-debug.apk
```

### Configuration

Update the backend server URL in `HexaNoteRetrofitClient.kt`:

```kotlin
private const val BASE_URL = "http://YOUR_SERVER_IP:8001/api/v1/"
```

---

## Configuration

### Environment Variables

| Variable | Default | Description |
|---|---|---|
| `DATABASE_URL` | `sqlite:///./data/hexanote.db` | SQLite database path |
| `WEAVIATE_URL` | `http://weaviate:8080` | Weaviate vector DB URL |
| `NEXA_URL` | `http://172.23.224.1:8883` | Nexa AI server URL (Windows host) |
| `SIMPLE_PASSWORD` | `hexanote` | Authentication password |
| `SECRET_KEY` | (see config.py) | JWT signing secret (change in production) |

### Docker Compose Services

| Service | Port | Description |
|---|---|---|
| `backend` | 8001 | FastAPI application server |
| `weaviate` | 8080 | Vector database for semantic search |

### AI Models

| Model | Purpose | Details |
|---|---|---|
| ` NexaAI/jina-v2-rerank-npu` |for Re-ranking| Rerankeing |
| `djuna/jina-embeddings-v2-base-en-Q5_K_M-GGUF` | Text embeddings | 768-dimensional vectors |
| `NexaAI/Llama3.2-3B-NPU-Turbo` | LLM generation | NPU-accelerated, ~25 tokens/sec |

---

## Tech Stack Summary

| Component | Technologies |
|---|---|
| **Backend** | Python, FastAPI, SQLAlchemy, SQLite, Weaviate, Nexa |
| **Windows Client** | Electron, React, TypeScript, Vite, Tailwind CSS |
| **Android Client** | Kotlin, Jetpack Compose, Retrofit, WhisperKit |
| **AI / ML** | Llama 3.2 (LLM), Jina Embeddings v2, WhisperKit (ASR) |
| **Infrastructure** | Docker Compose, WSL2 |

---

## License

This project is for educational and personal use.
