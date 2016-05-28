package org.inspira.jcapiz.polivoto.servicios;

import android.app.Activity;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import org.inspira.jcapiz.polivoto.R;
import org.inspira.jcapiz.polivoto.acciones.TerminarVotacion;
import org.inspira.jcapiz.polivoto.actividades.SalaDeEspera;
import org.inspira.jcapiz.polivoto.database.Votaciones;
import org.inspira.jcapiz.polivoto.threading.AsistenteDeVotacion;
import org.inspira.jcapiz.polivoto.threading.Boss;

import java.util.TimerTask;

public class MiServicio extends Service {

    private static final int POLIVOTO_SERVICE = 319;
    private final IBinder mBinder = new LocalBinder();
    private int cuenta = 0;
    private ServiceHandler mServiceHandler;
    private NotificationManager mNM;
    private Activity mActivity;
    private TerminarVotacion tv;
    private Boss boss;

    public class LocalBinder extends Binder {
        public MiServicio getService() {
            return MiServicio.this;
        }
    }


    // Handler that receives messages from the thread
    private final class ServiceHandler extends Handler {

        public ServiceHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            try {
                boss.start();
                try {
                    boss.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }catch(Exception e){
                e.printStackTrace();
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
        tv = new TerminarVotacion(this);
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                tv.finalizar(boss);
            }
        };
        boss = new Boss(this, agenteDeInteraccion, task, new Votaciones(this).isVotacionActualGlobal());
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // For each start request, send a message to start a job and deliver the
        // start ID so we know which request we're stopping when we finish the
        // job
        Votaciones db = new Votaciones(this);
        Message msg = mServiceHandler.obtainMessage();
        msg.arg1 = startId;
        msg.arg2 = db.isVotacionActualGlobal() ? 1 : 0;
        mServiceHandler.sendMessage(msg);
        makeNotification();
        // If we get killed, after returning from here, restart (START_STICKY)
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d("Mars", "Destroying service. I love everyone");
        if(mNM != null)
            mNM.cancel(POLIVOTO_SERVICE);
        stopService(new Intent(this, ServicioDeReloj.class));
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    private void makeNotification(){
        mNM = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        // Creates an explicit intent for an Activity in your app
        Intent resultIntent = new Intent(this, SalaDeEspera.class);
        PendingIntent resultPendingIntent =
                PendingIntent.getActivity(
                        this,
                        0,
                        resultIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.logo_notification)
                .setOngoing(true)
                .setContentIntent(resultPendingIntent)
                .setVibrate(new long[]{100, 100, 100, 600})
                .setContentTitle("PoliVoto")
                .setContentText("Vote usté");
        mNM.notify(POLIVOTO_SERVICE, mBuilder.build());
    }

    public void setSalaDeEspera(Activity mActivity){
        this.mActivity = mActivity;
        tv.setActivity(mActivity);
    }

    public Activity getActivity(){
        return mActivity;
    }

    public int obtenerCuenta(){
        return cuenta;
    }

    private AsistenteDeVotacion.InteraccionConActividad agenteDeInteraccion = new AsistenteDeVotacion.InteraccionConActividad() {
        @Override
        public void actualizaConteoDeParticipantes() {
            incrementaContadorDeParticipantes();
        }
    };

    public void incrementaContadorDeParticipantes(){
        cuenta++;
        try{mActivity.runOnUiThread(new AccionesEnSalaDeEspera());}catch(NullPointerException ignore){}
    }

    private class AccionesEnSalaDeEspera extends Thread{
        @Override
        public void run(){
            ((SalaDeEspera)mActivity).incrementaContador();
        }

    }
}