-- ====================================================================
-- RailFlow Seed Initial Data (V2)
-- Realistic Indian Railways Stations, Trains, Routes, Coaches & Inventory
-- ====================================================================

-- 1. Roles
INSERT INTO roles (id, name) VALUES
(1, 'USER'),
(2, 'ADMIN'),
(3, 'SUPPORT')
ON CONFLICT (id) DO NOTHING;

-- 2. Initial Users (Passwords: Admin@1234 and User@1234)
INSERT INTO users (id, first_name, last_name, email, password_hash, phone_number, is_enabled, is_email_verified, created_at, updated_at) VALUES
(1, 'System', 'Admin', 'admin@railflow.com', '$2a$12$.MUILDIE5MKPG5KiHXj2xO.tBtoQPZjvgKWiJc7vAdPP7qH4bFMOm', '+919876543210', true, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(2, 'Gaurav', 'Kumar', 'user@railflow.com', '$2a$12$emrbJalD6ZquDDlIz9IUwe7okqck25AmdrTYFfX7yDFEfeG5E0Txa', '+919876543211', true, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (id) DO NOTHING;

INSERT INTO user_roles (user_id, role_id) VALUES
(1, 1),
(1, 2),
(2, 1)
ON CONFLICT (user_id, role_id) DO NOTHING;

-- 3. Major Railway Stations
INSERT INTO stations (id, station_code, station_name, city, state, latitude, longitude, zone_code, is_active, created_at, updated_at) VALUES
(1, 'NDLS', 'New Delhi', 'New Delhi', 'Delhi', 28.6429, 77.2195, 'NR', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(2, 'CNB', 'Kanpur Central', 'Kanpur', 'Uttar Pradesh', 26.4536, 80.3512, 'NCR', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(3, 'PRYJ', 'Prayagraj Junction', 'Prayagraj', 'Uttar Pradesh', 25.4484, 81.8340, 'NCR', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(4, 'BSB', 'Varanasi Junction', 'Varanasi', 'Uttar Pradesh', 25.3283, 82.9866, 'NR', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(5, 'HWH', 'Howrah Junction', 'Kolkata', 'West Bengal', 22.5839, 88.3426, 'ER', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(6, 'MMCT', 'Mumbai Central', 'Mumbai', 'Maharashtra', 18.9696, 72.8193, 'WR', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(7, 'BRC', 'Vadodara Junction', 'Vadodara', 'Gujarat', 22.3106, 73.1812, 'WR', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(8, 'SBC', 'KSR Bengaluru City', 'Bengaluru', 'Karnataka', 12.9781, 77.5694, 'SWR', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(9, 'MAS', 'MGR Chennai Central', 'Chennai', 'Tamil Nadu', 13.0827, 80.2755, 'SR', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(10, 'PNBE', 'Patna Junction', 'Patna', 'Bihar', 25.6022, 85.1376, 'ECR', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (id) DO NOTHING;

-- 4. Iconic Trains
INSERT INTO trains (id, train_number, train_name, train_type, source_station_id, destination_station_id, departure_time, arrival_time, total_distance, is_active, created_at, updated_at) VALUES
(1, '12302', 'Howrah Rajdhani Express', 'RAJDHANI', 1, 5, '16:55:00', '09:55:00', 1450, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(2, '22436', 'Vande Bharat Express', 'VANDE_BHARAT', 1, 4, '06:00:00', '14:00:00', 760, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(3, '12952', 'Mumbai Tejas Rajdhani', 'RAJDHANI', 1, 6, '16:55:00', '08:35:00', 1386, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(4, '12560', 'Shiv Ganga Express', 'SUPERFAST', 1, 4, '20:05:00', '06:10:00', 760, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (id) DO NOTHING;

-- Running days for all 4 trains
INSERT INTO train_running_days (train_id, day_of_week) VALUES
(1, 'MONDAY'), (1, 'TUESDAY'), (1, 'WEDNESDAY'), (1, 'THURSDAY'), (1, 'FRIDAY'), (1, 'SATURDAY'), (1, 'SUNDAY'),
(2, 'TUESDAY'), (2, 'WEDNESDAY'), (2, 'FRIDAY'), (2, 'SATURDAY'), (2, 'SUNDAY'),
(3, 'MONDAY'), (3, 'TUESDAY'), (3, 'WEDNESDAY'), (3, 'THURSDAY'), (3, 'FRIDAY'), (3, 'SATURDAY'), (3, 'SUNDAY'),
(4, 'MONDAY'), (4, 'TUESDAY'), (4, 'WEDNESDAY'), (4, 'THURSDAY'), (4, 'FRIDAY'), (4, 'SATURDAY'), (4, 'SUNDAY')
ON CONFLICT (train_id, day_of_week) DO NOTHING;

-- 5. Routes & Route Stations
INSERT INTO train_routes (id, train_id) VALUES
(1, 1),
(2, 2),
(3, 3),
(4, 4)
ON CONFLICT (id) DO NOTHING;

-- Route 1: 12302 Howrah Rajdhani (NDLS -> CNB -> PRYJ -> PNBE -> HWH)
INSERT INTO route_stations (train_route_id, station_id, sequence_number, arrival_time, departure_time, distance_from_source, day_offset, platform_number) VALUES
(1, 1, 1, NULL, '16:55:00', 0, 0, '16'),
(1, 2, 2, '21:30:00', '21:35:00', 440, 0, '1'),
(1, 3, 3, '23:43:00', '23:45:00', 635, 0, '4'),
(1, 10, 4, '04:00:00', '04:10:00', 1000, 1, '1'),
(1, 5, 5, '09:55:00', NULL, 1450, 1, '8')
ON CONFLICT (train_route_id, sequence_number) DO NOTHING;

-- Route 2: 22436 Vande Bharat (NDLS -> CNB -> PRYJ -> BSB)
INSERT INTO route_stations (train_route_id, station_id, sequence_number, arrival_time, departure_time, distance_from_source, day_offset, platform_number) VALUES
(2, 1, 1, NULL, '06:00:00', 0, 0, '16'),
(2, 2, 2, '10:08:00', '10:10:00', 440, 0, '1'),
(2, 3, 3, '12:08:00', '12:10:00', 635, 0, '6'),
(2, 4, 4, '14:00:00', NULL, 760, 0, '1')
ON CONFLICT (train_route_id, sequence_number) DO NOTHING;

-- Route 3: 12952 Mumbai Tejas Rajdhani (NDLS -> BRC -> MMCT)
INSERT INTO route_stations (train_route_id, station_id, sequence_number, arrival_time, departure_time, distance_from_source, day_offset, platform_number) VALUES
(3, 1, 1, NULL, '16:55:00', 0, 0, '3'),
(3, 7, 2, '03:20:00', '03:28:00', 992, 1, '2'),
(3, 6, 3, '08:35:00', NULL, 1386, 1, '1')
ON CONFLICT (train_route_id, sequence_number) DO NOTHING;

-- Route 4: 12560 Shiv Ganga Express (NDLS -> CNB -> PRYJ -> BSB)
INSERT INTO route_stations (train_route_id, station_id, sequence_number, arrival_time, departure_time, distance_from_source, day_offset, platform_number) VALUES
(4, 1, 1, NULL, '20:05:00', 0, 0, '12'),
(4, 2, 2, '01:30:00', '01:35:00', 440, 1, '5'),
(4, 3, 3, '03:45:00', '03:55:00', 635, 1, '5'),
(4, 4, 4, '06:10:00', NULL, 760, 1, '8')
ON CONFLICT (train_route_id, sequence_number) DO NOTHING;

-- 6. Coaches (Train 1 & Train 2 & Train 4)
INSERT INTO coaches (id, train_id, coach_number, coach_type, total_seats, is_active) VALUES
(1, 1, 'A1', 'TWO_A', 20, true),
(2, 1, 'B1', 'THREE_A', 24, true),
(3, 1, 'B2', 'THREE_A', 24, true),
(4, 1, 'H1', 'ONE_A', 10, true),
(5, 2, 'C1', 'CC', 24, true),
(6, 2, 'C2', 'CC', 24, true),
(7, 2, 'E1', 'EC', 16, true),
(8, 4, 'S1', 'SL', 24, true),
(9, 4, 'S2', 'SL', 24, true),
(10, 4, 'B1', 'THREE_A', 24, true),
(11, 4, 'A1', 'TWO_A', 20, true)
ON CONFLICT (id) DO NOTHING;

-- 7. Seats in Coaches
-- Coach 1 (TWO_A, 20 seats)
INSERT INTO seats (coach_id, seat_number, berth_type, is_active)
SELECT 1, gs,
       CASE ((gs - 1) % 4)
           WHEN 0 THEN 'LOWER'
           WHEN 1 THEN 'UPPER'
           WHEN 2 THEN 'SIDE_LOWER'
           ELSE 'SIDE_UPPER'
       END, true
FROM generate_series(1,20) AS gs
ON CONFLICT (coach_id, seat_number) DO NOTHING;

-- Coach 2 (THREE_A, 24 seats)
INSERT INTO seats (coach_id, seat_number, berth_type, is_active)
SELECT 2, gs,
       CASE ((gs - 1) % 6)
           WHEN 0 THEN 'LOWER'
           WHEN 1 THEN 'MIDDLE'
           WHEN 2 THEN 'UPPER'
           WHEN 3 THEN 'SIDE_LOWER'
           WHEN 4 THEN 'SIDE_UPPER'
           ELSE 'LOWER'
       END, true
FROM generate_series(1,24) AS gs
ON CONFLICT (coach_id, seat_number) DO NOTHING;

-- Coach 5 (Vande Bharat CC, 24 seats)
INSERT INTO seats (coach_id, seat_number, berth_type, is_active)
SELECT 5, gs,
       CASE ((gs - 1) % 2)
           WHEN 0 THEN 'WINDOW'
           ELSE 'AISLE'
       END, true
FROM generate_series(1,24) AS gs
ON CONFLICT (coach_id, seat_number) DO NOTHING;

-- Coach 8 (Shiv Ganga Sleeper S1, 24 seats)
INSERT INTO seats (coach_id, seat_number, berth_type, is_active)
SELECT 8, gs,
       CASE ((gs - 1) % 6)
           WHEN 0 THEN 'LOWER'
           WHEN 1 THEN 'MIDDLE'
           WHEN 2 THEN 'UPPER'
           WHEN 3 THEN 'SIDE_LOWER'
           WHEN 4 THEN 'SIDE_UPPER'
           ELSE 'LOWER'
       END, true
FROM generate_series(1,24) AS gs
ON CONFLICT (coach_id, seat_number) DO NOTHING;

-- Reset Sequences to prevent primary key collision
SELECT setval('roles_id_seq', (SELECT COALESCE(MAX(id), 1) FROM roles));
SELECT setval('users_id_seq', (SELECT COALESCE(MAX(id), 1) FROM users));
SELECT setval('stations_id_seq', (SELECT COALESCE(MAX(id), 1) FROM stations));
SELECT setval('trains_id_seq', (SELECT COALESCE(MAX(id), 1) FROM trains));
SELECT setval('coaches_id_seq', (SELECT COALESCE(MAX(id), 1) FROM coaches));
SELECT setval('seats_id_seq', (SELECT COALESCE(MAX(id), 1) FROM seats));
SELECT setval('train_routes_id_seq', (SELECT COALESCE(MAX(id), 1) FROM train_routes));
