"""
Nexa CLI client for embeddings and generation.
Connects to Nexa CLI servers running on Windows.
Uses OpenAI-compatible API endpoints.
"""
import requests
from typing import List
from config import settings


class NexaClient:
    """Client for Nexa CLI servers (OpenAI-compatible)."""

    def __init__(self):
        # Two separate servers: one for embeddings, one for LLM
        self.embed_url = settings.nexa_embed_url
        self.llm_url = settings.nexa_llm_url
        self.embed_model = settings.nexa_embedding_model
        self.gen_model = settings.nexa_generation_model

    def is_healthy(self) -> bool:
        """Check if both Nexa servers are reachable."""
        try:
            # Check embedding server
            response = requests.get(f"{self.embed_url}/health", timeout=5)
            embed_ok = response.status_code == 200

            # Check LLM server
            response = requests.get(f"{self.llm_url}/health", timeout=5)
            llm_ok = response.status_code == 200

            return embed_ok and llm_ok
        except Exception as e:
            print(f"Nexa server health check failed: {e}")
            return False

    def embed_texts(self, texts: List[str]) -> List[List[float]]:
        """
        Generate embeddings for a list of texts using OpenAI-compatible API.

        Args:
            texts: List of strings to embed

        Returns:
            List of embedding vectors

        Raises:
            RuntimeError: If embedding request fails
        """
        try:
            response = requests.post(
                f"{self.embed_url}/v1/embeddings",
                json={
                    "input": texts,
                    "model": self.embed_model
                },
                timeout=120
            )
            response.raise_for_status()
            data = response.json()

            # Extract embeddings in order (OpenAI format)
            embeddings = [
                item["embedding"]
                for item in sorted(data["data"], key=lambda x: x["index"])
            ]
            return embeddings
        except Exception as e:
            raise RuntimeError(f"Failed to generate embeddings: {e}")

    def generate(self, prompt: str, max_tokens: int = 512) -> str:
        """
        Generate text using LLM via OpenAI-compatible chat completions API.

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
                f"{self.llm_url}/v1/chat/completions",
                json={
                    "model": self.gen_model,
                    "messages": [
                        {"role": "user", "content": prompt}
                    ],
                    "max_tokens": max_tokens,
                    "stream": False
                },
                timeout=120
            )
            response.raise_for_status()
            data = response.json()

            # Extract response from OpenAI format
            return data["choices"][0]["message"]["content"]
        except Exception as e:
            raise RuntimeError(f"Failed to generate response: {e}")

    def rerank(self, query: str, documents: List[str]) -> List[float]:
        """
        Re-rank documents based on their relevance to the query using Nexa's re-ranker.

        Args:
            query: The search query
            documents: List of document texts to re-rank

        Returns:
            List of relevance scores (higher = more relevant)

        Raises:
            RuntimeError: If re-ranking request fails
        """
        try:
            response = requests.post(
                f"{self.llm_url}/v1/reranking",
                json={
                    "model": "NexaAI/jina-v2-rerank-npu",
                    "query": query,
                    "documents": documents,
                    "batch_size": len(documents),  # Process all documents in one batch
                    "normalize": True,
                    "normalize_method": "softmax"
                },
                timeout=60
            )
            response.raise_for_status()
            data = response.json()

            # DEBUG: Print raw API response
            print(f"🔍 [DEBUG] Nexa rerank API response: {data}")

            # Return relevance scores
            return data["result"]
        except Exception as e:
            raise RuntimeError(f"Failed to re-rank documents: {e}")


# Global instance
nexa_client = None


def get_nexa_client() -> NexaClient:
    """Get or create Nexa client instance."""
    global nexa_client
    if nexa_client is None:
        nexa_client = NexaClient()
    return nexa_client
