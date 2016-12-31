package eu.applabs.allplaylibrary.services.spotify;

import eu.applabs.allplaylibrary.data.ServiceCategory;

public class SpotifyCategory extends ServiceCategory {

    private String mName = null;

    public SpotifyCategory(String name) {
        mName = name;
    }

    @Override
    public String getCategoryName() {
        return mName;
    }
}
