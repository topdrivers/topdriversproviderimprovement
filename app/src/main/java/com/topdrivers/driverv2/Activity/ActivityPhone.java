package com.topdrivers.driverv2.Activity;

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

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.FirebaseException;
import com.google.firebase.FirebaseTooManyRequestsException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.rilixtech.widget.countrycodepicker.Country;
import com.rilixtech.widget.countrycodepicker.CountryCodePicker;
import com.topdrivers.driverv2.R;
import com.topdrivers.driverv2.Utilities.MyTextView;
import com.topdrivers.driverv2.Utilities.Utilities;


import java.util.concurrent.TimeUnit;

/**
 * Created by jayakumar on 31/01/17.
 */

public class ActivityPhone extends AppCompatActivity {

    ImageView backArrow;
    FloatingActionButton nextICON;
    EditText mEdtPhone;
    EditText mEdtCountryCode;
    MyTextView register, forgetPassword;
    LinearLayout lnrBegin;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;
    String firstName;
    String lastName;
    String email;
    String password;
    String phoneNumber;
    CountryCodePicker ccp;

    Boolean isLoginSocial;
    String access_token, loginBy;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phone);

        if (Build.VERSION.SDK_INT > 15) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }

        firstName = getIntent().getStringExtra("FIRST_NAME");
        lastName = getIntent().getStringExtra("LAST_NAME");
        email = getIntent().getStringExtra("EMAIL");
        password = getIntent().getStringExtra("PASSWORD");

        isLoginSocial = getIntent().getBooleanExtra("IS_LOGIN_SOCIAL", false);
        access_token = getIntent().getStringExtra("ACCESS_TOKEN");
        loginBy = getIntent().getStringExtra("LOGIN_BY");

        initView();
        mEdtPhone = (EditText) findViewById(R.id.mEdtPhoneNumber);
        ccp = (CountryCodePicker) findViewById(R.id.ccp);
        nextICON = (FloatingActionButton) findViewById(R.id.nextICON);
        backArrow = (ImageView) findViewById(R.id.backArrow);
        forgetPassword = (MyTextView) findViewById(R.id.forgetPassword);
        lnrBegin = (LinearLayout) findViewById(R.id.lnrBegin);

        nextICON.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mEdtPhone.getText().toString().equals("")) {
                    displayMessage(getString(R.string.phone_validation));

                } else {
                    if ((!isValidPhone(mEdtPhone.getText().toString()))) {
                        displayMessage(getString(R.string.phone_validation_length));
                    } else {
                        Utilities.hideKeyboard(ActivityPhone.this);
                        phoneNumber = ccp.getFullNumberWithPlus() + mEdtPhone.getText().toString();
                        FirebaseAuth.getInstance().signOut();
                        startPhoneNumberVerification(phoneNumber);
                    }


                }
            }
        });

        ccp.setOnCountryChangeListener(new CountryCodePicker.OnCountryChangeListener() {
            @Override
            public void onCountrySelected(Country selectedCountry) {

            }
        });

        backArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });


    }

    private void initView() {
        mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

            @Override
            public void onCodeAutoRetrievalTimeOut(String verificationId) {
                Log.d("onCodeAuto", "onVerificationCompleted:" + verificationId);


            }

            @Override
            public void onVerificationCompleted(PhoneAuthCredential credential) {
                Log.d("onVerificationCompleted", "onVerificationCompleted:" + credential);
                if (isLoginSocial) {
                    Intent mainIntent = new Intent(ActivityPhone.this, ActivityVerifyCode.class);
                    mainIntent.putExtra("IS_LOGIN_SOCIAL", isLoginSocial);
                    mainIntent.putExtra("PHONE_NUMBER", phoneNumber);
                    mainIntent.putExtra("LOGIN_BY", loginBy);
                    mainIntent.putExtra("ACCESS_TOKEN", access_token);
                    mainIntent.putExtra("isVerified", true);
                    startActivity(mainIntent);
                    ActivityPhone.this.finish();
                }

            }

            @Override
            public void onVerificationFailed(FirebaseException e) {
                Log.w("onVerificationFailed", "onVerificationFailed", e);


                if (e instanceof FirebaseAuthInvalidCredentialsException) {
                    Log.e("Exception:", "FirebaseAuthInvalidCredentialsException" + e);
                } else if (e instanceof FirebaseTooManyRequestsException) {
                    Toast.makeText(ActivityPhone.this, "FirebaseTooManyRequestsException", Toast.LENGTH_SHORT).show();
                }

            }

            @Override
            public void onCodeSent(String verificationId,
                                   PhoneAuthProvider.ForceResendingToken token) {
                //for low level version which doesn't do aoto verififcation save the verification code and the token
                Log.d("onCodeSent", "onCodeSent:" + verificationId);
                Log.i("Verification code:", verificationId);
                Intent mainIntent = new Intent(ActivityPhone.this, ActivityVerifyCode.class);
                mainIntent.putExtra("VERIFY_ID", verificationId);
                mainIntent.putExtra("FIRST_NAME", firstName);
                mainIntent.putExtra("LAST_NAME", lastName);
                mainIntent.putExtra("EMAIL", email);
                mainIntent.putExtra("PASSWORD", password);
                mainIntent.putExtra("PHONE_NUMBER", phoneNumber);
                mainIntent.putExtra("TOKEN", token);
                mainIntent.putExtra("IS_LOGIN_SOCIAL", isLoginSocial);
                mainIntent.putExtra("LOGIN_BY", loginBy);
                mainIntent.putExtra("ACCESS_TOKEN", access_token);
                startActivity(mainIntent);
                ActivityPhone.this.finish();
            }
        };
    }


    private void startPhoneNumberVerification(String phoneNumber) {
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                phoneNumber,        // Phone number to verify
                60,                 // Timeout duration
                TimeUnit.SECONDS,   // Unit of timeout
                this,               // Activity (for callback binding)
                mCallbacks);        // OnVerificationStateChangedCallbacks
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


    private boolean isValidPhone(String phone) {
        if (phone.length() >= 9) {
            return true;
        } else {
            return false;

        }
    }

}