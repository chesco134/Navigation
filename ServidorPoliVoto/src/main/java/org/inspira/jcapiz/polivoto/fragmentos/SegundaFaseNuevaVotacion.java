package org.inspira.jcapiz.polivoto.fragmentos;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.inspira.jcapiz.polivoto.R;
import org.inspira.jcapiz.polivoto.adaptadores.AdaptadorDeOpciones;
import org.inspira.jcapiz.polivoto.escuchas.ClickSobreContenedorEnunciadoPregunta;
import org.inspira.jcapiz.polivoto.escuchas.ClickSobreElementoDeListaOpciones;
import org.inspira.jcapiz.polivoto.escuchas.ClickSobreNuevaOpcion;
import org.inspira.jcapiz.polivoto.pojo.Pregunta;

/**
 * Created by jcapiz on 8/05/16.
 */
public class SegundaFaseNuevaVotacion extends Fragment {

    @Override
    public void onAttach(Context context){
        super.onAttach(context);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle b){
        View rootView = inflater.inflate(R.layout.segunda_fase_nueva_votacion, parent, false);
        RelativeLayout contenedorPregunta = (RelativeLayout) rootView.findViewById(R.id.segunda_fase_nueva_votacion_contenedor_enunciado_pregunta);
        TextView enunciadoPregunta = (TextView) rootView.findViewById(R.id.segunda_fase_nueva_votacion_enunciado_pregunta);
        Bundle a = getArguments();
        Pregunta pregunta = (Pregunta)a.getSerializable("pregunta");
        assert pregunta != null;
        enunciadoPregunta.setText(pregunta.getEnunciado());
        contenedorPregunta.setOnClickListener(new ClickSobreContenedorEnunciadoPregunta(getContext(), pregunta, enunciadoPregunta));
        TextView etiquetaNuevaOpcion = (TextView) rootView.findViewById(R.id.segunda_fase_nueva_votacion_etiqueta_lista_de_opciones);
        ListView listaDeOpciones = (ListView) rootView.findViewById(R.id.segunda_fase_nueva_votacion_lista_opciones);
        AdaptadorDeOpciones adaptador = new AdaptadorDeOpciones(pregunta, getContext());
        listaDeOpciones.setAdapter(adaptador);
        listaDeOpciones.setOnItemClickListener(new ClickSobreElementoDeListaOpciones(getContext(), adaptador));
        etiquetaNuevaOpcion.setOnClickListener(new ClickSobreNuevaOpcion(getContext(), adaptador));
        return rootView;
    }
}
