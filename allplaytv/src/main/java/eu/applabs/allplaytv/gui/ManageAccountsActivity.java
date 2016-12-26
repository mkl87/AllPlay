package eu.applabs.allplaytv.gui;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.FragmentManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v17.leanback.app.BackgroundManager;
import android.support.v17.leanback.app.BrowseFragment;
import android.support.v17.leanback.widget.ArrayObjectAdapter;
import android.support.v17.leanback.widget.HeaderItem;
import android.support.v17.leanback.widget.ListRow;
import android.support.v17.leanback.widget.ListRowPresenter;
import android.support.v17.leanback.widget.OnItemViewClickedListener;
import android.support.v17.leanback.widget.Presenter;
import android.support.v17.leanback.widget.Row;
import android.support.v17.leanback.widget.RowPresenter;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import eu.applabs.allplaylibrary.data.SettingsManager;
import eu.applabs.allplaylibrary.player.PlayerListener;
import eu.applabs.allplaylibrary.player.ServicePlayer;
import eu.applabs.allplaylibrary.player.Player;
import eu.applabs.allplaytv.R;
import eu.applabs.allplaytv.presenter.ServicePresenter;

public class ManageAccountsActivity extends Activity implements OnItemViewClickedListener, PlayerListener {

    private FragmentManager mFragmentManager;
    private BrowseFragment mBrowseFragment;
    private SettingsManager mSettingsManager;
    private ArrayObjectAdapter mManageAccountAdapter;

    private Player m_Player = null;

    private List<ServicePlayer.ServiceType> m_ConnectedServices = null;
    private List<ServicePlayer.ServiceType> m_AvailableServices = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manageaccounts);

        m_ConnectedServices = new ArrayList<>();
        m_AvailableServices = new ArrayList<>();

        mSettingsManager = SettingsManager.getInstance();
        mSettingsManager.initialize(this);

        mFragmentManager = getFragmentManager();
        mBrowseFragment = (BrowseFragment) mFragmentManager.findFragmentById(R.id.id_frag_ManageAccounts);

        mBrowseFragment.setHeadersState(BrowseFragment.HEADERS_ENABLED);
        mBrowseFragment.setTitle("AllPlay");
        mBrowseFragment.setOnItemViewClickedListener(this);

        BackgroundManager backgroundManager = BackgroundManager.getInstance(this);
        backgroundManager.attach(this.getWindow());
        backgroundManager.setDrawable(getResources().getDrawable(R.drawable.background, null));

        mManageAccountAdapter = new ArrayObjectAdapter(new ListRowPresenter());
        mBrowseFragment.setAdapter(mManageAccountAdapter);

        m_Player = Player.getInstance();
        m_Player.registerListener(this);

        updateUI();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        m_Player.unregisterListener(this);

        mFragmentManager = null;
        mBrowseFragment = null;
        mManageAccountAdapter = null;

        Glide.get(this).clearMemory();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(!m_Player.checkActivityResult(requestCode, resultCode, data)) {
            // Seems to be a result for another request
        }
    }

    public void updateUI() {
        runOnUiThread(
            new Runnable() {
                @Override
                public void run() {
                    mManageAccountAdapter.clear();
                    m_ConnectedServices.clear();
                    m_AvailableServices.clear();

                    // Connected services

                    ArrayObjectAdapter connectedServiceAdapter = new ArrayObjectAdapter(new ServicePresenter());
                    Set<String> connectedServices = mSettingsManager.getConnectedServices();
                    for(String s : mSettingsManager.getConnectedServices()) {
                        ServicePlayer.ServiceType type = ServicePlayer.ServiceType.values()[Integer.valueOf(s)];
                        connectedServiceAdapter.add(type);
                        m_ConnectedServices.add(type);
                    }

                    HeaderItem connectedServicesHeader = new HeaderItem(getResources().getString(R.string.manageaccountsactivity_category_connectedservices));
                    mManageAccountAdapter.add(new ListRow(connectedServicesHeader, connectedServiceAdapter));

                    // Available services

                    ArrayObjectAdapter availableServiceAdapter = new ArrayObjectAdapter(new ServicePresenter());

                    if(!connectedServices.contains(String.valueOf(ServicePlayer.ServiceType.GoogleMusic.getValue()))) {
                        availableServiceAdapter.add(ServicePlayer.ServiceType.GoogleMusic);
                        m_AvailableServices.add(ServicePlayer.ServiceType.GoogleMusic);
                    }

                    if(!connectedServices.contains(String.valueOf(ServicePlayer.ServiceType.Spotify.getValue()))) {
                        availableServiceAdapter.add(ServicePlayer.ServiceType.Spotify);
                        m_AvailableServices.add(ServicePlayer.ServiceType.Spotify);
                    }

                    if(!connectedServices.contains(String.valueOf(ServicePlayer.ServiceType.Deezer.getValue()))) {
                        availableServiceAdapter.add(ServicePlayer.ServiceType.Deezer);
                        m_AvailableServices.add(ServicePlayer.ServiceType.Deezer);
                    }

                    HeaderItem availableServicesHeader = new HeaderItem(getResources().getString(R.string.manageaccountsactivity_category_availableservices));
                    mManageAccountAdapter.add(new ListRow(availableServicesHeader, availableServiceAdapter));
                }
            }
        );
    }

    @Override
    public void onItemClicked(Presenter.ViewHolder itemViewHolder, Object item, RowPresenter.ViewHolder rowViewHolder, Row row) {
        if(item instanceof ServicePlayer.ServiceType) {
            final ServicePlayer.ServiceType type = (ServicePlayer.ServiceType) item;
            final Player player = Player.getInstance();

            if(m_AvailableServices.contains(type)) {
                if(!player.login(type, this)) {
                    Toast.makeText(this, getResources().getString(R.string.manageaccountsactivity_toast_commingsoon), Toast.LENGTH_SHORT).show();
                }
            } else {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle(getResources().getString(R.string.manageaccountsactivity_disconnectdialog_title));
                builder.setMessage(getResources().getString(R.string.manageaccountsactivity_disconnectdialog_text));

                builder.setPositiveButton(getResources().getString(R.string.manageaccountsactivity_disconnectdialog_positive), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) { player.logout(type); }
                });
                builder.setNegativeButton(getResources().getString(R.string.manageaccountsactivity_disconnectdialog_negative), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) { dialog.dismiss(); }
                });

                builder.create().show();
            }
        }
    }

    @Override
    public void onPlayerStateChanged(ServicePlayer.ServiceType type, ServicePlayer.State old_state, ServicePlayer.State new_state) {
        // Nothing to do..
    }

    @Override
    public void onPlayerEvent(ServicePlayer.Event event) {
        // Nothing to do..
    }

    @Override
    public void onLoginSuccess(ServicePlayer.ServiceType type) {
        updateUI();
    }

    @Override
    public void onLoginError(ServicePlayer.ServiceType type) {
        Toast.makeText(this, getResources().getString(R.string.manageaccountsactivity_toast_loginerror), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onLogoutSuccess(ServicePlayer.ServiceType type) {
        updateUI();
    }

    @Override
    public void onLogoutError(ServicePlayer.ServiceType type) {
        Toast.makeText(this, getResources().getString(R.string.manageaccountsactivity_toast_logouterror), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onPlayerPlaybackPositionChanged(int position) {
        // Nothing to do..
    }
}
