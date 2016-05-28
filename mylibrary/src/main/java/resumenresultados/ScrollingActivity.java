package resumenresultados;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.inspira.jcapiz.mylibrary.R;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;
import java.util.Date;

import resumenresultados.shared.Opcion;
import resumenresultados.shared.Pregunta;
import resumenresultados.shared.Votacion;
import servidorweb.ContactoServidorWeb;
import servidorweb.FormatoSolicitud;

public class ScrollingActivity extends AppCompatActivity {

    private TextView escuela;
    private TextView fechaInicio;
    private TextView fechaFin;
    private TextView matriculaTotal;
    private TextView totalParticipantes;
    private TextView porcentajeDeParticipacion;
    private LinearLayout tablaDeResultados;
    private Votacion votacion;
    private String titulo;
    private String encabezadoDetallesDeParticipantes;
    private String[] detallesParticipantes;
    private int matricula;
    private int participantes;
    private float porcentajeParticipacion;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scrolling);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Aún en construcción", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
        escuela = (TextView)findViewById(R.id.detalles_votacion_valor_escuela);
        fechaInicio = (TextView)findViewById(R.id.detalles_votacion_valor_fecha_inicio);
        fechaFin = (TextView) findViewById(R.id.detalles_votacion_valor_fecha_fin);
        matriculaTotal = (TextView) findViewById(R.id.detalles_votacion_valor_matricula_total);
        totalParticipantes = (TextView) findViewById(R.id.detalles_votacion_valor_total_participantes);
        porcentajeDeParticipacion = (TextView) findViewById(R.id.detalles_votacion_valor_porcentaje_participacion);
        tablaDeResultados = (LinearLayout) findViewById(R.id.detalles_votacion_contenedor_tablas_de_resumen);
        if( savedInstanceState == null ){
            Bundle extras = getIntent().getExtras();
            votacion = (Votacion)extras.getSerializable("votacion");
            titulo = extras.getString("titulo");
            escuela.setText(extras.getString("escuela"));
            fechaInicio.setText(formatearFecha(extras.getLong("fecha_inicio")));
            fechaFin.setText(formatearFecha(extras.getLong("fecha_fin")));
            participantes = extras.getInt("total_participantes");
            totalParticipantes.setText("" + participantes);
            matricula = extras.getInt("matricula_total");
            String textoDeMatricula = "" + (matricula == -1 ? 0 : matricula);
            matriculaTotal.setText(textoDeMatricula);
            porcentajeParticipacion = extras.getFloat("porcentaje_de_participacion") * 100;
            String loQueVamosAponer = String.format("%.2f", porcentajeParticipacion) + "%";
            porcentajeDeParticipacion.setText(loQueVamosAponer);
            detallesParticipantes = extras.getStringArray("rows");
            encabezadoDetallesDeParticipantes = extras.getString("header");
            colocaElementosDeResultado();
        }
        toolbar.setTitle(titulo);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public void onSaveInstanceState(Bundle outState){
        outState.putString("titulo", titulo);
        outState.putString("escuela", escuela.getText().toString());
        outState.putString("fecha_inicio", fechaInicio.getText().toString());
        outState.putString("fecha_fin", fechaFin.getText().toString());
        outState.putInt("matricula_total", matricula);
        outState.putInt("total_participantes", participantes);
        outState.putFloat("porcentaje_de_participacion", porcentajeParticipacion);
        outState.putSerializable("votacion", votacion);
        outState.putStringArray("rows", detallesParticipantes);
        outState.putString("header", encabezadoDetallesDeParticipantes);
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState){
        titulo = savedInstanceState.getString("titulo");
        escuela.setText(savedInstanceState.getString("escuela"));
        fechaInicio.setText(savedInstanceState.getString("fecha_inicio"));
        fechaFin.setText(savedInstanceState.getString("fecha_fin"));
        matricula = savedInstanceState.getInt("matricula_total");
        String textoDeMatricula = "" + (matricula == -1 ? 0 : matricula);
        matriculaTotal.setText(textoDeMatricula);
        participantes = savedInstanceState.getInt("total_participantes");
        totalParticipantes.setText("" + participantes);
        porcentajeParticipacion = savedInstanceState.getFloat("porcentaje_de_participacion");
        String loQueVamosAponer = String.format("%.2f",porcentajeParticipacion)+"%";
        porcentajeDeParticipacion.setText(loQueVamosAponer);
        detallesParticipantes = savedInstanceState.getStringArray("rows");
        encabezadoDetallesDeParticipantes = savedInstanceState.getString("header");
        votacion = (Votacion) savedInstanceState.getSerializable("votacion");
        if(tablaDeResultados.getChildCount() == 0)
            colocaElementosDeResultado();
        getSupportActionBar().setTitle(titulo);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.menu_scrolling, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        if(item.getItemId() == R.id.action_settings){
            new Thread(){
                @Override
                public void run(){
                    String respuesta = ContactoServidorWeb.subirDatosDeVotacion(FormatoSolicitud.armarSolicitud(votacion));
                    try {
                        if(respuesta != null) {
                            JSONObject miJ = new JSONObject(respuesta);
                            String mensaje = null;
                            String link = null;
                            switch (miJ.getString("operacion")) {
                                case "Existe":
                                    mensaje = ("Esta votación ya ha sido compartida en el sitio web") +
                                            ("Puedes consultar el resultado en el siguiente enlace:");
                                    link = miJ.getString("link");
                                    break;
                                case "Correcto":
                                    mensaje = ("La votación se ha compartido en el sitio web exitosamente") +
                                            ("Puedes consultar el resultado en el siguiente enlace:");
                                    link = miJ.getString("link");
                                    break;
                                case "Error":
                                    mensaje = ("Error al ejecutar query");
                                    break;
                            }
                            showInformationDialog(mensaje, link);
                        }else{
                            showInformationDialog("Servicio por el momento no disponible", null);
                        }
                    }catch(JSONException e){
                        e.printStackTrace();
                    }
                }
            }.start();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showInformationDialog(final String message, final String link){
        new DialogFragment(){
            @Override
            public Dialog onCreateDialog(Bundle savedInstanceState){
                View view = ((LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.informacion_de_subir_resultados, null, false);
                TextView mensaje = (TextView)view.findViewById(R.id.informacion_de_subir_resultados_texto);
                TextView linkTV = (TextView)view.findViewById(R.id.informacion_de_subir_resultados_vinculo);
                mensaje.setText(message);
                linkTV.setText("");
                if(link != null) linkTV.setText(link);
                return new AlertDialog.Builder(getContext())
                        .setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        })
                        .setView(view)
                        .create();
            }

            @Override
            public void onPause(){
                super.onPause();
                dismiss();
            }
        }.show(getSupportFragmentManager(), "Informando");
    }

    private String formatearFecha(long fechaMilis){
        Date fechaInicio = new Date(fechaMilis);
        Calendar calendarioInicio = Calendar.getInstance();
        calendarioInicio.setTime(fechaInicio);
        int diaDelMes = calendarioInicio.get(Calendar.DAY_OF_MONTH);
        String entradaDiaDelMes = String.valueOf(calendarioInicio.get(Calendar.DAY_OF_MONTH));
        String textDiaDelMes = diaDelMes < 10 ? "0" + entradaDiaDelMes : entradaDiaDelMes;
        int mes = calendarioInicio.get(Calendar.MONTH) + 1;
        String entradaMes = String.valueOf(mes);
        String textoMes = mes < 10 ? "0" + entradaMes : entradaMes;
        int year = calendarioInicio.get(Calendar.YEAR);
        String entradaYear = String.valueOf(year);
        String textoYear = year < 10 ? "0" + entradaYear : entradaYear;
        int horas = calendarioInicio.get(Calendar.HOUR_OF_DAY);
        int minutos = calendarioInicio.get(Calendar.MINUTE);
        int segundos = calendarioInicio.get(Calendar.SECOND);
        return textDiaDelMes+"/"+textoMes+"/"+textoYear+" "+(horas < 10 ? "0"+horas:horas)+":"+(minutos < 10 ? "0"+minutos:minutos)+":"+(segundos<10?"0"+segundos:segundos);
    }

    private void colocaElementosDeResultado(){
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        int totalVotosPorPregunta;
        for(Pregunta pregunta : votacion.getPreguntas()){
            totalVotosPorPregunta = 0;
            View resultadoPorPregunta = inflater.inflate(R.layout.resultado_por_pregunta, tablaDeResultados, false);
            TextView valorPregunta = (TextView) resultadoPorPregunta.findViewById(R.id.resultado_por_pregunta_valor_pregunta);
            valorPregunta.setTypeface(Typeface.createFromAsset(getAssets(),"RobotoCondensed-Regular.ttf"));
            LinearLayout contenedorDeOpciones = (LinearLayout) resultadoPorPregunta.findViewById(R.id.resultado_por_pregunta_lista_opciones);
            valorPregunta.setText(pregunta.getTitulo());
            for(int i=0; i<pregunta.obtenerCantidadDeOpciones(); i++){
                View opcionesDePregunta = inflater.inflate(R.layout.entrada_cuenta_opcion_pregunta,contenedorDeOpciones,false);
                TextView valorDeOpcion = (TextView) opcionesDePregunta.findViewById(R.id.entrada_cuenta_opcion_pregunta_valor_pregunta);
                valorDeOpcion.setTypeface(Typeface.createFromAsset(getAssets(),"Roboto-Regular.ttf"));
                Opcion opcionActual = pregunta.obtenerOpcion(i);
                valorDeOpcion.setText(opcionActual.getNombre());
                String textoValorCantidadOpcion = "" + opcionActual.getCantidad();
                TextView valorCantidadOpcion = (TextView) opcionesDePregunta.findViewById(R.id.entrada_cuenta_opcion_pregunta_valor_cantidad);
                valorCantidadOpcion.setTypeface(Typeface.createFromAsset(getAssets(),"Roboto-Regular.ttf"));
                valorCantidadOpcion.setText(textoValorCantidadOpcion);
                contenedorDeOpciones.addView(opcionesDePregunta);
                totalVotosPorPregunta += opcionActual.getCantidad();
            }
            tablaDeResultados.addView(resultadoPorPregunta);
            TextView textoTotal = (TextView) resultadoPorPregunta.findViewById(R.id.resultado_por_pregunta_texto_total);
            textoTotal.setTypeface(Typeface.createFromAsset(getAssets(),"Roboto-Regular.ttf"));
            TextView total = (TextView) resultadoPorPregunta.findViewById(R.id.resultado_por_pregunta_valor_total);
            total.setTypeface(Typeface.createFromAsset(getAssets(), "RobotoCondensed-Bold.ttf"));
            String valorTotal = "" + totalVotosPorPregunta;
            total.setText(valorTotal);
        }
        View listaDeParticipantes = inflater.inflate(R.layout.detalles_de_quienes_han_participado, tablaDeResultados, false);
        TextView detallesDeQuienesHanParticipado = (TextView) listaDeParticipantes.findViewById(R.id.header);
        detallesDeQuienesHanParticipado.setText(encabezadoDetallesDeParticipantes);
        detallesDeQuienesHanParticipado.setTypeface(Typeface.createFromAsset(getAssets(), "RobotoCondensed-Bold.ttf"));
        tablaDeResultados.addView(listaDeParticipantes);
        for(String fila : detallesParticipantes){
            View view = inflater.inflate(R.layout.lista_detalles_participante, tablaDeResultados,false);
            TextView texto = (TextView)view.findViewById(R.id.lista_detalles_participante_valor_boleta);
            String[] entradas = fila.split("\n");
            texto.setText(entradas[0]);
            texto.setTypeface(Typeface.createFromAsset(getAssets(), "Roboto-Bold.ttf"));
            TextView textoTiempos = (TextView)view.findViewById(R.id.lista_detalles_participante_valor_tiempos);
            String contenidoTextoTiempos = entradas[1] + "\n" + entradas[2];
            textoTiempos.setText(contenidoTextoTiempos);
            textoTiempos.setTypeface(Typeface.createFromAsset(getAssets(), "Roboto-Regular.ttf"));
            tablaDeResultados.addView(view);
        }
    }
}