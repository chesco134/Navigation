package org.inspira.jcapiz.polivoto.threading;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import org.inspira.jcapiz.polivoto.acciones.AccionesTablaVotacion;
import org.inspira.jcapiz.polivoto.pojo.Pregunta;
import org.inspira.jcapiz.polivoto.pojo.Votacion;
import org.inspira.jcapiz.polivoto.proveedores.ProveedorDeRecursos;
import org.inspira.jcapiz.polivoto.actividades.Bienvenida;
import org.inspira.jcapiz.polivoto.actividades.ConfiguraParticipantes;
import org.inspira.jcapiz.polivoto.actividades.IntercambioDeSecretos;
import org.inspira.jcapiz.polivoto.database.Votaciones;
import org.inspira.jcapiz.polivoto.database.acciones.AccionesTablaPerfiles;
import org.inspira.jcapiz.polivoto.database.acciones.AccionesTablaUsuario;
import org.inspira.jcapiz.polivoto.networking.IOHandler;
import org.inspira.jcapiz.polivoto.pojo.DatosDeAccionCliente;
import org.inspira.jcapiz.polivoto.pojo.UsuarioActivo;
import org.inspira.jcapiz.polivoto.proveedores.ProveedorDeTiempoRemoto;
import org.inspira.jcapiz.polivoto.seguridad.Hasher;
import org.inspira.jcapiz.polivoto.seguridad.MD5Hash;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TreeMap;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;

/**
 * Created by jcapiz on 28/11/15.
 */
public class AsistenteDeVotacion extends Thread {

    private static final long INTERVALO_ACTIVIDAD = (long)(1.5 * 60000);
    private IOHandler ioHandler;
    private Context context;
    private String rHost;
    private static volatile DatosDeAccionCliente addc;
    private static volatile List<String> hostsPostulados;
    private static Map<Integer,UsuarioActivo> usuariosActivos;

    private UsuarioActivo segundoVocal;
    private int idAttempt;
    private Votaciones db;
    private byte[] chunk;
    private Cipher cip;
    private int resp;
    private InteraccionConActividad agenteDeInteraccion;

    public AsistenteDeVotacion(InputStream entrada, OutputStream salida){
        ioHandler = new IOHandler(new DataInputStream(entrada), new DataOutputStream(salida));
        if(hostsPostulados == null)
            hostsPostulados = new ArrayList<>();
        if(addc == null) {
            addc = new DatosDeAccionCliente();
            addc.setUsrName("Administrador");
            /*
            String str = "";
            try {
                NetworkInterface nif = NetworkInterface.getByName("wlan0");
                Enumeration<InetAddress> addrs = nif.getInetAddresses();
                while (addrs.hasMoreElements()) {
                    InetAddress addr = addrs.nextElement();
                    str = str.concat(addr.toString() + "\n");
                }
            }catch(NullPointerException | IOException e){
                e.printStackTrace();
                Log.d("Asistente", "No estamos conectados a una red");
            }catch(ArrayIndexOutOfBoundsException e){
                e.printStackTrace();
                str = ("/192.168.43.1");
            }
            String[] arrs = str.split("/");
            String mAddr = arrs[arrs.length - 1];
            */
        }
    }

    public interface InteraccionConActividad{
        void actualizaConteoDeParticipantes();
    }

    public void setAgenteDeInteraccion(InteraccionConActividad agenteDeInteraccion) {
        this.agenteDeInteraccion = agenteDeInteraccion;
    }

    public void setContext(Context context){
        this.context = context;
    }

    public void setRHost(String host){
        Log.d("Asistente", "Host set: " + host);
        rHost = host.split("/")[1].split(":")[0];
        ioHandler.setHost(rHost);
    }

    @Override
    public void run(){
        try{
            String categoriaStaff = null;
            // Espera por el primer byte y revísalo a su llegada.
            int cByte = ioHandler.readInt();
            Log.d("SocketHandler", "Guten tag2 " + rHost + "\t" + cByte);
            db = new Votaciones(context);
            // Data collector.
            if(cByte == -2){
                IntercambioDeSecretos.shedService(ioHandler, context);
            }
            if(ProveedorDeRecursos.isSecureOptionSelected(context))
                categoriaStaff = IntercambioDeSecretos.efectuaIntercambio(ioHandler, context, rHost);
            else{
                try {
                    Map<String, Integer> map = IntercambioDeSecretos.userCheckup(context, new String(ioHandler.handleIncommingMessage()), rHost, null);
                    categoriaStaff = map.keySet().toArray(new String[]{})[0];
                    ioHandler.writeInt(map.get(categoriaStaff));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            if( cByte == -1 && categoriaStaff != null) {
                if("Consultor".equals(categoriaStaff))
                    synchronized (this) {
                        try{
                            wait(900);
                        }catch(InterruptedException e){
                            e.printStackTrace();
                        }
                        new ContactaConsultor(resInicio, rHost, "{\"action\": 10}").start();
                    }
            }
            else if(AccionesTablaUsuario.revisaValidezDeToken(context, cByte)){
                // Read the first byte to know which sKey to load so you can decipher the String.
                idAttempt = (cByte);
//                actualizaActividadDeUsuario(idAttempt);
                SecretKeySpec sk = null;
                chunk = ioHandler.handleIncommingMessage(); // Obtenemos los bytes cifrados.
                try {
                    String jstr;
                    if(ProveedorDeRecursos.isSecureOptionSelected(context)) {
                        sk = new SecretKeySpec(db.obtenerSKeyEncoded(idAttempt), "AES");
                        cip = Cipher.getInstance("AES");
                        cip.init(Cipher.DECRYPT_MODE, sk);
                        jstr = new String(cip.doFinal(chunk));
                        cip.init(Cipher.ENCRYPT_MODE, sk);
                    }else{
                        jstr = new String(chunk);
                    }
                    Log.d("AsistenteDeVotacion", jstr);
                    JSONObject json = new JSONObject(jstr);
                    // En el objecto JSON esperamos encontrar al usuario y a sus intenciones
                    db.insertaUserAction(idAttempt, jstr);
                    // La tarea a realizar est&aacute; indicada por un entero.
                    int requestedAction = json.getInt("action");
                    // 1 Postulate me!.
                    // 2 Pide validación de boleta.
                    // 3 Entrega voto.
                    // 4 Pide conteo de votos.
                    // 5 Pide título de votación.
                    // 6 Pide preguntas de votación.
                    // 7 Pide fecha de inicio de votación.
                    // 8 Pide fecha de término de votación.
                    // 9 Alta participante.
                    // 10 Pide perfiles.
                    // 11 Pide preguntas para participante.
                    // 12 Participante contestó pregunta.
                    // 13 Pide opciones de pregunta.
                    // 14 Pide agente externo validación de boleta.
                    // 15 Pide cantidad de votos al momento.
                    // 16 Recibir información de inicio.
                    // 17 Atención a sincronía de tiempos
                    switch(requestedAction){
                        case 1: // 1 Postulate me!.
                            action1();
                            break;
                        case 2:// 2 Pide validación de boleta.
                            action2(json);
                            break;
                        case 3: // 3 Entrega voto.
                            action3(json);
                            break;
                        case 4: // 4 Pide conteo de votos.
                            action4(json);
                            break;
                        case 5:  // 5 Pide título de votación.
                            action5();
                            break;
                        case 6:  // 6 Pide preguntas de votación.
                            action6();
                            break;
                        case 7:  // 7 Pide fecha de inicio de votación.
                            action7();
                            break;
                        case 8:  // 8 Pide fecha de término de votación.
                            action8();
                            break;
                        case 9: // 9 Alta participante.
                            action9(json);
                            break;
                        case 10: // 10 Pide perfiles.
                            action10();
                            break;
                        case 11: // 11 Pide preguntas para participante.
                            action11(json);
                            break;
                        case 12:  // 12 Participante contestó pregunta.
                            action12(json);
                            break;
                        case 13:  // 13 Pide opciones de pregunta.
                            action13(json);
                            break;
                        case 14: // 14 Pide agente externo validación de boleta.
                            action14(json);
                            break;
                        case 15: // 15 Pide cantidad de votos al momento.
                            action15();
                            break;
                        case 16: // 16 Recibir información de inicio.
                            action16(json);
                            break;
                        case 17: // 17 Atención a sincronía de tiempos
                            action17(json);
                            break;
                        default:
                            // Cuenta como heart beat.
                    }
                } catch (NoSuchAlgorithmException | NoSuchPaddingException | BadPaddingException | JSONException | InvalidKeyException | IllegalBlockSizeException e) {
                    e.printStackTrace();
                }
            }
        }catch(IOException e){//this can be very funy
            Log.d("AsistenteV", "Vino y se fué: " + rHost);
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            Log.d("IllegalAccess", "Alguien no identificado intentó acceder, con id: " + idAttempt + " y host: " + rHost);
            e.printStackTrace();
        }finally {
            try {
                ioHandler.close();
            }catch(IOException e){
                Log.e("From finally","Error closing everything?");
            }
            Log.d("PoliVoto", "Fin de la atención");
        }
    }

    private void action1() throws IOException, BadPaddingException, IllegalBlockSizeException, JSONException {
        if("Participante".equals(db.obtenerUsuarioPorIdAttempt(idAttempt))){
            if(!hostsPostulados.contains(rHost)) hostsPostulados.add(rHost);
            // Participante may need to wait until a new Capturista contacts it
        }else if ("Capturista".equals(db.obtenerUsuarioPorIdAttempt(idAttempt))) {
            Log.d("HostsPostulados",String.valueOf(hostsPostulados.size()));
            while (hostsPostulados.size() == 0);
            JSONObject json = new JSONObject();
            json.put("nHost", hostsPostulados.remove(hostsPostulados.size() - 1));
            json.put("key", Hasher.bytesToString(db.obtenerKeyByHost(json.getString("nHost"), "Participante")));
            if( ProveedorDeRecursos.isSecureOptionSelected(context))
                chunk = cip.doFinal(json.toString().getBytes());
            else
                chunk = json.toString().getBytes();
            ioHandler.sendMessage(chunk);
            ioHandler.close();
        }
    }

    // Pide validación de Boleta
    private void action2(JSONObject json) throws IOException, BadPaddingException, IllegalBlockSizeException, JSONException {
        // Las siguientes dos líneas deberían ser implementación del servicio.
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        Boolean usarMatricula = sharedPref.getBoolean(ConfiguraParticipantes.USAR_MATRICULA_KEY, false);
        String boleta = json.getString("boleta");
        resp = db.consultaExistenciaBoleta(boleta) ? 1 : 0;
        Log.d("ValidacionDeBoleta", "¿existe "+boleta+"? " + (resp != 0 ? "Sí" : "No") + "\n¿Cargamos matrícula? " + (usarMatricula ? "Sí" : "No"));
        // Termina proceso de validación local.
        // Debemos revisar si se está participando en una votación global y validar también.
        // Debemos contactar a la entidad encargada de hacer la validación remota y esperar.
        if(db.isVotacionActualGlobal() && resp == 0) {
            String host = ProveedorDeRecursos.obtenerRecursoString(context, "ultimo_consultor_activo");
            JSONObject j = new JSONObject();
            j.put("boleta", new MD5Hash().makeHash(boleta));
            j.put("action", 1);
            j.put("host", ProveedorDeRecursos.obtenerRecursoString(context, "ultimo_consultor_activo"));
            j.put("title", db.obtenerTituloVotacionActual());
            j.put("id_votacion", db.obtenerUltimaVotacionGlobal());
            Socket socket = new Socket(host, 5010);
            DataInputStream entrada = new DataInputStream(socket.getInputStream());
            DataOutputStream salida = new DataOutputStream(socket.getOutputStream());
            IOHandler ioh = new IOHandler(entrada, salida);
            ioh.sendMessage(j.toString().getBytes());
            Log.d("Validacion","Data sent (" + j.toString() + "), waiting for response...");
            byte[] bts = ioh.handleIncommingMessage();
            j = new JSONObject(new String(bts));
            Log.d("Validacion", "Response has arrived (" + j.toString() + ")");
            resp = j.getInt("veredicto");
            socket.close();
        }
        if(resp == 0 && !usarMatricula){
            action9(json);
            Log.d("ValidacionDeBoleta", "Insertando participante... " + resp);
        }else if(usarMatricula && resp == 0)
            resp = 2;
        json = new JSONObject();
        json.put("veredicto", resp);
        if(resp != 0){
            String perfil = AccionesTablaPerfiles.obtenerPerfilParticipante(context, boleta);
            json.put("perfil", perfil);
        }
        if( ProveedorDeRecursos.isSecureOptionSelected(context))
            chunk = cip.doFinal(json.toString().getBytes());
        else
            chunk = json.toString().getBytes();
        ioHandler.sendMessage(chunk);
        ioHandler.close();
    }

    // Entrega voto
    private void action3(JSONObject json) throws IOException, BadPaddingException, IllegalBlockSizeException, JSONException {
        String pregunta = json.getString("pregunta");
        int idVotacion = db.obtenerIdVotacionActual();
        String perfil = json.getString("perfil");
        String voto = json.getString("voto");
        Log.e("Horror!!", "Llegó: " + json.toString());
        if (db.insertaVoto(new MD5Hash().makeHashForSomeBytes(json.getString("idVoto")), idVotacion, perfil, voto, idAttempt, db.obtenerIdPregunta(pregunta, idVotacion)) != -1){
            resp = 1;
        }else
            resp = 0;
        if( ProveedorDeRecursos.isSecureOptionSelected(context))
            chunk = cip.doFinal(String.valueOf(resp).getBytes());
        else
            chunk = String.valueOf(resp).getBytes();
        ioHandler.sendMessage(chunk);
        ioHandler.close();
    }

    // Pide conteo de votos
    private void action4(JSONObject json) throws IOException, BadPaddingException, IllegalBlockSizeException, JSONException {
        /**********************************************************************
         *
         * 		El Consultor pregunta con un título cuales son los resultados
         * 	que desea obtener, así, se debe llamar a "consultarVotos", pasando
         * 	el nombre de la pregunta como parámetro de búsqueda y armando una
         * 	lista ligada con los títulos de las opciones @ número de votos.
         *
         ************************************************************************/
        List<Pregunta> preguntas = new ArrayList<>();
        Collections.addAll(preguntas, db.obtenerPreguntasVotacionActual());
        Votacion votacion = new Votacion();
        votacion.setPreguntas(preguntas);
        Pregunta pregunta = votacion.obtenerPregunta(votacion.buscarPregunta(json.getString("pregunta")));
        json = obtenerConteoDeVotos(pregunta.getId());
        if( ProveedorDeRecursos.isSecureOptionSelected(context))
            chunk = cip.doFinal(json.toString().getBytes());
        else
            chunk = json.toString().getBytes();
        ioHandler.sendMessage(chunk);
        ioHandler.close();
    }

    // Pide título de votación.
    private void action5() throws IOException, BadPaddingException, IllegalBlockSizeException {
        String titulo = db.obtenerTituloVotacionActual();
        if( ProveedorDeRecursos.isSecureOptionSelected(context))
            chunk = cip.doFinal(titulo.getBytes());
        else
            chunk = titulo.getBytes();
        ioHandler.sendMessage(chunk);
        ioHandler.close();
    }

    // Pide preguntas de votación
    private void action6() throws JSONException, BadPaddingException, IllegalBlockSizeException, IOException {
        Pregunta preguntas[] = db.obtenerPreguntasVotacionActual();
        JSONArray jarr = new JSONArray();
        for(int i=preguntas.length - 1; i>=0;i--){
            jarr.put(i,preguntas[i].getEnunciado());
        }
        if( ProveedorDeRecursos.isSecureOptionSelected(context))
            chunk = cip.doFinal(jarr.toString().getBytes());
        else
            chunk = jarr.toString().getBytes();
        ioHandler.sendMessage(chunk);
        ioHandler.close();
    }

    // Pide fecha de inicio de votación.
    private void action7() throws JSONException, BadPaddingException, IllegalBlockSizeException, IOException {
        JSONObject datosDeVotacionActual = db.obtenerDatosDeVotacionActual();
        if(ProveedorDeRecursos.isSecureOptionSelected(context)){
            chunk = cip.doFinal(String.valueOf(datosDeVotacionActual.getLong("Fecha_Inicio")).getBytes());
        }else{
            chunk = String.valueOf(datosDeVotacionActual.getLong("Fecha_Inicio")).getBytes();
        }
        ioHandler.sendMessage(chunk);
        ioHandler.close();
    }

    // Pide fecha de término de votación.
    private void action8() throws JSONException, BadPaddingException, IllegalBlockSizeException, IOException {
        JSONObject datosDeVotacionActual = db.obtenerDatosDeVotacionActual();
        if(ProveedorDeRecursos.isSecureOptionSelected(context)){
            chunk = cip.doFinal(String.valueOf(datosDeVotacionActual.getLong("Fecha_Fin")).getBytes());
        }else{
            chunk = String.valueOf(datosDeVotacionActual.getLong("Fecha_Fin")).getBytes();
        }
        ioHandler.sendMessage(chunk);
        ioHandler.close();
    }

    // Alta participante.
    private void action9(JSONObject json) throws JSONException {
        String boleta = null;
        try{boleta = json.getString("boleta");}catch(JSONException e){}
        String perfil = null;
        try{perfil = json.getString("perfil");}catch(JSONException e){}
        String escuela = null;
        try{escuela = json.getString("escuela");}catch(JSONException e){}
        String nombre = null;
        try{nombre = json.getString("nombre");}catch(JSONException e){}
        String apPaterno = null;
        try{apPaterno = json.getString("ap_paterno");}catch(JSONException e){}
        String apMaterno = null;
        try{apMaterno = json.getString("ap_materno");}catch(JSONException e){}
        if(!db.insertaParticipante(boleta, perfil == null ? "" : perfil, escuela == null ? "" : escuela)){
            // Si no se proporcionan
            String ubicacion = ProveedorDeRecursos.obtenerRecursoString(context, "ubicacion");
            Log.d("Snorlax", ubicacion);
            resp = db.insertaParticipante(boleta, "NaN", ubicacion) ? 1 : -1;
        }
        if( nombre != null && apPaterno != null && apMaterno != null )
            if(db.insertaNombreParticipante(boleta, nombre, apPaterno, apMaterno) == -1 )
                resp = -1;
        if(resp != -1) {
            Pregunta[] pregs = db.obtenerPreguntasVotacionActual();
            for(Pregunta preg : pregs) { // Habilita cada pregunta para el participante.
                if( db.insertaParticipantePregunta(boleta, preg.getEnunciado()) == -1 )
                    resp = -1;
            }
            resp = 1;
        }
    }

    // Pide perfiles.
    public void action10() throws BadPaddingException, IllegalBlockSizeException, JSONException, IOException {
        String[] perfiles = db.obtenerPerfiles();
        JSONArray jsonArray = new JSONArray();
        for(int i=0; i<perfiles.length;i++)
            jsonArray.put(i,perfiles[i]);
        if(ProveedorDeRecursos.isSecureOptionSelected(context)){
            chunk = cip.doFinal(jsonArray.toString().getBytes());
        }else{
            chunk = jsonArray.toString().getBytes();
        }
        ioHandler.sendMessage(chunk);
        ioHandler.close();
    }

    // Pide preguntas para participante.
    public void action11(JSONObject json) throws JSONException, BadPaddingException, IllegalBlockSizeException, IOException {
        String boleta = json.getString("boleta");
        String[] pregsParticipante = db.consultaParticipantePreguntas(boleta);
        JSONArray jsonArray1 = new JSONArray();
        for(int i=pregsParticipante.length-1; i>=0;i--)
            jsonArray1.put(pregsParticipante[i]);
        Log.d("accion 11", "las preguntas para " + boleta + " son: " + jsonArray1.toString());
        if(ProveedorDeRecursos.isSecureOptionSelected(context)){
            chunk = cip.doFinal(jsonArray1.toString().getBytes());
        }else{
            chunk = jsonArray1.toString().getBytes();
        }
        ioHandler.sendMessage(chunk);
        ioHandler.close();
    }

    // Participante contestó pregunta
    public void action12(JSONObject json) throws JSONException {
        String boleta = json.getString("boleta");
        String pregunta = json.getString("pregunta");
        db.actualizaParticipantePregunta(boleta, pregunta);
        if(db.consultaParticipanteHaVotado(boleta)){
            agenteDeInteraccion.actualizaConteoDeParticipantes();
            Integer poblacion;
            poblacion = db.obtenerCantidadParticipantes(db.obtenerIdVotacionActual());
            ActualizaVotoEnConsultor envoy = new ActualizaVotoEnConsultor(db.grabHostForUserLoginAttempt(db.grabLastUserIdAttmptSucceded("Consultor")));
            envoy.setParticipantes(poblacion);
            envoy.start();
        }
    }

    // Consulta opciones pregunta
    public void action13(JSONObject json) throws IOException, JSONException, BadPaddingException, IllegalBlockSizeException {
        if(ProveedorDeRecursos.isSecureOptionSelected(context)){
            chunk = cip.doFinal(db.obtenerOpcionesPregunta(json.getString("pregunta")).toString().getBytes());
        }else{
            chunk = db.obtenerOpcionesPregunta(json.getString("pregunta")).toString().getBytes();
        }
        ioHandler.sendMessage(chunk);
        ioHandler.close();
    }

    public void action14(JSONObject json) throws BadPaddingException, IllegalBlockSizeException, IOException, JSONException {
        // Las siguientes dos líneas deben ser implementación del servicio.
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        Boolean usarMatricula = sharedPref.getBoolean(ConfiguraParticipantes.USAR_MATRICULA_KEY, false);
        String boleta = json.getString("boleta");
        Log.d("Dragon Slayer", "Estamos por comprobar una boleta extrangera! La boleta es: " + boleta);
        if(db.consultaRemotaExistenciaBoleta(boleta)){
            resp = 1;
        }else{
            resp = 0;
        }
        String perfil = db.obtenerPerfilDeUsuario(boleta);
        Log.d("ValidacionDeBoleta", "¿existe "+boleta+"? " + (resp != 0 ? "Sí" : "No") + "\n¿Cargamos matrícula? " + (usarMatricula ? "Sí" : "No"));
        if(usarMatricula && resp == 0)
            resp = 2;
        json = new JSONObject();
        json.put("veredicto", resp);
        json.put("perfil", perfil);
        if(ProveedorDeRecursos.isSecureOptionSelected(context)){
            chunk = cip.doFinal(json.toString().getBytes());
        }else{
            chunk = json.toString().getBytes();
        }
        ioHandler.sendMessage(chunk);
        ioHandler.close();
    }

    // 15 Pide cantidad de votos al momento.
    public void action15() throws IOException, BadPaddingException, IllegalBlockSizeException, JSONException {
        int cantidad = db.obtenerCantidadParticipantes(db.obtenerIdVotacionActual());
        // Actualmente el criterio para la decisión es que la hora actual sea menor a la de fin.
        SharedPreferences preferences = context.getSharedPreferences(Bienvenida.class.getName(), Context.MODE_PRIVATE);
        String ubicacion = preferences.getString("ubicacion", "NaN");
        Log.d("Rasmodius", "--> " + ubicacion);
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        Boolean usarMatricula = sharedPref.getBoolean(ConfiguraParticipantes.USAR_MATRICULA_KEY, false);
        Integer poblacion;
        if(usarMatricula)
            poblacion = db.obtenerTotalNumParticipantes(db.obtenerDatosDeVotacionActual().getInt("idVotacion"));
        else
            poblacion = db.obtenerCantidadParticipantes(db.obtenerIdVotacionActual());
        JSONObject json = new JSONObject();
        try{
            json.put("poblacion", poblacion.intValue());
            json.put("votos",cantidad);
            json.put("lugar", ubicacion);
            JSONObject datosDeVotacionActual = db.obtenerDatosDeVotacionActual();
            long tiempoFinal = datosDeVotacionActual.getBoolean("Soy_Propietario") ? datosDeVotacionActual.getLong("Fecha_Fin") - System.currentTimeMillis() : ProveedorDeTiempoRemoto.obtenerTiempoRemoto(context, db.isVotacionActualGlobal());
            json.put("tiempo_final", tiempoFinal );
            json.put("tiempo_inicial", datosDeVotacionActual.getLong("Fecha_Inicio"));
            json.put("tiempo_final_final", datosDeVotacionActual.getLong("Fecha_Fin"));
            json.put("titulo", datosDeVotacionActual.getString("Titulo"));
            json.put("t_salida", new Date().getTime());
        }catch(JSONException e){
            e.printStackTrace();
        }
        Log.e("VotosAlMomento", json.toString());
        if(ProveedorDeRecursos.isSecureOptionSelected(context)){
            chunk = cip.doFinal(json.toString().getBytes());
        }else{
            chunk = json.toString().getBytes();
        }
        ioHandler.sendMessage(chunk);
        ioHandler.close();
    }

    // Recibir información de inicio.
    private void action16(JSONObject json){

    }

    // Atención a sincronía de tiempos
    private void action17(JSONObject json) throws JSONException, IOException, BadPaddingException, IllegalBlockSizeException {
        json.put("t_salida", new Date().getTime());
        if(ProveedorDeRecursos.isSecureOptionSelected(context)){
            chunk = cip.doFinal(json.toString().getBytes());
        }else{
            chunk = json.toString().getBytes();
        }
        ioHandler.sendMessage(chunk);
        ioHandler.close();
        Log.d("Puts", "sent: " + json.toString());
    }

    public JSONObject obtenerConteoDeVotos(int idPregunta){
        JSONObject json = null;
        if(db == null)
            db = new Votaciones(context);
        try {
            Map<String, Integer> resultados = db.obtenerResultadosPorPregunta(idPregunta, db.obtenerIdVotacionActual());
            // Podría darse el caso en que mejor se cambie a ObjectOutputStream y se mande la lista ligada.
            JSONObject jsresp;
            JSONArray jarr = new JSONArray();
            for (String str : resultados.keySet()) {
                jsresp = new JSONObject();
                jsresp.put("reactivo", str);
                jsresp.put("cantidad", resultados.get(str));
                jarr.put(jsresp);
            }
            int idVotacion = db.obtenerIdVotacionActual();
            String[] perfiles = db.obtenerPerfilesDeVotacion(idVotacion);
            Map<String, Integer> resultadosPorPreguntaPorPerfil;
            JSONArray jPerfilesArray = new JSONArray();
            JSONObject jperfil;
            JSONArray jresultadosPorPreguntaPorPerfil;
            for (String perfil : perfiles) {
                resultadosPorPreguntaPorPerfil = db.obtenerResultadosPorPreguntaPorPerfil(idPregunta, perfil, idVotacion);
                jresultadosPorPreguntaPorPerfil = new JSONArray();
                for (String key : resultadosPorPreguntaPorPerfil.keySet()) {
                    jsresp = new JSONObject();
                    jsresp.put("reactivo", key);
                    jsresp.put("cantidad", resultadosPorPreguntaPorPerfil.get(key));
                    jresultadosPorPreguntaPorPerfil.put(jsresp);
                }
                jperfil = new JSONObject();
                jperfil.put("perfil", perfil);
                jperfil.put("resultados", jresultadosPorPreguntaPorPerfil);
                jPerfilesArray.put(jperfil);
            }
            json = new JSONObject();
            json.put("resultados_normal", jarr);
            json.put("resultados_por_perfil", jPerfilesArray);
        }catch(JSONException e){
            e.printStackTrace();
        }
        return json;
    }

    private ContactaConsultor.ResultadoContactoConsultor resInicio = new ContactaConsultor.ResultadoContactoConsultor() {
        @Override
        public void hecho() {
            ProveedorDeRecursos.guardarRecursoString(context, "ultimo_consultor_activo", rHost);
        }

        @Override
        public void percance(String mensaje) {}
    };
}