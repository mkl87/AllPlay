package eu.applabs.allplaylibrary.services.deezer;

import eu.applabs.allplaylibrary.data.ServiceCategory;

public class DeezerCategory extends ServiceCategory {

    private String mName;

    public DeezerCategory(String name) {
        mName = name;
    }

    @Override
    public String getCategoryName() {
        return mName;
    }
}
