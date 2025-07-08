# CORS Configuration for HTTPS Deployment

## Overview
This application now supports configurable CORS settings for secure HTTPS deployment. The CORS configuration is environment-specific and can be customized for different deployment scenarios.

## Configuration Files

### Local Development (`application.properties`)
```properties
app.cors.allowed-origins=http://localhost:3000,http://localhost:5173,http://localhost:8080
app.cors.allowed-methods=GET,POST,PUT,DELETE,OPTIONS
app.cors.allowed-headers=*
app.cors.allow-credentials=true
app.cors.exposed-headers=Authorization
```

### Production (`application-docker.properties`)
```properties
app.cors.allowed-origins=${CORS_ALLOWED_ORIGINS:https://yourdomain.com,https://www.yourdomain.com}
app.cors.allowed-methods=GET,POST,PUT,DELETE,OPTIONS
app.cors.allowed-headers=*
app.cors.allow-credentials=true
app.cors.exposed-headers=Authorization
```

## Deployment Instructions

### 1. Local Development
No additional configuration needed. The application will use the default localhost origins.

### 2. Docker Production Deployment

#### Option A: Using Environment Variables
Set the `CORS_ALLOWED_ORIGINS` environment variable before running:

```bash
export CORS_ALLOWED_ORIGINS="https://yourdomain.com,https://www.yourdomain.com,https://api.yourdomain.com"
docker-compose -f docker-compose.prod.yml up -d
```

#### Option B: Using .env File
Create a `.env` file in your project root:

```bash
# .env
JWT_SECRET=your-production-jwt-secret-key-here
CORS_ALLOWED_ORIGINS=https://yourdomain.com,https://www.yourdomain.com,https://api.yourdomain.com
```

Then run:
```bash
docker-compose -f docker-compose.prod.yml up -d
```

#### Option C: Direct Docker Command
```bash
docker run -d \
  -p 8080:8080 \
  -e SPRING_PROFILES_ACTIVE=production \
  -e JWT_SECRET=your-production-jwt-secret \
  -e CORS_ALLOWED_ORIGINS="https://yourdomain.com,https://www.yourdomain.com" \
  your-app-image
```

### 3. Server Deployment (with reverse proxy)

If you're using a reverse proxy (like Nginx) with SSL termination, make sure to:

1. **Set the correct origins**: Include your domain with HTTPS protocol
2. **Configure proxy headers**: Ensure your reverse proxy forwards the correct headers

Example Nginx configuration:
```nginx
location /api/ {
    proxy_pass http://localhost:8080/;
    proxy_set_header Host $host;
    proxy_set_header X-Real-IP $remote_addr;
    proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
    proxy_set_header X-Forwarded-Proto $scheme;
}
```

## Security Considerations

1. **Never use wildcard origins (`*`) in production** - This configuration now enforces specific origins
2. **Use HTTPS in production** - All allowed origins should use HTTPS protocol
3. **Limit origins to trusted domains** - Only include domains you control
4. **Review exposed headers** - Only expose necessary headers

## Testing CORS Configuration

### Test with curl:
```bash
# Test preflight request
curl -X OPTIONS \
  -H "Origin: https://yourdomain.com" \
  -H "Access-Control-Request-Method: POST" \
  -H "Access-Control-Request-Headers: Content-Type,Authorization" \
  -v https://your-api-domain.com/api/records

# Test actual request
curl -X POST \
  -H "Origin: https://yourdomain.com" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer your-token" \
  -v https://your-api-domain.com/api/records
```

### Expected Response Headers:
```
Access-Control-Allow-Origin: https://yourdomain.com
Access-Control-Allow-Credentials: true
Access-Control-Allow-Methods: GET, POST, PUT, DELETE, OPTIONS
Access-Control-Allow-Headers: *
Access-Control-Expose-Headers: Authorization
Access-Control-Max-Age: 3600
```

## Troubleshooting

### Common Issues:

1. **CORS error in browser**: Check that your frontend domain is in the allowed origins list
2. **Preflight failures**: Ensure OPTIONS method is included in allowed methods
3. **Credential issues**: Make sure `allow-credentials` is set to true if you're sending cookies/auth headers

### Debug Steps:

1. Check application logs for CORS-related errors
2. Verify environment variables are set correctly
3. Test API endpoints with different origins
4. Check browser developer tools for CORS error messages

## Environment Variables Reference

| Variable | Description | Default | Example |
|----------|-------------|---------|---------|
| `CORS_ALLOWED_ORIGINS` | Comma-separated list of allowed origins | `https://yourdomain.com,https://www.yourdomain.com` | `https://app.example.com,https://www.example.com` |
| `JWT_SECRET` | Secret key for JWT tokens | (development key) | `your-production-secret-key` |
| `SPRING_PROFILES_ACTIVE` | Active Spring profile | `default` | `production` |
