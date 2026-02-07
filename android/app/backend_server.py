"""
Simple Flask Backend for Notes Assistant App
This is a minimal server for testing the Android app.
In production, replace with proper OCR (Tesseract, Google Vision) and LLM (OpenAI, Claude API).
"""

from flask import Flask, request, jsonify
from datetime import datetime
import os
import uuid

app = Flask(__name__)

# Store uploaded notes (in production, use a database)
notes_storage = {}

@app.route('/')
def home():
    return jsonify({
        "message": "Notes Assistant API",
        "version": "1.0",
        "endpoints": ["/upload-note", "/query"]
    })

@app.route('/upload-note', methods=['POST'])
def upload_note():
    """
    Handle image upload from the Android app
    In production, this would:
    1. Save the image
    2. Run OCR (Tesseract, Google Cloud Vision, etc.)
    3. Extract text from handwritten notes
    4. Store in vector database for RAG
    """
    try:
        if 'image' not in request.files:
            return jsonify({
                "status": "error",
                "message": "No image provided"
            }), 400
        
        image_file = request.files['image']
        
        # Generate unique note ID
        note_id = str(uuid.uuid4())[:8]
        
        # In production: Save file and run OCR
        # For now, simulate OCR extraction
        simulated_ocr_text = """
        Machine Learning Notes
        - Supervised Learning: Uses labeled data
        - Unsupervised Learning: Finds patterns in unlabeled data
        - Key algorithms: Linear Regression, Decision Trees, Neural Networks
        - Training process: Split data, train model, validate, test
        """
        
        # Store the note
        notes_storage[note_id] = {
            "id": note_id,
            "text": simulated_ocr_text,
            "timestamp": datetime.now().isoformat(),
            "filename": image_file.filename
        }
        
        print(f"✓ Uploaded note {note_id}: {image_file.filename}")
        
        return jsonify({
            "noteId": note_id,
            "status": "success",
            "message": f"Image processed successfully. Extracted {len(simulated_ocr_text)} characters."
        }), 200
        
    except Exception as e:
        print(f"✗ Error in upload: {str(e)}")
        return jsonify({
            "status": "error",
            "message": str(e)
        }), 500

@app.route('/query', methods=['POST'])
def query():
    """
    Handle voice query from the Android app
    In production, this would:
    1. Receive transcribed query
    2. Search vector database (RAG)
    3. Send to LLM with relevant context
    4. Return LLM response
    """
    try:
        data = request.json
        query_text = data.get('query', '')
        note_id = data.get('noteId')
        
        print(f"? Query received: '{query_text}'")
        if note_id:
            print(f"  Note ID: {note_id}")
        
        # In production: Use RAG + LLM
        # For now, provide intelligent mock responses
        
        # Simple keyword matching for demo
        query_lower = query_text.lower()
        
        if 'supervised' in query_lower or 'learning' in query_lower:
            answer = "Based on your notes, supervised learning uses labeled data to train models. The key difference from unsupervised learning is that you provide the correct answers during training."
        elif 'algorithm' in query_lower:
            answer = "Your notes mention several key algorithms: Linear Regression for continuous predictions, Decision Trees for classification, and Neural Networks for complex pattern recognition."
        elif 'training' in query_lower or 'process' in query_lower:
            answer = "The training process in your notes follows these steps: First, split your data into training and testing sets. Then train the model on the training data, validate its performance, and finally test it on unseen data."
        elif 'key' in query_lower or 'main' in query_lower or 'important' in query_lower:
            answer = "The key points in your notes are: supervised learning uses labeled data, unsupervised finds patterns without labels, and common algorithms include regression, decision trees, and neural networks."
        elif 'summarize' in query_lower or 'summary' in query_lower:
            answer = "Your notes cover machine learning fundamentals: two main types of learning (supervised and unsupervised), key algorithms like linear regression and neural networks, and the standard training process of splitting, training, validating, and testing."
        else:
            # Generic response
            answer = f"I found information related to '{query_text}' in your notes. The notes discuss machine learning concepts including supervised and unsupervised learning, various algorithms, and the training process. Could you be more specific about what you'd like to know?"
        
        print(f"✓ Response: {answer[:100]}...")
        
        return jsonify({
            "answer": answer,
            "status": "success"
        }), 200
        
    except Exception as e:
        print(f"✗ Error in query: {str(e)}")
        return jsonify({
            "answer": f"Sorry, I encountered an error: {str(e)}",
            "status": "error"
        }), 500

@app.route('/notes', methods=['GET'])
def list_notes():
    """List all stored notes (for debugging)"""
    return jsonify({
        "notes": list(notes_storage.values()),
        "count": len(notes_storage)
    })

if __name__ == '__main__':
    print("=" * 60)
    print("Notes Assistant Backend Server")
    print("=" * 60)
    print("\nServer starting on http://0.0.0.0:5000")
    print("\nFor Android Emulator, use: http://10.0.2.2:5000")
    print("For real device, use your computer's IP address")
    print("\nEndpoints:")
    print("  POST /upload-note - Upload image")
    print("  POST /query - Ask questions")
    print("  GET  /notes - List all notes")
    print("\nPress Ctrl+C to stop the server")
    print("=" * 60)
    print()
    
    app.run(host='0.0.0.0', port=5000, debug=True)
