package application;

import databasePart1.DatabaseHelper;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

public class PrivateMessagesPage {
    private final Stage stage;
    private final DatabaseHelper dbHelper;
    private final String currentUser;

    public PrivateMessagesPage(Stage stage, String currentUser) {
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

        Label header = new Label("Private Messages");
        header.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");

        VBox messageList = new VBox(10);
        messageList.setPadding(new Insets(10));
        messageList.setStyle("-fx-border-color: gray; -fx-border-width: 1; -fx-background-color: #f4f4f4;");
        refreshMessages(messageList);

        // Message Form
        TextField recipientField = new TextField();
        recipientField.setPromptText("Recipient username");

        TextArea messageBox = new TextArea();
        messageBox.setPromptText("Type your message...");
        messageBox.setPrefRowCount(3);

        Button sendButton = new Button("Send Message");
        sendButton.setOnAction(e -> {
            String to = recipientField.getText().trim();
            String msg = messageBox.getText().trim();
            if (!to.isEmpty() && !msg.isEmpty()) {
                try {
                    if (dbHelper.sendPrivateMessage(currentUser, to, msg)) {
                        showAlert("Success", "Message sent.");
                        recipientField.clear();
                        messageBox.clear();
                        refreshMessages(messageList);
                    } else {
                        showAlert("Error", "Failed to send message.");
                    }
                } catch (SQLException ex) {
                    showAlert("Error", "Database error: " + ex.getMessage());
                }
            }
        });

        HBox form = new HBox(10, recipientField, messageBox, sendButton);
        form.setAlignment(Pos.CENTER);

        Button backButton = new Button("⬅ Back");
        backButton.setOnAction(e -> {
            try {
                User user = dbHelper.getUserByUsername(currentUser);
                new UserHomePage(dbHelper, user).show(stage);
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        });

        root.getChildren().addAll(header, messageList, form, backButton);

        Scene scene = new Scene(root, 800, 600);
        stage.setScene(scene);
        stage.setTitle("Private Messages");
        stage.show();
    }

    private void refreshMessages(VBox messageList) {
        messageList.getChildren().clear();
        try {
            List<Map<String, Object>> messages = dbHelper.getPrivateMessages(currentUser);
            for (Map<String, Object> msg : messages) {
                VBox card = new VBox(3);
                card.setStyle("-fx-padding: 5; -fx-border-color: lightgray; -fx-background-color: white;");
                String sender = (String) msg.get("sender_id");
                String receiver = (String) msg.get("receiver_id");
                String text = (String) msg.get("message_text");
                String timestamp = msg.get("timestamp").toString();

                card.getChildren().addAll(
                    new Label("From: " + sender),
                    new Label("To: " + receiver),
                    new Label("At: " + timestamp),
                    new Label("Message: " + text)
                );
                messageList.getChildren().add(card);
            }
            if (messages.isEmpty()) {
                messageList.getChildren().add(new Label("No messages to display."));
            }
        } catch (SQLException e) {
            showAlert("Error", "Could not load messages.");
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
