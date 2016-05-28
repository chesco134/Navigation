package org.inspira.jcapiz.polivoto.threading;

import android.util.Log;

import org.inspira.jcapiz.polivoto.networking.IOHandler;
import org.inspira.jcapiz.polivoto.pojo.ValoresEsperanzaDeTiempo;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

/**
 * Created by jcapiz on 11/05/16.
 */
public class TareaDeConexion implements Runnable {

    private String host;
    private int port;
    private ValoresEsperanzaDeTiempo valoresEsperanzaDeTiempo;
    private int posicion;
    private long espera;
    private float tasaDeIncremento;

    public TareaDeConexion(String host, int port, ValoresEsperanzaDeTiempo valoresEsperanzaDeTiempo, int posicion) {
        this.host = host;
        this.port = port;
        this.valoresEsperanzaDeTiempo = valoresEsperanzaDeTiempo;
        this.posicion = posicion;
        espera = 1000;
        tasaDeIncremento = 1.05f;
    }

    @Override
    public void run(){
        boolean success = false;
        while(!success)
        try{
            Socket socket = new Socket(host, port);
            DataInputStream entrada = new DataInputStream(socket.getInputStream());
            DataOutputStream salida = new DataOutputStream(socket.getOutputStream());
            IOHandler ioHandler = new IOHandler(entrada, salida);
            ioHandler.sendMessage(obtenerMensaje().getBytes());
            procesarMensajeLlegada(new String(ioHandler.handleIncommingMessage()));
            success = true;
            Log.d("TareaDeConexion", "Hecho (position " + posicion + ")");
        }catch(IOException e){
            e.printStackTrace();
            synchronized (this){
                try{
                    Log.d("TareaDeConexion", "Limite de espera: " + espera);
                    wait(espera *= tasaDeIncremento);
                }catch(InterruptedException ex){
                    ex.printStackTrace();
                }
            }
        }
    }

    private String obtenerMensaje(){
        String mensaje = null;
        try{
            JSONObject json = new JSONObject();
            json.put("action", 12);
            mensaje = json.toString();
        }catch(JSONException e){
            e.printStackTrace();
        }
        return mensaje;
    }

    private void procesarMensajeLlegada(String mensaje){
        long tLlegada = new java.util.Date().getTime();
        long tSalida;
        try{
            JSONObject json = new JSONObject(mensaje);
            tSalida = json.getLong("t_salida");
            valoresEsperanzaDeTiempo.agregarMillisLlegada(posicion, tLlegada);
            valoresEsperanzaDeTiempo.agregarMillisSalida(posicion, tSalida);
        }catch(JSONException e){
            e.printStackTrace();
        }
    }
}