package eu.applabs.allplaylibrary.event;

import eu.applabs.allplaylibrary.services.ServicePlaylist;

public class PlaylistEvent extends Event {

    private ServicePlaylist mServicePlaylist;

    public PlaylistEvent(ServicePlaylist servicePlaylist) {
        super(EventType.PLAYLIST_EVENT);

        mServicePlaylist = servicePlaylist;
    }

    public ServicePlaylist getServicePlaylist() {
        return mServicePlaylist;
    }

}
