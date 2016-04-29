package com.vulong.android.popmovies;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.List;

/**
 * Created by mrlong on 4/28/16.
 */
public class MoviePosterAdapter extends ArrayAdapter<MoviePoster> {
    private static final String LOG_TAG = MoviePosterAdapter.class.getSimpleName();

    public MoviePosterAdapter(Activity context, List<MoviePoster> moviePosters) {
        // Here, we initialize the ArrayAdapter's internal storage for the context and the list.
        // the second argument is used when the ArrayAdapter is populating a single TextView.
        // Because this is a custom adapter for two TextViews and an ImageView, the adapter is not
        // going to use this second argument, so it can be any value. Here, we used 0.
        super(context, 0, moviePosters);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Gets the AndroidFlavor object from the ArrayAdapter at the appropriate position
        MoviePoster moviePoster = getItem(position);

        // Adapters recycle views to AdapterViews.
        // If this is a new View object we're getting, then inflate the layout.
        // If not, this view already has the layout inflated from a previous call to getView,
        // and we modify the View widgets as usual.
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(
                    R.layout.poster_layout, parent, false);
        }

        ImageView posterView = (ImageView) convertView.findViewById(R.id.poster_img);
        Picasso.with(this.getContext()).load(correctPath(moviePoster.posterPath)).into(posterView);

//        TextView versionNameView = (TextView) convertView.findViewById(R.id.movie_title);
//        versionNameView.setText(moviePoster.movieTitle);

        return convertView;
    }

    public String correctPath(String detailPath)
    {
        return "http://image.tmdb.org/t/p/w780"+detailPath;
    }
}
