package org.inspira.jcapiz.polivoto.escuchas;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

import org.inspira.jcapiz.polivoto.database.Votaciones;
import org.inspira.jcapiz.polivoto.dialogos.DialogoDeLista;
import org.inspira.jcapiz.polivoto.pojo.Escuela;
import org.inspira.jcapiz.polivoto.proveedores.ProveedorDeRecursos;

/**
 * Created by jcapiz on 12/05/16.
 */
public class ClickSobreContenedorLugarVotacion implements View.OnClickListener {
    private Context context;
    private TextView referencedView;

    public static ClickSobreContenedorLugarVotacion obtenerManejador(Context context, TextView referencedView){
        if(!(context instanceof AppCompatActivity))
            throw new ClassCastException("Sólo podemos trabajar con AppCompatActivity");
        return new ClickSobreContenedorLugarVotacion(context, referencedView);
    }

    private ClickSobreContenedorLugarVotacion(Context context, TextView referencedView){
        this.context = context;
        this.referencedView = referencedView;
    }

    @Override
    public void onClick(View view){
        iniciaListaDeTiposDeCentro();
    }

    private void iniciaListaDeTiposDeCentro(){
        Votaciones db = new Votaciones(context);
        Escuela[] escuelas = db.obtenerEscuelasPorCategoria(ProveedorDeRecursos.obtenerRecursoString(context, "categoria"));
        String[] nombresEscuelas = new String[escuelas.length];
        for(int i=0; i<escuelas.length; i++)
            nombresEscuelas[i] = escuelas[i].getNombre();
        DialogoDeLista ddl = new DialogoDeLista();
        ddl.setElementos(nombresEscuelas);
        ddl.setTitulo("Seleccione escuela o centro");
        ddl.setAccion(addl);
        ddl.show(((AppCompatActivity)context).getSupportFragmentManager(), "Seleccionar escuela");
    }

    private DialogoDeLista.AccionDialogoDeLista addl = new DialogoDeLista.AccionDialogoDeLista() {
        @Override
        public void objetoSeleccionado(String texto) {
            referencedView.setText(texto);
            ProveedorDeRecursos.guardarRecursoString(context, "ubicacion", texto);
        }
    };
}
