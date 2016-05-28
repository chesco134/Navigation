package org.inspira.jcapiz.polivoto.escuchas;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

import org.inspira.jcapiz.polivoto.dialogos.EntradaTexto;
import org.inspira.jcapiz.polivoto.pojo.Pregunta;

/**
 * Created by jcapiz on 8/05/16.
 */
public class ClickSobreContenedorEnunciadoPregunta implements View.OnClickListener {

    private Context context;
    private Pregunta pregunta;
    private TextView etiquetaEnunciadoPregunta;

    public ClickSobreContenedorEnunciadoPregunta(Context context, Pregunta pregunta, TextView etiquetaEnunciadoPregunta) {
        this.context = context;
        this.pregunta = pregunta;
        this.etiquetaEnunciadoPregunta = etiquetaEnunciadoPregunta;
    }

    @Override
    public void onClick(View v){
        iniciaEntradaDeTexto();
    }

    private void iniciaEntradaDeTexto(){
        EntradaTexto et = new EntradaTexto();
        et.setAccionDialogo(accionDialogo);
        Bundle b = new Bundle();
        b.putString("mensaje", "Modificar pregunta");
        et.setArguments(b);
        et.show(((AppCompatActivity)context).getSupportFragmentManager(), "Cambiar Enunciado de Pregunta");
    }

    private EntradaTexto.AccionDialogo accionDialogo = new EntradaTexto.AccionDialogo() {
        @Override
        public void accionPositiva(DialogFragment fragment) {
            String texto = ((EntradaTexto) fragment).getEntradaDeTexto();
            if(!"".equals(texto) && !pregunta.getEnunciado().equals(texto)){
                etiquetaEnunciadoPregunta.setText(texto);
                pregunta.setEnunciado(texto);
            }
        }

        @Override
        public void accionNegativa(DialogFragment fragment) {}
    };
}
