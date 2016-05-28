package org.inspira.jcapiz.polivoto.actividades;

import android.app.Activity;
import android.os.Bundle;

import org.inspira.jcapiz.polivoto.R;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by jcapiz on 31/12/15.
 */
public class Splash extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash);
    }

    @Override
    protected void onResume(){
        super.onResume();
        new Timer()
                .schedule(new TimerTask(){
                    @Override
                    public void run(){
                        finish();
                    }
                },3000);
    }

    @Override
    public void onBackPressed(){}
}
