package eu.applabs.allplaylibrary;

import android.app.Activity;

import javax.inject.Inject;
import javax.inject.Singleton;

import dagger.Component;
import eu.applabs.allplaylibrary.data.MusicLibrary;
import eu.applabs.allplaylibrary.data.ServiceCategory;
import eu.applabs.allplaylibrary.player.Player;
import eu.applabs.allplaylibrary.services.deezer.DeezerCategory;
import eu.applabs.allplaylibrary.services.deezer.DeezerPlayer;
import eu.applabs.allplaylibrary.services.deezer.DeezerPlaylist;
import eu.applabs.allplaylibrary.services.spotify.SpotifyCategory;
import eu.applabs.allplaylibrary.services.spotify.SpotifyPlayer;
import eu.applabs.allplaylibrary.services.spotify.SpotifyPlaylist;

public class AllPlayLibrary {

    @Singleton
    @Component(modules = BaseModule.class)
    public interface ApplicationComponent {
        void inject(AllPlayLibrary allPlayLibrary);
        void inject(ServiceCategory serviceCategory);

        void inject(SpotifyCategory spotifyCategory);
        void inject(SpotifyPlayer spotifyPlayer);
        void inject(SpotifyPlaylist spotifyPlaylist);

        void inject(DeezerCategory deezerCategory);
        void inject(DeezerPlayer deezerPlayer);
        void inject(DeezerPlaylist deezerPlaylist);
    }

    private static AllPlayLibrary mAllPlayLibrary;

    private ApplicationComponent mApplicationComponent;
    private Activity mActivity;

    @Inject
    MusicLibrary mMusicLibrary;

    @Inject
    Player mPlayer;

    // Private (Singelton)
    private AllPlayLibrary() {}

    public static synchronized AllPlayLibrary getInstance() {
        if(AllPlayLibrary.mAllPlayLibrary == null) {
            AllPlayLibrary.mAllPlayLibrary = new AllPlayLibrary();
        }

        return AllPlayLibrary.mAllPlayLibrary;
    }

    public void init(Activity activity){
        if(mActivity == null){
            mActivity = activity;

            mApplicationComponent = DaggerAllPlayLibrary_ApplicationComponent.builder()
                    .baseModule(new BaseModule(this))
                    .build();

            component().inject(this);
        }
    }

    public void deinit() {
        mActivity = null;
    }

    public ApplicationComponent component() {
        return mApplicationComponent;
    }

    public Activity getActivity() {
        return mActivity;
    }

    public Player getPlayer() {
        return mPlayer;
    }

    public MusicLibrary getMusicLibrary() {
        return mMusicLibrary;
    }
}
