package org.inspira.jcapiz.polivoto.pojo;

/**
 * Created by jcapiz on 7/04/16.
 */
public class Escuela extends ModeloDeDatos {

    //idEscuela INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, Nombre text not null, latitud real, longitud real
    private String nombre;
    private int idCategoria;

    public Escuela() {
    }

    public Escuela(int id) {
        super(id);
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public int getIdCategoria() {
        return idCategoria;
    }

    public void setIdCategoria(int idCategoria) {
        this.idCategoria = idCategoria;
    }
}
