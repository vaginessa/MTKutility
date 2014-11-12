/**
 * @author Alex Tauber
 * 
 * This file is part of the Android app MTKutility.
 * 
 * MTKutility is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, version 3 of the License. This extends to files
 * included that were authored by others and modified to make them suitable for
 * MTKutility. All files included were subject to open source licensing.
 * 
 * MTKutility is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You can review a copy of the GNU General Public License
 * at http://www.gnu.org/licenses.
 *
 */
package com.adtdev.mtkutility;

import com.adtdev.mtkutility.R;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
//import android.widget.Toast;

public class aboutFragment extends Fragment{
	//layout inflater values
	private View mV;
	private WebView wv;


	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		MTKutility.debugWrite(132, "aboutFragment - onCreate()");
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		MTKutility.debugWrite(132, "aboutFragment - onCreateView()");
		// Inflate the layout for this fragment
		mV =  inflater.inflate(R.layout.aboutfragment, container, false);
		wv = (WebView) mV.findViewById(R.id.webview);  
		wv.loadUrl(MTKutility.aboutXMLfile);

		return mV;
	}

	@Override
	public void onPause() {
		super.onPause();
//		Toast.makeText(getActivity(), "onPause", Toast.LENGTH_SHORT).show();
	}

	@Override
	public void onResume() {
		super.onResume();
//		Toast.makeText(getActivity(), "onResume", Toast.LENGTH_SHORT).show();
	}

}
