package eu.applabs.allplaylibrary;

import android.app.Activity;
import android.content.Context;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import eu.applabs.allplaylibrary.data.MusicCatalog;
import eu.applabs.allplaylibrary.data.SettingsManager;
import eu.applabs.allplaylibrary.player.NowPlayingPlaylist;
import eu.applabs.allplaylibrary.player.Player;

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
    NowPlayingPlaylist provideNowPlayingPlaylist() {
        return new NowPlayingPlaylist();
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