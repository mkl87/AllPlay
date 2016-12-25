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

    private Playlist m_PlayerPlaylist = null;
    private Context m_Context = null;
    private ImageView m_Background = null;
    private List<Song> m_SongList = null;
    private OnPositionSelectedListener mOnPositionSelectedListener;

    public PlaylistAdapter(Context context, ImageView background, List<Song> list, OnPositionSelectedListener onPositionSelectedListener) {
        m_Context = context;
        m_Background = background;
        m_SongList = list;
        mOnPositionSelectedListener = onPositionSelectedListener;

        m_PlayerPlaylist = Player.getInstance().getPlaylist();
    }

    public void clearPlaylistAdapter() {
        m_PlayerPlaylist = null;
        m_SongList = null;

        Glide.clear(m_Background);
        m_Background = null;

        Glide.get(m_Context).clearMemory();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.playlistrow, parent, false);
        ViewHolder vh = new ViewHolder(v);

        return vh;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        if(position == m_PlayerPlaylist.getCurrentSongIndex()) {
            holder.m_CardView.bringToFront();
            holder.m_LinearLayout.setBackgroundColor(m_Context.getResources().getColor(R.color.accent));

            // Clear the old image
            Glide.clear(m_Background);
            Glide.get(m_Context).clearMemory();

            // Set the new image
            Glide.with(m_Context)
                    .load(m_PlayerPlaylist.getPlaylistAsSongList().get(m_PlayerPlaylist.getCurrentSongIndex()).getCoverSmall())
                    .centerCrop()
                    .error(m_Context.getDrawable(R.drawable.nocover))
                    .into(m_Background);

        } else {
            holder.m_LinearLayout.setBackgroundColor(m_Context.getResources().getColor(R.color.background));
        }

        holder.m_Title.setText(m_SongList.get(position).getTitle());
        holder.m_Artist.setText(m_SongList.get(position).getArtist());
        holder.mOnPositionSelectedListener = mOnPositionSelectedListener;

        Glide.with(m_Context)
                .load(m_SongList.get(position).getCoverSmall())
                .centerCrop()
                .error(m_Context.getResources().getDrawable(R.drawable.nocover, null))
                .into(holder.m_ImageView);
    }

    @Override
    public void onViewDetachedFromWindow(ViewHolder holder) {
        super.onViewDetachedFromWindow(holder);

        Glide.clear(holder.m_ImageView);
        Glide.get(m_Context).clearMemory();
    }

    @Override
    public int getItemCount() {
        if(m_SongList != null) {
            return m_SongList.size();
        }

        return 0;
    }

    public void updatePlaylist(List<Song> list) {
        m_SongList = list;
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
