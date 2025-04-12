package application;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import databasePart1.DatabaseHelper;
import java.util.Map;
import java.util.List;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Optional;
import java.sql.Statement;
import java.sql.ResultSet;

public class FlaggedItemDashboard extends Application {
    private DatabaseHelper dbHelper;
    private String currentUser;
    private TableView<Map<String, Object>> flaggedItemsTable;
    private ObservableList<Map<String, Object>> flaggedItemsData;

    public FlaggedItemDashboard(String currentUser) {
        this.currentUser = currentUser;
        this.dbHelper = new DatabaseHelper();
        try {
            dbHelper.connectToDatabase();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Flagged Items Dashboard");

        // Create main layout
        VBox mainLayout = new VBox(10);
        mainLayout.setPadding(new Insets(10));

        // Create table for flagged items
        flaggedItemsTable = new TableView<>();
        flaggedItemsData = FXCollections.observableArrayList();
        flaggedItemsTable.setItems(flaggedItemsData);

        // Create columns
        TableColumn<Map<String, Object>, String> typeCol = new TableColumn<>("Type");
        typeCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(
            (String) data.getValue().get("item_type")));

        TableColumn<Map<String, Object>, Integer> idCol = new TableColumn<>("Item ID");
        idCol.setCellValueFactory(data -> new javafx.beans.property.SimpleIntegerProperty(
            (Integer) data.getValue().get("item_id")).asObject());

        TableColumn<Map<String, Object>, String> flaggedByCol = new TableColumn<>("Flagged By");
        flaggedByCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(
            (String) data.getValue().get("flagged_by")));

        TableColumn<Map<String, Object>, String> reasonCol = new TableColumn<>("Reason");
        reasonCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(
            (String) data.getValue().get("flag_reason")));

        TableColumn<Map<String, Object>, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(
            (String) data.getValue().get("status")));

        TableColumn<Map<String, Object>, String> timestampCol = new TableColumn<>("Timestamp");
        timestampCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(
            data.getValue().get("timestamp").toString()));

        flaggedItemsTable.getColumns().addAll(typeCol, idCol, flaggedByCol, reasonCol, statusCol, timestampCol);

        // Create content display section
        VBox contentSection = new VBox(5);
        Label contentLabel = new Label("Original Content");
        contentLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
        TextArea contentArea = new TextArea();
        contentArea.setEditable(false);
        contentArea.setPrefRowCount(3);

        // Update content display when item is selected
        flaggedItemsTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
            if (newSel != null) {
                try {
                    String itemType = (String) newSel.get("item_type");
                    int itemId = (Integer) newSel.get("item_id");
                    String content = null;
                    
                    if ("question".equals(itemType)) {
                        content = dbHelper.getQuestionText(itemId);
                    } else if ("answer".equals(itemType)) {
                        content = dbHelper.getAnswerText(itemId);
                    }
                    
                    contentArea.setText(content != null ? content : "Content not found");
                } catch (SQLException e) {
                    showError("Error loading content: " + e.getMessage());
                }
            } else {
                contentArea.clear();
            }
        });

        contentSection.getChildren().addAll(contentLabel, contentArea);

        // Create comment section
        VBox commentSection = new VBox(5);
        Label commentLabel = new Label("Comments");
        commentLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        // Create a list view for existing comments
        ListView<String> commentsListView = new ListView<>();
        commentsListView.setPrefHeight(200);
        commentsListView.setStyle("-fx-border-color: lightgray; -fx-border-width: 1px;");

        TextArea commentArea = new TextArea();
        commentArea.setPromptText("Enter your comment here...");
        Button addCommentButton = new Button("Add Comment");
        Button resolveButton = new Button("Mark as Resolved");

        // Update comments when flagged item is selected
        flaggedItemsTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
            if (newSel != null) {
                commentsListView.getItems().clear();
                @SuppressWarnings("unchecked")
                List<Map<String, String>> comments = (List<Map<String, String>>) newSel.get("comments");
                if (comments != null) {
                    for (Map<String, String> comment : comments) {
                        String commentText = String.format("[%s] %s: %s", 
                            comment.get("timestamp"),
                            comment.get("staff_member"),
                            comment.get("comment_text")
                        );
                        commentsListView.getItems().add(commentText);
                    }
                }
            } else {
                commentsListView.getItems().clear();
            }
        });

        // Add comment button action
        addCommentButton.setOnAction(e -> {
            Map<String, Object> selectedItem = flaggedItemsTable.getSelectionModel().getSelectedItem();
            if (selectedItem != null && !commentArea.getText().isEmpty()) {
                try {
                    dbHelper.addStaffComment(
                        (Integer) selectedItem.get("id"),
                        currentUser,
                        commentArea.getText()
                    );
                    commentArea.clear();
                    refreshFlaggedItems();

                    // Select the same item again to refresh comments
                    int selectedIndex = flaggedItemsTable.getSelectionModel().getSelectedIndex();
                    flaggedItemsTable.getSelectionModel().clearSelection();
                    flaggedItemsTable.getSelectionModel().select(selectedIndex);
                } catch (SQLException ex) {
                    showError("Error adding comment: " + ex.getMessage());
                }
            } else if (selectedItem == null) {
                showAlert("Error", "Please select a flagged item first");
            } else {
                showAlert("Error", "Please enter a comment");
            }
        });

        // Add resolve button action
        resolveButton.setOnAction(e -> {
            Map<String, Object> selectedItem = flaggedItemsTable.getSelectionModel().getSelectedItem();
            if (selectedItem != null) {
                try {
                    dbHelper.updateFlaggedItemStatus(
                        (Integer) selectedItem.get("id"),
                        "resolved"
                    );
                    refreshFlaggedItems();
                    showAlert("Success", "Item marked as resolved");
                } catch (SQLException ex) {
                    showError("Error updating status: " + ex.getMessage());
                }
            } else {
                showAlert("Error", "Please select an item to resolve");
            }
        });

        commentSection.getChildren().addAll(
            commentLabel,
            commentsListView,  // Add the comments list view
            new Label("Add New Comment:"),
            commentArea,
            new HBox(10, addCommentButton, resolveButton)
        );

        // Create flag new item section
        VBox flagSection = new VBox(5);
        Label flagLabel = new Label("Flag New Item");
        flagLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        // Create tabs for Questions and Answers
        TabPane contentTabs = new TabPane();
        
        // Questions Tab
        Tab questionsTab = new Tab("Questions");
        VBox questionsBox = new VBox(5);
        TableView<Question> questionsTable = new TableView<>();
        
        TableColumn<Question, String> questionIdCol = new TableColumn<>("ID");
        questionIdCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(
            String.valueOf(data.getValue().getId())));
        
        TableColumn<Question, String> questionUserCol = new TableColumn<>("User");
        questionUserCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(
            data.getValue().getUsername()));
        
        TableColumn<Question, String> questionTextCol = new TableColumn<>("Question");
        questionTextCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(
            data.getValue().getQuestion()));
        
        questionsTable.getColumns().addAll(questionIdCol, questionUserCol, questionTextCol);
        
        // Answers Tab
        Tab answersTab = new Tab("Answers");
        VBox answersBox = new VBox(5);
        TableView<Map<String, Object>> answersTable = new TableView<>();
        
        TableColumn<Map<String, Object>, String> answerIdCol = new TableColumn<>("ID");
        answerIdCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(
            String.valueOf(data.getValue().get("id"))));
        
        TableColumn<Map<String, Object>, String> answerUserCol = new TableColumn<>("User");
        answerUserCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(
            (String) data.getValue().get("username")));
        
        TableColumn<Map<String, Object>, String> answerTextCol = new TableColumn<>("Answer");
        answerTextCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(
            (String) data.getValue().get("answer")));
        
        answersTable.getColumns().addAll(answerIdCol, answerUserCol, answerTextCol);
        
        // Load questions and answers
        try {
            questionsTable.getItems().addAll(dbHelper.getAllQuestions());
            
            // Load answers using the new method
            List<Map<String, Object>> answers = dbHelper.getAllAnswers();
            answersTable.getItems().addAll(answers);
        } catch (SQLException e) {
            showError("Error loading content: " + e.getMessage());
        }
        
        // Create separate flag buttons for questions and answers
        Button flagQuestionButton = new Button("Flag Selected Question");
        Button flagAnswerButton = new Button("Flag Selected Answer");

        // Question flag button action
        flagQuestionButton.setOnAction(e -> {
            try {
                Question selectedQuestion = questionsTable.getSelectionModel().getSelectedItem();
                if (selectedQuestion != null) {
                    TextInputDialog dialog = new TextInputDialog();
                    dialog.setTitle("Flag Question");
                    dialog.setHeaderText("Enter reason for flagging");
                    dialog.setContentText("Reason:");
                    
                    Optional<String> result = dialog.showAndWait();
                    if (result.isPresent() && !result.get().isEmpty()) {
                        dbHelper.flagItem("question", selectedQuestion.getId(), currentUser, result.get());
                        refreshFlaggedItems();
                        showAlert("Success", "Question flagged successfully!");
                    }
                } else {
                    showAlert("Error", "Please select a question to flag");
                }
            } catch (SQLException ex) {
                showError("Error flagging question: " + ex.getMessage());
            }
        });

        // Answer flag button action
        flagAnswerButton.setOnAction(e -> {
            try {
                Map<String, Object> selectedAnswer = answersTable.getSelectionModel().getSelectedItem();
                if (selectedAnswer != null) {
                    TextInputDialog dialog = new TextInputDialog();
                    dialog.setTitle("Flag Answer");
                    dialog.setHeaderText("Enter reason for flagging");
                    dialog.setContentText("Reason:");
                    
                    Optional<String> result = dialog.showAndWait();
                    if (result.isPresent() && !result.get().isEmpty()) {
                        dbHelper.flagItem("answer", (Integer) selectedAnswer.get("id"), currentUser, result.get());
                        refreshFlaggedItems();
                        showAlert("Success", "Answer flagged successfully!");
                    }
                } else {
                    showAlert("Error", "Please select an answer to flag");
                }
            } catch (SQLException ex) {
                showError("Error flagging answer: " + ex.getMessage());
            }
        });
        
        questionsBox.getChildren().addAll(questionsTable, flagQuestionButton);
        answersBox.getChildren().addAll(answersTable, flagAnswerButton);
        
        questionsTab.setContent(questionsBox);
        answersTab.setContent(answersBox);
        contentTabs.getTabs().addAll(questionsTab, answersTab);
        
        flagSection.getChildren().addAll(flagLabel, contentTabs);

        // Add all sections to main layout
        mainLayout.getChildren().addAll(
            new Label("Flagged Items Dashboard"),
            flaggedItemsTable,
            contentSection,
            commentSection,
            flagSection
        );

        // Make the main table taller
        flaggedItemsTable.setPrefHeight(200);

        // Add spacing between sections
        mainLayout.setSpacing(15);

        // Set up the scene
        Scene scene = new Scene(mainLayout, 800, 900);
        primaryStage.setScene(scene);
        primaryStage.show();

        // Initial load of flagged items
        refreshFlaggedItems();
    }

    private void refreshFlaggedItems() {
        try {
            List<Map<String, Object>> items = dbHelper.getAllFlaggedItems();
            flaggedItemsData.clear();
            flaggedItemsData.addAll(items);
        } catch (SQLException e) {
            showError("Error loading flagged items: " + e.getMessage());
        }
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static void main(String[] args) {
        launch(args);
    }
} 