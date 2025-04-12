package application;

import databasePart1.DatabaseHelper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import javafx.scene.control.TableView;

public class ManageUsersPage {
    private final DatabaseHelper databaseHelper;

    public ManageUsersPage(DatabaseHelper databaseHelper) {
        this.databaseHelper = databaseHelper;
    }

    public void show(Stage primaryStage) {
        VBox layout = new VBox(15);
        layout.setStyle("-fx-padding: 20; -fx-alignment: center; -fx-background-color: black;");

        // ✅ Title Label
        Label titleLabel = new Label("Manage Users");
        titleLabel.setStyle("-fx-font-size: 22px; -fx-text-fill: white; -fx-font-weight: bold;");

        // ✅ TableView Setup
        TableView<User> userTable = new TableView<>();
        userTable.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);

        // ✅ Table Columns
        TableColumn<User, String> userNameCol = new TableColumn<>("Username");
        userNameCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getUserName()));

        TableColumn<User, String> emailCol = new TableColumn<>("Email");
        emailCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getEmail()));
        
        TableColumn<User, String> roleCol = new TableColumn<>("Roles");
        roleCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getRole()));
        
     // ✅ Add "Edit Roles" Column
        TableColumn<User, Void> editRolesCol = new TableColumn<>("Edit Roles");
        editRolesCol.setCellFactory(param -> new TableCell<>() {
            private final Button editRolesButton = new Button("Edit Roles");

            {
                editRolesButton.setStyle(
                    "-fx-background-color: #4CAF50; " +
                    "-fx-text-fill: white; " +
                    "-fx-background-radius: 5px; " +
                    "-fx-padding: 5px;"
                );

                editRolesButton.setOnMouseEntered(e -> editRolesButton.setStyle(
                    "-fx-background-color: #45a049; -fx-text-fill: white; -fx-background-radius: 5px; -fx-padding: 5px;"
                ));
                editRolesButton.setOnMouseExited(e -> editRolesButton.setStyle(
                    "-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-background-radius: 5px; -fx-padding: 5px;"
                ));

                editRolesButton.setOnAction(event -> {
                    User user = getTableView().getItems().get(getIndex());
                    openEditRolesDialog(user, userTable);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(editRolesButton);
                }
            }
        });


        // ✅ Delete User Column
        TableColumn<User, Void> deleteCol = new TableColumn<>("Delete");
        deleteCol.setCellFactory(param -> new TableCell<>() {
            private final Button deleteButton = new Button("❌ Delete");

            {
                deleteButton.setStyle(
                    "-fx-background-color: #ff4d4d; " +
                    "-fx-text-fill: white; " +
                    "-fx-background-radius: 5px; " +
                    "-fx-padding: 5px;"
                );

                deleteButton.setOnMouseEntered(e -> deleteButton.setStyle(
                    "-fx-background-color: #cc0000; -fx-text-fill: white; -fx-background-radius: 5px; -fx-padding: 5px;"
                ));
                deleteButton.setOnMouseExited(e -> deleteButton.setStyle(
                    "-fx-background-color: #ff4d4d; -fx-text-fill: white; -fx-background-radius: 5px; -fx-padding: 5px;"
                ));

                deleteButton.setOnAction(e -> {
                    User selectedUser = getTableView().getItems().get(getIndex());

                    // ✅ Prevent Admin Deletion
                    if ("admin".equalsIgnoreCase(selectedUser.getRole())) {
                        showAlert("❌ Admin accounts cannot be deleted!", Alert.AlertType.ERROR);
                        return;
                    }

                    // ✅ Confirmation Dialog
                    Alert confirmDialog = new Alert(Alert.AlertType.CONFIRMATION);
                    confirmDialog.setTitle("Delete User");
                    confirmDialog.setHeaderText("Are you sure you want to delete " + selectedUser.getUserName() + "?");
                    confirmDialog.setContentText("This action cannot be undone!");

                    confirmDialog.showAndWait().ifPresent(response -> {
                        if (response == ButtonType.OK) {
                            boolean deleted = databaseHelper.deleteUser(selectedUser.getUserName());
                            if (deleted) {
                                refreshUserTable(userTable);
                                showAlert("✅ User deleted successfully!", Alert.AlertType.INFORMATION);
                            } else {
                                showAlert("❌ Failed to delete user!", Alert.AlertType.ERROR);
                            }
                        }
                    });
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : deleteButton);
            }
        });

        // ✅ Add Columns to Table
        Collections.addAll(userTable.getColumns(), userNameCol, emailCol,roleCol,  editRolesCol,deleteCol);

        // ✅ Load Users into Table
        refreshUserTable(userTable);

        // ✅ Back Button
        Button backButton = new Button("⬅ Back");
        backButton.setStyle("-fx-font-size: 14px; -fx-padding: 10px 20px; -fx-background-color: #555555; -fx-text-fill: white;");
        backButton.setOnAction(e -> new AdminHomePage(databaseHelper).show(primaryStage));

        layout.getChildren().addAll(titleLabel, userTable, backButton);

        // ✅ Scene Setup
        Scene scene = new Scene(layout, 800, 500);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Manage Users");
        primaryStage.show();
    }

 // ✅ Open Role Selection Dialog
    private void openEditRolesDialog(User user, TableView<User> userTable) {
        Stage dialogStage = new Stage();
        dialogStage.setTitle("Edit Roles for " + user.getUserName());

        VBox layout = new VBox(10);
        layout.setStyle("-fx-padding: 20; -fx-alignment: center; -fx-background-color: black;");

        Label instructionLabel = new Label("Select roles for " + user.getUserName() + ":");
        instructionLabel.setStyle("-fx-text-fill: white; -fx-font-size: 14px;");

        // ✅ Role Checkboxes
        CheckBox studentCheck = new CheckBox("Student");
        CheckBox instructorCheck = new CheckBox("Instructor");
        CheckBox staffCheck = new CheckBox("Staff");
        CheckBox reviewerCheck = new CheckBox("Reviewer");

        // ✅ Pre-check current roles
        String currentRoles = user.getRole().toLowerCase();
        if (currentRoles.contains("student")) studentCheck.setSelected(true);
        if (currentRoles.contains("instructor")) instructorCheck.setSelected(true);
        if (currentRoles.contains("staff")) staffCheck.setSelected(true);
        if (currentRoles.contains("reviewer")) reviewerCheck.setSelected(true);

        // ✅ Save Button
        Button saveButton = new Button("Save Changes");
        saveButton.setStyle(
            "-fx-font-size: 14px; -fx-background-color: #4CAF50; " +
            "-fx-text-fill: white; -fx-background-radius: 5px; -fx-padding: 10px;"
        );

        saveButton.setOnAction(e -> {
            // ✅ Collect Selected Roles
            StringBuilder newRoles = new StringBuilder();
            if (studentCheck.isSelected()) newRoles.append("student,");
            if (instructorCheck.isSelected()) newRoles.append("instructor,");
            if (staffCheck.isSelected()) newRoles.append("staff,");
            if (reviewerCheck.isSelected()) newRoles.append("reviewer,");

            if (newRoles.length() > 0) {
                newRoles.setLength(newRoles.length() - 1); // ✅ Remove last comma
            }

            try {
                boolean updated = databaseHelper.updateUserRoles(user.getUserName(), newRoles.toString());
                if (updated) {
                    showAlert("✅ Roles updated successfully!", Alert.AlertType.INFORMATION);
                    refreshUserTable(userTable); // ✅ Refresh table
                    dialogStage.close();
                } else {
                    showAlert("❌ Failed to update roles!", Alert.AlertType.ERROR);
                }
            } catch (SQLException ex) {
                showAlert("❌ Database error!", Alert.AlertType.ERROR);
                ex.printStackTrace();
            }
        });

        // ✅ Close Button
        Button closeButton = new Button("Cancel");
        closeButton.setStyle(
            "-fx-font-size: 14px; -fx-background-color: #ff4d4d; " +
            "-fx-text-fill: white; -fx-background-radius: 5px; -fx-padding: 10px;"
        );
        closeButton.setOnAction(e -> dialogStage.close());

        layout.getChildren().addAll(instructionLabel, studentCheck, instructorCheck, staffCheck, reviewerCheck, saveButton, closeButton);

        Scene scene = new Scene(layout, 300, 350);
        dialogStage.setScene(scene);
        dialogStage.show();
    }

    // ✅ Refresh Table with Users
    private void refreshUserTable(TableView<User> userTable) {
        try {
            List<User> users = databaseHelper.getAllUsers(); // ✅ Fetch latest users with updated roles
            ObservableList<User> userList = FXCollections.observableArrayList(users);
            userTable.setItems(userList);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // ✅ Helper Method to Show Alerts
    private void showAlert(String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
