package photogallery.android.bignerdranch.com.photogallery;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.GridView;

import java.io.IOException;
import java.util.ArrayList;

public class PhotoGalleryFragment extends Fragment {

    public static final String TAG = "PhotoGalleryFragment";

    GridView gridView;
    ArrayList<GalleryItem> items;

    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        new FetchItemsTask().execute();
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
            gridView.setAdapter(new ArrayAdapter<GalleryItem>(getActivity(), android.R.layout.simple_gallery_item, items));
        }
        else
        {
            gridView.setAdapter(null);
        }
    }

    private class FetchItemsTask extends AsyncTask<Void, Void, ArrayList<GalleryItem>> {
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
}
