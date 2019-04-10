package edu.carleton.comp4601.archivetools;

import java.io.File;
import java.io.IOException;

import java.util.ArrayList;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

public class ArchiveScoreExtractorTools {
	
	public static ArrayList<Double> getReviewSentimentPairings(String reviewContent) throws IOException {

		//METHOD 1: large loaded work bank
		ArrayList<String> positiveBank = retrievePosSentimentWordBank();
		ArrayList<String> negativeBank = retrieveNegSentimentWordBank();
		
		Double goodCount = 0.0, badCount = 0.0;
			
		//Check fields against sentiment word banks
		for(String fieldGood: positiveBank)
			goodCount += reviewContent.split("\\s?"+fieldGood+".*?\\s").length - 1;
		for(String fieldBad: negativeBank)
			badCount += reviewContent.split("\\s?"+fieldBad+".*?\\s").length - 1;
		
		//Create and then return the new list of sentiment pairings
		ArrayList<Double> SentimentPairings = new ArrayList<Double>();
		SentimentPairings.add(goodCount);
		SentimentPairings.add(badCount);
		return  SentimentPairings;
	}
	
	
	
	
	public static Double getExtractedRating(ArrayList<Double> sentimentPairings) {
		
		Double rating = null;
		Double goodSentimentCount = sentimentPairings.get(0);
		Double badSentimentCount = sentimentPairings.get(1);
		
		//check for one sided sentiment count
		if(!(goodSentimentCount == 0.0 || badSentimentCount == 0.0)) {
		
			Double sentimentRatio = goodSentimentCount / badSentimentCount;
		
			//NEUTRAL
			if(sentimentRatio == 1.0) {
				rating = 2.5;
			}
			
			//GOOD 3-5
			else if(sentimentRatio > 3.0) {
				rating = 5.0;
			}
			else if(sentimentRatio > 1.5) {
				rating = 4.0;
			}
			else if(sentimentRatio > 1.0) {
				rating = 3.0;
			}
			
			//NEGATIVE 0-2
			else if(sentimentRatio > 0.30){
				rating = 2.0;
			}
			else if (sentimentRatio > 0.15){
				rating = 1.0;
			}
			else {
				rating = 0.0;
			}
		}
		
		if(goodSentimentCount == 0.0) {
			rating = 0.0;
		}
		if(badSentimentCount == 0.0) {
			rating = 5.0;
		}
		
		return rating;
	}
	
	
	
	public static ArrayList<String> retrievePosSentimentWordBank() throws IOException {

		//Get positive wordbank string from the parsed file
		String archivePath = "/Users/" + System.getProperty("user.name") + "/Desktop/archive/resources/PositiveWordsVocabularyList-EnchantedLearning.html";
    	File positiveWordbankFile = new File(archivePath);

		//Use Jsoup to collect relevant movie system information required
    	Document jDoc = Jsoup.parse(positiveWordbankFile, "UTF-8");
		Elements positiveWordBank = jDoc.select("div.wordlist-item");		
		ArrayList<String> positiveWordList = new ArrayList<String>();
		for (int i = 0; i < positiveWordBank.size(); i++) {
			positiveWordList.add(positiveWordBank.get(i).html().toString());
		}

		//Return the ArrayList of positive words <String>
		return positiveWordList;
	}
	
	public static ArrayList<String> retrieveNegSentimentWordBank() throws IOException {

		//Get negative wordbank string from the parsed file
		String archivePath = "/Users/" + System.getProperty("user.name") + "/Desktop/archive/resources/NegativeWordsVocabularyList-EnchantedLearning.html";
    	File negativeWordbankFile = new File(archivePath);
    			
		//Use Jsoup to collect relevant movie system information required
    	Document jDoc = Jsoup.parse(negativeWordbankFile, "UTF-8");
		Elements negativeWordBank = jDoc.select("div.wordlist-item");		
		ArrayList<String> negativeWordList = new ArrayList<String>();
		for (int i = 0; i < negativeWordBank.size(); i++) {
			negativeWordList.add(negativeWordBank.get(i).html().toString());
		}
		
		//Return the ArrayList of negative words <String>
		return negativeWordList;

	}
	
	//This main serves as a proof of concept, and also provides analysis regarding the similarity of review EXTRACTED versus review GIVEN movie ratings
	public static void main(String[] args) throws IOException {
		
		// TEST & SHOW:
		//     - do these review rating sentiment assessments make sense? 
		//     - how EXTRACTED SCORES seem compared to GIVEN scores from archive?
		//     - algorithm showing user bias
		//	   - how this could have been implemented based on other reviews content with accuracy comparing to score, and factoring in users bias to certain movies
		//
		String page = "";
			
			//TEST HIGH SCORING
			page = "this is a great film that shows the neverending battle of Religion. you can kill one person but they will breed more people so in the end it is a neverending battle. but in closing very well done film,,great story great director";
			//Score given from archive:  5.0
			//Score extracted from data: 5.0
			System.out.println("MOVIE 1: High score");
			System.out.println(" Extracted Score:  " + getExtractedRating(getReviewSentimentPairings(page)));
			System.out.println(" Given Star Score: 5.0");
			System.out.println(" Good / Bad Score: "+getReviewSentimentPairings(page) + "\n");			
			
			//TEST LOW SCORING
			page = "why do they make these kid of crap movies. god this movie sucks it sucks.there is one funny part in the film, when they go to another fast food place and the talk to the the man working. other then that its a bomb!";
			//Score given from archive:  1.0
			//Score extracted from data: 0.0
			System.out.println("MOVIE 2: Low score");
			System.out.println(" Extracted Score:  " + getExtractedRating(getReviewSentimentPairings(page)));
			System.out.println(" Given Star Score: 1.0");
			System.out.println(" Good / Bad Score: "+getReviewSentimentPairings(page) + "\n");
			
			//TEST NEUTRAL SCORING
			page = "if gorey is what your looking for then this is it. if plot is what your looking for then it seems like this movie is a lot like the plot of another by the name of \\\"SEVEN\\\" its not like a clone of SEVEN but in ways its the same. i also hated the ending if you have one bullet you shoot the pad lock... duh";
			//Score given from archive:  3.0
			//Score extracted from data: 2.5
			System.out.println("MOVIE 3: Neutral score");
			System.out.println(" Extracted Score:  " + getExtractedRating(getReviewSentimentPairings(page)));
			System.out.println(" Given Star Score: 3.0");
			System.out.println(" Good / Bad Score: "+getReviewSentimentPairings(page) + "\n");
			
			System.out.println("Positive Comparison List -> "+retrievePosSentimentWordBank());
			System.out.println("Negative Comparison List -> "+retrieveNegSentimentWordBank());
			
			page = "this is in my top 5 of 06 with out a doubt. loaded with dark comedy and touching moments. u got a grampa that does H, a little girl (olive) who is a child \"model\", the suicidal gay uncle, the brother who doesnt speak, the dad who is an very blunt person, and the mother the smoker who hides it from her husband. but any they all go on a round trip to cali to have olive enter the little miss sunshine padg. and there are many bumps in the road. a must own movie..... u will love it Rated R For, Strong Language, Drug Content";
			//Score given from archive:  
			//Score extracted from data: 
			System.out.println("MOVIE 4: CONFLICTED");
			System.out.println(" Extracted Score:  " + getExtractedRating(getReviewSentimentPairings(page)));
			System.out.println(" Given Star Score: ");
			System.out.println(" Good / Bad Score: "+getReviewSentimentPairings(page) + "\n");
			
			System.out.println("Positive Comparison List -> "+retrievePosSentimentWordBank());
			System.out.println("Negative Comparison List -> "+retrieveNegSentimentWordBank());

	}
	
}
