package eu.applabs.allplaylibrary.player;

import android.app.Activity;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import eu.applabs.allplaylibrary.R;
import eu.applabs.allplaylibrary.data.ServiceLibrary;
import eu.applabs.allplaylibrary.data.ServiceCategory;
import eu.applabs.allplaylibrary.data.ServicePlaylist;
import eu.applabs.allplaylibrary.data.MusicLibrary;
import eu.applabs.allplaylibrary.data.Song;

public class Playlist implements ServiceLibrary, ServiceCategory, ServicePlaylist {

    public interface OnPlaylistUpdateListener {
        void onPlaylistUpdate();
    }

    private List<Song> m_PlayList = null;
    private int m_CurrentIndex = 0;

    private List<OnPlaylistUpdateListener> mM_OnPlaylistUpdateListenerList = null;

    private Activity m_Activity = null;

    private MusicLibrary m_MusicLibrary = null;
    private List<OnCategoryUpdateListener> mM_OnCategoryUpdateListenerList = null;

    public Playlist(Activity activity) {
        mM_OnCategoryUpdateListenerList = new ArrayList<>();
        m_Activity = activity;
        m_CurrentIndex = 0;
        m_PlayList = new CopyOnWriteArrayList<>();

        m_MusicLibrary = MusicLibrary.getInstance();
        mM_OnPlaylistUpdateListenerList = new ArrayList<>();
    }

    public void setPlaylist(List<Song> list) {
        if(list != null && list.size() > 0) {
            clear();

            for (Song song : list) {
                if(song != null) {
                    m_PlayList.add(song);
                }
            }
        }

        m_MusicLibrary.addMusicLibrary(this);
        registerListener((OnCategoryUpdateListener) m_MusicLibrary);
        notifyListener();
    }

    public void remove(Song song) {
        if(m_PlayList != null) {
            for(int i = 0; i < m_PlayList.size(); ++i) {
                if(m_PlayList.get(i) == song) {
                    if(i <= m_CurrentIndex) {
                        m_CurrentIndex--;
                    }

                    m_PlayList.remove(song);
                    break;
                }
            }
        }

        notifyListener();
    }

    public void clear() {
        if(m_PlayList != null) {
            m_CurrentIndex = 0;
            m_PlayList.clear();
        }

        m_MusicLibrary.removeMusicLibrary(this);
        unregisterListener((OnCategoryUpdateListener) m_MusicLibrary);
        notifyListener();
    }

    public List<Song> getPlaylistAsSongList() {
        return m_PlayList;
    }

    public int getCurrentSongIndex() {
        return m_CurrentIndex;
    }

    public Song getCurrentSong() {
        if(m_PlayList != null && m_CurrentIndex >= 0 && m_CurrentIndex < m_PlayList.size()) {
            return m_PlayList.get(m_CurrentIndex);
        }

        return null;
    }

    public Song getPrevSong() {
        if(m_PlayList != null && m_CurrentIndex > 0) {
            m_CurrentIndex--;
            notifyListener();

            return m_PlayList.get(m_CurrentIndex);
        }

        return null;
    }

    public Song getNextSong() {
        if(m_PlayList != null && m_PlayList.size() > m_CurrentIndex + 1) {
            m_CurrentIndex++;
            notifyListener();

            return m_PlayList.get(m_CurrentIndex);
        }

        return null;
    }

    public void registerListener(OnPlaylistUpdateListener listener) {
        mM_OnPlaylistUpdateListenerList.add(listener);
    }

    public void unregisterListener(OnPlaylistUpdateListener listener) {
        mM_OnPlaylistUpdateListenerList.remove(listener);
    }

    private void notifyListener() {
        for(OnPlaylistUpdateListener listener : mM_OnPlaylistUpdateListenerList) {
            listener.onPlaylistUpdate();
        }

        for(OnCategoryUpdateListener listener : mM_OnCategoryUpdateListenerList) {
            listener.onCategoryUpdate();
        }
    }

    // Library methods to get displayed in the list

    @Override
    public void clearLibrary() {

    }

    @Override
    public void addCategory(ServiceCategory category) {

    }

    @Override
    public void removeCategory(ServiceCategory category) {

    }

    @Override
    public List<ServiceCategory> getCategories() {
        List<ServiceCategory> list = new CopyOnWriteArrayList<>();
        list.add(this);

        return list;
    }

    @Override
    public List<ServiceCategory> search(String query) {
        return null;
    }

    // Category methods to get displayed in the list

    @Override
    public void clearCategory() {

    }

    @Override
    public String getCategoryName() {
        return m_Activity.getResources().getString(R.string.category_currentplayback);
    }

    @Override
    public void addPlaylist(ServicePlaylist playlist) {

    }

    @Override
    public void removePlaylist(ServicePlaylist playlist) {

    }

    @Override
    public List<ServicePlaylist> getPlaylists() {
        List<ServicePlaylist> list = new CopyOnWriteArrayList<>();
        list.add(this);

        return list;
    }

    @Override
    public void registerListener(OnCategoryUpdateListener listener) {
        mM_OnCategoryUpdateListenerList.add(listener);
    }

    @Override
    public void unregisterListener(OnCategoryUpdateListener listener) {
        mM_OnCategoryUpdateListenerList.remove(listener);
    }

    // Playlist methods

    @Override
    public void clearPlaylist() {

    }

    @Override
    public String getPlaylistName() {
        Song song = getCurrentSong();

        if(song != null) {
            return song.getTitle();
        }

        return "";
    }

    @Override
    public String getCoverUrl() {
        Song song = getCurrentSong();

        if(song != null) {
            return song.getCoverSmall();
        }

        return "";
    }

    @Override
    public int getSize() {
        if(m_PlayList != null) {
            return m_PlayList.size();
        }

        return 0;
    }

    @Override
    public List<Song> getPlaylist() {
        return m_PlayList;
    }

    @Override
    public void registerListener(ServicePlaylist.OnPlaylistUpdateListener listener) {

    }

    @Override
    public void unregisterListener(ServicePlaylist.OnPlaylistUpdateListener listener) {

    }
}
