package eu.applabs.allplaytv.adapter;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import eu.applabs.allplaylibrary.AllPlayLibrary;
import eu.applabs.allplaylibrary.Playlist;
import eu.applabs.allplaylibrary.data.Song;
import eu.applabs.allplaytv.AllPlayTVApplication;
import eu.applabs.allplaytv.R;

public class PlaylistAdapter extends RecyclerView.Adapter<PlaylistAdapter.ViewHolder> {

    public interface OnPositionSelectedListener {
        void onPositionSelected(int position);
    }

    @Inject
    Context mContext;

    private Playlist mPlayerPlaylist = AllPlayLibrary.getInstance().getPlayer().getPlaylist();
    private ImageView mBackground;
    private List<Song> mSongList;
    private OnPositionSelectedListener mOnPositionSelectedListener;

    public PlaylistAdapter(ImageView background, List<Song> list, OnPositionSelectedListener onPositionSelectedListener) {
        AllPlayTVApplication.component().inject(this);

        mBackground = background;
        mSongList = list;
        mOnPositionSelectedListener = onPositionSelectedListener;
    }

    public void clearImages() {
        Glide.clear(mBackground);
        Glide.get(mContext).clearMemory();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.playlistrow, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        if(position == mPlayerPlaylist.getCurrentSongIndex()) {
            holder.mCardView.bringToFront();
            holder.mLinearLayout.setBackgroundColor(ContextCompat.getColor(mContext, R.color.accent));

            clearImages();

            // Set the new image
            Glide.with(mContext)
                    .load(mPlayerPlaylist.getPlaylist().get(mPlayerPlaylist.getCurrentSongIndex()).getCoverSmall())
                    .centerCrop()
                    .error(ContextCompat.getDrawable(mContext, R.drawable.nocover))
                    .into(mBackground);

        } else {
            holder.mLinearLayout.setBackgroundColor(ContextCompat.getColor(mContext, R.color.background));
        }

        holder.mTitle.setText(mSongList.get(position).getTitle());
        holder.mArtist.setText(mSongList.get(position).getArtist());
        holder.mOnPositionSelectedListener = mOnPositionSelectedListener;

        Glide.with(mContext)
                .load(mSongList.get(position).getCoverSmall())
                .centerCrop()
                .error(ContextCompat.getDrawable(mContext, R.drawable.nocover))
                .into(holder.mImageView);
    }

    @Override
    public void onViewDetachedFromWindow(ViewHolder holder) {
        super.onViewDetachedFromWindow(holder);

        Glide.clear(holder.mImageView);
        Glide.get(mContext).clearMemory();
    }

    @Override
    public int getItemCount() {
        if(mSongList != null) {
            return mSongList.size();
        }

        return 0;
    }

    public void updatePlaylist(List<Song> list) {
        mSongList = list;
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        @BindView(R.id.id_ll_PlaylistRow_LinearLayout)
        LinearLayout mLinearLayout;
        @BindView(R.id.id_cv_PlaylistRow_Card)
        CardView mCardView;
        @BindView(R.id.id_iv_PlaylistRow_Cover)
        ImageView mImageView;
        @BindView(R.id.id_tv_PlaylistRow_Title)
        TextView mTitle;
        @BindView(R.id.id_tv_PlaylistRow_Artist)
        TextView mArtist;

        OnPositionSelectedListener mOnPositionSelectedListener;

        public ViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
            view.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if(mOnPositionSelectedListener != null) {
                mOnPositionSelectedListener.onPositionSelected(getAdapterPosition());
            }
        }
    }
}
