package eu.applabs.allplaylibrary.data;

import java.util.List;

public interface ServiceLibrary {

    interface OnServiceLibrarySearchResult {
        void onSearchResult(List<ServiceCategory> list);
    }

    void clearLibrary();
    void addCategory(ServiceCategory category);
    void removeCategory(ServiceCategory category);
    List<ServiceCategory> getCategories();
    List<ServiceCategory> search(String query);
}
