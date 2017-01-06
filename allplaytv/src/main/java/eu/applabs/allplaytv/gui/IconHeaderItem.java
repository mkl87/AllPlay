package eu.applabs.allplaytv.gui;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.v17.leanback.widget.HeaderItem;
import android.support.v4.content.ContextCompat;

import eu.applabs.allplaylibrary.services.ServiceType;
import eu.applabs.allplaytv.R;

public class IconHeaderItem extends HeaderItem {

    Drawable mIcon;

    public IconHeaderItem(long id, String name, Drawable icon) {
        super(id, name);

        mIcon = icon;
    }

    public IconHeaderItem(String name, Drawable icon) {
        super(name);

        mIcon = icon;
    }

    public IconHeaderItem(Context context, String name, ServiceType serviceType) {
        super(name);

        switch (serviceType) {
            case SPOTIFY:
                mIcon = ContextCompat.getDrawable(context, R.drawable.ic_spotify);
                break;
            case DEEZER:
                mIcon = ContextCompat.getDrawable(context, R.drawable.ic_deezer);
                break;
            case GOOGLE_MUSIC:
                mIcon = ContextCompat.getDrawable(context, R.drawable.ic_googlemusic);
                break;
            default:
                mIcon = ContextCompat.getDrawable(context, R.drawable.ic_default);
                break;
        }
    }

    public Drawable getIcon() {
        return mIcon;
    }

}
