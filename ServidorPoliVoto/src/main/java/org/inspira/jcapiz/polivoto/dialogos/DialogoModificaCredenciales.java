package org.inspira.jcapiz.polivoto.dialogos;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import org.inspira.jcapiz.polivoto.R;

/**
 * Created by jcapiz on 1/01/16.
 */
public class DialogoModificaCredenciales extends DialogFragment {

    private String currentUserName;
    private EditText passActual;
    private EditText passNuevo;
    private EditText confirmarPass;
    private DialogoDeConsultaSimple.AgenteDeInteraccionConResultado agenteDeInteraccion;

    public void setAgenteDeInteraccion(DialogoDeConsultaSimple.AgenteDeInteraccionConResultado agente){
        agenteDeInteraccion = agente;
    }

    public String getCurrentUserName(){
        return currentUserName;
    }

    public String getPassActual(){
        return passActual.getText().toString();
    }

    public String getPassNuevo(){
        return passNuevo.getText().toString();
    }

    public String getPassConfirmacion(){
        return confirmarPass.getText().toString();
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState){
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rootView = inflater.inflate(R.layout.modificar_credenciales, null, false);
        passActual = (EditText) rootView.findViewById(R.id.pass_actual);
        passNuevo = (EditText) rootView.findViewById(R.id.pass_nuevo);
        confirmarPass = (EditText) rootView.findViewById(R.id.confirmar_pass);
        passActual.setTypeface(Typeface.createFromAsset(getActivity().getAssets(),"Roboto-Regular.ttf"));
        passNuevo.setTypeface(Typeface.createFromAsset(getActivity().getAssets(),"Roboto-Regular.ttf"));
        confirmarPass.setTypeface(Typeface.createFromAsset(getActivity().getAssets(),"Roboto-Regular.ttf"));
        confirmarPass.setOnEditorActionListener(new DoneHandler());
        builder.setView(rootView);
        builder.setPositiveButton("Confirmar", new PositiveClickHandler());
        if( savedInstanceState == null ){
            currentUserName = getArguments().getString("current_user_name");
        }else{
            currentUserName = savedInstanceState.getString("current_user_name");
            passActual.setText(savedInstanceState.getString("pass_actual"));
            passNuevo.setText(savedInstanceState.getString("pass_nuevo"));
            confirmarPass.setText(savedInstanceState.getString("confirmar_pass"));
        }
        builder.setTitle("Modificando " + currentUserName);
        return builder.create();
    }

    @Override
    public void onSaveInstanceState(Bundle outState){
        outState.putString("pass_actual", passActual.getText().toString());
        outState.putString("pass_nuevo", passNuevo.getText().toString());
        outState.putString("confirmar_pass", confirmarPass.getText().toString());
        outState.putString("current_user_name", currentUserName);
        super.onSaveInstanceState(outState);
    }

    private class PositiveClickHandler implements DialogInterface.OnClickListener{

        @Override
        public void onClick(DialogInterface dialog, int which) {
            agenteDeInteraccion.clickSobreAccionPositiva(DialogoModificaCredenciales.this);
        }
    }

    private class DoneHandler implements TextView.OnEditorActionListener{

        @Override
        public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
            boolean handled = false;
            if(actionId == EditorInfo.IME_ACTION_DONE){
                AlertDialog mmm = (AlertDialog) getDialog();
                Button pButton = mmm.getButton(AlertDialog.BUTTON_POSITIVE);
                pButton.performClick();
                handled = true;
            }
            return handled;
        }
    }
}