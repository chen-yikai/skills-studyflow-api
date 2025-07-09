#!/bin/bash

echo "Testing Username-Based API Key Authentication"
echo "============================================"

BASE_URL="http://localhost:8080"

# Function to test API key authentication
test_api_key() {
    local username="$1"
    echo "Testing API key for username: $username"
    
    # Test getting records
    local response=$(curl -s -w "HTTP_STATUS:%{http_code}" -H "key: $username" "$BASE_URL/records")
    local http_status=$(echo "$response" | grep -o "HTTP_STATUS:[0-9]*" | cut -d: -f2)
    local body=$(echo "$response" | sed 's/HTTP_STATUS:[0-9]*$//')
    
    echo "HTTP Status: $http_status"
    if [ "$http_status" = "200" ]; then
        echo "✓ Authentication successful"
        echo "Response: $body"
    else
        echo "✗ Authentication failed"
        echo "Response: $body"
    fi
    echo "---"
}

echo "1. Testing valid usernames..."
test_api_key "elias"
test_api_key "yang" 
test_api_key "scplay"
test_api_key "user"
test_api_key "key"

echo ""
echo "2. Testing invalid username..."
test_api_key "invalid-user"

echo ""
echo "3. Testing without API key..."
echo "Testing without API key:"
response=$(curl -s -w "HTTP_STATUS:%{http_code}" "$BASE_URL/records")
http_status=$(echo "$response" | grep -o "HTTP_STATUS:[0-9]*" | cut -d: -f2)
body=$(echo "$response" | sed 's/HTTP_STATUS:[0-9]*$//')

echo "HTTP Status: $http_status"
if [ "$http_status" = "401" ]; then
    echo "✓ Correctly rejected unauthenticated request"
else
    echo "✗ Should have rejected unauthenticated request"
fi
echo "Response: $body"
