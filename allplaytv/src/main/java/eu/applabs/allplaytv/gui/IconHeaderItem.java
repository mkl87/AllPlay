package eu.applabs.allplaytv.gui;

import android.graphics.drawable.Drawable;
import android.support.v17.leanback.widget.HeaderItem;

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

    public Drawable getIcon() {
        return mIcon;
    }

}
