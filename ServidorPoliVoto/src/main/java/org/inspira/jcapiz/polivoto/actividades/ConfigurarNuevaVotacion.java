package org.inspira.jcapiz.polivoto.actividades;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.FrameLayout;

import org.inspira.jcapiz.polivoto.R;
import org.inspira.jcapiz.polivoto.database.Votaciones;
import org.inspira.jcapiz.polivoto.dialogos.DialogoDeConsultaSimple;
import org.inspira.jcapiz.polivoto.dialogos.Informacion;
import org.inspira.jcapiz.polivoto.fragmentos.PrimeraFaseNuevaVotacion;
import org.inspira.jcapiz.polivoto.pojo.ControlDeRetorno;
import org.inspira.jcapiz.polivoto.pojo.Pregunta;
import org.inspira.jcapiz.polivoto.pojo.Votacion;
import org.inspira.jcapiz.polivoto.proveedores.ProveedorDeRecursos;
import org.inspira.jcapiz.polivoto.proveedores.ProveedorSnackBar;
import org.inspira.jcapiz.polivoto.servicios.AtencionConsultaDatosVotaciones;
import org.inspira.jcapiz.polivoto.threading.ContactaConsultor;
import org.inspira.jcapiz.polivoto.threading.MostrarSnackUI;
import org.inspira.jcapiz.polivoto.threading.RegistraVotacionGlobal;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by jcapiz on 8/05/16.
 */
public class ConfigurarNuevaVotacion extends AppCompatActivity implements RegistraVotacionGlobal.AccionFinalRVG, ContactaConsultor.ResultadoContactoConsultor {

    private static final String HINT = "Se trata del título y las preguntas que verán los participantes," +
            " por cada pregunta aparecerá una papeleta con sus respectivas opciones." +
            "\nPara editar las opciones de cada pregunta debe hacer click sobre ella." +
            "\nLas preguntas que contengan menos de dos opciones serán ignoradas al" +
            " momento de construir la plantilla de la(s) papeleta(s).";
    private static final int ESPERAR_CONSULTOR = 0;
    private static final int ACTIVIDAD_DE_ESPERA = 1;
    private ControlDeRetorno controlDeRetorno;
    private Votacion votacion;
    private FrameLayout contenedor;

    @Override
    protected void onCreate(Bundle b){
        super.onCreate(b);
        setContentView(R.layout.contenedor_de_un_fragmento);
        if(b == null){
            votacion = new Votacion();
            controlDeRetorno = new ControlDeRetorno();
        } else {
            votacion = (Votacion) b.getSerializable("votacion");
            controlDeRetorno = (ControlDeRetorno) b.getSerializable("control_de_retorno");
        }
        assert controlDeRetorno != null;
        controlDeRetorno.setValue(0);
        agregarFragmento();
        contenedor = (FrameLayout)findViewById(R.id.contenedor_de_un_fragmento_este_contenedor);
    }

    @Override
    public void onSaveInstanceState(Bundle b){
        b.putSerializable("votacion", votacion);
        b.putSerializable("control_de_retorno", controlDeRetorno);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.menu_principal, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu){
        menu.findItem(R.id.add).setVisible(false);
        menu.findItem(R.id.less).setVisible(false);
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        boolean consumed = false;
        int itemId = item.getItemId();
        if(itemId == R.id.info){
            showInformationDialog(HINT);
            consumed = true;
        } else if(itemId == R.id.confirmar){
            evaluarCompletitud();
            consumed = true;
        }
        return consumed;
    }

    @Override
    public void onBackPressed(){
        if(controlDeRetorno.getValue() == 0) {
            launchDialogoMensaje();
        }else{
            super.onBackPressed();
            controlDeRetorno.decreaseValue();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data){
        if(requestCode == ESPERAR_CONSULTOR && resultCode == RESULT_OK){
            lanzaActividadDeEspera("Sincronizando...");
            String consultorHost = ProveedorDeRecursos.obtenerRecursoString(this, "ultimo_consultor_activo");
            RegistraVotacionGlobal rvg = new RegistraVotacionGlobal(this, consultorHost);
            rvg.setVotacion(votacion);
            rvg.execute();
        }
    }

    private void launchDialogoMensaje() {
        Bundle argumentos = new Bundle();
        argumentos.putString("mensaje","¿Desea salir? Se perderán los datos de configuración de esta votación");
        DialogoDeConsultaSimple dialogoDeConsulta = new DialogoDeConsultaSimple();
        dialogoDeConsulta.setArguments(argumentos);
        dialogoDeConsulta.setAgenteDeInteraccion(
                new DialogoDeConsultaSimple.AgenteDeInteraccionConResultado() {
                    @Override
                    public void clickSobreAccionPositiva(DialogFragment dialogo) {
                        finish();
                    }

                    @Override
                    public void clickSobreAccionNegativa(DialogFragment dialogo) {}
                }
        );
        dialogoDeConsulta.show(getSupportFragmentManager(), "PidePermiso");
    }

    private void lanzaActividadDeEspera(String mensaje){
        Intent i = new Intent(this, ActividadDeEspera.class);
        i.putExtra("message", mensaje);
        startActivityForResult(i, ACTIVIDAD_DE_ESPERA);
    }

    private void finalizarActividadDeEspera(){
        finishActivity(ACTIVIDAD_DE_ESPERA);
    }

    private void lanzaActividadDeEsperaDeConsultor(){
        stopService(new Intent(this, AtencionConsultaDatosVotaciones.class));
        startActivityForResult(new Intent(this, EsperandoConsultor.class), ESPERAR_CONSULTOR);
    }

    private void evaluarCompletitud() {
        boolean existeTitulo = votacion.getTitulo() != null;
        boolean existeFechaInicial = votacion.getFechaInicio() != -1;
        boolean existeFechaFinal = votacion.getFechaFin() != -1;
        boolean preguntasSuficientes = votacion.getPreguntas().size() > 0;
        boolean integridadPreguntas = true;
        for(Pregunta pregunta : votacion.getPreguntas())
            if( pregunta.getOpciones().size() < 2 ) {
                integridadPreguntas = false;
                break;
            }
        if(!existeTitulo)
            ProveedorSnackBar
                    .muestraBarraDeBocados(contenedor, "Hace falta un título de votación");
        if(!existeFechaInicial)
            ProveedorSnackBar
                    .muestraBarraDeBocados(contenedor, "Hace falta la fecha inicial");
        if(!existeFechaFinal)
            ProveedorSnackBar
                    .muestraBarraDeBocados(contenedor, "Hace falta la fecha final");
        if(!preguntasSuficientes)
            ProveedorSnackBar
                    .muestraBarraDeBocados(contenedor, "Se requiere al menos una pregunta");
        if(!integridadPreguntas)
            ProveedorSnackBar
                    .muestraBarraDeBocados(contenedor, "Hay preguntas sin opciones completas");
        if( existeTitulo && existeFechaInicial && existeFechaFinal && preguntasSuficientes && integridadPreguntas){
            votacion.setLugar(ProveedorDeRecursos.obtenerRecursoString(this, "ubicacion"));
            votacion.setIdEscuela(new Votaciones(this).obtenerIdDeEscuela(votacion.getLugar()));
            if(votacion.isGlobal()){
                lanzaActividadDeEsperaDeConsultor();
            } else {
                ProveedorDeRecursos.guardaVotacion(this, votacion, true);
                setResult(RESULT_OK);
                finish();
            }
        }
    }

    private void showInformationDialog(String mensaje){
        Informacion info = new Informacion();
        Bundle argumentos = new Bundle();
        argumentos.putString("mensaje",mensaje);
        info.setArguments(argumentos);
        info.show(getSupportFragmentManager(), "Informacion");
    }

    private void agregarFragmento(){
        PrimeraFaseNuevaVotacion pfnv = new PrimeraFaseNuevaVotacion();
        Bundle a = new Bundle();
        a.putSerializable("votacion", votacion);
        pfnv.setArguments(a);
        a.putSerializable("control_de_retorno", controlDeRetorno);
        getSupportFragmentManager()
                .beginTransaction()
                .add(R.id.contenedor_de_un_fragmento_este_contenedor, pfnv)
                .commit();
    }

    @Override
    public void exito() {
        finalizarActividadDeEspera();
        setResult(RESULT_OK);
        finish();
    }

    @Override
    public void hecho() {

    }

    @Override
    public void percance(String mensaje) {
        finalizarActividadDeEspera();
        runOnUiThread(new MostrarSnackUI(contenedor, mensaje));
    }
}
