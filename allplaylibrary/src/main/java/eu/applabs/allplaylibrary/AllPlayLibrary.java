package eu.applabs.allplaylibrary;

import android.app.Activity;
import android.content.Intent;

import java.util.*;

import javax.inject.Inject;
import javax.inject.Singleton;

import dagger.Component;
import eu.applabs.allplaylibrary.module.BaseModule;
import eu.applabs.allplaylibrary.services.ServiceCategory;
import eu.applabs.allplaylibrary.data.SettingsManager;
import eu.applabs.allplaylibrary.services.ServiceType;
import eu.applabs.allplaylibrary.services.deezer.DeezerPlayer;
import eu.applabs.allplaylibrary.services.deezer.DeezerPlaylist;
import eu.applabs.allplaylibrary.services.spotify.SpotifyPlayer;
import eu.applabs.allplaylibrary.services.spotify.SpotifyPlaylist;

public class AllPlayLibrary {

    @Singleton
    @Component(modules = BaseModule.class)
    public interface ApplicationComponent {
        void inject(AllPlayLibrary allPlayLibrary);
        void inject(Playlist playlist);
        void inject(Player player);
        void inject(ServiceCategory serviceCategory);
        void inject(SettingsManager settingsManager);

        void inject(SpotifyPlayer spotifyPlayer);
        void inject(SpotifyPlaylist spotifyPlaylist);

        void inject(DeezerPlayer deezerPlayer);
        void inject(DeezerPlaylist deezerPlaylist);
    }

    private static AllPlayLibrary mAllPlayLibrary;

    private ApplicationComponent mApplicationComponent;
    private Activity mActivity;

    @Inject
    MusicCatalog mMusicCatalog;

    @Inject
    Player mPlayer;

    @Inject
    SettingsManager mSettingsManager;

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

    public MusicCatalog getMusicLibrary() {
        return mMusicCatalog;
    }

    public List<ServiceType> getConnectedServiceTypes() {
        return mSettingsManager.getConnectedServiceTypes();
    }

    public boolean connectServiceType(Activity activity, ServiceType serviceType) {
        return mPlayer.connectServiceType(activity, serviceType);
    }

    public boolean disconnectServiceType(ServiceType serviceType) {
        return mPlayer.disconnectServiceType(serviceType);
    }

    public boolean checkActivityResult(int requestCode, int resultCode, Intent intent) {
        return mPlayer.checkActivityResult(requestCode, resultCode, intent);
    }
}
