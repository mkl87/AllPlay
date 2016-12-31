package eu.applabs.allplaylibrary.services.gmusic;

import eu.applabs.allplaylibrary.data.ServiceLibrary;
import eu.applabs.allplaylibrary.services.ServiceType;

public class GMusicService extends ServiceLibrary {

    @Override
    public ServiceType getServiceType() {
        return ServiceType.GOOGLE_MUSIC;
    }

}
