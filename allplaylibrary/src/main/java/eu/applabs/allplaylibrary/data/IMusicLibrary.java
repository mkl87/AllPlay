package eu.applabs.allplaylibrary.data;

import java.util.List;

public interface IMusicLibrary {
    void clearLibrary();
    void addCategory(IMusicLibraryCategory category);
    void removeCategory(IMusicLibraryCategory category);
    List<IMusicLibraryCategory> getCategories();
    List<IMusicLibraryCategory> search(String query);
}
