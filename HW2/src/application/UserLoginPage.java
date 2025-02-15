package application;

import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import java.sql.SQLException;
import databasePart1.*;

public class UserLoginPage {

    private final DatabaseHelper databaseHelper;

    public UserLoginPage(DatabaseHelper databaseHelper) {
        this.databaseHelper = databaseHelper;
    }

    public void show(Stage primaryStage) {
        // ✅ Set Layout with Black Background
        VBox layout = new VBox(15);
        layout.setStyle("-fx-alignment: center; -fx-padding: 30; -fx-background-color: #000000;");

        // ✅ Styled Label for Title
        Label titleLabel = new Label("User Login");
        titleLabel.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: white;");

        // ✅ Input Fields
        TextField userNameField = createStyledTextField("Enter Username");
        PasswordField passwordField = createStyledPasswordField("Enter Password");

        // ✅ Styled Error Label (Red Text)
        Label errorLabel = new Label();
        errorLabel.setStyle("-fx-text-fill: red; -fx-font-size: 12px;");

        // ✅ Styled Buttons
        Button loginButton = createStyledButton("Login");
        Button backButton = createStyledButton("← Back to Main Page");

        // ✅ Styled Hyperlinks
        Hyperlink createAccountLink = createStyledHyperlink("Create New Account");
        Hyperlink forgotPasswordLink = createStyledHyperlink("Forgot Password?");

        // ✅ Login Button Action (Without OTP)
        loginButton.setOnAction(a -> {
            String userName = userNameField.getText();
            String password = passwordField.getText();  

            try {
                String roles = databaseHelper.getUserRole(userName); // ✅ Fetch roles from DB

                if (roles != null) {
                    User user = new User(userName, password, roles);

                    if (databaseHelper.login(user)) {
                        // ✅ If user has multiple roles, show selection dialog
                        if (roles.contains(",")) {
                            showRoleSelectionDialog(userName, roles, primaryStage);
                        } else {
                            new WelcomeLoginPage(databaseHelper).show(primaryStage, user);
                        }
                    } else {
                        errorLabel.setText("Incorrect username or password.");
                    }
                } else {
                    errorLabel.setText("User does not exist. Please create an account.");
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });


        // ✅ Navigation Actions
        backButton.setOnAction(e -> new SetupLoginSelectionPage(databaseHelper).show(primaryStage));
        createAccountLink.setOnAction(e -> new SetupAccountPage(databaseHelper).show(primaryStage));
        forgotPasswordLink.setOnAction(e -> new ForgotPasswordPage(databaseHelper).show(primaryStage));

        // ✅ Add Components to Layout
        layout.getChildren().addAll(titleLabel, userNameField, passwordField, loginButton, errorLabel, createAccountLink, forgotPasswordLink, backButton);

        // ✅ Set Scene
        Scene scene = new Scene(layout, 800, 400);
        primaryStage.setScene(scene);
        primaryStage.setTitle("User Login");
        primaryStage.show();
    }

    // ✅ Helper Method for Creating Styled Buttons
    private Button createStyledButton(String text) {
        Button button = new Button(text);
        button.setMinWidth(200);
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

    // ✅ Helper Method for Styled Text Fields
    private TextField createStyledTextField(String placeholder) {
        TextField textField = new TextField();
        textField.setPromptText(placeholder);
        textField.setMaxWidth(250);
        textField.setStyle(
            "-fx-background-color: #333333; " +
            "-fx-text-fill: white; " +
            "-fx-font-size: 14px; " +
            "-fx-background-radius: 5px; " +
            "-fx-padding: 8px;"
        );
        return textField;
    }

    // ✅ Helper Method for Styled Password Fields
    private PasswordField createStyledPasswordField(String placeholder) {
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText(placeholder);
        passwordField.setMaxWidth(250);
        passwordField.setStyle(
            "-fx-background-color: #333333; " +
            "-fx-text-fill: white; " +
            "-fx-font-size: 14px; " +
            "-fx-background-radius: 5px; " +
            "-fx-padding: 8px;"
        );
        return passwordField;
    }

    // ✅ Helper Method for Styled Hyperlinks
    private Hyperlink createStyledHyperlink(String text) {
        Hyperlink hyperlink = new Hyperlink(text);
        hyperlink.setStyle(
            "-fx-font-size: 14px; " +
            "-fx-text-fill: lightblue;"
        );
        return hyperlink;
    }
 // ✅ Show Role Selection Dialog if User Has Multiple Roles
    private void showRoleSelectionDialog(String userName, String roles, Stage primaryStage) {
        Stage roleStage = new Stage();
        roleStage.setTitle("Select Your Role");

        VBox layout = new VBox(10);
        layout.setStyle("-fx-padding: 20; -fx-alignment: center; -fx-background-color: black;");

        Label instructionLabel = new Label("Select the role you want to use:");
        instructionLabel.setStyle("-fx-text-fill: white; -fx-font-size: 14px;");

        ToggleGroup roleGroup = new ToggleGroup();
        
        VBox roleButtons = new VBox(5);
        for (String role : roles.split(",")) {
            RadioButton roleOption = new RadioButton(role.trim());
            roleOption.setToggleGroup(roleGroup);
            roleOption.setStyle("-fx-text-fill: white;");
            roleButtons.getChildren().add(roleOption);
        }

        Button confirmButton = new Button("Confirm");
        confirmButton.setStyle("-fx-font-size: 14px; -fx-background-color: #4CAF50; -fx-text-fill: white; -fx-background-radius: 5px; -fx-padding: 10px;");

        confirmButton.setOnAction(e -> {
            RadioButton selectedRole = (RadioButton) roleGroup.getSelectedToggle();
            if (selectedRole != null) {
                String chosenRole = selectedRole.getText();
                User user = new User(userName, "", chosenRole);
                new WelcomeLoginPage(databaseHelper).show(primaryStage, user);
                roleStage.close();
            }
        });

        layout.getChildren().addAll(instructionLabel, roleButtons, confirmButton);
        Scene scene = new Scene(layout, 300, 250);
        roleStage.setScene(scene);
        roleStage.show();
    }

}
