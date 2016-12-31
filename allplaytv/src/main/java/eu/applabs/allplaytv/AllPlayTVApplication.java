package eu.applabs.allplaytv;

import android.app.Application;

import javax.inject.Singleton;

import dagger.Component;
import eu.applabs.allplaytv.adapter.PlaylistAdapter;

public class AllPlayTVApplication extends Application {

    @Singleton
    @Component(modules = BaseModule.class)
    public interface ApplicationComponent {
        void inject(AllPlayTVApplication allPlayTVApplication);
        void inject(PlaylistAdapter playlistAdapter);
    }

    private static ApplicationComponent mApplicationComponent;

    @Override
    public void onCreate() {
        super.onCreate();

        mApplicationComponent = DaggerAllPlayTVApplication_ApplicationComponent.builder()
                .baseModule(new BaseModule(this))
                .build();

        component().inject(this);
    }

    public static ApplicationComponent component() {
        return mApplicationComponent;
    }

}
