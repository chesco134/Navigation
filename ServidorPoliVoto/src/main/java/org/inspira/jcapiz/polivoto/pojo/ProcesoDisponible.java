package org.inspira.jcapiz.polivoto.pojo;

/**
 * Created by jcapiz on 12/05/16.
 */
public class ProcesoDisponible {

    private int idVotacion;
    private String titulo;
    private long fFinal;
    private long fInicial;

    public long getfFinal() {
        return fFinal;
    }

    public void setfFinal(long fFinal) {
        this.fFinal = fFinal;
    }

    public long getfInicial() {
        return fInicial;
    }

    public void setfInicial(long fInicial) {
        this.fInicial = fInicial;
    }

    public int getIdVotacion() {
        return idVotacion;
    }

    public void setIdVotacion(int idVotacion) {
        this.idVotacion = idVotacion;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }
}
