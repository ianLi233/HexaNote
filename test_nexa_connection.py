#!/usr/bin/env python3
"""
Test script to verify Nexa server connectivity and functionality.
Run this before deploying to ensure Nexa is accessible from WSL.
"""
import requests
import sys

NEXA_URL = "http://172.23.224.1:8883"
EMBEDDING_MODEL = "djuna/jina-embeddings-v2-base-en-Q5_K_M-GGUF:Q5_K_M"
GENERATION_MODEL = "NexaAI/Llama3.2-3B-NPU-Turbo"


def test_connection():
    """Test if Nexa server is reachable."""
    print("🔗 Testing Nexa server connection...")
    try:
        response = requests.get(f"{NEXA_URL}/v1/models", timeout=5)
        if response.status_code == 200:
            print("✓ Nexa server is reachable")
            return True
        else:
            print(f"✗ Nexa server returned status {response.status_code}")
            return False
    except requests.exceptions.ConnectionError:
        print(f"✗ Cannot connect to Nexa server at {NEXA_URL}")
        print("  Make sure Nexa is running on Windows:")
        print("  - Open PowerShell")
        print("  - Run: nexa server start")
        return False
    except Exception as e:
        print(f"✗ Error: {e}")
        return False


def test_models():
    """Test if required models are available."""
    print("\n📚 Checking available models...")
    try:
        response = requests.get(f"{NEXA_URL}/v1/models", timeout=5)
        data = response.json()

        available_models = [model["id"] for model in data["data"]]
        print(f"Found {len(available_models)} models:")
        for model in available_models:
            print(f"  - {model}")

        # Check required models
        print("\n🔍 Checking required models...")
        embedding_ok = EMBEDDING_MODEL in available_models
        generation_ok = GENERATION_MODEL in available_models

        if embedding_ok:
            print(f"✓ Embedding model found: {EMBEDDING_MODEL}")
        else:
            print(f"✗ Embedding model NOT found: {EMBEDDING_MODEL}")
            print("  Run: nexa model pull djuna/jina-embeddings-v2-base-en-Q5_K_M-GGUF:Q5_K_M")

        if generation_ok:
            print(f"✓ Generation model found: {GENERATION_MODEL}")
        else:
            print(f"✗ Generation model NOT found: {GENERATION_MODEL}")
            print("  Run: nexa model pull NexaAI/Llama3.2-3B-NPU-Turbo")

        return embedding_ok and generation_ok
    except Exception as e:
        print(f"✗ Error checking models: {e}")
        return False


def test_embedding():
    """Test embedding generation."""
    print("\n🧠 Testing embedding generation...")
    try:
        response = requests.post(
            f"{NEXA_URL}/v1/embeddings",
            json={
                "model": EMBEDDING_MODEL,
                "input": ["Hello, world!", "Test embedding"]
            },
            timeout=30
        )
        response.raise_for_status()
        data = response.json()

        embeddings = data["data"]
        print(f"✓ Generated {len(embeddings)} embeddings")
        print(f"  Embedding dimension: {len(embeddings[0]['embedding'])}")
        return True
    except Exception as e:
        print(f"✗ Embedding generation failed: {e}")
        return False


def test_generation():
    """Test text generation."""
    print("\n💬 Testing text generation...")
    try:
        response = requests.post(
            f"{NEXA_URL}/v1/chat/completions",
            json={
                "model": GENERATION_MODEL,
                "messages": [
                    {"role": "user", "content": "Say hello in one sentence."}
                ],
                "max_tokens": 50
            },
            timeout=60
        )
        response.raise_for_status()
        data = response.json()

        generated_text = data["choices"][0]["message"]["content"]
        print(f"✓ Generation successful")
        print(f"  Response: {generated_text[:100]}")
        return True
    except Exception as e:
        print(f"✗ Text generation failed: {e}")
        return False


def main():
    """Run all tests."""
    print("=" * 60)
    print("Nexa Server Connectivity Test")
    print("=" * 60)

    results = {
        "connection": test_connection(),
        "models": test_models(),
        "embedding": test_embedding(),
        "generation": test_generation()
    }

    print("\n" + "=" * 60)
    print("Test Summary")
    print("=" * 60)

    all_passed = all(results.values())

    for test_name, passed in results.items():
        status = "✓ PASS" if passed else "✗ FAIL"
        print(f"{status} - {test_name.capitalize()}")

    print("=" * 60)

    if all_passed:
        print("\n🎉 All tests passed! You can proceed with deployment.")
        print("\nNext steps:")
        print("1. docker compose down")
        print("2. docker volume rm hexanote_weaviate_data  # Clean old embeddings")
        print("3. docker compose up -d")
        print("4. python3 5-migrate-to-notes_run-after-docker-compose-up-d.py")
        return 0
    else:
        print("\n❌ Some tests failed. Please fix the issues above before deploying.")
        return 1


if __name__ == "__main__":
    sys.exit(main())
