package org.inspira.jcapiz.polivoto.pojo;

/**
 * Created by jcapiz on 7/04/16.
 */
public class Usuario extends ModeloDeDatos {

    //Usuario(idUsuario INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, Name text not null, Psswd blob not null)
    private String name;
    private byte[] psswd;

    public Usuario() {
    }

    public Usuario(int id) {
        super(id);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public byte[] getPsswd() {
        return psswd;
    }

    public void setPsswd(byte[] psswd) {
        this.psswd = psswd;
    }
}
