package org.inspira.jcapiz.polivoto.database.acciones;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import org.inspira.jcapiz.polivoto.database.Votaciones;

import java.util.Date;

/**
 * Created by jcapiz on 14/04/16.
 */
public class AccionesTablaUsuario {

    public static boolean revisaValidezDeToken(Context context, int token){
        SQLiteDatabase db = new Votaciones(context).getReadableDatabase();
        Cursor c = db.rawQuery("select count(*) from AttemptSucceded where " +
                "idLoginAttempt = CAST(? as INTEGER) and CAST(? as LONG) < expiration_time",
                new String[]{String.valueOf(token), String.valueOf(new Date().getTime())});
        c.moveToFirst();
        boolean esValido = c.getInt(0) > 0;
        c.close();
        db.close();
        return true;
    }
}
