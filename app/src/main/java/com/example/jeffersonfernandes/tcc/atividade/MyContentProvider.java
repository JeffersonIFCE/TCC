package com.example.jeffersonfernandes.tcc.atividade;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.HashMap;

public class MyContentProvider extends ContentProvider {
    static final String PROVIDER_NAME = "com.example.jeffersonfernandes.tcc.atividade.MyContentProvider";
    static Uri CONTENT_URI = Uri.parse("content://" + PROVIDER_NAME + "/info");

    static final String ID = "id";
    static final String CORRENTE = "corrente";
    static final String POTENCIA = "potencia";
    static final String TENSAO = "tensao";
    static final String TEMPERATURA = "temperatura";
    static final String DATA = "data";
    static final String HORA = "hora";

    private HashMap<String, String> PROJECTION_INFO_MAP;

    static final int INFO = 1;
    static final int INFO_ID = 2;

    static UriMatcher uriMatcher;
    static {
        uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI(PROVIDER_NAME, "info", INFO);
        uriMatcher.addURI(PROVIDER_NAME, "info/#", INFO_ID);
    }

    private SQLiteDatabase db;
    static final String DATABASE_NAME = "MFotovoltaico";
    static final String TABLE_NAME = "dadosmonitor";
    static final int VERSION = 1;
    static final String CREATE_TABLE =
            "CREATE TABLE " + TABLE_NAME +
                    "(id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "corrente FLOAT NOT NULL, " +
                    "potencia FLOAT NOT NULL, " +
                    "temperatura FLOAT NOT NULL, " +
                    "tensao FLOAT NOT NULL, " +
                    "data TEXT NOT NULL," +
                    "hora TEXT NOT NULL)";

    private class DatabaseHelper extends SQLiteOpenHelper {
        DatabaseHelper (Context context) {
            super(context, DATABASE_NAME, null, VERSION);
        }
        @Override
        public void onCreate(SQLiteDatabase sqLiteDatabase) {
            sqLiteDatabase.execSQL(CREATE_TABLE);
        }

        @Override
        public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
            sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
            onCreate(sqLiteDatabase);
        }

    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        // Implement this to handle requests to delete one or more rows.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public String getType(Uri uri) {
        // TODO: Implement this to handle requests for the MIME type of the data
        // at the given URI.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
         long rowID = db.insert(TABLE_NAME, null, values);

        if(rowID > 0) {
            Uri _uri = ContentUris.withAppendedId(CONTENT_URI, rowID);
            getContext().getContentResolver().notifyChange(_uri, null);
            return _uri;
        }

        throw new SQLException("Falha ao adicionar registro no content provider");
    }

    @Override
    public boolean onCreate() {
        Context context = getContext();
        DatabaseHelper dbHelper = new DatabaseHelper(context);

        db = dbHelper.getWritableDatabase();
        return (db == null) ? false : true;
    }

    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        SQLiteQueryBuilder sqLiteQueryBuilder = new SQLiteQueryBuilder();

        sqLiteQueryBuilder.setTables(TABLE_NAME);

        switch (uriMatcher.match(uri)) {
            case INFO:
                sqLiteQueryBuilder.setProjectionMap(PROJECTION_INFO_MAP);
                break;

            case INFO_ID:
                sqLiteQueryBuilder.appendWhere(ID + "=" + uri.getPathSegments().get(1));
                break;

            default:
        }

        if(sortOrder == null || sortOrder == "") {
            sortOrder = ID;
        }

        Cursor c = sqLiteQueryBuilder.query(db, projection, selection, selectionArgs,
                null,  null,sortOrder);
        c.setNotificationUri(getContext().getContentResolver(), uri);
        return c;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection,
            String[] selectionArgs) {
        // TODO: Implement this to handle requests to update one or more rows.
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
