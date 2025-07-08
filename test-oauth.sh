#!/bin/bash

# Test script for simplified OAuth endpoints

echo "Testing OAuth Endpoints"
echo "======================"

# Start the server in background if not already running
if ! pgrep -f "java.*skills-studyflow-api" > /dev/null; then
    echo "Starting server..."
    ./gradlew bootRun &
    SERVER_PID=$!
    sleep 10
    echo "Server started with PID: $SERVER_PID"
else
    echo "Server is already running"
fi

# Test 1: Check if login page loads
echo -e "\n1. Testing /auth/signin login page:"
curl -X GET http://localhost:8080/auth/signin \
  -w "\nHTTP Status: %{http_code}\n" | head -5

echo -e "\n2. Testing /auth/authenticate with valid credentials:"
curl -X POST http://localhost:8080/auth/authenticate \
  -H "Content-Type: application/json" \
  -d '{"username": "demo", "password": "password123"}' \
  -w "\nHTTP Status: %{http_code}\n"

echo -e "\n3. Testing /auth/authenticate with invalid credentials:"
curl -X POST http://localhost:8080/auth/authenticate \
  -H "Content-Type: application/json" \
  -d '{"username": "demo", "password": "wrongpassword"}' \
  -w "\nHTTP Status: %{http_code}\n"

echo -e "\n4. Testing /auth/authenticate with missing fields:"
curl -X POST http://localhost:8080/auth/authenticate \
  -H "Content-Type: application/json" \
  -d '{"username": "demo"}' \
  -w "\nHTTP Status: %{http_code}\n"

echo -e "\n5. Testing /auth/signout:"
curl -X POST http://localhost:8080/auth/signout \
  -H "Content-Type: application/json" \
  -w "\nHTTP Status: %{http_code}\n"

echo -e "\n6. Testing /auth/verify with a valid token:"
# First get a token
TOKEN=$(curl -s -X POST http://localhost:8080/auth/authenticate \
  -H "Content-Type: application/json" \
  -d '{"username": "demo", "password": "password123"}' | \
  grep -o '"accessToken":"[^"]*"' | cut -d'"' -f4)

if [ -n "$TOKEN" ]; then
    echo "Using token: ${TOKEN:0:20}..."
    curl -X GET http://localhost:8080/auth/verify \
      -H "Authorization: Bearer $TOKEN" \
      -w "\nHTTP Status: %{http_code}\n"
else
    echo "Failed to get token"
fi

echo -e "\nTest completed!"

# Stop the server if we started it
if [ -n "$SERVER_PID" ]; then
    echo "Stopping server..."
    kill $SERVER_PID
fi
