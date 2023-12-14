import java.sql.*;

public class DatabaseConnector {
    private static final String JDBC_URL = "jdbc:mysql://localhost:3306/java_project";
    private static final String USER = "root";
    private static final String PASSWORD = "";

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(JDBC_URL, USER, PASSWORD);
    }
}
