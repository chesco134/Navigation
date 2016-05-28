package org.inspira.jcapiz.polivoto.threading;

import android.view.View;

import org.inspira.jcapiz.polivoto.proveedores.ProveedorSnackBar;

/**
 * Created by jcapiz on 8/05/16.
 */
public class MostrarSnackUI implements Runnable {

    private View view;
    private String mensaje;

    public MostrarSnackUI(View view, String mensaje) {
        this.view = view;
        this.mensaje = mensaje;
    }

    @Override
    public void run(){
        ProveedorSnackBar
                .muestraBarraDeBocados(view, mensaje);
    }
}
