package com.topdrivers.driverv2.Services;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.PowerManager;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.topdrivers.driverv2.Activity.MainActivity;
import com.topdrivers.driverv2.BuildConfig;
import com.topdrivers.driverv2.Helper.SharedHelper;
import com.topdrivers.driverv2.Helper.URLHelper;
import com.topdrivers.driverv2.R;
import com.topdrivers.driverv2.TopdriversApplication;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class LocationShareService extends Service {

    private static final String TAG = LocationShareService.class.getSimpleName();
    String NOTIFICATION_CHANNEL_ID = "com.topdrivers.provider.Services";

    private int UPDATE_INTERVAL_IN_MILLISECONDS = 5000;
    private int FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS = UPDATE_INTERVAL_IN_MILLISECONDS / 2;
    private LocationRequest locationRequest = null;
    private LocationSettingsRequest mLocationSettingsRequest = null;
    private LocationCallback mLocationCallback = null;
    private FusedLocationProviderClient mFusedLocationClient = null;
    private SettingsClient mSettingsClient = null;
    private NotificationManager manager = null;
    private String token;
    private Binder mBinder = new MyBinder();
    private Notification notification;
    private Handler mServiceHandler = null;
    private PowerManager.WakeLock wakeLock = null;
    @Override
    public IBinder onBind(Intent intent) {
        try {
            stopForeground(true);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return mBinder;
    }

    @Override
    public void onRebind(Intent intent) {
        try {
            stopForeground(true);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        super.onRebind(intent);

    }

    @Override
    public boolean onUnbind(Intent intent) {
        try {


            startForeground(2, notification);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return super.onUnbind(intent);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        return Service.START_STICKY;
    }

    @SuppressLint("InvalidWakeLockTag")
    @Override
    public void onCreate() {
        super.onCreate();
        token = SharedHelper.getKey(this, "access_token");
        HandlerThread handlerThread =new  HandlerThread(TAG);
        handlerThread.start();
        mServiceHandler =new Handler(handlerThread.getLooper());
        createNotification();
        buildSettingRequest();
        createLocationCallback();
        PowerManager powerManager = (PowerManager)getSystemService(Context.POWER_SERVICE);
        wakeLock     = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "TopDriver");
        wakeLock.acquire();

    }

    private void buildSettingRequest() {
        try {
            mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
            mSettingsClient = LocationServices.getSettingsClient(this);
            locationRequest = LocationRequest.create();
            locationRequest.setInterval(UPDATE_INTERVAL_IN_MILLISECONDS);
            locationRequest.setFastestInterval(FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS);
            locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
            LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
            builder.addLocationRequest(locationRequest);
            mLocationSettingsRequest = builder.build();
            try {
                mSettingsClient.checkLocationSettings(mLocationSettingsRequest)
                        .addOnSuccessListener(new OnSuccessListener<LocationSettingsResponse>() {
                            @Override
                            public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                                if (ActivityCompat.checkSelfPermission(LocationShareService.this,
                                        Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                                    stopSelf();
                                    return;
                                }
                                mFusedLocationClient.requestLocationUpdates(locationRequest,
                                        mLocationCallback, Looper.myLooper());
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, e.toString());
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void createLocationCallback() {
        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);
                Log.d(TAG, "locationResult" + locationResult);
                if (locationResult.getLastLocation() != null)
                    updateLocation(locationResult.getLastLocation());
            }
        };
    }

    private void updateLocation(Location mLastLocation) {
        Log.d(TAG, "Latitude:" + mLastLocation.getLatitude());
        Log.d(TAG, "Longitude" + mLastLocation.getLongitude());
        String url = URLHelper.base + "api/provider/trip?latitude=" + mLastLocation.getLatitude() +
                "&longitude=" + mLastLocation.getLongitude();
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        if (BuildConfig.DEBUG)
                            Log.d(TAG, "Location updated successfully");
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (error != null && error.getMessage() != null)
                    Log.e(TAG, error.getMessage());
            }
        }) {
            @Override
            public Map<String, String> getHeaders() {
                HashMap<String, String> headers = new HashMap<>();
                headers.put("X-Requested-With", "XMLHttpRequest");
                headers.put("Authorization", "Bearer " + token);
                return headers;
            }
        };
        TopdriversApplication.getInstance().addToRequestQueue(request);
    }

    private void createNotification() {
        Log.d(TAG, "CreateNotification");
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.O) {
            try {
                String channelName = "Current location sharing";
                NotificationChannel channel = new NotificationChannel(NOTIFICATION_CHANNEL_ID,
                        channelName, NotificationManager.IMPORTANCE_HIGH);
                channel.setLightColor(Color.BLUE);
                channel.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
                manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                assert manager != null;
                manager.createNotificationChannel(channel);
                notification = new NotificationCompat.Builder(this,
                        NOTIFICATION_CHANNEL_ID)
                        .setOngoing(true)
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setContentTitle(getString(R.string.app_name) + "- Current location " +
                                "sharing")
                        .setPriority(NotificationManager.IMPORTANCE_MAX)
                        .setCategory(Notification.CATEGORY_SERVICE)
                        .setOngoing(true)
                        .setAutoCancel(false)
                        .setContentIntent(PendingIntent.getActivity(this, 0, new Intent(), 0))
                        .build();
                notification.flags = Notification.FLAG_ONGOING_EVENT;
                startForeground(2, notification);
            } catch (Exception e) {
            }

        } else {
            Intent notificationIntent = new Intent(this, MainActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
                    notificationIntent, 0);

            notification = new NotificationCompat.Builder(this,
                    NOTIFICATION_CHANNEL_ID)
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setColor(ContextCompat.getColor(this, R.color.colorPrimaryDark))
                    .setContentTitle(getString(R.string.app_name))
                    .setContentText(getString(R.string.app_name) + "- Current location sharing")
                    .setContentIntent(pendingIntent).build();
            startForeground(2, notification);
        }
    }

    private void onStopUpdate() {
        try {
            mFusedLocationClient.removeLocationUpdates(mLocationCallback);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy");
        stopForeground(true);
        onStopUpdate();
        super.onDestroy();
    }

    public class MyBinder extends Binder {

        public LocationShareService getService() {
            return LocationShareService.this;

        }
    }
}
