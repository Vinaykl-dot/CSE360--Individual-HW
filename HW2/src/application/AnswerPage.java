package application;

import databasePart1.DatabaseHelper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import java.sql.SQLException;
import java.util.List;

public class AnswerPage {
    private final DatabaseHelper databaseHelper;
    private final User user;
    private final String question;
    private final ObservableList<String> answerList = FXCollections.observableArrayList();

    public AnswerPage(DatabaseHelper databaseHelper, User user, String question) {
        this.databaseHelper = databaseHelper;
        this.user = user;
        this.question = question;
    }

    public void show(Stage primaryStage) {
        VBox layout = new VBox(10);
        layout.setStyle("-fx-padding: 20; -fx-alignment: center; -fx-background-color: black;");

        Label titleLabel = new Label("Answers for: " + question);
        titleLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: white; -fx-font-weight: bold;");

        // ✅ Answer List View
        ListView<String> answerListView = new ListView<>(answerList);
        answerListView.setPrefHeight(250);
        loadAnswers();

        // ✅ Text Field for New Answer
        TextField newAnswerField = new TextField();
        newAnswerField.setPromptText("Type your answer here...");

        // ✅ Submit Answer Button
        Button submitButton = new Button("Submit Answer");
        submitButton.setOnAction(e -> {
            String answer = newAnswerField.getText().trim();
            if (!answer.isEmpty()) {
                try {
                    databaseHelper.addAnswer(question, user.getUserName(), answer);
                    loadAnswers(); // ✅ Refresh UI
                    newAnswerField.clear();
                } catch (SQLException ex) {
                    showAlert("Error submitting answer!", Alert.AlertType.ERROR);
                }
            }
        });

        // ✅ Mark as Resolved Button
     // ✅ Mark as Resolved Button
     // ✅ Mark Answer as Resolved and Refresh UI
        Button resolveButton = new Button("Mark as Resolved");
        resolveButton.setOnAction(e -> {
            String selectedAnswer = answerListView.getSelectionModel().getSelectedItem();
            if (selectedAnswer != null) {
                try {
                    databaseHelper.markAnswerAsResolved(question, selectedAnswer);
                    showAlert("✅ Answer marked as resolved!", Alert.AlertType.INFORMATION);

                    // ✅ Refresh QAPage when answer is resolved
                    new QAPage(databaseHelper, user).show(primaryStage);
                } catch (SQLException ex) {
                    showAlert("Error marking as resolved!", Alert.AlertType.ERROR);
                }
            } else {
                showAlert("Please select an answer to mark as resolved!", Alert.AlertType.WARNING);
            }
        });

        // ✅ Edit Answer Button
        Button editButton = new Button("Edit Answer");
        editButton.setOnAction(e -> {
            String selectedAnswer = answerListView.getSelectionModel().getSelectedItem();
            if (selectedAnswer != null && selectedAnswer.startsWith(user.getUserName() + ": ")) {
                TextInputDialog dialog = new TextInputDialog(selectedAnswer.split(": ")[1]);
                dialog.setTitle("Edit Answer");
                dialog.setHeaderText("Edit your answer:");
                dialog.setContentText("New Answer:");
                dialog.showAndWait().ifPresent(newAnswer -> {
                    if (!newAnswer.isEmpty()) {
                        try {
                            databaseHelper.updateAnswer(user.getUserName(), selectedAnswer.split(": ")[1], newAnswer);
                            loadAnswers(); // ✅ Refresh UI
                        } catch (SQLException ex) {
                            showAlert("Error updating answer!", Alert.AlertType.ERROR);
                        }
                    }
                });
            } else {
                showAlert("You can only edit your own answers!", Alert.AlertType.WARNING);
            }
        });

        // ✅ Delete Answer Button
        Button deleteButton = new Button("Delete Answer");
        deleteButton.setOnAction(e -> {
            String selectedAnswer = answerListView.getSelectionModel().getSelectedItem();
            if (selectedAnswer != null && selectedAnswer.startsWith(user.getUserName() + ": ")) {
                Alert confirmDialog = new Alert(Alert.AlertType.CONFIRMATION);
                confirmDialog.setTitle("Delete Answer");
                confirmDialog.setHeaderText("Are you sure you want to delete this answer?");
                confirmDialog.showAndWait().ifPresent(response -> {
                    if (response == ButtonType.OK) {
                        try {
                            databaseHelper.deleteAnswer(user.getUserName(), selectedAnswer.split(": ")[1]);
                            loadAnswers(); // ✅ Refresh UI
                        } catch (SQLException ex) {
                            showAlert("Error deleting answer!", Alert.AlertType.ERROR);
                        }
                    }
                });
            } else {
                showAlert("You can only delete your own answers!", Alert.AlertType.WARNING);
            }
        });

        // ✅ Back Button to Return to QAPage
        Button backButton = new Button("⬅ Back to Questions");
        backButton.setStyle("-fx-font-size: 14px; -fx-padding: 10px 20px; -fx-background-color: #555555; -fx-text-fill: white;");
        backButton.setOnAction(e -> new QAPage(databaseHelper, user).show(primaryStage));

        layout.getChildren().addAll(titleLabel, answerListView, newAnswerField, submitButton, resolveButton, editButton, deleteButton, backButton);

        Scene scene = new Scene(layout, 600, 550);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Answer Page");
        primaryStage.show();
    }

    // ✅ Load Answers from Database (With Username & Resolved Marking)
    private void loadAnswers() {
        try {
            answerList.clear();
            List<String> answers = databaseHelper.getAnswersForQuestion(question);
            answerList.addAll(answers);
        } catch (SQLException e) {
            showAlert("Error loading answers!", Alert.AlertType.ERROR);
        }
    }

    // ✅ Show Alerts
    private void showAlert(String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setContentText(message);
        alert.showAndWait();
    }
}

