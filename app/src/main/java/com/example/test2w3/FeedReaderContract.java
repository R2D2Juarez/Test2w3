package com.example.test2w3;

import android.provider.BaseColumns;

/**
 * Created by pharr on 06/08/17.
 */

public class FeedReaderContract {
    private FeedReaderContract(){}

    public static class FeedEntry implements BaseColumns {
        public static final String TABLE_NAME = "users";
        public static final String COLUMN_NAME_NAME = "name";
        public static final String COLUMN_NAME_ADDRESS = "address";
        public static final String COLUMN_NAME_EMAIL = "email";
        public static final String COLUMN_NAME_PICTURE_LOCATIION = "picture_location";
    }
}
