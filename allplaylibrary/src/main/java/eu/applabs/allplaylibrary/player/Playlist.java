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

    private Activity mActivity;
    private List<Song> mSongList = new CopyOnWriteArrayList<>();
    private int mCurrentIndex = 0;

    private MusicLibrary mMusicLibrary = MusicLibrary.getInstance();
    private List<OnPlaylistUpdateListener> mOnPlaylistUpdateListenerList = new ArrayList<>();
    private List<OnCategoryUpdateListener> mOnCategoryUpdateListenerList = new ArrayList<>();

    public Playlist(Activity activity) {
        mActivity = activity;
    }

    public void setPlaylist(List<Song> list) {
        if(list != null && list.size() > 0) {
            clear();

            for (Song song : list) {
                if(song != null) {
                    mSongList.add(song);
                }
            }
        }

        mMusicLibrary.addMusicLibrary(this);
        registerListener((OnCategoryUpdateListener) mMusicLibrary);
        notifyListener();
    }

    public void remove(Song song) {
        if(mSongList != null) {
            for(int i = 0; i < mSongList.size(); ++i) {
                if(mSongList.get(i) == song) {
                    if(i <= mCurrentIndex) {
                        mCurrentIndex--;
                    }

                    mSongList.remove(song);
                    break;
                }
            }
        }

        notifyListener();
    }

    public void clear() {
        if(mSongList != null) {
            mCurrentIndex = 0;
            mSongList.clear();
        }

        mMusicLibrary.removeMusicLibrary(this);
        unregisterListener((OnCategoryUpdateListener) mMusicLibrary);
        notifyListener();
    }

    public List<Song> getPlaylistAsSongList() {
        return mSongList;
    }

    public int getCurrentSongIndex() {
        return mCurrentIndex;
    }

    public Song getCurrentSong() {
        if(mSongList != null && mCurrentIndex >= 0 && mCurrentIndex < mSongList.size()) {
            return mSongList.get(mCurrentIndex);
        }

        return null;
    }

    public Song getPrevSong() {
        if(mSongList != null && mCurrentIndex > 0) {
            mCurrentIndex--;
            notifyListener();

            return mSongList.get(mCurrentIndex);
        }

        return null;
    }

    public Song getNextSong() {
        if(mSongList != null && mSongList.size() > mCurrentIndex + 1) {
            mCurrentIndex++;
            notifyListener();

            return mSongList.get(mCurrentIndex);
        }

        return null;
    }

    public void registerListener(OnPlaylistUpdateListener listener) {
        mOnPlaylistUpdateListenerList.add(listener);
    }

    public void unregisterListener(OnPlaylistUpdateListener listener) {
        mOnPlaylistUpdateListenerList.remove(listener);
    }

    private void notifyListener() {
        for(OnPlaylistUpdateListener listener : mOnPlaylistUpdateListenerList) {
            listener.onPlaylistUpdate();
        }

        for(OnCategoryUpdateListener listener : mOnCategoryUpdateListenerList) {
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
        return mActivity.getResources().getString(R.string.category_currentplayback);
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
        mOnCategoryUpdateListenerList.add(listener);
    }

    @Override
    public void unregisterListener(OnCategoryUpdateListener listener) {
        mOnCategoryUpdateListenerList.remove(listener);
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
        if(mSongList != null) {
            return mSongList.size();
        }

        return 0;
    }

    @Override
    public List<Song> getPlaylist() {
        return mSongList;
    }

    @Override
    public void registerListener(ServicePlaylist.OnPlaylistUpdateListener listener) {

    }

    @Override
    public void unregisterListener(ServicePlaylist.OnPlaylistUpdateListener listener) {

    }
}
