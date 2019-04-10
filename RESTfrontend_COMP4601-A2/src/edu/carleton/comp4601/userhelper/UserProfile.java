package edu.carleton.comp4601.userhelper;

import java.util.ArrayList;
import java.util.HashMap;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class UserProfile {
	
	//Attributes of a user
	private String userid;
	private HashMap<String, Double> givenScores;
	private ArrayList<String> moviesReviewed; 
	private String preferredGenre;
	private String community;
	
	//Constructor for default user no data
	public UserProfile(String userid){ 
		this.userid = userid; 		
		preferredGenre = "No Preference";
		givenScores = new HashMap<String, Double>();
		moviesReviewed = new ArrayList<String>();
		community = "general";
	}
	
	//Constructor to make new user with data
	public UserProfile(String userid, HashMap<String, Double> givenScores, ArrayList<String> moviesReviewed, String preferredGenre, String community){
		this.userid = userid;
		this.givenScores = givenScores;
		this.moviesReviewed = moviesReviewed;
		this.preferredGenre = preferredGenre;
		this.community = community;
	}
	
	//Setters
	/*UserID*/          public void setName(String userid) { this.userid = userid; }
	/*Extracted Score*/ public void setGivenScores(HashMap<String, Double> givenScores) { this.givenScores = givenScores; }
	/*Movies Reviewed*/ public void setMoviesReviewed(ArrayList<String> moviesReviewed) { this.moviesReviewed = moviesReviewed; }
	/*PreferredGenre*/  public void setPreferredGenre(String preferredGenre) { this.preferredGenre = preferredGenre; }
	/*Community*/       public void setCommunity(String community) { this.community = community; }
	
	//Getters
	/*UserID*/          public String getUserId(){ return userid;}
	/*Extracted Score*/ public ArrayList<String> getMoviesReviewed(){ return moviesReviewed; }
	/*Movies Reviewed*/ public String getCommunity(){ return community; }
	/*PreferredGenre*/  public HashMap<String, Double> getGivenScores(){ return givenScores;}
	/*Movies Reviewed*/ public String getPreferredGenre(){ return preferredGenre; }
						public double addRating(String movieName, double score) { return givenScores.put(movieName, score); }
}
