package application;

import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import databasePart1.DatabaseHelper;

/**
 * AdminHomePage class represents the user interface for the admin user.
 * Admins can generate invitation codes and manage users.
 */
public class AdminHomePage {

    private final DatabaseHelper databaseHelper;

    public AdminHomePage(DatabaseHelper databaseHelper) {
        this.databaseHelper = databaseHelper;
    }

    /**
     * Displays the admin page in the provided primary stage.
     * @param primaryStage The primary stage where the scene will be displayed.
     */
    public void show(Stage primaryStage) {
        VBox layout = new VBox(15); // ✅ Increased spacing for better visibility
        layout.setStyle("-fx-alignment: center; -fx-padding: 30; -fx-background-color: black;");

        // ✅ Welcome label for admin
        Label adminLabel = new Label("Hello, Admin!");
        adminLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: white;");

        // ✅ Button to generate invitation codes
        Button generateCodeButton = createStyledButton("Generate Invitation Code");
        Label codeLabel = new Label();  // ✅ Label to display generated code
        codeLabel.setStyle("-fx-text-fill: white; -fx-font-size: 14px;");

        generateCodeButton.setOnAction(e -> {
            String code = databaseHelper.generateInvitationCode();  // ✅ Fetch from database
            codeLabel.setText("Generated Code: " + code);
        });

        // ✅ Button to manage users
        Button manageUsersButton = createStyledButton("Manage Users");
        manageUsersButton.setOnAction(e -> new ManageUsersPage(databaseHelper).show(primaryStage));
        
        Button backButton = createStyledButton("⬅ Back");
        backButton.setOnAction(e -> new WelcomeLoginPage(databaseHelper).show(primaryStage, new User("admin", "", "admin")));

        // ✅ Logout button to return to login page
        Button logoutButton = createStyledButton("Logout");
        logoutButton.setOnAction(e -> new UserLoginPage(databaseHelper).show(primaryStage));

        // ✅ Add elements to layout
        layout.getChildren().addAll(adminLabel, generateCodeButton, codeLabel, manageUsersButton, backButton, logoutButton);

        // ✅ Scene Setup
        Scene adminScene = new Scene(layout, 800, 400);
        primaryStage.setScene(adminScene);
        primaryStage.setTitle("Admin Page");
        primaryStage.show();
    }

    // ✅ Styled Button Helper Method
    private Button createStyledButton(String text) {
        Button button = new Button(text);
        button.setMinWidth(200); // ✅ Set Fixed Width for Uniform Size
        button.setStyle(
            "-fx-font-size: 14px; " +
            "-fx-background-color: #555555; " + // Dark Grey Buttons
            "-fx-text-fill: white; " + // White Text
            "-fx-background-radius: 5px; " +
            "-fx-padding: 10px;"
        );
        button.setOnMouseEntered(e -> button.setStyle(
            "-fx-font-size: 14px; " +
            "-fx-background-color: #888888; " + // Lighter Grey on Hover
            "-fx-text-fill: white; " +
            "-fx-background-radius: 5px; " +
            "-fx-padding: 10px;"
        ));
        button.setOnMouseExited(e -> button.setStyle(
            "-fx-font-size: 14px; " +
            "-fx-background-color: #555555; " + // Reset to Dark Grey
            "-fx-text-fill: white; " +
            "-fx-background-radius: 5px; " +
            "-fx-padding: 10px;"
        ));
        return button;
    }
}

