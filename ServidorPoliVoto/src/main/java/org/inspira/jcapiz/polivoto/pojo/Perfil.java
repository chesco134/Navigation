package org.inspira.jcapiz.polivoto.pojo;

/**
 * Created by jcapiz on 7/04/16.
 */
public class Perfil extends ModeloDeDatos {
    //idPerfil INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, perfil text not null
    private String perfil;

    public Perfil() {
    }

    public Perfil(int id) {
        super(id);
    }

    public String getPerfil() {
        return perfil;
    }

    public void setPerfil(String perfil) {
        this.perfil = perfil;
    }
}
