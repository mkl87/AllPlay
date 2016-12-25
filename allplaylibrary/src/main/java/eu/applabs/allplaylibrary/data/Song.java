package eu.applabs.allplaylibrary.data;

import eu.applabs.allplaylibrary.player.ServicePlayer;

public class Song {

    private ServicePlayer.ServiceType mServiceType;
    private String mUri;
    private String mId;

    private String mTitle;
    private String mArtist;
    private String mAlbum;
    private long mDuration;
    private String mCoverSmall;
    private String mCoverBig;
    private String mArtistUri;
    private String mArtistPicture;

    private int mLikes;
    private int mDislikes;

    public Song() {
        mServiceType = ServicePlayer.ServiceType.Undefined;
        mUri = "";
        mId = "";
        mTitle = "";
        mArtist = "";
        mAlbum = "";
        mDuration = 0;
        mCoverSmall = "";
        mCoverBig = "";
        mArtistUri = "";
        mArtistPicture = "";
        mLikes = 0;
        mDislikes = 0;
    }

    // Setter

    public void setServiceType(ServicePlayer.ServiceType serviceType) {
        mServiceType = serviceType;
    }

    public void setUri(String uri) {
        mUri = uri;
    }

    public void setId(String id) {
        mId = id;
    }

    public void setTitle(String title) {
        mTitle = title;
    }

    public void setArtist(String artist) {
        mArtist = artist;
    }

    public void setAlbum(String album) {
        mAlbum = album;
    }

    public void setDuration(long duration) {
        mDuration = duration;
    }

    public void setCoverSmall(String coverSmall) {
        mCoverSmall = coverSmall;
    }

    public void setCoverBig(String coverBig) {
        mCoverBig = coverBig;
    }

    public void setArtistUri(String artistUri) {
        mArtistUri = artistUri;
    }

    public void setArtistPicture(String artistPicture) {
        mArtistPicture = artistPicture;
    }

    public void setLikes(int likes) {
        mLikes = likes;
    }

    public void setDislikes(int dislikes) {
        mDislikes = dislikes;
    }

    // Getter

    public ServicePlayer.ServiceType getServiceType() {
        return mServiceType;
    }

    public String getUri() {
        return mUri;
    }

    public String getId() {
        return mId;
    }

    public String getTitle() {
        return mTitle;
    }

    public String getArtist() {
        return mArtist;
    }

    public String getAlbum() {
        return mAlbum;
    }

    public long getDuration() {
        return mDuration;
    }

    public String getCoverBig() {
        return mCoverBig;
    }

    public String getCoverSmall() {
        return mCoverSmall;
    }

    public String getArtistUri() {
        return mArtistUri;
    }

    public String getArtistPicture() {
        return mArtistPicture;
    }

    public int getLikes() {
        return mLikes;
    }

    public int getDislikes() {
        return mDislikes;
    }
}
