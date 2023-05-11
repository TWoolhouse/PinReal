package uk.woolhouse.pinreal;

import android.app.Activity;
import android.widget.Toast;

public class Err {
    public static void unknown(Activity activity, String type, String uuid) {
        Toast.makeText(activity.getApplicationContext(), String.format("Unknown %s: %s", type, uuid), Toast.LENGTH_SHORT).show();
        activity.finish();
    }
}
