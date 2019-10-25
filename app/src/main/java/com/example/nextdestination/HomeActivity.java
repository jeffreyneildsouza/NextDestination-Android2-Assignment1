package com.example.nextdestination;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import android.Manifest;
import android.app.Notification;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RemoteViews;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import static com.example.nextdestination.Notification.CHANNEL_1_ID;

public class HomeActivity extends AppCompatActivity implements
        FetchAddressTask.OnTaskCompleted{

    private static final int REQUEST_LOCATION_PERMISSION = 1;
    private static final String TAG = "Location";
    private Button button_location;
    private Button button_viewOnMap;

    private Location mLastLocation;
    private TextView textview_lastLocation, textView_destination;
    private FusedLocationProviderClient mFusedLocationClient;

    Geocoder geocoder;

    Double latitude, longitude;
    public static final double minLat = -90.00;
    public static final double maxLat = 90.00;
    public static double minLon = 0.00;
    public static double maxLon = 180.00;

    String lastLocation, final_destination;
    String history[]  = {"", "", "", "", "", ""};

    private NotificationManagerCompat notificationManager;


    /*-------*/
    public static final String SHARED_PREFS = "prefs";
    public static final String KEY_BUTTON_TEXT = "keyButtonText";

    private int appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {


        super.onCreate(savedInstanceState);


        setContentView(R.layout.activity_home);
        button_location = findViewById(R.id.button_location);
        textview_lastLocation = findViewById(R.id.textview_lastLocation);
        textView_destination = findViewById(R.id.textView_destination);
        button_viewOnMap = findViewById(R.id.button_viewMap);


        notificationManager = NotificationManagerCompat.from(this);



        button_location.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getLocation();
                latitude = minLat + (double)(Math.random() * ((maxLat - minLat) + 1));
                double roundOff = Math.round(latitude * 100.00) / 100.00;
                latitude = roundOff;
                longitude = minLon + (double)(Math.random() * ((maxLon - minLon) + 1));
                roundOff = Math.round(longitude * 100.00) / 100.00;
                longitude = roundOff;
                String destination = getCompleteAddressString(latitude, longitude);
                textView_destination.setText(destination);
                final_destination = destination;



                notification();
                //confirmConfiguration();



            }
        });


        button_viewOnMap.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                Intent map = new Intent(HomeActivity.this, MapsActivity.class);
                Bundle a = new Bundle();
                a.putDouble("Latitude", latitude);
                map.putExtras(a);
                Bundle b = new Bundle();
                b.putDouble("Longitude", longitude);
                map.putExtras(b);
                startActivity(map);
            }
        });
        //initialize the fused location client provider
        mFusedLocationClient =
                LocationServices.getFusedLocationProviderClient(this);


        /*---widget---


        Intent configIntent = getIntent();
        Bundle extras = configIntent.getExtras();
        if (extras != null) {
            appWidgetId = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID,
                    AppWidgetManager.INVALID_APPWIDGET_ID);
        }

        Intent resultValue = new Intent();
        resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        setResult(RESULT_CANCELED, resultValue);

        if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish();
        }
        */


    }

    /*
    public void confirmConfiguration() {
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);

        Intent intent = new Intent(this, MapsActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);

        String widget_text = final_destination;

        RemoteViews views = new RemoteViews(this.getPackageName(), R.layout.layout_widget);
        views.setOnClickPendingIntent(R.id.widget_destination_TextView, pendingIntent);
        views.setCharSequence(R.id.widget_destination_TextView, "setText", widget_text);

        appWidgetManager.updateAppWidget(appWidgetId, views);

        SharedPreferences prefs = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(KEY_BUTTON_TEXT + appWidgetId, widget_text);
        editor.apply();

        Intent resultValue = new Intent();
        resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        setResult(RESULT_OK, resultValue);
        finish();
    }


     */
    public void getLocation(){

        if(ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this,new String[]
                            {Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_LOCATION_PERMISSION);

        }else{
            // Log.d(TAG,"getLocation: permissions granted");
            mFusedLocationClient.getLastLocation().addOnSuccessListener(
                    new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            if(location != null){
                                //mLastLocation = location;
                                new FetchAddressTask(
                                        HomeActivity.this
                                        ,HomeActivity.this)
                                        .execute(location);



                            }else{
                                textview_lastLocation.setText(R.string.no_location);
                            }
                        }
                    }
            );

        }

        //show some loading text while the
        // FetchAddressTask runs in the background
        textview_lastLocation.setText(getString(R.string.address_text,
                getString(R.string.loading),
                System.currentTimeMillis()));

        final_destination = textview_lastLocation.getText().toString();




    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions, @NonNull int[] grantResults){
        switch(requestCode){
            case REQUEST_LOCATION_PERMISSION:
                //if a permission is granted, get the location,
                // otherwise show a message
                if(grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    getLocation();
                }else{
                    Toast.makeText(this,
                            R.string.location_permission_denied,
                            Toast.LENGTH_SHORT).show();
                }
                break;


        }
    }

    @Override
    public void onTaskCompleted(String result) {
        //update our UI
        textview_lastLocation.setText(getString(R.string.address_text
                ,result
                ,System.currentTimeMillis()));
    }



    private String getCompleteAddressString(double LATITUDE, double LONGITUDE) {
        String destination = "";
        geocoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(LATITUDE, LONGITUDE, 1);
            if (addresses != null) {
                Address returnedAddress = addresses.get(0);
                StringBuilder strReturnedAddress = new StringBuilder("");

                for (int i = 0; i <= returnedAddress.getMaxAddressLineIndex(); i++) {
                    strReturnedAddress.append(returnedAddress.getAddressLine(i)).append("\n");
                }
                destination = strReturnedAddress.toString();
                Log.w("Destination address", strReturnedAddress.toString());
            } else {
                Log.w("Destination address", "No Address returned!");
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.w("Destination address", "Canont get Address!");
        }
        List<String> list = new LinkedList<String>(Arrays.asList(history));
        list.add(0, destination);
        history = list.toArray(new String[list.size()]);
        return destination;
    }

    public void notification() {
        String title = "Your new destination is set!";
        String message = "Destination - " + final_destination;

        Notification notification = new NotificationCompat.Builder(this, CHANNEL_1_ID)
                .setSmallIcon(R.drawable.ic_destination)
                .setContentTitle(title)
                .setContentText(message)
                .setStyle(new NotificationCompat.InboxStyle()
                        .addLine(history[1])
                        .addLine(history[2])
                        .addLine(history[3])
                        .addLine(history[4])
                        .addLine(history[5])
                        .setBigContentTitle("Destination History")
                        .setSummaryText("Summary"))
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .build();

        notificationManager.notify(2, notification);
    }
}
