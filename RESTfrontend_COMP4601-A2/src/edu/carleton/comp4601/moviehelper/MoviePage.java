package edu.carleton.comp4601.moviehelper;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class MoviePage {
	
	//Attributes of a movie
	private String movieid;
	private String reviews;
	private String genre;
	private Double score;
	
	//Constructor for default movie no data
	public MoviePage(String movieid){ 
		this.movieid = movieid; 		
		genre = "No Classification";
		score = 0.0;
		reviews = null;
	}
	
	//Constructor to make new movie with data
	public MoviePage(String movieid, String reviews, String genre, Double score){
		this.movieid = movieid;
		this.genre = genre;
		this.reviews = reviews;
		this.score = score;
	}
	
	//Setters
	/*Movie ID*/          public void setName(String movieid) { this.movieid = movieid; }
	/*Movie Reviews*/ public void setReviews(String reviews) { this.reviews = reviews; }
	/*Movie Genre*/  public void setGenre(String genre) { this.genre = genre; }
	/*Movie Score*/     public void setScore(Double score) { this.score = score; }
	
	//Getters
	/*Movie ID*/        public String getMovieId(){ return movieid;}
	/*Movie Score*/    public Double getScore(){ return score; }
	/*Movie Reviews*/  public String getReviews(){ return reviews;}
	/*Movie Genre*/    public String getGenre(){ return genre; }
						
}
