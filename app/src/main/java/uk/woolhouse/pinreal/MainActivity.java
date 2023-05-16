package uk.woolhouse.pinreal;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.storage.FirebaseStorage;

import uk.woolhouse.pinreal.R;
import uk.woolhouse.pinreal.notification.SenderService;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
//        db.useEmulator("10.0.2.2", 8080);
        FirebaseStorage storage = FirebaseStorage.getInstance();
//        storage.useEmulator("10.0.2.2", 9199);

//        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
//                .setPersistenceEnabled(false)
//                .build();
//        db.setFirestoreSettings(settings);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void gotoLandmark1(View view) {
        startActivity(LandmarkActivity.From(MainActivity.this, "QsVNkwl5r2OzMOg36iLU"));
    }

    public void gotoCamera(View view) {
        startActivity(ProximityActivity.From(MainActivity.this));
    }

    public void gotoWebview(View view) {
        startActivity(GuideActivity.From(this));
    }
}
