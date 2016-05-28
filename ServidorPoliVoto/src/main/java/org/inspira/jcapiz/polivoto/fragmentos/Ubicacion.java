package org.inspira.jcapiz.polivoto.fragmentos;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.inspira.jcapiz.polivoto.R;
import org.inspira.jcapiz.polivoto.actividades.Bienvenida;
import org.inspira.jcapiz.polivoto.escuchas.ClickSobreContenedorLugarVotacion;
import org.inspira.jcapiz.polivoto.escuchas.ClickSobreContenedorTipoDeCentro;
import org.inspira.jcapiz.polivoto.proveedores.ProveedorDeRecursos;

/**
 * Created by jcapiz on 29/12/15. tul
 */
public class Ubicacion extends Fragment {

    @Override
    public void onAttach(Context ctx){
        super.onAttach(ctx);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup root, Bundle savedInstanceState){
        View rootView = inflater.inflate(R.layout.ubicacion, root, false);
        RelativeLayout contenedorTipoDeCentro = (RelativeLayout) rootView.findViewById(R.id.ubicacion_contenedor_tipo_de_centro);
        TextView tipoDeCentro = (TextView) rootView.findViewById(R.id.ubicacion_tipo_de_centro);
        RelativeLayout contenedorLugar = (RelativeLayout) rootView.findViewById(R.id.ubicacion_contenedor_lugar);
        TextView lugar = (TextView) rootView.findViewById(R.id.ubicacion_nombre_lugar_votacion);
        contenedorLugar.setOnClickListener(ClickSobreContenedorLugarVotacion.obtenerManejador(getContext(), lugar));
        contenedorTipoDeCentro.setOnClickListener(ClickSobreContenedorTipoDeCentro.obtenerManejador(getContext(), tipoDeCentro, contenedorLugar));
        String categoria = ProveedorDeRecursos.obtenerRecursoString(getContext(), "categoria");
        String lugarDeVotacion = ProveedorDeRecursos.obtenerRecursoString(getContext(), "ubicacion");
        tipoDeCentro.setText("NaN".equals(categoria) ? "" : categoria);
        lugar.setText("NaN".equals(lugarDeVotacion) ? "" : lugarDeVotacion);
        contenedorLugar.setVisibility("".equals(tipoDeCentro.getText().toString()) ? View.INVISIBLE : View.VISIBLE);
        setHasOptionsMenu(true);
        return rootView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater){
        menu.findItem(R.id.add).setVisible(false);
        menu.findItem(R.id.less).setVisible(false);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        if(item.getItemId() == R.id.confirmar){
            // Debo agregar la selección actual a un objeto de configuración.
            placeMainFragment();
        } else if (item.getItemId() == R.id.info){
            mostrarInformacion();
        }
        return super.onOptionsItemSelected(item);
    }

    private void mostrarInformacion() {
        ((Bienvenida)getActivity()).showInformationDialog("" +
                "Haga click en el botón \"más\" para indicar el nombre del lugar en el cuál se " +
                "llevará a cabo el proceso de auscultación, o selecciónelo de la lista de abajo." +
                " Adicionalmente, es posible proporcionar las coordenadas geográficas activando " +
                "el gps.\nCuando termine, seleccione \"continuar\" del menú de opciones.");
    }

    private void placeMainFragment(){
        FragmentManager fm = getActivity().getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        ft.remove(this)
                .add(R.id.main_container, new Lobby(), "Lobby")
                .commit();
    }
}