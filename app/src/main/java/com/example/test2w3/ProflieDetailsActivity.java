package com.example.test2w3;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import com.example.test2w3.FeedReaderContract.FeedEntry;

public class ProflieDetailsActivity extends AppCompatActivity {

    private DBHelper helper;
    private SQLiteDatabase database;
    private String searchValue = "";
    private String LOCATION = "";

    TextView profileNameTV;
    TextView profileAddressTV;
    TextView profileEmailTV;
    ImageView profileUserPictureIV;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_proflie_details);

        helper = new DBHelper(this);
        database = helper.getWritableDatabase();

        Intent intent = getIntent();
        searchValue = intent.getStringExtra(MainActivity.SEARCH_RECORD);

        profileNameTV = (TextView) findViewById(R.id.tv_profile_name);
        profileAddressTV = (TextView) findViewById(R.id.tv_profile_address);
        profileEmailTV = (TextView) findViewById(R.id.tv_profile_email);
        profileUserPictureIV = (ImageView) findViewById(R.id.iv_userPofilePicture);

        searchRecord();
        getImageFromURL();

    }

    public void searchRecord(){
        Cursor cursor = null;
        cursor = this.database.rawQuery(
                "select * from " + FeedEntry.TABLE_NAME +
                " where " + FeedEntry._ID + "=" + searchValue  , null);
        if (cursor != null){
            if (cursor.moveToFirst()){
                String entryId = String.valueOf(cursor.getLong(cursor.getColumnIndexOrThrow(FeedEntry._ID)));
                String entryName = cursor.getString(cursor.getColumnIndexOrThrow(FeedEntry.COLUMN_NAME_NAME));
                String entryAddress = cursor.getString(cursor.getColumnIndexOrThrow(FeedEntry.COLUMN_NAME_ADDRESS));
                String entryEmail = cursor.getString(cursor.getColumnIndexOrThrow(FeedEntry.COLUMN_NAME_EMAIL));
                LOCATION = cursor.getString(cursor.getColumnIndexOrThrow(FeedEntry.COLUMN_NAME_PICTURE_LOCATIION));

                profileNameTV.setText(entryName);
                profileAddressTV.setText(entryAddress);
                profileEmailTV.setText(entryEmail);
            }
        }
    }

    public void getImageFromURL(){
        new DownloadImageTask(profileUserPictureIV).execute(LOCATION);
    }
}
