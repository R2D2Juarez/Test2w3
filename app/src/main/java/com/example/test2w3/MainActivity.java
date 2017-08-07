package com.example.test2w3;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Random;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import com.example.test2w3.FeedReaderContract.FeedEntry;

public class MainActivity extends AppCompatActivity {

    private DBHelper helper;
    private SQLiteDatabase database;

    private static final String TAG = MainActivity.class.getSimpleName() + "_TAG";
    private static final String BASE_URL = "https://randomuser.me/api";
    private static String ADDRESS = "";
    private static String URL_PICTURE = "";
    public static String SEARCH_RECORD;
    private static Bitmap IMAGE_BITMAP;

    OkHttpClient client;

    Button getUserBTN;
    TextView nameTV;
    TextView addressTV;
    TextView emailTV;
    ImageView userPictureIV;
    EditText searchRecordET;
    TextView usersAddedET;
    Button saveProfileBTN;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        helper = new DBHelper(this);
        database = helper.getWritableDatabase();

        getUserBTN = (Button) findViewById(R.id.btn_getRandomUser);
        nameTV = (TextView) findViewById(R.id.tv_name);
        addressTV = (TextView) findViewById(R.id.tv_address);
        emailTV = (TextView) findViewById(R.id.tv_email);
        userPictureIV = (ImageView) findViewById(R.id.iv_userPicture);
        searchRecordET = (EditText) findViewById(R.id.et_seacrRecord);
        usersAddedET = (TextView) findViewById(R.id.tv_users_Added);
        saveProfileBTN = (Button) findViewById(R.id.btn_save);

        client = new OkHttpClient.Builder().build();
        //getRandomJSON();
    }

//________________GET THE RANDOM JSON AND SET VARIABLES_____________________________________________

    public void getRandomJSON(){
        Request request = new Request.Builder().url(BASE_URL).build();

        client.newCall(request).enqueue(
            new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    e.printStackTrace();
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    String resp = response.body().string();

                    //GET THE JSON RESPONSE
                    try{

                        JSONObject result = new JSONObject(resp);
                        JSONArray results = result.getJSONArray("results"); // Array of objects

                        JSONObject nameJSONObject = results.getJSONObject(0).getJSONObject("name");
                        JSONObject locationJSONObject = results.getJSONObject(0).getJSONObject("location");
                        JSONObject pictureJSONObject = results.getJSONObject(0).getJSONObject("picture");
                        final String emailJSONString = results.getJSONObject(0).getString("email");

                        Gson gson = new Gson();

                        //CREATE THE JSON OBJETC INTO A JAVA OBJECT
                        final Name nameObject = gson.fromJson(
                                String.valueOf(nameJSONObject),
                                Name.class);

                        final Location locationObject = gson.fromJson(
                                String.valueOf(locationJSONObject),
                                Location.class);

                        final Picture pictureObject = gson.fromJson(
                                String.valueOf(pictureJSONObject),
                                Picture.class);

                        //GET THE ATTACHED OBJECT ATTRIBUTES INTO THE CONSTANTS
                        URL_PICTURE = pictureObject.getMedium();
                        ADDRESS = locationObject.getStreet() + ", " +
                                  locationObject.getCity() + ", " +
                                  locationObject.getState() + ", " +
                                  locationObject.getPostcode();

                        //RUN THIS TRHAED IN ORDER TO EDIT COMPONENT IN THE VIEW
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                nameTV.setText(nameObject.getFirst() + " " + nameObject.getLast());
                                addressTV.setText(ADDRESS);
                                emailTV.setText(emailJSONString);
                                new DownloadImageTask(userPictureIV).execute(pictureObject.getMedium());
                            }
                        });

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        );
    }

//___________________________DOWNLOAD THE PICTURE INTO A BITMAP_____________________________________

    public void getImageFromURL(){
        new DownloadImageTask(userPictureIV).execute(URL_PICTURE);
    }
//__________________________________________________________________________________________________

    public void saveImageToExternalStorage(Bitmap imageBitmap) {


        String root = Environment.getExternalStorageDirectory().toString();
        File myDir = new File(root + "/Test2_w3");
        if (myDir.exists()){
            Log.d(TAG, "saveImageToExternalStorage: myDir already exists");
        }else {
            myDir.mkdirs();
            Log.d(TAG, "saveImageToExternalStorage: myDir was created!!");
        }

        String fname = "Image-" + "Test" + ".jpg";

        File file = new File(myDir, fname);
        Log.i(TAG, "" + file);
        Log.d(TAG, "saveImageToExternalStorage: " + myDir.toString());

        if (file.exists()) Log.d(TAG, "saveImageToExternalStorage: File alreadhy exixts ----> ");

        try {
            Log.d(TAG, "saveImageToExternalStorage: Get into the Try process");
            FileOutputStream out = new FileOutputStream(file);
            imageBitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
            out.flush();
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

//____________________________READ ALL THE PROFILES SAVED___________________________________________

    public void readAllProfiles(){

        usersAddedET.setText("");
        String[] projection = {
                FeedEntry._ID,
                FeedEntry.COLUMN_NAME_NAME
        };

        Cursor cursor = database.query(
                FeedEntry.TABLE_NAME,       // TABLE
                projection,                 //Projection
                null,                       //Selection Where
                null,                       //Values for selection
                null,                       //Group by
                null,                       //Filters
                null                        //Sort order
        );
        while (cursor.moveToNext()){

            long entryId = cursor.getLong(cursor.getColumnIndexOrThrow(FeedEntry._ID));
            String entryName = cursor.getString(cursor.getColumnIndexOrThrow(FeedEntry.COLUMN_NAME_NAME));

            usersAddedET.append("[ " + entryId + " ] " + "NAME --> " + entryName + "\n");
        }
    }
//_____________________________SAVE THE PROFILE DETAILS_____________________________________________

    public void saveProfile(){
        //Bitmap imageBitmap = ((BitmapDrawable)userPictureIV.getDrawable()).getBitmap();
        //saveImageToExternalStorage(imageBitmap);

        ContentValues values = new ContentValues();
        values.put(FeedEntry.COLUMN_NAME_NAME, nameTV.getText().toString());
        values.put(FeedEntry.COLUMN_NAME_ADDRESS, addressTV.getText().toString());
        values.put(FeedEntry.COLUMN_NAME_EMAIL, emailTV.getText().toString());
        values.put(FeedEntry.COLUMN_NAME_PICTURE_LOCATIION,URL_PICTURE);

        long recordId = database.insert(
                FeedEntry.TABLE_NAME,
                null,
                values);
        if (recordId > 0){
            Log.d(TAG, "saveRecord: Record saved");
            Toast.makeText(this, "Record SAVED", Toast.LENGTH_SHORT).show();
        }else {
            Log.d(TAG, "saveRecord: Record not saved");
            Toast.makeText(this, "Record NOT SAVED", Toast.LENGTH_SHORT).show();
        }
    }
//___________________________________________________________________________________________


    public void getRandomUser(View view){

        getRandomJSON();
    }
//__________________________________________________________________________________________________

    public void saveUser(View view) {


        saveProfile();
        //Bitmap image = ((BitmapDrawable)userPictureIV.getDrawable()).getBitmap();
        //saveImageToExternalStorage(image);
        readAllProfiles();
    }
//__________________________________________________________________________________________________

    public void getProfileDetails(View view) {
        String value = searchRecordET.getText().toString();
        Intent intent = new Intent(MainActivity.this, ProflieDetailsActivity.class);
        intent.putExtra(SEARCH_RECORD, value);
        startActivity(intent);
    }


//__________________________________________________________________________________________________

}
