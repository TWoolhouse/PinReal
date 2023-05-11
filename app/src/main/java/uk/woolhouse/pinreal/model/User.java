package uk.woolhouse.pinreal.model;

import com.google.firebase.firestore.DocumentSnapshot;

import java.util.Objects;

public final class User {
    private final String uuid;
    private final String img;

    public User(String uuid, String img) {
        this.uuid = uuid;
        this.img = img;
    }

    public static User From(DocumentSnapshot doc) {
        return new User(doc.getId(), doc.getString("img"));
    }

    public String name() {
        return uuid;
    }

    public String uuid() {
        return uuid;
    }

    public String img() {
        return img;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (User) obj;
        return Objects.equals(this.uuid, that.uuid) &&
                Objects.equals(this.img, that.img);
    }

    @Override
    public int hashCode() {
        return Objects.hash(uuid, img);
    }

    @Override
    public String toString() {
        return "User[" +
                "uuid=" + uuid + ", " +
                "img=" + img + ']';
    }

}
