package uk.woolhouse.pinreal;

import android.annotation.SuppressLint;
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

public class ProximityActivity extends AppCompatActivity {

    static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final String TAG = "ProximityActivity";
    private File buffer;

    @NonNull
    public static Intent From(android.content.Context packageContext) {
        var intent = new Intent(packageContext, ProximityActivity.class);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_proximity);
    }

    public void camera(View view) {
        dispatchTakePictureIntent();
    }

    private void dispatchTakePictureIntent() {
        var intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        try {
            buffer = createImageFile();
            if (buffer != null) {
                var uri = FileProvider.getUriForFile(this, "uk.woolhouse.PinReal.fileprovider", buffer);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
                startActivityForResult(intent, REQUEST_IMAGE_CAPTURE);
            }
        } catch (ActivityNotFoundException e) {
            Log.e(TAG, "Camera App not Found!");
        } catch (IOException e) {
            Log.e(TAG, "Unable to create File!");
        }
    }

    private File createImageFile() throws IOException {
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile("camera", ".jpg", storageDir);
        return image;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE) {
            if (resultCode == RESULT_OK && buffer != null) {
                // TODO: Un-hardcode
                upload(buffer, "QsVNkwl5r2OzMOg36iLU", "alice");
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void upload(@NonNull File file, @NonNull String landmark, @NonNull String owner) {
         startForegroundService(SenderService.From(this, file, landmark, owner));
    }
}