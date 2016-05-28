package org.inspira.jcapiz.polivoto.actividades;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.design.widget.Snackbar;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import org.inspira.jcapiz.polivoto.R;
import org.inspira.jcapiz.polivoto.proveedores.ProveedorDeRecursos;
import org.inspira.jcapiz.polivoto.adaptadores.MyFragmentStatePagerAdapter;
import org.inspira.jcapiz.polivoto.database.Votaciones;
import org.inspira.jcapiz.polivoto.dialogos.DialogoDeConsultaSimple;
import org.inspira.jcapiz.polivoto.dialogos.Informacion;
import org.inspira.jcapiz.polivoto.fragmentos.AdminOpcionesPregunta;
import org.inspira.jcapiz.polivoto.fragmentos.FechasDeVotacion;
import org.inspira.jcapiz.polivoto.fragmentos.FormularioDeVotacion;
import org.inspira.jcapiz.polivoto.pojo.Votacion;
import org.inspira.jcapiz.polivoto.proveedores.ProveedorSnackBar;
import org.inspira.jcapiz.polivoto.threading.RegistraVotacionGlobal;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import resumenresultados.shared.Pregunta;

/**
 * Created by jcapiz on 2/01/16.
 */
public class NuevoProcesoDeVotacion extends AppCompatActivity implements
        ActionBar.TabListener {

    public static final String PARTICIPANTES_FILE = Environment.getExternalStorageDirectory().getAbsolutePath() + "/settings.csv";
    public static final String FILE_NAME = Environment.getExternalStorageDirectory().getAbsolutePath() + "/votaciones.conf";
    public static final String RESULTS_FILE = Environment.getExternalStorageDirectory().getAbsolutePath() + "/resultados.polivoto";
    private static final int STAND_BY = 128;
    private static final int DATA_LOADER = 782;
    private static final int ESPERAR_CONSULTOR = 228;
    private Votaciones db;
    private FormularioDeVotacion formulario;
    private FechasDeVotacion fechas;
    private List<Pregunta> preguntas;
    private MyFragmentStatePagerAdapter adapter;
    private LinkedList<Fragment> fragmentitos;
    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.contenido_nuevo_proceso_de_votacion);
        fragmentitos = new LinkedList<>();
        formulario = new FormularioDeVotacion();
        Bundle args = new Bundle();
        args.putString("header", "");
        formulario.setArguments(args);
        fechas = new FechasDeVotacion();
        fechas.setArguments(args);
        fragmentitos.add(formulario);
        fragmentitos.add(fechas);

        // Set up the action bar.
        final ActionBar actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        adapter = new MyFragmentStatePagerAdapter(getSupportFragmentManager(), fragmentitos);

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(adapter);

        // When swiping between different sections, select the corresponding
        // tab. We can also use ActionBar.Tab#select() to do this if we have
        // a reference to the Tab.
        mViewPager
                .addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
                    @Override
                    public void onPageSelected(int position) {
                        actionBar.setSelectedNavigationItem(position);
                    }
                });

        // For each of the sections in the app, add a tab to the action bar.
        for (int i = 0; i < adapter.getCount(); i++) {
            // Create a tab with text corresponding to the page title defined by
            // the adapter. Also specify this Activity object, which implements
            // the TabListener interface, as the callback (listener) for when
            // this tab is selected.
            actionBar.addTab(actionBar.newTab()
                    .setIcon(FADED_ICONS[i])
                    .setTabListener(this));
        }
        db = new Votaciones(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.menu_principal, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        if( item.getItemId() == R.id.confirmar ){
            if( validaFormulario() && validaFechas() )
                if(!formulario.isGlobal()) {
                    if (revisaFechasDeVotacion()) { // El resultado sarisfactorio es el inicio del proceso.
                        guardarVotacion(armarVotacion());
                        finish();
                    }
                }
                else
                    configuracionGlobal();
            else
                Log.d("Form_Fechas", "Algo no pasó la prueba... Form? " + validaFormulario() + ", Fechas? " + validaFechas());
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onTabSelected(ActionBar.Tab tab,
                              FragmentTransaction fragmentTransaction) {
        // When the given tab is selected, switch to the corresponding page in
        // the ViewPager.
        tab.setIcon(ICONS[tab.getPosition()]);
        mViewPager.setCurrentItem(tab.getPosition());
        try {
            getSupportActionBar().setTitle(TITULOS[tab.getPosition()]);
        }catch (NullPointerException e){
            e.printStackTrace();
        }
    }

    @Override
    public void onTabUnselected(ActionBar.Tab tab,
                                FragmentTransaction fragmentTransaction) {
        if( tab.getPosition() > -1 ) {
            tab.setIcon(FADED_ICONS[tab.getPosition()]);
            if (tab.getPosition() == 2) {
                AdminOpcionesPregunta adminOpcionesPregunta = (AdminOpcionesPregunta)adapter.getItem(2);
                adminOpcionesPregunta.colocarTitulo();
                getSupportActionBar().removeTab(tab);
                adapter.remove(2);
                adapter = new MyFragmentStatePagerAdapter(getSupportFragmentManager(), fragmentitos);
                mViewPager.setAdapter(adapter);
            }
        }
    }

    @Override
    public void onTabReselected(ActionBar.Tab tab,
                                FragmentTransaction fragmentTransaction) {
    }

    public void setPreguntas(List<Pregunta> preguntas){
        this.preguntas = preguntas;
    }

    public void showInformationDialog(String mensaje){
        Informacion info = new Informacion();
        Bundle argumentos = new Bundle();
        argumentos.putString("mensaje",mensaje);
        info.setArguments(argumentos);
        info.show(getSupportFragmentManager(), "Informacion");
    }

    public boolean validaFechas(){
        boolean correcto = false;
        List<Fragment> fragmentos = getSupportFragmentManager().getFragments();
        FechasDeVotacion fechasDeVotacion = null;
        for( Fragment fragmento : fragmentos )
            if( fragmento instanceof FechasDeVotacion ) {
                fechasDeVotacion = (FechasDeVotacion) fragmento;
                break;
            }
        if( fechasDeVotacion != null ){
            correcto = fechasDeVotacion.validaFechas();
        }
        return correcto;
    }

    public boolean validaFormulario(){
        boolean correcto = false;
        List<Fragment> fragmentos = getSupportFragmentManager().getFragments();
        FormularioDeVotacion formularioDeVotacion = null;
        for( Fragment fragmento : fragmentos )
            if( fragmento instanceof FormularioDeVotacion ) {
                formularioDeVotacion = (FormularioDeVotacion) fragmento;
                break;
            }
        if( formularioDeVotacion != null ){
            correcto = formularioDeVotacion.validaFormulario();
        }
        return correcto;
    }

    public void setCurrentItem(int position){
        mViewPager.setCurrentItem(position);
    }

    public Fragment obtenerFragmento(int posicion){
        Fragment fragmento = null;
        try{
            fragmento = adapter.getItem(posicion);
        }catch(ArrayIndexOutOfBoundsException ignore){}
        return fragmento;
    }

    public void addFragment(Fragment fragment){
        adapter.add(fragment);
    }

    private void configuracionGlobal(){
        lanzaDialogoConfirmacion();
    }

    private void lanzaDialogoConfirmacion(){
        Bundle argumentos = new Bundle();
        argumentos.putString("mensaje","¿Desea publicar ésta votación como global? Otros sitios podrán unirse antes de su fecha de comienzo y participar.");
        DialogoDeConsultaSimple dialogoDeConsulta = new DialogoDeConsultaSimple();
        dialogoDeConsulta.setArguments(argumentos);
        dialogoDeConsulta.setAgenteDeInteraccion(agenteDeInteraccion);
        dialogoDeConsulta.show(getSupportFragmentManager(), "PidePermiso");
    }

    public void quitarActividadDeEspera() {
        finishActivity(STAND_BY);
    }

    public void makeSnackbar(String mensaje){
        Snackbar.make(mViewPager, mensaje, Snackbar.LENGTH_SHORT)
                .setAction("Aviso", null).show();
    }

    private boolean revisaFechasDeVotacion(){
        Date fechaInicio = fechas.obtenerFechaInicio();
        Date fechaFin = fechas.obtenerFechaFin();
        if( fechaInicio != null && fechaFin != null ) {
            return true;
        }else {
            makeSnackbar("Revise los tiempos de votación");
            Log.d("Preparativos", "Fecha inicio? " + (fechaInicio != null) + ", fecha fin? " + (fechaFin != null));
            return false;
        }
    }

    public Votacion armarVotacion(){
        Votacion votacion = new Votacion();
        String titulo = formulario.getTitulo();
        Date fechaInicio = fechas.obtenerFechaInicio();
        Date fechaFin = fechas.obtenerFechaFin();
        votacion.setFechaInicio(fechaInicio.getTime());
        votacion.setFechaFin(fechaFin.getTime());
        votacion.setTitulo(titulo);
        String location = getSharedPreferences(Bienvenida.class.getName(), Context.MODE_PRIVATE)
                .getString("ubicacion", "NaN");
        votacion.setIdEscuela(db.obtenerIdDeEscuela(location));
        int k = 0;
        for (Pregunta pregunta : preguntas) {
            votacion.agregarPregunta(pregunta.getTitulo());
            for (int i = 0; i < pregunta.obtenerCantidadDeOpciones(); i++) {
                votacion.getPreguntas().get(k).agregarOpcion(pregunta.obtenerOpcion(i).getNombre());
            }
            k++;
        }
        return votacion;
    }

    public void guardarVotacion(Votacion votacion){
        String location = getSharedPreferences(Bienvenida.class.getName(), Context.MODE_PRIVATE)
                .getString("ubicacion", "NaN");
        int idVotacion = db.insertaVotacion(votacion.getTitulo(), location, votacion.getFechaInicio(), votacion.getFechaFin(), true);
        votacion.setId(idVotacion);
        ProveedorDeRecursos.guardarRecursoEntero(this, "idVotacion", idVotacion);
        for (org.inspira.jcapiz.polivoto.pojo.Pregunta pregunta : votacion.getPreguntas()) {
            db.insertaPregunta(pregunta.getEnunciado(), votacion.getId());
            for (int i = 0; i < pregunta.getOpciones().size(); i++) {
                db.insertaOpcion(pregunta.obtenerOpcion(i).getReactivo(), pregunta.getId());
            }
        }
        if (PreferenceManager.getDefaultSharedPreferences(this)
                .getBoolean(ConfiguraParticipantes.USAR_MATRICULA_KEY, false)) {
            Log.d("Yaranzo", "We came here (inicio con matrícula)");
            lanzarCargadorDeDatos("Cargando matrícula");
        }
        if(formulario.isGlobal())
            db.setVotacionAsGlobal(0,-1); // 0 is like padding... (does nothing special)
    }

    private DialogoDeConsultaSimple.AgenteDeInteraccionConResultado agenteDeInteraccion = new DialogoDeConsultaSimple.AgenteDeInteraccionConResultado() {

        @Override
        public void clickSobreAccionPositiva(DialogFragment dialogo) {
            if(revisaFechasDeVotacion()) {
                // We need the Capturista
                startActivityForResult(new Intent(NuevoProcesoDeVotacion.this, EsperandoConsultor.class), ESPERAR_CONSULTOR);
            }
        }

        @Override
        public void clickSobreAccionNegativa(DialogFragment dialogo) {
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
    };

    private void lanzarCargadorDeDatos(String label){
        Votaciones v = new Votaciones(this);
        Bundle extras = new Bundle();
        extras.putString("label", label);
        org.inspira.jcapiz.polivoto.pojo.Pregunta[] preguntas = v.obtenerPreguntasVotacionActual();
        String[] enunciados = new String[preguntas.length];
        for(int i=0; i<enunciados.length; i++)
            enunciados[i] = preguntas[i].getEnunciado();
        extras.putStringArray("titulos", enunciados);
        Intent dataLoader = new Intent(this, CargadorDeMatricula.class);
        dataLoader.putExtras(extras);
        startActivityForResult(dataLoader, DATA_LOADER);
    }

    private void lanzaActividadDeEspera(String mensaje){
        Intent i = new Intent(this, ActividadDeEspera.class);
        i.putExtra("message", mensaje);
        startActivityForResult(i, STAND_BY);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data){
        if(requestCode == ESPERAR_CONSULTOR){
            if(resultCode == RESULT_OK) {
                lanzaActividadDeEspera("Sincronizando...");
                int lastUID = db.grabLastUserIdAttmptSucceded("Consultor");
                String consultorHost = db.grabHostForUserLoginAttempt(lastUID);
                RegistraVotacionGlobal rvg = new RegistraVotacionGlobal(NuevoProcesoDeVotacion.this, consultorHost);
                rvg.setVotacion(armarVotacion());
                rvg.execute();
            }else{
                ProveedorSnackBar
                        .muestraBarraDeBocados(mViewPager, "Inténtelo dentro de unos momentos");
            }
        }
    }

    @Override
    public void onBackPressed(){
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
                    public void clickSobreAccionNegativa(DialogFragment dialogo) {

                    }
                }
        );
        dialogoDeConsulta.show(getSupportFragmentManager(), "PidePermiso");
    }

    private static final int[] FADED_ICONS = {R.drawable.ic_description_grey_24dp,
                                            R.drawable.ic_schedule_grey_24dp,
                                            R.drawable.ic_receipt_grey_24dp};

    private static final int[] ICONS = {R.drawable.ic_description_white_24dp,
                                        R.drawable.ic_schedule_white_24dp,
                                        R.drawable.ic_receipt_white_24dp};

    private static final String[] TITULOS = {
            "Nueva votación",
            "Configurar Tiempos",
            "Configura pregunta"
    };
}