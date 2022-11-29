package com.topdrivers.driverv2.Helper;



public class URLHelper {
   public static final String base = "https://top-drivers.fr/";
   // public static final String base = "http://165.227.114.124/";
    //public static final String base = "http://6586b14b.ngrok.io/";
    public static final String HELP_URL = base+"";
    public static final String CALL_PHONE = "1";
    public static final String APP_URL = "https://play.google.com/store/apps/details?id=com.topdrivers.provider&hl=en";
    public static final String login = base + "api/provider/oauth/token";
    public static final String register = base + "api/provider/register";
    public static final String CHECK_MAIL_ALREADY_REGISTERED = base+"api/provider/verify";
    public static final String USER_PROFILE_API = base + "api/provider/profile";
    public static final String UPDATE_AVAILABILITY_API = base + "api/provider/profile/available";
    public static final String GET_HISTORY_API = base + "api/provider/requests/history";
    public static final String GET_HISTORY_DETAILS_API = base + "api/provider/requests/history/details";
    public static final String CHANGE_PASSWORD_API = base + "api/provider/profile/password";
    public static final String UPCOMING_TRIP_DETAILS = base + "api/provider/requests/upcoming/details";
    public static final String UPCOMING_TRIPS = base + "api/provider/requests/upcoming";
    public static final String CANCEL_REQUEST_API = base + "api/provider/cancel";
    public static final String TARGET_API = base + "api/provider/target";
    public static final String RESET_PASSWORD = base + "api/provider/reset/password";
    public static final String FORGET_PASSWORD = base + "api/provider/forgot/password";
    public static final String FACEBOOK_LOGIN = base + "api/provider/auth/facebook";
    public static final String GOOGLE_LOGIN = base + "api/provider/auth/google";
    public static final String LOGOUT = base + "api/provider/logout";
    public static final String SUMMARY = base + "api/provider/summary";
    public static final String HELP = base + "api/provider/help";
    public static final int client_id = 2;
    public static final String client_secret = "KJrfP8J7xFCKbN4roMAIpyjRFE0bsIPJF0x2pMWy";
    public static final String STRIPE_TOKEN = "pk_live_nKfvqiFdIDUgNJP1IDqhosiR";
    public static final String loginCar = base+"oauth/token";
    public static final String PAY_NOW_API = base+"api/user/payment-driver";
 public static final String PAY_NOW_API_token  =base+"api/provider/oauth/get_login";
}
