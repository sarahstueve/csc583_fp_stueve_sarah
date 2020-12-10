package main;


import org.apache.lucene.document.Document;

public class ResultClassWatson {
	int qID;
	Document doc;
	float score;
	
	public ResultClassWatson(int id, Document d, float doc_score) {
		qID = id;
		doc = d;
		score = doc_score;
		
	}
}
