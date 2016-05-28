package org.inspira.jcapiz.polivoto.acciones;

import android.app.Activity;
import android.app.Service;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import org.inspira.jcapiz.polivoto.database.Votaciones;
import org.inspira.jcapiz.polivoto.networking.IOHandler;
import org.inspira.jcapiz.polivoto.proveedores.ProveedorDeRecursos;
import org.inspira.jcapiz.polivoto.seguridad.MD5Hash;
import org.inspira.jcapiz.polivoto.threading.AsistenteDeVotacion;
import org.inspira.jcapiz.polivoto.threading.Boss;
import org.inspira.jcapiz.polivoto.threading.MostrarToastUI;
import org.inspira.jcapiz.polivoto.threading.TerminaVotacionGlobal;
import org.inspira.jcapiz.polivoto.threading.TerminaVotacionLocal;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import resumenresultados.ScrollingActivity;
import resumenresultados.shared.Opcion;
import resumenresultados.shared.Pregunta;
import resumenresultados.shared.ResultadoPorPerfil;
import resumenresultados.shared.Votacion;

/**
 * Created by jcapiz on 17/05/16.
 */
public class TerminarVotacion {

    private Votaciones db;
    private Service mService;
    private Activity mActivity;

    public TerminarVotacion(Service mService){
        this.mService = mService;
        db = new Votaciones(mService);
    }

    public void setActivity(Activity mActivity){
        this.mActivity = mActivity;
    }

    public void finalizar(final Boss boss){
        new Thread(){
            @Override
            public void run(){
                long initTime = System.currentTimeMillis();
                try{
                    sleep(15000);
                }catch(InterruptedException ignore){
                    ignore.printStackTrace();
                }
                long endTime = System.currentTimeMillis();
                Log.e("Thread to stop", "We delayed: " + (endTime - initTime) + " millis");
                Log.e("Oracio", "Ora ya desperté!!!! Apagando todo ámonos ya!!");
                int idVotacion = db.obtenerIdVotacionActual();
                String[] votando = db.consultaVotando(idVotacion);
                String[] tmp;
                for (String str : votando) {
                    tmp = str.split(",");
                    String perfil = db.obtenerPerfilDeUsuario(tmp[0]);
                    int idPregunta = Integer.parseInt(tmp[1]);
                    String pregunta = "";
                    for (int i = 2; i < tmp.length; i++)
                        pregunta = pregunta.concat(tmp[i]);
                    String voto = "Anular mi voto";
                    db.insertaVoto(new MD5Hash().makeHashForSomeBytes(tmp[0]), db.obtenerIdVotacionActual(), perfil, voto, db.grabAdminLoginAttempt(), idPregunta);
                }
                Log.e("Oracio", "Terminé de ver que pedo!!!");
                if (db.isVotacionActualGlobal()) {
                    new TerminaVotacionGlobal(mActivity).execute();
                    Log.e("Oracio", "Inicié la finalización global!!!!");
                }
                try {
                    JSONObject json = new JSONObject();
                    AsistenteDeVotacion adv = new AsistenteDeVotacion(null, null);
                    adv.setContext(mService);
                    int i=1;
                    for(org.inspira.jcapiz.polivoto.pojo.Pregunta pregunta : db.obtenerPreguntas()) {
                        json.put("pregunta_" + i, pregunta.getEnunciado());
                        json.put("resultados_" + i++, adv.obtenerConteoDeVotos(pregunta.getId()));
                    }
                    json.put("total_preguntas", i-1);
                    json.put("action", 2);
                    json.put("titulo", db.obtenerTituloVotacionActual());
                    json.put("lugar", ProveedorDeRecursos.obtenerRecursoString(mService, "ubicacion"));
                    json.put("idVotacion", db.obtenerUltimaVotacionGlobal());
                    json.put("es_global", db.isVotacionActualGlobal());
                    Log.e("Oracio", "Terminé de hacer el formato de datos finales!!!!!");
                    Socket socket = new Socket(ProveedorDeRecursos.obtenerRecursoString(mService, "ultimo_consultor_activo"),
                            5004);
                    Log.e("Oracio", "Me conecté!!!!");
                    IOHandler ioHandler = new IOHandler(
                            new DataInputStream(socket.getInputStream()),
                            new DataOutputStream(socket.getOutputStream())
                    );
                    ioHandler.sendMessage(json.toString().getBytes());
                    ioHandler.close();
                    socket.close();
                    Log.e("Oracio", "Terminé de mandar datos finales al consultor!!!!");
                }catch(IOException | JSONException e){
                    e.printStackTrace();
                }
                try {
                    mActivity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(mActivity, "Votación terminada", Toast.LENGTH_SHORT).show();
                            mActivity.setResult(Activity.RESULT_OK);
                            mActivity.finish();
                        }
                    });
                }catch(NullPointerException e){
                    e.printStackTrace();
                }
                try {
                    TerminaVotacionLocal tvl = new TerminaVotacionLocal(mService);
                    tvl.setOnFinishVL(new TerminaVotacionLocal.OnFinishVL() {
                        @Override
                        public void success() {
                            boss.stopActions();
                            mService.stopSelf();
                            launchDetallesVotacion(db.obtenerIdVotacionActual());
                            if(mActivity != null) mActivity.runOnUiThread(new MostrarToastUI(mActivity, "Server down!"));
                        }

                        @Override
                        public void error(String message) {
                            mService.stopSelf();
                            launchDetallesVotacion(db.obtenerIdVotacionActual());
                            if(mActivity != null) mActivity.runOnUiThread(new MostrarToastUI(mActivity, message));
                        }
                    });
                    tvl.execute();
                    Log.e("Terminator", "Terminando votación local");
                } catch (NoSuchAlgorithmException e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }

    private void launchDetallesVotacion(int idVotacion){
        Intent i = new Intent(mActivity, ScrollingActivity.class);
        org.inspira.jcapiz.polivoto.pojo.Votacion votacion = new org.inspira.jcapiz.polivoto.pojo.Votacion(idVotacion);
        try {
            JSONObject datosDeVotacion = db.obtenerDatosDeVotacion(idVotacion);
            votacion.setTitulo(datosDeVotacion.getString("Titulo"));
            votacion.setIdEscuela(datosDeVotacion.getInt("idEscuela"));
            votacion.setLugar(db.obtenerNombreDeEscuela(datosDeVotacion.getInt("idEscuela")));
            votacion.setFechaInicio(datosDeVotacion.getLong("Fecha_Inicio"));
            votacion.setFechaFin(datosDeVotacion.getLong("Fecha_Fin"));
            i.putExtra("matricula_total", db.obtenerTotalNumParticipantes(idVotacion));
            try{
                i.putExtra("porcentaje_de_participacion", (float)db.obtenerCantidadParticipantes(idVotacion)/(float)db.obtenerTotalNumParticipantes(idVotacion));
            }catch(ArithmeticException a){ i.putExtra("porcentaje_de_participacion", 0f); }
        } catch (JSONException ignore){ ignore.printStackTrace(); }
        i.putExtra("total_participantes", db.obtenerCantidadParticipantes(idVotacion));
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        Votacion votacionShared = new Votacion(votacion.getTitulo());
        org.inspira.jcapiz.polivoto.pojo.Pregunta[] preguntas = db.obtenerPreguntasDeVotacion(idVotacion);
        String perfiles[] = db.obtenerPerfiles();
        Map<String,Integer> resultados;
        Map<String, Integer> resultadosPorPerfil;
        ResultadoPorPerfil resultadoPorPerfil;
        Opcion opcion;
        List<Opcion> opciones;
        for(org.inspira.jcapiz.polivoto.pojo.Pregunta preg : preguntas){
            Pregunta pregunta = new Pregunta(preg.getEnunciado());
            resultados = db.obtenerResultadosPorPregunta(preg.getId(), idVotacion);
            for(String str : resultados.keySet()) {
                opcion = new Opcion(str);
                opcion.setCantidad(resultados.get(str));
                pregunta.agregarOpcion(opcion);
            }
            for(String perfil : perfiles) {
                resultadosPorPerfil = db.obtenerResultadosPorPreguntaPorPerfil(preg.getId(), perfil, idVotacion);
                resultadoPorPerfil = new ResultadoPorPerfil();
                resultadoPorPerfil.setPerfil(perfil);
                opciones = new ArrayList<>();
                for(String key : resultadosPorPerfil.keySet()){
                    opcion = new Opcion(key);
                    opcion.setCantidad(resultadosPorPerfil.get(key));
                    opciones.add(opcion);
                }
                resultadoPorPerfil.setOpciones(opciones);
                pregunta.agregarResultadoPorPerfil(resultadoPorPerfil);
            }
            votacionShared.agregaPregunta(pregunta);
        }
        votacionShared.setIdEscuela(votacion.getIdEscuela());
        votacionShared.setDescripcion("Descripción no disponible.");
        votacionShared.setFechaFinal(ProveedorDeRecursos.obtenerFechaFormatoYearFirst(votacion.getFechaFin()));
        votacionShared.setFechaInicial(ProveedorDeRecursos.obtenerFechaFormatoYearFirst(votacion.getFechaInicio()));
        votacionShared.setMatricula(db.obtenerTotalNumParticipantes(idVotacion));
        votacionShared.setTotalParticipantes(db.obtenerCantidadParticipantes(idVotacion));
        votacionShared.setPorcentajeParticipacion(i.getFloatExtra("porcentaje_de_participacion", 0f));
        votacionShared.setGrupos(perfiles);
        votacionShared.setHash(db.obtenerHashVotacion(idVotacion));
        i.putExtra("titulo", votacion.getTitulo());
        i.putExtra("escuela", votacion.getLugar());
        i.putExtra("fecha_inicio", votacion.getFechaInicio());
        i.putExtra("fecha_fin", votacion.getFechaFin());
        i.putExtra("votacion", votacionShared);
        i.putExtra("rows", new String[]{});//db.quienesHanParticipado(idVotacion));
        i.putExtra("header", "Detalles de Participantes");
        mActivity.startActivity(i);
    }
}
