#!/bin/bash

API_BASE="http://localhost:8080"

echo "=== Simple Authentication Test ==="

# Test 1: Get a token
echo "1. Getting JWT token..."
TOKEN_RESPONSE=$(curl -s -X POST "$API_BASE/auth/authenticate" \
  -H "Content-Type: application/json" \
  -d '{"username": "elias", "password": "meow"}')

echo "Token response: $TOKEN_RESPONSE"

# Extract token (simple extraction)
TOKEN=$(echo "$TOKEN_RESPONSE" | grep -o '"accessToken":"[^"]*' | cut -d'"' -f4)

if [ -z "$TOKEN" ]; then
    echo "❌ Could not get JWT token"
    exit 1
fi

echo "✅ Token received: ${TOKEN:0:20}..."

# Test 2: Test a simple GET endpoint that should work
echo -e "\n2. Testing simple GET endpoint with token..."
curl -s -X GET "$API_BASE/records" \
  -H "Authorization: Bearer $TOKEN" \
  -w "\nHTTP Status: %{http_code}\n"

# Test 3: Test with API key
echo -e "\n3. Testing simple GET endpoint with API key..."
curl -s -X GET "$API_BASE/records" \
  -H "key: key" \
  -w "\nHTTP Status: %{http_code}\n"

# Test 4: Test screenshot list endpoint
echo -e "\n4. Testing screenshot list endpoint with token..."
curl -s -X GET "$API_BASE/records/screenshots" \
  -H "Authorization: Bearer $TOKEN" \
  -w "\nHTTP Status: %{http_code}\n"

# Test 5: Test screenshot list endpoint with API key
echo -e "\n5. Testing screenshot list endpoint with API key..."
curl -s -X GET "$API_BASE/records/screenshots" \
  -H "key: key" \
  -w "\nHTTP Status: %{http_code}\n"

echo -e "\n=== Test Complete ==="
