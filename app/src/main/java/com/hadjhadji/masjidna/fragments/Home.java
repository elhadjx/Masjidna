package com.hadjhadji.masjidna.fragments;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.media.Image;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.hadjhadji.masjidna.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class Home extends Fragment {
    TextView tv_amount;
    ImageView languageIV;
    String language;


    public Home() {
        // Required empty public constructor
    }

    public static Home newInstance() {
        Home fragment = new Home();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        //SharedPref
        SharedPreferences sharedPref = getActivity().getSharedPreferences(getString(R.string.masjidna_shared_pref),Context.MODE_PRIVATE);

        //Read from SharedPref
        language = sharedPref.getString("salatLang","en");
        updateResources(getContext(),language);
        languageIV = view.findViewById(R.id.languageIV);
        if (language.equals("ar")){
            languageIV.setImageResource(R.drawable.spain);
        } else {
            languageIV.setImageResource(R.drawable.arab);
        }
        languageIV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                language = sharedPref.getString("salatLang","en");

                if (language.equals("sp")){
                    languageIV.setImageResource(R.drawable.arab);
                    updateResources(getContext(),"ar");
                    SharedPreferences.Editor editor = sharedPref.edit();
                    editor.putString("salatLang", "ar");
                    editor.apply();

                } else {
                    languageIV.setImageResource(R.drawable.spain);
                    updateResources(getContext(),"sp");
                    SharedPreferences.Editor editor = sharedPref.edit();
                    editor.putString("salatLang", "sp");
                    editor.apply();
                }
                refreshActivity();

            }
        });

        FirebaseDatabase database = FirebaseDatabase.getInstance("https://masjidna-8e74b-default-rtdb.europe-west1.firebasedatabase.app");


        DatabaseReference last_jummah = database.getReference("Jummuas").child("last");


        last_jummah.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                //tv_amount.setText("€"+snapshot.getValue().toString());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        return view;
    }
    private static void updateResources(Context context, String language) {
        Locale locale = new Locale(language);
        Locale.setDefault(locale);

        Resources resources = context.getResources();

        Configuration configuration = resources.getConfiguration();

        configuration.setLocale(locale);

        resources.updateConfiguration(configuration, resources.getDisplayMetrics());
    }
    private void refreshActivity(){
        getActivity().finish();
        startActivity(getActivity().getIntent());
    }
}