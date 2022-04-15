package com.hadjhadji.masjidna.BroadcastR;

import static android.content.Context.ALARM_SERVICE;
import static androidx.legacy.content.WakefulBroadcastReceiver.startWakefulService;

import static com.hadjhadji.masjidna.NotifActivity.FULL_SCREEN_ACTION;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.hadjhadji.masjidna.MainActivity;
import com.hadjhadji.masjidna.NotifActivity;
import com.hadjhadji.masjidna.R;
import com.hadjhadji.masjidna.services.NotifierService;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Locale;

public class MyReceiver extends BroadcastReceiver {
    String currentDate;
    static SharedPreferences sharedPref;
    static Context scontext;
    @Override
    public void onReceive(Context context, Intent intent) {
        scontext = context;
        final PendingResult pendingResult = goAsync();
        Task asyncTask = new Task(pendingResult, intent);
        asyncTask.execute();
    }

    private static class Task extends AsyncTask<String, Integer, String> {

        private final PendingResult pendingResult;
        private final Intent intent;

        private Task(PendingResult pendingResult, Intent intent) {
            this.pendingResult = pendingResult;
            this.intent = intent;

        }

        @Override
        protected String doInBackground(String... strings) {
            Log.e("Masjidna/MyReceiver","i checked " + timestampToHHmm(System.currentTimeMillis()));
            /*sharedPref = scontext.getSharedPreferences(scontext.getString(R.string.masjidna_shared_pref),Context.MODE_PRIVATE);
            redo(scontext, intent);

            //getting last news timestamp from shared prefs
            SharedPreferences sharedPref = scontext.getSharedPreferences(scontext.getString(R.string.masjidna_shared_pref),Context.MODE_PRIVATE);

            final long[] lastNewsTimestamp = {sharedPref.getLong("lastNewsTimestamp", 0)};

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
                        }
                        createNotification(""+snapshot.child("title").getValue(),notif_msg,scontext);

                    }

                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });

            ReportToFirebase("MyReceiver","I checked: " + timestampToHHmm(System.currentTimeMillis()) );
            */

            redo(scontext,intent);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                scontext.startForegroundService(new Intent(scontext, NotifierService.class));
            }
            scontext.startService(new Intent(scontext, NotifierService.class));
            startWakefulService(scontext, new Intent(scontext, NotifierService.class));
            StringBuilder sb = new StringBuilder();
            sb.append("Action: " + intent.getAction() + "\n");
            sb.append("URI: " + intent.toUri(Intent.URI_INTENT_SCHEME).toString() + "\n");
            String log = sb.toString();
            Log.e("MyReceiver", log);
            return log;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            // Must call finish() so the BroadcastReceiver can be recycled.
            pendingResult.finish();
        }
    }
    private static void redo(Context context, Intent intent) {
        try {
            String currentDate = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(new Date());
            FirebaseDatabase database = FirebaseDatabase.getInstance("https://masjidna-8e74b-default-rtdb.europe-west1.firebasedatabase.app");
            DatabaseReference salat_ref = database.getReference("Salat").child(currentDate);
            salat_ref.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {

                    String fajrStr = "" + snapshot.child("fajr").getValue();
                    String dohrStr = "" + snapshot.child("dohr").getValue();
                    String asrStr = "" + snapshot.child("asr").getValue();
                    String maghrebStr = "" + snapshot.child("maghreb").getValue();
                    String ishaStr = "" + snapshot.child("isha").getValue();


                    //set to shared prefs
                    SharedPreferences.Editor editor = sharedPref.edit();
                    editor.putString("fajr", fajrStr);
                    editor.putString("dohr", dohrStr);
                    editor.putString("asr", asrStr);
                    editor.putString("maghreb", maghrebStr);
                    editor.putString("isha", ishaStr);
                    editor.apply();
                    Log.e("Masjidna/MyReceiver","Updated Salat");
                    Log.e("Masjidna/MyReceiver","Fajr: " + fajrStr);
                    Log.e("Masjidna/MyReceiver","Dohr: " + dohrStr);
                    Log.e("Masjidna/MyReceiver","Asr: " + asrStr);
                    Log.e("Masjidna/MyReceiver","Maghreb: " + maghrebStr);
                    Log.e("Masjidna/MyReceiver","Isha: " + ishaStr);
                    long currentTime = (System.currentTimeMillis()/60000)*60;
                    ArrayList<Long> prayerTimes = new ArrayList<>(Arrays.asList(
                            Long.parseLong(todaysFtoTimestamp(""+sharedPref.getString("fajr","0"))),
                            Long.parseLong(todaysFtoTimestamp(""+sharedPref.getString("dohr","0"))),
                            Long.parseLong(todaysFtoTimestamp(""+sharedPref.getString("asr","0"))),
                            Long.parseLong(todaysFtoTimestamp(""+sharedPref.getString("maghreb","0"))),
                            Long.parseLong(todaysFtoTimestamp(""+sharedPref.getString("isha","0")))
                    ));

                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }

            });
        } catch (Exception e) {
            Log.e("Masjidna/MyReceiver",e.getMessage());
        }
    }
    public static void createNotification(String title, String message, Context context){
        createNotificationChannel(context);
        Intent notificationIntent = new Intent(context, MainActivity.class);
        notificationIntent.putExtra("Fragment","NotificationFragment");
        PendingIntent contentIntent = PendingIntent.getActivity(context, 0, notificationIntent, 0);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "MasjidnaChannel")
                .setSmallIcon(R.drawable.ic_mosque_svgrepo_com)
                .setContentTitle(title)
                .setContentText(message)
                .setVibrate(new long[]{1000, 2000, 4000, 4000})
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setContentIntent(contentIntent);

        //playAdhan(context);
        RingtoneManager.getRingtone(context, RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)).play();
        NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(context);
        notificationManagerCompat.notify(100,builder.build());
    }
    private static void createNotificationChannel(Context context) {
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
    public void setAlarm(Context context,long timeInS){
        timeInS -= 3600; //-3600 gmt timezone one hour
        try {
            Intent intent = new Intent(FULL_SCREEN_ACTION, null,context, MyBroadcastReceiver.class);
            intent.putExtra("isAdhan", true);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(
                    context, 234324243, intent, 0);
            AlarmManager alarmManager = (AlarmManager) context.getSystemService(ALARM_SERVICE);

            alarmManager.setExact(AlarmManager.RTC_WAKEUP, timeInS * 1000, pendingIntent);

            Log.e("MyReceiver",timestampToHHmm(timeInS*1000));
            ReportToFirebase("MyReceiver","ASF adhan: "+ timestampToHHmm(timeInS*1000));
        } catch (Exception e){
            ReportToFirebase("Alarm in MyReceiver",e.getMessage());
        }
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


}