package com.example.jeffersonfernandes.tcc.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.jeffersonfernandes.tcc.modelo.Valores;

public class ValoresDAO extends SQLiteOpenHelper {

    private static final String NOME_BANCO = "DBMonitorFotovoltaico";
    private static final int VERSAO = 1;
    private static final String TABELA = "valoresTable";

    private static final String ID = "id";
    private static final String CORRENTE = "corrente";
    private static final String POTENCIA = "potencia";
    private static final String TENSAO = "tensao";
    private static final String TEMPERATURA = "temperatura";
    private static final String DATA = "data";
    private static final String HORA = "hora";

    public ValoresDAO(Context context){
        super(context, NOME_BANCO, null, VERSAO);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String sql = "CREATE TABLE " + TABELA + " ( " +
                " " + ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                " " + CORRENTE + " FLOAT, " + POTENCIA + " FLOAT, " +
                " " + TENSAO + " FLOAT, " + TEMPERATURA + " FLOAT, " +
                " " + DATA + " TEXT, " +
                " " + HORA + " TEXT);";
        db.execSQL(sql);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        /*String sql = "DROP TABLE IS EXISTS " + TABELA;
        db.execSQL(sql);
        onCreate(db);*/
    }

    public long salvarValores (Valores v){
        ContentValues values = new ContentValues();
        long retornoDB;

        values.put(CORRENTE, v.getCorrente());
        values.put(POTENCIA, v.getPotencia());
        values.put(TENSAO, v.getTensao());
        values.put(TEMPERATURA, v.getTemperatura());
        values.put(DATA, v.getData());
        values.put(HORA, v.getHora());

        retornoDB = getWritableDatabase().insert(TABELA, null, values);

        return retornoDB;
    }

    public void excluirValores (String c){
        long retornoDB;

        //String[] args = {String.valueOf(c.getId())};
        retornoDB = getWritableDatabase().delete(TABELA,ID+"="+c,null);

    }

    public String consultarId (){
        String[] coluna = {ID};

        Cursor cursor = getWritableDatabase().query(TABELA, coluna, null, null, null, null, null, null);

        StringBuffer buffer = new StringBuffer();
        while (cursor.moveToNext()){

            int id = cursor.getInt(0);
            buffer.append(id);
            buffer.append("\n");
        }

        return buffer.toString();
    }

    public String consultarCorrente () {

        String[] coluna = {ID, CORRENTE};

        Cursor cursor = getWritableDatabase().query(TABELA, coluna, null, null, null, null, null, null);

        StringBuffer buffer = new StringBuffer();
        while (cursor.moveToNext()){

            float valor = cursor.getFloat(1);
            buffer.append(valor);
            buffer.append("\n");
        }

        return buffer.toString();
    }

    public String consultarPotencia () {

        String[] coluna = {ID, POTENCIA};

        Cursor cursor = getWritableDatabase().query(TABELA, coluna, null, null, null, null, null, null);

        StringBuffer buffer = new StringBuffer();
        while (cursor.moveToNext()){

            float valor = cursor.getFloat(1);
            buffer.append(valor);
            buffer.append("\n");
        }

        return buffer.toString();
    }

    public String consultarTensao () {

        String[] coluna = {ID, TENSAO};

        Cursor cursor = getWritableDatabase().query(TABELA, coluna, null, null, null, null, null, null);

        StringBuffer buffer = new StringBuffer();
        while (cursor.moveToNext()){

            float valor = cursor.getFloat(1);
            buffer.append(valor);
            buffer.append("\n");
        }

        return buffer.toString();
    }

    public String consultarTemperatura() {

        String[] coluna = {ID, TEMPERATURA};

        Cursor cursor = getWritableDatabase().query(TABELA, coluna, null, null, null, null, null, null);

        StringBuffer buffer = new StringBuffer();
        while (cursor.moveToNext()){

            float valor = cursor.getFloat(1);
            buffer.append(valor);
            buffer.append("\n");
        }

        return buffer.toString();
    }

    public String consultarData () {

        String[] coluna = {ID, DATA};

        Cursor cursor = getWritableDatabase().query(TABELA, coluna, null, null, null, null, null, null);

        StringBuffer buffer = new StringBuffer();
        while (cursor.moveToNext()){

            String data = cursor.getString(1);
            buffer.append(data);
            buffer.append("\n");
        }

        return buffer.toString();
    }

    public String consultarHora() {

        String[] coluna = {ID, HORA};

        Cursor cursor = getWritableDatabase().query(TABELA, coluna, null, null, null, null, null, null);

        StringBuffer buffer = new StringBuffer();
        while (cursor.moveToNext()){

            String hora = cursor.getString(1);
            buffer.append(hora);
            buffer.append("\n");
        }

        return buffer.toString();
    }

}
