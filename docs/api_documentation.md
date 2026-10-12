# REST API Specification (OpenAPI Style)

**Project:** DevOps-Enabled Public Parking Availability Dashboard (ParkWatch)  
**Student:** Sajiya Shaikh | Roll No: 24102A2005  
**Base URL:** `http://localhost:8080/api/v1`  
**Content-Type:** `application/json`

---

## 1. Summary of Endpoints

| Method | Endpoint | Description | Status Codes |
| :--- | :--- | :--- | :--- |
| `GET` | `/api/v1/health` | Application health ping & service metadata | `200 OK` |
| `GET` | `/api/v1/dashboard/summary` | Overall city parking statistics & totals | `200 OK` |
| `GET` | `/api/v1/locations` | List all parking locations (search & status filters) | `200 OK` |
| `GET` | `/api/v1/locations/{id}` | Get single location by ID with details | `200 OK`, `404 Not Found` |
| `POST` | `/api/v1/locations` | Register a new parking location | `201 Created`, `400 Bad Request` |
| `PUT` | `/api/v1/locations/{id}` | Update parking details or capacity | `200 OK`, `400 Bad Request`, `404 Not Found` |
| `PATCH`| `/api/v1/locations/{id}/occupancy` | Direct update of occupied space count | `200 OK`, `400 Bad Request`, `404 Not Found` |
| `DELETE`| `/api/v1/locations/{id}` | Remove a parking location | `204 No Content`, `404 Not Found` |
| `POST` | `/api/v1/events` | Submit check-in or check-out event | `201 Created`, `400 Bad Request`, `404 Not Found` |
| `GET` | `/api/v1/events/location/{id}` | Retrieve audit event history for location | `200 OK`, `404 Not Found` |
| `GET` | `/api/v1/events/recent` | Retrieve recent events across all facilities | `200 OK` |
| `GET` | `/api/v1/alerts` | List all locations in high (>85%) or critical (>95%) saturation | `200 OK` |
| `GET` | `/actuator/health` | Spring Boot Actuator system health status | `200 OK`, `503 Service Unavailable` |

---

## 2. Endpoint Details & Schemas

### 2.1 GET `/api/v1/dashboard/summary`
Returns city-wide aggregated totals for public parking availability.

**Response Example (`200 OK`):**
```json
{
  "totalLocations": 24,
  "totalCapacity": 4250,
  "totalOccupied": 2780,
  "totalAvailable": 1470,
  "overallOccupancyRate": 65.41,
  "totalEvSpots": 180,
  "totalAccessibleSpots": 115,
  "criticalAlertsCount": 2,
  "highAlertsCount": 3
}
```

---

### 2.2 GET `/api/v1/locations`
Query parameters supported:
- `search` (string, optional) - search by name or address
- `filter` (string, optional) - filter by status: `ACTIVE`, `FULL`, `MAINTENANCE`

**Response Example (`200 OK`):**
```json
[
  {
    "id": 1,
    "name": "Metro Central Garage",
    "address": "100 Grand Avenue, Downtown",
    "cityDistrict": "Downtown Core",
    "totalCapacity": 250,
    "occupiedSpaces": 185,
    "availableSpaces": 65,
    "occupancyRate": 74.0,
    "evChargingSpots": 12,
    "accessibleSpots": 8,
    "hourlyRate": 3.50,
    "status": "ACTIVE",
    "statusBadge": "MODERATE",
    "updatedAt": "2026-10-12T22:50:00"
  }
]
```

---

### 2.3 POST `/api/v1/locations`
Create a new parking lot facility.

**Request Body:**
```json
{
  "name": "Civic Center Innovation Garage",
  "address": "500 Tech Boulevard, Downtown",
  "cityDistrict": "Downtown",
  "totalCapacity": 100,
  "occupiedSpaces": 25,
  "evChargingSpots": 10,
  "accessibleSpots": 4,
  "hourlyRate": 2.50,
  "status": "ACTIVE"
}
```

**Response Example (`201 Created`):**
```json
{
  "id": 25,
  "name": "Civic Center Innovation Garage",
  "address": "500 Tech Boulevard, Downtown",
  "cityDistrict": "Downtown",
  "totalCapacity": 100,
  "occupiedSpaces": 25,
  "availableSpaces": 75,
  "occupancyRate": 25.0,
  "evChargingSpots": 10,
  "accessibleSpots": 4,
  "hourlyRate": 2.50,
  "status": "ACTIVE"
}
```

---

### 2.4 POST `/api/v1/events`
Record a vehicle check-in, check-out, or maintenance hold.

**Request Body:**
```json
{
  "locationId": 25,
  "eventType": "CHECK_IN",
  "spacesCount": 5,
  "notes": "Automated gate sensor entry"
}
```

**Validation & Integrity Constraints:**
- If `CHECK_IN` causes `occupiedSpaces + spacesCount > totalCapacity`, returns `400 Bad Request`:
  ```json
  {
    "status": 400,
    "error": "Bad Request",
    "message": "Occupied spaces cannot exceed total capacity (100)."
  }
  ```

---

### 2.5 GET `/api/v1/alerts`
Returns real-time high (>85%) and critical (>95%) capacity alert entries.

**Response Example (`200 OK`):**
```json
[
  {
    "locationId": 3,
    "locationName": "North Station Park & Ride",
    "cityDistrict": "North Suburbs",
    "currentOccupancy": 192,
    "totalCapacity": 200,
    "occupancyRate": 96.0,
    "severity": "CRITICAL",
    "alertMessage": "Location has reached critical capacity threshold (96.00% occupied)."
  }
]
```

---

### 2.6 GET `/actuator/health`
System operational health check.

**Response Example (`200 OK`):**
```json
{
  "status": "UP",
  "components": {
    "db": {
      "status": "UP",
      "details": {
        "database": "H2",
        "validationQuery": "isValid()"
      }
    },
    "diskSpace": {
      "status": "UP"
    },
    "ping": {
      "status": "UP"
    }
  }
}
```
