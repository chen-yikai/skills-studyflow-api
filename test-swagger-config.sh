#!/bin/bash

echo "Testing Swagger Configuration and API Key Functionality"
echo "======================================================="

BASE_URL="http://localhost:8080"

# Wait for application to start
echo "Starting application and waiting for it to be ready..."
./gradlew bootRun > /dev/null 2>&1 &
APP_PID=$!

# Wait for application to start
sleep 15

echo "1. Testing Swagger UI availability..."
echo "===================================="

swagger_response=$(curl -s -o /dev/null -w "%{http_code}" "$BASE_URL/swagger-ui.html")
if [ "$swagger_response" = "200" ]; then
    echo "✓ Swagger UI is accessible"
else
    echo "✗ Swagger UI not accessible (HTTP $swagger_response)"
fi

echo ""
echo "2. Testing OpenAPI documentation..."
echo "=================================="

# Check OpenAPI documentation
api_docs=$(curl -s "$BASE_URL/v3/api-docs")
echo "Security schemes in OpenAPI docs:"
echo "$api_docs" | jq '.components.securitySchemes'

echo ""
echo "3. Testing API Key Authentication with different users..."
echo "========================================================"

# Function to test API key authentication
test_api_key_auth() {
    local username="$1"
    echo "Testing API key authentication for user: $username"
    
    # Create a test record
    create_response=$(curl -s -X POST \
        -H "key: $username" \
        -H "Content-Type: application/json" \
        -d "{
            \"id\": \"test-$username-$(date +%s)\",
            \"name\": \"Test Record for $username\",
            \"file\": \"test.txt\",
            \"date\": \"2025-01-01\",
            \"note\": [{\"time\": \"10:00\", \"data\": \"Test note for $username\"}],
            \"tags\": [\"test\"],
            \"screenshots\": []
        }" \
        "$BASE_URL/records")
    
    echo "Create response: $create_response"
    
    # Get records
    get_response=$(curl -s -H "key: $username" "$BASE_URL/records")
    echo "Get records response: $get_response"
    
    # Search records
    search_response=$(curl -s -H "key: $username" "$BASE_URL/records/search?q=Test")
    echo "Search response: $search_response"
    
    echo "---"
}

# Test different usernames
test_api_key_auth "elias"
test_api_key_auth "yang"
test_api_key_auth "key"

echo ""
echo "4. Testing data isolation..."
echo "=========================="

# Create record for elias
elias_record_id="elias-isolation-test-$(date +%s)"
curl -s -X POST \
    -H "key: elias" \
    -H "Content-Type: application/json" \
    -d "{
        \"id\": \"$elias_record_id\",
        \"name\": \"Elias Private Record\",
        \"file\": \"elias-private.txt\",
        \"date\": \"2025-01-01\",
        \"note\": [{\"time\": \"10:00\", \"data\": \"This is Elias private data\"}],
        \"tags\": [\"private\"],
        \"screenshots\": []
    }" \
    "$BASE_URL/records" > /dev/null

# Try to access Elias record with Yang's key
echo "Yang trying to access Elias's record:"
yang_access=$(curl -s -H "key: yang" "$BASE_URL/records/$elias_record_id")
echo "$yang_access"

# Yang should see empty records
echo "Yang's records (should not include Elias's):"
yang_records=$(curl -s -H "key: yang" "$BASE_URL/records")
echo "$yang_records"

# Elias should see his record
echo "Elias's records (should include his record):"
elias_records=$(curl -s -H "key: elias" "$BASE_URL/records")
echo "$elias_records"

echo ""
echo "5. Cleaning up..."
echo "================="

# Clean up test record
curl -s -X DELETE -H "key: elias" "$BASE_URL/records/$elias_record_id" > /dev/null

# Stop the application
kill $APP_PID 2>/dev/null

echo "✓ Testing completed!"
echo ""
echo "Summary:"
echo "- API key authentication should work with usernames: elias, yang, scplay, user, key"
echo "- Each user should only see their own data"
echo "- Swagger UI should be accessible at http://localhost:8080/swagger-ui.html"
echo "- In Swagger UI, use the 'Authorize' button and enter a username in the 'apiKey' field"
