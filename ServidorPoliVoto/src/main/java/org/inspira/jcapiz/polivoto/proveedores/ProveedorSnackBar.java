package org.inspira.jcapiz.polivoto.proveedores;

import android.support.design.widget.Snackbar;
import android.view.View;

import org.inspira.jcapiz.polivoto.R;

/**
 * Created by Siempre on 19/02/2016.
 */
public class ProveedorSnackBar {

    public static void muestraBarraDeBocados(View view, String mensaje){
        Snackbar.make(view, mensaje, Snackbar.LENGTH_SHORT)
                .setAction("Aviso", null).show();
    }
}
