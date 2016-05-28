package resumenresultados.Adaptadores;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import org.inspira.jcapiz.mylibrary.R;

public class ListaDeQuienesHanParticipado extends BaseAdapter{

	String[] array;
	Activity activity;
	LayoutInflater lInflater;
	
	public ListaDeQuienesHanParticipado(String[] array, Activity activity){
		this.array = array;
		this.activity = activity;
		lInflater = (LayoutInflater)activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}
	
	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return array.length;
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		View view;
		TextView text;
		if( convertView == null ){
			view = lInflater.inflate(R.layout.elemento_de_detalles_de_quienes_han_participado,parent,false);
		}else{
			view = convertView;
		}
		text = (TextView)view.findViewById(R.id.list_element);
		text.setText(array[position]);
		return view;
	}

}
