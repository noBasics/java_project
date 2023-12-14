import java.sql.*;
import java.util.*;

public class Reports {
    //private static final String BUY_TYPE = "buy";
    private static final String SELL_TYPE = "sell";

    public static void generateReports(Scanner scanner, String userRole) {
        try (Connection connection = DatabaseConnector.getConnection()) {
            // Generate and display various reports based on your requirements
            generateRevenueReport(connection);
            generateQuantitySoldReport(connection);
            generateMostSoldProduct(connection);
            // Add more reports as needed
            
            System.out.print("Press Enter to go back to the main menu...");
            scanner.nextLine();
            MainMenu.displayMainMenu(scanner, userRole);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static void generateRevenueReport(Connection connection) {
        try {
            double totalRevenue = calculateTotalRevenue(connection);
            System.out.println("   Total Revenue: $" + totalRevenue);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static double calculateTotalRevenue(Connection connection) throws SQLException {
        String query = "SELECT SUM(transactions.quantity * products.price) AS revenue " +
                       "FROM transactions " +
                       "JOIN products ON transactions.product_name = products.name " +
                       "WHERE transactions.type = ?";

        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, SELL_TYPE);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getDouble("revenue");
                }
            }
        }

        return 0.0;
    }


    private static void generateQuantitySoldReport(Connection connection) {
        try {
            Map<String, Integer> quantitySoldMap = calculateQuantitySold(connection);
            System.out.println("   Quantities Sold for Each Product:");
            for (Map.Entry<String, Integer> entry : quantitySoldMap.entrySet()) {
                System.out.println("    - " + entry.getKey() + ": " + entry.getValue() + " units");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static Map<String, Integer> calculateQuantitySold(Connection connection) throws SQLException {
        Map<String, Integer> quantitySoldMap = new HashMap<>();
        String query = "SELECT transactions.product_name, SUM(transactions.quantity) AS total_quantity " +
                       "FROM transactions " +
                       "WHERE transactions.type = ? " +
                       "GROUP BY transactions.product_name";

        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, SELL_TYPE);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    String productName = resultSet.getString("product_name");
                    int totalQuantity = resultSet.getInt("total_quantity");
                    quantitySoldMap.put(productName, totalQuantity);
                }
            }
        }

        return quantitySoldMap;
    }

    private static void generateMostSoldProduct(Connection connection) {
        try {
            String mostSoldProduct = calculateMostSoldProduct(connection);
            System.out.println("   Most Sold Product: " + mostSoldProduct);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static String calculateMostSoldProduct(Connection connection) throws SQLException {
        String query = "SELECT product_name, SUM(quantity) AS total_quantity FROM transactions " +
                "WHERE type = ? GROUP BY product_name ORDER BY total_quantity DESC LIMIT 1";

        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, SELL_TYPE);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getString("product_name");
                }
            }
        }

        return "N/A";
    }
}
