import java.sql.*;

public class DatabaseConnector {
    private static final String JDBC_URL = "jdbc:mysql://localhost:3306/java_project";
    private static final String USER = "haroun";
    private static final String PASSWORD = "Haroun.2001";

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(JDBC_URL, USER, PASSWORD);
    }
}
