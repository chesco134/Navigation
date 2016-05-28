package org.inspira.jcapiz.polivoto.fragmentos;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
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
import org.inspira.jcapiz.polivoto.actividades.Bienvenida;
import org.inspira.jcapiz.polivoto.database.Votaciones;
import org.inspira.jcapiz.polivoto.proveedores.ProveedorDeRecursos;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import resumenresultados.ScrollingActivity;
import resumenresultados.shared.Opcion;
import resumenresultados.shared.Pregunta;
import resumenresultados.shared.ResultadoPorPerfil;
import resumenresultados.shared.Votacion;

/**
 * Created by jcapiz on 29/12/15.
 */
public class HistorialVotaciones extends Fragment {

    private ListView listaVotaciones;
    private TextView textoNoHayVotaciones;
    private ArrayAdapter<String> titulos;
    private Votaciones db;

    @Override
    public void onAttach(Context ctx){
        super.onAttach(ctx);
        db = new Votaciones(ctx);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup root, Bundle savedInstancestate){
        View rootView = inflater.inflate(R.layout.historial_votaciones, root, false);
        listaVotaciones = (ListView) rootView.findViewById(R.id.historial_votaciones_lista);
        listaVotaciones.setOnItemClickListener(new AccionDeClick());
        textoNoHayVotaciones = (TextView) rootView.findViewById(R.id.historial_votaciones_texto_no_hay_votaciones);
        textoNoHayVotaciones.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mostrarInformacion();
            }
        });
        setHasOptionsMenu(true);
        return rootView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater){
        menu.findItem(R.id.add).setVisible(false);
        menu.findItem(R.id.less).setVisible(false);
        menu.findItem(R.id.confirmar).setVisible(false);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        if(item.getItemId() == R.id.info){
            mostrarInformacion();
        }
        return super.onOptionsItemSelected(item);
    }

    private void mostrarInformacion() {
        ((Bienvenida)getActivity()).showInformationDialog("" +
                "Muestra los títulos de las distintas votaciones que han habido usando" +
                " el presente dispositivo como servidor. Seleccionar cada título muestra detalles" +
                " adicionales del proceso.");
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState){
        super.onActivityCreated(savedInstanceState);
        // Here I need to fill in the list.
        // In order to retrieve the information needed, a query needs to be done to the data base.
        if( savedInstanceState == null ){
            JSONArray votacionesConcluidas = db.obtenerVotacionesConcluidas();
            String[] titles = new String[votacionesConcluidas.length()];
            try {
                for (int i = 0; i < titles.length; i++)
                    titles[i] = votacionesConcluidas.getJSONObject(i).getString("titulo");
            }catch(JSONException e){
                e.printStackTrace();
            }
            titulos = new ArrayAdapter<>(getActivity(),android.R.layout.simple_list_item_1, titles);
        }else{
            String[] rows = savedInstanceState.getStringArray("titulos");
            ArrayList<String> titles = new ArrayList<>();
            for(String row : rows)
                titles.add(row);
            titulos = new ArrayAdapter<>(getActivity(),android.R.layout.simple_list_item_1,
                    titles);
        }
        listaVotaciones.setAdapter(titulos);
        try{
            ActionBar actionBar = ((Bienvenida)getActivity()).getSupportActionBar();
            actionBar.setTitle("Historial de Votaciones");
        }catch(NullPointerException ignore){}
    }

    @Override
    public void onResume(){
        super.onResume();
        if(titulos.getCount() > 0)
            textoNoHayVotaciones.setVisibility(View.INVISIBLE);
    }

    @Override
    public void onSaveInstanceState(Bundle outState){
        String[] rows = new String[titulos.getCount()];
        for(int i=0; i<rows.length; i++){
            rows[i] = titulos.getItem(i);
        }
        outState.putStringArray("titulos", rows);
        super.onSaveInstanceState(outState);
    }

    private class AccionDeClick implements ListView.OnItemClickListener {

        @Override
        public void onItemClick(AdapterView<?> parent, View clickedRowLayout, int position, long id) {
            // Cada vez que hagan click sobre un elemento, se debe lanzar una actividad de detalles.
            // Cada entrada de la lista corresponde a una alineación hecha a medida.
            JSONArray jelementos = db.obtenerVotacionesConcluidas();
            int idVotacion = -1;
            try{
                idVotacion = jelementos.getJSONObject(position).getInt("idVotacion");
            }catch(JSONException e){
                e.printStackTrace();
            }
            launchDetallesVotacion(idVotacion);
        }
    }

    private void launchDetallesVotacion(int idVotacion){
        // Debe ser colocado un fragmento que muestre los detalles de la votación seleccionada.
        Intent i = new Intent(getActivity(), ScrollingActivity.class);
        org.inspira.jcapiz.polivoto.pojo.Votacion votacion = new org.inspira.jcapiz.polivoto.pojo.Votacion(idVotacion);
        try {
            JSONObject datosDeVotacion = db.obtenerDatosDeVotacion(idVotacion);
            votacion.setTitulo(datosDeVotacion.getString("Titulo"));
            votacion.setIdEscuela(datosDeVotacion.getInt("idEscuela"));
            votacion.setLugar(db.obtenerNombreDeEscuela(datosDeVotacion.getInt("idEscuela")));
            votacion.setFechaInicio(datosDeVotacion.getLong("Fecha_Inicio"));
            votacion.setFechaFin(datosDeVotacion.getLong("Fecha_Fin"));
            i.putExtra("matricula_total", db.obtenerTotalNumParticipantes(idVotacion));
            try{
                i.putExtra("porcentaje_de_participacion", (float)db.obtenerCantidadParticipantes(idVotacion)/(float)db.obtenerTotalNumParticipantes(idVotacion));
            }catch(ArithmeticException a){ i.putExtra("porcentaje_de_participacion", (float)0); }
        } catch (JSONException ignore){ ignore.printStackTrace(); }
        i.putExtra("total_participantes", db.obtenerCantidadParticipantes(idVotacion));
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        Votacion votacionShared = new Votacion(votacion.getTitulo());
        org.inspira.jcapiz.polivoto.pojo.Pregunta[] preguntas = db.obtenerPreguntasDeVotacion(idVotacion);
        String perfiles[] = db.obtenerPerfiles();
        Map<String,Integer> resultados;
        Map<String, Integer> resultadosPorPerfil;
        ResultadoPorPerfil resultadoPorPerfil;
        Opcion opcion;
        List<Opcion> opciones;
        for(org.inspira.jcapiz.polivoto.pojo.Pregunta preg : preguntas){
            Pregunta pregunta = new Pregunta(preg.getEnunciado());
            resultados = db.obtenerResultadosPorPregunta(preg.getId(), idVotacion);
            for(String str : resultados.keySet()) {
                opcion = new Opcion(str);
                opcion.setCantidad(resultados.get(str));
                pregunta.agregarOpcion(opcion);
            }
            for(String perfil : perfiles) {
                resultadosPorPerfil = db.obtenerResultadosPorPreguntaPorPerfil(preg.getId(), perfil, idVotacion);
                resultadoPorPerfil = new ResultadoPorPerfil();
                resultadoPorPerfil.setPerfil(perfil);
                opciones = new ArrayList<>();
                for(String key : resultadosPorPerfil.keySet()){
                    opcion = new Opcion(key);
                    opcion.setCantidad(resultadosPorPerfil.get(key));
                    opciones.add(opcion);
                }
                resultadoPorPerfil.setOpciones(opciones);
                pregunta.agregarResultadoPorPerfil(resultadoPorPerfil);
            }
            votacionShared.agregaPregunta(pregunta);
        }
        votacionShared.setIdEscuela(votacion.getIdEscuela());
        votacionShared.setDescripcion("Descripción no disponible.");
        votacionShared.setFechaFinal(ProveedorDeRecursos.obtenerFechaFormatoYearFirst(votacion.getFechaFin()));
        votacionShared.setFechaInicial(ProveedorDeRecursos.obtenerFechaFormatoYearFirst(votacion.getFechaInicio()));
        votacionShared.setMatricula(db.obtenerTotalNumParticipantes(idVotacion));
        votacionShared.setTotalParticipantes(db.obtenerCantidadParticipantes(idVotacion));
        votacionShared.setPorcentajeParticipacion(i.getFloatExtra("porcentaje_de_participacion", 0f));
        votacionShared.setGrupos(db.obtenerPerfiles());
        votacionShared.setHash(db.obtenerHashVotacion(idVotacion));
        i.putExtra("titulo", votacion.getTitulo());
        i.putExtra("escuela", votacion.getLugar());
        i.putExtra("fecha_inicio", votacion.getFechaInicio());
        i.putExtra("fecha_fin", votacion.getFechaFin());
        i.putExtra("votacion", votacionShared);
        i.putExtra("rows", new String[]{});//db.quienesHanParticipado(idVotacion));
        i.putExtra("header", "Detalles de Participantes");
        startActivity(i);
    }
}