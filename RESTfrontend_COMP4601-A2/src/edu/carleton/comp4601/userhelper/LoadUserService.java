package edu.carleton.comp4601.userhelper;

import java.util.ArrayList;
import java.util.HashMap;

import javax.xml.bind.annotation.XmlRootElement;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoException;

import edu.carleton.comp4601.resources.MyMongoClient;

//Consider this the pipeline for user data between the database and the server 
@XmlRootElement
public class LoadUserService {
	
	private ArrayList<UserProfile> users;
	
	//Create instance runtime
	public static void setInstance(LoadUserService instance) {
		LoadUserService.instance = instance;
	}
	public static LoadUserService getInstance() {
		if (instance == null)
			instance = new LoadUserService();
		return instance;
	}	
	private static LoadUserService instance;
	
	//Constructor -----------------------------------------------------------------------------------------------------------------------------------------------------
	public LoadUserService(){
		users = new ArrayList<UserProfile>();
		populateCollection();
	}
	
	//Getters ---------------------------------------------------------------------------------------------------------------------------------------------------------
	public ArrayList<UserProfile> getUsers(){ return users; }
	
	public UserProfile getUser(String user){
		for(UserProfile u: users)
			if(u.getUserId().equals(user))
				return u;
		
		return null;
	}
	
	//Service Utility Methods -----------------------------------------------------------------------------------------------------------------------------------------
	
	//Add user to collection
	public void addUser(UserProfile user){ 
		users.add(user); 
	}
	
	//Load users into collection
	public void populateCollection(){
		
		//Mongo config and load all the data we need should be calculated in this repository now
		MyMongoClient mc = MyMongoClient.getInstance();
		DB database = mc.getDB();
		DBCollection users = database.getCollection("users");
		DBCollection reviews = database.getCollection("reviews");

		//Traverse DB collection and load it into the user collection when requested from browser
		DBCursor curs = users.find(); 
		try {
        while(curs.hasNext()) {
        	
        	//Get data about current user from database
            DBObject currUser = curs.next();
            String userId = currUser.get("UserID").toString() ; 
            String genre = currUser.get("Preferred Genre").toString();                
            String community = "Easy Watchers";
            
            //set community
            if(genre == "Action") {
            	community = "Action & Classics";
            }
            else if(genre == "Comedy") {
            	community = "Laugh Lovers";
            }
            else if(genre == "Horror") {
            	community = "Fear Fanatics";
            }
            
            //Create identifier object for current user profile
            DBObject userSearchObject = new BasicDBObject();
            userSearchObject.put("UserID", userId);

            //Get list of movies the user has reviewed & get scores for movies the user has reviewed
            ArrayList<String> moviesReviewed = new ArrayList<String>();
            HashMap<String, Double> givenScores = new HashMap<String, Double>(); 
            DBCursor moviesReviewedByUser = reviews.find(userSearchObject);
            while (moviesReviewedByUser.hasNext()) {
            	DBObject currentReview = moviesReviewedByUser.next();
            	moviesReviewed.add(currentReview.get("MovieID").toString());
            	givenScores.put(currentReview.get("MovieID").toString(), Double.parseDouble(currentReview.get("Extracted Scoring").toString()));
            }
                
            //Add user to this user collection
            System.out.println(userId + " has been added!");
            UserProfile newUser = new UserProfile(userId, givenScores, moviesReviewed, genre, community);
            this.addUser(newUser);
        }

        } catch (MongoException x) {
        	System.out.println("Could not load users, refer to error:");
            x.printStackTrace();
        }
	}
}
