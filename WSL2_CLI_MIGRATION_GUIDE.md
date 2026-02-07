# WSL2 Backend Migration Guide (Using Nexa CLI)

## Overview

This guide shows how to configure the backend to use Nexa CLI servers running on Windows.

**Architecture:**
- Windows: Two Nexa CLI servers (embeddings on :8881, LLM on :8882)
- WSL2: Backend calls Windows servers via OpenAI-compatible APIs
- WSL2: Weaviate (with manual embeddings, no Ollama)

## Prerequisites

✅ Nexa CLI servers running on Windows (see WINDOWS_NEXA_CLI_GUIDE.md)
✅ Windows IP address noted
✅ Both servers tested and working

## Step 1: Get Windows Host IP from WSL2

```bash
# In WSL2, get the Windows host IP
cat /etc/resolv.conf | grep nameserver | awk '{print $2}'

# Example output: 172.24.160.1
# This is your WINDOWS_IP
```

## Step 2: Test Connectivity from WSL2

```bash
# Replace WINDOWS_IP with the IP from Step 1

# Test embedding server
curl http://WINDOWS_IP:8881/health

# Test LLM server
curl http://WINDOWS_IP:8882/health

# Both should return {"status": "healthy"} or similar

# Test embedding functionality
curl -X POST http://WINDOWS_IP:8881/v1/embeddings \
  -H "Content-Type: application/json" \
  -d '{"input": ["test"], "model": "NexaAI/embeddinggemma-300m-npu"}'

# Test LLM functionality
curl -X POST http://WINDOWS_IP:8882/v1/chat/completions \
  -H "Content-Type: application/json" \
  -d '{
    "model": "NexaAI/Llama3.2-3B-NPU-Turbo",
    "messages": [{"role": "user", "content": "Hi"}],
    "max_tokens": 20
  }'
```

If these work, you're ready to proceed!

## Step 3: Update Backend Configuration

### File: `backend/config.py`

Add/modify these settings:

```python
class Settings(BaseSettings):
    # ... existing settings ...

    # Nexa CLI Servers (running on Windows)
    # Replace 172.24.160.1 with your Windows IP
    nexa_embed_url: str = "http://172.24.160.1:8881"
    nexa_llm_url: str = "http://172.24.160.1:8882"
    nexa_embedding_model: str = "NexaAI/embeddinggemma-300m-npu"
    nexa_generation_model: str = "NexaAI/Llama3.2-3B-NPU-Turbo"

    # OLD - Comment out Ollama settings
    # ollama_url: str = "http://ollama:11434"
    # ollama_embedding_model: str = "mxbai-embed-large:latest"
    # ollama_generation_model: str = "llama3.2:1b"
```

## Step 4: Create Nexa Client Service

Create file: `backend/services/nexa_client.py`

Copy the content from the `nexa_client_cli.py` file I created.

You can copy it from WSL2:

```bash
cp /tmp/claude-1000/-home-qc-de-HexaNote/f5560acc-3bf1-4e21-b47f-c248f8061675/scratchpad/nexa_client_cli.py \
   /home/qc_de/HexaNote/backend/services/nexa_client.py
```

## Step 5: Update Weaviate Service

The `weaviate_service.py` needs these changes:

### Change 1: Remove Ollama Vectorizer in `ensure_collection()`

```python
# OLD
self.client.collections.create(
    name=self.collection_name,
    vectorizer_config=wvc.config.Configure.Vectorizer.text2vec_ollama(...),
    generative_config=wvc.config.Configure.Generative.ollama(...),
    ...
)

# NEW
self.client.collections.create(
    name=self.collection_name,
    vectorizer_config=wvc.config.Configure.Vectorizer.none(),  # Manual embeddings
    properties=[...],
)
```

### Change 2: Add Manual Embeddings in `index_note()`

```python
def index_note(self, note_id: str, title: str, content: str,
               tags: List[str], created_at: str, updated_at: str) -> Optional[str]:
    try:
        from services.nexa_client import get_nexa_client

        note_collection = self.client.collections.get(self.collection_name)
        nexa = get_nexa_client()

        # Delete existing chunks
        self.delete_note(note_id)

        # Split content into chunks
        content_chunks = chunk_text(content)
        total_chunks = len(content_chunks)

        first_uuid = None
        for i, chunk in enumerate(content_chunks):
            # Combine title and chunk for better context
            text_to_embed = f"{title}\n\n{chunk}"

            # Generate embedding using Nexa
            embeddings = nexa.embed_texts([text_to_embed])
            embedding_vector = embeddings[0]

            properties = {
                "note_id": note_id,
                "title": title,
                "content": chunk,
                "chunk_index": i,
                "total_chunks": total_chunks,
                "tags": ", ".join(tags) if tags else "",
                "created_at": created_at,
                "updated_at": updated_at,
            }

            # Insert with explicit vector
            weaviate_uuid = note_collection.data.insert(
                properties=properties,
                vector=embedding_vector  # Pass embedding explicitly
            )
            if i == 0:
                first_uuid = str(weaviate_uuid)

        print(f"Indexed note {note_id} as {total_chunks} chunk(s)")
        return first_uuid
    except Exception as e:
        print(f"Error indexing note {note_id}: {e}")
        return None
```

### Change 3: Replace Ollama Generation in `generative_search()`

```python
def generative_search(self, query: str, limit: int = 5,
                     tag_filter: Optional[List[str]] = None,
                     additional_context: Optional[str] = None) -> Dict:
    try:
        from services.nexa_client import get_nexa_client

        nexa = get_nexa_client()
        note_collection = self.client.collections.get(self.collection_name)

        # ... (keep existing search and filtering logic) ...

        # Build prompt (same as before)
        full_prompt = f"""You are a helpful AI assistant. The user has asked:

"{query}"{additional_context_section}

Below are relevant excerpts from notes:

{context_text}

Based on the excerpts above, provide a helpful answer.
- Reference specific information if available.
- Be concise but thorough."""

        # Call Nexa LLM instead of Ollama
        generated_text = nexa.generate(full_prompt, max_tokens=512)

        # ... (keep existing response building logic) ...

        return {
            "response": generated_text,
            "context_notes": context_notes
        }
    except Exception as e:
        print(f"Error in generative search: {e}")
        return {
            "response": f"Error: {str(e)}",
            "context_notes": []
        }
```

## Step 6: Update Docker Compose

### File: `docker-compose.yml`

```yaml
services:
  # Remove or comment out Ollama
  # ollama:
  #   image: ollama/ollama
  #   ...

  weaviate:
    command:
    - --host
    - 0.0.0.0
    - --port
    - '8080'
    - --scheme
    - http
    image: cr.weaviate.io/semitechnologies/weaviate:1.32.8
    ports:
    - 8080:8080
    - 50051:50051
    volumes:
    - weaviate_data:/var/lib/weaviate
    restart: on-failure:0
    environment:
      QUERY_DEFAULTS_LIMIT: 25
      PERSISTENCE_DATA_PATH: '/var/lib/weaviate'
      ENABLE_API_BASED_MODULES: 'false'  # Changed from 'true'
      CLUSTER_HOSTNAME: 'node1'
      # Remove Ollama modules
      # ENABLE_MODULES: 'text2vec-ollama,generative-ollama'
      MODULES_CLIENT_TIMEOUT: '300s'
      QUERY_MAXIMUM_RESULTS: 100
      AUTHENTICATION_ANONYMOUS_ACCESS_ENABLED: 'false'
      AUTHENTICATION_APIKEY_ENABLED: 'true'
      AUTHENTICATION_APIKEY_ALLOWED_KEYS: 'user-a-key,user-b-key'
      AUTHENTICATION_APIKEY_USERS: 'user-a,user-b'
      AUTHORIZATION_ENABLE_RBAC: 'true'
      AUTHORIZATION_RBAC_ROOT_USERS: 'user-a'
    # Remove depends_on ollama

  backend:
    build: ./backend
    container_name: hexanote-backend
    ports:
      - "8001:8000"
    volumes:
      - ./backend:/app
      - hexanote_data:/data
    environment:
      - DATABASE_URL=sqlite:////data/hexanote.db
      - WEAVIATE_URL=http://weaviate:8080
      - WEAVIATE_API_KEY=user-a-key
      # Nexa CLI servers on Windows
      - NEXA_EMBED_URL=http://172.24.160.1:8881  # Replace with your IP
      - NEXA_LLM_URL=http://172.24.160.1:8882
      - NEXA_EMBEDDING_MODEL=NexaAI/embeddinggemma-300m-npu
      - NEXA_GENERATION_MODEL=NexaAI/Llama3.2-3B-NPU-Turbo
      - DEBUG=false
    depends_on:
      - weaviate
    restart: on-failure:0
    extra_hosts:
      - "host.docker.internal:host-gateway"

volumes:
  weaviate_data:
  # Remove ollama_data
  hexanote_data:
```

## Step 7: Environment Variables

Create/update `.env` file in backend directory:

```env
# Nexa CLI Servers (Windows)
# Replace with your Windows IP
NEXA_EMBED_URL=http://172.24.160.1:8881
NEXA_LLM_URL=http://172.24.160.1:8882
NEXA_EMBEDDING_MODEL=NexaAI/embeddinggemma-300m-npu
NEXA_GENERATION_MODEL=NexaAI/Llama3.2-3B-NPU-Turbo

# Weaviate
WEAVIATE_URL=http://weaviate:8080
WEAVIATE_API_KEY=user-a-key
```

## Step 8: Migration Steps

### 1. Backup
```bash
cd /home/qc_de/HexaNote
cp ./data/hexanote.db ./data/hexanote.db.backup
```

### 2. Stop Services
```bash
docker-compose down
```

### 3. Update Code
- Update `config.py` (add Nexa URLs)
- Copy `nexa_client.py` to `backend/services/`
- Update `weaviate_service.py` (3 changes above)
- Update `docker-compose.yml`

### 4. Delete Old Weaviate Collection
```bash
# Start Weaviate only
docker-compose up weaviate -d

# Wait 10 seconds for it to be ready
sleep 10

# Delete old collection (has Ollama config)
python3 << 'EOF'
import weaviate
from weaviate.classes.init import Auth
client = weaviate.connect_to_local(
    host='localhost',
    port=8080,
    auth_credentials=Auth.api_key('user-a-key')
)
try:
    client.collections.delete('Note')
    print('✓ Deleted old collection')
except:
    print('No collection to delete')
client.close()
EOF
```

### 5. Start All Services
```bash
docker-compose up -d

# Check logs
docker-compose logs -f backend
```

### 6. Reindex Notes (Uses Nexa for Embeddings)
```bash
curl -X POST http://localhost:8001/api/v1/notes/reindex
```

This will take a while as each note is embedded using Windows Nexa server.

## Step 9: Testing

```bash
# Test health
curl http://localhost:8001/api/v1/health

# Test search (uses Nexa embeddings)
curl "http://localhost:8001/api/v1/notes/search/semantic?q=test&limit=3"

# Test chat (uses Nexa LLM)
curl -X POST http://localhost:8001/api/v1/chat/query \
  -H "Content-Type: application/json" \
  -d '{"message": "What is in my notes?"}'
```

## Troubleshooting

### Can't Connect to Windows
```bash
# Check Windows IP
cat /etc/resolv.conf | grep nameserver | awk '{print $2}'

# Test connectivity
curl http://WINDOWS_IP:8881/health
curl http://WINDOWS_IP:8882/health

# Check Windows Firewall allows ports 8881, 8882
```

### Weaviate Errors
```bash
# Check Weaviate logs
docker-compose logs weaviate

# Make sure old collection was deleted
# It can't have Ollama vectorizer config
```

### Slow Performance
- First embedding/generation loads models (1-2 min)
- Subsequent requests use NPU (should be fast)
- Check Windows Nexa servers are using --npu flag

### Backend Can't Import nexa_client
```bash
# Make sure file exists
ls backend/services/nexa_client.py

# Check for syntax errors
python3 backend/services/nexa_client.py
```

## Summary

**Windows Side:**
- Run: `nexa server NexaAI/embeddinggemma-300m-npu --npu --port 8881`
- Run: `nexa server NexaAI/Llama3.2-3B-NPU-Turbo --npu --port 8882`

**WSL2 Side:**
- Backend calls Windows servers via OpenAI-compatible APIs
- Weaviate uses manual embeddings (no Ollama modules)
- Much simpler and more stable than Python SDK!
