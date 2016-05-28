package org.inspira.jcapiz.polivoto.threading;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import org.inspira.jcapiz.polivoto.actividades.Bienvenida;
import org.inspira.jcapiz.polivoto.actividades.NuevoProcesoDeVotacion;
import org.inspira.jcapiz.polivoto.database.Votaciones;
import org.inspira.jcapiz.polivoto.networking.IOHandler;
import org.inspira.jcapiz.polivoto.pojo.Opcion;
import org.inspira.jcapiz.polivoto.pojo.Pregunta;
import org.inspira.jcapiz.polivoto.pojo.Votacion;
import org.inspira.jcapiz.polivoto.proveedores.ProveedorDeMarshalling;
import org.inspira.jcapiz.polivoto.proveedores.ProveedorDeRecursos;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by jcapiz on 5/12/15.
 */
public class RegistraVotacionGlobal extends AsyncTask<String,String,String> {

    private String consultorHost;
    private Activity activity;
    private Votacion votacion;
    private AccionFinalRVG accionFinal;

    public RegistraVotacionGlobal(Activity activity, String consultorHost){
        if( ! (activity instanceof AccionFinalRVG ))
            throw new ClassCastException("La actividad debe implementar AccionFinalRVG");
        this.activity = activity;
        this.consultorHost = consultorHost;
        accionFinal = (AccionFinalRVG) activity;
    }

    public interface AccionFinalRVG{
        void exito();
        void percance(String mensaje);
    }

    public void setVotacion(Votacion votacion) {
        this.votacion = votacion;
    }
    // Localmente debe acceder al servicio {@bindService} para entregar una nueva tarea.
    // Dicha tarea debe ser administrada por el antes mencionado para ser atendida.
    // Tal tarea es una solicitud de conexión al servidor global para registrar el proceso.

    @Override
    protected String doInBackground(String... args){
        String result;
        try{
            JSONObject json = new JSONObject();
            json.put("action", 3);
            json.put("objeto_serializado", new JSONObject(ProveedorDeMarshalling.marshallMyVotingObject(votacion)));
            Log.d("RVG", "Vamo a mandá: " + json.toString());
            Log.d("RegisterVP", "Tiempo inicial ----> " + ProveedorDeRecursos.obtenerFormatoEnHoras(votacion.getFechaInicio()));
            Log.d("RegisterVP", "Tiempo final ----> " + ProveedorDeRecursos.obtenerFormatoEnHoras(votacion.getFechaFin()));
            Socket socket = new Socket(consultorHost, 5010);
            DataInputStream entrada = new DataInputStream(socket.getInputStream());
            DataOutputStream salida = new DataOutputStream(socket.getOutputStream());
            IOHandler ioHandler = new IOHandler(entrada, salida);
            ioHandler.sendMessage(json.toString().getBytes());
            byte[] bts = ioHandler.handleIncommingMessage();
            result = new String(bts);
            Log.d("Validacion", "Response has arrived (" + result + ")");
        }catch(JSONException | IOException e){
            e.printStackTrace();
            result = e.getMessage();
        }
        return result;
    }

    @Override
    protected void onPostExecute(String result){
        // Sigue método para quitar la actividad de espera.
        try {
            JSONObject json = new JSONObject(result);
            if ("¡Listo!".equals(json.getString("resp"))) {
                Votacion aux = ProveedorDeMarshalling.unmarshallMyVotingObject(json.getString("objeto_serializado"));
                votacion.setId(aux.getId());
                if(votacion.getId() > 0 && ProveedorDeRecursos.guardaVotacion(activity, votacion, true)) {
                    enviarPerfiles(aux.getId());
                }else{
                    accionFinal.percance("Votación registrada o por el momento no podemos atenderle");
                }
            } else {
                accionFinal.percance(result);
            }
        }catch(JSONException e){
            e.printStackTrace();
        }
    }

    private void enviarPerfiles(int idVotacion){
        Votaciones db = new Votaciones(activity);
        String[] perfiles = db.obtenerPerfiles();
        String mensaje = formatoEnvioDePerfiles(perfiles, idVotacion);
        ContactaConsultor contacto = new ContactaConsultor(resMandaPerfiles, ProveedorDeRecursos.obtenerRecursoString(activity, "ultimo_consultor_activo"), mensaje);
        contacto.start();
    }

    private String formatoEnvioDePerfiles(String[] perfiles, int idVotacion){
        String mensaje = null;
        try{
            JSONObject json = new JSONObject();
            json.put("action", 14);
            json.put("id_votacion", idVotacion);
            JSONArray jperfiles = new JSONArray();
            for(String perfil : perfiles)
                jperfiles.put(perfil);
            json.put("perfil", jperfiles);
            mensaje = json.toString();
        }catch(JSONException e){
            e.printStackTrace();
        }
        return mensaje;
    }

    private ContactaConsultor.ResultadoContactoConsultor resMandaPerfiles = new ContactaConsultor.ResultadoContactoConsultor() {
        @Override
        public void hecho() {
            accionFinal.exito();
        }

        @Override
        public void percance(String mensaje) {
            accionFinal.percance(mensaje);
        }
    };
}