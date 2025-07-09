#!/bin/bash

echo "Testing Search Endpoint"
echo "======================"

BASE_URL="http://localhost:8080"
API_KEY="key"

# Function to make API call with key authentication
search_records() {
    local query="$1"
    echo "Searching for: '$query'"
    curl -s -H "key: $API_KEY" "$BASE_URL/records/search?q=$query" | jq '.' 2>/dev/null || curl -s -H "key: $API_KEY" "$BASE_URL/records/search?q=$query"
    echo ""
}

# Function to create a test record
create_test_record() {
    local id="$1"
    local name="$2"
    local note_data="$3"
    
    echo "Creating test record: $name"
    curl -s -X POST -H "key: $API_KEY" -H "Content-Type: application/json" \
        -d "{
            \"id\": \"$id\",
            \"name\": \"$name\",
            \"file\": \"test.txt\",
            \"date\": \"2025-01-01\",
            \"note\": [{\"time\": \"10:00\", \"data\": \"$note_data\"}],
            \"tags\": [\"test\"],
            \"screenshots\": []
        }" \
        "$BASE_URL/records" > /dev/null
    echo "✓ Created: $name"
}

echo "1. Creating test data..."
create_test_record "test1" "Math Study Session" "Reviewed algebra fundamentals and quadratic equations"
create_test_record "test2" "Programming Tutorial" "Learned about Spring Boot controllers and REST APIs"
create_test_record "test3" "Reading Notes" "Finished chapter 3 of the programming book"
create_test_record "test4" "Project Work" "Implemented search functionality in the application"

echo ""
echo "2. Testing search functionality..."
echo "================================"

# Test search by name
echo "🔍 Search by name:"
search_records "Math"
search_records "Programming"

# Test search by note data
echo "🔍 Search by note content:"
search_records "algebra"
search_records "Spring Boot"
search_records "chapter"

# Test case insensitive search
echo "🔍 Case insensitive search:"
search_records "MATH"
search_records "spring"

# Test partial matches
echo "🔍 Partial matches:"
search_records "prog"
search_records "book"

# Test no results
echo "🔍 No results:"
search_records "nonexistent"

# Test empty query (should return error)
echo "🔍 Empty query test:"
curl -s -H "key: $API_KEY" "$BASE_URL/records/search?q=" | jq '.' 2>/dev/null || curl -s -H "key: $API_KEY" "$BASE_URL/records/search?q="

echo ""
echo "3. Cleaning up test data..."
echo "========================="
for id in test1 test2 test3 test4; do
    curl -s -X DELETE -H "key: $API_KEY" "$BASE_URL/records/$id" > /dev/null
    echo "✓ Deleted: $id"
done

echo ""
echo "Search endpoint testing completed!"
