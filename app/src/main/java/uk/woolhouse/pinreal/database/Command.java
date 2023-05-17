package uk.woolhouse.pinreal.database;

public class Command {
    private static final String TableCreateImg =
            "CREATE TABLE " + Contract.Img.TABLE_NAME + " (" +
                    Contract.Img._ID + " INTEGER PRIMARY KEY," +
                    Contract.Img.name + " TEXT," +
                    Contract.Img.data + " BLOB)";

    private static final String TableCreateLandmark =
            "CREATE TABLE " + Contract.Landmark.TABLE_NAME + " (" +
                    Contract.Landmark._ID + " INTEGER PRIMARY KEY," +
                    Contract.Landmark.uuid + " TEXT," +
                    Contract.Landmark.name + " TEXT," +
                    Contract.Landmark.desc + " TEXT," +
                    Contract.Landmark.img + " TEXT," +
                    Contract.Landmark.lat + " REAL," +
                    Contract.Landmark.lng + " REAL," +
                    Contract.Landmark.radius + " REAL)";

    public static final String[] TableCreate = new String[]{
            TableCreateImg, TableCreateLandmark,
    };

    private static final String TableDeleteImg =
            "DROP TABLE IF EXISTS " + Contract.Img.TABLE_NAME;
    private static final String TableDeleteLandmark =
            "DROP TABLE IF EXISTS " + Contract.Landmark.TABLE_NAME;

    public static final String[] TableDelete = new String[]{
            TableDeleteImg, TableDeleteLandmark
    };

    public static final String ImgGet =
        String.format("SELECT %s,%s FROM %s WHERE %s = ? LIMIT 1", Contract.Img.name, Contract.Img.data, Contract.Img.TABLE_NAME, Contract.Img.name);

    public static final String LandmarkGet =
            String.format("SELECT %s,%s,%s,%s,%s,%s,%s FROM %s WHERE %s = ? LIMIT 1", Contract.Landmark.uuid, Contract.Landmark.name, Contract.Landmark.desc, Contract.Landmark.img, Contract.Landmark.lat, Contract.Landmark.lng, Contract.Landmark.radius, Contract.Landmark.TABLE_NAME, Contract.Landmark.uuid);

}