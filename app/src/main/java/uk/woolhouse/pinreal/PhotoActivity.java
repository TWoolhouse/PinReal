package uk.woolhouse.pinreal;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import uk.woolhouse.pinreal.model.Landmark;

public class PhotoActivity extends AppCompatActivity {

    private static final String TAG = "PhotoActivity";
    private static final String ARG_UUID = "uuid_photo";

    private String uuid;
    private Landmark landmark;
    private LocationFinder finder;
    private Database db;

    @NonNull
    public static Intent From(android.content.Context packageContext, @NonNull String uuid) {
        Intent intent = new Intent(packageContext, PhotoActivity.class);
        intent.putExtra(ARG_UUID, uuid);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo);

        Intent intent = getIntent();
        uuid = intent.getStringExtra(ARG_UUID);

        var view_landmark_name = (TextView) findViewById(R.id.photo_landmark_name);
        var view_landmark_btn = (Button) findViewById(R.id.photo_btn_landmark);
        var view_photo = (ImageView) findViewById(R.id.photo_img);
        var view_user_name = (TextView) findViewById(R.id.photo_user_name);

        finder = new LocationFinder(this, loc -> {
            if (landmark != null) {
                view_landmark_btn.post(() -> view_landmark_btn.setText(LocationFinder.format(landmark.location(), loc)));
            }
        });

        db = new Database(this);
        db.photo(uuid, photo -> {
            if (photo == null) {
                Err.unknown(this, "Photo", uuid);
                return;
            }
            db.landmark(photo.landmark(), landmark -> {
                view_landmark_name.setText(landmark.name());
                this.landmark = landmark;
            });
            db.user(photo.owner(), user -> {
                // TODO: USER PFP
            });
            db.img(photo.img(), file -> {
                X.SetImage(view_photo, file);
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

    @Override
    protected void onDestroy() {
        db.onDestroy();
        super.onDestroy();
    }
}