package org.inspira.jcapiz.polivoto.adaptadores;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import org.inspira.jcapiz.polivoto.R;

import java.util.List;

/**
 * Created by jcapiz on 5/01/16.
 */
public class ListaDePreguntas extends BaseAdapter {

    private List<View> lista;
    private Context context;
    private int recursoAinflar;
    private LayoutInflater inflater;

    public ListaDePreguntas(Context context, int recursoAinflar) {
        this.context = context;
        this.recursoAinflar = recursoAinflar;
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return lista.size();
    }

    @Override
    public Object getItem(int position) {
        return lista.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View rootView;
        LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if(convertView != null){
            rootView = convertView;
        }else{
            rootView = inflater.inflate(R.layout.fila_de_lista_de_preguntas, parent, false);

        }
        return rootView;
    }
}
