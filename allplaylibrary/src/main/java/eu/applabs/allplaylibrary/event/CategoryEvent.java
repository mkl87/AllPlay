package eu.applabs.allplaylibrary.event;

import eu.applabs.allplaylibrary.services.ServiceCategory;

public class CategoryEvent extends Event {

    private ServiceCategory mServiceCategory;

    public CategoryEvent(ServiceCategory serviceCategory) {
        super(EventType.CATEGORY_EVENT);

        mServiceCategory = serviceCategory;
    }

    public ServiceCategory getServiceCategory() {
        return mServiceCategory;
    }

}
