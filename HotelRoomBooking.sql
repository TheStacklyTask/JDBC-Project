
CREATE DATABASE IF NOT EXISTS HotelRoomBookingDB;
USE HotelRoomBookingDB;

CREATE TABLE IF NOT EXISTS hotels (
    hotel_id INT PRIMARY KEY AUTO_INCREMENT,
    hotel_name VARCHAR(100) NOT NULL,
    city VARCHAR(50) NOT NULL
);

CREATE TABLE IF NOT EXISTS rooms (
    room_id INT PRIMARY KEY AUTO_INCREMENT,
    hotel_id INT NOT NULL,
    room_number VARCHAR(10) NOT NULL,
    room_type VARCHAR(30) NOT NULL,
    price_per_night DECIMAL(10,2) NOT NULL,
    room_status VARCHAR(20) DEFAULT 'AVAILABLE',
    FOREIGN KEY (hotel_id) REFERENCES hotels(hotel_id),
    UNIQUE (hotel_id, room_number)
);

CREATE TABLE IF NOT EXISTS bookings (
    booking_id INT PRIMARY KEY AUTO_INCREMENT,
    customer_name VARCHAR(100) NOT NULL,
    room_id INT NOT NULL,
    nights INT NOT NULL,
    total_amount DECIMAL(10,2) NOT NULL,
    booking_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (room_id) REFERENCES rooms(room_id),
    UNIQUE (room_id)
);

INSERT INTO hotels (hotel_name, city)
SELECT 'Green Park Hotel', 'Chennai'
WHERE NOT EXISTS (
    SELECT 1 FROM hotels WHERE hotel_name = 'Green Park Hotel'
);

INSERT INTO hotels (hotel_name, city)
SELECT 'Sunrise Residency', 'Chennai'
WHERE NOT EXISTS (
    SELECT 1 FROM hotels WHERE hotel_name = 'Sunrise Residency'
);

INSERT INTO rooms
(hotel_id, room_number, room_type, price_per_night)
SELECT hotel_id, '101', 'Standard', 1500
FROM hotels
WHERE hotel_name = 'Green Park Hotel'
AND NOT EXISTS (
    SELECT 1 FROM rooms
    WHERE hotel_id = hotels.hotel_id AND room_number = '101'
);

INSERT INTO rooms
(hotel_id, room_number, room_type, price_per_night)
SELECT hotel_id, '102', 'Deluxe', 2500
FROM hotels
WHERE hotel_name = 'Green Park Hotel'
AND NOT EXISTS (
    SELECT 1 FROM rooms
    WHERE hotel_id = hotels.hotel_id AND room_number = '102'
);

SELECT * FROM hotels;
SELECT * FROM rooms;
SELECT * FROM bookings;
