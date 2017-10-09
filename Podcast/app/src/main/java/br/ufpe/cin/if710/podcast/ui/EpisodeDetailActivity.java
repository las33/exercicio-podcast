package br.ufpe.cin.if710.podcast.ui;

import android.app.Activity;
import android.icu.text.SimpleDateFormat;
import android.os.Bundle;
import android.widget.TextView;

import java.text.ParseException;
import java.util.Date;
import java.util.Locale;

import br.ufpe.cin.if710.podcast.R;
import br.ufpe.cin.if710.podcast.ui.adapter.XmlFeedAdapter;

public class EpisodeDetailActivity extends Activity {


    private TextView textTitle;
    private TextView textPubDate;
    private TextView textDescription;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_episode_detail);

        //TODO preencher com informações do episódio clicado na lista...

        this.textTitle = findViewById(R.id.titleED);
        this.textPubDate = findViewById(R.id.pubDateED);
        this.textDescription = findViewById(R.id.descriptionED);


        this.textTitle.setText(this.getIntent().getExtras().getString(XmlFeedAdapter.TITLE));
        this.textDescription.setText(this.getIntent().getExtras().getString(XmlFeedAdapter.DESCRIPTION));

        //Data formatada
        try {
            SimpleDateFormat format = new SimpleDateFormat("EEE, dd MMM yyyy hh:mm:ss Z");
            Date newDate = format.parse(this.getIntent().getExtras().getString(XmlFeedAdapter.PUBDATE));

            format = new SimpleDateFormat("dd/MMM/yyyy");
            String date = format.format(newDate);
            this.textPubDate.setText(date);
        }catch (ParseException e){
            System.out.println("Erro de parse na data");
        }
    }
}
