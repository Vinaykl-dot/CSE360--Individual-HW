package application;

import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import databasePart1.*;

public class WelcomeLoginPage {
    
    private final DatabaseHelper databaseHelper;

    public WelcomeLoginPage(DatabaseHelper databaseHelper) {
        this.databaseHelper = databaseHelper;
    }

    public void show(Stage primaryStage, User user) {
        VBox layout = new VBox(15); // Increased spacing
        layout.setStyle("-fx-alignment: center; -fx-padding: 30; -fx-background-color: #000000;"); // ✅ Black Background

        // ✅ Styled Welcome Label (White Text)
        Label welcomeLabel = new Label("Welcome to the System!");
        welcomeLabel.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: white;");

        // ✅ Styled Buttons (Fixed Width)
        Button continueButton = createStyledButton("Continue to Your Page");
        Button updateInfoButton = createStyledButton("Update Info");
        Button logoutButton = createStyledButton("Logout");

        // ✅ Set Button Actions
        continueButton.setOnAction(a -> {
            String role = user.getRole();
            System.out.println("User Role at Welcome Page: " + role); // ✅ Debugging Output
            if ("admin".equals(role)) {
                new AdminHomePage(databaseHelper).show(primaryStage);
            } else {
                new UserHomePage(databaseHelper, user).show(primaryStage); // ✅ Pass required arguments
            }

        });

        updateInfoButton.setOnAction(e -> {
            new UpdateUserPage(databaseHelper, user.getUserName()).show(primaryStage);
        });

        // ✅ Fix: Logout now redirects to Login Page instead of exiting
        logoutButton.setOnAction(a -> {
            new UserLoginPage(databaseHelper).show(primaryStage); // ✅ Only Redirect, No Database Close
        });

       
        // ✅ Add Elements to Layout
        layout.getChildren().addAll(welcomeLabel, continueButton, updateInfoButton, logoutButton);

        // ✅ Set Scene with Updated Styling
        Scene scene = new Scene(layout, 800, 400);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Welcome Page");
        primaryStage.show();
    }

    // ✅ Helper Method for Creating Uniform Buttons
    private Button createStyledButton(String text) {
        Button button = new Button(text);
        button.setMinWidth(220); // ✅ Set Fixed Width for Uniform Size
        button.setStyle(
            "-fx-font-size: 14px; " +
            "-fx-background-color: #333333; " + // Dark Grey Buttons
            "-fx-text-fill: white; " + // White Text
            "-fx-background-radius: 8px; " +
            "-fx-padding: 12px;"
        );
        button.setOnMouseEntered(e -> button.setStyle(
            "-fx-font-size: 14px; " +
            "-fx-background-color: #555555; " + // Lighter Grey on Hover
            "-fx-text-fill: white; " +
            "-fx-background-radius: 8px; " +
            "-fx-padding: 12px;"
        ));
        button.setOnMouseExited(e -> button.setStyle(
            "-fx-font-size: 14px; " +
            "-fx-background-color: #333333; " + // Reset to Dark Grey
            "-fx-text-fill: white; " +
            "-fx-background-radius: 8px; " +
            "-fx-padding: 12px;"
        ));
        return button;
    }
}



