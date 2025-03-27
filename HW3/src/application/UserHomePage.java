package application;

import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import databasePart1.DatabaseHelper;

public class UserHomePage {
    
    private final DatabaseHelper databaseHelper; // ✅ Store databaseHelper
    private final User user; // ✅ Store user

    public UserHomePage(DatabaseHelper databaseHelper, User user) {
        this.databaseHelper = databaseHelper; // ✅ Assign databaseHelper
        this.user = user; // ✅ Assign user
    }

    public void show(Stage primaryStage) {
        VBox layout = new VBox(20);  // ✅ Added spacing for better UI
        layout.setStyle("-fx-alignment: center; -fx-padding: 40; -fx-background-color: black;");

        // ✅ Label to display Hello user
        Label userLabel = new Label("Hello, " + user.getUserName() + "!");
        userLabel.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: white;");

        // ✅ "Go to Q&A System" Button for Students Only
        Button qaButton = new Button("Go to My ED Discussion");
        qaButton.setStyle("-fx-font-size: 16px; -fx-padding: 12px 24px; -fx-background-color: green; -fx-text-fill: white;");
        qaButton.setOnAction(e -> new QAPage(databaseHelper, user).show(primaryStage));

        // ✅ Back Button to return to Welcome Page
        Button backButton = new Button("⬅ Back to Welcome Page");
        backButton.setStyle("-fx-font-size: 14px; -fx-padding: 10px 20px; -fx-background-color: #555555; -fx-text-fill: white;");
        backButton.setOnAction(e -> new WelcomeLoginPage(databaseHelper).show(primaryStage, user)); 

        // ✅ Add Components to Layout
        layout.getChildren().addAll(userLabel, qaButton, backButton);

        Scene userScene = new Scene(layout, 800, 400);
        primaryStage.setScene(userScene);
        primaryStage.setTitle("User Page");
        primaryStage.show();
    }
}
