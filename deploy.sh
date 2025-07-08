#!/bin/bash

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

echo -e "${GREEN}🚀 Deploying Skills StudyFlow API${NC}"
echo "================================="

# Check if Docker is available
if ! command -v docker &> /dev/null; then
    echo -e "${RED}❌ Docker is not installed. Please install Docker first.${NC}"
    echo "Visit: https://docs.docker.com/get-docker/"
    exit 1
fi

# Check if Docker daemon is running
if ! docker info &> /dev/null; then
    echo -e "${RED}❌ Docker daemon is not running. Please start Docker first.${NC}"
    exit 1
fi

echo -e "${GREEN}✓ Docker is available and running${NC}"

# Create records and screenshots directories if they don't exist
if [ ! -d "records" ]; then
    echo -e "${YELLOW}📁 Creating records directory...${NC}"
    mkdir -p records
fi

if [ ! -d "screenshots" ]; then
    echo -e "${YELLOW}📁 Creating screenshots directory...${NC}"
    mkdir -p screenshots
fi

# Stop any running containers
echo -e "${YELLOW}🛑 Stopping existing containers...${NC}"
docker compose down 2>/dev/null || docker-compose down 2>/dev/null || true

# Build and start the application
echo -e "${YELLOW}🔨 Building and starting the application...${NC}"
docker compose up --build -d 2>/dev/null || docker-compose up --build -d

# Wait for the application to start
echo -e "${YELLOW}⏳ Waiting for application to start...${NC}"
sleep 30

# Check if the application is running
if curl -f -s http://localhost:8080/auth/signin > /dev/null; then
    echo -e "${GREEN}✅ Application is running successfully!${NC}"
    echo ""
    echo "🌐 Access points:"
    echo "   - API: http://localhost:8080"
    echo "   - Swagger UI: http://localhost:8080/ui"
    echo "   - Login Page: http://localhost:8080/auth/signin"
    echo ""
    echo "🔐 Authentication required for most endpoints!"
    echo "📱 To get a token, authenticate with:"
    echo "   curl -X POST http://localhost:8080/auth/authenticate \\"
    echo "        -H 'Content-Type: application/json' \\"
    echo "        -d '{"username":"elias","password":"meow"}'"
    echo ""
    echo "Then use the token in requests:"
    echo "   curl -H 'Authorization: Bearer YOUR_TOKEN' http://localhost:8080/records"
else
    echo -e "${RED}❌ Application failed to start. Check logs:${NC}"
    echo "   docker-compose logs"
fi
