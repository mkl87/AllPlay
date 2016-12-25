package eu.applabs.allplaylibrary.data;

import java.util.List;

public interface ServiceCategory {

    interface OnCategoryUpdateListener {
        void onCategoryUpdate();
    }

    void clearCategory();
    String getCategoryName();
    void addPlaylist(ServicePlaylist playlist);
    void removePlaylist(ServicePlaylist playlist);
    List<ServicePlaylist> getPlaylists();

    void registerListener(OnCategoryUpdateListener listener);
    void unregisterListener(OnCategoryUpdateListener listener);
}
