package org.inspira.jcapiz.polivoto.pojo;

/**
 * Created by jcapiz on 7/04/16.
 */
public class Voto implements Shareable {

    //Voto(idVoto blob not null, idVotacion INTEGER NOT NULL, idPerfil INTEGER NOT NULL
    //Voto text not null, idLoginAttempt INTEGER NOT NULL, idPregunta INTEGER NOT NULL
    private byte[] idVoto;
    private int idVotacion;
    private int idPerfil;
    private String voto;
    private int idLoginAttempt;
    private int idPregunta;

    public byte[] getIdVoto() {
        return idVoto;
    }

    public void setIdVoto(byte[] idVoto) {
        this.idVoto = idVoto;
    }

    public int getIdVotacion() {
        return idVotacion;
    }

    public void setIdVotacion(int idVotacion) {
        this.idVotacion = idVotacion;
    }

    public int getIdPerfil() {
        return idPerfil;
    }

    public void setIdPerfil(int idPerfil) {
        this.idPerfil = idPerfil;
    }

    public String getVoto() {
        return voto;
    }

    public void setVoto(String voto) {
        this.voto = voto;
    }

    public int getIdLoginAttempt() {
        return idLoginAttempt;
    }

    public void setIdLoginAttempt(int idLoginAttempt) {
        this.idLoginAttempt = idLoginAttempt;
    }

    public int getIdPregunta() {
        return idPregunta;
    }

    public void setIdPregunta(int idPregunta) {
        this.idPregunta = idPregunta;
    }
}
