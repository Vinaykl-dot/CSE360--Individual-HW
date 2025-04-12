package databasePart1;
import java.sql.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.HashMap;
import java.util.Map;
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

	    String reviewsTable = "CREATE TABLE IF NOT EXISTS reviews ("
	            + "id INT AUTO_INCREMENT PRIMARY KEY, "
	            + "question_id INT, "
	            + "answer_id INT, "
	            + "reviewer_id VARCHAR(255), "
	            + "review_text TEXT NOT NULL, "
	            + "rating INT, "
	            + "timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP, "
	            + "FOREIGN KEY (question_id) REFERENCES questions(id), "
	            + "FOREIGN KEY (answer_id) REFERENCES answers(id), "
	            + "FOREIGN KEY (reviewer_id) REFERENCES cse360users(userName))";
	    statement.execute(reviewsTable);

	    String reviewerPermissionsTable = "CREATE TABLE IF NOT EXISTS reviewer_permissions ("
	            + "user_id VARCHAR(255) PRIMARY KEY, "
	            + "is_approved BOOLEAN DEFAULT FALSE, "
	            + "approved_by VARCHAR(255), "
	            + "approval_date TIMESTAMP, "
	            + "FOREIGN KEY (user_id) REFERENCES cse360users(userName), "
	            + "FOREIGN KEY (approved_by) REFERENCES cse360users(userName))";
	    statement.execute(reviewerPermissionsTable);

	    String privateMessagesTable = "CREATE TABLE IF NOT EXISTS private_messages ("
	            + "id INT AUTO_INCREMENT PRIMARY KEY, "
	            + "sender_id VARCHAR(255) NOT NULL, "
	            + "receiver_id VARCHAR(255) NOT NULL, "
	            + "message_text TEXT NOT NULL, "
	            + "timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP, "
	            + "FOREIGN KEY (sender_id) REFERENCES cse360users(userName), "
	            + "FOREIGN KEY (receiver_id) REFERENCES cse360users(userName))";
	    statement.execute(privateMessagesTable);

	    // New table for flagged items
	    String flaggedItemsTable = "CREATE TABLE IF NOT EXISTS flagged_items ("
	            + "id INT AUTO_INCREMENT PRIMARY KEY, "
	            + "item_type VARCHAR(20) NOT NULL, " // 'question' or 'answer'
	            + "item_id INT NOT NULL, " // ID of the question or answer
	            + "flagged_by VARCHAR(255) NOT NULL, "
	            + "flag_reason TEXT, "
	            + "status VARCHAR(20) DEFAULT 'open', " // 'open' or 'resolved'
	            + "timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP, "
	            + "FOREIGN KEY (flagged_by) REFERENCES cse360users(userName))";
	    statement.execute(flaggedItemsTable);

	    // New table for staff comments on flagged items
	    String staffCommentsTable = "CREATE TABLE IF NOT EXISTS staff_comments ("
	            + "id INT AUTO_INCREMENT PRIMARY KEY, "
	            + "flagged_item_id INT NOT NULL, "
	            + "staff_member VARCHAR(255) NOT NULL, "
	            + "comment_text TEXT NOT NULL, "
	            + "timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP, "
	            + "FOREIGN KEY (flagged_item_id) REFERENCES flagged_items(id), "
	            + "FOREIGN KEY (staff_member) REFERENCES cse360users(userName))";
	    statement.execute(staffCommentsTable);

	    String reviewerWeightsTable = "CREATE TABLE IF NOT EXISTS reviewer_weights ("
	            + "student_id VARCHAR(255), "
	            + "reviewer_id VARCHAR(255), "
	            + "weight_value FLOAT DEFAULT 1.0, "
	            + "PRIMARY KEY (student_id, reviewer_id), "
	            + "FOREIGN KEY (student_id) REFERENCES cse360users(userName), "
	            + "FOREIGN KEY (reviewer_id) REFERENCES cse360users(userName))";
	    statement.execute(reviewerWeightsTable);

	    String trustedReviewersTable = "CREATE TABLE IF NOT EXISTS trusted_reviewers ("
	            + "student_id VARCHAR(255), "
	            + "reviewer_id VARCHAR(255), "
	            + "PRIMARY KEY (student_id, reviewer_id), "
	            + "FOREIGN KEY (student_id) REFERENCES cse360users(userName), "
	            + "FOREIGN KEY (reviewer_id) REFERENCES cse360users(userName))";
	    statement.execute(trustedReviewersTable);
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
    String query = "SELECT q.id, q.userName, q.question, " +
                   "(CASE WHEN EXISTS (SELECT 1 FROM answers a WHERE a.question = q.question AND a.resolved = TRUE) " +
                   "THEN TRUE ELSE FALSE END) AS resolved_status " +
                   "FROM questions q";

    try (PreparedStatement stmt = connection.prepareStatement(query);
         ResultSet rs = stmt.executeQuery()) {
        while (rs.next()) {
            int id = rs.getInt("id");
            String user = rs.getString("userName");
            String questionText = rs.getString("question");
            boolean isResolved = rs.getBoolean("resolved_status");

            questions.add(new Question(id, user, questionText, isResolved));
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
	public List<Map<String, Object>> getAllReviewsByReviewer(String reviewerId) throws SQLException {
	    List<Map<String, Object>> reviews = new ArrayList<>();
	    String query = "SELECT * FROM reviews WHERE reviewer_id = ?";
	    try (PreparedStatement pstmt = connection.prepareStatement(query)) {
	        pstmt.setString(1, reviewerId);
	        ResultSet rs = pstmt.executeQuery();
	        while (rs.next()) {
	            Map<String, Object> review = new HashMap<>();
	            review.put("id", rs.getInt("id"));
	            review.put("question_id", rs.getInt("question_id"));
	            review.put("review_text", rs.getString("review_text"));
	            review.put("rating", rs.getInt("rating"));
	            review.put("timestamp", rs.getTimestamp("timestamp"));
	            reviews.add(review);
	        }
	    }
	    return reviews;
	}

	public boolean updateReview(int reviewId, String newText) throws SQLException {
	    String query = "UPDATE reviews SET review_text = ? WHERE id = ?";
	    try (PreparedStatement pstmt = connection.prepareStatement(query)) {
	        pstmt.setString(1, newText);
	        pstmt.setInt(2, reviewId);
	        return pstmt.executeUpdate() > 0;
	    }
	}

	public boolean deleteReview(int reviewId) throws SQLException {
	    String query = "DELETE FROM reviews WHERE id = ?";
	    try (PreparedStatement pstmt = connection.prepareStatement(query)) {
	        pstmt.setInt(1, reviewId);
	        return pstmt.executeUpdate() > 0;
	    }
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

	// Review Management Methods
	public boolean addReview(int questionId, int answerId, String reviewerId, String reviewText, int rating) throws SQLException {
	    String query = "INSERT INTO reviews (question_id, answer_id, reviewer_id, review_text, rating) VALUES (?, ?, ?, ?, ?)";
	    try (PreparedStatement pstmt = connection.prepareStatement(query)) {
	        pstmt.setInt(1, questionId);
	        pstmt.setInt(2, answerId);
	        pstmt.setString(3, reviewerId);
	        pstmt.setString(4, reviewText);
	        pstmt.setInt(5, rating);
	        return pstmt.executeUpdate() > 0;
	    }
	}

	public List<Map<String, Object>> getReviewsForQuestion(int questionId) throws SQLException {
	    List<Map<String, Object>> reviews = new ArrayList<>();
	    String query = "SELECT r.*, u.userName as reviewer_name FROM reviews r " +
	                  "JOIN cse360users u ON r.reviewer_id = u.userName " +
	                  "WHERE r.question_id = ?";
	    try (PreparedStatement pstmt = connection.prepareStatement(query)) {
	        pstmt.setInt(1, questionId);
	        ResultSet rs = pstmt.executeQuery();
	        while (rs.next()) {
	            Map<String, Object> review = new HashMap<>();
	            review.put("id", rs.getInt("id"));
	            review.put("reviewer_name", rs.getString("reviewer_name"));
	            review.put("review_text", rs.getString("review_text"));
	            review.put("rating", rs.getInt("rating"));
	            review.put("timestamp", rs.getTimestamp("timestamp"));
	            reviews.add(review);
	        }
	    }
	    return reviews;
	}

	// Reviewer Permission Methods
	public boolean requestReviewerPermission(String userId) throws SQLException {
	    String query = "INSERT INTO reviewer_permissions (user_id, is_approved) VALUES (?, false)";
	    try (PreparedStatement pstmt = connection.prepareStatement(query)) {
	        pstmt.setString(1, userId);
	        return pstmt.executeUpdate() > 0;
	    }
	}

	public boolean approveReviewer(String userId, String approvedBy) throws SQLException {
	    String query = "UPDATE reviewer_permissions SET is_approved = true, approved_by = ?, approval_date = CURRENT_TIMESTAMP WHERE user_id = ?";
	    try (PreparedStatement pstmt = connection.prepareStatement(query)) {
	        pstmt.setString(1, approvedBy);
	        pstmt.setString(2, userId);
	        return pstmt.executeUpdate() > 0;
	    }
	}

	// Private Messaging Methods
	public boolean sendPrivateMessage(String senderId, String receiverId, String messageText) throws SQLException {
	    String query = "INSERT INTO private_messages (sender_id, receiver_id, message_text) VALUES (?, ?, ?)";
	    try (PreparedStatement pstmt = connection.prepareStatement(query)) {
	        pstmt.setString(1, senderId);
	        pstmt.setString(2, receiverId);
	        pstmt.setString(3, messageText);
	        return pstmt.executeUpdate() > 0;
	    }
	}

	public List<Map<String, Object>> getPrivateMessages(String userId) throws SQLException {
	    List<Map<String, Object>> messages = new ArrayList<>();
	    String query = "SELECT * FROM private_messages WHERE sender_id = ? OR receiver_id = ? ORDER BY timestamp DESC";
	    try (PreparedStatement pstmt = connection.prepareStatement(query)) {
	        pstmt.setString(1, userId);
	        pstmt.setString(2, userId);
	        ResultSet rs = pstmt.executeQuery();
	        while (rs.next()) {
	            Map<String, Object> message = new HashMap<>();
	            message.put("id", rs.getInt("id"));
	            message.put("sender_id", rs.getString("sender_id"));
	            message.put("receiver_id", rs.getString("receiver_id"));
	            message.put("message_text", rs.getString("message_text"));
	            message.put("timestamp", rs.getTimestamp("timestamp"));
	            message.put("is_read", rs.getBoolean("is_read"));
	            messages.add(message);
	        }
	    }
	    return messages;
	}

	// Trusted Reviewers Methods
	public boolean addTrustedReviewer(String studentId, String reviewerId) throws SQLException {
	    String query = "INSERT INTO trusted_reviewers (student_id, reviewer_id) VALUES (?, ?)";
	    try (PreparedStatement pstmt = connection.prepareStatement(query)) {
	        pstmt.setString(1, studentId);
	        pstmt.setString(2, reviewerId);
	        return pstmt.executeUpdate() > 0;
	    }
	}

	public List<String> getTrustedReviewers(String studentId) throws SQLException {
	    List<String> reviewers = new ArrayList<>();
	    String query = "SELECT reviewer_id FROM trusted_reviewers WHERE student_id = ?";
	    try (PreparedStatement pstmt = connection.prepareStatement(query)) {
	        pstmt.setString(1, studentId);
	        ResultSet rs = pstmt.executeQuery();
	        while (rs.next()) {
	            reviewers.add(rs.getString("reviewer_id"));
	        }
	    }
	    return reviewers;
	}

	// Reviewer Weight Methods
	public boolean setReviewerWeight(String studentId, String reviewerId, float weight) throws SQLException {
	    String query = "INSERT INTO reviewer_weights (student_id, reviewer_id, weight_value) VALUES (?, ?, ?) " +
	                  "ON DUPLICATE KEY UPDATE weight_value = ?";
	    try (PreparedStatement pstmt = connection.prepareStatement(query)) {
	        pstmt.setString(1, studentId);
	        pstmt.setString(2, reviewerId);
	        pstmt.setFloat(3, weight);
	        pstmt.setFloat(4, weight);
	        return pstmt.executeUpdate() > 0;
	    }
	}

	public float getReviewerWeight(String studentId, String reviewerId) throws SQLException {
	    String query = "SELECT weight_value FROM reviewer_weights WHERE student_id = ? AND reviewer_id = ?";
	    try (PreparedStatement pstmt = connection.prepareStatement(query)) {
	        pstmt.setString(1, studentId);
	        pstmt.setString(2, reviewerId);
	        ResultSet rs = pstmt.executeQuery();
	        if (rs.next()) {
	            return rs.getFloat("weight_value");
	        }
	    }
	    return 1.0f; // Default weight if not set
	}

	// Instructor Review Management Methods
	public boolean hasPendingReviewerRequest(String userName) throws SQLException {
	    String query = "SELECT is_approved FROM reviewer_permissions WHERE user_id = ?";
	    try (PreparedStatement pstmt = connection.prepareStatement(query)) {
	        pstmt.setString(1, userName);
	        ResultSet rs = pstmt.executeQuery();
	        if (rs.next()) {
	            return !rs.getBoolean("is_approved");
	        }
	    }
	    return false;
	}

	public User getUserByUsername(String userName) throws SQLException {
	    String query = "SELECT * FROM cse360users WHERE userName = ?";
	    try (PreparedStatement pstmt = connection.prepareStatement(query)) {
	        pstmt.setString(1, userName);
	        ResultSet rs = pstmt.executeQuery();
	        if (rs.next()) {
	            return new User(
	                rs.getString("userName"),
	                rs.getString("password"),
	                rs.getString("role"),
	                rs.getString("email")
	            );
	        }
	    }
	    return null;
	}

	public boolean rejectReviewerRequest(String userName) throws SQLException {
	    String query = "DELETE FROM reviewer_permissions WHERE user_id = ?";
	    try (PreparedStatement pstmt = connection.prepareStatement(query)) {
	        pstmt.setString(1, userName);
	        return pstmt.executeUpdate() > 0;
	    }
	}

	/**
	 * Flags a question or answer as concerning
	 * @param itemType 'question' or 'answer'
	 * @param itemId ID of the question or answer
	 * @param flaggedBy Username of the staff member who flagged it
	 * @param reason Reason for flagging
	 * @return true if successful
	 */
	public boolean flagItem(String itemType, int itemId, String flaggedBy, String reason) throws SQLException {
	    reconnectIfNeeded();
	    String sql = "INSERT INTO flagged_items (item_type, item_id, flagged_by, flag_reason) VALUES (?, ?, ?, ?)";
	    try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
	        pstmt.setString(1, itemType);
	        pstmt.setInt(2, itemId);
	        pstmt.setString(3, flaggedBy);
	        pstmt.setString(4, reason);
	        return pstmt.executeUpdate() > 0;
	    }
	}

	/**
	 * Adds a staff comment to a flagged item
	 * @param flaggedItemId ID of the flagged item
	 * @param staffMember Username of the staff member
	 * @param commentText The comment text
	 * @return true if successful
	 */
	public boolean addStaffComment(int flaggedItemId, String staffMember, String commentText) throws SQLException {
	    reconnectIfNeeded();
	    String sql = "INSERT INTO staff_comments (flagged_item_id, staff_member, comment_text) VALUES (?, ?, ?)";
	    try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
	        pstmt.setInt(1, flaggedItemId);
	        pstmt.setString(2, staffMember);
	        pstmt.setString(3, commentText);
	        return pstmt.executeUpdate() > 0;
	    }
	}

	/**
	 * Updates the status of a flagged item
	 * @param flaggedItemId ID of the flagged item
	 * @param newStatus New status ('open' or 'resolved')
	 * @return true if successful
	 */
	public boolean updateFlaggedItemStatus(int flaggedItemId, String newStatus) throws SQLException {
	    reconnectIfNeeded();
	    String sql = "UPDATE flagged_items SET status = ? WHERE id = ?";
	    try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
	        pstmt.setString(1, newStatus);
	        pstmt.setInt(2, flaggedItemId);
	        return pstmt.executeUpdate() > 0;
	    }
	}

	/**
	 * Gets all flagged items with their comments
	 * @return List of flagged items with their details and comments
	 */
	public List<Map<String, Object>> getAllFlaggedItems() throws SQLException {
	    reconnectIfNeeded();
	    List<Map<String, Object>> flaggedItems = new ArrayList<>();
	    
	    // First get all flagged items
	    String sql = "SELECT * FROM flagged_items ORDER BY timestamp DESC";
	    try (Statement stmt = connection.createStatement();
	         ResultSet rs = stmt.executeQuery(sql)) {
	        while (rs.next()) {
	            Map<String, Object> item = new HashMap<>();
	            int flaggedItemId = rs.getInt("id");
	            item.put("id", flaggedItemId);
	            item.put("item_type", rs.getString("item_type"));
	            item.put("item_id", rs.getInt("item_id"));
	            item.put("flagged_by", rs.getString("flagged_by"));
	            item.put("flag_reason", rs.getString("flag_reason"));
	            item.put("status", rs.getString("status"));
	            item.put("timestamp", rs.getTimestamp("timestamp"));
	            
	            // Get comments for this flagged item
	            List<Map<String, String>> comments = new ArrayList<>();
	            String commentSql = "SELECT * FROM staff_comments WHERE flagged_item_id = ? ORDER BY timestamp DESC";
	            try (PreparedStatement pstmt = connection.prepareStatement(commentSql)) {
	                pstmt.setInt(1, flaggedItemId);
	                ResultSet commentRs = pstmt.executeQuery();
	                while (commentRs.next()) {
	                    Map<String, String> comment = new HashMap<>();
	                    comment.put("staff_member", commentRs.getString("staff_member"));
	                    comment.put("comment_text", commentRs.getString("comment_text"));
	                    comment.put("timestamp", commentRs.getTimestamp("timestamp").toString());
	                    comments.add(comment);
	                }
	            }
	            item.put("comments", comments);
	            
	            flaggedItems.add(item);
	        }
	    }
	    return flaggedItems;
	}

	/**
	 * Gets the original question text for a flagged question
	 * @param questionId ID of the question
	 * @return The question text, or null if not found
	 */
	public String getQuestionText(int questionId) throws SQLException {
	    reconnectIfNeeded();
	    String sql = "SELECT question FROM questions WHERE id = ?";
	    try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
	        pstmt.setInt(1, questionId);
	        ResultSet rs = pstmt.executeQuery();
	        if (rs.next()) {
	            return rs.getString("question");
	        }
	    }
	    return null;
	}

	/**
	 * Gets the original answer text for a flagged answer
	 * @param answerId ID of the answer
	 * @return The answer text, or null if not found
	 */
	public String getAnswerText(int answerId) throws SQLException {
	    reconnectIfNeeded();
	    String sql = "SELECT answer FROM answers WHERE id = ?";
	    try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
	        pstmt.setInt(1, answerId);
	        ResultSet rs = pstmt.executeQuery();
	        if (rs.next()) {
	            return rs.getString("answer");
	        }
	    }
	    return null;
	}

	/**
	 * Gets all answers from the database
	 * @return List of maps containing answer details
	 */
	public List<Map<String, Object>> getAllAnswers() throws SQLException {
	    reconnectIfNeeded();
	    List<Map<String, Object>> answers = new ArrayList<>();
	    String sql = "SELECT id, username, answer FROM answers";
	    try (Statement stmt = connection.createStatement();
	         ResultSet rs = stmt.executeQuery(sql)) {
	        while (rs.next()) {
	            Map<String, Object> answer = new HashMap<>();
	            answer.put("id", rs.getInt("id"));
	            answer.put("username", rs.getString("username"));
	            answer.put("answer", rs.getString("answer"));
	            answers.add(answer);
	        }
	    }
	    return answers;
	}

}
