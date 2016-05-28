package servidorweb;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import resumenresultados.shared.Opcion;
import resumenresultados.shared.Pregunta;
import resumenresultados.shared.Votacion;

/**
 * Created by jcapiz on 23/05/16.
 */
public class FormatoSolicitud {

    public static String armarSolicitud(Votacion votacion){
        String solicitudSerializada = null;
        try{
            JSONObject json = new JSONObject();
            JSONObject jvotacion = new JSONObject();
            jvotacion.put("institucion", votacion.getIdEscuela());
            jvotacion.put("vhash", votacion.getHash());
            jvotacion.put("titulo", votacion.getTitulo());
            jvotacion.put("descripcion", votacion.getDescripcion());
            jvotacion.put("fecha_in", votacion.getFechaInicial());
            jvotacion.put("fecha_fin", votacion.getFechaFinal());
            jvotacion.put("matricula", votacion.getMatricula());
            jvotacion.put("total_p", votacion.getTotalParticipantes());
            jvotacion.put("tasa_p", String.format("%.2f",votacion.getPorcentajeParticipacion()*100f).concat("%"));
            jvotacion.put("estado", "Visible");
            JSONObject jdataGrupos = new JSONObject();
            JSONArray jgrupos = new JSONArray();
            JSONObject jgrupo;
            JSONObject jpreguntas;
            JSONArray jpreguntasArray;
            JSONObject jpreguntasArrayObject;
            JSONObject jopciones;
            JSONArray jopcionesArray;
            JSONObject jopcionesArrayObject;
            for(int i=0; i<votacion.getGrupos().length; i++){
                jgrupo = new JSONObject();
                jgrupo.put("nombre", votacion.getGrupos()[i]);
                jpreguntasArray = new JSONArray();
                for(int j=0; j<votacion.getPreguntas().size(); j++){
                    jpreguntasArrayObject = new JSONObject();
                    jpreguntasArrayObject.put("titulo_pregunta", votacion.getPreguntas().get(j).getTitulo());
                    jopciones = new JSONObject();
                    jopcionesArray = new JSONArray();
                    for(Opcion opcion : votacion.getPreguntas().get(j).getResultadosPorPerfil().get(votacion.getPreguntas().get(j).buscarPerfil(votacion.getGrupos()[i])).getOpciones()){
                        jopcionesArrayObject = new JSONObject();
                        jopcionesArrayObject.put("name", opcion.getNombre());
                        jopcionesArrayObject.put("y", opcion.getCantidad());
                        jopcionesArray.put(jopcionesArrayObject);
                    }
                    jopciones.put("data", jopcionesArray);
                    jpreguntasArrayObject.put("opciones", jopciones);
                    jpreguntasArray.put(jpreguntasArrayObject);
                }
                jpreguntas = new JSONObject();
                jpreguntas.put("data", jpreguntasArray);
                jgrupo.put("preguntas", jpreguntas);
                jgrupos.put(jgrupo);
            }
            jgrupo = new JSONObject();
            jgrupo.put("nombre", "Total");
            jpreguntasArray = new JSONArray();
            for(Pregunta pregunta : votacion.getPreguntas()){
                jpreguntasArrayObject = new JSONObject();
                jpreguntasArrayObject.put("titulo_pregunta", pregunta.getTitulo());
                jopciones = new JSONObject();
                jopcionesArray = new JSONArray();
                for(Opcion opcion : pregunta.getOpciones()){
                    jopcionesArrayObject = new JSONObject();
                    jopcionesArrayObject.put("name", opcion.getNombre());
                    jopcionesArrayObject.put("y", opcion.getCantidad());
                    jopcionesArray.put(jopcionesArrayObject);
                }
                jopciones.put("data", jopcionesArray);
                jpreguntasArrayObject.put("opciones", jopciones);
                jpreguntasArray.put(jpreguntasArrayObject);
            }
            jpreguntas = new JSONObject();

            jpreguntas.put("data", jpreguntasArray);
            jgrupo.put("preguntas", jpreguntas);
            jgrupos.put(jgrupo);
            jdataGrupos.put("data", jgrupos);
            jvotacion.put("grupos", jdataGrupos);
            JSONArray jvotaciones = new JSONArray();
            jvotaciones.put(jvotacion);
            json.put("votacion", jvotaciones);
            solicitudSerializada = json.toString();
        }catch(JSONException e){
            e.printStackTrace();
        }
        Log.e("Horror!!", "La solicitú es: " + solicitudSerializada);
        return solicitudSerializada;
    }
}
