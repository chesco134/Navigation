package org.inspira.jcapiz.polivoto.fragmentos;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

import org.inspira.jcapiz.polivoto.*;
import org.inspira.jcapiz.polivoto.database.Votaciones;
import org.inspira.jcapiz.polivoto.actividades.Bienvenida;
import org.inspira.jcapiz.polivoto.seguridad.MD5Hash;

import java.util.Arrays;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by jcapiz on 29/12/15.
 */
public class ClavesUsuario extends Fragment {

    private EditText capturista;
    private EditText consultor;
    private EditText participante;
    private EditText capturistaConf;
    private EditText consultorConf;
    private EditText participanteConf;
    private Votaciones db;

    @Override
    public void onAttach(Context ctx){
        super.onAttach(ctx);
        db = new Votaciones(ctx);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup root, Bundle savedInstanceState){
        View rootView = inflater.inflate(R.layout.claves_usuarios,root,false);
        setHasOptionsMenu(true);
        capturista = (EditText) rootView.findViewById(R.id.clave_de_capturista);
        consultor = (EditText) rootView.findViewById(R.id.clave_de_consultor);
        participante = (EditText) rootView.findViewById(R.id.clave_de_participante);
        capturistaConf = (EditText) rootView.findViewById(R.id.clave_de_capturista_confirmar);
        consultorConf = (EditText) rootView.findViewById(R.id.clave_de_consultor_confirmar);
        participanteConf = (EditText) rootView.findViewById(R.id.clave_de_participante_confirmar);
        capturista.setTypeface(Typeface.createFromAsset(getActivity().getAssets(), "Roboto-Regular.ttf"));
        consultor.setTypeface(Typeface.createFromAsset(getActivity().getAssets(),"Roboto-Regular.ttf"));
        capturistaConf.setTypeface(Typeface.createFromAsset(getActivity().getAssets(), "Roboto-Regular.ttf"));
        consultorConf.setTypeface(Typeface.createFromAsset(getActivity().getAssets(), "Roboto-Regular.ttf"));
        participanteConf.setTypeface(Typeface.createFromAsset(getActivity().getAssets(), "Roboto-Regular.ttf"));
        participante.setTypeface(Typeface.createFromAsset(getActivity().getAssets(), "Roboto-Regular.ttf"));
        participanteConf.setOnEditorActionListener(new AccionSobreTeclaListo()); // May not be good
        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState){
        super.onActivityCreated(savedInstanceState);
        if(savedInstanceState != null) {
            capturista.setText(savedInstanceState.getString("capturista"));
            consultor.setText(savedInstanceState.getString("consultor"));
            participante.setText(savedInstanceState.getString("participante"));
            capturistaConf.setText(savedInstanceState.getString("capturista_conf"));
            consultorConf.setText(savedInstanceState.getString("consultor_conf"));
            participanteConf.setText(savedInstanceState.getString("participante_conf"));
        }
        try{
            ActionBar actionBar = ((Bienvenida)getActivity()).getSupportActionBar();
            assert actionBar != null;
            actionBar.setTitle("Claves de usuarios");
        }catch(ClassCastException | NullPointerException e){
            e.printStackTrace();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState){
        super.onSaveInstanceState(outState);
        outState.putString("capturista", capturista.getText().toString());
        outState.putString("consultor", consultor.getText().toString());
        outState.putString("participante", participante.getText().toString());
        outState.putString("capturista_conf", capturistaConf.getText().toString());
        outState.putString("consultor_conf", consultorConf.getText().toString());
        outState.putString("participante_conf", participanteConf.getText().toString());
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater){
        menu.findItem(R.id.add).setVisible(false);
        menu.findItem(R.id.less).setVisible(false);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        if(item.getItemId() == R.id.confirmar){
            accionConfirmar();
        } else if (item.getItemId() == R.id.info){
            ((Bienvenida)getActivity()).showInformationDialog("" +
                    "Aquí es en donde se configuran las contraseñas para cada uno de los distintos tipos de usuarios" +
                    " que intervienen en el sistema. Después podrán ser cambiadas si es requerido.");
        }
        return super.onOptionsItemSelected(item);
    }

    private void accionConfirmar(){
        // Obten todas las contraseñas y actualiza los campos pertinentes.
        // Debemos hacer una validación usando expresiones comunes. Demandar un contenido mínimo.
        boolean correcto;
        MD5Hash md5 = new MD5Hash();
        if( camposCompletos() ){
            correcto = administraCredencialUsuario(R.id.claves_usuarios_etiqueta_capturista,
                    md5.makeHashForSomeBytes(capturista.getText().toString().trim()),
                    md5.makeHashForSomeBytes(capturistaConf.getText().toString().trim()));
            correcto = administraCredencialUsuario(R.id.claves_usuarios_etiqueta_consultor,
                    md5.makeHashForSomeBytes(consultor.getText().toString().trim()),
                    md5.makeHashForSomeBytes(consultorConf.getText().toString().trim())) && correcto;
            correcto = administraCredencialUsuario(R.id.claves_usuarios_etiqueta_participante,
                    md5.makeHashForSomeBytes(participante.getText().toString().trim()),
                    md5.makeHashForSomeBytes(participanteConf.getText().toString().trim())) && correcto;
            if(correcto)
                nextFragment();
        }else{
            makeSnackbar("Cuide los campos vacios");
        }
    }

    private boolean camposCompletos() {
        return !"".equals(capturista.getText().toString().trim())
                && !"".equals(consultor.getText().toString().trim())
                && !"".equals(participante.getText().toString().trim())
                && !"".equals(capturistaConf.getText().toString().trim())
                && !"".equals(consultorConf.getText().toString().trim())
                && !"".equals(participanteConf.getText().toString().trim());
    }

    private void nextFragment(){
        FragmentManager fm = getActivity().getSupportFragmentManager();
        fm.beginTransaction()
                .remove(this)
                .add(R.id.main_container, new GruposPoblacion(), "Grupos dentro de la población")
                .commit();
    }

    private boolean administraCredencialUsuario(int etiquetaRes, byte[] psswd, byte[] psswdConf){
        TextView usuario = (TextView) getView().findViewById(etiquetaRes);
        boolean correcto = false;
        if(confirmarPass(usuario, psswd, psswdConf)) {
            if (db.revisaExistenciaDeCredencial(usuario.getText().toString()))
                db.actualizaUsuarioPsswd(usuario.getText().toString(), psswd);
            else
                db.insertaUsuario(usuario.getText().toString(), psswd);
            correcto = true;
        }else
            makeSnackbar("Algunas credenciales no coinciden");
        return correcto;
    }

    private boolean confirmarPass(TextView usuario, byte[] psswd, byte[] psswdConf) {
        Log.d("Piper", usuario.getText().toString());
        Log.d("Piper", Arrays.toString(psswd));
        Log.d("Piper", Arrays.toString(psswdConf));
        if(!Arrays.equals(psswd, psswdConf)) {
            usuario.setTextColor(getResources().getColor(R.color.polivoto_color));
            new Timer().schedule(new TemporizadorDeColorDeEtiqueta(usuario), 3000);
            return false;
        }else {
            return true;
        }
    }

    private void makeSnackbar(String message){
        Snackbar.make(capturista,message,Snackbar.LENGTH_SHORT)
                .setAction("Aviso", null).show();
    }

    private class TemporizadorDeColorDeEtiqueta extends TimerTask {

        private TextView etiqueta;

        public TemporizadorDeColorDeEtiqueta(TextView etiqueta){
            this.etiqueta = etiqueta;
        }

        @Override
        public void run(){
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    etiqueta.setTextColor(Color.GRAY);
                }
            });
        }
    }

    private class AccionSobreTeclaListo implements TextView.OnEditorActionListener{

        @Override
        public boolean onEditorAction(TextView v, int actionId, KeyEvent event){
            boolean handled = false;
            if(actionId == EditorInfo.IME_ACTION_DONE){
                accionConfirmar();
                handled = true;
            }
            return handled;
        }
    }
}