package org.inspira.jcapiz.polivoto.networking.soap;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

import org.json.JSONException;
import org.json.JSONObject;
import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;
import org.xmlpull.v1.XmlPullParserException;

import android.util.Log;

/**
 * Created by Alfonso 7 on 21/09/2015.
 */
public class ClienteSOA {
    private static final String NAMESPACE = "http://votingservice.develops.capiz.org";
    private static final String SOAP_ACTION = "urn:serviceChooser";
    private static final String MAIN_REQUEST_URL = "http://192.168.1.78:8080/AnotherCode/services/ServAvailableVoteProcesses.ServAvailableVoteProcessesHttpSoap11Endpoint/";

    public String sendAction(String fValue) throws IOException, XmlPullParserException {
        String data;
        String methodname = "serviceChooser";

        SoapObject request = new SoapObject(NAMESPACE, methodname);
        request.addProperty("json", fValue);

        SoapSerializationEnvelope envelope = getSoapSerializationEnvelope(request);

        HttpTransportSE ht = getHttpTransportSE();
        ht.call(SOAP_ACTION, envelope);
        Object inti = envelope.getResponse();
        data = inti.toString();
        return data;
    }

    private final SoapSerializationEnvelope getSoapSerializationEnvelope(SoapObject request) {
        SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
        envelope.implicitTypes = true;
        //envelope.setAddAdornments(false);
        envelope.setOutputSoapObject(request);

        return envelope;
    }

    private final HttpTransportSE getHttpTransportSE() {
        HttpTransportSE ht = new HttpTransportSE(MAIN_REQUEST_URL);//"http://"+grabServerURL()+":5001/AnotherCode/services/ServAvailableVoteProcesses.ServAvailableVoteProcessesHttpSoap11Endpoint/");
        ht.debug = true;
        ht.setXmlVersionTag("<!--?xml version=\"1.0\" encoding= \"UTF-8\" ?-->");
        return ht;
    }

    private String grabServerURL(){
        String url = "189.143.24.33";
        try{
            URL serverURL = new URL("http://votacionesipn.com/services/?tag=gimmeAddr");
            HttpURLConnection con = (HttpURLConnection)serverURL.openConnection();
            DataInputStream entrada = new DataInputStream(con.getInputStream());
            byte[] bytesChunk = new byte[512];
            int bytesLeidos;
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            while((bytesLeidos = entrada.read(bytesChunk))!=-1)
                baos.write(bytesChunk, 0, bytesLeidos);
            JSONObject json = new JSONObject(baos.toString());
            con.disconnect();
            url = json.getString("content");
        }catch(IOException e){
            e.printStackTrace();
            Log.e("Mamushka", e.getMessage());
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            Log.e("Mamushka", e.getMessage());
        }
        Log.d("El Zukam", url);
        return url;
    }

}