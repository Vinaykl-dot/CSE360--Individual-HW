package databasePart1;
import java.sql.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import application.Question;


import application.User;


/**
 * The DatabaseHelper class is responsible for managing the connection to the database,
 * performing operations such as user registration, login validation, and handling invitation codes.
 */
public class DatabaseHelper {

	// JDBC driver name and database URL 
	static final String JDBC_DRIVER = "org.h2.Driver";   
	static final String DB_URL = "jdbc:h2:~/FoundationDatabase";  

	//  Database credentials 
	static final String USER = "sa"; 
	static final String PASS = ""; 

	private Connection connection = null;
	private Statement statement = null; 
	//	PreparedStatement pstmt

	public void connectToDatabase() throws SQLException {
		try {
			Class.forName(JDBC_DRIVER); // Load the JDBC driver
			System.out.println("Connecting to database...");
			connection = DriverManager.getConnection(DB_URL, USER, PASS);
			statement = connection.createStatement(); 
			

			createTables();  // Create the necessary tables if they don't exist
		} catch (ClassNotFoundException e) {
			System.err.println("JDBC Driver not found: " + e.getMessage());
		}
	}

	private void createTables() throws SQLException {
	  

	    String userTable = "CREATE TABLE IF NOT EXISTS cse360users ("
	            + "id INT AUTO_INCREMENT PRIMARY KEY, "
	            + "userName VARCHAR(255) UNIQUE, "
	            + "email VARCHAR(255) UNIQUE, "
	            + "password VARCHAR(255), "
	            + "role VARCHAR(20))";
	    statement.execute(userTable);

	    String answersTable = "CREATE TABLE IF NOT EXISTS answers ("
	            + "id INT AUTO_INCREMENT PRIMARY KEY, "
	            + "question TEXT NOT NULL, "
	            + "username TEXT NOT NULL, "  // ✅ Fixed column name
	            + "answer TEXT NOT NULL, "
	            + "resolved BOOLEAN DEFAULT FALSE)";
	    statement.execute(answersTable);

	    String questionsTable = "CREATE TABLE IF NOT EXISTS questions ("
	            + "id INT AUTO_INCREMENT PRIMARY KEY, "
	            + "userName VARCHAR(255) NOT NULL, "
	            + "question TEXT NOT NULL)";
	    statement.execute(questionsTable);

	    String invitationCodesTable = "CREATE TABLE IF NOT EXISTS InvitationCodes ("
	            + "code VARCHAR(10) PRIMARY KEY, "
	            + "isUsed BOOLEAN DEFAULT FALSE)";
	    statement.execute(invitationCodesTable);
	}





	// Check if the database is empty
//✅ Ensure this is inside the DatabaseHelper class!
public boolean isDatabaseEmpty() throws SQLException {
 String query = "SELECT COUNT(*) AS count FROM cse360users";
 try (ResultSet resultSet = statement.executeQuery(query)) {
     if (resultSet.next()) {
         return resultSet.getInt("count") == 0;
     }
 }
 return true;  // Default to true if query fails
}
public List<Question> getAllQuestions() throws SQLException {
    List<Question> questions = new ArrayList<>();
    String query = "SELECT q.userName, q.question, "
                 + "(CASE WHEN EXISTS (SELECT 1 FROM answers a WHERE a.question = q.question AND a.resolved = TRUE) "
                 + "THEN TRUE ELSE FALSE END) AS resolved_status "
                 + "FROM questions q";

    try (PreparedStatement stmt = connection.prepareStatement(query);
         ResultSet rs = stmt.executeQuery()) {
        while (rs.next()) {
            String user = rs.getString("userName");  
            String questionText = rs.getString("question");  
            boolean isResolved = rs.getBoolean("resolved_status");  

            questions.add(new Question(user, questionText, isResolved));  
        }
    }
    return questions;
}




// ✅ Add a New Question
public boolean addQuestion(String userName, String questionText) throws SQLException {
    String query = "INSERT INTO questions (userName, question) VALUES (?, ?)";
    try (PreparedStatement pstmt = connection.prepareStatement(query)) {
        pstmt.setString(1, userName);
        pstmt.setString(2, questionText);
        return pstmt.executeUpdate() > 0;  // ✅ Return true if insertion was successful
    }
}
// ✅ Update Question in Database
public boolean updateQuestion(String userName, String oldQuestion, String newQuestion) throws SQLException {
    String query = "UPDATE questions SET question = ? WHERE userName = ? AND question = ?";
    try (PreparedStatement pstmt = connection.prepareStatement(query)) {
        pstmt.setString(1, newQuestion);
        pstmt.setString(2, userName);
        pstmt.setString(3, oldQuestion);
        return pstmt.executeUpdate() > 0; // ✅ Returns true if update is successful
    }
}

// ✅ Delete Question from Database
public boolean deleteQuestion(String userName, String question) throws SQLException {
    String query = "DELETE FROM questions WHERE userName = ? AND question = ?";
    try (PreparedStatement pstmt = connection.prepareStatement(query)) {
        pstmt.setString(1, userName);
        pstmt.setString(2, question);
        return pstmt.executeUpdate() > 0;  // ✅ Returns true if deletion was successful
    }
}
public void addAnswer(String question, String user, String answer) throws SQLException {
    String query = "INSERT INTO answers (question, username, answer, resolved) VALUES (?, ?, ?, false)";
    
    System.out.println("Debug: Inserting Answer...");
    System.out.println("Question: " + question);
    System.out.println("User: " + user);
    System.out.println("Answer: " + answer);

    try (PreparedStatement stmt = connection.prepareStatement(query)) {
        stmt.setString(1, question);
        stmt.setString(2, user);
        stmt.setString(3, answer);
        stmt.executeUpdate();
        
        // ✅ Explicitly commit to ensure data persists
        connection.commit();  
        System.out.println("✅ Answer Inserted Successfully and Committed!");
    } catch (SQLException e) {
        e.printStackTrace(); // ✅ Print full SQL error
    }
}
public List<String> getAnswersForQuestion(String question) throws SQLException {
    List<String> answers = new ArrayList<>();
    String query = "SELECT username, answer FROM answers WHERE question = ?";

    try (PreparedStatement stmt = connection.prepareStatement(query)) {
        stmt.setString(1, question);
        ResultSet rs = stmt.executeQuery();
        while (rs.next()) {
            String user = rs.getString("username");
            String answer = rs.getString("answer");
            answers.add(user + ": " + answer); // ✅ Shows "Username: Answer"
        }
    }
    return answers;
}


// ✅ Retrieve the Resolved Answer (if any)
public String getResolvedAnswer(String question) throws SQLException {
    String query = "SELECT answer FROM answers WHERE question = ? AND resolved = TRUE LIMIT 1";
    try (PreparedStatement stmt = connection.prepareStatement(query)) {
        stmt.setString(1, question);
        ResultSet rs = stmt.executeQuery();
        if (rs.next()) {
            return rs.getString("answer"); // ✅ Return the resolved answer
        }
    }
    return null; // ✅ No resolved answer found
}
// ✅ Update an Answer (Only if the answer belongs to the user)
public boolean updateAnswer(String userName, String oldAnswer, String newAnswer) throws SQLException {
    String query = "UPDATE answers SET answer = ? WHERE username = ? AND answer = ? AND resolved = FALSE";
    try (PreparedStatement pstmt = connection.prepareStatement(query)) {
        pstmt.setString(1, newAnswer);
        pstmt.setString(2, userName);
        pstmt.setString(3, oldAnswer);
        return pstmt.executeUpdate() > 0; // ✅ Returns true if update is successful
    }
}

// ✅ Delete an Answer (Only if the answer belongs to the user)
public boolean deleteAnswer(String userName, String answer) throws SQLException {
    String query = "DELETE FROM answers WHERE username = ? AND answer = ?";
    try (PreparedStatement pstmt = connection.prepareStatement(query)) {
        pstmt.setString(1, userName);
        pstmt.setString(2, answer);
        return pstmt.executeUpdate() > 0; // ✅ Returns true if deletion is successful
    }
}
public void markAnswerAsResolved(String question, String answer) throws SQLException {
    String query = "UPDATE answers SET resolved = TRUE WHERE question = ? AND answer = ?";
    
    try (PreparedStatement stmt = connection.prepareStatement(query)) {
        stmt.setString(1, question);
        stmt.setString(2, answer);
        int rowsUpdated = stmt.executeUpdate();
        
        if (rowsUpdated > 0) {
            System.out.println("✅ Answer marked as resolved in the database.");
        } else {
            System.out.println("❌ No rows updated. Check if the question and answer exist.");
        }
    }
}


	// Registers a new user in the database.
	// ✅ Store email when registering a new user
	public void register(User user) throws SQLException {
	    String insertUser = "INSERT INTO cse360users (userName, email, password, role) VALUES (?, ?, ?, ?)";
	    try (PreparedStatement pstmt = connection.prepareStatement(insertUser)) {
	        pstmt.setString(1, user.getUserName());
	        pstmt.setString(2, user.getEmail());  // ✅ Store email
	        pstmt.setString(3, user.getPassword());
	        pstmt.setString(4, user.getRole());
	        pstmt.executeUpdate();
	    }
	}
	// ✅ Debugging version of addAnswer
	

	// Validates a user's login credentials.
	public boolean login(User user) throws SQLException {
	    reconnectIfNeeded();

	    // ❌ Remove OTP login check
	    // if (isOneTimePassword(user.getUserName(), user.getPassword())) return true;

	    // ✅ Only check normal password
	    String query = "SELECT * FROM cse360users WHERE userName = ? AND password = ? AND role = ?";
	    try (PreparedStatement pstmt = connection.prepareStatement(query)) {
	        pstmt.setString(1, user.getUserName());
	        pstmt.setString(2, user.getPassword());
	        pstmt.setString(3, user.getRole());
	        try (ResultSet rs = pstmt.executeQuery()) {
	            return rs.next();
	        }
	    }
	}


	
	// Checks if a user already exists in the database based on their userName.
	public boolean doesUserExist(String userName) {
	    String query = "SELECT COUNT(*) FROM cse360users WHERE userName = ?";
	    try (PreparedStatement pstmt = connection.prepareStatement(query)) {
	        
	        pstmt.setString(1, userName);
	        ResultSet rs = pstmt.executeQuery();
	        
	        if (rs.next()) {
	            // If the count is greater than 0, the user exists
	            return rs.getInt(1) > 0;
	        }
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
	    return false; // If an error occurs, assume user doesn't exist
	}
	
	// Retrieves the role of a user from the database using their UserName.
	public String getUserRole(String userName) {
	    String query = "SELECT role FROM cse360users WHERE userName = ?";
	    try (PreparedStatement pstmt = connection.prepareStatement(query)) {
	        pstmt.setString(1, userName);
	        ResultSet rs = pstmt.executeQuery();

	        if (rs.next()) {
	            String role = rs.getString("role");
	            System.out.println("User Role Retrieved: " + role); // ✅ Debugging Output
	            return role;
	        }
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
	    return null; // Return null if no user exists
	}

	// Generates a new invitation code and inserts it into the database.
	public String generateInvitationCode() {
	    String code = UUID.randomUUID().toString().substring(0, 4); // Generate a random 4-character code
	    String query = "INSERT INTO InvitationCodes (code) VALUES (?)";

	    try (PreparedStatement pstmt = connection.prepareStatement(query)) {
	        pstmt.setString(1, code);
	        pstmt.executeUpdate();
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }

	    return code;
	}
	
	// Validates an invitation code to check if it is unused.
	public boolean validateInvitationCode(String code) {
	    String query = "SELECT * FROM InvitationCodes WHERE code = ? AND isUsed = FALSE";
	    try (PreparedStatement pstmt = connection.prepareStatement(query)) {
	        pstmt.setString(1, code);
	        ResultSet rs = pstmt.executeQuery();
	        if (rs.next()) {
	            // Mark the code as used
	            markInvitationCodeAsUsed(code);
	            return true;
	        }
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
	    return false;
	}
	// ✅ Fetch all users for admin
	public List<User> getAllUsers() throws SQLException {
	    List<User> userList = new ArrayList<>();
	    String query = "SELECT userName, email, role FROM cse360users";
	    
	    try (PreparedStatement pstmt = connection.prepareStatement(query);
	         ResultSet rs = pstmt.executeQuery()) {
	        
	        while (rs.next()) {
	            String userName = rs.getString("userName");
	            String email = rs.getString("email");
	            String role = rs.getString("role");
	            userList.add(new User(userName, "", role, email)); // Password is not needed
	        }
	    }
	    return userList;
	}
	// ✅ Retrieve All Questions from the Questions Table
	// ✅ Retrieve All Questions and Indicate Resolved Status
	// ✅ Retrieve All Questions and Indicate Resolved Status
	

	// ✅ Retrieve All Answers for a Question
	

	// Marks the invitation code as used in the database.
	private void markInvitationCodeAsUsed(String code) {
	    String query = "UPDATE InvitationCodes SET isUsed = TRUE WHERE code = ?";
	    try (PreparedStatement pstmt = connection.prepareStatement(query)) {
	        pstmt.setString(1, code);
	        pstmt.executeUpdate();
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
	}
	// ✅ Check if an email (username) exists in the database
	// ✅ Check if an email exists in the database (fixing incorrect username check)
	public boolean isEmailRegistered(String email) {
	    String query = "SELECT COUNT(*) FROM cse360users WHERE email = ?";  // ✅ Check email column
	    try (PreparedStatement pstmt = connection.prepareStatement(query)) {
	        pstmt.setString(1, email);
	        ResultSet rs = pstmt.executeQuery();
	        if (rs.next()) {
	            return rs.getInt(1) > 0;
	        }
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
	    return false;
	}

	// ✅ Get user's email by username
	public String getUserEmail(String userName) {
	    String query = "SELECT email FROM cse360users WHERE userName = ?";
	    try (PreparedStatement pstmt = connection.prepareStatement(query)) {
	        pstmt.setString(1, userName);
	        ResultSet rs = pstmt.executeQuery();
	        if (rs.next()) {
	            return rs.getString("email");  // ✅ Return email if found
	        }
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
	    return null;  // No email found
	}
	// ✅ Method to update user email or password
	public boolean updateUserInfo(String userName, String newEmail, String newPassword) throws SQLException {
	    String query = "UPDATE cse360users SET email = ?, password = ? WHERE userName = ?";
	    try (PreparedStatement pstmt = connection.prepareStatement(query)) {
	        pstmt.setString(1, newEmail);
	        pstmt.setString(2, newPassword);
	        pstmt.setString(3, userName);

	        int rowsAffected = pstmt.executeUpdate();
	        return rowsAffected > 0;  // ✅ Returns true if update is successful
	    }
	}
	// ✅ Add this method inside DatabaseHelper.java
	public void registerAdmin(User admin) throws SQLException {
	    String insertAdmin = "INSERT INTO cse360users (userName, email, password, role) VALUES (?, ?, ?, ?)";
	    try (PreparedStatement pstmt = connection.prepareStatement(insertAdmin)) {
	        pstmt.setString(1, admin.getUserName());
	        pstmt.setString(2, admin.getEmail());
	        pstmt.setString(3, admin.getPassword());
	        pstmt.setString(4, "admin");  // ✅ Explicitly store "admin" role
	        pstmt.executeUpdate();
	    }
	}

	// ✅ Update Username
	public boolean updateUsername(String oldUsername, String newUsername) throws SQLException {
	    String query = "UPDATE cse360users SET userName = ? WHERE userName = ?";
	    try (PreparedStatement pstmt = connection.prepareStatement(query)) {
	        pstmt.setString(1, newUsername);
	        pstmt.setString(2, oldUsername);
	        return pstmt.executeUpdate() > 0;
	    }
	}

	// ✅ Update Email
	public boolean updateEmail(String userName, String newEmail) throws SQLException {
	    String query = "UPDATE cse360users SET email = ? WHERE userName = ?";
	    try (PreparedStatement pstmt = connection.prepareStatement(query)) {
	        pstmt.setString(1, newEmail);
	        pstmt.setString(2, userName);
	        return pstmt.executeUpdate() > 0;
	    }
	}

	// ✅ Update Password
	public boolean updatePassword(String userName, String newPassword) throws SQLException {
	    String query = "UPDATE cse360users SET password = ? WHERE userName = ?";
	    try (PreparedStatement pstmt = connection.prepareStatement(query)) {
	        pstmt.setString(1, newPassword);
	        pstmt.setString(2, userName);
	        return pstmt.executeUpdate() > 0;
	    }
	}
	// ✅ Check if the password is a one-time password (OTP)
	


	// ✅ Simulate sending a password reset email (Replace with actual email logic if needed)
	public void sendPasswordResetEmail(String email) {
	    System.out.println("Password reset link has been sent to: " + email);
	    // In a real application, integrate with an email service (SMTP, JavaMail API, etc.)
	}
	public void reconnectIfNeeded() {
	    try {
	        if (connection == null || connection.isClosed()) {
	            connectToDatabase(); // ✅ Reconnect if the connection is closed
	        }
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
	}
	// ✅ Delete User from Database
	public boolean deleteUser(String userName) {
	    String query = "DELETE FROM cse360users WHERE userName = ?";
	    try (PreparedStatement pstmt = connection.prepareStatement(query)) {
	        pstmt.setString(1, userName);
	        return pstmt.executeUpdate() > 0;  // ✅ Return true if deletion was successful
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
	    return false;
	}

	// ✅ Update User Roles in the Database
	public boolean updateUserRoles(String userName, String newRoles) throws SQLException {
	    String query = "UPDATE cse360users SET role = ? WHERE userName = ?";
	    try (PreparedStatement pstmt = connection.prepareStatement(query)) {
	        pstmt.setString(1, newRoles);
	        pstmt.setString(2, userName);
	        return pstmt.executeUpdate() > 0; // ✅ Returns true if update is successful
	    }
	}

	// ✅ Mark an Answer as Resolved
	// ✅ Mark an Answer as Resolved
	


	// Closes the database connection and statement.
	public void closeConnection() {
		try{ 
			if(statement!=null) statement.close(); 
		} catch(SQLException se2) { 
			se2.printStackTrace();
		} 
		try { 
			if(connection!=null) connection.close(); 
		} catch(SQLException se){ 
			se.printStackTrace(); 
		} 
	}

}
