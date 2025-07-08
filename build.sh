#!/bin/bash

# Skills StudyFlow API Build Script

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

echo -e "${GREEN}🔨 Building Skills StudyFlow API Docker Image${NC}"
echo "=============================================="

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

# Build the Docker image
echo -e "${YELLOW}📦 Building Docker image...${NC}"
docker build -t skills-studyflow-api:latest .

echo -e "${GREEN}✅ Docker image built successfully!${NC}"
echo ""
echo "🚀 To run the application:"
echo "   docker run -p 8080:8080 -v \$(pwd)/records:/app/records -v \$(pwd)/screenshots:/app/screenshots skills-studyflow-api:latest"
echo ""
echo "🌐 Or use docker-compose:"
echo "   docker-compose up -d"

exit 0
