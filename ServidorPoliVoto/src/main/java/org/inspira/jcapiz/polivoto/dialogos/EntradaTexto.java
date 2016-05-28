package org.inspira.jcapiz.polivoto.dialogos;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import org.inspira.jcapiz.polivoto.R;
import org.inspira.jcapiz.polivoto.escuchas.EfectoDeEnfoque;

import java.io.Serializable;

/**
 * Created by Siempre on 19/02/2016.
 */
public class EntradaTexto extends DialogFragment {

    private String mensaje;
    private String contenido;
    private int tipoDeEntradaDeTexto = -1;

    public interface AccionDialogo extends Serializable{
        void accionPositiva(DialogFragment fragment);
        void accionNegativa(DialogFragment fragment);
    }

    public void setTipoDeEntradaDeTexto(int tipoDeEntradaDeTexto) {
        this.tipoDeEntradaDeTexto = tipoDeEntradaDeTexto;
    }

    private AccionDialogo accionDialogo;

    public String getEntradaDeTexto() {
        return entradaDeTexto.getText().toString().trim();
    }

    public void setEntradaDeTexto(EditText entradaDeTexto) {
        this.entradaDeTexto = entradaDeTexto;
    }

    private EditText entradaDeTexto;

    public void setAccionDialogo(AccionDialogo accionDialogo) {
        this.accionDialogo = accionDialogo;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        Bundle args;
        LayoutInflater inflater = (LayoutInflater)getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View root = inflater.inflate(R.layout.entrada_de_texto, null);
        entradaDeTexto = (EditText)root.findViewById(R.id.entrada_de_texto);
        entradaDeTexto.setOnFocusChangeListener(new EfectoDeEnfoque(getActivity(),
                root.findViewById(R.id.piso_entrada_de_texto)));
        if(savedInstanceState == null){
            args = getArguments();
        }else{
            args = savedInstanceState;
            accionDialogo = (AccionDialogo)args.getSerializable("accion_dialogo");
        }
        contenido = args.getString("contenido");
        entradaDeTexto.setText(contenido);
        if(tipoDeEntradaDeTexto != -1)
            entradaDeTexto.setInputType(tipoDeEntradaDeTexto);
        mensaje = args.getString("mensaje");
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
            entradaDeTexto.setTextColor(Color.WHITE);
        }
        builder.setTitle(mensaje)
                .setPositiveButton(R.string.dialogo_entrada_texto_aceptar, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        accionDialogo.accionPositiva(EntradaTexto.this);
                    }
                })
                .setNegativeButton(R.string.dialogo_entrada_texto_cancelar, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        accionDialogo.accionNegativa(EntradaTexto.this);
                    }
                })
                .setView(root);
        return builder.create();
    }

    @Override
    public void onSaveInstanceState(Bundle outState){
        outState.putString("contenido", contenido);
        outState.putString("mensaje", mensaje);
        outState.putString("texto", entradaDeTexto.getText().toString());
        outState.putSerializable("accion_dialogo", accionDialogo);
    }

}