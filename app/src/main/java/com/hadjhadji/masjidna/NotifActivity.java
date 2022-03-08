package com.hadjhadji.masjidna;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.hadjhadji.masjidna.BroadcastR.MyBroadcastReceiver;
import com.hadjhadji.masjidna.BroadcastR.MyReceiver;
import com.hadjhadji.masjidna.services.NotifierService;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class NotifActivity extends AppCompatActivity {

    private static final String TAG = "NotifAct" ;
    private static final String CHANNEL_ID = "my_channel";
    public static final String FULL_SCREEN_ACTION = "full_screen_action";
    static final int NOTIFICATION_ID = 1;
    Button btn_close;
    TextView notifActFullscreenTv;
    MediaPlayer mediaplayer;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notif);

        ReportToFirebase("ClassMonitor","NotifActivity/OnCreate Started");

        //07-03-2022
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
                | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
                | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        //
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            // For newer than Android Oreo: call setShowWhenLocked, setTurnScreenOn
            setShowWhenLocked(true);
            setTurnScreenOn(true);

        } else {
            // For older versions, do it as you did before.
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }
        startService(new Intent(getApplication(), NotifierService.class));

        SharedPreferences sharedPreferences = getSharedPreferences(getString(R.string.masjidna_shared_pref),Context.MODE_PRIVATE);

        //Defining Views
        btn_close = findViewById(R.id.btn_closeAdhan);
        notifActFullscreenTv = findViewById(R.id.notifActFullscreenTv);

        refreshSalat();

        String salatName = "";
        Context context = getApplicationContext();
        long currentTime = (System.currentTimeMillis()/60000) * 60;
        ArrayList<Long> prayerTimes = new ArrayList<>(Arrays.asList(
                Long.parseLong(todaysFtoTimestamp(""+sharedPreferences.getString("fajr","0"))),
                Long.parseLong(todaysFtoTimestamp(""+sharedPreferences.getString("dohr","0"))),
                Long.parseLong(todaysFtoTimestamp(""+sharedPreferences.getString("asr","0"))),
                Long.parseLong(todaysFtoTimestamp(""+sharedPreferences.getString("maghreb","0"))),
                Long.parseLong(todaysFtoTimestamp(""+sharedPreferences.getString("isha","0")))
        ));
        for (int i = 0; i < prayerTimes.size(); i++) {
            if (prayerTimes.get(i) == currentTime ) {
                if (i == 4){
                    getnextFajr();
                    setAlarm(Long.parseLong(todaysFtoTimestamp(""+sharedPreferences.getString("fajr","0"))));
                } else {
                    setAlarm(prayerTimes.get(i+1));
                }

                String language = sharedPreferences.getString("salatLang", "es");

                switch (i) {
                    case 1:
                        salatName = getString(R.string.Fajr);
                        if (language.equals("ar")) salatName = "الفجر";
                        break;
                    case 2:
                        salatName = getString(R.string.Dhuhr);
                        if (language.equals("ar")) salatName = "الظهر";
                        break;
                    case 3:
                        salatName = getString(R.string.Asr);
                        if (language.equals("ar")) salatName = "العصر";
                        break;
                    case 4:
                        salatName = getString(R.string.Maghrib);
                        if (language.equals("ar")) salatName = "المغرب";
                        break;
                    case 0:
                        salatName = getString(R.string.Isha);
                        if (language.equals("ar")) salatName = "العشاء";
                        break;
                }

                break;
            }
        }
        notifActFullscreenTv.setText(context.getString(R.string.prayerNotifar) + " " + salatName);



        //Create MP ADHAN
        mediaplayer = MediaPlayer.create(NotifActivity.this, R.raw.azan1);
        if(mediaplayer == null) {
            Log.v(TAG, "Create() on MediaPlayer failed.");
        } else {
            mediaplayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {

                @Override
                public void onCompletion(MediaPlayer mediaplayer) {
                    mediaplayer.stop();
                    mediaplayer.release();
                    finish();
                }
            });

            mediaplayer.start();
        }



        btn_close.setOnClickListener((view -> {
            mediaplayer.stop();
            finish();
        }));




        /*Intent i = new Intent(getApplicationContext(),MainActivity.class);
        startActivity(i);*/

        /*
        SharedPreferences sharedPref = getSharedPreferences(getString(R.string.masjidna_shared_pref),Context.MODE_PRIVATE);


        boolean extraIsAdhan = false;
        try {
            extraIsAdhan = (Boolean) getIntent().getExtras().get("isAdhan");
        } catch (Exception e){
            ReportToFirebase("NotifAcXtra",e.getMessage());
            e.printStackTrace();
        }
        Log.e("NotifActivity","extraIsAdhan: " + extraIsAdhan);
        if (extraIsAdhan) {
            createNotification("Adhan", "Adhan time");
        } else {
            createNotification("Adhan", "10 minutes left");

            //setAlarm(sharedPref.getLong("nextSalatTimeStamp",0),true);
        }
        */
        /*createNotification("Adhan", "10 minutes left");
        Intent startMain = new Intent(Intent.ACTION_MAIN);
        startMain.addCategory(Intent.CATEGORY_HOME);
        startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(startMain);*/
    }

    @Override
    protected void onStop() {
        super.onStop();
        mediaplayer.stop();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mediaplayer.stop();
    }

    public void setAlarm(long timeInS){
        timeInS -= 3600; //-3600 gmt timezone one hour
        try {
            Intent intent = new Intent(FULL_SCREEN_ACTION, null,this, MyBroadcastReceiver.class);
            intent.putExtra("isAdhan", true);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(
                    this.getApplicationContext(), 234324243, intent, 0);
            AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
            //timeInS - 3600 ( one hour, problem of timezone)

            alarmManager.setExact(AlarmManager.RTC_WAKEUP, timeInS * 1000, pendingIntent);
            Date date = new Date();
            date.setTime(timeInS * 1000);
            ReportToFirebase("NotifActivity","ASF adhan:"+ date.toString());
        } catch (Exception e){

            ReportToFirebase("NotifActivity",e.getMessage());
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
    void getnextFajr(){
        Calendar c = Calendar.getInstance();
        c.setTime(new Date());
        c.add(Calendar.DAY_OF_MONTH,1);
        String currentDate = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(c.getTime());
        FirebaseDatabase database = FirebaseDatabase.getInstance("https://masjidna-8e74b-default-rtdb.europe-west1.firebasedatabase.app");
        DatabaseReference salat_ref = database.getReference("Salat").child(currentDate);
        salat_ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                String fajrStr = ""+snapshot.child("fajr").getValue();


                //set to shared prefs
                SharedPreferences sharedPref = getSharedPreferences(getString(R.string.masjidna_shared_pref),Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putString("fajr",fajrStr);
                editor.apply();
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    void refreshSalat(){
        Calendar c = Calendar.getInstance();
        c.setTime(new Date());
        String currentDate = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(c.getTime());

        FirebaseDatabase database = FirebaseDatabase.getInstance("https://masjidna-8e74b-default-rtdb.europe-west1.firebasedatabase.app");
        DatabaseReference salat_ref = database.getReference("Salat").child(currentDate);
        salat_ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                String fajrStr = ""+snapshot.child("fajr").getValue();
                String dohrStr = ""+snapshot.child("dohr").getValue();
                String asrStr = ""+snapshot.child("asr").getValue();
                String maghrebStr = ""+snapshot.child("maghreb").getValue();
                String ishaStr = ""+snapshot.child("isha").getValue();


                //set to shared prefs
                SharedPreferences sharedPref = getSharedPreferences(getString(R.string.masjidna_shared_pref),Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putString("fajr",fajrStr);
                editor.putString("dohr",dohrStr);
                editor.putString("asr",asrStr);
                editor.putString("maghreb",maghrebStr);
                editor.putString("isha",ishaStr);
                editor.apply();
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }
    public static void CreateFullScreenNotification(Context context) {
        Intent intent = new Intent(context, NotifActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_NO_USER_ACTION | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(context, "MasjidnaChannelF")
                        .setSmallIcon(R.drawable.ic_launcher_background)
                        .setContentTitle("Full Screen Alarm Test")
                        .setContentText("This is a test")
                        .setPriority(NotificationCompat.PRIORITY_MAX)
                        .setCategory(NotificationCompat.CATEGORY_ALARM)
                        .setContentIntent(pendingIntent)
                        .setFullScreenIntent(pendingIntent, true);
        NotificationManagerCompat.from(context).notify(1599, notificationBuilder.build());
    }

    private static void createNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);

            if (notificationManager.getNotificationChannel("MasjidnaChannelF") == null) {
                NotificationChannel channel = new NotificationChannel("MasjidnaChannelF", "channel_name", NotificationManager.IMPORTANCE_HIGH);
                channel.setDescription("channel_description");
                notificationManager.createNotificationChannel(channel);
            }
        }
    }

    /*public void setAlarm(long timeInS, Boolean isAdhan){
        timeInS -= 3600; // timezone -1 hour
        try {
            SharedPreferences sharedPref = getSharedPreferences(getString(R.string.masjidna_shared_pref), Context.MODE_PRIVATE);
            if (sharedPref.getLong("nextAlarm", 60) < (timeInS - 60) ||
                    sharedPref.getLong("nextAlarm", 60) > (timeInS + 60)) {
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putLong("nextAlarm", timeInS);
                editor.apply();
                Intent intent = new Intent(this, MyBroadcastReceiver.class);
                intent.putExtra("isAdhan", isAdhan);
                PendingIntent pendingIntent = PendingIntent.getBroadcast(
                        this.getApplicationContext(), 234324243, intent, 0);
                AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
                //timeInS - 3600 ( one hour, problem of timezone)

                alarmManager.setExact(AlarmManager.RTC_WAKEUP, timeInS * 1000, pendingIntent);
                Date date = new Date();
                date.setTime(timeInS * 1000);
                ReportToFirebase("Alarm in NotifierAct 2", "Adhan: " + isAdhan + " " + date.toString());
            } else {
                //do nothing
                Log.e("Alarm", "Already set alarm");
                Date date = new Date();
                date.setTime(sharedPref.getLong("nextAlarm", 60)* 1000);
                ReportToFirebase("Alarm Already set/NotifAct",date.toString());
            }
        } catch (Exception e){
            ReportToFirebase("Alarm in NotifAct",e.getMessage());
        }
    }
    */

}