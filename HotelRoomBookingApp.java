
package org.JDBC;

import java.sql.*;
import java.util.*;

public class HotelRoomBookingApp {

    static Scanner sc = new Scanner(System.in);

    public static void main(String[] args) {

        try (Connection con =
                DBConnection.getConnection("HotelRoomBookingDB")) {

            System.out.println("Database connected successfully!");

            int choice;

            do {
                System.out.println("\n===== HOTEL ROOM BOOKING SYSTEM =====");
                System.out.println("1. View Hotels");
                System.out.println("2. View All Rooms");
                System.out.println("3. Add Hotel");
                System.out.println("4. Add Room");
                System.out.println("5. Book Rooms");
                System.out.println("6. View Booking Details");
                System.out.println("7. Cancel Booking");
                System.out.println("8. Exit");
                System.out.print("Enter your choice: ");

                choice = sc.nextInt();
                sc.nextLine();

                switch (choice) {
                    case 1:
                        viewHotels(con);
                        break;
                    case 2:
                        viewRooms(con);
                        break;
                    case 3:
                        addHotel(con);
                        break;
                    case 4:
                        addRoom(con);
                        break;
                    case 5:
                        bookRooms(con);
                        break;
                    case 6:
                        viewBookings(con);
                        break;
                    case 7:
                        cancelBooking(con);
                        break;
                    case 8:
                        System.out.println("Thank you!");
                        break;
                    default:
                        System.out.println("Invalid choice!");
                }

            } while (choice != 8);

        } catch (SQLException e) {
            System.out.println("Database error: " + e.getMessage());
        }
    }

    // 1. View Hotels
    static void viewHotels(Connection con) throws SQLException {

        String sql = "SELECT * FROM hotels";

        try (PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            System.out.println("\n===== HOTEL LIST =====");

            while (rs.next()) {
                System.out.println(
                    "Hotel ID: " + rs.getInt("hotel_id")
                    + " | Name: " + rs.getString("hotel_name")
                    + " | City: " + rs.getString("city")
                );
            }
        }
    }

    // 2. View Rooms
    static void viewRooms(Connection con) throws SQLException {

        String sql =
            "SELECT r.room_id, h.hotel_name, h.city, "
            + "r.room_number, r.room_type, "
            + "r.price_per_night, r.room_status "
            + "FROM rooms r JOIN hotels h "
            + "ON r.hotel_id = h.hotel_id "
            + "ORDER BY h.hotel_id, r.room_number";

        try (PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            System.out.println("\n===== HOTEL ROOM DETAILS =====");

            while (rs.next()) {
                String status = rs.getString("room_status");

                String GREEN = "\u001B[32m";
                String RED = "\u001B[31m";
                String RESET = "\u001B[0m";

                String color = status.equals("AVAILABLE")
                        ? GREEN : RED;

                System.out.println(
                    "Room ID: " + rs.getInt("room_id")
                    + " | Hotel: " + rs.getString("hotel_name")
                    + " | City: " + rs.getString("city")
                    + " | Room: " + rs.getString("room_number")
                    + " | Type: " + rs.getString("room_type")
                    + " | Price/Night: Rs."
                    + rs.getDouble("price_per_night")
                    + " | Status: " + color + status + RESET
                );
            }
        }
    }

    // 3. Add Hotel
    static void addHotel(Connection con) throws SQLException {

        System.out.println("\n===== ADD HOTEL =====");

        System.out.print("Enter Hotel Name: ");
        String name = sc.nextLine().trim();

        System.out.print("Enter City: ");
        String city = sc.nextLine().trim();

        if (name.isEmpty() || city.isEmpty()) {
            System.out.println("Hotel name and city are required!");
            return;
        }

        String sql =
            "INSERT INTO hotels (hotel_name, city) VALUES (?, ?)";

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, name);
            ps.setString(2, city);
            ps.executeUpdate();

            System.out.println("Hotel added successfully!");
        }
    }

    // 4. Add Room
    static void addRoom(Connection con) throws SQLException {

        viewHotels(con);

        System.out.print("\nEnter Hotel ID: ");
        int hotelId = sc.nextInt();
        sc.nextLine();

        System.out.print("How many rooms do you want to add? ");
        int count = sc.nextInt();
        sc.nextLine();

        if (count <= 0) {
            System.out.println("Invalid room count!");
            return;
        }

        for (int i = 1; i <= count; i++) {

            System.out.println("\nEnter details for room " + i);

            System.out.print("Room Number: ");
            String roomNumber = sc.nextLine().trim();

            System.out.print("Room Type (Standard/Deluxe/Suite): ");
            String roomType = sc.nextLine().trim();

            System.out.print("Price Per Night: ");
            double price = sc.nextDouble();
            sc.nextLine();

            if (roomNumber.isEmpty()
                    || roomType.isEmpty() || price <= 0) {
                System.out.println("Invalid room details. Skipped.");
                continue;
            }

            String sql =
                "INSERT INTO rooms "
                + "(hotel_id, room_number, room_type, price_per_night) "
                + "VALUES (?, ?, ?, ?)";

            try (PreparedStatement ps = con.prepareStatement(sql)) {

                ps.setInt(1, hotelId);
                ps.setString(2, roomNumber);
                ps.setString(3, roomType);
                ps.setDouble(4, price);

                ps.executeUpdate();
                System.out.println("Room " + roomNumber + " added!");

            } catch (SQLException e) {
                if (e.getErrorCode() == 1062) {
                    System.out.println(
                        "This room number already exists in this hotel."
                    );
                } else if (e.getErrorCode() == 1452) {
                    System.out.println("Invalid Hotel ID!");
                } else {
                    throw e;
                }
            }
        }
    }

    // 5. Book Multiple Rooms
    static void bookRooms(Connection con) throws SQLException {

        viewHotels(con);

        System.out.print("\nEnter Hotel ID: ");
        int hotelId = sc.nextInt();
        sc.nextLine();

        System.out.print("Enter Customer Name: ");
        String customerName = sc.nextLine().trim();

        if (customerName.isEmpty()) {
            System.out.println("Customer name is required!");
            return;
        }

        System.out.print("How many rooms do you want to book? ");
        int roomCount = sc.nextInt();
        sc.nextLine();

        if (roomCount <= 0) {
            System.out.println("Invalid room count!");
            return;
        }

        // Show rooms belonging to the selected hotel
        String roomSql =
            "SELECT room_number, room_type, price_per_night, room_status "
            + "FROM rooms WHERE hotel_id = ? ORDER BY room_number";

        try (PreparedStatement ps = con.prepareStatement(roomSql)) {

            ps.setInt(1, hotelId);

            try (ResultSet rs = ps.executeQuery()) {

                System.out.println("\n===== ROOMS IN SELECTED HOTEL =====");

                while (rs.next()) {
                    String status = rs.getString("room_status");

                    System.out.println(
                        "Room: " + rs.getString("room_number")
                        + " | Type: " + rs.getString("room_type")
                        + " | Price/Night: Rs."
                        + rs.getDouble("price_per_night")
                        + " | Status: " + status
                    );
                }
            }
        }

        System.out.print("\nHow many nights? ");
        int nights = sc.nextInt();
        sc.nextLine();

        if (nights <= 0) {
            System.out.println("Invalid number of nights!");
            return;
        }

        List<String> selectedRooms = new ArrayList<>();
        Set<String> uniqueRooms = new HashSet<>();

        for (int i = 1; i <= roomCount; i++) {

            System.out.print("Choose room number " + i + ": ");
            String roomNumber = sc.nextLine().trim();

            if (roomNumber.isEmpty()
                    || !uniqueRooms.add(roomNumber)) {
                System.out.println("Invalid or duplicate room number!");
                return;
            }

            selectedRooms.add(roomNumber);
        }

        boolean oldAutoCommit = con.getAutoCommit();

        try {
            con.setAutoCommit(false);

            double totalAmount = 0;

            // Lock selected rooms and validate availability
            String checkSql =
                "SELECT room_id, price_per_night, room_status "
                + "FROM rooms WHERE hotel_id = ? "
                + "AND room_number = ? FOR UPDATE";

            try (PreparedStatement ps =
                    con.prepareStatement(checkSql)) {

                for (String roomNumber : selectedRooms) {

                    ps.setInt(1, hotelId);
                    ps.setString(2, roomNumber);

                    try (ResultSet rs = ps.executeQuery()) {

                        if (!rs.next()) {
                            throw new SQLException(
                                "Room " + roomNumber + " not found!"
                            );
                        }

                        if (!rs.getString("room_status")
                                .equals("AVAILABLE")) {
                            throw new SQLException(
                                "Room " + roomNumber + " is already booked!"
                            );
                        }

                        totalAmount +=
                            rs.getDouble("price_per_night") * nights;
                    }
                }
            }

            // Book all selected rooms
            for (String roomNumber : selectedRooms) {

                String getRoomId =
                    "SELECT room_id, price_per_night FROM rooms "
                    + "WHERE hotel_id = ? AND room_number = ?";

                int roomId;
                double price;

                try (PreparedStatement ps =
                        con.prepareStatement(getRoomId)) {

                    ps.setInt(1, hotelId);
                    ps.setString(2, roomNumber);

                    try (ResultSet rs = ps.executeQuery()) {
                        rs.next();
                        roomId = rs.getInt("room_id");
                        price = rs.getDouble("price_per_night");
                    }
                }

                String insertSql =
                    "INSERT INTO bookings "
                    + "(customer_name, room_id, nights, total_amount) "
                    + "VALUES (?, ?, ?, ?)";

                try (PreparedStatement ps =
                        con.prepareStatement(insertSql)) {

                    ps.setString(1, customerName);
                    ps.setInt(2, roomId);
                    ps.setInt(3, nights);
                    ps.setDouble(4, price * nights);
                    ps.executeUpdate();
                }

                String updateSql =
                    "UPDATE rooms SET room_status = 'BOOKED' "
                    + "WHERE room_id = ? AND room_status = 'AVAILABLE'";

                try (PreparedStatement ps =
                        con.prepareStatement(updateSql)) {

                    ps.setInt(1, roomId);

                    if (ps.executeUpdate() != 1) {
                        throw new SQLException(
                            "Room " + roomNumber
                            + " could not be booked."
                        );
                    }
                }
            }

            con.commit();

            System.out.println("\n===== BOOKING CONFIRMED =====");
            System.out.println("Customer: " + customerName);
            System.out.println("Hotel ID: " + hotelId);
            System.out.println("Number of Rooms: " + roomCount);
            System.out.println("Rooms: " + String.join(", ", selectedRooms));
            System.out.println("Number of Nights: " + nights);
            System.out.printf("Total Amount: Rs.%.2f%n", totalAmount);
            System.out.println("All rooms booked successfully!");

        } catch (SQLException e) {

            con.rollback();
            System.out.println("Booking failed: " + e.getMessage());

        } finally {
            con.setAutoCommit(oldAutoCommit);
        }
    }

    // 6. View Booking Details
    static void viewBookings(Connection con) throws SQLException {

        String sql =
            "SELECT b.booking_id, b.customer_name, "
            + "h.hotel_name, r.room_number, r.room_type, "
            + "b.nights, b.total_amount, b.booking_time "
            + "FROM bookings b "
            + "JOIN rooms r ON b.room_id = r.room_id "
            + "JOIN hotels h ON r.hotel_id = h.hotel_id "
            + "ORDER BY b.booking_id";

        try (PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            System.out.println("\n===== BOOKING DETAILS =====");

            boolean found = false;

            while (rs.next()) {
                found = true;

                System.out.println(
                    "\nBooking ID: " + rs.getInt("booking_id")
                    + "\nCustomer: " + rs.getString("customer_name")
                    + "\nHotel: " + rs.getString("hotel_name")
                    + "\nRoom: " + rs.getString("room_number")
                    + "\nRoom Type: " + rs.getString("room_type")
                    + "\nNights: " + rs.getInt("nights")
                    + "\nAmount: Rs." + rs.getDouble("total_amount")
                    + "\nBooked At: " + rs.getTimestamp("booking_time")
                );
            }

            if (!found) {
                System.out.println("No bookings available.");
            }
        }
    }

    // 7. Cancel Booking
    static void cancelBooking(Connection con) throws SQLException {

        viewBookings(con);

        System.out.print("\nEnter Booking ID to cancel: ");
        int bookingId = sc.nextInt();
        sc.nextLine();

        boolean oldAutoCommit = con.getAutoCommit();

        try {
            con.setAutoCommit(false);

            String findSql =
                "SELECT room_id FROM bookings "
                + "WHERE booking_id = ? FOR UPDATE";

            int roomId;

            try (PreparedStatement ps = con.prepareStatement(findSql)) {

                ps.setInt(1, bookingId);

                try (ResultSet rs = ps.executeQuery()) {

                    if (!rs.next()) {
                        System.out.println("Booking ID not found!");
                        con.rollback();
                        return;
                    }

                    roomId = rs.getInt("room_id");
                }
            }

            String deleteSql =
                "DELETE FROM bookings WHERE booking_id = ?";

            try (PreparedStatement ps = con.prepareStatement(deleteSql)) {
                ps.setInt(1, bookingId);
                ps.executeUpdate();
            }

            String updateSql =
                "UPDATE rooms SET room_status = 'AVAILABLE' "
                + "WHERE room_id = ?";

            try (PreparedStatement ps = con.prepareStatement(updateSql)) {
                ps.setInt(1, roomId);
                ps.executeUpdate();
            }

            con.commit();
            System.out.println("Booking cancelled successfully!");
            System.out.println("Room is now available.");

        } catch (SQLException e) {

            con.rollback();
            System.out.println("Cancellation failed: " + e.getMessage());

        } finally {
            con.setAutoCommit(oldAutoCommit);
        }
    }
}
