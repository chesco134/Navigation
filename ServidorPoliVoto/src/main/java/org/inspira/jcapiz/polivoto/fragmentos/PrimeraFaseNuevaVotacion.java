package org.inspira.jcapiz.polivoto.fragmentos;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SwitchCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.inspira.jcapiz.polivoto.R;
import org.inspira.jcapiz.polivoto.adaptadores.AdaptadorDePreguntas;
import org.inspira.jcapiz.polivoto.escuchas.ClickSobreContenedorDeFechaVotacion;
import org.inspira.jcapiz.polivoto.escuchas.ClickSobreContenedorTituloVotacion;
import org.inspira.jcapiz.polivoto.escuchas.ClickSobreElementoDeListaPreguntas;
import org.inspira.jcapiz.polivoto.escuchas.ClickSobreNuevaPregunta;
import org.inspira.jcapiz.polivoto.escuchas.ClickSobreSwitchVolverGlobal;
import org.inspira.jcapiz.polivoto.pojo.ControlDeRetorno;
import org.inspira.jcapiz.polivoto.pojo.Pregunta;
import org.inspira.jcapiz.polivoto.pojo.Votacion;
import org.inspira.jcapiz.polivoto.proveedores.ProveedorDeRecursos;

/**
 * Created by jcapiz on 8/05/16.
 */
public class PrimeraFaseNuevaVotacion extends Fragment {

    private AdaptadorDePreguntas adaptador;
    private Votacion votacion;

    @Override
    public void onAttach(Context context){
        super.onAttach(context);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle b){
        View rootView = inflater.inflate(R.layout.primera_fase_nueva_votacion, parent, false);
        TextView tituloTV = (TextView) rootView.findViewById(R.id.primera_fase_nueva_votacion_titulo);
        TextView fechaInicialTV = (TextView) rootView.findViewById(R.id.primera_fase_nueva_votacion_fecha_inicial);
        TextView fechaFinalTV = (TextView) rootView.findViewById(R.id.primera_fase_nueva_votacion_fecha_final);
        ListView listaDePreguntasLV = (ListView) rootView.findViewById(R.id.primera_fase_nueva_votacion_lista_preguntas);
        Bundle a = getArguments();
        Votacion votacion = (Votacion) a.getSerializable("votacion");
        assert votacion != null;
        tituloTV.setText(votacion.getTitulo());
        fechaInicialTV.setText(ProveedorDeRecursos.obtenerFecha(new java.util.Date(votacion.getFechaInicio())));
        fechaFinalTV.setText(ProveedorDeRecursos.obtenerFecha(new java.util.Date(votacion.getFechaFin())));
        adaptador = new AdaptadorDePreguntas(getContext(), votacion);
        listaDePreguntasLV.setAdapter(adaptador);
        listaDePreguntasLV.setOnItemClickListener(new ClickSobreElementoDeListaPreguntas(((AppCompatActivity) getContext()).getSupportFragmentManager(), adaptador, (ControlDeRetorno) a.getSerializable("control_de_retorno")));
        RelativeLayout contenedorTitulo = (RelativeLayout) rootView.findViewById(R.id.primera_fase_nueva_votacion_contenedor_titulo);
        contenedorTitulo.setOnClickListener(new ClickSobreContenedorTituloVotacion(getContext(), tituloTV, votacion));
        RelativeLayout contenedorFechaInicial = (RelativeLayout) rootView.findViewById(R.id.primera_fase_nueva_votacion_contenedor_fecha_inicial);
        RelativeLayout contenedorFechaFinal = (RelativeLayout) rootView.findViewById(R.id.primera_fase_nueva_votacion_contenedor_fecha_final);
        contenedorFechaInicial.setOnClickListener(new ClickSobreContenedorDeFechaVotacion(getContext(), votacion, ClickSobreContenedorDeFechaVotacion.FECHA_INICIAL, fechaInicialTV, fechaFinalTV));
        contenedorFechaFinal.setOnClickListener(new ClickSobreContenedorDeFechaVotacion(getContext(), votacion, ClickSobreContenedorDeFechaVotacion.FECHA_FINAL, fechaInicialTV, fechaFinalTV));
        TextView etiquetaNuevaPregunta = (TextView) rootView.findViewById(R.id.primera_fase_nueva_votacion_etiqueta_nueva_pregunta);
        etiquetaNuevaPregunta.setOnClickListener(new ClickSobreNuevaPregunta(getContext(), adaptador));
        SwitchCompat volverGlobal = (SwitchCompat) rootView.findViewById(R.id.primera_fase_nueva_votacion_switch_global);
        volverGlobal.setOnCheckedChangeListener(new ClickSobreSwitchVolverGlobal(getContext(), votacion));
        volverGlobal.setChecked(votacion.isGlobal());
        this.votacion = votacion;
        return rootView;
    }

    @Override
    public void onResume(){
        super.onResume();
        agregarPreguntas(votacion);
    }

    private void agregarPreguntas(Votacion votacion){
        for(Pregunta pregunta : votacion.getPreguntas())
            adaptador.agregarPregunta(pregunta);
    }
}
