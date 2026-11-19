# RailFlow – IRCTC-Inspired High-Concurrency Railway Reservation System

![Java 21](https://img.shields.io/badge/Java-21-orange?logo=openjdk)
![Spring Boot 3.2](https://img.shields.io/badge/Spring%20Boot-3.2.5-brightgreen?logo=springboot)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-blue?logo=postgresql)
![Redis](https://img.shields.io/badge/Redis-Redisson-red?logo=redis)
![Apache Kafka](https://img.shields.io/badge/Apache%20Kafka-Event--Driven-black?logo=apachekafka)
![Razorpay](https://img.shields.io/badge/Payment-Razorpay-blue?logo=razorpay)
![React 18](https://img.shields.io/badge/Frontend-React%2018%20%2B%20Tailwind-cyan?logo=react)

RailFlow is an enterprise-grade railway reservation platform designed to mirror Indian Railways (IRCTC) production booking workflows, with strict guarantees around **multithreading, atomic inventory locking, distributed idempotency, payment signature verification, and event-driven asynchronous processing**.

---

## 1. Architectural Highlights & Interview Talking Points

### A. High-Concurrency Seat Allocation & Race Condition Prevention
* **The Challenge:** 10,000+ concurrent requests attempting to book the last available Tatkal or General quota seat on high-demand trains (e.g., *Vande Bharat* or *Rajdhani Express*).
* **Distributed Locking (Redisson):**
  ```java
  String lockKey = "seat-lock:" + trainId + ":" + date + ":" + coachType;
  RLock lock = redissonClient.getLock(lockKey);
  boolean acquired = lock.tryLock(10, 30, TimeUnit.SECONDS);
  ```
* **Database Row Locking:** Uses Hibernate optimistic concurrency control (`@Version`) combined with JPA pessimistic write locking (`@Lock(LockModeType.PESSIMISTIC_WRITE)`) on specific inventory segments.
* **Segment-Based Inventory Model:** Seats are partitioned into overlapping intermediate journey segments (e.g. `NDLS -> CNB -> PRYJ -> BSB`). A seat vacated at Kanpur can be re-allocated for Kanpur to Varanasi without double booking.
* **10-Minute Ephemeral Seat Holds:** When a passenger initiates checkout, selected inventory entries enter status `HELD` with an expiry timestamp. A `@Scheduled` background worker (`releaseExpiredHolds()`) automatically returns abandoned holds back to `AVAILABLE`.

### B. Distributed Idempotency (Preventing Duplicate Charges)
* Every booking submission accepts an `Idempotency-Key` HTTP header.
* Stored in `idempotency_records` table and cached in Redis.
* Concurrent duplicate clicks return the same pending/confirmed response rather than allocating multiple seats or generating duplicate Razorpay orders.

### C. Razorpay Payment Gateway & HMAC-SHA256 Verification
* Dual verification: Immediate frontend return confirmation + backend webhook listener.
* Cryptographic signature verification using HMAC-SHA256 (`Mac.getInstance("HmacSHA256")`).
* State machine enforcement: `INITIATED -> SEATS_HELD -> PAYMENT_PENDING -> CONFIRMED` (Failure branches: `PAYMENT_FAILED`, `HOLD_EXPIRED`, `CANCELLED`).

### D. Event-Driven Architecture (Transactional Outbox + Kafka)
* When a booking commits, an event record is written into `outbox_events` in the **same PostgreSQL transaction**.
* `OutboxPublisherJob` reads pending outbox events and pushes them to Kafka topics:
  * `booking-created-events`
  * `booking-confirmed-events`
  * `booking-cancelled-events`
  * `payment-successful-events`
* `BookingEventConsumer` listens asynchronously, generates PDF reservation slips, and sends confirmation emails.

### E. Dynamic PDF Ticket & QR Code Generation
* Generated via **OpenPDF (LibrePDF)** with official Indian Railways Electronic Reservation Slip (ERS) layout.
* Encodes PNR verification link inside an embedded high-density **ZXing 2D QR Code**.

---

## 2. Technology Stack

| Layer | Technology |
|---|---|
| **Core Language** | Java 21 LTS |
| **Backend Framework** | Spring Boot 3.2.5 (Spring MVC, Data JPA, Security) |
| **Database** | PostgreSQL 16 (HikariCP connection pool, Flyway migrations) |
| **Distributed Cache & Locking** | Redis 7 + Redisson 3.27 |
| **Event Bus** | Apache Kafka + Zookeeper (Transactional Outbox Pattern) |
| **Authentication & RBAC** | Spring Security 6 + JWT (jjwt 0.12.3) + BCrypt |
| **Payment Gateway** | Razorpay REST API + Webhooks (Mock / Live modes) |
| **Ticket Documents** | OpenPDF 1.3 + Google ZXing QR Engine |
| **Frontend** | React 18, Vite, Tailwind CSS, Lucide Icons, Axios |
| **API Documentation** | SpringDoc OpenAPI 3.0 / Swagger UI |
| **DevOps & Containers** | Docker, Multi-Stage Dockerfile, Docker Compose |

---

## 3. Project Directory Structure

```
RailFlow/
├── pom.xml                               # Java 21 Spring Boot parent pom with all dependencies
├── Dockerfile                            # Multi-stage Amazon Corretto 21 container
├── docker-compose.yml                    # PostgreSQL, Redis, Kafka, Zookeeper, RailFlow App
├── README.md
│
├── src/main/java/com/railflow/
│   ├── RailFlowApplication.java         # @SpringBootApplication, @EnableScheduling, @EnableAsync
│   ├── admin/                           # Operations dashboard stats & user telemetry
│   ├── auth/                            # Registration, Login, JWT tokens, Idempotency service
│   ├── booking/                         # State machine, PNR generator, booking service
│   ├── cancellation/                    # IRCTC refund rules & voluntary cancellation
│   ├── common/                          # Enums, ApiResponse, MapStruct mappers
│   ├── config/                          # Security, Redis, ThreadPoolTaskExecutor, Swagger
│   ├── exception/                       # GlobalExceptionHandler & custom domain exceptions
│   ├── inventory/                       # Redisson distributed locks, Segment-wise seat engine
│   ├── notification/                    # Kafka consumers, EmailService, Transactional Outbox
│   ├── passenger/                       # Passenger profile management
│   ├── payment/                         # Razorpay order creation, HMAC verification, Webhooks
│   ├── refund/                          # Automated refund tracking
│   ├── route/                           # Train routes & sequence-based station lookups
│   ├── schedule/                        # Day-wise train schedules & delays
│   ├── security/                        # JwtTokenProvider, JwtAuthenticationFilter
│   ├── station/                         # Station lookup & autocomplete
│   ├── ticket/                          # OpenPDF Electronic Reservation Slip + ZXing QR code
│   ├── train/                           # Trains, coaches, berths, train search engine
│   ├── user/                            # User entity, roles, profile management
│   └── waitinglist/                     # FIFO RAC / WL allocation & promotion engine
│
├── src/main/resources/
│   ├── application.yml                  # Full datasource, redis, kafka, jwt, razorpay configuration
│   └── db/migration/
│       ├── V1__initial_schema.sql       # 20+ tables DDL, foreign keys, unique constraints, indexes
│       └── V2__seed_initial_data.sql    # 10 Indian Railway stations, 4 trains, routes, coaches, inventory
│
└── frontend/                            # React 18 Single Page Application
    ├── package.json
    ├── vite.config.js
    ├── tailwind.config.js
    └── src/
        ├── App.jsx                      # Router & navigation
        ├── context/AuthContext.jsx      # JWT auth state
        ├── pages/
        │   ├── HomePage.jsx             # IRCTC train search bar
        │   ├── SearchResultsPage.jsx    # Live trains & class-wise availability
        │   ├── PassengerDetailsPage.jsx # Multi-passenger form & berth preferences
        │   ├── PaymentCheckoutPage.jsx  # Razorpay checkout & 10-min countdown timer
        │   ├── BookingConfirmationPage.jsx # PNR confirmation & PDF ticket download
        │   ├── PnrEnquiryPage.jsx       # Instant PNR status tracker
        │   ├── MyBookingsPage.jsx       # Booking history & cancellation modal
        │   ├── AuthPage.jsx             # JWT Login & Registration
        │   └── AdminDashboardPage.jsx   # Telemetry, revenue metrics & fleet status
        └── services/api.js              # Axios client with JWT interceptor & Idempotency key
```

---

## 4. Quickstart: Running with Docker Compose

To boot the entire ecosystem (PostgreSQL, Redis, Kafka, Zookeeper, and the Spring Boot application):

```bash
docker-compose up -d
```

### Services Started:
* **RailFlow Application:** `http://localhost:8080`
* **Swagger UI:** `http://localhost:8080/swagger-ui.html`
* **OpenAPI Docs:** `http://localhost:8080/api-docs`
* **PostgreSQL:** `localhost:5432` (DB: `railflow`, User: `railflow_user`, Pass: `railflow_pass`)
* **Redis:** `localhost:6379`
* **Apache Kafka:** `localhost:9092`

---

## 5. Running the React Frontend

```bash
cd frontend
npm install
npm run dev
```

Visit `http://localhost:3000` in your browser.

### Pre-Seeded Demo Credentials:
* **Admin:** `admin@railflow.com` / `Admin@1234`
* **Traveler:** `user@railflow.com` / `User@1234`

---

## 6. Core REST API Endpoints

### Authentication & Users
* `POST /api/v1/auth/register` – Register new user account
* `POST /api/v1/auth/login` – Login & obtain JWT Access + Refresh tokens
* `POST /api/v1/auth/refresh-token` – Refresh expired JWT access token
* `GET  /api/v1/users/profile` – Current user profile

### Stations, Trains & Search
* `GET  /api/v1/stations` – All active stations
* `GET  /api/v1/stations/search?q=NDLS` – Autocomplete stations
* `GET  /api/v1/trains/search?from=NDLS&to=BSB&date=2024-12-25` – Search trains serving route
* `GET  /api/v1/trains/{id}` – Train details with route stops & coaches

### Seat Inventory & Booking Engine
* `GET  /api/v1/availability?trainId=1&date=2024-12-25&coachType=3A` – Live available berths
* `POST /api/v1/availability/hold` – Temporarily lock seats for 10 minutes (Redisson lock)
* `POST /api/v1/bookings` *(Header: `Idempotency-Key`)* – Initiate reservation & allocate seats
* `GET  /api/v1/bookings/{pnr}` – Real-time PNR enquiry
* `GET  /api/v1/bookings/my` – User booking history
* `POST /api/v1/bookings/{id}/cancel` – Cancel ticket & calculate IRCTC refund

### Razorpay Payments & Refunds
* `POST /api/v1/payments/create-order` – Create Razorpay order
* `POST /api/v1/payments/verify` – Verify HMAC-SHA256 signature and confirm booking
* `POST /api/v1/payments/webhook` – Idempotent payment webhook listener
* `GET  /api/v1/refunds/booking/{bookingId}` – Track refund progress

### Ticket Generation
* `GET  /api/v1/tickets/{pnr}/pdf` – Download OpenPDF reservation slip
* `GET  /api/v1/tickets/{pnr}/qr` – Generate ticket verification QR code

### Admin Operations
* `GET  /api/v1/admin/dashboard` – System-wide booking telemetry, revenue, and active fleet
* `GET  /api/v1/admin/users` – Paginated user management
* `PUT  /api/v1/admin/users/{id}/status?enabled=true` – Toggle user status
