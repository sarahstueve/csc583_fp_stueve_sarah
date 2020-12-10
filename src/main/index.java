/*
 * Sarah Stueve
 * CSC 583 Final Project
 * Mihai Surdeanu
 * Fall 2020
 */


package main;
import java.io.BufferedReader;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Paths;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;

import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.analysis.en.PorterStemFilter;



public class index {
	Directory index = null;
	boolean indexExists = false;
	String inputFilePath = "";
	
	public index(String inputFile, String idxFilePath, boolean stem, boolean lemmatize) throws FileNotFoundException, IOException {
		inputFilePath = inputFile;
		buildIndex(inputFilePath, idxFilePath, stem, lemmatize);
	}
	
	public void buildIndex(String inputFilePath, String idxFilePath, boolean stem, boolean lemmatize) throws FileNotFoundException, IOException {
		//Get file from resources folder
		Analyzer analyzer = null;
        File dir = new File(inputFilePath);
        File[] directoryListing = dir.listFiles();
        if (directoryListing != null) {
        	for (File doc : directoryListing) {
        		System.out.println(doc);
        		try (FileInputStream fs = new FileInputStream (doc)){
        			String content = getFileContent(fs, "utf-8");
        			// Initialize index
        			String[] docWikis = content.split("(\n\n\n)");
        			index = FSDirectory.open(Paths.get(idxFilePath));
    				// Specify analyzer for tokenizing text
        			if (lemmatize) {
        				analyzer = new EnglishAnalyzer();
        			}else {
        				analyzer = new StandardAnalyzer();
        			}
        			IndexWriterConfig config = new IndexWriterConfig(analyzer);
                	IndexWriter w = new IndexWriter(index, config);
        			// Traverse over wikis in doc, add to index
        			for (String wiki : docWikis) {
        				int splitPos = wiki.indexOf("]]");
        				if (wiki != "") {
        					wiki = wiki.strip();
        					String wikiName = wiki.substring(2, splitPos);
            				String info = wiki.substring(splitPos + 2).replaceAll("CATEGORIES: ", "");
            				String updatedInfo = "";
            				if (stem && lemmatize) {
            					String stemmedInfo = stemmer(analyzer, info);
            					updatedInfo = lemmatize(analyzer, stemmedInfo);
            				}
            				else if (stem) {
            					updatedInfo = stemmer(analyzer, info);
            				}else if (lemmatize) {
            					updatedInfo = lemmatize(analyzer, info);
            				}else {
            					updatedInfo = info;
            				}
            				
            				addDoc(w, updatedInfo, wikiName);
        				}
        			}
        			w.close();	
        		}
                indexExists = true;
            }
        	}
        }
	
	public static String getFileContent(
	   // solution used from: https://stackoverflow.com/questions/15161553/how-to-convert-fileinputstream-into-string-in-java
	   FileInputStream fis,
	   String encoding ) throws IOException {
	   try( BufferedReader br = new BufferedReader( new InputStreamReader(fis, encoding ))){
	      StringBuilder sb = new StringBuilder();
	      String line;
	      while(( line = br.readLine()) != null ) {
	         sb.append( line );
	         sb.append( '\n' );
	      }
	      return sb.toString();
	   }
	}          
	
    private static void addDoc(IndexWriter w, String text, String wikiName) throws IOException {
    	//Adapted from Lucene tutorial
        Document doc = new Document();
        doc.add(new TextField("text", text, Field.Store.YES));
        doc.add(new StringField("wikiName", wikiName, Field.Store.YES));
        w.addDocument(doc);
    }
    
    public static String stemmer(Analyzer analyzer, String wiki) throws IOException{
    	PorterStemFilter stemmer = new PorterStemFilter(analyzer.tokenStream("field", wiki));
    	stemmer.reset();
    	String stemmed = "";
    	while (stemmer.incrementToken()) {
    	    stemmed += stemmer.getAttribute(CharTermAttribute.class).toString() + " ";
    	}
//    	System.out.println(stemmed);
    	stemmer.end();
    	stemmer.close();
    	return stemmed;
    }
    
    public static String lemmatize(Analyzer analyzer, String wiki) throws IOException {
    	
    	TokenStream stream = analyzer.tokenStream("field", wiki);
    	stream.reset();
    	String lemmatized = "";
    	while (stream.incrementToken()) {
    	    lemmatized += stream.getAttribute(CharTermAttribute.class).toString() + " ";
    	}
    	stream.end();
    	stream.close();
//    	System.out.println(lemmatized);
    	return lemmatized;
    }
}

