import java.sql.*;
import java.util.Scanner;

public class ManageProducts {

    public static void manageProductsMenu(Scanner scanner) {
        while (true) {
            System.out.println("--------------- Manage Products Menu ---------------");
            System.out.println("1. Create a New Product");
            System.out.println("2. Display All Products");
            System.out.println("3. Edit an Existing Product");
            System.out.println("4. Delete an Existing Product");
            System.out.println("5. Back to Main Menu");
            System.out.println("-----------------------------------------");
            System.out.print("Enter your choice (1-5): ");

            int choice = scanner.nextInt();
            scanner.nextLine(); // Consume the newline character

            switch (choice) {
                case 1:
                    createNewProduct(scanner);
                    break;
                case 2:
                    displayAllProducts();
                    break;
                case 3:
                	editProduct(scanner);
                    break;
                case 4:
                    deleteProduct(scanner);
                    break;
                case 5:
                    MainMenu.displayMainMenu(scanner); // Return to the main menu
                default:
                    System.out.println("Invalid choice. Please choose a number between 1 and 5.");
            }
        }
    }

    private static void createNewProduct(Scanner scanner) {
        try (Connection connection = DatabaseConnector.getConnection()) {
            System.out.print("Enter product name: ");
            String productName = scanner.nextLine();

            // Check if the product already exists
            if (productExists(connection, productName)) {
                System.out.println("Product with the same name already exists. Please choose a different name.");
                return; // Exit the method without inserting the product
            }

            System.out.print("Enter product category: ");
            String category = scanner.nextLine();

            System.out.print("Enter product quantity: ");
            int quantity = scanner.nextInt();
            scanner.nextLine(); // Consume the newline character

            System.out.print("Enter product price: ");
            double price = scanner.nextDouble();
            scanner.nextLine(); // Consume the newline character

            // Insert the new product into the database
            String query = "INSERT INTO products (name, category, quantity, price) VALUES (?, ?, ?, ?)";
            try (PreparedStatement preparedStatement = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
                preparedStatement.setString(1, productName);
                preparedStatement.setString(2, category);
                preparedStatement.setInt(3, quantity);
                preparedStatement.setDouble(4, price);

                int rowsAffected = preparedStatement.executeUpdate();

                if (rowsAffected > 0) {
                    // Retrieve the generated ID (auto-incremented primary key)
                    try (ResultSet generatedKeys = preparedStatement.getGeneratedKeys()) {
                        if (generatedKeys.next()) {
                            int generatedId = generatedKeys.getInt(1);
                            System.out.println("Product added successfully with ID: " + generatedId);
                        } else {
                            System.out.println("Failed to retrieve the generated ID.");
                        }
                    }
                } else {
                    System.out.println("Failed to add the product. Please try again.");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static boolean productExists(Connection connection, String productName) throws SQLException {
        String query = "SELECT * FROM products WHERE name = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, productName);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                return resultSet.next(); // true if the product already exists, false otherwise
            }
        }
    }

    private static void displayAllProducts() {
        try (Connection connection = DatabaseConnector.getConnection()) {
            String query = "SELECT * FROM products";
            try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    if (resultSet.next()) {
                        // Display column headers
                        System.out.println("\n-----------------------------------------------------------------------");
                        System.out.printf("%-5s %-20s %-20s %-10s %-10s%n", "ID", "Product Name", "Category", "Quantity", "Price");
                        System.out.println("-----------------------------------------------------------------------");

                        // Display each product
                        do {
                            int id = resultSet.getInt("id");
                            String productName = resultSet.getString("name");
                            String category = resultSet.getString("category");
                            int quantity = resultSet.getInt("quantity");
                            double price = resultSet.getDouble("price");

                            System.out.printf("%-5s %-20s %-20s %-10s %-10s%n", id, productName, category, quantity, price);
                        } while (resultSet.next());
                    } else {
                        System.out.println("No products found.");
                    }
                    System.out.printf("\n");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    
    private static void editProduct(Scanner scanner) {
        System.out.print("Enter the name of the product to edit: ");
        String productName = scanner.nextLine();

        
        try (Connection connection = DatabaseConnector.getConnection()) {
            // Check if the product exists
            if (!productExists(connection, productName)) {
                System.out.println("Product not found.");
                return;
            }

            // Prompt the user for the attribute to update
            System.out.print("Choose attribute to update (name, category, quantity, price): ");
            String attribute = scanner.nextLine().toLowerCase(); // Convert to lowercase for case-insensitive comparison

            // Check if the user wants to update the name, and if so, check if the new name already exists
            if ("name".equals(attribute)) {
                System.out.print("Enter the new name: ");
                String newName = scanner.nextLine();

                if (!productName.equals(newName) && productExists(connection, newName)) {
                    System.out.println("Product with the new name already exists. Please choose a different name.");
                    return; // Exit the method without updating
                }

                // Update the product name in the database
                updateProductAttribute(connection, productName, "name", newName);
                System.out.println("Product name updated successfully.");
            } else if ("category".equals(attribute) || "quantity".equals(attribute)) {
                // For category or quantity, prompt the user for the new value
                System.out.print("Enter the new " + attribute + ": ");
                String newValue = scanner.nextLine();

                // Update the selected attribute in the database
                updateProductAttribute(connection, productName, attribute, newValue);
                System.out.println(attribute + " updated successfully.");
            } else if ("price".equals(attribute)) {
                // For price, prompt the user for the new value as a double
                System.out.print("Enter the new price: ");
                double newPrice = scanner.nextDouble();
                scanner.nextLine(); // Consume the newline character

                // Update the price in the database
                updateProductAttribute(connection, productName, attribute, newPrice);
                System.out.println("Price updated successfully.");
            } else {
                System.out.println("Invalid attribute. Please choose 'name', 'category', 'quantity', or 'price'.");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static void updateProductAttribute(Connection connection, String productName, String attribute, Object newValue) throws SQLException {
        String query = "UPDATE products SET " + attribute + " = ? WHERE name = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setObject(1, newValue); // Use setObject to handle different data types
            preparedStatement.setString(2, productName);
            preparedStatement.executeUpdate();
        }
    }


    private static void deleteProduct(Scanner scanner) {
        System.out.print("Enter the name of the product to delete: ");
        String productName = scanner.nextLine();

        try (Connection connection = DatabaseConnector.getConnection()) {
            // Check if the product exists
            if (!productExists(connection, productName)) {
                System.out.println("Product not found.");
                return;
            }

            // Confirm deletion with the user
            System.out.print("Are you sure you want to delete the product '" + productName + "'? (yes/no): ");
            String confirmation = scanner.nextLine().toLowerCase(); // Convert to lowercase for case-insensitive comparison

            if ("yes".equals(confirmation)) {
                // Delete the product from the database
                deleteProductFromDatabase(connection, productName);
                System.out.println("Product deleted successfully.");
            } else {
                System.out.println("Deletion canceled.");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static void deleteProductFromDatabase(Connection connection, String productName) throws SQLException {
        String query = "DELETE FROM products WHERE name = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, productName);
            preparedStatement.executeUpdate();
        }
    }
}
