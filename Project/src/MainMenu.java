import java.sql.*;
import java.util.*;

public class MainMenu {
    public static void main(String[] args) {
    	/*Scanner scanner = new Scanner(System.in);
    			
        displayLoginFrame();
        boolean isLoggedIn = false;
        
        while(!isLoggedIn) {
		        try {
		            System.out.print("Enter your username: ");
		            String username = scanner.nextLine();
		
		            System.out.print("Enter your password: ");
		            String password = scanner.nextLine();
		
		            if (authenticateUser(username, password)) {
		                System.out.println("Login successful!");
		                isLoggedIn = true;
		                
		                
		                
		            } else {
		                System.out.println("Invalid username or password.");
		                System.out.println("Press Enter to retry...");
		                scanner.nextLine();
		            }
		        } catch (SQLException e) {
		            e.printStackTrace();
		        }
		    }
        */
    	
    	Scanner scanner = new Scanner(System.in);

        displayLoginFrame();
        boolean isLoggedIn = false;
        String userRole = null; // Variable to store the user's role

        while (!isLoggedIn) {
            try {
                System.out.print("Enter your username: ");
                String username = scanner.nextLine();

                System.out.print("Enter your password: ");
                String password = scanner.nextLine();

                userRole = authenticateUser(username, password);

                if (userRole != null) {
                    System.out.println("Login successful! You're logged in as " + userRole);
                    isLoggedIn = true;
                } else {
                    System.out.println("Invalid username or password.");
                    System.out.println("Press Enter to retry...");
                    scanner.nextLine();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        
        if(isLoggedIn) {
  
        	// Display the main menu
            displayMainMenu(scanner, userRole);
            
        }
        
      }
    
    

    private static void displayLoginFrame() {
        System.out.println("------------------------------------------------");
        System.out.println("|                  Login System                  |");
        System.out.println("------------------------------------------------");
    }

    public static void displayMainMenu(Scanner scanner, String userRole) {
        System.out.println("--------------- Main Menu ---------------");
        System.out.println("1. Manage Products");
        System.out.println("2. Manage Transactions");
        System.out.println("3. Reports");
        System.out.println("-----------------------------------------");
        System.out.print("Enter your choice (1-3): ");
        
        // Get the user's choice from the menu
        int choice = scanner.nextInt();
        scanner.nextLine(); // Consume the newline character

        // Process the user's choice
        processMenuChoice(choice, scanner, userRole);
        
    }

    /*private static boolean authenticateUser(String username, String password) throws SQLException {
        try (Connection connection = DatabaseConnector.getConnection()) {
            String query = "SELECT * FROM users WHERE username = ? AND password = ?";
            try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                preparedStatement.setString(1, username);
                preparedStatement.setString(2, password);

                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    return resultSet.next(); // true if the user exists, false otherwise
                }
            }
        }
    }*/
    
    private static String authenticateUser(String username, String password) throws SQLException {
        try (Connection connection = DatabaseConnector.getConnection()) {
            String query = "SELECT * FROM users WHERE username = ? AND password = ?";
            try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                preparedStatement.setString(1, username);
                preparedStatement.setString(2, password);

                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    if (resultSet.next()) {
                        // User exists, return the role
                        return resultSet.getString("role");
                        
                    } else {
                        // User does not exist
                        return null;
                    }
                }
            }
        }
    }


    private static void processMenuChoice(int choice, Scanner scanner, String userRole) {
        switch (choice) {
            case 1:
                System.out.println("You chose: Manage Products");
                // Implement the logic for managing products
                //ManageProducts.manageProductsMenu(scanner);
                
                if ("admin".equalsIgnoreCase(userRole)) {
                    ManageProducts.manageProductsMenu(scanner, userRole);
                } else {
                    System.out.println("Access denied. You do not have permission to manage products.");
                    displayMainMenu(scanner, userRole);
                }
                
                break;
            case 2:
                System.out.println("You chose: Manage Transactions");
                ManageTransactions.manageTransactionsMenu(scanner, userRole);
                // Implement the logic for managing transactions
                break;
            case 3:
                System.out.println("You chose: Reports");
                Reports.generateReports(scanner, userRole);
                // Implement the logic for generating reports;
                break;
            default:
                System.out.println("Invalid choice. Please choose a number between 1 and 3.");
                displayMainMenu(scanner, userRole); //added it because of a problem when coming back from  product menu 
        }
    }
}
