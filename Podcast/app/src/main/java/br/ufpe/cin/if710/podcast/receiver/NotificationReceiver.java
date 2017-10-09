package br.ufpe.cin.if710.podcast.receiver;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;

import br.ufpe.cin.if710.podcast.ui.MainActivity;

/**
 * Created by Leonardo on 09/10/2017.
 */

public class NotificationReceiver extends BroadcastReceiver {

    private final CharSequence contentTitle = "Podcast Baixado";
    private final CharSequence contentText = "";
    private Intent mNotificationIntent;
    private PendingIntent mContentIntent;


    @Override
    public void onReceive(Context context, Intent intent) {

        NotificationManager mNotifyManager;
        NotificationCompat.Builder mBuilder;

        //Ação da notificação
        mNotificationIntent = new Intent(context.getApplicationContext(), MainActivity.class);
        mContentIntent = PendingIntent.getActivity(context.getApplicationContext(), 0, mNotificationIntent, 0);

        //Cria a notificação
        mBuilder = new NotificationCompat.Builder(context.getApplicationContext());
        mBuilder.setTicker("Podcast novo!")
                .setSmallIcon(android.R.drawable.stat_sys_warning)
                .setAutoCancel(true)
                .setContentTitle(contentTitle)
                .setContentText(contentText)
                .setContentIntent(mContentIntent)
                .setPriority(NotificationCompat.PRIORITY_HIGH);

        // passa notificacao para o notification manager
        mNotifyManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        mNotifyManager.notify(01,mBuilder.build());
    }
}
