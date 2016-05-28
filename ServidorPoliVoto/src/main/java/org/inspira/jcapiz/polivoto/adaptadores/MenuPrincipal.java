package org.inspira.jcapiz.polivoto.adaptadores;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import org.inspira.jcapiz.polivoto.R;
import org.inspira.jcapiz.polivoto.proveedores.ProveedorDeRecursos;
import org.inspira.jcapiz.polivoto.actividades.ConfiguraParticipantes;
import org.inspira.jcapiz.polivoto.actividades.SolicitarClaveAdmin;
import org.inspira.jcapiz.polivoto.fragmentos.GruposPoblacion;
import org.inspira.jcapiz.polivoto.fragmentos.HistorialVotaciones;
import org.inspira.jcapiz.polivoto.fragmentos.Ubicacion;
import org.inspira.jcapiz.polivoto.servicios.AtencionConsultaDatosVotaciones;

/**
 * Created by jcapiz on 31/12/15.
 */
public class MenuPrincipal extends BaseAdapter {

    private Context context;

    public MenuPrincipal(Context context){
        this.context = context;
    }

    @Override
    public int getCount() {
        return LOGOS.length;
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View rootView;
        rootView = ((LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE))
                .inflate(R.layout.panel_boton, parent, false);
        if(parent.getWidth() > 0){
            rootView.setLayoutParams(new AbsListView.LayoutParams((parent.getWidth()/3) -2, (parent.getWidth()/3) - 2));
            rootView.setBackgroundResource(colores[position]);
        }
        ImageView icon = (ImageView) rootView.findViewById(R.id.panel_boton_icono);
        icon.setImageResource(LOGOS[position]);
        TextView panelText = (TextView) rootView.findViewById(R.id.panel_boton_texto);
        if(position == 5)
            if(ProveedorDeRecursos.obtenerRecursoEntero(context, "extado_servicio_historial") == 0)
                panelText.setText(ETIQUETAS[position]);
            else
                panelText.setText("Deshabilitar\nhistorial");
        else
            panelText.setText(ETIQUETAS[position]);
        panelText.setTypeface(Typeface.createFromAsset(context.getAssets(), "Roboto-Black.ttf"));
        rootView.setOnClickListener(new ButtonClickHandler(position));
        return rootView;
    }

    private class ButtonClickHandler implements View.OnClickListener{

        private final int destiny;

        public ButtonClickHandler(int destiny) {
            this.destiny = destiny;
        }

        @Override
        public void onClick(View view){
            switch(destiny){
                case 0: // Zona
                    switchFragment(new Ubicacion());
                    break;
                case 1: // Grupos
                    switchFragment(new GruposPoblacion());
                    break;
                case 2: // Credenciales
                    /**
                     * Arma un Diálogo que permita ingresar el pass y su confirmación,
                     * para la lista de usuarios que existen en la base de datos.
                     * Los anteriores viven en unn ListView.
                     * **/
                    context.startActivity(new Intent(context, SolicitarClaveAdmin.class));
                    break;
                case 3: // Votaciones
                    switchFragment(new HistorialVotaciones());
                    break;
                case 4: // Configuración
                    launchConfiguraciones();
                    break;
                case 5: // Configuración
                    TextView panelText = (TextView) view.findViewById(R.id.panel_boton_texto);
                    int estadoRecursoHistorial = ProveedorDeRecursos
                            .obtenerRecursoEntero(context, "extado_servicio_historial");
                    if(estadoRecursoHistorial == 0) {
                        panelText.setText("Deshabilitar\nhistorial");
                        context.startService(new Intent(context, AtencionConsultaDatosVotaciones.class));
                    }else {
                        panelText.setText(R.string.texto_habilita_acceso_a_historial_votaciones);
                        context.stopService(new Intent(context, AtencionConsultaDatosVotaciones.class));
                    }
                    ProveedorDeRecursos.guardarRecursoEntero(context, "extado_servicio_historial",
                            estadoRecursoHistorial == 0 ? 1 : 0);
                    break;
            }
        }
    }

    private void switchFragment(Fragment fragment){
        Bundle argumentos = new Bundle();
        argumentos.putBoolean("isChange", true);
        fragment.setArguments(argumentos);
        ((AppCompatActivity)context).getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.main_container, fragment)
                .addToBackStack(null)
                .commit();
    }

    private void launchConfiguraciones() {
        context.startActivity(new Intent(context, ConfiguraParticipantes.class));
    }

    private static final int[] LOGOS = {R.drawable.ic_location_on_white_24dp,
                                        R.drawable.ic_people_white_24dp,
                                        R.drawable.ic_account_balance_wallet_white_24dp,
                                        R.drawable.ic_event_white_24dp,
                                        R.drawable.ic_settings_white_24dp,
                                        R.drawable.ic_tap_and_play_white_24dp};
    private static final int[] ETIQUETAS = {R.string.texto_zona,
                                            R.string.texto_grupos,
                                            R.string.texto_credenciales,
                                            R.string.texto_votaciones,
                                            R.string.texto_configuraciones,
                                            R.string.texto_habilita_acceso_a_historial_votaciones};
    private static final int[] colores = {R.drawable.my_custom_panel_button_3,
            R.drawable.my_custom_panel_button_2,
            R.drawable.my_custom_panel_button_1,
            R.drawable.my_custom_panel_button_4,
            R.drawable.my_custom_panel_button_5,
            R.drawable.my_custom_panel_button_6};

}
