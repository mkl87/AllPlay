package eu.applabs.allplaylibrary.services.deezer;

import eu.applabs.allplaylibrary.data.ServiceLibrary;
import eu.applabs.allplaylibrary.services.ServiceType;

public class DeezerService extends ServiceLibrary {

    @Override
    public ServiceType getServiceType() {
        return ServiceType.DEEZER;
    }

}
