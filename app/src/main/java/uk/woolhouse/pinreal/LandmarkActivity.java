package uk.woolhouse.pinreal;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.Locale;

import uk.woolhouse.pinreal.model.Landmark;

public class LandmarkActivity extends AppCompatActivity {
    private static final String TAG = "LandmarkActivity";
    private static final String ARG_UUID = "uuid_landmark";
    private String uuid;
    private Landmark landmark;
    private LocationFinder finder;
    private Database db;

    private Proximity proximity = null;
    private Boolean taken = true;

    @NonNull
    public static Intent From(android.content.Context packageContext, @NonNull String uuid) {
        var intent = new Intent(packageContext, LandmarkActivity.class);
        intent.putExtra(ARG_UUID, uuid);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_landmark);

        var intent = getIntent();
        uuid = intent.getStringExtra(ARG_UUID);

        var view_title = (TextView) this.findViewById(R.id.landmark_text_title);
        var view_description = (TextView) this.findViewById(R.id.landmark_text_description);
        var view_img = (ImageView) this.findViewById(R.id.landmark_image);
        var view_dist = (TextView) this.findViewById(R.id.landmark_text_distance);
        var view_visitors = (TextView) this.findViewById(R.id.landmark_text_visitors);
        var view_photos = (RecyclerView) this.findViewById(R.id.landmark_images);
        var view_camera = (Button) this.findViewById(R.id.landmark_camera);
        var view_refresh = (SwipeRefreshLayout) this.findViewById(R.id.refresh_landmark_photos);
        view_photos.setLayoutManager(new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL));

        finder = new LocationFinder(this, geo -> {
            if (landmark != null) {
                view_dist.post(() -> view_dist.setText(LocationFinder.format(landmark.location(), geo)));
                view_camera.post(() -> view_camera.setVisibility(!taken && (geo.distanceTo(landmark.location()) < landmark.radius()) ? View.VISIBLE : View.GONE));
            }
        });

        db = new Database(this);
        db.landmark(uuid, landmark -> {
            if (landmark == null) {
                Err.unknown(this, "Landmark", uuid);
                return;
            }
            this.landmark = landmark;
            view_title.setText(landmark.name());
            view_description.setText(landmark.desc());

            db.img(landmark.img(), file -> {
                X.SetImage(view_img, file);
            });

            var fire = FirebaseFirestore.getInstance();
            fire.collection("photo").whereEqualTo("landmark", fire.collection("landmark").document(uuid)).orderBy("time", Query.Direction.DESCENDING).get().addOnSuccessListener(queryDocumentSnapshots -> {
                view_visitors.setText(String.valueOf(queryDocumentSnapshots.size()));
                var me = X.me(this);
                var found = false;
                for (var doc : queryDocumentSnapshots) {
                    if (doc.getDocumentReference("owner").getId().equals(me)) {
                        found = true;
                        break;
                    }
                }
                if (!found)
                    taken = false;
                view_photos.setAdapter(new PhotoLandmarkAdapter(db, queryDocumentSnapshots));
            });
            view_refresh.setOnRefreshListener(() -> {
                fire.collection("photo").whereEqualTo("landmark", fire.collection("landmark").document(uuid)).orderBy("time", Query.Direction.DESCENDING).get().addOnSuccessListener(queryDocumentSnapshots -> {
                    view_visitors.setText(String.valueOf(queryDocumentSnapshots.size()));
                    view_photos.setAdapter(new PhotoLandmarkAdapter(db, queryDocumentSnapshots));
                    view_refresh.setRefreshing(false);
                });
            });
        });
    }

    @Override
    protected void onPause() {
        finder.pause();
        super.onPause();
    }

    @Override
    protected void onResume() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            finder.resume();
        }
        super.onResume();
    }

    public void showMap(Location location) {
        var intent = new Intent(Intent.ACTION_VIEW, Uri.parse(String.format(Locale.UK, "geo:%f,%f", location.getLatitude(), location.getLongitude())));
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        }
    }

    public void openLocation(View view) {
        if (landmark != null) {
            showMap(landmark.location());
        }
    }

    @Override
    protected void onDestroy() {
        db.onDestroy();
        super.onDestroy();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (proximity == null || proximity.onActivityResult(requestCode, resultCode, data)) super.onActivityResult(requestCode, resultCode, data);
        proximity = null;
    }

    public void camera(View view) {
        if (taken) return;
        proximity = new Proximity(this, uuid, X.me(this));
        taken = true;
    }
}
