package edu.carleton.comp4601.archivetools;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;

import edu.carleton.comp4601.archivetools.ArchiveScoreExtractorTools;
import edu.carleton.comp4601.databasetools.MyMongoClient;

public class ArchiveScoreExtractor {
	
	static ArrayList<String> uniqueUsers;
	static double tempHol;
	
	public static void main(String args[]) throws IOException {

		//STORAGE RECALL
    	MyMongoClient mc = new MyMongoClient();
    	DB database = mc.getDB();

    	//OPEN RAW COLLECTIONS
    	DBCollection reviews = database.getCollection("reviews");
    	DBCollection movies = database.getCollection("movies");
    	
    	//POPULATE AUGMENTED REVIEW REPOSITORY
    	//Extract information from other collections to create an augmented collection
    	for(DBObject Review : reviews.find()) {

    		//Find the current DBObject containing the review data
    		DBObject ExtractedScoreReviewToUpdate = reviews.findOne(Review);
    		
    		//THIS ADDS A REVIEW SCORE
    		//Only update scoring if required! Check if current extracted scoring is null or not.
    		if((ExtractedScoreReviewToUpdate.get("Extracted Scoring") == "null") || (ExtractedScoreReviewToUpdate.get("Extracted Scoring") == null)){
    			
    			//Calculate scoring for each movie review based off of review text sentiment
    			ExtractedScoreReviewToUpdate.put("Extracted Scoring", ArchiveScoreExtractorTools.getExtractedRating(ArchiveScoreExtractorTools.getReviewSentimentPairings(Review.get("Review Text Log").toString())));
            	System.out.println("ReviewTextAnalysis ---- User: " + Review.get("UserID").toString() + " Movie: " + Review.get("MovieID").toString() + " Extracted Score: " + ArchiveScoreExtractorTools.getExtractedRating(ArchiveScoreExtractorTools.getReviewSentimentPairings(Review.get("Review Text Log").toString())));
            	
            	//Update the review with an extracted score from text
            	reviews.update(Review, ExtractedScoreReviewToUpdate);
    		}   			
    	}
    	
    	
    	//POPULATE AUGMENTED MOVIE SUMMARY REPOSITORY - extract information from other collections to create an augmented collection
    	for(DBObject MovieData : movies.find()) {

    		//Find the object containing the movie data
    		DBObject ExtractedScoreMovieToUpdate = movies.findOne(MovieData);
    		
    		//THIS ADDS AN OVERALL MOVIE SCORE
    		//Only update scoring if required! Check if current extracted scoring is null or not.
    		//if((ExtractedScoreMovieToUpdate.get("Extracted Scoring") == "null") || (ExtractedScoreMovieToUpdate.get("Extracted Scoring") == null) || (ExtractedScoreMovieToUpdate.get("Extracted Scoring").toString() == "0")) {
    			
    			String movieName = ExtractedScoreMovieToUpdate.get("MovieID").toString();
    			
    			if(movieName != null) {
    				 
    	                BasicDBObject movieToSearch = new BasicDBObject();		
    	                movieToSearch.put("MovieID", movieName);	
    	                DecimalFormat df = new DecimalFormat("#.##");
    	                double movieScore = 0.0;	
    	                double movieStarScore = 0.0;	
    	                double reviewCount = 0;
    	                
    	                for(DBObject review : reviews.find(movieToSearch)) {    	                	
    	                	
    	                		System.out.println("Current Review For: " + review.get("MovieID").toString() + " - Movie To Search For: "+ movieToSearch.get("MovieID").toString() + " /n" );

    	                		//Get the current reviews EXTRACTED score and factor it into summed total
    	                		double thisReviewScore = (double)review.get("Extracted Scoring");
    	                		movieScore += thisReviewScore;
    	                		reviewCount++;

    	                		//Print report for EXRTRACTED movie review score
    	                		System.out.println("This Review Score: " + thisReviewScore + "\n" + "Current total: " + movieScore + "\n" + ("Review Count: " + reviewCount));

    	                		//Get the current reviews GIVEN score and factor it into summed total
    	    	              	double thisReviewStarScore = Double.parseDouble(review.get("User Star Scoring").toString());
    	                		movieStarScore += thisReviewStarScore;

    	                		//Print report for GIVEN movie review score
    	                		System.out.println("This Star Score: " + thisReviewStarScore + "\n" + "Current total: " + movieStarScore + "\n" + ("Review Count: " + reviewCount));
    	                		
    	                		//Get CURRENT score - last iteration is final update, but if stopped at any time it will have inserted a cumuliative average
    	    	                double starScoreToInsert = movieStarScore / reviewCount;
    	    	                double scoreToInsert = movieScore / reviewCount;
    	    	              	
    	    	                //Update DB with new field values
    	    	                ExtractedScoreMovieToUpdate.put("Extracted Scoring", df.format(scoreToInsert));	
    	    	                ExtractedScoreMovieToUpdate.put("User Star Scoring", df.format(starScoreToInsert));	
    	    	                movies.update(MovieData, ExtractedScoreMovieToUpdate);
    	    	                
    	    	                //Print report for EXTRACTED AND GIVEN inserted values
    	    	                System.out.println("Extracted Score Inserted:" + df.format(scoreToInsert) + "/n /n");
    	    	                System.out.println("Average User Star Score Inserted:" + df.format(starScoreToInsert) + "/n /n");
    	                }      																				
    	            }								
    	}
	}
}
