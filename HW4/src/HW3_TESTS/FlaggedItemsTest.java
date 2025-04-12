package HW3_TESTS;

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

public class FlaggedItemsTest {
    private DatabaseHelper dbHelper;
    private String testStaffUser = "testStaff";
    private String testQuestion = "Test Question";
    private String testAnswer = "Test Answer";
    private int questionId;
    private int answerId;

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

    @Test
    public void testGetOriginalContent() throws SQLException {
        // Test getting question text
        String questionText = dbHelper.getQuestionText(questionId);
        assertEquals("Should retrieve original question text", testQuestion, questionText);

        // Test getting answer text
        String answerText = dbHelper.getAnswerText(answerId);
        assertEquals("Should retrieve original answer text", testAnswer, answerText);
    }

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