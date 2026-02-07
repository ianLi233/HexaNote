# Windows Setup Guide for Nexa SDK RAG

## Prerequisites

- **Windows on Snapdragon X Elite**
- **Python 3.11 - 3.13 (ARM64 version)**
  - Download from: https://www.python.org/downloads/windows/
  - Make sure to select ARM64 installer
  - Check "Add Python to PATH" during installation

## Step 1: Verify Python Installation

Open PowerShell and verify:

```powershell
python --version
# Should show Python 3.11.x or 3.12.x or 3.13.x

python -c "import platform; print(platform.machine())"
# Should show 'ARM64'
```

## Step 2: Create Project Directory

```powershell
# Create directory for Nexa SDK server
cd $HOME\Documents
mkdir HexaNoteNexaServer
cd HexaNoteNexaServer
```

## Step 3: Set Up Virtual Environment

```powershell
# Create virtual environment
python -m venv .venv

# Activate virtual environment
.\.venv\Scripts\Activate.ps1

# If you get execution policy error, run:
# Set-ExecutionPolicy -ExecutionPolicy RemoteSigned -Scope CurrentUser
```

## Step 4: Install Nexa SDK Dependencies

```powershell
# Install nexaai SDK
pip install nexaai

# Install FastAPI and uvicorn for server
pip install fastapi uvicorn[standard] pydantic

# Install additional dependencies
pip install python-multipart
```

## Step 5: Create Nexa SDK Server

Create a file named `nexa_server.py` with the following content:

```python
from fastapi import FastAPI, HTTPException
from pydantic import BaseModel
from typing import List, Optional
import uvicorn
from nexaai.llm import LLM, GenerationConfig
from nexaai.common import ModelConfig
from nexaai.embedder import Embedder, EmbeddingConfig

app = FastAPI(title="Nexa SDK Server for HexaNote")

# Model configurations for Snapdragon X Elite NPU
EMBED_MODEL = "NexaAI/embeddinggemma-300m-npu"
LLM_MODEL = "NexaAI/Llama3.2-3B-NPU-Turbo"
PLUGIN_ID = "npu"
DEVICE_ID = "npu"

# Initialize models at startup (lazy loading)
embedder = None
llm = None

def get_embedder():
    global embedder
    if embedder is None:
        print(f"Loading embedding model: {EMBED_MODEL}")
        embedder = Embedder.from_(
            name_or_path=EMBED_MODEL,
            plugin_id=PLUGIN_ID
        )
    return embedder

def get_llm():
    global llm
    if llm is None:
        print(f"Loading LLM: {LLM_MODEL}")
        m_cfg = ModelConfig()
        llm = LLM.from_(
            LLM_MODEL,
            plugin_id=PLUGIN_ID,
            device_id=DEVICE_ID,
            m_cfg=m_cfg
        )
    return llm

# Request/Response models
class EmbeddingRequest(BaseModel):
    input: List[str]
    model: Optional[str] = EMBED_MODEL

class EmbeddingResponse(BaseModel):
    object: str = "list"
    data: List[dict]
    model: str
    usage: dict

class GenerateRequest(BaseModel):
    model: Optional[str] = LLM_MODEL
    prompt: str
    stream: bool = False
    max_tokens: Optional[int] = 512

class GenerateResponse(BaseModel):
    response: str
    model: str

@app.get("/")
def root():
    return {
        "service": "Nexa SDK Server for HexaNote",
        "status": "running",
        "embed_model": EMBED_MODEL,
        "llm_model": LLM_MODEL
    }

@app.get("/health")
def health():
    return {"status": "healthy"}

@app.post("/v1/embeddings", response_model=EmbeddingResponse)
def create_embeddings(request: EmbeddingRequest):
    """OpenAI-compatible embeddings endpoint."""
    try:
        embedder = get_embedder()
        texts = request.input if isinstance(request.input, list) else [request.input]

        # Generate embeddings
        batch_size = len(texts)
        embeddings = embedder.generate(
            texts=texts,
            config=EmbeddingConfig(batch_size=batch_size)
        )

        # Format response
        data = [
            {
                "object": "embedding",
                "embedding": emb.tolist(),
                "index": i
            }
            for i, emb in enumerate(embeddings)
        ]

        return EmbeddingResponse(
            data=data,
            model=request.model or EMBED_MODEL,
            usage={
                "prompt_tokens": sum(len(t.split()) for t in texts),
                "total_tokens": sum(len(t.split()) for t in texts)
            }
        )
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))

@app.post("/api/generate", response_model=GenerateResponse)
def generate(request: GenerateRequest):
    """Ollama-compatible generate endpoint (non-streaming)."""
    try:
        llm_instance = get_llm()

        # Generate response
        g_cfg = GenerationConfig(max_tokens=request.max_tokens or 512)
        response_text = llm_instance.generate(request.prompt, g_cfg=g_cfg)

        return GenerateResponse(
            response=response_text,
            model=request.model or LLM_MODEL
        )
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))

if __name__ == "__main__":
    print("=" * 60)
    print("Nexa SDK Server for HexaNote")
    print("=" * 60)
    print(f"Embedding Model: {EMBED_MODEL}")
    print(f"LLM Model: {LLM_MODEL}")
    print(f"Plugin: {PLUGIN_ID}")
    print(f"Device: {DEVICE_ID}")
    print("=" * 60)
    print("Starting server on http://0.0.0.0:8888")
    print("Models will be downloaded on first request (may take a few minutes)")
    print("=" * 60)

    uvicorn.run(app, host="0.0.0.0", port=8888)
```

Save this file as `nexa_server.py` in your `HexaNoteNexaServer` directory.

## Step 6: Run the Server

```powershell
# Make sure virtual environment is activated
.\.venv\Scripts\Activate.ps1

# Run the server
python nexa_server.py
```

The server will start on `http://0.0.0.0:8888`. On first request, it will download the models (this may take a few minutes depending on your internet connection).

## Step 7: Test the Server

Open a new PowerShell window and test:

```powershell
# Test health endpoint
curl http://localhost:8888/health

# Test embeddings
curl -X POST http://localhost:8888/v1/embeddings `
  -H "Content-Type: application/json" `
  -d '{"input": ["Hello world"]}'

# Test generation
curl -X POST http://localhost:8888/api/generate `
  -H "Content-Type: application/json" `
  -d '{"prompt": "What is the capital of France?", "stream": false}'
```

## Step 8: Find Your Windows IP Address

From WSL2, you'll need to connect to Windows host. Get your Windows IP:

```powershell
# Get your Windows IP address
ipconfig

# Look for "Wireless LAN adapter Wi-Fi" or "Ethernet adapter"
# Note down the IPv4 Address (e.g., 192.168.1.100)
```

## Step 9: Keep Server Running

To run the server in the background:

Option 1: Use a separate PowerShell window (keep it open)

Option 2: Use Windows Task Scheduler to run at startup

Option 3: Use `pythonw` for background execution:
```powershell
pythonw nexa_server.py
```

## Troubleshooting

### Model Download Issues
- Models will be downloaded to `~/.cache/nexa.ai/nexa_sdk/models`
- Ensure you have enough disk space (models are ~2-4GB each)
- If download fails, delete the cached model and retry

### NPU Not Available
- Make sure you have the latest Qualcomm drivers
- Check Device Manager for NPU device
- If NPU fails, the code will fallback to CPU

### Firewall Issues
- Windows Firewall may block the server
- Allow Python through firewall when prompted
- Or manually add rule for port 8888

### Port Already in Use
- Change port in `nexa_server.py`: `uvicorn.run(app, host="0.0.0.0", port=9999)`
- Update backend configuration accordingly

## Next Steps

Once the server is running on Windows, you'll need to:
1. Update the backend code in WSL2 to use this server
2. Get the Windows host IP address to configure in backend
3. Test connectivity from WSL2 to Windows server
