package Automated_Tests;

import databasePart1.DatabaseHelper;
import java.sql.SQLException;
import java.util.List;
import java.util.ArrayList;
import application.Question;

public class Questions_Test {
	
	
	static int tests_passed = 0;	//Counter for passed test case
	static int tests_failed = 0;	//Counter for failed test cases
	
	static DatabaseHelper dbHelper = new DatabaseHelper();
	
	public static void main(String[] args) {
        try {
            dbHelper.connectToDatabase(); // Ensure database is connected

            // Run test cases
            performTestCase(1, "Testing adding an question", () -> addQuestion_test());
            performTestCase(2, "Testing getting questions", () -> getQuestion_test());
            performTestCase(3, "Testing updating an question", () -> updateQuestion_test());
            performTestCase(4, "Testing deleting an question", () -> deleteQuestion_test());
            performTestCase(5, "Testing resolving a question", () -> resolvedQuestion_test());

        } catch (SQLException e) {
            e.printStackTrace();
        }
	
        
        // Display test summary
        System.out.println("____________________________________________________________________________");
        System.out.println("\nNumber of tests passed: " + tests_passed);
        System.out.println("Number of tests failed: " + tests_failed);
    }
	
private static void performTestCase(int testCase, String inputText, TestMethod method) {
		
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


	@FunctionalInterface
	interface TestMethod {
	    boolean run() throws SQLException;
	}
	
	//Test isolation deletes current questions in the database 
	private static void delete_all_questions() throws SQLException{
		List<Question> questions =  dbHelper.getAllQuestions();
		for(Question tmp : questions) {
			dbHelper.deleteQuestion(tmp.getUsername(), tmp.getQuestion());
		}
	}
	
	//Tests whether an answer was correctly inserted into the database
	private static boolean addQuestion_test() throws SQLException {
		delete_all_questions();
		
		String question = "TEST QUESTION"; 
		String user = "TEST_USER";
		
		dbHelper.addQuestion(user, question); //Adds question to database
		
		List<Question> questions = dbHelper.getAllQuestions(); //Returns all questions
		
		for(Question tmp : questions) {
			if(tmp.getUsername().equals(user) && tmp.getQuestion().equals(question)) {
				return true; //Checks whether question exists in the database
			}
		}
		return false;
	}
	
	//Tests whether a question is returned from the database
	private static boolean getQuestion_test() throws SQLException{
		delete_all_questions(); //Clears other test questions from database;
		
		int num_found = 0;	//Counts number of questions properly returned
		
		String user = "TEST_USER";
		List<String> test_questions =  new ArrayList<>(); //Stores questions asked for comparison
		
		String question1 = "Question 1";
		dbHelper.addQuestion(user, question1); //adds question to database
		test_questions.add(question1); 		//Adds question to internal list
		
		String question2 = "Question 2";
		dbHelper.addQuestion(user, question2);
		test_questions.add(question2);
		
		String question3 = "Question 3";
		dbHelper.addQuestion(user, question3);
		test_questions.add(question3);
		
		List<Question> questions = dbHelper.getAllQuestions(); 	//Returns all questions
		
		for(int i = 0; i < questions.size(); i++) {
			if(questions.get(i).getQuestion().equals(test_questions.get(i))){ //Checks questions in database list against internal list
				num_found++;
			}
		}
		if(num_found == 3) { //Only returns true if all 3 were found
			return true;
		}
		return false;
	}
	
	//Tests to make sure a question is correctly updated after an edit
	private static boolean updateQuestion_test() throws SQLException{
		delete_all_questions();
	
		String user = "USER";
		String init_question = "INIT QUESTION";
		dbHelper.addQuestion(user, init_question);
		
		String updated_question =  "UPDATED QUESTION";
		
		boolean updated = dbHelper.updateQuestion(user, init_question, updated_question); //Updates question
		
		List<Question> questions = dbHelper.getAllQuestions();
		
		boolean found_updated = false; //updated question exists in list
		boolean found_init = false;	//Initial question also exists in list
		
		for(Question tmp : questions) {
			if(tmp.getQuestion().equals(updated_question)) {
				found_updated = true;
			}
			if(tmp.getQuestion().equals(init_question)) {
				found_init = true;
			}
		}
		return updated && found_updated && !found_init; //passes if updates successfully and initial question is not still in list
	}
	
	//Tests whether a question was properly deleted
	private static boolean deleteQuestion_test() throws SQLException{
		delete_all_questions();
		String user = "USER";
		String question =  "This is a question";
		
		dbHelper.addQuestion(user, question);
		
		boolean delete =  dbHelper.deleteQuestion(user, question);	//Deletes question
		
		if(delete) { 	//Checks if it was properly deleted 
			return true;
		}
		return false;
	}
	
	//Tests whether a question was resolved
	private static boolean resolvedQuestion_test() throws SQLException{
		delete_all_questions();
		String user = "USER";
		String question = "Resolved question";
		
		String answer = "Resolved answer";
		
		dbHelper.addAnswer(question, user, answer);
		dbHelper.markAnswerAsResolved(question, answer);	//Marks an answer to the question as resolved
		
		String resolved = dbHelper.getResolvedAnswer(question);	//Resolved is the answer that was returned as resolved
		
		if(resolved != null && resolved.equals(resolved)) {		//Checks whether an answer was returned and it is the same question that was originally resolved
			return true;
		}
		
		return false;
		
	}
	
}
