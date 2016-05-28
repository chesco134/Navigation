package org.inspira.jcapiz.polivoto.pojo;

/**
 * Created by jcapiz on 7/04/16.
 */
public class PreguntaOpcion implements Shareable {

    //Pregunta_Opcion(idPregunta INTEGER NOT NULL, idOpcion INTEGER NOT NULL
    private int idPregunta;
    private int idOpcion;

    public int getIdPregunta() {
        return idPregunta;
    }

    public void setIdPregunta(int idPregunta) {
        this.idPregunta = idPregunta;
    }

    public int getIdOpcion() {
        return idOpcion;
    }

    public void setIdOpcion(int idOpcion) {
        this.idOpcion = idOpcion;
    }
}
