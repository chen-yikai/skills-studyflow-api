# StudyFlow OAuth Flow

## Overview
This is a simplified OAuth-like authentication flow designed for mobile app integration.

## Endpoints

### 1. GET /auth/signin
**Purpose**: Displays the login page for user authentication

**Parameters**:
- `redirect_uri` (optional): Custom redirect URI. Default: `studyflow://oauth`

**Example**:
```
GET /auth/signin
GET /auth/signin?redirect_uri=myapp://auth
```

### 2. POST /auth/authenticate
**Purpose**: Authenticates user credentials and returns JWT token

**Request Body**:
```json
{
  "username": "demo",
  "password": "password123"
}
```

**Response** (Success):
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "tokenType": "Bearer",
  "username": "demo"
}
```

### 3. POST /auth/signout
**Purpose**: Sign out functionality

**Response**:
```json
{
  "message": "Sign out successful"
}
```

### 4. GET /auth/verify
**Purpose**: Verify if an access token is valid

**Headers**:
```
Authorization: Bearer <access_token>
```

**Response**:
```json
{
  "valid": true,
  "user": {
    "id": "user-id",
    "username": "demo",
    "email": "demo@example.com"
  }
}
```

## Mobile App Integration Flow

1. **Mobile app opens login page**: Navigate to `/auth/signin` in a web view
2. **User enters credentials**: User fills out the login form
3. **Successful authentication**: Page redirects to `studyflow://oauth?access_token=...&token_type=Bearer&username=demo`
4. **Mobile app captures redirect**: App intercepts the custom URL scheme and extracts the access token
5. **Use token for API calls**: Include the token in subsequent API requests

## URL Redirect Format

After successful login, the page redirects to:
```
studyflow://oauth?access_token=<JWT_TOKEN>&token_type=Bearer&username=<USERNAME>
```

## Demo Users

- Username: `demo`, Password: `password123`
- Username: `user1`, Password: `pass123`  
- Username: `admin`, Password: `admin123`

## Testing

You can test the flow by:
1. Starting the server: `./gradlew bootRun`
2. Opening `http://localhost:8080/auth/signin` in a browser
3. Logging in with demo credentials
4. Observing the redirect URL (will show an error in browser since `studyflow://` isn't registered)

## Security Notes

- JWT tokens expire after 24 hours (configurable)
- Use HTTPS in production
- Store JWT secret securely
- Consider implementing token refresh mechanism for production use
