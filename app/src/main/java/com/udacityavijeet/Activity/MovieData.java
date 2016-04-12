package com.udacityavijeet.Activity;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.udacityavijeet.Helper.AppController;
import com.udacityavijeet.R;

public class MovieData extends AppCompatActivity {

    ImageView poster, backdrop;
    TextView title, overview, userR, releaseD;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_data);


        String name = getIntent().getStringExtra("movieTitle");
        String synopsis = getIntent().getStringExtra("movieSynopsis");
        String rating = getIntent().getStringExtra("movieUR");
        String backdropURL = getIntent().getStringExtra("movieBackdrop");
        String posterURL = getIntent().getStringExtra("moviePoster");
        String release = getIntent().getStringExtra("movieRD");

        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        CollapsingToolbarLayout collapsingToolbar =
                (CollapsingToolbarLayout) findViewById(R.id.collapsing_toolbar);
        collapsingToolbar.setTitle(name);

        WindowManager wm = (WindowManager) getApplicationContext().getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        Point point = new Point();
        display.getSize(point);
        int width = (point.x)/2;
        int layoutW, layoutH;
        layoutW = width-15;
        layoutH = (int)(1.5 * (width-15));

//        poster = (ImageView) findViewById(R.id.imageMV_Poster);
        backdrop = (ImageView) findViewById(R.id.imageMV_backdrop);
//        downloadIMG(posterURL, poster, layoutW, layoutH);
        downloadIMG(backdropURL, backdrop, layoutW, layoutH);

//        title = (TextView) findViewById(R.id.textMV_Name);
        overview = (TextView) findViewById(R.id.textMV_Synopsis);
        userR = (TextView) findViewById(R.id.textMV_UserR);
        releaseD = (TextView) findViewById(R.id.textMV_RD);

//        title.setText(name);
        overview.setText(synopsis);
        userR.setText("Rating : " + rating);
        releaseD.setText("Release Date : " + release);
//        fabListener((FloatingActionButton) findViewById(R.id.fab_play));
    }

//    private void fabListener(FloatingActionButton fab){
//        fab.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
////                Toast.makeText(getApplicationContext(), "Trailer to be added in Stage 2", Toast.LENGTH_LONG).show();
//                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.youtube.com/watch?v=cxLG2wtE7TM")));
//            }
//        });
//    }

    private void downloadIMG (final String url, final ImageView imageViewIMG, final int layoutW, final int layoutH ){
        ImageLoader imageLoader = AppController.getInstance().getImageLoader();

// If you are using normal ImageView
        imageLoader.get(url, new ImageLoader.ImageListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("MyApp", "Image Load Error: " + error.getMessage());
            }

            @Override
            public void onResponse(ImageLoader.ImageContainer response, boolean arg1) {
                if (response.getBitmap() != null) {
                    Bitmap temp = Bitmap.createScaledBitmap(response.getBitmap(), layoutW, layoutH, true);
                    imageViewIMG.setImageBitmap(temp);
                }
            }
        });
    }
}
