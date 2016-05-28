package org.inspira.jcapiz.polivoto.actividades;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.widget.TextView;

import org.inspira.jcapiz.polivoto.R;
import org.inspira.jcapiz.polivoto.database.Votaciones;

public class CargadorDeMatricula extends Activity{

	private class DatasetHandler extends Thread{
		
		private Activity activity;
		private String[] titulos;

		public void setTitulos(String[] titulos) {
			this.titulos = titulos;
		}

		public DatasetHandler(Activity activity){
			this.activity = activity;
		}
		
		@Override
		public void run(){
			Votaciones bd = new Votaciones(activity);
			bd.insertaRegistro(getSharedPreferences(Bienvenida.class.getName(), Context.MODE_PRIVATE)
					.getString("ubicacion","NaN"),titulos);
			setResult(RESULT_OK);
			finish();
		}
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.cargador_de_matricula);
		final TextView label = (TextView)findViewById(R.id.label);
		if( savedInstanceState == null ){
			Bundle extras = getIntent().getExtras();
			label.setText(extras.getString("label"));
			String[] titulos = extras.getStringArray("titulos");
			DatasetHandler dataHandler = new DatasetHandler(this);
			dataHandler.setTitulos(titulos);
			dataHandler.start();
		}
	}
}