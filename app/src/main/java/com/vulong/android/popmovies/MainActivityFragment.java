package com.vulong.android.popmovies;

import android.content.Intent;
import android.graphics.Movie;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Vector;
import java.util.concurrent.ExecutionException;

/**
 * A placeholder fragment containing a simple view.
 */
public class MainActivityFragment extends Fragment {
    private final String LOG_TAG = MainActivityFragment.class.getSimpleName();
    private MoviePosterAdapter movieAdapter;
    private ArrayList<MoviePoster> moviePosterList;

    public MainActivityFragment() {
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.main_fragment, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.action_refresh: {
                updateMovies();
                break;
            }
            default:
                break;

        }

        return super.onOptionsItemSelected(item);
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        moviePosterList = new ArrayList<MoviePoster>();
        movieAdapter = new MoviePosterAdapter(getActivity(), moviePosterList);

        // Get a reference to the ListView, and attach this adapter to it.
        final GridView gridView = (GridView) rootView.findViewById(R.id.movie_poster_grid);
        gridView.setAdapter(movieAdapter);

        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
//                MoviePoster mvPos = movieAdapter.getItem(i);
//                CharSequence text = mvPos.movieTitle;
//                Toast toast = Toast.makeText(gridView.getContext(), text, Toast.LENGTH_SHORT);
//                toast.show();
                try {
                    Intent movieDetailIntent = new Intent(getActivity(), MovieDetailActivity.class);
//                    MoviePoster selectedMovie = (MoviePoster)adapterView.getItemAtPosition(i);
                    MoviePoster selectedMovie = movieAdapter.getItem(i);
                    movieDetailIntent.putExtra("MovieID",selectedMovie.movieId);
                    startActivity(movieDetailIntent);
                }
                catch (Exception ex)
                {
                    CharSequence errTxt = ex.getMessage();
                    Toast toast = Toast.makeText(getContext(), errTxt, Toast.LENGTH_SHORT);
                    toast.show();
                    Log.e(LOG_TAG,ex.getMessage(),ex);
                }
            }
        }
        );


        return rootView;
    }

    public class FetchMovieTask extends AsyncTask<String, Void, Vector> {
        private final String LOG_TAG = FetchMovieTask.class.getSimpleName();
        @Override
        protected Vector doInBackground(String... params) {
            // These two need to be declared outside the try/catch
            // so that they can be closed in the finally block.

            final String QUERY_POPULAR = "http://api.themoviedb.org/3/movie/popular?";
            final String QUERY_TOPRATED = "http://api.themoviedb.org/3/movie/top_rated?";
            final String APPID_PARAM = "api_key";
            final String APPID_VALUE = "af7d6c47be759946143eb257a037ca1c";
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;
            // Will contain the raw JSON response as a string.
            String forecastJsonStr = null;
            try {
                String strQueryCommand = QUERY_POPULAR;
                String strQueryType = "";
                if (params[0] != null)
                    strQueryType = params[0];
                if (!strQueryType.equals("POP"))
                    strQueryCommand = QUERY_TOPRATED;

                Uri builtUri = Uri.parse(strQueryCommand).buildUpon()
                        .appendQueryParameter(APPID_PARAM, APPID_VALUE)
                        .build();

                URL url = new URL(builtUri.toString());
                // Create the request to OpenWeatherMap, and open the connection
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                // Read the input stream into a String
                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if (inputStream == null) {
                    // Nothing to do.
                    return null;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {
                    // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                    // But it does make debugging a *lot* easier if you print out the completed
                    // buffer for debugging.
                    buffer.append(line + "\n");
                }

                if (buffer.length() == 0) {
                    // Stream was empty.  No point in parsing.
                    return null;
                }
                forecastJsonStr = buffer.toString();
                Log.v(LOG_TAG, "Forecase JSON STRING:" + forecastJsonStr);
            } catch (IOException e) {
                Log.e("PlaceholderFragment", "Error ", e);
                // If the code didn't successfully get the weather data, there's no point in attemping
                // to parse it.
                return null;
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        Log.e("PlaceholderFragment", "Error closing stream", e);
                    }
                }
            }

            try {
                    return getMovieDataFromJson(forecastJsonStr);

            } catch (JSONException e) {
                Log.e(LOG_TAG, e.getMessage(), e);
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Vector vtResult) {
            try {
                super.onPostExecute(vtResult);
                movieAdapter.clear();
//                moviePosterList = new MoviePoster[vtResult.size()];
                for (int i = 0; i < vtResult.size(); i++) {
                    Vector vtMovie = (Vector) vtResult.elementAt(i);
                    String strId = vtMovie.elementAt(0).toString();
                    String strTitle = vtMovie.elementAt(1).toString();
                    String strPath = vtMovie.elementAt(2).toString();
                    MoviePoster mv = new MoviePoster(strId,strTitle,strPath);
                    movieAdapter.add(mv);
//                    moviePosterList[i]=mv;
                }

            }
            catch (Exception e)
            {
                Log.e(LOG_TAG, e.getMessage(),e);
                e.printStackTrace();
            }
        }
    }
    String strSort = "";
    String POP = "POP";
    String TOP = "TOP";
    private void updateMovies() {
        FetchMovieTask fetchData = new FetchMovieTask();
        if(!strSort.equals(POP))
            strSort = POP;
        else
            strSort = TOP;
        fetchData.execute(strSort);
    }
    private Vector getMovieDataFromJson(String listMovieJsonStr)
            throws JSONException {
        final String OWM_RESULT = "results";
        final String OWM_TITLE = "title";
        final String OWM_IMAGE = "poster_path";
        final String OWM_ID = "id";

        JSONObject listMovieJson = new JSONObject(listMovieJsonStr);
        JSONArray moviesArray = listMovieJson.getJSONArray(OWM_RESULT);

        Vector vtResult = new Vector();

//        String[] resultStrs = new String[moviesArray.length()];
        for (int i = 0; i < moviesArray.length(); i++) {
            JSONObject movieObject = moviesArray.getJSONObject(i);
            Vector mvDetail = new Vector();
            mvDetail.add(movieObject.getString(OWM_ID));
            mvDetail.add(movieObject.getString(OWM_TITLE));
            mvDetail.add(movieObject.getString(OWM_IMAGE));
            vtResult.add(mvDetail);
        }

//        for (String s : resultStrs) {
//            Log.v(LOG_TAG, "Forecast entry: " + s);
//        }
        return vtResult;
    }
}
