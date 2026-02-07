"""
Weaviate service for vector storage and RAG operations with chunking support.
Migrated from LocalRAG Python scripts (1-create-collection.py, 3-semantic_search.py, 4-generative_search.py).
Now uses Nexa for embeddings and generation instead of Ollama.
"""
import weaviate
import weaviate.classes as wvc
import weaviate.classes.config as wc
from weaviate.classes.init import Auth, AdditionalConfig, Timeout
from typing import List, Dict, Optional
import json
import requests

from config import settings
from services.nexa_client import get_nexa_client


def chunk_text(text: str, chunk_size: int = 1500, overlap: int = 200) -> List[str]:
    """
    Split text into overlapping chunks to handle long documents.

    Args:
        text: Text to chunk
        chunk_size: Maximum characters per chunk
        overlap: Number of characters to overlap between chunks

    Returns:
        List of text chunks
    """
    if len(text) <= chunk_size:
        return [text]

    chunks = []
    start = 0

    while start < len(text):
        end = start + chunk_size

        # If this isn't the last chunk, try to break at a sentence or word boundary
        if end < len(text):
            # Look for sentence boundary (., !, ?) in the last 100 chars
            for sep in ['. ', '! ', '? ', '\n\n', '\n']:
                last_sep = text[end-100:end].rfind(sep)
                if last_sep != -1:
                    end = end - 100 + last_sep + len(sep)
                    break

        chunk = text[start:end].strip()
        if chunk:
            chunks.append(chunk)

        # Move start forward, with overlap
        start = end - overlap if end < len(text) else end

    return chunks


class WeaviateService:
    """Service for interacting with Weaviate vector database."""

    def __init__(self):
        """Initialize Weaviate client with configuration."""
        self.client = None
        self.collection_name = "Note"
        self.nexa = get_nexa_client()  # Get Nexa client for embeddings and generation
        self._connect()

    def _connect(self):
        """Establish connection to Weaviate with retry logic."""
        import time
        max_retries = 10
        retry_delay = 3

        for attempt in range(max_retries):
            try:
                # For docker networking (weaviate:8080)
                self.client = weaviate.connect_to_local(
                    host="weaviate",
                    port=8080,
                    auth_credentials=Auth.api_key(settings.weaviate_api_key),
                    additional_config=AdditionalConfig(
                        timeout=Timeout(
                            init=settings.weaviate_timeout_init,
                            query=settings.weaviate_timeout_query,
                            insert=settings.weaviate_timeout_insert
                        )
                    )
                )
                print(f"✓ Connected to Weaviate successfully")
                return
            except Exception as e:
                if attempt < max_retries - 1:
                    print(f"⏳ Weaviate not ready (attempt {attempt + 1}/{max_retries}), retrying in {retry_delay}s...")
                    time.sleep(retry_delay)
                else:
                    print(f"❌ Failed to connect to Weaviate after {max_retries} attempts: {e}")
                    print("   Make sure Weaviate container is running and healthy")
                    raise

    def ensure_collection(self) -> bool:
        """
        Create Note collection if it doesn't exist.
        Based on 1-create-collection.py logic, adapted for notes with chunking support.

        Returns:
            bool: True if collection exists or was created successfully
        """
        try:
            # Check if collection already exists
            try:
                self.client.collections.get(self.collection_name)
                print(f"Collection '{self.collection_name}' already exists")
                return True
            except:
                pass

            # Create Note collection with chunking support
            # Use text2vec-none because we handle embeddings manually via Nexa
            self.client.collections.create(
                name=self.collection_name,
                vectorizer_config=wvc.config.Configure.Vectorizer.none(),
                properties=[
                    wc.Property(
                        name="note_id",
                        data_type=wc.DataType.TEXT,
                        skip_vectorization=True,
                    ),
                    wc.Property(
                        name="title",
                        data_type=wc.DataType.TEXT,
                    ),
                    wc.Property(
                        name="content",
                        data_type=wc.DataType.TEXT,
                    ),
                    wc.Property(
                        name="chunk_index",
                        data_type=wc.DataType.INT,
                        skip_vectorization=True,
                    ),
                    wc.Property(
                        name="total_chunks",
                        data_type=wc.DataType.INT,
                        skip_vectorization=True,
                    ),
                    wc.Property(
                        name="tags",
                        data_type=wc.DataType.TEXT,
                    ),
                    wc.Property(
                        name="created_at",
                        data_type=wc.DataType.TEXT,
                        skip_vectorization=True,
                    ),
                    wc.Property(
                        name="updated_at",
                        data_type=wc.DataType.TEXT,
                        skip_vectorization=True,
                    ),
                ],
            )
            print(f"Created collection '{self.collection_name}' with chunking support")
            return True
        except Exception as e:
            print(f"Error ensuring collection: {e}")
            return False

    def index_note(
        self,
        note_id: str,
        title: str,
        content: str,
        tags: List[str],
        created_at: str,
        updated_at: str
    ) -> Optional[str]:
        """
        Add or update note in vector database with automatic chunking.
        Long documents are split into chunks to fit within embedding model context limits.

        Args:
            note_id: Unique note identifier
            title: Note title
            content: Markdown content
            tags: List of tags
            created_at: Creation timestamp
            updated_at: Update timestamp

        Returns:
            str: Weaviate UUID of first chunk if successful, None otherwise
        """
        try:
            note_collection = self.client.collections.get(self.collection_name)

            # First, delete any existing chunks for this note
            self.delete_note(note_id)

            # Split content into chunks
            content_chunks = chunk_text(content)
            total_chunks = len(content_chunks)

            first_uuid = None

            # Generate embeddings for all chunks at once using Nexa
            chunk_texts = [f"{title}\n\n{chunk}" for chunk in content_chunks]  # Include title for better context
            try:
                embeddings = self.nexa.embed_texts(chunk_texts)
                print(f"Generated {len(embeddings)} embeddings via Nexa for note {note_id}")
            except Exception as e:
                print(f"Error generating embeddings for note {note_id}: {e}")
                return None

            for i, (chunk, embedding) in enumerate(zip(content_chunks, embeddings)):
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

                # Insert chunk with manual embedding vector
                weaviate_uuid = note_collection.data.insert(
                    properties=properties,
                    vector=embedding  # Manually provide embedding from Nexa
                )
                if i == 0:
                    first_uuid = str(weaviate_uuid)

            print(f"Indexed note {note_id} as {total_chunks} chunk(s)")
            return first_uuid
        except Exception as e:
            print(f"Error indexing note {note_id}: {e}")
            return None

    def delete_note(self, note_id: str) -> bool:
        """
        Remove all chunks of a note from vector database.

        Args:
            note_id: Note identifier to delete

        Returns:
            bool: True if deleted successfully
        """
        try:
            note_collection = self.client.collections.get(self.collection_name)

            # Use delete_many with filter instead of fetching all objects first
            # This is more efficient and avoids pagination limits
            result = note_collection.data.delete_many(
                where=wvc.query.Filter.by_property("note_id").equal(note_id)
            )

            deleted_count = result.successful if hasattr(result, 'successful') else 0

            if deleted_count > 0:
                print(f"Deleted {deleted_count} chunk(s) for note: {note_id}")
                return True
            else:
                print(f"No chunks found to delete for note: {note_id}")
                return False
        except Exception as e:
            print(f"Error deleting note {note_id}: {e}")
            return False

    def semantic_search(
        self,
        query: str,
        limit: int = 5,
        tag_filter: Optional[List[str]] = None
    ) -> List[Dict]:
        """
        Search notes using semantic similarity with chunk deduplication.
        Retrieves chunks and groups them by note_id to return complete notes.
        Now uses Nexa for embedding generation.

        Args:
            query: Search query string
            limit: Maximum number of notes to return (not chunks)
            tag_filter: Optional list of tags to filter by

        Returns:
            List of matching notes with relevance scores
        """
        try:
            note_collection = self.client.collections.get(self.collection_name)

            # Generate query embedding using Nexa
            try:
                query_embeddings = self.nexa.embed_texts([query])
                query_vector = query_embeddings[0]
                print(f"Generated query embedding via Nexa (dim: {len(query_vector)})")
            except Exception as e:
                print(f"Error generating query embedding: {e}")
                return []

            # Build filters if tag_filter provided
            filters = None
            if tag_filter:
                tag_conditions = [
                    wvc.query.Filter.by_property("tags").contains_any(tag)
                    for tag in tag_filter
                ]
                if len(tag_conditions) == 1:
                    filters = tag_conditions[0]
                else:
                    filters = wvc.query.Filter.any_of(tag_conditions)

            # Retrieve more chunks than needed to ensure we get enough unique notes
            # Use near_vector instead of near_text since we have manual embeddings
            response = note_collection.query.near_vector(
                near_vector=query_vector,
                limit=limit * 3,  # Get 3x chunks to account for multi-chunk notes
                filters=filters,
                return_metadata=wvc.query.MetadataQuery(distance=True)
            )

            # Deduplicate and aggregate by note_id
            notes_dict = {}
            for obj in response.objects:
                note_id = str(obj.properties.get("note_id"))
                if note_id not in notes_dict:
                    # Use the first chunk's data (usually most relevant)
                    notes_dict[note_id] = {
                        "note_id": note_id,
                        "title": obj.properties.get("title", ""),
                        "content": obj.properties.get("content", ""),
                        "tags": obj.properties.get("tags", "").split(", ") if obj.properties.get("tags") else [],
                        "created_at": obj.properties.get("created_at", ""),
                        "updated_at": obj.properties.get("updated_at", ""),
                        "relevance_score": 1 - obj.metadata.distance if obj.metadata.distance else 0.0,
                        "chunk_index": obj.properties.get("chunk_index", 0)
                    }

            # Sort by relevance and return top results
            results = sorted(notes_dict.values(), key=lambda x: x["relevance_score"], reverse=True)
            return results[:limit]
        except Exception as e:
            print(f"Error in semantic search: {e}")
            return []

    def search_within_note(
        self,
        note_id: str,
        query: str,
        window_chunks: int = 2
    ) -> Optional[Dict]:
        """
        Search within a specific note and return a context window around the best match.

        Args:
            note_id: The note to search within
            query: Search query
            window_chunks: Number of chunks to include before and after the best match

        Returns:
            Dict with combined context text and metadata, or None if note not found
        """
        try:
            print(f"\n🔍 [SEARCH_WITHIN_NOTE] note_id={note_id}, query='{query}'")
            note_collection = self.client.collections.get(self.collection_name)

            # Generate query embedding using Nexa
            try:
                query_embeddings = self.nexa.embed_texts([query])
                query_vector = query_embeddings[0]
            except Exception as e:
                print(f"Error generating query embedding: {e}")
                return None

            # Search within this specific note
            response = note_collection.query.near_vector(
                near_vector=query_vector,
                limit=50,  # Get all chunks for this note
                filters=wvc.query.Filter.by_property("note_id").equal(note_id),
                return_metadata=wvc.query.MetadataQuery(distance=True)
            )

            if not response.objects:
                print(f"❌ [SEARCH_WITHIN_NOTE] No chunks found for note_id={note_id}")
                return None

            print(f"✓ [SEARCH_WITHIN_NOTE] Found {len(response.objects)} chunks")

            # Find the best matching chunk
            best_match = response.objects[0]
            best_chunk_index = best_match.properties.get("chunk_index", 0)
            total_chunks = best_match.properties.get("total_chunks", 1)
            title = best_match.properties.get("title", "")

            print(f"📍 [SEARCH_WITHIN_NOTE] Best match: chunk {best_chunk_index}/{total_chunks}, title='{title}'")
            print(f"   Distance: {best_match.metadata.distance if hasattr(best_match.metadata, 'distance') else 'N/A'}")
            print(f"   Content preview: {best_match.properties.get('content', '')[:100]}...")

            # Calculate window range
            start_index = max(0, best_chunk_index - window_chunks)
            end_index = min(total_chunks - 1, best_chunk_index + window_chunks)
            print(f"📊 [SEARCH_WITHIN_NOTE] Context window: chunks {start_index}-{end_index}")

            # Fetch all chunks in the window range
            all_chunks = note_collection.query.fetch_objects(
                filters=wvc.query.Filter.by_property("note_id").equal(note_id),
                limit=total_chunks
            )

            # Sort by chunk_index and extract the window
            sorted_chunks = sorted(
                all_chunks.objects,
                key=lambda x: x.properties.get("chunk_index", 0)
            )

            window_chunks_list = [
                chunk for chunk in sorted_chunks
                if start_index <= chunk.properties.get("chunk_index", 0) <= end_index
            ]

            # Combine chunks into context
            context_text = "\n\n".join([
                chunk.properties.get("content", "")
                for chunk in window_chunks_list
            ])

            return {
                "context": context_text,
                "title": title,
                "chunk_range": f"{start_index}-{end_index}",
                "total_chunks": total_chunks,
                "best_chunk_index": best_chunk_index
            }

        except Exception as e:
            print(f"Error searching within note {note_id}: {e}")
            return None

    def generative_search(
        self,
        query: str,
        limit: int = 5,
        tag_filter: Optional[List[str]] = None,
        additional_context: Optional[str] = None,
        note_id_filter: Optional[str] = None  # NEW: Limit search to specific note
    ) -> Dict:
        """
        Generate response using RAG with distance filtering and re-ranking.

        Pipeline:
        1. Retrieve chunks with distance scores (optionally scoped to a note)
        2. Filter by distance threshold (< 0.3)
        3. Re-rank top 4 chunks using Nexa cross-encoder
        4. Filter by rerank threshold (> 0.5), max 2 chunks
        5. Generate response using selected chunks

        Args:
            query: User's question
            limit: Maximum number of unique notes to use as context
            tag_filter: Optional list of tags to filter by
            additional_context: Additional context to include (from clicked note)
            note_id_filter: Optional note ID to limit search scope (for focused conversation)

        Returns:
            Dict with AI response and context notes
        """
        try:
            note_collection = self.client.collections.get(self.collection_name)

            # Build filters for tag_filter and note_id_filter
            filters = None
            filter_conditions = []

            # Add note_id filter if provided (limit search to specific note)
            if note_id_filter:
                filter_conditions.append(
                    wvc.query.Filter.by_property("note_id").equal(note_id_filter)
                )

            # Add tag filter if provided
            if tag_filter:
                tag_conditions = [
                    wvc.query.Filter.by_property("tags").contains_any(tag)
                    for tag in tag_filter
                ]
                if len(tag_conditions) == 1:
                    filter_conditions.append(tag_conditions[0])
                else:
                    filter_conditions.append(wvc.query.Filter.any_of(tag_conditions))

            # Combine filters
            if len(filter_conditions) == 1:
                filters = filter_conditions[0]
            elif len(filter_conditions) > 1:
                filters = wvc.query.Filter.all_of(filter_conditions)

            import time
            rag_start_time = time.time()

            scope_msg = f" [SCOPED TO NOTE: {note_id_filter[:8]}...]" if note_id_filter else ""
            print(f"\n🤖 [GENERATIVE_SEARCH] query='{query}', limit={limit}{scope_msg}")

            # Generate query embedding using Nexa
            embed_start = time.time()
            try:
                query_embeddings = self.nexa.embed_texts([query])
                query_vector = query_embeddings[0]
                embed_time = time.time() - embed_start
                print(f"⏱️  [TIMING] Query embedding: {embed_time:.2f}s")
                print(f"Generated query embedding via Nexa (dim: {len(query_vector)})")
            except Exception as e:
                print(f"Error generating query embedding: {e}")
                return {
                    "response": "Error generating query embedding.",
                    "context_notes": []
                }

            # Step 1: Retrieve chunks with distance scores
            search_start = time.time()
            query_response = note_collection.query.near_vector(
                near_vector=query_vector,
                limit=limit * 4,  # Over-retrieve to have options after filtering
                filters=filters,
                return_metadata=wvc.query.MetadataQuery(distance=True)
            )

            search_time = time.time() - search_start
            print(f"⏱️  [TIMING] Vector search: {search_time:.2f}s")

            if not query_response.objects:
                return {
                    "response": "No relevant notes found to answer your question.",
                    "context_notes": []
                }

            # Step 2: Filter by distance threshold and log all chunks
            DISTANCE_THRESHOLD = 0.3  # Lower distance = better match (0.0 = perfect, 2.0 = opposite)
            print(f"📏 [GENERATIVE_SEARCH] Distance threshold: {DISTANCE_THRESHOLD}")
            print(f"📚 [GENERATIVE_SEARCH] Retrieved {len(query_response.objects)} chunks before filtering:")

            filtered_chunks = []
            for i, obj in enumerate(query_response.objects):
                distance = obj.metadata.distance if hasattr(obj.metadata, 'distance') else None
                note_id = str(obj.properties.get("note_id"))
                title = obj.properties.get("title", "")
                chunk_idx = obj.properties.get("chunk_index", 0)

                relevance_emoji = "✓" if (distance is not None and distance < DISTANCE_THRESHOLD) else "✗"
                distance_str = f"{distance:.4f}" if distance is not None else "N/A"
                print(f"   {relevance_emoji} [{i+1}] distance={distance_str}, note_id={note_id[:8]}, title='{title}', chunk={chunk_idx}")

                if distance is not None and distance < DISTANCE_THRESHOLD:
                    filtered_chunks.append((obj, distance))

            print(f"✓ [GENERATIVE_SEARCH] {len(filtered_chunks)} chunks passed distance threshold")

            # If no chunks pass threshold, keep top 10 best matches
            if not filtered_chunks:
                print(f"⚠️  [GENERATIVE_SEARCH] No chunks passed threshold, keeping top 10 best matches")
                filtered_chunks = [
                    (obj, obj.metadata.distance if hasattr(obj.metadata, 'distance') else 1.0)
                    for obj in query_response.objects[:10]
                ]

            # Step 3: Re-rank only top 10 chunks (to reduce re-ranking time)
            # Sort by distance and take top 10
            filtered_chunks.sort(key=lambda x: x[1])  # Lower distance = better
            chunks_to_rerank = filtered_chunks[:10]

            rerank_start = time.time()
            print(f"🔄 [GENERATIVE_SEARCH] Re-ranking top {len(chunks_to_rerank)} chunks with Nexa...")
            try:
                # Extract content for re-ranking (include title for better relevance scoring)
                documents = [
                    f"{obj.properties.get('title', '')}\n\n{obj.properties.get('content', '')}"
                    for obj, _ in chunks_to_rerank
                ]

                # Call Nexa re-ranker
                rerank_scores = self.nexa.rerank(query, documents)
                rerank_time = time.time() - rerank_start
                print(f"⏱️  [TIMING] Re-ranking: {rerank_time:.2f}s")
                print(f"✓ [GENERATIVE_SEARCH] Re-ranking complete, got {len(rerank_scores)} scores")
                print(f"🔍 [DEBUG] Raw rerank scores: {rerank_scores}")  # Show all scores

                # Combine chunks with re-rank scores
                reranked_chunks = [
                    (obj, distance, rerank_score)
                    for (obj, distance), rerank_score in zip(chunks_to_rerank, rerank_scores)
                ]

                # Log re-ranked results
                print(f"📊 [GENERATIVE_SEARCH] Re-ranked chunks (sorted by re-rank score):")
                sorted_reranked = sorted(reranked_chunks, key=lambda x: x[2], reverse=True)  # Higher score = better
                for i, (obj, distance, rerank_score) in enumerate(sorted_reranked[:10]):
                    note_id = str(obj.properties.get("note_id"))
                    title = obj.properties.get("title", "")
                    chunk_idx = obj.properties.get("chunk_index", 0)
                    print(f"   [{i+1}] rerank={rerank_score:.4f}, distance={distance:.4f}, note_id={note_id[:8]}, title='{title}', chunk={chunk_idx}")

            except Exception as e:
                print(f"⚠️  [GENERATIVE_SEARCH] Re-ranking failed: {e}, falling back to distance scores")
                # Fallback to distance-based scoring
                reranked_chunks = [(obj, distance, 1.0 - distance) for obj, distance in chunks_to_rerank]

            # Step 4: Filter by rerank score threshold and limit chunks
            # Looser thresholds when scoped to a specific note (advanced search)
            if note_id_filter:
                RERANK_THRESHOLD = 0.05
                MAX_CHUNKS_IN_PROMPT = 4
            else:
                RERANK_THRESHOLD = 0.5
                MAX_CHUNKS_IN_PROMPT = 2
            print(f"📏 [GENERATIVE_SEARCH] Rerank threshold: {RERANK_THRESHOLD}, max chunks: {MAX_CHUNKS_IN_PROMPT}")

            high_scoring_chunks = [
                (obj, distance, rerank_score)
                for obj, distance, rerank_score in reranked_chunks
                if rerank_score > RERANK_THRESHOLD
            ]

            print(f"✓ [GENERATIVE_SEARCH] {len(high_scoring_chunks)} chunks passed rerank threshold")

            # If no chunks pass threshold, use only the first chunk (best by distance)
            if not high_scoring_chunks:
                print(f"⚠️  [GENERATIVE_SEARCH] No chunks passed rerank threshold, using first chunk only")
                reranked_chunks.sort(key=lambda x: x[2], reverse=True)  # Sort by rerank score
                final_chunks = reranked_chunks[:1]  # Take only the first chunk
            else:
                # Sort by rerank score and take up to MAX_CHUNKS_IN_PROMPT
                high_scoring_chunks.sort(key=lambda x: x[2], reverse=True)
                final_chunks = high_scoring_chunks[:MAX_CHUNKS_IN_PROMPT]

            # Count unique notes for logging
            unique_note_ids = set(str(obj.properties.get("note_id")) for obj, _, _ in final_chunks)
            print(f"🎯 [GENERATIVE_SEARCH] Selected {len(final_chunks)} chunks from {len(unique_note_ids)} unique note(s)")

            print(f"📊 [GENERATIVE_SEARCH] Using {len(final_chunks)} chunks for generation:")
            for i, (obj, distance, rerank_score) in enumerate(final_chunks):
                note_id = str(obj.properties.get("note_id"))
                title = obj.properties.get("title", "")
                chunk_idx = obj.properties.get("chunk_index", 0)
                print(f"   [{i+1}] rerank={rerank_score:.4f}, distance={distance:.4f}, note_id={note_id[:8]}, title='{title}', chunk={chunk_idx}")

            # Step 4: Build context from filtered chunks
            additional_context_section = ""
            if additional_context:
                print(f"📝 [GENERATIVE_SEARCH] Additional context: {len(additional_context)} chars")
                additional_context_section = f"""

IMPORTANT - Additional Context Provided:
---
{additional_context}
---

Please focus on the additional context above to answer the question."""

            context_excerpts = []
            for obj, _, _ in final_chunks:
                title = obj.properties.get("title", "")
                content = obj.properties.get("content", "")
                chunk_idx = obj.properties.get("chunk_index", 0)
                total_chunks = obj.properties.get("total_chunks", 1)

                context_excerpts.append(f"""---
**From: {title}** (chunk {chunk_idx}/{total_chunks})
{content}
---""")

            context_text = "\n".join(context_excerpts)

            # Step 5: Build prompt and call Nexa for generation
            full_prompt = f"""You are a helpful AI assistant. The user has asked the following question:

"{query}"{additional_context_section}

Below are relevant excerpts from the user's notes that may help answer this question:

{context_text}

Based on the excerpts above{" and the additional context" if additional_context else ""}, provide a helpful and accurate answer to the user's question.
- If the excerpts contain relevant information, reference it specifically.
- If the excerpts don't contain enough information to fully answer, say so.
- Be concise but thorough."""

            # DEBUG: Print full prompt
            print(f"\n{'='*80}")
            print(f"📝 [DEBUG] FULL PROMPT TO LLM:")
            print(f"{'='*80}")
            print(full_prompt)
            print(f"{'='*80}\n")

            # Call Nexa for generation
            gen_start = time.time()
            try:
                generated_text = self.nexa.generate(full_prompt, max_tokens=1024)
                gen_time = time.time() - gen_start
                print(f"⏱️  [TIMING] LLM generation: {gen_time:.2f}s")
                print(f"✓ [GENERATIVE_SEARCH] Generated response via Nexa: {len(generated_text)} chars")
            except Exception as e:
                print(f"❌ [GENERATIVE_SEARCH] Nexa generation error: {e}")
                generated_text = "I encountered an error while generating a response."

            # Step 6: Build context notes for response (deduplicated by note_id)
            seen_note_ids = set()
            context_notes = []
            for obj, _, _ in final_chunks:
                note_id = str(obj.properties.get("note_id"))

                # Skip if we've already added this note
                if note_id in seen_note_ids:
                    continue
                seen_note_ids.add(note_id)

                title = obj.properties.get("title", "")
                content = obj.properties.get("content", "")

                context_notes.append({
                    "note_id": note_id,
                    "title": title,
                    "content_preview": content[:200] + "..." if len(content) > 200 else content,
                })

            total_time = time.time() - rag_start_time
            print(f"\n⏱️  [TIMING] Total RAG pipeline: {total_time:.2f}s")

            return {
                "response": generated_text if generated_text else "I couldn't generate a response based on your notes.",
                "context_notes": context_notes
            }

        except Exception as e:
            print(f"❌ [GENERATIVE_SEARCH] Error: {e}")
            import traceback
            traceback.print_exc()
            return {
                "response": f"Error generating response: {str(e)}",
                "context_notes": []
            }

    def is_ready(self) -> bool:
        """Check if Weaviate is ready and responsive."""
        try:
            return self.client.is_ready()
        except Exception as e:
            print(f"Weaviate not ready: {e}")
            return False

    def close(self):
        """Close Weaviate connection."""
        if self.client:
            self.client.close()
            print("Weaviate connection closed")


# Global instance (initialized in main.py lifespan)
weaviate_service = None


def get_weaviate_service() -> WeaviateService:
    """Dependency injection for Weaviate service."""
    global weaviate_service
    if weaviate_service is None:
        weaviate_service = WeaviateService()
    return weaviate_service
