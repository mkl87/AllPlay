package eu.applabs.allplaytv.presenter;

import android.graphics.drawable.Drawable;
import android.support.v17.leanback.widget.ImageCardView;
import android.support.v17.leanback.widget.Presenter;
import android.view.ViewGroup;


import com.bumptech.glide.Glide;

import eu.applabs.allplaylibrary.player.ServicePlayer;
import eu.applabs.allplaytv.R;

public class ServicePresenter extends Presenter {

    private static int s_CardWidth = 400;
    private static int s_CardHeight = 400;
    private static int s_SelectedBackground = 0;
    private static int s_DefaultBackground = 0;

    private Drawable m_DefaultCardImage = null;

    private ViewGroup m_Parent = null;

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent) {
        m_Parent = parent;
        m_DefaultCardImage = parent.getResources().getDrawable(R.drawable.ic_action_person, null);
        s_SelectedBackground = parent.getResources().getColor(R.color.accent);
        s_DefaultBackground = parent.getResources().getColor(R.color.primary);

        ImageCardView cardView = new ImageCardView(parent.getContext()) {
            @Override
            public void setSelected(boolean selected) {
                updateCardBackgroundColor(this, selected);
                super.setSelected(selected);
            }
        };

        cardView.setFocusable(true);
        cardView.setFocusableInTouchMode(true);
        updateCardBackgroundColor(cardView, false);

        return new ViewHolder(cardView);
    }

    private static void updateCardBackgroundColor(ImageCardView view, boolean selected) {
        if(selected) {
            view.setInfoAreaBackgroundColor(s_SelectedBackground);
        } else {
            view.setInfoAreaBackgroundColor(s_DefaultBackground);
        }
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, Object item) {
        ServicePlayer.ServiceType type = (ServicePlayer.ServiceType) item;

        ImageCardView cardView = (ImageCardView) viewHolder.view;
        switch(type) {
            case GoogleMusic:
                cardView.setTitleText(m_Parent.getResources().getString(R.string.service_gmusic));
                cardView.setMainImage(m_Parent.getResources().getDrawable(R.drawable.gmusic, null));
                break;
            case Spotify:
                cardView.setTitleText(m_Parent.getResources().getString(R.string.service_spotify));
                cardView.setMainImage(m_Parent.getResources().getDrawable(R.drawable.spotify, null));
                break;
            case Deezer:
                cardView.setTitleText(m_Parent.getResources().getString(R.string.service_deezer));
                cardView.setMainImage(m_Parent.getResources().getDrawable(R.drawable.deezer, null));
                break;
        }
        cardView.setMainImageDimensions(s_CardWidth, s_CardHeight);
    }

    @Override
    public void onUnbindViewHolder(ViewHolder viewHolder) {
        ImageCardView cardView = (ImageCardView) viewHolder.view;
        Glide.clear(cardView.getMainImageView());
        Glide.get(cardView.getContext()).clearMemory();

        cardView.setBadgeImage(null);
        cardView.setMainImage(null);
    }
}
