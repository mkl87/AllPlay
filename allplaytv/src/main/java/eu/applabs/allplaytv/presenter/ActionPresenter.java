package eu.applabs.allplaytv.presenter;

import android.graphics.drawable.Drawable;
import android.support.v17.leanback.widget.ImageCardView;
import android.support.v17.leanback.widget.Presenter;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;

import eu.applabs.allplaytv.R;
import eu.applabs.allplaytv.data.Action;

public class ActionPresenter extends Presenter {

    private static int s_CardWidth = 200;
    private static int s_CardHeight = 200;
    private static int s_SelectedBackground = 0;
    private static int s_DefaultBackground = 0;

    private Drawable m_DefaultCardImage = null;

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent) {
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
        Action action = (Action) item;

        ImageCardView cardView = (ImageCardView) viewHolder.view;
        cardView.setTitleText(action.getName());
        cardView.setMainImageDimensions(s_CardWidth, s_CardHeight);
        cardView.setMainImage(action.getIcon());

        Glide.with(viewHolder.view.getContext())
                .load("")
                .centerCrop()
                .error(m_DefaultCardImage)
                .into(cardView.getMainImageView());
    }

    @Override
    public void onUnbindViewHolder(ViewHolder viewHolder) {
        ImageCardView cardView = (ImageCardView) viewHolder.view;
        Glide.clear(cardView.getMainImageView());
        Glide.get(cardView.getContext()).clearMemory();

        cardView.setMainImage(null);
        m_DefaultCardImage = null;
    }
}
