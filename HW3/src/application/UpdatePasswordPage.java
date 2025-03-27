package application;

import databasePart1.DatabaseHelper;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import java.sql.SQLException;

public class UpdatePasswordPage {
    private final DatabaseHelper databaseHelper;
    private final String userName;

    public UpdatePasswordPage(DatabaseHelper databaseHelper, String userName) {
        this.databaseHelper = databaseHelper;
        this.userName = userName;
    }

    public void show(Stage primaryStage) {
        VBox layout = new VBox(10);
        layout.setStyle("-fx-padding: 20; -fx-alignment: center; -fx-background-color: black;");

        Label titleLabel = new Label("Reset Your Password");
        titleLabel.setStyle("-fx-font-size: 18px; -fx-text-fill: white; -fx-font-weight: bold;");

        PasswordField newPasswordField = new PasswordField();
        newPasswordField.setPromptText("Enter new password");
        newPasswordField.setMaxWidth(250);

        Label messageLabel = new Label();
        messageLabel.setStyle("-fx-text-fill: white;");

        Button updateButton = new Button("Update Password");
        updateButton.setOnAction(e -> {
            String newPassword = newPasswordField.getText();

            String validationError = PasswordEvaluator.evaluatePassword(newPassword);
            if (!validationError.isEmpty()) {
                messageLabel.setText(validationError);
                return;
            }

            try {
                databaseHelper.updatePassword(userName, newPassword);
                messageLabel.setText("Password updated successfully! Please log in.");
            } catch (SQLException ex) {
                ex.printStackTrace();
                messageLabel.setText("Database error.");
            }
        });

        Button backButton = new Button("Back to Login");
        backButton.setOnAction(e -> new UserLoginPage(databaseHelper).show(primaryStage));

        layout.getChildren().addAll(titleLabel, newPasswordField, updateButton, messageLabel, backButton);
        primaryStage.setScene(new Scene(layout, 400, 300));
        primaryStage.setTitle("Reset Password");
        primaryStage.show();
    }
}
