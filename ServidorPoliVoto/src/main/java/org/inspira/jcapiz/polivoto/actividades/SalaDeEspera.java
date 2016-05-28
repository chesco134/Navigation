package org.inspira.jcapiz.polivoto.actividades;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import org.inspira.jcapiz.polivoto.R;
import org.inspira.jcapiz.polivoto.database.Votaciones;
import org.inspira.jcapiz.polivoto.networking.IOHandler;
import org.inspira.jcapiz.polivoto.proveedores.ProveedorDeRecursos;
import org.inspira.jcapiz.polivoto.proveedores.ProveedorDeTiempoRemoto;
import org.inspira.jcapiz.polivoto.servicios.MiServicio;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

/**
 * Created by jcapiz on 7/01/16.
 */
public class SalaDeEspera extends AppCompatActivity {

    private TextView etiquetaContador;
    private TextView cuentaRegresiva;
    private AdministraCuentaRegresiva adminCuentaRegresiva;
    private int contador;
    private boolean esGlobal;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sala_de_espera);
        etiquetaContador = (TextView)findViewById(R.id.sala_de_espera_counter);
        cuentaRegresiva = (TextView)findViewById(R.id.sala_de_espera_fecha_inicio);
        etiquetaContador.setOnClickListener(new ClickSobreCantidadDeParticipantes());
        if(savedInstanceState == null) {
            esGlobal = getIntent().getExtras().getBoolean("es_global", false);
            contador = 0;
            contadorHaCambiado();
        }
        getSupportActionBar().setTitle(new Votaciones(this).obtenerTituloVotacionActual());
    }

    @Override
    public void onSaveInstanceState(Bundle outState){
        outState.putString("cuenta_regresiva", cuentaRegresiva.getText().toString());
        outState.putInt("contador", contador);
        outState.putBoolean("es_global", esGlobal);
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState){
        cuentaRegresiva.setText(savedInstanceState.getString("cuenta_regresiva"));
        contador = savedInstanceState.getInt("contador");
        esGlobal = savedInstanceState.getBoolean("es_global");
        contadorHaCambiado();
    }

    @Override
    protected void onStart(){
        super.onStart();
        doBindService();
    }

    @Override
    protected void onResume(){
        super.onResume();
        adminCuentaRegresiva = new AdministraCuentaRegresiva();
        adminCuentaRegresiva.execute();
    }

    @Override
    protected void onStop(){
        super.onStop();
        doUnbindService();
        adminCuentaRegresiva.setIsRunning(false);
        adminCuentaRegresiva = null;
    }

    public void actualizaCuentaRegresiva(String contenido){
        cuentaRegresiva.setText(contenido);
    }

    public void incrementaContador(){
        contador++;
        contadorHaCambiado();
    }

    private void contadorHaCambiado(){
        String contenido = "" + contador;
        etiquetaContador.setText(contenido);
    }

    private class AdministraCuentaRegresiva extends AsyncTask<String,Long,Long> {

        private boolean isRunning;

        @Override
        protected Long doInBackground(String... args){
            Log.d("Countdown Thread", "Iniciamos cuenta regresiva.");
            long millis = -1;
            try {
                millis = ProveedorDeTiempoRemoto.obtenerTiempoRemoto(SalaDeEspera.this, esGlobal);
            } catch (IOException | JSONException e) {
                e.printStackTrace();
            }
            publishProgress(millis);
            isRunning = true;
            while( isRunning && millis > 0 ) {
                SystemClock.sleep(1000);
                millis -= 1000;
                publishProgress(millis);
            }
            return 0L;
        }

        @Override
        protected void onProgressUpdate(Long... params){
            long tiempoActual = params[0];
            actualizaCuentaRegresiva(calcularEtiqueta(tiempoActual));
        }

        @Override
        protected void onPostExecute(Long tiempoFinal){
            actualizaCuentaRegresiva(calcularEtiqueta(tiempoFinal));
        }

        private String calcularEtiqueta(long tiempoActualMilis){
            int horasRestantes = (int)(tiempoActualMilis/3600000);
            long minutosRestantesMilis = tiempoActualMilis - horasRestantes*3600000;
            int minutosRestantes = (int)(minutosRestantesMilis/60000);
            long segundosRestantesMilis = minutosRestantesMilis - minutosRestantes*60000;
            int segundosRestantes = (int)(segundosRestantesMilis/1000);
            return (horasRestantes < 10 ? "0" + horasRestantes : horasRestantes) +
                    ":" +
                    (minutosRestantes < 10 ? "0" + minutosRestantes : minutosRestantes) +
                    ":" +
                    (segundosRestantes < 10 ? "0" + segundosRestantes : segundosRestantes);
        }

        public void setIsRunning(boolean isRunning) {
            this.isRunning = isRunning;
        }
    }

    private void verParticipantes(){
        Votaciones db = new Votaciones(this);
        try {
            JSONObject json = db.obtenerDatosDeVotacionActual();
            String[] participaron = db.quienesHanParticipado(json.getInt("idVotacion"));
            Intent i = new Intent(this, DetallesDeQuienesHanParticipado.class);
            i.putExtra("rows", participaron);
            i.putExtra("header", "Participantes al momento");
            startActivity(i);
        }catch(JSONException ignore){}
    }

    private class ClickSobreCantidadDeParticipantes implements View.OnClickListener{

        @Override
        public void onClick(View view){
            verParticipantes();
        }
    }

    /*********************************
     * EMPIEZA LA ZONA DEL SERVICIO
     **********************************/

    private MiServicio mBoundService;

    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            // This is called when the connection with the service has been
            // established, giving us the service object we can use to
            // interact with the service. Because we have bound to a explicit
            // service that we know is running in our own process, we can
            // cast its IBinder to a concrete class and directly access it.
            mBoundService = ((MiServicio.LocalBinder) service).getService();
            try {
                mBoundService.setSalaDeEspera(SalaDeEspera.this);
                contador = mBoundService.obtenerCuenta();
                contadorHaCambiado();
            }catch(NullPointerException ignore){
                ignore.printStackTrace();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName className) {
            // This is called when the connection with the service has been
            // unexpectedly disconnected -- that is, its process crashed.
            // Because it is running in our same process, we should never
            // see this happen.
            mBoundService = null;
        }
    };

    private boolean mIsBound = false;

    void doBindService() {
        // Establish a connection with the service. We use an explicit
        // class name because we want a specific service implementation that
        // we know will be running in our own process (and thus won't be
        // supporting component replacement by other applications).
        if(!mIsBound) {
            cuentaRegresiva.setText("Iniciando...");
            bindService(new Intent(this, MiServicio.class), mConnection,
                    Context.BIND_AUTO_CREATE);
            mIsBound = true;
        }
    }

    public void doUnbindService() {
        if (mIsBound) {
            // Detach our existing connection.
            unbindService(mConnection);
            mIsBound = false;
        }
    }

}