package org.inspira.jcapiz.polivoto.fragmentos;

import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import org.inspira.jcapiz.polivoto.*;
import org.inspira.jcapiz.polivoto.database.Votaciones;
import org.inspira.jcapiz.polivoto.actividades.Bienvenida;
import org.inspira.jcapiz.polivoto.database.acciones.AccionesTablaPerfiles;
import org.inspira.jcapiz.polivoto.dialogos.DialogoDeConsultaSimple;
import org.inspira.jcapiz.polivoto.dialogos.ObtenerTexto;
import org.inspira.jcapiz.polivoto.dialogos.RemueveElementosDeLista;
import org.inspira.jcapiz.polivoto.pojo.Perfil;
import org.inspira.jcapiz.polivoto.proveedores.ProveedorSnackBar;

import java.util.ArrayList;

/**
 * Created by jcapiz on 29/12/15.
 */
public class GruposPoblacion extends Fragment {

    private ArrayAdapter<String> grupos;
    private ListView listaGrupos;
    private Votaciones db;

    @Override
    public void onAttach(Context ctx){
        super.onAttach(ctx);
        db = new Votaciones(ctx);
    }

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        if(savedInstanceState == null) {
            String[] groups = db.obtenerPerfiles();
            ArrayList<String> grps = new ArrayList<>();
            for(String grp : groups)
                if(!"NaN".equals(grp))
                    grps.add(grp);
            grupos = new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1, grps);
        }else
            grupos = new ArrayAdapter<>(getActivity(),android.R.layout.simple_list_item_1, new ArrayList<String>());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup root, Bundle savedInstanceState){
        View rootView = inflater.inflate(R.layout.grupos_de_poblacion,root,false);
        listaGrupos = (ListView) rootView.findViewById(R.id.lista_grupos_de_poblacion);
        rootView.findViewById(R.id.grupo_de_poblacion_etiqueta_grupos)
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mostrarInformacion();
                    }
                });
        setHasOptionsMenu(true);
        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState){
        super.onActivityCreated(savedInstanceState);
        if(grupos.isEmpty() && savedInstanceState != null){
            String[] perfiles = savedInstanceState.getStringArray("grupos");
            assert perfiles != null;
            for(String perfil : perfiles)
                if(!"NaN".equals(perfil))
                    grupos.add(perfil);
        }
        listaGrupos.setAdapter(grupos);
        listaGrupos.setOnItemClickListener(new AccionSobreGrupoSeleccionado("Cambiar texto"));
        try{
            ActionBar actionBar = ((Bienvenida)getActivity()).getSupportActionBar();
            actionBar.setTitle("Grupos");
        }catch(NullPointerException | ClassCastException ignored){}
    }

    @Override
    public void onSaveInstanceState(Bundle outState){
        super.onSaveInstanceState(outState);
        String[] perfiles = new String[grupos.getCount()];
        for(int i=0; i<perfiles.length;i++)
            perfiles[i] = grupos.getItem(i);
        outState.putStringArray("grupos", perfiles);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        int itemId = item.getItemId();
        if( itemId== R.id.add){
            // Inicia un dialogo para agregar información suficiente acerca de un grupo.
            muestraDialogoDeIngresoDeTexto("Escriba el nombre del nuevo grupo");
        } else if( itemId == R.id.less ){
            // Si la lista que conservas de grupos no está vacía, quita el último elemento (mientras aún no haya personas registradas bajo ese perfil).
            removerGrupos();
        } else if( itemId == R.id.confirmar ){
            // Si la lista no está vacía, guarda los cambios hechos a la base de datos.
            commitGrupos();
        } else if(itemId == R.id.info){
            mostrarInformacion();
        }
        return super.onOptionsItemSelected(item);
    }

    private void mostrarInformacion() {
        ((Bienvenida)getActivity()).showInformationDialog("" +
                "Sólo es posible eliminar grupos bajo los cuales aún no hayan participantes " +
                "registrados.\n\n" +
                "Los grupos son una forma de separar a la población que participa." +
                " No todos los procesos de votación requieren de una separación como tal" +
                " en grupos, sólo es por fines estadísticos.");
    }

    private void commitGrupos(){
        // Aquí se lleva a cabo la tarea de inserción de grupos a la base de datos.
        for(int i = 0; i<grupos.getCount(); i++)
            db.insertaPerfil(grupos.getItem(i));
        try {
            if (!getArguments().getBoolean("isChange"))
                nextFragment();
            else
                makeSnack("Cambios realizados");
        }catch(NullPointerException ignore){ nextFragment(); }
    }

    private void nextFragment(){
        FragmentManager fm = getActivity().getSupportFragmentManager();
        fm.beginTransaction()
                .remove(this)
                .add(R.id.main_container, new Ubicacion(), "Ubicacion")
                .commit();
    }

    private void removerGrupos(){
        Perfil[] perfiles = AccionesTablaPerfiles.obtenerPerfilesVacios(getContext());
        final String[] elementos = new String[perfiles.length];
        int i=0;
        for(Perfil perfil : perfiles)
            elementos[i++] = perfil.getPerfil();
        RemueveElementosDeLista rm = new RemueveElementosDeLista();
        Bundle args = new Bundle();
        args.putString("titulo", "Grupos a remover");
        args.putStringArray("elementos", elementos);
        rm.setArguments(args);
        rm.setAgenteDeInteraccion(new DialogoDeConsultaSimple.AgenteDeInteraccionConResultado() {
            @Override
            public void clickSobreAccionPositiva(DialogFragment dialogo) {
                String[] elementosSeleccionados = ((RemueveElementosDeLista) dialogo)
                        .getElementosSeleccionados().toArray(new String[]{});
                for (String elemento : elementosSeleccionados) {
                    grupos.remove(elemento);
                    AccionesTablaPerfiles.removerPerfil(getContext(), elemento);
                }
            }

            @Override
            public void clickSobreAccionNegativa(DialogFragment dialogo) {
            }
        });
        rm.show(((AppCompatActivity)getContext()).getSupportFragmentManager(), "Remover Perfiles");
    }

    private void makeSnack(String message){
        Snackbar.make(listaGrupos, message, Snackbar.LENGTH_SHORT)
                .setAction("Aviso",null).show();
    }

    private void muestraDialogoDeIngresoDeTexto(String mensaje){
        ObtenerTexto obtenerTexto = new ObtenerTexto();
        Bundle argumentosObtenerTexto = new Bundle();
        argumentosObtenerTexto.putString("mensaje",mensaje);
        obtenerTexto.setArguments(argumentosObtenerTexto);
        obtenerTexto.setAgenteDeInteraccion(new ObtenerTextoDeDialogo());
        obtenerTexto.show(getActivity().getSupportFragmentManager(),"Escribir Texto");
    }

    private class ObtenerTextoDeDialogo implements DialogoDeConsultaSimple.AgenteDeInteraccionConResultado{

        @Override
        public void clickSobreAccionPositiva(DialogFragment dialogo){
            try{
                ObtenerTexto obtenerTexto = (ObtenerTexto) dialogo;
                String nuevoGrupo = obtenerTexto.obtenerTexto().trim();
                if(!"".equals(nuevoGrupo)) {
                    Log.d("Shokan", "Agregando " + nuevoGrupo);
                    grupos.add(nuevoGrupo);
                    db.insertaPerfil(nuevoGrupo);
                }
            } catch(ClassCastException e){
                // ¿Nos equivocamos de fragmento?
            }
        }

        @Override
        public void clickSobreAccionNegativa(DialogFragment dialogo){}
    }

    private class CambiarTexto implements DialogoDeConsultaSimple.AgenteDeInteraccionConResultado{

        private int posicion;

        public CambiarTexto(int posicion){
            this.posicion = posicion;
        }

        @Override
        public void clickSobreAccionPositiva(DialogFragment dialogo){
            try{
                ObtenerTexto obtenerTexto = (ObtenerTexto)dialogo;
                String nuevoGrupo = obtenerTexto.obtenerTexto().trim();
                if(!"".equals(nuevoGrupo)) {
                    if(!"NaN".equals(nuevoGrupo)) {
                        String antiguoTexto = grupos.getItem(posicion);
                        Perfil perfil = new Perfil(AccionesTablaPerfiles.obtenerIdPerfil(getContext(), antiguoTexto));
                        perfil.setPerfil(nuevoGrupo);
                        int position = grupos.getPosition(antiguoTexto);
                        grupos.remove(antiguoTexto);
                        grupos.insert(nuevoGrupo, position);
                        AccionesTablaPerfiles.actualizaNombreDePerfil(getContext(), perfil);
                    }else{
                        ProveedorSnackBar
                                .muestraBarraDeBocados(listaGrupos, "Ese es un nombre incorrecto.");
                    }
                }
            }catch(ClassCastException ignored){}
        }

        @Override
        public void clickSobreAccionNegativa(DialogFragment dialogo){}
    }

    private class AccionSobreGrupoSeleccionado implements AdapterView.OnItemClickListener {

        private String mensajeDeDialogo;

        public AccionSobreGrupoSeleccionado(String mensajeDeDialogo){
            this.mensajeDeDialogo = mensajeDeDialogo;
        }

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            ObtenerTexto obtenerTexto = new ObtenerTexto();
            CambiarTexto cambiarTexto = new CambiarTexto(position);
            Bundle argumentos = new Bundle();
            argumentos.putString("mensaje",mensajeDeDialogo);
            argumentos.putString("texto_anterior", ((TextView) view).getText().toString());
            obtenerTexto.setArguments(argumentos);
            obtenerTexto.setAgenteDeInteraccion(cambiarTexto);
            obtenerTexto.show(getActivity().getSupportFragmentManager(),"Cambiar texto");
        }
    }
}