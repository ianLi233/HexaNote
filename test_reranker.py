#!/usr/bin/env python3
"""
Test Nexa reranker API directly to debug why all scores are identical.
"""
import requests
import json

NEXA_URL = "http://172.23.224.1:8883"

def test_reranker():
    """Test Nexa reranking with sample documents."""

    query = "E6692 course rubrik"

    # Sample documents (simulating different chunks)
    documents = [
        "This is about E6692 machine learning course grading rubric and evaluation criteria.",
        "E6692 focuses on deep learning and neural networks with project assessments.",
        "The E4040 course covers introduction to neural networks and deep learning.",
        "This document discusses general course policies and attendance requirements.",
        "Unrelated content about cooking recipes and food preparation."
    ]

    print("=" * 80)
    print("Testing Nexa Reranker API")
    print("=" * 80)
    print(f"Query: {query}")
    print(f"\nDocuments ({len(documents)} total):")
    for i, doc in enumerate(documents):
        print(f"  [{i+1}] {doc[:80]}...")

    print("\n" + "=" * 80)
    print("Calling Nexa /v1/reranking endpoint...")
    print("=" * 80)

    try:
        response = requests.post(
            f"{NEXA_URL}/v1/reranking",
            json={
                "model": "NexaAI/jina-v2-rerank-npu",
                "query": query,
                "documents": documents,
                "batch_size": len(documents),
                "normalize": True,
                "normalize_method": "softmax"
            },
            timeout=60
        )

        print(f"Status Code: {response.status_code}")

        response.raise_for_status()
        data = response.json()

        print("\n" + "=" * 80)
        print("Raw API Response:")
        print("=" * 80)
        print(json.dumps(data, indent=2))

        if "result" in data:
            scores = data["result"]
            print("\n" + "=" * 80)
            print("Rerank Scores:")
            print("=" * 80)
            for i, (doc, score) in enumerate(zip(documents, scores)):
                print(f"  [{i+1}] score={score:.6f} - {doc[:60]}...")

            # Check if all scores are identical
            if len(set(scores)) == 1:
                print("\n⚠️  WARNING: All scores are IDENTICAL!")
                print("This indicates the reranker might not be working correctly.")
            else:
                print("\n✓ Scores are different - reranker is working!")

    except Exception as e:
        print(f"\n❌ Error: {e}")
        if hasattr(e, 'response'):
            print(f"Response: {e.response.text}")

if __name__ == "__main__":
    test_reranker()
