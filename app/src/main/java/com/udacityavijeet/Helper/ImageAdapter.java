package com.udacityavijeet.Helper;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.support.v4.util.LruCache;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.udacityavijeet.R;

import java.util.HashMap;
import java.util.List;


public class ImageAdapter extends ArrayAdapter<ContentMovie> {


    HashMap<String, Boolean> isRequested;
    private LruCache<String, Bitmap> mMemoryCacheB;

    //private LruCache<String, String> mMemoryCacheT;

    public ImageAdapter(Context context, List<ContentMovie> objects) {
        super(context, 0, objects);

        final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);
        // Use 1/8th of the available memory for this memory cache.
        final int cacheSize = maxMemory / 8 ;

        mMemoryCacheB = new android.support.v4.util.LruCache<String, Bitmap>(cacheSize) {
            @Override
            protected int sizeOf(String key, Bitmap bitmap) {
                // The cache size will be measured in kilobytes rather than
                // number of items.
                return bitmap.getByteCount() / 1024;
            }
        };

        isRequested = new HashMap<>();

//        mMemoryCacheT = new android.support.v4.util.LruCache<String, String>(cacheSize) {
//            @Override
//            protected int sizeOf(String key, String value) {
//                // The cache size will be measured in kilobytes rather than
//                // number of items.
//                return value.getBytes() / 1024;
//            }
//        };

    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ContentMovie contentMovie = getItem(position);
        View v;
        WindowManager wm = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        Point point = new Point();
        display.getSize(point);
        int width = (point.x)/2;
        int layoutW, layoutH;
        layoutW = width-15;
        layoutH = (int)(1.5 * (width-15));

        ImageView imageView;
        TextView textView;
//        if (convertView == null) {
//            convertView = LayoutInflater.from(getContext()).inflate(
//                    R.layout.grid_view_layout, parent, false);

            LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(     Context.LAYOUT_INFLATER_SERVICE );
            v = inflater.inflate(R.layout.grid_view_layout, parent, false);
//        } else {
//            v = (View) convertView;
//        }

        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(layoutW, layoutH );

        imageView = (ImageView) v.findViewById(R.id.gridViewLayoutImage);
        imageView.setLayoutParams(params);
        imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
        textView = (TextView) v.findViewById(R.id.gridViewLayoutText );
        textView.setText("");

        Bitmap bitmap = getBitmapFromMemCache(contentMovie.details[0]);
        if (bitmap != null) {
            Log.v("MyApp", "Cache:" + contentMovie.details[1] );
            imageView.setImageBitmap(bitmap);
        } else if( isRequested.get(contentMovie.details[0])==null ) {
            //imageView.setImageResource(R.drawable.image_placeholder);
            Log.v("MyApp", "Download:" + contentMovie.details[1] );
            isRequested.put(contentMovie.details[0], true);
            downloadIMG(contentMovie.details[0], contentMovie.details[2], imageView, layoutW, layoutH );
        } else {
            downloadIMG(contentMovie.details[0], contentMovie.details[2], imageView, layoutW, layoutH );
        }

        return  v;
    }

    public void addBitmapToMemoryCache(String key, Bitmap bitmap) {
        if (getBitmapFromMemCache(key) == null) {
            mMemoryCacheB.put(key, bitmap);
        }
    }

    public Bitmap getBitmapFromMemCache(String key) {
        return mMemoryCacheB.get(key);
    }

    private void downloadIMG (final String  ID, final String url, final ImageView imageViewIMG, final int layoutW, final int layoutH ){
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
                    // load image into imageview
                    Bitmap temp = Bitmap.createScaledBitmap(response.getBitmap(), layoutW, layoutH, true);
                    addBitmapToMemoryCache(ID, temp);
                    imageViewIMG.setImageBitmap(temp);
                }
            }
        });
    }
}
