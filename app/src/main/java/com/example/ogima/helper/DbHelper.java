package com.example.ogima.helper;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import androidx.annotation.Nullable;

public class DbHelper extends SQLiteOpenHelper {

    public static int VERSION = 1;
    public static String DB_NAME = "INFOS";
    public static String TABLE_NAME = "user";

    public DbHelper(@Nullable Context context) {
        super(context, DB_NAME, null, VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        String sqlUsuario = "CREATE TABLE IF NOT EXISTS " + TABLE_NAME
                + " (id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                " contadorAlteracao INT(2), dataSalva TEXT, dataAtual TEXT) ";

        try{
            db.execSQL(sqlUsuario);
            Log.i("INFO DB", "Sucesso ao criar a tabela");
        }catch (Exception e){
            Log.i("INFO DB","Erro ao criar a tabela " + e.getStackTrace());
        }

    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }
}
