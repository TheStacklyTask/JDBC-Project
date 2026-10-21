package org.JDBC;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {

	    private static final String URL =
	            "jdbc:mysql://localhost:3306/";

	    private static final String USER = "root";

	    private static final String PASSWORD = "Hari1997@";

	    public static Connection getConnection(String databaseName)
	            throws SQLException {

	        String dbURL = URL + databaseName;

	        return DriverManager.getConnection(
	                dbURL, USER, PASSWORD
	      );
	  }
}
