package com.topdrivers.driverv2.Activity;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkError;
import com.android.volley.NetworkResponse;
import com.android.volley.NoConnectionError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;

import com.google.firebase.messaging.FirebaseMessaging;
import com.topdrivers.driverv2.Helper.ConnectionHelper;
import com.topdrivers.driverv2.Helper.CustomDialog;
import com.topdrivers.driverv2.Helper.SharedHelper;
import com.topdrivers.driverv2.Helper.URLHelper;
import com.topdrivers.driverv2.R;
import com.topdrivers.driverv2.TopdriversApplication;
import com.topdrivers.driverv2.Utilities.CountDownTimerUtil;
import com.topdrivers.driverv2.Utilities.MyTextView;
import com.topdrivers.driverv2.Utilities.Utilities;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.topdrivers.driverv2.TopdriversApplication.trimMessage;


/**
 * Created by jayakumar on 31/01/17.
 */

public class ActivityVerifyCode extends AppCompatActivity {

    ImageView backArrow;
    FloatingActionButton nextICON;
    EditText mEdtCodeVerify;
    MyTextView tvResendCode;
    MyTextView tvNumberCode;
    LinearLayout lnrBegin;
    private FirebaseAuth mAuth;
    String verificationId;
    CustomDialog customDialog;
    String device_token, device_UDID;
    Utilities utils = new Utilities();
    public Context context = ActivityVerifyCode.this;
    String firstName;
    String lastName;
    String email;
    String password;
    String phoneNumber;

    Boolean isLoginSocial, isVerified;
    String access_token, loginBy;

    PhoneAuthProvider.ForceResendingToken token;
    Boolean isInternet;
    ConnectionHelper helper;
    CountDownTimerUtil mCountDownTimer;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verify_code);
        mAuth = FirebaseAuth.getInstance();
        GetToken();
        verificationId = getIntent().getStringExtra("VERIFY_ID");
        firstName = getIntent().getStringExtra("FIRST_NAME");
        lastName = getIntent().getStringExtra("LAST_NAME");
        email = getIntent().getStringExtra("EMAIL");
        password = getIntent().getStringExtra("PASSWORD");
        phoneNumber = getIntent().getStringExtra("PHONE_NUMBER");
        token = getIntent().getExtras().getParcelable("TOKEN");

        isLoginSocial = getIntent().getBooleanExtra("IS_LOGIN_SOCIAL", false);
        access_token = getIntent().getStringExtra("ACCESS_TOKEN");
        loginBy = getIntent().getStringExtra("LOGIN_BY");

        isVerified = getIntent().getBooleanExtra("isVerified", false);

        if (isVerified && loginBy != null) {
            if (loginBy.equals("facebook")) {
                loginSocial(access_token,URLHelper.FACEBOOK_LOGIN, loginBy);
            } else {
                loginSocial(access_token,URLHelper.GOOGLE_LOGIN, loginBy);
            }
        }

        helper = new ConnectionHelper(context);
        isInternet = helper.isConnectingToInternet();
        countDownResend();


        if (Build.VERSION.SDK_INT > 15) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }

        mEdtCodeVerify = (EditText) findViewById(R.id.edtCodeVerify);
        nextICON = (FloatingActionButton) findViewById(R.id.nextICON);
        backArrow = (ImageView) findViewById(R.id.backArrow);
        tvResendCode = (MyTextView) findViewById(R.id.tvResendCode);
        tvNumberCode = (MyTextView) findViewById(R.id.tvNumberCode);
        lnrBegin = (LinearLayout) findViewById(R.id.lnrBegin);
        tvNumberCode.setText(String.format(getString(R.string.tv_verify_code_number), phoneNumber));
        tvResendCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startPhoneNumberVerification(phoneNumber,token);
                countDownResend();

            }
        });

        nextICON.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mEdtCodeVerify.getText().toString().equals("")) {

                    displayMessage(getString(R.string.verify_code_validation));
                } else {
                    verifyPhoneNumberWithCode(verificationId, mEdtCodeVerify.getText().toString());

                }
            }
        });

        backArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

            @Override
            public void onCodeAutoRetrievalTimeOut(String verification) {

            }

            @Override
            public void onVerificationCompleted(PhoneAuthCredential credential) {

            }

            @Override
            public void onVerificationFailed(FirebaseException e) {


            }

            @Override
            public void onCodeSent(String verification,
                                   PhoneAuthProvider.ForceResendingToken token) {
                verificationId  = verification;

            }
        };
    }

    public void countDownResend() {
        int count = 60;
        mCountDownTimer = new CountDownTimerUtil(60 * 1000, 1000) {
            public void onTick(long millisUntilFinished) {
                //here you can have your logic to set text to edittext
                tvResendCode.setClickable(false);
                tvResendCode.setEnabled(false);
                tvResendCode.setTextColor(ContextCompat.getColor(ActivityVerifyCode.this, R.color.suggestion_color));
                tvResendCode.setText(getString(R.string.tv_resend_code) + " (" + (millisUntilFinished / 1000) + ")");
            }

            public void onFinish() {
                tvResendCode.setTextColor(ContextCompat.getColor(ActivityVerifyCode.this, R.color.text_color_header));
                tvResendCode.setClickable(true);
                tvResendCode.setEnabled(true);
                tvResendCode.setText(getString(R.string.tv_resend_code));

            }
        };
        mCountDownTimer.start();
    }


    public void GetToken() {
        try {
            if (!SharedHelper.getKey(context, "device_token").equals("") && SharedHelper.getKey(context, "device_token") != null) {
                device_token = SharedHelper.getKey(context, "device_token");
            } else {
                device_token = "" + FirebaseMessaging.getInstance().getToken();
                SharedHelper.putKey(context, "device_token", "" + FirebaseMessaging.getInstance().getToken());
            }
        } catch (Exception e) {
            device_token = "COULD NOT GET FCM TOKEN";
        }

        try {
            device_UDID = android.provider.Settings.Secure.getString(getContentResolver(), android.provider.Settings.Secure.ANDROID_ID);
        } catch (Exception e) {
            device_UDID = "COULD NOT GET UDID";
            e.printStackTrace();
        }
    }

    private void startPhoneNumberVerification(String phoneNumber, PhoneAuthProvider.ForceResendingToken token) {
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                phoneNumber,        // Phone number to verify
                60,                 // Timeout duration
                TimeUnit.SECONDS,   // Unit of timeout
                this,               // Activity (for callback binding)
                mCallbacks,
                token);        // OnVerificationStateChangedCallbacks
    }
    private void verifyPhoneNumberWithCode(String verificationId, String code) {
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, code);
        signInWithPhoneAuthCredential(credential);
    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            if (isLoginSocial) {
                                if (loginBy.equals("facebook")) {
                                    loginSocial(access_token,URLHelper.FACEBOOK_LOGIN, loginBy);
                                } else {
                                    loginSocial(access_token,URLHelper.GOOGLE_LOGIN, loginBy);
                                }
                            } else {
                                registerAPI();
                            }

                            FirebaseUser user = task.getResult().getUser();

                            Log.d("Sign in with phone auth", "Success " + user);


                        } else {
                            Log.d("Sign in with phone auth", "Success ");

                        }
                    }
                });
    }

    private void registerAPI() {

        customDialog = new CustomDialog(ActivityVerifyCode.this);
        if (customDialog != null)
            customDialog.show();
        JSONObject object = new JSONObject();
        try {

            object.put("device_type", "android");
            object.put("device_id", device_UDID);
            object.put("device_token", "" + device_token);
            object.put("login_by", "manual");
            object.put("first_name", firstName);
            object.put("last_name", lastName);
            object.put("email", email);
            object.put("password", password);
            object.put("password_confirmation", password);
            object.put("mobile", phoneNumber);
//            object.put("picture", "");
//            object.put("social_unique_id", "");

            Log.d("InputToRegisterAPI", "" + object.toString());
            utils.print("InputToRegisterAPI", "" + object);

        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, URLHelper.register, object, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                if ((customDialog != null) && (customDialog.isShowing()))
                    //customDialog.dismiss();
                    utils.print("SignInResponse", response.toString());
                SharedHelper.putKey(ActivityVerifyCode.this, "email", email);
                SharedHelper.putKey(ActivityVerifyCode.this, "password", password);
                signIn();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if ((customDialog != null) && (customDialog.isShowing()))
                    customDialog.dismiss();
                String json = null;
                String Message;
                NetworkResponse response = error.networkResponse;

                if (response != null && response.data != null) {
                    utils.print("MyTest", "" + error);
                    utils.print("MyTestError", "" + error.networkResponse);
                    utils.print("MyTestError1", "" + response.statusCode);
                    try {
                        JSONObject errorObj = new JSONObject(new String(response.data));

                        if (response.statusCode == 400 || response.statusCode == 405 || response.statusCode == 500) {
                            try {
                                displayMessage(errorObj.optString("message"));
                            } catch (Exception e) {
                                displayMessage(getString(R.string.something_went_wrong));
                            }
                        } else if (response.statusCode == 401) {
                            try {
                                if (errorObj.optString("message").equalsIgnoreCase("invalid_token")) {
                                    //   Refresh token
                                } else {
                                    displayMessage(errorObj.optString("message"));
                                }
                            } catch (Exception e) {
                                displayMessage(getString(R.string.something_went_wrong));
                            }

                        } else if (response.statusCode == 422) {

                            json = trimMessage(new String(response.data));
                            if (json != "" && json != null) {
                                if (json.startsWith("The email has already been taken")) {
                                    displayMessage(getString(R.string.email_exist));
                                } else {
                                    displayMessage(getString(R.string.something_went_wrong));
                                }
                                //displayMessage(json);
                            } else {
                                displayMessage(getString(R.string.please_try_again));
                            }

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
                        registerAPI();
                    }
                }
            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<String, String>();
                headers.put("X-Requested-With", "XMLHttpRequest");
                return headers;
            }
        };

        TopdriversApplication.getInstance().addToRequestQueue(jsonObjectRequest);

    }

    public void displayMessage(String toastString) {
        try {
            Snackbar.make(getCurrentFocus(), toastString, Snackbar.LENGTH_SHORT)
                    .setAction("Action", null).show();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, toastString, Toast.LENGTH_SHORT).show();
        }
    }

    private boolean isValidEmail(String email) {
        String EMAIL_PATTERN = "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@"
                + "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";
        Pattern pattern = Pattern.compile(EMAIL_PATTERN);
        Matcher matcher = pattern.matcher(email);
        return matcher.matches();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.activity_back_in, R.anim.activity_back_out);
    }

    public void signIn() {
        if (isInternet) {
            customDialog = new CustomDialog(ActivityVerifyCode.this);
            customDialog.setCancelable(false);
            if (customDialog != null)
                customDialog.show();
            JSONObject object = new JSONObject();
            try {
                object.put("device_type", "android");
                object.put("device_id", device_UDID);
                object.put("device_token", device_token);
                object.put("email", SharedHelper.getKey(ActivityVerifyCode.this, "email"));
                object.put("password", SharedHelper.getKey(ActivityVerifyCode.this, "password"));
                utils.print("InputToLoginAPI", "" + object);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, URLHelper.login, object, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    if (customDialog != null && customDialog.isShowing())
                        customDialog.dismiss();
                    utils.print("SignUpResponse", response.toString());
                    SharedHelper.putKey(context, "access_token", response.optString("access_token"));
                    if (!response.optString("currency").equalsIgnoreCase("") && response.optString("currency") != null)
                        SharedHelper.putKey(context, "currency", response.optString("currency"));
                    else
                        SharedHelper.putKey(context, "currency", "$");
                    getProfile();
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    if (customDialog != null && customDialog.isShowing())
                        customDialog.dismiss();
                    String json = null;
                    NetworkResponse response = error.networkResponse;
                    if (response != null && response.data != null) {
                        try {
                            JSONObject errorObj = new JSONObject(new String(response.data));
                            utils.print("ErrorInLoginAPI", "" + errorObj.toString());

                            if (response.statusCode == 400 || response.statusCode == 405 || response.statusCode == 500 || response.statusCode == 401) {
                                displayMessage(getString(R.string.something_went_wrong));
                            } else if (response.statusCode == 422) {
                                json = trimMessage(new String(response.data));
                                if (json != "" && json != null) {
                                    displayMessage(json);
                                } else {
                                    displayMessage(getString(R.string.please_try_again));
                                }

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
                            signIn();
                        }
                    }
                }
            }) {
                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    HashMap<String, String> headers = new HashMap<String, String>();
                    headers.put("X-Requested-With", "XMLHttpRequest");
                    return headers;
                }
            };

            TopdriversApplication.getInstance().addToRequestQueue(jsonObjectRequest);
        } else {
            displayMessage(getString(R.string.something_went_wrong_net));
        }

    }



    public void GoToMainActivity() {
        if (customDialog != null && customDialog.isShowing())
            customDialog.dismiss();
        Intent mainIntent = new Intent(ActivityVerifyCode.this, MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(mainIntent);
        ActivityVerifyCode.this.finish();
    }
    public void getProfile() {
        if (isInternet) {
            customDialog = new CustomDialog(ActivityVerifyCode.this);
            customDialog.setCancelable(false);
            if (customDialog != null)
                customDialog.show();
            JSONObject object = new JSONObject();
            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, URLHelper.USER_PROFILE_API, object, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    if (customDialog != null && customDialog.isShowing())
                        customDialog.dismiss();
                    utils.print("GetProfile", response.toString());
                    SharedHelper.putKey(ActivityVerifyCode.this, "id", response.optString("id"));
                    SharedHelper.putKey(ActivityVerifyCode.this, "first_name", response.optString("first_name"));
                    SharedHelper.putKey(ActivityVerifyCode.this, "last_name", response.optString("last_name"));
                    SharedHelper.putKey(ActivityVerifyCode.this, "email", response.optString("email"));
                    if (response.optString("avatar").startsWith("http"))
                        SharedHelper.putKey(context, "picture", response.optString("avatar"));
                    else
                        SharedHelper.putKey(context, "picture", URLHelper.base + "storage/" + response.optString("avatar"));
                    SharedHelper.putKey(ActivityVerifyCode.this, "gender", "" + response.optString("gender"));
                    SharedHelper.putKey(ActivityVerifyCode.this, "mobile", response.optString("mobile"));
                    SharedHelper.putKey(context, "approval_status", response.optString("status"));
                    if(!response.optString("currency").equalsIgnoreCase("") && response.optString("currency") != null)
                        SharedHelper.putKey(context, "currency",response.optString("currency"));
                    else
                        SharedHelper.putKey(context, "currency","$");
                    SharedHelper.putKey(context, "sos", response.optString("sos"));
                    SharedHelper.putKey(ActivityVerifyCode.this, "loggedIn", getString(R.string.True));

                    if (response.optJSONObject("service") != null) {
                        JSONObject service = response.optJSONObject("service");
                        JSONObject serviceType = service.optJSONObject("service_type");
                        SharedHelper.putKey(context, "service", serviceType.optString("name"));
                    }
                    SharedHelper.putKey(ActivityVerifyCode.this, "login_by", "manual");
                    GoToMainActivity();
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    if (customDialog != null && customDialog.isShowing())
                        customDialog.dismiss();
                    String json = null;
                    String Message;
                    NetworkResponse response = error.networkResponse;
                    if (response != null && response.data != null) {
                        try {
                            JSONObject errorObj = new JSONObject(new String(response.data));

                            if (response.statusCode == 400 || response.statusCode == 405 || response.statusCode == 500) {
                                displayMessage(getString(R.string.something_went_wrong));
                            } else if (response.statusCode == 401) {
                                SharedHelper.putKey(context, "loggedIn", getString(R.string.False));
                                GoToBeginActivity();
                            } else if (response.statusCode == 422) {

                                json = trimMessage(new String(response.data));
                                if (json != "" && json != null) {
                                    displayMessage(json);
                                } else {
                                    displayMessage(getString(R.string.please_try_again));
                                }

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
                            getProfile();
                        }
                    }
                }
            }) {
                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    HashMap<String, String> headers = new HashMap<String, String>();
                    headers.put("X-Requested-With", "XMLHttpRequest");
                    headers.put("Authorization", "Bearer " + SharedHelper.getKey(ActivityVerifyCode.this, "access_token"));
                    return headers;
                }
            };

            TopdriversApplication.getInstance().addToRequestQueue(jsonObjectRequest);
        } else {
            displayMessage(getString(R.string.something_went_wrong_net));
        }
    }

    private void GoToBeginActivity() {
        if (customDialog != null && customDialog.isShowing())
            customDialog.dismiss();
        Intent mainIntent = new Intent(ActivityVerifyCode.this, ActivityEmail.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(mainIntent);
        ActivityVerifyCode.this.finish();
    }

    public void loginSocial(final String accesstoken, final String URL, final String Loginby) {

        customDialog = new CustomDialog(context);
        customDialog.setCancelable(false);
        if (customDialog != null)
            customDialog.show();
        JSONObject json = new JSONObject();
        try {
            json.put("device_type", "android");
            json.put("device_token", device_token);
            json.put("accessToken", accesstoken);
            json.put("device_id", device_UDID);
            json.put("login_by", Loginby);
            json.put("mobile", phoneNumber);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, URL, json, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {

                if ((customDialog != null) && customDialog.isShowing())
                    customDialog.dismiss();

                if (response != null) {
                    try {
                        JSONObject jsonObject = new JSONObject(response.toString());
                        String status = jsonObject.optString("status");
                        if (status.equalsIgnoreCase("true")) {
                            SharedHelper.putKey(ActivityVerifyCode.this, "token_type", jsonObject.optString("token_type"));
                            SharedHelper.putKey(ActivityVerifyCode.this, "access_token", jsonObject.optString("access_token"));
                            if (Loginby.equalsIgnoreCase("facebook"))
                                SharedHelper.putKey(ActivityVerifyCode.this, "login_by", "facebook");
                            if (Loginby.equalsIgnoreCase("google"))
                                SharedHelper.putKey(ActivityVerifyCode.this, "login_by", "google");

                            if (!jsonObject.optString("currency").equalsIgnoreCase("") && jsonObject.optString("currency") != null)
                                SharedHelper.putKey(context, "currency", jsonObject.optString("currency"));
                            else
                                SharedHelper.putKey(context, "currency", "$");
                            //phoneLogin();
                            getProfile();
                        } else {
                            JSONObject errorObject = new JSONObject(response.toString());
                            String strMessage = errorObject.optString("message");
                            displayMessage(strMessage);
                            GoToBeginActivity();
                        }

                    } catch (JSONException e1) {
                        e1.printStackTrace();
                        if (Loginby.equalsIgnoreCase("facebook")) {
                            displayMessage(getResources().getString(R.string.fb_error));
                        } else {
                            displayMessage(getResources().getString(R.string.google_login));
                        }
                    }
                }else {
                    displayMessage(getString(R.string.please_try_again));
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if ((customDialog != null) && customDialog.isShowing())
                    customDialog.dismiss();
                displayMessage(getString(R.string.please_try_again));
            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<String, String>();
                headers.put("X-Requested-With", "XMLHttpRequest");
                return headers;
            }
        };

        TopdriversApplication.getInstance().addToRequestQueue(request);

//        Ion.with(ActivityVerifyCode.this)
//                .load(URL)
//                .addHeader("X-Requested-With", "XMLHttpRequest")
////                .addHeader("Authorization",""+SharedHelper.getKey(context, "token_type")+" "+SharedHelper.getKey(context, "access_token"))
//                .setJsonObjectBody(json)
//                .asJsonObject()
//                .setCallback(new FutureCallback<JsonObject>() {
//                    @Override
//                    public void onCompleted(Exception e, JsonObject result) {
//                        // do stuff with the result or error
//                        if ((customDialog != null) && customDialog.isShowing())
//                            customDialog.dismiss();
//                        if (e != null) {
//                            if (e instanceof NetworkErrorException) {
//                                displayMessage(getString(R.string.oops_connect_your_internet));
//                            } else if (e instanceof TimeoutException) {
//                                loginSocial(accesstoken, URL, Loginby);
//                            } else{
//                                displayMessage(getString(R.string.please_try_again));
//                            }
//                            return;
//                        }
//                        if (result != null) {
//                            Log.v(Loginby + "_Response", result.toString());
//                            try {
//                                JSONObject jsonObject = new JSONObject(result.toString());
//                                String status = jsonObject.optString("status");
//                                if (status.equalsIgnoreCase("true")) {
//                                    SharedHelper.putKey(ActivityVerifyCode.this, "token_type", jsonObject.optString("token_type"));
//                                    SharedHelper.putKey(ActivityVerifyCode.this, "access_token", jsonObject.optString("access_token"));
//                                    if (Loginby.equalsIgnoreCase("facebook"))
//                                        SharedHelper.putKey(ActivityVerifyCode.this, "login_by", "facebook");
//                                    if (Loginby.equalsIgnoreCase("google"))
//                                        SharedHelper.putKey(ActivityVerifyCode.this, "login_by", "google");
//
//                                    if (!jsonObject.optString("currency").equalsIgnoreCase("") && jsonObject.optString("currency") != null)
//                                        SharedHelper.putKey(context, "currency", jsonObject.optString("currency"));
//                                    else
//                                        SharedHelper.putKey(context, "currency", "$");
//                                    //phoneLogin();
//                                    getProfile();
//                                } else {
//                                    JSONObject errorObject = new JSONObject(result.toString());
//                                    String strMessage = errorObject.optString("message");
//                                    displayMessage(strMessage);
//                                    GoToBeginActivity();
//                                }
//
//                            } catch (JSONException e1) {
//                                e1.printStackTrace();
//                                if (Loginby.equalsIgnoreCase("facebook")) {
//                                    displayMessage(getResources().getString(R.string.fb_error));
//                                } else {
//                                    displayMessage(getResources().getString(R.string.google_login));
//                                }
//                            }
//                        }else {
//                            displayMessage(getString(R.string.please_try_again));
//                        }
//                        // onBackPressed();
//                    }
//                });
    }
}