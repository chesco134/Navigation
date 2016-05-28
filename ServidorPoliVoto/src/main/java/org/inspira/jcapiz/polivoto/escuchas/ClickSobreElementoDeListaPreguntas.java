package org.inspira.jcapiz.polivoto.escuchas;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import org.inspira.jcapiz.polivoto.R;
import org.inspira.jcapiz.polivoto.adaptadores.AdaptadorDePreguntas;
import org.inspira.jcapiz.polivoto.fragmentos.SegundaFaseNuevaVotacion;
import org.inspira.jcapiz.polivoto.pojo.ControlDeRetorno;
import org.inspira.jcapiz.polivoto.pojo.Pregunta;
import org.inspira.jcapiz.polivoto.pojo.Votacion;

/**
 * Created by jcapiz on 8/05/16.
 */
public class ClickSobreElementoDeListaPreguntas implements ListView.OnItemClickListener {

    private FragmentManager fm;
    private AdaptadorDePreguntas adaptador;
    private ControlDeRetorno controlDeRetorno;
    private Pregunta pregunta;

    public ClickSobreElementoDeListaPreguntas(FragmentManager fm, AdaptadorDePreguntas adaptador, ControlDeRetorno controlDeRetorno) {
        this.fm = fm;
        this.adaptador = adaptador;
        this.controlDeRetorno = controlDeRetorno;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        TextView enunciadoPreguntaTV = (TextView)view.findViewById(R.id.elemento_fila_pregunta_enunciado_pregunta);
        String enunciadoPregunta = enunciadoPreguntaTV.getText().toString();
        pregunta = (Pregunta) adaptador.getItem(adaptador.buscarPregunta(enunciadoPregunta));
        colocaFragmento();
    }

    private void colocaFragmento(){
        SegundaFaseNuevaVotacion sfnv = new SegundaFaseNuevaVotacion();
        Bundle b = new Bundle();
        b.putSerializable("pregunta", pregunta);
        sfnv.setArguments(b);
        if(controlDeRetorno != null) {
            controlDeRetorno.increaseValue();
            Log.d("EscuchaClickLista", "---> " + controlDeRetorno);
        }
        fm.beginTransaction()
                .replace(R.id.contenedor_de_un_fragmento_este_contenedor, sfnv, pregunta.getEnunciado())
                .addToBackStack(pregunta.getEnunciado())
                .commit();
    }
}
