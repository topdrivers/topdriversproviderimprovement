package com.topdrivers.driverv2.Services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.topdrivers.driverv2.Activity.Offline;
import com.topdrivers.driverv2.Helper.SharedHelper;
import com.topdrivers.driverv2.Helper.URLHelper;
import com.topdrivers.driverv2.TopdriversApplication;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

public class ShutdownReceiver extends BroadcastReceiver {

    Context context;
    @Override
    public void onReceive(Context context, Intent intent) {
        //Insert code here
        this.context=context;
        if (intent.getAction() == "android.intent.action.ACTION_SHUTDOWN") {
            SharedHelper.putKey(context, "isShutdown","1");
            new AsyncTask<Void, Void, Void>() {
                @Override
                protected Void doInBackground(Void... voids) {
                    offLine();
                    return null;
                }
            }.execute();
        }
    }

    private void offLine()
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
                if (response != null) {
                    if (response.optJSONObject("service").optString("status").equalsIgnoreCase("offline")) {
                       // goOffline();
                    } else {
                    }
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
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

    public void goOffline() {
        try {
           /* FragmentManager manager = MainActivity.fragmentManager;
            FragmentTransaction transaction = manager.beginTransaction();
            transaction.replace(R.id.content, new Offline());
            transaction.commitAllowingStateLoss();*/
            Intent mainIntent = new Intent(context, Offline.class);
            context.startActivity(mainIntent);
            //context.finish();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}