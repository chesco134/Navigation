package org.inspira.jcapiz.polivoto.pojo;

import java.io.Serializable;

/**
 * Created by jcapiz on 9/05/16.
 */
public class ControlDeRetorno implements Serializable {

    private int value;

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }

    public void increaseValue(){
        value++;
    }

    public void decreaseValue(){
        value--;
    }
}
