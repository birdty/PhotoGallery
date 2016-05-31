package photogallery.android.bignerdranch.com.photogallery;

import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import java.io.IOException;
import java.util.ArrayList;

public class PhotoGalleryFragment extends Fragment {

    public static final String TAG = "PhotoGalleryFragment";

    GridView gridView;
    ArrayList<GalleryItem> items;
    ThumbnailDownloader<ImageView> thumbnailThread;

    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        new FetchItemsTask().execute();

        thumbnailThread = new ThumbnailDownloader<ImageView>(new Handler());

        thumbnailThread.setListener(new ThumbnailDownloader.Listener<ImageView>() {
            public void onThumbnailDownloaded(ImageView imageView, Bitmap thumbnail) {
                if ( isVisible() ) {
                    imageView.setImageBitmap(thumbnail);
                }
            }
        }
        );

        thumbnailThread.start();

        thumbnailThread.getLooper();

        Log.i(TAG, "Background thread started");

    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
        thumbnailThread.clearQueue();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View v = inflater.inflate(R.layout.fragment_photo_gallery, container, false);

        gridView = (GridView)v.findViewById(R.id.gridView);

        setupAdapter();

        return v;
    }

    void setupAdapter()
    {
        if ( getActivity() == null || gridView == null ) return;

        if ( items != null )
        {
            gridView.setAdapter( new GalleryItemAdapter(items));
        }
        else
        {
            gridView.setAdapter(null);
        }
    }

    private class FetchItemsTask extends AsyncTask<Void, Void, ArrayList<GalleryItem>>
    {
        @Override
        protected ArrayList<GalleryItem> doInBackground(Void... params)
        {
            return new FlickrFetchr().fetchItems();
        }

        protected void onPostExecute(ArrayList<GalleryItem> newItems)
        {
            items = newItems;
            setupAdapter();
        }
    }

    private class GalleryItemAdapter extends ArrayAdapter<GalleryItem> {
        public GalleryItemAdapter(ArrayList<GalleryItem> items)
        {
            super(getActivity(), 0, items);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent)
        {
            if ( convertView == null )
            {
                convertView = getActivity().getLayoutInflater().inflate(R.layout.gallery_item, parent, false);
            }

            ImageView imageView = (ImageView) convertView.findViewById(R.id.gallery_item_imageView);

            imageView.setImageResource(R.drawable.brian_up_close);

            GalleryItem item = getItem(position);

            thumbnailThread.queueThumbnail(imageView, item.getUrl());

            return convertView;
        }
    }
}
