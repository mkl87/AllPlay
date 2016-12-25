package eu.applabs.allplaytv.gui;

import android.app.Activity;
import android.app.FragmentManager;
import android.os.Bundle;
import android.support.v17.leanback.app.BackgroundManager;
import android.view.KeyEvent;

import com.bumptech.glide.Glide;

import eu.applabs.allplaytv.R;

public class SearchActivity extends Activity {

    private FragmentManager m_FragmentManager = null;
    private PlaylistSearchFragment m_PlaylistSearchFragment = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        m_FragmentManager = getFragmentManager();
        m_PlaylistSearchFragment = (PlaylistSearchFragment) m_FragmentManager.findFragmentById(R.id.id_frag_SearchActivity);
        m_PlaylistSearchFragment.setSearchActivity(this);

        BackgroundManager backgroundManager = BackgroundManager.getInstance(this);
        backgroundManager.attach(this.getWindow());
        backgroundManager.setDrawable(getResources().getDrawable(R.drawable.background, null));
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
                m_PlaylistSearchFragment.clear();
                m_PlaylistSearchFragment.startRecognition();
                return true;
            default:
                return super.onKeyUp(keyCode, event);
        }
    }

}
