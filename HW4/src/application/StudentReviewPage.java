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
import java.util.Map;

public class StudentReviewPage {
    private Stage stage;
    private DatabaseHelper dbHelper;
    private String currentUser;

    public StudentReviewPage(Stage stage, String currentUser) {
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

        Label headerLabel = new Label("Student Review Dashboard");
        headerLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");

        // ✅ Trusted-only checkbox
        CheckBox trustedOnlyCheckBox = new CheckBox("Show only reviews from trusted reviewers");

        // Question list
        ListView<Question> questionList = new ListView<>();
        try {
            questionList.getItems().addAll(dbHelper.getAllQuestions());
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // Reviews display
        VBox reviewsBox = new VBox(5);
        Label reviewsLabel = new Label("Reviews");
        reviewsLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
        reviewsBox.getChildren().add(reviewsLabel);

        // Update reviews on question selection or filter toggle
        questionList.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
            if (newSel != null) {
                updateReviewsDisplay(reviewsBox, reviewsLabel, newSel.getId(), trustedOnlyCheckBox.isSelected());
            }
        });

        trustedOnlyCheckBox.setOnAction(e -> {
            Question selected = questionList.getSelectionModel().getSelectedItem();
            if (selected != null) {
                updateReviewsDisplay(reviewsBox, reviewsLabel, selected.getId(), trustedOnlyCheckBox.isSelected());
            }
        });

        // Trusted Reviewers Management
        VBox trustedReviewersBox = new VBox(5);
        Label trustedLabel = new Label("Trusted Reviewers");
        trustedLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        ListView<String> trustedReviewersList = new ListView<>();
        try {
            trustedReviewersList.getItems().addAll(dbHelper.getTrustedReviewers(currentUser));
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // Reviewer weight controls
        HBox weightControls = new HBox(10);
        TextField reviewerField = new TextField();
        reviewerField.setPromptText("Reviewer username");
        Slider weightSlider = new Slider(0.1, 2.0, 1.0);
        weightSlider.setShowTickLabels(true);
        weightSlider.setShowTickMarks(true);
        Button setWeightButton = new Button("Set Weight");

        setWeightButton.setOnAction(e -> {
            try {
                dbHelper.setReviewerWeight(currentUser, reviewerField.getText(), (float) weightSlider.getValue());
                showAlert("Success", "Reviewer weight updated successfully!");
                reviewerField.clear();
                weightSlider.setValue(1.0);
            } catch (SQLException ex) {
                showAlert("Error", "Failed to update reviewer weight: " + ex.getMessage());
            }
        });

        weightControls.getChildren().addAll(
            new Label("Reviewer:"),
            reviewerField,
            new Label("Weight:"),
            weightSlider,
            setWeightButton
        );

        // Request Reviewer Permission
        Button requestPermissionButton = new Button("Request Reviewer Permission");
        requestPermissionButton.setOnAction(e -> {
            try {
                if (dbHelper.requestReviewerPermission(currentUser)) {
                    showAlert("Success", "Reviewer permission request submitted!");
                } else {
                    showAlert("Error", "Failed to submit reviewer permission request");
                }
            } catch (SQLException ex) {
                showAlert("Error", "Error submitting request: " + ex.getMessage());
            }
        });

        // Back button
        Button backButton = new Button("Back to Home");
        backButton.setOnAction(e -> {
            try {
                User user = dbHelper.getUserByUsername(currentUser);
                if (user != null) {
                    UserHomePage homePage = new UserHomePage(dbHelper, user);
                    homePage.show(stage);
                } else {
                    showAlert("Error", "User not found.");
                }
            } catch (SQLException ex) {
                showAlert("Error", "Database error: " + ex.getMessage());
            }
        });

        trustedReviewersBox.getChildren().addAll(
            trustedLabel,
            trustedReviewersList,
            weightControls,
            requestPermissionButton
        );

        root.getChildren().addAll(
            headerLabel,
            trustedOnlyCheckBox, // ✅ Added filter
            new Label("Questions and Reviews:"),
            questionList,
            reviewsBox,
            trustedReviewersBox,
            backButton
        );

        Scene scene = new Scene(root, 800, 1000);
        stage.setScene(scene);
        stage.setTitle("Student Review Dashboard");
        stage.show();
    }

    private void updateReviewsDisplay(VBox reviewsBox, Label reviewsLabel, int questionId, boolean trustedOnly) {
        reviewsBox.getChildren().clear();
        reviewsBox.getChildren().add(reviewsLabel);
        try {
            List<Map<String, Object>> reviews = dbHelper.getReviewsForQuestion(questionId);
            List<String> trusted = dbHelper.getTrustedReviewers(currentUser);

            boolean found = false;
            for (Map<String, Object> review : reviews) {
                String reviewer = (String) review.get("reviewer_name");
                if (!trustedOnly || trusted.contains(reviewer)) {
                    VBox reviewBox = new VBox(5);
                    reviewBox.setStyle("-fx-border-color: gray; -fx-border-width: 1; -fx-padding: 5;");
                    reviewBox.getChildren().addAll(
                        new Label("Reviewer: " + reviewer),
                        new Label("Rating: " + review.get("rating") + "/5"),
                        new Label((String) review.get("review_text")),
                        new Label("Date: " + review.get("timestamp"))
                    );
                    reviewsBox.getChildren().add(reviewBox);
                    found = true;
                }
            }

            if (!found) {
                Label noReviewsLabel = new Label("No reviews found for this question.");
                noReviewsLabel.setStyle("-fx-font-style: italic;");
                reviewsBox.getChildren().add(noReviewsLabel);
            }
        } catch (SQLException e) {
            showAlert("Error", "Failed to load reviews: " + e.getMessage());
        }
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
