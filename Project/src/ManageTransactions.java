import java.sql.*;
import java.util.Scanner;

public class ManageTransactions {
	
	public static void manageTransactionsMenu(Scanner scanner) {
        while (true) {
            System.out.println("--------------- Manage Transactions Menu ---------------");
            System.out.println("1. Create a New Transaction");
            System.out.println("2. Display All Transactions");
            System.out.println("3. Delete an Existing Transaction");
            System.out.println("4. Back to Main Menu");
            System.out.println("-----------------------------------------");
            System.out.print("Enter your choice (1-4): ");

            int choice = scanner.nextInt();
            scanner.nextLine(); // Consume the newline character

            switch (choice) {
                case 1:
                    createTransaction(scanner);
                    break;
                case 2:
                    displayTransactions();
                    break;
                case 3:
                	deleteTransaction(scanner);
                    break;
                case 4:
                	MainMenu.displayMainMenu(scanner);
                
                default:
                    System.out.println("Invalid choice. Please choose a number between 1 and 4.");
            }
        }
	}
	
	public static void createTransaction(Scanner scanner) {
        System.out.print("Enter transaction type (buy/sell): ");
        String transactionType = scanner.nextLine().toLowerCase();

        if (!transactionType.equals("buy") && !transactionType.equals("sell")) {
            System.out.println("Invalid transaction type. Please enter 'buy' or 'sell'.");
            return;
        }

        System.out.print("Enter product name: ");
        String productName = scanner.nextLine();

        try (Connection connection = DatabaseConnector.getConnection()) {
            // Check if the product exists
            if (!productExists(connection, productName)) {
                System.out.println("Product not found.");
                return;
            }

            System.out.print("Enter quantity: ");
            int quantity = scanner.nextInt();
            scanner.nextLine(); // Consume the newline character

            // Check if the transaction quantity is valid
            if (transactionType.equals("sell") && getProductQuantity(connection, productName) < quantity) {
                System.out.println("Not enough quantity to sell.");
                return;
            }

            // Insert the transaction into the database
            insertTransaction(connection, transactionType, productName, quantity);

            // Update product quantity based on transaction type
            updateProductQuantity(connection, productName, quantity, transactionType);

            System.out.println("Transaction recorded successfully.");

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void displayTransactions() {
        try (Connection connection = DatabaseConnector.getConnection()) {
            String query = "SELECT * FROM transactions";
            try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    if (resultSet.next()) {
                        System.out.println("--------------- All Transactions ---------------");
                        System.out.printf("%-5s %-10s %-20s %-10s %-20s%n", "ID", "Type", "Product Name", "Quantity", "Date");
                        System.out.println("--------------------------------------------------");

                        do {
                            int id = resultSet.getInt("id");
                            String type = resultSet.getString("type");
                            String productName = resultSet.getString("product_name");
                            int quantity = resultSet.getInt("quantity");
                            String date = resultSet.getString("date");

                            System.out.printf("%-5s %-10s %-20s %-10s %-20s%n", id, type, productName, quantity, date);
                        } while (resultSet.next());
                    } else {
                        System.out.println("No transactions found.");
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void deleteTransaction(Scanner scanner) {
        System.out.print("Enter the ID of the transaction to delete: ");
        int transactionId = scanner.nextInt();
        scanner.nextLine(); // Consume the newline character

        try (Connection connection = DatabaseConnector.getConnection()) {
            // Check if the transaction exists
            if (!transactionExists(connection, transactionId)) {
                System.out.println("Transaction not found.");
                return;
            }

            // Delete the transaction from the database
            deleteTransactionFromDatabase(connection, transactionId);

            System.out.println("Transaction deleted successfully.");

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static boolean transactionExists(Connection connection, int transactionId) throws SQLException {
        String query = "SELECT * FROM transactions WHERE id = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setInt(1, transactionId);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                return resultSet.next();
            }
        }
    }

    private static void deleteTransactionFromDatabase(Connection connection, int transactionId) throws SQLException {
        String getTransactionQuery = "SELECT * FROM transactions WHERE id = ?";
        String deleteTransactionQuery = "DELETE FROM transactions WHERE id = ?";

        try (PreparedStatement getTransactionStatement = connection.prepareStatement(getTransactionQuery);
             PreparedStatement deleteTransactionStatement = connection.prepareStatement(deleteTransactionQuery)) {

            // Get the transaction details before deleting it
            getTransactionStatement.setInt(1, transactionId);
            try (ResultSet resultSet = getTransactionStatement.executeQuery()) {
                if (resultSet.next()) {
                    String transactionType = resultSet.getString("type");
                    String productName = resultSet.getString("product_name");
                    int quantity = resultSet.getInt("quantity");

                    // Delete the transaction from the transactions table
                    deleteTransactionStatement.setInt(1, transactionId);
                    deleteTransactionStatement.executeUpdate();

                    // Update product quantity based on transaction type
                    updateProductQuantityDelete(connection, productName, quantity, transactionType);
                } else {
                    System.out.println("Transaction not found.");
                }
            }
        }
    }
    
    private static boolean productExists(Connection connection, String productName) throws SQLException {
        String query = "SELECT * FROM products WHERE name = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, productName);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                return resultSet.next();
            }
        }
    }

    private static int getProductQuantity(Connection connection, String productName) throws SQLException {
        String query = "SELECT quantity FROM products WHERE name = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, productName);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getInt("quantity");
                }
            }
        }
        return 0;
    }

    private static void insertTransaction(Connection connection, String transactionType, String productName, int quantity) throws SQLException {
        String query = "INSERT INTO transactions (type, product_name, quantity) VALUES (?, ?, ?)";
        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, transactionType);
            preparedStatement.setString(2, productName);
            preparedStatement.setInt(3, quantity);
            preparedStatement.executeUpdate();
        }
    }

    private static void updateProductQuantity(Connection connection, String productName, int quantity, String transactionType) throws SQLException {
        String updateQuery;
        if (transactionType.equals("sell")) {
            updateQuery = "UPDATE products SET quantity = quantity - ? WHERE name = ? AND quantity >= ?";
        } else {
            updateQuery = "UPDATE products SET quantity = quantity + ? WHERE name = ?";
        }

        try (PreparedStatement preparedStatement = connection.prepareStatement(updateQuery)) {
            preparedStatement.setInt(1, quantity);
            preparedStatement.setString(2, productName);

            if (transactionType.equals("sell")) {
                preparedStatement.setInt(3, quantity); // Ensure quantity is not reduced below zero
            }

            preparedStatement.executeUpdate();
        }
    }
    
    private static void updateProductQuantityDelete(Connection connection, String productName, int quantity, String transactionType) throws SQLException {
        String updateQuery;
        if (transactionType.equals("buy")) {
            updateQuery = "UPDATE products SET quantity = quantity - ? WHERE name = ? AND quantity >= ?";
        } else {
            updateQuery = "UPDATE products SET quantity = quantity + ? WHERE name = ?";
        }

        try (PreparedStatement preparedStatement = connection.prepareStatement(updateQuery)) {
            preparedStatement.setInt(1, quantity);
            preparedStatement.setString(2, productName);

            if (transactionType.equals("buy")) {
                preparedStatement.setInt(3, quantity); // Ensure quantity is not reduced below zero
            }

            preparedStatement.executeUpdate();
        }
    }

}
