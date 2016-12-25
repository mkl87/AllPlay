package eu.applabs.allplaylibrary.data;

import java.util.List;

public interface IMusicLibrarySearchResultCallback {
    void onResult(List<IMusicLibraryCategory> list);
}
