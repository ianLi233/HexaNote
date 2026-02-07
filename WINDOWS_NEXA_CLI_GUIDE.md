# Using Nexa CLI for HexaNote (Simpler Approach!)

## Why Use CLI Instead of Python SDK?

- ✅ No Python coding needed
- ✅ More stable (no import issues)
- ✅ OpenAI-compatible API out of the box
- ✅ Easier to manage

## Prerequisites

- Windows with Snapdragon X Elite (ARM64)
- Python 3.11-3.13 (ARM64) installed

## Installation

### Step 1: Install Nexa CLI

Open PowerShell:

```powershell
# Install nexaai package (for CLI tool)
pip install nexaai

# Verify installation
nexa --version
```

### Step 2: Start Embedding Model Server

```powershell
# Start embedding model on port 8881
# Note: NPU is auto-detected, no flag needed!
nexa serve NexaAI/embeddinggemma-300m-npu --host 0.0.0.0:8881
```

This will:
- **Auto-detect NPU** and use it automatically
- Download the model on first run (~1-2GB, takes 5-10 minutes)
- Start server at `http://0.0.0.0:8881`
- Expose OpenAI-compatible `/v1/embeddings` endpoint

**Keep this PowerShell window open!**

### Step 3: Start LLM Model Server (New Window)

Open a **second PowerShell window**:

```powershell
# Start LLM model on port 8882
nexa serve NexaAI/Llama3.2-3B-NPU-Turbo --host 0.0.0.0:8882
```

This will:
- **Auto-detect NPU** and use it automatically
- Download the model on first run (~2-3GB)
- Start server at `http://0.0.0.0:8882`
- Expose OpenAI-compatible `/v1/chat/completions` endpoint

**Keep this window open too!**

### Step 4: Test the Servers

Open a **third PowerShell window** to test:

```powershell
# Test embedding server
curl -X POST http://localhost:8881/v1/embeddings `
  -H "Content-Type: application/json" `
  -d '{"input": ["hello world"], "model": "NexaAI/embeddinggemma-300m-npu"}'

# Test LLM server
curl -X POST http://localhost:8882/v1/chat/completions `
  -H "Content-Type: application/json" `
  -d '{
    "model": "NexaAI/Llama3.2-3B-NPU-Turbo",
    "messages": [{"role": "user", "content": "Say hello"}],
    "max_tokens": 50
  }'
```

### Step 5: Get Your Windows IP Address

```powershell
ipconfig

# Look for "Wireless LAN adapter Wi-Fi" or "Ethernet adapter"
# Note the IPv4 Address (e.g., 192.168.1.100)
```

## Alternative: Combined Server (If Supported)

Some Nexa CLI versions support running multiple models. Try:

```powershell
# Check available options
nexa serve --help

# Note: Nexa Go CLI runs one model per server
# So you need two separate servers as shown above
```

If not, just run two separate servers as shown above.

## Keeping Servers Running

### Option 1: Keep PowerShell Windows Open
Just minimize the windows and leave them running.

### Option 2: Run as Background Services

Create two batch files:

**start_embedding.bat:**
```batch
@echo off
nexa serve NexaAI/embeddinggemma-300m-npu --host 0.0.0.0:8881
pause
```

**start_llm.bat:**
```batch
@echo off
nexa serve NexaAI/Llama3.2-3B-NPU-Turbo --host 0.0.0.0:8882
pause
```

Double-click each to start. Or use Windows Task Scheduler to run at startup.

### Option 3: Use pythonw (No Console)

```powershell
# In background (no console window)
Start-Process pythonw -ArgumentList "-m", "nexa", "server", "NexaAI/embeddinggemma-300m-npu", "--npu", "--port", "8881" -WindowStyle Hidden

Start-Process pythonw -ArgumentList "-m", "nexa", "server", "NexaAI/Llama3.2-3B-NPU-Turbo", "--npu", "--port", "8882" -WindowStyle Hidden
```

## Troubleshooting

### "nexa: command not found"

```powershell
# Check if nexaai is installed
pip show nexaai

# If not installed:
pip install nexaai

# If installed but command not found, add Python Scripts to PATH
# Or find nexa.exe in: C:\Users\YOUR_USER\AppData\Local\Programs\Python\Python311-arm64\Scripts\
```

### Port Already in Use

```powershell
# Change ports (use 8883, 8884, etc.)
nexa serve NexaAI/embeddinggemma-300m-npu --host 0.0.0.0:8883
nexa serve NexaAI/Llama3.2-3B-NPU-Turbo --host 0.0.0.0:8884
```

### Models Won't Load

```powershell
# Check cache directory
dir $HOME\.cache\nexa.ai\nexa_sdk\models

# Clear cache if corrupted
rm -r $HOME\.cache\nexa.ai\nexa_sdk\models\NexaAI
```

### NPU Not Working

NPU models should auto-detect and use NPU. If you want to use CPU-optimized GGUF models instead:

```powershell
# CPU-optimized GGUF models (faster on CPU if NPU isn't working)
nexa serve jinaai/jina-embeddings-v4-text-retrieval-GGUF/jina-embeddings-v4-text-retrieval-Q4_K_M.gguf --host 0.0.0.0:8881

nexa serve NexaAI/Qwen3-4B-GGUF/Qwen3-4B-Q4_0.gguf --host 0.0.0.0:8882
```

## Next Steps

Once both servers are running on Windows:

1. Note your Windows IP address
2. Note the ports (8881 for embeddings, 8882 for LLM)
3. Head back to WSL2 to configure the backend

The backend will call:
- `http://YOUR_WINDOWS_IP:8881/v1/embeddings` for embeddings
- `http://YOUR_WINDOWS_IP:8882/v1/chat/completions` for LLM generation
