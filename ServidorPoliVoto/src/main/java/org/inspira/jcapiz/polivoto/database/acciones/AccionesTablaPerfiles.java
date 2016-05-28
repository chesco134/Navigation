package org.inspira.jcapiz.polivoto.database.acciones;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import org.inspira.jcapiz.polivoto.database.Votaciones;
import org.inspira.jcapiz.polivoto.pojo.Perfil;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by jcapiz on 14/04/16.
 */
public class AccionesTablaPerfiles {

    public static void actualizaNombreDePerfil(Context context, Perfil perfil){
        ContentValues values = new ContentValues();
        values.put("perfil", perfil.getPerfil());
        SQLiteDatabase db = new Votaciones(context).getWritableDatabase();
        db.update("Perfil", values, "idPerfil = CAST(? as INTEGER)", new String[]{String.valueOf(perfil.getId())});
        db.close();
    }

    public static int obtenerIdPerfil(Context context, String perfil){
        SQLiteDatabase db = new Votaciones(context).getReadableDatabase();
        Cursor c = db.rawQuery("select idPerfil from perfil where perfil like ?",
                new String[]{perfil});
        int idPerfil = c.moveToFirst() ? c.getInt(0) : -1;
        c.close();
        db.close();
        return idPerfil;
    }

    public static String obtenerPerfil(Context context, int idPerfil){
        SQLiteDatabase db = new Votaciones(context).getReadableDatabase();
        Cursor c;
        c = db.rawQuery("select perfil from Perfil where idPerfil = CAST(? as INTEGER)"
                , new String[]{String.valueOf(idPerfil)});
        String perfil = null;
        if(c.moveToFirst())
            perfil = c.getString(0);
        c.close();
        db.close();
        return perfil;
    }

    public static Perfil[] obtenerPerfilesVacios(Context context){
        SQLiteDatabase db = new Votaciones(context).getReadableDatabase();
        Cursor c = db.rawQuery("select Perfil.idPerfil, Perfil.perfil from " +
                        "Perfil " +
                        "left join " +
                        "Participante " +
                        "on Perfil.idPerfil = Participante.idPerfil " +
                        "where Participante.idPerfil is null and perfil not like 'NaN' group by Perfil.idPerfil",
                null);
        List<Perfil> perfilList = new ArrayList<>();
        Perfil perfil;
        while(c.moveToNext()){
            perfil = new Perfil(c.getInt(c.getColumnIndex("idPerfil")));
            perfil.setPerfil(c.getString(c.getColumnIndex("perfil")));
            perfilList.add(perfil);
            Log.d("Perfiles", "Perfil vacío: " + perfil.getPerfil());
        }
        Log.d("Pana", "Done loading perfiles vacíos.");
        c.close();
        db.close();
        return perfilList.toArray(new Perfil[]{});
    }

    public static void removerPerfil(Context context, String perfil){
        SQLiteDatabase db = new Votaciones(context).getWritableDatabase();
        db.delete("Perfil", "perfil like ?", new String[]{perfil});
        db.close();
    }

    public static String[] obtenerPerfilesDeVotacion(Context context, int idVotacion){
        SQLiteDatabase db = new Votaciones(context).getReadableDatabase();
        Cursor c = db.rawQuery("select perfil from Perfil join (select idPerfil from " +
                "Participante join (select Boleta from Participante_Pregunta join Pregunta where idVotacion " +
                "= CAST(? as INTEGER) group by Boleta) a using(Boleta) group by idPerfil) b using(idPerfil)",
                new String[]{String.valueOf(idVotacion)});
        List<String> idPerfiles = new ArrayList<>();
        while(c.moveToNext()){
            idPerfiles.add(c.getString(0));
        }
        c.close();
        db.close();
        return idPerfiles.toArray(new String[]{});
    }

    public static String obtenerPerfilParticipante(Context context, String boleta){
        SQLiteDatabase db;
        Cursor c;
        String perfil = "NaN";
        db = new Votaciones(context).getReadableDatabase();
        c = db.rawQuery("select Perfil from Perfil join Participante using(idPerfil) where " +
                "Boleta like ?", new String[]{boleta});
        if(c.moveToFirst()) {
            perfil = c.getString(0);
        }
        c.close();
        db.close();
        return perfil;
    }
}
