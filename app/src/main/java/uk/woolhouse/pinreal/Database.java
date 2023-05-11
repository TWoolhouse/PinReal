package uk.woolhouse.pinreal;

import android.content.Context;
import android.util.Log;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import uk.woolhouse.pinreal.database.Connection;
import uk.woolhouse.pinreal.model.Landmark;
import uk.woolhouse.pinreal.model.Photo;
import uk.woolhouse.pinreal.model.User;

public class Database {
    private static final String TAG = "Cache";
    private final Connection db;
    private final FirebaseStorage fbs;
    private final FirebaseFirestore fbfs;

    public Database(Context context) {
        db = new Connection(context);
        fbs = FirebaseStorage.getInstance();
        fbfs = FirebaseFirestore.getInstance();
    }

    public void img(String name, Lambda<File> lambda) {
        try {
            File temp = File.createTempFile("cache", "pic");
            var blob = db.img_get(name);
            if (blob != null) {
                var stream = new FileOutputStream(temp);
                stream.write(blob);
                stream.close();
                lambda.call(temp);
            } else {
                fbs.getReference(name).getFile(temp).addOnSuccessListener(taskSnapshot -> {
                    try {
                        var stream = new FileInputStream(temp);
                        var data = new byte[(int) temp.length()];
                        stream.read(data);
                        stream.close();
                        db.img_set(name, data);
                    } catch (IOException e) {
                        Log.e(TAG, e.toString());
                    }
                    lambda.call(temp);
                });
            }
        } catch (IOException e) {
            Log.e(TAG, e.toString());
        }
    }

    public void landmark(String uuid, Lambda<Landmark> lambda) {
        var landmark = db.landmark_get(uuid);
        if (landmark != null) {
            lambda.call(landmark);
        } else {
            fbfs.collection("landmark").document(uuid).get().addOnSuccessListener(document -> {
                var model = document.exists() ? Landmark.From(document) : null;
                db.landmark_set(model);
                lambda.call(model);
            });
        }
    }
    public void photo(String uuid, Lambda<Photo> lambda) {
        fbfs.collection("photo").document(uuid).get().addOnSuccessListener(document -> {
            lambda.call(document.exists() ? Photo.From(document) : null);
        });
    }
    public void user(String uuid, Lambda<User> lambda) {
        fbfs.collection("user").document(uuid).get().addOnSuccessListener(document -> {
            lambda.call(document.exists() ? User.From(document) : null);
        });
    }


    public void onDestroy() {
        db.onDestroy();
    }

    public interface Lambda<T> {
        void call(T object);
    }
}
