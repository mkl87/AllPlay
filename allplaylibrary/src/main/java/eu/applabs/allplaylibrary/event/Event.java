package eu.applabs.allplaylibrary.event;

public class Event {

    public enum EventType {
        MUSIC_CATALOG_UPDATE,
        SERVICE_LIBRARY_UPDATE,
        SERVICE_CATEGORY_UPDATE,
        SERVICE_PLAYLIST_UPDATE,
        SERVICE_CONNECTION_EVENT,
        PLAYER_EVENT
    }

    private EventType mEventType;

    public Event(EventType eventType) {
        mEventType = eventType;
    }

    public EventType getEventType() {
        return mEventType;
    }

    public void setEventType(EventType eventType) {
        mEventType = eventType;
    }
}
