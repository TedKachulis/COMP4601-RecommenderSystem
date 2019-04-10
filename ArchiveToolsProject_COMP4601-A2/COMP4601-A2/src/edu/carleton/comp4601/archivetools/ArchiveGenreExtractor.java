package edu.carleton.comp4601.archivetools;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;

import edu.carleton.comp4601.databasetools.MyMongoClient;
import weka.classifiers.trees.RandomForest;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;

public class ArchiveGenreExtractor {
	
	public static String[] MOVIE_GENRE = {"Action", "Comedy", "Horror"};	
	HashMap<String, Integer> wordCount;
	RandomForest rfClassifier = null;
	static Instances ins = null;
	int counter = 0;
	int wordBankSize = 100; //words to consider from each review
	
	//Database config - retrieve database instance
	MyMongoClient mc = MyMongoClient.getInstance();
	DB database = mc.getDB();
	
	//Load all 3 collections from database instance
	DBCollection reviews = database.getCollection("reviews");
	DBCollection movies = database.getCollection("movies");
	DBCollection users = database.getCollection("users");
	
	//constructor(): ArchiveGenreExtractor()
	// Purpose:		Finds set of unique words for all review 
	//				documents and then trains using them.
	// 
	public ArchiveGenreExtractor() {
		
		//Declare list to be filled with review vocabulary
		ArrayList<String> reviewsArr = new ArrayList<String>(); 		
		
		//Iterate over all movie objects in movies collection
		for(DBObject movie : movies.find()) {

			//Select current movie							
            if(movie.get("MovieID").toString() != null) {
            	
            	//Create search object using movieId in a new DBO
                BasicDBObject movieToSearch = new BasicDBObject();		
                movieToSearch.put("MovieID", movie.get("MovieID").toString());						
                //Set review to reviews for given movie id												
                BasicDBObject reviewObjectForMovie = mc.findObject("COMP4601-A2", "reviews", movieToSearch);	
                String reviewText = reviewObjectForMovie.get("Review Text Log").toString();							
                //add the review text to collection																			
                reviewsArr.add(reviewText);		
            }									
		}
		
		//Declare list to be filled with stopwords vocabulary
		ArrayList<String> stopwords = new ArrayList<>();
		BufferedReader br;
		String line;

		//Try to load stopwords from project file system
		try {
			br = new BufferedReader(new FileReader("./src/edu/carleton/comp4601/archivetools/stop.txt"));
			while((line = br.readLine()) != null) {
				for(String word: line.split(" "))
					stopwords.add(word);
			}
		//If stopwords cannot be loaded from project file system
		} catch (FileNotFoundException e) { e.printStackTrace();
		} catch (IOException e) { e.printStackTrace();}
		
		//Create hashmap of term frequency in reviews
		wordCount = new HashMap<String, Integer>();
		for(String review: reviewsArr) {
			Matcher m = Pattern.compile("([\\w]*)\\s").matcher(review);
			counter = 0;
			//Add the amount of desired words from review & filter from stopwords
			while(m.find() && counter++ < wordBankSize){
				if(!stopwords.contains(m.group(1)) && !wordCount.containsKey(m.group(1)))
					wordCount.put(m.group(1), 0);
			}
		}
		//Declare random forest for classification and run train()
		rfClassifier = new RandomForest();
		train();
	}

	
	
	public static void main(String[] args){
		ArchiveGenreExtractor c = new ArchiveGenreExtractor();
		HashMap<String,Integer> words = c.getPageWords("ABC");
		String keyList = "", valList = "";
		for(String key: words.keySet()){
			keyList += key + "\t";
			valList += words.get(key) + "\t";
		}
		System.out.println(keyList);
		System.out.println(valList);
	}
	
	
	//train()
	// Purpose: 1. builds hashmap of training data (Page and Genre Keyset)
	//          2. searches the database for the requested movie id
	//		    3. trains the classifier with the preset result 
	public void train() {
			
		//HashMap of predetermined training data: MovieID and GenreID
		HashMap<String,Integer> trainingData = new HashMap<String,Integer>();
		
		//Genre 0: Action Training Data
		/*1*/ trainingData.put("B0068FZ05Q", 0); //Bourne Identity
		/*2*/ trainingData.put("B001NLF2ZI", 0); //The Interpreter
		/*3*/ trainingData.put("6303212263", 0); //Superman
		/*4*/ trainingData.put("0784010331", 0); //Total Recall
		/*5*/ trainingData.put("0767800117", 0); //Glory
		/*6*/ trainingData.put("0783226128", 0); //Scarface
		/*7*/ trainingData.put("B00005JN4W", 0); //The Incredibles
		
		//Genre 1: Comedy Training Data
		/*1*/ trainingData.put("B001KEHAI0", 1); //Groundhog day
		/*2*/ trainingData.put("0790742942", 1); //Beetlejuice
		/*3*/ trainingData.put("0800141709", 1); //Dr Strangelove
		/*4*/ trainingData.put("6301797965", 1); //Young Frankenstein
		/*5*/ trainingData.put("B00004CX8I", 1); //One Flew Over the Cuckoo's Nest
		/*6*/ trainingData.put("B00004RM0J", 1); //The Goonies
		
		//Genre 2: Horror Training Data
		/*1*/ trainingData.put("B00871C09S", 2); //Rear Window
		/*2*/ trainingData.put("B00004RJ74", 2); //From Dusk Til Dawn
		/*3*/ trainingData.put("B000VDDWEC", 2); //28 Days Later
		/*4*/ trainingData.put("6304240554", 2); //Dawn of the Dead
		/*5*/ trainingData.put("6304808879", 2); //Hellraiser	
		/*6*/ trainingData.put("B00003CXI7", 2); //What Lies Beneath
		/*7*/ trainingData.put("B00004CJ2O", 2); //Friday the 13th
		/*8*/ trainingData.put("B003EYVXUU", 2); //Let Me In
		
		//Load training data information using given movie and genre 
		System.out.println(" ----- Starting Genre Extractor -----");
		
		//Construct a list of movies user reviews to assess for genre
		HashMap<String, Integer> dataToTrain = new HashMap<String,Integer>();
		DBCursor curs = movies.find();
		while(curs.hasNext()) {
            DBObject movieData  = curs.next();
            String name = movieData.get("MovieID").toString();
            dataToTrain.put(name, -1);
		} //Now has training data in system
	    buildClassifier(trainingData, dataToTrain);
	    
	    int count = 0;
	    for(String movie: dataToTrain.keySet()) {

	    	count++;
	    	BasicDBObject archiveMovie = new BasicDBObject("MovieID", movie);
	    	BasicDBObject updateObject =  new BasicDBObject("MovieID", movie);
	    	updateObject.put("genre", dataToTrain.get(movie));
	    	MyMongoClient.getInstance().updateInCollection("COMP4601-A2", "movies", archiveMovie, updateObject);
	    }
	    
	    //Train completed, notify user of successfull run and metadata. Notify user of next sequence.
	    System.out.println("-> " + count + " genres successfully assessed on sentiment from: " + dataToTrain.size() + " movie review pages.");
	    System.out.println("-> Now assessing users favorite genres assessed on sentiment from: " + dataToTrain.size() + " movie review pages.");
	    
	    //Iterate through the user archive
    	for(DBObject userToUpdate : users.find()) {

    		//Notify: we are assessing genre preference for users
    		String currUserID = userToUpdate.get("UserID").toString();
        	System.out.println("Now assessing the preferred genre for user: " + currUserID);

        	//Setup: search object, reviews by user (userID)
        	BasicDBObject usersMoviesToCheck = new BasicDBObject();
        	usersMoviesToCheck.put("UserID", currUserID);
        	
        	//Analyze: what genre does this user most frequently review
			int comedyCount = 0, horrorCount = 0, actionCount = 0;
			for(DBObject movieToCheck : reviews.find(usersMoviesToCheck)){
				
				//Get genre from current review by user and notify console
				String thisGenre = movieToCheck.get("Extracted Genre").toString();
				System.out.println("Extracted Genre Found: " + thisGenre);			
				
				//Keep tally of all genres assessed. Notify console genre count.
				if(thisGenre.equals("Action")) {
					actionCount++;
					System.out.println("Action count:" + actionCount);
				}
				else if(thisGenre.equals("Horror")) {
					horrorCount++;
					System.out.println("Horror count:" + comedyCount);
				}
				else if(thisGenre.equals("Comedy")) {
					comedyCount++;
					System.out.println("Comedy count:" + horrorCount);	
				}									
			}

			//Create object to update with, and update genre string classifier
			BasicDBObject userResult = mc.findObject("COMP4601-A2", "users", usersMoviesToCheck);
			String preferredGenre = "Undefined";
				
				//Use tally of all genres assessed. Notify console genre choice.
				if((comedyCount > horrorCount) && (comedyCount > actionCount)) {
					preferredGenre = "Comedy";
				}
				else if((horrorCount > comedyCount) && (horrorCount > actionCount)) {
					preferredGenre = "Horror";						
				}					
				else if((actionCount > horrorCount) && (actionCount > comedyCount)) {
					preferredGenre = "Action";						
				}
			
			//Notify: users calculated preferred genre. Update: object with genre.
			System.out.println("Users Preferred Genre: " + preferredGenre);
			userResult.put("Preferred Genre", preferredGenre);
			users.update(userToUpdate, userResult);
    	}
    	
	}
	
	//getPageWords(String pageID);
	// Inputs : String pageID
	// Returns:	HashMap of term and term frequency keysets.
	// Purpose:	Searches archive database for the movie review data 
	//          Stores all counts of word occurences to the hashmap
	public HashMap<String,Integer> getPageWords(String pageID) {

		//Step 1: create hashmap
		HashMap<String,Integer> pageCounts = new HashMap<String,Integer>(wordCount); 
		
		//Step 2: search for and set review content to sample reviews
		BasicDBObject review = new BasicDBObject();
		review.put("MovieID", pageID);
		BasicDBObject result = mc.findObject("COMP4601-A2", "reviews", review);
		
		//Notify: output user id and the review content to the console
		System.out.println("USER ID: " + result.get("UserID").toString() + " Review: " + result.get("Review Text Log").toString());

		//Step 3: Iterate over words and occurences for pageCount return. 
		String reviewStr = result.get("Review Text Log").toString();
		for(String key: pageCounts.keySet()) {
			//When added to pageCounts, it's used in other step.
			pageCounts.put(key, reviewStr.split("\\s?"+key+"\\s").length - 1);
		}
		
		//Return pageCounts - mostly used for testing and display
		System.out.println(pageCounts.toString());
		return pageCounts;	
	}
	
	//buildClassifier(HashMap<String, Integer> trainingData, HashMap<String, Integer> unknownValues)
	//
	// Inputs : 	trainingData - HashMap<PageID,PredictionValues>, unknownValues for others
	//
	// Purpose:		1. Iterates through all pages/reviews to train.
	//				2. Assigns the remaining pages based on the training data similarity
	//
	public void buildClassifier(HashMap<String, Integer> trainingData, HashMap<String, Integer> unknownValues) {

		try {
			Instances training = buildTrainingData(trainingData);
			rfClassifier.buildClassifier(training);
			for(String key: unknownValues.keySet()){
				
				//Declare genre string to be reassigned
				String genre = "Not enough training data.";
				
				//Check for which genre should be inserted
				unknownValues.put(key,(int)testData(getPageWords(key)));
				if(unknownValues.get(key) == 0) {
					genre = "Action";
				}else if(unknownValues.get(key) == 1){
					genre = "Comedy";
				}
				else if(unknownValues.get(key) == 2) {
					genre = "Horror";
				}
				
				//Notify: output movie id and extracted genre to console
				System.out.println("Movie ID: " + key);
				System.out.println("Assessed Genre: " + genre);
				System.out.println("-----------------------");
				
				//Create search object and update object, update movies with movie genre result
				BasicDBObject movieGenreToUpdate = new BasicDBObject();
				movieGenreToUpdate.put("MovieID", key);
				BasicDBObject result = mc.findObject("COMP4601-A2", "movies", movieGenreToUpdate);
				result.put("Extracted Genre", genre);
				movies.update(movieGenreToUpdate, result);
				
				//Iterate through reviews for movie and update the extracted genre information
				for(DBObject reviewToUpdate : reviews.find(movieGenreToUpdate)){
					//Search objects and update objects
					BasicDBObject reviewGenreToUpdate = new BasicDBObject();
					reviewGenreToUpdate.put("MovieID", reviewToUpdate.get("MovieID").toString());
					reviewGenreToUpdate.put("UserID", reviewToUpdate.get("UserID").toString());
					//Update reviews in database to reflect the genre of the movie they are about
					BasicDBObject reviewResult = mc.findObject("COMP4601-A2", "reviews", reviewGenreToUpdate);
					reviewResult.put("Extracted Genre", genre);
					reviews.update(reviewToUpdate, reviewResult);
				}	
			}
			
		//If failed genre extraction process
		} catch (Exception e) {
			System.out.println("ERR: Unsuccessful extraction for review genres. Please review project setup and try again.");
			e.printStackTrace();
		}	
	}
	
	//buildTrainingData(HashMap<String,Integer> trainingData);
	//
	// Inputs :	HashMap<PageID, PredictionValue> trainingData
	//
	// Purpose:	1. Train classifier based on term frequency in page
	//			   and the predetermined genres of trainingData. 
	//          2. Returns an instance object containing all of the
	//			   up to date training data.
	//
	Instances buildTrainingData(HashMap<String,Integer> trainingData){
		
		//Get instance of term frequency data structure
		Instances newData = getInstancesOf(wordCount);
		
		//Iterate through term frequency review pages data
		int i = 0;
		for(String key: trainingData.keySet()){

			HashMap<String,Integer> words = getPageWords(key); //Retrieve pageWords dataset with term frequency of specified page
			newData.add(new DenseInstance(words.size()+1));    //Create new nested instance to then re train with other words
			for(String word: words.keySet())
				newData.get(i).setValue(newData.attribute(word),words.get(word).intValue());
			//Add the  new data training values to the attributes after overall newData has been iterated for
			newData.get(i).setValue(ins.attribute("TheMovieGenre"), MOVIE_GENRE[trainingData.get(key).intValue()]);
			i++;
		}
		return newData;
	}

	
	//testData(HashMap<String, Integer> unknownValues); 
	//
	// Parameters:	unknownValues - HashMap
	//
	// Returns:		the index of the genre determined from the data
	// 
	// Purpose:		Determines the classification of a SINGLE unknown page
	// 				(HashMap contains the count of all the words in that page)
	//
	int testData(HashMap<String, Integer> unknownValues){
		
		//Create new data instance to classify
		Instance newData = new DenseInstance(wordCount.size());
		//Set data structure and set all values
		newData.setDataset(ins);
		
		//Check and set determined classification
		for(String key: unknownValues.keySet())
			newData.setValue(ins.attribute(key),unknownValues.get(key).intValue());
		
		//Check prediction and return data classification
		double pred = 0;
		try { pred = rfClassifier.classifyInstance(newData); } 
		catch (Exception e) { e.printStackTrace(); }
		return (int)pred; 
	}
	
	//getInstancesOfHashMap(<String, Integer> wordCount);
	// Inputs : HashMap<String, Integer> wordCount
	// 
	// Purpose: Create instance object containing structure 
	// 			of classification data.
	//
	public static Instances getInstancesOf(HashMap<String, Integer> wordCount){
		
		//Return the current instance if one already exists
		if (ins != null)
			return ins;
		
		//If there wasn't already an instance, create one with attributes
		ArrayList<Attribute> attributes = new ArrayList<Attribute>();
		for(String key: wordCount.keySet()) {
			//Add attribute associated to word count
			attributes.add(new Attribute(key));
		}
		
		//Declares genre result from classifier, created attributes
		ArrayList<String> fvNominalVal = new ArrayList<String>(ArchiveGenreExtractor.MOVIE_GENRE.length);
		for(String genre: ArchiveGenreExtractor.MOVIE_GENRE)
		    fvNominalVal.add(genre);
		
		//Add attributes to attr list, add attributes to instance
		Attribute attr = new Attribute("TheMovieGenre", fvNominalVal);
		attributes.add(attr);
		
		//Declare result class as the genre attribute, return instance
		ins = new Instances("Rel",attributes, 10);
		ins.setClass(ins.attribute("TheMovieGenre"));
		return ins;
	}	

}
