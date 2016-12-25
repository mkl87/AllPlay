package eu.applabs.allplaylibrary.data;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import java.util.HashSet;
import java.util.Set;

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

    public void setConnectedServices(Set<String> connectedServices) {
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putStringSet(KEY_CONNECTED_SERVICES, connectedServices);
        editor.apply();
    }

    // Getter

    public Set<String> getConnectedServices() {
        return mSharedPreferences.getStringSet(KEY_CONNECTED_SERVICES, new HashSet<String>());
    }
}
