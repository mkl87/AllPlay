package eu.applabs.allplaylibrary.event;

import eu.applabs.allplaylibrary.services.ServiceType;

public class ServiceConnectionEvent extends Event {

    public enum ServiceConnectionEventType {
        CONNECTED,
        DISCONNECTED,
        ERROR
    }

    private ServiceConnectionEventType mServiceConnectionEventType;
    private ServiceType mServiceType;

    public ServiceConnectionEvent(ServiceConnectionEventType serviceConnectionEventType, ServiceType serviceType) {
        super(EventType.SERVICE_CONNECTION_EVENT);

        mServiceConnectionEventType = serviceConnectionEventType;
        mServiceType = serviceType;
    }

    public ServiceConnectionEventType getServiceConnectionEventType() {
        return mServiceConnectionEventType;
    }

    public void setServiceConnectionEventType(ServiceConnectionEventType serviceConnectionEventType) {
        mServiceConnectionEventType = serviceConnectionEventType;
    }

    public ServiceType getServiceType() {
        return mServiceType;
    }

    public void setServiceType(ServiceType serviceType) {
        mServiceType = serviceType;
    }
}
