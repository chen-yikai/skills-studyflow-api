#!/bin/bash

API_BASE="http://localhost:8080"

echo "=== Testing Upload Endpoints ===="

# First get a JWT token
echo "1. Getting JWT token..."
TOKEN_RESPONSE=$(curl -s -X POST "$API_BASE/auth/authenticate" \
  -H "Content-Type: application/json" \
  -d '{"username": "elias", "password": "meow"}')

echo "Token response: $TOKEN_RESPONSE"

TOKEN=$(echo $TOKEN_RESPONSE | grep -o '"accessToken":"[^"]*' | grep -o '[^"]*$')

if [ -z "$TOKEN" ]; then
    echo "Could not get JWT token, exiting..."
    exit 1
fi

echo "Token: $TOKEN"

# Create test files
echo "2. Creating test files..."
echo "dummy file content" > /tmp/test-file.txt
echo "dummy screenshot content" > /tmp/test-screenshot.png

echo -e "\n3. Testing regular file upload endpoint..."
curl -X POST "$API_BASE/records/upload" \
  -H "Authorization: Bearer $TOKEN" \
  -F "file=@/tmp/test-file.txt" \
  -v

echo -e "\n\n4. Testing screenshot upload endpoint..."
curl -X POST "$API_BASE/records/screenshots/upload" \
  -H "Authorization: Bearer $TOKEN" \
  -F "screenshot=@/tmp/test-screenshot.png" \
  -v

echo -e "\n\n5. Testing screenshot upload with API key..."
curl -X POST "$API_BASE/records/screenshots/upload" \
  -H "key: key" \
  -F "screenshot=@/tmp/test-screenshot.png" \
  -v

echo -e "\n\n6. Testing regular upload with API key..."
curl -X POST "$API_BASE/records/upload" \
  -H "key: key" \
  -F "file=@/tmp/test-file.txt" \
  -v

# Clean up
rm -f /tmp/test-file.txt /tmp/test-screenshot.png

echo -e "\n\n=== Test Complete ==="
