package org.inspira.jcapiz.polivoto.fragmentos;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.SwitchCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import org.inspira.jcapiz.polivoto.R;
import org.inspira.jcapiz.polivoto.actividades.NuevoProcesoDeVotacion;
import org.inspira.jcapiz.polivoto.dialogos.DialogoDeConsultaSimple;
import org.inspira.jcapiz.polivoto.dialogos.ObtenerTexto;
import org.inspira.jcapiz.polivoto.dialogos.RemueveElementosDeLista;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import resumenresultados.shared.Pregunta;

/**
 * Created by jcapiz on 2/01/16.
 */
public class FormularioDeVotacion extends Fragment {

    private EditText titulo;
    private TextView textoSwitchVolverGlobal;
    private ListView listaPreguntas;
    private SwitchCompat switchVolverGlobal;
    private ArrayAdapter<String> adapter;
    private List<Pregunta> preguntas;

    @Override
    public void onAttach(Context context){
        super.onAttach(context);
    }

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        if( preguntas == null) {
            preguntas = new ArrayList<>();
            if (savedInstanceState != null) {
                Pregunta[] questions = (Pregunta[]) savedInstanceState.getSerializable("preguntas");
                if (questions != null && questions.length > 0)
                    Collections.addAll(preguntas, questions);
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup root, Bundle savedInsatnceState){
        View rootView = inflater.inflate(R.layout.formulario_de_votacion, root, false);
        titulo = (EditText) rootView.findViewById(R.id.formulario_de_votacion_entrada_titulo_de_votacion);
        textoSwitchVolverGlobal = (TextView) rootView.findViewById(R.id.formulario_de_votacion_etiqueta_switch_volver_global);
        switchVolverGlobal = (SwitchCompat) rootView.findViewById(R.id.formulario_de_votacion_switch_volver_global);
        listaPreguntas = (ListView) rootView.findViewById(R.id.formulario_de_votacion_lista_de_preguntas);
        textoSwitchVolverGlobal.setOnClickListener(new ClickSobreEtiquetaDeSwitch());
        switchVolverGlobal.setOnCheckedChangeListener(new CambioEnSwitch());
        listaPreguntas.setOnItemClickListener(new ClickSobrePregunta());
        rootView.findViewById(R.id.formulario_de_votacion_encabezado_lista_de_preguntas)
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        agregarPregunta();
                    }
                });
        setHasOptionsMenu(true);
        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState){
        super.onActivityCreated(savedInstanceState);
        colocarTextoDeColor(switchVolverGlobal.isChecked());
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        if( item.getItemId() == R.id.add ){
            agregarPregunta();
        }else if( item.getItemId() == R.id.less ){
            quitarPreguntas();
        }else if( item.getItemId() == R.id.info ){
            ((NuevoProcesoDeVotacion)getActivity()).showInformationDialog("" +
                    "Se trata del título y las preguntas que verán los participantes," +
                    " por cada pregunta aparecerá una papeleta con sus respectivas opciones." +
                    "\nPara editar las opciones de cada pregunta debe hacer click sobre ella." +
                    "\nLas preguntas que contengan menos de dos opciones serán ignoradas al" +
                    " momento de construir la plantilla de la(s) papeleta(s).");
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onSaveInstanceState(Bundle outState){
        outState.putString("titulo", titulo.getText().toString());
        outState.putSerializable("preguntas", preguntas.toArray(new Pregunta[0]));
    }

    @Override
    public void onResume(){
        super.onResume();
        List<String> lst = new ArrayList<>();
        for(Pregunta pregunta : preguntas)
            lst.add(pregunta.getTitulo());
        adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1, lst);
        listaPreguntas.setAdapter(adapter);
    }

    public boolean isGlobal(){ return switchVolverGlobal != null && switchVolverGlobal.isChecked(); }

    public String getTitulo(){
        return titulo == null ? "" : titulo.getText().toString();
    }

    public boolean validaFormulario(){
        boolean correcto = false;
        int preguntasCorrectas = 0;
        for( Pregunta pregunta : preguntas ){
            if( pregunta.obtenerCantidadDeOpciones() > 1 )
                preguntasCorrectas ++;
        }
        if( preguntasCorrectas > 0 ){
            if(!"".equals(titulo.getText().toString().trim()))
                correcto = true;
            else
                makeSnackbar("Debe especificar un título para la votación");
        } else {
            makeSnackbar("Debe definir al menos una pregunta.");
        }
        return correcto;
    }

    public Pregunta obtenerPregunta(int posicion){
        return preguntas.get(posicion);
    }

    private void colocarTextoDeColor(boolean isChecked){
        Log.d("Colorear", "" + isChecked);
        if(isChecked)
            textoSwitchVolverGlobal.setTextColor(getResources().getColor(R.color.polivoto_color));
        else
            textoSwitchVolverGlobal.setTextColor(Color.GRAY);
    }

    private void agregarPregunta(){
        ObtenerTexto miTexto = new ObtenerTexto();
        Bundle arguments = new Bundle();
        arguments.putString("mensaje","Escriba la nueva pregunta");
        miTexto.setArguments(arguments);
        miTexto.setAgenteDeInteraccion(new AgenteDeInteraccionConDialogoParaPregunta());
        miTexto.show(getActivity().getSupportFragmentManager(), "");
    }

    private void quitarPreguntas(){
        Bundle argumentos = new Bundle();
        argumentos.putString("titulo","Remover preguntas");
        String[] elementos = new String[adapter.getCount()];
        for(int i=0; i<adapter.getCount(); i++){
            elementos[i] = adapter.getItem(i);
        }
        argumentos.putStringArray("elementos", elementos);
        RemueveElementosDeLista quitarPreguntas = new RemueveElementosDeLista();
        quitarPreguntas.setAgenteDeInteraccion(new AgenteDeInteraccionConDialogoDeQuitarPreguntas());
        quitarPreguntas.setArguments(argumentos);
        quitarPreguntas.show(getActivity().getSupportFragmentManager(), "");
    }

    private class AgenteDeInteraccionConDialogoParaPregunta implements DialogoDeConsultaSimple.AgenteDeInteraccionConResultado{

        @Override
        public void clickSobreAccionPositiva(DialogFragment dialogo) {
            ObtenerTexto miTexto = (ObtenerTexto) dialogo;
            String nuevaPregunta = miTexto.obtenerTexto().trim();
            if(!"".equals(nuevaPregunta)) {
                if(!preguntas.contains(nuevaPregunta)) {
                    preguntas.add(new Pregunta(nuevaPregunta));
                    ((NuevoProcesoDeVotacion) getActivity()).setPreguntas(preguntas);
                    adapter.add(nuevaPregunta);
                }else{
                    makeSnackbar("La pregunta ya existe");
                }
            }
        }

        @Override
        public void clickSobreAccionNegativa(DialogFragment dialogo) {}
    }

    private class AgenteDeInteraccionConDialogoDeQuitarPreguntas implements DialogoDeConsultaSimple.AgenteDeInteraccionConResultado{

        @Override
        public void clickSobreAccionPositiva(DialogFragment dialogo) {
            RemueveElementosDeLista remover = (RemueveElementosDeLista)dialogo;
            List<String> elementos = remover.getElementosSeleccionados();
            String elementoDeAdaptador;
            for(String elemento : elementos){
                for(int i=0; i<adapter.getCount();i++) {
                    elementoDeAdaptador = adapter.getItem(i);
                    if (elementoDeAdaptador.equals(elemento)) {
                        adapter.remove(elementoDeAdaptador);
                        preguntas.remove(obtenerPosicionPregunta(elementoDeAdaptador));
                        ((NuevoProcesoDeVotacion)getActivity()).setPreguntas(preguntas);
                        break;
                    }
                }
            }
            makeSnackbar("Se removieron " + elementos.size() + " elementos");
        }

        @Override
        public void clickSobreAccionNegativa(DialogFragment dialogo) {}
    }

    private class ClickSobrePregunta implements AdapterView.OnItemClickListener{

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            String pregunta = ((TextView)view).getText().toString();
            Bundle arguments = new Bundle();
            arguments.putSerializable("pregunta", obtenerPregunta(obtenerPosicionPregunta(pregunta)));
            AdminOpcionesPregunta opciones = new AdminOpcionesPregunta();
            opciones.setArguments(arguments);
            NuevoProcesoDeVotacion npdv = (NuevoProcesoDeVotacion)getActivity();
            npdv.addFragment(opciones);
            ActionBar actionBar = npdv.getSupportActionBar();
            actionBar.addTab(actionBar.newTab()
                    .setIcon(R.drawable.ic_receipt_grey_24dp)
                    .setTabListener(npdv));
            npdv.setCurrentItem(actionBar.getTabCount() - 1);
            adapter.remove(pregunta);
        }
    }

    private class CambioEnSwitch implements CompoundButton.OnCheckedChangeListener{

        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            colocarTextoDeColor(isChecked);
        }
    }

    private class ClickSobreEtiquetaDeSwitch implements View.OnClickListener{

        @Override
        public void onClick(View view){
            switchVolverGlobal.setChecked(!switchVolverGlobal.isChecked());
        }
    }

    private int obtenerPosicionPregunta(String pregunta){
        int posicion = -1;
        for(int i=0; i<preguntas.size(); i++){
            if(preguntas.get(i).getTitulo().equals(pregunta)) {
                posicion = i;
                break;
            }
        }
        return posicion;
    }

    private void makeSnackbar(String message){
        Snackbar.make(titulo, message, Snackbar.LENGTH_SHORT)
                .setAction("Aviso", null).show();
    }

}