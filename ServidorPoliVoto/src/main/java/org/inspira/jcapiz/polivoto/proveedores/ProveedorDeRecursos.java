package org.inspira.jcapiz.polivoto.proveedores;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import org.inspira.jcapiz.polivoto.R;
import org.inspira.jcapiz.polivoto.actividades.Bienvenida;
import org.inspira.jcapiz.polivoto.actividades.ConfiguraParticipantes;
import org.inspira.jcapiz.polivoto.database.Votaciones;
import org.inspira.jcapiz.polivoto.pojo.Opcion;
import org.inspira.jcapiz.polivoto.pojo.Pregunta;
import org.inspira.jcapiz.polivoto.pojo.Votacion;

import java.util.Calendar;

/**
 * Created by jcapiz on 7/04/16.
 */
public class ProveedorDeRecursos {

    private static final String SHARED_CLASS_NAME = Bienvenida.class.getName();

    public static void guardarRecursoEntero(Context context, String nombreRecurso, int valorRecurso){
        SharedPreferences.Editor editor =
                context.getSharedPreferences(SHARED_CLASS_NAME, Context.MODE_PRIVATE).edit();
        editor.putInt(nombreRecurso, valorRecurso);
        editor.apply();
    }

    public static int obtenerRecursoEntero(Context context, String nombre){
        return context.getSharedPreferences(SHARED_CLASS_NAME, Context.MODE_PRIVATE)
                .getInt(nombre, -1);
    }

    public static void guardarRecursoString(Context context, String nombreRecurso, String valorRecurso){
        SharedPreferences.Editor editor =
                context.getSharedPreferences(SHARED_CLASS_NAME, Context.MODE_PRIVATE).edit();
        editor.putString(nombreRecurso, valorRecurso);
        editor.apply();
    }

    public static String obtenerRecursoString(Context context, String nombre){
        return context.getSharedPreferences(SHARED_CLASS_NAME, Context.MODE_PRIVATE)
                .getString(nombre, "NaN");
    }

    public static boolean isSecureOptionSelected(Context context){
        return context.getSharedPreferences(ConfiguraParticipantes.class.getName(), Context.MODE_PRIVATE)
                .getBoolean("votacion_segura", true);
    }

    public static boolean guardaVotacion(Context context, Votacion votacion, boolean soyPropietario){
        Votaciones db = new Votaciones(context);
        int idVotacionLocal = db.insertaVotacion(votacion.getTitulo(),
                votacion.getLugar(),
                votacion.getFechaInicio(),
                votacion.getFechaFin(), soyPropietario);
        if(idVotacionLocal != -1) {
            for (Pregunta pregunta : votacion.getPreguntas()) {
                    pregunta.setId(db.insertaPregunta(pregunta.getEnunciado(), idVotacionLocal));
                for (Opcion opcion : pregunta.getOpciones()) {
                    opcion.setId(db.insertaOpcion(opcion.getReactivo(), pregunta.getId()));
                }
            }
            if (votacion.isGlobal()) {
                db.setVotacionAsGlobal(idVotacionLocal, votacion.getId());
            }
            votacion.setId(idVotacionLocal);
        }
        return idVotacionLocal != -1;
    }

    public static String obtenerFechaFormatoYearFirst(long millis){
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(millis);
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH) + 1;
        int dayOfMonth = c.get(Calendar.DAY_OF_MONTH);
        int hourOfDay = c.get(Calendar.HOUR_OF_DAY);
        int minute = c.get(Calendar.MINUTE);
        int second = c.get(Calendar.SECOND);
        return year
                + "/" +
                (month < 10 ? "0" + month : month)
                + "/" +
                (dayOfMonth < 10 ? "0" + dayOfMonth : dayOfMonth)
                + " " +
                (hourOfDay < 10 ? "0" + hourOfDay : hourOfDay)
                + ":" +
                (minute < 10 ? "0" + minute : minute)
                + ":" +
                (second < 10 ? "0" + second : second);
    }

    public static String obtenerFecha(){
        Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH) + 1;
        int dayOfMonth = c.get(Calendar.DAY_OF_MONTH);
        int hourOfDay = c.get(Calendar.HOUR_OF_DAY);
        int minute = c.get(Calendar.MINUTE);
        int second = c.get(Calendar.SECOND);
        return (dayOfMonth < 10 ? "0" + dayOfMonth : dayOfMonth)
                + "/" +
                (month < 10 ? "0" + month : month)
                + "/" +
                year
                + " " +
                (hourOfDay < 10 ? "0" + hourOfDay : hourOfDay)
                + ":" +
                (minute < 10 ? "0" + minute : minute)
                + ":" +
                (second < 10 ? "0" + second : second);
    }

    public static String obtenerFecha(java.util.Date fecha){
        if( fecha.getTime() < 0 )
            return "---";
        Calendar c = Calendar.getInstance();
        c.setTime(fecha);
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH) + 1;
        int dayOfMonth = c.get(Calendar.DAY_OF_MONTH);
        int hourOfDay = c.get(Calendar.HOUR_OF_DAY);
        int minute = c.get(Calendar.MINUTE);
        int second = c.get(Calendar.SECOND);
        return (dayOfMonth < 10 ? "0" + dayOfMonth : dayOfMonth)
                + "/" +
                (month < 10 ? "0" + month : month)
                + "/" +
                year
                + " " +
                (hourOfDay < 10 ? "0" + hourOfDay : hourOfDay)
                + ":" +
                (minute < 10 ? "0" + minute : minute)
                + ":" +
                (second < 10 ? "0" + second : second);
    }

    public static String obtenerFormatoEnHoras(long millis){
        long hours = (millis/(long)36e5);
        long minute = ((hours == 0l ? millis : (millis = (millis - hours*(long)36e5)))/(long)6e4);
        float second = ( minute == 0l ? millis : (millis - minute*(float)6e4))/1000;
        return (hours < 10 ? "0" + hours : hours)
                + ":" +
                (minute < 10 ? "0" + minute : minute)
                + ":" +
                (second < 10 ? "0" + second : second);
    }
}
