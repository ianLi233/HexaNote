#!/bin/bash
# Comprehensive API test for HexaNote with Nexa

echo "============================================================"
echo "HexaNote API Test Suite (with Nexa)"
echo "============================================================"

BASE_URL="http://localhost:8001"

# Colors for output
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Test counter
PASS=0
FAIL=0

test_endpoint() {
    local name=$1
    local method=$2
    local endpoint=$3
    local data=$4

    echo -e "\n${YELLOW}Testing: ${name}${NC}"

    if [ -z "$data" ]; then
        response=$(curl -s -w "\n%{http_code}" -X ${method} "${BASE_URL}${endpoint}")
    else
        response=$(curl -s -w "\n%{http_code}" -X ${method} "${BASE_URL}${endpoint}" \
            -H "Content-Type: application/json" \
            -d "${data}")
    fi

    http_code=$(echo "$response" | tail -n1)
    body=$(echo "$response" | sed '$d')

    if [ "$http_code" -ge 200 ] && [ "$http_code" -lt 300 ]; then
        echo -e "${GREEN}✓ PASS${NC} (HTTP ${http_code})"
        echo "Response: ${body:0:200}..."
        ((PASS++))
    else
        echo -e "${RED}✗ FAIL${NC} (HTTP ${http_code})"
        echo "Response: ${body}"
        ((FAIL++))
    fi
}

# 1. Health Check
test_endpoint "Health Check" "GET" "/api/v1/health"

# 2. List Notes
test_endpoint "List Notes" "GET" "/api/v1/notes"

# 3. Create Note (will test Nexa embeddings)
NOTE_DATA='{
  "title": "Test Note with Nexa",
  "content": "This is a test note to verify Nexa embeddings are working correctly. It contains information about AI and machine learning.",
  "tags": ["test", "nexa", "ai"]
}'
test_endpoint "Create Note (Nexa Embeddings)" "POST" "/api/v1/notes" "$NOTE_DATA"

# 4. Get note ID from list
echo -e "\n${YELLOW}Fetching created note ID...${NC}"
NOTE_ID=$(curl -s "${BASE_URL}/api/v1/notes" | grep -o '"id":"[^"]*"' | head -1 | sed 's/"id":"//;s/"//')
echo "Note ID: $NOTE_ID"

if [ -n "$NOTE_ID" ]; then
    # 5. Get Single Note
    test_endpoint "Get Single Note" "GET" "/api/v1/notes/${NOTE_ID}"

    # 6. Update Note
    UPDATE_DATA='{
      "title": "Updated Test Note",
      "content": "Updated content with more information about neural networks and deep learning.",
      "tags": ["test", "nexa", "updated"],
      "version": 1
    }'
    test_endpoint "Update Note" "PUT" "/api/v1/notes/${NOTE_ID}" "$UPDATE_DATA"

    # 6.5. Reindex all notes with Nexa embeddings (CRITICAL!)
    echo -e "\n${YELLOW}Reindexing all notes with Nexa embeddings...${NC}"
    test_endpoint "Reindex Notes (Nexa)" "POST" "/api/v1/notes/reindex"
    echo "Waiting for reindexing to complete..."
    sleep 3

    # 7. Semantic Search (tests Nexa embedding generation for queries)
    test_endpoint "Semantic Search (Nexa)" "GET" "/api/v1/notes/search/semantic?q=machine%20learning&limit=5"

    # 8. RAG Chat (tests both Nexa embeddings and generation)
    CHAT_DATA='{
      "message": "What information do I have about AI?",
      "limit": 5
    }'
    test_endpoint "RAG Chat (Nexa Generation)" "POST" "/api/v1/chat/query" "$CHAT_DATA"

    # 9. Get Tags
    test_endpoint "Get All Tags" "GET" "/api/v1/notes/tags"

    # 10. Delete Note
    test_endpoint "Delete Note" "DELETE" "/api/v1/notes/${NOTE_ID}"
else
    echo -e "${RED}Could not get note ID, skipping dependent tests${NC}"
    ((FAIL+=5))
fi

# Summary
echo -e "\n============================================================"
echo "Test Summary"
echo "============================================================"
echo -e "${GREEN}Passed: ${PASS}${NC}"
echo -e "${RED}Failed: ${FAIL}${NC}"
echo "============================================================"

if [ $FAIL -eq 0 ]; then
    echo -e "\n${GREEN}🎉 All tests passed! Nexa migration successful!${NC}"
    exit 0
else
    echo -e "\n${RED}❌ Some tests failed. Check the output above.${NC}"
    exit 1
fi
