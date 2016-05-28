package org.inspira.jcapiz.polivoto.actividades;

import android.app.Activity;
import android.os.Bundle;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.inspira.jcapiz.polivoto.R;
import resumenresultados.Adaptadores.ListaDeQuienesHanParticipado;

public class DetallesDeQuienesHanParticipado extends Activity {

	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.detalles_de_quienes_han_participado);
		try{
			Bundle extras = getIntent().getExtras();
			String[] rows = extras.getStringArray("rows");
			((TextView)findViewById(R.id.header)).setText(extras.getString("header"));
			ListView list = (ListView)findViewById(R.id.usrs_list);
			ListaDeQuienesHanParticipado adapter = new ListaDeQuienesHanParticipado(rows, this);
			list.setAdapter(adapter);
		}catch(Exception e){
			Toast.makeText(this, "Error: " + e.toString(), Toast.LENGTH_SHORT).show();
			finish();
		}
	}
}