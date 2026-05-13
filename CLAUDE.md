# report-engine-service

REST service untuk report engine, dibangun dengan Spring Boot 3.2.5 + Java 17 + Maven.

## Tech Stack

| Komponen | Library / Versi |
|----------|----------------|
| Framework | Spring Boot 3.2.5 |
| Java | 17 |
| Build | Maven |
| Logging | Log4j2 (`spring-boot-starter-log4j2`) |
| AOP | Spring AOP (`spring-boot-starter-aop`) |
| Dokumentasi API | SpringDoc OpenAPI 2.5.0 (Swagger UI) |
| Boilerplate | Lombok |
| Validasi | `spring-boot-starter-validation` (Jakarta Bean Validation) |

## Menjalankan Aplikasi

```bash
# Default (port 8080)
mvn spring-boot:run

# Dengan profile dev (log level DEBUG)
mvn spring-boot:run -Dspring-boot.run.profiles=dev

# Dengan profile prod (log level WARN)
mvn spring-boot:run -Dspring-boot.run.profiles=prod

# Build JAR
mvn clean package
java -jar target/report-engine-service-1.0.0-SNAPSHOT.jar
```

## Struktur Package

```
src/main/java/com/singgih/reportengineservice/
├── ReportEngineServiceApplication.java   ← entry point
├── aspect/
│   └── LoggingAspect.java                ← AOP: log input/output controller & service
├── config/
│   ├── LoggingProperties.java            ← @ConfigurationProperties app.logging.*
│   └── OpenApiConfig.java                ← metadata Swagger / OpenAPI
├── controller/
│   └── ReportController.java             ← REST endpoints /api/v1/reports
├── dto/
│   ├── ApiResponse.java                  ← response envelope generik
│   ├── ErrorCode.java                    ← enum kode error SGH-*
│   ├── ErrorMessage.java                 ← bilingual error message
│   ├── ErrorSchema.java                  ← error_schema node
│   └── ReportRequest.java                ← request DTO (create/update)
├── exception/
│   └── GlobalExceptionHandler.java       ← @RestControllerAdvice
├── filter/
│   └── RequestIdFilter.java              ← inject requestId ke MDC
├── model/
│   └── Report.java                       ← domain model
└── service/
    └── ReportService.java                ← business logic (in-memory store)

src/main/resources/
├── application.properties                ← base config
├── application-dev.properties            ← log level DEBUG
├── application-prod.properties           ← log level WARN
└── log4j2-spring.xml                     ← Log4j2 config (Console + RollingFile)
```

## REST Endpoints

Base URL: `http://localhost:8080/api/v1/reports`

| Method | Path | Deskripsi |
|--------|------|-----------|
| GET | `/` | Get semua report |
| GET | `/{id}` | Get report by ID |
| POST | `/` | Buat report baru |
| PUT | `/{id}` | Update report |
| DELETE | `/{id}` | Hapus report |

## Pola Response (ApiResponse Envelope)

Semua endpoint mengembalikan struktur berikut:

```json
{
  "error_schema": {
    "error_code": "SGH-000",
    "error_message": {
      "english": "Success",
      "indonesian": "Berhasil"
    }
  },
  "output_schema": { ... }
}
```

- JSON property naming: **SNAKE_CASE** (dikonfigurasi via `spring.jackson.property-naming-strategy=SNAKE_CASE`)
- `output_schema` bernilai `null` pada response error
- Factory methods di `ApiResponse`: `.success(data)`, `.created(data)`, `.error(ErrorCode)`, `.error(ErrorCode, data)`

## Error Codes

Didefinisikan di `ErrorCode.java` (enum):

| Kode | HTTP | Enum | Kondisi |
|------|------|------|---------|
| SGH-000 | 200 | `SUCCESS` | Sukses |
| SGH-000 | 201 | `CREATED` | Data berhasil dibuat |
| SGH-001 | 400 | `VALIDATION_ERROR` | Bean Validation gagal |
| SGH-002 | 404 | `NOT_FOUND` | Resource tidak ditemukan |
| SGH-003 | 409 | `BUSINESS_RULE_VIOLATION` | Pelanggaran aturan bisnis |
| SGH-401 | 401 | `UNAUTHORIZED` | Tidak diotorisasi |
| SGH-403 | 403 | `FORBIDDEN` | Akses ditolak |
| SGH-500 | 500 | `INTERNAL_SERVER_ERROR` | Kesalahan server |

Untuk menambah error baru: tambahkan entry di `ErrorCode.java` saja — `ErrorSchema.of(ErrorCode)` akan otomatis membuild pesan bilingual.

## Logging

### Library & Konfigurasi

- Library: **Log4j2** — logback di-exclude dari semua starter
- Config file: `src/main/resources/log4j2-spring.xml` (Spring-aware, mendukung `logging.level.*` dari properties)
- Log pattern menyertakan `[requestId]` dari MDC:
  ```
  %d{yyyy-MM-dd HH:mm:ss.SSS} [%t] %-5level [%X{requestId}] %logger{36} - %msg%n
  ```

### Output

| Appender | Detail |
|----------|--------|
| Console | stdout |
| RollingFile | `logs/report-engine-service.log` |
| Rotasi | Harian + 10 MB |
| Retensi | 30 hari, arsip: `logs/report-engine-service-yyyy-MM-dd-N.log.gz` |

### Request ID (MDC Tracing)

`RequestIdFilter` (servlet filter, `@Order(1)`) meng-inject `requestId` ke MDC setiap request:
- Diambil dari header `X-Request-ID` jika ada
- Jika tidak ada, di-generate otomatis (`UUID.randomUUID()`)
- Header `X-Request-ID` dikembalikan ke response
- MDC di-clear di `finally` block

### Log Level per Profile

```properties
# application-dev.properties
logging.level.com.singgih=DEBUG
logging.level.org.springframework.web=DEBUG

# application-prod.properties
logging.level.com.singgih=INFO
logging.level.org.springframework.web=WARN
```

### AOP Logging Aspect

`LoggingAspect` mencatat input dan output seluruh method di layer `controller` dan `service`:

```
[ReportController#create] --> [{"title":"Monthly Report","type":"PDF"}]
[ReportController#create] <-- (12ms) ResponseEntity[status=201, body={...}]
[ReportService#findById]  --> ["abc-123"]
[ReportService#findById]  <-- (2ms) {"id":"abc-123","title":...}
```

- `ResponseEntity` di-unwrap: hanya status + body yang dicatat
- Arg tidak aman (HttpServletRequest, HttpServletResponse, BindingResult, Stream) diganti `<ClassName>`
- Exception dicatat di level `ERROR` lalu di-rethrow
- Pesan di-trim jika melebihi `app.logging.max-message-length` (default: 5000 chars), dengan suffix `...[TRIMMED, total=N chars]`

### Konfigurasi Trim

```properties
# application.properties
app.logging.max-message-length=5000
```

Bisa di-override per profile:
```properties
# application-dev.properties
app.logging.max-message-length=10000

# application-prod.properties
app.logging.max-message-length=2000
```

Kelas: `config/LoggingProperties.java` (`@ConfigurationProperties(prefix = "app.logging")`).

## Swagger / SpringDoc

| URL | Deskripsi |
|-----|-----------|
| `http://localhost:8080/swagger-ui.html` | Swagger UI |
| `http://localhost:8080/v3/api-docs` | OpenAPI JSON spec |

Konfigurasi di `OpenApiConfig.java` (title, description, versi, kontak, server URL).  
Anotasi yang digunakan: `@Tag`, `@Operation`, `@Parameter`, `@Schema`.

Catatan: ada naming conflict antara `com.singgih...dto.ApiResponse` dan `io.swagger.v3.oas.annotations.responses.ApiResponse` — diselesaikan dengan fully-qualified annotation name di controller.

## Konvensi Pengembangan

### Menambah Endpoint Baru

1. Tambahkan method di `ReportService`
2. Tambahkan endpoint di `ReportController` — gunakan `ApiResponse.success()` / `.created()` / `.error(ErrorCode.X)`
3. Beri anotasi `@Operation` dengan `responses` untuk setiap HTTP status code
4. Jika ada error baru, tambahkan entry di `ErrorCode` enum

### Menambah DTO Baru

- Gunakan `@Schema` di setiap field (description + example)
- Gunakan `@NotBlank` / `@NotNull` + `requiredMode = Schema.RequiredMode.REQUIRED` untuk field wajib
- Nama field Java tetap camelCase — Jackson SNAKE_CASE config otomatis mengonversi ke snake_case di JSON

### Exception Handling

Semua exception ditangani di `GlobalExceptionHandler`:
- `MethodArgumentNotValidException` → `SGH-001` dengan map field → pesan error di `output_schema`
- `Exception` (fallback) → `SGH-500`
- Untuk menambah handler baru: tambahkan `@ExceptionHandler` method di `GlobalExceptionHandler`

## Properties Lengkap

```properties
# Identitas
spring.application.name=report-engine-service
server.port=8080

# Jackson
spring.jackson.property-naming-strategy=SNAKE_CASE
spring.jackson.serialization.write-dates-as-timestamps=false
spring.jackson.time-zone=Asia/Jakarta
spring.jackson.date-format=yyyy-MM-dd HH:mm:ss

# Logging Aspect
app.logging.max-message-length=5000

# SpringDoc
springdoc.api-docs.path=/v3/api-docs
springdoc.swagger-ui.path=/swagger-ui.html
springdoc.swagger-ui.operationsSorter=method
springdoc.swagger-ui.tagsSorter=alpha
springdoc.swagger-ui.try-it-out-enabled=true

# Actuator
management.endpoints.web.exposure.include=health,info,metrics
management.endpoint.health.show-details=always
```
