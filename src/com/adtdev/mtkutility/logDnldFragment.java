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
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import android.app.Activity;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.text.format.Time;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class logDnldFragment extends Fragment {
	private View mV;

	// screen objects
	private TextView   mEPOinfo;
	private TextView   mDLdesc;
	private CheckBox   mcb_mkGPX;
	private CheckBox   mcb_OneTrk;
	private TextView   btvDLpath;
	private TextView   gtvDLpath;
	private Button     btnBINdnld;
	private Button     btnCLR;
	private ScrollView msv_Text;
	private TextView   mtv_Serial;
	private Spinner    msl_overlap;

	//sharedPref
	private SharedPreferences sharedPref;
	private SharedPreferences.Editor prefEditor;
	private int	pref_cSize;
	private int	pref_mSize;
	private int	pref_ovrlap;
//	private String pref_path;
	private String BINpath;
	private String GPXpath;
	private File bin_file;

//	private Boolean    cbxGPXfile;
//	private Boolean    cbxOneTrk;

	private StringBuilder mText = new StringBuilder();
	private final static String BR = System.getProperty("line.separator");
	private static final int ScrollTextSize = 5120;
	private String msg;
	private String[] prms;

	// Progress Dialog
	private ProgressDialog pDialog;

	//binary to hex conversion values
	final protected static char[] hexArray = "0123456789ABCDEF".toCharArray();

	// Message types for publishProgress
	private static final int pCancel  = 0;
	private static final int pToast   = 1;
	private static final int pText    = 2;
	private static final int pProgBar = 3;

	GPSrxtx gpsdev;
	static ProgressDialog dialog;
	
	String pathName;
	String BINfile;
	String GPXfile;
	private final int REQUEST_CODE_BIN_DIR = 1;
	private final int REQUEST_CODE_GPX_DIR = 2;
//	private final int REQUEST_CODE_BIN_FILE = 3;

	// Keys
	public static final String KEY_TOAST = "toast";
	public static final String MESSAGEFIELD = "textSwitcher";
	public static final String KEY_PROGRESS = "progressCompleted";
	public static final String CLOSE_PROGRESS = "closeProgressDialog";
	public static final String CREATEGPX = "parseBinFile";

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		MTKutility.debugWrite(132, "logDnldFragment - onCreateView()");
		sharedPref = MTKutility.getSharedPreferences();
		prefEditor = sharedPref.edit();
		
		mV =  inflater.inflate(R.layout.logdnldfragment, container, false);

		btvDLpath  = (TextView) mV.findViewById(R.id.btvDLpath);
		btvDLpath.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				MTKutility.debugWrite(132, "logDnldFragment - button " + btvDLpath.getText() + " pressed");
				getBINpathPref();
			}
		});

		gtvDLpath  = (TextView) mV.findViewById(R.id.gtvDLpath);
		gtvDLpath.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				MTKutility.debugWrite(132, "logDnldFragment - button " + gtvDLpath.getText() + " pressed");
				getGPXpathPref();
			}
		});

		btnBINdnld = (Button) mV.findViewById(R.id.btnBINdnld);
		btnBINdnld.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				MTKutility.debugWrite(132, "logDnldFragment - button " + btnBINdnld.getText() + " pressed");
				startDownload();
			}
		});

		btnCLR = (Button) mV.findViewById(R.id.btnCLR);
		btnCLR.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				MTKutility.debugWrite(132, "logDnldFragment - button " + btnCLR.getText() + " pressed");
				//get log records count
				prms = MTKutility.mtkCmd("PMTK182,2,10", "PMTK182,3,10", MTKutility.cmdTimeOut*2);
				if (prms != null){
					long recs = Long.parseLong(prms[3], 16);
					if (recs == 0){
						msg = "MTK logger has 0 log records" + BR +
								"nothing to delete";
						Toast.makeText(getActivity(), msg, Toast.LENGTH_LONG).show();
					}else {
						DeleteLog task = new DeleteLog(getActivity());
						task.execute();
					}
				}
			}
		});

		mEPOinfo    = (TextView)   mV.findViewById(R.id.AGPStitle);
		mDLdesc     = (TextView)   mV.findViewById(R.id.mOLdesc);
		mcb_mkGPX   = (CheckBox)   mV.findViewById(R.id.mcb_mkGPX);
		mcb_OneTrk  = (CheckBox)   mV.findViewById(R.id.mcb_OneTrk);
		mtv_Serial  = (TextView)   mV.findViewById(R.id.mtv_Serial);
		msv_Text    = (ScrollView) mV.findViewById(R.id.msv_Text);
		msl_overlap = (Spinner)    mV.findViewById(R.id.msl_overlap);
		
		msl_overlap.setOnItemSelectedListener(new CustomOnItemSelectedListener());

		mEPOinfo.setTextSize(TypedValue.COMPLEX_UNIT_SP, MTKutility.epoMSGfont);
		mDLdesc.setTextSize(TypedValue.COMPLEX_UNIT_SP, MTKutility.dnldTexts-2);
		btnBINdnld.setTextSize(TypedValue.COMPLEX_UNIT_SP, MTKutility.dnldButtons);
		btnCLR.setTextSize(TypedValue.COMPLEX_UNIT_SP, MTKutility.dnldButtons);
		btvDLpath.setTextSize(TypedValue.COMPLEX_UNIT_SP, MTKutility.dnldButtons-4);
		gtvDLpath.setTextSize(TypedValue.COMPLEX_UNIT_SP, MTKutility.dnldButtons-4);
		mcb_mkGPX.setTextSize(TypedValue.COMPLEX_UNIT_SP, MTKutility.dnldTexts);
		mcb_OneTrk.setTextSize(TypedValue.COMPLEX_UNIT_SP, MTKutility.dnldTexts);

		return mV;
	}

	@Override
	public void  onPause() {
		super.onPause();
		MTKutility.debugWrite(132, "logDnldFragment - onPause()");
//		MTKutility.NMEAoutStart();
	}

	@Override
	public void onResume() {
		super.onResume();
		MTKutility.debugWrite(132, "logDnldFragment - onResume()");
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
		BINpath = sharedPref.getString("BINpath", Environment.getExternalStorageDirectory().toString());
		btvDLpath.setText(BINpath);
		GPXpath = sharedPref.getString("GPXpath", Environment.getExternalStorageDirectory().toString());
		gtvDLpath.setText(GPXpath);
		pref_cSize = Integer.parseInt(sharedPref.getString("chunkSizePref", ""));
		pref_mSize = Integer.parseInt(sharedPref.getString("memSizePref", ""));
		pref_ovrlap = Integer.parseInt(sharedPref.getString("overlapPref", "0"));
		msl_overlap.setSelection(pref_ovrlap);
//		cbxGPXfile = sharedPref.getBoolean("createGPXPref", true);
//		mcb_mkGPX.setChecked(cbxGPXfile);
//		cbxOneTrk = sharedPref.getBoolean("createOneTrkPref", false);
//		mcb_OneTrk.setChecked(cbxOneTrk);

		//		String spinVal = String.valueOf(msl_overlap.getSelectedItem());
		String epoinfo = MTKutility.getEPOinfo();
		mEPOinfo.setText(epoinfo);

		if (mtv_Serial.length() > 0) {
			StringBuilder sb = new StringBuilder();
			sb.append(mtv_Serial.getText());
			sb.delete(0, mtv_Serial.length());
			mtv_Serial.setText(sb);}
	}

//	private void savePreferences() {
//		MTKutility.debugWrite(132, "logDnldFragment - savePreferences()");
//		Boolean checkBoxState;
//		int selected;
//
//		checkBoxState = false;
//		if (mcb_mkGPX.isChecked()){
//			checkBoxState = true;
//		}
//		sharedPrefEditor.putBoolean("createGPXPref", checkBoxState);
//		sharedPrefEditor.commit();
//
//		checkBoxState = false;
//		if (mcb_OneTrk.isChecked()){checkBoxState = true;}
//		sharedPrefEditor.putBoolean("createOneTrkPref", checkBoxState);
//		sharedPrefEditor.commit();
//
//		selected = msl_overlap.getSelectedItemPosition();
//		sharedPrefEditor.putString("overlapPref", Integer.toString(selected));
//		sharedPrefEditor.commit();
//	}

	private void getBINpathPref() {
		MTKutility.debugWrite(132, "logDnldFragment - getBINpathPref()");
		//		SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());
		boolean foldersOnly = sharedPref.getBoolean("foldersOnly", false);
		Intent fileExploreIntent = new Intent(getActivity(), FilePickerActivity.class);
		fileExploreIntent.putExtra(FilePickerActivity.FullPath, MTKutility.rootDirectory);
		fileExploreIntent.putExtra(FilePickerActivity.LimitParent, true);
		fileExploreIntent.putExtra(FilePickerActivity.ShowHidden, false);
		fileExploreIntent.putExtra(FilePickerActivity.FoldersOnly, foldersOnly);

		startActivityForResult(fileExploreIntent, REQUEST_CODE_BIN_DIR);
	}

	private void getGPXpathPref() {
		MTKutility.debugWrite(132, "logDnldFragment - getGPXpathPref()");
		//		SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());
		boolean foldersOnly = false;
		Intent fileExploreIntent = new Intent(getActivity(), FilePickerActivity.class);
		fileExploreIntent.putExtra(FilePickerActivity.FullPath, MTKutility.rootDirectory);
		fileExploreIntent.putExtra(FilePickerActivity.LimitParent, true);
		fileExploreIntent.putExtra(FilePickerActivity.ShowHidden, false);
		fileExploreIntent.putExtra(FilePickerActivity.FoldersOnly, foldersOnly);

		startActivityForResult(fileExploreIntent, REQUEST_CODE_GPX_DIR);
	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		MTKutility.debugWrite(132, "MTKutility - onActivityResult("
				+ Integer.toString(requestCode)
				+ "," + Integer.toString(resultCode)
				+ "," + data +")");
//		SharedPreferences sharedPreferences = MTKutility.getSharedPreferences();
//		SharedPreferences.Editor editor = sharedPreferences.edit();
		
		//		Log.d(LOGTAG, "onActivityResult("+requestCode+","+resultCode+",...)");
		if (requestCode == REQUEST_CODE_BIN_DIR) {
			if (resultCode == Activity.RESULT_OK) {
				pathName = data.getStringExtra(FilePickerActivity.FullPath);
				if (pathName != null){
					Toast.makeText(getActivity(), "New Export Path:\n" + pathName, Toast.LENGTH_LONG).show();
					prefEditor.putString("BINpath", pathName);
					prefEditor.commit();
				} else {
					Toast.makeText(getActivity(), "No Changes Made", Toast.LENGTH_LONG).show();
				}
			} else {
				Toast.makeText(getActivity(), "No Changes Made", Toast.LENGTH_LONG).show();
			}
		}

		if (requestCode == REQUEST_CODE_GPX_DIR) {
			if (resultCode == Activity.RESULT_OK) {
				pathName = data.getStringExtra(FilePickerActivity.FullPath);
				if (pathName != null){
					Toast.makeText(getActivity(), "New Export Path:\n" + pathName, Toast.LENGTH_LONG).show();
					prefEditor.putString("GPXpath", pathName);
					prefEditor.commit();
				} else {
					Toast.makeText(getActivity(), "No Changes Made", Toast.LENGTH_LONG).show();
				}
			} else {
				Toast.makeText(getActivity(), "No Changes Made", Toast.LENGTH_LONG).show();
			}
		}

		super.onActivityResult(requestCode, resultCode, data);
	}

	private void startDownload() {
		MTKutility.debugWrite(132, "logDnldFragment - startDownload()");
		//check for BlueTooth connection
		if (!gpsdev.sock.isConnected()) {
			//re-establish the MTK logger connection
			if (!gpsdev.connect()){
				msg = "BlueTooth connection is missing";
				Toast.makeText(getActivity(), msg, Toast.LENGTH_LONG).show();
				MTKutility.debugWrite(132, "**** "+msg);
				return;}}

		if (gpsdev.sock.isConnected()) {
			//get log records count
			MTKutility.NMEAoutStop();
			prms = MTKutility.mtkCmd("PMTK182,2,10", "PMTK182,3,10", MTKutility.cmdTimeOut*2);
			if (prms != null){
				long recs = Long.parseLong(prms[3], 16);
				if (recs == 0){
					msg = "MTK logger has 0 log records" + BR +
							"nothing to download";
					Toast.makeText(getActivity(), msg, Toast.LENGTH_LONG).show();
					MTKutility.NMEAoutStart();
				} else {
					//start download in a background thread
					logDownload task = new logDownload(getActivity());
					task.execute();}}}
	}

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
//			if (msg.getData().containsKey(CREATEGPX)) {
//				createGPX(msg.getData().getString(CREATEGPX));}
			if (msg.getData().containsKey(KEY_TOAST)) {
				String message = msg.getData().getString(KEY_TOAST);
				Toast.makeText(getActivity(), message, Toast.LENGTH_LONG).show();}
		}
	};


	public void writeToMainTextArea(String text) {
		MTKutility.debugWrite(132, "logDnldFragment - writeToMainTextArea("+text+")");
		//    	main_textview.setText(text + '\n' + main_textview.getText());
		if (mtv_Serial.length() > ScrollTextSize) {
			StringBuilder sb = new StringBuilder();
			sb.append(mtv_Serial.getText());
			sb.delete(0, ScrollTextSize / 2);
			mtv_Serial.setText(sb);}

		mtv_Serial.append(text + "\n");
		mText.setLength(0);
		msv_Text.fullScroll(View.FOCUS_DOWN);
	}

	private void createGPX() {
		MTKutility.debugWrite(132, "logDnldFragment - createGPX()");
		boolean oneTrk = false;
		dialog = new ProgressDialog(getActivity());
		dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		dialog.setMessage("Converting GPS log to GPX file");
		dialog.setCancelable(true);
		dialog.setMax(100);
		dialog.show();

		// Start a new thread for it!
		BINpath = sharedPref.getString("BINpath", Environment.getExternalStorageDirectory().toString());
		BINpath = BINpath + "/" + BINfile;
		if (mcb_OneTrk.isChecked()) oneTrk = true;
		ParseBinFile parseBinFile = new ParseBinFile(BINpath, GPXfile, parseHandler, oneTrk);
		Thread gpxThread = new Thread(parseBinFile);
		gpxThread.start();
	}

	class logDownload extends AsyncTask<Void, String, Void> {
		private Context mContext;
		//		private ProgressDialog mProgress;
		//		private int mProgressDialog = 0;
		final int SIZEOF_SECTOR = 0x10000;
		private String file_time_stamp;
		private String reply = null;
		private String msg;
//		private File bin_file;
		private int bytesRead = 0;
		private BufferedOutputStream bin_output_stream = null;
		private boolean running = true;
//		private String[] prms;

		public logDownload(Context context){
			MTKutility.debugWrite(132, "logDnldFragment - logDownload.logDownload()");
			this.mContext = context;
		}

		@Override
		protected void onPreExecute(){
			MTKutility.debugWrite(132, "logDnldFragment - logDownload.onPreExecute()");
			msg = String.format("%d log records to download", MTKutility.BINrecs);
			MTKutility.debugWrite(132, ">>>> "+msg);
			btnBINdnld.setClickable(false);
		}

		@Override
		protected Void doInBackground(Void... params) {
			MTKutility.debugWrite(132, "logDnldFragment - logDownload.doInBackground()");
			publishProgress(String.valueOf(pProgBar), 
					String.valueOf(ProgressDialog.STYLE_HORIZONTAL),
					"Creating binary log file", null);
			downloadBINdata();
			publishProgress(String.valueOf(pCancel));
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			MTKutility.debugWrite(132, "logDnldFragment - logDownload.onPostExecute()");
			btnBINdnld.setClickable(true);
			MTKutility.NMEAoutStart();
			if ((mcb_mkGPX.isChecked())&&(bytesRead != 0)){
				createGPX();}
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
					pDialog.setTitle("Log download");
					pDialog.setMessage(values[2]);
					pDialog.setCancelable(true);
					if (Integer.parseInt(values[1]) == ProgressDialog.STYLE_HORIZONTAL){
						pDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
						pDialog.setIndeterminate(false);
						pDialog.setMax(100);
					}else {
						pDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
						pDialog.setIndeterminate(true);}
					pDialog.show();}
				break;
			case pToast:
				Toast.makeText(mContext, values[1], Toast.LENGTH_SHORT).show();
				break;
			case pText:
				writeToMainTextArea(values[1]);
				break;
			case pCancel:
				pDialog.dismiss();		
				break;
			default:
				break;}
		}

		private void downloadBINdata() {
			MTKutility.debugWrite(132, "logDnldFragment - logDownload.downloadBINdata()");
			String prms[] = new String[30];
			String log_full_method = "0";
			Time now = new Time();
			now.setToNow();
			file_time_stamp = now.format("%Y-%m-%d_%H%M%S");

			//query logging method 1=overlap, 2=stop when full
			prms = MTKutility.mtkCmd("PMTK182,2,6", "PMTK182,3,6", MTKutility.cmdTimeOut*2);
			if (prms == null){
				errorWhileDownloading();
				return;
			}
			log_full_method = prms[3];

			// Determine how many bytes we need to read from the GPS log
			int bytes_to_read = pref_mSize;
			if (log_full_method == "1") {
				// Device is in OVERLAP mode we don't know where data ends; read the entire memory.
				if (pref_ovrlap == 0) {
					msg = "NOTE! Your device is in 'Overwrite when FULL mode', this is not a very efficient mode for download over bluetooth. Aborting! If you really want to download in this mode, please enable it via the sharedPref";
					publishProgress(String.valueOf(pToast), msg);
					return;}
				if (bytes_to_read > 0) {
					msg = "Device is in OVERLAP mode, memory size set by user sharedPref";
					MTKutility.debugWrite(132, "**** "+msg);
				}else {
					msg = "Device is in OVERLAP mode";
					MTKutility.debugWrite(132, "**** "+msg);
					bytes_to_read = MTKutility.flashSize;}
			}else {
				msg = "Device is in STOP mode finding next write address";
				MTKutility.debugWrite(132, "**** "+msg);
				int next_write_address = 0;
				// Query the RCD_ADDR (data log Next Write Address).
				prms = MTKutility.mtkCmd("PMTK182,2,8", "PMTK182,3,8", MTKutility.cmdTimeOut*2);
				if (prms == null){
					errorWhileDownloading();
					return;
				}
				next_write_address = Integer.parseInt(prms[3], 16);  
				msg = String.format("Next write address: %d (0x%08X)", next_write_address, next_write_address);
				MTKutility.debugWrite(132, "**** "+msg);
				int sectors  = (int) Math.floor(next_write_address / SIZEOF_SECTOR);
				if (next_write_address % SIZEOF_SECTOR != 0) {
					sectors += 1;
				}
				bytes_to_read = sectors * SIZEOF_SECTOR;
			}
			msg = String.format("Need to read %d (0x%08X) bytes of log data from device...", bytes_to_read, bytes_to_read);
			MTKutility.debugWrite(132, "**** "+msg);

			// Open an output stream for writing
			String gpxStr = ".gpx";
			BINfile = "gpslog"+file_time_stamp+".bin";
			if (mcb_OneTrk.isChecked()) gpxStr = "OT.gpx";
			GPXfile = "gpslog"+file_time_stamp+gpxStr;
			bin_file = new File(BINpath.toString(), BINfile);
			try {bin_output_stream = new BufferedOutputStream(new FileOutputStream(bin_file), SIZEOF_SECTOR);
			} catch (FileNotFoundException e1) {
				e1.printStackTrace();
				errorWhileDownloading();
				return;
			}

			// To be safe we iterate requesting SIZEOF_CHUNK bytes at time.
			int offset = 0;
			abort:
			while (running && offset < bytes_to_read) {
				MTKutility.debugWrite(132, "**** offset=" + Integer.toString(offset));
				// Request log data (PMTK_LOG_REQ_DATA) from offset to bytes_to_read.
				String command = String.format("PMTK182,7,%08X,%08X", offset, pref_cSize);
				MTKutility.debugWrite(132, "**** "+String.format("Sending command: %s", command));
				try {gpsdev.sendCommand(command);
				} catch (IOException e1) {
					errorWhileDownloading();
					publishProgress(String.valueOf(pText),"GPS read failed - download aborted");
					return;
				}
				// Read from the device
				// The chunk might be split over more than one message
				// read until all bytes are received
				int number_of_empty = 0;
				byte[] tmp_array = new byte[pref_cSize];
				int bytes_received = 0;
				int messagesToRead = 1;
				int messagesRead = 0;
				int retry = 5;
				if (pref_cSize > 0x800) {
					messagesToRead = pref_cSize/0x800;}
				msg = String.format("pref_cSize=%d, waiting for %d PMTK182,8 responses", pref_cSize, messagesToRead);
				MTKutility.debugWrite(132, "**** "+msg);
				while (messagesRead < messagesToRead) {
					try {reply = gpsdev.waitForReply("PMTK182,8", MTKutility.cmdTimeOut*2);
					} catch (IOException e1) {
						e1.printStackTrace();
						MTKutility.debugWrite(132, "**** IOException:"+e1);
						errorWhileDownloading();
						break abort;
					} catch (InterruptedException e1) {
						e1.printStackTrace();
						MTKutility.debugWrite(132, "**** InterruptedException:"+e1);
						errorWhileDownloading();
						break abort;
					}
					if (reply == null) {
						MTKutility.debugWrite(132, "**** null reply received - retrying");
						// Asked for message was not found - repeat read request
						try {gpsdev.sendCommand(command);
						} catch (IOException e1) {
							errorWhileDownloading();
							break abort;
						}
						if (retry-- > 0){
							messagesRead = 0;
							continue;
						}
					}
					if (reply.contains("PMTK001")){
						if (reply.contains("PMTK001,182,7,3")){
							continue;
						}else{
							errorWhileDownloading();
							break abort;
						}
					}
					messagesRead++;
					msg = String.format("Got reply: %s", reply);
					MTKutility.debugWrite(132, "**** "+msg);
					for (int i = 20; i < (reply.length()-2); i += 2) {
						String string_byte = reply.substring(i, i+2);
						if (string_byte.equals("FF")) {
							number_of_empty++;}
						try {tmp_array[bytes_received] = (byte) (Integer.parseInt(string_byte, 16) & 0xFF);
						bytes_received++;
						} catch (NumberFormatException e) {}
					}
					double prgPCT = ((offset+bytes_received) / (double)bytes_to_read) * 100.0;
					int pct = (int) prgPCT;
					publishProgress(String.valueOf(pProgBar), null, null, Integer.toString(pct));
				}
				if (bytes_received != pref_cSize) {
					msg = String.format("ERROR! bytes_received(%d) != pref_cSize", bytes_received);
					MTKutility.debugWrite(132, "**** "+msg);
					continue;
				}else {
					offset += pref_cSize;
					try {bin_output_stream.write(tmp_array, 0, pref_cSize);
					} catch (IOException e) {
						e.printStackTrace();
						MTKutility.debugWrite(132, "**** IOException:"+e);
						publishProgress(String.valueOf(pText),"aborting\nfile write failed");
						return;
					}
				}
				// In OVERWRITE mode when user asked us, when we find and empty sector assume rest of memory is empty
				if (pref_ovrlap == 1 && number_of_empty == bytes_received) {
					offset = bytes_to_read;
					MTKutility.debugWrite(132, "**** Found empty sector - stopping read");
				}
			}

			// Close the bin file
			bytesRead = offset;
			try {if (bin_output_stream != null) {
				bin_output_stream.flush();
				bin_output_stream.close();}
			} catch (IOException e){
				e.printStackTrace();
			}

			if (!running) {
				publishProgress(String.valueOf(pText),"Download aborted");
				MTKutility.debugWrite(132, "**** Download aborted");
				cleanup();
				return;
			}
			// Send a status message to the main thread
			publishProgress(String.valueOf(pText),"Download complete saved to:" + bin_file);	
		}

		public void errorWhileDownloading() {
			MTKutility.debugWrite(132, "logDnldFragment - logDownload.errorWhileDownloading()");
			running = false;
			publishProgress(String.valueOf(pText),"Download failed");
		}

		public void cleanup() {
			MTKutility.debugWrite(132, "logDnldFragment - logDownload.cleanup()");
			// Clean up, delete the bin file
			bin_file.delete();
		}
	}

	class DeleteLog extends AsyncTask<Void, Void, Void> {
		//parameters passed are:logRecCount, logFormat, RbtnValParm
		private Context mContext;
		ProgressDialog pDialog;
		boolean loop = true;

		public DeleteLog(Context context){
			this.mContext = context;
		}

		@Override
		protected void onPreExecute(){
			MTKutility.debugWrite(132, "logDnldFragment - DeleteLog.onPreExecute()");
			pDialog = new ProgressDialog(mContext);
			pDialog.setTitle("Deleting log");
			pDialog.setMessage("working");
			pDialog.setCancelable(false);
			pDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
			pDialog.setIndeterminate(true);
			pDialog.show();
		}

		@Override
		protected void onPostExecute(Void result) {
			MTKutility.debugWrite(132, "logDnldFragment - DeleteLog.onPostExecute()");
			MTKutility.renewAGPS= true;
			mEPOinfo.setText(MTKutility.getEPOinfo());
			pDialog.dismiss();
		}

		@Override
		protected Void doInBackground(Void... parms) {
			MTKutility.debugWrite(132, "logDnldFragment - DeleteLog.doInBackground()");
			MTKutility.mtkCmd("PMTK182,6,1", "PMTK001,182,6", MTKutility.cmdTimeOut+40.0);
			return null;
		}
	}
	
	public class CustomOnItemSelectedListener implements OnItemSelectedListener {

		@Override
		public void onItemSelected(AdapterView<?> parent, View view, int pos,long id) {
//			Toast.makeText(parent.getContext(), 
//					"OnItemSelectedListener : " + parent.getItemAtPosition(pos).toString(),
//					Toast.LENGTH_SHORT).show();
//			int selected =  parent.getItemAtPosition(pos).toString();
			pref_ovrlap = pos;
			prefEditor.putString("overlapPref", Integer.toString(pos));
			prefEditor.commit();
		}

		@Override
		public void onNothingSelected(AdapterView<?> arg0) {
			// TODO Auto-generated method stub
		}

	}
}

