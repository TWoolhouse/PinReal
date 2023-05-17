package uk.woolhouse.pinreal;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

import uk.woolhouse.pinreal.model.Landmark;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private Database db;
    private LocationFinder finder;


    private List<Landmark> landmarks = null;
    private boolean located = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Global Setup
        {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED)
                    requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.POST_NOTIFICATIONS}, 0);
            } else {
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
                    requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION}, 0);
            }
        }

        // Activity Setup
        {
            db = new Database(this);

            var view_list_landmarks = (RecyclerView) findViewById(R.id.home_landmarks);
            var view_list_following = (RecyclerView) findViewById(R.id.home_following);

            finder = new LocationFinder(this, location -> {
                if (landmarks == null) return;
                view_list_landmarks.setAdapter(new LandmarkAdapter(db, location, landmarks));
                located = true;
                finder.pause();
            });
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED)
                finder.resume();

            view_list_landmarks.setLayoutManager(new LinearLayoutManager(this, RecyclerView.VERTICAL, false));
            view_list_following.setLayoutManager(new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL));

            var fire = FirebaseFirestore.getInstance();
            fire.collection("landmark").get().addOnSuccessListener(collection -> {
                ArrayList<Landmark> list = new ArrayList<>();
                for (QueryDocumentSnapshot document : collection) {
                    list.add(Landmark.From(document));
                }
                landmarks = list;
            });

            var refresh_following = (SwipeRefreshLayout) findViewById(R.id.refresh_following);
            refresh_following.setOnRefreshListener(() -> {
                fire.collection("photo").orderBy("time", Query.Direction.DESCENDING).get().addOnSuccessListener(collection -> {
                    view_list_following.setAdapter(new PhotoAdapter(db, collection));
                    refresh_following.setRefreshing(false);
                });
            });
            fire.collection("photo").orderBy("time", Query.Direction.DESCENDING).get().addOnSuccessListener(collection -> {
                view_list_following.setAdapter(new PhotoAdapter(db, collection));
            });
        }
    }

    @Override
    protected void onDestroy() {
        if (db != null) db.onDestroy();
        super.onDestroy();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        recreate();
    }

    @Override
    protected void onResume() {
        if (LoginActivity.current_user(this) == null) {
            startActivity(LoginActivity.From(this));
        }
        if (!located && (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED))
            finder.resume();
        super.onResume();
    }

    @Override
    protected void onPause() {
        finder.pause();
        super.onPause();
    }

    public void gotoWebview(View view) {
        startActivity(GuideActivity.From(this));
    }
}
