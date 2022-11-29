package com.topdrivers.driverv2.Services;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.AudioAttributes;
import android.net.Uri;
import android.os.Build;

import androidx.core.app.NotificationCompat;

import com.topdrivers.driverv2.Activity.MainActivity;
import com.topdrivers.driverv2.Helper.SharedHelper;
import com.topdrivers.driverv2.R;
import com.topdrivers.driverv2.TopdriversApplication;

public class AlarmReceiver extends BroadcastReceiver {
    private static final int TIME_VIBRATE = 1000;

    @Override
    public void onReceive(Context context, Intent intent) {

    //    Toast.makeText(context, "Alarm aaa", Toast.LENGTH_SHORT).show();
        SharedHelper.putBoolean(context, "OnTime", true);
        sendNotificationSound(context);
    }

    private void sendNotificationSound(Context context) {
        String CHANNEL_ID = "1234";
        Intent intent = new Intent(context, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent,
                PendingIntent.FLAG_ONE_SHOT);
        Uri soundUri;


        soundUri = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE + "://" + context.getApplicationContext().getPackageName() + "/" + R.raw.driver_scheduled_ride);
        TopdriversApplication.getInstance().playNotificationSound(soundUri);
        NotificationManager mNotificationManager = (NotificationManager) context.getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);

        //For API 26+ you need to put some additional code like below:
        NotificationChannel mChannel;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            mChannel = new NotificationChannel(CHANNEL_ID, "Top Driver", NotificationManager.IMPORTANCE_HIGH);
            mChannel.setLightColor(Color.GRAY);
            mChannel.enableLights(true);
            mChannel.setDescription("Votre course planifiée a commencé");
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
        NotificationCompat.Builder status = new NotificationCompat.Builder(context.getApplicationContext(), CHANNEL_ID);
        // status.setAutoCancel(true)
        status.setWhen(System.currentTimeMillis())
                .setSmallIcon(R.drawable.logo)
                //.setOnlyAlertOnce(true)
                .setContentTitle(context.getString(R.string.app_name))
                .setContentText("Votre course planifiée a commencé")
                .setVibrate(new long[]{0, 500, 1000})
                .setDefaults(Notification.DEFAULT_LIGHTS)
//                .setSound(soundUri)
                .setContentIntent(pendingIntent);
        //.setContent(views);

        mNotificationManager.notify(1233, status.build());
    }
}
