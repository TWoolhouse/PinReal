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

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.OkHttpResponseListener;
import com.google.firebase.FirebaseApp;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import okhttp3.Response;
import uk.woolhouse.pinreal.Database;
import uk.woolhouse.pinreal.Lambda;
import uk.woolhouse.pinreal.MainActivity;
import uk.woolhouse.pinreal.R;
import uk.woolhouse.pinreal.cb.SeqCollector;

public class SenderService extends Service {

    private static final String TAG = "NotificationSenderService";
    private static final String ARG_IMG = "img_data";
    private static final String ARG_LANDMARK = "landmark_uuid";
    private static final String ARG_OWNER = "owner_uuid";

    private Looper serviceLooper;
    private ServiceHandler serviceHandler;
    private FirebaseFirestore firestore;
    private FirebaseStorage firestorage;
    private Database db;

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
        db = new Database(this);
        AndroidNetworking.initialize(getApplicationContext());
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
        db.onDestroy();
        AndroidNetworking.shutDown();
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
            } catch (IOException e) {
            }
            var stopID = msg.arg1;
            upload(file, landmark, owner, photo -> {
                Log.i(TAG, String.format("Photo Done: %s @ %s <%s>", owner, landmark, filepath));
                file.delete();
                if (photo == null) stopSelf(stopID);
                else send_notification(owner, photo, success -> {
                    stopSelf(stopID);
                });
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

        private void upload(@NotNull File image, @NonNull String landmark, @NonNull String owner, Lambda<String> done) {
            firestore.collection("photo").add(new HashMap<String, Object>()).addOnSuccessListener(doc -> {
                var uuid = doc.getId();
                FileInputStream ifstream = null;
                try {
                    ifstream = new FileInputStream(image);
                } catch (FileNotFoundException e) {
                    return;
                }
                FileInputStream finalIfstream = ifstream;
                firestorage.getReference().child(uuid).putStream(ifstream, new StorageMetadata.Builder().setContentType("image/jpg").setCacheControl("public, max-age=31536000, immutable").build()).addOnSuccessListener(task -> {
                    Log.d(TAG, String.format("%s uploaded %d bytes", uuid, task.getBytesTransferred()));
                    Map<String, Object> photo = new HashMap<>();
                    photo.put("img", uuid);
                    photo.put("landmark", firestore.collection("landmark").document(landmark));
                    photo.put("owner", firestore.collection("user").document(owner));
                    photo.put("time", FieldValue.serverTimestamp());
                    doc.set(photo).addOnCompleteListener(_task -> {
                        done.call(_task.isSuccessful() ? uuid : null);
                    });
                }).addOnFailureListener(task -> {
                    Log.e(TAG, task.toString());
                    done.call(null);
                }).addOnCompleteListener(task -> {
                    try {
                        finalIfstream.close();
                    } catch (IOException e) {
                    }
                });
            }).addOnFailureListener(task -> {
                Log.e(TAG, task.toString());
                done.call(null);
            });
        }

        private void send_notification(@NonNull String owner, @NonNull String photo, Lambda<Boolean> done) {
            firestore.collection("user").document(owner).collection("followers").get().addOnSuccessListener(collection -> {
                Log.i(TAG, String.format("Notifying %d Users", collection.size()));
                var collector = new SeqCollector<String>();
                collector.wait(collection.size(), tokens -> {
                    var messages = message_post_new(tokens, photo);
                    var requests = new SeqCollector<Boolean>();
                    requests.wait(messages.size(), reqs -> {
                        done.call(true);
                    });
                    for (var msg : messages) {
                        var body = new JSONObject();
                        var tks = new JSONArray();
                        msg.tokens.forEach(tks::put);
                        try {
                            body.put("registration_ids", tks);
                            var data = new JSONObject();
                            data.put("type", MessagePostNew.TYPE);
                            data.put("uuid", photo);
                            body.put("data", data);
                        } catch (JSONException e) {
                            throw new RuntimeException(e);
                        }
                        var headers = new HashMap<String, String>();
                        // TODO: Place API Key Here!
                        headers.put("Authorization", "key=SECRET_KEY");
                        headers.put("Content-Type", "application/json");
                        Log.d(TAG, body.toString());
                        AndroidNetworking.post("https://fcm.googleapis.com/fcm/send")
                                .addHeaders(headers)
                                .addJSONObjectBody(body)
                                .build().getAsOkHttpResponse(new OkHttpResponseListener() {
                                    @Override
                                    public void onResponse(Response response) {
                                        Log.d(TAG, response.toString());
                                        Log.d(TAG, "Request Success!");
                                        requests.done(true);
                                    }
                                    @Override
                                    public void onError(ANError anError) {
                                        Log.w(TAG, "Request Failed!");
                                        requests.done(false);
                                    }
                                });
                    }
                });
                for (var subscription : collection) {
                    firestore.collection("user").document(subscription.getId()).get().addOnSuccessListener(doc -> {
                        collector.done(doc.getString("tk"));
                    });
                }
            }).addOnFailureListener(task -> {
                done.call(false);
            });
        }

        private List<List<String>> batch_tokens(List<String> tokens) {
            var batches = new ArrayList<List<String>>();
            var max_size = 500;
            var index = 0;
            while (index < tokens.size()) {
                batches.add(tokens.subList(index, Math.min(tokens.size(), index + max_size)));
                index += max_size;
            }
            return batches;
        }

        private List<MessagePostNew> message_post_new(List<String> tokens, @NonNull String photo) {
            var messages = new ArrayList<MessagePostNew>();
            for (var batch : batch_tokens(tokens))
                messages.add(new MessagePostNew(batch, photo));
            return messages;
        }
    }

    private static final class MessagePostNew {
        private final List<String> tokens;
        private final String photo;

        public static final String TYPE = "post_new";

        private MessagePostNew(List<String> tokens, String photo) {
            this.tokens = tokens;
            this.photo = photo;
        }

        public List<String> tokens() {
            return tokens;
        }

        public String photo() {
            return photo;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) return true;
            if (obj == null || obj.getClass() != this.getClass()) return false;
            var that = (MessagePostNew) obj;
            return Objects.equals(this.tokens, that.tokens) &&
                    Objects.equals(this.photo, that.photo);
        }

        @Override
        public int hashCode() {
            return Objects.hash(tokens, photo);
        }

        @Override
        public String toString() {
            return "MessagePostNew[" +
                    "tokens=" + tokens + ", " +
                    "photo=" + photo + ']';
        }
    }
}
