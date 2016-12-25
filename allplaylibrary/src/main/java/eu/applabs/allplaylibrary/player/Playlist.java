package eu.applabs.allplaylibrary.player;

import android.app.Activity;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import eu.applabs.allplaylibrary.R;
import eu.applabs.allplaylibrary.data.IMusicLibrary;
import eu.applabs.allplaylibrary.data.IMusicLibraryCategory;
import eu.applabs.allplaylibrary.data.IMusicLibraryCategoryUpdateListener;
import eu.applabs.allplaylibrary.data.IMusicLibraryPlaylist;
import eu.applabs.allplaylibrary.data.IMusicLibraryPlaylistUpdateListener;
import eu.applabs.allplaylibrary.data.MusicLibrary;
import eu.applabs.allplaylibrary.data.Song;

public class Playlist implements IMusicLibrary, IMusicLibraryCategory, IMusicLibraryPlaylist {
    private List<Song> m_PlayList = null;
    private int m_CurrentIndex = 0;

    private List<IPlaylistListener> m_IPlaylistListenerList = null;

    private Activity m_Activity = null;

    private MusicLibrary m_MusicLibrary = null;
    private List<IMusicLibraryCategoryUpdateListener> m_IMusicLibraryCategoryUpdateListenerList = null;

    public Playlist(Activity activity) {
        m_IMusicLibraryCategoryUpdateListenerList = new ArrayList<>();
        m_Activity = activity;
        m_CurrentIndex = 0;
        m_PlayList = new CopyOnWriteArrayList<>();

        m_MusicLibrary = MusicLibrary.getInstance();
        m_IPlaylistListenerList = new ArrayList<>();
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
        registerListener((IMusicLibraryCategoryUpdateListener) m_MusicLibrary);
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
        unregisterListener((IMusicLibraryCategoryUpdateListener) m_MusicLibrary);
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

    public void registerListener(IPlaylistListener listener) {
        m_IPlaylistListenerList.add(listener);
    }

    public void unregisterListener(IPlaylistListener listener) {
        m_IPlaylistListenerList.remove(listener);
    }

    private void notifyListener() {
        for(IPlaylistListener listener : m_IPlaylistListenerList) {
            listener.onPlaylistUpdate();
        }

        for(IMusicLibraryCategoryUpdateListener listener : m_IMusicLibraryCategoryUpdateListenerList) {
            listener.onMusicLibraryCategoryUpdate();
        }
    }

    // Library methods to get displayed in the list

    @Override
    public void clearLibrary() {

    }

    @Override
    public void addCategory(IMusicLibraryCategory category) {

    }

    @Override
    public void removeCategory(IMusicLibraryCategory category) {

    }

    @Override
    public List<IMusicLibraryCategory> getCategories() {
        List<IMusicLibraryCategory> list = new CopyOnWriteArrayList<>();
        list.add(this);

        return list;
    }

    @Override
    public List<IMusicLibraryCategory> search(String query) {
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
    public void addPlaylist(IMusicLibraryPlaylist playlist) {

    }

    @Override
    public void removePlaylist(IMusicLibraryPlaylist playlist) {

    }

    @Override
    public List<IMusicLibraryPlaylist> getPlaylists() {
        List<IMusicLibraryPlaylist> list = new CopyOnWriteArrayList<>();
        list.add(this);

        return list;
    }

    @Override
    public void registerListener(IMusicLibraryCategoryUpdateListener listener) {
        m_IMusicLibraryCategoryUpdateListenerList.add(listener);
    }

    @Override
    public void unregisterListener(IMusicLibraryCategoryUpdateListener listener) {
        m_IMusicLibraryCategoryUpdateListenerList.remove(listener);
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
    public void registerListener(IMusicLibraryPlaylistUpdateListener listener) {

    }

    @Override
    public void unregisterListener(IMusicLibraryPlaylistUpdateListener listener) {

    }
}
