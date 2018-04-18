package edu.uw.blioce.IR_HW1;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

import opennlp.tools.stemmer.PorterStemmer;

/**
 * This class serves as the main driver to obtain metrics from a list of test files.
 * It begins by iterating through the text to remove special characters, then it will 
 * stem the words using Apache OpenNLP Tools Porter Stemmer and finally remove all 
 * stopwords. After this is done, it will computer the term frequencies, inverse
 * document frequency, tf*idf, and the probablility for the top 30 words that occurred
 * in all documents. 
 * 
 * @author Brandon Lioce
 * @version April 17th, 2018
 * @class Information Retrieval
 * @assignment Homework 1
 *
 */
public class Driver {

	/** This is the directory in the project of the transcripts folder. */
	private static final String DIRECTORY = "src/transcripts/";

	/** A mapping of the term/token to the frequency it has occurred in the documents, stored in a StringCount object. */
	private static Map<String, StringCount> wordFrequencies = new HashMap<String, StringCount>();
	
	/** A set of all stopwords that are to be removed/not considered in this process. Populated from src/stopwords.txt. */
	private static Set<String> stopwords = new HashSet<String>();

	/**
	 * The main function of this class. Begins the process of scanning the documents and
	 * obtaining aforementioned metrics.
	 * @param args No arguments are used in this class.
	 * @throws IOException Thrown when the given directory or file is not found. 
	 */
	public static void main(String[] args) throws IOException {		
		PorterStemmer s = new PorterStemmer();
		
		// Populate the stopwords set with the words in the file
		Scanner scan = new Scanner(new File("src/stopwords.txt"));
		while(scan.hasNext()) {
			stopwords.add(scan.next());
		}
		scan.close();
		
		// Get a list of all the files in the transcript directory
		File dir = new File(DIRECTORY);
		File[] files = dir.listFiles();
		int totalWordCount = 0;

		
		for(int i = 0; i < files.length; i++) {
			scan = new Scanner(files[i]);

			int wordCount = 0;
			while(scan.hasNext()) {
				
				// Remove special characters from the token
				String token = scan.next().replaceAll("[^\\w\\s]", "").toLowerCase();
				
				// Stem the word using Apache OpenNLP Tools Porter Stemmer
				token = s.stem(token);
				wordCount++;
				
				// If the length of token is greater than 0 (can be 0 if all special characters)
				// and if the token is not a stopword
				if(token.length() > 0 && !stopwords.contains(token)) {
					if(wordFrequencies.containsKey(token)) {
						StringCount sc = wordFrequencies.get(token);
						sc.documents.add(i);
						sc.count++;
					} else {
						Set<Integer> docs = new HashSet<Integer>();
						docs.add(i);
						wordFrequencies.put(token, new StringCount(token, docs, 1));
					}
				}
			}
			scan.close();

			totalWordCount += wordCount;
		}
		
		System.out.println("Total number of words in all documents: " + totalWordCount);
		System.out.println("Total number of unique words: " + wordFrequencies.size());
		
		int singleCount = 0;
		for(StringCount sc: wordFrequencies.values()) {
			if(sc.count == 1) singleCount++;
		}
		
		System.out.println("Words with only 1 occurence across all documents: " + singleCount);
		System.out.println("Average words per document: " + (double)totalWordCount / (files.length + 1));
		
		List<StringCount> list = new ArrayList<StringCount>(wordFrequencies.values());
		
		System.out.println("\nWORD\tTF\tIDF\t\t\tTF*IDF\t\t\tProbability");
		
		Collections.sort(list);
		for(int i = 0; i < 30; i++) {
			StringCount sc = list.get(i);
			double tf = sc.count;
			double idf = Math.log10((double)files.length / sc.documents.size());
			double tf_idf = tf * idf;
			double p = (double)sc.count / totalWordCount;
			System.out.println(sc.word + "\t" + sc.count + "\t" + idf + "\t" + tf_idf + "\t" + p);
		}
	}
}

/**
 * This class is used as an aid to keep track of the documents in which a word 
 * appears and the frequency count of the word 
 * 
 * @author Brandon Lioce
 *
 */
class StringCount implements Comparable<StringCount>{
	String word;
	Set<Integer> documents;
	Integer count;
	
	/**
	 * A constructor for the StringCount object.
	 * @param theWord The word this object will represent.
	 * @param theDocuments The documents in which the word appears.
	 * @param theCount The frequency this word has appeared. 
	 */
	public StringCount(String theWord, Set<Integer> theDocuments, Integer theCount) {
		word = theWord;
		documents = theDocuments;
		count = theCount;
	}
	
	/** 
	 * {@inheritDoc}
	 * This implemented method allows this object to only be compared by its count value.
	 */
	public int compareTo(StringCount o) {
		return o.count.compareTo(count);
	}
	
	/**
	 * {@inheritDoc}
	 * This overridden method prints a string representation of the the object.
	 */
	public String toString() {
		return "{" + word + ":, " + count + ", documents: " + documents.toString() + "}";
	}
}
