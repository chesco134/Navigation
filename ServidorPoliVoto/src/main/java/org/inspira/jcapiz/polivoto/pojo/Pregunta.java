package org.inspira.jcapiz.polivoto.pojo;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by jcapiz on 7/04/16.
 */
public class Pregunta extends ModeloDeDatos {

    private String pregunta;
    private int idVotacion;
    private List<Opcion> opciones;

    public Pregunta() {
        opciones = new ArrayList<>();
    }

    public Pregunta(String pregunta){
        super();
        this.pregunta = pregunta;
        opciones = new ArrayList<>();
    }

    public Pregunta(int id) {
        super(id);
        opciones = new ArrayList<>();
    }

    public String getEnunciado() {
        return pregunta;
    }

    public void setEnunciado(String pregunta) {
        this.pregunta = pregunta;
    }

    public int getIdVotacion() {
        return idVotacion;
    }

    public void setIdVotacion(int idVotacion) {
        this.idVotacion = idVotacion;
    }

    public List<Opcion> getOpciones() {
        return opciones;
    }

    public void setOpciones(List<Opcion> opciones) {
        this.opciones = opciones;
    }

    public void agregarOpcion(Opcion opcion){
        int posicion = buscarOpcion(opcion.getReactivo());
        if(posicion == -1)
            opciones.add(opcion);
    }

    public void agregarOpcion(String opcion){
        int posicion = buscarOpcion(opcion);
        Opcion op;
        if(posicion == -1) {
            op = new Opcion();
            op.setReactivo(opcion);
            opciones.add(op);
        }
    }

    public int buscarOpcion(String titulo){
        int posicion = -1;
        for(int i=0; i < opciones.size(); i++)
            if(opciones.get(i).getReactivo().equals(titulo)){
                posicion = i;
                break;
            }
        return posicion;
    }

    public Opcion obtenerOpcion(int i) {
        return opciones.get(i);
    }

    public void eliminarOpcion(String reactivo){
        int posicion = buscarOpcion(reactivo);
        if(posicion > -1){
            opciones.remove(posicion);
        }
    }

    public void cambiarEnunciadoOpcion(String enunciadoAnterior, String nuevoEnunciado){
        int posicion = buscarOpcion(enunciadoAnterior);
        if(posicion > -1){
            Opcion opcion = opciones.get(posicion);
            opcion.setReactivo(nuevoEnunciado);
        }
    }

    public int obtenerCantidadOpciones(){
        return opciones.size();
    }
}
