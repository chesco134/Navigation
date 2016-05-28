package org.inspira.jcapiz.polivoto.fragmentos;

import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import org.inspira.jcapiz.polivoto.R;
import org.inspira.jcapiz.polivoto.dialogos.DialogoDeConsultaSimple;
import org.inspira.jcapiz.polivoto.dialogos.ObtenerTexto;
import org.inspira.jcapiz.polivoto.dialogos.RemueveElementosDeLista;
import org.inspira.jcapiz.polivoto.actividades.NuevoProcesoDeVotacion;

import java.util.ArrayList;
import java.util.List;

import resumenresultados.shared.Pregunta;

/**
 * Created by jcapiz on 2/01/16.
 */
public class AdminOpcionesPregunta extends Fragment {

    private Pregunta preguntaAnterior;
    private EditText pregunta;
    private ArrayAdapter<String> adapter;
    private ListView listaOpciones;

    @Override
    public void onAttach(Context context){
        super.onAttach(context);
    }

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        if(savedInstanceState != null){
            preguntaAnterior = (Pregunta)savedInstanceState.getSerializable("pregunta_anterior");
        }
        adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1, new ArrayList<String>());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState){
        View rootView = inflater.inflate(R.layout.admin_opciones_pregunta, parent, false);
        pregunta = (EditText) rootView.findViewById(R.id.admin_opciones_pregunta_texto_pregunta);
        if(savedInstanceState == null) {
            preguntaAnterior = (Pregunta)getArguments().getSerializable("pregunta");
            pregunta.setText(preguntaAnterior.getTitulo());
        }
        listaOpciones = (ListView) rootView.findViewById(R.id.admin_opciones_preguunta_lista_de_opciones);
        listaOpciones.setOnItemClickListener(new ClickSobreOpcion());
        TextView etiquetaOpciones = (TextView) rootView.findViewById(R.id.admin_opciones_pregunta_texto_lista_de_opciones);
        etiquetaOpciones.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                muestraDialogoDeIngresoDeTexto(null);
            }
        });
        setHasOptionsMenu(true);
        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState){
        super.onActivityCreated(savedInstanceState);
        listaOpciones.setAdapter(adapter);
        for(int i=0; i<preguntaAnterior.obtenerCantidadDeOpciones(); i++)
            adapter.add(preguntaAnterior.obtenerOpcion(i).getNombre());
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        int itemId = item.getItemId();
        if( itemId == R.id.confirmar){
            colocarTitulo();
            concluirEdicionDePregunta();
        } else if( itemId == R.id.add ){
            muestraDialogoDeIngresoDeTexto(null);
        } else if ( itemId == R.id.less ){
            muestraDialogoDeRemocionDeElementos();
        } else if ( itemId == R.id.info){
            ((NuevoProcesoDeVotacion)getActivity()).showInformationDialog("" +
                    "Edita los parámetros de la pregunta. Si la pregunta cuenta con menos de" +
                    " dos opciones, no será tomada en cuenta para construir la papeleta. Si" +
                    " la pregunta queda con el texto vacío, el texto quedará sin cambios.");
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onSaveInstanceState(Bundle outState){
        String[] elementos = new String[adapter.getCount()];
        for(int i=0; i<elementos.length; i++)
            elementos[i] = adapter.getItem(i);
        outState.putStringArray("elementos", elementos);
        outState.putSerializable("pregunta_anterior", preguntaAnterior);
        super.onSaveInstanceState(outState);
    }

    public void concluirEdicionDePregunta() throws NullPointerException {
        NuevoProcesoDeVotacion npdv = (NuevoProcesoDeVotacion) getActivity();
        npdv.setCurrentItem(0);
    }

    public void colocarTitulo(){
        String pregunta = this.pregunta.getText().toString().trim();
        if(!"".equals(pregunta))
            preguntaAnterior.setTitulo(pregunta);
    }

    private class ClickSobreOpcion implements AdapterView.OnItemClickListener{

        @Override
        public void onItemClick(AdapterView<?> parent, View selectedItem, int position, long id){
            String opcionSeleccionada = ((TextView)selectedItem).getText().toString();
            adapter.remove(opcionSeleccionada);
            preguntaAnterior.quitarOpcion(opcionSeleccionada);
            muestraDialogoDeIngresoDeTexto(opcionSeleccionada);
        }
    }

    private class AgenteDeInteraccionConIngresoDeTexto implements DialogoDeConsultaSimple.AgenteDeInteraccionConResultado{

        @Override
        public void clickSobreAccionPositiva(DialogFragment dialogo) {
            ObtenerTexto miTexto = (ObtenerTexto) dialogo;
            String opcion = miTexto.obtenerTexto().trim();
            if(!"".equals(opcion)) {
                if(!preguntaAnterior.existeOpcion(opcion)) {
                    adapter.add(opcion);
                    preguntaAnterior.agregarOpcion(opcion);
                }else{
                    makeSnackbar("La opción ya existe");
                }
            }
        }

        @Override
        public void clickSobreAccionNegativa(DialogFragment dialogo) {}
    }

    private class AgenteDeInteraccionConRemosionDeElementos implements DialogoDeConsultaSimple.AgenteDeInteraccionConResultado{

        @Override
        public void clickSobreAccionPositiva(DialogFragment dialogo) {
            RemueveElementosDeLista remover = (RemueveElementosDeLista)dialogo;
            List<String> seleccion = remover.getElementosSeleccionados();
            for(String elemento : seleccion) {
                adapter.remove(elemento);
                preguntaAnterior.quitarOpcion(elemento);
            }
            makeSnackbar(seleccion.size() + " elementos removidos");
        }

        @Override
        public void clickSobreAccionNegativa(DialogFragment dialogo) {}
    }

    private void muestraDialogoDeIngresoDeTexto(String textoAnterior){
        ObtenerTexto miTexto = new ObtenerTexto();
        Bundle argumentos = new Bundle();
        argumentos.putString("mensaje","Escriba la opción");
        argumentos.putString("texto_anterior", textoAnterior);
        miTexto.setArguments(argumentos);
        // Coloca el agente de interacción.
        miTexto.setAgenteDeInteraccion(new AgenteDeInteraccionConIngresoDeTexto());
        miTexto.show(getActivity().getSupportFragmentManager(), "");
    }

    private void muestraDialogoDeRemocionDeElementos(){
        RemueveElementosDeLista remover = new RemueveElementosDeLista();
        Bundle argumentos = new Bundle();
        argumentos.putString("titulo","Remover opciones");
        List<String> opciones = new ArrayList<>();
        for(int i=0; i<adapter.getCount(); i++){
            opciones.add(adapter.getItem(i));
        }
        argumentos.putStringArray("elementos", opciones.toArray(new String[0]));
        remover.setArguments(argumentos);
        // Coloca el agente de interacción.
        remover.setAgenteDeInteraccion(new AgenteDeInteraccionConRemosionDeElementos());
        remover.show(getActivity().getSupportFragmentManager(), "");
    }

    private FormularioDeVotacion obtenerFormularioDeVotacion(){
        NuevoProcesoDeVotacion npdv = (NuevoProcesoDeVotacion)getActivity();
        return (FormularioDeVotacion)npdv.obtenerFragmento(0);
    }

    private void makeSnackbar(String message){
        Snackbar.make(pregunta, message, Snackbar.LENGTH_SHORT)
                .setAction("Aviso", null).show();
    }
}