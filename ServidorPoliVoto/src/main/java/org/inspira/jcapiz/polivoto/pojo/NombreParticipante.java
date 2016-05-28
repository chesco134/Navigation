package org.inspira.jcapiz.polivoto.pojo;

/**
 * Created by jcapiz on 7/04/16.
 */
public class NombreParticipante implements Shareable {

    //Boleta TEXT NOT NULL, Nombre TEXT NOT NULL, ApPaterno TEXT NOT NULL, ApMaterno TEXT NOT NULL, primary key(Boleta), foreign key(Boleta) references Participante(Boleta) ON DELETE CASCADE ON UPDATE CASCADE
    private String boleta;
    private String nombre;
    private String apPaterno;
    private String apMaterno;

    public NombreParticipante(String boleta) {
        this.boleta = boleta;
    }

    public String getBoleta() {
        return boleta;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getApPaterno() {
        return apPaterno;
    }

    public void setApPaterno(String apPaterno) {
        this.apPaterno = apPaterno;
    }

    public String getApMaterno() {
        return apMaterno;
    }

    public void setApMaterno(String apMaterno) {
        this.apMaterno = apMaterno;
    }
}
