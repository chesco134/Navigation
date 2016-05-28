package org.inspira.jcapiz.polivoto.proveedores;

import android.content.Context;

import org.inspira.jcapiz.polivoto.database.Votaciones;
import org.inspira.jcapiz.polivoto.networking.IOHandler;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

/**
 * Created by jcapiz on 17/05/16.
 */
public class ProveedorDeTiempoRemoto {

    public static long obtenerTiempoRemoto(Context context, boolean esGlobal) throws IOException, JSONException {
        long millisRestantes;
        long tiempoFinal;
        Votaciones db;
        millisRestantes = -1;
        db = new Votaciones(context);
        tiempoFinal = -1;
        try {
            JSONObject json = db.obtenerDatosDeVotacionActual();
            if(json != null)
                tiempoFinal = json.getLong("Fecha_Fin");
            else
                return -1;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        if(esGlobal){
            try{
                Socket socket = new Socket(ProveedorDeRecursos.obtenerRecursoString(context, "ultimo_consultor_activo"), 5010);
                DataInputStream entrada = new DataInputStream(socket.getInputStream());
                DataOutputStream salida = new DataOutputStream(socket.getOutputStream());
                IOHandler ioHandler = new IOHandler(entrada, salida);
                ioHandler.sendMessage(("{\"action\":17, \"idVotacion\": " + new Votaciones(context).obtenerUltimaVotacionGlobal() + "}").getBytes());
                millisRestantes = new JSONObject(new String(ioHandler.handleIncommingMessage())).getLong("estampa_de_tiempo");
            }catch(JSONException | IOException e){
                e.printStackTrace();
            }
        }
        if(millisRestantes == -1)
            millisRestantes = System.currentTimeMillis();
        return tiempoFinal - millisRestantes;
    }
}
