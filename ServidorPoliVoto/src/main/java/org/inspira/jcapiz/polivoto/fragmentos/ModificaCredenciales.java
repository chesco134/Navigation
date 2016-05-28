package org.inspira.jcapiz.polivoto.fragmentos;

import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import org.inspira.jcapiz.polivoto.R;
import org.inspira.jcapiz.polivoto.actividades.SolicitarClaveAdmin;
import org.inspira.jcapiz.polivoto.database.Votaciones;
import org.inspira.jcapiz.polivoto.dialogos.DialogoDeConsultaSimple;
import org.inspira.jcapiz.polivoto.dialogos.DialogoModificaCredenciales;
import org.inspira.jcapiz.polivoto.seguridad.MD5Hash;

import java.util.List;

/**
 * Created by jcapiz on 1/01/16.
 */
public class ModificaCredenciales extends Fragment {

    private ListView listaUsuarios;
    private List<String> usuarios;
    private Votaciones db;

    @Override
    public void onAttach(Context ctx){
        super.onAttach(ctx);
        db = new Votaciones(ctx);
    }

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        usuarios = db.obtenerUsuarios();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup root, Bundle savedInstanceState){
        View rootView = inflater.inflate(R.layout.modifica_credenciales_fragment, root, false);
        listaUsuarios = (ListView) rootView.findViewById(R.id.usuarios);
        setHasOptionsMenu(true);
        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState){
        super.onActivityCreated(savedInstanceState);
        ((SolicitarClaveAdmin)getActivity()).getSupportActionBar().setTitle("Reescribe credenciales");
        listaUsuarios.setAdapter(new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1, usuarios));
        listaUsuarios.setOnItemClickListener(new AccionClickSobreItem());
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater){
        menu.findItem(R.id.add).setVisible(false);
        menu.findItem(R.id.less).setVisible(false);
        menu.findItem(R.id.confirmar).setVisible(false);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        if( R.id.info == item.getItemId() ){
            ((SolicitarClaveAdmin)getActivity()).showInformationDialog("" +
                    "En esta parte es posible modificar las contraseñas definidas para cada usuario.");
        }
        return super.onOptionsItemSelected(item);
    }

    private class AccionClickSobreItem implements AdapterView.OnItemClickListener{

        @Override
        public void onItemClick(AdapterView<?> groupView, View selectedRow, int position, long id){
            Bundle argumentos = new Bundle();
            argumentos.putString("current_user_name", ((TextView) selectedRow).getText().toString());
            DialogoModificaCredenciales dialogo = new DialogoModificaCredenciales();
            dialogo.setArguments(argumentos);
            dialogo.setAgenteDeInteraccion(new InteraccionConDialogo());
            dialogo.show(getActivity().getSupportFragmentManager(), "Redefinir credenciales");
        }
    }

    private class InteraccionConDialogo implements DialogoDeConsultaSimple.AgenteDeInteraccionConResultado{

        @Override
        public void clickSobreAccionPositiva(DialogFragment dialogo) {
            handleClickAction(dialogo);
        }

        @Override
        public void clickSobreAccionNegativa(DialogFragment dialogo) {}
    }

    private void handleClickAction(DialogFragment dialogo){
        DialogoModificaCredenciales mDialogo = (DialogoModificaCredenciales) dialogo;
        String user = mDialogo.getCurrentUserName();
        String currentPass = mDialogo.getPassActual();
        String newPass = mDialogo.getPassNuevo();
        String confirmPass = mDialogo.getPassConfirmacion();
        MD5Hash md5Hash = new MD5Hash();
        if( !"".equals(currentPass) && !"".equals(newPass) && !"".equals(confirmPass) ){
            if( db.consultaUsuario(user, md5Hash.makeHashForSomeBytes(currentPass)) ){
                if( newPass.equals(confirmPass) ){
                    db.actualizaUsuarioPsswd(user, md5Hash.makeHashForSomeBytes(newPass));
                    makeSnackbar("Contraseña actualizada");
                }else{
                    makeSnackbar("Las contraseñas no coinciden");
                }
            }else{
                makeSnackbar("Contraseña incorrecta");
            }
        }else{
            makeSnackbar("Por favor complete los campos");
        }
    }

    private void makeSnackbar(String message){
        Snackbar.make(listaUsuarios, message, Snackbar.LENGTH_SHORT)
                .setAction("Aviso", null).show();
    }
}