package uk.woolhouse.pinreal.notification;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import uk.woolhouse.pinreal.Lambda;
import uk.woolhouse.pinreal.MainActivity;
import uk.woolhouse.pinreal.R;

public class SenderService extends Service {

    private static final String TAG = "NotificationSenderService";
    private static final String ARG_IMG = "img_data";
    private static final String ARG_LANDMARK = "landmark_uuid";
    private static final String ARG_OWNER = "owner_uuid";

    private Looper serviceLooper;
    private ServiceHandler serviceHandler;
    private FirebaseFirestore firestore;
    private FirebaseStorage firestorage;

    @NonNull
    public static Intent From(android.content.Context packageContext, @NonNull File file, @NonNull String landmark, @NonNull String owner) {
        var intent = new Intent(packageContext, SenderService.class);
        intent.putExtra(ARG_IMG, file.getAbsolutePath());
        intent.putExtra(ARG_LANDMARK, landmark);
        intent.putExtra(ARG_OWNER, owner);
        return intent;
    }

    @Override
    public void onCreate() {
        var thread = new HandlerThread("SenderService", Process.THREAD_PRIORITY_BACKGROUND);
        thread.start();

        serviceLooper = thread.getLooper();
        serviceHandler = new ServiceHandler(serviceLooper);

        firestore = FirebaseFirestore.getInstance();
        firestorage = FirebaseStorage.getInstance();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        var notificationIntent = new Intent(this, MainActivity.class);
        var pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE);
        var channel = Channel.Posting;
        var notification = new Notification.Builder(this, channel.id).setContentTitle(getText(R.string.notification_upload_post_title)).setContentText(getText(R.string.notification_upload_post_body)).setSmallIcon(R.mipmap.ic_launcher).setContentIntent(pendingIntent).setCategory(Notification.CATEGORY_SERVICE).setOngoing(true).build();

        var notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationManager.createNotificationChannel(new NotificationChannel(channel.id, channel.name, NotificationManager.IMPORTANCE_LOW));
        }
        startForeground(1, notification);

        Message msg = serviceHandler.obtainMessage();
        msg.arg1 = startId;
        msg.setData(intent.getExtras());
        serviceHandler.sendMessage(msg);

        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    private final class ServiceHandler extends Handler {
        public ServiceHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            var bundle = msg.getData();
            var filepath = (String) bundle.get(ARG_IMG);
            var landmark = bundle.getString(ARG_LANDMARK);
            var owner = bundle.getString(ARG_OWNER);

            Log.d(TAG, String.format("Posting Photo: %s @ %s <%s>", owner, landmark, filepath));

            var file = new File(filepath);
            try {
                prepare_image(file);
            } catch (IOException e) {}
            upload(file, landmark, owner, _null -> {
                file.delete();
                stopSelf(msg.arg1);
            });
        }

        private void prepare_image(@NonNull File file) throws IOException {
            Log.d(TAG, String.format("Compressing Photo: %s", file.getAbsolutePath()));
            var opts = new BitmapFactory.Options();
            opts.inSampleSize = 1;
            var bitmap = BitmapFactory.decodeFile(file.getAbsolutePath(), opts);
            var stream = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, stream);
            stream.close();
        }

        private void upload(@NotNull File image, @NonNull String landmark, @NonNull String owner, Lambda<Object> done) {
            firestore.collection("photo").add(new HashMap<String, Object>()).addOnSuccessListener(doc -> {
                var uuid = doc.getId();
                FileInputStream ifstream = null;
                try {
                    ifstream = new FileInputStream(image);
                } catch (FileNotFoundException e) {}
                firestorage.getReference().child(uuid).putStream(ifstream, new StorageMetadata.Builder().setContentType("image/jpg").setCacheControl("public, max-age=31536000, immutable").build()).addOnSuccessListener(task -> {
                    Log.i(TAG, String.format("%s uploaded %d bytes", uuid, task.getBytesTransferred()));
                    Map<String, Object> photo = new HashMap<>();
                    photo.put("img", uuid);
                    photo.put("landmark", firestore.collection("landmark").document(landmark));
                    photo.put("owner", firestore.collection("user").document(owner));
                    photo.put("time", FieldValue.serverTimestamp());
                    doc.set(photo).addOnCompleteListener(_task -> {
                        done.call(null);
                    });
                }).addOnFailureListener(task -> {
                    Log.e(TAG, task.toString());
                    done.call(null);
                });
            }).addOnFailureListener(task -> {
                Log.e(TAG, task.toString());
                done.call(null);
            });
        }
    }
}
