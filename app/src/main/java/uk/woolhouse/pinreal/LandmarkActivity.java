package uk.woolhouse.pinreal;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.location.LocationListener;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.Filter;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;

import java.io.File;
import java.io.IOException;

public class LandmarkActivity extends AppCompatActivity {
    private static final String TAG = "LandmarkActivity";
    private static final String ARG_UUID = "uuid_landmark";
    private String uuid;
    private GeoPoint loc;
    private LocationFinder finder;

    @NonNull
    public static Intent From(android.content.Context packageContext, @NonNull String uuid) {
        Intent intent = new Intent(packageContext, LandmarkActivity.class);
        intent.putExtra(ARG_UUID, uuid);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_landmark);

        Intent intent = getIntent();
        uuid = intent.getStringExtra(ARG_UUID);

        TextView view_title = (TextView) this.findViewById(R.id.landmark_text_title);
        TextView view_description = (TextView) this.findViewById(R.id.landmark_text_description);
        ImageView view_img = (ImageView) this.findViewById(R.id.landmark_image);
        TextView view_dist = (TextView) this.findViewById(R.id.landmark_text_distance);
        RecyclerView view_photos = (RecyclerView) this.findViewById(R.id.landmark_images);
        view_photos.setLayoutManager(new GridLayoutManager(this, 2));

        finder = new LocationFinder(this, new LocationListener() {
            @Override
            public void onLocationChanged(@NonNull Location location) {
                if (loc != null) {
                    Log.d(TAG, location.toString());
                    float distance = location.distanceTo(LocationFinder.geo(loc));
                    view_dist.post(new Runnable() {
                        @Override
                        public void run() {
                            view_dist.setText(distance < 1000 ? String.format("%.1fm", distance) : String.format("%.2fkm", distance / 1000));
                        }
                    });
                }
            }
        });

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        DocumentReference doc_ref = db.collection("landmark").document(uuid);
        doc_ref.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        Log.d(TAG, document.getId() + " => " + document.getData());
                        view_title.setText(document.getString("name"));
                        view_description.setText(document.getString("desc"));
                        loc = document.getGeoPoint("loc");

                        FirebaseStorage storage = FirebaseStorage.getInstance();
                        try {
                            File temp = File.createTempFile("landmark", "img");
                            storage.getReference("bigben.jpg").getFile(temp).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                                @Override
                                public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                                    Bitmap bitmap = BitmapFactory.decodeFile(temp.getAbsolutePath());
                                    view_img.setImageBitmap(bitmap);
                                }
                            });
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }

                        db.collection("photo").whereEqualTo("landmark", doc_ref).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                            @Override
                            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                                view_photos.setAdapter(new PhotoAdapter(queryDocumentSnapshots));
                            }
                        });


                    } else {
                        Toast.makeText(getApplicationContext(), "Unknown Landmark: " + uuid, Toast.LENGTH_SHORT).show();
                        finish();
                    }
                } else {
                    Log.e(TAG, "get failed with ", task.getException());
                }
            }
        });
    }

    @Override
    protected void onPause() {
        finder.pause();
        super.onPause();
    }

    @Override
    protected void onResume() {
        finder.resume();
        super.onResume();
    }
}
