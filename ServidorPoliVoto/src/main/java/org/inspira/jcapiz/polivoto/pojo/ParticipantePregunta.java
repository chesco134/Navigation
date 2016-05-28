package org.inspira.jcapiz.polivoto.pojo;

/**
 * Created by jcapiz on 7/04/16.
 */
public class ParticipantePregunta implements Shareable {

    //Participante_Pregunta(Boleta TEXT not null, idPregunta INTEGER NOT NULL, Hora_Registro text not null, Hora_Participacion text
    private String boleta;
    private int idPregunta;
    private String horaRegistro;
    private String horaParticipacion;

    public String getBoleta() {
        return boleta;
    }

    public void setBoleta(String boleta) {
        this.boleta = boleta;
    }

    public int getIdPregunta() {
        return idPregunta;
    }

    public void setIdPregunta(int idPregunta) {
        this.idPregunta = idPregunta;
    }

    public String getHoraRegistro() {
        return horaRegistro;
    }

    public void setHoraRegistro(String horaRegistro) {
        this.horaRegistro = horaRegistro;
    }

    public String getHoraParticipacion() {
        return horaParticipacion;
    }

    public void setHoraParticipacion(String horaParticipacion) {
        this.horaParticipacion = horaParticipacion;
    }
}
