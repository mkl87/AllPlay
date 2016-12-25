package eu.applabs.allplaytv.data;

import android.content.Intent;
import android.graphics.drawable.Drawable;

public class Action {
    private String m_Name = null;
    private Drawable m_Icon = null;
    private Intent m_Intent = null;

    public Action() {
        m_Name = "";
    }

    public void setName(String name) {
        m_Name = name;
    }

    public void setIcon(Drawable icon) {
        m_Icon = icon;
    }

    public void setIntent(Intent intent) {
        m_Intent = intent;
    }

    public String getName() {
        return m_Name;
    }

    public Drawable getIcon() {
        return m_Icon;
    }

    public Intent getIntent() {
        return m_Intent;
    }
}
