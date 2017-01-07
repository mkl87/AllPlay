package eu.applabs.allplaytv.presenter;

import android.content.Context;
import android.support.v17.leanback.widget.ImageCardView;
import android.support.v17.leanback.widget.Presenter;
import android.support.v4.content.ContextCompat;
import android.view.ViewGroup;


import com.bumptech.glide.Glide;

import eu.applabs.allplaylibrary.services.ServiceType;
import eu.applabs.allplaytv.R;

public class ServicePresenter extends Presenter {

    private Context mContext;

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent) {
        mContext = parent.getContext();


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

    private void updateCardBackgroundColor(ImageCardView view, boolean selected) {
        if(selected) {
            view.setInfoAreaBackgroundColor(ContextCompat.getColor(mContext, R.color.accent));
        } else {
            view.setInfoAreaBackgroundColor(ContextCompat.getColor(mContext, R.color.primary));
        }
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, Object item) {
        ServiceType type = (ServiceType) item;

        ImageCardView cardView = (ImageCardView) viewHolder.view;
        switch(type) {
            case SPOTIFY:
                cardView.setTitleText(mContext.getString(R.string.service_spotify));
                cardView.setMainImage(ContextCompat.getDrawable(mContext, R.drawable.spotify));
                break;
            case DEEZER:
                cardView.setTitleText(mContext.getString(R.string.service_deezer));
                cardView.setMainImage(ContextCompat.getDrawable(mContext, R.drawable.deezer));
                break;
            case GOOGLE_MUSIC:
                cardView.setTitleText(mContext.getString(R.string.service_gmusic));
                cardView.setMainImage(ContextCompat.getDrawable(mContext, R.drawable.gmusic));
                break;
        }

        cardView.setMainImageDimensions(400, 400);
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
