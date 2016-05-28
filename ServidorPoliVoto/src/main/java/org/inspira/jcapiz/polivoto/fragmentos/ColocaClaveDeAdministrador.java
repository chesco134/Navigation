package org.inspira.jcapiz.polivoto.fragmentos;

import android.content.Context;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBar;
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

import org.inspira.jcapiz.polivoto.proveedores.ProveedorDeRecursos;
import org.inspira.jcapiz.polivoto.actividades.Bienvenida;
import org.inspira.jcapiz.polivoto.database.Votaciones;
import org.inspira.jcapiz.polivoto.R;
import org.inspira.jcapiz.polivoto.seguridad.MD5Hash;

/**
 * Created by jcapiz on 29/12/15.
 */
public class ColocaClaveDeAdministrador extends Fragment {

    private EditText psswd;
    private EditText psswd2;
    private Votaciones db;
    private Context context;

    @Override
    public void onAttach(Context context){
        super.onAttach(context);
        this.context = context;
    }

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        db = new Votaciones(context);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup root, Bundle savedInstanceState){
        if(savedInstanceState == null){
            ProveedorDeRecursos.guardarRecursoEntero(context, "extado_servicio_historial", 0);
        }
        View rootView = inflater.inflate(R.layout.activity_welcome,root, false);
        setHasOptionsMenu(true);
        psswd = (EditText) rootView.findViewById(R.id.pass_1);
        psswd2 = (EditText) rootView.findViewById(R.id.pass_2);
        psswd.setTypeface(Typeface.createFromAsset(getActivity().getAssets(), "Roboto-Regular.ttf"));
        psswd2.setTypeface(Typeface.createFromAsset(getActivity().getAssets(), "Roboto-Regular.ttf"));
        psswd2.setOnEditorActionListener(new AccionSobreTeclaListo());
        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState){
        super.onActivityCreated(savedInstanceState);
        if(savedInstanceState != null) {
            psswd.setText(savedInstanceState.getString("psswd"));
            psswd2.setText(savedInstanceState.getString("psswd2"));
        }
        try{
            ActionBar actionBar = ((Bienvenida)getActivity()).getSupportActionBar();
            actionBar.setTitle("Bienvenido");
        }catch(ClassCastException igonred){}
    }

    @Override
    public void onSaveInstanceState(Bundle outState){
        super.onSaveInstanceState(outState);
        outState.putString("psswd", psswd.getText().toString());
        outState.putString("psswd2", psswd2.getText().toString());
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater mInflater){
        menu.findItem(R.id.add).setVisible(false);
        menu.findItem(R.id.less).setVisible(false);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        if(item.getItemId() == R.id.confirmar){
            accionConfirmar();
        } else if (item.getItemId() == R.id.info){
            ((Bienvenida)getActivity()).showInformationDialog("" +
                    "La clave maestra es requerida ante acciones vitales durante la configuración del proceso de votación.");
        }
        return super.onOptionsItemSelected(item);
    }

    private void accionConfirmar(){
        // Toma los textos de las contraseñas y los delega al objeto admin de bd.
        // Si se agregan con éxito, pasa al siguiente fragmento.
        String pass = psswd.getText().toString();
        String confirmPass = psswd2.getText().toString();
        if(pass.length() < 5 || confirmPass.length() < 5)
            makeSnackbar("No deben haber menos de 5 caracteres");
        else
        if(pass.equals(confirmPass)) {
            administraCredencialUsuario("Administrador", pass);
            nextFragment();
        }else{
            makeSnackbar("Las contraseñas no coinciden");
        }
    }

    private void administraCredencialUsuario(String usuario, String psswd){
        MD5Hash md5 = new MD5Hash();
        if(db.revisaExistenciaDeCredencial(usuario))
            db.actualizaUsuarioPsswd(usuario, md5.makeHashForSomeBytes(psswd));
        else
            db.insertaUsuario(usuario, md5.makeHashForSomeBytes(psswd));
    }

    private void nextFragment(){
        FragmentManager fm = getActivity().getSupportFragmentManager();
        fm.beginTransaction()
                .remove(this)
                .add(R.id.main_container, new ClavesUsuario(), "Claves de usuarios")
                .commit();
    }

    private void makeSnackbar(String message){
        Snackbar.make(psswd,message,Snackbar.LENGTH_SHORT)
                .setAction("Aviso",null).show();
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