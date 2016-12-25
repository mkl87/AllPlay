package eu.applabs.allplaylibrary.services.deezer;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import eu.applabs.allplaylibrary.data.ServiceLibrary;
import eu.applabs.allplaylibrary.data.ServiceCategory;

public class DeezerService implements ServiceLibrary {

    private List<ServiceCategory> mM_ServiceCategoryList = null;

    public DeezerService() {
        mM_ServiceCategoryList = new CopyOnWriteArrayList<>();
    }

    @Override
    public void clearLibrary() {
        for(ServiceCategory category : mM_ServiceCategoryList) {
            category.clearCategory();
        }

        mM_ServiceCategoryList.clear();
    }

    @Override
    public void addCategory(ServiceCategory category) {
        mM_ServiceCategoryList.add(category);
    }

    @Override
    public void removeCategory(ServiceCategory category) {
        mM_ServiceCategoryList.remove(category);
    }

    @Override
    public List<ServiceCategory> getCategories() {
        return mM_ServiceCategoryList;
    }

    @Override
    public List<ServiceCategory> search(String query) {
        return null;
    }
}
