package Netflix_Movie_Recommendation_System;

import java.util.*;
import java.io.*;

public class MovieRecSystem {
  public static void main(String[] args) {
	  
		/******************************************************
		 *		READ IN MOVIES.DAT AND STORE IN HASHMAP		  *
		 ******************************************************/
		HashMap<Integer, String> movieList = new HashMap<Integer, String>(); //this is the main data structure for storing the list of movies and their title/date
   		String fileName = "movies.dat";
   		try {
            FileReader fileReader = new FileReader(fileName);
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            String line;
            while((line = bufferedReader.readLine()) != null) {
                String[] pair = line.split("\\|");
                int movieId = Integer.parseInt(pair[0]);
                String movieTitleAndDate = pair[1]; 
                movieList.put(movieId, movieTitleAndDate);    
            }
            bufferedReader.close();         
        }//end try block for movies.dat
        catch(FileNotFoundException ex) {
            System.out.println("Unable to open file '" + fileName + "'");                
        }
        catch(IOException ex) {
            System.out.println("Error reading file '" + fileName + "'");
        }
   		final int NUM_MOVIES = movieList.size();//the total number of movies is derived from the size of the hashmap
   		
   		/******************************************************
		 *	 READ IN RATINGS.DAT AND STORE IN NESTED HASHMAP  *
		 ******************************************************/
   		HashMap<Integer, HashMap<Integer, Double>> userRatings = new HashMap<Integer, HashMap<Integer, Double>>();//this is the main data structure for storing
   		//the large list of user rating.  They are stored in a NESTED hashmap, so that for each user id (1-943), each user has its own hashmap of movie titles that
   		//have been rated as well as the rating that has been given to that particular movie
        String fileName2 = "ratings.dat";
        try {
            FileReader fileReader2 = new FileReader(fileName2);
            BufferedReader bufferedReader2 = new BufferedReader(fileReader2);
            String line;
            while((line = bufferedReader2.readLine()) != null) {
                String[] pair2 = line.split("	");
                int userId = Integer.parseInt(pair2[0]);
                int movieId = Integer.parseInt(pair2[1]);
                double movieRating = Double.parseDouble(pair2[2]);
                HashMap<Integer, Double> movie_rating_for_inner_hashmap = new HashMap<Integer, Double>();//this is the constuctor for the inner hashmap
                movie_rating_for_inner_hashmap.put(movieId, movieRating);	//if there is no map yet assigned for a user, we build it here first 
                if (userRatings.get(userId) == null) { 		//if no map assigned for user yet, this will add the map for the user
                	userRatings.put(userId, movie_rating_for_inner_hashmap);
                }
                else {		//if the user already has a map associated, we simply add the next rating to the proper place in the map
                	userRatings.get(userId).put(movieId, movieRating);
                }
             }
             bufferedReader2.close();         
         }//end try block for ratings.dat
         catch(FileNotFoundException ex) {
            System.out.println("Unable to open file '" + fileName + "'");                
         }
         catch(IOException ex) {
            System.out.println("Error reading file '" + fileName + "'");                  
         } 
         final int NUM_USERS = userRatings.size();	//total number of users is read from the data file and stored here
         	
        /******************************************************
		 *				COMPUTE SIMILARITY TABLE 			  *
		 ******************************************************/
        //the similarity table takes the longest to compute.  Since it is by nature a nested structure, it will need to make over 2.5 million calculations
        //in order to generate the table.  Since this is a symmetric table though, we can reduce this to about 1.25 million calcs by only filling half the table
        
        System.out.println("                 *********************************************************************");
        System.out.println("                 *                       WELCOME TO NETFLIX                          *");
        System.out.println("                 *********************************************************************");
        System.out.println();
        System.out.println("Thanks for choosing NETFLIX.  Please wait while we generate the top 5 movie recommendations for each user.  ");
        System.out.println("************************************************************************************************************");
        System.out.println();
        System.out.println("calculating similarity table....");
        double startTime = System.nanoTime();
        //this is what takes the longest to calculate.  The table is about 1600x1600
        double[][] similarityTable = new double[NUM_MOVIES][NUM_MOVIES];	
        for (int i=0; i<NUM_MOVIES; i++) {
        	for (int j=0; j<i; j++) {	//by setting the check to j<i we ensure that only half the table is calculated.  Later we can return the proper value with a 
        		//simple check to ensure we return the correct value
        		similarityTable[i][j] = calculateSimilarity(i+1, j+1, userRatings, movieList); //calls calculate similarity method to compute the table value
        	}
        	if (i%200 == 0) 
        		System.out.println("still calculating....");
        }
        double endTime = System.nanoTime();
        System.out.println("Similarity table calculation time: " + ((endTime - startTime)/1000000000) + " seconds");
        System.out.println();
        System.out.println("Generating top 5 recommendation list for all users...");
        System.out.println();
        
        
        /******************************************************
		 *				PREDICT USER RATINGS	 			  *
		 ******************************************************/
        HashMap<Integer, PriorityQueue<FinalRating>> top5Recs = new HashMap<Integer, PriorityQueue<FinalRating>>();	//this is the final map that will be used for output
        for (int i=0; i<NUM_USERS; i++) {	//iterate all users
        	PriorityQueue<FinalRating> priorityQ = new PriorityQueue<FinalRating>();  //use priority queue to store values
        	PriorityQueue<FinalRating> priorityQ2 = new PriorityQueue<FinalRating>(); //this q will be used to return the top 5 only
        	for (int j=0; j<NUM_MOVIES; j++) {
        		
        		if (!userRatings.get(i+1).containsKey(j+1)) {  //if the user HAS NOT rated this movie, then we will predict a rating for this movie and store it in the Q
        			double predictedRating = predictedRating(i+1, j+1, userRatings.get(i+1), movieList, similarityTable); //call to predicRating method
        			FinalRating finalRate = new FinalRating(i+1, j+1, predictedRating, movieList);  //create a final rating object for use with priority Q
        			priorityQ.add(finalRate);  //add to the Q
        		}
        	}
        	//this code adds only the top 5 for each user to a new Q and adds it to the final output map
        	int x = 0;
        	while (x<5 && !priorityQ.isEmpty()) {
        		priorityQ2.add(priorityQ.poll());
        		x++;
        	}
        	top5Recs.put(i+1, priorityQ2);  //user top 5 has been added to final output map
        }
        
        /******************************************************
		 *			WRITE TOP 5 RECS TO OUTPUT.DAT FILE		  *
		 ******************************************************/
          //writes final solution to the file output.dat
    	  String outputFile = "output.dat";
    	  try {
			PrintWriter output = new PrintWriter(outputFile);
			for (int i=1; i<=NUM_USERS; i++) {
				output.println("Top 5 Movie Recommendations for UserID: " + i + " ==>" + top5Recs.get(i).toString());
			}
			output.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	 System.out.println();
    	 System.out.println("Thank You for using NETFLIX.  The top 5 movie recommendations for all users has been stored in the file 'output.dat'.");
    	 System.out.println();
    	 System.out.println("GOODBYE");
  }//end main method
  
  
  		/******************************************************
  		 *					ADDITIONAL METHODS	 			  *
  		 ******************************************************/
  //this is the method to predict user ratings, following formula for cosine similarity
  public static double predictedRating(int userId, int movieId, HashMap<Integer,Double> userRatings, HashMap<Integer, String> movieList, double[][]similarityTable) {
	  double sum = 0.0;
	  double count = 0.0;
	  for (int i=0; i<movieList.size(); i++) {
		  if (userRatings.containsKey(i+1)) {  //for all movies, if user has rated this movie, we will use it to calculate rating for unranked movie
			  double getSim = getSim(similarityTable, i+1, movieId);  //the getSim method is called, this is the check discussed earlier while computing the similarity 
			  //table.  This method is explained further in the comments for the actual method below
			  sum += userRatings.get(i+1) * getSim;   //the ranked movie is multiplied by its similarity to target movie and added to sum, which is numerator in final calc
			  count += getSim;  //similarity table value is added to total count, this will be denominator in final calculation
		  }
	  }
	  return sum/count;  //this is the predicted rating that will be returned
  }//end predictedRating method
  
 
  //as mentioned above, we need to check that we return a valid value for this 2d array of similarity values.  Since we only created half of the table, half of the values
  //will return 0.0 if not checked.  Since we built our array downwards, the left value in the array should always be greater than the value on the right.  For example,\
  //if the call is for the value at position [900][1000], we know this will return a 0.0, which is wrong, so we flip this to the symmetric value [1000][900] to obtain
  //the correct answer
  public static double getSim(double[][] simTable, int i, int j) {
	  if (i<j) {
		  return simTable[j-1][i-1]; //the -1 for each value is because our map values start at 1 but the array table starts at 0
	  }
	  else {
		  return simTable[i-1][j-1];
	  }
  }
  
  
  //this calculates the similarity table by creating 2 array lists for each movie being currently compared.  The ratings are added in order so we have 2 arrays of
  //uniform lenght.  Users are check in order to see if they have rated the movie, and the rating is added, if no rating, then 0.0 is added
  public static double calculateSimilarity(int movie1, int movie2, HashMap<Integer, HashMap<Integer, Double>> userRatings, HashMap<Integer, String> movieList) {
	  double numerator = 0;
	  double denominator = 0;
	  double denom1 = 0;
	  double denom2 = 0;
	  ArrayList<Double> movieRatings1 = new ArrayList<Double>();
	  ArrayList<Double> movieRatings2 = new ArrayList<Double>();
	 for (int i=0; i<userRatings.size(); i++) {
		  if (userRatings.get(i+1).containsKey(movie1)) {
			  movieRatings1.add(userRatings.get(i+1).get(movie1));
		  }
		  else {
			  movieRatings1.add(0.0);
		  }
		  if (userRatings.get(i+1).containsKey(movie2)) {
			  movieRatings2.add(userRatings.get(i+1).get(movie2));
		  }
		  else {
			  movieRatings2.add(0.0);
		  }
	  }
	  //this is the main calculation.  iterates through each list on order and adds the factor at each point i to the numerator value following the formula listed below 
	  //The denominator values are first calculated one at a time since we need the square root of both sides.  
	  
	  
	  
	  
		//					
		//				X=movie1 rating, Y=movie2 rating, n=number of users
	    //
		//			         
		//							 
		//							 Σ (X 1,n * Y 1,n)      
		//			        -------------------------------------
		//			    sqrt(    Σ (X^2 1,n)  *  Σ (Y^2 1,n)    )
		//
		//
		//
		//
		//				
	  
	  
	  
	  
	  
	  for (int i=0; i<movieRatings1.size(); i++) {
		  numerator += movieRatings1.get(i) * movieRatings2.get(i); 
		  denom1 += movieRatings1.get(i) * movieRatings1.get(i);
		  denom2 += movieRatings2.get(i) * movieRatings2.get(i);
		  denominator = Math.sqrt(denom1) * Math.sqrt(denom2);
	  }
	 double answer = numerator/denominator;
	 return answer;
  }//end calculateSimilarity method
}//end class MovieRecSystem
















