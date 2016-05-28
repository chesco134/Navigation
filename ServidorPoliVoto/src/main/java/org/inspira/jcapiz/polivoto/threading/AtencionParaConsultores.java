package org.inspira.jcapiz.polivoto.threading;

import android.content.Context;
import android.util.Log;

import org.inspira.jcapiz.polivoto.actividades.IntercambioDeSecretos;
import org.inspira.jcapiz.polivoto.database.Votaciones;
import org.inspira.jcapiz.polivoto.database.acciones.AccionesTablaUsuario;
import org.inspira.jcapiz.polivoto.networking.IOHandler;
import org.inspira.jcapiz.polivoto.pojo.Pregunta;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Map;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;

/**
 * Created by jcapiz on 14/04/16.
 */
public class AtencionParaConsultores extends Thread {

    private IOHandler ioHandler;
    private Context context;
    private String rHost;
    private Votaciones db;
    private Cipher cip;

    public AtencionParaConsultores(InputStream entrada, OutputStream salida){
        ioHandler = new IOHandler(new DataInputStream(entrada), new DataOutputStream(salida));
    }

    public void setContext(Context context){
        this.context = context;
    }

    public void setRHost(String host){
        rHost = host.split("/")[1].split(":")[0];
        ioHandler.setHost(rHost);
    }

    @Override
    public void run(){
        db = new Votaciones(context);
        try {
            int cByte = ioHandler.readInt();
            Log.d("Servidor", "Atendiendo al host: " + rHost + "\nNúmero entero recibido: " + cByte);
            String categoriaStaff;
            if( cByte == -1 && (categoriaStaff = IntercambioDeSecretos.efectuaIntercambio(ioHandler, context, rHost)) != null ) {
                if("Consultor".equals(categoriaStaff))
                synchronized (this) {
                    try{
                        wait(900);
                    }catch(InterruptedException e){
                        e.printStackTrace();
                    }
                    new ContactaConsultor(resInicio, rHost, "{\"action\": 9}").start();
                }
            }else if(AccionesTablaUsuario.revisaValidezDeToken(context, cByte)){
                atiendePeticion(cByte);
            }
            ioHandler.close();
        }catch(IOException e){
            Log.e("Servidor", "Error closing everything?");
            e.printStackTrace();
        }
        Log.d("Servidor", "Fin de la atención.");
    }

    private void atiendePeticion(int cByte) throws IOException {
        SecretKeySpec sk = new SecretKeySpec(db.obtenerSKeyEncoded(cByte),"AES");
        byte[] chunk = ioHandler.handleIncommingMessage(); // Obtenemos los bytes cifrados.
        try {
            cip = Cipher.getInstance("AES");
            cip.init(Cipher.DECRYPT_MODE, sk);
            String jstr = new String(cip.doFinal(chunk));
            JSONObject json = new JSONObject(jstr);
            // En el objecto JSON esperamos encontrar al usuario y a sus intenciones
            db.insertaUserAction(cByte, jstr);
            // La tarea a realizar est&aacute; indicada por un entero.
            int requestedAction = json.getInt("action");
            cip.init(Cipher.ENCRYPT_MODE, sk);
            switch(requestedAction){
                case 17: // Solicita votaciones existentes
                    action17();
                    break;
                case 18:
                    action18(json);
                    break;
                default:
            }
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | BadPaddingException | JSONException | InvalidKeyException | IllegalBlockSizeException e) {
            e.printStackTrace();
        }
    }

    private String armaDetallesVotacion(int idVotacion){
        String mensajeConDetallesDeVotacion = null;
        try {
            JSONObject datosDeVotacion = db.obtenerDatosDeVotacion(idVotacion);
            datosDeVotacion.put("lugar", db.obtenerNombreDeEscuela(datosDeVotacion.getInt("idEscuela")));
            datosDeVotacion.put("total_participantes", db.obtenerCantidadParticipantes(idVotacion));
            try{
                datosDeVotacion.put("porcentaje_de_participacion", (float)db.quienesHanParticipado(idVotacion).length/(float)db.obtenerTotalNumParticipantes(idVotacion));
            }catch(ArithmeticException a){ datosDeVotacion.put("porcentaje_de_participacion", (float) 0); }
            Pregunta[] preguntas = db.obtenerPreguntasDeVotacion(idVotacion);
            JSONArray jpreguntas = new JSONArray();
            JSONArray jopciones;
            JSONObject jpregunta;
            JSONObject jopcion;
            Map<String, Integer> resultadosPorPerfilPorPregunta;
            String[] perfilesDeVotacion = db.obtenerPerfilesDeVotacion(idVotacion);
            JSONArray jResultadosPorPerfil;
            JSONObject jResultadoPorPerfil;
            for(Pregunta pregunta : preguntas){
                jpregunta = new JSONObject();
                jpregunta.put("pregunta", pregunta.getEnunciado());
                Map<String,Integer> resultados = db.obtenerResultadosPorPregunta(pregunta.getId(), idVotacion);
                jopciones = new JSONArray();
                for(String str : resultados.keySet()) {
                    jopcion = new JSONObject();
                    jopcion.put("reactivo", str);
                    jopcion.put("cantidad", resultados.get(str));
                    jopciones.put(jopcion);
                }
                jpregunta.put("opciones", jopciones);
                jResultadosPorPerfil = new JSONArray();
                for(String perfil : perfilesDeVotacion) {
                    resultadosPorPerfilPorPregunta = db.obtenerResultadosPorPreguntaPorPerfil(pregunta.getId(), perfil, idVotacion);
                    jResultadoPorPerfil = new JSONObject();
                    jResultadoPorPerfil.put("perfil", perfil);
                    jopciones = new JSONArray();
                    for(String key : resultadosPorPerfilPorPregunta.keySet()){
                        jopcion = new JSONObject();
                        jopcion.put("reactivo", key);
                        jopcion.put("cantidad", resultadosPorPerfilPorPregunta.get(key));
                        jopciones.put(jopcion);
                    }
                    jResultadoPorPerfil.put("opciones", jopciones);
                    jResultadosPorPerfil.put(jResultadoPorPerfil);
                }
                jpregunta.put("resultados_perfiles", jResultadosPorPerfil);
                jpreguntas.put(jpregunta);
            }
            datosDeVotacion.put("preguntas", jpreguntas);
            String[] quienesHanParticipado = db.quienesHanParticipado(idVotacion);
            JSONArray quienesParticiparon = new JSONArray();
            for(String participante : quienesHanParticipado)
                quienesParticiparon.put(participante);
            datosDeVotacion.put("quienes_participaron", quienesParticiparon);
            mensajeConDetallesDeVotacion = datosDeVotacion.toString();
        } catch (JSONException ignore){ ignore.printStackTrace(); }
        return mensajeConDetallesDeVotacion;
    }

    // Solicita contenido.
    private void action18(JSONObject json) throws IOException, JSONException, BadPaddingException, IllegalBlockSizeException {
        byte[] chunk = cip.doFinal(String.valueOf(armaDetallesVotacion(json.getInt("idVotacion"))).getBytes());
        ioHandler.sendMessage(chunk);
        ioHandler.close();
    }

    // Solicita votaciones.
    private void action17() throws JSONException, BadPaddingException, IllegalBlockSizeException, IOException {
        JSONArray jelementos = db.obtenerVotacionesConcluidas();
        byte[] chunk = cip.doFinal(jelementos.toString().getBytes());
        ioHandler.sendMessage(chunk);
        ioHandler.close();
    }

    private ContactaConsultor.ResultadoContactoConsultor resInicio = new ContactaConsultor.ResultadoContactoConsultor() {
        @Override
        public void hecho() {}

        @Override
        public void percance(String mensaje) {}
    };
}
