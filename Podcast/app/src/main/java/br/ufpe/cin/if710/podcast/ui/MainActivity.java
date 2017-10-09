package br.ufpe.cin.if710.podcast.ui;

import android.Manifest;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.Toast;

import org.xmlpull.v1.XmlPullParserException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import br.ufpe.cin.if710.podcast.R;
import br.ufpe.cin.if710.podcast.aplication.MyApplication;
import br.ufpe.cin.if710.podcast.db.PodcastDBHelper;
import br.ufpe.cin.if710.podcast.db.PodcastProviderContract;
import br.ufpe.cin.if710.podcast.domain.ItemFeed;
import br.ufpe.cin.if710.podcast.domain.XmlFeedParser;
import br.ufpe.cin.if710.podcast.service.DownloadService;
import br.ufpe.cin.if710.podcast.service.MusicPlayer;
import br.ufpe.cin.if710.podcast.ui.adapter.XmlFeedAdapter;

public class MainActivity extends Activity {

    //ao fazer envio da resolucao, use este link no seu codigo!
    private final String RSS_FEED = "http://leopoldomt.com/if710/fronteirasdaciencia.xml";
    //TODO teste com outros links de podcast
    PodcastDBHelper db;
    AsyncTask t = null;
    private ListView itens;


    private static String[] PERMISSIONS = {
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        db = PodcastDBHelper.getInstance(this);
        itens = (ListView) findViewById(R.id.items);
        checkPermissions(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            startActivity(new Intent(this,SettingsActivity.class));
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStart() {
        super.onStart();
        new DownloadXmlTask().execute(RSS_FEED);

        new startServiceMusicTask().execute();
    }

    @Override
    protected void onStop() {
        super.onStop();
        XmlFeedAdapter adapter = (XmlFeedAdapter) itens.getAdapter();
        adapter.clear();  //muda o estado da myaplication
    }

    protected void onResume() {
        super.onResume();
        IntentFilter f = new IntentFilter(DownloadService.DOWNLOAD_COMPLETE);
        LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(onDownloadCompleteEvent, f);
        MyApplication.activityResumed(); //muda o estado da myaplication
    }

    @Override
    protected void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(getApplicationContext()).unregisterReceiver(onDownloadCompleteEvent);
        MyApplication.activityPaused();
    }

    private ServiceConnection sConn = new ServiceConnection(){
        @Override
        public void onServiceDisconnected(ComponentName name) {
            MyApplication.setMusicPlayer(null);
            MyApplication.setBound(false);
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {

            MyApplication.setMusicPlayer(((MusicPlayer.MusicBinder) service).getService());
            MyApplication.setBound(true);
        }
    };

    private BroadcastReceiver onDownloadCompleteEvent=new BroadcastReceiver() {
        public void onReceive(Context context, Intent i) {

            Toast.makeText(context, "Download Concluido", Toast.LENGTH_LONG).show();

            List<ItemFeed> itemList = getDados();

            XmlFeedAdapter adapter = new XmlFeedAdapter(getApplicationContext(), R.layout.itemlista, itemList);
            System.out.println("Primeiro plano");
                //atualizar o list view
            itens.setAdapter(adapter);
            itens.deferNotifyDataSetChanged();
        }
    };


    public static void checkPermissions(Activity activity) {

        int permissao = ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (permissao != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity,PERMISSIONS, 1);
        }
    }

    private class DownloadXmlTask extends AsyncTask<String, Void, List<ItemFeed>> {
        @Override
        protected void onPreExecute() {
            Toast.makeText(getApplicationContext(), "iniciando...", Toast.LENGTH_SHORT).show();
        }

        @Override
        protected List<ItemFeed> doInBackground(String... params) {
            List<ItemFeed> itemList = new ArrayList<>();

            ConnectivityManager cm =  (ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);

            NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
            boolean isConnected = activeNetwork != null &&
                    activeNetwork.isConnectedOrConnecting();

            //VERIFICA SE TEM INTERNET
            if( isConnected){
                try {
                   itemList = XmlFeedParser.parse(getRssFeed(params[0]));
                   persistirDados(itemList);
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (XmlPullParserException e) {
                    e.printStackTrace();
                }

            }
            //BUSCA OS DADOS PERSISTIDOS
            itemList = getDados();

            return itemList;
        }



        protected   void persistirDados (List<ItemFeed> itemList){
            //BUSCA OS DADOS JA CADASTRADOS
            Cursor result = db.getReadableDatabase()
                    .query(PodcastDBHelper.DATABASE_TABLE,
                            PodcastDBHelper.columns,
                            null,
                            null,
                            null,
                            null,
                            null);

            List<ItemFeed> itensSalvos = new ArrayList<>();
            if(result != null){
                itensSalvos   = cursorToList(result);
            }

            //ADICIONA SOMENTE ITENS QUE NÃO FORAM PREVIAMENTE CADASTRADOS
            for (ItemFeed item : itemList) {

                if(!itensSalvos.contains(item)){
                    itensSalvos.add(item);

                    ContentValues cv = new ContentValues();

                    cv.put(PodcastDBHelper.EPISODE_TITLE, item.getTitle());
                    cv.put(PodcastDBHelper.EPISODE_LINK, item.getLink());
                    cv.put(PodcastDBHelper.EPISODE_DATE, item.getPubDate());
                    cv.put(PodcastDBHelper.EPISODE_DESC, item.getDescription());
                    cv.put(PodcastDBHelper.EPISODE_DOWNLOAD_LINK, item.getDownloadLink());
                    cv.put(PodcastDBHelper.EPISODE_FILE_URI,"");

                    getContentResolver().insert(PodcastProviderContract.EPISODE_LIST_URI,cv);
                }
            }
        }


        @Override
        protected void onPostExecute(List<ItemFeed> feed) {
            Toast.makeText(getApplicationContext(), "terminando...", Toast.LENGTH_SHORT).show();

            //Adapter Personalizado
            XmlFeedAdapter adapter = new XmlFeedAdapter(getApplicationContext(), R.layout.itemlista, feed);

            //atualizar o list view
            itens.setAdapter(adapter);
            itens.setTextFilterEnabled(true);
            t = new LoadCursorTask().execute();

        }
    }



    private class startServiceMusicTask extends AsyncTask<String, Void, Boolean> {


        @Override
        protected Boolean doInBackground(String... strings) {

            if (!MyApplication.isBound()) {

                Intent bindIntent = new Intent(getApplicationContext(),MusicPlayer.class);
                MyApplication.setBound(bindService(bindIntent, sConn, Context.BIND_AUTO_CREATE));
            }

            return null;
        }
    }

    protected  List<ItemFeed> getDados(){
        Cursor cursor = db.getReadableDatabase()
                .query(PodcastDBHelper.DATABASE_TABLE,
                        PodcastDBHelper.columns,
                        null,
                        null,
                        null,
                        null,
                        null);
        List<ItemFeed> itens =  cursorToList(cursor);

        return itens;

    }


    private  List<ItemFeed> cursorToList(Cursor cursor){

        List<ItemFeed> itens = new ArrayList<>();
        ItemFeed itemFeed;
        cursor.moveToFirst();

        //Navega no cursor, criando objetos do tipo itemFeed
        while (!cursor.isAfterLast()){
            itemFeed= new ItemFeed (cursor.getString(cursor.getColumnIndex(PodcastProviderContract.EPISODE_TITLE)),
                    cursor.getString(cursor.getColumnIndex(PodcastProviderContract.EPISODE_LINK)),
                    cursor.getString(cursor.getColumnIndex(PodcastProviderContract.EPISODE_DATE)),
                    cursor.getString(cursor.getColumnIndex(PodcastProviderContract.EPISODE_DESC)),
                    cursor.getString(cursor.getColumnIndex(PodcastProviderContract.EPISODE_DOWNLOAD_LINK)),
                    cursor.getString(cursor.getColumnIndex(PodcastProviderContract.EPISODE_FILE_URI)));

           itens.add(itemFeed);
           cursor.moveToNext();

        };

        return itens;
    }


    private class LoadCursorTask extends BaseTask<Void> {
        @Override
        protected Cursor doInBackground(Void... params) {
            Cursor result=
                    db.getReadableDatabase()
                            .query(PodcastDBHelper.DATABASE_TABLE,
                                    PodcastDBHelper.columns,
                                    null,
                                    null,
                                    null,
                                    null,
                                    null);

            result.getCount();

            return result;
        }
    }






    private abstract class BaseTask<T> extends AsyncTask<T, Void, Cursor> {
        Cursor doQuery() {
            Cursor result=
                    db.getReadableDatabase()
                            .query(PodcastDBHelper.DATABASE_TABLE,
                                    PodcastDBHelper.columns,
                                    null,
                                    null,
                                    null,
                                    null,
                                    null);

            //força a query a ser executada
            //so executa quando fazemos algo que precisa do resultset
            result.getCount();

            System.out.println("CURSOR");
            System.out.println( result.getCount());

            return result;
        }

        @Override
        public void onPostExecute(Cursor result) {
        }
    }

    //TODO Opcional - pesquise outros meios de obter arquivos da internet
    private String getRssFeed(String feed) throws IOException {
        InputStream in = null;
        String rssFeed = "";
        try {
            URL url = new URL(feed);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            in = conn.getInputStream();
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            for (int count; (count = in.read(buffer)) != -1; ) {
                out.write(buffer, 0, count);
            }
            byte[] response = out.toByteArray();
            rssFeed = new String(response, "UTF-8");
        } finally {
            if (in != null) {
                in.close();
            }
        }
        return rssFeed;
    }
}