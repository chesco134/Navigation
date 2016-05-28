package org.inspira.jcapiz.polivoto.pojo;

/**
 * Created by Siempre on 22/02/2016.
 */
public class ModeloDeDatos implements Shareable {

    private int id;

    public ModeloDeDatos(){}

    public ModeloDeDatos(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}
