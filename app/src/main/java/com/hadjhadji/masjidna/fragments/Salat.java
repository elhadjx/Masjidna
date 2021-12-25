package com.hadjhadji.masjidna.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.hadjhadji.masjidna.MainActivity;
import com.hadjhadji.masjidna.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class Salat extends Fragment {
    TextView fajr_tv;
    TextView dohr_tv;
    TextView asr_tv;
    TextView maghreb_tv;
    TextView isha_tv;
    TextView timenow_tv;
    TextView timedate_tv;
    TextView hijridate_tv;
    Button btn_previousDay, btn_nextDay;
    String currentDate;
    SharedPreferences sharedPref;

    public static boolean gotDate;
    public static Calendar c;
    public Salat() {
        // Required empty public constructor
    }


    public static Salat newInstance(String param1, String param2) {
        Salat fragment = new Salat();
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
        View view =  inflater.inflate(R.layout.fragment_salat, container, false);

        fajr_tv = view.findViewById(R.id.fajr_tv);
        dohr_tv = view.findViewById(R.id.dohr_tv);
        asr_tv = view.findViewById(R.id.asr_tv);
        maghreb_tv = view.findViewById(R.id.maghreb_tv);
        isha_tv = view.findViewById(R.id.isha_tv);
        timenow_tv = view.findViewById(R.id.timenow_TV);
        timedate_tv = view.findViewById(R.id.timedate_TV);
        hijridate_tv = view.findViewById(R.id.hijridate_TV);
        btn_nextDay = view.findViewById(R.id.btn_nextDay);
        btn_previousDay = view.findViewById(R.id.btn_previousDay);

        gotDate = false;
        currentDate = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(new Date());
        c = Calendar.getInstance();
        c.setTime(new Date());
        sharedPref = getActivity().getSharedPreferences(getString(R.string.masjidna_shared_pref),Context.MODE_PRIVATE);

        Thread thread = new Thread() {

            @Override
            public void run() {
                refresh();
                while (!this.isInterrupted()) {
                        try {
                        Thread.sleep(100);
                        getActivity().runOnUiThread(new Runnable() {
                        public void run() {
                            updateResources(getContext(),"en");
                            timenow_tv.setText(
                                    new SimpleDateFormat("hh:mm:ss", Locale.getDefault()).format(new Date())
                            );
                            updateResources(getContext(),sharedPref.getString("salatLang","es"));
                        }
                        });
                    } catch (Exception e){
                        e.printStackTrace();
                    }
                }
            }
        };

        thread.start();

        refreshSalat();

        timedate_tv.setText(new SimpleDateFormat("EEEE dd MMMM yyyy", Locale.getDefault()).format(new Date()));


        refreshSalat();


        btn_nextDay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateResources(getContext(),"en");
                c.add(Calendar.DAY_OF_MONTH,1);
                currentDate = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(c.getTime());
                System.out.println(currentDate);
                updateResources(getContext(),sharedPref.getString("salatLang","es"));
                refresh();
            }
        });
        btn_previousDay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateResources(getContext(),"en");
                c.add(Calendar.DAY_OF_MONTH,-1);
                currentDate = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(c.getTime());
                System.out.println(currentDate);
                updateResources(getContext(),sharedPref.getString("salatLang","es"));
                refresh();
            }
        });

        return view;
    }

    private void refreshSalat() {
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

                fajr_tv.setText(fajrStr);
                dohr_tv.setText(dohrStr);
                asr_tv.setText(asrStr);
                maghreb_tv.setText(maghrebStr);
                isha_tv.setText(ishaStr);

                //set to shared prefs
                try {
                    SharedPreferences sharedPref = getActivity().getSharedPreferences(getString(R.string.masjidna_shared_pref),Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPref.edit();
                    editor.putString("fajr",fajrStr);
                    editor.putString("dohr",dohrStr);
                    editor.putString("asr",asrStr);
                    editor.putString("maghreb",maghrebStr);
                    editor.putString("isha",ishaStr);
                    editor.apply();
                } catch (Exception e){
                    e.printStackTrace();
                    ReportToFirebase(e.getMessage());
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public void refresh(){
        try {
            updateResources(getContext(),"en");
            currentDate = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(c.getTime());
            updateResources(getContext(),sharedPref.getString("salatLang","es"));
            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder()
                    .url("https://api.aladhan.com/v1/gToH?date=" + currentDate)
                    .build();
            Response response = null;
            String e = "";
            response = client.newCall(request).execute();
            JSONObject jo = new JSONObject(response.body().string());
            String hijriLang = "ar";
            if (!sharedPref.getString("salatLang","null").matches("ar")){
                hijriLang = "en";
            }
            e = ((JSONObject) ((JSONObject) (((JSONObject) jo.get("data")).get("hijri"))).get("weekday")).getString(hijriLang).concat(" " +
                    ((JSONObject) ((JSONObject) jo.get("data")).get("hijri")).getString("day")).concat(" " +
                    ((JSONObject) ((JSONObject) (((JSONObject) jo.get("data")).get("hijri"))).get("month")).getString(hijriLang)).concat(" " +
                    ((JSONObject) ((JSONObject) jo.get("data")).get("hijri")).getString("year"));
            String finalE = e;
            System.out.println(e);
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    hijridate_tv.setText(finalE);
                    updateResources(getContext(),"en");
                    timedate_tv.setText(new SimpleDateFormat("EEEE dd MMMM yyyy", Locale.getDefault()).format(c.getTime()));
                    updateResources(getContext(),sharedPref.getString("salatLang","es"));
                }
            });
            refreshSalat();
        } catch (Exception ee) {
            ee.printStackTrace();
        }
    }
    private void updateResources(Context context, String language) {
        try {
            Locale locale = new Locale(language);
            Locale.setDefault(locale);

            Resources resources = context.getResources();

            Configuration configuration = resources.getConfiguration();

            configuration.setLocale(locale);

            resources.updateConfiguration(configuration, resources.getDisplayMetrics());
        } catch (Exception e){
            e.printStackTrace();
        }
    }
    private static void ReportToFirebase(String message){
        FirebaseDatabase database = FirebaseDatabase.getInstance("https://masjidna-8e74b-default-rtdb.europe-west1.firebasedatabase.app");
        DatabaseReference report_ref = database.getReference("reports");
        report_ref.child(System.currentTimeMillis()+"").setValue(message);
    }
}