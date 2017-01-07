package eu.applabs.allplaytv.gui;

import android.app.Activity;

import com.bumptech.glide.Glide;

public class BaseActivity extends Activity {

    @Override
    protected void onDestroy() {
        Glide.get(this).clearMemory();

        super.onDestroy();
    }

}
