package org.inspira.jcapiz.polivoto.pojo;

/**
 * Created by jcapiz on 7/04/16.
 */
public class UserAction extends ModeloDeDatos {

    //UserAction(idUserAction INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, idLoginAttempt INTEGER NOT NULL, Action text not null, Action_Timestamp text not null
    private int idLoginAttempt;
    private String action;

    public UserAction() {
    }

    public UserAction(int id) {
        super(id);
    }

    public int getIdLoginAttempt() {
        return idLoginAttempt;
    }

    public void setIdLoginAttempt(int idLoginAttempt) {
        this.idLoginAttempt = idLoginAttempt;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }
}
