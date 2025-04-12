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

public class InstructorReviewManagementPage {
    private Stage stage;
    private DatabaseHelper dbHelper;
    private String currentUser;

    public InstructorReviewManagementPage(Stage stage, String currentUser) {
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
        Label headerLabel = new Label("Instructor Review Management");
        headerLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");

        // Pending Reviewer Requests
        VBox pendingRequestsBox = new VBox(5);
        Label pendingLabel = new Label("Pending Reviewer Requests");
        pendingLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        ListView<String> pendingRequestsList = new ListView<>();
        try {
            List<User> users = dbHelper.getAllUsers();
            for (User user : users) {
                if (user.getRole().equals("student")) {
                    if (dbHelper.hasPendingReviewerRequest(user.getUserName())) {
                        pendingRequestsList.getItems().add(user.getUserName());
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // User Details and Actions
        VBox userDetailsBox = new VBox(5);
        Label detailsLabel = new Label("User Details");
        detailsLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        TextArea userDetailsArea = new TextArea();
        userDetailsArea.setEditable(false);
        userDetailsArea.setPrefRowCount(5);

        pendingRequestsList.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                try {
                    User user = dbHelper.getUserByUsername(newSelection);
                    if (user != null) {
                        userDetailsArea.setText(
                            "Username: " + user.getUserName() + "\n" +
                            "Email: " + user.getEmail() + "\n" +
                            "Role: " + user.getRole()
                        );
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        });

        // Action Buttons
        HBox actionButtons = new HBox(10);
        Button approveButton = new Button("Approve Reviewer");
        Button rejectButton = new Button("Reject Request");

        approveButton.setOnAction(e -> {
            String selectedUser = pendingRequestsList.getSelectionModel().getSelectedItem();
            if (selectedUser != null) {
                try {
                    if (dbHelper.approveReviewer(selectedUser, currentUser)) {
                        showAlert("Success", "Reviewer request approved!");
                        pendingRequestsList.getItems().remove(selectedUser);
                        userDetailsArea.clear();
                    } else {
                        showAlert("Error", "Failed to approve reviewer request");
                    }
                } catch (SQLException ex) {
                    showAlert("Error", "Error approving request: " + ex.getMessage());
                }
            } else {
                showAlert("Error", "Please select a user to approve");
            }
        });

        rejectButton.setOnAction(e -> {
            String selectedUser = pendingRequestsList.getSelectionModel().getSelectedItem();
            if (selectedUser != null) {
                try {
                    if (dbHelper.rejectReviewerRequest(selectedUser)) {
                        showAlert("Success", "Reviewer request rejected!");
                        pendingRequestsList.getItems().remove(selectedUser);
                        userDetailsArea.clear();
                    } else {
                        showAlert("Error", "Failed to reject reviewer request");
                    }
                } catch (SQLException ex) {
                    showAlert("Error", "Error rejecting request: " + ex.getMessage());
                }
            } else {
                showAlert("Error", "Please select a user to reject");
            }
        });

        actionButtons.getChildren().addAll(approveButton, rejectButton);

        // ✅ Fixed Back Button Action (using existing AdminHomePage code)
        Button backButton = new Button("Back to Admin Dashboard");
        backButton.setOnAction(e -> {
            AdminHomePage adminPage = new AdminHomePage(dbHelper);
            adminPage.show(stage);
        });

        // Layout
        pendingRequestsBox.getChildren().addAll(
            pendingLabel,
            pendingRequestsList
        );

        userDetailsBox.getChildren().addAll(
            detailsLabel,
            userDetailsArea,
            actionButtons
        );

        root.getChildren().addAll(
            headerLabel,
            pendingRequestsBox,
            userDetailsBox,
            backButton
        );

        Scene scene = new Scene(root, 600, 800);
        stage.setScene(scene);
        stage.setTitle("Instructor Review Management");
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
