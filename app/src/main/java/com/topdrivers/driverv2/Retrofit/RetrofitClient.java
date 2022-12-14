package com.topdrivers.driverv2.Retrofit;

import com.topdrivers.driverv2.Helper.URLHelper;

import retrofit2.Retrofit;

/**
 * Created by CSS on 8/4/2017.
 */

public class RetrofitClient {
    private static Retrofit retrofit = null;

    public static Retrofit getLiveTrackingClient() {
        if (retrofit==null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(URLHelper.base)
                    .build();
        }
        return retrofit;
    }
}
