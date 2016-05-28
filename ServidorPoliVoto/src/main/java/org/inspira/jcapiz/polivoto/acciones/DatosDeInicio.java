package org.inspira.jcapiz.polivoto.acciones;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import org.inspira.jcapiz.polivoto.database.Votaciones;
import org.inspira.jcapiz.polivoto.pojo.Escuela;
import org.inspira.jcapiz.polivoto.pojo.NombreParticipante;
import org.inspira.jcapiz.polivoto.pojo.Participante;
import org.inspira.jcapiz.polivoto.pojo.Perfil;

/**
 * Created by jcapiz on 7/04/16.
 */
public class DatosDeInicio {

    public static void agregaEscuela(Context context, Escuela escuela){
        ContentValues values = new ContentValues();
        values.put("idEscuela", escuela.getId());
        values.put("Nombre", escuela.getNombre());
        Votaciones votaciones = new Votaciones(context);
        SQLiteDatabase db = votaciones.getWritableDatabase();
        db.insert("Escuela", "---", values);
        db.close();
    }

    public static void agregarPerfil(Context context, Perfil perfil){
        ContentValues values = new ContentValues();
        values.put("idPerfil", perfil.getId());
        values.put("perfil", perfil.getPerfil());
        SQLiteDatabase db = new Votaciones(context).getWritableDatabase();
        db.insert("Perfil", "---", values);
        db.close();
    }

    public static void agregarParticipante(Context context, Participante participante){
        ContentValues values = new ContentValues();
        values.put("Boleta", participante.getBoleta());
        values.put("idEscuela", participante.getIdEscuela());
        values.put("Fecha_Registro", participante.getFechaRegistro());
        values.put("BoletaHash", participante.getBoletaHash());
        SQLiteDatabase db = new Votaciones(context).getWritableDatabase();
        db.insert("Participante", "---", values);
        db.close();
    }

    public static void agregarNombreParticipante(Context context, NombreParticipante nombreParticipante){
        ContentValues values = new ContentValues();
        values.put("Boleta", nombreParticipante.getBoleta());
        values.put("Nombre", nombreParticipante.getNombre());
        values.put("ApPaterno", nombreParticipante.getApPaterno());
        values.put("ApMaterno", nombreParticipante.getApMaterno());
        SQLiteDatabase db = new Votaciones(context).getWritableDatabase();
        db.insert("NombreParticipante", "---", values);
        db.close();
    }
}
