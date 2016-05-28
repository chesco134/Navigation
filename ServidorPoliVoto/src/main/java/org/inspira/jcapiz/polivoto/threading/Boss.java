package org.inspira.jcapiz.polivoto.threading;

import android.content.Context;
import android.util.Log;

import org.inspira.jcapiz.polivoto.database.Votaciones;
import org.inspira.jcapiz.polivoto.networking.IOHandler;
import org.inspira.jcapiz.polivoto.proveedores.ProveedorDeRecursos;
import org.inspira.jcapiz.polivoto.proveedores.ProveedorDeTiempoRemoto;
import org.inspira.jcapiz.polivoto.seguridad.PRNGFixes;
import org.json.JSONException;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by jcapiz on 17/05/16.
 */
public class Boss extends Thread {

    private AsistenteDeVotacion.InteraccionConActividad agenteDeInteraccion;
    private ServerSocket server;
    private Context context;
    private TimerTask task;
    private boolean esGlobal;

    public Boss(Context context, AsistenteDeVotacion.InteraccionConActividad agenteDeInteraccion, TimerTask task, boolean esGlobal){
        this.context = context;
        this.agenteDeInteraccion = agenteDeInteraccion;
        this.task = task;
        this.esGlobal = esGlobal;
    }

    @Override
    public void run(){
        PRNGFixes.apply();
        try {
            Votaciones db = new Votaciones(context);
            long millis;
            if(!db.obtenerDatosDeVotacionActual().getBoolean(("Soy_Propietario")))
                millis = ProveedorDeTiempoRemoto.obtenerTiempoRemoto(context, db.isVotacionActualGlobal());
            else
                millis = ProveedorDeTiempoRemoto.obtenerTiempoRemoto(context, false);
            if(millis > 0) {
                Timer finisher = new Timer();
                finisher.schedule(task, millis);
                server = new ServerSocket(23543);
                new Thread(){ @Override public void run(){
                    try {
                        Socket socket = new Socket(ProveedorDeRecursos
                                .obtenerRecursoString(context, "ultimo_consultor_activo")
                                , 5010);
                        DataInputStream entrada = new DataInputStream(socket.getInputStream());
                        DataOutputStream salida = new DataOutputStream(socket.getOutputStream());
                        IOHandler ioHandler = new IOHandler(entrada, salida);
                        ioHandler.sendMessage("{\"action\": 10}".getBytes());
                        String resp = new String(ioHandler.handleIncommingMessage());
                        if("¡Listo!".equals(resp)){
                            Log.i("MiServicio", "¡Listo! Pero mock!");
                        }else{
                            Log.i("MiServicio", "No fue listo: " + resp + "$$");
                        }
                    }catch(IOException e){
                        e.printStackTrace();
                    }} }.start();
                while (true) { // En éste loop se producen tareas.
                    Socket socket = server.accept();
                    AsistenteDeVotacion asistenteDeVotacion = new AsistenteDeVotacion(socket.getInputStream(), socket.getOutputStream());
                    asistenteDeVotacion.setContext(context);
                    asistenteDeVotacion.setRHost(socket.getRemoteSocketAddress().toString());
                    asistenteDeVotacion.setAgenteDeInteraccion(agenteDeInteraccion);
                    asistenteDeVotacion.start();
                }
            }
        } catch (JSONException | IOException e) {
            e.printStackTrace();
//            ProveedorToast.showToast(context, "Server down!");
        }
    }

    public void stopActions(){
        try{
            server.close();
        }catch (IOException e){
            e.printStackTrace();
        }
    }
}
