package eu.applabs.allplaytv.data;

import android.content.Intent;
import android.graphics.drawable.Drawable;

public class Action {

    private String mName;
    private Drawable mIcon;
    private Intent mIntent;

    public Action() {
        mName = "";
    }

    public void setName(String name) {
        mName = name;
    }

    public void setIcon(Drawable icon) {
        mIcon = icon;
    }

    public void setIntent(Intent intent) {
        mIntent = intent;
    }

    public String getName() {
        return mName;
    }

    public Drawable getIcon() {
        return mIcon;
    }

    public Intent getIntent() {
        return mIntent;
    }
}
