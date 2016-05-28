package servidorweb;

import android.util.Log;

import org.json.JSONException;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLDecoder;

/**
 * Created by jcapiz on 23/05/16.
 */
public class ContactoServidorWeb {

    public static String subirDatosDeVotacion(String content) {
        String response = null;
        try {
            Log.e("Horror!!", "Now connecting");
            HttpURLConnection con = (HttpURLConnection) new URL("http://votacionesipn.com/WebServices/subir_local.php").openConnection();
            //HttpURLConnection con = (HttpURLConnection) new URL("http://localhost:80/WebServices/Ejemplo1/ws1.php").openConnection();
            Log.e("Horror!!", "Connected");
            con.setDoOutput(true);
            DataOutputStream salida = new DataOutputStream(con.getOutputStream());
            Log.e("Horror!!", "Sending message...");
            salida.write(content.getBytes());
            salida.flush();
            Log.e("Horror!!", "Message sent");
            int length;
            byte[] chunk = new byte[64];
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            DataInputStream entrada = new DataInputStream(con.getInputStream());
            Log.e("Horror!!", "Receiving data...");
            while ((length = entrada.read(chunk)) != -1) {
                baos.write(chunk, 0, length);
            }
            response = URLDecoder.decode(baos.toString(), "utf8").trim();
            response = response.substring(1, response.length());
            baos.close();
            con.disconnect();
            entrada.close();
            salida.close();
        }catch(IOException e){
            e.printStackTrace();
        }
        Log.e("Horror!!", "La respuesta es: " + response);
        return response;
    }
}
