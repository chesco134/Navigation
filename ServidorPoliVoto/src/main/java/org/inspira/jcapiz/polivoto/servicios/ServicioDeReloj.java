package org.inspira.jcapiz.polivoto.servicios;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import org.inspira.jcapiz.polivoto.threading.AsistenteDeSincroniaDeReloj;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by jcapiz on 18/04/16.
 */
public class ServicioDeReloj extends Service {

    private ServiceHandler mServiceHandler;
    private ServerSocket server;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    // Handler that receives messages from the thread
    private final class ServiceHandler extends Handler {

        public ServiceHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            try {
                Log.d("ServicioDeReloj", "Caldo de huateque");
                server = new ServerSocket(33059);
                Log.d("ServicioDeReloj", "Hemos iniciado");
                while (true) { // En éste loop se producen tareas.
                    Socket socket = server.accept();
                    new AsistenteDeSincroniaDeReloj(socket).start();
                }
            } catch (IOException e) {
                Toast.makeText(ServicioDeReloj.this, "Server down!",
                        Toast.LENGTH_SHORT).show();
                e.printStackTrace();
                if(!e.getMessage().contains("already in use"))
                stopSelf();
            }
        }
    }

    @Override
    public void onCreate() {
        HandlerThread thread = new HandlerThread("ServiceStartArguments",
                android.os.Process.THREAD_PRIORITY_BACKGROUND);
        thread.start();

        // Get the HandlerThread's Looper and use it for our Handler
        Looper mServiceLooper = thread.getLooper();
        mServiceHandler = new ServiceHandler(mServiceLooper);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // For each start request, send a message to start a job and deliver the
        // start ID so we know which request we're stopping when we finish the
        // job
        Message msg = mServiceHandler.obtainMessage();
        msg.arg1 = startId;
        mServiceHandler.sendMessage(msg);
        // If we get killed, after returning from here, restart (START_STICKY)
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d("Servicio", "Destroying service. I love everyone");
        try {
            server.close();
        } catch (IOException | NullPointerException e) {
            e.printStackTrace();
        }
        Log.d("ServicioDeReloj", "Ya nos vamos");
    }
}
