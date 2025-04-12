package application;

import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import databasePart1.DatabaseHelper;

public class UserHomePage {

    private final DatabaseHelper databaseHelper;
    private final User user;

    public UserHomePage(DatabaseHelper databaseHelper, User user) {
        this.databaseHelper = databaseHelper;
        this.user = user;
    }

    public void show(Stage primaryStage) {
        VBox layout = new VBox(20);
        layout.setStyle("-fx-alignment: center; -fx-padding: 40; -fx-background-color: black;");

        Label userLabel = new Label("Hello, " + user.getUserName() + "!");
        userLabel.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: white;");

        Label roleLabel = new Label("Role: " + user.getRole());
        roleLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #cccccc;");

        Button qaButton = new Button("Go to My ED Discussion");
        qaButton.setStyle("-fx-font-size: 16px; -fx-padding: 12px 24px; -fx-background-color: green; -fx-text-fill: white;");
        qaButton.setOnAction(e -> new QAPage(databaseHelper, user).show(primaryStage));

        // ✅ Student Review Page button
        Button studentReviewButton = null;
        if (user.getRole().equalsIgnoreCase("student")) {
            studentReviewButton = new Button("📝 View Reviews");
            studentReviewButton.setStyle("-fx-font-size: 14px; -fx-padding: 10px 20px; -fx-background-color: #007acc; -fx-text-fill: white;");
            studentReviewButton.setOnAction(e -> new StudentReviewPage(primaryStage, user.getUserName()).show());
        }

        // ✅ My Reviews Page button for reviewers
        Button reviewerButton = null;
        if (user.getRole().equalsIgnoreCase("reviewer")) {
            reviewerButton = new Button("📋 My Reviews");
            reviewerButton.setStyle("-fx-font-size: 14px; -fx-padding: 10px 20px; -fx-background-color: darkblue; -fx-text-fill: white;");
            reviewerButton.setOnAction(e -> new MyReviewsPage(primaryStage, user.getUserName()).show());
        }

        Button messagesButton = new Button("📨 Private Messages");
        messagesButton.setStyle("-fx-font-size: 14px; -fx-padding: 10px 20px; -fx-background-color: #2d2d2d; -fx-text-fill: white;");
        messagesButton.setOnAction(e -> new PrivateMessagesPage(primaryStage, user.getUserName()).show());

        Button backButton = new Button("⬅ Back to Welcome Page");
        backButton.setStyle("-fx-font-size: 14px; -fx-padding: 10px 20px; -fx-background-color: #555555; -fx-text-fill: white;");
        backButton.setOnAction(e -> new WelcomeLoginPage(databaseHelper).show(primaryStage, user));

        layout.getChildren().addAll(userLabel, roleLabel, qaButton);

        if (studentReviewButton != null) {
            layout.getChildren().add(studentReviewButton);
        }

        if (reviewerButton != null) {
            layout.getChildren().add(reviewerButton);
        }

        // Add Flagged Items Dashboard button for staff
        Button flaggedItemsButton = null;
        if (user.getRole().equalsIgnoreCase("staff")) {
            flaggedItemsButton = new Button("🚩 Flagged Items");
            flaggedItemsButton.setStyle("-fx-font-size: 14px; -fx-padding: 10px 20px; -fx-background-color: #ff4444; -fx-text-fill: white;");
            flaggedItemsButton.setOnAction(e -> new FlaggedItemDashboard(user.getUserName()).start(new Stage()));
        }

        if (flaggedItemsButton != null) {
            layout.getChildren().add(flaggedItemsButton);
        }

        layout.getChildren().addAll(messagesButton, backButton);

        Scene userScene = new Scene(layout, 800, 400);
        primaryStage.setScene(userScene);
        primaryStage.setTitle("User Page");
        primaryStage.show();
    }
}
