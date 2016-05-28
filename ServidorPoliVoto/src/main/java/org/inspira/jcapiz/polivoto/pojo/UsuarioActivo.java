package org.inspira.jcapiz.polivoto.pojo;

/**
 * Created by jcapiz on 7/04/16.
 */
public class UsuarioActivo {

    private int idTipoUsuario;
    private String host;
    private long expiracion;

    public UsuarioActivo(int idTipoUsuario, String host, long expiracion) {
        this.idTipoUsuario = idTipoUsuario;
        this.host = host;
        this.expiracion = expiracion;
    }

    public int getIdTipoUsuario() {
        return idTipoUsuario;
    }

    public void setIdTipoUsuario(int idTipoUsuario) {
        this.idTipoUsuario = idTipoUsuario;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public long getExpiracion() {
        return expiracion;
    }

    public void setExpiracion(long expiracion) {
        this.expiracion = expiracion;
    }
}
