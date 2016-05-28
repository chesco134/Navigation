package org.inspira.jcapiz.polivoto.actividades;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import org.inspira.jcapiz.polivoto.R;
import org.inspira.jcapiz.polivoto.proveedores.ProveedorDeRecursos;
import org.inspira.jcapiz.polivoto.database.Votaciones;
import org.inspira.jcapiz.polivoto.dialogos.Informacion;
import org.inspira.jcapiz.polivoto.fragmentos.ClavesUsuario;
import org.inspira.jcapiz.polivoto.fragmentos.ColocaClaveDeAdministrador;
import org.inspira.jcapiz.polivoto.fragmentos.GruposPoblacion;
import org.inspira.jcapiz.polivoto.fragmentos.Lobby;
import org.inspira.jcapiz.polivoto.fragmentos.Ubicacion;
import org.inspira.jcapiz.polivoto.servicios.AtencionConsultaDatosVotaciones;
import org.inspira.jcapiz.polivoto.servicios.MiServicio;
import org.inspira.jcapiz.polivoto.servicios.ServicioDeReloj;
import org.inspira.jcapiz.polivoto.threading.ContactaConsultor;
import org.inspira.jcapiz.polivoto.threading.MostrarSnackUI;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

public class Bienvenida extends AppCompatActivity {

    private static final int SPLASH = 324;
    private static final int SALA_DE_ESPERA = 321;
    private Toolbar toolbar;
    private Votaciones db;
    private boolean showSplash;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        db = new Votaciones(this);
        setContentView(R.layout.activity_lobby);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        startService(new Intent(this, ServicioDeReloj.class));
        if(savedInstanceState == null){
            if(ProveedorDeRecursos.obtenerRecursoEntero(this, "extado_servicio_historial") != 0)
                startService(new Intent(this, AtencionConsultaDatosVotaciones.class));
            else
                stopService(new Intent(this, AtencionConsultaDatosVotaciones.class));
            putFragment();
            showSplash = true;
        }else{
            showSplash = false;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.menu_principal, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume(){
        super.onResume();
        Log.e("Startup", "RESUMING");
        if(showSplash){
            Log.e("Startup", "Showing SPLASH");
            launchSplash();
            showSplash = false;
        }else{
            Log.e("Startup", "Cheching Voting processes Status");
            manageVotingProcess();
        }
    }

    public void showInformationDialog(String mensaje){
        Informacion info = new Informacion();
        Bundle argumentos = new Bundle();
        argumentos.putString("mensaje",mensaje);
        info.setArguments(argumentos);
        info.show(getSupportFragmentManager(), "Informacion");
    }

    private void manageVotingProcess(){
        try {
            final JSONObject datosDeVotacionActual;
            datosDeVotacionActual = db.obtenerDatosDeVotacionActual();
            Log.e("Startup", "Got last voting data.");
            if(datosDeVotacionActual != null) {
                    Log.e("Welcome", "Vamo a vé: " + db.isVotacionActualGlobal() + ", " + datosDeVotacionActual.getBoolean("Soy_Propietario"));
                if (db.isVotacionActualGlobal()) {
                    Log.i("Welcome", "Según esto ES global");
                    if(!datosDeVotacionActual.getBoolean("Soy_Propietario"))
                    new ContactaConsultor(new ContactaConsultor.ResultadoContactoConsultor() {
                        @Override
                        public void hecho() {
                        }

                        @Override
                        public void percance(String mensaje) {
                            try {
                                JSONObject json = new JSONObject(mensaje);
                                compruebaVotacion(datosDeVotacionActual.getLong("Fecha_Fin") - json.getLong("estampa_de_tiempo"), datosDeVotacionActual.getLong("Fecha_Inicio") - json.getLong("estampa_de_tiempo"), true);
                            } catch (JSONException e) {
                                e.printStackTrace();
                                runOnUiThread(new MostrarSnackUI(toolbar, mensaje));
                            }
                        }
                    }, ProveedorDeRecursos.obtenerRecursoString(this, "ultimo_consultor_activo")
                            , "{\"action\":17, \"idVotacion\": " + datosDeVotacionActual.getInt("idVotacionGlobal") + "}").start();
                    else{
                        Log.i("Welcome", "Es global pero no soy propietario");
                        long tiempoLocalActual = System.currentTimeMillis();
                        compruebaVotacion(datosDeVotacionActual.getLong("Fecha_Fin") - tiempoLocalActual, datosDeVotacionActual.getLong("Fecha_Inicio") - tiempoLocalActual, false);
                    }
                } else {
                    Log.i("Welcome", "Según esto no es global");
                    long tiempoLocalActual = System.currentTimeMillis();
                    compruebaVotacion(datosDeVotacionActual.getLong("Fecha_Fin") - tiempoLocalActual, datosDeVotacionActual.getLong("Fecha_Inicio") - tiempoLocalActual, false);
                }
            }else{
                Log.e("Startup", "Got no voting data");
            }
        } catch (JSONException ignore) {
            Log.e("Startup", "Exception Occurred");
            ignore.printStackTrace();
            Log.e("Startup", "Exception printed");
        }
    }

    private void compruebaVotacion(long tiempoRestanteDeVotacion, long tiempoRestanteParaIniciar, final boolean esGlobal) {
        if( tiempoRestanteDeVotacion > 0 ) { // Quiere decir que hay tiempo
            if(  tiempoRestanteParaIniciar <= 0 ) {
                iniciaServicioDeVotacion(esGlobal);
            }else{
                makeSnackbarLast("La votación iniciará cuando se alcance la fecha de inicio");
                new Timer().schedule(new TimerTask(){
                    @Override public void run(){
                        iniciaServicioDeVotacion(esGlobal);
                    }
                }, tiempoRestanteParaIniciar);
            }
        }
    }

    private void iniciaServicioDeVotacion(boolean esGlobal){
        stopService(new Intent(this, AtencionConsultaDatosVotaciones.class));
        ProveedorDeRecursos.guardarRecursoEntero(this, "extado_servicio_historial", 0);
        startService(new Intent(this, MiServicio.class));
        launchSalaDeEspera(esGlobal);
        makeSnackbar("Votación iniciada");
    }

    private void launchSalaDeEspera(boolean esGlobal){
        Intent i = new Intent(this, SalaDeEspera.class);
        i.putExtra("es_global", esGlobal);
        startActivityForResult(i, SALA_DE_ESPERA);
    }

    private void putFragment(){
        boolean existeAdmin = db.revisaExistenciaDeCredencial("Administrador");
        boolean existeCapturista = db.revisaExistenciaDeCredencial("Capturista");
        boolean existeConsultor = db.revisaExistenciaDeCredencial("Consultor");
        boolean existeParticipante = db.revisaExistenciaDeCredencial("Participante");
        boolean existenGrupos = db.revisaExistenciaDePerfiles();
        boolean existeUbicacion = db.consultaEscuela();
        Fragment fragment;
        String tag;
        if(!existeAdmin) {
            fragment = new ColocaClaveDeAdministrador();
            tag = "welcome";
        }else if( !existeCapturista || !existeConsultor || !existeParticipante ) {
            fragment = new ClavesUsuario();
            tag = "claves_usuario";
        }else if( !existenGrupos ) {
            fragment = new GruposPoblacion();
            tag = "grupos_poblacion";
        }else if( !existeUbicacion ) {
            fragment = new Ubicacion();
            tag = "ubicacion";
        }else {
            fragment = new Lobby();
            tag = "lobby";
        }
        getSupportFragmentManager()
                .beginTransaction()
                .add(R.id.main_container, fragment, tag)
                .commit();
    }

    private void makeSnackbar(String message){
        Snackbar.make(toolbar, message, Snackbar.LENGTH_SHORT)
                .setAction("Aviso", null).show();
    }

    private void makeSnackbarLast(String message){
        Snackbar.make(toolbar, message, Snackbar.LENGTH_LONG)
                .setAction("Aviso", null).show();
    }

    private void launchSplash(){
        Intent i = new Intent(this, Splash.class);
        startActivityForResult(i, SPLASH);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode,resultCode,data);
        //if(requestCode == SPLASH)
        //    manageVotingProcess();
        if( requestCode == SALA_DE_ESPERA )
            if( resultCode != RESULT_OK )
                finish();
    }
}