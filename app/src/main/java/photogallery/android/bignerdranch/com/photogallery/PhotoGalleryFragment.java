package photogallery.android.bignerdranch.com.photogallery;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.SearchManager;
import android.app.SearchableInfo;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import java.io.IOException;
import java.util.ArrayList;

public class PhotoGalleryFragment extends VisibleFragment {

    public static final String TAG = "PhotoGalleryFragment";

    GridView gridView;
    ArrayList<GalleryItem> items;
    ThumbnailDownloader<ImageView> thumbnailThread;

    @TargetApi(11)
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
    {
        super.onCreateOptionsMenu(menu, inflater);

        inflater.inflate(R.menu.fragment_photo_gallery, menu);

        if ( Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB )
        {
            MenuItem searchItem = menu.findItem(R.id.menu_item_search);

            SearchView searchView = (SearchView)searchItem.getActionView();

            SearchManager searchManager = (SearchManager)getActivity().getSystemService(Context.SEARCH_SERVICE);

            ComponentName name = getActivity().getComponentName();

            SearchableInfo searchInfo = searchManager.getSearchableInfo(name);

            searchView.setSearchableInfo(searchInfo);
        }
    }

    @Override
    @TargetApi(11)
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch ( item.getItemId() )
        {
            case R.id.menu_item_search:
                getActivity().onSearchRequested();
                return true;
            case R.id.menu_item_toggle_polling:
                boolean shouldStartAlarm = !PollService.isServiceAlarmOn(getActivity());
                PollService.setServiceAlarm(getActivity(), shouldStartAlarm);

                if ( Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB )
                {
                    getActivity().invalidateOptionsMenu();
                }

                return true;
            case R.id.menu_item_clear:
                PreferenceManager.getDefaultSharedPreferences(getActivity()).edit().putString(FlickrFetchr.PREF_SEARCH_QUERY, null)
                        .commit();
                updateItems();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu)
    {
        super.onPrepareOptionsMenu(menu);

        MenuItem toggleItem = menu.findItem(R.id.menu_item_toggle_polling);

        if ( PollService.isServiceAlarmOn(getActivity()) )
        {
            toggleItem.setTitle(R.string.stop_polling);
        }
        else
        {
            toggleItem.setTitle(R.string.start_polling);
        }
    }

    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        setHasOptionsMenu(true);

        updateItems();

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

    public void updateItems() {
        new FetchItemsTask().execute();
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
            Activity activity = getActivity();

            if ( activity == null )
                return new ArrayList<GalleryItem>();

            String query = PreferenceManager.getDefaultSharedPreferences(activity).getString(FlickrFetchr.PREF_SEARCH_QUERY, null);

            if ( query != null )
            {
                return new FlickrFetchr().search(query);
            }
            else
            {
                return new FlickrFetchr().fetchItems();
            }
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
