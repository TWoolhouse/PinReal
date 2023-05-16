package uk.woolhouse.pinreal;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.widget.ImageView;

import androidx.annotation.NonNull;

import java.io.File;

// Utility Class
public class X {

    // Set an image view from a temporary file, then clean up the file.
    public static void SetImage(ImageView view, File file) {
        try {
            view.setImageBitmap(BitmapFactory.decodeFile(file.getAbsolutePath()));
        } finally {
            file.delete();
        }
    }

    @NonNull
    public static String me(Context content) {
        return LoginActivity.current_user(content);
    }
}
