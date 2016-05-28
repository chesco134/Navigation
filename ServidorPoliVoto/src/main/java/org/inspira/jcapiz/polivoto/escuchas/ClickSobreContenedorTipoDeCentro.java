package org.inspira.jcapiz.polivoto.escuchas;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.inspira.jcapiz.polivoto.R;
import org.inspira.jcapiz.polivoto.database.Votaciones;
import org.inspira.jcapiz.polivoto.dialogos.DialogoDeLista;
import org.inspira.jcapiz.polivoto.pojo.Categoria;
import org.inspira.jcapiz.polivoto.proveedores.ProveedorDeRecursos;

/**
 * Created by jcapiz on 12/05/16. Tl
 */
public class ClickSobreContenedorTipoDeCentro implements View.OnClickListener {

    private Context context;
    private TextView referencedView;
    private RelativeLayout referencedContainer;

    public static ClickSobreContenedorTipoDeCentro obtenerManejador(Context context, TextView referencedView, RelativeLayout referencedContainer){
        if(!(context instanceof AppCompatActivity))
            throw new ClassCastException("Sólo podemos trabajar con AppCompatActivity");
        return new ClickSobreContenedorTipoDeCentro(context, referencedView, referencedContainer);
    }

    private ClickSobreContenedorTipoDeCentro(Context context, TextView referencedView, RelativeLayout referencedContainer){
        this.context = context;
        this.referencedView = referencedView;
        this.referencedContainer = referencedContainer;
    }

    @Override
    public void onClick(View view){
        iniciaListaDeTiposDeCentro();
    }

    private void iniciaListaDeTiposDeCentro(){
        Votaciones db = new Votaciones(context);
        Categoria[] categorias = db.obtenerCategorias();
        String[] nombresCategorias = new String[categorias.length];
        for(int i=0; i<categorias.length; i++)
            nombresCategorias[i] = categorias[i].getNombre();
        DialogoDeLista ddl = new DialogoDeLista();
        ddl.setElementos(nombresCategorias);
        ddl.setTitulo("Seleccione categoría");
        ddl.setAccion(addl);
        ddl.show(((AppCompatActivity)context).getSupportFragmentManager(), "Seleccionar categoría");
    }

    private DialogoDeLista.AccionDialogoDeLista addl = new DialogoDeLista.AccionDialogoDeLista() {
        @Override
        public void objetoSeleccionado(String texto) {
            referencedView.setText(texto);
            ((TextView)referencedContainer.findViewById(R.id.ubicacion_nombre_lugar_votacion)).setText("");
            referencedContainer.setVisibility(View.VISIBLE);
            ProveedorDeRecursos.guardarRecursoString(context, "categoria", texto);
        }
    };
}
