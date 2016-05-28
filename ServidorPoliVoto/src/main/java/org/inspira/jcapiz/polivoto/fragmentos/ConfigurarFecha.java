package org.inspira.jcapiz.polivoto.fragmentos;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.inspira.jcapiz.polivoto.R;
import org.inspira.jcapiz.polivoto.dialogos.DialogoDeConsultaSimple;
import org.inspira.jcapiz.polivoto.dialogos.ObtenerFecha;
import org.inspira.jcapiz.polivoto.dialogos.ObtenerHora;
import org.inspira.jcapiz.polivoto.pojo.TiempoDeVotacion;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by jcapiz on 28/01/16.
 */
public class ConfigurarFecha extends Fragment {

    public interface AccionAceptar{
        void aceptar(TiempoDeVotacion tiempoDeVotacion);
    }

    private TextView pregunta;
    private TextView fecha;
    private TextView hora;
    private TiempoDeVotacion tiempoDeVotacion;
    private AccionAceptar accionAceptar;

    public void setAccionAceptar(AccionAceptar accionAceptar) {
        this.accionAceptar = accionAceptar;
    }

    @Override
    public void onAttach(Context context){
        super.onAttach(context);
    }

    @Override
    public View onCreateView(LayoutInflater layoutInflater, ViewGroup parent, Bundle savedInstanceState){
        View rootView = layoutInflater.inflate(R.layout.configurar_fecha, parent, false);
        pregunta = (TextView) rootView.findViewById(R.id.configurar_fecha_texto_indicacion);
        fecha = (TextView)rootView.findViewById(R.id.configurar_fecha_texto_fecha);
        hora = (TextView)rootView.findViewById(R.id.configurar_fecha_texto_hora);
        fecha.setTypeface(Typeface.createFromAsset(getActivity().getAssets(),"RobotoCondensed-Bold.ttf"));
        hora.setTypeface(Typeface.createFromAsset(getActivity().getAssets(),"RobotoCondensed-Bold.ttf"));
        rootView.findViewById(R.id.configurar_fecha_boton_aceptar)
                .setOnClickListener(
                        new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                accionAceptar.aceptar(tiempoDeVotacion);
                            }
                        }
                );
        fecha.setOnClickListener(new ClickSobreElementoDeFecha());
        hora.setOnClickListener(new ClickSobreElementoDeHora());
        pregunta.setTypeface(Typeface.createFromAsset(getActivity().getAssets(), "Roboto-Black.ttf"));
        if( savedInstanceState == null ){
            pregunta.setText(getArguments().getString("pregunta"));
            tiempoDeVotacion = (TiempoDeVotacion) getArguments().getSerializable("tiempo_de_votacion");
            if(tiempoDeVotacion == null)
                tiempoDeVotacion = new TiempoDeVotacion();
            else {
                fecha.setText(tiempoDeVotacion.obtenerTextoFecha());
                hora.setText(tiempoDeVotacion.obtenerTextHora());
            }
        }else{
            tiempoDeVotacion = (TiempoDeVotacion) savedInstanceState.getSerializable("tiempo_de_votacion");
            assert tiempoDeVotacion != null;
            fecha.setText(tiempoDeVotacion.obtenerTextoFecha());
            hora.setText(tiempoDeVotacion.obtenerTextHora());
            pregunta.setText(savedInstanceState.getString("pregunta"));
        }
        return rootView;
    }

    @Override
    public void onSaveInstanceState(Bundle outState){
        super.onSaveInstanceState(outState);
        outState.putSerializable("tiempo_de_votacion", tiempoDeVotacion);
        outState.putString("pregunta", pregunta.getText().toString());
    }

    public void reiniciar(){
        if( fecha != null)
            fecha.setText("Fecha");
        if( hora != null )
            hora.setText("Hora");
    }

    private void makeSnackbar(String mensaje){
        Snackbar.make(getView(), mensaje, Snackbar.LENGTH_SHORT)
                .setAction("Aviso", null).show();
    }

    private class ClickSobreElementoDeFecha implements View.OnClickListener{

        @Override
        public void onClick(View view){
            ((TextView)view).setTextColor(Color.GRAY);
            showDatePickerDialog((TextView) view);
        }
    }

    private void showDatePickerDialog(TextView tView) {
        ObtenerFecha newFragment = new ObtenerFecha();
        newFragment.setAgenteDeInteraccion(new AgenteDeInteraccionConFecha(tView));
        newFragment.show(getActivity().getSupportFragmentManager(), "datePicker");
    }

    private class ClickSobreElementoDeHora implements View.OnClickListener{

        @Override
        public void onClick(View view){
            ((TextView)view).setTextColor(Color.GRAY);
            showTimePickerDialog((TextView)view);
        }
    }

    private void showTimePickerDialog(TextView tView) {
        ObtenerHora newFragment = new ObtenerHora();
        newFragment.setAgenteDeInteraccion(new AgenteDeInteraccionConHora(tView));
        newFragment.show(getActivity().getSupportFragmentManager(), "timePicker");
    }

    private class AgenteDeInteraccionConFecha implements DialogoDeConsultaSimple.AgenteDeInteraccionConResultado{

        private TextView etiquetaDeFecha;

        public AgenteDeInteraccionConFecha(TextView etiquetaDeFecha) {
            this.etiquetaDeFecha = etiquetaDeFecha;
        }

        @Override
        public void clickSobreAccionPositiva(DialogFragment dialogo) {
            long tiempoActual = new Date().getTime() - 10; // Margen de error de 10 ms.
            ObtenerFecha fecha = (ObtenerFecha) dialogo;
            long fechaObtenida = fecha.getFecha();
            if( fechaObtenida - tiempoActual >= 0 ){
                etiquetaDeFecha.setText(new SimpleDateFormat("dd/MM/yyyy").format(fechaObtenida));
                Calendar c = Calendar.getInstance();
                c.setTimeInMillis(fechaObtenida);
                tiempoDeVotacion.setYear(c.get(Calendar.YEAR));
                tiempoDeVotacion.setMonth(c.get(Calendar.MONTH));
                tiempoDeVotacion.setDayOfMonth(c.get(Calendar.DAY_OF_MONTH));
            }else{
                makeSnackbar("Fecha anterior al día de hoy");
            }
        }

        @Override
        public void clickSobreAccionNegativa(DialogFragment dialogo) {}
    }

    private class AgenteDeInteraccionConHora implements DialogoDeConsultaSimple.AgenteDeInteraccionConResultado{

        public TextView etiquetaDeHora;

        public AgenteDeInteraccionConHora(TextView etiquetaDeHora) {
            this.etiquetaDeHora = etiquetaDeHora;
        }

        @Override
        public void clickSobreAccionPositiva(DialogFragment dialogo) {
            ObtenerHora hora = (ObtenerHora) dialogo;
            Calendar c = Calendar.getInstance();
            c.setTimeInMillis(hora.getHora());
            int horaDelDia = c.get(Calendar.HOUR_OF_DAY);
            int minuto = c.get(Calendar.MINUTE);
            if ( hora.getHora() - new Date().getTime() >= 0 ) {
                String textoDeHora =
                        (horaDelDia < 10 ? "0" + horaDelDia : horaDelDia)
                                + ":" +
                                (minuto < 10 ? "0" + minuto : minuto);
                etiquetaDeHora.setText(textoDeHora);
                tiempoDeVotacion.setHourOfDay(horaDelDia);
                tiempoDeVotacion.setMinute(minuto);
            }else{
                makeSnackbar("Minutos incorrectos");
            }
        }

        @Override
        public void clickSobreAccionNegativa(DialogFragment dialogo) {}
    }
}