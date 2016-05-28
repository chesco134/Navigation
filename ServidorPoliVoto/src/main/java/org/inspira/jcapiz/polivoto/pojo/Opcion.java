package org.inspira.jcapiz.polivoto.pojo;

/**
 * Created by jcapiz on 7/04/16.
 */
public class Opcion extends ModeloDeDatos {

    //Opcion(idOpcion INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, Reactivo TEXT not null)
    private String reactivo;
    private int idPregunta;

    public Opcion() {
        super();
    }

    public Opcion(String opcion, int idPregunta){
        super();
        reactivo = opcion;
        this.idPregunta = idPregunta;
    }

    public Opcion(int id) {
        super(id);
    }

    public String getReactivo() {
        return reactivo;
    }

    public void setReactivo(String reactivo) {
        this.reactivo = reactivo;
    }

    public int getIdPregunta() {
        return idPregunta;
    }

    public void setIdPregunta(int idPregunta) {
        this.idPregunta = idPregunta;
    }
}
