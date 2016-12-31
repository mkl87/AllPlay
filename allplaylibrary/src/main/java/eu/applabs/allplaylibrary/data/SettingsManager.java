package eu.applabs.allplaylibrary.data;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import eu.applabs.allplaylibrary.services.ServiceType;

public class SettingsManager {

    private static SettingsManager mSettingsManager;
    private static boolean mIsInitialized = false;

    private static final String TAG = SettingsManager.class.getSimpleName();
    private static final String KEY_CONNECTED_SERVICES = TAG + ".ConnectedServices";

    private Context mContext;
    private SharedPreferences mSharedPreferences;

    private SettingsManager() {}

    public static synchronized SettingsManager getInstance() {
        if(SettingsManager.mSettingsManager == null) {
            SettingsManager.mSettingsManager = new SettingsManager();
        }

        return SettingsManager.mSettingsManager;
    }

    public boolean initialize(Context context) {
        if(!SettingsManager.mIsInitialized && context != null) {
            mContext = context;
            mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
            SettingsManager.mIsInitialized = true;
        }

        return SettingsManager.mIsInitialized;
    }

    // Setter

    public void setConnectedServices(List<ServiceType> connectedServices) {
        Set<String> connectedServiceSet = new HashSet<>();

        if(connectedServices.contains(ServiceType.SPOTIFY)) {
            connectedServiceSet.add(String.valueOf(ServiceType.SPOTIFY.ordinal()));
        }

        if(connectedServices.contains(ServiceType.DEEZER)) {
            connectedServiceSet.add(String.valueOf(ServiceType.DEEZER.ordinal()));
        }

        if(connectedServices.contains(ServiceType.GOOGLE_MUSIC)) {
            connectedServiceSet.add(String.valueOf(ServiceType.GOOGLE_MUSIC.ordinal()));
        }

        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putStringSet(KEY_CONNECTED_SERVICES, connectedServiceSet);
        editor.apply();
    }

    // Getter

    public List<ServiceType> getConnectedServices() {
        Set<String> connectedServices = mSharedPreferences.getStringSet(KEY_CONNECTED_SERVICES, new HashSet<String>());
        List<ServiceType> returnValues = new ArrayList<>();

        if(connectedServices.contains(String.valueOf(ServiceType.SPOTIFY.ordinal()))) {
            returnValues.add(ServiceType.SPOTIFY);
        }

        if(connectedServices.contains(String.valueOf(ServiceType.DEEZER.ordinal()))) {
            returnValues.add(ServiceType.DEEZER);
        }

        if(connectedServices.contains(String.valueOf(ServiceType.GOOGLE_MUSIC.ordinal()))) {
            returnValues.add(ServiceType.GOOGLE_MUSIC);
        }

        return returnValues;
    }
}
