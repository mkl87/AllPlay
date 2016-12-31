package eu.applabs.allplaylibrary;

import android.app.Activity;
import android.content.Context;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import eu.applabs.allplaylibrary.data.MusicLibrary;
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
    @Singleton
    Player providePlayer(MusicLibrary musicLibrary) {
        return new Player(mAllPlayLibrary.getActivity(), musicLibrary);
    }

    @Provides
    @Singleton
    MusicLibrary provideMusicLibrary() {
        return new MusicLibrary();
    }
}