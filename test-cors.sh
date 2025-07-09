#!/bin/bash

API_BASE="http://localhost:8080"

echo "=== Testing CORS Configuration ==="

# Test 1: Simple preflight request
echo "1. Testing preflight request (OPTIONS)..."
curl -X OPTIONS "$API_BASE/records" \
  -H "Origin: https://skills-studyflow-api.eliaschen.dev" \
  -H "Access-Control-Request-Method: GET" \
  -H "Access-Control-Request-Headers: Authorization" \
  -v

echo -e "\n\n2. Testing preflight request for POST..."
curl -X OPTIONS "$API_BASE/records/screenshots/upload" \
  -H "Origin: https://skills-studyflow-api.eliaschen.dev" \
  -H "Access-Control-Request-Method: POST" \
  -H "Access-Control-Request-Headers: Authorization,Content-Type" \
  -v

echo -e "\n\n3. Testing actual request with CORS headers..."
curl -X GET "$API_BASE/auth/signin" \
  -H "Origin: https://skills-studyflow-api.eliaschen.dev" \
  -v

echo -e "\n\n=== Test Complete ==="
