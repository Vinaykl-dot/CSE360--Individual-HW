package application;

import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import databasePart1.*;

public class SetupLoginSelectionPage {
    
    private final DatabaseHelper databaseHelper;

    public SetupLoginSelectionPage(DatabaseHelper databaseHelper) {
        this.databaseHelper = databaseHelper;
    }

    public void show(Stage primaryStage) {
        VBox layout = new VBox(20); // Increased spacing for better layout
        layout.setStyle("-fx-alignment: center; -fx-padding: 30; -fx-background-color: black;"); // ✅ Black background

        // ✅ Create styled buttons
        Button setupButton = createStyledButton("SetUp");
        Button loginButton = createStyledButton("Login");

        // ✅ Button actions
        setupButton.setOnAction(a -> {
            new SetupAccountPage(databaseHelper).show(primaryStage);
        });

        loginButton.setOnAction(a -> {
            new UserLoginPage(databaseHelper).show(primaryStage);
        });

        layout.getChildren().addAll(setupButton, loginButton);

        Scene scene = new Scene(layout, 800, 400);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Account Setup");
        primaryStage.show();
    }

    // ✅ Helper method to style buttons
    private Button createStyledButton(String text) {
        Button button = new Button(text);
        button.setStyle(
            "-fx-font-size: 18px; " +  // Bigger text
            "-fx-padding: 15px 40px; " +  // Bigger buttons
            "-fx-background-color: grey; " +
            "-fx-text-fill: white; " +
            "-fx-background-radius: 10px; "
        );
        button.setOnMouseEntered(e -> button.setStyle(
            "-fx-font-size: 18px; " +
            "-fx-padding: 15px 40px; " +
            "-fx-background-color: white; " +
            "-fx-text-fill: black; " +
            "-fx-background-radius: 10px; "
        ));
        button.setOnMouseExited(e -> button.setStyle(
            "-fx-font-size: 18px; " +
            "-fx-padding: 15px 40px; " +
            "-fx-background-color: grey; " +
            "-fx-text-fill: white; " +
            "-fx-background-radius: 10px; "
        ));
        return button;
    }
}

