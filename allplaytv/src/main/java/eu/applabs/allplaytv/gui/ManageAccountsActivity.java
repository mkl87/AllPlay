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
import android.support.v17.leanback.widget.ListRow;
import android.support.v17.leanback.widget.ListRowPresenter;
import android.support.v17.leanback.widget.OnItemViewClickedListener;
import android.support.v17.leanback.widget.Presenter;
import android.support.v17.leanback.widget.PresenterSelector;
import android.support.v17.leanback.widget.Row;
import android.support.v17.leanback.widget.RowPresenter;
import android.support.v4.content.ContextCompat;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import eu.applabs.allplaylibrary.AllPlayLibrary;
import eu.applabs.allplaylibrary.event.Event;
import eu.applabs.allplaylibrary.event.ServiceConnectionEvent;
import eu.applabs.allplaylibrary.services.ServicePlayer;
import eu.applabs.allplaylibrary.player.Player;
import eu.applabs.allplaylibrary.services.ServiceType;
import eu.applabs.allplaytv.R;
import eu.applabs.allplaytv.presenter.IconHeaderItemPresenter;
import eu.applabs.allplaytv.presenter.ServicePresenter;

public class ManageAccountsActivity extends Activity implements OnItemViewClickedListener, Observer {

    private FragmentManager mFragmentManager;
    private BrowseFragment mBrowseFragment;
    private ArrayObjectAdapter mManageAccountAdapter;

    private Player mPlayer = AllPlayLibrary.getInstance().getPlayer();

    private List<ServiceType> mConnectedServices = new ArrayList<>();
    private List<ServiceType> mAvailableServices = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manageaccounts);

        mFragmentManager = getFragmentManager();
        mBrowseFragment = (BrowseFragment) mFragmentManager.findFragmentById(R.id.id_frag_ManageAccounts);

        mBrowseFragment.setHeadersState(BrowseFragment.HEADERS_ENABLED);
        mBrowseFragment.setHeaderPresenterSelector(new PresenterSelector() {
            @Override
            public Presenter getPresenter(Object item) {
                return new IconHeaderItemPresenter();
            }
        });

        mBrowseFragment.setTitle(getString(R.string.app_name));
        mBrowseFragment.setOnItemViewClickedListener(this);

        BackgroundManager backgroundManager = BackgroundManager.getInstance(this);
        backgroundManager.attach(this.getWindow());
        backgroundManager.setDrawable(ContextCompat.getDrawable(this, R.drawable.background));

        mManageAccountAdapter = new ArrayObjectAdapter(new ListRowPresenter());
        mBrowseFragment.setAdapter(mManageAccountAdapter);

        mPlayer.addObserver(this);

        updateUI();
    }

    @Override
    protected void onDestroy() {
        mPlayer.deleteObserver(this);
        Glide.get(this).clearMemory();

        super.onDestroy();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(!mPlayer.checkActivityResult(requestCode, resultCode, data)) {
            // Seems to be a result for another request
        }
    }

    public void updateUI() {
        runOnUiThread(
            new Runnable() {
                @Override
                public void run() {
                    mManageAccountAdapter.clear();
                    mConnectedServices.clear();
                    mAvailableServices.clear();

                    // Connected services

                    ArrayObjectAdapter connectedServiceAdapter = new ArrayObjectAdapter(new ServicePresenter());
                    List<ServiceType> connectedServices = AllPlayLibrary.getInstance().getConnectedServiceTypes();
                    for(ServiceType serviceType : connectedServices) {
                        connectedServiceAdapter.add(serviceType);
                        mConnectedServices.add(serviceType);
                    }

                    IconHeaderItem connectedServicesHeader = new IconHeaderItem(getResources().getString(R.string.manageaccountsactivity_category_connectedservices), ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_connectedservices));
                    mManageAccountAdapter.add(new ListRow(connectedServicesHeader, connectedServiceAdapter));

                    // Available services

                    ArrayObjectAdapter availableServiceAdapter = new ArrayObjectAdapter(new ServicePresenter());

                    if(!connectedServices.contains(ServiceType.GOOGLE_MUSIC)) {
                        availableServiceAdapter.add(ServiceType.GOOGLE_MUSIC);
                        mAvailableServices.add(ServiceType.GOOGLE_MUSIC);
                    }

                    if(!connectedServices.contains(ServiceType.SPOTIFY)) {
                        availableServiceAdapter.add(ServiceType.SPOTIFY);
                        mAvailableServices.add(ServiceType.SPOTIFY);
                    }

                    if(!connectedServices.contains(ServiceType.DEEZER)) {
                        availableServiceAdapter.add(ServiceType.DEEZER);
                        mAvailableServices.add(ServiceType.DEEZER);
                    }

                    IconHeaderItem availableServicesHeader = new IconHeaderItem(getResources().getString(R.string.manageaccountsactivity_category_availableservices), ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_availableservices));
                    mManageAccountAdapter.add(new ListRow(availableServicesHeader, availableServiceAdapter));
                }
            }
        );
    }

    @Override
    public void onItemClicked(Presenter.ViewHolder itemViewHolder, Object item, RowPresenter.ViewHolder rowViewHolder, Row row) {
        if(item instanceof ServiceType) {
            final ServiceType type = (ServiceType) item;
            final Player player = AllPlayLibrary.getInstance().getPlayer();

            if(mAvailableServices.contains(type)) {
                if(!player.login(this, type)) {
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
    public void update(Observable observable, Object o) {
        if(o instanceof Event) {
            Event event = (Event) o;

            if(event instanceof ServiceConnectionEvent) {
                ServiceConnectionEvent serviceConnectionEvent = (ServiceConnectionEvent) event;

                if(serviceConnectionEvent.getServiceConnectionEventType() == ServiceConnectionEvent.ServiceConnectionEventType.CONNECTED
                        || serviceConnectionEvent.getServiceConnectionEventType() == ServiceConnectionEvent.ServiceConnectionEventType.DISCONNECTED) {
                    updateUI();
                }
            }
        }
    }
}
