package org.inspira.jcapiz.polivoto.actividades;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.util.Log;

import org.inspira.jcapiz.polivoto.R;
import org.inspira.jcapiz.polivoto.database.Votaciones;
import org.inspira.jcapiz.polivoto.pojo.ProcesoDisponible;
import org.inspira.jcapiz.polivoto.pojo.Votacion;
import org.inspira.jcapiz.polivoto.proveedores.ProveedorDeMarshalling;
import org.inspira.jcapiz.polivoto.proveedores.ProveedorDeRecursos;
import org.inspira.jcapiz.polivoto.proveedores.ProveedorSnackBar;
import org.inspira.jcapiz.polivoto.threading.ContactaConsultor;
import org.inspira.jcapiz.polivoto.threading.MostrarSnackUI;
import org.inspira.jcapiz.polivoto.threading.MostrarToastUI;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by jcapiz on 1/12/15.
 */
public class ConfiguraParticipantes extends PreferenceActivity implements SharedPreferences.OnSharedPreferenceChangeListener{

    public static final String USAR_MATRICULA_KEY = "usar_matricula_pref_key";
    public static final String NOMBRE_ARCHIVO_MATRICULA_KEY = "matricula_file_name_pref_key";
    public static final String ENTER_SOMETHING_KEY = "another_server_url";
    private static final String TITULO_VOTACION_GLOBAL = "Servidor Global";
    private static final String PARTICIPAR_EN_VOTACION_GLOBAL = "participar_en_votacion_global";
    private static final int ESPERAR_CONSULTOR = 228;

    private ListPreference votacionesDisponibles;
    private String host;
    private ProcesoDisponible[] procesosDisponibles;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
        if(savedInstanceState == null){
            host = null;
            ((CheckBoxPreference)findPreference(PARTICIPAR_EN_VOTACION_GLOBAL)).setChecked(false);
        }
        votacionesDisponibles = (ListPreference) getPreferenceScreen()
                .findPreference(getResources().getString(R.string.servidor_global));
        votacionesDisponibles.setEntries(new CharSequence[0]);
        votacionesDisponibles.setEntryValues(new CharSequence[0]);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
                                           String key) {
        if(key.equals(NOMBRE_ARCHIVO_MATRICULA_KEY)){
            Preference connectionPref = findPreference(key);
            // Set summary to be the user-description for the selected value
            connectionPref.setSummary(sharedPreferences.getString(key, ""));
        }else if(key.equals(TITULO_VOTACION_GLOBAL)){
            String smthg = sharedPreferences.getString(key, "Default");
            Log.e("TituloSeleccionado", smthg);
            if(!"".equals(smthg))
                iniciaSolicitudes( smthg );
        }else if(key.equals(PARTICIPAR_EN_VOTACION_GLOBAL)){
            if(sharedPreferences.getBoolean(key, false))
            if(host == null){
                startActivityForResult(new Intent(this, EsperandoConsultor.class), ESPERAR_CONSULTOR);
            }else{
                reloadList();
            }
            else{
                votacionesDisponibles = (ListPreference) getPreferenceScreen()
                        .findPreference(getResources().getString(R.string.servidor_global));
                votacionesDisponibles.setValue("");
            }
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState){
        super.onSaveInstanceState(outState);
        outState.putString("host", host);
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState){
        super.onRestoreInstanceState(savedInstanceState);
        host = savedInstanceState.getString("host");
    }

    @Override
    protected void onResume() {
        super.onResume();
        getPreferenceScreen().getSharedPreferences()
                .registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        getPreferenceScreen().getSharedPreferences()
                .unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onActivityResult(int requestCode,int resultCode, Intent data){
        if( resultCode == RESULT_OK ) {
            // El consultor se ha conectado satisfactoriamente.
            switch(requestCode) {
                case ESPERAR_CONSULTOR:
                    host = ProveedorDeRecursos
                            .obtenerRecursoString(ConfiguraParticipantes.this, "ultimo_consultor_activo");
                    reloadList();
                    break;
            }
        }
    }

    private int obtenerIdVotacionDeTitulo(String tituloSeleccionado){
        int idVotacion = -1;
        for(int i=0; i<procesosDisponibles.length; i++)
            if (procesosDisponibles[i].getTitulo().equals(tituloSeleccionado)){
                idVotacion = procesosDisponibles[i].getIdVotacion();
                break;
            }
        return idVotacion;
    }

    private String formatoSolicitudPerfiles(int idVotacion){
        String mensaje = null;
        try{
            JSONObject json = new JSONObject();
            json.put("action", 15);
            json.put("id_votacion", idVotacion);
            mensaje = json.toString();
        }catch(JSONException e){
            e.printStackTrace();
        }
        return mensaje;
    }

    private String solicitudRegistroAnteServidorGlobal(String tituloSeleccionado, int idVotacion){
        String solicitud = null;
        try {
            JSONObject json = new JSONObject();
            json.put("action", 5);
            json.put("id_votacion", idVotacion);
            json.put("title", tituloSeleccionado);
            json.put("place", ProveedorDeRecursos.obtenerRecursoString(ConfiguraParticipantes.this, "ubicacion"));
            json.put("id_place", new Votaciones(this).obtenerIdDeEscuela(json.getString("place")));
            solicitud = json.toString();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return solicitud;
    }

    private String solicitudCuestionarioParaVotacionGlobal (String tituloSeleccionado, int idVotacion) throws JSONException {
        JSONObject json = new JSONObject();
        json.put("action", 6);
        json.put("idVotacion", idVotacion);
        json.put("title", tituloSeleccionado);
        json.put("lugar", ProveedorDeRecursos.obtenerRecursoString(ConfiguraParticipantes.this, "ubicacion"));
        json.put("id_place", new Votaciones(this).obtenerIdDeEscuela(json.getString("lugar")));
        return json.toString();
    }

    private void reloadList(){
        runOnUiThread(new RecolocarTituloComponente("Cargando..."));
        new ContactaConsultor(resObtenerTitulos, host, "{\"action\": 7}").start();
    }

    public void iniciaSolicitudes(String tituloSeleccionado){
        votacionesDisponibles.setTitle(tituloSeleccionado);
        String solicitud;
        String solicitud2;
        String solicitud3;
        int idVotacionSeleccionado;
        idVotacionSeleccionado = obtenerIdVotacionDeTitulo(tituloSeleccionado);
        try{
            solicitud = solicitudCuestionarioParaVotacionGlobal(tituloSeleccionado, idVotacionSeleccionado);
            solicitud2 = formatoSolicitudPerfiles(idVotacionSeleccionado);
            solicitud3 = solicitudRegistroAnteServidorGlobal(tituloSeleccionado, idVotacionSeleccionado);
            ContactaConsultor c1 = new ContactaConsultor(defaultActions, host, solicitud3);
            c1.start();
            Log.e("Los titulos", "Started the first one");
            c1.join();
            ContactaConsultor c2 = new ContactaConsultor(resSolicitudPerfiles, host, solicitud2);
            c2.start();
            Log.e("Los titulos", "Started the second one");
            c2.join();
            ContactaConsultor c3 = new ContactaConsultor(resObtenerCuestionario, host, solicitud);
            c3.start();
            Log.e("Los titulos", "Started the thirth one");
            c3.join();
        }catch(InterruptedException | JSONException ignore){
            ignore.printStackTrace();
        }
    }

    private void recolocaListaDeTitulos(String[] titulos){
        runOnUiThread(new RecolocarTitulosDisponibles(titulos));
    }

    private ContactaConsultor.ResultadoContactoConsultor resObtenerTitulos = new ContactaConsultor.ResultadoContactoConsultor() {
        @Override
        public void hecho() {
            // In this particular instance we should never get here.
        }

        @Override
        public void percance(String mensaje) {
            Log.e("Los titulos", mensaje);
            procesosDisponibles = ProveedorDeMarshalling.unmarshallAvailableProcesses(mensaje);
            if(procesosDisponibles != null){
                runOnUiThread(new RecolocarTituloComponente());
                String[] titulos = new String[procesosDisponibles.length];
                for(int i=0; i<procesosDisponibles.length; i++)
                    titulos[i] = procesosDisponibles[i].getTitulo();
                recolocaListaDeTitulos(titulos);
            }else{
                runOnUiThread(new MostrarSnackUI(getListView(), mensaje));
                if(mensaje.contains("connect"))
                    runOnUiThread(new ApagarOpcionVotacionGlobal());
            }
        }
    };

    private ContactaConsultor.ResultadoContactoConsultor resObtenerCuestionario = new ContactaConsultor.ResultadoContactoConsultor() {
        @Override
        public void hecho() {
            // In this particular instance we should never get here.
        }

        @Override
        public void percance(String mensaje) {
            Log.i(getClass().getName(), "Nos llegó: " + mensaje);
            Votacion votacion = ProveedorDeMarshalling.unmarshallMyVotingObject(mensaje);
            Log.e("Settings", "Tiempo inicial ----> " + ProveedorDeRecursos.obtenerFormatoEnHoras(votacion.getFechaInicio()));
            Log.e("Settings", "Tiempo final ----> " + ProveedorDeRecursos.obtenerFormatoEnHoras(votacion.getFechaFin()));
            if(ProveedorDeRecursos.guardaVotacion(ConfiguraParticipantes.this, votacion, false)) {
                setResult(RESULT_OK);
                runOnUiThread(new MostrarToastUI(ConfiguraParticipantes.this, "Hecho"));
                finish();
            }else{
                ProveedorSnackBar.muestraBarraDeBocados(getListView(), "No fue posible guardar la votación");
            }
        }
    };

    private ContactaConsultor.ResultadoContactoConsultor defaultActions = new ContactaConsultor.ResultadoContactoConsultor() {
        @Override
        public void hecho() {}

        @Override
        public void percance(String mensaje) {
            runOnUiThread(new MostrarSnackUI(getListView(), mensaje));
        }
    };

    private ContactaConsultor.ResultadoContactoConsultor resSolicitudPerfiles = new ContactaConsultor.ResultadoContactoConsultor() {
        @Override
        public void hecho() {

        }

        @Override
        public void percance(String mensaje) {
            try{
                Log.e("Los perfiles", "Llegó como perfiles: " + mensaje);
                Votaciones db = new Votaciones(ConfiguraParticipantes.this);
                JSONArray jperfiles = new JSONArray(mensaje);
                JSONObject json;
                for(int i=0; i<jperfiles.length(); i++) {
                    json = jperfiles.getJSONObject(i);
                    db.insertaPerfil(json.getString("nombre_p"), json.getInt("id_p"));
                }
            }catch(JSONException e){
                e.printStackTrace();
                runOnUiThread(new MostrarSnackUI(getListView(), mensaje));
            }
        }
    };

    private class RecolocarTitulosDisponibles implements Runnable{

        private String[] titulos;

        public RecolocarTitulosDisponibles(String[] titulos) {
            this.titulos = titulos;
        }

        @Override
        public void run(){
            votacionesDisponibles.setEntries(titulos);
            votacionesDisponibles.setEntryValues(titulos);}
    }

    private class RecolocarTituloComponente implements Runnable{

        private String titulo;

        public RecolocarTituloComponente(String titulo) {
            this.titulo = titulo;
        }

        public RecolocarTituloComponente() {
            this.titulo = getResources().getString(R.string.participar_en_votacion_global_sum);
        }

        @Override
        public void run(){
            votacionesDisponibles.setTitle(titulo);
        }
    }

    private class ApagarOpcionVotacionGlobal implements Runnable{

        @Override
        public void run(){
            ((CheckBoxPreference)findPreference(PARTICIPAR_EN_VOTACION_GLOBAL))
                    .setChecked(false);
        }
    }
}