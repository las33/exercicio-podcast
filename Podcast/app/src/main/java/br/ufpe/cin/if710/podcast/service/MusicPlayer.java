package br.ufpe.cin.if710.podcast.service;

import android.app.PendingIntent;
import android.app.Service;
import android.media.MediaPlayer;

/**
 * Created by Leonardo on 09/10/2017.
 */

import android.content.Intent;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;

import br.ufpe.cin.if710.podcast.ui.MainActivity;


public class MusicPlayer extends Service {

    private final String TAG = "MusicPlayerWithBindingService";
    private static final int NOTIFICATION_ID = 2;
    private MediaPlayer mPlayer;
    private int mStartID;

    @Override
    public void onCreate() {
        super.onCreate();





    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {



        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        if (null != mPlayer) {
            mPlayer.stop();
            mPlayer.release();
        }
    }

    public void playMusic(String url) {
        Uri uri =  Uri.parse(url);

        // configurar media player
        mPlayer = MediaPlayer.create(this, uri);

        if (null != mPlayer) {
            //fica em loop
            mPlayer.setLooping(true);
        }

        if (!mPlayer.isPlaying()) {
            mPlayer.start();
        }
    }

    public void continueMusic() {
        if (!mPlayer.isPlaying()) {
            mPlayer.start();
        }
    }

    public void pauseMusic() {
        if (mPlayer.isPlaying()) {
            mPlayer.pause();
        }
    }


    public final IBinder mBinder = new MusicBinder();

    public class MusicBinder extends Binder {
      public  MusicPlayer getService() {
            // retorna a instancia do Service, para que clientes chamem metodos publicos
            return MusicPlayer.this;
        }
    }
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

}
