package com.udl.bss.barbershopschedule;

import android.Manifest;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.udl.bss.barbershopschedule.database.Users.BarbersSQLiteHelper;
import com.udl.bss.barbershopschedule.database.Users.UsersSQLiteHelper;
import com.udl.bss.barbershopschedule.utils.BitmapUtils;

public class RegisterActivity extends AppCompatActivity
        implements AdapterView.OnItemSelectedListener,
        GoogleApiClient.OnConnectionFailedListener,
        GoogleApiClient.ConnectionCallbacks {

    private static final int PLACE_PICKER_REQUEST = 2;

    private static final int PERMISSION_EXTERNAL_STORAGE = 1;
    private static final int PICK_IMAGE = 1;

    private GoogleApiClient mGoogleApiClient;

    private boolean isBarber;
    private ImageView imageView;
    private EditText et_name, et_pass, et_mail;
    private TextView textViewFormImage;
    private Bitmap bitmap;
    private Button btn_img;
    private Button btn_placesID;
    private String placesID;
    private byte[] image;

    private long id;
    private SharedPreferences sharedPreferences;
    private BarbersSQLiteHelper bsh;
    private UsersSQLiteHelper ush;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        buildGoogleApiClient();

        sharedPreferences = getSharedPreferences("my_preferences", Context.MODE_PRIVATE);
        bsh = new BarbersSQLiteHelper(getApplicationContext(), "DBBarbers", null, 1);
        ush = new UsersSQLiteHelper(getApplicationContext(), "DBUsers", null, 1);

        et_name = findViewById(R.id.editText_register_name);
        et_mail = findViewById(R.id.editText_register_mail);
        et_pass = findViewById(R.id.editText_register_password);
        btn_img = findViewById(R.id.button_form_image);
        Button btn_ok = findViewById(R.id.button_register_ok);
        btn_placesID = findViewById(R.id.button_selectPlacesID);
        imageView = findViewById(R.id.image_form);
        textViewFormImage = findViewById(R.id.textView_form_image);

        Spinner spinner = findViewById(R.id.mode_spinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this, R.array.spinner_items, android.R.layout.simple_spinner_item
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(this);


        btn_img.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(ContextCompat.checkSelfPermission(getApplicationContext(),
                        Manifest.permission.READ_EXTERNAL_STORAGE) ==
                        PackageManager.PERMISSION_DENIED) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                                PERMISSION_EXTERNAL_STORAGE);
                    }
                } else {
                    Intent pickIntent = new Intent(Intent.ACTION_PICK,
                            MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    pickIntent.setType("image/*");
                    startActivityForResult(pickIntent, PICK_IMAGE);
                }
            }
        });

        btn_ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveToDatabase();
            }
        });

        btn_placesID.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                displayPlacePicker();
            }
        });

    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        //Toast.makeText(this, "", Toast.LENGTH_SHORT).show();
        String item = parent.getItemAtPosition(position).toString();
        if (item.equals(getString(R.string.user))) {
            isBarber = false;
            btn_placesID.setVisibility(View.INVISIBLE);
            btn_img.setVisibility(View.INVISIBLE);
            textViewFormImage.setVisibility(View.INVISIBLE);
        } else if (item.equals(getString(R.string.barber))) {
            isBarber = true;
            btn_placesID.setVisibility(View.VISIBLE);
            btn_img.setVisibility(View.VISIBLE);
            textViewFormImage.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    private boolean registerOk () {
        if (isBarber){
            return et_name != null && et_mail != null && et_pass != null && image != null
                    && placesID != null && !et_name.getText().toString().equals("")
                    && !et_pass.toString().equals("") && !et_mail.getText().toString().equals("")
                    && bitmap != null;
        }

        return et_name != null && et_mail != null && et_pass != null
                && !et_name.getText().toString().equals("") && !et_pass.toString().equals("")
                && !et_mail.getText().toString().equals("");

    }

    private void saveToDatabase () {

        if (registerOk()) {

            ContentValues data = new ContentValues();
            data.put("name", et_name.getText().toString());
            data.put("mail", et_mail.getText().toString());
            data.put("password", et_pass.getText().toString());


            if (isBarber) {
                byte[] image = BitmapUtils.bitmapToByteArray(bitmap);
                data.put("image", image);
                data.put("placesID", placesID);
            }

            save(data);

            Toast.makeText(getApplicationContext(), "Registered succesfully", Toast.LENGTH_SHORT).show();

            String mode = isBarber ? getString(R.string.barber) : getString(R.string.user);
            saveToSharedPreferencesAndStart((int)id, et_name.getText().toString(), mode);

        } else {
            Toast.makeText(getApplicationContext(), getString(R.string.field_error), Toast.LENGTH_SHORT).show();
        }

    }

    private void save (final ContentValues data) {
        SQLiteDatabase db;

        if (isBarber) {
            db = bsh.getWritableDatabase();
            id = db.insert("Barbers", null, data);
        } else {
            db = ush.getWritableDatabase();
            id = db.insert("Users", null, data);
        }

    }

    private void saveToSharedPreferencesAndStart (int id, String name, String mode) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt("id", id);
        editor.putString("user", name);
        editor.putString("mode", mode);
        editor.apply();

        //startActivity(new Intent(getApplicationContext(), HomeActivity.class));
        finish();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);

        switch(requestCode) {
            case PICK_IMAGE:
                if(resultCode == Activity.RESULT_OK){
                    Uri selectedImage = intent.getData();
                    String[] filePathColumn = {MediaStore.Images.Media.DATA};

                    Cursor cursor = getContentResolver().query(selectedImage, filePathColumn, null, null, null);
                    if (cursor != null) {
                        cursor.moveToFirst();

                        int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                        String filePath = cursor.getString(columnIndex);
                        cursor.close();


                        bitmap = BitmapFactory.decodeFile(filePath);
                        if (BitmapUtils.sizeOfBitmap(bitmap) > 9999999) bitmap = BitmapUtils.reduceSize(bitmap);

                        imageView.setImageBitmap(bitmap);
                        image = BitmapUtils.bitmapToByteArray(bitmap);
                    }
                }
                break;

            case PLACE_PICKER_REQUEST:
                if(resultCode == RESULT_OK && intent != null){
                    placesID = PlacePicker.getPlace(this, intent).getId();
                }
                break;
        }
    }



    private void displayPlacePicker() {
        if( mGoogleApiClient == null || !mGoogleApiClient.isConnected() )
            return;

        PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();

        try {
            startActivityForResult( builder.build( this ), PLACE_PICKER_REQUEST );
        } catch ( GooglePlayServicesRepairableException e ) {
            Log.d( "PlacesAPI Demo", "GooglePlayServicesRepairableException thrown" );
        } catch ( GooglePlayServicesNotAvailableException e ) {
            Log.d( "PlacesAPI Demo", "GooglePlayServicesNotAvailableException thrown" );
        }
    }

    private void buildGoogleApiClient(){
        mGoogleApiClient = new GoogleApiClient
                .Builder( this )
                .enableAutoManage( this, 0, this )
                .addApi( Places.GEO_DATA_API )
                .addApi( Places.PLACE_DETECTION_API )
                .addConnectionCallbacks( this )
                .addOnConnectionFailedListener( this )
                .build();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if( mGoogleApiClient != null )
            mGoogleApiClient.connect();
    }

    @Override
    protected void onStop() {
        if( mGoogleApiClient != null && mGoogleApiClient.isConnected() ) {
            mGoogleApiClient.disconnect();
        }
        super.onStop();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.i("ON CONNECTION FAILED","Google Places API connection failed with error code:");
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

}