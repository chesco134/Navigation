package org.inspira.jcapiz.polivoto.actividades;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;

import org.inspira.jcapiz.polivoto.R;
import org.inspira.jcapiz.polivoto.dialogos.Informacion;
import org.inspira.jcapiz.polivoto.fragmentos.SolicitarClave;

/**
 * Created by jcapiz on 4/01/16.
 */
public class SolicitarClaveAdmin extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.solo_un_frame_layout);
        if(savedInstanceState == null){
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new SolicitarClave())
                    .commit();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.menu_principal, menu);
        return true;
    }

    public void showInformationDialog(String mensaje){
        Informacion info = new Informacion();
        Bundle argumentos = new Bundle();
        argumentos.putString("mensaje",mensaje);
        info.setArguments(argumentos);
        info.show(getSupportFragmentManager(), "Informacion");
    }
}