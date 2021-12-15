package com.example.ogima.helper;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.example.ogima.model.Informacoes;

import java.sql.Date;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;

public class InfoUserDAO implements IUserInfoDao{

    private SQLiteDatabase escreve;
    private SQLiteDatabase le;

    public InfoUserDAO(Context context) {
        DbHelper db = new DbHelper(context);
        escreve = db.getWritableDatabase();
        le = db.getReadableDatabase();
    }

    @Override
    public boolean salvar(Informacoes informacoes) {

        ContentValues cv = new ContentValues();
        cv.put("contadorAlteracao", informacoes.getContadorAlteracao());
        cv.put("dataAtual", informacoes.getDataAtual());
        cv.put("dataSalva", informacoes.getDataSalva());

        try {
            escreve.insert(DbHelper.TABLE_NAME, null, cv);
            Log.i("INFO DB", "Salvo com sucesso " + cv.get("contadorAlteracao"));
        }catch (Exception e){
            Log.i("INFO DB", "Erro ao salvar tarefa " + e.getStackTrace());
            return false;
        }

        return true;
    }

    @Override
    public boolean atualizar(Informacoes informacoes) {

        ContentValues cv = new ContentValues();
        cv.put("contadorAlteracao", informacoes.getContadorAlteracao());
        cv.put("dataAtual", informacoes.getDataAtual());
        cv.put("dataSalva", informacoes.getDataSalva());

        try {
            String[] args = {informacoes.getId().toString()};
            escreve.update(DbHelper.TABLE_NAME, cv, "id=?", args);
            Log.i("INFO DB", "Atualizado com sucesso");
        }catch (Exception e){
            Log.i("INFO DB", "Erro ao atualizar");
            return false;
        }

        return true;
    }

    @Override
    public boolean deletar(Informacoes informacoes) {
        return false;
    }

    @Override
    public boolean recuperar(Informacoes informacoes) {

        String sql = "SELECT * FROM " + DbHelper.TABLE_NAME + " ;";
        Cursor c = le.rawQuery(sql, null);

        while (c.moveToNext()){

            Long id = c.getLong(c.getColumnIndexOrThrow("id"));
            int contadorAlteracao = c.getInt(c.getColumnIndexOrThrow("contadorAlteracao"));
            String dataSalva = c.getString(c.getColumnIndexOrThrow("dataSalva"));
            String dataAtual = c.getString(c.getColumnIndexOrThrow("dataAtual"));

            informacoes.setId(id);
            informacoes.setContadorAlteracao(contadorAlteracao);
            informacoes.setDataAtual(dataAtual);
            informacoes.setDataSalva(dataSalva);

            //Log.i("INFO DB", "MeuId " + informacoes.getId());
            //Log.i("INFO DB", "MeuContador " + informacoes.getContadorAlteracao());

        }

        return true;
    }

    @Override
    public List<Informacoes> listar() {
        return null;
    }
}
