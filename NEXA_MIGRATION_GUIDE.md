# HexaNote Migration to Nexa

This guide explains the migration from Ollama to Nexa inference server.

## What Changed

### Architecture
- **Before**: Ollama container for embeddings + LLM
- **After**: Nexa server on Windows host (172.23.224.1:8883)

### Key Changes
1. Removed Ollama Docker container
2. Updated Weaviate to use manual embeddings (no vectorizer module)
3. Backend now uses Nexa client for embeddings and generation

## Migration Steps

### 1. Ensure Nexa is Running on Windows

Make sure your Nexa server is running on Windows host at `172.23.224.1:8883`

```bash
# From Windows PowerShell
nexa server start
```

Verify it's accessible from WSL:
```bash
# From WSL
curl http://172.23.224.1:8883/v1/models
```

### 2. Stop Existing Containers

```bash
cd /home/qc_de/HexaNote
docker compose down
```

### 3. Clean Weaviate Data (IMPORTANT!)

Since we changed from Ollama-based vectorization to manual embeddings, you need to recreate the Weaviate collection:

```bash
# Remove Weaviate data volume
docker volume rm hexanote_weaviate_data
```

### 4. Start New Stack

```bash
# Start Weaviate and backend (Ollama is now removed)
docker compose up -d
```

### 5. Initialize Database Schema

```bash
# Wait for containers to be healthy
sleep 10

# Run migration script
python3 5-migrate-to-notes_run-after-docker-compose-up-d.py
```

### 6. Verify Health

```bash
# Check backend health
curl http://localhost:8001/api/v1/health

# Check if backend can reach Nexa
docker logs hexanote-backend --tail 50
```

You should see:
- "Connected to Weaviate successfully"
- "Collection 'Note' already exists" or "Created collection 'Note'"

### 7. Reindex Existing Notes

If you have existing notes in SQLite, you need to reindex them with Nexa embeddings:

```bash
# From frontend, click "Reindex" button in Chat tab
# OR via API:
curl -X POST http://localhost:8001/api/v1/notes/reindex
```

## Configuration

### Environment Variables (in docker-compose.yml)

```yaml
environment:
  - NEXA_URL=http://172.23.224.1:8883
  - NEXA_EMBED_URL=http://172.23.224.1:8883
  - NEXA_LLM_URL=http://172.23.224.1:8883
  - NEXA_EMBEDDING_MODEL=djuna/jina-embeddings-v2-base-en-Q5_K_M-GGUF:Q5_K_M
  - NEXA_GENERATION_MODEL=NexaAI/Llama3.2-3B-NPU-Turbo
```

### Nexa Models

**Embedding Model**: `djuna/jina-embeddings-v2-base-en-Q5_K_M-GGUF:Q5_K_M`
- Used for semantic search
- Generates 768-dimensional embeddings

**Generation Model**: `NexaAI/Llama3.2-3B-NPU-Turbo`
- Used for RAG chat responses
- NPU-accelerated for fast inference

## Troubleshooting

### Backend Can't Reach Nexa

**Error**: "Failed to generate embeddings" or "Connection refused"

**Solution**:
1. Verify Nexa is running on Windows: `nexa server status`
2. Check WSL can reach Windows host:
   ```bash
   export WINDOWS_HOST=$(ip route | grep default | awk '{print $3}')
   echo $WINDOWS_HOST  # Should be 172.23.224.1
   curl http://$WINDOWS_HOST:8883/v1/models
   ```
3. Check Windows Firewall allows port 8883

### Weaviate Collection Error

**Error**: "Collection already exists with different vectorizer"

**Solution**: Remove Weaviate data and recreate:
```bash
docker compose down
docker volume rm hexanote_weaviate_data
docker compose up -d
```

### Semantic Search Returns No Results

**Cause**: Notes were indexed with old Ollama embeddings

**Solution**: Reindex all notes:
```bash
curl -X POST http://localhost:8001/api/v1/notes/reindex
```

## API Changes

No API endpoint changes! The migration is transparent to the frontend.

All endpoints work the same:
- `GET /api/v1/notes/search/semantic?q=query`
- `POST /api/v1/chat/query`
- `POST /api/v1/notes/reindex`

## Performance Notes

- Nexa uses NPU acceleration (faster than CPU Ollama)
- Embedding generation: ~100ms per note
- Chat generation: ~1-2s per response
- Batch embedding is supported for faster reindexing

## Rollback

To rollback to Ollama (not recommended):

1. Restore original `docker-compose.yml` from git
2. Restore original `backend/config.py` from git
3. Restore original `backend/services/weaviate_service.py` from git
4. Remove Weaviate data: `docker volume rm hexanote_weaviate_data`
5. Restart: `docker compose up -d`
6. Pull models:
   ```bash
   docker exec -it ollama ollama pull mxbai-embed-large
   docker exec -it ollama ollama pull llama3.2:1b
   ```
7. Reindex notes

## Support

If you encounter issues:
1. Check logs: `docker logs hexanote-backend --tail 100`
2. Check Nexa status: `nexa server status`
3. Verify connectivity: `curl http://172.23.224.1:8883/v1/models`
