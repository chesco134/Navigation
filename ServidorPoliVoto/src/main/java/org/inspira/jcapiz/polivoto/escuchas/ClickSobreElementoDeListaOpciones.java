package org.inspira.jcapiz.polivoto.escuchas;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import org.inspira.jcapiz.polivoto.adaptadores.AdaptadorDeOpciones;
import org.inspira.jcapiz.polivoto.dialogos.EntradaTexto;
import org.inspira.jcapiz.polivoto.pojo.Opcion;

/**
 * Created by jcapiz on 8/05/16.
 */
public class ClickSobreElementoDeListaOpciones implements ListView.OnItemClickListener {

    private Opcion opcion;
    private Context context;
    private AdaptadorDeOpciones adaptador;

    public ClickSobreElementoDeListaOpciones(Context context, AdaptadorDeOpciones adaptador) {
        this.context = context;
        this.adaptador = adaptador;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        opcion = (Opcion) adaptador.getItem(adaptador.buscarOpcion(((TextView)view).getText().toString()));
        iniciarEntradaDeTexto();
    }

    private void iniciarEntradaDeTexto(){
        EntradaTexto et = new EntradaTexto();
        et.setAccionDialogo(accionDialogo);
        Bundle b = new Bundle();
        b.putString("mensaje", "Modificar opción");
        b.putString("contenido", opcion.getReactivo());
        et.setArguments(b);
        et.show(((AppCompatActivity)context).getSupportFragmentManager(), "Cambiar enunciado de opción");
    }

    private EntradaTexto.AccionDialogo accionDialogo = new EntradaTexto.AccionDialogo() {
        @Override
        public void accionPositiva(DialogFragment fragment) {
            String texto = ((EntradaTexto) fragment).getEntradaDeTexto();
            if(!"".equals(texto) && !opcion.getReactivo().equals(texto)){
                adaptador.cambiarEnunciadoOpcion(opcion.getReactivo(), texto);
            }
        }

        @Override
        public void accionNegativa(DialogFragment fragment) {}
    };
}
