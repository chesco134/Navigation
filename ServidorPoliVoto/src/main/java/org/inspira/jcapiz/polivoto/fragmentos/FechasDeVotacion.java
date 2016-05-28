package org.inspira.jcapiz.polivoto.fragmentos;

import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.MenuItemCompat;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import org.inspira.jcapiz.polivoto.R;
import org.inspira.jcapiz.polivoto.actividades.NuevoProcesoDeVotacion;
import org.inspira.jcapiz.polivoto.pojo.TiempoDeVotacion;

import java.util.Date;

/**
 * Created by jcapiz on 2/01/16.
 */
public class FechasDeVotacion extends Fragment implements  ConfigurarFecha.AccionAceptar{

    private ConfigurarFecha fechaInicio;
    private ConfigurarFecha fechaFin;
    private TiempoDeVotacion inicio;
    private TiempoDeVotacion fin;
    private boolean isFirstActive;

    @Override
    public void onAttach(Context context){
        super.onAttach(context);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup root, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fechas_de_votacion, root, false);
        isFirstActive = savedInstanceState == null || savedInstanceState.getBoolean("isFirstActive");
        if(savedInstanceState != null){
            inicio = (TiempoDeVotacion) savedInstanceState.getSerializable("inicio");
            fin = (TiempoDeVotacion) savedInstanceState.getSerializable("fin");
        }
        colocaFragmento();
        setHasOptionsMenu(true);
        return rootView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater){
        menu.findItem(R.id.add).setVisible(false);
        menu.findItem(R.id.less).setVisible(false);
        menu.add(0, 319, 0, "Borrar tiempos");
        MenuItemCompat.setShowAsAction(menu.findItem(319), MenuItemCompat.SHOW_AS_ACTION_IF_ROOM | MenuItemCompat.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW);
        menu.findItem(319).setIcon(getResources().getDrawable(R.drawable.ic_clear_white_24dp));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        int itemId = item.getItemId();
        if( itemId == 319 ){
            reiniciar();
        } else if( itemId == R.id.info){
            ((NuevoProcesoDeVotacion)getActivity()).showInformationDialog("" +
                    "La fecha y hora de inicio y fin del proceso de" +
                    " votación actual.");
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean("isFirstActive", isFirstActive);
        outState.putSerializable("inicio", inicio);
        outState.putSerializable("fin", fin);
    }

    public boolean validaFechas(){
        if(inicio == null || fin == null)
            return false;
        String[] camposFaltantesInicio = inicio.comprobarCamposFaltantes();
        String[] camposFaltantesFin = fin.comprobarCamposFaltantes();
        return camposFaltantesInicio.length + camposFaltantesFin.length == 0;
    }

    private void reiniciar(){
        inicio = null;
        fin = null;
        try{fechaInicio.reiniciar();}catch(NullPointerException ignore){}
        try{fechaFin.reiniciar();}catch(NullPointerException ignore){}
        isFirstActive = true;
        colocaFragmento();
    }

    private void makeSnackbar(String mensaje){
        Snackbar.make(getView(), mensaje, Snackbar.LENGTH_SHORT)
                .setAction("Aviso", null).show();
    }

    private void colocaFragmento(){
        FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
        if(isFirstActive){
            fechaInicio = new ConfigurarFecha();
            fechaInicio.setAccionAceptar(this);
            Bundle argsFI = new Bundle();
            argsFI.putString("pregunta", "Coloque el tiempo inicial");
            argsFI.putSerializable("tiempo_de_votacion", inicio);
            fechaInicio.setArguments(argsFI);
            ft.replace(R.id.fechas_de_votacion_contenedor_principal, fechaInicio);
        }else{
            fechaFin = new ConfigurarFecha();
            fechaFin.setAccionAceptar(this);
            Bundle argsFF = new Bundle();
            argsFF.putString("pregunta", "Coloque el tiempo final");
            argsFF.putSerializable("tiempo_de_votacion", fin);
            fechaFin.setArguments(argsFF);
            ft.replace(R.id.fechas_de_votacion_contenedor_principal, fechaFin);
        }
        ft.commit();
    }

    @Override
    public void aceptar(TiempoDeVotacion tiempoDeVotacion) {
        String[] camposFaltantes = tiempoDeVotacion.comprobarCamposFaltantes();
        if( camposFaltantes.length == 0 ){
            tiempoDeVotacion.construirTiempo();
            if(isFirstActive) {
                inicio = tiempoDeVotacion;
                isFirstActive = false;
                colocaFragmento();
                makeSnackbar("¡Correcto!");
            }else{
                if( tiempoDeVotacion.getTimeInMillis() - inicio.getTimeInMillis() > 0){
                    fin = tiempoDeVotacion;
                    isFirstActive = true;
                    colocaFragmento();
                    makeSnackbar("¡Correcto!");
                }else{
                    makeSnackbar("Fecha final menor a fecha inicial");
                    this.fechaFin.reiniciar();
                }
            }
        }else{
            makeSnackbar(darFormato(camposFaltantes));
        }
    }

    private String darFormato(String[] camposFaltantes) {
        String mensajeDeCamposFaltantes = "Necesitamos también: ";
        String ultimoCampoFaltante = camposFaltantes[camposFaltantes.length - 1];
        for(String campoFaltante: camposFaltantes)
            if(!campoFaltante.equals(ultimoCampoFaltante))
                mensajeDeCamposFaltantes = mensajeDeCamposFaltantes.concat(campoFaltante).concat(", ");
            else
                mensajeDeCamposFaltantes = mensajeDeCamposFaltantes.concat(campoFaltante);
        return mensajeDeCamposFaltantes;
    }

    public Date obtenerFechaInicio() {
        return inicio == null ? null : new Date(inicio.getTimeInMillis());
    }

    public Date obtenerFechaFin(){
        return fin == null ? null : new Date(fin.getTimeInMillis());
    }
}