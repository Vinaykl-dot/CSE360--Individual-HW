package application;

/**
 * The User class represents a user entity in the system.
 * It contains the user's details such as userName, password, role, and email.
 */
public class User {
    private String userName;
    private String password;
    private String role;
    private String email;  // ✅ Added email field

    // ✅ Constructor to initialize a new User object with userName, password, role, and email.
    public User(String userName, String password, String role, String email) {
        this.userName = userName;
        this.password = password;
        this.role = role;
        this.email = email;  // ✅ Store email
    }

    // ✅ Constructor without email (for compatibility if needed)
    public User(String userName, String password, String role) {
        this.userName = userName;
        this.password = password;
        this.role = role;
        this.email = "";  // Default empty email
    }

    // ✅ Getter for email
    public String getEmail() {
        return email;
    }

    // ✅ Setter for email (if needed)
    public void setEmail(String email) {
        this.email = email;
    }

    // Other getters and setters
    public void setRole(String role) {
        this.role = role;
    }

    public String getUserName() { return userName; }
    public String getPassword() { return password; }
    public String getRole() { return role; }
}
