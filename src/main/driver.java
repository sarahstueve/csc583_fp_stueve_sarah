/*
 * Sarah Stueve
 * CSC 583 Final Project
 * Mihai Surdeanu
 * Fall 2020
 */
package main;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;

import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

//import index;
public class driver {
	static Directory wikiIndex;
	static List<List<ResultClassWatson>> predictions;
	static List<Question> gold;
	
	public static float calcPrecision(List<List<ResultClassWatson>> predictions, List<Question> gold) {
		float TP = 0;
		float FP = 0;
		for (Question question : gold) {
			for (int i = 0; i < predictions.size(); i++) {
				if (!predictions.get(i).isEmpty()) {
					if (question.qID == predictions.get(i).get(0).qID){
						if (question.correctAnswer.compareTo(predictions.get(i).get(0).doc.get("wikiName")) == 0 ){
							TP += 1;
							
						}else {
							FP += 1;
							// for printing out incorrect answers to look at patterns
//							System.out.printf("Incorrect (Category: %s) :\t%s,\t%s\n", question.questionText.get(0), question.correctAnswer, predictions.get(i).get(0).doc.get("wikiName"));
						}
					}
				}
			}
		}
		System.out.printf("Correct answers: %f", TP);
		System.out.printf("Incorrect answers: %f", FP);
		
		// Return precision calculation
		return TP / (TP + FP);
	}
	
	public static void main(String[] args) throws FileNotFoundException, IOException {
//		String idxFilePath = "src\\resources\\index.lucene";
		String idxFilePath = "src\\resources\\index_stemmed.lucene"; // best index
//		String idxFilePath = "src\\resources\\index_lemmatized.lucene";
//		String idxFilePath = "src\\resources\\index_stem_lemma.lucene";
		boolean stem = true; //set to true to stem data
		boolean lemmatize = false; //set to true to lemmatize data
		File tempfile = new File(idxFilePath);
		if (!tempfile.exists()) {
			index idx = new index("src\\resources\\wiki_data", idxFilePath, stem, lemmatize);
			wikiIndex = idx.index;
		}else {
			wikiIndex = FSDirectory.open(Paths.get(idxFilePath));
		}
		
		// Results when searching using default BM25
		babyWatson jeopardy = new babyWatson("src\\resources\\questions.txt", wikiIndex, false, stem, lemmatize);
		gold = jeopardy.parsedQs;
		predictions = jeopardy.ans;
		float precision1 = calcPrecision(predictions, gold);
		System.out.printf("Precision (BM25): %f\n", precision1);
		
		// Results when searching using tf-idf
//		babyWatson jeopardy2 = new babyWatson("src\\resources\\questions.txt", wikiIndex, true);
//		gold = jeopardy2.parsedQs;
//		predictions = jeopardy2.ans;
//		float precision2 = calcPrecision(predictions, gold);
//		System.out.printf("Precision (tf-idf): %f", precision2);
			
		
	}
	
	
}
