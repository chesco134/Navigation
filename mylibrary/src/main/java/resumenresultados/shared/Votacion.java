package resumenresultados.shared;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by jcapiz on 1/01/16.
 */
public class Votacion implements Serializable {

    private String hash;
    private String descripcion;
    private String fechaInicial;
    private String fechaFinal;
    private int matricula;
    private int totalParticipantes;
    private float porcentajeParticipacion;
    private String[] grupos;
    private int idEscuela;
    private String titulo;
    private List<Pregunta> preguntas;

    public Votacion(String titulo){
        this.titulo = titulo;
        preguntas = new ArrayList<>();
    }

    public void agregaPregunta(Pregunta pregunta){
        int posicion = buscaPregunta(pregunta.getTitulo());
        if(posicion == -1){
            preguntas.add(pregunta);
        }
    }

    public void agregaPregunta(String pregunta){
        int posicion = buscaPregunta(pregunta);
        if(posicion == -1){
            preguntas.add(new Pregunta(pregunta));
        }
    }

    public String getTitulo(){
        return titulo;
    }

    public List<Pregunta> getPreguntas(){
        return preguntas;
    }

    private int buscaPregunta(String pregunta){
        int posicion = -1;
        for(int i=0; i<preguntas.size(); i++)
            if(preguntas.get(i).getTitulo().equals(pregunta)){
                posicion = i;
                break;
            }
        return posicion;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getFechaFinal() {
        return fechaFinal;
    }

    public void setFechaFinal(String fechaFinal) {
        this.fechaFinal = fechaFinal;
    }

    public String getFechaInicial() {
        return fechaInicial;
    }

    public void setFechaInicial(String fechaInicial) {
        this.fechaInicial = fechaInicial;
    }

    public String[] getGrupos() {
        return grupos;
    }

    public void setGrupos(String[] grupos) {
        this.grupos = grupos;
    }

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public int getIdEscuela() {
        return idEscuela;
    }

    public void setIdEscuela(int idEscuela) {
        this.idEscuela = idEscuela;
    }

    public int getMatricula() {
        return matricula;
    }

    public void setMatricula(int matricula) {
        this.matricula = matricula;
    }

    public float getPorcentajeParticipacion() {
        return porcentajeParticipacion;
    }

    public void setPorcentajeParticipacion(float porcentajeParticipacion) {
        this.porcentajeParticipacion = porcentajeParticipacion;
    }

    public void setPreguntas(List<Pregunta> preguntas) {
        this.preguntas = preguntas;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public int getTotalParticipantes() {
        return totalParticipantes;
    }

    public void setTotalParticipantes(int totalParticipantes) {
        this.totalParticipantes = totalParticipantes;
    }
}
