package org.inspira.jcapiz.polivoto.pojo;

/**
 * Created by jcapiz on 7/04/16.
 */
public class VotacionGlobal extends ModeloDeDatos {

    // VotacionGlobal(idVotacion INTEGER NOT NULL PRIMARY KEY, Sincronizado INTEGER NOT NULL DEFAULT 0
    private boolean sincronizado;

    public VotacionGlobal() {
    }

    public VotacionGlobal(int id) {
        super(id);
    }

    public boolean isSincronizado() {
        return sincronizado;
    }

    public void setSincronizado(boolean sincronizado) {
        this.sincronizado = sincronizado;
    }
}
