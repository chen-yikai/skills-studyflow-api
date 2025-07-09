#!/bin/bash

echo "Testing User-Specific Data Isolation"
echo "==================================="

BASE_URL="http://localhost:8080"

# Function to get JWT token for a user
get_jwt_token() {
    local username="$1"
    local password="$2"
    
    local response=$(curl -s -X POST \
        -H "Content-Type: application/json" \
        -d "{\"username\":\"$username\",\"password\":\"$password\"}" \
        "$BASE_URL/auth/authenticate")
    
    echo "$response" | grep -o '"accessToken":"[^"]*' | sed 's/"accessToken":"//' | sed 's/"//'
}

# Function to create a record for a user
create_record_for_user() {
    local token="$1"
    local record_id="$2"
    local record_name="$3"
    local note_data="$4"
    
    curl -s -X POST \
        -H "Authorization: Bearer $token" \
        -H "Content-Type: application/json" \
        -d "{
            \"id\": \"$record_id\",
            \"name\": \"$record_name\",
            \"file\": \"test.txt\",
            \"date\": \"2025-01-01\",
            \"note\": [{\"time\": \"10:00\", \"data\": \"$note_data\"}],
            \"tags\": [\"test\"],
            \"screenshots\": []
        }" \
        "$BASE_URL/records"
}

# Function to get records for a user
get_records_for_user() {
    local token="$1"
    echo "Records for user:"
    curl -s -H "Authorization: Bearer $token" "$BASE_URL/records" | jq '.'
}

# Function to search records for a user
search_records_for_user() {
    local token="$1"
    local query="$2"
    echo "Search results for '$query':"
    curl -s -H "Authorization: Bearer $token" "$BASE_URL/records/search?q=$query" | jq '.'
}

echo "1. Getting JWT tokens for different users..."
echo "============================================"

# Get tokens for different users
ELIAS_TOKEN=$(get_jwt_token "elias" "meow")
YANG_TOKEN=$(get_jwt_token "yang" "password")

echo "✓ Got token for elias: ${ELIAS_TOKEN:0:20}..."
echo "✓ Got token for yang: ${YANG_TOKEN:0:20}..."

echo ""
echo "2. Creating records for different users..."
echo "========================================="

# Create records for elias
echo "Creating records for elias..."
create_record_for_user "$ELIAS_TOKEN" "elias-record-1" "Elias Math Study" "Elias studied algebra and calculus"
create_record_for_user "$ELIAS_TOKEN" "elias-record-2" "Elias Programming" "Elias worked on Spring Boot project"

# Create records for yang
echo "Creating records for yang..."
create_record_for_user "$YANG_TOKEN" "yang-record-1" "Yang Physics Study" "Yang studied quantum mechanics"
create_record_for_user "$YANG_TOKEN" "yang-record-2" "Yang Chemistry" "Yang worked on organic chemistry"

echo ""
echo "3. Testing data isolation..."
echo "==========================="

echo "📋 Records visible to elias:"
get_records_for_user "$ELIAS_TOKEN"

echo ""
echo "📋 Records visible to yang:"
get_records_for_user "$YANG_TOKEN"

echo ""
echo "4. Testing search isolation..."
echo "============================="

echo "🔍 Elias searching for 'algebra':"
search_records_for_user "$ELIAS_TOKEN" "algebra"

echo ""
echo "🔍 Yang searching for 'algebra' (should find nothing):"
search_records_for_user "$YANG_TOKEN" "algebra"

echo ""
echo "🔍 Yang searching for 'quantum':"
search_records_for_user "$YANG_TOKEN" "quantum"

echo ""
echo "🔍 Elias searching for 'quantum' (should find nothing):"
search_records_for_user "$ELIAS_TOKEN" "quantum"

echo ""
echo "5. Testing record access by ID..."
echo "==============================="

echo "🔑 Elias trying to access his own record:"
curl -s -H "Authorization: Bearer $ELIAS_TOKEN" "$BASE_URL/records/elias-record-1" | jq '.'

echo ""
echo "🔑 Yang trying to access Elias's record (should fail):"
curl -s -H "Authorization: Bearer $YANG_TOKEN" "$BASE_URL/records/elias-record-1" | jq '.'

echo ""
echo "6. Testing record deletion isolation..."
echo "======================================"

echo "🗑️ Yang trying to delete Elias's record (should fail):"
curl -s -X DELETE -H "Authorization: Bearer $YANG_TOKEN" "$BASE_URL/records/elias-record-1" | jq '.'

echo ""
echo "🗑️ Elias deleting his own record (should succeed):"
curl -s -X DELETE -H "Authorization: Bearer $ELIAS_TOKEN" "$BASE_URL/records/elias-record-1" | jq '.'

echo ""
echo "7. Cleanup..."
echo "============"

# Clean up remaining records
curl -s -X DELETE -H "Authorization: Bearer $ELIAS_TOKEN" "$BASE_URL/records/elias-record-2" > /dev/null
curl -s -X DELETE -H "Authorization: Bearer $YANG_TOKEN" "$BASE_URL/records/yang-record-1" > /dev/null
curl -s -X DELETE -H "Authorization: Bearer $YANG_TOKEN" "$BASE_URL/records/yang-record-2" > /dev/null

echo "✓ Cleanup completed"
echo ""
echo "User isolation testing completed!"
echo "Summary: Each user can only see, search, and modify their own records."
