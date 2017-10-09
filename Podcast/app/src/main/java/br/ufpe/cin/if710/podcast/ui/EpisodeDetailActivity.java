package br.ufpe.cin.if710.podcast.ui;

import android.app.Activity;
import android.icu.text.SimpleDateFormat;
import android.os.Bundle;
import android.widget.TextView;

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
        this.textPubDate.setText(this.getIntent().getExtras().getString(XmlFeedAdapter.PUBDATE));
        this.textDescription.setText(this.getIntent().getExtras().getString(XmlFeedAdapter.DESCRIPTION));
    }
}
