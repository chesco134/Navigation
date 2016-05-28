package org.inspira.jcapiz.polivoto.threading;

import org.inspira.jcapiz.polivoto.pojo.ValoresEsperanzaDeTiempo;

import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Created by jcapiz on 11/05/16.
 */
public class EsperanzaDeTiempo {

    private ConcurrentLinkedQueue<Runnable> obtencionDeTiempos;
    private volatile ValoresEsperanzaDeTiempo valoresEsperanzaDeTiempo;

    private int cantidadTrabajadores;
    private Trabajador[] trabajadores;
    private boolean running;
    private long tiempoLimiteDeEspera;
    private int trabajadoresFinalizados;
    private Long esperanzaCalculada;
    private String status;
    private String host;
    private TimeToCookAction timeToCook;

    public EsperanzaDeTiempo(int cantidadDemuestras, int cantidadTrabajadores, String host, int port, long tiempoLimiteDeEspera, TimeToCookAction timeToCook){
        valoresEsperanzaDeTiempo = new ValoresEsperanzaDeTiempo(cantidadDemuestras);
        obtencionDeTiempos = new ConcurrentLinkedQueue<>();
        this.cantidadTrabajadores = cantidadTrabajadores;
        this.host = host;
        this.timeToCook = timeToCook;
        trabajadores = new Trabajador[this.cantidadTrabajadores];
        for(int i=0; i<cantidadDemuestras; i++){
            obtencionDeTiempos.add(new TareaDeConexion(host, port, valoresEsperanzaDeTiempo, i));
        }
        esperanzaCalculada = null;
        this.tiempoLimiteDeEspera =
                tiempoLimiteDeEspera < (long)1.8e5 ? (long)1.8e5
                        : tiempoLimiteDeEspera < (long)6e5 ? tiempoLimiteDeEspera : (long)6e8 ;
    }

    public void bake(){
        new java.util.Timer().schedule(new java.util.TimerTask(){ @Override public void run(){ running = false; } }, tiempoLimiteDeEspera);
        running = true;
        trabajadoresFinalizados = 0;
        status = "Ok";
        for(int i=0; i<cantidadTrabajadores; i++){
            trabajadores[i] = new Trabajador();
            trabajadores[i].setPriority(Thread.currentThread().getPriority() - 1 );
            trabajadores[i].start();
        }
    }

    private class Trabajador extends Thread{

        @Override
        public void run(){
            while(running){
                Runnable task = obtencionDeTiempos.poll();
                if(task == null)
                    break;
                task.run();
            }
            trabajoTerminado();
        }
    }

    private synchronized void trabajoTerminado() {
        trabajadoresFinalizados++;
        if(trabajadoresFinalizados == cantidadTrabajadores){
            if(valoresEsperanzaDeTiempo.compruebaCompletitud()){
                valoresEsperanzaDeTiempo.calcularDeltaMillis();
                esperanzaCalculada = valoresEsperanzaDeTiempo.calcularEsperanzaDeTiempo();
            }else{
                status = "Hubieron problemas de conexión, por favor revise que el destino esté activo\nHost: ".concat(host);
            }
            running = false;
            timeToCook.coocked(esperanzaCalculada, status);
        }
    }

    public interface TimeToCookAction {
        void coocked(Long esperanzaCalculada, String status);
    }
}
