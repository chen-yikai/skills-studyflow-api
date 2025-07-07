#!/bin/bash

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

echo -e "${GREEN}🚀 Deploying Skills StudyFlow API${NC}"
echo "================================="

# Create records directory if it doesn't exist
if [ ! -d "records" ]; then
    echo -e "${YELLOW}📁 Creating records directory...${NC}"
    mkdir -p records
fi

# Stop any running containers
echo -e "${YELLOW}🛑 Stopping existing containers...${NC}"
docker-compose down

# Build and start the application
echo -e "${YELLOW}🔨 Building and starting the application...${NC}"
docker-compose up --build -d

# Wait for the application to start
echo -e "${YELLOW}⏳ Waiting for application to start...${NC}"
sleep 30

# Check if the application is running
if curl -f -s http://localhost:8080/records/files > /dev/null; then
    echo -e "${GREEN}✅ Application is running successfully!${NC}"
    echo ""
    echo "🌐 Access points:"
    echo "   - API: http://localhost:8080"
    echo "   - Swagger UI: http://localhost:8080/swagger-ui/index.html"
    echo "   - File List: http://localhost:8080/records/files"
    echo ""
    echo "📱 Quick test:"
    echo "   curl http://localhost:8080/records"
else
    echo -e "${RED}❌ Application failed to start. Check logs:${NC}"
    echo "   docker-compose logs"
fi
