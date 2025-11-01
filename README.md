# File Storage & Sharing System

A secure file storage and sharing service with password protection, auto-expiry, and download limits. Built with Spring Boot and MinIO.

![Java](https://img.shields.io/badge/Java-21-orange) ![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.7-brightgreen) ![PostgreSQL](https://img.shields.io/badge/PostgreSQL-15-blue)

---

## ✨ Features

- 🔐 **Secure File Upload** - Upload files up to 100MB
- 🔗 **Shareable Links** - Generate unique, unpredictable URLs
- 🔒 **Password Protection** - Optional password-based access
- ⏱️ **Auto-Expiry** - Files automatically delete after set time
- 📊 **Download Tracking** - Track counts and set download limits
- 🗑️ **Soft Delete** - Separate delete tokens for file management
- 📁 **Multiple File Types** - Images, documents, archives, etc.

---

## 🛠️ Tech Stack

**Backend:**
- Java 21
- Spring Boot 3.5.7
- Spring Data JPA + Hibernate
- Lombok

**Storage & Database:**
- PostgreSQL 15 (metadata)
- MinIO (object storage)
- Redis 7 (caching)

**Tools:**
- Gradle 8.14.3
- Docker & Docker Compose
- BCrypt (password hashing)

---

## 🚀 Quick Start

### Prerequisites
- Java 21+
- Docker Desktop
- Git

### Installation

**1. Clone & Start Services**
```bash
git clone https://github.com/0xNihad/file-storage
cd filestore-backend
docker-compose up -d
```

**2. Build & Run**
```bash
./gradlew bootRun
```

**3. Verify**
```bash
curl http://localhost:8080/api/health
```

Application runs on: `http://localhost:8080/api`

---

## 📚 API Endpoints

### Upload File
```http
POST /api/upload
Content-Type: multipart/form-data
```

**Parameters:**
- `file` (required) - File to upload
- `expiryHours` (optional) - Hours until deletion (default: 24)
- `password` (optional) - Password protect the file
- `maxDownloads` (optional) - Download limit

**Response:**
```json
{
  "shareUrl": "/api/f/x7k9mP2qL",
  "deleteUrl": "/api/delete/del_abc123",
  "expiresAt": "2025-11-01T14:30:00"
}
```

---

### Download File
```http
GET /api/f/{shareToken}?password={password}
```

---

### Get File Info
```http
GET /api/f/{shareToken}/info
```

**Response:**
```json
{
  "fileName": "document.pdf",
  "fileSize": 2048576,
  "downloadCount": 5,
  "isPasswordProtected": true,
  "isExpired": false
}
```

---

### Delete File
```http
DELETE /api/delete/{deleteToken}
```

---

## ⚙️ Configuration

**`application.yml`**
```yaml
server:
  port: 8080
  servlet:
    context-path: /api

filestore:
  storage:
    minio:
      endpoint: http://localhost:9000
      access-key: minioadmin
      secret-key: minioadmin
      bucket-name: filestore-bucket
  
  file:
    default-expiry-hours: 24
    max-file-size-bytes: 104857600  # 100MB
    allowed-extensions:
      - pdf
      - jpg
      - png
      - docx
      - zip
```

**`docker-compose.yml`**
```yaml
version: '3.8'

services:
  postgres:
    image: postgres:15
    ports:
      - "5432:5432"
    environment:
      POSTGRES_DB: filestore
      POSTGRES_USER: admin
      POSTGRES_PASSWORD: secret123

  redis:
    image: redis:7-alpine
    ports:
      - "6379:6379"

  minio:
    image: minio/minio
    command: server /data --console-address ":9001"
    ports:
      - "9000:9000"
      - "9001:9001"
    environment:
      MINIO_ROOT_USER: minioadmin
      MINIO_ROOT_PASSWORD: minioadmin
```

---

## 💡 Usage Examples

**Simple Upload:**
```bash
curl -X POST http://localhost:8080/api/upload \
  -F "file=@document.pdf"
```

**Password-Protected:**
```bash
curl -X POST http://localhost:8080/api/upload \
  -F "file=@secret.pdf" \
  -F "password=mySecret123"
```

**One-Time Download:**
```bash
curl -X POST http://localhost:8080/api/upload \
  -F "file=@data.xlsx" \
  -F "maxDownloads=1"
```

**Download with Password:**
```bash
curl -O http://localhost:8080/api/f/abc123?password=mySecret123
```

---

## 🗄️ Database Schema

```sql
CREATE TABLE files (
    id                  UUID PRIMARY KEY,
    original_file_name  VARCHAR(255) NOT NULL,
    file_size          BIGINT NOT NULL,
    storage_key        VARCHAR(500) NOT NULL,
    share_token        VARCHAR(50) UNIQUE NOT NULL,
    delete_token       VARCHAR(50) UNIQUE NOT NULL,
    password_hash      VARCHAR(255),
    upload_date        TIMESTAMP NOT NULL,
    expiry_date        TIMESTAMP NOT NULL,
    download_count     INTEGER DEFAULT 0,
    max_downloads      INTEGER,
    is_deleted         BOOLEAN DEFAULT FALSE,
    uploader_ip        VARCHAR(50),
    created_at         TIMESTAMP NOT NULL,
    updated_at         TIMESTAMP NOT NULL
);
```

---

## 🔒 Security

- **Token-based access** - Cryptographically secure share/delete tokens
- **BCrypt password hashing** - Industry-standard encryption
- **File validation** - Size, type, and content checks
- **Rate limiting** - IP-based upload restrictions
- **Soft delete** - Recovery option before permanent deletion

---

## 🧪 Development

**Run Tests:**
```bash
./gradlew test
```

**Access Database:**
```bash
docker exec -it filestore-postgres psql -U admin -d filestore
```

**MinIO Console:**
```
http://localhost:9001
Login: minioadmin / minioadmin
```

**Project Structure:**
```
src/main/java/com/filestore/
├── controller/       # REST endpoints
├── service/          # Business logic
├── repository/       # Database access
├── model/           # Entities & DTOs
├── exception/       # Error handling
└── util/            # Helpers & validators
```

---

## 🚢 Deployment

**Build JAR:**
```bash
./gradlew clean build
```

**Run with Docker:**
```bash
docker build -t filestore-backend .
docker run -p 8080:8080 filestore-backend
```

---

## 📞 Contact

- GitHub: [@0xNihad](https://github.com/0xNihad)
- Email: mirzazadanihad1@gmail.com

---

## 📄 License

MIT License - see [LICENSE](LICENSE) file

---

**Built with ❤️ using Spring Boot**
