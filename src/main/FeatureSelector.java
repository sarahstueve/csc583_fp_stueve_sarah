package main;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.store.Directory;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;

public class FeatureSelector {
	
	public FeatureSelector(Directory index, List<Question> questions) {
		;
	}
	
	public List<Question> getFeatures(Directory index, List<Question> questions, int k) throws IOException {
		
		IndexReader reader = DirectoryReader.open(index);
		HashMap<String, HashMap<String, Integer>> qTermFreqs  = new HashMap<String, HashMap<String, Integer>>();
		HashMap<String, String> wikiText = new HashMap<String, String>();
		// get text for docs related to all questions
		for (Question question : questions) {
			for (int i = 0; i < reader.maxDoc(); i++) {
				Document doc = reader.document(i);
				if (doc.get("wikiName").compareTo(question.correctAnswer) == 0) {
					wikiText.put(doc.get("wikiName"), doc.get("text"));
				}
			}
			qTermFreqs.put(question.correctAnswer, new HashMap<String, Integer>());
			// get counts of terms in questions in doc
			for (String token : question.questionText) {
				qTermFreqs.get(question.correctAnswer).put(token, 
						countSubstring(token, wikiText.get(question.correctAnswer)));
			}
			
			
		}
		
		HashMap<String, HashMap<String, Integer>> qTermFreqsSorted  = new HashMap<String, HashMap<String, Integer>>();
		qTermFreqs.forEach((key, value) -> {
			HashMap<String, Integer> sortedFreqs = sortByValue(value);
			qTermFreqsSorted.put(key, sortedFreqs);
		});
		// would have extracted sorted terms and created new list of Question objects, with questionText
		// limited to those terms.
		
		return questions;
		
		
		
		

	}
	
	public static HashMap<String, Integer> sortByValue(HashMap<String, Integer> hm) {
		// Solution taken from: https://www.geeksforgeeks.org/sorting-a-hashmap-according-to-values/
        // Create a list from elements of HashMap 
        List<Map.Entry<String, Integer> > list = 
        		new LinkedList<Map.Entry<String, Integer> >(hm.entrySet()); 
  
        // Sort the list 
        Collections.sort(list, new Comparator<Map.Entry<String, Integer> >() { 
            public int compare(Map.Entry<String, Integer> o1,  
                               Map.Entry<String, Integer> o2) 
            { 
                return (o1.getValue()).compareTo(o2.getValue()); 
            } 
        }); 
          
        // put data from sorted list to hashmap  
        HashMap<String, Integer> temp = new LinkedHashMap<String, Integer>(); 
        for (Map.Entry<String, Integer> aa : list) { 
            temp.put(aa.getKey(), aa.getValue()); 
        } 
        return temp; 
    } 
	
	public static Integer countSubstring(String sub, String orig) {
		int count = 0;
		int lastIndex = 0;
		while(lastIndex != -1) {
			lastIndex = orig.indexOf(sub, lastIndex);
			if (lastIndex != -1) {
				count++;
				lastIndex += sub.length();
			}
		}
		return count;
	}
	
	

}
