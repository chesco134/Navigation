package org.inspira.jcapiz.polivoto.pojo;

/**
 * Created by jcapiz on 7/04/16.
 */
public class Participante implements Shareable{

    //Boleta TEXT, idPerfil INTEGER NOT NULL, idEscuela INTEGER NOT NULL, Fecha_Registro TEXT, BoletaHash BLOB not null, PRIMARY KEY(Boleta), foreign key(idPerfil) references Perfil(idPerfil), foreign key(idEscuela) references Escuela(idEscuela) ON DELETE CASCADE ON UPDATE CASCADE
    private String boleta;
    private int idEscuela;
    private String fechaRegistro;
    private byte[] boletaHash;

    public String getBoleta() {
        return boleta;
    }

    public void setBoleta(String boleta) {
        this.boleta = boleta;
    }

    public int getIdEscuela() {
        return idEscuela;
    }

    public void setIdEscuela(int idEscuela) {
        this.idEscuela = idEscuela;
    }

    public String getFechaRegistro() {
        return fechaRegistro;
    }

    public void setFechaRegistro(String fechaRegistro) {
        this.fechaRegistro = fechaRegistro;
    }

    public byte[] getBoletaHash() {
        return boletaHash;
    }

    public void setBoletaHash(byte[] boletaHash) {
        this.boletaHash = boletaHash;
    }
}
