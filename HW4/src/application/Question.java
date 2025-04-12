package application;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;

/**
 * The Question class represents a question in the Q&A system.
 * It includes ID, username of the author, the question text, and its resolved status.
 */
public class Question {
    private final int id;  // ✅ Unique database ID
    private final SimpleStringProperty username;
    private final SimpleStringProperty question;
    private final SimpleBooleanProperty resolved;

    // ✅ Updated constructor to include ID
    public Question(int id, String username, String question, boolean resolved) {
        this.id = id;
        this.username = new SimpleStringProperty(username);
        this.question = new SimpleStringProperty(question);
        this.resolved = new SimpleBooleanProperty(resolved);
    }

    // ✅ Getter for ID
    public int getId() {
        return id;
    }

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
