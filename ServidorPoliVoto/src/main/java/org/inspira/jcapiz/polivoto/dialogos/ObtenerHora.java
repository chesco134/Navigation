package org.inspira.jcapiz.polivoto.dialogos;

import android.app.Dialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.text.format.DateFormat;
import android.util.Log;
import android.widget.TimePicker;

import java.util.Calendar;
import java.util.Date;

/**
 * Created by jcapiz on 2/01/16.
 */
public class ObtenerHora extends DialogFragment
        implements TimePickerDialog.OnTimeSetListener {

    private DialogoDeConsultaSimple.AgenteDeInteraccionConResultado agenteDeInteraccion;
    private long hora;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the current time as the default values for the picker
        final Calendar c = Calendar.getInstance();
        int hour = c.get(Calendar.HOUR_OF_DAY);
        int minute = c.get(Calendar.MINUTE);

        // Create a new instance of TimePickerDialog and return it
        TimePickerDialog tpd = new TimePickerDialog(getActivity(), this, hour, minute,
                DateFormat.is24HourFormat(getActivity()));
        tpd.setTitle("Definir hora");
        return tpd;
    }

    @Override
    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        // Do something with the time chosen by the user
        Log.d("Trophy", "Hora del día: " + hourOfDay + ":" + minute);
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
        calendar.set(Calendar.MINUTE, minute);
        hora = calendar.getTimeInMillis();
        agenteDeInteraccion.clickSobreAccionPositiva(ObtenerHora.this);
    }

    public void setAgenteDeInteraccion(DialogoDeConsultaSimple.AgenteDeInteraccionConResultado agenteDeInteraccion) {
        this.agenteDeInteraccion = agenteDeInteraccion;
    }

    public long getHora() {
        return hora;
    }
}
