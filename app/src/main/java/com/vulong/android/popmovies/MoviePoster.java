package com.vulong.android.popmovies;

/**
 * Created by mrlong on 4/28/16.
 */
public class MoviePoster {
    String movieId;
    String movieTitle;
    String posterPath;

    public MoviePoster(String vId, String vTitle, String vPath)
    {
        this.movieId = vId;
        this.movieTitle = vTitle;
        this.posterPath = vPath;
    }

}
