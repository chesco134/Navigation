package org.inspira.jcapiz.polivoto.escuchas;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

import org.inspira.jcapiz.polivoto.dialogos.EntradaTexto;
import org.inspira.jcapiz.polivoto.pojo.Votacion;

/**
 * Created by jcapiz on 8/05/16.
 */
public class ClickSobreContenedorTituloVotacion implements View.OnClickListener {

    private Context context;
    private TextView objetivo;
    private Votacion votacion;

    public ClickSobreContenedorTituloVotacion(Context context, TextView objetivo, Votacion votacion) {
        this.context = context;
        this.objetivo = objetivo;
        this.votacion = votacion;
    }

    @Override
    public void onClick(View v) {
        iniciaDialogoDeTexto();
    }

    private void iniciaDialogoDeTexto(){
        EntradaTexto et = new EntradaTexto();
        et.setAccionDialogo(accionEntradaTexto);
        Bundle b = new Bundle();
        b.putString("contenido", objetivo.getText().toString());
        b.putString("mensaje", "Título de votación");
        et.setArguments(b);
        et.show(((AppCompatActivity) context).getSupportFragmentManager(), "cambiar_texto");
    }

    private EntradaTexto.AccionDialogo accionEntradaTexto = new EntradaTexto.AccionDialogo() {

        @Override
        public void accionPositiva(DialogFragment fragment) {
            String nuevoTexto = ((EntradaTexto) fragment).getEntradaDeTexto();
            if(!"".equals(nuevoTexto)){
                objetivo.setText(nuevoTexto);
                votacion.setTitulo(nuevoTexto);
            }
        }

        @Override
        public void accionNegativa(DialogFragment fragment) {
        }
    };
}
