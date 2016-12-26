package eu.applabs.allplaytv.adapter;

import android.content.Context;
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

import eu.applabs.allplaylibrary.data.Song;
import eu.applabs.allplaylibrary.player.Player;
import eu.applabs.allplaylibrary.player.Playlist;
import eu.applabs.allplaytv.R;

public class PlaylistAdapter extends RecyclerView.Adapter<PlaylistAdapter.ViewHolder> {

    public interface OnPositionSelectedListener {
        void onPositionSelected(int position);
    }

    private Playlist mPlayerPlaylist = Player.getInstance().getPlaylist();
    private Context mContext;
    private ImageView mBackground;
    private List<Song> mSongList;
    private OnPositionSelectedListener mOnPositionSelectedListener;

    public PlaylistAdapter(Context context, ImageView background, List<Song> list, OnPositionSelectedListener onPositionSelectedListener) {
        mContext = context;
        mBackground = background;
        mSongList = list;
        mOnPositionSelectedListener = onPositionSelectedListener;
    }

    public void clearPlaylistAdapter() {
        mPlayerPlaylist = null;
        mSongList = null;

        Glide.clear(mBackground);
        mBackground = null;

        Glide.get(mContext).clearMemory();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.playlistrow, parent, false);
        ViewHolder vh = new ViewHolder(v);

        return vh;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        if(position == mPlayerPlaylist.getCurrentSongIndex()) {
            holder.m_CardView.bringToFront();
            holder.m_LinearLayout.setBackgroundColor(mContext.getResources().getColor(R.color.accent));

            // Clear the old image
            Glide.clear(mBackground);
            Glide.get(mContext).clearMemory();

            // Set the new image
            Glide.with(mContext)
                    .load(mPlayerPlaylist.getPlaylistAsSongList().get(mPlayerPlaylist.getCurrentSongIndex()).getCoverSmall())
                    .centerCrop()
                    .error(mContext.getDrawable(R.drawable.nocover))
                    .into(mBackground);

        } else {
            holder.m_LinearLayout.setBackgroundColor(mContext.getResources().getColor(R.color.background));
        }

        holder.m_Title.setText(mSongList.get(position).getTitle());
        holder.m_Artist.setText(mSongList.get(position).getArtist());
        holder.mOnPositionSelectedListener = mOnPositionSelectedListener;

        Glide.with(mContext)
                .load(mSongList.get(position).getCoverSmall())
                .centerCrop()
                .error(mContext.getResources().getDrawable(R.drawable.nocover, null))
                .into(holder.m_ImageView);
    }

    @Override
    public void onViewDetachedFromWindow(ViewHolder holder) {
        super.onViewDetachedFromWindow(holder);

        Glide.clear(holder.m_ImageView);
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

        View m_View;

        LinearLayout m_LinearLayout;
        CardView m_CardView;
        ImageView m_ImageView;
        TextView m_Title;
        TextView m_Artist;

        OnPositionSelectedListener mOnPositionSelectedListener;

        public ViewHolder(View v) {
            super(v);

            m_View = v;
            m_View.setOnClickListener(this);

            m_LinearLayout = (LinearLayout) m_View.findViewById(R.id.id_ll_PlaylistRow_LinearLayout);
            m_CardView = (CardView) m_View.findViewById(R.id.id_cv_PlaylistRow_Card);
            m_ImageView = (ImageView) m_View.findViewById(R.id.id_iv_PlaylistRow_Cover);
            m_Title = (TextView) m_View.findViewById(R.id.id_tv_PlaylistRow_Title);
            m_Artist = (TextView) m_View.findViewById(R.id.id_tv_PlaylistRow_Artist);
        }

        @Override
        public void onClick(View v) {
            if(mOnPositionSelectedListener != null) {
                mOnPositionSelectedListener.onPositionSelected(getAdapterPosition());
            }
        }
    }
}
