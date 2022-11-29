package com.topdrivers.driverv2.Activity;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.squareup.picasso.Picasso;
import com.topdrivers.driverv2.Fragment.Map;
import com.topdrivers.driverv2.Helper.CustomDialog;
import com.topdrivers.driverv2.Helper.SharedHelper;
import com.topdrivers.driverv2.Helper.URLHelper;
import com.topdrivers.driverv2.Helper.User;
import com.topdrivers.driverv2.R;
import com.topdrivers.driverv2.TopdriversApplication;
import com.topdrivers.driverv2.Utilities.AlarmUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

public class RideAcceptandRejectActivity extends AppCompatActivity {

    LinearLayout ll_01_contentLayer_accept_or_reject_now;
    TextView paymentModeText, pickupDistanceText, txt01Timer, txtSchedule,
            txt01Pickup, txt01UserName;
    //Animation
    Animation slide_down, slide_up;
    RatingBar rat01UserRating;
    ImageView img01User;
    String count = "0";
    CountDownTimer countDownTimer;
    MediaPlayer mPlayer;
    boolean timerCompleted = false;
    private CustomDialog customDialog;
    int method;
    //Button layer 02
    Button btn_02_accept;
    Button btn_02_reject;
    String requestId;
    private String mDateAlarm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ride_acceptand_reject);
        ll_01_contentLayer_accept_or_reject_now = (LinearLayout) findViewById(R.id.ll_01_contentLayer_accept_or_reject_now);
        slide_up = AnimationUtils.loadAnimation(this, R.anim.slide_up);
        slide_down = AnimationUtils.loadAnimation(this, R.anim.slide_down);
        paymentModeText = (TextView) findViewById(R.id.tv_payment_mode);
        txt01Pickup = findViewById(R.id.txtPickup);
        pickupDistanceText = findViewById(R.id.tv_pickup_distance);
        txt01Timer = findViewById(R.id.txt01Timer);
        img01User = findViewById(R.id.img01User);
        txt01UserName = findViewById(R.id.txt01UserName);
        txtSchedule = findViewById(R.id.txtSchedule);
        rat01UserRating = findViewById(R.id.rat01UserRating);
        //Button layer 02
        btn_02_accept = findViewById(R.id.btn_02_accept);
        btn_02_reject = findViewById(R.id.btn_02_reject);
        /*TopdriversApplication.getInstance().getRequestQueue().cancelAll(new RequestQueue.RequestFilter() {
            @Override
            public boolean apply(Request<?> request) {
                return true;
            }
        });*/
        String statusResponses = getIntent().getStringExtra("statusResponses");
        requestId = getIntent().getStringExtra("requestId");
        try {
            JSONArray newJArray = new JSONArray(statusResponses);
            setValuesTo_ll_01_contentLayer_accept_or_reject_now(newJArray);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        btn_02_accept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                countDownTimer.cancel();
                if (mPlayer != null && mPlayer.isPlaying()) {
                    /*mPlayer.stop();
                    mPlayer = null;*/
                    stopPlaying();
                }
                //  callClose();

                TopdriversApplication.getInstance().stopNotificationSound();
                Log.d("CheckStatus", requestId + "--");
                Log.d("CheckStatus", SharedHelper.getKey(RideAcceptandRejectActivity.this, "access_token"));

//                TopdriversApplication.getInstance().getRequestQueue().cancelAll("checkStatus");
                handleIncomingRequest("Accept", requestId);
            }
        });


        btn_02_reject.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                countDownTimer.cancel();
                if (mPlayer != null && mPlayer.isPlaying()) {
                   /* mPlayer.stop();
                    mPlayer = null;*/
                    stopPlaying();
                }

                TopdriversApplication.getInstance().stopNotificationSound();

                /*String rejectCount = SharedHelper.getKey(context, "refuses");
                if (rejectCount != null && rejectCount.equalsIgnoreCase("1")) {
                    TopdriversApplication.getInstance().getRequestQueue().cancelAll(new RequestQueue.RequestFilter() {
                        @Override
                        public boolean apply(Request<?> request) {
                            return true;
                        }
                    });
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            goOffline();
                        }
                    }, 5000);

                } else {
                    // handleIncomingRequest("Reject", request_id);
                    SharedHelper.putKey(context, "refuses", "1");


                }*/
                TopdriversApplication.getInstance().getRequestQueue().cancelAll("checkStatus");
                //        callClose();
                handleIncomingRequestReject("Reject", requestId);
            }
        });
    }

    private void callClose() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                customDialog.dismiss();
               /* Toast.makeText(RideAcceptandRejectActivity.this,
                        "" + getResources().getString(R.string.request_reject),
                        Toast.LENGTH_SHORT).show();*/
               /* String rejectCount = SharedHelper.getKey(RideAcceptandRejectActivity.this, "refuses");
                if (rejectCount != null && rejectCount.equalsIgnoreCase("1")) {
                    int refuseCountInt = Integer.parseInt(rejectCount);
                    if (refuseCountInt == 1) {
                       *//* if (!RideAcceptandRejectActivity.this.isFinishing()) {
                            customDialog = new CustomDialog(RideAcceptandRejectActivity.this);
                            customDialog.setCancelable(false);
                            customDialog.show();
                        }*//*
                        offLine();
                    } else {
                        refuseCountInt = refuseCountInt + 1;
                    }
                    SharedHelper.putKey(RideAcceptandRejectActivity.this, "refuses", refuseCountInt + "");
                } else {
                    SharedHelper.putKey(RideAcceptandRejectActivity.this, "refuses", "1");
                }*/

                try {
                    txt01Timer.setText("0");
                    //mapClear();
                    clearVisibility();
                    /*if (mPlayer != null && mPlayer.isPlaying()) {
                     *//* mPlayer.stop();
                                        mPlayer = null;*//*
                                        stopPlaying();
                                    }*/
                    ll_01_contentLayer_accept_or_reject_now.setVisibility(View.GONE);
                    //  CurrentStatus = "ONLINE";
                    //  PreviousStatus = "NULL";
                    // lnrGoOffline.setVisibility(View.VISIBLE);
                    // destinationLayer.setVisibility(View.GONE);
                    timerCompleted = true;
                    // handleIncomingRequest("Reject", request_id);
                    // handleIncomingRequestReject("Reject", request_id);
               /* TopdriversApplication.getInstance().getRequestQueue().cancelAll(new RequestQueue.RequestFilter() {
                    @Override
                    public boolean apply(Request<?> request) {
                        return true;
                    }
                });*/
                    finish();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, 5000);
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

                    if (mPlayer == null) {
//                        mPlayer = MediaPlayer.create(RideAcceptandRejectActivity.this, R.raw.alert_tone);

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
                // mapClear();
                // clearVisibility();
                //  whenMapCleared();
                if (mPlayer != null && mPlayer.isPlaying()) {
                    timerCompleted = false;
                    /*mPlayer.stop();
                    mPlayer = null;*/
                    stopPlaying();
                    countDownTimer.cancel();
                    countDownTimer = null;

                }
                mPlayer = null;
                TopdriversApplication.getInstance().stopNotificationSound();
                ll_01_contentLayer_accept_or_reject_now.setVisibility(View.GONE);
                finish();
                // CurrentStatus = "ONLINE";
                //  PreviousStatus = "NULL";
                //  lnrGoOffline.setVisibility(View.VISIBLE);
                //  destinationLayer.setVisibility(View.GONE);
                timerCompleted = true;
                //handleIncomingRequest("Reject", request_id);
                //  handleIncomingRequestReject("Reject", Map.request_id);
            }

        };
        countDownTimer.start();
        //}
        try {
            if (!statusResponse.optString("schedule_at").trim().equalsIgnoreCase("") && !statusResponse.optString("schedule_at").equalsIgnoreCase("null")) {
                txtSchedule.setVisibility(View.VISIBLE);
                String strSchedule = "";
                mDateAlarm = statusResponse.optString("schedule_at");
                try {
                    strSchedule =
                            getDate(statusResponse.optString("schedule_at")) + "th " + getMonth(statusResponse.optString("schedule_at"))
                                    + " " + getYear(statusResponse.optString("schedule_at")) + " " +
                                    "at " + getTime(statusResponse.optString("schedule_at"));
                    Log.d("c", strSchedule);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                // TODO Set local with this time
                txtSchedule.setText("Scheduled at : " + strSchedule);

                Log.d("CheckStatus -- ", statusResponse.optString("schedule_at"));

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
                        Picasso.with(RideAcceptandRejectActivity.this).load(user.getString("picture")).placeholder(R.drawable.ic_dummy_user).error(R.drawable.ic_dummy_user).into(img01User);
                    else
                        Picasso.with(RideAcceptandRejectActivity.this).load(URLHelper.base + "storage/" + user.getString(
                                "picture")).placeholder(R.drawable.ic_dummy_user).error(R.drawable.ic_dummy_user).into(img01User);
                } else {
                    img01User.setImageResource(R.drawable.ic_dummy_user);
                }
                final User userProfile = Map.user;
                img01User.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(RideAcceptandRejectActivity.this, ShowProfile.class);
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
        txt01Pickup.setText(Map.address);
    }

    private String getMonth(String date) throws ParseException {
        Date d = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH).parse(date);
        Calendar cal = Calendar.getInstance();
        cal.setTime(d);
        return new SimpleDateFormat("MMM").format(cal.getTime());
    }

    public long getMilliFromDate(String dateFormat) {
        Date date = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            date = formatter.parse(dateFormat);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        System.out.println("Today is " + date);
        return date.getTime();
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

    private void stopPlaying() {
        if (mPlayer != null) {
            mPlayer.stop();
            mPlayer.release();
            mPlayer = null;
        }
    }

    private void offLine() {
        customDialog = new CustomDialog(RideAcceptandRejectActivity.this);
        customDialog.setCancelable(false);
        try {
            customDialog.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
      /*  TopdriversApplication.getInstance().getRequestQueue().cancelAll(new RequestQueue.RequestFilter() {
            @Override
            public boolean apply(Request<?> request) {
                return true;
            }
        });*/
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
                        //goOffline();
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
                headers.put("Authorization", "Bearer " + SharedHelper.getKey(RideAcceptandRejectActivity.this, "access_token"));
                return headers;
            }
        };
        TopdriversApplication.getInstance().addToRequestQueue(jsonObjectRequest);
    }

    private void handleIncomingRequestReject(final String status, String id) {
        if (!RideAcceptandRejectActivity.this.isFinishing()) {
            customDialog = new CustomDialog(RideAcceptandRejectActivity.this);
            customDialog.setCancelable(false);
            customDialog.show();
        }
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
                        }
                        {
                            // if (!timerCompleted)
                            {
                                Toast.makeText(RideAcceptandRejectActivity.this,
                                        "" + getResources().getString(R.string.request_reject),
                                        Toast.LENGTH_SHORT).show();
                                /*String rejectCount = SharedHelper.getKey(RideAcceptandRejectActivity.this, "refuses");
                                if (rejectCount != null && rejectCount.equalsIgnoreCase("1")) {
                                    int refuseCountInt = Integer.parseInt(rejectCount);
                                    if (refuseCountInt == 1) {
                                        if (!RideAcceptandRejectActivity.this.isFinishing()) {
                                            customDialog = new CustomDialog(RideAcceptandRejectActivity.this);
                                            customDialog.setCancelable(false);
                                            customDialog.show();
                                        }
                                        offLine();
                                    } else {
                                        refuseCountInt = refuseCountInt + 1;
                                    }
                                    SharedHelper.putKey(RideAcceptandRejectActivity.this, "refuses", refuseCountInt + "");
                                } else {
                                    SharedHelper.putKey(RideAcceptandRejectActivity.this, "refuses", "1");
                                }*/

                                try {
                                    txt01Timer.setText("0");
                                    //mapClear();
                                    clearVisibility();
                                    /*if (mPlayer != null && mPlayer.isPlaying()) {
                                     *//* mPlayer.stop();
                                        mPlayer = null;*//*
                                        stopPlaying();
                                    }*/
                                    ll_01_contentLayer_accept_or_reject_now.setVisibility(View.GONE);
                                    //  CurrentStatus = "ONLINE";
                                    //  PreviousStatus = "NULL";
                                    // lnrGoOffline.setVisibility(View.VISIBLE);
                                    // destinationLayer.setVisibility(View.GONE);
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
                        /*String rejectCount = SharedHelper.getKey(RideAcceptandRejectActivity.this, "refuses");
                        if (rejectCount != null && rejectCount.length() > 0) {
                            int refuseCountInt = Integer.parseInt(rejectCount);
                            if (refuseCountInt == 1) {
                                if (!((Activity) RideAcceptandRejectActivity.this).isFinishing()) {
                                    customDialog = new CustomDialog(RideAcceptandRejectActivity.this);
                                    customDialog.setCancelable(false);
                                    customDialog.show();

                                }
                                offLine();
                            } else {
                                refuseCountInt = refuseCountInt + 1;
                            }
                            SharedHelper.putKey(RideAcceptandRejectActivity.this, "refuses", refuseCountInt + "");
                        } else {
                            SharedHelper.putKey(RideAcceptandRejectActivity.this, "refuses", "1");
                        }*/
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }) {
            @Override
            public java.util.Map<String, String> getHeaders() {
                HashMap<String, String> headers = new HashMap<>();
                headers.put("X-Requested-With", "XMLHttpRequest");
                headers.put("Authorization", "Bearer " + SharedHelper.getKey(RideAcceptandRejectActivity.this, "access_token"));
                return headers;
            }
        };
        TopdriversApplication.getInstance().addToRequestQueue(jsonObjectRequest);
    }

    @Override
    protected void onResume() {
        super.onResume();
        try {
            registerReceiver(broadcast_reciever, new IntentFilter("finish"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (broadcast_reciever.isOrderedBroadcast()) {
            unregisterReceiver(broadcast_reciever);
        }
    }

    private void handleIncomingRequest(final String status, final String id) {
            customDialog = new CustomDialog(RideAcceptandRejectActivity.this);
            customDialog.setCancelable(false);
            customDialog.show();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {

                try {
                    customDialog.dismiss();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                ll_01_contentLayer_accept_or_reject_now.setVisibility(View.GONE);
                TopdriversApplication.getInstance().getRequestQueue().cancelAll("checkStatus");
                finish();
            }
        }, 2000);

        if (mDateAlarm != null) {
            AlarmUtils.create(RideAcceptandRejectActivity.this, getMilliFromDate(mDateAlarm));
        }

        String url = URLHelper.base + "api/provider/trip/" + id;
        method = Request.Method.POST;

        final JsonArrayRequest jsonObjectRequest = new JsonArrayRequest(method, url, null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        Log.d("CheckStatus", id.toString() + "--" + response.toString());

                        Toast.makeText(RideAcceptandRejectActivity.this,
                                RideAcceptandRejectActivity.this.getResources().getString(R.string.request_accept),
                                Toast.LENGTH_SHORT).show();
                        if (customDialog != null) {
                            customDialog.dismiss();
                        }

                        SharedHelper.putKey(RideAcceptandRejectActivity.this, "refuses", "0");
                //        callClose();

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                try {
//                    Log.d("CheckStatusError", error.toString() + "--");

                    Toast.makeText(RideAcceptandRejectActivity.this,
                            "error",
                            Toast.LENGTH_SHORT).show();
                    customDialog.dismiss();
                    finish();


                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }) {
            @Override
            public java.util.Map<String, String> getHeaders() {
                HashMap<String, String> headers = new HashMap<>();
                headers.put("X-Requested-With", "XMLHttpRequest");
                headers.put("Authorization", "Bearer " + SharedHelper.getKey(RideAcceptandRejectActivity.this, "access_token"));
                return headers;
            }
        };
        jsonObjectRequest.setRetryPolicy(new DefaultRetryPolicy(
                5000 *10,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        TopdriversApplication.getInstance().addToRequestQueue(jsonObjectRequest);
    }

    public void clearVisibility() {

        try {
            if (ll_01_contentLayer_accept_or_reject_now.getVisibility() == View.VISIBLE) {
                ll_01_contentLayer_accept_or_reject_now.startAnimation(slide_down);
            }

            ll_01_contentLayer_accept_or_reject_now.setVisibility(View.GONE);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    BroadcastReceiver broadcast_reciever = new BroadcastReceiver() {

        @Override
        public void onReceive(Context arg0, Intent intent) {
            String action = intent.getAction();
            if (action.equals("finish")) {
                try {
                    stopPlaying();
                    TopdriversApplication.getInstance().stopNotificationSound();
                } catch (Exception e) {
                    e.printStackTrace();
                }
//finishing the activity
                try {
                    countDownTimer.cancel();
                    finish();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    };
}
