package uk.woolhouse.pinreal.model;

import android.database.Cursor;
import android.location.Location;

import com.google.firebase.firestore.DocumentSnapshot;

import java.util.Objects;

import uk.woolhouse.pinreal.LocationFinder;

public final class Landmark {
    private final String uuid;
    private final String name;
    private final String desc;
    private final String img;
    private final Location location;
    private final double radius;

    public Landmark(String uuid, String name, String desc, String img, Location location, double radius) {
        this.uuid = uuid;
        this.name = name;
        this.desc = desc;
        this.img = img;
        this.location = location;
        this.radius = radius;
    }

    public static Landmark From(DocumentSnapshot doc) {
        return new Landmark(doc.getId(), doc.getString("name"), doc.getString("desc"), doc.getString("img"), LocationFinder.geo(doc.getGeoPoint("loc")), doc.getDouble("radius"));
    }

    public static Landmark From(Cursor cursor) {
        var loc = new Location("");
        loc.setLatitude(cursor.getDouble(4));
        loc.setLongitude(cursor.getDouble(5));
        return new Landmark(cursor.getString(0), cursor.getString(1), cursor.getString(2), cursor.getString(3), loc, cursor.getDouble(6));
    }

    public String uuid() {
        return uuid;
    }

    public String name() {
        return name;
    }

    public String desc() {
        return desc;
    }

    public String img() {
        return img;
    }

    public Location location() {
        return location;
    }

    public double radius() {
        return radius;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (Landmark) obj;
        return Objects.equals(this.uuid, that.uuid) &&
                Objects.equals(this.name, that.name) &&
                Objects.equals(this.desc, that.desc) &&
                Objects.equals(this.img, that.img) &&
                Objects.equals(this.location, that.location) &&
                Double.doubleToLongBits(this.radius) == Double.doubleToLongBits(that.radius);
    }

    @Override
    public int hashCode() {
        return Objects.hash(uuid, name, desc, img, location, radius);
    }

    @Override
    public String toString() {
        return "Landmark[" +
                "uuid=" + uuid + ", " +
                "name=" + name + ", " +
                "desc=" + desc + ", " +
                "img=" + img + ", " +
                "location=" + location + ", " +
                "radius=" + radius + ']';
    }


}
