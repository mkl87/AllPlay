package eu.applabs.allplaylibrary.services.deezer;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import eu.applabs.allplaylibrary.data.ServiceLibrary;
import eu.applabs.allplaylibrary.data.ServiceCategory;

public class DeezerService implements ServiceLibrary {

    private List<ServiceCategory> mServiceCategoryList = new CopyOnWriteArrayList<>();;

    public DeezerService() {

    }

    @Override
    public void clearLibrary() {
        for(ServiceCategory category : mServiceCategoryList) {
            category.clearCategory();
        }

        mServiceCategoryList.clear();
    }

    @Override
    public void addCategory(ServiceCategory category) {
        mServiceCategoryList.add(category);
    }

    @Override
    public void removeCategory(ServiceCategory category) {
        mServiceCategoryList.remove(category);
    }

    @Override
    public List<ServiceCategory> getCategories() {
        return mServiceCategoryList;
    }

    @Override
    public List<ServiceCategory> search(String query) {
        return null;
    }
}
