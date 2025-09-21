
# Spring Boot Bean Scopes — `request`, `session`, `prototype`

A tiny Spring Boot project that demonstrates three bean scopes in the context of a real HTTP API:

- **`@RequestScope`** — a new bean instance per HTTP request.
- **`@SessionScope`** — one bean instance per *HTTP session* (persists across multiple requests from the same client).
- **`@Scope("prototype")`** — a *new* bean instance every time it is injected/asked from the context.

This repo includes a ready‑to‑use **Postman collection** with real requests, response headers, and bodies so you can reproduce the behavior.

---

## Quick start

> Requires **Java 21+** (or 17+ should also work) and either Maven or Gradle.

```bash
# clone and enter
git clone <your-repo-url>.git
cd <your-repo-folder>

# run (Maven)
./mvnw spring-boot:run

# or run (Gradle)
./gradlew bootRun
```

Server will start on **http://localhost:8080**.

---

## Endpoints at a glance

| Scope          | Endpoint                      | Method | Purpose |
|----------------|-------------------------------|--------|---------|
| **Session**    | `/bucket`                     | GET    | Read session bucket (persists per HTTP session) |
| **Session**    | `/bucket`                     | POST   | Add/update/remove item in the session bucket |
| **Request**    | `/transaction/p2p`            | POST   | Echo request scoped data for a single call |
| **Prototype**  | `/notification`               | POST   | Generates fresh values using a prototype bean |

> **Note on sessions:** To observe `@SessionScope` properly in Postman, keep the same tab and make sure the **cookie jar** is enabled so your `JSESSIONID` is reused between requests.

---

## API Examples (headers + bodies)

> The examples below are **taken from the included Postman collection**. Timestamps will differ on your machine.

### 1) Session scope — Bucket

#### GET `/bucket`

**Response headers**
```
Content-Type: application/json
Transfer-Encoding: chunked
Date: Sat, 23 Aug 2025 19:35:34 GMT
Keep-Alive: timeout=60
Connection: keep-alive
```

**Response body**
```json
[
  { "name": "Chixol Black", "count": 1 },
  { "name": "Samsung S25 Ultra", "count": 1 }
]
```

**cURL**
```bash
curl -X GET http://localhost:8080/bucket
```

---

#### POST `/bucket`

**Request body**
```json
{
  "name": "Samsung S25 Ultra",
  "count": -2
}
```

**Response headers**
```
Content-Type: application/json
Transfer-Encoding: chunked
Date: Sat, 23 Aug 2025 19:35:32 GMT
Keep-Alive: timeout=60
Connection: keep-alive
```

**Response body**
```json
[
  { "name": "Chixol Black", "count": 1 },
  { "name": "Samsung S25 Ultra", "count": 1 }
]
```

**cURL**
```bash
curl -X POST http://localhost:8080/bucket   -H "Content-Type: application/json"   -d '{ "name": "Samsung S25 Ultra", "count": -2 }'
```

> Why session scope here? The "bucket" lives in the user's HTTP session. If you open a *new* Postman tab (or clear cookies), you'll initialize a fresh bucket.

---

### 2) Request scope — P2P transaction

#### POST `/transaction/p2p`

**Request body**
```json
{
  "card": "4067070006562115",
  "amount": 11
}
```

**Response headers**
```
Content-Type: application/json
Transfer-Encoding: chunked
Date: Sat, 23 Aug 2025 19:52:08 GMT
Keep-Alive: timeout=60
Connection: keep-alive
```

**Response body**
```json
{
  "card": "4067070006562115",
  "amount": 11
}
```

**cURL**
```bash
curl -X POST http://localhost:8080/transaction/p2p   -H "Content-Type: application/json"   -d '{ "card": "4067070006562115", "amount": 11 }'
```

> Why request scope? Each HTTP call gets a brand‑new instance that is discarded right after the response is sent.

---

### 3) Prototype scope — Notification

#### POST `/notification`

**Request body**
```json
{
  "phone": "998935239989",
  "email": "nodir@gmail.com"
}
```

**Response headers**
```
Content-Type: application/json
Transfer-Encoding: chunked
Date: Sat, 23 Aug 2025 20:29:05 GMT
Keep-Alive: timeout=60
Connection: keep-alive
```

**Response body (sample)**
```json
{
  "data": {
    "phone": "4dbbdcbf-8932-44f0-83de-a16e6206e296",
    "email": "ef4d05d4-cd0a-4b34-b704-ba880568f1c6"
  }
}
```

**cURL**
```bash
curl -X POST http://localhost:8080/notification   -H "Content-Type: application/json"   -d '{ "phone": "998935239989", "email": "nodir@gmail.com" }'
```

> Why prototype? A **new** bean instance is produced each time it is requested, which is handy for one‑off generators (IDs, tokens, randomizers, etc.).

---

## Postman

The collection file lives at: **`Scope.postman_collection.json`** (included in the zip below).  
Import it to Postman and execute the three folders: `session`, `request`, `prototype`.

---

## FAQ

- **Do I need Spring proxies?**  
  Yes—Spring creates proxies to realize request & session scoping behind standard singleton controllers/services.

- **How do I “prove” session scope works?**  
  Hit `/bucket` several times from the *same* Postman tab: the state persists. Open a new tab (fresh cookies) to see a new, empty state.

---

## License

MIT (or set your preferred license).
