package uk.woolhouse.pinreal.notification;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.io.File;
import java.util.Map;

import uk.woolhouse.pinreal.Database;
import uk.woolhouse.pinreal.LoginActivity;
import uk.woolhouse.pinreal.PhotoActivity;
import uk.woolhouse.pinreal.R;
import uk.woolhouse.pinreal.cb.NameCollector;
import uk.woolhouse.pinreal.model.Landmark;
import uk.woolhouse.pinreal.model.User;

public class ReceiverService extends FirebaseMessagingService {
    private static final String TAG = "NotificationReceiveService";
    private Database db = null;

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        Log.d(TAG, "Notification!");

        var content = remoteMessage.getData();
        // Check if message contains a data payload.
        if (content.size() == 0) {
            Log.w(TAG, "Message contains no data!");
        }

        if ("post_new".equals(content.get("type"))) {
            notificationNewPost(content);
        } else {
            Log.w(TAG, "Message has no corresponding type");
        }
    }

    @Override
    public void onNewToken(@NonNull String token) {
        Log.d(TAG, "Refreshed token: " + token);
        LoginActivity.preferences(this).edit().putString(getString(R.string.key_token), token).apply();
    }

    @Override
    public void onCreate() {
        db = new Database(this);
        super.onCreate();
    }

    @Override
    public void onDestroy() {
        db.onDestroy();
        super.onDestroy();
    }

    private void notificationNewPost(@NonNull Map<String, String> content) {
        var uuid = content.get("uuid");
        Log.d(TAG, "Notification - Post New: " + uuid);

        var collector = new NameCollector<Object>();
        collector.wait(4, results -> {
            var img = (File) results.get("img");
            var user = (User) results.get("user");
            var landmark = (Landmark) results.get("landmark");
            var photo_uid = uuid;
            sendNotification(photo_uid.hashCode(), PhotoActivity.From(this, photo_uid), Channel.NewPost, notification -> {
                var title = String.format(getString(R.string.notification_new_post_title), user.name(), landmark.name());
                var body = String.format(getString(R.string.notification_new_post_body), user.name(), landmark.name());
                var bitmap = BitmapFactory.decodeFile(img.getAbsolutePath());
                return notification.setContentTitle(title).setContentText(body).setLargeIcon(bitmap).setCategory(Notification.CATEGORY_SOCIAL);
            });
        });

        db.photo(uuid, photo -> {
            db.landmark(photo.landmark(), obj -> collector.done("landmark", obj));
            db.user(photo.owner(), obj -> collector.done("user", obj));
            db.img(photo.img(), file -> collector.done("img", file));
            collector.done("photo", photo);
        });

    }

    private void sendNotification(int code, Intent intent, Channel channel, Notify builder) {
        var pendingIntent = PendingIntent.getActivity(this, code, intent, PendingIntent.FLAG_IMMUTABLE);

        var notificationBuilder = builder.build(new NotificationCompat.Builder(this, channel.id).setSmallIcon(R.mipmap.ic_launcher).setAutoCancel(true).setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)).setContentIntent(pendingIntent));

        var notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // Since android Oreo notification channel is needed.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationManager.createNotificationChannel(new NotificationChannel(channel.id, channel.name, NotificationManager.IMPORTANCE_DEFAULT));
        }

        notificationManager.notify(code, notificationBuilder.build());
    }

    public interface Notify {
        NotificationCompat.Builder build(@NonNull NotificationCompat.Builder builder);
    }
}
