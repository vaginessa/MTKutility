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

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;

import com.adtdev.mtkutility.R;

import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
//import android.net.ConnectivityManager;
//import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

public class EPOFragment extends Fragment {
	private View mV;

	// screen objects
	private TextView mEPOinfo;
	private EditText mFTP_IP;
	private EditText mFTP_port;
	private EditText mFTP_userid;
	private EditText mFTP_pswd;
	private EditText mFTP_file;
	private Button btnSave;
	private Button btnUpdt;
	private Button btnReset;
	private ScrollView mSvText;
	private TextView mTvSerial;

	private String sFTP_IP;
	private String sFTP_port;
	private String sFTP_userid;
	private String sFTP_pswd;
	private String sFTP_file;

	private Long tsLong;
	private String ts;
	
	SharedPreferences sharedPref;
	SharedPreferences.Editor prefEditor;
	private final static String BR = System.getProperty("line.separator");
	private static final int TEXT_MAX_SIZE = 5120;
	private static final int SatDataLen = 60;
	private static final int EPOblocksize = SatDataLen * 32;

	// Progress Dialog
	private ProgressDialog pDialog;

	//binary to hex conversion values
	final protected static char[] hexArray = "0123456789ABCDEF".toCharArray();
	private static final byte crByte = (byte) 0x0D;
	private static final byte lfByte = (byte) 0x0A;

	// Message types for publishProgress
	private static final int pCancel  = 0;
	private static final int pToast   = 1;
	private static final int pText    = 2;
	private static final int pProgBar = 3;

	private boolean abort;
	private String stringBuf;
	private String msg;
	private String[] parms = null;

	final byte[] binPMTK253 = new byte[] {
			(byte)0x04, 0x24, 0x0E, 0x00, (byte)0xFD, 0x00, 0x00, 0x00,
			(byte)0xC2, 0x01, 0x00, 0x30, 0x0D, 0x0A};

	GPSrxtx gpsdev;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		MTKutility.debugWrite(132, "EPOFragment - onCreateView()");
		gpsdev = MTKutility.getBTconnect();
		sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());
		prefEditor = sharedPref.edit();

		// Inflate the layout for this fragment
		mV =  inflater.inflate(R.layout.epofragment, container, false);

		btnSave = (Button) mV.findViewById(R.id.btn_save);
		btnSave.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				MTKutility.debugWrite(132, "EPOFragment - button " + btnSave.getText() + " pressed");
				savePreferences();
			}
		});

		btnUpdt = (Button) mV.findViewById(R.id.btn_Updt);
		btnUpdt.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				MTKutility.debugWrite(132, "EPOFragment - button " + btnUpdt.getText() + " pressed");
				refreshAGPS();
			}
		});

		btnReset = (Button) mV.findViewById(R.id.btn_Reset);
		btnReset.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				MTKutility.debugWrite(132, "EPOFragment - button " + btnReset.getText() + " pressed");
				resetAGPS();
			}
		});

		mEPOinfo    = (TextView) mV.findViewById(R.id.AGPStitle);
		mFTP_IP     = (EditText) mV.findViewById(R.id.mFTP_IP);
		mFTP_port   = (EditText) mV.findViewById(R.id.mFTP_port);
		mFTP_userid = (EditText) mV.findViewById(R.id.mFTP_userid);
		mFTP_pswd   = (EditText) mV.findViewById(R.id.mFTP_pswd);
		mFTP_file   = (EditText) mV.findViewById(R.id.mFTP_file);
		mTvSerial   = (TextView) mV.findViewById(R.id.mtv_Serial);
		mSvText     = (ScrollView) mV.findViewById(R.id.msv_Text);

		mEPOinfo.setTextSize(TypedValue.COMPLEX_UNIT_SP, MTKutility.epoMSGfont);
		mFTP_IP.setTextSize(TypedValue.COMPLEX_UNIT_SP, MTKutility.agpsTexts);
		mFTP_port.setTextSize(TypedValue.COMPLEX_UNIT_SP, MTKutility.agpsTexts);
		mFTP_userid.setTextSize(TypedValue.COMPLEX_UNIT_SP, MTKutility.agpsTexts);
		mFTP_pswd.setTextSize(TypedValue.COMPLEX_UNIT_SP, MTKutility.agpsTexts);
		mFTP_file.setTextSize(TypedValue.COMPLEX_UNIT_SP, MTKutility.agpsTexts);
		btnSave.setTextSize(TypedValue.COMPLEX_UNIT_SP, MTKutility.agpsButtons);
		btnUpdt.setTextSize(TypedValue.COMPLEX_UNIT_SP, MTKutility.agpsButtons);
		btnReset.setTextSize(TypedValue.COMPLEX_UNIT_SP, MTKutility.agpsButtons);

		return mV;
	}

	@Override
	public void onPause() {
		super.onPause();
		MTKutility.debugWrite(132, "EPOFragment - onPause()");
		MTKutility.NMEAoutStart();
	}

	@Override
	public void onResume() {
		super.onResume();
		MTKutility.debugWrite(132, "EPOFragment - onResume()");
		gpsdev = MTKutility.getBTconnect();
		//check for BlueTooth connection
		if (!gpsdev.sock.isConnected()) {
			//re-establish the MTK logger connection
			if (!gpsdev.connect()){
				msg = "BlueTooth connection is missing";
				Toast.makeText(getActivity(), msg, Toast.LENGTH_LONG).show();
				MTKutility.debugWrite(132, "**** "+msg);
				return;}}

		//get AGPS update sharedPref
		SharedPreferences prefs = MTKutility.getSharedPreferences();
		mFTP_IP.setText(prefs.getString("epo_ip", ""));
		mFTP_port.setText(prefs.getString("epo_port", ""));
		mFTP_userid.setText(prefs.getString("epo_username", ""));
		mFTP_pswd.setText(prefs.getString("epo_pswd", ""));
		mFTP_file.setText(prefs.getString("epo_file", ""));

		String epoInfo = MTKutility.getEPOinfo();
		mEPOinfo.setText(epoInfo);		
	}

	private void savePreferences() {
		MTKutility.debugWrite(132, "EPOFragment - savePreferences()");
		stringBuf = mFTP_IP.getText().toString();
		if (stringBuf != MTKutility.getSharedPreferences().getString("epo_ip", "")){
			prefEditor.putString("epo_ip", stringBuf);
			prefEditor.commit();}

		stringBuf = mFTP_port.getText().toString();
		if (stringBuf != MTKutility.getSharedPreferences().getString("epo_port", "")){
			prefEditor.putString("epo_port", stringBuf);
			prefEditor.commit();}

		stringBuf = mFTP_userid.getText().toString();
		if (stringBuf != MTKutility.getSharedPreferences().getString("epo_username", "")){
			prefEditor.putString("epo_username", stringBuf);
			prefEditor.commit();}

		stringBuf = mFTP_pswd.getText().toString();
		if (stringBuf != MTKutility.getSharedPreferences().getString("epo_pswd", "")){
			prefEditor.putString("epo_pswd", stringBuf);
			prefEditor.commit();}

		stringBuf = mFTP_file.getText().toString();
		if (stringBuf != MTKutility.getSharedPreferences().getString("epo_file", "")){
			prefEditor.putString("epo_file", stringBuf);
			prefEditor.commit();}
	}

	private void refreshAGPS() {
		MTKutility.debugWrite(132, "EPOFragment - refreshAGPS()");
		sFTP_IP = mFTP_IP.getText().toString();
		sFTP_port = mFTP_port.getText().toString();
		sFTP_userid = mFTP_userid.getText().toString();
		sFTP_pswd = mFTP_pswd.getText().toString();
		sFTP_file = mFTP_file.getText().toString();

		AGPSupdate task = new AGPSupdate(getActivity());
		MTKutility.debugWrite(132, "EPOFragment - starting AGPSupdate");
		task.execute();
	}

	private void resetAGPS() {
		MTKutility.debugWrite(132, "EPOFragment - resetAGPS()");
		//delete the MTK logger EPO data
		parms = MTKutility.mtkCmd("PMTK127", "PMTK001,127", MTKutility.cmdTimeOut);
		if (parms != null){
			int result = Integer.valueOf(parms[2]);
			if (result == 3){
//				mEPOinfo.setText("0 blocks AGPS data");
				MTKutility.renewAGPS = true;
				String epoinfo = MTKutility.getEPOinfo();
				mEPOinfo.setText(epoinfo);
			}
		}
	}


	class AGPSupdate extends AsyncTask<Void, String, Void> {
		private Context mContext;
		ProgressDialog mProgress;
		boolean loop = true;
		private byte[] EPOdata;
		private StringBuilder mText;

		public AGPSupdate(Context context){
			this.mContext = context;
		}


		@Override
		protected void onPreExecute(){
			MTKutility.debugWrite(132, "EPOFragment - AGPSupdate.onPreExecute()");
			btnUpdt.setClickable(false);
			btnReset.setClickable(false);
		}

		@Override
		protected Void doInBackground(Void... params) {
			MTKutility.debugWrite(132, "EPOFragment - AGPSupdate.doInBackground()");
			abort = false;

			//String values expected: Action, Style, message, percent
			publishProgress(String.valueOf(pProgBar), 
					String.valueOf(ProgressDialog.STYLE_SPINNER),
					"Downloading \nPlease wait...", null);
			MTKutility.debugWrite(132, "+++ downloading EPO file");
			EPOdata = ftpDownload();
			publishProgress(String.valueOf(pCancel));

			if (EPOdata != null){
				MTKutility.debugWrite(132, "+++ downloaded:"+Integer.toString(EPOdata.length));
				MTKutility.NMEAoutStop();
				publishProgress(String.valueOf(pProgBar), 
						String.valueOf(ProgressDialog.STYLE_HORIZONTAL),
						"Refreshing AGPS data", null);
				MTKutility.debugWrite(132, "+++ refreshing AGPS data");
				try {updateAGPSdata(EPOdata);
				} catch (IOException e) {abort = true;
				e.printStackTrace();}
				publishProgress(String.valueOf(pCancel));
				//				MTKutility.NMEAoutStart();
				if (abort){
					MTKutility.debugWrite(132, "+++ AGPS update failed");
					publishProgress(String.valueOf(pToast), "AGPS update failed");
				} else {
					publishProgress(String.valueOf(pToast), "EPO upload completed");
					publishProgress(String.valueOf(pProgBar), 
							String.valueOf(ProgressDialog.STYLE_SPINNER),
							"Waiting for MTK logger completion response", null);
					MTKutility.debugWrite(132, "+++ EPO upload completed");
					updateEPOinfo();
					publishProgress(String.valueOf(pToast), "AGPS update completed");
					publishProgress(String.valueOf(pCancel));}}
			return null;
		}

		private void updateEPOinfo() {
			MTKutility.debugWrite(132, "EPOFragment - AGPSupdate.updateEPOinfo()");
			int repeat = 100;
			//refresh EPO TextView
			do {
				repeat--;
				MTKutility.renewAGPS = true;
				MTKutility.goSleep(300);
				MTKutility.getEPOinfo();
			} while ((MTKutility.EPOblks == 0) && (repeat != 0));
		}

		@Override
		protected void onPostExecute(Void result) {
			MTKutility.debugWrite(132, "EPOFragment - AGPSupdate.onPostExecute()");
			MTKutility.NMEAoutStart();
			String epoInfo = MTKutility.getEPOinfo();
			mEPOinfo.setText(epoInfo);
			btnUpdt.setClickable(true);
			btnReset.setClickable(true);
		}

		@Override
		//String values expected:  Action, Style, message, percent						
		protected void onProgressUpdate(String... values) {
			int Action = Integer.parseInt(values[0]);
			switch (Action) {
			case pProgBar:
				if (values[1]==null){
					try {pDialog.setProgress(Integer.parseInt(values[3]));
					} catch (NumberFormatException e) {e.printStackTrace();}
				}else {
					pDialog = new ProgressDialog(mContext);
					pDialog.setTitle("AGPS data refresh");
					pDialog.setMessage(values[2]);
					pDialog.setCancelable(true);
					if (Integer.parseInt(values[1]) == ProgressDialog.STYLE_HORIZONTAL){
						pDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
						pDialog.setIndeterminate(false);
						pDialog.setMax(100);
					}else {
						pDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
						pDialog.setIndeterminate(true);}
					pDialog.show();
				}
				break;
			case pToast:
				Toast.makeText(mContext, values[1], Toast.LENGTH_SHORT).show();
				break;
			case pText:
				if (mTvSerial.length() > TEXT_MAX_SIZE) {
					StringBuilder sb = new StringBuilder();
					sb.append(mTvSerial.getText());
					sb.delete(0, TEXT_MAX_SIZE / 2);
					mTvSerial.setText(sb);}
				mTvSerial.append(values[1]);
				mText.setLength(0);
				mSvText.fullScroll(View.FOCUS_DOWN);
				break;
			case pCancel:
				pDialog.dismiss();		
				//clear the MTK log
				if (mTvSerial.length() > 0) {
					StringBuilder sb = new StringBuilder();
					sb.append(mTvSerial.getText());
					sb.delete(0, mTvSerial.length());
					mTvSerial.setText(sb);}
				break;
			default:
				break;}
		}

		private final byte[] ftpDownload(){
			MTKutility.debugWrite(132, "EPOFragment - AGPSupdate.ftpDownload()");
			byte[] result = null;
			boolean FTPresult = false;
			int EPOlen = 0;
			ByteArrayOutputStream EPOout;
			EPOout = new ByteArrayOutputStream();
			try {FTPClient ftpClient = new FTPClient();
			try {
				ftpClient.setConnectTimeout(5000);
				ftpClient.connect(sFTP_IP, Integer.parseInt(sFTP_port));
				ftpClient.enterLocalPassiveMode();
				ftpClient.login(sFTP_userid, sFTP_pswd);
				ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
				FTPresult=ftpClient.retrieveFile("/" + sFTP_file, EPOout);
				ftpClient.logout();
				ftpClient.disconnect();
				EPOlen = EPOout.size();
			} catch (IOException e) {abort = true; e.printStackTrace();
			} catch (Exception e){abort = true; e.printStackTrace();}
			if (FTPresult){
				msg = Integer.toString(EPOlen) + " AGPS data bytes downloaded";
			}else {
				msg = "AGPS data download failed";}
			publishProgress(String.valueOf(pToast), msg);

			//validate EPO file - length must be a multiple of the EPO SET size
			if (EPOlen != 0) {
				double Depoblocks = (EPOlen / EPOblocksize);
				int Iepoblocks = (int) Depoblocks;
				if (Depoblocks != Iepoblocks){
					//result = null;
					EPOlen = 0;}}
			} catch (Exception e) {abort = true;
			String exceptionMessage="FTP error: " + e.getMessage() + e.getCause();
			publishProgress(String.valueOf(pToast), exceptionMessage);}

			if (EPOlen == 0) {
				publishProgress(String.valueOf(pToast), "Downlaoded EPO file is invalid");
				abort = true;}
			result = EPOout.toByteArray();
			return result;
		}

		private void updateAGPSdata(byte[] EPObytes) throws IOException {
			MTKutility.debugWrite(132, "EPOFragment - AGPSupdate.updateAGPSdata()");
			String TAG = "updateAGPSdata";
			int packetIDX;
			int remainder;
			int loops;
			String[] prms = new String[10];
			String hBuf;
			byte[] rbuf = new byte[4096];
//			int EPOblks;

			// initialize counters
			int satPacketsSent = 0; // SAT data packets processed
			int epoSeq = 0;         // EPO binary packet sequence number
			int epoIDX = 0;         // EPO download byte index
			int satPackets = EPObytes.length / SatDataLen;

			// build EPO binary packet type 722
			byte[] epo_bin = new byte[191];
			epo_bin[0] = (byte) 0x04; //preamble - 2 bytes
			epo_bin[1] = (byte) 0x24;
			epo_bin[2] = (byte) 0xBF; //packet length - 2 bytes
			epo_bin[3] = (byte) 0x00;
			epo_bin[4] = (byte) 0xD2; //command ID - 2 bytes
			epo_bin[5] = (byte) 0x02;
			epo_bin[189] = (byte) 0x0D; // carriage return
			epo_bin[190] = (byte) 0x0A; // line feed
			prms = MTKutility.mtkCmd("PMTK607", "PMTK707", MTKutility.cmdTimeOut);
			if (prms != null){
				if (Integer.valueOf(prms[1]) != 0) {
					//delete the MTK logger EPO data
					MTKutility.mtkCmd("PMTK127", "PMTK001,127", MTKutility.cmdTimeOut);
				}
			}

			//Switch the protocol to BINARY mode
			MTKutility.debugWrite(132, "*********** switching to binary mode ***********");
			MTKutility.debugWrite(132, "*** sending: PMTK253,1,0");
			try {gpsdev.sendCommand("PMTK253,1,0");
			} catch (IOException e) {
				e.printStackTrace();
				//abort if write failed
				msg = "Aborting: switch to Binary mode failed";
				MTKutility.debugWrite(132, "*** " + msg);
				publishProgress(String.valueOf(pToast), msg);
				abort = true;
				return;}

			//wait for command to take effect
			MTKutility.goSleep(1000);

			boolean doExtract = false;
			//			double timeout = 10.0;
			byte[] extract = new byte[12];
			boolean found = false;
			//send the EPO data
			MTKutility.debugWrite(132, "+++ updating AGPS data");
			while ((satPacketsSent < satPackets) && (abort == false)){
				// clear the SAT data and checksum bytes
				for (int i = 8; i < 189; i++){
					epo_bin[i] = (byte) 0x00;}
				// set the EPO sequence number - 2 bytes
				epo_bin[6] = (byte) (epoSeq & 0xFF);
				epo_bin[7] = (byte) ((epoSeq >> 8) & 0xFF);
				// fill SAT data bytes with EPO data for 3 SAT data sets
				packetIDX = 8; loops = 0;
				while (loops < 3) {
					epo_bin[packetIDX] = EPObytes[epoIDX];
					epoIDX++; packetIDX++;
					remainder = (packetIDX - 8) % 60;
					if (remainder == 0) {
						satPacketsSent++; loops++;
						if (satPacketsSent == satPackets){
							loops = 3;}
						int prgPCT = (satPacketsSent * 100) / satPackets;
						//publishProgress String parms are: Action, Style, message, percent
						publishProgress(String.valueOf(pProgBar), null, null, Integer.toString(prgPCT));}
				}
				//set packet checksum - exclusive OR of bytes between the preamble and checksum
				for (int i = 2; i < 188; i++) {
					epo_bin[188] ^= epo_bin[i];
				}

				// send the EPO binary packet
				MTKutility.goSleep(200);
				try {gpsdev.sendBytes(epo_bin);} 
				catch (IOException e) {
					e.printStackTrace();
					//abort send if write fails
					MTKutility.debugWrite(132, "**** Aborting: BlueTooth send failed");
					publishProgress(String.valueOf(pToast), "Aborting: BlueTooth send failed");
					break;
				}
				if (MTKutility.debugFileIsOpen){
					tsLong = System.currentTimeMillis()/100;
					ts = tsLong.toString();
					MTKutility.debugWrite(132, "epo>> timestamp:"+ts);
					hBuf = bytesToHex(epo_bin) + BR;
					MTKutility.debugWrite(132, "epo>> "+hBuf);
				}
				epoSeq++;
				doExtract = false;
				found = false;
				int retry = (int) MTKutility.cmdTimeOut * 3;
				int buflen = 0;
				// Read from the device until we get the reply we are looking for
				while (retry > 0 && !found) {
					retry--;
					try {rbuf = gpsdev.readBytes(MTKutility.cmdTimeOut);}
					catch (IOException e) {e.printStackTrace();}
					catch (InterruptedException e) {e.printStackTrace();}
					if (rbuf.length == 0) {
						gpsdev.debugLog(TAG, "++++ No bytes read from device!");
						throw new IOException("No bytes read from device!");
					}
					if (MTKutility.debugFileIsOpen){
						tsLong = System.currentTimeMillis()/100;
						ts = tsLong.toString();
						MTKutility.debugWrite(132, "epo<< retry:"+ Integer.toString(retry) + "--timestamp:"+ts);
						hBuf = bytesToHex(rbuf) + BR;
						MTKutility.debugWrite(132, "epo<< "+hBuf);
//						String s = new String(rbuf);
//						MTKutility.debugWrite(132, "epo<< "+s);
					}
					for (int j = 0; j < rbuf.length; j++) {
						// Check if this is the start of a new message
						if ((!doExtract) && (rbuf[j] == 0x04)) {
							doExtract = true;
							buflen = 0;}
						if (doExtract){
							extract[buflen] = rbuf[j];
							if ((buflen == 1) && (extract[buflen] != 0x24)){
								doExtract = false;
							}
							buflen++;
							if (buflen > 11){
								doExtract = false;
								hBuf = bytesToHex(extract) + BR;
								if (extract[6] == epo_bin[6] && extract[7] == epo_bin[7]){
									found = true;
									publishProgress(String.valueOf(pText), hBuf);
								}
							}
						}
					}
//					MTKutility.goSleep(300);
				}
				if (found){
					if (extract[8] != 0x01){
						MTKutility.debugWrite(132, "**** Aborting: invalid EPO_bin");
						publishProgress(String.valueOf(pToast), "Aborting: invalid EPO_bin - retry update");
						abort = true;
					}
				}else {
					MTKutility.debugWrite(132, "**** Aborting: timeout on packet acknowledge");
					publishProgress(String.valueOf(pToast), "Aborting: timeout on packet acknowledge");
					abort = true;
				}
			}

			// clear the SAT data bytes
			for (int i = 8; i < 188; i++){
				epo_bin[i] = (byte) 0x00;}
			//set the final packet sequence number and checksum
			epo_bin[6] = (byte) 0xFF;
			epo_bin[7] = (byte) 0xFF;
			for (int i = 2; i < 188; i++) {
				epo_bin[188] ^= epo_bin[i];}
			// send the final EPO binary packet
			try {gpsdev.sendBytes(epo_bin);
			} catch (IOException e) {e.printStackTrace();}
			if (MTKutility.debugFileIsOpen){
				tsLong = System.currentTimeMillis()/100;
				ts = tsLong.toString();
				MTKutility.debugWrite(132, "epo<< timestamp:"+ts);
				hBuf = bytesToHex(epo_bin) + BR;
				MTKutility.debugWrite(132, "epo>> "+hBuf);
			}

			MTKutility.goSleep(1000);
			//Switch the protocol to NMEA mode
			MTKutility.debugWrite(132, "*** sending: binPMTK253");
			try {gpsdev.sendBytes(binPMTK253);
			} catch (IOException e) {e.printStackTrace();}
			MTKutility.goSleep(1000);
		}

		private String bytesToHex(byte[] bytes) {
			mText = new StringBuilder();

			for ( int j = 0; j < bytes.length; j++ ) {
				int v = bytes[j] & 0xFF;
				mText.append(hexArray[v >>> 4]);
				mText.append(hexArray[v & 0x0F]);
				if ((j > 0)&&(j < bytes.length - 1)){
					if ((bytes[j] == lfByte) & (bytes[j-1] == crByte)){
						mText.append(BR);}}
			}
			return new String(mText);
		}
	};
}