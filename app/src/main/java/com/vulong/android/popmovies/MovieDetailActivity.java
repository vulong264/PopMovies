package com.vulong.android.popmovies;

import android.app.Activity;
import android.app.Application;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.widget.TextViewCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

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
import java.util.List;
import java.util.Vector;

/**
 * Created by mrlong on 4/29/16.
 */
public class MovieDetailActivity extends Activity {
    private String strMovieId = "";
    private TextView titleTextview;
    private ImageView posterImgView;
    ArrayList<String> listTrailer=new ArrayList<String>();
    ArrayAdapter<String> trailerAdapter;
    ListView trailerListView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = getIntent().getExtras();
        strMovieId = bundle.getString("MovieID");
        setContentView(R.layout.detail_movie_layout);
        titleTextview = (TextView) findViewById(R.id.detail_title_textview);
        posterImgView = (ImageView) findViewById(R.id.poster_img_view);
        try
        {
            FetchMovieDetailTask fetchData = new FetchMovieDetailTask();
            fetchData.execute(strMovieId,"info");}
        catch(Exception ex)
        {
            Log.e("FetchDetailData", "Error ", ex);
        }
        trailerAdapter=new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,listTrailer);
        trailerListView = (ListView) findViewById(R.id.trailer_listview);
        trailerListView.setAdapter(trailerAdapter);
        try
        {
            FetchMovieDetailTask fetchData = new FetchMovieDetailTask();
            fetchData.execute(strMovieId,"videos");}
        catch(Exception ex)
        {
            Log.e("FetchDetailData", "Error ", ex);
        }
        trailerListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String strLink = trailerAdapter.getItem(position).toString();
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.youtube.com/watch?v="+strLink)));
            }
        });
    }
    public class FetchMovieDetailTask extends AsyncTask<String, Void, Vector> {
        private final String LOG_TAG = FetchMovieDetailTask.class.getSimpleName();
        private String strQueryType = "info";
        @Override
        protected Vector doInBackground(String... params) {
            // These two need to be declared outside the try/catch
            // so that they can be closed in the finally block.

            final String QUERY_COMMAND = "http://api.themoviedb.org/3/movie/";
            final String APPID_PARAM = "api_key";
            final String APPID_VALUE = "af7d6c47be759946143eb257a037ca1c";
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;
            // Will contain the raw JSON response as a string.
            String forecastJsonStr = null;
            try {

                String strMovieID = "";
                String strVideo = "";
                if (params != null)
                    strMovieID = params[0];
                strQueryType = "info";
                String strQueryCommand = QUERY_COMMAND+strMovieID;
                try{
                if(params[1]!=null) {
                    if(params[1].equals("videos")) {
                        strQueryCommand = strQueryCommand + "/videos";
                        strQueryType = "trailer";
                    }
                }}
                catch(Exception ex)
                {}


                strQueryCommand = strQueryCommand + "?";

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
                if(strQueryType.equals("trailer"))
                    return getTrailerFromJson(forecastJsonStr);
                else
                    return getMovieDataFromJson(forecastJsonStr);

            } catch (JSONException e) {
                Log.e(LOG_TAG, e.getMessage(), e);
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(Vector vtResult) {
            try {
                super.onPostExecute(vtResult);
                if(strQueryType.equals("trailer"))
                {
                    trailerAdapter.clear();
                    for (int i=0;i<vtResult.size();i++) {
                        Vector vtTrailer = (Vector) vtResult.elementAt(i);
                        trailerAdapter.add(vtTrailer.elementAt(2).toString());
                    }
                    trailerAdapter.notifyDataSetChanged();
                }
                else {
                    titleTextview.setText(vtResult.elementAt(1).toString());
                    String strPosterPath = correctPath(vtResult.elementAt(2).toString());
                    Picasso.with(getBaseContext()).load(correctPath(strPosterPath)).into(posterImgView);
                }
            }
            catch (Exception ex)
            {
                Log.e(LOG_TAG, ex.getMessage(),ex);
                ex.printStackTrace();
                CharSequence errTxt = ex.getMessage();
                Toast toast = Toast.makeText(getBaseContext(), errTxt, Toast.LENGTH_SHORT);
                toast.show();
                Log.e(LOG_TAG,ex.getMessage(),ex);
            }
        }
        public String correctPath(String detailPath)
        {
            return "http://image.tmdb.org/t/p/w780"+detailPath;
        }
        private Vector getMovieDataFromJson(String listMovieJsonStr)
                throws JSONException {
            final String OWM_RESULT = "results";
            final String OWM_TITLE = "title";
            final String OWM_IMAGE = "poster_path";
            final String OWM_ID = "id";

            JSONObject movieJson = new JSONObject(listMovieJsonStr);

            Vector vtResult = new Vector();
            vtResult.add(movieJson.getString(OWM_ID));
            vtResult.add(movieJson.getString(OWM_TITLE));
            vtResult.add(movieJson.getString(OWM_IMAGE));

//        String[] resultStrs = new String[moviesArray.length()];
//            for (int i = 0; i < moviesArray.length(); i++) {
//                JSONObject movieObject = moviesArray.getJSONObject(i);
//
//            }

//        for (String s : resultStrs) {
//            Log.v(LOG_TAG, "Forecast entry: " + s);
//        }
            return vtResult;
        }
        private Vector getTrailerFromJson(String listMovieJsonStr)
                throws JSONException {
            final String OWM_RESULT = "results";
            final String OWM_TITLE = "name";
            final String OWM_VID_ID = "key";
            final String OWM_ID = "id";

            JSONObject listTrailerJson = new JSONObject(listMovieJsonStr);
            JSONArray trailerArray = listTrailerJson.getJSONArray(OWM_RESULT);

            Vector vtResult = new Vector();

            for (int i = 0; i < trailerArray.length(); i++) {
                JSONObject trailerObject = trailerArray.getJSONObject(i);
                Vector mvDetail = new Vector();
                mvDetail.add(trailerObject.getString(OWM_ID));
                mvDetail.add(trailerObject.getString(OWM_TITLE));
                mvDetail.add(trailerObject.getString(OWM_VID_ID));
                vtResult.add(mvDetail);
            }

            return vtResult;
        }
    }
}
