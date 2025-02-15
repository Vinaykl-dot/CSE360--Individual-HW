package application;

import javafx.beans.property.SimpleStringProperty;
import databasePart1.DatabaseHelper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import java.sql.SQLException;
import java.util.List;




public class QAPage {
    private final DatabaseHelper databaseHelper;
    private final User user;
    private final ObservableList<Question> questionList = FXCollections.observableArrayList();

    public QAPage(DatabaseHelper databaseHelper, User user) {
        this.databaseHelper = databaseHelper;
        this.user = user;
    }

    public void show(Stage primaryStage) {
        VBox layout = new VBox(10);
        layout.setStyle("-fx-padding: 20; -fx-alignment: center; -fx-background-color: black;");

        Label titleLabel = new Label("Student Q&A System");
        titleLabel.setStyle("-fx-font-size: 18px; -fx-text-fill: white; -fx-font-weight: bold;");

        // ✅ TableView for Displaying Questions
        TableView<Question> questionTable = new TableView<>();
        questionTable.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY); // ✅ No constraints (default)


        // ✅ Column: Username
        TableColumn<Question, String> userColumn = new TableColumn<>("Username");
        userColumn.setCellValueFactory(cellData -> cellData.getValue().usernameProperty());

        TableColumn<Question, String> questionColumn = new TableColumn<>("Question");
        questionColumn.setCellValueFactory(cellData -> cellData.getValue().questionProperty());
     // ✅ Column: Resolved Status
     // ✅ Column: Resolved Status
        TableColumn<Question, String> resolvedColumn = new TableColumn<>("Resolved");
        resolvedColumn.setCellValueFactory(cellData -> {
            boolean isResolved = cellData.getValue().isResolved();
            return new SimpleStringProperty(isResolved ? "✔" : ""); // ✅ Display ✔ if resolved
        });




        // ✅ Column: Edit Button
        TableColumn<Question, Void> editColumn = new TableColumn<>("Edit");
        editColumn.setCellFactory(param -> new TableCell<>() {
            private final Button editButton = new Button("✏ Edit");

            {
                editButton.setOnAction(e -> {
                    Question selectedQuestion = getTableView().getItems().get(getIndex());
                    if (selectedQuestion.getUsername().equals(user.getUserName())) {
                        editQuestion(selectedQuestion);
                    } else {
                        showAlert("You can only edit your own questions!", Alert.AlertType.WARNING);
                    }
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(editButton);
                }
            }
        });

        // ✅ Column: Delete Button
        TableColumn<Question, Void> deleteColumn = new TableColumn<>("Delete");
        deleteColumn.setCellFactory(param -> new TableCell<>() {
            private final Button deleteButton = new Button("❌ Delete");

            {
                deleteButton.setOnAction(e -> {
                    Question selectedQuestion = getTableView().getItems().get(getIndex());
                    if (selectedQuestion.getUsername().equals(user.getUserName())) {
                        deleteQuestion(selectedQuestion);
                    } else {
                        showAlert("You can only delete your own questions!", Alert.AlertType.WARNING);
                    }
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(deleteButton);
                }
            }
        });

        questionTable.getColumns().add(userColumn);
        questionTable.getColumns().add(questionColumn);
        questionTable.getColumns().add(resolvedColumn);
        questionTable.getColumns().add(editColumn);
        questionTable.getColumns().add(deleteColumn);

        questionTable.setItems(questionList);
        loadQuestions();

        // ✅ TextField for Adding New Question
        TextField newQuestionField = new TextField();
        newQuestionField.setPromptText("Type your question here...");

        // ✅ Submit Question Button
        Button submitButton = new Button("Ask Question");
        submitButton.setOnAction(e -> {
            String questionText = newQuestionField.getText().trim();
            if (!questionText.isEmpty()) {
                try {
                    databaseHelper.addQuestion(user.getUserName(), questionText);
                    loadQuestions(); // ✅ Refresh UI
                    newQuestionField.clear();
                } catch (SQLException ex) {
                    showAlert("Error adding question!", Alert.AlertType.ERROR);
                }
            }
        });

        // ✅ View Answers Button
        Button viewAnswersButton = new Button("View Answers");
        viewAnswersButton.setOnAction(e -> {
            Question selectedQuestion = questionTable.getSelectionModel().getSelectedItem();
            if (selectedQuestion != null) {
                new AnswerPage(databaseHelper, user, selectedQuestion.getQuestion()).show(primaryStage);
            } else {
                showAlert("Please select a question!", Alert.AlertType.WARNING);
            }
        });

        // ✅ Back Button to Return to UserHomePage
        Button backButton = new Button("⬅ Back to User Page");
        backButton.setStyle("-fx-font-size: 14px; -fx-padding: 10px 20px; -fx-background-color: #555555; -fx-text-fill: white;");
        backButton.setOnAction(e -> new UserHomePage(databaseHelper, user).show(primaryStage));

        layout.getChildren().addAll(titleLabel, questionTable, newQuestionField, submitButton, viewAnswersButton, backButton);

        Scene scene = new Scene(layout, 700, 550);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Q&A System");
        primaryStage.show();
    }

    // ✅ Load Questions from the Database
    private void loadQuestions() {
        try {
            questionList.clear();
            List<Question> questions = databaseHelper.getAllQuestions();
            questionList.addAll(questions);
        } catch (SQLException e) {
            showAlert("Error loading questions!", Alert.AlertType.ERROR);
        }
    }

    // ✅ Edit Question Method
    private void editQuestion(Question question) {
        TextInputDialog dialog = new TextInputDialog(question.getQuestion());
        dialog.setTitle("Edit Question");
        dialog.setHeaderText("Edit your question:");
        dialog.setContentText("New Question:");

        dialog.showAndWait().ifPresent(newQuestion -> {
            if (!newQuestion.isEmpty()) {
                try {
                    databaseHelper.updateQuestion(user.getUserName(), question.getQuestion(), newQuestion);
                    loadQuestions(); // ✅ Refresh UI
                } catch (SQLException ex) {
                    showAlert("Error updating question!", Alert.AlertType.ERROR);
                }
            }
        });
    }

    // ✅ Delete Question Method
    private void deleteQuestion(Question question) {
        Alert confirmDialog = new Alert(Alert.AlertType.CONFIRMATION);
        confirmDialog.setTitle("Delete Question");
        confirmDialog.setHeaderText("Are you sure you want to delete this question?");
        confirmDialog.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    databaseHelper.deleteQuestion(user.getUserName(), question.getQuestion());
                    loadQuestions(); // ✅ Refresh UI
                } catch (SQLException ex) {
                    showAlert("Error deleting question!", Alert.AlertType.ERROR);
                }
            }
        });
    }

    // ✅ Show Alerts
    private void showAlert(String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
