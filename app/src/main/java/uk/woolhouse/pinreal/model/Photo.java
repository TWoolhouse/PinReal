package uk.woolhouse.pinreal.model;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.Objects;

public final class Photo {
    private final String uuid;
    private final String img;
    private final String landmark;
    private final String owner;
    private final Timestamp time;

    public Photo(String uuid, String img, String landmark, String owner, Timestamp time) {
        this.uuid = uuid;
        this.img = img;
        this.landmark = landmark;
        this.owner = owner;
        this.time = time;
    }

    public static Photo From(DocumentSnapshot doc) {
        return new Photo(doc.getId(), doc.getString("img"), doc.getDocumentReference("landmark").getId(), doc.getDocumentReference("owner").getId(), doc.getTimestamp("time"));
    }

    public String uuid() {
        return uuid;
    }

    public String img() {
        return img;
    }

    public String landmark() {
        return landmark;
    }

    public String owner() {
        return owner;
    }

    public Timestamp time() {
        return time;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (Photo) obj;
        return Objects.equals(this.uuid, that.uuid) &&
                Objects.equals(this.img, that.img) &&
                Objects.equals(this.landmark, that.landmark) &&
                Objects.equals(this.owner, that.owner) &&
                Objects.equals(this.time, that.time);
    }

    @Override
    public int hashCode() {
        return Objects.hash(uuid, img, landmark, owner, time);
    }

    @Override
    public String toString() {
        return "Photo[" +
                "uuid=" + uuid + ", " +
                "img=" + img + ", " +
                "landmark=" + landmark + ", " +
                "owner=" + owner + ", " +
                "time=" + time + ']';
    }

}
