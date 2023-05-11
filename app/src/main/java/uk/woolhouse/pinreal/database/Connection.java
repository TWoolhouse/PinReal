package uk.woolhouse.pinreal.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import uk.woolhouse.pinreal.model.Landmark;

public class Connection {
    private final Db db;
    private final SQLiteDatabase connection;

    public Connection(Context context) {
        db = new Db(context);
        connection = db.getWritableDatabase();
    }

    public void onDestroy() {
        db.close();
    }

    public byte[] img_get(String name) {
        var cursor = connection.rawQuery(Command.ImgGet, new String[]{name});
        try {
            if (cursor.moveToFirst()) {
                return cursor.getBlob(1);
            }
        } finally {
            cursor.close();
        }
        return null;
    }

    public void img_set(String name, byte[] data) {
        var values = new ContentValues();
        values.put(Contract.Img.name, name);
        values.put(Contract.Img.data, data);
        connection.insert(Contract.Img.TABLE_NAME, null, values);
    }

    public Landmark landmark_get(String uuid) {
        var cursor = connection.rawQuery(Command.LandmarkGet, new String[]{uuid});
        try {
            if (cursor.moveToFirst()) {
                return Landmark.From(cursor);
            }
        } finally {
            cursor.close();
        }
        return null;
    }

    public void landmark_set(Landmark landmark) {
        var values = new ContentValues();
        values.put(Contract.Landmark.uuid, landmark.uuid());
        values.put(Contract.Landmark.name, landmark.name());
        values.put(Contract.Landmark.desc, landmark.desc());
        values.put(Contract.Landmark.img, landmark.img());
        values.put(Contract.Landmark.lat, landmark.location().getLatitude());
        values.put(Contract.Landmark.lng, landmark.location().getLongitude());
        connection.insert(Contract.Landmark.TABLE_NAME, null, values);
    }

}
