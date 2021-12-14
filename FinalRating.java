package Netflix_Movie_Recommendation_System;

import java.util.HashMap;

//simple final rating class is used for the priorty q to store the final values.. 
//this is needed so we can override the compare method so it sorts by movie rating
public class FinalRating implements Comparable<FinalRating> {
	
	private int userId;
	private int movieId;
	private double movieRating;
	private HashMap<Integer, String> movieList;
	
	public FinalRating (int uid, int id, double rating, HashMap<Integer, String> movieList) {
		this.userId = uid;
		this.movieId = id;
		this.movieRating = rating;
		this.movieList = movieList;
	}
	
	public int getUserId() {
		return userId;
	}
	
	public HashMap<Integer, String> getMovieList() {
		return movieList;
	}

	public int getMovieId() {
		return movieId;
	}

	public double getMovieRating() {
		return movieRating;
	}

	public void setMovieId(int movieId) {
		this.movieId = movieId;
	}

	public void setMovieRating(double movieRating) {
		this.movieRating = movieRating;
	}
	
	public boolean equals(FinalRating other) {
		return this.getMovieRating() == other.getMovieRating();
	}
	
	//this needs to be overridden in order for the priority q to sort by movie rating
	public int compareTo(FinalRating other) {
		if (getMovieRating() == other.getMovieRating())
			return 0;
		else if (getMovieRating() > other.getMovieRating())
			return -1;
		else
			return 1;
	}
	
	public String toString() {
		return movieList.get(getMovieId()) + "  Predicted Score: " + getMovieRating();
	}

}
