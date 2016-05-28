package org.inspira.jcapiz.polivoto.networking;

import android.util.Log;

import org.inspira.jcapiz.polivoto.proveedores.ProveedorDeRecursos;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * Created by jcapiz on 14/12/15.
 */
public class IOHandler {

    private DataInputStream entrada;
    private DataOutputStream salida;
    private String host;
    private byte[] chunk;
    private int rate;

    public IOHandler(DataInputStream entrada, DataOutputStream salida){
        this.entrada = entrada;
        this.salida = salida;
        rate = 64;
    }

    public byte[] handleIncommingMessage() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        int toRead;
        byte[] block = new byte[64];
        // Anticipa cuantos bytes mandará el emisor, para luego leer por bloques antes de
        // esperar a que la nueva trama llegue.
        while ((toRead = readInt()) > 0) {
            int times = toRead/block.length;
            for ( int i=0; i<times; i++ ) {
                entrada.read(block);
                baos.write(block,0,block.length);
            }
            int remaining = toRead - times*block.length;
            if( remaining > 0 ){
                entrada.read(block);
                baos.write(block, 0, remaining);
            }
            writeInt(1);
        }
        chunk = baos.toByteArray();
        baos.close();
        return chunk;
    }

    public void sendMessage(byte[] message) throws IOException {
        long startingMillis = new java.util.Date().getTime();
        long endingMillis;
        long delay;
        Log.d("IOHandler", "Sending message: (" + ProveedorDeRecursos.obtenerFecha(new java.util.Date(startingMillis)) + ")");
        int times = message.length/rate;
        for( int i=0; i<times; i++ ){
            writeInt(rate);
            salida.write(message, i * rate, rate);
            readInt();
        }
        int remaining = message.length - times*rate;
        if(remaining > 0){
            writeInt(remaining);
            salida.write(message, times * rate, remaining);
            readInt();
        }
        writeInt(0);
        endingMillis = new java.util.Date().getTime();
        delay = endingMillis - startingMillis;
        Log.d("IOHandler", "Message sent: (" + ProveedorDeRecursos.obtenerFecha(new java.util.Date(endingMillis)) + ")");
        Log.d("IOHandler", "Delay: " + ProveedorDeRecursos.obtenerFormatoEnHoras(delay) + " (" + message.length + " bytes)");
    }

    public int readInt() throws IOException{
        return entrada.readInt();
    }

    public void writeInt(int i) throws IOException{
        salida.writeInt(i);
        salida.flush();
    }

    public long readLong() throws IOException{
        return entrada.readLong();
    }

    public void writeLong(long data) throws IOException{
        salida.writeLong(data);
        salida.flush();
    }

    public int getRate() {
        return rate;
    }

    public void setRate(int rate) {
        this.rate = rate;
    }

    public void close() throws IOException{
        entrada.close();
        salida.close();
    }

    public void setHost(String host) {
        this.host = host;
    }
}