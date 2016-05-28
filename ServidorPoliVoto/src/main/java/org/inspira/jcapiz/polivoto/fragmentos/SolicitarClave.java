package org.inspira.jcapiz.polivoto.fragmentos;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import org.inspira.jcapiz.polivoto.R;
import org.inspira.jcapiz.polivoto.actividades.SolicitarClaveAdmin;
import org.inspira.jcapiz.polivoto.database.Votaciones;
import org.inspira.jcapiz.polivoto.seguridad.MD5Hash;

/**
 * Created by jcapiz on 2/01/16.
 */
public class SolicitarClave extends Fragment {

    private Votaciones db;
    private EditText pass;

    @Override
    public void onAttach(Context context){
        super.onAttach(context);
        db = new Votaciones(context);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup root, Bundle savedInstanceState){
        View rootView = inflater.inflate(R.layout.unlocker, root, false);
        pass = (EditText) rootView.findViewById(R.id.psswd);
        getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setHasOptionsMenu(true);
        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState){
        super.onActivityCreated(savedInstanceState);
        ((SolicitarClaveAdmin)getActivity()).getSupportActionBar().setTitle("Credencial de acceso");
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater ignore){
        menu.findItem(R.id.add).setVisible(false);
        menu.findItem(R.id.less).setVisible(false);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        int itemId = item.getItemId();
        if( itemId == R.id.confirmar ){
            accionConfirmar();
        } else if( itemId == R.id.info){
            ((SolicitarClaveAdmin)getActivity()).showInformationDialog("" +
                    "Necesitamos confirmar que se trata del usuario administrador" +
                    " antes de llevar a cabo esta acción.");
        }
        return super.onOptionsItemSelected(item);
    }

    private void accionConfirmar(){
        MD5Hash md5 = new MD5Hash();
        if(db.consultaUsuario("Administrador", md5.makeHashForSomeBytes(pass.getText().toString()))){
            getActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container, new ModificaCredenciales(), "changing_pass")
                    .commit();
        }else{
            makeSnackbar("Contraseña incorrecta");
        }
    }

    private void makeSnackbar(String message){
        Snackbar.make(pass, message, Snackbar.LENGTH_SHORT)
                .setAction("Aviso", null).show();
    }
}