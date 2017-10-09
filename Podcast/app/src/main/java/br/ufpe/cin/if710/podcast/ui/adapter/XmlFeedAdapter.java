package br.ufpe.cin.if710.podcast.ui.adapter;

import java.text.ParseException;
import java.util.Date;
import java.util.List;
import android.content.Context;
import android.content.Intent;
import android.icu.text.SimpleDateFormat;
import android.net.Uri;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

import br.ufpe.cin.if710.podcast.R;
import br.ufpe.cin.if710.podcast.aplication.MyApplication;
import br.ufpe.cin.if710.podcast.domain.ItemFeed;
import br.ufpe.cin.if710.podcast.service.DownloadService;
import br.ufpe.cin.if710.podcast.service.MusicPlayer;
import br.ufpe.cin.if710.podcast.ui.EpisodeDetailActivity;

public class XmlFeedAdapter extends ArrayAdapter<ItemFeed> {

    int linkResource;

    public static final String TITLE = "title";
    public static final String LINK = "link";
    public static final String PUBDATE = "pubDate";
    public static final String DESCRIPTION = "description";
    public static final String DOWNLOAD_LINK = "downloadLink";


    public XmlFeedAdapter(Context context, int resource, List<ItemFeed> objects) {
        super(context, resource, objects);
        linkResource = resource;
    }

    /**
     * public abstract View getView (int position, View convertView, ViewGroup parent)
     * <p>
     * Added in API level 1
     * Get a View that displays the data at the specified position in the data set. You can either create a View manually or inflate it from an XML layout file. When the View is inflated, the parent View (GridView, ListView...) will apply default layout parameters unless you use inflate(int, android.view.ViewGroup, boolean) to specify a root view and to prevent attachment to the root.
     * <p>
     * Parameters
     * position	The position of the item within the adapter's data set of the item whose view we want.
     * convertView	The old view to reuse, if possible. Note: You should check that this view is non-null and of an appropriate type before using. If it is not possible to convert this view to display the correct data, this method can create a new view. Heterogeneous lists can specify their number of view types, so that this View is always of the right type (see getViewTypeCount() and getItemViewType(int)).
     * parent	The parent that this view will eventually be attached to
     * Returns
     * A View corresponding to the data at the specified position.
     */


	/*
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View rowView = inflater.inflate(R.layout.itemlista, parent, false);
		TextView textView = (TextView) rowView.findViewById(R.id.item_title);
		textView.setText(items.get(position).getTitle());
	    return rowView;
	}
	/**/

    //http://developer.android.com/training/improving-layouts/smooth-scrolling.html#ViewHolder
    static class ViewHolder {
        TextView item_title;
        TextView item_date;
        Button btn_Download;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final ViewHolder holder;
        if (convertView == null) {
            convertView = View.inflate(getContext(), linkResource, null);
            holder = new ViewHolder();
            holder.item_title = (TextView) convertView.findViewById(R.id.item_title);
            holder.item_date = (TextView) convertView.findViewById(R.id.item_date);
            holder.btn_Download = (Button) convertView.findViewById(R.id.item_action);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        //final pq é acessado dentro do ONCLICK
        final ItemFeed item = getItem(position);

        holder.item_title.setText(item.getTitle());


        try {


            SimpleDateFormat format = new SimpleDateFormat("EEE, dd MMM yyyy hh:mm:ss Z");
            Date newDate = format.parse(item.getPubDate());

            format = new SimpleDateFormat("dd/MMM/yyyy");
            String date = format.format(newDate);
            holder.item_date.setText(date);
        }catch (ParseException e){
            System.out.println("");
        }



        if(!item.getFileUri().isEmpty()){
            holder.btn_Download.setText("play");
        }


        holder.item_title.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //System.out.println("Click");
                Context context = getContext();
                Intent episodeDetailIntent = new Intent(context, EpisodeDetailActivity.class);

                episodeDetailIntent.putExtra(TITLE, item.getTitle());
                episodeDetailIntent.putExtra(LINK, item.getLink());
                episodeDetailIntent.putExtra(PUBDATE, item.getPubDate());
                episodeDetailIntent.putExtra(DESCRIPTION, item.getDescription());
                episodeDetailIntent.putExtra(DOWNLOAD_LINK, item.getDownloadLink());

                // chamando trasição de tela
                context.startActivity(episodeDetailIntent);
            }
        });


        holder.btn_Download.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                System.out.println("P1");
                Context context = getContext();
                Intent musicService = new Intent(context, MusicPlayer.class);
                //holder.btn_Download.setEnabled(false);
                if(holder.btn_Download.getText().equals("baixar")){

                    Intent downloadService = new Intent(context, DownloadService.class);

                    downloadService.setData(Uri.parse(item.getDownloadLink()));

                    context.startService(downloadService);
                    System.out.println("Baixar");
                    holder.btn_Download.setText("baixando");


                }else if(holder.btn_Download.getText().equals("play")){


                    if (MyApplication.isBound()) {
                        MyApplication.getMusicPlayer().playMusic(item.getFileUri());
                    }

                    System.out.println("escuta1r");
                    holder.btn_Download.setText("pause");

                } else  if(holder.btn_Download.getText().toString().equals("pause")){

                    if (MyApplication.isBound()) {
                        MyApplication.getMusicPlayer().pauseMusic();
                    }

                    holder.btn_Download.setText("unPause");

                }else if(holder.btn_Download.getText().equals("unPause")){


                    if (MyApplication.isBound()) {
                        MyApplication.getMusicPlayer().continueMusic();
                    }

                    System.out.println("escuta1r");
                    holder.btn_Download.setText("pause");

                }



            }
        });

        return convertView;
    }
}