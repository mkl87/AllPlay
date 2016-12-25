package eu.applabs.allplaylibrary.data;

import eu.applabs.allplaylibrary.player.IPlayer;

public class Song {

    private IPlayer.ServiceType m_ServiceType;
    private String m_Uri;
    private String m_Id;

    private String m_Title;
    private String m_Artist;
    private String m_Album;
    private long m_Duration;
    private String m_CoverSmall;
    private String m_CoverBig;
    private String m_ArtistUri;
    private String m_ArtistPicture;

    private int m_Likes;
    private int m_Dislikes;

    public Song() {
        m_ServiceType = IPlayer.ServiceType.Undefined;
        m_Uri = "";
        m_Id = "";
        m_Title = "";
        m_Artist = "";
        m_Album = "";
        m_Duration = 0;
        m_CoverSmall = "";
        m_CoverBig = "";
        m_ArtistUri = "";
        m_ArtistPicture = "";
        m_Likes = 0;
        m_Dislikes = 0;
    }

    // Setter

    public void setServiceType(IPlayer.ServiceType serviceType) {
        m_ServiceType = serviceType;
    }

    public void setUri(String uri) {
        m_Uri = uri;
    }

    public void setId(String id) {
        m_Id = id;
    }

    public void setTitle(String title) {
        m_Title = title;
    }

    public void setArtist(String artist) {
        m_Artist = artist;
    }

    public void setAlbum(String album) {
        m_Album = album;
    }

    public void setDuration(long duration) {
        m_Duration = duration;
    }

    public void setCoverSmall(String coverSmall) {
        m_CoverSmall = coverSmall;
    }

    public void setCoverBig(String coverBig) {
        m_CoverBig = coverBig;
    }

    public void setArtistUri(String artistUri) {
        m_ArtistUri = artistUri;
    }

    public void setArtistPicture(String artistPicture) {
        m_ArtistPicture = artistPicture;
    }

    public void setLikes(int likes) {
        m_Likes = likes;
    }

    public void setDislikes(int dislikes) {
        m_Dislikes = dislikes;
    }

    // Getter

    public IPlayer.ServiceType getServiceType() {
        return m_ServiceType;
    }

    public String getUri() {
        return m_Uri;
    }

    public String getId() {
        return m_Id;
    }

    public String getTitle() {
        return m_Title;
    }

    public String getArtist() {
        return m_Artist;
    }

    public String getAlbum() {
        return m_Album;
    }

    public long getDuration() {
        return m_Duration;
    }

    public String getCoverBig() {
        return m_CoverBig;
    }

    public String getCoverSmall() {
        return m_CoverSmall;
    }

    public String getArtistUri() {
        return m_ArtistUri;
    }

    public String getArtistPicture() {
        return m_ArtistPicture;
    }

    public int getLikes() {
        return m_Likes;
    }

    public int getDislikes() {
        return m_Dislikes;
    }
}
