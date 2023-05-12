package uk.woolhouse.pinreal.notification;

public enum Channel {
    NewPost("following_post_new", "New Post"),
    Posting("uploading_post", "Uploading Post");

    public final String id;
    public final String name;

    Channel(String id, String name) {
        this.id = id;
        this.name = name;
    }
}