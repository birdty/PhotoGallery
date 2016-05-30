package photogallery.android.bignerdranch.com.photogallery;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class PhotoGalleryActivity extends SingleFragmentActivity {

    @Override
    public Fragment createFragment()
    {
        return new PhotoGalleryFragment();
    }
}
