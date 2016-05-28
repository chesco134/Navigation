package org.inspira.jcapiz.polivoto.adaptadores;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import org.inspira.jcapiz.polivoto.R;
import org.inspira.jcapiz.polivoto.pojo.Pregunta;
import org.inspira.jcapiz.polivoto.pojo.Votacion;

/**
 * Created by jcapiz on 8/05/16.
 */
public class AdaptadorDePreguntas extends BaseAdapter {

    private Votacion votacion;
    private LayoutInflater inflater;

    public AdaptadorDePreguntas(Context context, Votacion votacion){
        this.votacion = votacion;
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public void agregarPregunta(Pregunta pregunta){
        votacion.agregarPregunta(pregunta);
        notifyDataSetChanged();
    }

    public void agregarPregunta(String enunciado){
        votacion.agregarPregunta(enunciado);
        notifyDataSetChanged();
    }

    public Pregunta eliminarPregunta(String enunciado){
        Pregunta preguntaEliminada = votacion.eliminarPregunta(enunciado);
        notifyDataSetChanged();
        return preguntaEliminada;
    }

    public int buscarPregunta(String enunciado){
        return votacion.buscarPregunta(enunciado);
    }

    public void agregarOpcion(int posicionPregunta, String enunciadoOpcion){
        votacion.agregarOpcion(posicionPregunta, enunciadoOpcion);
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return votacion.cantidadPreguntas();
    }

    @Override
    public Object getItem(int position) {
        return votacion.obtenerPregunta(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView != null ? convertView
                : inflater.inflate(R.layout.elemento_fila_pregunta, parent, false);
        Pregunta pregunta = votacion.obtenerPregunta(position);
        TextView enunciado = (TextView) view.findViewById(R.id.elemento_fila_pregunta_enunciado_pregunta);
        enunciado.setText(pregunta.getEnunciado());
        TextView cantidadDeOpciones = (TextView) view.findViewById(R.id.elemento_fila_pregunta_cantidad_opciones);
        cantidadDeOpciones.setText(String.valueOf(pregunta.getOpciones().size()));
        return view;
    }
}
