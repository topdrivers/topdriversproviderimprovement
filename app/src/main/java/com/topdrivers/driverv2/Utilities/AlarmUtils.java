package com.topdrivers.driverv2.Utilities;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import com.topdrivers.driverv2.Services.AlarmReceiver;

import java.util.Calendar;


/**
 * Created by framgia on 23/05/2017.
 */
public class AlarmUtils {
    private static int INDEX = 1;

    public static void create(Context context, long time) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, AlarmReceiver.class);
        intent.putExtra(Constant.KEY_TYPE, INDEX);
        PendingIntent pendingIntent =
                PendingIntent.getBroadcast(context, INDEX, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MINUTE, INDEX);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            alarmManager
                    .setExact(AlarmManager.RTC_WAKEUP, time, pendingIntent);
        } else {
            alarmManager
                    .set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
        }
    }
}
