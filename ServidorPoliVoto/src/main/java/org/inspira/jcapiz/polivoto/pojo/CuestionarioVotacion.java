package org.inspira.jcapiz.polivoto.pojo;

import java.io.Serializable;
import java.util.List;

/**
 * Created by jcapiz on 9/05/16.
 */
public class CuestionarioVotacion implements Serializable {

    private List<Pregunta> preguntas;

    public CuestionarioVotacion(List<Pregunta> preguntas) {
        this.preguntas = preguntas;
    }

    public List<Pregunta> obtenerPreguntas() {
        return preguntas;
    }

    public int obtenerCantidadPreguntas(){
        return preguntas.size();
    }

    public Pregunta obtenerPregunta(int posicion){
        return preguntas.get(posicion);
    }

    public void agregarPregunta(String enunciado){
        int posicion = buscarPregunta(enunciado);
        if(posicion == -1)
            preguntas.add(new Pregunta(enunciado));
    }

    public int buscarPregunta(String enunciado){
        int posicion = -1;
        for(int i=0; i<preguntas.size(); i++)
            if(preguntas.get(i).getEnunciado().equals(enunciado)){
                posicion = i;
                break;
            }
        return posicion;
    }
}