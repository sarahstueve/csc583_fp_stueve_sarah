package main;

import java.util.List;

public class Question {
	List<String> questionText;
	String correctAnswer;
	int qID;
	
	public Question(List<String> inputText, String inputAnswer, int IDNum) {
		questionText = inputText;
		correctAnswer = inputAnswer;
		qID = IDNum;
	}
}
