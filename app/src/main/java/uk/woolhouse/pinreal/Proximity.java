package uk.woolhouse.pinreal;

import static android.app.Activity.RESULT_OK;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import uk.woolhouse.pinreal.notification.SenderService;

public class Proximity {

    static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final String TAG = "ProximityActivity";
    private final String landmark;
    private final String owner;
    private File buffer;

    private final Activity activity;

    public Proximity(Activity activity, @NonNull String landmark, @NonNull String owner) {
        this.activity = activity;
        this.landmark = landmark;
        this.owner = owner;
        dispatchTakePictureIntent();
    }

    private void dispatchTakePictureIntent() {
        var intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        try {
            buffer = createImageFile();
            if (buffer != null) {
                var uri = FileProvider.getUriForFile(activity, "uk.woolhouse.PinReal.fileprovider", buffer);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
                activity.startActivityForResult(intent, REQUEST_IMAGE_CAPTURE);
            }
        } catch (ActivityNotFoundException e) {
            Log.e(TAG, "Camera App not Found!");
        } catch (IOException e) {
            Log.e(TAG, "Unable to create File!");
        }
    }

    private File createImageFile() throws IOException {
        File storageDir = activity.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile("camera", ".jpg", storageDir);
        return image;
    }

    protected boolean onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE) {
            if (resultCode == RESULT_OK && buffer != null) {
                upload(buffer, landmark, owner);
            }
            return false;
        } return true;
    }

    private void upload(@NonNull File file, @NonNull String landmark, @NonNull String owner) {
         activity.startForegroundService(SenderService.From(activity, file, landmark, owner));
    }
}