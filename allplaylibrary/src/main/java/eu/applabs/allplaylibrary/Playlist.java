package eu.applabs.allplaylibrary;

import android.content.Context;
import android.support.annotation.NonNull;

import java.util.List;

import javax.inject.Inject;

import eu.applabs.allplaylibrary.data.Observable;
import eu.applabs.allplaylibrary.event.PlaylistEvent;
import eu.applabs.allplaylibrary.services.ServiceLibrary;
import eu.applabs.allplaylibrary.services.ServiceCategory;
import eu.applabs.allplaylibrary.services.ServicePlaylist;
import eu.applabs.allplaylibrary.data.Song;
import eu.applabs.allplaylibrary.services.ServiceType;

public class Playlist extends Observable {

    @Inject
    protected Context mContext;

    @Inject
    protected MusicCatalog mMusicCatalog;

    private ServicePlaylist mCurrentServicePlaylist = null;
    private int mCurrentSongIndex = 0;

    private NowPlayingServiceLibrary mNowPlayingServiceLibrary;

    public Playlist() {
        AllPlayLibrary.getInstance().component().inject(this);
        mNowPlayingServiceLibrary = new NowPlayingServiceLibrary();
    }

    void setServicePlaylist(ServicePlaylist servicePlaylist) {
        mCurrentServicePlaylist = servicePlaylist;
        mNowPlayingServiceLibrary.syncServicePlaylist();
        mCurrentSongIndex = 0;

        if(!mMusicCatalog.containsLibrary(mNowPlayingServiceLibrary)) {
            mMusicCatalog.addServiceLibrary(mNowPlayingServiceLibrary);
        }

        notifyPlaylistUpdate();
    }

    void remove(@NonNull Song song) {
        if(mCurrentServicePlaylist != null) {
            int objectRemovedAtIndex = mCurrentServicePlaylist.getPlaylist().indexOf(song);

            // Check if the song was found and remove it
            if (objectRemovedAtIndex != -1) {
                mCurrentServicePlaylist.getPlaylist().remove(song);

                if (objectRemovedAtIndex <= mCurrentSongIndex) {
                    mCurrentSongIndex--;
                }

                notifyPlaylistUpdate();
            }
        }
    }

    void clear() {
        mCurrentSongIndex = 0;
        mCurrentServicePlaylist = null;
        notifyPlaylistUpdate();
    }

    public ServicePlaylist getServicePlaylist() {
        return mCurrentServicePlaylist;
    }

    public List<Song> getPlaylist() {
        if(mCurrentServicePlaylist != null) {
            return mCurrentServicePlaylist.getPlaylist();
        }

        return null;
    }

    public int getCurrentSongIndex() {
        return mCurrentSongIndex;
    }

    public Song getCurrentSong() {
        if(mCurrentSongIndex >= 0
                && mCurrentServicePlaylist != null
                && mCurrentSongIndex < mCurrentServicePlaylist.getPlaylist().size()) {

            return mCurrentServicePlaylist.getPlaylist().get(mCurrentSongIndex);
        }

        return null;
    }

    Song getPrevSong() {
        if(mCurrentServicePlaylist != null && mCurrentSongIndex > 0) {
            mCurrentSongIndex--;
            notifyPlaylistUpdate();

            return mCurrentServicePlaylist.getPlaylist().get(mCurrentSongIndex);
        }

        return null;
    }

    Song getNextSong() {
        if(mCurrentServicePlaylist != null && mCurrentServicePlaylist.getPlaylist().size() > mCurrentSongIndex + 1) {
            mCurrentSongIndex++;
            notifyPlaylistUpdate();

            return mCurrentServicePlaylist.getPlaylist().get(mCurrentSongIndex);
        }

        return null;
    }

    private void notifyPlaylistUpdate() {
        PlaylistEvent playlistEvent = new PlaylistEvent(mCurrentServicePlaylist);
        notifyObservers(playlistEvent);
    }

    private class NowPlayingServiceLibrary extends ServiceLibrary {

        private ServiceCategory mServiceCategory;

        NowPlayingServiceLibrary() {
            mServiceCategory = new ServiceCategory(mContext.getString(R.string.category_currentplayback), getServiceType());
            this.addCategory(mServiceCategory);
        }

        @Override
        public ServiceType getServiceType() {
            return ServiceType.UNDEFINED;
        }

        private void syncServicePlaylist() {
            mServiceCategory.clearCategory();
            mServiceCategory.addPlaylist(mCurrentServicePlaylist);
        }

    }

}
