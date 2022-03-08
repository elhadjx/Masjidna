package com.hadjhadji.masjidna;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.RingtoneManager;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.view.MenuItem;
import android.view.WindowManager;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.hadjhadji.masjidna.BroadcastR.MyBroadcastReceiver;
import com.hadjhadji.masjidna.BroadcastR.MyReceiver;
import com.hadjhadji.masjidna.fragments.Home;
import com.hadjhadji.masjidna.fragments.Notifications;
import com.hadjhadji.masjidna.fragments.Salat;
import com.hadjhadji.masjidna.services.NotifierService;

import java.util.Date;

public class MainActivity extends AppCompatActivity {
    public static BottomNavigationView bottomNavigationView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();

        StrictMode.setThreadPolicy(policy);
        startService(new Intent(getApplication(), NotifierService.class));
        ReportToFirebase("ClassMonitor","MainActivity Started");
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

        //set reapeating alarm
        try {
            Intent sintent = new Intent(this, MyReceiver.class);
            PendingIntent sender = PendingIntent.getBroadcast(this, 2, sintent, 0);
            AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);
            long l = ((new Date().getTime())/60000)*60000;
            if (l < new Date().getTime()) {
                l += 3600000; // start at next minute
            }
            am.setRepeating(AlarmManager.RTC_WAKEUP, l, 3600000, sender);
        } catch (Exception e){
            e.printStackTrace();
        }



        //registering the broadcast receiver
        /*MyBroadcastReceiver myBroadcastReceiver = new MyBroadcastReceiver();
        registerReceiver(myBroadcastReceiver, new IntentFilter(Intent.ACTION_TIME_TICK));*/

        bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.salat_mi);

        Fragment salat = new Salat();
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, salat);
        transaction.addToBackStack(null);
        transaction.commit();

        Fragment home = new Home();
        Fragment notifications = new Notifications();
        bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @SuppressLint("ResourceAsColor")
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.salat_mi:
                        item.getIcon().setTint(R.color.secondary);
                        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, salat).commit();
                        return true;

                    case R.id.home_mi:
                        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, home).commit();
                        return true;

                    case R.id.notifications_mi:
                        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, notifications).commit();
                        return true;
                }
                return false;
            }
        });
        try {
            if (getIntent().getExtras().getString("Fragment").equals("NotificationFragment")){
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, notifications).commit();
            }
        } catch (Exception e){
            e.printStackTrace();
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
            CharSequence name = "MasjidnaChannel" ;
            String description = "Masjidna Channel Description";
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