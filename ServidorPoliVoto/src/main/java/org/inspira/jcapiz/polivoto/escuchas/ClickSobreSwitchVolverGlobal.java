package org.inspira.jcapiz.polivoto.escuchas;

import android.content.Context;
import android.graphics.Color;
import android.widget.CompoundButton;

import org.inspira.jcapiz.polivoto.R;
import org.inspira.jcapiz.polivoto.pojo.Votacion;

/**
 * Created by jcapiz on 8/05/16.
 */
public class ClickSobreSwitchVolverGlobal implements CompoundButton.OnCheckedChangeListener {

    private Context context;
    private Votacion votacion;

    public ClickSobreSwitchVolverGlobal(Context context, Votacion votacion) {
        this.context = context;
        this.votacion = votacion;
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        votacion.setGlobal(isChecked);
        if(isChecked){
            buttonView.setTextColor(context.getResources().getColor(R.color.colorPrimary));
        }else{
            buttonView.setTextColor(Color.GRAY);
        }
    }
}
