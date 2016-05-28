package org.inspira.jcapiz.polivoto.threading;

import android.content.Context;

import org.inspira.jcapiz.polivoto.proveedores.ProveedorToast;

/**
 * Created by jcapiz on 10/05/16.
 */
public class MostrarToastUI implements Runnable {

    private Context context;
    private String mensaje;

    public MostrarToastUI(Context context, String mensaje) {
        this.context = context;
        this.mensaje = mensaje;
    }

    @Override
    public void run(){
        ProveedorToast
                .showToast(context, mensaje);
    }
}
