package eu.applabs.allplaylibrary.services.deezer;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import eu.applabs.allplaylibrary.data.IMusicLibrary;
import eu.applabs.allplaylibrary.data.IMusicLibraryCategory;

public class DeezerMusicLibrary implements IMusicLibrary {

    private List<IMusicLibraryCategory> m_IMusicLibraryCategoryList = null;

    public DeezerMusicLibrary() {
        m_IMusicLibraryCategoryList = new CopyOnWriteArrayList<>();
    }

    @Override
    public void clearLibrary() {
        for(IMusicLibraryCategory category : m_IMusicLibraryCategoryList) {
            category.clearCategory();
        }

        m_IMusicLibraryCategoryList.clear();
    }

    @Override
    public void addCategory(IMusicLibraryCategory category) {
        m_IMusicLibraryCategoryList.add(category);
    }

    @Override
    public void removeCategory(IMusicLibraryCategory category) {
        m_IMusicLibraryCategoryList.remove(category);
    }

    @Override
    public List<IMusicLibraryCategory> getCategories() {
        return m_IMusicLibraryCategoryList;
    }

    @Override
    public List<IMusicLibraryCategory> search(String query) {
        return null;
    }
}
