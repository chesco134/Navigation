package org.inspira.jcapiz.polivoto.pojo;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Created by jcapiz on 28/01/16.
 */
public class TiempoDeVotacion implements Shareable{

    private int year;
    private int month;
    private int dayOfMonth;
    private int hourOfDay;
    private int minute;
    private long timeInMillis;

    public TiempoDeVotacion() {
        year = -1;
        month = -1;
        dayOfMonth = -1;
        hourOfDay = -1;
        minute = -1;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public int getMonth() {
        return month;
    }

    public void setMonth(int month) {
        this.month = month;
    }

    public int getDayOfMonth() {
        return dayOfMonth;
    }

    public void setDayOfMonth(int dayOfMonth) {
        this.dayOfMonth = dayOfMonth;
    }

    public int getHourOfDay() {
        return hourOfDay;
    }

    public void setHourOfDay(int hourOfDay) {
        this.hourOfDay = hourOfDay;
    }

    public int getMinute() {
        return minute;
    }

    public void setMinute(int minute) {
        this.minute = minute;
    }

    public long getTimeInMillis() {
        return timeInMillis;
    }

    public String[] comprobarCamposFaltantes(){
        List<String> elementosFaltantes = new ArrayList<>();
        if(year == -1)
            elementosFaltantes.add("Año");
        if(month == -1)
            elementosFaltantes.add("Mes");
        if(dayOfMonth == -1)
            elementosFaltantes.add("Día");
        if(hourOfDay == -1)
            elementosFaltantes.add("Hora");
        if(minute == -1)
            elementosFaltantes.add("Minutos");
        return elementosFaltantes.toArray(new String[]{});
    }

    public void construirTiempo(){
        Calendar c = Calendar.getInstance();
        c.set(year, month, dayOfMonth, hourOfDay, minute);
        timeInMillis = c.getTimeInMillis();
    }

    public String obtenerTextoFecha(){
        return (dayOfMonth < 10 ? "0" + dayOfMonth : dayOfMonth)
                + "/" + ((month+1) < 10 ? "0" + (month+1) : (month+1))
                + "/" + year;
    }

    public String obtenerTextHora(){
        return (hourOfDay < 10 ? "0" + hourOfDay : hourOfDay)
                + ":" + (minute < 10 ? "0" + minute : minute);
    }
}
