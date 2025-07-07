# 🐳 Docker Deployment Guide

This guide explains how to deploy the Skills StudyFlow API using Docker.

## 📋 Prerequisites

- Docker (version 20.0+ recommended)
- Docker Compose (version 2.0+ recommended)

## 🚀 Quick Deployment

### Option 1: Using the deployment script (Recommended)
```bash
./deploy.sh
```

### Option 2: Manual deployment
```bash
# Create records directory for file persistence
mkdir -p records

# Build and start the application
docker-compose up --build -d

# Check logs
docker-compose logs -f
```

## 📁 File Structure

```
skills-studyflow-api/
├── Dockerfile
├── docker-compose.yml
├── deploy.sh
├── records/              # Mounted volume for uploaded files
└── src/
    └── main/resources/
        └── application-docker.properties
```

## 🔧 Configuration

### Docker Environment Variables
- `SPRING_PROFILES_ACTIVE=docker` - Uses Docker-specific configuration
- `JAVA_OPTS=-Xmx512m -Xms256m` - JVM memory settings

### Volume Mounts
- `./records:/app/records` - Persists uploaded files on the host

### Port Mapping
- `8080:8080` - Maps container port 8080 to host port 8080

## 🌐 Access Points

Once deployed, the application will be available at:

- **API Base URL:** http://localhost:8080
- **Swagger UI:** http://localhost:8080/swagger-ui/index.html
- **File List:** http://localhost:8080/records/files
- **Health Check:** http://localhost:8080/records/files

## 📱 API Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/records` | Get all study records |
| POST | `/records` | Create new study record |
| GET | `/records/files` | List uploaded files |
| POST | `/records/upload` | Upload video files |
| GET | `/records/files/{filename}` | Download specific file |

## 🔍 Monitoring & Troubleshooting

### Check application status
```bash
docker-compose ps
```

### View logs
```bash
docker-compose logs -f
```

### Check health
```bash
curl http://localhost:8080/records/files
```

### Restart application
```bash
docker-compose restart
```

### Stop application
```bash
docker-compose down
```

## 📊 Resource Limits

- **Memory:** 512MB max, 256MB initial
- **File Upload:** 1GB max file size
- **Connection Timeout:** 15 minutes

## 🔒 Production Considerations

For production deployment, consider:

1. **Reverse Proxy:** Use Nginx or Traefik
2. **SSL/TLS:** Add HTTPS certificates
3. **Database:** Add persistent database instead of in-memory storage
4. **Monitoring:** Add health checks and logging
5. **Security:** Add authentication and authorization
6. **Backup:** Regular backup of the `records` directory

### Example production docker-compose.yml
```yaml
version: '3.8'
services:
  skills-studyflow-api:
    build: .
    ports:
      - "8080:8080"
    volumes:
      - ./records:/app/records
    environment:
      - SPRING_PROFILES_ACTIVE=production
      - JAVA_OPTS=-Xmx1g -Xms512m
    restart: always
    labels:
      - "traefik.enable=true"
      - "traefik.http.routers.api.rule=Host(\`your-domain.com\`)"
```

## 🐛 Common Issues

### Port already in use
```bash
# Find process using port 8080
lsof -i :8080

# Kill the process
kill -9 <PID>
```

### Permission denied for records directory
```bash
# Fix permissions
sudo chown -R $USER:$USER records/
chmod 755 records/
```

### Out of memory errors
Increase memory limits in docker-compose.yml:
```yaml
environment:
  - JAVA_OPTS=-Xmx1g -Xms512m
```
