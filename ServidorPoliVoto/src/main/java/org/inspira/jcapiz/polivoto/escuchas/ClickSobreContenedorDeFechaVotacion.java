package org.inspira.jcapiz.polivoto.escuchas;

import android.content.Context;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

import org.inspira.jcapiz.polivoto.dialogos.DialogoDeConsultaSimple;
import org.inspira.jcapiz.polivoto.dialogos.ObtenerFecha;
import org.inspira.jcapiz.polivoto.dialogos.ObtenerHora;
import org.inspira.jcapiz.polivoto.pojo.Votacion;
import org.inspira.jcapiz.polivoto.proveedores.ProveedorDeRecursos;
import org.inspira.jcapiz.polivoto.proveedores.ProveedorSnackBar;

import java.util.Calendar;

/**
 * Created by jcapiz on 8/05/16.
 */
public class ClickSobreContenedorDeFechaVotacion implements View.OnClickListener {

    public static final int FECHA_INICIAL = 0;
    public static final int FECHA_FINAL = 1;

    private Context context;
    private Votacion votacion;
    private TextView etiquetaFechaInicial;
    private TextView etiquetaFechaFinal;
    private View v;
    private int tipoDeFecha;
    private long fechaActual;
    private long fechaObtenida;

    public ClickSobreContenedorDeFechaVotacion(Context context, Votacion votacion, int tipoDeFecha, TextView etiquetaFechaInicial, TextView etiquetaFechaFinal) {
        this.context = context;
        this.votacion = votacion;
        this.tipoDeFecha = tipoDeFecha;
        this.etiquetaFechaInicial = etiquetaFechaInicial;
        this.etiquetaFechaFinal = etiquetaFechaFinal;
    }

    @Override
    public void onClick(View v) {
        this.v = v;
        fechaActual = new java.util.Date().getTime();
        iniciarObtencionDeFecha();
    }

    private void iniciarObtencionDeFecha(){
        ObtenerFecha of = new ObtenerFecha();
        of.setAgenteDeInteraccion(agenteDeFecha);
        of.show(((AppCompatActivity) context).getSupportFragmentManager(), "obtener fecha");
    }

    private void iniciarObtencionDeHora(){
        ObtenerHora oh = new ObtenerHora();
        oh.setAgenteDeInteraccion(agenteDeHora);
        oh.show(((AppCompatActivity)context).getSupportFragmentManager(), "obtener hora");
    }
    private DialogoDeConsultaSimple.AgenteDeInteraccionConResultado agenteDeFecha = new DialogoDeConsultaSimple.AgenteDeInteraccionConResultado() {

        @Override
        public void clickSobreAccionPositiva(DialogFragment dialogo) {
            fechaObtenida = ((ObtenerFecha) dialogo).getFecha();
            long deltaF = fechaObtenida - fechaActual;
            if( deltaF >= 0){
                switch (tipoDeFecha){
                    case FECHA_INICIAL:
                        iniciarObtencionDeHora();
                        break;
                    case FECHA_FINAL:
                        if(deltaF < 432e7){
                            iniciarObtencionDeHora();
                        }else{
                            ProveedorSnackBar
                                    .muestraBarraDeBocados(v, "No podemos exeder de 5 días");
                        }
                        break;
                    default:
                }
            }else{
                ProveedorSnackBar
                        .muestraBarraDeBocados(v, "La fecha seleccionada es incorrecta");
            }
        }

        @Override
        public void clickSobreAccionNegativa(DialogFragment dialogo) {}
    };
    
    private DialogoDeConsultaSimple.AgenteDeInteraccionConResultado agenteDeHora = new DialogoDeConsultaSimple.AgenteDeInteraccionConResultado() {
        
        @Override
        public void clickSobreAccionPositiva(DialogFragment dialogo) {
            long milis = ((ObtenerHora) dialogo).getHora();
            Calendar c = Calendar.getInstance();
            c.setTimeInMillis(milis);
            Calendar miCalendarioActual = Calendar.getInstance();
            miCalendarioActual.setTimeInMillis(fechaObtenida);
            miCalendarioActual.set(Calendar.HOUR_OF_DAY, c.get(Calendar.HOUR_OF_DAY));
            miCalendarioActual.set(Calendar.MINUTE, c.get(Calendar.MINUTE));
            milis = miCalendarioActual.getTimeInMillis(); // Tiempo neto especificado
            long votacionFI = votacion.getFechaInicio();
            long votacionFF = votacion.getFechaFin();
            long deltaF = milis - fechaActual;
            long deltaFI = milis - votacionFI;
            switch (tipoDeFecha){
                case FECHA_INICIAL:
                    if( votacion.isGlobal() ) {
                        if(deltaF > 20000){
                            if(votacionFF != -1 && milis - votacionFF >= 0){
                                ProveedorSnackBar
                                        .muestraBarraDeBocados(v, "Debe cambiar también la fecha final");
                                etiquetaFechaFinal.setText("");
                                votacion.setFechaFin(-1l);
                            }
                            votacion.setFechaInicio(milis);
                            etiquetaFechaInicial.setText(ProveedorDeRecursos.obtenerFecha(new java.util.Date(milis)));
                        }else{
                            ProveedorSnackBar
                                    .muestraBarraDeBocados(v, "Los preparativos toman 20 segundos mínimo");
                        }
                    }else {
                        if (deltaF >= 1000) {
                            if(votacionFF != -1 && milis - votacionFF >= 0){
                                ProveedorSnackBar
                                        .muestraBarraDeBocados(v, "Debe cambiar también la fecha final");
                                etiquetaFechaFinal.setText("");
                                votacion.setFechaFin(-1l);
                            }
                            votacion.setFechaInicio(milis);
                            etiquetaFechaInicial.setText(ProveedorDeRecursos.obtenerFecha(new java.util.Date(milis)));
                        } else {
                            ProveedorSnackBar
                                    .muestraBarraDeBocados(v, "Los preparativos toman 1 segundo mínimo");
                        }
                    }
                    break;
                case FECHA_FINAL:
                    if(votacion.isGlobal()){
                        if(deltaF >= 80000){
                            etiquetaFechaFinal.setText(ProveedorDeRecursos.obtenerFecha(new java.util.Date(milis)));
                            votacion.setFechaFin(milis);
                        }else{
                            ProveedorSnackBar
                                    .muestraBarraDeBocados(v, "La duración mínima es 1 minuto");
                        }
                    }else{
                        if(deltaF >= 61000){
                            etiquetaFechaFinal.setText(ProveedorDeRecursos.obtenerFecha(new java.util.Date(milis)));
                            votacion.setFechaFin(milis);
                        }else{
                            ProveedorSnackBar
                                    .muestraBarraDeBocados(v, "La duración mínima es 1 minuto");
                        }
                    }
                    break;
                default:
            }
        }

        @Override
        public void clickSobreAccionNegativa(DialogFragment dialogo) {}
    };
}
