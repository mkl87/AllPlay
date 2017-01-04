package eu.applabs.allplaylibrary.services;

import java.util.ArrayList;
import java.util.List;

import eu.applabs.allplaylibrary.services.ServiceCategory;
import eu.applabs.allplaylibrary.services.ServiceType;

public abstract class ServiceLibrary {

    private List<ServiceCategory> mServiceCategoryList = new ArrayList<>();

    public interface OnServiceLibrarySearchResult {
        void onSearchResult(List<ServiceCategory> list);
    }

    /**
     * Abstract method to specify the type of the service
     *
     * @return ServiceType (Example: ServiceType.SPOTIFY)
     */
    public abstract ServiceType getServiceType();

    public void clearLibrary() {
        for(ServiceCategory category : mServiceCategoryList) {
            category.clearCategory();
        }

        mServiceCategoryList.clear();
    }

    public void addCategory(ServiceCategory category) {
        if(!mServiceCategoryList.contains(category)) {
            mServiceCategoryList.add(category);
        }
    }

    public void removeCategory(ServiceCategory category) {
        if(mServiceCategoryList.contains(category)) {
            mServiceCategoryList.remove(category);
        }
    }

    public List<ServiceCategory> getCategories() {
        return mServiceCategoryList;
    }

    public List<ServiceCategory> search(String query) {
        List<ServiceCategory> temp = new ArrayList<>();

        for(ServiceCategory category : mServiceCategoryList) {
            if(category.getCategoryName().contains(query)) {
                temp.add(category);
            }
        }

        return temp;
    }
}
