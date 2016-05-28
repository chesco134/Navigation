package org.inspira.jcapiz.polivoto.threading;

import android.app.Activity;
import android.os.AsyncTask;
import android.util.Log;

import org.inspira.jcapiz.polivoto.actividades.NuevoProcesoDeVotacion;
import org.inspira.jcapiz.polivoto.database.Votaciones;
import org.inspira.jcapiz.polivoto.proveedores.ProveedorDeRecursos;
import org.inspira.jcapiz.polivoto.seguridad.Cifrado;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

import resumenresultados.shared.ResultadoVotacion;

/**
 * Created by jcapiz on 5/12/15.
 */


public class TerminaVotacionGlobal extends AsyncTask<CharSequence,String,String> {
    Activity context;
    private Votaciones v;
    private int idVotacion;

    public TerminaVotacionGlobal(Activity context){
        this.context = context;
    }

    @Override
    protected String doInBackground(CharSequence... args){
        v = new Votaciones(context);
        idVotacion = v.obtenerIdVotacionActual();
        String response = null;
        JSONObject json = new JSONObject();
        try {
            json.put("action", 8);
            json.put("title", v.obtenerTituloVotacionActual());
            json.put("id_votacion", v.obtenerUltimaVotacionGlobal());
            new ContactaConsultor(res, ProveedorDeRecursos.obtenerRecursoString(context, "ultimo_consultor_activo"), json.toString()).start();
        } catch (JSONException e) {
            e.printStackTrace();
            response = ("Servicio por el momento no disponible");
        }
        v.close();
        return response;
    }

    @Override
    protected void onPostExecute(String arg){
        //Toast.makeText(context, arg, Toast.LENGTH_LONG).show();
        //((VotacionesConf)context).quitaActividad();
        //((VotacionesConf)context).detenServicio();
    }

    private ContactaConsultor.ResultadoContactoConsultor res = new ContactaConsultor.ResultadoContactoConsultor() {
        @Override
        public void hecho() {
            String[] rows = v.consultaVoto(idVotacion);
            String[] settings = v.quienesHanParticipado(v.obtenerIdVotacionActual());
            String[] votando = v.consultaVotando(idVotacion);
            String[] logs = v.obtenerLog();
            Cifrado cipher = new Cifrado("MyPriceOfHistory");
            byte[][] votosCifrados = new byte[rows.length][];
            byte[][] participantesCifrados = new byte[settings.length][];
            byte[][] votandoCifrados = new byte[votando.length][];
            byte[][] logsCifrados = new byte[logs.length][];
            Log.d("Capiz", "Tenemos " + rows.length + " votos.");
            for(int index = 0; index < rows.length; index++){
                votosCifrados[index] = cipher.cipher(rows[index]);
                Log.d("Capiz",rows[index]);
            }
            for(int index = 0; index<settings.length; index++){
                participantesCifrados[index] = cipher.cipher(settings[index]);
            }
            for(int index = 0; index<votando.length; index++){
                votandoCifrados[index] = cipher.cipher(votando[index]);
            }
            for(int index = 0; index<logs.length; index++){
                logsCifrados[index] = cipher.cipher(logs[index]);
            }
            ResultadoVotacion resultadoFinalVotos = new ResultadoVotacion(votosCifrados);
            ResultadoVotacion resultadoFinalParticipantes = new ResultadoVotacion(participantesCifrados);
            ResultadoVotacion resultadoFinalVotando = new ResultadoVotacion(votandoCifrados);
            ResultadoVotacion resultadoFinalLogs = new ResultadoVotacion(logsCifrados);
            try{
                ObjectOutputStream salidaArchivo = new ObjectOutputStream(new FileOutputStream(NuevoProcesoDeVotacion.RESULTS_FILE));
                salidaArchivo.writeObject(resultadoFinalVotos);
                salidaArchivo.writeObject(resultadoFinalParticipantes);
                salidaArchivo.writeObject(resultadoFinalVotando);
                salidaArchivo.writeObject(resultadoFinalLogs);
                salidaArchivo.close();
                //v.terminarProceso();
                String mensaje = "Votación terminada";
                Log.d("Finisher Task", mensaje);
                context.runOnUiThread(new MostrarToastUI(context, mensaje));
            }catch(IOException ex){
                context.runOnUiThread(new MostrarToastUI(context, ex.getMessage()));
                Log.d("Finisher Task", ex.getMessage());
            }
        }

        @Override
        public void percance(String mensaje) {
            Log.d("Finisher Task", mensaje);
            context.runOnUiThread(new MostrarToastUI(context, mensaje));
        }
    };
}