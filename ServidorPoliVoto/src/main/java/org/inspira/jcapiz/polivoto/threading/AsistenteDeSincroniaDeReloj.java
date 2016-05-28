package org.inspira.jcapiz.polivoto.threading;

import org.inspira.jcapiz.polivoto.networking.IOHandler;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

/**
 * Created by jcapiz on 18/04/16.
 */
public class AsistenteDeSincroniaDeReloj extends Thread {

    private IOHandler ioHandler;

    public AsistenteDeSincroniaDeReloj(Socket socket) throws IOException {
        DataInputStream entrada = new DataInputStream(socket.getInputStream());
        DataOutputStream salida = new DataOutputStream(socket.getOutputStream());
        ioHandler = new IOHandler(entrada, salida);
    }

    @Override
    public void run() {
        try {
            ioHandler.writeLong(System.currentTimeMillis());
            ioHandler.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}