package org.inspira.jcapiz.polivoto.adaptadores;

import android.app.ActionBar;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by jcapiz on 1/01/16.
 */
public class ListaDeCuadrosDeSeleccion extends BaseAdapter {

    private String[] ubicaciones;
    private Context context;
    private List<String> elementosSeleccionados;

    public ListaDeCuadrosDeSeleccion(Context context, String[] ubicaciones){
        this.context = context;
        this.ubicaciones = ubicaciones;
        elementosSeleccionados = new ArrayList<>();
    }

    @Override
    public int getCount() {
        return ubicaciones.length;
    }

    @Override
    public Object getItem(int position) {
        return ubicaciones[position];
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View rootView;
        if(convertView != null){
            rootView = convertView;
        }else{
            CheckBox box = new CheckBox(context);
            box.setText(ubicaciones[position]);
            box.setSelected(false);
            rootView = box;
        }
        rootView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CheckBox box = (CheckBox)view;
                if( box.isChecked() )
                    elementosSeleccionados.add(box.getText().toString());
                else
                    elementosSeleccionados.remove(box.getText().toString());
            }
        });
        return rootView;
    }

    public List<String> getElementosSeleccionados(){
        return elementosSeleccionados;
    }
}
