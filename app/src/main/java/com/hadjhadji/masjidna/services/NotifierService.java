package com.hadjhadji.masjidna.services;

import android.annotation.SuppressLint;
import android.app.IntentService;
import android.app.NotificationChannel;
import android.app.NotificationManager;
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
import com.hadjhadji.masjidna.MainActivity;
import com.hadjhadji.masjidna.R;
import com.hadjhadji.masjidna.models.Notification;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

public class NotifierService extends IntentService  {
    public NotifierService(){
        super(null);
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        System.out.println("i'm running hahaha");
        Log.e("hehehe","heheheheheh");
        CountDownTimer countDownTimer = new CountDownTimer(60000,1000) {
            @Override
            public void onTick(long l) {
                Log.e("1onTick",""+ l);
            }

            @Override
            public void onFinish() {
                Log.e("1onFinish","finished");

            }
        };
        countDownTimer.start();
    }

    @Override
    public int onStartCommand(@Nullable Intent intent, int flags, int startId) {
        new Thread(){

            @Override
            public void run() {
                super.run();
                while(true){
                    try {

                        Thread.sleep(10000);// 60 seconds

                        //getting last news timestamp from shared prefs
                        SharedPreferences sharedPref = getSharedPreferences(getString(R.string.masjidna_shared_pref),Context.MODE_PRIVATE);

                        final long[] lastNewsTimestamp = {sharedPref.getLong("lastNewsTimestamp", 0)};

                        /*SharedPreferences.Editor editor = sharedPref.edit();
                        for (int i = 0; i < 5; i++) {
                            editor.putBoolean("notified"+i,false);
                        }
                        editor.apply();*/


                        // get last news timestamp from firebase
                        // & check with last news timestamp on shared prefs
                        // & maybe notify


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
                                    createNotification(""+snapshot.child("title").getValue(),notif_msg);

                                }


                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });

                        //checking time if it's closer to Salat (10 minutes) to notify

                        ArrayList<Long> prayerTimes = new ArrayList<>(Arrays.asList(
                                Long.parseLong(todaysFtoTimestamp(""+sharedPref.getString("fajr","0"))),
                                Long.parseLong(todaysFtoTimestamp(""+sharedPref.getString("dohr","0"))),
                                Long.parseLong(todaysFtoTimestamp(""+sharedPref.getString("asr","0"))),
                                Long.parseLong(todaysFtoTimestamp(""+sharedPref.getString("maghreb","0"))),
                                Long.parseLong(todaysFtoTimestamp(""+sharedPref.getString("isha","0")))
                        ));
                        long currentTime = System.currentTimeMillis() / 1000;

                        for (int i = 0; i < prayerTimes.size(); i++) {
                            Log.e("prayer",i+" " + prayerTimes.get(i) + "\t " + currentTime);
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

                                Log.e("prayer2","chosen prayer " + salatName);

                                if (currentTime > (prayerTimes.get(i) - 630000)){
                                    Log.e("notification",""+sharedPref.getBoolean("notified"+i,false));
                                    if(sharedPref.getString("salatLang","es").equals("ar")){
                                        createNotification(salatName,salatName + " تبقى دقائق لصلاة ");
                                    }else{
                                        createNotification(salatName,salatName + " تبقى دقائق لصلاة ");
                                        //createNotification(salatName,"Quedan minutos para " + salatName);
                                    }
                                    /*if (!(sharedPref.getBoolean("notified"+i,false))){
                                        if(sharedPref.getString("salatLang","es").equals("ar")){
                                            createNotification(salatName,salatName + " تبقى دقائق لصلاة ");
                                        }else{
                                            createNotification(salatName,salatName + " تبقى دقائق لصلاة ");
                                            //createNotification(salatName,"Quedan minutos para " + salatName);
                                        }
                                        sharedPref.edit().putBoolean("notified"+i,true).apply();
                                        if (i==4) {
                                            sharedPref.edit().putBoolean("notified0",false).apply();
                                        } else {
                                            sharedPref.edit().putBoolean("notified"+(i+1),false).apply();
                                        }
                                    }*/
                                }
                                break;
                            } else Log.e("prayerN","not for "+ salatName);
                        }


                    } catch (InterruptedException e) {
                        e.printStackTrace();
                        ReportToFirebase(e.getMessage());
                    }
                }
            }
        }.start();



        return START_NOT_STICKY;
        //return super.onStartCommand(intent, flags, startId);
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
            ReportToFirebase(e.getMessage());
        }

        return r ;
    }
    private static void ReportToFirebase(String message){
        FirebaseDatabase database = FirebaseDatabase.getInstance("https://masjidna-8e74b-default-rtdb.europe-west1.firebasedatabase.app");
        DatabaseReference report_ref = database.getReference("reports").child((System.currentTimeMillis()/1000)+"");
        report_ref.setValue(message);
    }
}
