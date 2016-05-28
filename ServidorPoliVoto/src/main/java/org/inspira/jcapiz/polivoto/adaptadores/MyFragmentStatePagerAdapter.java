package org.inspira.jcapiz.polivoto.adaptadores;

import java.util.LinkedList;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

public class MyFragmentStatePagerAdapter extends FragmentStatePagerAdapter{

	public MyFragmentStatePagerAdapter(FragmentManager fm, LinkedList<Fragment> fragments) {
		super(fm);
		this.fragments = fragments;
	}

	LinkedList<Fragment> fragments;

	public void add(Fragment fragment){
		fragments.add(fragment);
		super.notifyDataSetChanged();
	}

    public Fragment remove(int posicion){
        Fragment f = fragments.remove(posicion);
        super.notifyDataSetChanged();
        return f;
    }

    public boolean remove(Fragment fragment){
        boolean f = fragments.remove(fragment);
        if( f ) super.notifyDataSetChanged();
        return f;
    }

	@Override
	public Fragment getItem(int arg0) {
			return fragments.get(arg0);
	}

	@Override
	public int getCount() {
			return fragments.size();
	}

	@Override
	public CharSequence getPageTitle(int position) {
			return "" + (position + 1);
	}
}
