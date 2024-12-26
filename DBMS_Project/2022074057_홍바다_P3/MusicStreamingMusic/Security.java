import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.*;

public class Security {
    static Connection connection;
    private static String adminToken = "music0715"; // token from company for verifying admin

    static void startDBConnection() {
        String url = "jdbc:mysql://localhost:3306/music_service"; // database URL
        String username = "root"; // MySQL username
        String password = "bada"; // MySQL password
        try {
            Class.forName("com.mysql.cj.jdbc.Driver"); // JDBC Driver
            connection = DriverManager.getConnection(url, username, password);
            // Statement is not used, so doesn't have to create instance for it
        } catch (ClassNotFoundException e) {
            System.out.println("MySQL JDBC Driver not found.");
            e.printStackTrace();
        } catch (SQLException e) {
            System.out.println("Failed to connect to the database.");
            e.printStackTrace();
        }
    }

    // for SELECT queries
    static ResultSet sqlResult(String sql) throws SQLException {
        PreparedStatement ps = connection.prepareStatement(sql);
        return ps.executeQuery(); // read from database
    }

    // for INSERT, UPDATE, DELETE queries
    static int sqlUpdate(String sql) throws SQLException {
        PreparedStatement ps = connection.prepareStatement(sql);
        return ps.executeUpdate(); // write to database
    }

    static boolean adminCheck(String token) {
        return token.equals(adminToken);
    }

    static String hashPassword(String password) { // hashing the password for security
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(password.getBytes());

            StringBuilder sb = new StringBuilder();
            for (byte b : hash) {
                sb.append(String.format("%02x", b)); // %02x: convert the byte to hexadecimal string into two characters -> 64 characters in total
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Error while hashing password", e);
        }
    }
}
