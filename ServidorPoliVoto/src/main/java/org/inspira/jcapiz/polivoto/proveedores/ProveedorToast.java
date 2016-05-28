package org.inspira.jcapiz.polivoto.proveedores;

import android.content.Context;
import android.widget.Toast;

/**
 * Created by Siempre on 22/02/2016.
 */
public class ProveedorToast {

    public static void showToast(Context context, String message){
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }

    public static void showToast(Context context, int messageId){
        Toast.makeText(context, messageId, Toast.LENGTH_SHORT).show();
    }
}
