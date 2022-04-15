package com.hadjhadji.masjidna.services;

import static com.hadjhadji.masjidna.NotifActivity.FULL_SCREEN_ACTION;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.IntentService;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.RingtoneManager;
import android.os.Build;
import android.os.CountDownTimer;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.hadjhadji.masjidna.BroadcastR.MyBroadcastReceiver;
import com.hadjhadji.masjidna.BroadcastR.MyReceiver;
import com.hadjhadji.masjidna.R;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

public class NotifierService extends IntentService  {
    public NotifierService(){
        super(null);
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {

    }

    @Override
    public int onStartCommand(@Nullable Intent intent, int flags, int startId) {

        try {

            //getting last news timestamp from shared prefs
            SharedPreferences sharedPref = getSharedPreferences(getString(R.string.masjidna_shared_pref),Context.MODE_PRIVATE);

            final long[] lastNewsTimestamp = {sharedPref.getLong("lastNewsTimestamp", 0)};



            // Check for news from Firebase & notify
            FirebaseDatabase database = FirebaseDatabase.getInstance("https://masjidna-8e74b-default-rtdb.europe-west1.firebasedatabase.app");
            DatabaseReference notif_ref = database.getReference("Notifications").child("Last");
            notif_ref.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    long updatedLastNewsTimestamp = Long.parseLong(""+snapshot.child("timestamp").getValue());

                    if (updatedLastNewsTimestamp > lastNewsTimestamp[0]) {

                        //update the shared prefs
                        SharedPreferences.Editor editor = sharedPref.edit();
                        editor.putLong("lastNewsTimestamp", updatedLastNewsTimestamp);
                        editor.apply();
                        lastNewsTimestamp[0] = updatedLastNewsTimestamp;
                        //notify new news message
                        String notif_msg = String.valueOf(snapshot.child("message").getValue());
                        if (notif_msg.length()>30){
                            notif_msg = notif_msg.substring(0,27) + "...";
                            RingtoneManager.getRingtone(getApplicationContext(), RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)).play();
                            createSimpleNotification(""+snapshot.child("title").getValue(),notif_msg, getApplicationContext());
                        }

                    }


                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });


            //checking time for salat

            ArrayList<Long> prayerTimes = new ArrayList<>(Arrays.asList(
                    Long.parseLong(todaysFtoTimestamp(""+sharedPref.getString("fajr","0"))),
                    Long.parseLong(todaysFtoTimestamp(""+sharedPref.getString("dohr","0"))),
                    Long.parseLong(todaysFtoTimestamp(""+sharedPref.getString("asr","0"))),
                    Long.parseLong(todaysFtoTimestamp(""+sharedPref.getString("maghreb","0"))),
                    Long.parseLong(todaysFtoTimestamp(""+sharedPref.getString("isha","0")))
            ));
            long currentTime = (System.currentTimeMillis() / 60000)*60 + 3600;

            for (int i = 0; i < prayerTimes.size(); i++) {
                Log.e("NotifierService",i+" " + prayerTimes.get(i)
                        + "(" +timestampToHHmm(prayerTimes.get(i)*1000) + ")\t "
                        + currentTime + "(" + timestampToHHmm(currentTime*1000) + ")");
                String salatName = "";
                String language = sharedPref.getString("salatLang","es");
                switch (i){
                    case 0:
                        salatName = getString(R.string.Fajr);
                        if (language.equals("ar")) salatName="الفجر";
                        break;
                    case 1:
                        salatName = getString(R.string.Dhuhr);
                        if (language.equals("ar")) salatName="الظهر";
                        break;
                    case 2:
                        salatName = getString(R.string.Asr);
                        if (language.equals("ar")) salatName="العصر";
                        break;
                    case 3:
                        salatName = getString(R.string.Maghrib);
                        if (language.equals("ar")) salatName="المغرب";
                        break;
                    case 4:
                        salatName = getString(R.string.Isha);
                        if (language.equals("ar")) salatName="العشاء";
                        break;
                }
                if (currentTime  < prayerTimes.get(i)){

                    Log.e("NotifierService","chosen prayer " + salatName);
                    if (currentTime < (prayerTimes.get(i) - 600)){
                        setAlarm(getApplicationContext(),(prayerTimes.get(i) - 600), false, salatName); // 10 minutes before Adhan Time
                    } else {
                        setAlarm(getApplicationContext(),prayerTimes.get(i),true, salatName); // Adhan Time
                    }

                    break;
                } else Log.e("prayerN","not for "+ salatName);
            }
        } catch (Exception e) {
            e.printStackTrace();
            ReportToFirebase("NotifierService/Exception",e.getMessage());
        }


        return START_NOT_STICKY;
    }
    public void createSimpleNotification(String title, String message, Context context){
        createNotificationChannel();
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
    public void setAlarm(Context context, long timeInS, Boolean isAdhan, String salatName){
        timeInS -= 3600; // timezone -1 hour gmt+1
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        Intent updateServiceIntent = new Intent(context, MyBroadcastReceiver.class);
        PendingIntent pendingUpdateIntent = PendingIntent.getService(context, 0, updateServiceIntent, 0);

        // Cancel alarms
        try {
            alarmManager.cancel(pendingUpdateIntent);
        } catch (Exception e) {
            Log.e("NotifierService", "AlarmManager update was not canceled. " + e.toString());
        }
        try {
            Intent intent;
            if (isAdhan){
                intent = new Intent(FULL_SCREEN_ACTION, null,this, MyBroadcastReceiver.class);
            } else {
                intent = new Intent(this,MyBroadcastReceiver.class);
            }
            intent.putExtra("isAdhan", isAdhan);
            intent.putExtra("salatName",salatName);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(
                    this.getApplicationContext(), 234324243, intent, 0);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, timeInS * 1000, pendingIntent);
            } else {
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, timeInS * 1000, pendingIntent);
            }
            ReportToFirebase("NotifierService", "ASF: " + isAdhan + " " +timestampToHHmm((timeInS+3600)*1000));

        } catch (Exception e){
            ReportToFirebase("NotifierService",e.getMessage());
        }
    }
    public void createNotification(String title, String message){
        createNotificationChannel();
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "MasjidnaChannel")
                .setSmallIcon(R.drawable.ic_mosque_svgrepo_com)
                .setContentTitle(title)
                .setContentText(message)
                .setVibrate(new long[]{1000, 2000, 4000, 4000})
                .setPriority(NotificationCompat.PRIORITY_MAX);
        RingtoneManager.getRingtone(getApplicationContext(), RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)).play();
        NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(this);
        notificationManagerCompat.notify(100,builder.build());
    }
    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            long currentTime = System.currentTimeMillis();
            CharSequence name = "MasjidnaChannel";
            String description = "Masjidna Channel Description" ;
            int importance = NotificationManager.IMPORTANCE_MAX;
            @SuppressLint("WrongConstant")
            NotificationChannel channel = new NotificationChannel("MasjidnaChannel", name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }
    private static String todaysFtoTimestamp(String f){
        long todaystimestamp = ((System.currentTimeMillis() / 86400000 )  * 86400 );
        //System.out.println("today: " + todaystimestamp);
        long todaysftimestamp = Integer.parseInt(fToTimestamp(f)) + todaystimestamp;
        //System.out.println("todayF: " + todaysftimestamp);
        return todaysftimestamp+"";
    }
    private static String fToTimestamp(String f){
        Log.e("fToTimestamp",f);
        String[] fArray = f.split(":");
        String r = "null hehe";
        try {
            int hours = Integer.parseInt(fArray[0]);
            int minutes = Integer.parseInt(fArray[1]);
            r = String.valueOf((minutes + (hours * 60)) * 60);
        } catch (Exception e){
            r = "0";
        }

        return r ;
    }
    public static void ReportToFirebase(String title, String message){
        Log.e("NotifierService",title + ": " + message);
        FirebaseDatabase database = FirebaseDatabase.getInstance("https://masjidna-8e74b-default-rtdb.europe-west1.firebasedatabase.app");
        DatabaseReference report_ref = database.getReference("reports").child(title).child(timestampToHHmm(System.currentTimeMillis()+3600000));
        //report_ref.setValue(message);
    }
    public static String timestampToHHmm(long timestampInMillis){
        timestampInMillis = (timestampInMillis/1000)/60; // to minutes
        //timestampInMillis += 60;
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
