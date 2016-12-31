package eu.applabs.allplaytv.gui;

import android.app.Activity;
import android.app.FragmentManager;
import android.os.Bundle;
import android.support.v17.leanback.app.BackgroundManager;
import android.support.v4.content.ContextCompat;
import android.view.KeyEvent;

import com.bumptech.glide.Glide;

import eu.applabs.allplaytv.R;

public class SearchActivity extends Activity {

    private PlaylistSearchFragment mPlaylistSearchFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        mPlaylistSearchFragment = (PlaylistSearchFragment) getFragmentManager().findFragmentById(R.id.id_frag_SearchActivity);
        mPlaylistSearchFragment.setSearchActivity(this);

        BackgroundManager backgroundManager = BackgroundManager.getInstance(this);
        backgroundManager.attach(this.getWindow());
        backgroundManager.setDrawable(ContextCompat.getDrawable(this, R.drawable.background));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        Glide.get(this).clearMemory();
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_SEARCH:
                mPlaylistSearchFragment.clear();
                mPlaylistSearchFragment.startRecognition();
                return true;
            default:
                return super.onKeyUp(keyCode, event);
        }
    }

}
