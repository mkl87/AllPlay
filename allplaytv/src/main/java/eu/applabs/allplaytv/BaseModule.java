package eu.applabs.allplaytv;

import android.content.Context;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module
public class BaseModule {

    private final AllPlayTVApplication mAllPlayTVApplication;

    public BaseModule(AllPlayTVApplication allPlayTVApplication) {
        mAllPlayTVApplication = allPlayTVApplication;
    }

    @Provides
    @Singleton
    Context provideContext() {
        return mAllPlayTVApplication;
    }
}
