package HW3_TESTS;

import databasePart1.DatabaseHelper;
import java.sql.SQLException;
import java.util.List;
import java.util.ArrayList;
import application.Question;

/**
 * A comprehensive test suite for the Question and Answer management system.
 * This class contains automated tests for various database operations including
 * question management and answer handling.
 *
 * <p>The test suite includes the following test cases:
 * <ul>
 *   <li>Question update functionality</li>
 *   <li>Question deletion</li>
 *   <li>Question resolution</li>
 *   <li>Answer addition</li>
 *   <li>Answer retrieval</li>
 * </ul>
 *
 * <p>Each test case is executed independently and reports its success or failure.
 * A summary of all test results is displayed at the end of execution.
 *
 * @see databasePart1.DatabaseHelper
 * @see application.Question
 */
public class Automated_tests {
    
    /** Counter for successfully passed test cases */
    static int tests_passed = 0;
    
    /** Counter for failed test cases */
    static int tests_failed = 0;
    
    /** Database helper instance for performing database operations */
    static DatabaseHelper dbHelper = new DatabaseHelper();
    
    /**
     * Main entry point for the test suite.
     * Executes all test cases and displays the results.
     *
     * <p>The test execution flow:
     * <ol>
     *   <li>Connects to the database</li>
     *   <li>Executes each test case in sequence</li>
     *   <li>Displays individual test results</li>
     *   <li>Shows final summary of passed and failed tests</li>
     * </ol>
     *
     * @param args command line arguments (not used)
     */
    public static void main(String[] args) {
        try {
            dbHelper.connectToDatabase(); // Ensure database is connected

            // Run test cases
            performTestCase(1, "Testing updating a question", () -> updateQuestion_test());
            performTestCase(2, "Testing deleting a question", () -> deleteQuestion_test());
            performTestCase(3, "Testing resolving a question", () -> resolvedQuestion_test());
            performTestCase(4, "Testing adding an answer", () -> testAddAnswer());
            performTestCase(5, "Testing getting answers for a question", () -> testGetAnswersForQuestion());

        } catch (SQLException e) {
            e.printStackTrace();
        }
    
        // Display test summary
        System.out.println("____________________________________________________________________________");
        System.out.println("\nNumber of tests passed: " + tests_passed);
        System.out.println("Number of tests failed: " + tests_failed);
    }
    
    /**
     * Executes a single test case and reports its result.
     *
     * @param testCase the test case number
     * @param inputText description of the test case
     * @param method the test method to execute
     */
    public static void performTestCase(int testCase, String inputText, TestMethod method) {
        
        // Display test case header
        System.out.println("____________________________________________________________________________\n");
        System.out.println("Test case: " + testCase + ": " + inputText);
        
        try {
            boolean result = method.run();
            if(result) {
                System.out.println("Success: " + inputText + " passed.");
                tests_passed++;
            } else {
                System.out.println("Failure: " + inputText + " failed.");
                tests_failed++;
            }
        } catch (Exception e) {
            System.out.println("Exception, the test case found error: " + e.getMessage());
            tests_failed++;
        }
    }

    /**
     * Functional interface for test methods.
     * All test methods must implement this interface.
     */
    @FunctionalInterface
    public interface TestMethod {
        /**
         * Executes the test method.
         *
         * @return true if the test passed, false otherwise
         * @throws SQLException if a database error occurs
         */
        boolean run() throws SQLException;
    }
    
    /**
     * Deletes all questions from the database for test isolation.
     * This method ensures a clean state before running each test.
     *
     * @throws SQLException if a database error occurs
     */
    public static void delete_all_questions() throws SQLException {
        List<Question> questions = dbHelper.getAllQuestions();
        for(Question tmp : questions) {
            dbHelper.deleteQuestion(tmp.getUsername(), tmp.getQuestion());
        }
    }
    
    /**
     * Tests the question update functionality.
     * Verifies that a question can be updated and the old version is removed.
     *
     * <p>Test steps:
     * <ol>
     *   <li>Clears all existing questions</li>
     *   <li>Adds an initial question</li>
     *   <li>Updates the question</li>
     *   <li>Verifies the update was successful</li>
     * </ol>
     *
     * @return true if the update test passes, false otherwise
     * @throws SQLException if a database error occurs
     */
    public static boolean updateQuestion_test() throws SQLException {
        delete_all_questions();
    
        String user = "USER";
        String init_question = "INIT QUESTION";
        dbHelper.addQuestion(user, init_question);
        
        String updated_question = "UPDATED QUESTION";
        
        boolean updated = dbHelper.updateQuestion(user, init_question, updated_question);
        
        List<Question> questions = dbHelper.getAllQuestions();
        
        boolean found_updated = false;
        boolean found_init = false;
        
        for(Question tmp : questions) {
            if(tmp.getQuestion().equals(updated_question)) {
                found_updated = true;
            }
            if(tmp.getQuestion().equals(init_question)) {
                found_init = true;
            }
        }
        return updated && found_updated && !found_init;
    }
    
    /**
     * Tests the question deletion functionality.
     * Verifies that a question can be successfully deleted from the database.
     *
     * <p>Test steps:
     * <ol>
     *   <li>Clears all existing questions</li>
     *   <li>Adds a test question</li>
     *   <li>Attempts to delete the question</li>
     *   <li>Verifies the deletion was successful</li>
     * </ol>
     *
     * @return true if the deletion test passes, false otherwise
     * @throws SQLException if a database error occurs
     */
    public static boolean deleteQuestion_test() throws SQLException {
        delete_all_questions();
        String user = "USER";
        String question = "This is a question";
        
        dbHelper.addQuestion(user, question);
        
        boolean delete = dbHelper.deleteQuestion(user, question);
        
        if(delete) {
            return true;
        }
        return false;
    }
    
    /**
     * Tests the question resolution functionality.
     * Verifies that an answer can be marked as resolved and retrieved.
     *
     * <p>Test steps:
     * <ol>
     *   <li>Clears all existing questions</li>
     *   <li>Adds a test question and answer</li>
     *   <li>Marks the answer as resolved</li>
     *   <li>Verifies the resolved answer can be retrieved</li>
     * </ol>
     *
     * @return true if the resolution test passes, false otherwise
     * @throws SQLException if a database error occurs
     */
    public static boolean resolvedQuestion_test() throws SQLException {
        delete_all_questions();
        String user = "USER";
        String question = "Resolved question";
        
        String answer = "Resolved answer";
        
        dbHelper.addAnswer(question, user, answer);
        dbHelper.markAnswerAsResolved(question, answer);
        
        String resolved = dbHelper.getResolvedAnswer(question);
        
        if(resolved != null && resolved.equals(resolved)) {
            return true;
        }
        
        return false;
    }
    
    /**
     * Tests the answer addition functionality.
     * Verifies that an answer can be successfully added to a question.
     *
     * <p>Test steps:
     * <ol>
     *   <li>Creates a test question</li>
     *   <li>Adds an answer to the question</li>
     *   <li>Verifies the answer was added successfully</li>
     * </ol>
     *
     * @return true if the answer addition test passes, false otherwise
     * @throws SQLException if a database error occurs
     */
    public static boolean testAddAnswer() throws SQLException {
        String question = "What is a good question?";
        String user = "testUser";
        String answer = "This is an answer.";
        dbHelper.addAnswer(question, user, answer);

        List<String> answers = dbHelper.getAnswersForQuestion(question);
        return answers.contains(user + ": " + answer);
    }

    /**
     * Tests the answer retrieval functionality.
     * Verifies that answers can be successfully retrieved for a question.
     *
     * <p>Test steps:
     * <ol>
     *   <li>Creates a test question</li>
     *   <li>Retrieves all answers for the question</li>
     *   <li>Verifies the answers can be retrieved</li>
     * </ol>
     *
     * @return true if the answer retrieval test passes, false otherwise
     * @throws SQLException if a database error occurs
     */
    public static boolean testGetAnswersForQuestion() throws SQLException {
        String question = "What is a good question?";
        List<String> answers = dbHelper.getAnswersForQuestion(question);
        return !answers.isEmpty();
    }
}
