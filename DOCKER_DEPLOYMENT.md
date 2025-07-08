# Docker Deployment Guide

This guide explains how to deploy the Skills StudyFlow API using Docker.

## Prerequisites

- Docker installed and running
- Docker Compose installed
- At least 1GB of available RAM
- Port 8080 available

## Quick Start

### 1. Simple Deployment

```bash
# Make the deploy script executable
chmod +x deploy.sh

# Deploy the application
./deploy.sh
```

### 2. Manual Deployment

```bash
# Create necessary directories
mkdir -p records screenshots

# Build and start the application
docker-compose up --build -d

# Check status
docker-compose ps
```

## Configuration

### Environment Variables

The application can be configured using environment variables:

- `JWT_SECRET`: Secret key for JWT token signing (default: auto-generated)
- `JAVA_OPTS`: JVM options (default: `-Xmx512m -Xms256m`)
- `SPRING_PROFILES_ACTIVE`: Spring profile (default: `docker`)

### Volumes

- `./records:/app/records` - Persistent storage for uploaded files
- `./screenshots:/app/screenshots` - Persistent storage for screenshots

## Deployment Options

### Development

```bash
docker-compose up --build -d
```

### Production

```bash
# Use production compose file
docker-compose -f docker-compose.prod.yml up --build -d
```

### With Custom JWT Secret

```bash
# Set JWT secret environment variable
export JWT_SECRET="your-very-secure-secret-key-here"

# Deploy with custom secret
docker-compose up --build -d
```

## Access Points

Once deployed, the application will be available at:

- **API Base URL**: http://localhost:8080
- **Swagger UI**: http://localhost:8080/ui
- **Login Page**: http://localhost:8080/auth/signin

## Authentication

The application requires JWT authentication for most endpoints. To get a token:

```bash
# Get authentication token
curl -X POST http://localhost:8080/auth/authenticate \
  -H 'Content-Type: application/json' \
  -d '{"username":"elias","password":"meow"}'
```

Use the returned token in subsequent requests:

```bash
# Use token in API requests
curl -H 'Authorization: Bearer YOUR_TOKEN' http://localhost:8080/records
```

## Default Users

The application comes with these default users:
- `elias` / `meow`
- `yang` / `password`
- `scplay` / `q123456`
- `user` / `password123`

## Management Commands

### View Logs

```bash
docker-compose logs -f
```

### Stop Application

```bash
docker-compose down
```

### Restart Application

```bash
docker-compose restart
```

### Clean Up

```bash
# Remove containers and volumes
docker-compose down -v

# Remove images as well
docker-compose down -v --rmi all
```

## Health Check

The application includes a health check that monitors:
- Application startup
- Basic endpoint availability
- Container health status

Check health status:

```bash
docker-compose ps
```

## Troubleshooting

### Application Won't Start

1. Check logs:
   ```bash
   docker-compose logs
   ```

2. Verify Docker resources:
   ```bash
   docker system df
   ```

3. Check port availability:
   ```bash
   lsof -i :8080
   ```

### Authentication Issues

1. Verify JWT secret is set correctly
2. Check token expiration (24 hours by default)
3. Ensure proper Authorization header format: `Bearer TOKEN`

### File Upload Issues

1. Check volume mounts:
   ```bash
   docker-compose exec app ls -la /app/
   ```

2. Verify directory permissions:
   ```bash
   ls -la records/ screenshots/
   ```

## Performance Tuning

### Memory Settings

Adjust JVM memory settings based on your system:

```yaml
environment:
  - JAVA_OPTS=-Xmx1g -Xms512m
```

### Resource Limits

For production, set resource limits:

```yaml
deploy:
  resources:
    limits:
      cpus: '2'
      memory: 1.5G
    reservations:
      cpus: '0.5'
      memory: 512M
```

## Security Considerations

1. **Change default JWT secret** in production
2. **Use environment variables** for sensitive configuration
3. **Limit container resources** to prevent resource exhaustion
4. **Use HTTPS** in production (requires reverse proxy)
5. **Change default passwords** for user accounts

## Backup and Recovery

### Backup Data

```bash
# Backup uploaded files
tar -czf backup-$(date +%Y%m%d).tar.gz records/ screenshots/
```

### Restore Data

```bash
# Stop application
docker-compose down

# Restore files
tar -xzf backup-YYYYMMDD.tar.gz

# Restart application
docker-compose up -d
```
