package com.topdrivers.driverv2;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import androidx.multidex.MultiDex;

import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.Volley;
import com.topdrivers.driverv2.Utilities.FontsOverride;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;

import io.github.inflationx.calligraphy3.CalligraphyConfig;
import io.github.inflationx.calligraphy3.CalligraphyInterceptor;
import io.github.inflationx.viewpump.ViewPump;


/**
 * Created by jayakumar on 29/01/17.
 */

public class TopdriversApplication extends Application {

    public static final String TAG = TopdriversApplication.class
            .getSimpleName();

    private RequestQueue mRequestQueue;
    private ImageLoader mImageLoader;

    private Ringtone ringtone;

    public boolean isOpenRideAccept = false;
    public boolean isOpenningFlow = false;

    private static TopdriversApplication mInstance;
    private Activity mCurrentActivity = null;

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }
    public Activity getCurrentActivity(){
        return mCurrentActivity;
    }
    public void setCurrentActivity(Activity mCurrentActivity){
        this.mCurrentActivity = mCurrentActivity;
    }
    @Override
    public void onCreate() {
        super.onCreate();
        mInstance = this;
//        initCalligraphyConfig();
        FontsOverride.setDefaultFont(this, "MONOSPACE", "ClanPro-NarrBook.otf");
//        FontsOverride.setDefaultFont(this, "DEFAULT", "ClanPro-Book.otf");
//        FontsOverride.setDefaultFont(this, "MONOSPACE", "ClanPro-Book.otf");
//        FontsOverride.setDefaultFont(this, "SERIF", "ClanPro-Book.otf");
//        FontsOverride.setDefaultFont(this, "SANS_SERIF", "ClanPro-Book.otf");
    }

    private void initCalligraphyConfig() {
//        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
//                .setDefaultFontPath(getResources().getString(R.string.bariol))
//                .setFontAttrId(R.attr.fontPath)
//                .build()
//        );

        ViewPump.init(ViewPump.builder()
                .addInterceptor(new CalligraphyInterceptor(
                        new CalligraphyConfig.Builder()
                                .setDefaultFontPath(getResources().getString(R.string.bariol))
                                .setFontAttrId(R.attr.fontPath)
                                .build()))
                .build());
    }

    public static synchronized TopdriversApplication getInstance() {
        return mInstance;
    }

    public RequestQueue getRequestQueue() {
        if (mRequestQueue == null) {
            mRequestQueue = Volley.newRequestQueue(getApplicationContext());
        }
        return mRequestQueue;
    }

    public <T> void addToRequestQueue(Request<T> req, String tag) {
        // set the no_user tag if tag is empty
        req.setTag(TextUtils.isEmpty(tag) ? TAG : tag);
        getRequestQueue().add(req);
    }

    public <T> void addToRequestQueue(Request<T> req) {
        req.setTag(TAG);
        getRequestQueue().add(req);
    }

    public void cancelPendingRequests(Object tag) {
        if (mRequestQueue != null) {
            mRequestQueue.cancelAll(tag);
        }
    }


    public static String trimMessage(String json){
        String trimmedString = "";

        try{
            JSONObject jsonObject = new JSONObject(json);
            Iterator<String> iter = jsonObject.keys();
            while (iter.hasNext()) {
                String key = iter.next();
                try {
                    JSONArray value = jsonObject.getJSONArray(key);
                    for (int i = 0, size = value.length(); i < size; i++) {
                        Log.e("Errors in Form",""+value.getString(i));
                        trimmedString += value.getString(i);
                        if(i < size-1) {
                            trimmedString += '\n';
                        }
                    }
                } catch (JSONException e) {

                    trimmedString += jsonObject.optString(key);
                }
            }
        } catch(JSONException e){
            e.printStackTrace();
            return null;
        }
        Log.e("Trimmed",""+trimmedString);

        return trimmedString;
    }

    public Ringtone getRingtone(Uri notification) {
        ringtone = RingtoneManager.getRingtone(getApplicationContext(), notification);

        return ringtone;
    }

    public void playNotificationSound(Uri notification) {
        getRingtone(notification).play();
    }

    public void stopNotificationSound() {
        if (ringtone != null && ringtone.isPlaying()) {
            ringtone.stop();
        }
    }


}
