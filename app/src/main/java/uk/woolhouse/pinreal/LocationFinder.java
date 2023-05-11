package uk.woolhouse.pinreal;

import static com.google.android.gms.location.LocationRequest.PRIORITY_HIGH_ACCURACY;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.location.Location;
import android.os.Looper;

import androidx.annotation.NonNull;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.firestore.GeoPoint;

public class LocationFinder {
    private static final String TAG = "LocationFinder";
    private final FusedLocationProviderClient fusedLocationClient;
    private final LocationListener callback;
    private final LocationCallback cb;
    private final LocationRequest request;
    private final Looper looper;
    private boolean active = false;

    public LocationFinder(@NonNull Activity activity, @NonNull LocationListener callback) {
        this.callback = callback;
        request = new LocationRequest();
        request.setInterval(1000);
        request.setPriority(PRIORITY_HIGH_ACCURACY);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(activity);
        looper = Looper.getMainLooper();

        cb = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                callback.onLocationChanged(locationResult.getLastLocation());
            }
        };
    }

    public static final Location geo(@NonNull GeoPoint geo) {
        Location loc = new Location("");
        loc.setLatitude(geo.getLatitude());
        loc.setLongitude(geo.getLongitude());
        return loc;
    }

    public static final String format(@NonNull Location from, @NonNull Location to) {
        var dist = from.distanceTo(to);
        return dist < 1000 ? String.format("%.1fm", dist) : String.format("%.2fkm", dist / 1000);
    }

    @SuppressLint("MissingPermission")
    public final LocationFinder resume() {
        if (!active) {
            active = true;
            fusedLocationClient.requestLocationUpdates(request, cb, looper);
        }
        return this;
    }

    public final void pause() {
        if (active) {
            active = false;
            fusedLocationClient.removeLocationUpdates(cb);
        }
    }
}
