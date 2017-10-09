package br.ufpe.cin.if710.podcast.service;

import android.app.IntentService;

import android.content.ContentValues;
import android.content.Intent;
import android.os.Environment;

import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import br.ufpe.cin.if710.podcast.aplication.MyApplication;
import br.ufpe.cin.if710.podcast.db.PodcastProviderContract;


/**
 * Created by Leonardo on 08/10/2017.
 */

public class DownloadService extends IntentService  {

    public static final String DOWNLOAD_COMPLETE = "br.ufpe.cin.if710.podcast.broadcast.DOWNLOAD_COMPLETE";


    public DownloadService() {
        super("DownloadService");
    }


    @Override
    protected void onHandleIntent(Intent intent) {
        //Classe de download do podcast
        try {

            File root = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            root.mkdirs();

            File output = new File(root, intent.getData().getLastPathSegment());
            if (!output.exists()) {
                output.createNewFile();
            }
            System.out.println(intent.getData());
            URL url = new URL(intent.getData().toString());
            HttpURLConnection c = (HttpURLConnection) url.openConnection();
            FileOutputStream fos = new FileOutputStream(output.getPath());
            BufferedOutputStream out = new BufferedOutputStream(fos);

            try {
                InputStream in = c.getInputStream();
                byte[] buffer = new byte[8192];
                int len = 0;
                while ((len = in.read(buffer)) >= 0) {
                    out.write(buffer, 0, len);
                }
                out.flush();
            }
            finally {
                fos.getFD().sync();
                out.close();
                c.disconnect();
            }

            //Salva uri no banco
            ContentValues content = new ContentValues();
            content.put(PodcastProviderContract.EPISODE_FILE_URI, output.getPath());

            String[] selectionArgs = {intent.getData().toString()};

            getContentResolver().update(PodcastProviderContract.EPISODE_LIST_URI,
                    content,
                    PodcastProviderContract.EPISODE_DOWNLOAD_LINK + "== ?",
                    selectionArgs);

            //Envia um broadcast local dependendo do estado da activity
            Intent intentDC = new Intent(DOWNLOAD_COMPLETE);
            if(MyApplication.isActivityVisible()){
                LocalBroadcastManager.getInstance(this).sendBroadcast(intentDC);
            }else{
                sendBroadcast(intentDC);
            }

        } catch (IOException e2) {
            System.out.println(e2.getMessage());
            Log.e(getClass().getName(), "Exception durante download" + e2.getStackTrace(), e2);
        }
    }

}

