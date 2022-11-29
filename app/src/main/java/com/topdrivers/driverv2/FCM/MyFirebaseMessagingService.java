package com.topdrivers.driverv2.FCM;

import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.AudioAttributes;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.topdrivers.driverv2.Activity.MainActivity;
import com.topdrivers.driverv2.Helper.SharedHelper;
import com.topdrivers.driverv2.R;
import com.topdrivers.driverv2.TopdriversApplication;

import java.util.List;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private static final String TAG = "MyFirebaseMsgService";
    private int notificationId = 0;
    private boolean isPush;

    @Override
    public void onNewToken(@NonNull String s) {
        super.onNewToken(s);
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        try {
            Log.d(TAG, "onMessageReceived" + "  " + remoteMessage.getData().get("message"));
            if (remoteMessage.getData() != null) {
                Log.d(TAG, remoteMessage.getData().get("message"));
                String message = remoteMessage.getData().get("message");
                String isCancelled = remoteMessage.getData().get("is_cancelled");
                if (isAppIsInBackground(getApplicationContext())
                        && (message.equalsIgnoreCase("New Incoming Ride")
                        || message.equalsIgnoreCase("Nouvelle course à venir"))) {
                    Intent mainIntent = new Intent(getApplicationContext(), MainActivity.class);
                    mainIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    //  startActivity(mainIntent);
                } else {
                    isPush = true;
                }
                sendNotificationSound(message, isCancelled);



            /*String sdkNotification = remoteMessage.getData().get(Constants.HT_SDK_NOTIFICATION_KEY);
            if (sdkNotification != null && sdkNotification.equalsIgnoreCase("true")) {
                *//*
                 * HyperTrack notifications are received here
                 * Dont handle these notifications. This might end up in a crash
                 *//*
                return;
            }*/
            } else {
                Log.d(TAG, "FCM Notification failed");
            }
        } catch (Exception e) {
        }
    }

    private void sendNotification(String message, String isCancelled) {
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra("push", isPush);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent,
                PendingIntent.FLAG_ONE_SHOT);
        Uri soundUri;
//        if (isCancelled.equalsIgnoreCase("true")) {
//            soundUri = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE + "://" + getPackageName()
//                    + "/raw/ride_cancelled");
//        } else
//            soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);


        if (message.equals("Votre trajet accepté par un chauffeur")) {
            soundUri = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE + "://" + getApplicationContext().getPackageName() + "/" + R.raw.driver_disconnected);
        } else if (message.equals("Chauffeur arrivé à votre position")) {
            soundUri = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE + "://" + getApplicationContext().getPackageName() + "/" + R.raw.driver_ride_cancelled);
        } else {
            soundUri = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE + "://" + getApplicationContext().getPackageName() + "/" + R.raw.driver_scheduled_ride);

        }


        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this,
                getString(R.string.default_notification_channel_id))
                .setSmallIcon(R.drawable.notification_white)
                .setContentTitle(getString(R.string.app_name))
                .setContentText(message)
                .setAutoCancel(true)
                .setSound(soundUri)
                .setContentIntent(pendingIntent);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                AudioAttributes attributes = new AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                        .build();

                NotificationChannel channel =
                        new NotificationChannel(getString(R.string.default_notification_channel_id),
                                "App Notifications",
                                NotificationManager.IMPORTANCE_HIGH);
                channel.setShowBadge(false);
                notificationBuilder.setBadgeIconType(NotificationCompat.BADGE_ICON_SMALL);
                notificationBuilder.setChannelId(getString(R.string.default_notification_channel_id));
                channel.setSound(soundUri, attributes);

                //  notificationBuilder.setSound(soundUri);
                notificationBuilder.build();
                notificationManager.createNotificationChannel(channel);
            }
            notificationManager.notify(notificationId++, notificationBuilder.build());
        }
    }

    private void sendNotificationSound(String messageBody, String isCancelled) {
        String CHANNEL_ID = "1234";
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra("Notification", messageBody);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent,
                PendingIntent.FLAG_ONE_SHOT);
        Uri soundUri;

        Log.d("Message", messageBody + "---");


        if (messageBody.equals("Votre course planifiée a commencé")) {
            soundUri = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE + "://" + getApplicationContext().getPackageName() + "/" + R.raw.driver_scheduled_ride);
        } else if (messageBody.equals("Utilisateur a annulé la course")) {
            soundUri = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE + "://" + getApplicationContext().getPackageName() + "/" + R.raw.driver_ride_cancelled);
            playNotificationSound(getApplicationContext(), soundUri);
            SharedHelper.putBoolean(getApplicationContext(), "IS_CANCEL", true);

        } else {
            soundUri = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE + "://" + getApplicationContext().getPackageName() + "/" + R.raw.alert_tone);
        }

        playNotificationSound(getApplicationContext(), soundUri);

        NotificationManager mNotificationManager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);

        //For API 26+ you need to put some additional code like below:
        NotificationChannel mChannel;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            mChannel = new NotificationChannel(CHANNEL_ID, "Top Driver", NotificationManager.IMPORTANCE_HIGH);
            mChannel.setLightColor(Color.GRAY);
            mChannel.enableLights(true);
            mChannel.setDescription(messageBody);
            AudioAttributes audioAttributes = new AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                    .build();
//            mChannel.setSound(soundUri, audioAttributes);

            if (mNotificationManager != null) {
                mNotificationManager.createNotificationChannel(mChannel);
            }
        }


        //General code:
        NotificationCompat.Builder status = new NotificationCompat.Builder(getApplicationContext(), CHANNEL_ID);
        // status.setAutoCancel(true)
        status.setWhen(System.currentTimeMillis())
                .setSmallIcon(R.drawable.logo)
                //.setOnlyAlertOnce(true)
                .setContentTitle(getString(R.string.app_name))
                .setContentText(messageBody)
                .setVibrate(new long[]{0, 500, 1000})
                .setDefaults(Notification.DEFAULT_LIGHTS)
//                .setSound(soundUri)
                .setContentIntent(pendingIntent);
        //.setContent(views);

        mNotificationManager.notify(1233, status.build());
    }

    private boolean isAppIsInBackground(Context context) {
        boolean isInBackground = true;
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT_WATCH) {
            List<ActivityManager.RunningAppProcessInfo> runningProcesses =
                    am.getRunningAppProcesses();
            for (ActivityManager.RunningAppProcessInfo processInfo : runningProcesses) {
                if (processInfo.importance ==
                        ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                    for (String activeProcess : processInfo.pkgList) {
                        if (activeProcess.equals(context.getPackageName())) {
                            isInBackground = false;
                        }
                    }
                }
            }
        } else {
            List<ActivityManager.RunningTaskInfo> taskInfo = am.getRunningTasks(1);
            ComponentName componentInfo = taskInfo.get(0).topActivity;
            if (componentInfo.getPackageName().equals(context.getPackageName())) {
                isInBackground = false;
            }
        }
        return isInBackground;
    }

    public void playNotificationSound(Context context, Uri notification) {
        try {
//            Uri notification = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE + "://" + getApplicationContext().getPackageName() + "/" + R.raw.ride_cancelled);/*RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);*/
//            Ringtone r = RingtoneManager.getRingtone(context, notification);
//            r.play();

            TopdriversApplication.getInstance().playNotificationSound(notification);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}