package org.inspira.jcapiz.polivoto.pojo;

/**
 * Created by jcapiz on 11/05/16.
 */
public class ValoresEsperanzaDeTiempo {

    private long[] millisLlegada;
    private long[] millisSalida;
    private long[] deltaMillis;
    private final int cantidadElementos;

    public ValoresEsperanzaDeTiempo(int cantidadElementos){
        this.cantidadElementos = cantidadElementos;
        millisLlegada = new long[this.cantidadElementos];
        millisSalida = new long[this.cantidadElementos];
        deltaMillis = new long[this.cantidadElementos];
        for(int i=0; i<this.cantidadElementos; i++){
            millisLlegada[i] = 0;
            millisSalida[i] = 0;
            deltaMillis[i] = 0;
        }
    }

    public boolean agregarMillisSalida(int posicion, long millis){
        boolean agregado = false;
        if(posicion >= 0 && posicion < cantidadElementos){
            millisLlegada[posicion] = millis;
            agregado = true;
        }
        return agregado;
    }

    public boolean agregarMillisLlegada(int posicion, long millis){
        boolean agregado = false;
        if(posicion >= 0 && posicion < cantidadElementos){
            millisSalida[posicion] = millis;
            agregado = true;
        }
        return agregado;
    }

    public void calcularDeltaMillis(){
        for(int i=0; i<cantidadElementos; i++)
            deltaMillis[i] = millisLlegada[i] - millisSalida[i];
    }

    public long calcularEsperanzaDeTiempo(){
        long esperanzaDeTiempo = 0;
        for(long x : deltaMillis)
            esperanzaDeTiempo += x/cantidadElementos;
        return esperanzaDeTiempo;
    }

    public boolean compruebaCompletitud(){
        boolean completo = true;
        for(int i=0; i<cantidadElementos; i++){
            if(millisLlegada[i] == 0 || millisSalida[i] == 0) {
                completo = false;
                break;
            }
        }
        return completo;
    }
}
