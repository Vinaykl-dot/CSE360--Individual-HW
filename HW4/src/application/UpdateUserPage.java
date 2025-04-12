package application;

import databasePart1.DatabaseHelper;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import java.sql.SQLException;

public class UpdateUserPage {

    private final DatabaseHelper databaseHelper;
    private final String userName;

    public UpdateUserPage(DatabaseHelper databaseHelper, String userName) {
        this.databaseHelper = databaseHelper;
        this.userName = userName;
    }

    public void show(Stage primaryStage) {
        VBox layout = new VBox(15);
        layout.setStyle("-fx-alignment: center; -fx-padding: 30; -fx-background-color: black;");

        Label titleLabel = new Label("Update Your Information");
        titleLabel.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: white;");

        // ✅ Buttons for choosing what to update
        Button changeUsernameButton = createStyledButton("Change Username");
        Button changeEmailButton = createStyledButton("Change Email");
        Button changePasswordButton = createStyledButton("Change Password");
        Button backButton = createStyledButton("⬅ Back");

        // ✅ Open respective pages for updates
        changeUsernameButton.setOnAction(e -> updateField(primaryStage, "username"));
        changeEmailButton.setOnAction(e -> updateField(primaryStage, "email"));
        changePasswordButton.setOnAction(e -> updateField(primaryStage, "password"));
        backButton.setOnAction(e -> new WelcomeLoginPage(databaseHelper).show(primaryStage, new User(userName, "", databaseHelper.getUserRole(userName))));

        layout.getChildren().addAll(titleLabel, changeUsernameButton, changeEmailButton, changePasswordButton, backButton);

        Scene scene = new Scene(layout, 400, 350);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Update User Info");
        primaryStage.show();
    }

    private void updateField(Stage primaryStage, String field) {
        VBox layout = new VBox(15);
        layout.setStyle("-fx-alignment: center; -fx-padding: 30; -fx-background-color: black;");

        Label instructionLabel = new Label("Enter your current password to proceed:");
        instructionLabel.setStyle("-fx-text-fill: white; -fx-font-size: 14px;");

        PasswordField currentPasswordField = createStyledPasswordField("Enter Current Password");
        TextField newFieldInput = createStyledTextField("");

        Label newFieldLabel = new Label();
        newFieldLabel.setStyle("-fx-text-fill: white; -fx-font-size: 14px;");

        if (field.equals("username")) {
            newFieldLabel.setText("Enter New Username:");
            newFieldInput.setPromptText("New Username");
        } else if (field.equals("email")) {
            newFieldLabel.setText("Enter New Email:");
            newFieldInput.setPromptText("New Email");
        } else {
            newFieldLabel.setText("Enter New Password:");
            newFieldInput.setPromptText("New Password");
        }

        Label errorLabel = new Label();
        errorLabel.setStyle("-fx-text-fill: red; -fx-font-size: 14px;");

        Button submitButton = createStyledButton("Submit");
        Button backButton = createStyledButton("⬅ Back");

        String role = databaseHelper.getUserRole(userName); // ✅ Fetch role once
        System.out.println("User Role Retrieved Before Going Back: " + role); // Debugging

        backButton.setOnAction(e -> new WelcomeLoginPage(databaseHelper).show(primaryStage, new User(userName, "", role)));


        submitButton.setOnAction(e -> {
            String currentPassword = currentPasswordField.getText();
            String newFieldValue = newFieldInput.getText().trim();

            try {
                // ✅ Validate user before updating
                User user = new User(userName, currentPassword, databaseHelper.getUserRole(userName));
                if (!databaseHelper.login(user)) {
                    errorLabel.setText("❌ Incorrect password! Please try again.");
                    return;
                }

                boolean success = false;

                if (field.equals("username")) {
                    success = databaseHelper.updateUsername(userName, newFieldValue);
                } else if (field.equals("email")) {
                    success = databaseHelper.updateEmail(userName, newFieldValue);
                } else {
                    // ✅ Validate password using PasswordEvaluator
                    String passwordError = PasswordEvaluator.evaluatePassword(newFieldValue);
                    if (!passwordError.isEmpty()) {
                        errorLabel.setText("❌ " + passwordError);
                        return;
                    }
                    success = databaseHelper.updatePassword(userName, newFieldValue);
                }

                if (success) {
                    errorLabel.setStyle("-fx-text-fill: green;");
                    errorLabel.setText("✅ Update successful!");
                } else {
                    errorLabel.setText("❌ Update failed. Try again.");
                }
            } catch (SQLException ex) {
                errorLabel.setText("❌ Database error.");
                ex.printStackTrace();
            }
        });

        layout.getChildren().addAll(instructionLabel, currentPasswordField, newFieldLabel, newFieldInput, submitButton, errorLabel, backButton);

        Scene scene = new Scene(layout, 400, 400);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Update " + field);
        primaryStage.show();
    }

    // ✅ Styled Buttons with Curved Edges
    private Button createStyledButton(String text) {
        Button button = new Button(text);
        button.setMinWidth(250);
        button.setStyle(
            "-fx-font-size: 14px; " +
            "-fx-background-color: #555555; " +
            "-fx-text-fill: white; " +
            "-fx-background-radius: 10px; " +  // ✅ Curved edges
            "-fx-padding: 10px;"
        );
        button.setOnMouseEntered(e -> button.setStyle(
            "-fx-font-size: 14px; " +
            "-fx-background-color: #888888; " +
            "-fx-text-fill: white; " +
            "-fx-background-radius: 10px; " +
            "-fx-padding: 10px;"
        ));
        button.setOnMouseExited(e -> button.setStyle(
            "-fx-font-size: 14px; " +
            "-fx-background-color: #555555; " +
            "-fx-text-fill: white; " +
            "-fx-background-radius: 10px; " +
            "-fx-padding: 10px;"
        ));
        return button;
    }

    // ✅ Styled TextFields with Curved Edges
    private TextField createStyledTextField(String placeholder) {
        TextField textField = new TextField();
        textField.setPromptText(placeholder);
        textField.setMaxWidth(250);
        textField.setStyle(
            "-fx-background-color: #333333; " +
            "-fx-text-fill: white; " +
            "-fx-font-size: 14px; " +
            "-fx-background-radius: 10px; " +  // ✅ Curved edges
            "-fx-padding: 8px;"
        );
        return textField;
    }

    // ✅ Styled PasswordField with Curved Edges
    private PasswordField createStyledPasswordField(String placeholder) {
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText(placeholder);
        passwordField.setMaxWidth(250);
        passwordField.setStyle(
            "-fx-background-color: #333333; " +
            "-fx-text-fill: white; " +
            "-fx-font-size: 14px; " +
            "-fx-background-radius: 10px; " +  // ✅ Curved edges
            "-fx-padding: 8px;"
        );
        return passwordField;
    }
}
