package br.ufpe.cin.if710.podcast.aplication;

import android.app.Application;

import br.ufpe.cin.if710.podcast.service.MusicPlayer;

/**
 * Created by Leonardo on 09/10/2017.
 */

public class MyApplication extends Application {

    //Monitora se a aplicação está em primeiro Plano

    private static boolean isInForeground;
    private static  boolean bound;
    private static MusicPlayer musicPlayer;

    public static boolean isActivityVisible() {
        return isInForeground;
    }

    public static void activityResumed() {
        isInForeground = true;
    }

    public static void activityPaused() {
        isInForeground = false;
    }

    public static void setBound(boolean x) {
        bound = x;
    }

    public static MusicPlayer getMusicPlayer() {return  musicPlayer;}

    public static void setMusicPlayer(MusicPlayer m) { musicPlayer = m;}

    public static void activityStop() {
        bound = false;
    }

    public static boolean isBound() {return bound;}

}
