package org.inspira.jcapiz.polivoto.actividades;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import org.inspira.jcapiz.polivoto.R;
import org.inspira.jcapiz.polivoto.networking.IOHandler;
import org.inspira.jcapiz.polivoto.proveedores.ProveedorDeRecursos;
import org.inspira.jcapiz.polivoto.threading.AsistenteDeVotacion;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by jcapiz on 7/05/16.
 */
public class EsperandoConsultor extends Activity {

    private ServerSocket server;
    private Timer sentenciaFinal;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.esperando_consultor);
        sentenciaFinal = new Timer();
        sentenciaFinal.schedule(new TimerTask() {
            @Override
            public void run() {
                cerrarServidor();
            }
        }, 300000); // Espera 5 minutos
        new HiloDeEspera().start();
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        Log.e("EsperandoConsultor", "came to onDestroy");
        sentenciaFinal.cancel();
        try{
            server.close();
        }catch(NullPointerException | IOException ignore){}
    }

    private class HiloDeEspera extends Thread{

        @Override
        public void run(){
            IOHandler ioHandler = null;
            DataInputStream entrada;
            DataOutputStream salida;
            String rHost;
            int times = 0;
            try {
                server = new ServerSocket(23543);
                Socket socket;
                do {
                    if(ioHandler != null) {
                        ioHandler.close();
                        Log.d("EsperaConsultor", String.format("Intentando otra vez (#%d)", ++times));
                    }
                    Log.d("EsperarConsultor", "Estamos esperando a uno");
                    socket = server.accept();
                    socket.close();
                    socket = server.accept();
                    Log.d("EsperarConsultor", "Ya llegó uno");
                    entrada = new DataInputStream(socket.getInputStream());
                    salida = new DataOutputStream(socket.getOutputStream());
                    ioHandler = new IOHandler(entrada, salida);
                    int solicitud = ioHandler.readInt();
                    Log.d("EsperarConsultor", "Ya recivimos algo del uno. Solicitud: " + solicitud);
                    rHost = socket.getRemoteSocketAddress().toString().split("/")[1].split(":")[0];
                }while(!"Consultor".equals(IntercambioDeSecretos.efectuaIntercambio(ioHandler, EsperandoConsultor.this, rHost)));                ProveedorDeRecursos.guardarRecursoString(EsperandoConsultor.this, "ultimo_consultor_activo", rHost);
                sentenciaFinal.cancel();
                setResult(RESULT_OK);
            }catch(IOException e){
                e.printStackTrace();
            } finally {
                finish();
            }
        }
    }

    private void cerrarServidor(){
        try{
            server.close();
        }catch(IOException e){
            e.printStackTrace();
        }
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(EsperandoConsultor.this, "Se agotó el tiempo de espera", Toast.LENGTH_LONG).show();
            }
        });
    }
}
