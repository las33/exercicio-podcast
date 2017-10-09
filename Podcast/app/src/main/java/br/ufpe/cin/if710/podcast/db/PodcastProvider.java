package br.ufpe.cin.if710.podcast.db;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;

import br.ufpe.cin.if710.podcast.domain.ItemFeed;

public class PodcastProvider extends ContentProvider {

    PodcastDBHelper db;

    public PodcastProvider() {
    }

    private boolean isItemFeedUri(Uri uri) {
        return uri.getLastPathSegment().equals(PodcastProviderContract.EPISODE_TABLE);
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        if (isItemFeedUri(uri)) {
            return db.getWritableDatabase().delete(PodcastDBHelper.DATABASE_TABLE,selection,selectionArgs);
        }
        else return 0;
    }

    @Override
    public String getType(Uri uri) {
        // TODO: Implement this to handle requests for the MIME type of the data
        // at the given URI.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        if (isItemFeedUri(uri)) { //Antes de inserir testa a uri da tabela
            long id = db.getWritableDatabase().replace(PodcastDBHelper.DATABASE_TABLE,null,values);
            return Uri.withAppendedPath(PodcastProviderContract.EPISODE_LIST_URI, Long.toString(id));
        }
        else return null;
    }

    @Override
    public boolean onCreate() {
        db = PodcastDBHelper.getInstance(getContext());
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {
        Cursor cursor = null;

        if (isItemFeedUri(uri)) { //Antes de pesquisar testa a uri da tabela
            cursor = db.getReadableDatabase().query(PodcastDBHelper.DATABASE_TABLE,projection, selection, selectionArgs,null,null,sortOrder);
        }
        else {
            throw new UnsupportedOperationException("Not yet implemented");
        }

        return cursor;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {
        if (isItemFeedUri(uri)) { //Antes de dar update testa a uri da tabela
            return db.getWritableDatabase().update(PodcastDBHelper.DATABASE_TABLE, values, selection, selectionArgs);
        }
        else return 0;
    }
}
