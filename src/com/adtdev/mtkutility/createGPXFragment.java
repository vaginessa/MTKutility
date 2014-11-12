/**
 * @author Al Tauber
 * rework of Bastiaan Naber's AndroidMTK DownloadBinRunnable file to implement
 * it in a fragment with AsyncTask (GNU GPL v3 license)
 * https://code.google.com/p/androidmtk/
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
import java.util.Locale;
import android.app.Activity;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

public class createGPXFragment extends Fragment {
	private View mV;

	// screen objects
	private CheckBox   mcb_OneTrk;
	private TextView   btvBINfile;
	private TextView   btvGPXpath;
	private Button     btnMkGPX;
	private ScrollView msv_Text;
	private TextView   mtv_Serial;

	//sharedPref
	private SharedPreferences sharedPref;
	private SharedPreferences.Editor prefEditor;

	private StringBuilder mText = new StringBuilder();
	private static final int ScrollTextSize = 5120;

	//binary to hex conversion values
	final protected static char[] hexArray = "0123456789ABCDEF".toCharArray();

	GPSrxtx gpsdev;
	static ProgressDialog dialog;
	
	String BINfile = "";
	String BINpath = "";
	String GPXpath = "";
	String GPXname = "";
	private final int requestBINfile = 1;
	private final int requestGPXpath = 2;
	
	// Keys
	public static final String KEY_TOAST = "toast";
	public static final String MESSAGEFIELD = "textSwitcher";
	public static final String KEY_PROGRESS = "progressCompleted";
	public static final String CLOSE_PROGRESS = "closeProgressDialog";
	public static final String CREATEGPX = "parseBinFile";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		sharedPref = MTKutility.getSharedPreferences();
		prefEditor = sharedPref.edit();
	}	//onCreate()
	  
	  @Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		MTKutility.debugWrite(132, "createGPXFragment - onCreateView()");

		mV =  inflater.inflate(R.layout.creategpxfragment, container, false);

		btvBINfile  = (TextView) mV.findViewById(R.id.btvBINfile);
		btvBINfile.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				MTKutility.debugWrite(132, "createGPXFragment - button " + btvBINfile.getText() + " pressed");
				getBINfilename();
			}
		});

		btvGPXpath  = (TextView) mV.findViewById(R.id.btvGPXpath);
		btvGPXpath.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				MTKutility.debugWrite(132, "createGPXFragment - button " + btvGPXpath.getText() + " pressed");
				getGPXpathPref();
			}
		});

		btnMkGPX = (Button) mV.findViewById(R.id.btnMkGPX);
		btnMkGPX.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				MTKutility.debugWrite(132, "createGPXFragment - button " + btnMkGPX.getText() + " pressed");
				convertToGPX();
			}
		});

		mcb_OneTrk  = (CheckBox)   mV.findViewById(R.id.mcb1OneTrk);
		mtv_Serial  = (TextView)   mV.findViewById(R.id.mtv_Serial);
		msv_Text    = (ScrollView) mV.findViewById(R.id.msv_Text);

		mcb_OneTrk.setTextSize(TypedValue.COMPLEX_UNIT_SP, MTKutility.dnldTexts);		
		btnMkGPX.setTextSize(TypedValue.COMPLEX_UNIT_SP, MTKutility.dnldButtons);
		btvBINfile.setTextSize(TypedValue.COMPLEX_UNIT_SP, MTKutility.dnldButtons-4);
		btvGPXpath.setTextSize(TypedValue.COMPLEX_UNIT_SP, MTKutility.dnldButtons-4);

		return mV;
	}	//onCreateView()

	@Override
	public void  onPause() {
		super.onPause();
		MTKutility.debugWrite(132, "createGPXFragment - onPause()");
	}	//onPause()

	@Override
	public void onResume() {
		super.onResume();
		MTKutility.debugWrite(132, "createGPXFragment - onResume()");

		if (mtv_Serial.length() > 0) {
			StringBuilder sb = new StringBuilder();
			sb.append(mtv_Serial.getText());
			sb.delete(0, mtv_Serial.length());
			mtv_Serial.setText(sb);
		}
		if (!GPXname.isEmpty()){
			btvBINfile.setText(BINfile);
		}
		BINpath = sharedPref.getString("BINpath", Environment.getExternalStorageDirectory().toString());
		GPXpath = sharedPref.getString("GPXpath", Environment.getExternalStorageDirectory().toString());
		btvGPXpath.setText(GPXpath);
	}	//onResume()

	private void getBINfilename() {
		MTKutility.debugWrite(132, "createGPXFragment - getBINfilename()");
		Intent fileExploreIntent = new Intent(getActivity(), FilePickerActivity.class);
		fileExploreIntent.putExtra(FilePickerActivity.FullPath, BINpath);
		fileExploreIntent.putExtra(FilePickerActivity.LimitParent, false);
		fileExploreIntent.putExtra(FilePickerActivity.FileName, "");
		fileExploreIntent.putExtra(FilePickerActivity.ShowHidden, false);
		fileExploreIntent.putExtra(FilePickerActivity.FoldersOnly, false);

		startActivityForResult(fileExploreIntent, requestBINfile);
	}	//getBINfilename()

	private void getGPXpathPref() {
		MTKutility.debugWrite(132, "createGPXFragment - getGPXpathPref()");
		Intent fileExploreIntent = new Intent(getActivity(), FilePickerActivity.class);
		fileExploreIntent.putExtra(FilePickerActivity.FullPath, MTKutility.rootDirectory);
		fileExploreIntent.putExtra(FilePickerActivity.FileName, "");
		fileExploreIntent.putExtra(FilePickerActivity.ShowHidden, false);
		fileExploreIntent.putExtra(FilePickerActivity.FoldersOnly, true);

		startActivityForResult(fileExploreIntent, requestGPXpath);
	}	//getGPXpathPref()

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		MTKutility.debugWrite(132, "MTKutility - onActivityResult("
				+ Integer.toString(requestCode)
				+ "," + Integer.toString(resultCode)
				+ "," + data +")");
		
		if (requestCode == requestBINfile) {
			if (resultCode == Activity.RESULT_OK) {
				GPXname = data.getStringExtra(FilePickerActivity.FileName);
				if (!GPXname.isEmpty()){
					BINfile  = data.getStringExtra(FilePickerActivity.FullPath);
					Toast.makeText(getActivity(), "binary source is:\n" + BINfile, Toast.LENGTH_LONG).show();
					GPXname = GPXname.toLowerCase(Locale.CANADA);
					GPXname = GPXname.replace(".bin", ".gpx");
				} else {
					Toast.makeText(getActivity(), "no file selected", Toast.LENGTH_LONG).show();
				}
			} else {
				Toast.makeText(getActivity(), "no Changes Made", Toast.LENGTH_LONG).show();
			}
		}

		if (requestCode == requestGPXpath) {
			if (resultCode == Activity.RESULT_OK) {
				GPXpath = data.getStringExtra(FilePickerActivity.FullPath);
				if (GPXpath != null){
					Toast.makeText(getActivity(), "New Export Path:\n" + GPXpath, Toast.LENGTH_LONG).show();
					prefEditor.putString("GPXpath", GPXpath);
					prefEditor.commit();
				} else {
					Toast.makeText(getActivity(), "No Changes Made", Toast.LENGTH_LONG).show();
				}
			} else {
				Toast.makeText(getActivity(), "No Changes Made", Toast.LENGTH_LONG).show();
			}
		}

		super.onActivityResult(requestCode, resultCode, data);
	}	//onActivityResult()

	// Define a Handler 
	final Handler parseHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			if (msg.getData().containsKey(MESSAGEFIELD)) {
				writeToMainTextArea(msg.getData().getString(MESSAGEFIELD));}
			if (msg.getData().containsKey(CLOSE_PROGRESS)) {
				if (msg.getData().getInt(CLOSE_PROGRESS) == 1){
					dialog.dismiss();}}
			if (msg.getData().containsKey(KEY_PROGRESS)) {
				dialog.show();
				dialog.setProgress(msg.getData().getInt(KEY_PROGRESS));}
			if (msg.getData().containsKey(KEY_TOAST)) {
				String message = msg.getData().getString(KEY_TOAST);
				Toast.makeText(getActivity(), message, Toast.LENGTH_LONG).show();}
		}
	};	//parseHandler

	public void writeToMainTextArea(String text) {
		MTKutility.debugWrite(132, "createGPXFragment - writeToMainTextArea("+text+")");
		if (mtv_Serial.length() > ScrollTextSize) {
			StringBuilder sb = new StringBuilder();
			sb.append(mtv_Serial.getText());
			sb.delete(0, ScrollTextSize / 2);
			mtv_Serial.setText(sb);}

		mtv_Serial.append(text + "\n");
		mText.setLength(0);
		msv_Text.fullScroll(View.FOCUS_DOWN);
	}	//writeToMainTextArea()

	private void convertToGPX() {
		MTKutility.debugWrite(132, "createGPXFragment - createGPX()");
		boolean oneTrk = false;
		dialog = new ProgressDialog(getActivity());
		dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		dialog.setMessage("Converting GPS log to GPX file");
		dialog.setCancelable(true);
		dialog.setMax(100);
		dialog.show();

		// Start a new thread for it!
		if (mcb_OneTrk.isChecked()){
			oneTrk = true;
			GPXname = GPXname.replace(".gpx", "OT.gpx");
		}
		ParseBinFile parseBinFile = new ParseBinFile(BINfile, GPXname, parseHandler, oneTrk);
		Thread gpxThread = new Thread(parseBinFile);
		gpxThread.start();
	}	//convertToGPX()
}