#!/bin/bash

API_BASE="http://localhost:8080"
# Token from Android app logs
TOKEN="eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJjOWE1OTQwMy1iYzJmLTQ4MDEtYTI0ZC1iM2UxYzhkMDdhYmQiLCJ1c2VybmFtZSI6ImVsaWFzIiwiZW1haWwiOiJlbGlhc0BleGFtcGxlLmNvbSIsImlhdCI6MTc1MTk5ODQ2MiwiZXhwIjoxNzUyMDg0ODYyfQ.T2xAO75MBsm0oyF9ofOWERpWmczgwDl_7eN8i0RJRi4"

echo "=== Testing Android JWT Token ==="
echo "Token: ${TOKEN:0:50}..."

# Test 1: Check if server is running
echo -e "\n1. Testing server connectivity..."
curl -s -X GET "$API_BASE/auth/signin" -w "\nHTTP Status: %{http_code}\n" | head -3

# Test 2: Test token verification endpoint
echo -e "\n2. Testing token verification..."
curl -s -X GET "$API_BASE/auth/verify" \
  -H "Authorization: Bearer $TOKEN" \
  -w "\nHTTP Status: %{http_code}\n"

# Test 3: Test records endpoint
echo -e "\n3. Testing records endpoint..."
curl -s -X GET "$API_BASE/records" \
  -H "Authorization: Bearer $TOKEN" \
  -w "\nHTTP Status: %{http_code}\n"

# Test 4: Test screenshots list endpoint
echo -e "\n4. Testing screenshots list endpoint..."
curl -s -X GET "$API_BASE/records/screenshots" \
  -H "Authorization: Bearer $TOKEN" \
  -w "\nHTTP Status: %{http_code}\n"

# Test 5: Test screenshot upload endpoint (POST)
echo -e "\n5. Testing screenshot upload endpoint..."
echo "dummy screenshot content" > /tmp/test-screenshot.png
curl -s -X POST "$API_BASE/records/screenshots/upload" \
  -H "Authorization: Bearer $TOKEN" \
  -F "screenshot=@/tmp/test-screenshot.png" \
  -w "\nHTTP Status: %{http_code}\n"

# Clean up
rm -f /tmp/test-screenshot.png

echo -e "\n=== Test Complete ==="
