package org.inspira.jcapiz.polivoto.pojo;

/**
 * Created by jcapiz on 7/04/16.
 */
public class LoginAttempt extends ModeloDeDatos {

    //LoginAttempt(idLoginAttempt INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, idUsuario INTEGER NOT NULL,
    // Attempt_Timestamp text not null, Host text not null,
    private int idUsuario;
    private String timeStamp;
    private String host;

    public LoginAttempt() {
    }

    public LoginAttempt(int id) {
        super(id);
    }

    public int getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(int idUsuario) {
        this.idUsuario = idUsuario;
    }

    public String getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(String timeStamp) {
        this.timeStamp = timeStamp;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }
}
