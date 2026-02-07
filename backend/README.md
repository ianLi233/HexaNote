- This is a self-hosted solution, so there are no API usage fees or per-token costs

## Model Choice Justification (Windows + WSL Benchmark)

We benchmarked two local models on the same machine using different hardware acceleration paths.

### Performance Comparison

| Model | Runtime Environment | Hardware Used | Generation Speed |
|------|---------------------|---------------|------------------|
| **NexaAI Llama3.2-3B-NPU-Turbo** | Native Windows (Nexa SDK) | NPU Acceleration | ~25.6 tokens/sec |
| **Ollama llama3.2:1b** | Docker inside WSL | CPU | ~16 tokens/sec |

### Key Observations

- The 3B NPU-Turbo model is ~60% faster despite being 3× larger than the CPU model running in WSL

### Conclusion

We selected **NexaAI Llama3.2-3B-NPU-Turbo** because it provides significantly better performance and responsiveness on our hardware, making it more suitable for interactive AI workloads than the smaller CPU-bound model.


## How Search Works

The system answers questions using three stages: Semantic Search → RAG → Deep Search.  
Each stage has a different role, filtering logic, and time cost.

---

### 1. Semantic Search (Finding Relevant Knowledge)

Semantic search retrieves note content based on meaning, not keywords.

Embedding model used  
- djuna/jina-embeddings-v2-small-en-Q5_K_M-GGUF  
- Produces 768-dimensional embeddings for both queries and note chunks

What happens  
1. The user query is converted into an embedding vector  
2. The system compares it against embeddings of all stored note chunks  
3. A distance threshold of 0.3 is applied  
   - Only chunks with semantic distance ≤ 0.3 are kept  
   - This removes loosely related content  
4. The remaining chunks are passed to a re-ranking model for deeper relevance scoring  

Typical timing

| Step | Time |
|------|------|
| Query embedding | ~1–2 seconds |
| Vector similarity search | ~0 seconds |
| Re-ranking | ~3 seconds |

Total Semantic Search Time: ~4–5 seconds

Result: A small set of chunks that are semantically close to the query.

---

### 2. RAG (Retrieval-Augmented Generation)

RAG generates the final answer using the retrieved chunks as grounded context.

What happens  
1. The top re-ranked chunks are inserted into the LLM prompt  
2. A re-rank confidence threshold of 0.5 is applied  
   - Only chunks with re-rank score ≥ 0.5 are used  
   - This ensures only high-confidence, highly relevant context is provided  
3. The LLM generates a response using this filtered context  

Typical timing

| Step | Time |
|------|------|
| Prompt assembly | negligible |
| LLM generation | ~12–20 seconds |

Total RAG Time: ~12–20 seconds

Result: A grounded answer that relies on high-relevance excerpts instead of guessing.

---

### 3. Deep Search (Search Within a Specific Note)

Deep search is used for follow-up questions after a relevant note has already been identified.

What happens  
1. Instead of searching the entire database again, the system searches within one specific note  
2. It finds the most relevant chunk inside that note  
3. A context window (neighboring chunks) is retrieved for better continuity  

Typical timing

| Step | Time |
|------|------|
| Search within note | < 1 second |
| Context window retrieval | negligible |

Total Deep Search Time: ~0.5–1 second

Result: Faster, more focused follow-up answers using detailed context from a single source.

---

### Overall Flow Timing

| Stage | Purpose | Threshold | Typical Time |
|------|---------|-----------|--------------|
| Semantic Search | Find relevant chunks by meaning | Distance ≤ 0.3 | ~4–5 s |
| RAG Filtering | Keep only high-confidence chunks | Re-rank ≥ 0.5 | included above |
| RAG Generation | Produce grounded answer | — | ~12–20 s |
| Deep Search | Focused search inside one note | — | ~1 s |

---

### Summary

- Embeddings model: djuna/jina-embeddings-v2-small-en-Q5_K_M-GGUF  
- Semantic threshold: 0.3 (filters weak matches)  
- RAG re-rank threshold: 0.5 (keeps only strong context)  
- Deep search speeds up follow-ups by narrowing scope  



## Local AI Setup (Windows + WSL)

This project runs all AI models locally on Windows using Nexa (with NPU acceleration), while the backend runs inside WSL using Docker.

---

### Windows (ARM64) — Run Nexa

Nexa handles embeddings, reranking, and LLM inference.

Requirements:
- Python 3.11–3.13  
- ARM64 Python distribution (required for optimal NPU performance)

Pull the models and start the Nexa server:

```bash
nexa pull NexaAI/jina-v2-rerank-npu
nexa pull NexaAI/Llama3.2-3B-NPU-Turbo

nexa serve --host 0.0.0.0:8883 --keepalive 60000

```
### Make sure Docker is installed and running in WSL

# Get the Windows host IP so the backend can reach Nexa
export WINDOWS_HOST=$(ip route | grep default | awk '{print $3}')
echo $WINDOWS_HOST
# Example: 172.23.224.1 (update backend config if different)

cd ..

docker compose up -d --build backend

# View backend logs
docker logs hexanote-backend -f


