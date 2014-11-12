/**
KK * @author Alex Tauber
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
import java.io.IOException;
import java.util.Arrays;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.content.Context;
//import android.bluetooth.BluetoothAdapter;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
//import android.os.ConditionVariable;
import android.preference.PreferenceManager;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
//import android.widget.Toast;

public class MainFragment extends Fragment {
	private Activity mContext; // = getActivity();
	private SharedPreferences sharedPrefs;
	private SharedPreferences.Editor sharedPrefEditor;
	private SharedPreferences appPrefs;
	private SharedPreferences.Editor appPrefEditor;
	private String[] parms;
	private static boolean mStop = false;
	private boolean mRunningMainLoop = false;
	private StringBuilder mText = new StringBuilder();
	private static final int TEXT_MAX_SIZE = 5120;

	//layout inflater values
	private View mV;
	private TextView txtGGA;
	private TextView txtGLL;
	private TextView txtGSA;
	private TextView txtGSV;
	private TextView txtRMC;
	private TextView txtVTG;
	private TextView txtZDA;

	private TextView txNMEAinp;
	private TextView txtRS;
	private Button btnConnect;
	private Button btnPause;
	private Button btnSvNMEA;
	private Button btnNMEAdflt;
	private Button btnCold;
	private Button btnWarm;
	private Button btnHot;
	private Button btnReset;
	private ScrollView mSvText;
	private TextView mTvSerial;
	private TextView mEPOinfo;
	private Spinner GGA;
	private Spinner GLL;
	private Spinner GSA;
	private Spinner GSV;
	private Spinner RMC;
	private Spinner VTG;
	private Spinner ZDA;
	private int valx;
	private int resetCmd;
	private ProgressDialog pDialog;
	
	ArrayAdapter<CharSequence> arrayadapter;

	GPSrxtx gpsdev;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		MTKutility.debugWrite(132, "MainFragment - onCreateView()");
		
		// Inflate the layout for this fragment
		mV =  inflater.inflate(R.layout.mainfragment, container, false);
		txNMEAinp = (TextView) mV.findViewById(R.id.txNMEAinp);
		txtGGA = (TextView) mV.findViewById(R.id.txtGGA);
		txtGLL = (TextView) mV.findViewById(R.id.txtGLL);
		txtGSA = (TextView) mV.findViewById(R.id.txtGSA);
		txtGSV = (TextView) mV.findViewById(R.id.txtGSV);
		txtRMC = (TextView) mV.findViewById(R.id.txtRMC);
		txtVTG = (TextView) mV.findViewById(R.id.txtVTG);
		txtZDA = (TextView) mV.findViewById(R.id.txtZDA);
		txtRS = (TextView) mV.findViewById(R.id.txtRS);


		mSvText = (ScrollView) mV.findViewById(R.id.msv_Text);
		mTvSerial = (TextView) mV.findViewById(R.id.mtv_Serial);
		mEPOinfo  = (TextView) mV.findViewById(R.id.AGPStitle);
		GGA = (Spinner) mV.findViewById(R.id.GGA);
		GLL = (Spinner) mV.findViewById(R.id.GLL);
		GSA = (Spinner) mV.findViewById(R.id.GSA);
		GSV = (Spinner) mV.findViewById(R.id.GSV);
		RMC = (Spinner) mV.findViewById(R.id.RMC);
		VTG = (Spinner) mV.findViewById(R.id.VTG);
		ZDA = (Spinner) mV.findViewById(R.id.ZDA);

		final CharSequence [] listValues = getResources().getStringArray(R.array.listNMEAfixShow);
		arrayadapter = new ArrayAdapter<CharSequence>(mContext,
	            R.layout.spinnerlayout, listValues){
	        @Override
	        public View getView(int position, View convertView, ViewGroup parent) {
	            View v = super.getView(position, convertView, parent);
	            if (v instanceof TextView)
	                ((TextView) v).setTextSize(MTKutility.mainSPNRfont);
	            return v;}
	    };
		
//		final ArrayAdapter arrayadapter = ArrayAdapter.createFromResource(mContext, R.array.listNMEAfixShow, R.layout.spinnerlayout);
		arrayadapter.setDropDownViewResource(R.layout.spinnerdropdown);
		GGA.setAdapter(arrayadapter);
		GLL.setAdapter(arrayadapter);
		GSA.setAdapter(arrayadapter);
		GSV.setAdapter(arrayadapter);
		RMC.setAdapter(arrayadapter);
		VTG.setAdapter(arrayadapter);
		ZDA.setAdapter(arrayadapter);
		sharedPrefs = PreferenceManager.getDefaultSharedPreferences(mContext);
		sharedPrefEditor = sharedPrefs.edit();
		appPrefs = mContext.getSharedPreferences("otherprefs", Context.MODE_PRIVATE);
		appPrefEditor = appPrefs.edit();

		btnConnect = (Button) mV.findViewById(R.id.btn_connect);
		btnConnect.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				MTKutility.debugWrite(132, "MainFragment - button " + btnConnect.getText() + " pressed");
				if (!MTKutility.btDeviceAvailable()){
					return;}
				gpsdev = MTKutility.getBTconnect();
				if (MTKutility.connected){
					if (gpsdev.sock.isConnected()) {
						gpsdev.close();
						mStop = true;
						mTvSerial.setText("");
						mText.setLength(0);
						btnConnect.setText("Connect");
						MTKutility.connected = false;
						MTKutility.hidetabs();
						btnPause.setEnabled(false);
						btnNMEAdflt.setEnabled(false);
						btnSvNMEA.setEnabled(false);
						btnCold.setEnabled(false);
						btnWarm.setEnabled(false);
						btnHot.setEnabled(false);
						btnReset.setEnabled(false);}
				}else{
					MTKutility.connectBT(mContext);
					if (MTKutility.connected){
						btnPause.setEnabled(true);
						btnNMEAdflt.setEnabled(true);
						btnSvNMEA.setEnabled(true);
						btnCold.setEnabled(true);
						btnWarm.setEnabled(true);
						btnHot.setEnabled(true);
						btnReset.setEnabled(true);
						MTKutility.showTabs();
						showMTKinfo();}
				}
			}
		});

		btnPause = (Button) mV.findViewById(R.id.btn_pause);
		btnPause.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				MTKutility.debugWrite(132, "MainFragment - button " + btnPause.getText() + " pressed");
				if (!MTKutility.connected){
					MTKutility.notconnected(mContext);
					return;}				
				if (mStop) {
					mStop = false;
					showMTKinfo();
					btnPause.setText("Pause log");
				} else {
					mStop = true;
					btnPause.setText("Resume log");}
			}
		});

		btnSvNMEA = (Button) mV.findViewById(R.id.btnSvNMEA);
		btnSvNMEA.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				MTKutility.debugWrite(132, "MainFragment - button " + btnSvNMEA.getText() + " pressed");
				if (!MTKutility.connected){
					MTKutility.notconnected(mContext);
					return;}
				String cmd = "PMTK314,"
						+ String.valueOf(GLL.getSelectedItem()) + ","
						+ String.valueOf(RMC.getSelectedItem()) + ","
						+ String.valueOf(VTG.getSelectedItem()) + ","
						+ String.valueOf(GGA.getSelectedItem()) + ","
						+ String.valueOf(GSA.getSelectedItem()) + ","
						+ String.valueOf(GSV.getSelectedItem()) + ","
						+ "0,0,0,0,0,0,0,0,0,0,0,"
						+ String.valueOf(ZDA.getSelectedItem()) + ","
						+ "0";
				try { gpsdev.sendCommand(cmd);}
				catch (IOException e2) {e2.printStackTrace();}
				String NMEAsettings = cmd.substring(7);
				appPrefEditor.putString("NMEAsettings", NMEAsettings);
				appPrefEditor.commit();
			}
		});

		btnNMEAdflt = (Button) mV.findViewById(R.id.btnNMEAdflt);
		btnNMEAdflt.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				MTKutility.debugWrite(132, "MainFragment - button " + btnNMEAdflt.getText() + " pressed");
				if (!MTKutility.connected){
					MTKutility.notconnected(mContext);
					return;}
				String NMEAsettings = ",0,1,0,1,1,1,0,0,0,0,0,0,0,0,0,0,0,0,0";
				String cmd = "PMTK314" + NMEAsettings;
				try { gpsdev.sendCommand(cmd);}
				catch (IOException e2) {e2.printStackTrace();}
				valx = arrayadapter.getPosition("1");
				GGA.setSelection(valx);
				valx = arrayadapter.getPosition("0");
				GLL.setSelection(valx);
				valx = arrayadapter.getPosition("1");
				GSA.setSelection(valx);
				valx = arrayadapter.getPosition("1");
				GSV.setSelection(valx);
				valx = arrayadapter.getPosition("1");
				RMC.setSelection(valx);
				valx = arrayadapter.getPosition("0");
				VTG.setSelection(valx);
				valx = arrayadapter.getPosition("0");
				ZDA.setSelection(valx);
				appPrefEditor.putString("NMEAsettings", NMEAsettings);
				appPrefEditor.commit();
			}
		});

		btnHot = (Button) mV.findViewById(R.id.btnHot);
		btnHot.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				MTKutility.debugWrite(132, "MainFragment - button " + btnHot.getText() + " pressed");
				if (!MTKutility.connected){
					MTKutility.notconnected(mContext);
					return;
				}				
				resetCmd = 101;
				ResetCMD doreset = new ResetCMD();
				doreset.execute();
			}
		});

		btnWarm = (Button) mV.findViewById(R.id.btnWarm);
		btnWarm.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				MTKutility.debugWrite(132, "MainFragment - button " + btnWarm.getText() + " pressed");
				if (!MTKutility.connected){
					MTKutility.notconnected(mContext);
					return;
				}			
				resetCmd = 102;
				ResetCMD doreset = new ResetCMD();
				doreset.execute();
			}
		});

		btnCold = (Button) mV.findViewById(R.id.btnCold);
		btnCold.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				MTKutility.debugWrite(132, "MainFragment - button " + btnCold.getText() + " pressed");
				if (!MTKutility.connected){
					MTKutility.notconnected(mContext);
					return;
				}				
				resetCmd = 103;
				ResetCMD doreset = new ResetCMD();
				doreset.execute();
			}
		});

		btnReset = (Button) mV.findViewById(R.id.btnReset);
		btnReset.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				MTKutility.debugWrite(132, "MainFragment - button " + btnReset.getText() + " pressed");
				if (!MTKutility.connected){
					MTKutility.notconnected(mContext);
					return;
				}
				resetCmd = 104;
				ResetCMD doreset = new ResetCMD();
				doreset.execute();
			}
		});

		mEPOinfo.setTextSize(TypedValue.COMPLEX_UNIT_SP, MTKutility.epoMSGfont);
		txNMEAinp.setTextSize(TypedValue.COMPLEX_UNIT_SP, MTKutility.mainSetMsgfont);
		txtGGA.setTextSize(TypedValue.COMPLEX_UNIT_SP, MTKutility.mainSPNRfont-2);
		txtGLL.setTextSize(TypedValue.COMPLEX_UNIT_SP, MTKutility.mainSPNRfont-2);
		txtGSA.setTextSize(TypedValue.COMPLEX_UNIT_SP, MTKutility.mainSPNRfont-2);
		txtGSV.setTextSize(TypedValue.COMPLEX_UNIT_SP, MTKutility.mainSPNRfont-2);
		txtRMC.setTextSize(TypedValue.COMPLEX_UNIT_SP, MTKutility.mainSPNRfont-2);
		txtVTG.setTextSize(TypedValue.COMPLEX_UNIT_SP, MTKutility.mainSPNRfont-2);
		txtZDA.setTextSize(TypedValue.COMPLEX_UNIT_SP, MTKutility.mainSPNRfont-2);
		btnSvNMEA.setTextSize(TypedValue.COMPLEX_UNIT_SP, MTKutility.mainBtnSVEfont);
		
		btnConnect.setTextSize(TypedValue.COMPLEX_UNIT_SP, MTKutility.mainbtnBARfont);
		btnPause.setTextSize(TypedValue.COMPLEX_UNIT_SP, MTKutility.mainbtnBARfont);
		btnNMEAdflt.setTextSize(TypedValue.COMPLEX_UNIT_SP, MTKutility.mainbtnBARfont);
		btnCold.setTextSize(TypedValue.COMPLEX_UNIT_SP, MTKutility.mainbtnBARfont);
		btnWarm.setTextSize(TypedValue.COMPLEX_UNIT_SP, MTKutility.mainbtnBARfont);
		btnHot.setTextSize(TypedValue.COMPLEX_UNIT_SP, MTKutility.mainbtnBARfont);
		btnReset.setTextSize(TypedValue.COMPLEX_UNIT_SP, MTKutility.mainbtnBARfont);
		txtRS.setTextSize(TypedValue.COMPLEX_UNIT_SP, MTKutility.mainbtnBARfont-2);

		btnPause.setEnabled(false);
		btnNMEAdflt.setEnabled(false);
		btnSvNMEA.setEnabled(false);
		btnCold.setEnabled(false);
		btnWarm.setEnabled(false);
		btnHot.setEnabled(false);
		btnReset.setEnabled(false);
		
		//use width to size frames so that screen is evenly filled
		int sWidth = (MTKutility.screenWidth - 85)/7;
		txtGGA.setMinimumWidth(sWidth);
		txtGLL.setMinimumWidth(sWidth);
		txtGSA.setMinimumWidth(sWidth);
		txtGSV.setMinimumWidth(sWidth);
		txtRMC.setMinimumWidth(sWidth);
		txtVTG.setMinimumWidth(sWidth);
		txtZDA.setMinimumWidth(sWidth);

		return mV;
	}	//onCreateView()

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mContext = getActivity();
		MTKutility.debugWrite(132, "MainFragment - onCreate()");
		
		sharedPrefs = MTKutility.getSharedPreferences();
		sharedPrefEditor = sharedPrefs.edit();
		String GPS_bluetooth_id = sharedPrefs.getString("bluetoothListPref", "-1");
		if ("-1".equals(GPS_bluetooth_id) || GPS_bluetooth_id.length() == 0) {
		    MTKutility.aboutIsActive = true;
			MTKutility.aboutXMLfile = "file:///android_asset/MTKstartup.html";
		    FragmentTransaction ft = getFragmentManager().beginTransaction();
		    ft.replace(android.R.id.content, new aboutFragment());
		    ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
		    ft.addToBackStack(null);
		    ft.commit();
		}
	}

	@Override
	public void onPause() {
		super.onPause();
		MTKutility.debugWrite(132, "MainFragment - onPause()");
		//stop the background thread that reads the MTK logger
		mStop = true;
	}

	@Override
	public void onResume() {
		MTKutility.debugWrite(132, "MainFragment - onResume()");
		super.onResume();
		gpsdev = MTKutility.getBTconnect();
		if (MTKutility.connected){
			showMTKinfo();
		}
	}

	private void showMTKinfo(){
		MTKutility.debugWrite(132, "MainFragment - showMTKinfo()");
		if (gpsdev.sock.isConnected()) {
			btnConnect.setText("Disconnect");
			btnPause.setText("Pause log");	
			btnPause.setEnabled(true);
			btnNMEAdflt.setEnabled(true);
			btnSvNMEA.setEnabled(true);
			btnCold.setEnabled(true);
			btnWarm.setEnabled(true);
			btnHot.setEnabled(true);
			btnReset.setEnabled(true);
			String epoinfo = MTKutility.getEPOinfo();
			mEPOinfo.setText(epoinfo);
			refreshNMEAsettings();
			if (!mRunningMainLoop) {
				mainloop();
			}
		} else {
			btnConnect.setText("Connect");
		}
	}

	private void refreshNMEAsettings(){
		MTKutility.debugWrite(132, "MainFragment - refreshNMEAsettings()");

		String[] prms = MTKutility.mtkCmd("PMTK414", "PMTK514", MTKutility.cmdTimeOut);
		if (prms != null){
			String NMEAsettings = Arrays.toString(prms).replace(" ", "").substring(8,46);
			if (NMEAsettings.matches(",0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0")){
				NMEAsettings = appPrefs.getString("NMEAsettings", "");
				String cmd = "PMTK314" + NMEAsettings;
				MTKutility.debugWrite(132, "*** sending: " + cmd);
				try {gpsdev.sendCommand(cmd);
				} catch (IOException e) {e.printStackTrace();}
			}
			prms = NMEAsettings.split(",");
			valx = arrayadapter.getPosition(prms[1]);
			GLL.setSelection(valx);
			valx = arrayadapter.getPosition(prms[2]);
			RMC.setSelection(valx);
			valx = arrayadapter.getPosition(prms[3]);
			VTG.setSelection(valx);
			valx = arrayadapter.getPosition(prms[4]);
			GGA.setSelection(valx);
			valx = arrayadapter.getPosition(prms[5]);
			GSA.setSelection(valx);
			valx = arrayadapter.getPosition(prms[6]);
			GSV.setSelection(valx);
			valx = arrayadapter.getPosition(prms[18]);
			ZDA.setSelection(valx);}
	}

	private void mainloop() {
		MTKutility.debugWrite(132, "MainFragment - mainloop()");
		mStop = false;
		mRunningMainLoop = true;
		AsyncThread task = new AsyncThread();
		task.execute();
	}

	class AsyncThread extends AsyncTask<Void, String, Void> {
		boolean loop = true;

		@Override
		protected Void doInBackground(Void... params) {
			MTKutility.debugWrite(132, "MainFragment - doInBackground()");
			String reply = null;
			boolean ok = true;

			while (loop) {
				try { reply = gpsdev.readString(10.0);}
				catch (IOException e2) {ok = false;
				e2.printStackTrace();}
				catch (InterruptedException e2) {ok = false;
				e2.printStackTrace();}

				if (ok) {publishProgress(reply);}

				if (mStop) {
					mRunningMainLoop = false;
					break;}
			}
			return null;
		}

		@Override
		protected void onProgressUpdate(String... values) {
//			MTKutility.debugWrite(132, "MainFragment - onProgressUpdate()");
			if (mTvSerial.length() > TEXT_MAX_SIZE) {
				StringBuilder sb = new StringBuilder();
				sb.append(mTvSerial.getText());
				sb.delete(0, TEXT_MAX_SIZE / 2);
				mTvSerial.setText(sb);}
			mTvSerial.append(values[0]);
			mText.setLength(0);
			mSvText.fullScroll(View.FOCUS_DOWN);
		}
	};
	
	private class ResetCMD extends AsyncTask<Void, Void, Integer> {

		@Override
		protected Integer doInBackground(Void... params) {
			MTKutility.debugWrite(132, "MainFragment.resetCmd - doInBackground()");
			switch(resetCmd){
			case 101:
				MTKutility.mtkCmd("PMTK101", "PMTK010,001", MTKutility.cmdTimeOut*2);
				return resetCmd;
			case 102:
				MTKutility.mtkCmd("PMTK102", "PMTK010,001", MTKutility.cmdTimeOut*2);
				return resetCmd;
			case 103:
				MTKutility.mtkCmd("PMTK103", "PMTK010,001", MTKutility.cmdTimeOut*2);
				return resetCmd;
			case 104:
				MTKutility.mtkCmd("PMTK104", "PMTK010,001", MTKutility.cmdTimeOut*2);
				return resetCmd;
			}
			return resetCmd;
		}

		@Override
		protected void onPostExecute(Integer result) {
			MTKutility.debugWrite(132, "MainFragment.resetCmd - onPostExecute()");
			pDialog.dismiss();
			mStop = false;
			showMTKinfo();
		}

		@Override
		protected void onPreExecute() {
			MTKutility.debugWrite(132, "MainFragment.resetCmd - onPreExecute()");
			mStop = true;
			pDialog = new ProgressDialog(mContext);
			pDialog.setTitle("Logger reset");
			switch(resetCmd){
			case 101:
				pDialog.setMessage("Hot restart");
				break;
			case 102:
				pDialog.setMessage("Warm restart");
				break;
			case 103:
				pDialog.setMessage("Cold restart");
				break;
			case 104:
				pDialog.setMessage("Factory reset");
//				break;
			}
			pDialog.setCancelable(true);
			pDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
			pDialog.setIndeterminate(true);
			pDialog.show();
		}
	}
}