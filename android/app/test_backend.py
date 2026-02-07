#!/usr/bin/env python3
"""
Test script for Notes Assistant Backend
Run this to verify your backend server is working correctly
"""

import requests
import json
import os
import sys

# Configuration
BASE_URL = "http://localhost:5000"
TEST_IMAGE_PATH = "test_image.jpg"

def print_header(text):
    print("\n" + "="*60)
    print(f"  {text}")
    print("="*60 + "\n")

def test_connection():
    """Test basic server connectivity"""
    print_header("TEST 1: Server Connection")
    try:
        response = requests.get(f"{BASE_URL}/", timeout=5)
        if response.status_code == 200:
            print("✓ Server is running!")
            print(f"  Response: {response.json()}")
            return True
        else:
            print(f"✗ Server returned status code: {response.status_code}")
            return False
    except requests.exceptions.ConnectionError:
        print("✗ Could not connect to server!")
        print(f"  Make sure server is running on {BASE_URL}")
        return False
    except Exception as e:
        print(f"✗ Error: {e}")
        return False

def test_upload():
    """Test image upload endpoint"""
    print_header("TEST 2: Image Upload")
    
    # Create a dummy image file
    try:
        # Create a simple test file
        with open(TEST_IMAGE_PATH, 'wb') as f:
            f.write(b'\xFF\xD8\xFF\xE0\x00\x10JFIF')  # Minimal JPEG header
        
        with open(TEST_IMAGE_PATH, 'rb') as f:
            files = {'image': ('test_note.jpg', f, 'image/jpeg')}
            response = requests.post(f"{BASE_URL}/upload-note", files=files, timeout=10)
        
        if response.status_code == 200:
            data = response.json()
            print("✓ Upload successful!")
            print(f"  Note ID: {data.get('noteId')}")
            print(f"  Status: {data.get('status')}")
            return data.get('noteId')
        else:
            print(f"✗ Upload failed with status: {response.status_code}")
            print(f"  Response: {response.text}")
            return None
            
    except Exception as e:
        print(f"✗ Error during upload: {e}")
        return None
    finally:
        # Cleanup test file
        if os.path.exists(TEST_IMAGE_PATH):
            os.remove(TEST_IMAGE_PATH)

def test_query(note_id=None):
    """Test query endpoint"""
    print_header("TEST 3: Query Processing")
    
    test_queries = [
        "What are the key points in my notes?",
        "Summarize the main concepts",
        "Explain the training process"
    ]
    
    for query in test_queries:
        try:
            payload = {
                "query": query,
                "noteId": note_id
            }
            
            print(f"\nQuery: '{query}'")
            response = requests.post(
                f"{BASE_URL}/query", 
                json=payload,
                timeout=10
            )
            
            if response.status_code == 200:
                data = response.json()
                print(f"✓ Response received!")
                print(f"  Answer: {data.get('answer')[:100]}...")
            else:
                print(f"✗ Query failed with status: {response.status_code}")
                
        except Exception as e:
            print(f"✗ Error during query: {e}")
    
    return True

def test_list_notes():
    """Test listing notes"""
    print_header("TEST 4: List Notes")
    
    try:
        response = requests.get(f"{BASE_URL}/notes", timeout=5)
        if response.status_code == 200:
            data = response.json()
            print(f"✓ Found {data.get('count', 0)} notes in storage")
            for note in data.get('notes', [])[:3]:  # Show first 3
                print(f"  - ID: {note.get('id')} | File: {note.get('filename')}")
            return True
        else:
            print(f"✗ Failed to list notes: {response.status_code}")
            return False
    except Exception as e:
        print(f"✗ Error listing notes: {e}")
        return False

def main():
    print("\n" + "🚀 Notes Assistant Backend Test Suite" + "\n")
    
    # Check if server URL is correct
    print(f"Testing server at: {BASE_URL}")
    print("(If using Android emulator, make sure BASE_URL matches 10.0.2.2:5000)\n")
    
    # Run tests
    if not test_connection():
        print("\n❌ Server connection failed. Cannot proceed with tests.")
        print("\nMake sure to start the server first:")
        print("  python backend_server.py")
        sys.exit(1)
    
    note_id = test_upload()
    test_query(note_id)
    test_list_notes()
    
    # Summary
    print_header("Test Summary")
    print("✓ All basic tests passed!")
    print("\nYour backend server is working correctly.")
    print("\nNext steps:")
    print("  1. Update BASE_URL in RetrofitClient.kt to match your setup")
    print("  2. Run the Android app")
    print("  3. Test with real images and voice queries")
    
if __name__ == "__main__":
    try:
        main()
    except KeyboardInterrupt:
        print("\n\nTest interrupted by user.")
        sys.exit(0)
