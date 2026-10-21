
CREATE DATABASE IF NOT EXISTS MovieTicketDB;
USE MovieTicketDB;

CREATE TABLE IF NOT EXISTS movies (
    movie_id INT PRIMARY KEY AUTO_INCREMENT,
    movie_name VARCHAR(100) NOT NULL,
    language VARCHAR(30),
    duration INT
);

CREATE TABLE IF NOT EXISTS theaters (
    theater_id INT PRIMARY KEY AUTO_INCREMENT,
    theater_name VARCHAR(100) NOT NULL,
    city VARCHAR(50)
);

CREATE TABLE IF NOT EXISTS shows (
    show_id INT PRIMARY KEY AUTO_INCREMENT,
    movie_id INT NOT NULL,
    theater_id INT NOT NULL,
    show_time DATETIME NOT NULL,
    ticket_price DECIMAL(8,2) NOT NULL,
    total_seats INT NOT NULL,
    FOREIGN KEY (movie_id) REFERENCES movies(movie_id),
    FOREIGN KEY (theater_id) REFERENCES theaters(theater_id)
);

CREATE TABLE IF NOT EXISTS bookings (
    booking_id INT PRIMARY KEY AUTO_INCREMENT,
    customer_name VARCHAR(100) NOT NULL,
    show_id INT NOT NULL,
    seat_number VARCHAR(10) NOT NULL,
    amount DECIMAL(8,2) NOT NULL,
    booking_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (show_id) REFERENCES shows(show_id),
    UNIQUE (show_id, seat_number)
);

INSERT INTO movies (movie_name, language, duration)
SELECT 'Leo', 'Tamil', 164
WHERE NOT EXISTS (
    SELECT 1 FROM movies WHERE movie_name = 'Leo'
);

INSERT INTO movies (movie_name, language, duration)
SELECT 'Jailer', 'Tamil', 168
WHERE NOT EXISTS (
    SELECT 1 FROM movies WHERE movie_name = 'Jailer'
);

INSERT INTO theaters (theater_name, city)
SELECT 'PVR Cinemas', 'Chennai'
WHERE NOT EXISTS (
    SELECT 1 FROM theaters WHERE theater_name = 'PVR Cinemas'
);

INSERT INTO theaters (theater_name, city)
SELECT 'INOX Cinemas', 'Chennai'
WHERE NOT EXISTS (
    SELECT 1 FROM theaters WHERE theater_name = 'INOX Cinemas'
);

INSERT INTO shows
(movie_id, theater_id, show_time, ticket_price, total_seats)
SELECT m.movie_id, t.theater_id,
       '2026-12-25 10:00:00', 150.00, 20
FROM movies m
JOIN theaters t ON t.theater_name = 'PVR Cinemas'
WHERE m.movie_name = 'Leo'
AND NOT EXISTS (
    SELECT 1 FROM shows s
    WHERE s.movie_id = m.movie_id
      AND s.theater_id = t.theater_id
      AND s.show_time = '2026-12-25 10:00:00'
);

INSERT INTO shows
(movie_id, theater_id, show_time, ticket_price, total_seats)
SELECT m.movie_id, t.theater_id,
       '2026-12-25 14:00:00', 180.00, 20
FROM movies m
JOIN theaters t ON t.theater_name = 'INOX Cinemas'
WHERE m.movie_name = 'Jailer'
AND NOT EXISTS (
    SELECT 1 FROM shows s
    WHERE s.movie_id = m.movie_id
      AND s.theater_id = t.theater_id
      AND s.show_time = '2026-12-25 14:00:00'
);

SELECT * FROM movies;
SELECT * FROM theaters;
SELECT * FROM shows;
SELECT * FROM bookings;

USE MovieTicketDB;

INSERT INTO movies (movie_name, language, duration)
VALUES ('Amaran', 'Tamil', 169);
INSERT INTO movies (movie_name, language, duration)
VALUES ('Dragon', 'Tamil', 157);

INSERT INTO movies (movie_name, language, duration)
VALUES ('Jailer 2', 'Tamil', 160);

USE MovieTicketDB;

INSERT INTO theaters (theater_name, city)
VALUES ('AGS Cinemas', 'Chennai');

USE MovieTicketDB;

INSERT INTO theaters (theater_name, city)
VALUES ('Ega Cinemas', 'Chennai');

INSERT INTO theaters (theater_name, city)
VALUES ('Rohini Cinemas', 'Chennai');