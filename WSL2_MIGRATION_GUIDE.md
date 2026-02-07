# WSL2 Backend Migration Guide

## Overview

You need to modify the backend code to:
1. Remove Ollama dependencies from Weaviate
2. Call Windows Nexa SDK server for embeddings and generation
3. Update configuration to point to Windows host

## Step 1: Get Windows Host IP from WSL2

```bash
# In WSL2, get the Windows host IP
cat /etc/resolv.conf | grep nameserver | awk '{print $2}'

# This will give you the Windows host IP (e.g., 172.24.160.1)
# Save this IP - you'll need it for configuration
```

## Step 2: Test Connectivity to Windows Server

```bash
# Test if you can reach the Windows Nexa server from WSL2
# Replace WINDOWS_IP with the IP from Step 1
curl http://WINDOWS_IP:8888/health

# Example:
# curl http://172.24.160.1:8888/health
```

## Step 3: Update Backend Configuration

### File: `backend/config.py`

Add these new settings:

```python
class Settings(BaseSettings):
    # ... existing settings ...

    # Nexa SDK Server (running on Windows)
    nexa_url: str = "http://172.24.160.1:8888"  # Replace with your Windows IP
    nexa_embedding_model: str = "NexaAI/embeddinggemma-300m-npu"
    nexa_generation_model: str = "NexaAI/Llama3.2-3B-NPU-Turbo"

    # Keep Ollama settings commented for reference
    # ollama_url: str = "http://ollama:11434"
    # ollama_embedding_model: str = "mxbai-embed-large:latest"
    # ollama_generation_model: str = "llama3.2:1b"
```

## Step 4: Create Nexa Client Service

Create a new file `backend/services/nexa_client.py`:

```python
"""
Nexa SDK client for embeddings and generation.
Connects to Nexa SDK server running on Windows.
"""
import requests
from typing import List
from config import settings


class NexaClient:
    """Client for Nexa SDK server."""

    def __init__(self):
        self.base_url = settings.nexa_url
        self.embed_model = settings.nexa_embedding_model
        self.gen_model = settings.nexa_generation_model

    def is_healthy(self) -> bool:
        """Check if Nexa server is reachable."""
        try:
            response = requests.get(f"{self.base_url}/health", timeout=5)
            return response.status_code == 200
        except Exception as e:
            print(f"Nexa server health check failed: {e}")
            return False

    def embed_texts(self, texts: List[str]) -> List[List[float]]:
        """
        Generate embeddings for a list of texts.

        Args:
            texts: List of strings to embed

        Returns:
            List of embedding vectors

        Raises:
            RuntimeError: If embedding request fails
        """
        try:
            response = requests.post(
                f"{self.base_url}/v1/embeddings",
                json={
                    "input": texts,
                    "model": self.embed_model
                },
                timeout=120
            )
            response.raise_for_status()
            data = response.json()

            # Extract embeddings in order
            embeddings = [item["embedding"] for item in sorted(data["data"], key=lambda x: x["index"])]
            return embeddings
        except Exception as e:
            raise RuntimeError(f"Failed to generate embeddings: {e}")

    def generate(self, prompt: str, max_tokens: int = 512) -> str:
        """
        Generate text using LLM.

        Args:
            prompt: Input prompt
            max_tokens: Maximum tokens to generate

        Returns:
            Generated text

        Raises:
            RuntimeError: If generation request fails
        """
        try:
            response = requests.post(
                f"{self.base_url}/api/generate",
                json={
                    "model": self.gen_model,
                    "prompt": prompt,
                    "stream": False,
                    "max_tokens": max_tokens
                },
                timeout=120
            )
            response.raise_for_status()
            data = response.json()
            return data.get("response", "")
        except Exception as e:
            raise RuntimeError(f"Failed to generate response: {e}")


# Global instance
nexa_client = None


def get_nexa_client() -> NexaClient:
    """Get or create Nexa client instance."""
    global nexa_client
    if nexa_client is None:
        nexa_client = NexaClient()
    return nexa_client
```

## Step 5: Major Changes to weaviate_service.py

The key changes:
1. Remove Ollama vectorizer from Weaviate collection config
2. Manually generate embeddings using Nexa client before inserting
3. Replace Ollama generation with Nexa generation

Key modifications:

### In `ensure_collection()` method:

```python
# OLD - with Ollama vectorizer
self.client.collections.create(
    name=self.collection_name,
    vectorizer_config=wvc.config.Configure.Vectorizer.text2vec_ollama(...),
    generative_config=wvc.config.Configure.Generative.ollama(...),
    ...
)

# NEW - no vectorizer, manual embeddings
self.client.collections.create(
    name=self.collection_name,
    vectorizer_config=wvc.config.Configure.Vectorizer.none(),  # Manual embeddings
    properties=[...],
)
```

### In `index_note()` method:

```python
# Need to manually embed content before inserting
from services.nexa_client import get_nexa_client

nexa = get_nexa_client()

for i, chunk in enumerate(content_chunks):
    # Generate embedding for chunk using Nexa
    chunk_text = f"{title}\n\n{chunk}"  # Combine title and content
    embeddings = nexa.embed_texts([chunk_text])
    embedding_vector = embeddings[0]

    properties = {
        "note_id": note_id,
        "title": title,
        "content": chunk,
        # ... other properties
    }

    # Insert with explicit vector
    weaviate_uuid = note_collection.data.insert(
        properties=properties,
        vector=embedding_vector  # Pass embedding explicitly
    )
```

### In `generative_search()` method:

```python
# Replace Ollama API call with Nexa client
from services.nexa_client import get_nexa_client

nexa = get_nexa_client()

# Build prompt
full_prompt = f"""..."""

# Call Nexa instead of Ollama
generated_text = nexa.generate(full_prompt, max_tokens=512)
```

## Step 6: Update Docker Compose

### File: `docker-compose.yml`

Remove or comment out Ollama service, update Weaviate config:

```yaml
services:
  # Comment out Ollama - no longer needed
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
      # Remove Ollama module configs
      # ENABLE_MODULES: 'text2vec-ollama,generative-ollama'
      # OLLAMA_API_ENDPOINT: 'http://ollama:11434'
      MODULES_CLIENT_TIMEOUT: '300s'
      QUERY_MAXIMUM_RESULTS: 100
      AUTHENTICATION_ANONYMOUS_ACCESS_ENABLED: 'false'
      AUTHENTICATION_APIKEY_ENABLED: 'true'
      AUTHENTICATION_APIKEY_ALLOWED_KEYS: 'user-a-key,user-b-key'
      AUTHENTICATION_APIKEY_USERS: 'user-a,user-b'
      AUTHORIZATION_ENABLE_RBAC: 'true'
      AUTHORIZATION_RBAC_ROOT_USERS: 'user-a'
    # Remove depends_on ollama
    # depends_on:
    #   - ollama

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
      - NEXA_URL=http://172.24.160.1:8888  # Your Windows IP
      - DEBUG=false
    depends_on:
      - weaviate
    restart: on-failure:0
    # Allow access to Windows host
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
# Nexa SDK Server (Windows)
NEXA_URL=http://172.24.160.1:8888  # Replace with your Windows host IP
NEXA_EMBEDDING_MODEL=NexaAI/embeddinggemma-300m-npu
NEXA_GENERATION_MODEL=NexaAI/Llama3.2-3B-NPU-Turbo

# Weaviate
WEAVIATE_URL=http://weaviate:8080
WEAVIATE_API_KEY=user-a-key
```

## Step 8: Migration Plan

1. **Backup your database:**
   ```bash
   cp ./data/hexanote.db ./data/hexanote.db.backup
   ```

2. **Stop current services:**
   ```bash
   docker-compose down
   ```

3. **Update code** with the changes above

4. **Start Weaviate only** (to clear and recreate collection):
   ```bash
   docker-compose up weaviate -d
   ```

5. **Delete old collection** (it has Ollama vectorizer config):
   ```bash
   python -c "
   import weaviate
   from weaviate.classes.init import Auth
   client = weaviate.connect_to_local(
       host='localhost',
       port=8080,
       auth_credentials=Auth.api_key('user-a-key')
   )
   try:
       client.collections.delete('Note')
       print('Deleted old collection')
   except:
       print('No collection to delete')
   client.close()
   "
   ```

6. **Start all services:**
   ```bash
   docker-compose up -d
   ```

7. **Reindex all notes** (this will use Nexa for embeddings):
   ```bash
   curl -X POST http://localhost:8001/api/v1/notes/reindex
   ```

## Testing

After migration:

```bash
# Test health
curl http://localhost:8001/api/v1/health

# Test search (should use Nexa embeddings)
curl "http://localhost:8001/api/v1/notes/search/semantic?q=test&limit=5"

# Test chat (should use Nexa generation)
curl -X POST http://localhost:8001/api/v1/chat/query \
  -H "Content-Type: application/json" \
  -d '{"message": "What is in my notes?"}'
```

## Troubleshooting

### Can't connect to Windows host
- Check Windows Firewall allows port 8888
- Verify Windows IP with `cat /etc/resolv.conf | grep nameserver | awk '{print $2}'`
- Test with `curl http://WINDOWS_IP:8888/health`

### Weaviate errors
- Make sure you deleted the old collection with Ollama vectorizer
- Check Weaviate logs: `docker-compose logs weaviate`

### Slow performance
- First request loads models (can take 1-2 minutes)
- Subsequent requests should be faster with NPU acceleration
