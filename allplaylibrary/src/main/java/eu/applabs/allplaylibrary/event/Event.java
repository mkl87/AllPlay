package eu.applabs.allplaylibrary.event;

public class Event {

    public enum EventType {
        MUSIC_CATALOG_EVENT,
        SERVICE_LIBRARY_EVENT,
        CATEGORY_EVENT,
        PLAYLIST_EVENT,
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
