package org.inspira.jcapiz.polivoto.escuchas;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import org.inspira.jcapiz.polivoto.adaptadores.AdaptadorDePreguntas;
import org.inspira.jcapiz.polivoto.dialogos.EntradaTexto;

/**
 * Created by jcapiz on 8/05/16.
 */
public class ClickSobreNuevaPregunta implements View.OnClickListener {

    private Context context;
    private AdaptadorDePreguntas adaptador;

    public ClickSobreNuevaPregunta(Context context, AdaptadorDePreguntas adaptador) {
        this.context = context;
        this.adaptador = adaptador;
    }

    @Override
    public void onClick(View v) {
        iniciaDialogoDeEntradaDeTexto();
    }

    private void iniciaDialogoDeEntradaDeTexto(){
        EntradaTexto et = new EntradaTexto();
        et.setAccionDialogo(accionEntradaTexto);
        Bundle b = new Bundle();
        b.putString("mensaje", "Agregar pregunta");
        et.setArguments(b);
        et.show(((AppCompatActivity)context).getSupportFragmentManager(), "Agregar Pregunta");
    }

    private EntradaTexto.AccionDialogo accionEntradaTexto = new EntradaTexto.AccionDialogo() {
        @Override
        public void accionPositiva(DialogFragment fragment) {
            String texto = ((EntradaTexto) fragment).getEntradaDeTexto();
            if(!"".equals(texto)){
                adaptador.agregarPregunta(texto);
            }
        }

        @Override
        public void accionNegativa(DialogFragment fragment) {

        }
    };
}
