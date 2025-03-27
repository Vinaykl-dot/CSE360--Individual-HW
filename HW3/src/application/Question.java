package application;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;

public class Question {
    private final SimpleStringProperty username;
    private final SimpleStringProperty question;
    private final SimpleBooleanProperty resolved;

    public Question(String username, String question, boolean resolved) {
        this.username = new SimpleStringProperty(username);
        this.question = new SimpleStringProperty(question);
        this.resolved = new SimpleBooleanProperty(resolved);
    }

    // ✅ Use JavaFX properties
    public String getUsername() {
        return username.get();
    }

    public String getQuestion() {
        return question.get();
    }

    public boolean isResolved() {
        return resolved.get();
    }

    public void setUsername(String username) {
        this.username.set(username);
    }

    public void setQuestion(String question) {
        this.question.set(question);
    }

    public void setResolved(boolean resolved) {
        this.resolved.set(resolved);
    }

    // ✅ JavaFX property methods (for TableView binding)
    public SimpleStringProperty usernameProperty() {
        return username;
    }

    public SimpleStringProperty questionProperty() {
        return question;
    }

    public SimpleBooleanProperty resolvedProperty() {
        return resolved;
    }

    @Override
    public String toString() {
        return username.get() + ": " + question.get() + (resolved.get() ? " ✔" : "");
    }
}

