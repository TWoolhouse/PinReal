package uk.woolhouse.pinreal.database;

import android.provider.BaseColumns;

public class Contract {
    public static final int VERSION = 3;

    public class Img implements BaseColumns {
        public static final String TABLE_NAME = "img";
        public static final String name = "name";
        public static final String data = "data";
    }

    public class Landmark implements  BaseColumns {
        public static final String TABLE_NAME = "landmark";
        public static final String uuid = "uuid";
        public static final String name = "name";
        public static final String desc = "desc";
        public static final String img = "img";
        public static final String lat = "lat";
        public static final String lng = "long";
    }

    private Contract() {}
}
