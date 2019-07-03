package com.example.menutask.utility;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;


import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.example.menutask.MainActivity;
import com.example.menutask.R;
import com.example.menutask.interfaces.AsyncResponse;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;


public class NotificationWorker extends Worker {
    private final String CHANNEL_ID="105";
    public NotificationWorker(
            @NonNull Context context,
            @NonNull WorkerParameters params) {
        super(context, params);
    }
    private final int notificationId=1;
    @NonNull
    @Override
    public Result doWork() {
        // Do the work here
        createNotificationChannel(getApplicationContext());
        checkForNewArticles();
        // Indicate whether the task finished successfully with the Result
        return Result.success();
    }
    private void checkForNewArticles(){
        /* date_time format which is accepted by API*/
        final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault());
        sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
        Calendar calendar = Calendar.getInstance();
        /* getting articles published in last 20 minutes*/
        calendar.add(Calendar.MINUTE,-20);
        String fromDate=sdf.format(calendar.getTime());
        final String checkURL="https://content.guardianapis.com/search?type=article&api-key=521507b1-6b35-4656-8c03-de57542895f0&from-date="+fromDate;
        new GetJson(checkURL, new AsyncResponse() {
            @Override
            public void processFinish(String output) {
                try {
                    JSONObject obj = new JSONObject(output);
                    String status=obj.getJSONObject("response").getString("status");

                    final int total=obj.getJSONObject("response").getInt("total");
                    if(status.equals("ok") && total > 0) {
                        sendNotification(total);
                    }

                }catch(JSONException e){
                    e.printStackTrace();
                }


            }
        }).execute();
    }
    private void sendNotification(int numberOfnewArticles){
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        String notificationText=numberOfnewArticles + " new articles are available";
        PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
       // Bitmap notIcon= BitmapFactory.decodeResource(getApplicationContext().getResources(), R.drawable.android_icon);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), CHANNEL_ID)
                .setSmallIcon(R.drawable.android_icon)
                .setContentTitle("New Articles are available")
                /* notify with sound, light and vibration */
                .setDefaults(-1)
                .setContentText(notificationText)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(notificationText))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(getApplicationContext());

        // notificationId is a unique int for each notification that you must define
        notificationManager.notify(notificationId, builder.build());
    }
    private void createNotificationChannel(@NonNull Context context) {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "channel_name";
            String description = "channel_description";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }
}
