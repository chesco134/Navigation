package resumenresultados.shared;

import java.io.Serializable;
import java.util.LinkedList;

public class Pregunta implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String titulo;
	private LinkedList<Opcion> opciones;
	private LinkedList<ResultadoPorPerfil> resultadosPorPerfil;

	public Pregunta(String titulo) {
		this.titulo = titulo;
		opciones = new LinkedList<>();
        resultadosPorPerfil = new LinkedList<>();
	}

	public Opcion obtenerOpcion(int posicion) {
		return opciones.get(posicion);
	}

	public void quitarOpcion(String opcion) {
		int posicion = buscarOpcion(opcion);
		if (posicion > -1) {
			opciones.remove(posicion);
		}
	}

	public void agregarOpcion(String opcion) {
		int posicion = buscarOpcion(opcion);
		if (posicion == -1) {
            opciones.add(new Opcion(opcion));
		}
	}

	public void agregarOpcion(Opcion opcion) {
		int posicion = buscarOpcion(opcion.getNombre());
		if (posicion == -1) {
            opciones.add(opcion);
		}
	}

	public boolean existeOpcion(String opcion){
		return buscarOpcion(opcion) > -1;
	}

	public void setTitulo(String titulo) {
		this.titulo = titulo;
	}

	public int obtenerCantidadDeOpciones() {
		return opciones.size();
	}

	private int buscarOpcion(String opcion) {
		int posicion = -1;
		for (int i = 0; i < opciones.size(); i++) {
			if (opciones.get(i).getNombre().equals(opcion)) {
				posicion = i;
				break;
			}
		}
		return posicion;
	}

    public void agregarResultadoPorPerfil(ResultadoPorPerfil resultadoPorPerfil){
        int posicion = buscarPerfil(resultadoPorPerfil.getPerfil());
        if(posicion == -1)
            resultadosPorPerfil.add(resultadoPorPerfil);
    }

    public int buscarPerfil(String perfil){
        int posicion = -1;
        for(int i=0; i<resultadosPorPerfil.size(); i++){
            if(resultadosPorPerfil.get(i).getPerfil().equals(perfil)){
                posicion = i;
                break;
            }
        }
        return posicion;
    }

	public String getTitulo() {
		return titulo;
	}

	public LinkedList<Opcion> getOpciones() {
		return opciones;
	}

	public void setOpciones(LinkedList<Opcion> opciones) {
		this.opciones = opciones;
	}

	public LinkedList<ResultadoPorPerfil> getResultadosPorPerfil() {
		return resultadosPorPerfil;
	}

	public void setResultadosPorPerfil(LinkedList<ResultadoPorPerfil> resultadosPorPerfil) {
		this.resultadosPorPerfil = resultadosPorPerfil;
	}
}
