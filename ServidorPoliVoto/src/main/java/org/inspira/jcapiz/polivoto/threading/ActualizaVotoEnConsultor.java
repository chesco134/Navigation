package org.inspira.jcapiz.polivoto.threading;

import android.util.Log;

import org.inspira.jcapiz.polivoto.networking.IOHandler;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

/**
 * Created by jcapiz on 9/01/16.
 */
public class ActualizaVotoEnConsultor extends Thread {

    private String host;
    private int participantes;

    public void setParticipantes(int participantes) {
        this.participantes = participantes;
    }

    public ActualizaVotoEnConsultor(String host){
        this.host = host;
    }

    @Override
    public void run(){
        try{
            Socket socket = new Socket(host,5004);
            IOHandler ioHandler = new IOHandler(
                    new DataInputStream(socket.getInputStream()),
                    new DataOutputStream(socket.getOutputStream())
            );
            JSONObject json = new JSONObject();
            json.put("action", 1);
            json.put("participantes", participantes);
            ioHandler.sendMessage(json.toString().getBytes());
            ioHandler.close();
            socket.close();
            Log.d("Envoy", "Actualización enviada.");
        }catch(JSONException | IOException e){ e.printStackTrace(); }
    }
}
