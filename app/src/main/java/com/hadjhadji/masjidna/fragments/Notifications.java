package com.hadjhadji.masjidna.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.hadjhadji.masjidna.R;
import com.hadjhadji.masjidna.models.Notification;

import java.sql.Time;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class Notifications extends Fragment {
    TextView notif_title;
    TextView notif_message;
    TextView notif_date;

    public Notifications() {
        // Required empty public constructor
    }

    public static Notifications newInstance(String param1, String param2) {
        Notifications fragment = new Notifications();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view =  inflater.inflate(R.layout.fragment_notifications, container, false);
        final Notification[] notification = new Notification[1];
        notif_title = view.findViewById(R.id.notif_titleTV);
        notif_message = view.findViewById(R.id.notif_messageTV);
        notif_date = view.findViewById(R.id.notif_dateTV);
        SharedPreferences sharedPref = getActivity().getSharedPreferences(getString(R.string.masjidna_shared_pref),Context.MODE_PRIVATE);
        String notif_titleSP = sharedPref.getString("notif_titleSP", "loading");
        String notif_messageSP = sharedPref.getString("notif_messageSP", "loading");
        String notif_dateSP = sharedPref.getString("notif_dateSP", "00");
        notif_title.setText(notif_titleSP);
        notif_message.setText(notif_messageSP);
        //Date notif_dateDT = new Date((new Time(Long.parseLong(notif_dateSP))).getTime());
        notif_date.setText("...");
        FirebaseDatabase database = FirebaseDatabase.getInstance("https://masjidna-8e74b-default-rtdb.europe-west1.firebasedatabase.app");
        DatabaseReference notif_ref = database.getReference("Notifications").child("Last");
        notif_ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                notification[0] = new Notification(
                        ""+snapshot.child("id").getValue(),
                        ""+snapshot.child("title").getValue(),
                        ""+snapshot.child("message").getValue(),
                        ""+snapshot.child("timestamp").getValue());
                notif_title.setText(notification[0].getTitle());
                notif_message.setText(notification[0].getMessage());
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putLong("lastNewsTimestamp", Long.parseLong(notification[0].getTimestamp()));
                editor.apply();
                Date notif_dateDT = new Date((new Time(Long.parseLong(notification[0].getTimestamp()))).getTime());
                Calendar c = Calendar.getInstance();
                c.setTime(notif_dateDT);

                notif_date.setText(new SimpleDateFormat("dd/MM HH:mm", Locale.getDefault()).format(c.getTime()));
                Log.e("key",snapshot.getKey());
                for(DataSnapshot postSnapShot:snapshot.getChildren()){
                    Log.e(postSnapShot.getKey(),""+postSnapShot.getValue());
                }
                Log.e("childs",""+snapshot.getChildrenCount());

                editor.putString("notif_titleSP", notification[0].getTitle());
                editor.putString("notif_messageSP", notification[0].getMessage());
                editor.putString("notif_dateSP", notification[0].getTimestamp());
                editor.apply();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


        return view;
    }
}