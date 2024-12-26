
import java.sql.*;
import java.util.ArrayList;
import java.util.Scanner;

public class MusicStreamingService {
    private static ResultSet resultSet;
    private static Scanner scan = new Scanner(System.in);

    public static void main(String[] args) throws SQLException {
        Security.startDBConnection();
        startApplication();
    }

    public static void startApplication() throws SQLException {
        String input;

        while (true) {
            System.out.println("\n\n===================== Welcome to Music Streaming Application =====================");
            System.out.println("0. Exit");
            System.out.println("1. Manager Menu");
            System.out.println("2. User Menu");
            System.out.println("----------------------------------------------------------------------------------");
            System.out.print("Please enter your option: ");
            input = scan.nextLine();

            switch (input) {
                case "0": {
                    System.out.println("\nSee you next time! Goodbye...");
                    return;
                }
                case "1":
                case "2": {
                    initialMenu(Integer.parseInt(input));
                    break;
                }
                default: {
                    System.out.println("\nWrong Input! Try again...");
                    break;
                }
            }
        }
    }

    private static void initialMenu(int input) throws SQLException {
        while (true) {
            String input2;
            String role = input == 1 ? "managers" : "users";

            System.out.println("\n\n=============================== Welcome " + role.toUpperCase() + " ===============================");
            System.out.println("0. Return to previous menu");
            System.out.println("1. Login");
            System.out.println("2. Sign up");
            System.out.println("3. Forgot Password");
            System.out.println("----------------------------------------------------------------------------------");
            System.out.print("Please enter your option: ");
            input2 = scan.nextLine();

            switch (input2) {
                case "0": {
                    return;
                }
                case "1": {
                    login(role);
                    break;
                }
                case "2": {
                    signup(role);
                    break;
                }
                case "3": {
                    forgotPassword(role);
                    break;
                }
                default: {
                    System.out.println("\nWrong Input! Try again...");
                    break;
                }
            }
        }
    }

    private static void login(String role) throws SQLException {
        // check if username & password exist
        while (true) {
            System.out.println("\n\n===================================== Log In =====================================");
            if (role.equals("managers")) {
                System.out.print("Enter the admin token provided by the company (or 0 to return): ");
                String adminToken = scan.nextLine();
                if (adminToken.equals("0")) break;
                if (!Security.adminCheck(adminToken)) { // check if it's admin
                    System.out.println("Invalid Admin Token! Verify your admin token from company.");
                    return;
                } else {
                    System.out.println("Verified you are admin. Please proceed your login.\n");
                }
            }
            System.out.print("Enter your username: ");
            String username = scan.nextLine();
            if (username.equals("0")) {
                break;
            }
            System.out.print("Enter your password: ");
            String password = scan.nextLine();

            String hashedPassword = Security.hashPassword(password);

            String query = "SELECT * FROM " + role + " WHERE username = '" + username + "' AND password = '" + hashedPassword + "'"; // compare with hashed password
            resultSet = Security.sqlResult(query);

            System.out.println("----------------------------------------------------------------------------------");
            if (resultSet.next()) {
                System.out.println("Login successful! Welcome, " + username);
                if (role.equals("managers")) {
                    Manager manager = new Manager(resultSet.getInt("managerId"));
                    manager.managerMenu();
                } else {
                    User user = new User(resultSet.getInt("userId"));
                    user.userMenu();
                }
                break;
            } else {
                System.out.println("Invalid username or password. Please try again.");
                System.out.println("If you want to go back to previous menu, enter 0 in the next \"username\" field.");
            }
        }
    }

    private static void signup(String role) throws SQLException {
        // check if username & password are valid (username should be unique)
        while (true) {
            System.out.println("\n\n===================================== Sign Up =====================================");
            if (role.equals("managers")) {
                System.out.print("Enter the admin token provided by the company: ");
                String adminToken = scan.nextLine();
                if (!Security.adminCheck(adminToken)) {
                    System.out.println("Invalid Admin Token! Verify your admin token from company.");
                    return;
                } else {
                    System.out.println("Verified you are admin. Please proceed your sign up.\n");
                }
            }
            System.out.print("Enter your username: ");
            String username = scan.nextLine();
            if (username.equals("0")) {
                break;
            }
            String password;
            String password_ck;
            while (true) {
                System.out.print("Enter your password: ");
                password = scan.nextLine();
                if (password.isEmpty()) {
                    System.out.println("Password is required. Try again.");
                    continue;
                }
                System.out.print("Enter your password again: ");
                password_ck = scan.nextLine();
                if (!password.equals(password_ck)) {
                    System.out.println("Passwords do not match! Try again...");
                } else {
                    break;
                }
            }

            String hashedPassword = Security.hashPassword(password);

            String checkQuery = "SELECT * FROM " + role + " WHERE username = '" + username + "'";
            resultSet = Security.sqlResult(checkQuery);
            System.out.println("----------------------------------------------------------------------------------");
            // check if the username already exists since username is unique attribute (redundancy check)
            if (resultSet.next()) {
                System.out.println("Username already exists. Please choose different username.");
                System.out.println("If you want to go back to previous menu, enter \"0\" in the next \"username\" field.");
            } else {
                // insert new user if username does not exist
                String insertQuery = "INSERT INTO " + role + " (username, password) VALUES ('" + username + "', '" + hashedPassword + "')";
                int result = Security.sqlUpdate(insertQuery);
                if (role.equals("users")) {
                    String managerQuery = "SELECT managerId FROM managers";
                    resultSet = Security.sqlResult(managerQuery);

                    ArrayList<Integer> managerIds = new ArrayList<>();
                    int id;
                    while (resultSet.next()) {
                        id = resultSet.getInt("managerId");
                        managerIds.add(id);
                    }
                    // assert a random manager for a user
                    String managingQuery = "UPDATE users SET managedBy = " + managerIds.get((int) (Math.random() * managerIds.size())) + " WHERE username = '" + username + "'";
                    Security.sqlUpdate(managingQuery);
                }
                if (result > 0) {
                    System.out.println("Sign up successful! You can now log in.");
                } else {
                    System.out.println("Error during sign up. Please try again.");
                }
                break;
            }

        }
    }

    private static void forgotPassword(String role) throws SQLException {
        // check if username exists & enter new password
        while (true) {
            System.out.println("\n\n================================= Forgot Password ================================");
            if (role.equals("managers")) {
                System.out.print("Enter the admin token provided by the company: ");
                String adminToken = scan.nextLine();
                if (!Security.adminCheck(adminToken)) {
                    System.out.println("Invalid Admin Token! Verify your admin token from company.");
                    return;
                } else {
                    System.out.println("Verified you are admin. Please proceed.\n");
                }
            }
            System.out.print("Enter your username: ");
            String username = scan.nextLine();
            if (username.equals("0")) {
                break;
            }

            // check if the username exists
            String checkQuery = "SELECT * FROM " + role + " WHERE username = '" + username + "'";
            resultSet = Security.sqlResult(checkQuery);
            if (resultSet.next()) {
                System.out.print("Enter your new password: ");
                String newPassword = scan.nextLine();
                if (newPassword.isEmpty()) {
                    System.out.println("Password is required. Try again.");
                    continue;
                }
                System.out.print("Enter your new password again: ");
                String newPassword_ck = scan.nextLine();
                if (!newPassword.equals(newPassword_ck)) {
                    System.out.println("Passwords do not match! Try again...");
                    continue;
                }
                String hashedPassword = Security.hashPassword(newPassword); // update with hashed password
                String updateQuery = "UPDATE " + role + " SET password = '" + hashedPassword + "' WHERE username = '" + username + "'";
                Security.sqlUpdate(updateQuery);
                System.out.println("----------------------------------------------------------------------------------");
                System.out.println("Password reset successful! You can now log in.");
                break;
            } else {
                System.out.println("----------------------------------------------------------------------------------");
                System.out.println("Username not found. Please try again.");
                System.out.println("If you want to go back to previous menu, enter \"0\" in the next \"username\" field.");
            }

        }
    }
}
