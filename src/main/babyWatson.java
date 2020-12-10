/*
 * Sarah Stueve
 * CSC 583 Final Project
 * Mihai Surdeanu
 * Fall 2020
 */

package main;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.io.File;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.similarities.ClassicSimilarity;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;


public class babyWatson {
	List<List<ResultClassWatson>> ans;
	List<Question> parsedQs = new ArrayList<Question>();
	
	public babyWatson(String fileName, Directory index, boolean setSearch, boolean stem, boolean lemma) throws FileNotFoundException, IOException {
		parsedQs = questionParser(fileName, stem, lemma);
		ans = QuestionAnswerer(parsedQs, index, setSearch);
		
	}
	
	public List<Question> questionParser(String fileName, boolean stem, boolean lemma) throws java.io.FileNotFoundException,java.io.IOException {
		
		try (FileInputStream fs = new FileInputStream (new File(fileName))){
			String questions = index.getFileContent(fs, "utf-8");
			String[] qList = questions.split("(\n\n)");
			List<Question> parsedQList = new ArrayList<Question>();
			int id = 0;
			for (String question : qList) {
				// normalize question text
				// split up different components of question, combine
				// category and question text
				String[] splitQ = question.split("\n");
				String category = splitQ[0].replaceAll("[^A-Za-z0-9@ ]", "").toLowerCase();
				String questionText = category + splitQ[1].replaceAll("[^A-Za-z0-9@ ]", "").toLowerCase();
				String updatedQText = "";
				if (stem && lemma) {
					Analyzer analyzer = new EnglishAnalyzer();
					updatedQText = index.stemmer(analyzer, index.lemmatize(analyzer, questionText));
				}else if (stem) {
					Analyzer analyzer = new StandardAnalyzer();
					updatedQText = index.stemmer(analyzer, questionText);
				}else if (lemma) {
					Analyzer analyzer = new EnglishAnalyzer();
					updatedQText = index.lemmatize(analyzer, questionText);
				}else {
					updatedQText = questionText;
				}
				String[] splitQText = updatedQText.split(" ");
				List<String> al = new ArrayList<String>();
				al.addAll(Arrays.asList(splitQText));
				String answer = splitQ[2];
				// add question object to parsedQList
				parsedQList.add(new Question(al, answer, id));
				id += 1;
			}
			return parsedQList;
		}
		
	}
	
	public List<List<ResultClassWatson>> QuestionAnswerer(List<Question> questions, Directory index, boolean setSearch) {
		List<List<ResultClassWatson>> ans=new ArrayList<List<ResultClassWatson>>();
    	StandardAnalyzer analyzer = new StandardAnalyzer();
    	try {
    		for (Question question : questions) {
    			String queryStr = String.join(" ", question.questionText);
    			Query q = new QueryParser("text", analyzer).parse(queryStr);
    			
    			//Search index
    			int hitsPerPage = 10;
    			IndexReader reader = DirectoryReader.open(index);
    			IndexSearcher searcher = new IndexSearcher(reader);
    			if (setSearch) {
    				searcher.setSimilarity(new ClassicSimilarity());
    			}
    			TopDocs docs = searcher.search(q, hitsPerPage);
    			ScoreDoc[] hits = docs.scoreDocs;
    			List<ResultClassWatson> temp = new ArrayList<ResultClassWatson>();
    			
    			//Print information and build ans
    			for(int i=0;i<hits.length;++i) {
    			    int wiki = hits[i].doc;
    			    Document d = searcher.doc(wiki);
//    			    System.out.println((i + 1) + ". " + d.get("wikiName") + "\t" + Double.toString(hits[i].score));
    			    temp.add(new ResultClassWatson(question.qID, d, hits[i].score));
    			}
    			ans.add(temp);
    		}
    		}catch (ParseException e) {
    			e.printStackTrace();
    		} catch (IOException ioe) {
				ioe.printStackTrace();
			}
    		
    	
        return ans;
	}
	
	
}
