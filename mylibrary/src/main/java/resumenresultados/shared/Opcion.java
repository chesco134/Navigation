package resumenresultados.shared;

import java.io.Serializable;

public class Opcion implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String nombre;
	private int cantidad;

	public Opcion(String nombre) {
		this.nombre = nombre;
		cantidad = 0;
	}

	public String getNombre() {
		return nombre;
	}

    /*

    * Se usa en el conteo de votos, representa la cantidad de
    * elecciones que ha acumulado durante el proceso.
    *
    */

	public void setCantidad(int cantidad){
		this.cantidad = cantidad;
	}

	public int getCantidad() {
		return cantidad;
	}

    public void elevarCantidad(){
        cantidad++;
    }
}