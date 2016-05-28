package org.inspira.jcapiz.polivoto.networking;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import org.inspira.jcapiz.polivoto.acciones.AccionesTablaVotacion;
import org.inspira.jcapiz.polivoto.acciones.DatosDeInicio;
import org.inspira.jcapiz.polivoto.actividades.Bienvenida;
import org.inspira.jcapiz.polivoto.database.Votaciones;
import org.inspira.jcapiz.polivoto.pojo.Escuela;
import org.inspira.jcapiz.polivoto.pojo.NombreParticipante;
import org.inspira.jcapiz.polivoto.pojo.Opcion;
import org.inspira.jcapiz.polivoto.pojo.Participante;
import org.inspira.jcapiz.polivoto.pojo.Perfil;
import org.inspira.jcapiz.polivoto.pojo.Pregunta;
import org.inspira.jcapiz.polivoto.pojo.PreguntaOpcion;
import org.inspira.jcapiz.polivoto.pojo.Votacion;
import org.inspira.jcapiz.polivoto.pojo.VotacionGlobal;
import org.inspira.jcapiz.polivoto.seguridad.Hasher;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

/**
 * Created by jcapiz on 7/04/16.
 */
public class ProporcionarInformacionDeInicio {

    private Context context;
    String mensaje;

    public ProporcionarInformacionDeInicio(Context context) {
        this.context = context;
    }

    private void prepararDatos() {
        Votaciones db = new Votaciones(context);
        SQLiteDatabase database = db.getReadableDatabase();
        Cursor c;
        try {
            JSONObject json = new JSONObject();
            json.put("action", 16);
            c = database.rawQuery("select * from Usuario", null);
            JSONArray jusuarios = new JSONArray();
            JSONObject jusuario;
            while (c.moveToNext()) {
                jusuario = new JSONObject();
                jusuario.put("idUsuario", c.getInt(c.getColumnIndex("idUsuario")));
                jusuario.put("Name", c.getString(c.getColumnIndex("Name")));
                jusuario.put("Psswd", c.getBlob(c.getColumnIndex("Psswd")));
                jusuarios.put(jusuario);
            }
            c.close();
            json.put("usuarios", jusuarios);
            c = database.rawQuery("select * from Perfil", null);
            JSONArray jperfiles = new JSONArray();
            JSONObject jperfil;
            while (c.moveToNext()) {
                jperfil = new JSONObject();
                jperfil.put("idPerfil", c.getInt(c.getColumnIndex("idPerfil")));
                jperfil.put("perfil", c.getString(c.getColumnIndex("perfil")));
                jperfiles.put(jperfil);
            }
            c.close();
            json.put("perfiles", jperfiles);
            SharedPreferences prefs = context.getSharedPreferences(Bienvenida.class.getName(),
                    Context.MODE_PRIVATE);
            JSONObject jvotacion = db.obtenerDatosDeVotacionActual();
            c = database.rawQuery("select * from Pregunta where idVotacion = CAST(? as INTEGER)",
                    new String[]{String.valueOf(json.getJSONObject("votacion").getInt("idVotacion"))});
            JSONArray jpreguntas = new JSONArray();
            JSONObject jpregunta;
            Cursor x;
            JSONArray jopciones;
            JSONObject jopcion;
            while (c.moveToNext()) {
                jpregunta = new JSONObject();
                jpregunta.put("idPregunta", c.getInt(c.getColumnIndex("idPregunta")));
                jpregunta.put("Pregunta", c.getString(c.getColumnIndex("Pregunta")));
                x = database.rawQuery("select Opcion.idOpcion, Reactivo from PreguntaOpcion " +
                                "join Opcion using(idOpcion) where idPregunta = " +
                                "CAST(? as INTEGER)",
                        new String[]{String.valueOf(c.getInt(c.getColumnIndex("idPregunta")))});
                jopciones = new JSONArray();
                while (x.moveToNext()) {
                    jopcion = new JSONObject();
                    jopcion.put("idOpcion", x.getInt(x.getColumnIndex("idOpcion")));
                    jopcion.put("Reactivo", x.getString(x.getColumnIndex("Reactivo")));
                    jopciones.put(jopcion);
                }
                x.close();
                jpregunta.put("opciones", jopciones);
                jpreguntas.put(jpregunta);
            }
            c.close();
            jvotacion.put("preguntas", jpreguntas);
            json.put("votacion", jvotacion);
            String tituloActual = db.obtenerTituloVotacionActual();
            String escuela = prefs.getString("ubicacion", "NaN");
            c = database.rawQuery("select * from Escuela where Nombre like ?",
                    new String[]{escuela});
            c.moveToFirst();
            JSONObject jescuela = new JSONObject();
            jescuela.put("idEscuela", c.getInt(c.getColumnIndex("idEscuela")));
            jescuela.put("Nombre", escuela);
            jescuela.put("latitud", c.getFloat(c.getColumnIndex("latitud")));
            jescuela.put("longitud", c.getFloat(c.getColumnIndex("longitud")));
            c.close();
            json.put("escuela", jescuela);
            c = database.rawQuery("select idVotacion from Votacion where Titulo like ? " +
                            "order by Fecha_Inicio desc limit 1",
                    new String[]{tituloActual});
            c.moveToFirst();
            int idVotacionActual = c.getInt(0);
            c.close();
            c = database.rawQuery("select Participante.Boleta, idPerfil, idEscuela, " +
                    "Fecha_Registro, BoletaHash from " +
                    "(select Boleta from (select idPregunta from Pregunta where " +
                    "idVotacion = CAST(? as INTEGER))a" +
                    " join Participante_Pregunta using(idPregunta) group by(Boleta)) b" +
                    " join Participante using(Boleta)" +
                    "", new String[]{String.valueOf(idVotacionActual)});
            JSONArray jparticipantes = new JSONArray();
            JSONObject jparticipante;
            JSONArray jnombres;
            JSONObject jnombre;
            while (c.moveToNext()) {
                jparticipante = new JSONObject();
                jparticipante.put("Boleta", c.getString(c.getColumnIndex("Boleta")));
                jparticipante.put("idPerfil", c.getString(c.getColumnIndex("idPerfil")));
                jparticipante.put("idEscuela", c.getString(c.getColumnIndex("idEscuela")));
                jparticipante.put("Fecha_Registro", c.getString(c.getColumnIndex("Fecha_Registro")));
                jparticipante.put("BoletaHash", c.getString(c.getColumnIndex("Boleta")));
                x = database.rawQuery("select * from NombreParticipante where Boleta like ?",
                        new String[]{jparticipante.getString("Boleta")});
                jnombres = new JSONArray();
                while (x.moveToNext()) {
                    jnombre = new JSONObject();
                    jnombre.put("Nombre", x.getString(x.getColumnIndex("Nombre")));
                    jnombre.put("ApPaterno", x.getString(x.getColumnIndex("ApPaterno")));
                    jnombre.put("ApMaterno", x.getString(x.getColumnIndex("ApMaterno")));
                    jnombre.put("Boleta", x.getString(x.getColumnIndex("Boleta")));
                    jnombres.put(jnombre);
                }
                x.close();
                jparticipante.put("nombres", jnombres);
                jparticipantes.put(jparticipante);
            }
            c.close();
            json.put("participantes", jparticipantes);
            mensaje = json.toString();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void recibirDatos(JSONObject json) {
        Hasher hasher = new Hasher();
        try {
            JSONObject jescuela = json.getJSONObject("escuela");
            Escuela escuela = new Escuela(json.getInt("idEscuela"));
            escuela.setNombre(jescuela.getString("Nombre"));
            DatosDeInicio.agregaEscuela(context, escuela);
            Perfil perfil;
            JSONArray jperfiles = json.getJSONArray("perfiles");
            JSONObject jperfil;
            for (int i = 0; i < jperfiles.length(); i++) {
                jperfil = jperfiles.getJSONObject(i);
                perfil = new Perfil(jperfil.getInt("idPerfil"));
                perfil.setPerfil(jperfil.getString("perfil"));
                DatosDeInicio.agregarPerfil(context, perfil);
            }
            Participante participante;
            JSONArray jparticipantes = json.getJSONArray("participantes");
            JSONObject jparticipante;
            JSONArray jnombres;
            JSONObject jnombre;
            NombreParticipante nombreParticipante;
            for (int i = 0; i < jparticipantes.length(); i++) {
                jparticipante = jparticipantes.getJSONObject(i);
                participante = new Participante();
                participante.setBoleta(jparticipante.getString("Boleta"));
                participante.setIdEscuela(jparticipante.getInt("idEscuela"));
                participante.setFechaRegistro(jparticipante.getString("Fecha_Registro"));
                participante.setBoletaHash(
                        hasher.stringParse(jparticipante.getString("BoletaHash")));
                DatosDeInicio.agregarParticipante(context, participante);
                jnombres = jparticipante.getJSONArray("nombres");
                for (int j = 0; j < jnombres.length(); j++) {
                    jnombre = jnombres.getJSONObject(j);
                    nombreParticipante = new NombreParticipante(jnombre.getString("Boleta"));
                    nombreParticipante.setNombre(jnombre.getString("Nombre"));
                    nombreParticipante.setApPaterno(jnombre.getString("ApPaterno"));
                    nombreParticipante.setApMaterno(jnombre.getString("ApMaterno"));
                    DatosDeInicio.agregarNombreParticipante(context, nombreParticipante);
                }
            }
            JSONObject jvotacion = json.getJSONObject("votacion");
            Votacion votacion = new Votacion(jvotacion.getInt("idVotacion"));
            votacion.setIdEscuela(jvotacion.getInt("idEscuela"));
            votacion.setTitulo(jvotacion.getString("Titulo"));
//            votacion.setFechaInicio(jvotacion.getString("Fecha_Inicio"));
//            votacion.setFechaFin(jvotacion.getString("Fecha_Fin"));
            AccionesTablaVotacion.agregaVotacion(context, votacion);
            JSONArray jpreguntas = jvotacion.getJSONArray("preguntas");
            JSONObject jpregunta;
            Pregunta pregunta;
            JSONArray jopciones;
            JSONObject jopcion;
            Opcion opcion;
            PreguntaOpcion preguntaOpcion;
            for (int i = 0; i < jpreguntas.length(); i++) {
                jpregunta = jpreguntas.getJSONObject(i);
                pregunta = new Pregunta(jpregunta.getInt("idPregunta"));
                pregunta.setIdVotacion(votacion.getId());
                pregunta.setEnunciado(jpregunta.getString("Pregunta"));
                AccionesTablaVotacion.agregarPregunta(context, pregunta);
                jopciones = jpregunta.getJSONArray("opciones");
                for (int j = 0; j < jopciones.length(); j++) {
                    jopcion = jopciones.getJSONObject(j);
                    opcion = new Opcion(jopcion.getInt("idOpcion"));
                    opcion.setReactivo(jopcion.getString("Reactivo"));
                    AccionesTablaVotacion.agregarOpcion(context, opcion);
                    preguntaOpcion = new PreguntaOpcion();
                    preguntaOpcion.setIdOpcion(opcion.getId());
                    preguntaOpcion.setIdPregunta(pregunta.getId());
                    AccionesTablaVotacion.agregarPreguntaOpcion(context, preguntaOpcion);
                }
            }
            int sincronizado = jvotacion.getInt("Sincronizado");
            if (sincronizado != -1) {
                VotacionGlobal votacionGlobal = new VotacionGlobal(votacion.getId());
                votacionGlobal.setSincronizado(sincronizado != 0);
                AccionesTablaVotacion.agregarVotacionGlobal(context, votacionGlobal);
            }
            AccionesTablaVotacion.agregarVotacionRespaldo(context, votacion.getId());
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void enviaInformacionDeInicio(String host, int token){
        prepararDatos();
        try{
            Socket socket = new Socket(host, 23543);
            IOHandler ioHandler = new IOHandler(
                    new DataInputStream(socket.getInputStream()),
                    new DataOutputStream(socket.getOutputStream())
            );
            ioHandler.writeInt(token);
            ioHandler.sendMessage(mensaje.getBytes());
            ioHandler.close();
        }catch(IOException e){
            e.printStackTrace();
        }
    }
}
