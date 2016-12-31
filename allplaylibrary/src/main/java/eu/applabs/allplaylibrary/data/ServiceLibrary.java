package eu.applabs.allplaylibrary.data;

import java.util.ArrayList;
import java.util.List;

public class ServiceLibrary {

    private List<ServiceCategory> mServiceCategoryList = new ArrayList<>();

    public interface OnServiceLibrarySearchResult {
        void onSearchResult(List<ServiceCategory> list);
    }

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
