package eu.applabs.allplaylibrary.services;

import java.util.ArrayList;
import java.util.List;

import eu.applabs.allplaylibrary.data.Observable;
import eu.applabs.allplaylibrary.data.Song;

public abstract class ServicePlaylist extends Observable {

    protected List<Song> mSongList = new ArrayList<>();

    public abstract String getPlaylistName();
    public abstract String getCoverUrl();

    public void clearPlaylist() {
        mSongList.clear();
    }

    public int getSize() {
        return mSongList.size();
    }

    public List<Song> getPlaylist() {
        return mSongList;
    }

}
