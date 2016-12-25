package eu.applabs.allplaylibrary.services.deezer;

import com.deezer.sdk.model.Album;
import com.deezer.sdk.model.Playlist;
import com.deezer.sdk.model.Track;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import eu.applabs.allplaylibrary.data.ServicePlaylist;
import eu.applabs.allplaylibrary.data.MusicLibrary;
import eu.applabs.allplaylibrary.data.Song;
import eu.applabs.allplaylibrary.player.ServicePlayer;

public class DeezerPlaylist implements ServicePlaylist {

    private String m_Name = null;
    private String m_Cover = null;
    private List<Song> m_SongList = null;
    private MusicLibrary m_MusicLibrary = null;

    private List<OnPlaylistUpdateListener> mM_OnPlaylistUpdateListenerList = null;

    public DeezerPlaylist(String name) {
        m_Name = name;
        m_SongList = new CopyOnWriteArrayList<>();
        mM_OnPlaylistUpdateListenerList = new CopyOnWriteArrayList<>();

        m_MusicLibrary = MusicLibrary.getInstance();
        registerListener(m_MusicLibrary);
    }

    public void addSongs(Playlist playlist) {
        if(playlist != null) {
            m_Name = playlist.getTitle();
            m_Cover = playlist.getPictureUrl();

            for(Track t : playlist.getTracks()) {
                Song song = new Song();
                song.setTitle(t.getTitle());
                song.setAlbum(m_Name);
                song.setArtist(t.getArtist().getName());
                song.setCoverSmall(m_Cover);
                song.setId(String.valueOf(t.getId()));
                song.setServiceType(ServicePlayer.ServiceType.Deezer);
                song.setUri(t.getPreviewUrl());
                song.setDuration((long) t.getDuration());

                m_SongList.add(song);
            }

            notifyListener();
        }
    }

    public void addSongs(Album album) {
        if(album != null) {
            m_Name = album.getTitle();
            m_Cover = album.getCoverUrl();

            for(Track t : album.getTracks()) {
                Song song = new Song();
                song.setTitle(t.getTitle());
                song.setAlbum(m_Name);
                song.setArtist(t.getArtist().getName());
                song.setCoverSmall(m_Cover);
                song.setId(String.valueOf(t.getId()));
                song.setServiceType(ServicePlayer.ServiceType.Deezer);
                song.setUri(t.getPreviewUrl());
                song.setDuration((long) t.getDuration());

                m_SongList.add(song);
            }

            notifyListener();
        }
    }

    public void addSongs(List<Track> tracks) {
        if(tracks != null && tracks.size() > 0) {
            m_Cover = tracks.get(0).getAlbum().getCoverUrl(); // Maybe just show a star?!
        }

        for(Track t : tracks) {
            Song song = new Song();
            song.setTitle(t.getTitle());
            song.setAlbum(m_Name);
            song.setArtist(t.getArtist().getName());
            song.setCoverSmall(m_Cover);
            song.setId(String.valueOf(t.getId()));
            song.setServiceType(ServicePlayer.ServiceType.Deezer);
            song.setUri(t.getPreviewUrl());
            song.setDuration((long) t.getDuration());

            m_SongList.add(song);
        }

        notifyListener();
    }

    @Override
    public void clearPlaylist() {
        m_SongList.clear();
    }

    @Override
    public String getPlaylistName() {
        return m_Name;
    }

    @Override
    public String getCoverUrl() {
        return m_Cover;
    }

    @Override
    public int getSize() {
        if(m_SongList != null) {
            return m_SongList.size();
        }

        return 0;
    }

    @Override
    public List<Song> getPlaylist() {
        return m_SongList;
    }

    @Override
    public void registerListener(OnPlaylistUpdateListener listener) {
        mM_OnPlaylistUpdateListenerList.add(listener);
    }

    @Override
    public void unregisterListener(OnPlaylistUpdateListener listener) {
        mM_OnPlaylistUpdateListenerList.remove(listener);
    }

    private void notifyListener() {
        for(OnPlaylistUpdateListener listener : mM_OnPlaylistUpdateListenerList) {
            listener.onPlaylistUpdate();
        }
    }
}
