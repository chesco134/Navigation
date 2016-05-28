package org.inspira.jcapiz.polivoto.escuchas;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import org.inspira.jcapiz.polivoto.adaptadores.AdaptadorDeOpciones;
import org.inspira.jcapiz.polivoto.dialogos.EntradaTexto;

/**
 * Created by jcapiz on 8/05/16.
 */
public class ClickSobreNuevaOpcion implements View.OnClickListener {

    private Context context;
    private AdaptadorDeOpciones adaptador;

    public ClickSobreNuevaOpcion(Context context, AdaptadorDeOpciones adaptador) {
        this.context = context;
        this.adaptador = adaptador;
    }

    @Override
    public void onClick(View v){
        iniciarEntradaDeTexto();
    }

    private void iniciarEntradaDeTexto(){
        EntradaTexto et = new EntradaTexto();
        et.setAccionDialogo(accionDialogo);
        Bundle b = new Bundle();
        b.putString("mensaje", "Agregar opción");
        et.setArguments(b);
        et.show(((AppCompatActivity)context).getSupportFragmentManager(), "Agregar Opción");
    }

    private EntradaTexto.AccionDialogo accionDialogo = new EntradaTexto.AccionDialogo() {
        @Override
        public void accionPositiva(DialogFragment fragment) {
            String texto = ((EntradaTexto) fragment).getEntradaDeTexto();
            if(!"".equals(texto)){
                adaptador.agregarOpcion(texto);
            }
        }

        @Override
        public void accionNegativa(DialogFragment fragment) {}
    };
}
