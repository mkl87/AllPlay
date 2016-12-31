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

public class NowPlayingPlaylist extends ServicePlaylist {

    private Activity mActivity;
    private int mCurrentIndex = 0;

    private ServiceLibrary mNowPlayingServiceLibrary = new ServiceLibrary();
    private NowPlayingCategory mNowPlayingCategory = new NowPlayingCategory();
    private MusicLibrary mMusicLibrary;

    public NowPlayingPlaylist(Activity activity, MusicLibrary musicLibrary) {
        mActivity = activity;
        mMusicLibrary = musicLibrary;

        mNowPlayingServiceLibrary.addCategory(mNowPlayingCategory);
        mMusicLibrary.addMusicLibrary(mNowPlayingServiceLibrary);
    }

    public void setPlaylist(List<Song> list) {
        mNowPlayingCategory.addPlaylist(this);

        if(list != null && list.size() > 0) {
            clear();

            for (Song song : list) {
                if(song != null) {
                    mSongList.add(song);
                }
            }
        }

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
        mNowPlayingCategory.clearCategory();

        if(mSongList != null) {
            mCurrentIndex = 0;
            mSongList.clear();
        }

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

    private class NowPlayingCategory extends ServiceCategory {

        @Override
        public String getCategoryName() {
            return mActivity.getString(R.string.category_currentplayback);
        }

    }
}
