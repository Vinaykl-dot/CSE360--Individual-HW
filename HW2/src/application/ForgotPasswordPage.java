package application;

import databasePart1.DatabaseHelper;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class ForgotPasswordPage {
    private final DatabaseHelper databaseHelper;

    public ForgotPasswordPage(DatabaseHelper databaseHelper) {
        this.databaseHelper = databaseHelper;
    }

    public void show(Stage primaryStage) {
        VBox layout = new VBox(10);
        layout.setStyle("-fx-padding: 20; -fx-alignment: center; -fx-background-color: black;");

        Label titleLabel = new Label("Forgot Password?");
        titleLabel.setStyle("-fx-font-size: 18px; -fx-text-fill: white; -fx-font-weight: bold;");

        TextField usernameField = new TextField();
        usernameField.setPromptText("Enter your username or email");
        usernameField.setMaxWidth(250);

        Label messageLabel = new Label();
        messageLabel.setStyle("-fx-text-fill: white;");

        // ✅ Send OTP Button
        Button sendOTPButton = new Button("Send OTP");
        sendOTPButton.setOnAction(e -> {
            String usernameOrEmail = usernameField.getText().trim();

            if (usernameOrEmail.isEmpty()) {
                messageLabel.setText("❌ Please enter your username or email.");
                return;
            }

            // ✅ Simulating OTP Sent Message
            messageLabel.setText("✅ OTP sent to your email.");
        });

        Button backButton = new Button("← Back to Login");
        backButton.setOnAction(e -> new UserLoginPage(databaseHelper).show(primaryStage));

        // ✅ Final UI (Only Input, Send OTP, and Back Button)
        layout.getChildren().addAll(titleLabel, usernameField, sendOTPButton, messageLabel, backButton);

        primaryStage.setScene(new Scene(layout, 400, 300));
        primaryStage.setTitle("Forgot Password");
        primaryStage.show();
    }
}

    
