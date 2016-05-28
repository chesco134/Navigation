package org.inspira.jcapiz.polivoto.actividades;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

import org.inspira.jcapiz.polivoto.R;

public class ActividadDeEspera extends Activity{

	@Override
	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.esperando_participante);
		if(savedInstanceState == null)
		try{
			((TextView)findViewById(R.id.message)).setText(getIntent().getExtras().getString("message"));
		}catch(NullPointerException e){
			e.printStackTrace();
		}
	}
	
	@Override
	protected void onSaveInstanceState(Bundle outState){
		super.onSaveInstanceState(outState);
		outState.putString("message",((TextView)findViewById(R.id.message)).getText().toString());
	}
	
	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState){
		super.onRestoreInstanceState(savedInstanceState);
		((TextView)findViewById(R.id.message)).setText(savedInstanceState.getString("message"));
	}
}