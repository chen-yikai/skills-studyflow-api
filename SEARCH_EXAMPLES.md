# Search Endpoint Examples

## Overview
The search endpoint allows you to find records by searching through:
- **Record names**
- **Note data content**

## Endpoint
```
GET /records/search?q={query}
```

## Authentication
Use either:
- API Key: `key: key` (header)
- JWT Bearer Token: `Authorization: Bearer {token}`

## Request Examples

### 1. Search by Record Name
```bash
# Search for records with "Math" in the name
curl -H "key: key" "http://localhost:8080/records/search?q=Math"

# Search for records with "Programming" in the name
curl -H "key: key" "http://localhost:8080/records/search?q=Programming"
```

### 2. Search by Note Data
```bash
# Search for records containing "algebra" in any note
curl -H "key: key" "http://localhost:8080/records/search?q=algebra"

# Search for records containing "Spring Boot" in any note
curl -H "key: key" "http://localhost:8080/records/search?q=Spring%20Boot"
```

### 3. Case-Insensitive Search
```bash
# These will find the same results (case doesn't matter)
curl -H "key: key" "http://localhost:8080/records/search?q=MATH"
curl -H "key: key" "http://localhost:8080/records/search?q=math"
curl -H "key: key" "http://localhost:8080/records/search?q=Math"
```

### 4. Partial Matches
```bash
# Search for partial words
curl -H "key: key" "http://localhost:8080/records/search?q=prog"  # matches "Programming"
curl -H "key: key" "http://localhost:8080/records/search?q=alg"   # matches "algebra"
```

## Response Format

### Success Response (200 OK)
```json
{
  "query": "Math",
  "count": 1,
  "results": [
    {
      "id": "record1",
      "name": "Math Study Session",
      "file": "math_notes.pdf",
      "date": "2025-01-08",
      "note": [
        {
          "time": "10:00",
          "data": "Reviewed algebra fundamentals and quadratic equations"
        }
      ],
      "tags": ["mathematics", "algebra"],
      "screenshots": []
    }
  ]
}
```

### Error Response (400 Bad Request)
```json
{
  "message": "Query parameter cannot be blank"
}
```

## Features

✅ **Search by name**: Finds records with matching names  
✅ **Search by note content**: Searches through all note data  
✅ **Case-insensitive**: Works regardless of letter case  
✅ **Partial matching**: Finds partial word matches  
✅ **Multiple results**: Returns all matching records  
✅ **Result count**: Shows how many records matched  
✅ **Query echo**: Shows what was searched for  

## Usage in Swagger UI

1. Go to `http://localhost:8080/swagger-ui.html`
2. Authenticate using the "key" option with value "key"
3. Find the `/records/search` endpoint
4. Enter your search query in the `q` parameter
5. Execute the request

## Search Logic

The search function looks for the query string in:
1. **Record.name** - The record's title/name
2. **Record.note[].data** - The content of all note entries

The search is:
- Case-insensitive
- Substring matching (partial matches work)
- OR-based (matches either name OR note content)

## Performance Notes

- The search is performed in-memory for fast results
- All searches are substring-based (contains matching)
- Empty or blank queries return a 400 error
- Results are returned in the order they exist in the system
