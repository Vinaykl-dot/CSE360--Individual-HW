package Automated_Tests;

import databasePart1.DatabaseHelper;
import java.sql.SQLException;
import java.util.List;

public class AnswerUnitTests {
	
	
	static int numPassed = 0;  // Counter for passed tests
    static int numFailed = 0;  // Counter for failed tests
    static DatabaseHelper dbHelper = new DatabaseHelper(); // Database Helper instance
    
	
	public static void main(String[] args) {
        try {
            dbHelper.connectToDatabase(); // Ensure database is connected

            // Run test cases
            performTestCase(1, "Testing adding an answer", () -> testAddAnswer());
            performTestCase(2, "Testing getting answers for a question", () -> testGetAnswersForQuestion());
            performTestCase(3, "Testing updating an answer", () -> testUpdateAnswer());
            performTestCase(4, "Testing deleting an answer", () -> testDeleteAnswer());
            performTestCase(5, "Testing marking an answer as resolved", () -> testMarkAnswerAsResolved());
            performTestCase(6, "Testing getting the resolved answer", () -> testGetResolvedAnswer());

        } catch (SQLException e) {
            e.printStackTrace();
        }
	
        
        // Display test summary
        System.out.println("____________________________________________________________________________");
        System.out.println("\nNumber of tests passed: " + numPassed);
        System.out.println("Number of tests failed: " + numFailed);
    }
	
	
	// This method executes a test case, prints test results, and tracks numPassed and numFailed
	private static void performTestCase(int testCase, String inputText, TestMethod method) {
		
		// Display test case header
		System.out.println("____________________________________________________________________________\n");
		System.out.println("Test case: " + testCase + ": " + inputText);
		
		try {
			boolean result = method.run();
			if(result) {
				System.out.println("Success: " + inputText + " passed.");
				numPassed++;
			} else {
				System.out.println("Failure: " + inputText + " failed.");
				numFailed++;
			}
		} catch (Exception e) {
			System.out.println("Exception, the test case found error: " + e.getMessage());
			numFailed++;
		}
	}
	
	
    @FunctionalInterface
    interface TestMethod {
        boolean run() throws SQLException;
    }
    
    
    // (1) Testing adding an answer
    private static boolean testAddAnswer() throws SQLException {
        String question = "What is a good question?";
        String user = "testUser";
        String answer = "This is an answer.";
        dbHelper.addAnswer(question, user, answer);

        // Ensure answer exists in database
        List<String> answers = dbHelper.getAnswersForQuestion(question);
        return answers.contains(user + ": " + answer);
    }

    // (2) Testing getting answers for a question
    private static boolean testGetAnswersForQuestion() throws SQLException {
        String question = "What is a good question?";
        List<String> answers = dbHelper.getAnswersForQuestion(question);
        return !answers.isEmpty(); // If answers exist, test passes
    }

    // (3) Testing updating an answer
    private static boolean testUpdateAnswer() throws SQLException {
        String user = "testUser";
        String oldAnswer = "This is the old answer.";
        String newAnswer = "This is the new answer.";

        boolean updated = dbHelper.updateAnswer(user, oldAnswer, newAnswer);
        List<String> answers = dbHelper.getAnswersForQuestion("What is a good question?");
        return updated && answers.contains(user + ": " + newAnswer);
    }

    // (4) Testing deleting an answer
    private static boolean testDeleteAnswer() throws SQLException {
        String user = "testUser";
        String answer = "This is an answer.";

        boolean deleted = dbHelper.deleteAnswer(user, answer);
        List<String> answers = dbHelper.getAnswersForQuestion("What is a good question?");
        return deleted && !answers.contains(user + ": " + answer);
    }

    // (5) Testing marking an answer as resolved
    private static boolean testMarkAnswerAsResolved() throws SQLException {
        String question = "What is a good question?";
        String answer = "This is an answer.";
        dbHelper.addAnswer(question, "testUser", answer);

        dbHelper.markAnswerAsResolved(question, answer);
        String resolvedAnswer = dbHelper.getResolvedAnswer(question);
        return resolvedAnswer != null && resolvedAnswer.equals(answer);
    }

    // (6) Testing getting the resolved answer
    private static boolean testGetResolvedAnswer() throws SQLException {
        String question = "What is a good question?";
        String resolvedAnswer = dbHelper.getResolvedAnswer(question);
        return resolvedAnswer != null; // If a resolved answer exists, test passes
    }
}
