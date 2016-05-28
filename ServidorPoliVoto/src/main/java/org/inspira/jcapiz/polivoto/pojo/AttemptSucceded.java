package org.inspira.jcapiz.polivoto.pojo;

/**
 * Created by jcapiz on 7/04/16.
 */
public class AttemptSucceded extends ModeloDeDatos {

    private byte[] secretKey;

    public AttemptSucceded() {
    }

    public AttemptSucceded(int id) {
        super(id);
    }

    public byte[] getSecretKey() {
        return secretKey;
    }

    public void setSecretKey(byte[] secretKey) {
        this.secretKey = secretKey;
    }
}
