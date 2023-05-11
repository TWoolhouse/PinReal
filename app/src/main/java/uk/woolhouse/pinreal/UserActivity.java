package uk.woolhouse.pinreal;

import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import uk.woolhouse.pinreal.model.User;

public class UserActivity extends AppCompatActivity {

    private static final String TAG = "UserActivity";
    private static final String ARG_UUID = "uuid_user";

    private String uuid;
    private Database db;

    @NonNull
    public static Intent From(android.content.Context packageContext, @NonNull String uuid) {
        Intent intent = new Intent(packageContext, UserActivity.class);
        intent.putExtra(ARG_UUID, uuid);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);

        Intent intent = getIntent();
        uuid = intent.getStringExtra(ARG_UUID);

        var view_username = (TextView) findViewById(R.id.user_text_username);
        var view_post_count = (TextView) findViewById(R.id.user_text_photo_count);
        var view_img = (ImageView) findViewById(R.id.user_img_pfp);
        var view_posts = (RecyclerView) findViewById(R.id.user_imgs);
        view_posts.setLayoutManager(new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL));

        db = new Database(this);
        db.user(uuid, user -> {
            if (user == null) {
                Err.unknown(this, "User", uuid);
            }
            view_username.setText(user.name());

            db.img(user.img(), file -> {
                view_img.setImageBitmap(BitmapFactory.decodeFile(file.getAbsolutePath()));
            });

            var fire = FirebaseFirestore.getInstance();
            fire.collection("photo").whereEqualTo("owner", fire.collection("user").document(uuid)).orderBy("time", Query.Direction.DESCENDING).get().addOnSuccessListener(queryDocumentSnapshots -> {
                view_post_count.setText(String.valueOf(queryDocumentSnapshots.size()));
                view_posts.setAdapter(new PhotoUserAdapter(db, queryDocumentSnapshots));
            });
        });
    }
}