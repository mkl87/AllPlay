package eu.applabs.allplaylibrary.data;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import java.util.HashSet;
import java.util.Set;

public class SettingsManager {
    private static SettingsManager s_Instance = null;
    private static boolean s_Initialized = false;

    private static final String s_Classname = SettingsManager.class.getSimpleName();
    private static final String s_ConnectedServices = s_Classname + ".ConnectedServices";

    private Context m_Context = null;
    private SharedPreferences m_SharedPreferences = null;
    private SharedPreferences.Editor m_Editor = null;

    private SettingsManager() {}

    public static synchronized SettingsManager getInstance() {
        if(SettingsManager.s_Instance == null) {
            SettingsManager.s_Instance = new SettingsManager();
        }

        return SettingsManager.s_Instance;
    }

    public boolean initialize(Context context) {
        if(!SettingsManager.s_Initialized && context != null) {
            m_Context = context;
            m_SharedPreferences = PreferenceManager.getDefaultSharedPreferences(m_Context);
            m_Editor = m_SharedPreferences.edit();
            SettingsManager.s_Initialized = true;
        }

        return SettingsManager.s_Initialized;
    }

    // Setter

    public void setConnectedServices(Set<String> connectedServices) {
        m_Editor.putStringSet(s_ConnectedServices, connectedServices);
        m_Editor.apply();
    }

    // Getter

    public Set<String> getConnectedServices() {
        return m_SharedPreferences.getStringSet(s_ConnectedServices, new HashSet<String>());
    }
}
