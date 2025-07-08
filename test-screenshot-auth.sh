#!/bin/bash

API_BASE="http://localhost:8080"

echo "=== Testing Screenshot Endpoint Authentication ==="

# Test 1: Check if screenshot endpoint is accessible without authentication
echo "1. Testing screenshot endpoint without authentication..."
curl -X GET "$API_BASE/records/screenshots" -v

echo -e "\n\n"

# Test 2: Test with API key authentication (key: key)
echo "2. Testing screenshot endpoint with API key authentication..."
curl -X GET "$API_BASE/records/screenshots" -H "key: key" -v

echo -e "\n\n"

# Test 3: Test with JWT token (first get a token)
echo "3. Getting JWT token..."
TOKEN_RESPONSE=$(curl -s -X POST "$API_BASE/auth/authenticate" \
  -H "Content-Type: application/json" \
  -d '{"username": "elias", "password": "meow"}')

echo "Token response: $TOKEN_RESPONSE"

TOKEN=$(echo $TOKEN_RESPONSE | grep -o '"accessToken":"[^"]*' | grep -o '[^"]*$')

if [ -n "$TOKEN" ]; then
    echo "4. Testing screenshot endpoint with JWT token..."
    curl -X GET "$API_BASE/records/screenshots" -H "Authorization: Bearer $TOKEN" -v
else
    echo "4. Could not get JWT token, trying with dummy token..."
    curl -X GET "$API_BASE/records/screenshots" -H "Authorization: Bearer dummy-token" -v
fi

echo -e "\n\n"

# Test 5: Test screenshot upload with API key
echo "5. Testing screenshot upload with API key..."
echo "Creating dummy screenshot file..."
echo "dummy screenshot content" > /tmp/test-screenshot.png

curl -X POST "$API_BASE/records/screenshots/upload" \
  -H "key: key" \
  -F "screenshot=@/tmp/test-screenshot.png" \
  -v

echo -e "\n\n"

# Clean up
rm -f /tmp/test-screenshot.png

echo "=== Test Complete ==="
