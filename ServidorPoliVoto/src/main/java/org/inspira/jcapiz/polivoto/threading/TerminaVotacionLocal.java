package org.inspira.jcapiz.polivoto.threading;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import org.inspira.jcapiz.polivoto.database.Votaciones;
import org.inspira.jcapiz.polivoto.proveedores.ProveedorDeRecursos;
import org.inspira.jcapiz.polivoto.seguridad.Hasher;
import org.inspira.jcapiz.polivoto.seguridad.MD5Hash;
import org.inspira.jcapiz.polivoto.servicios.MiServicio;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;

import resumenresultados.shared.ResultadoVotacion;

/**
 * Created by jcapiz on 7/12/15.
 */
public class TerminaVotacionLocal extends AsyncTask<String,String,String> {

    private static final String RUTA = Environment.getExternalStorageDirectory().getAbsolutePath()+"/PoliVoto/Resultados";
    SecretKey secretKey;
    private Context ctx;

    public interface OnFinishVL{
        void success();
        void error(String message);
    }

    private OnFinishVL onFinishVL;

    public void setOnFinishVL(OnFinishVL onFinishVL) {
        this.onFinishVL = onFinishVL;
    }

    public TerminaVotacionLocal(Context ctx) throws NoSuchAlgorithmException {
        this.ctx = ctx;
        KeyGenerator keyGen;
        keyGen = KeyGenerator.getInstance("AES");
        keyGen.init(128); // por ejemplo
        secretKey = keyGen.generateKey();
    }

    private byte[] cifrar(byte[] materiaPrima) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException {
        Cipher cipher;
        cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.ENCRYPT_MODE, secretKey);
        return cipher.doFinal(materiaPrima);
    }

    private byte[] cifrarConPubKey(byte[] materiaPrima){
        byte[] resultado = null;
        try {
            DataInputStream keyfis = new DataInputStream(ctx.getAssets().open("key/pubTutska.der"));
            byte[] encKey = new byte[keyfis.available()];
            keyfis.read(encKey);
            keyfis.close();
            X509EncodedKeySpec pubKeySpec = new X509EncodedKeySpec(encKey);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            PublicKey pub = keyFactory.generatePublic(pubKeySpec);
            Cipher cipher;
            cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
            cipher.init(Cipher.ENCRYPT_MODE, pub);
            resultado = cipher.doFinal(materiaPrima);
        } catch (IOException | InvalidKeyException | NoSuchPaddingException | NoSuchAlgorithmException | InvalidKeySpecException | BadPaddingException | IllegalBlockSizeException e) {
            e.printStackTrace();
        }
        return resultado;
    }

    @Override
    protected String doInBackground(String... args){
        File f = new File(Environment.getExternalStorageDirectory(), "PoliVoto");
        if (!f.exists()) {
            f.mkdirs();
        }
        f = new File(Environment.getExternalStorageDirectory() + "/PoliVoto", "Resultados");
        if (!f.exists()) {
            f.mkdirs();
        }
        String result;
        final Votaciones v = new Votaciones(ctx);
        int idVotacion = v.obtenerIdVotacionActual();
        String[] rows = v.consultaVoto(idVotacion);
        String[] settings = v.quienesHanParticipado(idVotacion);
        String[] votando = v.consultaVotando(idVotacion);
        String[] logs = v.obtenerLog();
        try {
            byte[][] votosCifrados = new byte[rows.length][];
            byte[][] participantesCifrados = new byte[settings.length][];
            byte[][] votandoCifrados = new byte[votando.length][];
            byte[][] logsCifrados = new byte[logs.length][];
            for (int index = 0; index < rows.length; index++) {
                votosCifrados[index] = cifrar(rows[index].getBytes());
                Log.e("Capiz", rows[index]);
            }
            Log.e("Capiz", "Tenemos " + rows.length + " votos.");
            for (int index = 0; index < settings.length; index++) {
                participantesCifrados[index] = cifrar(settings[index].getBytes());
            }
            for (int index = 0; index < votando.length; index++) {
                votandoCifrados[index] = cifrar(votando[index].getBytes());
            }
            for (int index = 0; index < logs.length; index++) {
                logsCifrados[index] = cifrar(logs[index].getBytes());
            }
            ResultadoVotacion resultadoFinalVotos = new ResultadoVotacion(votosCifrados);
            ResultadoVotacion resultadoFinalParticipantes = new ResultadoVotacion(participantesCifrados);
            ResultadoVotacion resultadoFinalVotando = new ResultadoVotacion(votandoCifrados);
            ResultadoVotacion resultadoFinalLogs = new ResultadoVotacion(logsCifrados);
            try {
                ObjectOutputStream salidaArchivo = new ObjectOutputStream(new FileOutputStream(
                        RUTA+"/"+v.obtenerTituloVotacionActual()+".pvt"));
                salidaArchivo.writeObject(resultadoFinalVotos);
                salidaArchivo.writeObject(resultadoFinalParticipantes);
                salidaArchivo.writeObject(resultadoFinalVotando);
                salidaArchivo.writeObject(resultadoFinalLogs);
                salidaArchivo.close();
                //v.terminarProceso();
                result = "Éxito al finalizar la votación!";
                DataOutputStream salida = new DataOutputStream(new FileOutputStream(new File(
                        RUTA+"/"+v.obtenerTituloVotacionActual()+"_Llave.bts")));
                salida.write(cifrarConPubKey(secretKey.getEncoded()));
                salida.close();
            } catch (IOException ex) {
                result = "Error al finalizar la votación:\nRevise los permisos de la app";
            }
            JSONObject json = new JSONObject();
            json.put("action", 11);
            json.put("id_votacion", v.obtenerUltimaVotacionGlobal());
            json.put("id_place", v.obtenerIdDeEscuela(ProveedorDeRecursos.obtenerRecursoString(ctx, "ubicacion")));
            FileInputStream fis = new FileInputStream(new File(
                    RUTA+"/"+v.obtenerTituloVotacionActual()+".pvt"
            ));
            DataInputStream entrada = new DataInputStream(fis);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            byte[] chunk = new byte[64];
            int length;
            while((length = entrada.read(chunk)) != -1)
                baos.write(chunk, 0, length);
            String hash = Hasher.bytesToString(baos.toByteArray());
            Log.e("TerminaVL", "Se leyeron " + baos.toByteArray().length + " bytes.");
            baos.close();
            v.guardarHashVotacion(idVotacion, new MD5Hash().makeHash(hash));
            json.put("contenido_cifrado", hash);
            fis.close();
            fis = new FileInputStream(new File(
                    RUTA+"/"+v.obtenerTituloVotacionActual()+"_Llave.bts"
            ));
            entrada = new DataInputStream(fis);
            baos = new ByteArrayOutputStream();
            while((length = entrada.read(chunk)) != -1)
                baos.write(chunk, 0, length);
            json.put("secreto_cifrado", Hasher.bytesToString(baos.toByteArray()));
            baos.close();
            fis.close();
            String hst = ProveedorDeRecursos.obtenerRecursoString(ctx, "ultimo_consultor_activo");
            Log.e("TerminaVL", "Hst --> " + hst);
            ContactaConsultor contacto = new ContactaConsultor(new ContactaConsultor.ResultadoContactoConsultor() {
                @Override
                public void hecho() {
                    /** Colocar en alto la bandera de "Sincronizado" correspondiente a la votacion. **/
                    v.sincronizaVotacion(v.obtenerTituloVotacionActual());
                }

                @Override
                public void percance(String mensaje) {

                }
            }, hst, json.toString());
            contacto.start();
//            contacto.join();
            v.close();
        } catch (JSONException | InvalidKeyException | NoSuchAlgorithmException | NoSuchPaddingException | IllegalBlockSizeException | BadPaddingException | IOException e) {
            e.printStackTrace();
            result = (e.getMessage());
        }
        return result;
    }

    @Override
    protected void onPostExecute(String result){
        if("Éxito al finalizar la votación!".equals(result)) {
            Votaciones db = new Votaciones(ctx);
            db.finalizaVotacion(db.obtenerIdVotacionActual());
            Activity activity = ((MiServicio) ctx).getActivity();
            if (activity != null) activity.runOnUiThread(new MostrarToastUI(activity, "Gracias"));
            onFinishVL.success();
        }else{
            onFinishVL.error(result);
        }
    }
}