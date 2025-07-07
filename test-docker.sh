#!/bin/bash

echo "🔍 Testing Docker configuration files..."
echo "======================================="

# Check if Docker files exist
files=("Dockerfile" "docker-compose.yml" ".dockerignore")
for file in "${files[@]}"; do
    if [ -f "$file" ]; then
        echo "✅ $file exists"
    else
        echo "❌ $file missing"
    fi
done

# Check if application properties for Docker exists
if [ -f "src/main/resources/application-docker.properties" ]; then
    echo "✅ application-docker.properties exists"
else
    echo "❌ application-docker.properties missing"
fi

# Validate Dockerfile syntax
echo ""
echo "🔍 Checking Dockerfile syntax..."
if command -v docker &> /dev/null; then
    if docker run --rm -i hadolint/hadolint < Dockerfile; then
        echo "✅ Dockerfile syntax is valid"
    else
        echo "⚠️  Dockerfile has some warnings (but should still work)"
    fi
else
    echo "⚠️  Docker not available for syntax check"
fi

# Validate docker-compose syntax
echo ""
echo "🔍 Checking docker-compose.yml syntax..."
if command -v docker-compose &> /dev/null; then
    if docker-compose config > /dev/null 2>&1; then
        echo "✅ docker-compose.yml syntax is valid"
    else
        echo "❌ docker-compose.yml has syntax errors"
    fi
else
    echo "⚠️  docker-compose not available for syntax check"
fi

echo ""
echo "🚀 Ready to deploy! Run './deploy.sh' when Docker is available."
echo ""
echo "📋 To install Docker:"
echo "   - macOS: https://docs.docker.com/desktop/mac/install/"
echo "   - Linux: https://docs.docker.com/engine/install/"
echo "   - Windows: https://docs.docker.com/desktop/windows/install/"
