#!/bin/bash

echo "Testing API key authentication..."
echo "=================================="

# Test without authentication (should fail)
echo "1. Testing without authentication:"
curl -s -o /dev/null -w "Status: %{http_code}\n" http://localhost:8080/records

# Test with API key 'key' (should work)
echo "2. Testing with API key 'key':"
curl -s -H "key: key" -o /dev/null -w "Status: %{http_code}\n" http://localhost:8080/records

# Test with wrong API key (should fail)
echo "3. Testing with wrong API key:"
curl -s -H "key: wrong" -o /dev/null -w "Status: %{http_code}\n" http://localhost:8080/records

echo "=================================="
echo "Expected results:"
echo "1. Status: 401 (Unauthorized)"
echo "2. Status: 200 (OK)"
echo "3. Status: 401 (Unauthorized)"
