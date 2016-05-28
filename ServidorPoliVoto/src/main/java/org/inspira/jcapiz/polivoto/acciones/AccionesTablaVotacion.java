package org.inspira.jcapiz.polivoto.acciones;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import org.inspira.jcapiz.polivoto.database.Votaciones;
import org.inspira.jcapiz.polivoto.pojo.Opcion;
import org.inspira.jcapiz.polivoto.pojo.Pregunta;
import org.inspira.jcapiz.polivoto.pojo.PreguntaOpcion;
import org.inspira.jcapiz.polivoto.pojo.Votacion;
import org.inspira.jcapiz.polivoto.pojo.VotacionGlobal;
import org.inspira.jcapiz.polivoto.proveedores.ProveedorDeRecursos;

/**
 * Created by jcapiz on 7/04/16.
 */
public class AccionesTablaVotacion {

    public static void agregaVotacion(Context context, Votacion votacion){
        ContentValues values = new ContentValues();
        values.put("idVotacion", votacion.getId());
        values.put("idEscuela", votacion.getIdEscuela());
        values.put("Titulo", votacion.getTitulo());
        values.put("Fecha_Inicio", votacion.getFechaInicio());
        values.put("Fecha_Fin", votacion.getFechaFin());
        SQLiteDatabase db = new Votaciones(context).getWritableDatabase();
        db.insert("Votacion", "---", values);
        db.close();
    }

    public static void agregarPregunta(Context context, Pregunta pregunta){
        ContentValues values = new ContentValues();
        values.put("idPregunta", pregunta.getId());
        values.put("idVotacion", pregunta.getIdVotacion());
        values.put("Pregunta", pregunta.getEnunciado());
        SQLiteDatabase db = new Votaciones(context).getWritableDatabase();
        db.insert("Pregunta", "---", values);
        db.close();
    }

    public static void agregarOpcion(Context context, Opcion opcion){
        ContentValues values = new ContentValues();
        values.put("idOpcion", opcion.getId());
        values.put("Reactivo", opcion.getReactivo());
        SQLiteDatabase db = new Votaciones(context).getWritableDatabase();
        db.insert("Opcion", "---", values);
        db.close();
    }

    public static void agregarPreguntaOpcion(Context context, PreguntaOpcion preguntaOpcion){
        ContentValues values = new ContentValues();
        values.put("idOpcion", preguntaOpcion.getIdOpcion());
        values.put("idPregunta", preguntaOpcion.getIdPregunta());
        SQLiteDatabase db = new Votaciones(context).getWritableDatabase();
        db.insert("PreguntaOpcion", "---", values);
        db.close();
    }

    public static void agregarVotacionGlobal(Context context, VotacionGlobal votacionGlobal){
        ContentValues values = new ContentValues();
        values.put("Sincronizado", votacionGlobal.isSincronizado() ? 1 : 0);
        SQLiteDatabase db = new Votaciones(context).getWritableDatabase();
        db.insert("VotacionGlobal", "---", values);
        db.close();
    }

    public static void agregarVotacionRespaldo(Context context, int idVotacion){
        ContentValues values = new ContentValues();
        values.put("idVotacion", idVotacion);
        SQLiteDatabase db = new Votaciones(context).getWritableDatabase();
        db.insert("Votacion", "---", values);
        db.close();
    }

    public static boolean esVotacionDeRespaldo(Context context){
        SQLiteDatabase db = new Votaciones(context).getReadableDatabase();
        Cursor c = db.rawQuery("select count(*) from VotacionRespaldo where idVotacion = " +
                "CAST(? as INTEGER)",
                new String[]{String.valueOf(ProveedorDeRecursos.obtenerRecursoEntero(context, "idVotacion"))});
        c.moveToFirst();
        boolean votacionDeRespaldo = c.getInt(0) > 0;
        c.close();
        db.close();
        return votacionDeRespaldo;
    }
}
