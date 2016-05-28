package org.inspira.jcapiz.polivoto.threading;

import org.inspira.jcapiz.polivoto.networking.IOHandler;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

/**
 * Created by jcapiz on 9/05/16.
 */
public class ContactaConsultor extends Thread {

    private String host;
    private String serializedMessage;
    private ResultadoContactoConsultor contactoConsultor;

    public ContactaConsultor(ResultadoContactoConsultor contactoConsultor, String host, String serializedMessage){
        this.host = host;
        this.serializedMessage = serializedMessage;
        this.contactoConsultor = contactoConsultor;
    }

    public interface ResultadoContactoConsultor{
        void hecho();
        void percance(String mensaje);
    }

    @Override
    public void run(){
        try{
            Socket socket;
            socket = new Socket();
            socket.connect(new InetSocketAddress(host, 5010), 5000);
            DataInputStream entrada = new DataInputStream(socket.getInputStream());
            DataOutputStream salida = new DataOutputStream(socket.getOutputStream());
            IOHandler ioHandler = new IOHandler(entrada, salida);
            ioHandler.sendMessage(serializedMessage.getBytes());
            byte[] resp = ioHandler.handleIncommingMessage();
            ioHandler.close();
            socket.close();
            String respuesta = new String(resp);
            if(!"¡Listo!".equals(respuesta))
                contactoConsultor.percance(respuesta);
            else
                contactoConsultor.hecho();
        }catch(IOException e){
            e.printStackTrace();
            contactoConsultor.percance(e.getMessage());
        }
    }
}
