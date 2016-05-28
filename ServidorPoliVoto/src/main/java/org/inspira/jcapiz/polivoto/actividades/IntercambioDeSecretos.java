package org.inspira.jcapiz.polivoto.actividades;

import android.content.Context;
import android.util.Log;

import org.inspira.jcapiz.polivoto.database.Votaciones;
import org.inspira.jcapiz.polivoto.networking.IOHandler;
import org.inspira.jcapiz.polivoto.networking.soap.ClienteSOA;
import org.inspira.jcapiz.polivoto.seguridad.Hasher;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.util.Map;
import java.util.TreeMap;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;

/**
 * Created by jcapiz on 7/05/16.
 */
public class IntercambioDeSecretos {

    public static String efectuaIntercambio(IOHandler ioHandler, Context context, String rHost){
        String categoriaStaff = null;
        try {
            KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
            kpg.initialize(1024);
            KeyPair kp = kpg.genKeyPair();
            Key publicKey = kp.getPublic();
            Key privateKey = kp.getPrivate();
            Log.d("Toröl", "~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
            // Sending the public key
            ioHandler.sendMessage(publicKey.getEncoded()); //** Successfuly sent public key **//
            Log.d("Toröl","Public key sent (" + publicKey.getEncoded().length + ")");
            byte[] cipheredAESKey = ioHandler.handleIncommingMessage();
            Log.d("**************","" + cipheredAESKey.length);
            Cipher cip = Cipher.getInstance("RSA/ECB/PKCS1Padding"); // That String is needed in ANDROID
            cip.init(Cipher.DECRYPT_MODE, privateKey);
            byte[] encodedAESKey = cip.doFinal(cipheredAESKey);
            SecretKeySpec skp = new SecretKeySpec(encodedAESKey, "AES");
            Log.d("Kamisama", "Retrieving ciphered message from client ***");
            byte[] cipheredMessage = ioHandler.handleIncommingMessage();
            cip = Cipher.getInstance("AES");
            cip.init(Cipher.DECRYPT_MODE, skp);
            // Remaining bytes conform a JSON String.
            String jstr = new String(cip.doFinal(cipheredMessage));
            Log.d("Shura", jstr);
            // This json contains the User credentials.
            Map<String, Integer> map = userCheckup(context, jstr, rHost, skp.getEncoded());
            //Log.d("La ruptura", "lid: " + lid + ", login attempt succeded: " + lon); // Hence you use the id to retrieve
            categoriaStaff = map.keySet().toArray(new String[]{})[0];
            ioHandler.writeInt(map.get(categoriaStaff));
        } catch (IOException | NoSuchAlgorithmException | InvalidKeyException | IllegalBlockSizeException | NoSuchPaddingException | BadPaddingException | JSONException e) {
            e.printStackTrace();
        }
        return categoriaStaff;
    }

    public static Map<String, Integer> userCheckup(Context context, String jstr, String rHost, byte[] skey) throws JSONException {
        Map<String, Integer> map = null;
        Votaciones db = new Votaciones(context);
        JSONObject json = new JSONObject(jstr);
        String uName = json.getString("uName");
        String psswd = json.getString("psswd"); // Password is hashed with md5
        // You need to keep the secret key for the user.
        int lid;
        boolean bol = db.consultaUsuario(uName, Hasher.hexStringToByteArray(psswd));
        long lon;
        if (bol) {
            lid = db.insertaLoginAttempt(uName, rHost);
            if(skey != null)
            lon = db.insertaAttemptSucceded(lid, skey);
            map = new TreeMap<>();
            map.put(uName, lid);
        }else{
            lid = -1;
            lon = -1;
        }
        return map;
    }

    public static void shedService(IOHandler ioHandler, Context context){
        JSONObject json;
        try {
            byte[] chunk = ioHandler.handleIncommingMessage();
            json = new JSONObject(new String(chunk));
            Votaciones v = new Votaciones(context);
            JSONObject row;
            JSONArray jarr = json.getJSONArray("content");
            Log.d("RESCUER","GOT HERE WITH: " + jarr.toString());
            switch (json.getInt("action")){
                case 1:
                    for(int i=0; i<jarr.length(); i++){
                        row = jarr.getJSONObject(i);
                        Log.d("Mayunia","Inserting: " + jarr.toString());
                        Log.d("Mayunia","Inserted: " + v.insertaVoto2(Hasher.hexStringToByteArray(row.getString("idVoto")),
                                row.getInt("idPerfil"),
                                row.getString("voto"),
                                row.getString("pregunta")));
                    }
                    break;
                case 2:
                    for(int i=0; i<jarr.length(); i++){
                        row = jarr.getJSONObject(i);
                        v.insertaParticipante(row.getString("boleta"),
                                row.getString("perfil"),
                                row.getString("escuela"));
                    }break;
            }
        } catch (JSONException | IOException e) {
            e.printStackTrace();
        }
    }
}
