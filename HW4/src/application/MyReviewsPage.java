package application;

import databasePart1.DatabaseHelper;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

public class MyReviewsPage {
    private final Stage stage;
    private final String currentUser;
    private final DatabaseHelper dbHelper;

    public MyReviewsPage(Stage stage, String currentUser) {
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
        VBox root = new VBox(15);
        root.setPadding(new Insets(15));
        root.setAlignment(Pos.TOP_CENTER);

        Label title = new Label("My Reviews");
        title.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");

        VBox reviewList = new VBox(10);
        reviewList.setPadding(new Insets(10));
        reviewList.setStyle("-fx-border-color: gray; -fx-border-width: 1; -fx-background-color: #f8f8f8;");
        refreshReviews(reviewList);

        Button backButton = new Button("⬅ Back");
        backButton.setOnAction(e -> {
            try {
                User user = dbHelper.getUserByUsername(currentUser);
                new UserHomePage(dbHelper, user).show(stage);
            } catch (SQLException ex) {
                showAlert("Error", "User not found.");
            }
        });

        root.getChildren().addAll(title, reviewList, backButton);

        Scene scene = new Scene(root, 800, 600);
        stage.setScene(scene);
        stage.setTitle("My Reviews");
        stage.show();
    }

    private void refreshReviews(VBox reviewList) {
        reviewList.getChildren().clear();
        try {
            List<Map<String, Object>> reviews = dbHelper.getAllReviewsByReviewer(currentUser);
            if (reviews.isEmpty()) {
                Label noReviews = new Label("You have not written any reviews yet.");
                noReviews.setStyle("-fx-font-style: italic;");
                reviewList.getChildren().add(noReviews);
            } else {
                for (Map<String, Object> review : reviews) {
                    VBox card = new VBox(5);
                    card.setStyle("-fx-border-color: gray; -fx-border-width: 1; -fx-padding: 10; -fx-background-color: white;");

                    Label questionIdLabel = new Label("Question ID: " + review.get("question_id"));
                    Label ratingLabel = new Label("Rating: " + review.get("rating") + "/5");
                    Label reviewTextLabel = new Label("Review: " + review.get("review_text"));
                    Label dateLabel = new Label("Date: " + review.get("timestamp"));

                    // Edit and Delete buttons
                    Button editBtn = new Button("Edit");
                    Button deleteBtn = new Button("Delete");

                    HBox actionRow = new HBox(10, editBtn, deleteBtn);
                    actionRow.setAlignment(Pos.CENTER_LEFT);

                    // Edit logic
                    editBtn.setOnAction(e -> {
                        TextInputDialog dialog = new TextInputDialog((String) review.get("review_text"));
                        dialog.setTitle("Edit Review");
                        dialog.setHeaderText("Edit your review for Question ID: " + review.get("question_id"));
                        dialog.setContentText("New Review:");

                        dialog.showAndWait().ifPresent(newText -> {
                            if (!newText.trim().isEmpty()) {
                                try {
                                    dbHelper.updateReview((int) review.get("id"), newText);
                                    showAlert("Success", "Review updated.");
                                    refreshReviews(reviewList);
                                } catch (SQLException ex) {
                                    showAlert("Error", "Update failed: " + ex.getMessage());
                                }
                            }
                        });
                    });

                    // Delete logic
                    deleteBtn.setOnAction(e -> {
                        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
                        confirm.setHeaderText("Are you sure you want to delete this review?");
                        confirm.showAndWait().ifPresent(response -> {
                            if (response == ButtonType.OK) {
                                try {
                                    dbHelper.deleteReview((int) review.get("id"));
                                    showAlert("Deleted", "Review deleted.");
                                    refreshReviews(reviewList);
                                } catch (SQLException ex) {
                                    showAlert("Error", "Delete failed: " + ex.getMessage());
                                }
                            }
                        });
                    });

                    card.getChildren().addAll(questionIdLabel, ratingLabel, reviewTextLabel, dateLabel, actionRow);
                    reviewList.getChildren().add(card);
                }
            }
        } catch (SQLException e) {
            showAlert("Error", "Failed to load reviews: " + e.getMessage());
        }
    }

    private void showAlert(String title, String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}
