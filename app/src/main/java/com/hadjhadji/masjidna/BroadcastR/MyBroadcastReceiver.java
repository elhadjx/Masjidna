package com.hadjhadji.masjidna.BroadcastR;

import static android.content.Context.ALARM_SERVICE;
import static androidx.legacy.content.WakefulBroadcastReceiver.startWakefulService;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.PowerManager;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.hadjhadji.masjidna.NotifActivity;
import com.hadjhadji.masjidna.R;
import com.hadjhadji.masjidna.services.AudioAdhan;
import com.hadjhadji.masjidna.services.NotifierService;

import java.util.Date;

public class MyBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        try {
            ReportToFirebase("ClassMonitor","MyBroadCastReceiver/onReceive Started");

            //Toast.makeText(context.getApplicationContext(), "yes", Toast.LENGTH_SHORT).show();
            PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
            @SuppressLint("InvalidWakeLockTag") PowerManager.WakeLock wakeLock = pm.newWakeLock((PowerManager.PARTIAL_WAKE_LOCK| PowerManager.ACQUIRE_CAUSES_WAKEUP), "TAG");
            wakeLock.acquire();



            boolean extraIsAdhan = false;
            String salatName = "";
            try{
                extraIsAdhan = (Boolean) intent.getExtras().get("isAdhan");
                salatName = (String) intent.getExtras().get("salatName");
            } catch (Exception e){
                ReportToFirebase("MyBRgetxtra",e.getMessage());
                e.printStackTrace();
            }

            if (extraIsAdhan){
                createNotification(salatName,context.getString(R.string.prayerNotifar) + " " + salatName, context);

                if (NotifActivity.FULL_SCREEN_ACTION.equals(intent.getAction()))
                    NotifActivity.CreateFullScreenNotification(context);
                NotifActivity.CreateFullScreenNotification(context);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(new Intent(context, NotifierService.class));
                    context.startForegroundService(new Intent(context, AudioAdhan.class));
                    context.startForegroundService(new Intent(context, NotifActivity.class));
                }
                playAdhan(context);
                context.startService(new Intent(context, NotifierService.class));
                context.startService(new Intent(context, AudioAdhan.class));
                context.startService(new Intent(context, NotifActivity.class));
                startWakefulService(context, new Intent(context, NotifierService.class));
                startWakefulService(context, new Intent(context, AudioAdhan.class));
                startWakefulService(context, new Intent(context, NotifActivity.class));
                Intent pintent = new Intent(context, NotifActivity.class);
                pintent.putExtra("isAdhan",extraIsAdhan);
                pintent.putExtra("salatName",salatName);
                pintent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(pintent);
            } else {
                createSimpleNotification(salatName,context.getString(R.string.prayerNotif) + " " + salatName, context);
                RingtoneManager.getRingtone(context, RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)).play();

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(new Intent(context, NotifierService.class));
                }
                context.startService(new Intent(context, NotifierService.class));
                startWakefulService(context, new Intent(context, NotifierService.class));

            }


            Log.e("MyBroadcastReceiver","extraIsAdhan: " + extraIsAdhan);

        } catch (Exception e){
            ReportToFirebase("MyBroadcastRec",e.getMessage());
        }

    }
    public void createSimpleNotification(String title, String message, Context context){
        createNotificationChannel(context);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "MasjidnaChannel")
                .setSmallIcon(R.drawable.ic_mosque_svgrepo_com)
                .setContentTitle(title)
                .setContentText(message)
                .setVibrate(new long[]{1000, 2000, 4000, 4000})
                .setPriority(NotificationCompat.PRIORITY_MAX);

        RingtoneManager.getRingtone(context, RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)).play();
        NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(context);
        notificationManagerCompat.notify(100,builder.build());
    }
    public void createNotification(String title, String message, Context context){
        createNotificationChannel(context);
        playAdhan(context);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "MasjidnaChannel")
                .setSmallIcon(R.drawable.ic_mosque_svgrepo_com)
                .setContentTitle(title)
                .setContentText(message)
                .setVibrate(new long[]{1000, 2000, 4000, 4000})
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setSound(Uri.parse("android.resource://"
                        + context.getPackageName() + "/" + R.raw.azan1));
        NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(context);
        notificationManagerCompat.notify(100,builder.build());
    }
    private void playAdhan(Context context) {
        MediaPlayer mediaplayer = MediaPlayer.create(context, R.raw.azan1);
        if(mediaplayer == null) {
            Log.v("myBroad", "Create() on MediaPlayer failed.");
        } else {
            mediaplayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {

                @Override
                public void onCompletion(MediaPlayer mediaplayer) {
                    mediaplayer.stop();
                    mediaplayer.release();
                }
            });

            mediaplayer.start();
        }
    }
    private void createNotificationChannel(Context context) {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "MasjidnaChannel" ;
            String description = "Masjidna Channel Description";
            int importance = NotificationManager.IMPORTANCE_MAX;
            @SuppressLint("WrongConstant")
            NotificationChannel channel = new NotificationChannel("MasjidnaChannel", name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }
    public static void ReportToFirebase(String title, String message){
        FirebaseDatabase database = FirebaseDatabase.getInstance("https://masjidna-8e74b-default-rtdb.europe-west1.firebasedatabase.app");
        DatabaseReference report_ref = database.getReference("reports").child(title).child(timestampToHHmm(System.currentTimeMillis()));
        report_ref.setValue(message);
    }
    public static String timestampToHHmm(long timestampInMillis){
        timestampInMillis = (timestampInMillis/1000)/60; // to minutes
        timestampInMillis += 60;
        int minutes = (int) (timestampInMillis % 60);
        int hours = (int) ((timestampInMillis / 60) % 24);
        String str_minutes = minutes + "";
        String str_hours = hours + "";
        if (minutes < 10)
            str_minutes = "0" + minutes;
        if (hours < 10)
            str_hours = "0"+hours;
        return str_hours+":"+str_minutes;

    }
}
