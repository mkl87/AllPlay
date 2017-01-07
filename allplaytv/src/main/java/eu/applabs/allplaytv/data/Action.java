package eu.applabs.allplaytv.data;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;

public class Action {

    private String mName;
    private Drawable mIcon;
    private Intent mIntent;

    public Action(@NonNull String name, @NonNull Drawable icon, @NonNull Intent intent) {
        mName = name;
        mIcon = icon;
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
