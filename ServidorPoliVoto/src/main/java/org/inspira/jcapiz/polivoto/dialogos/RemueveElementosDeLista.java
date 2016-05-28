package org.inspira.jcapiz.polivoto.dialogos;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.widget.ListView;

import org.inspira.jcapiz.polivoto.adaptadores.ListaDeCuadrosDeSeleccion;

import java.util.List;

/**
 * Created by jcapiz on 2/01/16.
 */
public class RemueveElementosDeLista extends DialogFragment {

    private DialogoDeConsultaSimple.AgenteDeInteraccionConResultado agenteDeInteraccion;
    private ListaDeCuadrosDeSeleccion adaptador;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState){
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        String titulo = getArguments().getString("titulo");
        builder.setTitle(titulo);
        ListView list = new ListView(getContext());
        String[] elementos = getArguments().getStringArray("elementos");
        adaptador = new ListaDeCuadrosDeSeleccion(getContext(),elementos);
        list.setAdapter(adaptador);
        builder.setView(list);
        builder.setPositiveButton("Confirmar", new ClickSobreAccionPositiva());
        return builder.create();
    }

    public void setAgenteDeInteraccion(DialogoDeConsultaSimple.AgenteDeInteraccionConResultado agenteDeInteraccion) {
        this.agenteDeInteraccion = agenteDeInteraccion;
    }

    public List<String> getElementosSeleccionados() {
        return adaptador.getElementosSeleccionados();
    }

    private class ClickSobreAccionPositiva implements DialogInterface.OnClickListener{

        @Override
        public void onClick(DialogInterface dialog, int which) {
            agenteDeInteraccion.clickSobreAccionPositiva(RemueveElementosDeLista.this);
        }
    }

    public static void prepareElements(Integer[] elements){
        Integer hold;
        for(int i=0; i<elements.length; i++){
            for(int j=i+1; j<elements.length; j++){
                if(elements[i] < elements[j]){
                    hold = elements[i];
                    elements[i] = elements[j];
                    elements[j] = hold;
                }
            }
        }
    }
}