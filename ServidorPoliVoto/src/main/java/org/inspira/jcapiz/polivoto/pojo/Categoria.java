package org.inspira.jcapiz.polivoto.pojo;

/**
 * Created by jcapiz on 12/05/16.
 */
public class Categoria extends ModeloDeDatos {

    private String nombre;

    public Categoria() {
    }

    public Categoria(int id) {
        super(id);
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }
}
