package com.udacityavijeet.Activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.Toast;

import com.udacityavijeet.Helper.ContentMovie;
import com.udacityavijeet.Helper.ImageAdapter;
import com.udacityavijeet.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;

public class Home extends AppCompatActivity {

    GridView gridView;
    ImageAdapter imageAdapter;
    final String BaseURL = "http://api.themoviedb.org/3/discover/movie";
    final String BaseIMG = "http://image.tmdb.org/t/p/w154";
    final String BaseIMG2 = "http://image.tmdb.org/t/p/w500";
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    AlertDialog alertDialog;
    ArrayList<ContentMovie> movieList;

    private int visibleThreshold = 5;
    private int currentPage = 1;
    private int previousTotal = 0;
    private boolean loading = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        getSupportActionBar().setTitle(getResources().getString(R.string.app_name));

        sharedPreferences = getApplicationContext().getSharedPreferences("Sort", getApplicationContext().MODE_PRIVATE);
        editor = sharedPreferences.edit();
        editor.putString("sort", "popularity.desc");
        editor.apply();
        movieList = new ArrayList<>();
        final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);

        // Use 1/8th of the available memory for this memory cache.
        final int cacheSize = maxMemory / 8;
        gridView = (GridView) findViewById(R.id.gridView_category);



        if(savedInstanceState==null){
            Log.v("MyApp", "onCreate() null");
            SearchAPI searchAPI = new SearchAPI();
            searchAPI.execute();
        } else {
            Log.v("MyApp", "onCreate() not null");
            int size = savedInstanceState.getInt("size");
            movieList = savedInstanceState.getParcelableArrayList("movie");
            imageAdapter = new ImageAdapter(getApplicationContext(), movieList);
            gridView.setAdapter(imageAdapter);
        }

        createDialog();
        gridScroll();

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        Log.v("MyApp", "onSaveInstanceState");
        outState.putInt("size", movieList.size());
        outState.putParcelableArrayList("movie", movieList );
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        Log.v("MyApp", "onRestoreInstanceState");
        int size = savedInstanceState.getInt("size");
        movieList = savedInstanceState.getParcelableArrayList("movie");

        imageAdapter = new ImageAdapter(getApplicationContext(), movieList);
        gridView.setAdapter(imageAdapter);
    }

    private void gridScroll(){

        gridView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {

            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem,
                                 int visibleItemCount, int totalItemCount) {
                if (loading) {
                    if (totalItemCount > previousTotal) {
                        loading = false;
                        previousTotal = totalItemCount;
                        currentPage++;
                    }
                }
                if (!loading && (totalItemCount - visibleItemCount) <= (firstVisibleItem + visibleThreshold)) {
                    // I load the next page of gigs using a background task,
                    // but you can call any function here.
                    SearchAPI searchAPI = new SearchAPI();
                    searchAPI.execute();
                    loading = true;
                }
            }
        });
    }

    private void gridListener(){
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Log.v("MyApp", movieList.get(position).details[0] + " " + movieList.get(position).details[1]);
                Intent intent = new Intent(Home.this, MovieData.class );
                intent.putExtra("movieID", movieList.get(position).details[0]);
                intent.putExtra("movieTitle", movieList.get(position).details[1]);
                intent.putExtra("moviePoster", movieList.get(position).details[2]);
                intent.putExtra("movieSynopsis", movieList.get(position).details[3]);
                intent.putExtra("movieUR", movieList.get(position).details[4]);
                intent.putExtra("movieRD", movieList.get(position).details[5]);
                intent.putExtra("movieBackdrop", movieList.get(position).details[6]);
                startActivity(intent);
            }
        });
    }

    private void createDialog(){
        final CharSequence s[] = {"Popularity", "Highest Rated"};
        alertDialog = new AlertDialog.Builder(this)
                .setTitle("Select Sort By")
                .setSingleChoiceItems(s, 0, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (s[which].equals("Popularity")) {
                            Log.v("MyApp", "Dialog:Popularity");
                            editor.putString("sort", "popularity.desc");
                            editor.apply();
                            movieList.clear();
                            currentPage = 1;
                            SearchAPI searchAPI = new SearchAPI();
                            searchAPI.execute();
                            alertDialog.hide();
                        } else if (s[which].equals("Highest Rated")) {
                            Log.v("MyApp", "Dialog:Highest Rated");
                            editor.putString("sort", "vote_average.desc");
                            editor.apply();
                            movieList.clear();
                            currentPage = 1;
                            SearchAPI searchAPI = new SearchAPI();
                            searchAPI.execute();
                            alertDialog.hide();
                        }
                    }
                }).create();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.home_page, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.sort :
                alertDialog.show();
        }
        return super.onOptionsItemSelected(item);
    }

    public class SearchAPI extends AsyncTask<Void, Void, String> {

        @Override
        protected String doInBackground(Void... params) {

            String error = null;

            HttpURLConnection urlConnection = null;
            BufferedReader bufferedReader = null;

            URL url = null;
            try {

                Uri uri = Uri.parse(BaseURL).buildUpon().appendQueryParameter("sort_by", sharedPreferences.getString("sort", "popularity.desc"))
                        .appendQueryParameter("api_key", getString(R.string.api))
                        .appendQueryParameter("page", Integer.toString(currentPage)).build();

                Log.v("MyApp", getClass().toString() + " " + uri.toString());
                url = new URL(uri.toString());
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                InputStream inputStream = urlConnection.getInputStream();

                StringBuffer buffer = new StringBuffer();
                if (inputStream == null) {
                    return "null_inputstream";
                }

                bufferedReader = new BufferedReader(new InputStreamReader(inputStream));

                String line;

                while ((line = bufferedReader.readLine()) != null) {
                    buffer.append(line + '\n');
                }

                if (buffer.length() == 0) {
                    return "null_inputstream";
                }

                String stringJSON = buffer.toString();
                return stringJSON;
            } catch (UnknownHostException | ConnectException e) {
                error = "null_internet";
                e.printStackTrace();
            } catch (IOException e) {
                error = "null_file";
                e.printStackTrace();
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (bufferedReader != null) {
                    try {
                        bufferedReader.close();
                    } catch (final IOException e) {
                        Log.e("MyApp", "ErrorClosingStream", e);
                    }
                }
            }
            return error;
        }//doinbackground

        @Override
        protected void onPostExecute(String strJSON) {

            if (strJSON.equals("null_inputstream") || strJSON.equals("null_file")) {
                Toast.makeText(Home.this, "Unable to Connect to Internet", Toast.LENGTH_SHORT).show();
                return;
            }

            if (strJSON.equals("null_internet")) {
                Toast.makeText(Home.this, "No Internet Connectivity", Toast.LENGTH_SHORT).show();
                return;
            }

            Log.v("MyApp", getClass().toString() + strJSON);
            try {
                JSONObject jsonObject = new JSONObject(strJSON);
                JSONArray results = jsonObject.getJSONArray("results");

                for (int i = 0; i < results.length(); i++) {
                    JSONObject movie = results.getJSONObject(i);
                    movieList.add(new ContentMovie(
                            movie.getString("id"),
                            movie.getString("title"),
                            BaseIMG + movie.getString("poster_path"),
                            movie.getString("overview"),
                            movie.getString("vote_average"),
                            movie.getString("release_date"),
                            BaseIMG2 + movie.getString("backdrop_path")
                    ));
                }

                imageAdapter = new ImageAdapter(getApplicationContext(), movieList);
                gridView.setAdapter(imageAdapter);
                gridListener();
//                GetBitmap getBitmap = new GetBitmap();
//                getBitmap.execute();
                Log.v("MyApp", getClass().toString() + " End of onPost ");
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
}