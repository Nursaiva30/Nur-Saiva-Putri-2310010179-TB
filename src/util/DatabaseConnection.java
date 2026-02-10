package util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Helper class untuk koneksi database MySQL
 */
public class DatabaseConnection {
    
    private static final String URL = "jdbc:mysql://localhost:3306/pegawai2";
    private static final String USER = "root";
    private static final String PASSWORD = "";
    
    private static Connection connection;
    
    /**
     * Mendapatkan koneksi ke database
     * @return Connection object
     */
    public static Connection getConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                Class.forName("com.mysql.cj.jdbc.Driver");
                connection = DriverManager.getConnection(URL, USER, PASSWORD);
            }
        } catch (ClassNotFoundException e) {
            System.err.println("MySQL Driver tidak ditemukan!");
            e.printStackTrace();
        } catch (SQLException e) {
            System.err.println("Gagal koneksi ke database!");
            e.printStackTrace();
        }
        return connection;
    }
    
    /**
     * Menutup koneksi database
     */
    public static void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
