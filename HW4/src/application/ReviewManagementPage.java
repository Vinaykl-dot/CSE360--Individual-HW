package application;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import databasePart1.DatabaseHelper;
import java.sql.SQLException;
import java.util.List;

public class ReviewManagementPage {
    private Stage stage;
    private DatabaseHelper dbHelper;
    private String currentUser;

    public ReviewManagementPage(Stage stage, String currentUser) {
        this.stage = stage;
        this.currentUser = currentUser;
        this.dbHelper = new DatabaseHelper();
        try {
            dbHelper.connectToDatabase();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void show() {
        VBox root = new VBox(10);
        root.setPadding(new Insets(10));
        root.setAlignment(Pos.CENTER);

        // Header
        Label headerLabel = new Label("Review Management");
        headerLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");

        // Question List
        ListView<Question> questionList = new ListView<>();
        try {
            questionList.getItems().addAll(dbHelper.getAllQuestions());
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // Review Form
        VBox reviewForm = new VBox(10);
        TextArea reviewText = new TextArea();
        reviewText.setPromptText("Enter your review");
        reviewText.setPrefRowCount(3);

        Slider ratingSlider = new Slider(1, 5, 3);
        ratingSlider.setShowTickLabels(true);
        ratingSlider.setShowTickMarks(true);
        ratingSlider.setMajorTickUnit(1);
        ratingSlider.setBlockIncrement(1);
        ratingSlider.setSnapToTicks(true);

        Button submitReview = new Button("Submit Review");
        submitReview.setOnAction(e -> {
            Question selectedQuestion = questionList.getSelectionModel().getSelectedItem();
            if (selectedQuestion != null) {
                try {
                    dbHelper.addReview(
                        selectedQuestion.getId(), // ✅ Assumes Question has an ID now
                        -1, // Not linking to a specific answer
                        currentUser,
                        reviewText.getText(),
                        (int) ratingSlider.getValue()
                    );
                    showAlert("Success", "Review submitted successfully!");
                    reviewText.clear();
                    ratingSlider.setValue(3);
                } catch (SQLException ex) {
                    showAlert("Error", "Failed to submit review: " + ex.getMessage());
                }
            } else {
                showAlert("Error", "Please select a question to review");
            }
        });

        // ✅ Back Button - Returns to User Home Page
        Button backButton = new Button("Back to Home");
        backButton.setOnAction(e -> {
            try {
                User user = dbHelper.getUserByUsername(currentUser);
                if (user != null) {
                    UserHomePage homePage = new UserHomePage(dbHelper, user);
                    homePage.show(stage); // ✅ Pass stage into show()
                } else {
                    showAlert("Error", "User not found in database.");
                }
            } catch (SQLException ex) {
                showAlert("Error", "Database error: " + ex.getMessage());
            }
        });

        // Layout
        reviewForm.getChildren().addAll(
            new Label("Review Text:"),
            reviewText,
            new Label("Rating:"),
            ratingSlider,
            submitReview
        );

        root.getChildren().addAll(
            headerLabel,
            new Label("Select a Question to Review:"),
            questionList,
            reviewForm,
            backButton
        );

        Scene scene = new Scene(root, 600, 800);
        stage.setScene(scene);
        stage.setTitle("Review Management");
        stage.show();
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
