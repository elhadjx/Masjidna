package com.hadjhadji.masjidna.services;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;
import android.media.MediaPlayer;
import android.util.Log;

import com.hadjhadji.masjidna.MainActivity;
import com.hadjhadji.masjidna.R;

public class AudioAdhan extends IntentService {

    // TODO: Rename actions, choose action names that describe tasks that this
    // IntentService can perform, e.g. ACTION_FETCH_NEW_ITEMS
    private static final String ACTION_FOO = "com.hadjhadji.masjidna.services.action.FOO";
    private static final String ACTION_BAZ = "com.hadjhadji.masjidna.services.action.BAZ";

    // TODO: Rename parameters
    private static final String EXTRA_PARAM1 = "com.hadjhadji.masjidna.services.extra.PARAM1";
    private static final String EXTRA_PARAM2 = "com.hadjhadji.masjidna.services.extra.PARAM2";

    public AudioAdhan() {
        super("AudioAdhan");
    }

    // TODO: Customize helper method
    public static void startActionFoo(Context context, String param1, String param2) {
        Intent intent = new Intent(context, AudioAdhan.class);
        intent.setAction(ACTION_FOO);
        intent.putExtra(EXTRA_PARAM1, param1);
        intent.putExtra(EXTRA_PARAM2, param2);
        context.startService(intent);
    }

    // TODO: Customize helper method
    public static void startActionBaz(Context context, String param1, String param2) {
        Intent intent = new Intent(context, AudioAdhan.class);
        intent.setAction(ACTION_BAZ);
        intent.putExtra(EXTRA_PARAM1, param1);
        intent.putExtra(EXTRA_PARAM2, param2);
        context.startService(intent);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        MainActivity.ReportToFirebase("ClassMonitor","AudioAdhan/OnHandleIntent Started");

        playAdhan(getApplicationContext());
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

    private void handleActionFoo(String param1, String param2) {
        // TODO: Handle action Foo
        throw new UnsupportedOperationException("Not yet implemented");
    }

    private void handleActionBaz(String param1, String param2) {
        // TODO: Handle action Baz
        throw new UnsupportedOperationException("Not yet implemented");
    }
}