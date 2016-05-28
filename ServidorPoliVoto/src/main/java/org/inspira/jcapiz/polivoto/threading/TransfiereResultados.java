package org.inspira.jcapiz.polivoto.threading;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import org.inspira.jcapiz.polivoto.actividades.ConfiguraParticipantes;
import org.inspira.jcapiz.polivoto.database.Votaciones;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

/**
 * Created by jcapiz on 25/02/16.
 */
public class TransfiereResultados extends Thread {

    private Context context;

    public TransfiereResultados(Context context){
        this.context = context;
    }

    @Override
    public void run(){
        Votaciones v = new Votaciones(context);
        JSONArray jarr;
        try {
            jarr = new JSONArray(v.grabVotosForVotacion(v.obtenerTituloVotacionActual()));
            JSONObject json = new JSONObject();
            json.put("action",1);
            json.put("content",jarr);
            SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
            String something = sharedPref.getString(ConfiguraParticipantes.ENTER_SOMETHING_KEY, "");
            Log.d("TransferResults", "Contacting " + something);
            try {
                Socket socket = new Socket(something,23543);
                DataOutputStream sal = new DataOutputStream(socket.getOutputStream());
                sal.write((byte) 254);
                DataInputStream in = new DataInputStream(socket.getInputStream());
                Log.d("SANDER", "We are about to send " + json.toString().getBytes().length + " bytes\n" + json
                        .toString());
                byte[] chunk = json.toString().getBytes();
                for(int i = 0; i < (chunk.length/64); i++){
                    sal.write(64);
                    sal.write(chunk,i*64,64);
                    in.read();
                }
                int blocks = (int)(chunk.length/64);
                int remaining = chunk.length - 64*blocks;
                if(remaining > 0) {
                    sal.write(remaining);
                    sal.write(chunk, blocks*64, remaining);
                    in.read();
                }
                sal.write(0);
                sal.flush();
                in.read();
                jarr = new JSONArray(v.grabParticipantes());
                json.put("action", 2);
                json.put("content", jarr);
                socket = new Socket(something,23543);
                sal = new DataOutputStream(socket.getOutputStream());
                sal.write((byte)254);
                sal.write(json.toString().getBytes().length);
                sal.write(json.toString().getBytes());
                sal.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (JSONException e) {
            e.printStackTrace();
        } catch(IllegalArgumentException e){
            e.printStackTrace();
            // Toast.makeText(VotacionesConf.this,"No hay resultados qué migrar",Toast.LENGTH_SHORT).show();
            // Debería simplemente aparecer habilitada o deshabilitada.
        }
    }
}