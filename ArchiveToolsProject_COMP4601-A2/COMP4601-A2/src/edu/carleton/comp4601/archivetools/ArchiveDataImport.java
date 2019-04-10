package edu.carleton.comp4601.archivetools;

import java.io.File;
import java.io.IOException;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.jsoup.*;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;

import edu.carleton.comp4601.databasetools.MyMongoClient;

public class ArchiveDataImport {
     
    public static void main(String args[]) throws IOException{

    	//STORAGE SETUP
    	MyMongoClient mc = new MyMongoClient();
    	DB database = mc.getDB();
    	
    	//REFRESH STORAGE - Remove if data processing complete
    	database.getCollection("reviews").drop();
    	database.getCollection("movies").drop();
    	database.getCollection("users").drop();
    	
    	//OPEN COLLECTIONS
    	DBCollection reviews = database.getCollection("reviews");
    	DBCollection users = database.getCollection("users");
    	DBCollection movies = database.getCollection("movies");
    	
    	//DATA INPUT - Construct archive access path to "archive" folder on users desktop...
    	String archivePath = "/Users/" + System.getProperty("user.name") + "/Desktop/archive/";
    	
    	//DATA LOAD - Collects list of files to parse into storage in next step...
    	List<Path> reviewList = Files.walk(Paths.get(archivePath + "reviews/")).filter(Files::isRegularFile).collect(Collectors.toList());
    	List<Path> userList = Files.walk(Paths.get(archivePath + "users/")).filter(Files::isRegularFile).collect(Collectors.toList());
    	List<Path> movieList = Files.walk(Paths.get(archivePath + "pages/")).filter(Files::isRegularFile).collect(Collectors.toList());
    	
    	//STORE AND PARSE REVIEW DATA -> MONGO DB 
    	for (Path reviewFile : reviewList) {
    		
			File content = new File(reviewFile.toString());
			Document jDoc = Jsoup.parse(content, "UTF-8");
			
			//Use Jsoup to collect relevant movie system information required
			String userID = jDoc.select("meta[name=userId]").first().attr("content"); 
			String userName = jDoc.select("meta[name=profileName]").first().attr("content");  
			String starUserRating = jDoc.select("meta[name=score]").first().attr("content");  
			String movieName = jDoc.title();
			String reviewText = jDoc.body().text();
			String extractedUserRating = null;
			String extractedGenre = null;
			
			System.out.println("Test Print Machine - Extracted Data \n");
			System.out.println("Film Name/ID: " + movieName);
			System.out.println("User Name/ID: " + userName + " (ID: " + userID + ")");
			System.out.println("User Rating:  " + starUserRating + "/5.0");
			System.out.println("User Review:  " + reviewText);
			
    		//DATABASE INSERTIONS - newDataField.put(FIELD NAME,INFORMATION);
			BasicDBObject newDataField = new BasicDBObject();
			newDataField.put("MovieID",movieName);
			newDataField.put("UserID",userID);
			newDataField.put("Username", userName);
			newDataField.put("Extracted Scoring", extractedUserRating);
			newDataField.put("User Star Scoring", starUserRating);
			newDataField.put("Extracted Genre", extractedGenre);
			newDataField.put("Review Text Log", reviewText);
			reviews.insert(newDataField);	
    	}
    	
    	//STORE AND PARSE USER DATA -> MONGO DB 
    	for (Path userFile : userList) {
    		
    		File content = new File(userFile.toString());
    		Document jDoc = Jsoup.parse(content, "UTF-8");
    		String userID = jDoc.title();
    		String preferredGenre = null;
    		
    		//Add friends to array list collection of string "names"
    		Elements userElements = jDoc.getElementsByTag("a");
    		ArrayList<String> moviePages = new ArrayList<String>();
    		for (int i = 0; i < userElements.size(); i++) {
    		    moviePages.add(userElements.get(i).html());
    		}

    		//DATABASE INSERTIONS - newDataField.put(FIELD NAME,INFORMATION);
			BasicDBObject newDataField = new BasicDBObject();
			newDataField.put("UserID",userID);
			newDataField.put("Movies Reviewed", moviePages);
			newDataField.put("Preferred Genre", preferredGenre);
			users.insert(newDataField);	
    	}
    	
    	//STORE AND PARSE PAGES DATA (as movie content) -> MONGO DB 
    	for (Path movieFile : movieList) {
    		
    		File content = new File(movieFile.toString());
    		Document jDoc = Jsoup.parse(content, "UTF-8");
    		String movieId = movieFile.toString().substring(33,43);
    		String genre = null;
    		String extractedUserRating = null;
    		String starUserRating = null;
    		
    		//Add all reviews to string for later topic sentiment review, doesn't matter what user these belong to at the moment. 
    		Elements reviewElements = jDoc.getElementsByTag("p");
    		ArrayList<String >sentimentMetadata = new ArrayList<String>();
    		for (int i = 0; i < reviewElements.size(); i++) {
    		    sentimentMetadata.add(reviewElements.get(i).html());
    		}
    		
    		//DATABASE INSERTIONS - newDataField.put(FIELD NAME,INFORMATION);
			BasicDBObject newDataField = new BasicDBObject();
			newDataField.put("MovieID", movieId);
			newDataField.put("Extracted Genre", genre);
			newDataField.put("Movie Reviews Log", sentimentMetadata);
			newDataField.put("Extracted Scoring", extractedUserRating);
			newDataField.put("User Star Scoring", starUserRating);
			movies.insert(newDataField);	
    	}
    }
}

