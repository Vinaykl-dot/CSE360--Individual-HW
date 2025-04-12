package application;

import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import java.sql.SQLException;
import databasePart1.*;

public class SetupAccountPage {
    
    private final DatabaseHelper databaseHelper;

    public SetupAccountPage(DatabaseHelper databaseHelper) {
        this.databaseHelper = databaseHelper;
    }

    public void show(Stage primaryStage) {
        VBox layout = new VBox(15);
        layout.setStyle("-fx-alignment: center; -fx-padding: 30; -fx-background-color: #000000;");

        Label titleLabel = new Label("Account Setup");
        titleLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: white;");

        TextField userNameField = createStyledTextField("Enter Username");
        TextField emailField = createStyledTextField("Enter Email");
        PasswordField passwordField = createStyledPasswordField("Enter Password");
        TextField inviteCodeField = createStyledTextField("Enter Invitation Code");

        Label errorLabel = new Label();
        errorLabel.setStyle("-fx-text-fill: red; -fx-font-size: 14px;");

        Button setupButton = createStyledButton("Setup");
        Button backButton = createStyledButton("← Back to Main Page");

        // ✅ Setup Button Action
        setupButton.setOnAction(a -> {
            String userName = userNameField.getText();
            String email = emailField.getText();
            String password = passwordField.getText();
            String code = inviteCodeField.getText();

            try {
                if (!email.contains("@")) {  
                    errorLabel.setText("Invalid email format.");
                    return;
                }

                String userNameValidationError = UserNameRecognizer.checkForValidUserName(userName);
                if (!userNameValidationError.isEmpty()) {
                    errorLabel.setText(userNameValidationError);
                    return;
                }

                String passwordValidationError = PasswordEvaluator.evaluatePassword(password);
                if (!passwordValidationError.isEmpty()) {
                    errorLabel.setText(passwordValidationError);
                    return;
                }

                if (!databaseHelper.doesUserExist(userName)) {

                    // ✅ First user should be an admin
                    boolean isDatabaseEmpty = databaseHelper.isDatabaseEmpty();
                    String role = isDatabaseEmpty ? "admin" : "user";

                    // ✅ Users (not admin) require an invitation code
                    if (!isDatabaseEmpty && !databaseHelper.validateInvitationCode(code)) {
                        errorLabel.setText("Invalid invitation code.");
                        return;
                    }

                    // ✅ Register User
                    User user = new User(userName, password, role, email);  
                    databaseHelper.register(user);

                    // ✅ Redirect to Welcome Page
                    new WelcomeLoginPage(databaseHelper).show(primaryStage, user);
                } else {
                    errorLabel.setText("Username already taken.");
                }

            } catch (SQLException e) {
                e.printStackTrace();
                errorLabel.setText("Database error.");
            }
        });

        // ✅ Back Button Action
        backButton.setOnAction(e -> {
            SetupLoginSelectionPage selectionPage = new SetupLoginSelectionPage(databaseHelper);
            selectionPage.show(primaryStage);
        });

        // ✅ Only show invite code field if users exist
        try {
            if (databaseHelper.isDatabaseEmpty()) {
                inviteCodeField.setVisible(false);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        layout.getChildren().addAll(titleLabel, userNameField, emailField, passwordField, inviteCodeField, setupButton, errorLabel, backButton);

        Scene scene = new Scene(layout, 800, 500);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Account Setup");
        primaryStage.show();
    }

    // ✅ Styled Buttons
    private Button createStyledButton(String text) {
        Button button = new Button(text);
        button.setMinWidth(250);
        button.setMinHeight(40);
        button.setStyle(
            "-fx-font-size: 14px; " +
            "-fx-background-color: #555555; " +
            "-fx-text-fill: white; " +
            "-fx-background-radius: 5px; " +
            "-fx-padding: 10px;"
        );
        button.setOnMouseEntered(e -> button.setStyle(
            "-fx-font-size: 14px; " +
            "-fx-background-color: #888888; " +
            "-fx-text-fill: white; " +
            "-fx-background-radius: 5px; " +
            "-fx-padding: 10px;"
        ));
        button.setOnMouseExited(e -> button.setStyle(
            "-fx-font-size: 14px; " +
            "-fx-background-color: #555555; " +
            "-fx-text-fill: white; " +
            "-fx-background-radius: 5px; " +
            "-fx-padding: 10px;"
        ));
        return button;
    }

    // ✅ Styled Text Fields
    private TextField createStyledTextField(String placeholder) {
        TextField textField = new TextField();
        textField.setPromptText(placeholder);
        textField.setMaxWidth(300);
        textField.setStyle(
            "-fx-background-color: #333333; " +
            "-fx-text-fill: white; " +
            "-fx-font-size: 14px; " +
            "-fx-background-radius: 5px; " +
            "-fx-padding: 8px;"
        );
        return textField;
    }

    // ✅ Styled Password Fields
    private PasswordField createStyledPasswordField(String placeholder) {
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText(placeholder);
        passwordField.setMaxWidth(300);
        passwordField.setStyle(
            "-fx-background-color: #333333; " +
            "-fx-text-fill: white; " +
            "-fx-font-size: 14px; " +
            "-fx-background-radius: 5px; " +
            "-fx-padding: 8px;"
        );
        return passwordField;
    }
}
