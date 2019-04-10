package edu.carleton.comp4601.moviehelper;

import java.util.ArrayList;

import javax.xml.bind.annotation.XmlRootElement;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoException;

import edu.carleton.comp4601.resources.MyMongoClient;

//Consider this the pipeline for movie data between the database and the server 
@XmlRootElement
public class LoadMovieService {
	
	private ArrayList<MoviePage> movies;
	
	//Create instance runtime
	public static void setInstance(LoadMovieService instance) {
		LoadMovieService.instance = instance;
	}
	public static LoadMovieService getInstance() {
		if (instance == null)
			instance = new LoadMovieService();
		return instance;
	}	
	private static LoadMovieService instance;
	
	//Constructor -----------------------------------------------------------------------------------------------------------------------------------------------------
	public LoadMovieService(){
		movies = new ArrayList<MoviePage>();
		populateCollection();
	}
	
	//Getters ---------------------------------------------------------------------------------------------------------------------------------------------------------
	public ArrayList<MoviePage> getMovies(){ return movies; }
	
	public MoviePage getMovie(String movie){
		for(MoviePage m: movies)
			if(m.getMovieId().equals(movie))
				return m;
		
		return null;
	}
	
	//Service Utility Methods -----------------------------------------------------------------------------------------------------------------------------------------
	
	//Add user to collection
	public void addMovie(MoviePage movie){ 
		movies.add(movie); 
	}
	
	//Load users into collection
	public void populateCollection(){
		
		//Mongo config and load all the data we need should be calculated in this repository now
		MyMongoClient mc = MyMongoClient.getInstance();
		DB database = mc.getDB();
		DBCollection movies = database.getCollection("movies");
		
		//Traverse DB collection and load it into the user collection when requested from browser
		DBCursor curs = movies.find(); 
		try {
        while(curs.hasNext()) {
        	
        	//Gets information from the DB about the current movie
            DBObject currMovie = curs.next();
            String movieId = currMovie.get("MovieID").toString(); 
            String genre = currMovie.get("Extracted Genre").toString(); 
            String reviewsPage = "<a href=\"https://sikaman.dyndns.org:8443/WebSite/rest/site/courses/4601/assignments/training/pages/" + movieId + ".html\"" + ">Check Out the Reviews!</a>";
            Double givenAvgScore = Double.parseDouble(currMovie.get("User Star Scoring").toString());
            Double extractedAvgScore = Double.parseDouble(currMovie.get("Extracted Scoring").toString());
            Double score = (givenAvgScore + extractedAvgScore)/2;
            
            //Create identifier object for current movie page
            DBObject movieSearchObject = new BasicDBObject();
            movieSearchObject.put("MovieID", movieId);
                
            //Add user to this user collection
            System.out.println(movieId + " has been added!");
            MoviePage newMovie = new MoviePage(movieId, reviewsPage, genre, score);
            this.addMovie(newMovie);
        }

        } catch (MongoException x) {
        	System.out.println("Could not load movies, refer to error:");
            x.printStackTrace();
        }
	}	
}
