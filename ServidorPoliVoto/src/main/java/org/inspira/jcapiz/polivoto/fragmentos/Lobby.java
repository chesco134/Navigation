package org.inspira.jcapiz.polivoto.fragmentos;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;

import org.inspira.jcapiz.polivoto.*;
import org.inspira.jcapiz.polivoto.actividades.ConfiguraParticipantes;
import org.inspira.jcapiz.polivoto.actividades.ConfigurarNuevaVotacion;
import org.inspira.jcapiz.polivoto.actividades.EsperandoConsultor;
import org.inspira.jcapiz.polivoto.actividades.NuevoProcesoDeVotacion;
import org.inspira.jcapiz.polivoto.actividades.Bienvenida;
import org.inspira.jcapiz.polivoto.actividades.SolicitarClaveAdmin;
import org.inspira.jcapiz.polivoto.adaptadores.MenuPrincipal;
import org.inspira.jcapiz.polivoto.threading.TerminaVotacionLocal;

import java.security.NoSuchAlgorithmException;

/**
 * Created by jcapiz on 31/12/15.
 */
public class Lobby extends Fragment {

    private GridView gridMenu;
    private boolean hadResume = false;

    @Override
    public void onAttach(Context ctx){
        super.onAttach(ctx);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup root, Bundle savedInstanceState){
        View rootView = inflater.inflate(R.layout.content_lobby, root, false);
        FloatingActionButton fab = (FloatingActionButton) rootView.findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                launchNuevoProcesoDeVotacion();
            }
        });
        gridMenu = (GridView) rootView.findViewById(R.id.content_lobby_menu);
        gridMenu.setAdapter(new MenuPrincipal(getActivity()));
        fab.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                startActivity(new Intent(getContext(), EsperandoConsultor.class));
                return true;
            }
        });
        setHasOptionsMenu(true);
        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState){
        super.onActivityCreated(savedInstanceState);
        try{
            ActionBar actionBar = ((Bienvenida)getActivity()).getSupportActionBar();
            actionBar.setTitle("PoliVoto");
        }catch(ClassCastException ignored){}
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater){
        menu.findItem(R.id.add).setVisible(false);
        menu.findItem(R.id.less).setVisible(false);
        menu.findItem(R.id.confirmar).setVisible(false);
        menu.findItem(R.id.info).setVisible(false);
    }

    @Override
    public void onResume(){
        super.onResume();
        if(hadResume){
            FragmentManager fm = getActivity().getSupportFragmentManager();
            fm.beginTransaction().detach(fm.findFragmentByTag("changing_pass")).commit();
        }
    }

    private void launchNuevoProcesoDeVotacion(){
        Intent i = new Intent(getContext(), ConfigurarNuevaVotacion.class);
        startActivity(i);
    }
}