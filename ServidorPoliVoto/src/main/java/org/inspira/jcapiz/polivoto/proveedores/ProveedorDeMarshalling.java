package org.inspira.jcapiz.polivoto.proveedores;

import org.inspira.jcapiz.polivoto.pojo.CuestionarioVotacion;
import org.inspira.jcapiz.polivoto.pojo.Opcion;
import org.inspira.jcapiz.polivoto.pojo.Pregunta;
import org.inspira.jcapiz.polivoto.pojo.ProcesoDisponible;
import org.inspira.jcapiz.polivoto.pojo.Votacion;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by jcapiz on 8/05/16.
 */
public class ProveedorDeMarshalling {

    public static String marshallMyVotingObject(Votacion votacion){
        String serializedString = null;
        try{
            JSONObject json = new JSONObject();
            json.put("idVotacion", votacion.getId());
            json.put("titulo", votacion.getTitulo());
            json.put("lugar", votacion.getLugar());
            json.put("id_place", votacion.getIdEscuela());
            json.put("fecha_inicial", votacion.getFechaInicio());
            json.put("fecha_final", votacion.getFechaFin());
            json.put("es_global", votacion.isGlobal() ? 1 : 0);
            JSONObject jpregunta;
            JSONArray jpreguntas = new JSONArray();
            JSONArray jopciones;
            JSONObject jopcion;
            for(Pregunta pregunta : votacion.getPreguntas()){
                jpregunta = new JSONObject();
                jpregunta.put("enunciado", pregunta.getEnunciado());
                jopciones = new JSONArray();
                for(Opcion opcion : pregunta.getOpciones()){
                    jopcion = new JSONObject();
                    jopcion.put("enunciado", opcion.getReactivo());
                    jopciones.put(jopcion);
                }
                jpregunta.put("opciones", jopciones);
                jpreguntas.put(jpregunta);
            }
            json.put("preguntas", jpreguntas);
            serializedString = json.toString();
        }catch(JSONException e){
            e.printStackTrace();
        }
        return serializedString;
    }

    public static Votacion unmarshallMyVotingObject(String serializedObject){
        Votacion votacion = null;
        try{
            JSONObject json = new JSONObject(serializedObject);
            JSONArray jpreguntas = json.getJSONArray("preguntas");
            votacion = new Votacion();
            votacion.setId(json.getInt("idVotacion"));
            votacion.setTitulo(json.getString("titulo"));
            votacion.setLugar(json.getString("lugar"));
            votacion.setIdEscuela(json.getInt("id_place"));
            votacion.setFechaInicio(json.getLong("fecha_inicial"));
            votacion.setFechaFin(json.getLong("fecha_final"));
            votacion.setGlobal(json.getInt("es_global") != 0);
            JSONObject jpregunta;
            JSONArray jopciones;
            JSONObject jopcion;
            for(int i=0; i < jpreguntas.length(); i++){
                jpregunta = jpreguntas.getJSONObject(i);
                votacion.agregarPregunta(jpregunta.getString("enunciado"));
                jopciones = jpregunta.getJSONArray("opciones");
                for(int j=0; j < jopciones.length(); j++){
                    jopcion = jopciones.getJSONObject(j);
                    votacion.agregarOpcion(i, jopcion.getString("enunciado"));
                }
            }
        }catch(JSONException e){
            e.printStackTrace();
        }
        return votacion;
    }

    public static String marshallQuiz(CuestionarioVotacion cuestionario){
        String marshalledQuiz = null;
        try{
            JSONObject json = new JSONObject();
            JSONArray jpreguntas = new JSONArray();
            JSONObject jpregunta;
            JSONArray jopciones;
            JSONObject jopcion;
            Pregunta pregunta;
            Opcion opcion;
            for(int i=0; i<cuestionario.obtenerCantidadPreguntas(); i++){
                pregunta = cuestionario.obtenerPregunta(i);
                jpregunta = new JSONObject();
                jpregunta.put("enunciado", pregunta.getEnunciado());
                jopciones = new JSONArray();
                for(int j=0; j<pregunta.obtenerCantidadOpciones(); j++){
                    opcion = pregunta.obtenerOpcion(j);
                    jopcion = new JSONObject();
                    jopcion.put("enunciado", opcion.getReactivo());
                    jopciones.put(jopcion);
                }
                jpregunta.put("opciones", jopciones);
                jpreguntas.put(jpregunta);
            }
            json.put("cuestionario", jpreguntas);
            marshalledQuiz = json.toString();
        }catch(JSONException e){
            e.printStackTrace();
        }
        return marshalledQuiz;
    }

    public static CuestionarioVotacion unmarsharllQuiz(String marshalledQuiz){
        CuestionarioVotacion cuestionario = null;
        try{
            JSONObject json = new JSONObject(marshalledQuiz);
            JSONArray jpreguntas = json.getJSONArray("cuestionario");
            JSONObject jpregunta;
            JSONArray jopciones;
            JSONObject jopcion;
            Pregunta pregunta;
            List<Pregunta> preguntas = new ArrayList<>();
            Opcion opcion;
            List<Opcion> opciones;
            for(int i=0; i<jpreguntas.length(); i++){
                jpregunta = jpreguntas.getJSONObject(i);
                pregunta = new Pregunta();
                pregunta.setEnunciado(jpregunta.getString("enunciado"));
                opciones = new ArrayList<>();
                jopciones = jpregunta.getJSONArray("opciones");
                for(int j=0; j<jopciones.length(); j++){
                    jopcion = jopciones.getJSONObject(j);
                    opcion = new Opcion(jopcion.getString("enunciado"), jopcion.getInt("idPregunta"));
                    opciones.add(opcion);
                }
                pregunta.setOpciones(opciones);
                preguntas.add(pregunta);
            }
            cuestionario = new CuestionarioVotacion(preguntas);
        }catch(JSONException e){
            e.printStackTrace();
        }
        return cuestionario;
    }

    public static ProcesoDisponible[] unmarshallAvailableProcesses(String marshalledProcesses){
        ProcesoDisponible[] processes = null;
        try{
            JSONArray jprocesses = new JSONArray(marshalledProcesses);
            JSONObject jprocess;
            List<ProcesoDisponible> titulos = new ArrayList<>();
            ProcesoDisponible procesoDisponible;
            for(int i=0; i<jprocesses.length(); i++){
                jprocess = jprocesses.getJSONObject(i);
                procesoDisponible = new ProcesoDisponible();
                procesoDisponible.setTitulo(jprocess.getString("titulo"));
                procesoDisponible.setIdVotacion(jprocess.getInt("id_votacion"));
                procesoDisponible.setfInicial(jprocess.getLong("fecha_inicial"));
                procesoDisponible.setfFinal(jprocess.getLong("fecha_final"));
                titulos.add(procesoDisponible);
            }
            processes = titulos.toArray(new ProcesoDisponible[]{});
        }catch(JSONException e){
            e.printStackTrace();
        }
        return processes;
    }
}
