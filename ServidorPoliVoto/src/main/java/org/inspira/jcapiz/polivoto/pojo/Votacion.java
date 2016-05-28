package org.inspira.jcapiz.polivoto.pojo;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by jcapiz on 7/04/16.
 */
public class Votacion extends ModeloDeDatos {

    private int idEscuela;
    private String lugar;
    private String titulo;
    private long fechaInicio;
    private long fechaFin;
    private boolean global;
    private List<Pregunta> preguntas;

    public Votacion() {
        preguntas = new ArrayList<>();
        fechaInicio = -1;
        fechaFin = -1;
    }

    public Votacion(int id) {
        super(id);
        preguntas = new ArrayList<>();
        fechaInicio = -1;
        fechaFin = -1;
    }

    public int getIdEscuela() {
        return idEscuela;
    }

    public void setIdEscuela(int idEscuela) {
        this.idEscuela = idEscuela;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public long getFechaInicio() {
        return fechaInicio;
    }

    public void setFechaInicio(long fechaInicio) {
        this.fechaInicio = fechaInicio;
    }

    public long getFechaFin() {
        return fechaFin;
    }

    public void setFechaFin(long fechaFin) {
        this.fechaFin = fechaFin;
    }

    public List<Pregunta> getPreguntas() {
        return preguntas;
    }

    public void setPreguntas(List<Pregunta> preguntas) {
        this.preguntas = preguntas;
    }

    public boolean isGlobal() {
        return global;
    }

    public void setGlobal(boolean global) {
        this.global = global;
    }

    public String getLugar() {
        return lugar;
    }

    public void setLugar(String lugar) {
        this.lugar = lugar;
    }

    public void agregarPregunta(Pregunta pregunta){
        int posicion = buscarPregunta(pregunta.getEnunciado());
        if(posicion == -1)
            preguntas.add(pregunta);
    }

    public void agregarPregunta(String pregunta){
        int posicion = buscarPregunta(pregunta);
        if(posicion == -1)
            preguntas.add(new Pregunta(pregunta));
    }

    public int buscarPregunta(String titulo){
        int posicion = -1;
        for(int i=0; i < preguntas.size(); i++)
            if(preguntas.get(i).getEnunciado().equals(titulo)){
                posicion = i;
                break;
            }
        return posicion;
    }

    public Pregunta eliminarPregunta(String enunciado){
        int posicion = buscarPregunta(enunciado);
        if(posicion != -1)
            return preguntas.remove(posicion);
        else
            return null;
    }

    public void agregarOpcion(int posicionPregunta, String enunciadoOpcion){
        preguntas.get(posicionPregunta).agregarOpcion(enunciadoOpcion);
    }

    public int cantidadPreguntas(){
        return preguntas.size();
    }

    public Pregunta obtenerPregunta(int posicion){
        return preguntas.get(posicion);
    }

    public void cambiarEnunciadoPregunta(String enunciadoAnterior, String nuevoEnunciado){
        int posicion = buscarPregunta(enunciadoAnterior);
        Pregunta pregunta;
        if(posicion > -1){
            pregunta = preguntas.get(posicion);
            pregunta.setEnunciado(nuevoEnunciado);
        }
    }
}
