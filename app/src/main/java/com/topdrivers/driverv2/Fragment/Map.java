package com.topdrivers.driverv2.Fragment;

import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.NotificationManager;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.LayerDrawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.media.MediaPlayer;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.SystemClock;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkError;
import com.android.volley.NetworkResponse;
import com.android.volley.NoConnectionError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.material.snackbar.Snackbar;
import com.squareup.picasso.Picasso;
import com.topdrivers.driverv2.Activity.MainActivity;
import com.topdrivers.driverv2.Activity.RideAcceptandRejectActivity;
import com.topdrivers.driverv2.Activity.ShowProfile;
import com.topdrivers.driverv2.Activity.WaitingForApproval;
import com.topdrivers.driverv2.Activity.WelcomeScreenActivity;
import com.topdrivers.driverv2.Helper.ConnectionHelper;
import com.topdrivers.driverv2.Helper.CustomDialog;
import com.topdrivers.driverv2.Helper.DataParser;
import com.topdrivers.driverv2.Helper.SharedHelper;
import com.topdrivers.driverv2.Helper.URLHelper;
import com.topdrivers.driverv2.Helper.User;
import com.topdrivers.driverv2.R;
import com.topdrivers.driverv2.Retrofit.ApiInterface;
import com.topdrivers.driverv2.Retrofit.RetrofitClient;
import com.topdrivers.driverv2.Services.CheckScheduleService;
import com.topdrivers.driverv2.Services.CustomFloatingViewService;
import com.topdrivers.driverv2.Services.FloatingViewService;
import com.topdrivers.driverv2.Services.LocationShareService;
import com.topdrivers.driverv2.TopdriversApplication;
import com.topdrivers.driverv2.Utilities.Utilities;
import com.topdrivers.driverv2.lib.KalmanLocationManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;

import static android.content.Context.JOB_SCHEDULER_SERVICE;
import static android.content.Context.NOTIFICATION_SERVICE;
import static com.topdrivers.driverv2.TopdriversApplication.trimMessage;

public class Map extends Fragment implements OnMapReadyCallback, LocationListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, View.OnClickListener,
        GoogleMap.OnCameraMoveListener {

    public static final int CHECK_SCHEDULE_JOB_ID = 12;
    public static final int REQUEST_LOCATION = 1450;
    private static final String TAG = Map.class.getSimpleName();
    private static final int CODE_DRAW_OVER_OTHER_APP_PERMISSION = 2084;
    /**
     * Request location updates with the highest possible frequency on gps.
     * Typically, this means one update per second for gps.
     */
    private static final long GPS_TIME = 1000;
    /**
     * For the network provider, which gives locations with less accuracy (less reliable),
     * request updates every 5 seconds.
     */
    private static final long NET_TIME = 5000;
    /**
     * For the filter-time argument we use a "real" value: the predictions are triggered by a timer.
     * Lets say we want 5 updates (estimates) per second = update each 200 millis.
     */

    private boolean isBackground = false;
    private boolean openAcceptScreen = false;
    private boolean openArrivedScreen = false;
    private boolean isFromResume = false;

    private static final long FILTER_TIME = 200;
    public static SupportMapFragment mapFragment = null;
    public Handler ha;
    String isPaid = "";
    public String myLat = "";
    public String myLng = "";
    String CurrentStatus = " ";
    String PreviousStatus = " ";
    public String request_id = " ";
    String givenTotalAmount = "0";
    int method;
    Activity activity;
    Context context;
    CountDownTimer countDownTimer;
    int value = 0;
    AlertDialog cancelDialog;
    android.app.AlertDialog cancelReasonDialog;
    Marker currentMarker;
    GoogleApiClient mGoogleApiClient;
    LocationRequest mLocationRequest;
    ParserTask parserTask;
    ImageView imgCurrentLoc;
    boolean normalPlay = false;
    String s_address = "", d_address = "";
    //content layout 01
    TextView txt01Pickup;
    TextView txt01Timer;
    ImageView img01User;
    TextView txt01UserName;
    TextView txtSchedule;
    RatingBar rat01UserRating;
    //content layer 02
    ImageView img02User;
    TextView txt02UserName;
    RatingBar rat02UserRating;
    TextView txt02ScheduledTime;
    TextView txt02From;
    TextView txt02To;
    TextView topSrcDestTxtLbl;
    //content layer 03
    ImageView img03User;
    ImageView img04User;
    TextView txt03UserName;
    TextView txt04UserName;
    RatingBar rat03UserRating;
    RatingBar rat04UserRating;
    ImageView img03Call;
    ImageView img03Status1;
    ImageView img03Status2;
    ImageView img03Status3;
    //content layer 04
    TextView txt04InvoiceId;
    TextView txt04BasePrice;
    TextView txt04Distance;
    TextView txt04Tax;
    TextView txt04Total;
    TextView txt04AmountToPaid;
    TextView txt04PaymentMode;
    TextView txt04Commision;
    TextView lblProviderName;
    ImageView paymentTypeImg;
    TextView amountPaid;
    //content layer 05
    ImageView img05User;
    RatingBar rat05UserRating;
    EditText edt05Comment, given_total_amount;
    //Button layer 01
    Button btn_01_status, btn_confirm_payment, btn_rate_submit;
    Button btn_go_offline;
    LinearLayout lnrGoOffline;
    //Button layer 02
    Button btn_02_accept;
    Button btn_02_reject;
    Button btn_cancel_ride;
    //map layout
    LinearLayout ll_01_mapLayer;
    //content layout
    LinearLayout ll_01_contentLayer_accept_or_reject_now;
    LinearLayout ll_02_contentLayer_accept_or_reject_later;
    LinearLayout ll_03_contentLayer_service_flow;
    LinearLayout ll_04_contentLayer_payment;
    LinearLayout ll_05_contentLayer_feedback;
    LinearLayout errorLayout;
    //menu icon
    ImageView menuIcon;
    int NAV_DRAWER = 0;
    DrawerLayout drawer;
    Utilities utils = new Utilities();
    MediaPlayer mPlayer;
    ImageView imgNavigationToSource;
    String crt_lat = "", crt_lng = "";
    boolean timerCompleted = false;
    TextView destination;
    ConnectionHelper helper;
    LinearLayout destinationLayer;
    View view;
    boolean doubleBackToExitPressedOnce = false;
    //Animation
    Animation slide_down, slide_up;
    //Distance calculation
    TextView lblDistanceTravelled;
    boolean scheduleTrip = false;
    boolean showBatteryAlert = true;
    private TextView paymentModeText;
    private TextView pickupDistanceText;
    private androidx.appcompat.app.AlertDialog viewDialog;
    private String token;
    //map variable
    private GoogleMap mMap;
    private double srcLatitude = 0;
    private double srcLongitude = 0;
    private double destLatitude = 0;
    private double destLongitude = 0;
    private LatLng sourceLatLng;
    private LatLng destLatLng;
    private LatLng currentLatLng;
    private String bookingId;
    public static String address;
    public static User user = new User();
    private ImageView sos;
    //Button layout
    private CustomDialog customDialog;
    private Object previous_request_id = " ";
    private String count;
    private JSONArray statusResponses;
    private String feedBackRating;
    private String feedBackComment;
    private KalmanLocationManager mKalmanLocationManager;
    private LatLng oldPosition = null;
    private LatLng newPosition = null;
    private Marker sourceMarker;
    private Marker destinationMarker;
    private Polyline polyline;
    private boolean isPickupDirectionFetched;
    private boolean isDropDirectionFetched;
    private Location mLocation;
    private boolean cardConfirm = false;
    private Runnable runnable;
    // private JsonObjectRequest jsonObjectRequest;

    public Map() {

    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context = context;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        if (activity == null) {
            activity = getActivity();
        }
        if (context == null) {
            context = getContext();
        }

        mKalmanLocationManager = new KalmanLocationManager(context);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        if (view == null) {
            view = inflater.inflate(R.layout.fragment_map, container, false);
        }
        if (activity == null) {
            activity = getActivity();
        }
        if (context == null) {
            context = getContext();
        }
        findViewById(view);
        if (SharedHelper.getKey(context, "access_token_payment").length() > 0)
            getToken("no");
        //confirmAmount("DROPPED", request_id, "10");

        token = SharedHelper.getKey(context, "access_token");
        helper = new ConnectionHelper(context);

        //permission to access location
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // Android M Permission check
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        } else {
            setUpMapIfNeeded();
            MapsInitializer.initialize(activity);
        }

        ha = new Handler();
        update("COMPLETED", "1021");
        btn_01_status.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                update(CurrentStatus, request_id);
            }
        });
        btn_confirm_payment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (cardConfirm) {
                    payNow();
                  /* setValuesTo_ll_05_contentLayer_feedback(statusResponses);
                    if (ll_05_contentLayer_feedback.getVisibility() == View.GONE) {
                        ll_05_contentLayer_feedback.startAnimation(slide_up);
                    }
                    edt05Comment.setText("");
                    ll_05_contentLayer_feedback.setVisibility(View.VISIBLE);
                    sos.setVisibility(View.GONE);
                    destinationLayer.setVisibility(View.GONE);
                    btn_01_status.setText(context.getResources().getString(R.string.rate_user));
                    CurrentStatus = "RATE";
                    cardConfirm = false;*/
                } else {
                    if (CurrentStatus.equals("DROPPED"))
                        givenTotalAmount = given_total_amount.getText().toString();

                    update(CurrentStatus, request_id);
                }
            }
        });

        btn_rate_submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                update(CurrentStatus, request_id);
            }
        });

        btn_go_offline.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //  update(CurrentStatus, request_id);
//                goOffLine("ONLINE", request_id);
                Uri uri = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE + "://" + getContext().getPackageName() + "/" + R.raw.driver_disconnected);
                Ringtone ringtone = RingtoneManager.getRingtone(getContext(), uri);
                ringtone.play();
                TopdriversApplication.getInstance().getRequestQueue().cancelAll("checkStatus");
                offLine();
            }
        });

        errorLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        imgCurrentLoc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Double crtLat, crtLng;
                if (!crt_lat.equalsIgnoreCase("") && !crt_lng.equalsIgnoreCase("")) {
                    crtLat = Double.parseDouble(crt_lat);
                    crtLng = Double.parseDouble(crt_lng);
                    if (crtLat != null && crtLng != null) {
                        LatLng loc = new LatLng(crtLat, crtLng);
                        CameraPosition cameraPosition =
                                new CameraPosition.Builder().target(loc).zoom(16).build();
                        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
                    }
                }
            }
        });

        btn_02_accept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO Lan debug
                countDownTimer.cancel();
                if (mPlayer != null && mPlayer.isPlaying()) {
                    /*mPlayer.stop();
                    mPlayer = null;*/
                    stopPlaying();
                }
                handleIncomingRequest("Accept", request_id);
            }
        });


        btn_02_reject.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                countDownTimer.cancel();
                if (mPlayer != null && mPlayer.isPlaying()) {
                   /* mPlayer.stop();
                    mPlayer = null;*/
                    System.out.println("------------playng----------");
                    stopPlaying();
                }


                String rejectCount = SharedHelper.getKey(context, "refuses");
                if (rejectCount != null && rejectCount.equalsIgnoreCase("1")) {
                 /*   TopdriversApplication.getInstance().getRequestQueue().cancelAll(new RequestQueue.RequestFilter() {
                        @Override
                        public boolean apply(Request<?> request) {
                            return true;
                        }
                    });*/
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            goOffline();
                        }
                    }, 5000);

                } else {
                    // handleIncomingRequest("Reject", request_id);
                    SharedHelper.putKey(context, "refuses", "1");


                }
                handleIncomingRequestReject("Reject", request_id);
            }
        });

        btn_cancel_ride.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showCancelDialog();
            }
        });

        menuIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    if (NAV_DRAWER == 0) {
                        drawer.openDrawer(GravityCompat.START);
                    } else {
                        NAV_DRAWER = 0;
                        drawer.closeDrawers();
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        img03Call.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String mobile = SharedHelper.getKey(context, "provider_mobile_no");
                if (mobile != null && !mobile.equalsIgnoreCase("null") && mobile.length() > 0) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        requestPermissions(new String[]{Manifest.permission.CALL_PHONE}, 2);
                    } else {
                        Intent intent = new Intent(Intent.ACTION_CALL);
                        intent.setData(Uri.parse("tel:" + SharedHelper.getKey(context,
                                "provider_mobile_no")));
                        startActivity(intent);
                    }
                } else {
                    displayMessage(context.getResources().getString(R.string.user_no_mobile));
                }
            }
        });

        imgNavigationToSource.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /*if (btn_01_status.getText().toString().equalsIgnoreCase("ARRIVED")) {
                    Uri naviUri = Uri.parse("http://maps.google.com/maps?f=d&hl=en&daddr=" +
                    s_address);
                    Intent intent = new Intent(Intent.ACTION_VIEW, naviUri);
                    intent.setClassName("com.google.android.apps.maps", "com.google.android.maps
                    .MapsActivity");
                    startActivity(intent);
                } else {
                    Uri naviUri2 = Uri.parse("http://maps.google.com/maps?f=d&hl=en&saddr=" +
                    s_address + "&daddr=" + d_address);
                    Intent intent = new Intent(Intent.ACTION_VIEW, naviUri2);
                    intent.setClassName("com.google.android.apps.maps", "com.google.android.maps
                    .MapsActivity");
                    startActivity(intent);
                }*/


                String uri = "";

                if (btn_01_status.getText().toString().equalsIgnoreCase("ARRIVED")) {
                    uri = "geo: " + sourceLatLng.latitude + "," + sourceLatLng.longitude;
                } else {
                    uri = "geo: " + destLatLng.latitude + "," + destLatLng.longitude;
                }

//                uri = "geo: "+sourceLatLng.latitude+","+sourceLatLng.longitude;
                Uri naviUri2 =
                        Uri.parse("http://maps.google.com/maps?f=d&hl=en&saddr=" + s_address +
                                "&daddr=" + d_address);
                startActivity(new Intent(android.content.Intent.ACTION_VIEW,
                        naviUri2));


                //Check if the application has draw over other apps permission or not?
                //This permission is by default available for API<23. But for API > 23
                //you have to ask for the permission in runtime.
//                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays
//                (context)) {
//                    //If the draw over permission is not available open the settings screen
//                    //to grant the permission.
//                    Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
//                            Uri.parse("package:" + context.getPackageName()));
//                    startActivityForResult(intent, CODE_DRAW_OVER_OTHER_APP_PERMISSION);
//                } else {
//                    initializeView();
//                }
                showCustomFloatingView(context, true);
            }
        });
        statusCheck();
        return view;
    }

    private void stopPlaying() {
        if (mPlayer != null) {
            mPlayer.stop();
            mPlayer.release();
            mPlayer = null;
        }
    }

    public void statusCheck() {
        final LocationManager manager =
                (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);

        if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            enableLoc();
        }
    }

    private void enableLoc() {
        mGoogleApiClient = new GoogleApiClient.Builder(getActivity())
                .addApi(LocationServices.API)
                .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
                    @Override
                    public void onConnected(Bundle bundle) {

                    }

                    @Override
                    public void onConnectionSuspended(int i) {
                        mGoogleApiClient.connect();
                    }
                })
                .addOnConnectionFailedListener(new GoogleApiClient.OnConnectionFailedListener() {
                    @Override
                    public void onConnectionFailed(ConnectionResult connectionResult) {

                        utils.print("Location error",
                                "Location error " + connectionResult.getErrorCode());
                    }
                }).build();
        mGoogleApiClient.connect();

        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(30 * 1000);
        locationRequest.setFastestInterval(5 * 1000);
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest);

        builder.setAlwaysShow(true);

        PendingResult<LocationSettingsResult> result =
                LocationServices.SettingsApi.checkLocationSettings(mGoogleApiClient,
                        builder.build());
        result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
            @Override
            public void onResult(LocationSettingsResult result) {
                final Status status = result.getStatus();
                switch (status.getStatusCode()) {
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        try {
                            // Show the dialog by calling startResolutionForResult(),
                            // and check the result in onActivityResult().
                            status.startResolutionForResult(getActivity(), REQUEST_LOCATION);

                        } catch (NullPointerException | IntentSender.SendIntentException e) {
                            e.printStackTrace();
                            try {
                                status.startResolutionForResult(activity, REQUEST_LOCATION);
                            } catch (Exception ee) {
                                ee.printStackTrace();
                            }
                        }
                        break;
                }
            }
        });
//	        }

    }

    private void findViewById(View view) {
        //Menu Icon
        menuIcon = view.findViewById(R.id.menuIcon);
        imgCurrentLoc = view.findViewById(R.id.imgCurrentLoc);
        drawer = activity.findViewById(R.id.drawer_layout);
        amountPaid = view.findViewById(R.id.amountPaid);
        //map layer
        ll_01_mapLayer = view.findViewById(R.id.ll_01_mapLayer);

        //Button layer 01
        btn_01_status = view.findViewById(R.id.btn_01_status);
        btn_rate_submit = view.findViewById(R.id.btn_rate_submit);
        btn_confirm_payment = view.findViewById(R.id.btn_confirm_payment);

        //Button layer 02
        btn_02_accept = view.findViewById(R.id.btn_02_accept);
        btn_02_reject = view.findViewById(R.id.btn_02_reject);
        btn_cancel_ride = view.findViewById(R.id.btn_cancel_ride);
        btn_go_offline = view.findViewById(R.id.btn_go_offline);

//        Button btn_tap_when_arrived, btn_tap_when_pickedup,btn_tap_when_dropped,
//        btn_tap_when_paid, btn_rate_user
        //content layer
        ll_01_contentLayer_accept_or_reject_now =
                view.findViewById(R.id.ll_01_contentLayer_accept_or_reject_now);
        ll_02_contentLayer_accept_or_reject_later =
                view.findViewById(R.id.ll_02_contentLayer_accept_or_reject_later);
        ll_03_contentLayer_service_flow =
                view.findViewById(R.id.ll_03_contentLayer_service_flow);
        ll_04_contentLayer_payment =
                view.findViewById(R.id.ll_04_contentLayer_payment);
        //test
        ll_05_contentLayer_feedback =
                view.findViewById(R.id.ll_05_contentLayer_feedback);
        lnrGoOffline = view.findViewById(R.id.lnrGoOffline);
        imgNavigationToSource = view.findViewById(R.id.imgNavigationToSource);

        //content layout 01
        txt01Pickup = view.findViewById(R.id.txtPickup);
        paymentModeText = view.findViewById(R.id.tv_payment_mode);
        pickupDistanceText = view.findViewById(R.id.tv_pickup_distance);
        txt01Timer = view.findViewById(R.id.txt01Timer);
        img01User = view.findViewById(R.id.img01User);
        txt01UserName = view.findViewById(R.id.txt01UserName);
        txtSchedule = view.findViewById(R.id.txtSchedule);
        rat01UserRating = view.findViewById(R.id.rat01UserRating);
        sos = view.findViewById(R.id.sos);
        LayerDrawable drawable = (LayerDrawable) rat01UserRating.getProgressDrawable();
        drawable.getDrawable(0).setColorFilter(Color.GRAY, PorterDuff.Mode.SRC_ATOP);
        drawable.getDrawable(1).setColorFilter(Color.parseColor("#FFAB00"),
                PorterDuff.Mode.SRC_ATOP);
        drawable.getDrawable(2).setColorFilter(Color.parseColor("#FFAB00"),
                PorterDuff.Mode.SRC_ATOP);

        //content layer 02
        img02User = view.findViewById(R.id.img02User);
        txt02UserName = view.findViewById(R.id.txt02UserName);
        rat02UserRating = view.findViewById(R.id.rat02UserRating);
        LayerDrawable stars02 = (LayerDrawable) rat02UserRating.getProgressDrawable();
        stars02.getDrawable(2).setColorFilter(Color.YELLOW, PorterDuff.Mode.SRC_ATOP);
        txt02ScheduledTime = view.findViewById(R.id.txt02ScheduledTime);
        lblDistanceTravelled = view.findViewById(R.id.lblDistanceTravelled);
        txt02From = view.findViewById(R.id.txt02From);
        txt02To = view.findViewById(R.id.txt02To);

        //content layer 03
        img03User = view.findViewById(R.id.img03User);
        img04User = view.findViewById(R.id.img04User);
        txt03UserName = view.findViewById(R.id.txt03UserName);
        txt04UserName = view.findViewById(R.id.txt04UserName);
        rat03UserRating = view.findViewById(R.id.rat03UserRating);
        rat04UserRating = view.findViewById(R.id.rat04UserRating);
        LayerDrawable drawable_02 = (LayerDrawable) rat03UserRating.getProgressDrawable();
        drawable_02.getDrawable(0).setColorFilter(Color.GRAY, PorterDuff.Mode.SRC_ATOP);
        drawable_02.getDrawable(1).setColorFilter(Color.parseColor("#FFAB00"),
                PorterDuff.Mode.SRC_ATOP);
        drawable_02.getDrawable(2).setColorFilter(Color.parseColor("#FFAB00"),
                PorterDuff.Mode.SRC_ATOP);
        img03Call = view.findViewById(R.id.img03Call);
        img03Status1 = view.findViewById(R.id.img03Status1);
        img03Status2 = view.findViewById(R.id.img03Status2);
        img03Status3 = view.findViewById(R.id.img03Status3);

        //content layer 04
        txt04InvoiceId = view.findViewById(R.id.invoice_txt);
        txt04BasePrice = view.findViewById(R.id.txt04BasePrice);
        txt04Distance = view.findViewById(R.id.txt04Distance);
        txt04Tax = view.findViewById(R.id.txt04Tax);
        txt04Total = view.findViewById(R.id.txt04Total);
        txt04AmountToPaid = view.findViewById(R.id.txt04AmountToPaid);
        txt04PaymentMode = view.findViewById(R.id.txt04PaymentMode);
        txt04Commision = view.findViewById(R.id.txt04Commision);
        destination = view.findViewById(R.id.destination);
        lblProviderName = view.findViewById(R.id.lblProviderName);
        paymentTypeImg = view.findViewById(R.id.paymentTypeImg);
        errorLayout = view.findViewById(R.id.lnrErrorLayout);
        destinationLayer = view.findViewById(R.id.destinationLayer);
        given_total_amount = view.findViewById(R.id.given_total_amount);

        //content layer 05
        img05User = view.findViewById(R.id.img05User);
        rat05UserRating = view.findViewById(R.id.rat05UserRating);

        LayerDrawable stars05 = (LayerDrawable) rat05UserRating.getProgressDrawable();
        stars05.getDrawable(0).setColorFilter(Color.GRAY, PorterDuff.Mode.SRC_ATOP);
        stars05.getDrawable(1).setColorFilter(Color.parseColor("#FFAB00"),
                PorterDuff.Mode.SRC_ATOP);
        stars05.getDrawable(2).setColorFilter(Color.parseColor("#FFAB00"),
                PorterDuff.Mode.SRC_ATOP);
        edt05Comment = view.findViewById(R.id.edt05Comment);

        topSrcDestTxtLbl = view.findViewById(R.id.src_dest_txt);

        //Load animation
        slide_down = AnimationUtils.loadAnimation(getActivity(), R.anim.slide_down);
        slide_up = AnimationUtils.loadAnimation(getActivity(), R.anim.slide_up);


        view.setFocusableInTouchMode(true);
        view.requestFocus();
        view.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getAction() != KeyEvent.ACTION_DOWN)
                    return true;

                if (keyCode == KeyEvent.KEYCODE_BACK) {
                    if (doubleBackToExitPressedOnce) {
                        getActivity().finish();
                        return false;
                    }

                    doubleBackToExitPressedOnce = true;
                    Toast.makeText(getActivity(), "Please click BACK again to exit",
                            Toast.LENGTH_SHORT).show();

                    new Handler().postDelayed(new Runnable() {

                        @Override
                        public void run() {
                            doubleBackToExitPressedOnce = false;
                        }
                    }, 5000);
                    return true;
                }
                return false;
            }
        });

        sos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showSosDialog();
            }
        });

        destinationLayer.setOnClickListener(this);
        ll_01_contentLayer_accept_or_reject_now.setOnClickListener(this);
        ll_03_contentLayer_service_flow.setOnClickListener(this);
        ll_04_contentLayer_payment.setOnClickListener(this);
        ll_05_contentLayer_feedback.setOnClickListener(this);
        lnrGoOffline.setOnClickListener(this);
        errorLayout.setOnClickListener(this);

    }

    private void mapClear() {
        if (parserTask != null) {
            parserTask.cancel(true);
            parserTask = null;
        }

        if (!crt_lat.equalsIgnoreCase("") && !crt_lat.equalsIgnoreCase("")) {
            LatLng myLocation = new LatLng(Double.parseDouble(crt_lat),
                    Double.parseDouble(crt_lng));
            CameraPosition cameraPosition =
                    new CameraPosition.Builder().target(myLocation).zoom(17).build();
            mMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));


        }

        whenMapCleared();
        srcLatitude = 0;
        srcLongitude = 0;
        destLatitude = 0;
        destLongitude = 0;
    }

    private void whenMapCleared() {
        if (mMap != null)
            mMap.clear();
        isPickupDirectionFetched = false;
        isDropDirectionFetched = false;
        currentMarker = null;
        try {
            if (mMap != null) {
                currentMarker = mMap.addMarker(new MarkerOptions()
                        .position(oldPosition != null ? oldPosition : newPosition)
                        .anchor(0.5f, 0.75f)
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.provider_location_icon)));

            }
        } catch (Exception e) {
        }
    }

    public void clearVisibility() {

        try {
            if (ll_01_contentLayer_accept_or_reject_now.getVisibility() == View.VISIBLE) {
                ll_01_contentLayer_accept_or_reject_now.startAnimation(slide_down);
            } else if (ll_02_contentLayer_accept_or_reject_later.getVisibility() == View.VISIBLE) {
                ll_02_contentLayer_accept_or_reject_later.startAnimation(slide_down);
            } else if (ll_03_contentLayer_service_flow.getVisibility() == View.VISIBLE) {
                //ll_03_contentLayer_service_flow.startAnimation(slide_down);
            } else if (ll_04_contentLayer_payment.getVisibility() == View.VISIBLE) {
                ll_04_contentLayer_payment.startAnimation(slide_down);
            } else if (ll_04_contentLayer_payment.getVisibility() == View.VISIBLE) {
                ll_04_contentLayer_payment.startAnimation(slide_down);
            } else if (ll_05_contentLayer_feedback.getVisibility() == View.VISIBLE) {
                ll_05_contentLayer_feedback.startAnimation(slide_down);
            }

            ll_01_contentLayer_accept_or_reject_now.setVisibility(View.GONE);
            ll_02_contentLayer_accept_or_reject_later.setVisibility(View.GONE);
            ll_03_contentLayer_service_flow.setVisibility(View.GONE);
            ll_04_contentLayer_payment.setVisibility(View.GONE);
            ll_05_contentLayer_feedback.setVisibility(View.GONE);
            lnrGoOffline.setVisibility(View.GONE);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @TargetApi(Build.VERSION_CODES.M)
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        switch (requestCode) {
            case 1:
                if (grantResults.length > 0) {
                    if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                        // Permission Granted
//                        //Toast.makeText(SignInActivity.this, "PERMISSION_GRANTED", Toast
//                        .LENGTH_SHORT).show();
                        setUpMapIfNeeded();
                        MapsInitializer.initialize(activity);

                        /*mKalmanLocationManager.requestLocationUpdates(
                                KalmanLocationManager.UseProvider.GPS_AND_NET, FILTER_TIME,
                                GPS_TIME, NET_TIME, mLocationListener, true);*/

                        if (ContextCompat.checkSelfPermission(context,
                                Manifest.permission.ACCESS_FINE_LOCATION)
                                == PackageManager.PERMISSION_GRANTED) {

                            if (mGoogleApiClient == null) {
                                buildGoogleApiClient();
                            }
                            setUpMapIfNeeded();
                            MapsInitializer.initialize(activity);

                        }
                    } else {
                        requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}
                                , 1);
                    }
                }
                break;
            case 2:
                try {
                    if (grantResults.length > 0) {
                        if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                            // Permission Granted
                            Intent intent = new Intent(Intent.ACTION_CALL);
                            intent.setData(Uri.parse("tel:" + SharedHelper.getKey(context,
                                    "provider_mobile_no")));
                            startActivity(intent);
                        } else {
                            requestPermissions(new String[]{Manifest.permission.CALL_PHONE}, 2);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            case 3:
                if (grantResults.length > 0) {
                    if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                        // Permission Granted
                        Intent intent = new Intent(Intent.ACTION_CALL);
                        intent.setData(Uri.parse("tel:" + SharedHelper.getKey(context, "sos")));
                        startActivity(intent);
                    } else {
                        requestPermissions(new String[]{Manifest.permission.CALL_PHONE}, 3);
                    }
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }

       /* if (requestCode == HyperTrack.REQUEST_CODE_LOCATION_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0]
                    == PackageManager.PERMISSION_GRANTED) {
                // Check if Location Settings are enabled to proceed
//                checkForLocationSettings();

            } else {
                // Handle Location Permission denied error
                Toast.makeText(context, "Location Permission denied.", Toast.LENGTH_SHORT).show();
            }
        }*/

    }

    private void setUpMapIfNeeded() {
        if (mMap == null) {
            FragmentManager fm = getChildFragmentManager();
            mapFragment = ((SupportMapFragment) fm.findFragmentById(R.id.provider_map));
            mapFragment.getMapAsync(this);
        }
        if (mMap != null) {
            setupMap();
        }
    }

    private void setSourceLocationOnMap(LatLng latLng) {
        /*if (mMap != null){
            mMap.clear();
            if (latLng != null){
                CameraPosition cameraPosition = new CameraPosition.Builder().target(latLng).zoom
                (16).build();
                MarkerOptions options = new MarkerOptions().position(latLng).anchor(0.5f, 0.5f);
                options.position(latLng).isDraggable();
                mMap.addMarker(options);
                mMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
            }
        }*/
    }

    private void setPickupLocationOnMap() {
        Log.d(TAG, "setPickupLocationOnMap");
        try {
            String currentLatitude = SharedHelper.getKey(context, "current_lat");
            String currentLongitude = SharedHelper.getKey(context, "current_lng");
            if (!isPickupDirectionFetched && currentLatitude != null && currentLongitude != null) {
                isPickupDirectionFetched = true;
                sourceLatLng = new LatLng(Double.parseDouble(currentLatitude),
                        Double.parseDouble(currentLongitude));
                destLatLng = new LatLng(srcLatitude, srcLongitude);
                CameraPosition cameraPosition = new CameraPosition.Builder()
                        .target(destLatLng).zoom(17).build();
                MarkerOptions options = new MarkerOptions();
                options.position(destLatLng).isDraggable();
                mMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
                String url = getUrl(sourceLatLng.latitude, sourceLatLng.longitude,
                        destLatLng.latitude, destLatLng.longitude);
                FetchUrl fetchUrl = new FetchUrl();
                fetchUrl.execute(url);
            }
        } catch (Exception e) {
        }
    }

    private void setDestinationLocationOnMap() {
        Log.d(TAG, "setDestinationLocationOnMap");
        if (!isDropDirectionFetched) {
            isDropDirectionFetched = true;
            sourceLatLng = new LatLng(srcLatitude, srcLongitude);
            destLatLng = new LatLng(destLatitude, destLongitude);
            CameraPosition cameraPosition = new CameraPosition.Builder()
                    .target(destLatLng).zoom(17).build();
            MarkerOptions options = new MarkerOptions();
            options.position(destLatLng).isDraggable();
            mMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
            String url = getUrl(sourceLatLng.latitude, sourceLatLng.longitude,
                    destLatLng.latitude, destLatLng.longitude);
            if (url.length() > 0) {
                FetchUrl fetchUrl = new FetchUrl();
                fetchUrl.execute(url);
            }
        }
    }

    @SuppressWarnings("MissingPermission")
    private void setupMap() {
        mMap.setMyLocationEnabled(false);
        mMap.getUiSettings().setMyLocationButtonEnabled(false);
        mMap.setBuildingsEnabled(true);
        mMap.getUiSettings().setCompassEnabled(false);
        mMap.getUiSettings().setRotateGesturesEnabled(false);
        mMap.getUiSettings().setTiltGesturesEnabled(false);
        mMap.setOnCameraMoveListener(this);
//        mMap.setLocationSource(mLocationSource);
//        mMap.setMyLocationEnabled(true);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        try {
            // Customise the styling of the base map using a JSON object defined
            // in a raw resource file.
            boolean success = googleMap.setMapStyle(
                    MapStyleOptions.loadRawResourceStyle(
                            activity, R.raw.style_json));

            if (!success) {
                Log.e("Map:Style", "Style parsing failed.");
            } else {
                Log.e("Map:Style", "Style Applied.");
            }
        } catch (Resources.NotFoundException e) {
            Log.e("Map:Style", "Can't find style. Error: ", e);
        }
        mMap = googleMap;
        // do other tasks here
        setupMap();


        //Initialize Google Play Services
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(context,
                    Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                //Location Permission already granted

//                mMap.setMyLocationEnabled(true);
            } else {
                //Request Location Permission
                checkLocationPermission();
            }
        } else {
            buildGoogleApiClient();
//            mMap.setMyLocationEnabled(true);
        }
    }

    private void checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(),
                    Manifest.permission.ACCESS_FINE_LOCATION)) {
                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                new AlertDialog.Builder(context)
                        .setTitle("Location Permission Needed")
                        .setMessage("This app needs the Location permission, please accept to use" +
                                " location functionality")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                //Prompt the user once explanation has been shown
                                ActivityCompat.requestPermissions(getActivity(),
                                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                        1);
                            }
                        })
                        .create()
                        .show();
            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(getActivity(),
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        1);
            }
        }
    }

    @SuppressLint("RestrictedApi")
    @Override
    public void onConnected(@Nullable Bundle bundle) {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(3000);
        mLocationRequest.setFastestInterval(3000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        if (ContextCompat.checkSelfPermission(context,
                Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED && mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient,
                    mLocationRequest, this);
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }










    @Override
    public void onLocationChanged(Location location) {

        if (location.isFromMockProvider()) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
            Toast.makeText(context, getString(R.string.gps_issue), Toast.LENGTH_SHORT).show();
            CurrentStatus = "ONLINE";
            update(CurrentStatus, null);

            return;
        }
        if (location != null) {
            crt_lat = String.valueOf(location.getLatitude());
            crt_lng = String.valueOf(location.getLongitude());
            mLocation = location;
            currentLatLng = new LatLng(location.getLatitude(), location.getLongitude());
            SharedHelper.putKey(context, "current_lat", "" + crt_lat);
            SharedHelper.putKey(context, "current_lng", "" + crt_lng);
        }
        if (mMap != null) {
            addCar(new LatLng(location.getLatitude(), location.getLongitude()));

            // if (value == 0) {
            myLat = String.valueOf(location.getLatitude());
            myLng = String.valueOf(location.getLongitude());

            LatLng myLocation = new LatLng(location.getLatitude(), location.getLongitude());
            CameraPosition cameraPosition =
                    new CameraPosition.Builder().target(myLocation).zoom(17).build();
            if (value == 0) {
                MarkerOptions markerOptions = new MarkerOptions();
                markerOptions.position(myLocation);
                Marker marker = mMap.addMarker(markerOptions);
                marker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.ic_marker));
            }
            mMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
            mMap.setPadding(0, 0, 0, 135);
            mMap.getUiSettings().setZoomControlsEnabled(true);
            mMap.getUiSettings().setMyLocationButtonEnabled(true);

            //check status every 3 sec
            /*try {
                ha.removeCallbacksAndMessages(null);
            } catch (Exception e) {
                e.printStackTrace();
            }*/
            ha.postDelayed(new Runnable() {
                @Override
                public void run() {
                    //call function
                    try {
                     /*   TopdriversApplication.getInstance().getRequestQueue().cancelAll(new RequestQueue.RequestFilter() {
                            @Override
                            public boolean apply(Request<?> request) {
                                return true;
                            }
                        });*/
                        checkStatus();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    ha.postDelayed(this, 500);
                }
            }, 500);

            value++;

            //}

            /*HyperTrack.getCurrentLocation(new HyperTrackCallback() {
                @Override
                public void onSuccess(@NonNull SuccessResponse response) {

                    Location loc = (Location) response.getResponseObject();

                    crt_lat = String.valueOf(loc.getLatitude());
                    crt_lng = String.valueOf(loc.getLongitude());
                    currentLatLng = new LatLng(loc.getLatitude(), loc.getLongitude());
                    SharedHelper.putKey(context, "current_lat", "" + crt_lat);
                    SharedHelper.putKey(context, "current_lng", "" + crt_lng);

                }

                @Override
                public void onError(@NonNull ErrorResponse errorResponse) {

                }
            });*/
        }
    }

    private void addCar(final LatLng latLng) {
        if (isDetached()) return;
        if (latLng != null && latLng.latitude > 0 && latLng.longitude > 0) {
            mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
            if (newPosition != null) {
                oldPosition = newPosition;
                newPosition = latLng;
            } else {
                newPosition = latLng;
            }
            if (oldPosition != null && currentMarker != null) {
                animateMarker(oldPosition, newPosition, currentMarker);
                currentMarker.setRotation(bearingBetweenLocations(oldPosition, newPosition));
            }

        }
    }

    private float bearingBetweenLocations(LatLng latLng1, LatLng latLng2) {
        double PI = 3.14159;
        double lat1 = latLng1.latitude * PI / 180;
        double long1 = latLng1.longitude * PI / 180;
        double lat2 = latLng2.latitude * PI / 180;
        double long2 = latLng2.longitude * PI / 180;

        double dLon = (long2 - long1);

        double y = Math.sin(dLon) * Math.cos(lat2);
        double x = Math.cos(lat1) * Math.sin(lat2) - Math.sin(lat1)
                * Math.cos(lat2) * Math.cos(dLon);

        double brng = Math.atan2(y, x);

        brng = Math.toDegrees(brng);
        brng = (brng + 360) % 360;
        return (float) brng;
    }

    private void animateMarker(final LatLng startPosition, final LatLng toPosition,
                               final Marker marker) {
        final Handler handler = new Handler();
        final long start = SystemClock.uptimeMillis();
        final long duration = 700;
        final Interpolator interpolator = new LinearInterpolator();

        handler.post(new Runnable() {
            @Override
            public void run() {
                try {
                    long elapsed = SystemClock.uptimeMillis() - start;
                    float t = interpolator.getInterpolation((float) elapsed / duration);
                    double lng = t * toPosition.longitude + (1 - t) * startPosition.longitude;
                    double lat = t * toPosition.latitude + (1 - t) * startPosition.latitude;

                    marker.setPosition(new LatLng(lat, lng));

                    // Post again 16ms later.
                    if (t < 1.0) handler.postDelayed(this, 16);
                    else marker.setVisible(true);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    public void onClick(View v) {

    }

    @Override
    public void onCameraMove() {
        utils.print("Current marker", "Zoom Level " + mMap.getCameraPosition().zoom);
        if (currentMarker != null) {
            if (!mMap.getProjection().getVisibleRegion().latLngBounds.contains(currentMarker.getPosition())) {
                utils.print("Current marker", "Current Marker is not visible");
                if (imgCurrentLoc.getVisibility() == View.GONE) {
                    imgCurrentLoc.setVisibility(View.VISIBLE);
                }
            } else {
                utils.print("Current marker", "Current Marker is visible");
                if (imgCurrentLoc.getVisibility() == View.VISIBLE) {
                    imgCurrentLoc.setVisibility(View.GONE);
                }
                if (mMap.getCameraPosition().zoom < 16.0f) {
                    if (imgCurrentLoc.getVisibility() == View.GONE) {
                        imgCurrentLoc.setVisibility(View.VISIBLE);
                    }
                }
            }
        }
    }





    private String downloadUrl(String strUrl) throws IOException {
        String data = "";
        InputStream iStream = null;
        HttpURLConnection urlConnection = null;
        try {
            URL url = new URL(strUrl);

            // Creating an http connection to communicate with url
            urlConnection = (HttpURLConnection) url.openConnection();

            // Connecting to url
            urlConnection.connect();

            // Reading data from url
            iStream = urlConnection.getInputStream();

            BufferedReader br = new BufferedReader(new InputStreamReader(iStream));

            StringBuffer sb = new StringBuffer();

            String line = "";
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }

            data = sb.toString();
            Log.d("downloadUrl", data.toString());
            br.close();

        } catch (Exception e) {
            Log.d("Exception", e.toString());
        } finally {
            iStream.close();
            urlConnection.disconnect();
        }
        return data;
    }

    private String getUrl(double source_latitude, double source_longitude, double dest_latitude,
                          double dest_longitude) {
        String url = "";
        try {
            // Origin of route
            String str_origin = "origin=" + source_latitude + "," + source_longitude;

            // Destination of route
            String str_dest = "destination=" + dest_latitude + "," + dest_longitude;


            // Sensor enabled
            String sensor = "sensor=false";

            // Building the parameters to the web service
            String parameters = str_origin + "&" + str_dest + "&" + sensor;

            // Output format
            String output = "json";

            // Building the url to the web service
            url = "https://maps.googleapis.com/maps/api/directions/" + output + "?"
                    + parameters + "&key=" + getString(R.string.google_map_api);
        } catch (Exception e) {
        }

        return url;
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private boolean isJobServiceOn(Context context) {
        JobScheduler scheduler = (JobScheduler) context
                .getSystemService(JOB_SCHEDULER_SERVICE);
        boolean hasBeenScheduled = false;
        for (JobInfo jobInfo : scheduler.getAllPendingJobs()) {
            if (jobInfo.getId() == CHECK_SCHEDULE_JOB_ID) {
                hasBeenScheduled = true;
                break;
            }
        }
        return hasBeenScheduled;
    }

    private boolean isServiceRunning() {
        ActivityManager manager = (ActivityManager) context
                .getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service :
                manager.getRunningServices(Integer.MAX_VALUE)) {
            if (LocationShareService.class.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    private void startForegroundService() {
        if (!isJobServiceOn(context)) {
            scheduleJob(context);
        }
        if (!isServiceRunning()) {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
                context.startService(new Intent(context, LocationShareService.class));
            } else {
                Intent serviceIntent = new Intent(context, LocationShareService.class);
                ContextCompat.startForegroundService(context, serviceIntent);
            }
        }
    }

    private void stopForegroundService() {
        if (isServiceRunning())
            context.stopService(new Intent(context, LocationShareService.class));

    }

    @TargetApi(Build.VERSION_CODES.M)
    public void scheduleJob(Context context) {
        ComponentName serviceComponent = new ComponentName(context, CheckScheduleService.class);
        JobInfo.Builder builder = new JobInfo.Builder(CHECK_SCHEDULE_JOB_ID, serviceComponent);
        builder.setMinimumLatency(5000); // wait at least
        builder.setOverrideDeadline(12000); // maximum delay
        builder.setRequiresDeviceIdle(true); // device should be idle
        builder.setRequiresCharging(false); // we don't care if the device is charging or not
        JobScheduler jobScheduler;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            jobScheduler = context.getSystemService(JobScheduler.class);
        } else
            jobScheduler = (JobScheduler) getActivity().getSystemService(JOB_SCHEDULER_SERVICE);
        jobScheduler.schedule(builder.build());
    }

    private void checkStatus() {
        try {
            /* Battery status check */
            if (Utilities.getBatteryLevel(context)) {
                if (showBatteryAlert) {
                    Utilities.notify(context, activity);
                    showBatteryAlert = false;
                }
            }


            if (helper.isConnectingToInternet()) {

                if (SharedHelper.getKey(context, "is_track").equalsIgnoreCase("YES")) {
                    if (CurrentStatus.equalsIgnoreCase("DROPPED") || CurrentStatus.equalsIgnoreCase("COMPLETED")) {
//                        updateLiveTracking(crt_lat, crt_lng);

                        updateTrack();

                    }
                }
                System.out.println("---------crtlat----------"+crt_lat);
                System.out.println("---------crtlng----------"+crt_lng);

                String url = URLHelper.base + "api/provider/trip?latitude=" + crt_lat +
                        "&longitude=" + crt_lng;
                System.out.println("------------url----------"+url);
                utils.print("Destination Current Lat", "" + crt_lat);
                utils.print("Destination Current Lng", "" + crt_lng);
                /*if (jsonObjectRequest != null) {
                    jsonObjectRequest.cancel();
                }*/

                JsonObjectRequest jsonObjectRequest =
                        new JsonObjectRequest(Request.Method.GET, url, null,
                                new Response.Listener<JSONObject>() {
                                    @Override
                                    public void onResponse(JSONObject response) {
                                        System.out.println("--------------response-firsst-------------"+response);

                                        try {
                                            SharedHelper.putKey(context, "accountStatus",
                                                    response.optString("account_status"));
                                            SharedHelper.putKey(context, "serviceStatus",
                                                    response.optString("service_status"));
                                            if (response.optJSONArray("requests").length() > 0) {
                                                JSONObject jsonObject =
                                                        response.optJSONArray("requests").getJSONObject(0).optJSONObject("request").optJSONObject("user");

                                                if (jsonObject != null) {
                                                    user.setFirstName(jsonObject.optString(
                                                            "first_name"));
                                                    System.out.println("-----------usergetfirstname------"+user.getFirstName());
                                                    user.setLastName(jsonObject.optString(
                                                            "last_name"));
                                                    user.setEmail(jsonObject.optString("email"));
                                                    if (jsonObject.optString("picture").startsWith("http"))
                                                        user.setImg(jsonObject.optString("picture"
                                                        ));
                                                    else
                                                        user.setImg(URLHelper.base + "storage/" + jsonObject.optString("picture"));
                                                    user.setRating(jsonObject.optString("rating"));
                                                    user.setMobile(jsonObject.optString("mobile"));
                                                    bookingId =
                                                            response.optJSONArray("requests").getJSONObject(0).optJSONObject("request").optString("booking_id");
                                                    address =
                                                            response.optJSONArray("requests").getJSONObject(0).optJSONObject("request").optString("s_address");
                                                    SharedHelper.putKey(context, "is_track",
                                                            response.optJSONArray("requests").getJSONObject(0).optJSONObject("request").optString("is_track"));
                                                }
                                            } else {
                                                try {
                                                    customDialog.dismiss();
                                                } catch (Exception e) {
                                                    e.printStackTrace();
                                                }

                                            }
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }
                                        Log.d("CheckStatus",response.optString("account_status"));

                                        if (response.optString("account_status").equals("new") || response.optString("account_status").equals("onboarding")) {
                                            ha.removeMessages(0);
                                            Intent intent = new Intent(activity,
                                                    WaitingForApproval.class);
                                            activity.startActivity(intent);
                                            activity.finish();
                                            stopForegroundService();
                                        } else {

                                            if (response.optString("service_status").equals(
                                                    "offline")) {
                                                ha.removeMessages(0);
//                    Intent intent = new Intent(activity, Offline.class);
//                    activity.startActivity(intent);
                                                stopForegroundService();
                                                goOffline();
                                            } else {

                                                startForegroundService();
                                                if (response.optJSONArray("requests") != null && response.optJSONArray("requests").length() > 0) {
                                                    JSONObject statusResponse = null;
                                                    try {
                                                        statusResponses = response.optJSONArray(
                                                                "requests");


                                                        statusResponse =
                                                                response.optJSONArray("requests").getJSONObject(0).optJSONObject("request");
                                                        Log.d("CheckStatus",statusResponse.toString());
                                                        s_address = statusResponse.optString(
                                                                "s_address");
                                                        d_address = statusResponse.optString(
                                                                "d_address");
                                                        request_id =
                                                                response.optJSONArray("requests").getJSONObject(0).optString("request_id");
                                                    } catch (JSONException e) {
                                                        e.printStackTrace();
                                                    }

                                                    if ((statusResponse != null) && (request_id != null)) {
                                                        if ((!previous_request_id.equals(request_id) || previous_request_id.equals(" ")) && mMap != null) {
                                                            previous_request_id = request_id;
                                                            srcLatitude =
                                                                    Double.valueOf(statusResponse.optString("s_latitude"));
                                                            srcLongitude =
                                                                    Double.valueOf(statusResponse.optString("s_longitude"
                                                                    ));
                                                            destLatitude =
                                                                    Double.valueOf(statusResponse.optString("d_latitude"));
                                                            destLongitude =
                                                                    Double.valueOf(statusResponse.optString("d_longitude"
                                                                    ));
                                                            //noinspection deprecation
                                                            setSourceLocationOnMap(currentLatLng);
                                                            setPickupLocationOnMap();
                                                            sos.setVisibility(View.GONE);
                                                        }
                                                        String payMode = statusResponse.optString(
                                                                "payment_mode");
                                                        utils.print("Cur_and_New_status :",
                                                                "" + CurrentStatus +
                                                                        "," + statusResponse.optString(
                                                                        "status"));
                                                        try {
                                                            if (PreviousStatus != null && PreviousStatus.length() > 0) {

                                                            } else {

                                                                PreviousStatus = "";

                                                            }
                                                        } catch (Exception e) {
                                                            e.printStackTrace();
                                                        }
                                                        if (!PreviousStatus.equals(statusResponse.optString(
                                                                "status"))) {
                                                            if (statusResponse.optString(
                                                                    "status").equals(
                                                                    "COMPLETED")
                                                                    && statusResponse.optString(
                                                                    "paid").equals("0"
                                                            ) && payMode.equalsIgnoreCase("CARD")) {

                                                            } else {
                                                                PreviousStatus =
                                                                        statusResponse.optString(
                                                                                "status");
                                                                clearVisibility();
                                                            }
                                                            utils.print("responseObj(" + request_id + ")",
                                                                    statusResponse.toString());
                                                            utils.print("Cur_and_New_status :",
                                                                    "" + CurrentStatus + "," + statusResponse.optString(
                                                                            "status"));
                                                            if (!statusResponse.optString("status"
                                                            ).equalsIgnoreCase(
                                                                    "SEARCHING")) {
                                                                timerCompleted = false;

                                                                if (mPlayer != null && mPlayer.isPlaying()) {
                                                                   /* mPlayer.stop();
                                                                    mPlayer = null;*/
                                                                    stopPlaying();
                                                                    countDownTimer.cancel();
                                                                }
                                                            }
                                                            if (statusResponse.optString("status").equalsIgnoreCase(
                                                                    "SEARCHING")) {
                                                                //timerCompleted = true;
                                                                if (countDownTimer == null && ll_01_contentLayer_accept_or_reject_now.getVisibility() == View.GONE) {
                                                                scheduleTrip = false;
                                                                if (!timerCompleted ) {
String ss=txt01Timer.getText().toString();
                                                                    if (ll_01_contentLayer_accept_or_reject_now.getVisibility() == View.GONE && (txt01Timer.getText().toString().equalsIgnoreCase("1")||txt01Timer.getText().toString().equalsIgnoreCase("0"))) {
                                                                        ll_01_contentLayer_accept_or_reject_now.startAnimation(slide_up);
                                                                        ll_01_contentLayer_accept_or_reject_now.setVisibility(View.VISIBLE);
                                                                        setValuesTo_ll_01_contentLayer_accept_or_reject_now(statusResponses);
                                                                    }
/*
                                                                        getActivity().runOnUiThread(new Runnable() {
                                                                            @Override

                                                                            public void run() {

                                                                            }
                                                                        });

 */



                                                                }
                                                                 }

                                                                try {
                                                                    //if (countDownTimer == null && ll_01_contentLayer_accept_or_reject_now.getVisibility() == View.GONE) {
                                                                    if (!timerCompleted) {
                                                                        scheduleTrip = false;
                                                                        timerCompleted = true;

                                                                        if (isBackground) {
                                                                            openAcceptScreen = true;
                                                                            TopdriversApplication.getInstance().isOpenRideAccept = false;
                                                                        }
                                                                        //TODO Lan debug
                                                                        Intent intent = new Intent(getActivity(), RideAcceptandRejectActivity.class);
                                                                        intent.putExtra("statusResponses", statusResponses.toString());
                                                                        intent.putExtra("requestId", request_id);
                                                                        getActivity().startActivity(intent);
                                                                        mPlayer = MediaPlayer.create(context, R.raw.alert_tone);
                                                                                    mPlayer.start();
                                                                                    timerCompleted = true;



                                                                    }else{
                                                                        mPlayer.stop();
                                                                    }
                                                                    /*if (!timerCompleted *//*&& mPlayer.isPlaying()*//*) {


                                                                        MainActivity.mMyApp.getCurrentActivity().runOnUiThread(new Runnable() {
                                                                            @Override

                                                                            public void run() {
                                                                                if (ll_01_contentLayer_accept_or_reject_now.getVisibility() == View.GONE) {

                                                                                        setValuesTo_ll_01_contentLayer_accept_or_reject_now(statusResponses);
                                                                                }

                                                                            }
                                                                        });

                                                                    }*/
                                                                    // }
                                                                } catch (Exception e) {
                                                                    e.printStackTrace();

                                                                    //if (ll_01_contentLayer_accept_or_reject_now.getVisibility() == View.GONE) {
                                                                    /*if (!timerCompleted) {

                                                                        if (ll_01_contentLayer_accept_or_reject_now.getVisibility() == View.GONE) {
                                                                            ll_01_contentLayer_accept_or_reject_now.setVisibility(View.VISIBLE);
                                                                            ll_01_contentLayer_accept_or_reject_now.startAnimation(slide_up);
                                                                            timerCompleted = true;
                                                                            setValuesTo_ll_01_contentLayer_accept_or_reject_now(statusResponses);
                                                                        }

                                                                    }*/
                                                                    // }
                                                                }

                                                                CurrentStatus = "STARTED";
                                                            } else if (statusResponse.optString(
                                                                    "status").equals(
                                                                    "STARTED")) {
                                                                try {
                                                                    customDialog.dismiss();
                                                                } catch (Exception e) {
                                                                    e.printStackTrace();
                                                                }
                                                                //TODO Need to debug
                                                                TopdriversApplication.getInstance().getRequestQueue().cancelAll("checkStatus");
                                                                setValuesTo_ll_03_contentLayer_service_flow(statusResponses);
                                                                ll_03_contentLayer_service_flow.setVisibility(View.VISIBLE);
                                                                if (ll_03_contentLayer_service_flow.getVisibility() == View.GONE) {
                                                                    //ll_03_contentLayer_service_flow
                                                                    // .startAnimation(slide_up);
                                                                }


                                                                btn_01_status.setText(context.getResources().getString(R.string.tap_when_arrived));
                                                                CurrentStatus = "ARRIVED";
                                                                sos.setVisibility(View.GONE);
                                                                if (srcLatitude == 0 && srcLongitude == 0 && destLatitude == 0 && destLongitude == 0) {
                                                                    mapClear();
                                                                    srcLatitude =
                                                                            Double.valueOf(statusResponse.optString(
                                                                                    "s_latitude"));
                                                                    srcLongitude =
                                                                            Double.valueOf(statusResponse.optString(
                                                                                    "s_longitude"));
                                                                    destLatitude =
                                                                            Double.valueOf(statusResponse.optString(
                                                                                    "d_latitude"));
                                                                    destLongitude =
                                                                            Double.valueOf(statusResponse.optString(
                                                                                    "d_longitude"));
                                                                    //noinspection deprecation
                                                                    //
                                                                    setSourceLocationOnMap(currentLatLng);
                                                                    setPickupLocationOnMap();
                                                                }
                                                                img03Status1.setImageResource(R.drawable.arrived);
                                                                img03Status2.setImageResource(R.drawable.pickup);
                                                                sos.setVisibility(View.GONE);
                                                                btn_cancel_ride.setVisibility(View.VISIBLE);
                                                                destinationLayer.setVisibility(View.VISIBLE);
                                                                address =
                                                                        statusResponse.optString(
                                                                                "s_address");
                                                                if (address != null && !address.equalsIgnoreCase(
                                                                        "null") && address.length() > 0)
                                                                    destination.setText(address);
                                                                else
                                                                    destination.setText(getAddress(statusResponse.optString("s_latitude"),
                                                                            statusResponse.optString("s_longitude"
                                                                            )));
                                                                topSrcDestTxtLbl.setText(context.getResources().getString(R.string.pick_up));
                                                            } else if (statusResponse.optString(
                                                                    "status").equals(
                                                                    "ARRIVED")) {
                                                                try {
                                                                    customDialog.dismiss();
                                                                } catch (Exception e) {
                                                                    e.printStackTrace();
                                                                }
                                                                setValuesTo_ll_03_contentLayer_service_flow(statusResponses);
                                                                ll_03_contentLayer_service_flow.setVisibility(View.VISIBLE);
                                                                btn_01_status.setText(context.getResources().getString(R.string.tap_when_pickedup));
                                                                sos.setVisibility(View.GONE);
                                                                img03Status1.setImageResource(R.drawable.arrived_select);
                                                                img03Status2.setImageResource(R.drawable.pickup);
                                                                CurrentStatus = "PICKEDUP";
                                                                setSourceLocationOnMap(currentLatLng);
                                                                setDestinationLocationOnMap();
                                                                btn_cancel_ride.setVisibility(View.VISIBLE);
                                                                destinationLayer.setVisibility(View.VISIBLE);
                                                                address =
                                                                        statusResponse.optString(
                                                                                "d_address");
                                                                try {
                                                                    if (address != null && !address.equalsIgnoreCase(
                                                                            "null") && address.length() > 0)
                                                                        destination.setText(address);
                                                                    else
                                                                        destination.setText(getAddress(statusResponse.optString("d_latitude"),
                                                                                statusResponse.optString("d_longitude"
                                                                                )));
                                                                    topSrcDestTxtLbl.setText(context.getResources().getString(R.string.drop_at));
                                                                } catch (Exception e) {
                                                                }
                                                            } else if (statusResponse.optString(
                                                                    "status").equals(
                                                                    "PICKEDUP")) {
                                                                setValuesTo_ll_03_contentLayer_service_flow(statusResponses);
                                                                ll_03_contentLayer_service_flow.setVisibility(View.VISIBLE);
                                                                btn_01_status.setText(context.getResources().getString(R.string.tap_when_dropped));
                                                                sos.setVisibility(View.VISIBLE);
                                                                img03Status1.setImageResource(R.drawable.arrived_select);
                                                                img03Status2.setImageResource(R.drawable.pickup_select);
                                                                CurrentStatus = "PAYMENT";
                                                                destinationLayer.setVisibility(View.VISIBLE);
                                                                btn_cancel_ride.setVisibility(View.GONE);
                                                                address =
                                                                        statusResponse.optString(
                                                                                "d_address");
                                                                if (address != null && !address.equalsIgnoreCase(
                                                                        "null") && address.length() > 0)
                                                                    destination.setText(address);
                                                                else {
                                                                    destination.setText(getAddress(statusResponse.optString("d_latitude"), statusResponse.optString("d_longitude")));
                                                                }
                                                                topSrcDestTxtLbl.setText(context.getResources().getString(R.string.drop_at));
                                                                mapClear();

                                                                srcLatitude =
                                                                        Double.valueOf(statusResponse.optString(
                                                                                "s_latitude"));
                                                                srcLongitude =
                                                                        Double.valueOf(statusResponse.optString(
                                                                                "s_longitude"));
                                                                destLatitude =
                                                                        Double.valueOf(statusResponse.optString(
                                                                                "d_latitude"));
                                                                destLongitude =
                                                                        Double.valueOf(statusResponse.optString(
                                                                                "d_longitude"));

                                                                setSourceLocationOnMap(currentLatLng);
                                                                try {
                                                                    setDestinationLocationOnMap();
                                                                } catch (Exception e) {
                                                                    e.printStackTrace();
                                                                }
                                                            } else if (statusResponse.optString(
                                                                    "status").equals(
                                                                    "PAYMENT")
                                                                    && statusResponse.optString(
                                                                    "paid").equals("0"
                                                            )) {
                                               /* setValuesTo_ll_04_contentLayer_payment
                                                (statusResponses);
                                                if (ll_04_contentLayer_payment.getVisibility() ==
                                                 View.GONE) {
                                                    ll_04_contentLayer_payment.startAnimation
                                                    (slide_up);
                                                }
                                                ll_04_contentLayer_payment.setVisibility(View
                                                .VISIBLE);
                                                img03Status1.setImageResource(R.drawable.arrived);
                                                img03Status2.setImageResource(R.drawable.pickup);
                                                btn_confirm_payment.setText(context.getResources
                                                ().getString(R.string.tap_amount_confirm));
                                                sos.setVisibility(View.VISIBLE);
                                                destinationLayer.setVisibility(View.GONE);*/
                                                                CurrentStatus = "DROPPED";

                                                                String amount = "0";

                                                                if (statusResponse.optJSONObject(
                                                                        "payment") != null) {
                                                                    amount =
                                                                            statusResponse.optJSONObject(
                                                                                    "payment").optString(
                                                                                    "payable");
                                                                    confirmAmount("COMPLETED",
                                                                            request_id, amount);
                                                                } else {
                                                                    PreviousStatus = "";
                                                                }


                                                            } else if (statusResponse.optString(
                                                                    "status").equals(
                                                                    "DROPPED")
                                                                    && statusResponse.optString(
                                                                    "paid").equals("0"
                                                            ) && payMode.equalsIgnoreCase("CASH")) {
                                                                setValuesTo_ll_04_contentLayer_payment(statusResponses);
                                                                if (ll_04_contentLayer_payment.getVisibility() == View.GONE) {
                                                                    ll_04_contentLayer_payment.startAnimation(slide_up);
                                                                }
                                                                ll_04_contentLayer_payment.setVisibility(View.VISIBLE);
                                                                try {
                                                                    viewDialog.dismiss();
                                                                    customDialog.dismiss();
                                                                } catch (Exception e) {
                                                                    e.printStackTrace();
                                                                }
                                                                img03Status1.setImageResource(R.drawable.arrived);
                                                                img03Status2.setImageResource(R.drawable.pickup);
                                                                btn_confirm_payment.setText(context.getResources().getString(R.string.tap_when_paid));
                                                                amountPaid.setText(context.getResources().getString(R.string.amount_to_be_paid));
                                                                sos.setVisibility(View.VISIBLE);
                                                                destinationLayer.setVisibility(View.GONE);
                                                                CurrentStatus = "COMPLETED";
                                                                try {
                                                                    isPaid = statusResponse.optString("paid");
                                                                } catch (Exception e) {
                                                                    e.printStackTrace();
                                                                }
                                                            } else if (statusResponse.optString(
                                                                    "status").equals(
                                                                    "DROPPED")
                                                                    && statusResponse.optString(
                                                                    "paid").equals("0"
                                                            ) && payMode.equalsIgnoreCase("CARD")) {

                                                                setValuesTo_ll_04_contentLayer_payment(statusResponses);
                                                                if (ll_04_contentLayer_payment.getVisibility() == View.GONE) {
                                                                    ll_04_contentLayer_payment.startAnimation(slide_up);
                                                                }
                                                                ll_04_contentLayer_payment.setVisibility(View.VISIBLE);
                                                                try {
                                                                    viewDialog.dismiss();
                                                                    customDialog.dismiss();
                                                                } catch (Exception e) {
                                                                    e.printStackTrace();
                                                                }
                                                                img03Status1.setImageResource(R.drawable.arrived);
                                                                img03Status2.setImageResource(R.drawable.pickup);
                                                                btn_confirm_payment.setText(context.getResources().getString(R.string.tap_when_paid));
                                                                amountPaid.setText("Paid Amount");
                                                                sos.setVisibility(View.VISIBLE);
                                                                destinationLayer.setVisibility(View.GONE);
                                                                CurrentStatus = "COMPLETED";
                                                                try {
                                                                    isPaid = statusResponse.optString("paid");
                                                                } catch (Exception e) {
                                                                    e.printStackTrace();
                                                                }
                                                            } else if (statusResponse.optString(
                                                                    "status").equals(
                                                                    "DROPPED") && statusResponse.optString("paid").equals("1")) {
                                                                setValuesTo_ll_05_contentLayer_feedback(statusResponses);
                                                                if (ll_05_contentLayer_feedback.getVisibility() == View.GONE) {
                                                                    ll_05_contentLayer_feedback.startAnimation(slide_up);
                                                                }
                                                                ll_05_contentLayer_feedback.setVisibility(View.VISIBLE);
                                                                btn_01_status.setText(context.getResources().getString(R.string.rate_user));
                                                                sos.setVisibility(View.VISIBLE);
                                                                destinationLayer.setVisibility(View.GONE);
                                                                CurrentStatus = "RATE";
                                                                try {
                                                                    customDialog.dismiss();
                                                                } catch (Exception e) {
                                                                    e.printStackTrace();
                                                                }
//                                                if (isMyServiceRunning(LocationTracking.class)) {
//                                                    activity.stopService(service_intent);
//                                                }
//                                                LocationTracking.distance = 0.0f;
                                                            } else if (statusResponse.optString(
                                                                    "status").equals(
                                                                    "COMPLETED")
                                                                    && statusResponse.optString(
                                                                    "paid").equals("0"
                                                            ) && payMode.equalsIgnoreCase("CARD")) {
                                                                lnrGoOffline.setVisibility(View.GONE);
                                                                setValuesTo_ll_04_contentLayer_payment(statusResponses);
                                                                if (ll_04_contentLayer_payment.getVisibility() == View.GONE) {
                                                                    ll_04_contentLayer_payment.startAnimation(slide_up);
                                                                }
                                                                ll_04_contentLayer_payment.setVisibility(View.VISIBLE);
                                                                try {
                                                                    viewDialog.dismiss();
                                                                    customDialog.dismiss();
                                                                } catch (Exception e) {
                                                                    e.printStackTrace();
                                                                }
                                                                img03Status1.setImageResource(R.drawable.arrived);
                                                                img03Status2.setImageResource(R.drawable.pickup);
                                                                btn_confirm_payment.setText(context.getResources().getString(R.string.tap_when_paid));
                                                                amountPaid.setText("Paid Amount");
                                                                sos.setVisibility(View.VISIBLE);
                                                                destinationLayer.setVisibility(View.GONE);
                                                                CurrentStatus = "COMPLETED";
                                                                cardConfirm = true;

                                                            } else if (statusResponse.optString(
                                                                    "status").equals(
                                                                    "COMPLETED")
                                                                    && statusResponse.optString(
                                                                    "paid").equals("1"
                                                            ) && payMode.equalsIgnoreCase("CARD")) {
                                                                setValuesTo_ll_05_contentLayer_feedback(statusResponses);
                                                                if (ll_05_contentLayer_feedback.getVisibility() == View.GONE) {
                                                                    ll_05_contentLayer_feedback.startAnimation(slide_up);
                                                                }
                                                                edt05Comment.setText("");
                                                                ll_05_contentLayer_feedback.setVisibility(View.VISIBLE);
                                                                sos.setVisibility(View.GONE);
                                                                destinationLayer.setVisibility(View.GONE);
                                                                btn_01_status.setText(context.getResources().getString(R.string.rate_user));
                                                                CurrentStatus = "RATE";
                                                                cardConfirm = false;
                                                                try {
                                                                    customDialog.dismiss();
                                                                } catch (Exception e) {
                                                                    e.printStackTrace();
                                                                }
                                                              /*  setValuesTo_ll_04_contentLayer_payment(statusResponses);
                                                                if (ll_04_contentLayer_payment.getVisibility() == View.GONE) {
                                                                    ll_04_contentLayer_payment.startAnimation(slide_up);
                                                                }
                                                                ll_04_contentLayer_payment.setVisibility(View.VISIBLE);
                                                                img03Status1.setImageResource(R.drawable.arrived);
                                                                img03Status2.setImageResource(R.drawable.pickup);
                                                                btn_confirm_payment.setText(context.getResources().getString(R.string.tap_when_paid));
                                                                sos.setVisibility(View.VISIBLE);
                                                                amountPaid.setText("Paid Amount");
                                                                destinationLayer.setVisibility(View.GONE);
                                                                CurrentStatus = "COMPLETED";
                                                                cardConfirm = true;*/
                                                            } else if (statusResponse.optString(
                                                                    "status").equals(
                                                                    "COMPLETED") && payMode.equalsIgnoreCase("CASH")) {
                                                                setValuesTo_ll_05_contentLayer_feedback(statusResponses);
                                                                if (ll_05_contentLayer_feedback.getVisibility() == View.GONE) {
                                                                    ll_05_contentLayer_feedback.startAnimation(slide_up);
                                                                }
                                                                edt05Comment.setText("");
                                                                ll_05_contentLayer_feedback.setVisibility(View.VISIBLE);
                                                                sos.setVisibility(View.GONE);
                                                                destinationLayer.setVisibility(View.GONE);
                                                                btn_01_status.setText(context.getResources().getString(R.string.rate_user));
                                                                CurrentStatus = "RATE";
                                                                try {
                                                                    customDialog.dismiss();
                                                                } catch (Exception e) {
                                                                    e.printStackTrace();
                                                                }
//                                                if (isMyServiceRunning(LocationTracking.class)) {
//                                                    activity.stopService(service_intent);
//                                                }
//                                                LocationTracking.distance = 0.0f;
                                                            } else if (statusResponse.optString(
                                                                    "status").equals(
                                                                    "SCHEDULED")) {
                                                                if (mMap != null) {
                                                                    if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                                                                        return;
                                                                    }
                                                                    whenMapCleared();
                                                                }
                                                                clearVisibility();
                                                                CurrentStatus = "SCHEDULED";
                                                                if (lnrGoOffline.getVisibility() == View.GONE) {
                                                                    lnrGoOffline.startAnimation(slide_up);
                                                                }
                                                                lnrGoOffline.setVisibility(View.VISIBLE);
                                                                utils.print("statusResponse",
                                                                        "null");
                                                                destinationLayer.setVisibility(View.GONE);
//                                                if (isMyServiceRunning(LocationTracking.class)) {
//                                                    activity.stopService(service_intent);
//                                                }
//                                                LocationTracking.distance = 0.0f;
                                                            }
                                                        }
                                                    } else {
                                                        if (mMap != null) {
                                                            if (ActivityCompat.checkSelfPermission(activity,
                                                                    Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                                                                return;
                                                            }
                                                            timerCompleted = false;
                                                            whenMapCleared();
                                                            if (mPlayer != null && mPlayer.isPlaying()) {
                                                                /*mPlayer.stop();
                                                                mPlayer = null;*/
                                                                stopPlaying();
                                                                countDownTimer.cancel();
                                                            }
                                                            try {
                                                            Intent intent = new Intent("finish");
                                                                getActivity().sendBroadcast(intent);
                                                            } catch (Exception e) {
                                                                e.printStackTrace();
                                                            }

                                                        }
//                                        if (isMyServiceRunning(LocationTracking.class)) {
//                                            activity.stopService(service_intent);
//                                        }
//                                        LocationTracking.distance = 0.0f;

                                                        clearVisibility();
                                                        lnrGoOffline.setVisibility(View.VISIBLE);
                                                        destinationLayer.setVisibility(View.GONE);
                                                        CurrentStatus = "ONLINE";
                                                        PreviousStatus = "NULL";
                                                        utils.print("statusResponse", "null");
                                                    }

                                                } else {
                                                    timerCompleted = false;

                                                    // Cancel flow layout by using physically code
                                                    if (SharedHelper.getBoolean(context, "IS_CANCEL") && ll_03_contentLayer_service_flow.getVisibility() != View.VISIBLE) {
                                                        SharedHelper.putBoolean(context, "IS_CANCEL", false);
                                                    } else if (SharedHelper.getBoolean(context, "IS_CANCEL") && ll_03_contentLayer_service_flow.getVisibility() == View.VISIBLE) {
                                                        PreviousStatus = "STARTED";
                                                        SharedHelper.putBoolean(context, "IS_CANCEL", false);
                                                        TopdriversApplication.getInstance().isOpenningFlow = false;
                                                    }

                                                    // Show layout by using alarm
                                                    if (SharedHelper.getBoolean(context, "OnTime")) {
                                                        if (isBackground) {
                                                            TopdriversApplication.getInstance().isOpenningFlow = true;
                                                        //    openArrivedScreen = true;
                                                        } else {
                                                            TopdriversApplication.getInstance().isOpenningFlow = false;
                                                            SharedHelper.putBoolean(context, "OnTime", false);
                                                            checkUpcomingList();
                                                        }
                                                    }

                                                    if (cancelDialog != null) {
                                                        if (cancelDialog.isShowing() && ll_03_contentLayer_service_flow.getVisibility() != View.VISIBLE) {
                                                            cancelDialog.dismiss();
                                                        }
                                                    }

                                                    if (PreviousStatus.equalsIgnoreCase("STARTED")) {
                                                        Toast.makeText(context,
                                                                context.getResources().getString(R.string.user_busy),
                                                                Toast.LENGTH_SHORT).show();
                                                    }

                                                    if (PreviousStatus.equalsIgnoreCase("ARRIVED")) {
                                                        Toast.makeText(context,
                                                                context.getResources().getString(R.string.user_busy),
                                                                Toast.LENGTH_SHORT).show();
                                                    }

                                                    if (cancelReasonDialog != null && ll_03_contentLayer_service_flow.getVisibility() != View.VISIBLE) {
                                                        if (cancelReasonDialog.isShowing()) {
                                                            cancelReasonDialog.dismiss();
                                                        }
                                                    }
                                                    if (!PreviousStatus.equalsIgnoreCase("NULL")) {
                                                        utils.print("response", "null");
                                                        if (mMap != null) {
                                                            if (ActivityCompat.checkSelfPermission(activity,
                                                                    Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                                                                return;
                                                            }
                                                            whenMapCleared();
                                                        }
                                                        if (mPlayer != null && mPlayer.isPlaying()) {
                                                          /*  mPlayer.stop();
                                                            mPlayer = null;*/
                                                            stopPlaying();
                                                            countDownTimer.cancel();
                                                        }
                                                        try {
                                                            Intent intent = new Intent("finish");
                                                            getActivity().sendBroadcast(intent);
                                                        } catch (Exception e) {
                                                            e.printStackTrace();
                                                        }
                                                        clearVisibility();
                                                        lnrGoOffline.setVisibility(View.VISIBLE);
                                                        destinationLayer.setVisibility(View.GONE);
                                                        CurrentStatus = "ONLINE";
                                                        PreviousStatus = "NULL";
                                                        utils.print("statusResponse", "null");
                                                    }

                                                    // open flow layout from background to foreground
                                                    if (TopdriversApplication.getInstance().isOpenningFlow && isFromResume) {
                                                        SharedHelper.putBoolean(context, "OnTime", false);
                                                        checkUpcomingList();
                                                        TopdriversApplication.getInstance().isOpenningFlow = false;
                                                        //    openArrivedScreen = false;
                                                        isFromResume = false;
                                                    }

                                                }
                                            }
                                        }


                                        /*try {
                                            customDialog.dismiss();
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }*/
                                    }
                                }, new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                utils.print("Error", error.toString());
                                //errorHandler(error);
                                timerCompleted = false;
                                mapClear();
                                clearVisibility();
                                CurrentStatus = "ONLINE";
                                PreviousStatus = "NULL";
                                lnrGoOffline.setVisibility(View.VISIBLE);
                                destinationLayer.setVisibility(View.GONE);
                                if (mPlayer != null && mPlayer.isPlaying()) {
                                    /*mPlayer.stop();
                                    mPlayer = null;*/
                                    stopPlaying();
                                    countDownTimer.cancel();
                                }
//                        if (errorLayout.getVisibility() != View.VISIBLE) {
//                            errorLayout.setVisibility(View.VISIBLE);
//                            sos.setVisibility(View.GONE);
//                        }
                            }
                        }) {
                            @Override
                            public java.util.Map<String, String> getHeaders() {
                                HashMap<String, String> headers = new HashMap<>();
                                headers.put("X-Requested-With", "XMLHttpRequest");
                                headers.put("Authorization", "Bearer " + token);
                                return headers;
                            }
                        };

               /* jsonObjectRequest.setRetryPolicy(new DefaultRetryPolicy(
                        3000,
                        DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                        DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));*/
                TopdriversApplication.getInstance().addToRequestQueue(jsonObjectRequest, "checkStatus");
            } else {
                displayMessage(context.getResources().getString(R.string.oops_connect_your_internet));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void updateTrack() {

       /* HyperTrack.getCurrentLocation(new HyperTrackCallback() {
            @Override
            public void onSuccess(@NonNull SuccessResponse response) {

                Location location = (Location) response.getResponseObject();
                String str = String.valueOf(location.getLatitude());
                updateLiveTracking(String.valueOf(location.getLatitude()),
                        String.valueOf(location.getLongitude()));
            }

            @Override
            public void onError(@NonNull ErrorResponse errorResponse) {
            }
        });*/
    }

    private void setValuesTo_ll_01_contentLayer_accept_or_reject_now(JSONArray status) {


        ll_01_contentLayer_accept_or_reject_now.setVisibility(View.VISIBLE);
        ll_01_contentLayer_accept_or_reject_now.startAnimation(slide_up);

        JSONObject statusResponse = new JSONObject();
        try {
            statusResponse = status.getJSONObject(0).getJSONObject("request");
            paymentModeText.setText(statusResponse.getString("payment_mode"));
            pickupDistanceText.setText(status.getJSONObject(0)
                    .getInt("user_location_distance") + " "
                    + getString(R.string.distance_unit));
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            if (!status.getJSONObject(0).optString("time_left_to_respond").equals("")) {
                count = status.getJSONObject(0).getString("time_left_to_respond");
            } else {
                count = "0";
            }
        } catch (Exception e) {
            e.printStackTrace();
            count = "0";
        }
        // if (countDownTimer == null && !timerCompleted) {
        countDownTimer = new CountDownTimer(Integer.parseInt(count) * 1000, 1000) {

            @SuppressLint("SetTextI18n")
            public void onTick(long millisUntilFinished) {
                try {
                    txt01Timer.setText("" + millisUntilFinished / 1000);
                    System.out.println("-----------notif------------");
                    if (mPlayer == null) {
                        mPlayer = MediaPlayer.create(context, R.raw.alert_tone);

                        if (!mPlayer.isPlaying()) {
                            if (!timerCompleted) {
                                mPlayer.start();
                                timerCompleted = true;
                            }
                        }
                    }
                    timerCompleted = true;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            public void onFinish() {
                txt01Timer.setText("0");
                mapClear();
                clearVisibility();
                whenMapCleared();
                if (mPlayer != null && mPlayer.isPlaying()) {
                    timerCompleted = false;
                    /*mPlayer.stop();
                    mPlayer = null;*/
                    stopPlaying();
                    countDownTimer.cancel();
                    countDownTimer = null;
                }
                mPlayer = null;
                ll_01_contentLayer_accept_or_reject_now.setVisibility(View.GONE);
                CurrentStatus = "ONLINE";
                PreviousStatus = "NULL";
                lnrGoOffline.setVisibility(View.VISIBLE);
                destinationLayer.setVisibility(View.GONE);
                timerCompleted = true;
                //handleIncomingRequest("Reject", request_id);
                handleIncomingRequestReject("Reject", request_id);
            }
        };
        countDownTimer.start();
        //}
        try {
            if (!statusResponse.optString("schedule_at").trim().equalsIgnoreCase("") && !statusResponse.optString("schedule_at").equalsIgnoreCase("null")) {
                txtSchedule.setVisibility(View.VISIBLE);
                String strSchedule = "";
                try {
                    strSchedule =
                            getDate(statusResponse.optString("schedule_at")) + "th " + getMonth(statusResponse.optString("schedule_at"))
                                    + " " + getYear(statusResponse.optString("schedule_at")) + " " +
                                    "at " + getTime(statusResponse.optString("schedule_at"));
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                txtSchedule.setText("Scheduled at : " + strSchedule);
            } else {
                txtSchedule.setVisibility(View.GONE);
            }
            final JSONObject user = statusResponse.getJSONObject("user");
            if (user != null) {
                if (!user.optString("picture").equals("null")) {
                    //Glide.with(activity).load(URLHelper.base+"storage/"+user.getString
                    // ("picture")).placeholder(R.drawable.ic_dummy_user).error(R.drawable
                    // .ic_dummy_user).into(img01User);
                    if (user.optString("picture").startsWith("http"))
                        Picasso.get().load(user.getString("picture")).placeholder(R.drawable.ic_dummy_user).error(R.drawable.ic_dummy_user).into(img01User);
                    else
                        Picasso.get().load(URLHelper.base + "storage/" + user.getString(
                                "picture")).placeholder(R.drawable.ic_dummy_user).error(R.drawable.ic_dummy_user).into(img01User);
                } else {
                    img01User.setImageResource(R.drawable.ic_dummy_user);
                }
                final User userProfile = this.user;
                img01User.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(context, ShowProfile.class);
                        intent.putExtra("user", userProfile);
                        startActivity(intent);
                    }
                });
                txt01UserName.setText(user.optString("first_name") + " " + user.optString(
                        "last_name"));
                if (statusResponse.getJSONObject("user").getString("rating") != null) {
                    rat01UserRating.setRating(Float.valueOf(user.getString("rating")));
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        txt01Pickup.setText(address);
    }

    private void setValuesTo_ll_03_contentLayer_service_flow(JSONArray status) {
        JSONObject statusResponse = new JSONObject();
        try {
            statusResponse = status.getJSONObject(0);
        } catch (JSONException e) {
            e.printStackTrace();
        }
       // try {
        JSONObject user=null;
        try {
            user = statusResponse.getJSONObject("request").getJSONObject("user");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        if (user != null) {
            if (!user.optString("mobile").equals("null")) {
                SharedHelper.putKey(context, "provider_mobile_no", "" + user.optString(
                        "mobile"));
            } else {
                SharedHelper.putKey(context, "provider_mobile_no", "");
            }
            if (!user.optString("picture").equals("null")) {
                //Glide.with(activity).load(URLHelper.base+"storage/"+user.getString
                // ("picture")).placeholder(R.drawable.ic_dummy_user).error(R.drawable
                // .ic_dummy_user).into(img03User);
                if (user.optString("picture").startsWith("http")) {
                    try {
                        Picasso.get().load(user.getString("picture")).placeholder(R.drawable.ic_dummy_user).error(R.drawable.ic_dummy_user).into(img03User);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                } else {
                    try {
                        Picasso.get().load(URLHelper.base + "storage/" + user.getString(
                                "picture")).placeholder(R.drawable.ic_dummy_user).error(R.drawable.ic_dummy_user).into(img03User);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            } else {
                img03User.setImageResource(R.drawable.ic_dummy_user);
            }
            final User userProfile = this.user;
            img03User.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(context, ShowProfile.class);
                    intent.putExtra("user", userProfile);
                    startActivity(intent);
                }
            });
            txt03UserName.setText(user.optString("first_name") + " " + user.optString(
                    "last_name"));
            try {
                if (statusResponse.getJSONObject("user").getString("rating") != null) {
                    try {
                        rat03UserRating.setRating(Float.valueOf(user.getString("rating")));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else {
                    rat03UserRating.setRating(0);
                }
            }catch (JSONException e1) {
                e1.printStackTrace();
            }
        }
       /* } catch (JSONException e) {
            e.printStackTrace();
        }*/
    }

    private void Trick_setValuesTo_ll_03_contentLayer_service_flow(JSONArray status) {
        JSONObject statusResponse = new JSONObject();
        try {
            statusResponse = status.getJSONObject(0);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        // try {
        JSONObject user=null;
        try {
            user = statusResponse.getJSONObject("user");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        if (user != null) {
            if (!user.optString("mobile").equals("null")) {
                SharedHelper.putKey(context, "provider_mobile_no", "" + user.optString(
                        "mobile"));
            } else {
                SharedHelper.putKey(context, "provider_mobile_no", "");
            }
            if (!user.optString("picture").equals("null")) {
                //Glide.with(activity).load(URLHelper.base+"storage/"+user.getString
                // ("picture")).placeholder(R.drawable.ic_dummy_user).error(R.drawable
                // .ic_dummy_user).into(img03User);
                if (user.optString("picture").startsWith("http")) {
                    try {
                        Picasso.get().load(user.getString("picture")).placeholder(R.drawable.ic_dummy_user).error(R.drawable.ic_dummy_user).into(img03User);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                } else {
                    try {
                        Picasso.get().load(URLHelper.base + "storage/" + user.getString(
                                "picture")).placeholder(R.drawable.ic_dummy_user).error(R.drawable.ic_dummy_user).into(img03User);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            } else {
                img03User.setImageResource(R.drawable.ic_dummy_user);
            }
            final User userProfile = this.user;
            img03User.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(context, ShowProfile.class);
                    intent.putExtra("user", userProfile);
                    startActivity(intent);
                }
            });
            txt03UserName.setText(user.optString("first_name") + " " + user.optString(
                    "last_name"));
            try {
                if (statusResponse.getJSONObject("user").getString("rating") != null) {
                    try {
                        rat03UserRating.setRating(Float.valueOf(user.getString("rating")));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else {
                    rat03UserRating.setRating(0);
                }
            }catch (JSONException e1) {
                e1.printStackTrace();
            }
        }
       /* } catch (JSONException e) {
            e.printStackTrace();
        }*/
    }

    private void setValuesTo_ll_04_contentLayer_payment(JSONArray status) {
        JSONObject statusResponse = new JSONObject();
        try {
            statusResponse = status.getJSONObject(0).getJSONObject("request");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        try {
            txt04InvoiceId.setText(context.getResources().getString(R.string.invoice) + " " + bookingId);
            txt04BasePrice.setText(SharedHelper.getKey(context, "currency") + "" + statusResponse.getJSONObject("payment").optString("fixed"));
            txt04Distance.setText(SharedHelper.getKey(context, "currency") + "" + statusResponse.getJSONObject("payment").optString("distance"));
            txt04Tax.setText(SharedHelper.getKey(context, "currency") + "" + statusResponse.getJSONObject("payment").optString("tax"));
            txt04Total.setText(SharedHelper.getKey(context, "currency") + ""
                    + statusResponse.getJSONObject("payment").optString("total"));
            txt04AmountToPaid.setText(SharedHelper.getKey(context, "currency") + ""
                    + statusResponse.getJSONObject("payment").optString("total_amount_given"));
            txt04PaymentMode.setText(statusResponse.getString("payment_mode"));
            txt04Commision.setText(SharedHelper.getKey(context, "currency") + "" + statusResponse.getJSONObject("payment").optString("commision"));
            if (statusResponse.getString("payment_mode").equals("CASH")) {
                paymentTypeImg.setImageResource(R.drawable.money_icon);
            } else {
                paymentTypeImg.setImageResource(R.drawable.visa_icon);
            }
            try {
                JSONObject user = statusResponse.getJSONObject("user");
                if (user != null) {
                    if (!user.optString("mobile").equals("null")) {
                        SharedHelper.putKey(context, "provider_mobile_no", "" + user.optString(
                                "mobile"));
                    } else {
                        SharedHelper.putKey(context, "provider_mobile_no", "");
                    }
                    if (!user.optString("picture").equals("null")) {
                        //Glide.with(activity).load(URLHelper.base+"storage/"+user.getString
                        // ("picture")).placeholder(R.drawable.ic_dummy_user).error(R.drawable
                        // .ic_dummy_user).into(img03User);
                        if (user.optString("picture").startsWith("http"))
                            Picasso.get().load(user.getString("picture")).placeholder(R.drawable.ic_dummy_user).error(R.drawable.ic_dummy_user).into(img04User);
                        else
                            Picasso.get().load(URLHelper.base + "storage/" + user.getString("picture")).placeholder(R.drawable.ic_dummy_user).error(R.drawable.ic_dummy_user).into(img04User);
                    } else {
                        img04User.setImageResource(R.drawable.ic_dummy_user);
                    }
                    final User userProfile = this.user;
                    img04User.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent intent = new Intent(context, ShowProfile.class);
                            intent.putExtra("user", userProfile);
                            startActivity(intent);
                        }
                    });
                    txt04UserName.setText(user.optString("first_name") + " " + user.optString(
                            "last_name"));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void setValuesTo_ll_05_contentLayer_feedback(JSONArray status) {
        rat05UserRating.setRating(1.0f);
        feedBackRating = "1";
        rat05UserRating.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
            public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {
                utils.print("rating", rating + "");
                if (rating < 1.0f) {
                    rat05UserRating.setRating(1.0f);
                    feedBackRating = "1";
                }
                feedBackRating = String.valueOf((int) rating);
            }
        });
        JSONObject statusResponse;
        try {
            statusResponse = status.getJSONObject(0).getJSONObject("request");
            JSONObject user = statusResponse.getJSONObject("user");
            if (user != null) {
                lblProviderName.setText(context.getResources().getString(R.string.rate_your_trip) + " " + user.optString("first_name") + " " + user.optString("last_name"));
                if (!user.optString("picture").equals("null")) {
//                    new DownloadImageTask(img05User).execute(user.getString("picture"));
                    //Glide.with(activity).load(URLHelper.base+"storage/"+user.getString
                    // ("picture")).placeholder(R.drawable.ic_dummy_user).error(R.drawable
                    // .ic_dummy_user).into(img05User);
                    if (user.optString("picture").startsWith("http"))
                        Picasso.get().load(user.getString("picture")).placeholder(R.drawable.ic_dummy_user).error(R.drawable.ic_dummy_user).into(img05User);
                    else
                        Picasso.get().load(URLHelper.base + "storage/" + user.getString(
                                "picture")).placeholder(R.drawable.ic_dummy_user).error(R.drawable.ic_dummy_user).into(img05User);
                } else {
                    img05User.setImageResource(R.drawable.ic_dummy_user);
                }
                final User userProfile = this.user;
                img05User.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(context, ShowProfile.class);
                        intent.putExtra("user", userProfile);
                        startActivity(intent);
                    }
                });
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        feedBackComment = edt05Comment.getText().toString();
    }

    private void goOffLine(final String status, String id) {
        Log.d("currentStatus", CurrentStatus);
        customDialog = new CustomDialog(activity);
        customDialog.setCancelable(false);
        customDialog.show();
        if (status.equals("ONLINE")) {
            JSONObject param = new JSONObject();
            try {
                param.put("service_status", "offline");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST,
                    URLHelper.UPDATE_AVAILABILITY_API, param, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    customDialog.dismiss();
                    if (response != null) {
                        if (response.optJSONObject("service").optString("status").equalsIgnoreCase("offline")) {
                            try {
                                stopForegroundService();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            goOffline();
                        } else {
                            displayMessage(context.getResources().getString(R.string.something_went_wrong));
                        }
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    customDialog.dismiss();
                    utils.print("Error", error.toString());
                    errorHandler(error);
                }
            }) {
                @Override
                public java.util.Map<String, String> getHeaders() {
                    HashMap<String, String> headers = new HashMap<>();
                    headers.put("X-Requested-With", "XMLHttpRequest");
                    headers.put("Authorization", "Bearer " + token);
                    return headers;
                }
            };
            TopdriversApplication.getInstance().addToRequestQueue(jsonObjectRequest);
        }
    }

    private void driverGoOffLine() {
        Log.d("currentStatus", CurrentStatus);
        customDialog = new CustomDialog(activity);
        customDialog.setCancelable(false);
        customDialog.show();

         //if (status.equals("ONLINE"))
        {
            JSONObject param = new JSONObject();
            try {
                param.put("service_status", "offline");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST,
                    URLHelper.UPDATE_AVAILABILITY_API, param, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    customDialog.dismiss();
                    if (response != null) {
                        if (response.optJSONObject("service").optString("status").equalsIgnoreCase("offline")) {
                            try {
                                stopForegroundService();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            goOffline();
                        } else {
                            displayMessage(context.getResources().getString(R.string.something_went_wrong));
                        }
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    customDialog.dismiss();
                    utils.print("Error", error.toString());
                    errorHandler(error);
                }
            }) {
                @Override
                public java.util.Map<String, String> getHeaders() {
                    HashMap<String, String> headers = new HashMap<>();
                    headers.put("X-Requested-With", "XMLHttpRequest");
                    headers.put("Authorization", "Bearer " + token);
                    return headers;
                }
            };
            TopdriversApplication.getInstance().addToRequestQueue(jsonObjectRequest);
        }
    }

    private void update(final String status, String id) {
        Log.d("currentStatus", CurrentStatus);
        customDialog = new CustomDialog(activity);
        customDialog.setCancelable(false);
        customDialog.show();

        /*if (status.equals("ONLINE")) {
            JSONObject param = new JSONObject();
            try {
                param.put("service_status", "offline");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST,
                    URLHelper.UPDATE_AVAILABILITY_API, param, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    customDialog.dismiss();
                    if (response != null) {
                        if (response.optJSONObject("service").optString("status").equalsIgnoreCase("offline")) {
                            try {
                                stopForegroundService();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            goOffline();
                        } else {
                            displayMessage(context.getResources().getString(R.string.something_went_wrong));
                        }
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    customDialog.dismiss();
                    utils.print("Error", error.toString());
                    errorHandler(error);
                }
            }) {
                @Override
                public java.util.Map<String, String> getHeaders() {
                    HashMap<String, String> headers = new HashMap<>();
                    headers.put("X-Requested-With", "XMLHttpRequest");
                    headers.put("Authorization", "Bearer " + token);
                    return headers;
                }
            };
            TopdriversApplication.getInstance().addToRequestQueue(jsonObjectRequest);
        } else*/
        {
            System.out.println("----------------id-------------"+id);
            TopdriversApplication.getInstance().getRequestQueue().cancelAll("checkStatus");
            String url;
            JSONObject param = new JSONObject();
            System.out.println("-----------status------------"+status);
            if (status.equals("RATE")) {
                url = URLHelper.base + "api/provider/trip/" + id + "/rate";
                try {
                    param.put("rating", feedBackRating);
                    param.put("comment", edt05Comment.getText().toString());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                utils.print("Input", param.toString());
            } else {
                url = URLHelper.base + "api/provider/trip/" + id;
                System.out.println("------------id------------"+id);
                try {
                    param.put("_method", "PATCH");
                    if (status.equals("DROPPED")) {
                        System.out.println("---------------dropped----------"+givenTotalAmount);
                        param.put("total_amount_given", givenTotalAmount);
                    }
                    param.put("status", status);
                    if (SharedHelper.getKey(context, "is_track").equalsIgnoreCase("YES")) {
                        if (CurrentStatus.equalsIgnoreCase("COMPLETED")) {
                            param.put("address", getAddress(crt_lat, crt_lng));
                        }
                    }
                    if (CurrentStatus.equalsIgnoreCase("PICKEDUP") || CurrentStatus.equalsIgnoreCase("DROPPED")) {
                        param.put("latitude", crt_lat);
                        param.put("longitude", crt_lng);
                        System.out.println("---------params---------"+param.toString());
                        //param.put("status",  CurrentStatus);
                    }
                    Log.d("params", param.toString());
                    System.out.println("---------params---------"+param.toString());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            final JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST
                    , url, param, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {

                    if (ll_03_contentLayer_service_flow.getVisibility() == View.VISIBLE) {
                        TopdriversApplication.getInstance().isOpenningFlow = false;
                    }

                    customDialog.dismiss();
                    System.out.println("-----------response-----------"+response);
                    if (response.optJSONObject("requests") != null) {

                        utils.print("request", response.optJSONObject("requests").toString());
                    }
                    if (status.equals("RATE")) {
                        System.out.println("------dropped--------");
                        clearVisibility();
                        lnrGoOffline.setVisibility(View.VISIBLE);
                        destinationLayer.setVisibility(View.GONE);
                        LatLng myLocation = new LatLng(Double.parseDouble(crt_lat),
                                Double.parseDouble(crt_lng));
                        CameraPosition cameraPosition =
                                new CameraPosition.Builder().target(myLocation).zoom(17).build();
                         mMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
                        mapClear();

                        Intent mainIntent = new Intent(activity, MainActivity.class);
                        mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(mainIntent);
                        activity.finish();
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    customDialog.dismiss();
                    utils.print("Error", error.toString());
                    if (status.equals("RATE")) {
                        lnrGoOffline.setVisibility(View.VISIBLE);
                        destinationLayer.setVisibility(View.GONE);
                    }
                    System.out.println("------------ERROR----------------");
                    //errorHandler(error);
                }
            }) {
                @Override
                public java.util.Map<String, String> getHeaders() {
                    HashMap<String, String> headers = new HashMap<>();
                    headers.put("X-Requested-With", "XMLHttpRequest");
                    headers.put("Authorization", "Bearer " + token);
                    Log.d("token", token);
                    return headers;
                }
            };
            TopdriversApplication.getInstance().addToRequestQueue(jsonObjectRequest);
        }
    }

    public void cancelRequest(String id, String reason) {
        customDialog = new CustomDialog(context);
        customDialog.setCancelable(false);
        customDialog.show();
        JSONObject object = new JSONObject();
        try {
            object.put("id", id);
            object.put("cancel_reason", reason);
            Log.e("", "request_id" + id);
        } catch (Exception e) {
            e.printStackTrace();
        }
        TopdriversApplication.getInstance().getRequestQueue().cancelAll("checkStatus");
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST,
                URLHelper.CANCEL_REQUEST_API, object, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {

                if (ll_03_contentLayer_service_flow.getVisibility() == View.VISIBLE) {
                    TopdriversApplication.getInstance().isOpenningFlow = false;
                }

                customDialog.dismiss();
                utils.print("CancelRequestResponse", response.toString());
                Toast.makeText(context,
                        "" + context.getResources().getString(R.string.request_cancel),
                        Toast.LENGTH_SHORT).show();
                mapClear();
                clearVisibility();
                lnrGoOffline.setVisibility(View.VISIBLE);
                destinationLayer.setVisibility(View.GONE);
                CurrentStatus = "ONLINE";
                PreviousStatus = "NULL";
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                customDialog.dismiss();
                String json;
                NetworkResponse response = error.networkResponse;
                if (response != null && response.data != null) {
                    try {
                        JSONObject errorObj = new JSONObject(new String(response.data));
                        if (response.statusCode == 400 || response.statusCode == 405 || response.statusCode == 500) {
                            try {
                                displayMessage(errorObj.optString("message"));
                            } catch (Exception e) {
                                displayMessage(context.getResources().getString(R.string.something_went_wrong));
                                e.printStackTrace();
                            }
                        } else if (response.statusCode == 401) {
                            GoToBeginActivity();
                        } else if (response.statusCode == 422) {
                            json = trimMessage(new String(response.data));
                            if (json != null && !json.equals("")) {
                                displayMessage(json);
                            } else {
                                displayMessage(context.getResources().getString(R.string.please_try_again));
                            }
                        } else if (response.statusCode == 503) {
                            displayMessage(context.getResources().getString(R.string.server_down));
                        } else {
                            displayMessage(context.getResources().getString(R.string.please_try_again));
                        }
                    } catch (Exception e) {
                        displayMessage(context.getResources().getString(R.string.something_went_wrong));
                        e.printStackTrace();
                    }
                } else {
                    displayMessage(context.getResources().getString(R.string.please_try_again));
                }
            }
        }) {
            @Override
            public java.util.Map<String, String> getHeaders() {
                HashMap<String, String> headers = new HashMap<>();
                headers.put("X-Requested-With", "XMLHttpRequest");
                headers.put("Authorization", "Bearer " + SharedHelper.getKey(context,
                        "access_token"));
                Log.e("", "Access_Token" + SharedHelper.getKey(context, "access_token"));
                return headers;
            }
        };
        TopdriversApplication.getInstance().addToRequestQueue(jsonObjectRequest);
    }

    private void offLine() {
        customDialog = new CustomDialog(context);
        customDialog.setCancelable(false);
        try {
            customDialog.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
        /*TopdriversApplication.getInstance().getRequestQueue().cancelAll(new RequestQueue.RequestFilter() {
            @Override
            public boolean apply(Request<?> request) {
                return true;
            }
        });*/
        TopdriversApplication.getInstance().getRequestQueue().cancelAll("checkStatus");
        JSONObject param = new JSONObject();
        try {
            param.put("service_status", "offline");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST,
                URLHelper.UPDATE_AVAILABILITY_API, param, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    customDialog.dismiss();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (response != null) {
                    if (response.optJSONObject("service").optString("status").equalsIgnoreCase("offline")) {
                        goOffline();
                    } else {
                    }
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                try {
                    customDialog.dismiss();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }) {
            @Override
            public java.util.Map<String, String> getHeaders() {
                HashMap<String, String> headers = new HashMap<>();
                headers.put("X-Requested-With", "XMLHttpRequest");
                headers.put("Authorization", "Bearer " + SharedHelper.getKey(context, "access_token"));
                return headers;
            }
        };
        TopdriversApplication.getInstance().addToRequestQueue(jsonObjectRequest);
    }

    private void handleIncomingRequest(final String status, String id) {
        if (!((Activity) context).isFinishing()) {
            customDialog = new CustomDialog(activity);
            customDialog.setCancelable(false);
            customDialog.show();
            System.out.println("---------------disfinishing-------------------");
        }
        System.out.println("---------------dhandleincoming-------------------");
        TopdriversApplication.getInstance().getRequestQueue().cancelAll("checkStatus");
        String url = URLHelper.base + "api/provider/trip/" + id;
        if (status.equals("Accept")) {
            method = Request.Method.POST;
        } else {
            method = Request.Method.DELETE;
        }
        final JsonArrayRequest jsonObjectRequest = new JsonArrayRequest(method, url, null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        System.out.println("---------------dismiss previuos-------------------");
                        try {
                            customDialog.dismiss();
                            System.out.println("---------------dismiss-------------------");
                        } catch (Exception e) {
                            System.out.println("---------------dismiss exception-------------------");
                            e.printStackTrace();
                        }
                        if (status.equals("Accept")) {
                            System.out.println("-----------accept ride------------");
                            Toast.makeText(context,
                                    context.getResources().getString(R.string.request_accept),
                                    Toast.LENGTH_SHORT).show();

                            SharedHelper.putKey(context, "refuses", "0");
                        } else {
                            if (!timerCompleted) {
                                Toast.makeText(context,
                                        "" + context.getResources().getString(R.string.request_reject),
                                        Toast.LENGTH_SHORT).show();
                                String rejectCount = SharedHelper.getKey(context, "refuses");
                                if (rejectCount != null && rejectCount.length() > 0) {
                                    int refuseCountInt = Integer.parseInt(rejectCount);
                                    if (refuseCountInt == 1) {
                                       /* if (!((Activity) context).isFinishing()) {
                                            customDialog = new CustomDialog(activity);
                                            customDialog.setCancelable(false);
                                            customDialog.show();
                                        }*/
                                        offLine();
                                    } else {
                                        refuseCountInt = refuseCountInt + 1;
                                    }
                                    SharedHelper.putKey(context, "refuses", refuseCountInt + "");
                                } else {
                                    SharedHelper.putKey(context, "refuses", "1");
                                }
                            } else {
//                            Toast.makeText(context, ""+context.getResources().getString(R
//                            .string.request_time_out), Toast.LENGTH_SHORT).show();
                                //Toast.makeText(context, "" + context.getResources().getString(R
                                // .string
                                // .request_time_out), Toast.LENGTH_SHORT).show();
                               /* try {
                                    txt01Timer.setText("0");
                                    mapClear();
                                    clearVisibility();
                                    if (mPlayer != null && mPlayer.isPlaying()) {
                                       *//* mPlayer.stop();
                                        mPlayer = null;*//*
                                        stopPlaying();
                                    }
                                    ll_01_contentLayer_accept_or_reject_now.setVisibility(View.GONE);
                                    CurrentStatus = "ONLINE";
                                    PreviousStatus = "NULL";
                                    lnrGoOffline.setVisibility(View.VISIBLE);
                                    destinationLayer.setVisibility(View.GONE);
                                    timerCompleted = true;
                                    handleIncomingRequest("Reject", request_id);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }*/
                            }
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                try {
                    customDialog.dismiss();

                    if (!status.equalsIgnoreCase("Accept")) {
                        clearVisibility();
                        String rejectCount = SharedHelper.getKey(context, "refuses");
                        if (rejectCount != null && rejectCount.length() > 0) {
                            int refuseCountInt = Integer.parseInt(rejectCount);
                            if (refuseCountInt == 1) {
                               /* if (!((Activity) context).isFinishing()) {
                                    customDialog = new CustomDialog(activity);
                                    customDialog.setCancelable(false);
                                    customDialog.show();
                                }*/
                                offLine();
                            } else {
                                refuseCountInt = refuseCountInt + 1;
                            }
                            SharedHelper.putKey(context, "refuses", refuseCountInt + "");
                        } else {
                            SharedHelper.putKey(context, "refuses", "1");
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                utils.print("Error", error.toString());
            }
        }) {
            @Override
            public java.util.Map<String, String> getHeaders() {
                HashMap<String, String> headers = new HashMap<>();
                headers.put("X-Requested-With", "XMLHttpRequest");
                headers.put("Authorization", "Bearer " + token);
                return headers;
            }
        };
        TopdriversApplication.getInstance().addToRequestQueue(jsonObjectRequest);
    }

    private void handleIncomingRequestReject(final String status, String id) {
        if (!((Activity) context).isFinishing()) {
            customDialog = new CustomDialog(activity);
            customDialog.setCancelable(false);
            customDialog.show();
        }
        TopdriversApplication.getInstance().getRequestQueue().cancelAll("checkStatus");
        String url = URLHelper.base + "api/provider/trip/" + id;
        {
            method = Request.Method.DELETE;
        }
        final JsonArrayRequest jsonObjectRequest = new JsonArrayRequest(method, url, null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        try {
                            customDialog.dismiss();
                        } catch (Exception e) {
                            e.printStackTrace();
                            e.printStackTrace();
                        }
                        {
                            // if (!timerCompleted)
                            {
                                Toast.makeText(context,
                                        "" + context.getResources().getString(R.string.request_reject),
                                        Toast.LENGTH_SHORT).show();
                                String rejectCount = SharedHelper.getKey(context, "refuses");
                                if (rejectCount != null && rejectCount.equalsIgnoreCase("1")) {
                                    int refuseCountInt = Integer.parseInt(rejectCount);
                                    if (refuseCountInt == 1) {
                                       /* if (!((Activity) context).isFinishing()) {
                                            customDialog = new CustomDialog(activity);
                                            customDialog.setCancelable(false);
                                            customDialog.show();
                                        }*/
                                        offLine();
                                    } else {
                                        refuseCountInt = refuseCountInt + 1;
                                    }
                                    SharedHelper.putKey(context, "refuses", refuseCountInt + "");
                                } else {
                                    SharedHelper.putKey(context, "refuses", "1");
                                }

                                try {
                                    txt01Timer.setText("0");
                                    mapClear();
                                    clearVisibility();
                                    if (mPlayer != null && mPlayer.isPlaying()) {
                                       /* mPlayer.stop();
                                        mPlayer = null;*/
                                        stopPlaying();
                                    }
                                    ll_01_contentLayer_accept_or_reject_now.setVisibility(View.GONE);
                                    CurrentStatus = "ONLINE";
                                    PreviousStatus = "NULL";
                                    lnrGoOffline.setVisibility(View.VISIBLE);
                                    destinationLayer.setVisibility(View.GONE);
                                    timerCompleted = true;
                                    // handleIncomingRequest("Reject", request_id);
                                    // handleIncomingRequestReject("Reject", request_id);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }

                            } /*else {
//                            Toast.makeText(context, ""+context.getResources().getString(R
//                            .string.request_time_out), Toast.LENGTH_SHORT).show();
                                //Toast.makeText(context, "" + context.getResources().getString(R
                                // .string
                                // .request_time_out), Toast.LENGTH_SHORT).show();
                                try {
                                    txt01Timer.setText("0");
                                    mapClear();
                                    clearVisibility();
                                    if (mPlayer != null && mPlayer.isPlaying()) {
                                       *//* mPlayer.stop();
                                        mPlayer = null;*//*
                                        stopPlaying();
                                    }
                                    ll_01_contentLayer_accept_or_reject_now.setVisibility(View.GONE);
                                    CurrentStatus = "ONLINE";
                                    PreviousStatus = "NULL";
                                    lnrGoOffline.setVisibility(View.VISIBLE);
                                    destinationLayer.setVisibility(View.GONE);
                                    timerCompleted = true;
                                   // handleIncomingRequest("Reject", request_id);
                                    handleIncomingRequestReject("Reject", request_id);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }*/
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                try {
                    customDialog.dismiss();

                    if (!status.equalsIgnoreCase("Accept")) {
                        clearVisibility();
                        String rejectCount = SharedHelper.getKey(context, "refuses");
                        if (rejectCount != null && rejectCount.length() > 0) {
                            int refuseCountInt = Integer.parseInt(rejectCount);
                            if (refuseCountInt == 1) {
                               /* if (!((Activity) context).isFinishing()) {
                                    customDialog = new CustomDialog(activity);
                                    customDialog.setCancelable(false);
                                    customDialog.show();
                                }*/
                                offLine();
                            } else {
                                refuseCountInt = refuseCountInt + 1;
                            }
                            SharedHelper.putKey(context, "refuses", refuseCountInt + "");
                        } else {
                            SharedHelper.putKey(context, "refuses", "1");
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                utils.print("Error", error.toString());
            }
        }) {
            @Override
            public java.util.Map<String, String> getHeaders() {
                HashMap<String, String> headers = new HashMap<>();
                headers.put("X-Requested-With", "XMLHttpRequest");
                headers.put("Authorization", "Bearer " + token);
                return headers;
            }
        };
        TopdriversApplication.getInstance().addToRequestQueue(jsonObjectRequest);
    }

    public void errorHandler(VolleyError error) {
        utils.print("Error", error.toString());
        String json;
        NetworkResponse response = error.networkResponse;
        if (response != null && response.data != null) {
            try {
                JSONObject errorObj = new JSONObject(new String(response.data));
                utils.print("ErrorHandler", "" + errorObj.toString());
                if (response.statusCode == 400 || response.statusCode == 405 || response.statusCode == 500) {
                    try {
                        displayMessage(errorObj.optString("message"));
                    } catch (Exception e) {
                        displayMessage(context.getResources().getString(R.string.something_went_wrong));
                    }
                } else if (response.statusCode == 401) {
                    SharedHelper.putKey(context, "loggedIn",
                            context.getResources().getString(R.string.False));
                    GoToBeginActivity();
                } else if (response.statusCode == 422) {
                    json = TopdriversApplication.trimMessage(new String(response.data));
                    if (json != null && !json.equals("")) {
                        displayMessage(json);
                    } else {
                        displayMessage(context.getResources().getString(R.string.please_try_again));
                    }
                } else if (response.statusCode == 503) {
                    displayMessage(context.getResources().getString(R.string.server_down));
                } else {
                    displayMessage(context.getResources().getString(R.string.please_try_again));
                }
            } catch (Exception e) {
                displayMessage(context.getResources().getString(R.string.something_went_wrong));
            }
        } else {
            displayMessage(context.getResources().getString(R.string.please_try_again));
        }
    }

    public void displayMessage(String toastString) {
        utils.print("displayMessage", "" + toastString);
        Snackbar.make(getView(), toastString, Snackbar.LENGTH_SHORT).setAction("Action", null).show();
    }

    public void GoToBeginActivity() {
        SharedHelper.putKey(activity, "loggedIn", context.getResources().getString(R.string.False));
        Intent mainIntent = new Intent(activity, WelcomeScreenActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(mainIntent);
        activity.finish();
    }

    public void goOffline() {
        //try {
           /*FragmentManager manager = MainActivity.fragmentManager;
            FragmentTransaction transaction = manager.beginTransaction();
            transaction.replace(R.id.content, new Offline());
            transaction.commitAllowingStateLoss();*/
        if (getActivity() != null) {
            SharedHelper.putKey(context, "refuses", "0");
            SharedHelper.putKey(context, "offline", "1");
            Intent mainIntent = new Intent(getActivity(), MainActivity.class);
            mainIntent.putExtra("offline", true);
            mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            getActivity().startActivity(mainIntent);
            getActivity().finish();
        }
        /*} catch (Exception e) {
            e.printStackTrace();
        }*/
    }

    @Override
    public void onDestroy() {
        if (mPlayer != null && mPlayer.isPlaying()) {
           /* mPlayer.stop();
            mPlayer = null;*/
            stopPlaying();

        }
        ha.removeCallbacksAndMessages(null);
        super.onDestroy();
    }

    private String getMonth(String date) throws ParseException {
        Date d = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH).parse(date);
        Calendar cal = Calendar.getInstance();
        cal.setTime(d);
        return new SimpleDateFormat("MMM").format(cal.getTime());
    }

    private String getDate(String date) throws ParseException {
        Date d = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH).parse(date);
        Calendar cal = Calendar.getInstance();
        cal.setTime(d);
        return new SimpleDateFormat("dd").format(cal.getTime());
    }

    private String getYear(String date) throws ParseException {
        Date d = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH).parse(date);
        Calendar cal = Calendar.getInstance();
        cal.setTime(d);
        return new SimpleDateFormat("yyyy").format(cal.getTime());
    }

    private String getTime(String date) throws ParseException {
        Date d = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH).parse(date);
        Calendar cal = Calendar.getInstance();
        cal.setTime(d);
        return new SimpleDateFormat("hh:mm a").format(cal.getTime());
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_LOCATION) {
            if (resultCode == Activity.RESULT_CANCELED) {
                Toast.makeText(context, "Request Cancelled", Toast.LENGTH_SHORT).show();
            }
        }
        if (requestCode == CODE_DRAW_OVER_OTHER_APP_PERMISSION) {
            showCustomFloatingView(getActivity(), false);
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
       /* if (requestCode == HyperTrack.REQUEST_CODE_LOCATION_SERVICES) {
            if (resultCode != Activity.RESULT_OK) {
                // Handle Enable Location Services request denied error
                Toast.makeText(context, "Kindly ensure location is enabled", Toast.LENGTH_SHORT).show();
            }*//* else {
                // Check if Location Settings are enabled to proceed
//                checkForLocationSettings();
            }*//*
        }*/
    }

    public String getAddress(String strLatitude, String strLongitude) {
        Geocoder geocoder;
        List<Address> addresses;
        geocoder = new Geocoder(getActivity(), Locale.getDefault());
        String address = "", city = "";
        try {
            double latitude = Double.parseDouble(strLatitude);
            double longitude = Double.parseDouble(strLongitude);
            addresses = geocoder.getFromLocation(latitude, longitude, 1); // Here 1 represent max
            // location result to returned, by documents it recommended 1 to 5
            address = addresses.get(0).getAddressLine(0); // If any additional address line
            // present than only, check with max available address lines by getMaxAddressLineIndex()
            city = addresses.get(0).getLocality();
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (address.length() > 0 || city.length() > 0)
            return address + ", " + city;
        else
            return context.getResources().getString(R.string.no_address);
    }

    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager =
                (ActivityManager) activity.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service :
                manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void onPause() {
        super.onPause();
        if (customDialog != null) {
            if (customDialog.isShowing()) {
                customDialog.dismiss();
            }
        }

        if (ll_03_contentLayer_service_flow.getVisibility() == View.VISIBLE &&
                btn_01_status.getText().equals(context.getResources().getString(R.string.tap_when_arrived))) {
            TopdriversApplication.getInstance().isOpenningFlow = true;
        }

        isBackground = true;
        isFromResume = false;
        // Remove location updates
//            mKalmanLocationManager.removeUpdates(mLocationListener);
        if (ha != null) {
            ha.removeCallbacksAndMessages(null);
        }
    }

    private void showCancelDialog() {
        AlertDialog.Builder builder;
        builder = new AlertDialog.Builder(context);
        builder.setTitle(context.getResources().getString(R.string.cancel_confirm));
        builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                showReasonDialog();
            }
        });
        builder.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //Reset to previous seletion menu in navigation
                dialog.dismiss();
            }
        });
        builder.setCancelable(false);
        cancelDialog = builder.create();
        cancelDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface arg) {
            }
        });
        cancelDialog.show();
    }

    private void showReasonDialog() {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(context);
        LayoutInflater inflater = activity.getLayoutInflater();
        View view = inflater.inflate(R.layout.cancel_dialog, null);
        Button submitBtn = view.findViewById(R.id.submit_btn);
        final EditText reason = view.findViewById(R.id.reason_etxt);
        builder.setView(view);
        cancelReasonDialog = builder.create();
        cancelReasonDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        submitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cancelReasonDialog.dismiss();
                if (reason.getText().toString().length() > 0)
                    cancelRequest(request_id, reason.getText().toString());
                else
                    cancelRequest(request_id, "");
            }
        });
        cancelReasonDialog.show();
    }

    private void showSosDialog() {
        AlertDialog.Builder builder;
        builder = new AlertDialog.Builder(context);
        builder.setTitle(context.getResources().getString(R.string.sos_confirm));
        builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //cancelRequest(request_id);
                dialog.dismiss();
                String mobile = SharedHelper.getKey(context, "sos");
                if (mobile != null && !mobile.equalsIgnoreCase("null") && mobile.length() > 0) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        requestPermissions(new String[]{Manifest.permission.CALL_PHONE}, 3);
                    } else {
                        Intent intent = new Intent(Intent.ACTION_CALL);
                        intent.setData(Uri.parse("tel:" + mobile));
                        startActivity(intent);
                    }
                } else {
                    displayMessage(context.getResources().getString(R.string.user_no_mobile));
                }

            }
        });
        builder.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //Reset to previous seletion menu in navigation
                dialog.dismiss();
            }
        });
        builder.setCancelable(false);
        AlertDialog dialog = builder.create();
        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface arg) {
            }
        });
        dialog.show();
    }

    @Override
    public void onResume() {
        super.onResume();

        isBackground = false;
        isFromResume = true;
        try {
            PreviousStatus = "";
            NotificationManager notificationManager =
                    (NotificationManager) getActivity().getSystemService(NOTIFICATION_SERVICE);
            notificationManager.cancelAll();

            if (openAcceptScreen && !TopdriversApplication.getInstance().isOpenRideAccept) {
                scheduleTrip = false;
                timerCompleted = true;
                Intent intent = new Intent(MainActivity.mMyApp, RideAcceptandRejectActivity.class);
                intent.putExtra("statusResponses", statusResponses.toString());
                intent.putExtra("requestId", request_id);
                MainActivity.mMyApp.startActivity(intent);
                TopdriversApplication.getInstance().isOpenRideAccept = true;
            }

           /* try {
                ha.removeCallbacksAndMessages(null);
            } catch (Exception e) {
                e.printStackTrace();
            }*/
            ha.postDelayed(new Runnable() {
                @Override
                public void run() {
                    //call function
                    try {
                      /*  TopdriversApplication.getInstance().getRequestQueue().cancelAll(new RequestQueue.RequestFilter() {
                            @Override
                            public boolean apply(Request<?> request) {
                                return true;
                            }
                        });*/
                        checkStatus();

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    ha.postDelayed(this, 500);
                }
            }, 500);

//            if (openArrivedScreen) {
//                trickShowUI();
//                openArrivedScreen = false;
//            }

            /*if (ContextCompat.checkSelfPermission(context,
                    Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED && mGoogleApiClient != null &&
                    mGoogleApiClient.isConnected()) {
                mKalmanLocationManager.requestLocationUpdates(
                        KalmanLocationManager.UseProvider.GPS_AND_NET, FILTER_TIME, GPS_TIME,
                        NET_TIME, mLocationListener, true);
            }*/
           /* if (!HyperTrack.checkLocationPermission(context)) {
                HyperTrack.requestPermissions(activity);
                return;
            }
            if (!HyperTrack.checkLocationServices(context)) {
                HyperTrack.requestLocationServices(activity);
            }*/
//            updateuser();
//            CreateAction();
            context.stopService(new Intent(context, CustomFloatingViewService.class));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

   /* private void CreateAction() {
        ActionParamsBuilder actionParamsBuilder = new ActionParamsBuilder();
        actionParamsBuilder.setType(Action.TYPE_VISIT);
        HyperTrack.createAction(actionParamsBuilder.build(), new HyperTrackCallback() {
            @Override
            public void onSuccess(@NonNull SuccessResponse response) {
                Action action = (Action) response.getResponseObject();
//                saveVisitAction(action);
            }

            @Override
            public void onError(@NonNull ErrorResponse errorResponse) {
            }
        });
    }

    private void updateuser() {
        UserParams userParams = new UserParams().setName(SharedHelper.getKey(context, "first_name"
        ) + " " + SharedHelper.getKey(context, "last_name")).setPhone(SharedHelper.getKey(context
                , "mobile")).setUniqueId(SharedHelper.getKey(context, "id"));
        HyperTrack.getOrCreateUser(userParams, new HyperTrackCallback() {
            @Override
            public void onSuccess(@NonNull SuccessResponse successResponse) {
                User user = (User) successResponse.getResponseObject();
            }

            @Override
            public void onError(@NonNull ErrorResponse errorResponse) {

            }
        });
    }*/

    @Override
    public void onStop() {
        super.onStop();
        // ha.removeCallbacks();
    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(context)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();
    }

    private void updateLiveTracking(String latitude, String longitude) {
        ApiInterface mApiInterface =
                RetrofitClient.getLiveTrackingClient().create(ApiInterface.class);
        Call<ResponseBody> call = mApiInterface.getLiveTracking("XMLHttpRequest",
                "Bearer " + token, request_id, latitude, longitude);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call,
                                   retrofit2.Response<ResponseBody> response) {
                Log.e("sUCESS", "SUCESS" + response.body());
                if (response.body() != null) {
                    try {
                        String bodyString = new String(response.body().bytes());
                        Log.e("sUCESS", "bodyString" + bodyString);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
            }
        });
    }

    /**
     * Set and initialize the view elements.
     */
    private void initializeView() {
        context.startService(new Intent(context, FloatingViewService.class));
        activity.finish();
    }

    private void showCustomFloatingView(Context context, boolean isShowOverlayPermission) {
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP_MR1) {
            final Intent intent = new Intent(context, CustomFloatingViewService.class);
            ContextCompat.startForegroundService(context, intent);
            return;
        }
        if (Settings.canDrawOverlays(context)) {
            final Intent intent = new Intent(context, CustomFloatingViewService.class);
            ContextCompat.startForegroundService(context, intent);
            return;
        }
        if (isShowOverlayPermission) {
            final Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:" + context.getPackageName()));
            startActivityForResult(intent, CODE_DRAW_OVER_OTHER_APP_PERMISSION);
        }
    }

    private void confirmAmount(final String status, final String requestId, String amount) {
        try {
            androidx.appcompat.app.AlertDialog.Builder rideOthersDialogue =
                    new androidx.appcompat.app.AlertDialog.Builder(getActivity());
            LayoutInflater inflater = this.getLayoutInflater();
            final View mobileView = inflater.inflate(R.layout.confirm_amount, null);
            rideOthersDialogue.setView(mobileView);
            rideOthersDialogue.setCancelable(false);
            final TextView total_amount = mobileView.findViewById(R.id.total_amount);
            final EditText total_amount_given = mobileView.findViewById(R.id.total_amount_given);
            total_amount.setText(SharedHelper.getKey(context, "currency") + amount);
            total_amount_given.setText(amount);
            final TextView confirm_amount_submit =
                    mobileView.findViewById(R.id.confirm_amount_submit);
            /*total_amount_given.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                    total_amount.setText(charSequence + "");
                }

                @Override
                public void afterTextChanged(Editable editable) {

                }
            });*/
            viewDialog = rideOthersDialogue.create();
            /*total_amount.setText(amount + "");*/
            confirm_amount_submit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (total_amount_given.getText().toString().trim().isEmpty()) {
                        Toast.makeText(
                                getActivity().getApplicationContext(),
                                getString(R.string.error_confirmation_amount),
                                Toast.LENGTH_SHORT
                        ).show();
                    } else {
                        viewDialog.dismiss();
                        givenTotalAmount = total_amount_given.getText().toString();
                        update(status, requestId);

                    }
                }
            });
            viewDialog.show();
        } catch (Exception mobileNoException) {
            mobileNoException.printStackTrace();
        }
    }

    // Fetches data from url passed
    private class FetchUrl extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... url) {
            // For storing data from web service
            String data = "";
            try {
                // Fetching the data from web service
                data = downloadUrl(url[0]);
                Log.d("Background Task data", data.toString());
            } catch (Exception e) {
                Log.d("Background Task", e.toString());
            }
            return data;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            if (TextUtils.isEmpty(result)) {
                isPickupDirectionFetched = false;
                isDropDirectionFetched = false;
                return;
            }
            parserTask = new ParserTask();
            // Invokes the thread for parsing the JSON data
            parserTask.execute(result);
        }
    }

    /**
     * A class to parse the Google Places in JSON format
     */
    private class ParserTask extends AsyncTask<String, Integer,
            List<List<HashMap<String, String>>>> {

        // Parsing the data in non-ui thread
        @Override
        protected List<List<HashMap<String, String>>> doInBackground(String... jsonData) {
            JSONObject jObject;
            List<List<HashMap<String, String>>> routes = null;
            try {
                jObject = new JSONObject(jsonData[0]);
                Log.d("ParserTask", jsonData[0]);
                DataParser parser = new DataParser();
                Log.d("ParserTask", parser.toString());
                // Starts parsing data
                routes = parser.parse(jObject);
                Log.d("ParserTask", "Executing routes");
                Log.d("ParserTask", routes.toString());
            } catch (Exception e) {
                Log.d("ParserTask", e.toString());
                isPickupDirectionFetched = false;
                isDropDirectionFetched = false;
                e.printStackTrace();
            }
            return routes;
        }

        // Executes in UI thread, after the parsing process
        @Override
        protected void onPostExecute(List<List<HashMap<String, String>>> result) {
            if (result.isEmpty()) {
                isPickupDirectionFetched = false;
                isDropDirectionFetched = false;
                return;
            }
            ArrayList<LatLng> points;
            PolylineOptions lineOptions = null;
            // Traversing through all the routes
            for (int i = 0; i < result.size(); i++) {
                points = new ArrayList<>();
                lineOptions = new PolylineOptions();
                // Fetching i-th route
                List<HashMap<String, String>> path = result.get(i);
                // Fetching all the points in i-th route
                for (int j = 0; j < path.size(); j++) {
                    HashMap<String, String> point = path.get(j);
                    double lat = Double.parseDouble(point.get("lat"));
                    double lng = Double.parseDouble(point.get("lng"));
                    LatLng position = new LatLng(lat, lng);
                    points.add(position);
                }
                if (sourceMarker != null)
                    sourceMarker.remove();
                if (destinationMarker != null)
                    destinationMarker.remove();
                MarkerOptions markerOptions = new MarkerOptions().title("Source")
                        .anchor(0.5f, 0.5f)
                        .position(sourceLatLng).icon(BitmapDescriptorFactory.fromResource(R.drawable.user_marker));
                sourceMarker = mMap.addMarker(markerOptions);
                MarkerOptions markerOptions1 = new MarkerOptions().title("Destination")
                        .anchor(0.5f, 0.5f)
                        .position(destLatLng).icon(BitmapDescriptorFactory.fromResource(R.drawable.provider_marker));
                destinationMarker = mMap.addMarker(markerOptions1);
                LatLngBounds.Builder builder = new LatLngBounds.Builder();
                LatLngBounds bounds;
                builder.include(sourceLatLng);
                builder.include(destLatLng);
                if (CurrentStatus.equalsIgnoreCase("STARTED")) {
                    CameraPosition cameraPosition =
                            new CameraPosition.Builder().target(sourceLatLng).zoom(17).build();
                    MarkerOptions markerOptionsq = new MarkerOptions();
                    markerOptionsq.position(sourceLatLng);
                    mMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
                } else {
                    bounds = builder.build();
                    int padding = 320; // offset from edges of the map in pixels
                    CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, padding);
                    try {
                        mMap.moveCamera(cu);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                mMap.getUiSettings().setMapToolbarEnabled(false);
                // Adding all the points in the route to LineOptions
                lineOptions.addAll(points);
                lineOptions.width(10);
                lineOptions.color(Color.parseColor(context.getResources().getString(0 + R.color.colorAccent)));
                Log.d("onPostExecute", "onPostExecute lineoptions decoded");
            }
            // Drawing polyline in the Google Map for the i-th route
            if (lineOptions != null) {
                if (polyline != null)
                    polyline.remove();
                polyline = mMap.addPolyline(lineOptions);
            } else {
                Log.d("onPostExecute", "without Polylines drawn");
            }
        }
    }


    public void payNow() {

        customDialog = new CustomDialog(context);
        customDialog.setCancelable(false);
        if (customDialog != null)
            customDialog.show();
        TopdriversApplication.getInstance().getRequestQueue().cancelAll("checkStatus");
        JSONObject object = new JSONObject();
        try {
            object.put("request_id", request_id/* SharedHelper.getKey(context, "request_id")*/);
            object.put("payment_mode", "CARD");
            object.put("is_paid", "0");
        } catch (Exception e) {
            e.printStackTrace();
        }
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST,
                URLHelper.PAY_NOW_API, object, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                utils.print("PayNowRequestResponse", response.toString());
                if ((customDialog != null) && (customDialog.isShowing()))
                    customDialog.dismiss();

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if ((customDialog != null) && (customDialog.isShowing()))
                    customDialog.dismiss();
                String json = "";
                NetworkResponse response = error.networkResponse;
                if (response != null && response.data != null) {
                    try {
                        JSONObject errorObj = new JSONObject(new String(response.data));

                        if (response.statusCode == 400 || response.statusCode == 405 || response.statusCode == 500) {
                            try {
                                displayMessage(errorObj.optString(
                                        "message"));
                            } catch (Exception e) {
                                displayMessage(
                                        context.getResources().getString(R.string.something_went_wrong));
                            }
                        } else if (response.statusCode == 401) {
                            // refreshAccessToken("PAY_NOW");
                            getToken("PAY_NOW");
                        } else if (response.statusCode == 422) {

                            json = trimMessage(new String(response.data));
                            if (json != "" && json != null) {
                                displayMessage(json);
                            } else {
                                displayMessage(
                                        context.getResources().getString(R.string.please_try_again));
                            }
                        } else if (response.statusCode == 503) {
                            displayMessage(
                                    context.getResources().getString(R.string.server_down));
                        } else {
                            displayMessage(
                                    context.getResources().getString(R.string.please_try_again));
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                        displayMessage(
                                context.getResources().getString(R.string.something_went_wrong));
                    }

                } else {
                    displayMessage(
                            context.getResources().getString(R.string.please_try_again));
                }
            }
        }) {
            @Override
            public java.util.Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<String, String>();
                /* headers.put("Authorization", "Bearer " *//*+ SharedHelper.getKey(context, "token_type") + " "*//*
                        + "eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiIsImp0aSI6IjdiNTdmNmQwYzMwMzM0NzVhOWVlNDY1ZGM2YmRiMWRjYzA1ZGJhNzI0NDFlNDNjMjdmYmM5NzZhYzg4NWRkMGQ5OTk5ZjYyM2VhODU5MDIxIn0.eyJhdWQiOiIzIiwianRpIjoiN2I1N2Y2ZDBjMzAzMzQ3NWE5ZWU0NjVkYzZiZGIxZGNjMDVkYmE3MjQ0MWU0M2MyN2ZiYzk3NmFjODg1ZGQwZDk5OTlmNjIzZWE4NTkwMjEiLCJpYXQiOjE1ODQxNjkxOTQsIm5iZiI6MTU4NDE2OTE5NCwiZXhwIjoxODk5NzAxOTk0LCJzdWIiOiIzOTAiLCJzY29wZXMiOltdfQ.MrOQra2GsphLIAzHwYxxNYwSTXaiXEWh_hNVyRdnz2-y6QJwOF1JGHmCvDkFqOQwRBJFxo34_SJt_rtAABbXRQTdRIitj97P9Drjz1QPvKFb4Db6KrtISwornhloMnRDbGRCFgbtYoc4SXjOM6Tp-x_leKyEk7ZiS3NnJ7tAQQKLweSfWljqpd17Hw6P55h-KsGkNZ2o74x947qBz8JeWKy0bS7DDD3ra1rSTkrvnYvZ2aNn_BhH6j_tokaXNFyfARLm9U3IwUOkhRf6_xTZU4eMuKQCICIoXV789x1Nx8IBtyOzEmVGV99EngZ1ZEv1QWB2_j2eZqJJ4coeczyi3gEwPgFo8uxvSqll_tmLtfZx1iTU-Zwu3Gf0AOiu5c1k6y0LeGBO39agnKKvVuisbFtg3CD8Uy4J98PE7MJUX2w4l-Aqt0e9KCbSXDHoYzuHiZuyzMlN_NiS1wSy6ls_GOCloT20SoblJ4sWtLyaK53wPMA0U7hWL6p8FTSRigLKEj6RxQR4xgaEFcDRktj2UyDq1qne5eYNnguJkbzDxga6Qbsur9A0B6uMukUYhGKSjO0t4uYlxBtCHcoGBBKjm3g0_r43BYGHYb9zQJ2X_uWbHZu4Sb-DbuxECzpaOtbZUQodogOCJX9CIILokdFkQ1PL4DeEvyfrEVj3BMn2r3k"*//*SharedHelper.getKey(context, "access_token")*//*);
                 */
                headers.put("X-Requested-With", "XMLHttpRequest");
                headers.put("Authorization", "Bearer " + "eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiIsImp0aSI6IjUwMjI0NTI3MmVkMTUzOWEwMTg5ZTM5NjBiMzMwMWJiMzYxYTdlYzk3NGE4OWM0ODhhZDcwYzU3Y2Y4NjJlNzdmOGI3NzYyOGU0NzcxNTJiIn0.eyJhdWQiOiIzIiwianRpIjoiNTAyMjQ1MjcyZWQxNTM5YTAxODllMzk2MGIzMzAxYmIzNjFhN2VjOTc0YTg5YzQ4OGFkNzBjNTdjZjg2MmU3N2Y4Yjc3NjI4ZTQ3NzE1MmIiLCJpYXQiOjE2MDY0NzAxOTAsIm5iZiI6MTYwNjQ3MDE5MCwiZXhwIjoxOTIyMDAyOTkwLCJzdWIiOiIzMzYiLCJzY29wZXMiOltdfQ.p0Dw4dFODFlHJynJXE-lJUbXj9bAWeBsAwcNeY0SlEPzE7oRsBmx5gIg8pdg_fZvvteOz4TqH3hoKxUksO_MdxYzVbMV69KPjktex7tTlTQ06VWfQQeJIXDPu5h_iRGLanIU0eLt5EjBdU1bBaYbBg-fhfWS0PBNylIS3k_KLJzEv_g823H4lQ9b8JNzbX_g6-kodh4tzDOHwj_kxAgtsnVzeT0V0cK3dls7A4x6P_Zz0R7JGrfmPJOhNGNE76p-IYXHUS8M9QdoisXuw6UH9Xvhi_RF_nKsTwMTKb9SydNmvpLpu4VDGDg7xdU02uXi4qBIZHxz6_RRF-W80KKhHUNNNO6dpa3YlSykFP-xgcU7iyl8CPA-5Ii-_DivuG0a_4W9-aRLdBW8WXDl8IWbNuTppkgrlEJKbVZXiEgmnrQnsrMswm3hj361wuSXNf-BzABXYag1cbjM3tqhXY3Sn5kP-x9u2kAjZPz2d-5e8W5EB5D4G8W35rX4oOzflAGtvESQ06xX5jtDZY2JEbKR9elsjnSgz0Lqk8ItKeye8_o5uXxgszoDSxQ10RPaUn0Jvo1y67wSc2DWJJJGr9blJyaHiTQ1TS7ZxBwvqjn0SYRKMtFEw4cdQyXvtw-RmXes1Gd6EKf3hhPaGQ3c8Fnl5VFzF_3QXjzVA44esWXVhkA");
                return headers;
            }
        };

        TopdriversApplication.getInstance().addToRequestQueue(jsonObjectRequest);

    }

    private void refreshAccessToken(final String tag) {

        JSONObject object = new JSONObject();
        try {

            object.put("grant_type", "refresh_token");
            object.put("client_id", URLHelper.client_id);
            object.put("client_secret", URLHelper.client_secret);
            object.put("refresh_token", SharedHelper.getKey(context, "refresh_token"));
            object.put("scope", "");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST,
                URLHelper.loginCar, object, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                utils.print("SignUpResponse", response.toString());
                SharedHelper.putKey(context, "access_token", response.optString("access_token"));
                SharedHelper.putKey(context, "refresh_token", response.optString("refresh_token"));
                SharedHelper.putKey(context, "token_type", response.optString("token_type"));

                if (tag.equalsIgnoreCase("PAY_NOW")) {
                    payNow();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                String json = "";
                NetworkResponse response = error.networkResponse;

                if (response != null && response.data != null) {
                    SharedHelper.putKey(context, "loggedIn",
                            context.getResources().getString(R.string.False));
                    // utils.GoToBeginActivity(getActivity());
                }
            }
        }) {


            @Override
            public java.util.Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<String, String>();
                headers.put("X-Requested-With", "XMLHttpRequest");
                headers.put("Authorization", "" + SharedHelper.getKey(context, "token_type") + " "
                        + SharedHelper.getKey(context, "access_token"));
                return headers;
            }
        };

        TopdriversApplication.getInstance().addToRequestQueue(jsonObjectRequest);
    }

    private void trickShowUI(JSONArray status) {
        TopdriversApplication.getInstance().getRequestQueue().cancelAll("checkStatus");
        ll_03_contentLayer_service_flow.setVisibility(View.VISIBLE);
        Trick_setValuesTo_ll_03_contentLayer_service_flow(status);
        lnrGoOffline.setVisibility(View.GONE);

        btn_01_status.setText(context.getResources().getString(R.string.tap_when_arrived));
        CurrentStatus = "ARRIVED";
        sos.setVisibility(View.GONE);
        img03Status1.setImageResource(R.drawable.arrived);
        img03Status2.setImageResource(R.drawable.pickup);
        sos.setVisibility(View.GONE);
        btn_cancel_ride.setVisibility(View.VISIBLE);
        destinationLayer.setVisibility(View.VISIBLE);
        topSrcDestTxtLbl.setText(context.getResources().getString(R.string.pick_up));
    }

    private void getToken(final String tag) {
        StringRequest stringRequest = new StringRequest(Request.Method.POST, URLHelper.PAY_NOW_API_token,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject obj = new JSONObject(response);
                            JSONObject jsonObject = obj.getJSONObject("success");
                            SharedHelper.putKey(context, "access_token_payment", jsonObject.optString("token"));
                            if (tag.equalsIgnoreCase("PAY_NOW")) {
                                payNow();
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        utils.print("token", response);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        utils.print("token", error.toString());
                    }
                }) {
            @Override
            protected java.util.Map<String, String> getParams() {
                java.util.Map<String, String> params = new HashMap<String, String>();
                params.put("email", SharedHelper.getKey(context, "email"));
                params.put("password", SharedHelper.getKey(context, "password"));
                return params;
            }

        };

        TopdriversApplication.getInstance().addToRequestQueue(stringRequest);
    }

    public void getUpcomingDetails(JSONObject jsonObject) {

        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(URLHelper.UPCOMING_TRIP_DETAILS + "?request_id=" + jsonObject.optString("id"), new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                trickShowUI(response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                String json = null;
                String Message;
                NetworkResponse response = error.networkResponse;
                if (response != null && response.data != null) {

                    try {
                        JSONObject errorObj = new JSONObject(new String(response.data));

                        if (response.statusCode == 400 || response.statusCode == 405 || response.statusCode == 500) {
                            try {
                                displayMessage(errorObj.optString("message"));
                            } catch (Exception e) {
                                displayMessage(getString(R.string.something_went_wrong));
                                e.printStackTrace();
                            }

                        } else if (response.statusCode == 401) {
                            GoToBeginActivity();
                        } else if (response.statusCode == 422) {

                            json = trimMessage(new String(response.data));
                            if (json != "" && json != null) {
                                displayMessage(json);
                            } else {
                                displayMessage(getString(R.string.please_try_again));
                            }
                        } else if (response.statusCode == 503) {
                            displayMessage(getString(R.string.server_down));

                        } else {
                            displayMessage(getString(R.string.please_try_again));

                        }

                    } catch (Exception e) {
                        displayMessage(getString(R.string.something_went_wrong));
                        e.printStackTrace();
                    }

                } else {
                    if (error instanceof NoConnectionError) {
                        displayMessage(getString(R.string.oops_connect_your_internet));
                    } else if (error instanceof NetworkError) {
                        displayMessage(getString(R.string.oops_connect_your_internet));
                    }
                }
            }
        }) {
            @Override
            public java.util.Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<String, String>();
                headers.put("X-Requested-With", "XMLHttpRequest");
                headers.put("Authorization", "Bearer " + SharedHelper.getKey(context, "access_token"));
                return headers;
            }
        };

        TopdriversApplication.getInstance().addToRequestQueue(jsonArrayRequest);
    }

    public void checkUpcomingList() {

        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(URLHelper.UPCOMING_TRIPS, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {

                if (response != null && response.length() > 0) {
                    try {
                        getUpcomingDetails(response.getJSONObject(response.length() - 1));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                String json = null;
                String Message;
                NetworkResponse response = error.networkResponse;
                if (response != null && response.data != null) {
                    try {
                        JSONObject errorObj = new JSONObject(new String(response.data));

                        if (response.statusCode == 400 || response.statusCode == 405 || response.statusCode == 500) {
                            try {
                                displayMessage(errorObj.optString("message"));
                            } catch (Exception e) {
                                displayMessage(getString(R.string.something_went_wrong));
                            }

                        } else if (response.statusCode == 401) {
                            GoToBeginActivity();
                        } else if (response.statusCode == 422) {

                            json = TopdriversApplication.trimMessage(new String(response.data));
                            if (json != "" && json != null) {
                                displayMessage(json);
                            } else {
                                displayMessage(getString(R.string.please_try_again));
                            }
                        } else if (response.statusCode == 503) {
                            displayMessage(getString(R.string.server_down));

                        } else {
                            displayMessage(getString(R.string.please_try_again));

                        }

                    } catch (Exception e) {
                        displayMessage(getString(R.string.something_went_wrong));

                    }

                } else {
                    if (error instanceof NoConnectionError) {
                        displayMessage(getString(R.string.oops_connect_your_internet));
                    } else if (error instanceof NetworkError) {
                        displayMessage(getString(R.string.oops_connect_your_internet));
                    } else if (error instanceof TimeoutError) {
                        checkUpcomingList();
                    }
                }
            }
        }) {
            @Override
            public java.util.Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<String, String>();
                headers.put("X-Requested-With", "XMLHttpRequest");
                headers.put("Authorization", "Bearer " + SharedHelper.getKey(context, "access_token"));
                return headers;
            }
        };

        TopdriversApplication.getInstance().addToRequestQueue(jsonArrayRequest);
    }
}