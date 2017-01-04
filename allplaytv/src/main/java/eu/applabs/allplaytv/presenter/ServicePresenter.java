package eu.applabs.allplaytv.presenter;

import android.support.v17.leanback.widget.ImageCardView;
import android.support.v17.leanback.widget.Presenter;
import android.support.v4.content.ContextCompat;
import android.view.ViewGroup;


import com.bumptech.glide.Glide;

import eu.applabs.allplaylibrary.services.ServiceType;
import eu.applabs.allplaytv.R;

public class ServicePresenter extends Presenter {

    private static final int CARD_WIDTH = 400;
    private static final int CARD_HEIGHT = 400;

    private static int SELECTED_BACKGROUND = 0;
    private static int DEFAULT_BACKGROUND = 0;

    private ViewGroup mParent;

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent) {
        mParent = parent;

        SELECTED_BACKGROUND = ContextCompat.getColor(mParent.getContext(), R.color.accent);
        DEFAULT_BACKGROUND = ContextCompat.getColor(mParent.getContext(), R.color.primary);

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
            view.setInfoAreaBackgroundColor(SELECTED_BACKGROUND);
        } else {
            view.setInfoAreaBackgroundColor(DEFAULT_BACKGROUND);
        }
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, Object item) {
        ServiceType type = (ServiceType) item;

        ImageCardView cardView = (ImageCardView) viewHolder.view;
        switch(type) {
            case GOOGLE_MUSIC:
                cardView.setTitleText(mParent.getResources().getString(R.string.service_gmusic));
                cardView.setMainImage(mParent.getResources().getDrawable(R.drawable.gmusic, null));
                break;
            case SPOTIFY:
                cardView.setTitleText(mParent.getResources().getString(R.string.service_spotify));
                cardView.setMainImage(mParent.getResources().getDrawable(R.drawable.spotify, null));
                break;
            case DEEZER:
                cardView.setTitleText(mParent.getResources().getString(R.string.service_deezer));
                cardView.setMainImage(mParent.getResources().getDrawable(R.drawable.deezer, null));
                break;
        }
        cardView.setMainImageDimensions(CARD_WIDTH, CARD_HEIGHT);
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
