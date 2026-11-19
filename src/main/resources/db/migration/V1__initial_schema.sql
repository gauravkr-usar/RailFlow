-- ====================================================================
-- RailFlow Complete PostgreSQL Database Schema (V1)
-- High-Concurrency IRCTC-Inspired Railway Reservation System
-- ====================================================================

-- 1. Users and Authentication
CREATE TABLE IF NOT EXISTS roles (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS users (
    id BIGSERIAL PRIMARY KEY,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    email VARCHAR(150) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    phone_number VARCHAR(20),
    date_of_birth DATE,
    is_enabled BOOLEAN DEFAULT TRUE NOT NULL,
    is_email_verified BOOLEAN DEFAULT FALSE NOT NULL,
    version BIGINT DEFAULT 0,
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS user_roles (
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    role_id BIGINT NOT NULL REFERENCES roles(id) ON DELETE CASCADE,
    PRIMARY KEY (user_id, role_id)
);

CREATE TABLE IF NOT EXISTS refresh_tokens (
    id BIGSERIAL PRIMARY KEY,
    token VARCHAR(500) NOT NULL UNIQUE,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    expiry_date TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    is_revoked BOOLEAN DEFAULT FALSE NOT NULL
);

CREATE TABLE IF NOT EXISTS otp_records (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(150) NOT NULL,
    otp VARCHAR(10) NOT NULL,
    purpose VARCHAR(50) NOT NULL,
    expires_at TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    is_used BOOLEAN DEFAULT FALSE NOT NULL,
    created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS idempotency_records (
    id BIGSERIAL PRIMARY KEY,
    idempotency_key VARCHAR(100) NOT NULL UNIQUE,
    request_hash VARCHAR(255),
    response TEXT,
    status VARCHAR(30) NOT NULL,
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    expires_at TIMESTAMP WITHOUT TIME ZONE NOT NULL
);

-- 2. Railway Stations, Trains, Coaches, and Seats
CREATE TABLE IF NOT EXISTS stations (
    id BIGSERIAL PRIMARY KEY,
    station_code VARCHAR(10) NOT NULL UNIQUE,
    station_name VARCHAR(150) NOT NULL,
    city VARCHAR(100) NOT NULL,
    state VARCHAR(100) NOT NULL,
    latitude DOUBLE PRECISION,
    longitude DOUBLE PRECISION,
    zone_code VARCHAR(10),
    is_active BOOLEAN DEFAULT TRUE NOT NULL,
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS trains (
    id BIGSERIAL PRIMARY KEY,
    train_number VARCHAR(20) NOT NULL UNIQUE,
    train_name VARCHAR(150) NOT NULL,
    train_type VARCHAR(50) NOT NULL,
    source_station_id BIGINT REFERENCES stations(id),
    destination_station_id BIGINT REFERENCES stations(id),
    departure_time TIME WITHOUT TIME ZONE NOT NULL,
    arrivalTime TIME WITHOUT TIME ZONE,
    arrival_time TIME WITHOUT TIME ZONE,
    total_distance INT,
    is_active BOOLEAN DEFAULT TRUE NOT NULL,
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS train_running_days (
    train_id BIGINT NOT NULL REFERENCES trains(id) ON DELETE CASCADE,
    day_of_week VARCHAR(20) NOT NULL,
    PRIMARY KEY (train_id, day_of_week)
);

CREATE TABLE IF NOT EXISTS coaches (
    id BIGSERIAL PRIMARY KEY,
    train_id BIGINT NOT NULL REFERENCES trains(id) ON DELETE CASCADE,
    coach_number VARCHAR(10) NOT NULL,
    coach_type VARCHAR(20) NOT NULL,
    total_seats INT NOT NULL,
    is_active BOOLEAN DEFAULT TRUE NOT NULL,
    UNIQUE (train_id, coach_number)
);

CREATE TABLE IF NOT EXISTS seats (
    id BIGSERIAL PRIMARY KEY,
    coach_id BIGINT NOT NULL REFERENCES coaches(id) ON DELETE CASCADE,
    seat_number INT NOT NULL,
    berth_type VARCHAR(30) NOT NULL,
    is_active BOOLEAN DEFAULT TRUE NOT NULL,
    UNIQUE (coach_id, seat_number)
);

-- 3. Routes, Route Stations, and Schedules
CREATE TABLE IF NOT EXISTS train_routes (
    id BIGSERIAL PRIMARY KEY,
    train_id BIGINT NOT NULL REFERENCES trains(id) ON DELETE CASCADE UNIQUE
);

CREATE TABLE IF NOT EXISTS route_stations (
    id BIGSERIAL PRIMARY KEY,
    train_route_id BIGINT NOT NULL REFERENCES train_routes(id) ON DELETE CASCADE,
    station_id BIGINT NOT NULL REFERENCES stations(id),
    sequence_number INT NOT NULL,
    arrival_time TIME WITHOUT TIME ZONE,
    departure_time TIME WITHOUT TIME ZONE,
    distance_from_source INT DEFAULT 0,
    day_offset INT DEFAULT 0,
    platform_number VARCHAR(10),
    UNIQUE (train_route_id, sequence_number)
);

CREATE TABLE IF NOT EXISTS train_schedules (
    id BIGSERIAL PRIMARY KEY,
    train_id BIGINT NOT NULL REFERENCES trains(id),
    journey_date DATE NOT NULL,
    status VARCHAR(30) DEFAULT 'ON_TIME',
    delay_minutes INT DEFAULT 0,
    remarks VARCHAR(255),
    UNIQUE (train_id, journey_date)
);

CREATE TABLE IF NOT EXISTS train_runs (
    id BIGSERIAL PRIMARY KEY,
    train_id BIGINT NOT NULL REFERENCES trains(id),
    run_date DATE NOT NULL,
    is_active BOOLEAN DEFAULT TRUE NOT NULL,
    scheduled_departure_time TIME WITHOUT TIME ZONE,
    actual_departure_time TIME WITHOUT TIME ZONE,
    UNIQUE (train_id, run_date)
);

-- 4. Seat Inventory & Distributed Locking (Segment-based)
CREATE TABLE IF NOT EXISTS seat_inventory (
    id BIGSERIAL PRIMARY KEY,
    train_id BIGINT NOT NULL REFERENCES trains(id),
    coach_id BIGINT NOT NULL REFERENCES coaches(id),
    seat_id BIGINT NOT NULL REFERENCES seats(id),
    source_station_id BIGINT NOT NULL REFERENCES stations(id),
    destination_station_id BIGINT NOT NULL REFERENCES stations(id),
    journey_date DATE NOT NULL,
    coach_type VARCHAR(20) NOT NULL,
    status VARCHAR(20) DEFAULT 'AVAILABLE' NOT NULL,
    fare NUMERIC(10, 2) NOT NULL,
    hold_expiry TIMESTAMP WITHOUT TIME ZONE,
    booking_id BIGINT,
    version BIGINT DEFAULT 0,
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (train_id, coach_id, seat_id, journey_date, source_station_id, destination_station_id)
);

CREATE TABLE IF NOT EXISTS seat_holds (
    id BIGSERIAL PRIMARY KEY,
    hold_token VARCHAR(100) NOT NULL UNIQUE,
    user_id BIGINT,
    journey_date DATE NOT NULL,
    train_id BIGINT,
    source_station_id BIGINT,
    dest_station_id BIGINT,
    coach_type VARCHAR(20),
    expires_at TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    is_released BOOLEAN DEFAULT FALSE NOT NULL,
    created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS seat_hold_inventories (
    seat_hold_id BIGINT NOT NULL REFERENCES seat_holds(id) ON DELETE CASCADE,
    inventory_id BIGINT NOT NULL,
    PRIMARY KEY (seat_hold_id, inventory_id)
);

-- 5. Passengers, Bookings, and Booking Passengers
CREATE TABLE IF NOT EXISTS passengers (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT REFERENCES users(id) ON DELETE SET NULL,
    name VARCHAR(150) NOT NULL,
    age INT NOT NULL,
    gender VARCHAR(20) NOT NULL,
    id_proof_type VARCHAR(50),
    id_proof_number VARCHAR(100),
    is_active BOOLEAN DEFAULT TRUE NOT NULL,
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS bookings (
    id BIGSERIAL PRIMARY KEY,
    pnr VARCHAR(20) NOT NULL UNIQUE,
    user_id BIGINT NOT NULL REFERENCES users(id),
    train_id BIGINT NOT NULL REFERENCES trains(id),
    source_station_id BIGINT NOT NULL REFERENCES stations(id),
    destination_station_id BIGINT NOT NULL REFERENCES stations(id),
    journey_date DATE NOT NULL,
    coach_type VARCHAR(20) NOT NULL,
    status VARCHAR(30) NOT NULL,
    total_fare NUMERIC(10, 2) NOT NULL,
    hold_token VARCHAR(100),
    idempotency_key VARCHAR(100),
    version BIGINT DEFAULT 0,
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS booking_passengers (
    id BIGSERIAL PRIMARY KEY,
    booking_id BIGINT NOT NULL REFERENCES bookings(id) ON DELETE CASCADE,
    passenger_id BIGINT NOT NULL REFERENCES passengers(id),
    seat_inventory_id BIGINT,
    coach_number VARCHAR(10),
    seat_number INT,
    berth_type VARCHAR(30),
    status VARCHAR(30) NOT NULL,
    waiting_list_number INT,
    rac_number INT,
    fare NUMERIC(10, 2) NOT NULL
);

-- 6. Payments, Cancellations, and Refunds
CREATE TABLE IF NOT EXISTS payments (
    id BIGSERIAL PRIMARY KEY,
    booking_id BIGINT NOT NULL REFERENCES bookings(id),
    razorpay_order_id VARCHAR(100) UNIQUE,
    razorpay_payment_id VARCHAR(100),
    razorpay_signature VARCHAR(255),
    amount NUMERIC(10, 2) NOT NULL,
    currency VARCHAR(10) DEFAULT 'INR' NOT NULL,
    status VARCHAR(30) NOT NULL,
    payment_method VARCHAR(30),
    failure_reason VARCHAR(255),
    attempt_count INT DEFAULT 0,
    webhook_processed BOOLEAN DEFAULT FALSE,
    idempotency_key VARCHAR(100),
    version BIGINT DEFAULT 0,
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS refunds (
    id BIGSERIAL PRIMARY KEY,
    payment_id BIGINT NOT NULL REFERENCES payments(id),
    booking_id BIGINT NOT NULL REFERENCES bookings(id),
    refund_amount NUMERIC(10, 2) NOT NULL,
    razorpay_refund_id VARCHAR(100),
    status VARCHAR(30) NOT NULL,
    reason VARCHAR(255),
    initiated_at TIMESTAMP WITHOUT TIME ZONE,
    completed_at TIMESTAMP WITHOUT TIME ZONE,
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS cancellations (
    id BIGSERIAL PRIMARY KEY,
    booking_id BIGINT NOT NULL REFERENCES bookings(id) UNIQUE,
    cancelled_by BIGINT NOT NULL,
    cancellation_time TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    reason VARCHAR(255),
    original_fare NUMERIC(10, 2) NOT NULL,
    cancellation_charges NUMERIC(10, 2) NOT NULL,
    refund_amount NUMERIC(10, 2) NOT NULL,
    refund_id BIGINT REFERENCES refunds(id),
    cancellation_rule VARCHAR(255),
    created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- 7. Waiting List, Notifications, and Outbox Pattern
CREATE TABLE IF NOT EXISTS waiting_lists (
    id BIGSERIAL PRIMARY KEY,
    booking_id BIGINT NOT NULL REFERENCES bookings(id),
    passenger_id BIGINT NOT NULL REFERENCES passengers(id),
    train_id BIGINT NOT NULL,
    journey_date DATE NOT NULL,
    coach_type VARCHAR(20) NOT NULL,
    position INT NOT NULL,
    status VARCHAR(30) NOT NULL,
    created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS notifications (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT,
    recipient VARCHAR(150) NOT NULL,
    type VARCHAR(50) NOT NULL,
    subject VARCHAR(255),
    content TEXT NOT NULL,
    status VARCHAR(30) NOT NULL,
    retry_count INT DEFAULT 0,
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    sent_at TIMESTAMP WITHOUT TIME ZONE
);

CREATE TABLE IF NOT EXISTS outbox_events (
    id BIGSERIAL PRIMARY KEY,
    aggregate_type VARCHAR(100) NOT NULL,
    aggregate_id VARCHAR(100) NOT NULL,
    event_type VARCHAR(100) NOT NULL,
    payload TEXT NOT NULL,
    status VARCHAR(30) DEFAULT 'PENDING' NOT NULL,
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    processed_at TIMESTAMP WITHOUT TIME ZONE
);

-- Performance Indexes
CREATE INDEX IF NOT EXISTS idx_seat_inv_search ON seat_inventory(train_id, journey_date, coach_type, status);
CREATE INDEX IF NOT EXISTS idx_seat_inv_lookup ON seat_inventory(source_station_id, destination_station_id);
CREATE INDEX IF NOT EXISTS idx_route_stations ON route_stations(train_route_id, sequence_number);
CREATE INDEX IF NOT EXISTS idx_bookings_user ON bookings(user_id);
CREATE INDEX IF NOT EXISTS idx_bookings_pnr ON bookings(pnr);
CREATE INDEX IF NOT EXISTS idx_payments_order ON payments(razorpay_order_id);
CREATE INDEX IF NOT EXISTS idx_payments_booking ON payments(booking_id);
CREATE INDEX IF NOT EXISTS idx_outbox_pending ON outbox_events(status, created_at);
