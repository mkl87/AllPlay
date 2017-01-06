package eu.applabs.allplaytv.adapter;

import android.support.v17.leanback.widget.ArrayObjectAdapter;

import eu.applabs.allplaylibrary.services.ServiceCategory;
import eu.applabs.allplaylibrary.services.ServicePlaylist;
import eu.applabs.allplaytv.presenter.PlaylistPresenter;

public class ServiceCategoryAdapter extends ArrayObjectAdapter {

    private ServiceCategory mServiceCategory;

    public ServiceCategoryAdapter(ServiceCategory serviceCategory) {
        super(new PlaylistPresenter());
        setServiceCategory(serviceCategory);
    }

    public ServiceCategory getServiceCategory() {
        return mServiceCategory;
    }

    public void setServiceCategory(ServiceCategory serviceCategory) {
        clear();
        mServiceCategory = serviceCategory;
        syncPlaylistItems();
    }

    private void syncPlaylistItems() {
        for(ServicePlaylist servicePlaylist : mServiceCategory.getPlaylists()) {
            add(servicePlaylist);
        }
    }
}
