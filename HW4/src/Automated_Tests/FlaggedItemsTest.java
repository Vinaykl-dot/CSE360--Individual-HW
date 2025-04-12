package Automated_Tests;

import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;
import org.junit.After;

import databasePart1.DatabaseHelper;
import application.Question;
import application.User;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/**
 * Test class for the flagging system functionality in the Q&A platform.
 * This class tests the ability to flag questions and answers, add comments,
 * resolve flags, and manage access control for staff members.
 * 
 * The test suite includes:
 * - Flagging questions and answers
 * - Adding and managing comments on flagged items
 * - Resolving flagged items
 * - Access control for non-staff users
 * - Retrieving original content of flagged items
 * 
 * @author [Your Name]
 * @version 1.0
 */
public class FlaggedItemsTest {
    private DatabaseHelper dbHelper;
    private String testStaffUser = "testStaff";
    private String testQuestion = "Test Question";
    private String testAnswer = "Test Answer";
    private int questionId;
    private int answerId;

    /**
     * Sets up the test environment before each test.
     * Creates a test staff user, a test question, and a test answer in the database.
     * 
     * @throws SQLException if there is an error connecting to or querying the database
     */
    @Before
    public void setUp() throws SQLException {
        dbHelper = new DatabaseHelper();
        dbHelper.connectToDatabase();
        
        // Create a test staff user
        User staffUser = new User(testStaffUser, "password", "staff", "staff@test.com");
        dbHelper.register(staffUser);

        // Create a test question
        dbHelper.addQuestion(testStaffUser, testQuestion);
        List<Question> questions = dbHelper.getAllQuestions();
        questionId = questions.get(questions.size() - 1).getId();

        // Create a test answer
        dbHelper.addAnswer(testQuestion, testStaffUser, testAnswer);
        List<Map<String, Object>> answers = dbHelper.getAllAnswers();
        answerId = (Integer) answers.get(answers.size() - 1).get("id");
    }

    /**
     * Tests the ability to flag a question.
     * Verifies that:
     * - A staff member can flag a question
     * - The flag is properly stored in the database
     * - The flag status is initially set to "open"
     * 
     * @throws SQLException if there is an error accessing the database
     */
    @Test
    public void testFlagQuestion() throws SQLException {
        // Test flagging a question
        boolean flagged = dbHelper.flagItem("question", questionId, testStaffUser, "Inappropriate content");
        assertTrue("Should successfully flag a question", flagged);

        // Verify the flagged item exists
        List<Map<String, Object>> flaggedItems = dbHelper.getAllFlaggedItems();
        boolean found = false;
        for (Map<String, Object> item : flaggedItems) {
            if (item.get("item_type").equals("question") && 
                (Integer)item.get("item_id") == questionId) {
                found = true;
                assertEquals("Inappropriate content", item.get("flag_reason"));
                assertEquals("open", item.get("status"));
                break;
            }
        }
        assertTrue("Flagged question should be in the database", found);
    }

    /**
     * Tests the ability to flag an answer.
     * Verifies that:
     * - A staff member can flag an answer
     * - The flag is properly stored in the database
     * - The flag status is initially set to "open"
     * 
     * @throws SQLException if there is an error accessing the database
     */
    @Test
    public void testFlagAnswer() throws SQLException {
        // Test flagging an answer
        boolean flagged = dbHelper.flagItem("answer", answerId, testStaffUser, "Wrong information");
        assertTrue("Should successfully flag an answer", flagged);

        // Verify the flagged item exists
        List<Map<String, Object>> flaggedItems = dbHelper.getAllFlaggedItems();
        boolean found = false;
        for (Map<String, Object> item : flaggedItems) {
            if (item.get("item_type").equals("answer") && 
                (Integer)item.get("item_id") == answerId) {
                found = true;
                assertEquals("Wrong information", item.get("flag_reason"));
                assertEquals("open", item.get("status"));
                break;
            }
        }
        assertTrue("Flagged answer should be in the database", found);
    }

    /**
     * Tests the ability to add comments to flagged items.
     * Verifies that:
     * - Comments can be added to a flagged item
     * - Comment text and author are properly stored
     * - Comments can be retrieved with the flagged item
     * 
     * @throws SQLException if there is an error accessing the database
     */
    @Test
    public void testAddComment() throws SQLException {
        // First flag an item
        dbHelper.flagItem("question", questionId, testStaffUser, "Test reason");
        List<Map<String, Object>> flaggedItems = dbHelper.getAllFlaggedItems();
        int flaggedItemId = (Integer) flaggedItems.get(0).get("id");

        // Test adding a comment
        String commentText = "This is a test comment";
        boolean commented = dbHelper.addStaffComment(flaggedItemId, testStaffUser, commentText);
        assertTrue("Should successfully add a comment", commented);

        // Verify the comment exists
        flaggedItems = dbHelper.getAllFlaggedItems();
        boolean found = false;
        for (Map<String, Object> item : flaggedItems) {
            if ((Integer)item.get("id") == flaggedItemId) {
                @SuppressWarnings("unchecked")
                List<Map<String, String>> comments = (List<Map<String, String>>) item.get("comments");
                assertNotNull("Comments list should not be null", comments);
                assertFalse("Comments list should not be empty", comments.isEmpty());
                assertEquals(commentText, comments.get(0).get("comment_text"));
                assertEquals(testStaffUser, comments.get(0).get("staff_member"));
                found = true;
                break;
            }
        }
        assertTrue("Comment should be found in the database", found);
    }

    /**
     * Tests the ability to resolve a flagged item.
     * Verifies that:
     * - A flagged item's status can be updated to "resolved"
     * - The status change is properly stored in the database
     * 
     * @throws SQLException if there is an error accessing the database
     */
    @Test
    public void testResolveFlag() throws SQLException {
        // First flag an item
        dbHelper.flagItem("question", questionId, testStaffUser, "Test reason");
        List<Map<String, Object>> flaggedItems = dbHelper.getAllFlaggedItems();
        int flaggedItemId = (Integer) flaggedItems.get(0).get("id");

        // Test resolving the flag
        boolean resolved = dbHelper.updateFlaggedItemStatus(flaggedItemId, "resolved");
        assertTrue("Should successfully resolve the flag", resolved);

        // Verify the status is updated
        flaggedItems = dbHelper.getAllFlaggedItems();
        boolean found = false;
        for (Map<String, Object> item : flaggedItems) {
            if ((Integer)item.get("id") == flaggedItemId) {
                assertEquals("resolved", item.get("status"));
                found = true;
                break;
            }
        }
        assertTrue("Resolved status should be found in the database", found);
    }

    /**
     * Tests the ability to retrieve original content of flagged items.
     * Verifies that:
     * - Original question text can be retrieved
     * - Original answer text can be retrieved
     * - Retrieved content matches the original content
     * 
     * @throws SQLException if there is an error accessing the database
     */
    @Test
    public void testGetOriginalContent() throws SQLException {
        // Test getting question text
        String questionText = dbHelper.getQuestionText(questionId);
        assertEquals("Should retrieve original question text", testQuestion, questionText);

        // Test getting answer text
        String answerText = dbHelper.getAnswerText(answerId);
        assertEquals("Should retrieve original answer text", testAnswer, answerText);
    }

    /**
     * Tests access control for non-staff users.
     * Verifies that:
     * - Non-staff users cannot flag items
     * - The system properly enforces role-based access control
     * 
     * @throws SQLException if there is an error accessing the database
     */
    @Test
    public void testNonStaffCannotFlag() throws SQLException {
        // Create a non-staff user
        String nonStaffUser = "testStudent";
        User studentUser = new User(nonStaffUser, "password", "student", "student@test.com");
        dbHelper.register(studentUser);

        try {
            // Attempt to flag a question as non-staff user
            boolean flagged = dbHelper.flagItem("question", questionId, nonStaffUser, "Test reason");
            assertFalse("Non-staff user should not be able to flag items", flagged);
        } finally {
            // Clean up
            dbHelper.deleteUser(nonStaffUser);
        }
    }

    /**
     * Tests the ability to add multiple comments to a flagged item.
     * Verifies that:
     * - Multiple comments can be added to the same flagged item
     * - Comments are stored in chronological order
     * - All comments can be retrieved correctly
     * 
     * @throws SQLException if there is an error accessing the database
     */
    @Test
    public void testMultipleComments() throws SQLException {
        // Flag an item
        dbHelper.flagItem("question", questionId, testStaffUser, "Test reason");
        List<Map<String, Object>> flaggedItems = dbHelper.getAllFlaggedItems();
        int flaggedItemId = (Integer) flaggedItems.get(0).get("id");

        // Add multiple comments
        String[] comments = {"First comment", "Second comment", "Third comment"};
        for (String comment : comments) {
            boolean added = dbHelper.addStaffComment(flaggedItemId, testStaffUser, comment);
            assertTrue("Should successfully add comment: " + comment, added);
        }

        // Verify all comments exist
        flaggedItems = dbHelper.getAllFlaggedItems();
        for (Map<String, Object> item : flaggedItems) {
            if ((Integer)item.get("id") == flaggedItemId) {
                @SuppressWarnings("unchecked")
                List<Map<String, String>> commentsList = (List<Map<String, String>>) item.get("comments");
                assertNotNull("Comments list should not be null", commentsList);
                assertEquals("Should have all comments", comments.length, commentsList.size());
                // Comments are stored in reverse order (newest first)
                for (int i = 0; i < comments.length; i++) {
                    assertEquals(comments[comments.length - 1 - i], commentsList.get(i).get("comment_text"));
                }
                break;
            }
        }
    }

    /**
     * Cleans up the test environment after each test.
     * - Resolves any flagged items created by the test
     * - Deletes the test user and their content
     * - Closes the database connection
     * 
     * @throws SQLException if there is an error accessing the database
     */
    @After
    public void tearDown() throws SQLException {
        // Clean up test data
        try {
            // Delete flagged items first (due to foreign key constraints)
            List<Map<String, Object>> flaggedItems = dbHelper.getAllFlaggedItems();
            for (Map<String, Object> item : flaggedItems) {
                if (item.get("flagged_by").equals(testStaffUser)) {
                    dbHelper.updateFlaggedItemStatus((Integer)item.get("id"), "resolved");
                }
            }

            // Delete test user and their content
            dbHelper.deleteUser(testStaffUser);
        } finally {
            dbHelper.closeConnection();
        }
    }
} 