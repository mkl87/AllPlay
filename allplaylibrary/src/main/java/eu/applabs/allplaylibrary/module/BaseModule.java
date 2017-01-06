package eu.applabs.allplaylibrary.module;

import android.app.Activity;
import android.content.Context;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import eu.applabs.allplaylibrary.AllPlayLibrary;
import eu.applabs.allplaylibrary.MusicCatalog;
import eu.applabs.allplaylibrary.Player;
import eu.applabs.allplaylibrary.Playlist;
import eu.applabs.allplaylibrary.data.SettingsManager;

@Module
public class BaseModule {

    private final AllPlayLibrary mAllPlayLibrary;

    public BaseModule(AllPlayLibrary allPlayLibrary) {
        mAllPlayLibrary = allPlayLibrary;
    }

    @Provides
    Activity provideActivity() {
        return mAllPlayLibrary.getActivity();
    }

    @Provides
    Context provideContext(Activity activity) {
        return activity;
    }

    @Provides
    @Singleton
    Player providePlayer() {
        return new Player();
    }

    @Provides
    @Singleton
    Playlist provideNowPlayingPlaylist() {
        return new Playlist();
    }

    @Provides
    @Singleton
    MusicCatalog provideMusicLibrary() {
        return new MusicCatalog();
    }

    @Provides
    @Singleton
    SettingsManager provideSettingsManager() {
        return new SettingsManager();
    }
}