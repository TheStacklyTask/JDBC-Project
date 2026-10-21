package org.JDBC;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Scanner;

public class MovieTicketApp {

    static Scanner sc = new Scanner(System.in);

    public static void main(String[] args) {

        try (Connection con =
                DBConnection.getConnection("MovieTicketDB")) {

            System.out.println("Database connected successfully!");

            int choice;

            do {
                System.out.println("\n===== MOVIE TICKET BOOKING SYSTEM =====");
                System.out.println("1. View Movies");
                System.out.println("2. View Theaters");
                System.out.println("3. View Shows and Available Seats");
                System.out.println("4. Book Ticket");
                System.out.println("5. View Booking Details");
                System.out.println("6. Add Theater");
                System.out.println("7. Add Movie");
                System.out.println("8. Exit");
                System.out.print("Enter your choice: ");

                choice = sc.nextInt();
                sc.nextLine();

                switch (choice) {
                    case 1:
                        viewMovies(con);
                        break;

                    case 2:
                        viewTheaters(con);
                        break;

                    case 3:
                        viewShows(con);
                        break;

                    case 4:
                        bookTicket(con);
                        break;

                    case 5:
                        viewBookings(con);
                        break;
                    case 6:
                        addTheater(con);
                        break;
                        
                    case 7:
                        addMovie(con);
                        break;

                    case 8:
                        System.out.println("Thank you!");
                        break;


                    default:
                        System.out.println("Invalid choice!");
                }

            } while (choice != 6);

        } catch (SQLException e) {
            System.out.println("Database error: " + e.getMessage());
        }
    }

    // 1. View Movies
    static void viewMovies(Connection con) throws SQLException {

        String sql = "SELECT * FROM movies";

        try (PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            System.out.println("\n--- MOVIE LIST ---");

            while (rs.next()) {
                System.out.println(
                    "Movie ID: " + rs.getInt("movie_id")
                    + " | Name: " + rs.getString("movie_name")
                    + " | Language: " + rs.getString("language")
                    + " | Duration: " + rs.getInt("duration") + " mins"
                );
            }
        }
    }

    // 2. View Theaters
    static void viewTheaters(Connection con) throws SQLException {

        String sql = "SELECT * FROM theaters";

        try (PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            System.out.println("\n--- THEATER LIST ---");

            while (rs.next()) {
                System.out.println(
                    "Theater ID: " + rs.getInt("theater_id")
                    + " | Name: " + rs.getString("theater_name")
                    + " | City: " + rs.getString("city")
                );
            }
        }
    }
    
    // Add theater option

static void addTheater(Connection con) throws SQLException {

    System.out.print("Enter Theater Name: ");
    String name = sc.nextLine().trim();

    System.out.print("Enter City: ");
    String city = sc.nextLine().trim();

    if (name.isEmpty() || city.isEmpty()) {
        System.out.println("Theater name and city are required!");
        return;
    }

    String sql =
        "INSERT INTO theaters (theater_name, city) VALUES (?, ?)";

    try (PreparedStatement ps = con.prepareStatement(sql)) {

        ps.setString(1, name);
        ps.setString(2, city);

        int rows = ps.executeUpdate();

        if (rows > 0) {
            System.out.println("Theater added successfully!");
        }
    }
}


	// movie added option


static void addMovie(Connection con) throws SQLException {

    System.out.println("\n===== ADD NEW MOVIE =====");

    System.out.print("Enter Movie Name: ");
    String movieName = sc.nextLine().trim();

    System.out.print("Enter Language: ");
    String language = sc.nextLine().trim();

    System.out.print("Enter Duration (minutes): ");
    int duration = sc.nextInt();
    sc.nextLine();

    if (movieName.isEmpty() || language.isEmpty()
            || duration <= 0) {
        System.out.println("Please enter valid movie details!");
        return;
    }

    String sql =
        "INSERT INTO movies (movie_name, language, duration) "
        + "VALUES (?, ?, ?)";

    try (PreparedStatement ps = con.prepareStatement(sql)) {

        ps.setString(1, movieName);
        ps.setString(2, language);
        ps.setInt(3, duration);

        int rows = ps.executeUpdate();

        if (rows > 0) {
            System.out.println("Movie added successfully!");
        }
    }
}


    // 3. View Shows and Available Seats

static void viewShows(Connection con) throws SQLException {

    String sql =
        "SELECT s.show_id, m.movie_name, t.theater_name, "
        + "s.show_time, s.ticket_price, s.total_seats "
        + "FROM shows s "
        + "JOIN movies m ON s.movie_id = m.movie_id "
        + "JOIN theaters t ON s.theater_id = t.theater_id";

    try (PreparedStatement ps = con.prepareStatement(sql);
         ResultSet rs = ps.executeQuery()) {

        System.out.println("\n========== AVAILABLE SHOWS ==========");

        while (rs.next()) {

            int showId = rs.getInt("show_id");
            int totalSeats = rs.getInt("total_seats");

            System.out.println("\nShow ID: " + showId);
            System.out.println("Movie: " + rs.getString("movie_name"));
            System.out.println("Theater: " + rs.getString("theater_name"));
            System.out.println("Show Time: " + rs.getTimestamp("show_time"));
            System.out.println("Ticket Price: Rs." + rs.getDouble("ticket_price"));

            showSeatLayout(con, showId, totalSeats);
        }
    }
}


static void showSeatLayout(
        Connection con, int showId, int totalSeats)
        throws SQLException {

    String GREEN = "\u001B[32m";
    String RED = "\u001B[31m";
    String RESET = "\u001B[0m";

    java.util.Set<String> bookedSeats =
            new java.util.HashSet<>();

    String sql =
        "SELECT seat_number FROM bookings WHERE show_id = ?";

    try (PreparedStatement ps = con.prepareStatement(sql)) {

        ps.setInt(1, showId);

        try (ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                bookedSeats.add(rs.getString("seat_number"));
            }
        }
    }

    System.out.println("\n================ SCREEN =================");
    System.out.println("==========================================");

    for (int i = 1; i <= totalSeats; i++) {

        String seat = "S" + i;

        if (!bookedSeats.contains(seat)) {

            System.out.print(
                GREEN + "[ " + seat + " ]" + RESET + " "
            );

        } else {

            System.out.print(
                RED + "[ X  ]" + RESET + " "
            );
        }

        // Five seats in each row
        if (i % 5 == 0) {
            System.out.println();
            System.out.println();
        }
    }

    if (totalSeats % 5 != 0) {
        System.out.println();
    }

    System.out.println("==========================================");
    System.out.println(GREEN + "Green = Available" + RESET);
    System.out.println(RED + "Red X = Booked" + RESET);
}


// 4. Book Ticket

static void bookTicket(Connection con) throws SQLException {

    System.out.println("\n===== BOOK MOVIE TICKETS =====");

    System.out.print("Enter Show ID: ");
    int showId = sc.nextInt();
    sc.nextLine();

    System.out.print("Enter Customer Name: ");
    String customerName = sc.nextLine().trim();

    if (customerName.isEmpty()) {
        System.out.println("Customer name cannot be empty!");
        return;
    }

    double price;
    int totalSeats;

    String showSql =
        "SELECT ticket_price, total_seats "
        + "FROM shows WHERE show_id = ?";

    try (PreparedStatement ps = con.prepareStatement(showSql)) {
        ps.setInt(1, showId);

        try (ResultSet rs = ps.executeQuery()) {
            if (!rs.next()) {
                System.out.println("Invalid Show ID!");
                return;
            }

            price = rs.getDouble("ticket_price");
            totalSeats = rs.getInt("total_seats");
        }
    }

    showSeatLayout(con, showId, totalSeats);

    System.out.print("\nHow many tickets do you want to book? ");
    int ticketCount = sc.nextInt();
    sc.nextLine();

    if (ticketCount <= 0 || ticketCount > totalSeats) {
        System.out.println("Invalid ticket count!");
        return;
    }

    java.util.List<String> selectedSeats =
        new java.util.ArrayList<>();

    java.util.Set<String> uniqueSeats =
        new java.util.HashSet<>();

    // Select all required seats
    for (int i = 1; i <= ticketCount; i++) {

        System.out.print("Choose seat " + i + ": ");
        String seat = sc.nextLine().trim().toUpperCase();

        if (!seat.matches("S([1-9]|1[0-9]|20)")) {
            System.out.println("Invalid seat number!");
            return;
        }

        int seatNo = Integer.parseInt(seat.substring(1));

        if (seatNo > totalSeats) {
            System.out.println("Seat does not exist!");
            return;
        }

        if (!uniqueSeats.add(seat)) {
            System.out.println("You selected this seat already!");
            return;
        }

        selectedSeats.add(seat);
    }

    boolean oldAutoCommit = con.getAutoCommit();

    try {
        con.setAutoCommit(false);

        // Lock the show row during booking
        String lockSql =
            "SELECT show_id FROM shows "
            + "WHERE show_id = ? FOR UPDATE";

        try (PreparedStatement ps = con.prepareStatement(lockSql)) {
            ps.setInt(1, showId);

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new SQLException("Show not found!");
                }
            }
        }

        // Check all selected seats before inserting
        String checkSql =
            "SELECT seat_number FROM bookings "
            + "WHERE show_id = ? AND seat_number = ?";

        try (PreparedStatement ps = con.prepareStatement(checkSql)) {

            for (String seat : selectedSeats) {
                ps.setInt(1, showId);
                ps.setString(2, seat);

                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        throw new SQLException(
                            "Seat " + seat + " is already booked!"
                        );
                    }
                }
            }
        }

        // Insert all seats as one transaction
        String insertSql =
            "INSERT INTO bookings "
            + "(customer_name, show_id, seat_number, amount) "
            + "VALUES (?, ?, ?, ?)";

        java.util.List<Integer> bookingIds =
            new java.util.ArrayList<>();

        try (PreparedStatement ps = con.prepareStatement(
                insertSql,
                java.sql.Statement.RETURN_GENERATED_KEYS)) {

            for (String seat : selectedSeats) {
                ps.setString(1, customerName);
                ps.setInt(2, showId);
                ps.setString(3, seat);
                ps.setDouble(4, price);

                ps.executeUpdate();

                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) {
                        bookingIds.add(keys.getInt(1));
                    }
                }
            }
        }

        con.commit();

        System.out.println("\n===== BOOKING CONFIRMED =====");
        System.out.println("Customer Name: " + customerName);
        System.out.println("Number of Tickets: " + ticketCount);
        System.out.println("Selected Seats: "
                + String.join(", ", selectedSeats));
        System.out.printf("Price Per Ticket: Rs.%.2f%n", price);
        System.out.printf(
            "Total Amount: Rs.%.2f%n", price * ticketCount
        );
        System.out.println("Booking IDs: " + bookingIds);
        System.out.println("Booking Successful!");

    } catch (SQLException e) {

        con.rollback();
        System.out.println("Booking failed: " + e.getMessage());

    } finally {
        con.setAutoCommit(oldAutoCommit);
    }
}

    
    // 5. View Booking Details
    static void viewBookings(Connection con) throws SQLException {

        String sql =
            "SELECT b.booking_id, b.customer_name, "
            + "m.movie_name, t.theater_name, "
            + "s.show_time, b.seat_number, b.amount "
            + "FROM bookings b "
            + "JOIN shows s ON b.show_id = s.show_id "
            + "JOIN movies m ON s.movie_id = m.movie_id "
            + "JOIN theaters t ON s.theater_id = t.theater_id "
            + "ORDER BY b.booking_id";

        try (PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            System.out.println("\n--- BOOKING HISTORY ---");

            boolean found = false;

            while (rs.next()) {
                found = true;

                System.out.println(
                    "\nBooking ID: " + rs.getInt("booking_id")
                    + "\nCustomer: " + rs.getString("customer_name")
                    + "\nMovie: " + rs.getString("movie_name")
                    + "\nTheater: " + rs.getString("theater_name")
                    + "\nShow Time: " + rs.getTimestamp("show_time")
                    + "\nSeat: " + rs.getString("seat_number")
                    + "\nAmount: Rs." + rs.getDouble("amount")
                );
            }

            if (!found) {
                System.out.println("No bookings available.");
            }
        }
    }
}

