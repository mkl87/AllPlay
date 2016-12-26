package eu.applabs.allplaytv.presenter;

import android.graphics.drawable.Drawable;
import android.support.v17.leanback.widget.ImageCardView;
import android.support.v17.leanback.widget.Presenter;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;

import eu.applabs.allplaytv.R;
import eu.applabs.allplaytv.data.Action;

public class ActionPresenter extends Presenter {

    private static int CARD_WIDTH = 200;
    private static int CARD_HEIGHT = 200;
    private static int SELECTED_BACKGROUND = 0;
    private static int DEFAULT_BACKGROUND = 0;

    private ViewGroup mParent;
    private Drawable mDefaultCardImage;

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent) {
        mParent = parent;

        mDefaultCardImage = ContextCompat.getDrawable(mParent.getContext(), R.drawable.ic_action_person);
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
        Action action = (Action) item;

        ImageCardView cardView = (ImageCardView) viewHolder.view;
        cardView.setTitleText(action.getName());
        cardView.setMainImageDimensions(CARD_WIDTH, CARD_HEIGHT);
        cardView.setMainImage(action.getIcon());

        Glide.with(viewHolder.view.getContext())
                .load("")
                .centerCrop()
                .error(mDefaultCardImage)
                .into(cardView.getMainImageView());
    }

    @Override
    public void onUnbindViewHolder(ViewHolder viewHolder) {
        ImageCardView cardView = (ImageCardView) viewHolder.view;
        Glide.clear(cardView.getMainImageView());
        Glide.get(cardView.getContext()).clearMemory();

        cardView.setMainImage(null);
        mDefaultCardImage = null;
    }
}
