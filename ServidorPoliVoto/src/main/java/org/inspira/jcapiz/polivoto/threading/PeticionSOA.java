package org.inspira.jcapiz.polivoto.threading;

import org.inspira.jcapiz.polivoto.networking.soap.ClienteSOA;
import org.json.JSONException;
import org.json.JSONObject;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;

/**
 * Created by jcapiz on 3/12/15.
 */
public class PeticionSOA extends Thread {

    private int action;
    private JSONObject json;
    private boolean error;

    public PeticionSOA(){
        error = false;
    }

    public void setAction(int action){
        this.action = action;
    }

    public void setJson(JSONObject json){
        this.json = json;
    }

    @Override
    public void run(){
        try {
            json.put("action",action);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        ClienteSOA ex = new ClienteSOA();
        try {
            error = !"success".equals(ex.sendAction(json.toString()));
        } catch (IOException | XmlPullParserException e) {
            e.printStackTrace();
            error = true;
        }
    }

    public boolean error(){
        return error;
    }
}
