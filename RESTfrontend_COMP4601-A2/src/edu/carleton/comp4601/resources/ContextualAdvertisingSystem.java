package edu.carleton.comp4601.resources;

import edu.carleton.comp4601.moviehelper.LoadMovieService;
import edu.carleton.comp4601.moviehelper.MoviePage;
import edu.carleton.comp4601.userhelper.LoadUserService;
import edu.carleton.comp4601.userhelper.UserProfile;

import java.util.ArrayList;
import java.util.Random;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType; 

 
//Main root path as per requirements of assignment
@Path("/rs")
public class ContextualAdvertisingSystem {
	
	private static final String NAME = "COMP4601 A2 - Movie Archive C.A.S. & Analytics - Ted Kachulis 100970278";
	private static boolean contextHit = false;

	//Display name as test that endpoints are working
	@GET
	public String nameOfSystem() {
		return NAME;
	}
	 
	//Display readme endpoint, created to offer navigation help for documentation purposes
	@Path("readme") 
	@GET
	@Produces(MediaType.TEXT_HTML) 
	public String helpPage() {
		
		//HTML output and table config
		String html = "<html> <body> <head><style>table, th, td {border: 1px solid black;}</style></head> <table style= \"width:100%\"> <tr> <th>Topic</th> <th>Help</th> </tr>";
		//Add help sections with topic and help content
		html = html + "<tr><td>"+ " Author" + "</td><td>" + "COMP4601 A2 - Movie Archive C.A.S. & Analytics - Ted Kachulis 100970278" + "</td></tr>"; 
		html = html + "<tr><td>"+ " Loading Data" + "</td><td>" + "Copy 'archive' folder from .zip onto your desktop, and then run 'ArchiveDataImport.java' as a java application in eclipse." + "</td></tr>"; 
		html = html + "<tr><td>"+ " Extracting Data" + "</td><td>" + "After 'Loading Data' you can run 'ArchiveScoreExtractor.java' and 'ArchiveGenreExtractor.java' as java applications in eclipse." + "</td></tr>"; 
		html = html + "<tr><td>"+ " /context" + "</td><td>" + "After data has been loaded and parsed by the backend portion of the project, context should be the first endpoint accessed. It is ensured by reminding the user to access it first when any other endpoint is accessed. '/context' displays information about the systems user base." + "</td></tr>"; 
		html = html + "<tr><td>"+ " /community" + "</td><td>" + "After context has been successfully run, accessing '/community' will present a list of users sorted by community category. Used for recommendations on page fetch." + "</td></tr>"; 
		html = html + "<tr><td>"+ " /fetch/{user}/{page}" + "</td><td>" + "After context has been successfully run, accessing '/fetch/user/page' will present the user with a movie summary of extracted information, and an advertisement about a film targeted towards their user category." + "</td></tr>"; 
		html = html + "<tr><td>"+ " advertising/{category}" + "</td><td>" + "Can be run at any time, displays a log of advertisement available for community based film recommendation systems." + "</td></tr>"; 
		//HTML end tags to close row & table & body & html
		html = html + " </td>  </tr> </table></body></html>";			
		return html; 
	}
	
	@Path("context")
	@GET
	@Produces(MediaType.TEXT_HTML)
	public String contextProcessor() {
		
		try { //Try to load user profiles from database
			LoadUserService.getInstance();
			System.out.println("User collection loaded from database!");		
		} 
		catch (Exception e) { e.printStackTrace(); 
			System.out.println("Archive utility sequence incorrect - ensure archive is loaded & context is run first. Refer to readme.txt & try again.");
		}
		
		//HTML output and table style/heading config
		String html = "<html> <body> <head><style>table, th, td {border: 1px solid black;}</style></head>";		
		html = html + "<table style= \"width:100%\"> <tr> <th>UserID</th> <th>Preferred Genre</th> <th>Extracted Movie Scores</th> <th>Movies Reviewed</th> <th>Community</th> </tr>";
		
			//Now add each user profile to the table via the html string
			ArrayList<UserProfile> users = LoadUserService.getInstance().getUsers();
			for(int i = 0; i < users.size(); i++){
				html = html + "<tr><td>"+ users.get(i).getUserId() + "</td><td>" + users.get(i).getPreferredGenre() + "</td><td>" + users.get(i).getGivenScores().toString() + "</td><td> ";
				for(int f = 0; f < users.get(i).getMoviesReviewed().size(); f++){
					html = html + users.get(i).getMoviesReviewed().get(f) + ", ";
				}
				html = html + "</td> <td> ";
				
				if(users.get(i).getPreferredGenre().toString().equals("Action")) {
					html = html + "Action & Classics";
				}
				else if(users.get(i).getPreferredGenre().toString().equals("Comedy")) {
					html = html + "Laugh Lovers";
				}
				else if(users.get(i).getPreferredGenre().toString().equals("Horror")) {
					html = html + "Fear Fanatics";
				}
				
			}
	
		//HTML end tags to close row & table & body & html, update context validation and return the page via html string
		html = html + " </td>  </tr> </table></body></html>";	     
		contextHit = true;
		return html;
	}
	
	@Path("community")
	@GET
	@Produces(MediaType.TEXT_HTML)
	public String communityProcessor() {
		
		//Ensure context is run first as per assignment requirement
		String html = "";
		if(contextHit == false) {
			html = "Archive utility sequence incorrect - ensure archive is loaded & context is run first. Refer to readme.txt & try again.";
		}
		else {
			//HTML output and table config
			html = "<html> <body> <head><style>table, th, td {border: 1px solid black;}</style></head>";		
			html = html + "<table style= \"width:100%\"> <tr> <th>Community</th> <th>Community Members</th> </tr>";
		
			//Now add each user profile to the table via the html string
			ArrayList<UserProfile> users = LoadUserService.getInstance().getUsers();
			String actionCommunity = "";
			String comedyCommunity = "";
			String horrorCommunity = "";
			
			for(int i = 0; i < users.size(); i++){
	
				if(users.get(i).getPreferredGenre().toString().equals("Action")) {
					actionCommunity = actionCommunity + users.get(i).getUserId().toString() + ", ";
				}
				else if(users.get(i).getPreferredGenre().toString().equals("Comedy")) {
					comedyCommunity = comedyCommunity + users.get(i).getUserId().toString() + ", ";
				}
				else if(users.get(i).getPreferredGenre().toString().equals("Horror")) {
					horrorCommunity = horrorCommunity + users.get(i).getUserId().toString() + ", ";
				}
				
			}
	 
			html = html + "<tr><td>"+ "Action & Classics" + "</td><td>" + actionCommunity + "</td></tr>"; 
			html = html + "<tr><td>"+ "Laugh Lovers" + "</td><td>" + comedyCommunity + "</td></tr>"; 
			html = html + "<tr><td>"+ "Fear Fanatics" + "</td><td>" + horrorCommunity + "</td></tr>"; 
			html = html + " </table></body></html>"; //HTML end tags to close row & table & body & html
		}
		return html;
	}
	
	@Path("fetch/{user}/{page}") 
	@GET
	@Produces(MediaType.TEXT_HTML)
	public String fetchUserPageProcessor(@PathParam("user") String user, @PathParam("page") String page) {
		//Ensure context is run first as per assignment requirement
				String html = "";
				if(contextHit == false) {
					html = "Archive utility sequence incorrect - ensure archive is loaded & context is run first. Refer to readme.txt & try again.";
				}
				else {
					//HTML output and table config
					html = "<html> <body> <head><style>table, th, td {border: 1px solid black;}</style></head>";		
					html = html + "<table style= \"width:100%\"> <tr> <th> User: " + user + " - Page: " + page + "</th> </tr>";
				
					//Now add each user profile to the table via the html string
					ArrayList<UserProfile> users = LoadUserService.getInstance().getUsers();
					String prefGen = "";
					for(int i = 0; i < users.size(); i++){
			
						if(users.get(i).getUserId().toString().equals(user)) {
							prefGen =  users.get(i).getPreferredGenre().toString();
							Random rand = new Random(); 
							int ad = rand.nextInt(3)+1;

							if(prefGen.equals("Action")) {
								html = html + "<tr><td>"+ "<img src=\"https://raw.githubusercontent.com/TedKachulis/RecommenderSystemImgPlaceholders/master/adverts/actionAd" + ad + ".jpg\" alt=\"Action 1 Ad\">"  + "</td></tr>"; 
								
								if(ad==1) {
									html = html + "<tr><td>"+ " <a href=\"https://sikaman.dyndns.org:8443/WebSite/rest/site/courses/4601/assignments/training/pages/" + "6303212263" + ".html\"> Click to check out the reviews (for the recommended movie)! </a>" + "</td></tr>";
								}
								else if(ad==2) {
									html = html + "<tr><td>"+ " <a href=\"https://sikaman.dyndns.org:8443/WebSite/rest/site/courses/4601/assignments/training/pages/" + "0784010331" + ".html\"> Click to check out the reviews (for the recommended movie)! </a>" + "</td></tr>";
								}
								else if(ad==3) {
									html = html + "<tr><td>"+ " <a href=\"https://sikaman.dyndns.org:8443/WebSite/rest/site/courses/4601/assignments/training/pages/" + "0783226128" + ".html\"> Click to check out the reviews (for the recommended movie)! </a>" + "</td></tr>";
								} 
								
							}
							else if(prefGen.equals("Horror")) {
								html = html + "<tr><td>"+ "<img src=\"https://raw.githubusercontent.com/TedKachulis/RecommenderSystemImgPlaceholders/master/adverts/horrorAd" + ad + ".jpg\" alt=\"Horror 1 Ad\">"  + "</td></tr>";
								
								if(ad==1) {
									html = html + "<tr><td>"+ " <a href=\"https://sikaman.dyndns.org:8443/WebSite/rest/site/courses/4601/assignments/training/pages/" + "6304240554" + ".html\"> Click to check out the reviews (for the recommended movie)! </a>" + "</td></tr>";
								}
								else if(ad==2) {
									html = html + "<tr><td>"+ " <a href=\"https://sikaman.dyndns.org:8443/WebSite/rest/site/courses/4601/assignments/training/pages/" + "B00004CJ2O" + ".html\"> Click to check out the reviews (for the recommended movie)! </a>" + "</td></tr>";
								}
								else if(ad==3) {
									html = html + "<tr><td>"+ " <a href=\"https://sikaman.dyndns.org:8443/WebSite/rest/site/courses/4601/assignments/training/pages/" + "B003EYVXUU" + ".html\"> Click to check out the reviews (for the recommended movie)! </a>" + "</td></tr>";
								}
							}
							else if(prefGen.equals("Comedy")) {
								html = html + "<tr><td>"+ "<img src=\"https://raw.githubusercontent.com/TedKachulis/RecommenderSystemImgPlaceholders/master/adverts/comedyAd" + ad + ".jpg\" alt=\"Comedy 1 Ad\">"  + "</td></tr>";
								
								if(ad==1) {
									html = html + "<tr><td>"+ " <a href=\"https://sikaman.dyndns.org:8443/WebSite/rest/site/courses/4601/assignments/training/pages/" + "B001KEHAI0" + ".html\"> Click to check out the reviews (for the recommended movie)! </a>" + "</td></tr>";
								}
								else if(ad==2) {
									html = html + "<tr><td>"+ " <a href=\"https://sikaman.dyndns.org:8443/WebSite/rest/site/courses/4601/assignments/training/pages/" + "B00004RM0J" + ".html\"> Click to check out the reviews (for the recommended movie)! </a>" + "</td></tr>";
								}
								else if(ad==3) {
									html = html + "<tr><td>"+ " <a href=\"https://sikaman.dyndns.org:8443/WebSite/rest/site/courses/4601/assignments/training/pages/" + "B00004CX8I" + ".html\"> Click to check out the reviews (for the recommended movie)! </a>" + "</td></tr>";
								}
							} 
						}
					}
			 
					ArrayList<MoviePage> movies = LoadMovieService.getInstance().getMovies();
					String genre = "", score = "", reviewString = "";
					//Sort for movie we want
					for(int i = 0; i < movies.size(); i++){
						
						//Find it
						if(movies.get(i).getMovieId().toString().equals(page)) {
							
							//Get data about movie
							genre = movies.get(i).getGenre().toString();
							score = movies.get(i).getScore().toString();
							reviewString = movies.get(i).getReviews();
							
						} 
					}

					html = html + "<tr><td>"+ " Movie: " + page + " <br /> Average Rating: " + score + " <br /> Genre: " + genre + " <br /> Reviews: " + reviewString + "</td></tr>"; 
					html = html + " </table></body></html>"; //HTML end tags to close row & table & body & html
				}
				return html;	
}
	
	//Display advertisements for given category of user communities, refer to /readme endpoint for help displaying desired content
	@Path("advertising/{category}")
	@GET
	@Produces(MediaType.TEXT_HTML)  
	public String advertisingCategoryProcessor(@PathParam("category") String category) {

		//HTML output and table config
		String html = "<html> <body> <head><style>table, th, td {border: 1px solid black;}</style></head><table style= \"width:100%\"> <tr> <th> Category </th> <th>Advertisement Samples</th> </tr>";		

		if(category.equals("actionclassics")) {
			html = html + "<tr><td>"+ " Action & Classics" + "</td><td>" + "<img src=\"https://raw.githubusercontent.com/TedKachulis/RecommenderSystemImgPlaceholders/master/adverts/actionAd1.jpg\" alt=\"Action 1 Ad\">"  + "</td></tr>";
			html = html + "<tr><td>"+ " Action & Classics" + "</td><td>" + "<img src=\"https://raw.githubusercontent.com/TedKachulis/RecommenderSystemImgPlaceholders/master/adverts/actionAd2.jpg\" alt=\"Action 2 Ad\">"  + "</td></tr>";
			html = html + "<tr><td>"+ " Action & Classics" + "</td><td>" + "<img src=\"https://raw.githubusercontent.com/TedKachulis/RecommenderSystemImgPlaceholders/master/adverts/actionAd3.jpg\" alt=\"Action 3 Ad\">"  + "</td></tr>"; 
		}
		else if(category.equals("fearfanatics")) { 
			html = html + "<tr><td>"+ " Fear Fanatics" + "</td><td>"+ "<img src=\"https://raw.githubusercontent.com/TedKachulis/RecommenderSystemImgPlaceholders/master/adverts/horrorAd1.jpg\" alt=\"Horror 1 Ad\">"  + "</td></tr>"; 
			html = html + "<tr><td>"+ " Fear Fanatics" + "</td><td>"+ "<img src=\"https://raw.githubusercontent.com/TedKachulis/RecommenderSystemImgPlaceholders/master/adverts/horrorAd2.jpg\" alt=\"Horror 2 Ad\">"  + "</td></tr>"; 
			html = html + "<tr><td>"+ " Fear Fanatics" + "</td><td>"+ "<img src=\"https://raw.githubusercontent.com/TedKachulis/RecommenderSystemImgPlaceholders/master/adverts/horrorAd3.jpg\" alt=\"Horror 3 Ad\">"  + "</td></tr>"; 
		}
		else if(category.equals("laughlovers")) { 
			html = html + "<tr><td>"+ " Laugh Lovers" + "</td><td>" + "<img src=\"https://raw.githubusercontent.com/TedKachulis/RecommenderSystemImgPlaceholders/master/adverts/comedyAd1.jpg\" alt=\"Comedy 1 Ad\">"  + "</td></tr>"; 
			html = html + "<tr><td>"+ " Laugh Lovers" + "</td><td>" + "<img src=\"https://raw.githubusercontent.com/TedKachulis/RecommenderSystemImgPlaceholders/master/adverts/comedyAd2.jpg\" alt=\"Comedy 2 Ad\">"  + "</td></tr>"; 
			html = html + "<tr><td>"+ " Laugh Lovers" + "</td><td>" + "<img src=\"https://raw.githubusercontent.com/TedKachulis/RecommenderSystemImgPlaceholders/master/adverts/comedyAd3.jpg\" alt=\"Comedy 3 Ad\">"  + "</td></tr>"; 
		}

		return html;   
	}    
	  
	
} 
