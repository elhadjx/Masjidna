package com.hadjhadji.masjidna;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.media.RingtoneManager;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.hadjhadji.masjidna.fragments.Home;
import com.hadjhadji.masjidna.fragments.Notifications;
import com.hadjhadji.masjidna.fragments.Salat;
import com.hadjhadji.masjidna.services.NotifierService;

public class MainActivity extends AppCompatActivity {
    BottomNavigationView bottomNavigationView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();

        StrictMode.setThreadPolicy(policy);
        startService(new Intent(getApplication(), NotifierService.class));


        bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.home_mi);

        Fragment home = new Home();
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, home);
        transaction.addToBackStack(null);
        transaction.commit();

        Fragment salat = new Salat();
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

}