package org.inspira.jcapiz.polivoto.adaptadores;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import org.inspira.jcapiz.polivoto.R;
import org.inspira.jcapiz.polivoto.pojo.Pregunta;

/**
 * Created by jcapiz on 8/05/16.
 */
public class AdaptadorDeOpciones extends BaseAdapter {

    private LayoutInflater inflater;
    private Pregunta pregunta;

    public AdaptadorDeOpciones(Pregunta pregunta, Context context) {
        this.pregunta = pregunta;
        this.inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public void agregarOpcion(String opcion){
        pregunta.agregarOpcion(opcion);
        notifyDataSetChanged();
    }

    public void eliminarOpcion(String opcion){
        pregunta.eliminarOpcion(opcion);
        notifyDataSetChanged();
    }

    public void cambiarEnunciadoOpcion(String enunciadoAnterior, String nuevoEnunciado){
        pregunta.cambiarEnunciadoOpcion(enunciadoAnterior, nuevoEnunciado);
        notifyDataSetChanged();
    }

    public int buscarOpcion(String opcion){
        return pregunta.buscarOpcion(opcion);
    }

    @Override
    public int getCount() {
        return pregunta.getOpciones().size();
    }

    @Override
    public Object getItem(int position) {
        return pregunta.obtenerOpcion(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView != null ? convertView
                : inflater.inflate(R.layout.my_simple_list_item, parent, false);
        TextView textView = (TextView)view;
        textView.setText(pregunta.obtenerOpcion(position).getReactivo());
        return view;
    }
}
