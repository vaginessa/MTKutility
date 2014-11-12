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
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Arrays;
import java.util.Locale;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class MTKhtmlSettings extends Fragment {
	Context contxt;

	final String spcFill = "&nbsp;&nbsp";
	final static String checked = "✔";
	final String radiobtn = "&#128280";
	final int dfltBitmask = 0x8002003F;
	static double bumpTimeOut = 3;
	private String htmFS;
	private String htmFSs;

	//layout inflater values
	private View mV;
	private Button wvbtnUTC;
	private Button wvbtnNav;
	private Button wvbtnRec;
	private Button wvbtnMtd;
	private Button wvbtnSat;
	private Button wvbtnDif;
	private Button wvbtnDOP;
	private Button wvbtnFreq;
	private Button wvbtnDflt;
	private Button wvbtnSave;

	//checkboxes
	private static String cbxDate; //date / time
	private static String cbxMili; //milliseconds
	private static String cbxLat; //latitude
	private static String cbxLon; //longitude
	private static String cbxHei; //height
	private static String cbxSpd; //speed
	private static String cbxHed; //heading
	private static String cbxDis; //distance
	private static String cbxRCR; //reason for recorded track point
	private static String cbxVal; //fix mode - SPS, DGPS, etc.
	private static String cbxFxo; //only record track points that have a satellite fix
	private static String cbxNsat; //number of satellites
	private static String cbxSID; //satellite id
	private static String cbxEle; //satellite elevation 
	private static String cbxAzi; //satellite azimuth
	private static String cbxSNR; //satellite number
	private static String cbxDSTA; //differential reference station id
	private static String cbxDAGE; //age of differential data in seconds 
	private static String cbxPDOP; //position dilution of precision (m)
	private static String cbxHDOP; //horizontal dilution of precision (m)
	private static String cbxVDOP; //vertical dilution of precision (m)

	private static String radOvrl;
	private static String radStop;

	boolean SIDchecked = false;

	// Log format is stored as a bitmask field.
	static final int cbxDateMask = 0x00000001;
	static final int cbxMiliMask = 0x00040000;
	static final int cbxNsatMask = 0x00001000;
	static final int cbxSIDMask = 0x00002000;
	static final int cbxEleMask = 0x00004000;
	static final int cbxAziMask = 0x00008000;
	static final int cbxSNRMask = 0x00010000;
	static final int cbxLatMask = 0x00000004;
	static final int cbxLonMask = 0x00000008;
	static final int cbxHeiMask = 0x00000010;
	static final int cbxSpdMask = 0x00000020;
	static final int cbxHedMask = 0x00000040;
	static final int cbxDisMask = 0x00080000;
	static final int cbxDSTAMask = 0x00000080;
	static final int cbxDAGEMask = 0x00000100;
	static final int cbxRCRMask = 0x00020000;
	static final int cbxValMask = 0x00000002;
	static final int cbxFxoMask = 0x80000000;
	static final int cbxPDOPMask = 0x00000200;
	static final int cbxHDOPMask = 0x00000400;
	static final int cbxVDOPMask = 0x00000800;

	static int logFldsMask = 0x00000000;
	static int logFldsMasks = 0x00000000;
	static int logRecLen = 0;
	static int maxRecords;
	static double maxTime;
	static int logRecCount;
	static int intVal;
	static double dVal;
	//	static int MTKlogFormat;
	static int logMode;
	static int logModes;
	private String cmd;
	static String txtByTstr;
	static String txtByDstr;
	static String txtBySstr;
	private String msg;
	private String[] parms;

	static DecimalFormat TimeFmt = new DecimalFormat("#0.00");
	//	static String tag = "MTKlogSettings";

	NumberFormat nf = NumberFormat.getInstance(Locale.US);

	private static double txtByTd;
	private int txtByDi;
	private int txtBySi;
	private double txtByTds;
	private int txtByDis;
	private int txtBySis;

	private WebView HTMview;
	final boolean itmChk[] = new boolean[10];

	GPSrxtx gpsdev;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		MTKutility.debugWrite(132, "MTKhtmlSettings-onCreate()");
		contxt = getActivity();
		gpsdev = MTKutility.getBTconnect();
	}	//onCreate()

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		MTKutility.debugWrite(132, "MTKhtmlSettings-onCreateView()");
		// Inflate the layout for this fragment
		mV =  inflater.inflate(R.layout.mtkhtmlsettings, container, false);

		HTMview = (WebView) mV.findViewById(R.id.webView);

		wvbtnRec = (Button) mV.findViewById(R.id.wvbtnRec);
		wvbtnRec.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				showpopupRec();
			}
		});

		wvbtnMtd = (Button) mV.findViewById(R.id.wvbtnMtd);
		wvbtnMtd.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				showpopupRecMethod();
			}
		});

		wvbtnNav = (Button) mV.findViewById(R.id.wvbtnNav);
		wvbtnNav.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				showpopupNav();
			}
		});

		wvbtnSat = (Button) mV.findViewById(R.id.wvbtnSat);
		wvbtnSat.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				showpopupSat();
			}
		});

		wvbtnUTC = (Button) mV.findViewById(R.id.wvbtnUTC);
		wvbtnUTC.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				showpopupUTC();
			}
		});

		wvbtnDif = (Button) mV.findViewById(R.id.wvbtnDif);
		wvbtnDif.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				showpopupDiff();
			}
		});

		wvbtnDOP = (Button) mV.findViewById(R.id.wvbtnDOP);
		wvbtnDOP.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				showpopupDOP();
			}
		});

		wvbtnFreq = (Button) mV.findViewById(R.id.wvbtnFreq);
		wvbtnFreq.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				showpopupFreq();
			}
		});	

		wvbtnDflt = (Button) mV.findViewById(R.id.wvbtnDflt);
		wvbtnDflt.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				restoreDefaults();
			}
		});	

		wvbtnSave = (Button) mV.findViewById(R.id.wvbtnSave);
		wvbtnSave.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				saveChanges();
			}
		});

		wvbtnUTC.setTextSize(TypedValue.COMPLEX_UNIT_SP, MTKutility.htmlStngsBtns);
		wvbtnNav.setTextSize(TypedValue.COMPLEX_UNIT_SP, MTKutility.htmlStngsBtns);
		wvbtnRec.setTextSize(TypedValue.COMPLEX_UNIT_SP, MTKutility.htmlStngsBtns);
		wvbtnMtd.setTextSize(TypedValue.COMPLEX_UNIT_SP, MTKutility.htmlStngsBtns);
		wvbtnSat.setTextSize(TypedValue.COMPLEX_UNIT_SP, MTKutility.htmlStngsBtns);
		wvbtnDif.setTextSize(TypedValue.COMPLEX_UNIT_SP, MTKutility.htmlStngsBtns);
		wvbtnDOP.setTextSize(TypedValue.COMPLEX_UNIT_SP, MTKutility.htmlStngsBtns);
		wvbtnFreq.setTextSize(TypedValue.COMPLEX_UNIT_SP, MTKutility.htmlStngsBtns);
		wvbtnDflt.setTextSize(TypedValue.COMPLEX_UNIT_SP, MTKutility.htmlStngsBtns);
		wvbtnSave.setTextSize(TypedValue.COMPLEX_UNIT_SP, MTKutility.htmlStngsBtns);
		htmFS = Integer.toString(MTKutility.htmlStngsTxts);
		htmFSs = Integer.toString(MTKutility.htmlStngsTxts - 4);

		return mV;
	}	//onCreateView()

	@Override
	public void onPause() {
		super.onPause();
		MTKutility.debugWrite(132, "MTKhtmlSettings-onPause()");
		//		MTKutility.NMEAoutStart();
	}	//onPause()

	@Override
	public void onResume() {
		super.onResume();
		MTKutility.debugWrite(132, "MTKhtmlSettings-onResume()");
		gpsdev = MTKutility.getBTconnect();
		//check for BlueTooth connection
		if (!gpsdev.sock.isConnected()) {
			//re-establish the MTK logger connection
			if (!gpsdev.connect()){
				msg = "BlueTooth connection is missing";
				Toast.makeText(contxt, msg, Toast.LENGTH_LONG).show();
				MTKutility.debugWrite(132, "**** "+msg);
				return;
			}
		}
		//		MTKutility.NMEAoutStop();
		//		MTKutility.getPreferences();
		refreshTextValues();			
		refreshCheckboxes();
		HTMview.loadDataWithBaseURL(null, newHTML(), "text/html", "utf-8", null);
	}	//onResume()

	private void refreshTextValues() {
		MTKutility.debugWrite(132, "MTKhtmlSettings-refreshTextValues()");

		//get recording by time value
		parms = MTKutility.mtkCmd("PMTK182,2,3", "PMTK182,3,3", MTKutility.cmdTimeOut*bumpTimeOut);
		MTKutility.debugWrite(132, ">>>> MTKhtmlSettings mtkCmd parms =:" + Arrays.toString(parms));
		if (parms != null){
			txtByTd = Double.parseDouble(parms[3]) / 10.0;
			txtByTds = txtByTd;
		}

		//get recording by distance value
		parms = MTKutility.mtkCmd("PMTK182,2,4", "PMTK182,3,4", MTKutility.cmdTimeOut*bumpTimeOut);
		MTKutility.debugWrite(132, ">>>> MTKhtmlSettings mtkCmd parms =:" + Arrays.toString(parms));
		if (parms != null){
			txtByDi = Integer.valueOf(parms[3]) / 10;
			txtByDis = txtByDi;
		}

		//get recording by speed value
		parms = MTKutility.mtkCmd("PMTK182,2,5", "PMTK182,3,5", MTKutility.cmdTimeOut*bumpTimeOut);
		MTKutility.debugWrite(132, ">>>> MTKhtmlSettings mtkCmd parms =:" + Arrays.toString(parms));
		if (parms != null){
			txtBySi = Integer.valueOf(parms[3]) / 10;
			txtBySis = txtBySi;
		}
	}	//refreshTextValues()

	private void refreshCheckboxes() {
		MTKutility.debugWrite(132, "MTKhtmlSettings-refreshCheckboxes()");

		clearAllSettings();
		//get recording mode - stop/overwrite
		parms = MTKutility.mtkCmd("PMTK182,2,6", "PMTK182,3,6", MTKutility.cmdTimeOut*bumpTimeOut);
		MTKutility.debugWrite(132, ">>>> MTKhtmlSettings mtkCmd parms =:" + Arrays.toString(parms));
		if (parms != null){
			logMode = Integer.parseInt(parms[3]);
			logModes = logMode;
			switch (logMode){
			case 1:
				radOvrl = radiobtn;
				break;
			case 2:
				radStop = radiobtn;
				break;
			}
		}

		//get fields logged bitmask and set checkboxes
		parms = MTKutility.mtkCmd("PMTK182,2,2", "PMTK182,3,2", MTKutility.cmdTimeOut*bumpTimeOut);
		MTKutility.debugWrite(132, ">>>> MTKhtmlSettings mtkCmd parms =:" + Arrays.toString(parms));
		if (parms != null){
			logFldsMask = (int) Long.parseLong(parms[3], 16);
			logFldsMasks = logFldsMask;
			logRecLen = 0;

			if ((logFldsMask & cbxDateMask) == cbxDateMask) {
				cbxDate = checked; cbxDate();}

			if ((logFldsMask & cbxMiliMask) == cbxMiliMask) {
				cbxMili = checked; cbxMili();}

			if ((logFldsMask & cbxNsatMask) == cbxNsatMask) {
				cbxNsat = checked; cbxNsat();}

			if ((logFldsMask & cbxSIDMask) == cbxSIDMask) {
				cbxSID = checked; cbxSID();}

			if ((logFldsMask & cbxEleMask) == cbxEleMask) {
				cbxEle = checked; cbxEle();}

			if ((logFldsMask & cbxAziMask) == cbxAziMask) {
				cbxAzi = checked; cbxAzi();}

			if ((logFldsMask & cbxEleMask) == cbxEleMask) {
				cbxEle = checked; cbxEle();}

			if ((logFldsMask & cbxLatMask) == cbxLatMask) {
				cbxLat = checked; cbxLat();}

			if ((logFldsMask & cbxLonMask) == cbxLonMask) {
				cbxLon = checked; cbxLon();}

			if ((logFldsMask & cbxHeiMask) == cbxHeiMask) {
				cbxHei = checked; cbxHei();}

			if ((logFldsMask & cbxSpdMask) == cbxSpdMask) {
				cbxSpd = checked; cbxSpd();}

			if ((logFldsMask & cbxHedMask) == cbxHedMask) {
				cbxHed = checked; cbxHed();}

			if ((logFldsMask & cbxDisMask) == cbxDisMask) {
				cbxDis = checked; cbxDis();}

			if ((logFldsMask & cbxDSTAMask) == cbxDSTAMask) {
				cbxDSTA = checked; cbxDSTA();}

			if ((logFldsMask & cbxDAGEMask) == cbxDAGEMask) {
				cbxDAGE = checked; cbxDAGE();}

			if ((logFldsMask & cbxRCRMask) == cbxRCRMask) {
				cbxRCR = checked; cbxRCR();}

			if ((logFldsMask & cbxValMask) == cbxValMask) {
				cbxVal = checked; cbxVal();}

			if ((logFldsMask & cbxFxoMask) == cbxFxoMask) {
				cbxFxo = checked; cbxFxo();}

			if ((logFldsMask & cbxPDOPMask) == cbxPDOPMask) {
				cbxPDOP = checked; cbxPDOP();}

			if ((logFldsMask & cbxHDOPMask) == cbxHDOPMask) {
				cbxHDOP = checked; cbxHDOP();}

			if ((logFldsMask & cbxVDOPMask) == cbxVDOPMask) {
				cbxVDOP = checked; cbxVDOP();}
		}	
	}	//refreshCheckboxes()

	private void restoreDefaults() {
		MTKutility.debugWrite(132, "MTKhtmlSettings-saveChanges()");
		//time recording frequency
		parms = MTKutility.mtkCmd("PMTK182,1,3,50", "PMTK001,182", MTKutility.cmdTimeOut);
		//distance recording frequency
		parms = MTKutility.mtkCmd("PMTK182,1,4,0", "PMTK001,182", MTKutility.cmdTimeOut);
		//speed over recording frequency
		parms = MTKutility.mtkCmd("PMTK182,1,5,0", "PMTK001,182", MTKutility.cmdTimeOut);
		parms = MTKutility.mtkCmd("PMTK300,1000,0,0,0,0", "PMTK001,300", MTKutility.cmdTimeOut);
		//set Stop when full logging mode
		parms = MTKutility.mtkCmd("PMTK182,1,6,2", "PMTK001,182,1", MTKutility.cmdTimeOut);
		//send fields to record bitmask
		cmd = String.format("PMTK182,1,2,%S", Integer.toHexString(dfltBitmask));
		parms = MTKutility.mtkCmd(cmd, "PMTK001,182,1", MTKutility.cmdTimeOut);
		wvbtnSave.setTextColor(Color.BLACK);

		refreshTextValues();			
		refreshCheckboxes();
		HTMview.loadDataWithBaseURL(null, newHTML(), "text/html", "utf-8", null);
	}	//restoreDefaults()

	private void clearAllSettings() {
		cbxDate = spcFill;
		cbxMili = spcFill;
		cbxLat = spcFill;
		cbxLon = spcFill;
		cbxHei = spcFill;
		cbxSpd = spcFill;
		cbxHed = spcFill;
		cbxDis = spcFill;
		cbxRCR = spcFill;
		cbxVal = spcFill;
		cbxFxo = spcFill;
		cbxNsat = spcFill;
		cbxSID = spcFill;
		cbxEle = spcFill; 
		cbxAzi = spcFill;
		cbxSNR = spcFill;
		cbxDSTA = spcFill;
		cbxDAGE = spcFill; 
		cbxPDOP = spcFill;
		cbxHDOP = spcFill;
		cbxVDOP = spcFill;
		radOvrl = spcFill;
		radStop = spcFill;
	}	//clearAllSettings()

	private void saveChanges() {
		MTKutility.debugWrite(132, "MTKhtmlSettings-saveChanges()");
		boolean saved = false;

		if (txtByTds != txtByTd){
			txtByTstr = Integer.toString((int)(txtByTd * 10.0));
			cmd = String.format("PMTK182,1,3,%s", txtByTstr);
			parms = MTKutility.mtkCmd(cmd, "PMTK001,182", MTKutility.cmdTimeOut);
			if (txtByTd < 10){
				cmd = String.format("PMTK300,%s00,0,0,0,0", Integer.toString((int) txtByTd)); 
			}else{
				cmd = "PMTK300,1000,0,0,0,0";
			}
			parms = MTKutility.mtkCmd(cmd, "PMTK001,300", MTKutility.cmdTimeOut);
			txtByTds = txtByTd;
			saved = true;
		}

		if (txtByDis != txtByDi){
			txtByTstr = Integer.toString((int)(txtByDi * 10.0));
			cmd = String.format("PMTK182,1,4,%s", txtByTstr);
			parms = MTKutility.mtkCmd(cmd, "PMTK001,182", MTKutility.cmdTimeOut);
			txtByDis = txtByDi;
			saved = true;
		}

		if (txtBySis != txtBySi){
			txtByTstr = Integer.toString((int)(txtBySi * 10.0));
			cmd = String.format("PMTK182,1,5,%s", txtByTstr);
			parms = MTKutility.mtkCmd(cmd, "PMTK001,182", MTKutility.cmdTimeOut);
			txtBySis = txtBySi;
			saved = true;
		}

		if (logFldsMasks != logFldsMask){
			//send bitmask
			cmd = String.format("PMTK182,1,2,%S", Integer.toHexString(logFldsMask));
			parms = MTKutility.mtkCmd(cmd, "PMTK001,182,1", MTKutility.cmdTimeOut);
			logFldsMasks = logFldsMask;
			saved = true;
		}

		if (logModes != logMode){
			//send overwrite mode setting
			cmd = String.format("PMTK182,1,6,%S", Integer.toString(logMode));
			parms = MTKutility.mtkCmd(cmd, "PMTK001,182,1", MTKutility.cmdTimeOut);
			logModes = logMode;
			saved = true;
		}

		if (saved){
			//			refreshTextValues();			
			//			refreshCheckboxes();
			//			HTMview.loadDataWithBaseURL(null, newHTML(), "text/html", "utf-8", null);
			Toast.makeText(contxt, "changes have been saved", Toast.LENGTH_LONG).show();
		}else {
			Toast.makeText(contxt, "no changes to save", Toast.LENGTH_LONG).show();
		}
		wvbtnSave.setTextColor(Color.BLACK);
	}	//saveChanges()

	// the following methods are called when from MTKutility.onCheckboxClicked
	// they adjust the settings bitmask and log record length to reflect checked settings 
	static void cbxDate(){
		MTKutility.debugWrite(132, "MTKhtmlSettings-cbxDate()");
		//date / time - size I4
		if (cbxDate == checked){
			logFldsMask |= cbxDateMask;
			logRecLen += 6;
		}else {
			logFldsMask ^= cbxDateMask;
			logRecLen -= 6;
		}
	}	//cbxDate()

	static void cbxMili(){
		MTKutility.debugWrite(132, "MTKhtmlSettings-cbxMili()");
		//milliseconds - size U2
		if (cbxMili == checked){
			logFldsMask |= cbxMiliMask;
			logRecLen += 2;
		}else {
			logFldsMask ^= cbxMiliMask;
			logRecLen -= 2;
		}
	}	//cbxMili()

	static void cbxLat(){
		MTKutility.debugWrite(132, "MTKhtmlSettings-cbxLat()");
		//latitude - size R8
		if (cbxLat == checked){
			logFldsMask |= cbxLatMask;
			logRecLen += 8;
		}else {
			logFldsMask ^= cbxLatMask;
			logRecLen -= 8;
		}
	}	//cbxLat()

	static void cbxLon(){
		MTKutility.debugWrite(132, "MTKhtmlSettings-cbxLon()");
		//longitude - size R8
		if (cbxLon == checked){
			logFldsMask |= cbxLonMask;
			logRecLen += 8;
		}else {
			logFldsMask ^= cbxLonMask;
			logRecLen -= 8;
		}
	}	//cbxLon()

	static void cbxHei(){
		MTKutility.debugWrite(132, "MTKhtmlSettings-cbxHei()");
		//height - size R4
		if (cbxHei == checked){
			logFldsMask |= cbxHeiMask;
			logRecLen += 4;
		}else {
			logFldsMask ^= cbxHeiMask;
			logRecLen -= 4;
		}
	}	//cbxHei()

	static void cbxSpd(){
		MTKutility.debugWrite(132, "MTKhtmlSettings-cbxSpd()");
		//speed - size R4
		if (cbxSpd == checked){
			logFldsMask |= cbxSpdMask;
			logRecLen += 4;
		}else {
			logFldsMask ^= cbxSpdMask;
			logRecLen -= 4;
		}
	}	//cbxSpd()

	static void cbxHed(){
		MTKutility.debugWrite(132, "MTKhtmlSettings-cbxHed()");
		//heading - size R4
		if (cbxHed == checked){
			logFldsMask |= cbxHedMask;
			logRecLen += 4;
		}else {
			logFldsMask ^= cbxHedMask;
			logRecLen -= 4;
		}
	}	//cbxHed()

	static void cbxDis(){
		MTKutility.debugWrite(132, "MTKhtmlSettings-cbxDis()");
		//distance - size 8??
		if (cbxDis == checked){
			logFldsMask |= cbxDisMask;
			logRecLen += 8;
		}else {
			logFldsMask ^= cbxDisMask;
			logRecLen -= 8;
		}
	}	//cbxDis()

	static void cbxRCR(){
		MTKutility.debugWrite(132, "MTKhtmlSettings-cbxRCR()");
		//reason for recorded track point - size U2
		if (cbxRCR == checked){
			logFldsMask |= cbxRCRMask;
			logRecLen += 2;
		}else {
			logFldsMask ^= cbxRCRMask;
			logRecLen -= 2;
		}
	}	//cbxRCR()

	static void cbxVal(){
		MTKutility.debugWrite(132, "MTKhtmlSettings-cbxVal()");
		//fix mode - SPS, DGPS, etc. - size U2
		if (cbxVal == checked){
			logFldsMask |= cbxValMask;
			logRecLen += 2;
		}else {
			logFldsMask ^= cbxValMask;
			logRecLen -= 2;
		}
	}	//cbxVal()

	static void cbxFxo(){
		MTKutility.debugWrite(132, "MTKhtmlSettings-cbxFxo()");
		//only record track points that have a satellite fix
		if (cbxFxo == checked){
			logFldsMask |= cbxFxoMask;
			//does not affect log record length
		}else {
			logFldsMask ^= cbxFxoMask;
		}
	}	//cbxFxo()

	static void cbxNsat(){
		MTKutility.debugWrite(132, "MTKhtmlSettings-cbxNsat()");
		//number of satellites - size U2
		if (cbxNsat == checked){
			logFldsMask |= cbxNsatMask;
			logRecLen += 2;
		}else {
			logFldsMask ^= cbxNsatMask;
			logRecLen -= 2;
		}
	}	//cbxNsat()

	static void cbxSID(){
		MTKutility.debugWrite(132, "MTKhtmlSettings-cbxSID()");
		//satellite id - size U4
		if (cbxSID == checked){
			logFldsMask |= cbxSIDMask;
			logRecLen += 4;
		}else {
			logFldsMask ^= cbxSIDMask;
			logRecLen -= 4;
		}
	}	//cbxSID()

	static void cbxEle(){
		MTKutility.debugWrite(132, "MTKhtmlSettings-cbxEle()");
		//satellite elevation - size I2
		if (cbxEle == checked){
			logFldsMask |= cbxEleMask;
			logRecLen += 2;
		}else {
			logFldsMask ^= cbxEleMask;
			logRecLen -= 2;
		}
	}//cbxEle()

	static void cbxAzi(){
		MTKutility.debugWrite(132, "MTKhtmlSettings-cbxAzi()");
		//satellite azimuth - size U2
		if (cbxAzi == checked){
			logFldsMask |= cbxAziMask;
			logRecLen += 2;
		}else {
			logFldsMask ^= cbxAziMask;
			logRecLen -= 2;
		}
	}//cbxAzi()

	static void cbxSNR(){
		MTKutility.debugWrite(132, "MTKhtmlSettings-cbxSNR()");
		//satellite number - size U2
		if (cbxSNR == checked){
			logFldsMask |= cbxSNRMask;
			logRecLen += 2;
		}else {
			logFldsMask ^= cbxSNRMask;
			logRecLen -= 2;
		}
	}//cbxSNR()

	static void cbxDSTA(){
		MTKutility.debugWrite(132, "MTKhtmlSettings-cbxDSTA()");
		//differential reference station id - size U2
		if (cbxDSTA == checked){
			logFldsMask |= cbxDSTAMask;
			logRecLen += 2;
		}else {
			logFldsMask ^= cbxDSTAMask;
			logRecLen -= 2;
		}
	}//cbxDSTA()

	static void cbxDAGE(){
		MTKutility.debugWrite(132, "MTKhtmlSettings-cbxDAGE()");
		//age of differential data in seconds - size R4
		if (cbxDAGE == checked){
			logFldsMask |= cbxDAGEMask;
			logRecLen += 4;
		}else {
			logFldsMask ^= cbxDAGEMask;
			logRecLen -= 4;
		}
	}//cbxDAGE()

	static void cbxPDOP(){
		MTKutility.debugWrite(132, "MTKhtmlSettings-cbxPDOP()");
		//position dilution of precision (m) - size U2
		if (cbxPDOP == checked){
			logFldsMask |= cbxPDOPMask;
			logRecLen += 4;
		}else {
			logFldsMask ^= cbxPDOPMask;
			logRecLen -= 4;
		}
	}//cbxPDOP()

	static void cbxHDOP(){
		MTKutility.debugWrite(132, "MTKhtmlSettings-cbxHDOP()");
		//horizontal dilution of precision (m) - size U2
		if (cbxHDOP == checked){
			logFldsMask |= cbxHDOPMask;
			logRecLen += 4;
		}else {
			logFldsMask ^= cbxHDOPMask;
			logRecLen -= 4;}
	}//cbxHDOP()

	static void cbxVDOP(){
		MTKutility.debugWrite(132, "MTKhtmlSettings-cbxVDOP()");
		//vertical dilution of precision (m) - size U2
		if (cbxVDOP == checked){
			logFldsMask |= cbxVDOPMask;
			logRecLen += 4;
		}else {
			logFldsMask ^= cbxVDOPMask;
			logRecLen -= 4;}
	}//cbxVDOP()
	// end of settings methods

	private void showpopupUTC() {
		MTKutility.debugWrite(132, "MTKhtmlSettings-showpopupUTC()");
		final CharSequence[] items={"Date/time","Mili-second"};
		boolean [] setChk = new boolean[items.length];

		for (int i = 0; i < items.length; i++) {setChk[i] = false; itmChk[i]=false;}

		if (cbxDate == checked){setChk[0] = true; itmChk[0] = true;}
		if (cbxMili == checked){setChk[1] = true; itmChk[1] = true;}

		AlertDialog.Builder builder=new AlertDialog.Builder(contxt);
		builder.setTitle("UTC")
		.setPositiveButton("Save", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				saveUTC();
			}
		})
		.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				//do nothing
			}
		})
		.setMultiChoiceItems(items,setChk, new DialogInterface.OnMultiChoiceClickListener() {
			//			@Override
			@Override
			public void onClick(DialogInterface dialog, int which, boolean isChecked) {
				itmChk[which]=isChecked;	
			}
		})
		.show();
	} 	//showpopupUTC()

	private void saveUTC() {
		if ((itmChk[0]) && (cbxDate != checked)){cbxDate = checked;setSve();cbxDate();}
		if ((!itmChk[0]) && (cbxDate == checked)){cbxDate = spcFill;setSve();cbxDate();}

		if ((itmChk[1]) && (cbxMili != checked)){cbxMili = checked;setSve();cbxMili();}
		if ((!itmChk[1]) && (cbxMili == checked)){cbxMili = spcFill;setSve();cbxMili();}

		HTMview.loadDataWithBaseURL(null, newHTML(), "text/html", "utf-8", null);
	} 	//saveUTC()

	private void showpopupNav() {
		MTKutility.debugWrite(132, "MTKhtmlSettings-showpopupNav()");
		final CharSequence[] items={"Latitude","Longitude","Height","Speed","Heading","Distance"};
		boolean [] setChk = new boolean[items.length];

		for (int i = 0; i < items.length; i++) {setChk[i] = false; itmChk[i]=false;}

		if (cbxLat == checked){setChk[0] = true; itmChk[0] = true;}
		if (cbxLon == checked){setChk[1] = true; itmChk[1] = true;}
		if (cbxHei == checked){setChk[2] = true; itmChk[2] = true;}
		if (cbxSpd == checked){setChk[3] = true; itmChk[3] = true;}
		if (cbxHed == checked){setChk[4] = true; itmChk[4] = true;}
		if (cbxDis == checked){setChk[5] = true; itmChk[5] = true;}

		AlertDialog.Builder builder=new AlertDialog.Builder(contxt);
		builder.setTitle("Navigation")
		.setPositiveButton("Save", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				saveNav();
			}
		})
		.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				//do nothing
			}
		})
		.setMultiChoiceItems(items,setChk, new DialogInterface.OnMultiChoiceClickListener() {
			//			@Override
			@Override
			public void onClick(DialogInterface dialog, int which, boolean isChecked) {
				itmChk[which]=isChecked;	
			}
		})
		.show();
	} 	//showpopupNav()

	private void saveNav() {
		if ((itmChk[0]) && (cbxLat != checked)){cbxLat = checked;setSve();cbxLat();}
		if ((!itmChk[0]) && (cbxLat == checked)){cbxLat = spcFill;setSve();cbxLat();}

		if ((itmChk[1]) && (cbxLon != checked)){cbxLon = checked;setSve();cbxLon();}
		if ((!itmChk[1]) && (cbxLon == checked)){cbxLon = spcFill;setSve();cbxLon();}

		if ((itmChk[2]) && (cbxHei != checked)){cbxHei = checked;setSve();cbxHei();}
		if ((!itmChk[2]) && (cbxHei == checked)){cbxHei = spcFill;setSve();cbxHei();}

		if ((itmChk[3]) && (cbxSpd != checked)){cbxSpd = checked;setSve();cbxSpd();}
		if ((!itmChk[3]) && (cbxSpd == checked)){cbxSpd = spcFill;setSve();cbxSpd();}

		if ((itmChk[4]) && (cbxHed != checked)){cbxHed = checked;setSve();cbxHed();}
		if ((!itmChk[4]) && (cbxHed == checked)){cbxHed = spcFill;setSve();cbxHed();}

		if ((itmChk[5]) && (cbxDis != checked)){cbxDis = checked;setSve();cbxDis();}
		if ((!itmChk[5]) && (cbxDis == checked)){cbxDis = spcFill;setSve();cbxDis();}

		HTMview.loadDataWithBaseURL(null, newHTML(), "text/html", "utf-8", null);
	} 	//saveNav()

	private void showpopupRec() {
		MTKutility.debugWrite(132, "MTKhtmlSettings-showpopupRec()");
		final CharSequence[] items={"Reason","Fix mode","Fix only"};
		boolean [] setChk = new boolean[items.length];

		for (int i = 0; i < items.length; i++) {setChk[i] = false; itmChk[i]=false;}

		if (cbxRCR == checked){setChk[0] = true; itmChk[0] = true;}
		if (cbxVal == checked){setChk[1] = true; itmChk[1] = true;}
		if (cbxFxo == checked){setChk[2] = true; itmChk[2] = true;}

		AlertDialog.Builder builder=new AlertDialog.Builder(contxt);
		builder.setTitle("Recording")
		.setPositiveButton("Save", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				saveRec();
			}
		})
		.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				//do nothing
			}
		})
		.setMultiChoiceItems(items,setChk, new DialogInterface.OnMultiChoiceClickListener() {
			//			@Override
			@Override
			public void onClick(DialogInterface dialog, int which, boolean isChecked) {
				itmChk[which]=isChecked;	
			}
		})
		.show();
	} 	//showpopupRec()

	private void saveRec() {
		if ((itmChk[0]) && (cbxRCR != checked)){cbxRCR = checked;setSve();cbxRCR();}
		if ((!itmChk[0]) && (cbxRCR == checked)){cbxRCR = spcFill;setSve();cbxRCR();}

		if ((itmChk[1]) && (cbxVal != checked)){cbxVal = checked;setSve();cbxVal();}
		if ((!itmChk[1]) && (cbxVal == checked)){cbxVal = spcFill;setSve();cbxDis();}

		if ((itmChk[2]) && (cbxFxo != checked)){cbxFxo = checked;setSve();cbxFxo();}
		if ((!itmChk[2]) && (cbxFxo == checked)){cbxFxo = spcFill;setSve();cbxFxo();}

		HTMview.loadDataWithBaseURL(null, newHTML(), "text/html", "utf-8", null);
	} 	//saveRec()

	private void showpopupRecMethod() {
		MTKutility.debugWrite(132, "MTKhtmlSettings-showpopupRecMethod()");
		final CharSequence[] items={"Overwrite","Stop"};
		int setChk = -1;

		if (radOvrl == radiobtn){setChk = 0;}
		if (radStop == radiobtn){setChk = 1;}

		AlertDialog.Builder builder=new AlertDialog.Builder(contxt);
		builder.setTitle("When memory is full")
		.setPositiveButton("Save", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				saveRecMethod();
			}
		})
		.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				//do nothing
			}
		})
		.setSingleChoiceItems(items,setChk, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				logMode = which+1;	
			}
		})
		.show();
	} 	//showpopupRecMethod()

	private void saveRecMethod() {
		switch (logMode){
		case 1:
			if (radOvrl != radiobtn){
				radOvrl = radiobtn;
				radStop = spcFill;
				setSve();
				break;
			}
		case 2:
			if (radStop != radiobtn){
				radStop = radiobtn;
				radOvrl = spcFill;
				setSve();
				break;
			}
		}

		HTMview.loadDataWithBaseURL(null, newHTML(), "text/html", "utf-8", null);
	} 	//saveRecMethod()

	private void showpopupSat() {
		MTKutility.debugWrite(132, "MTKhtmlSettings-showpopupSat()");
		final CharSequence[] items={"NSAT","SID","Elevation","Azimuth","SNR"};
		boolean [] setChk = new boolean[items.length];
		msg = "SID must be checked first";

		for (int i = 0; i < items.length; i++) {setChk[i] = false; itmChk[i]=false;}

		if (cbxNsat == checked){setChk[0] = true; itmChk[0] = true;}
		if (cbxSID  == checked){setChk[1] = true; itmChk[1] = true;}
		if (cbxEle  == checked){setChk[2] = true; itmChk[2] = true;}
		if (cbxAzi  == checked){setChk[3] = true; itmChk[3] = true;}
		if (cbxSNR  == checked){setChk[4] = true; itmChk[4] = true;}

		AlertDialog.Builder builder=new AlertDialog.Builder(contxt);
		builder.setTitle("Satellite info")
		.setPositiveButton("Save", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				saveSat();
			}
		})
		.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				//do nothing
			}
		})
		.setMultiChoiceItems(items,setChk, new DialogInterface.OnMultiChoiceClickListener() {
			//			@Override
			@Override
			public void onClick(DialogInterface dialog, int which, boolean isChecked) {
				switch (which){
				case 0:
					itmChk[which]=isChecked;
					break;
				case 1:
					itmChk[which]=isChecked;
					SIDchecked = isChecked;
					if (!isChecked){
						itmChk[2]=false;
						itmChk[3]=false;
						itmChk[4]=false;
					}
					break;
				default:
					if (SIDchecked){
						itmChk[which]=isChecked;
					}else {
						Toast.makeText(contxt, msg, Toast.LENGTH_SHORT).show();
					}
					break;
				}
			}
		})
		.show();
	} 	//showpopupSat()

	private void saveSat() {
		if ((itmChk[0]) && (cbxNsat != checked)){cbxNsat = checked;setSve();cbxNsat();}
		if ((!itmChk[0]) && (cbxNsat == checked)){cbxNsat = spcFill;setSve();cbxNsat();}

		if ((itmChk[1]) && (cbxSID != checked)){cbxSID = checked;setSve();cbxSID();}
		if ((!itmChk[1]) && (cbxSID == checked)){cbxSID = spcFill;setSve();cbxSID();}

		if ((itmChk[2]) && (cbxEle != checked)){cbxEle = checked;setSve();cbxEle();}
		if ((!itmChk[2]) && (cbxEle == checked)){cbxEle = spcFill;setSve();cbxEle();}

		if ((itmChk[3]) && (cbxAzi != checked)){cbxAzi = checked;setSve();cbxAzi();}
		if ((!itmChk[3]) && (cbxAzi == checked)){cbxAzi = spcFill;setSve();cbxAzi();}

		if ((itmChk[4]) && (cbxSNR != checked)){cbxSNR = checked;setSve();cbxSNR();}
		if ((!itmChk[4]) && (cbxSNR == checked)){cbxSNR = spcFill;setSve();cbxSNR();}

		HTMview.loadDataWithBaseURL(null, newHTML(), "text/html", "utf-8", null);
	} 	//saveSat()

	private void showpopupDiff() {
		MTKutility.debugWrite(132, "MTKhtmlSettings-showpopupDiff()");
		final CharSequence[] items={"DSTA","DAGE"};
		boolean [] setChk = new boolean[items.length];

		for (int i = 0; i < items.length; i++) {setChk[i] = false; itmChk[i]=false;}

		if (cbxDSTA == checked){setChk[0] = true; itmChk[0] = true;}
		if (cbxDAGE == checked){setChk[1] = true; itmChk[1] = true;}

		AlertDialog.Builder builder=new AlertDialog.Builder(contxt);
		builder.setTitle("Differential")
		.setPositiveButton("Save", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				saveDiff();
			}
		})
		.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				//do nothing
			}
		})
		.setMultiChoiceItems(items,setChk, new DialogInterface.OnMultiChoiceClickListener() {
			//			@Override
			@Override
			public void onClick(DialogInterface dialog, int which, boolean isChecked) {
				itmChk[which]=isChecked;	
			}
		})
		.show();
	} 	//showpopupDiff()

	private void saveDiff() {
		if ((itmChk[0]) && (cbxDSTA != checked)){cbxDSTA = checked;setSve();cbxDSTA();}
		if ((!itmChk[0]) && (cbxDSTA == checked)){cbxDSTA = spcFill;setSve();cbxDSTA();}

		if ((itmChk[1]) && (cbxDAGE != checked)){cbxDAGE = checked;setSve();cbxDAGE();}
		if ((!itmChk[1]) && (cbxDAGE == checked)){cbxDAGE = spcFill;setSve();cbxDAGE();}

		HTMview.loadDataWithBaseURL(null, newHTML(), "text/html", "utf-8", null);
	} 	//saveDiff()

	private void showpopupDOP() {
		MTKutility.debugWrite(132, "MTKhtmlSettings-showpopupDOP()");
		final CharSequence[] items={"PDOP","HDOP","VDOP"};
		boolean [] setChk = new boolean[items.length];

		for (int i = 0; i < items.length; i++) {setChk[i] = false; itmChk[i]=false;}

		if (cbxPDOP == checked){setChk[0] = true; itmChk[0] = true;}
		if (cbxHDOP == checked){setChk[1] = true; itmChk[1] = true;}
		if (cbxVDOP == checked){setChk[2] = true; itmChk[2] = true;}

		AlertDialog.Builder builder=new AlertDialog.Builder(contxt);
		builder.setTitle("Degree of Precision")
		.setPositiveButton("Save", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				saveDOP();
			}
		})
		.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				//do nothing
			}
		})
		.setMultiChoiceItems(items,setChk, new DialogInterface.OnMultiChoiceClickListener() {
			//			@Override
			@Override
			public void onClick(DialogInterface dialog, int which, boolean isChecked) {
				itmChk[which]=isChecked;	
			}
		})
		.show();
	} 	//showpopupDOP()

	private void saveDOP() {
		if ((itmChk[0]) && (cbxPDOP != checked)){cbxPDOP = checked;setSve();cbxPDOP();}
		if ((!itmChk[0]) && (cbxPDOP == checked)){cbxPDOP = spcFill;setSve();cbxPDOP();}

		if ((itmChk[1]) && (cbxHDOP != checked)){cbxHDOP = checked;setSve();cbxHDOP();}
		if ((!itmChk[1]) && (cbxHDOP == checked)){cbxHDOP = spcFill;setSve();cbxHDOP();}

		if ((itmChk[2]) && (cbxVDOP != checked)){cbxVDOP = checked;setSve();cbxVDOP();}
		if ((!itmChk[2]) && (cbxVDOP == checked)){cbxVDOP = spcFill;setSve();cbxVDOP();}

		HTMview.loadDataWithBaseURL(null, newHTML(), "text/html", "utf-8", null);
	} 	//saveDOP()

	private void showpopupFreq() {
		MTKutility.debugWrite(132, "MTKhtmlSettings-showpopupFreq()");

		String sVal;
		// get prompts.xml view
		LayoutInflater layoutInflater = LayoutInflater.from(contxt);
		View promptView = layoutInflater.inflate(R.layout.freqpopup, null);

		final EditText txtByT = (EditText) promptView.findViewById(R.id.txtByT);
		final EditText txtByD = (EditText) promptView.findViewById(R.id.txtByD);
		final EditText txtByS = (EditText) promptView.findViewById(R.id.txtByS);
		//		dVal = txtByTd / 10.0;
		sVal = String.format(Locale.CANADA,"%.1f", txtByTd);
		txtByT.setText(sVal);

		//		val = txtByDi / 10;
		sVal = Integer.toString(txtByDi);
		txtByD.setText(sVal);

		//		val = txtBySi / 10;
		sVal = Integer.toString(txtBySi);
		txtByS.setText(sVal);

		// setup a dialog window
		AlertDialog.Builder builder=new AlertDialog.Builder(contxt);
		builder.setCancelable(false)
		.setView(promptView)
		.setPositiveButton("Save", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int id) {
				setSve();
				// get user input and set it to result
				double dtxtByT;
				int itxtByD;
				int itxtByS;
				String msg;
				txtByTstr = txtByT.getText().toString().replace(",",".");
				dtxtByT = Double.parseDouble(txtByTstr);
				if ((dtxtByT != 0.0)&&((dtxtByT < 0.1)||(dtxtByT > 999.0))){
					msg = "by time is invald: must be <=999.0 & >=0.1";
					Toast.makeText(contxt, msg, Toast.LENGTH_LONG).show();
					return;}

				txtByDstr = txtByD.getText().toString();
				itxtByD = Integer.parseInt(txtByDstr);
				if ((itxtByD != 0)&&((itxtByD < 1)||(itxtByD > 9999))){
					msg = "by distance is invald: must be <=9999 & >=10";
					Toast.makeText(contxt, msg, Toast.LENGTH_LONG).show();
					return;}

				txtBySstr = txtByS.getText().toString();
				itxtByS = Integer.parseInt(txtBySstr);
				if ((itxtByS != 0)&&((itxtByS < 1)||(itxtByS > 999))){
					msg = "speed over is invald: must be <=999 & >=1";
					Toast.makeText(contxt, msg, Toast.LENGTH_LONG).show();
					return;}

				txtByTd = dtxtByT;
				txtByDi = itxtByD;
				txtBySi = itxtByS;
				HTMview.loadDataWithBaseURL(null, newHTML(), "text/html", "utf-8", null);
			}
		})
		.setNegativeButton("Cancel",
				new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog,	int id) {
				dialog.cancel();
			}
		})
		.show();
	} 	//showpopupFreq()

	private void setSve() {
		wvbtnSave.setTextColor(Color.RED);
	}//setSve()

	private String newHTML(){
		MTKutility.debugWrite(132, "MTKhtmlSettings-newHTML()");

		StringBuilder sb = new StringBuilder();
		String bmStrng = "Bitmask: " + String.format("%8S", Integer.toHexString(logFldsMask)).replace(' ', '0');
		String lrStrng;
		int days;
		int hours;
		if ((logRecLen > 0) && (MTKutility.flashSize > 0)){
			maxRecords = MTKutility.flashSize / logRecLen;
			maxTime = maxRecords * txtByTd;
			hours = (int) maxTime / 3600;
			days = hours / 24;
			hours = hours - (days * 24);
			lrStrng = Integer.toString(logRecLen) + " Bytes/record  Max:" +
					NumberFormat.getInstance().format(maxRecords) +
					" records<br>Time:" + Integer.toString(days) + " days " +
					Integer.toString(hours) + " hours";			
		}else {
			lrStrng = Integer.toString(logRecLen) + " Bytes/record";
		}

		int tablewidth = (MTKutility.screenDPI/3) * (MTKutility.screenWidth/MTKutility.screenDPI);
		String tPX = Integer.toString(tablewidth) +"px;";
		MTKutility.debugWrite(132, String.format("screenWidth=%s - tablewidth=%s", MTKutility.screenWidth, tablewidth));

		sb.append("<html><head>"); 
		sb.append("<style>td {font-size:"+htmFS+"px; vertical-align:top;}</style>");
		sb.append("</head><body>");
		sb.append("<table style=\"text-align: left;\" border=\"1\" cellpadding=\"0\" cellspacing=\"0\"><colgroup><col span=\"3\" style=width:"+tPX+"></colgroup>");

		sb.append("<td style=\"vertical-align: top;\">Recording<br>");
		sb.append("<table style=\"text-align: left; width: 100%;\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\"><tbody>");
		sb.append("<tr><td style=\"vertical-align: top;\">"+ cbxRCR +"</td>");
		sb.append("<td style=\"vertical-align: top;\">Reason<br></td></tr>");
		sb.append("<tr><td style=\"vertical-align: top;\">"+ cbxVal +"</td>");
		sb.append("<td style=\"vertical-align: top;\">Fix mode<br></td></tr>");
		sb.append("<tr><td style=\"vertical-align: top;\">"+ cbxFxo +"</td>");
		sb.append("<td style=\"vertical-align: top;\">Fix only<br></td></tr>");
		sb.append("<tr><td colspan=\"2\" style=\"vertical-align: top;\">Method<br></td></tr>");
		sb.append("<tr><td style=\"vertical-align: top;\">"+ radOvrl +"<br>");
		sb.append("</td><td style=\"vertical-align: top;\">Overwrite<br></td></tr>");
		sb.append("<tr><td style=\"vertical-align: top;\">"+ radStop +"<br></td>");
		sb.append("<td style=\"vertical-align: top;\">Stop<br></td></tr>");
		sb.append("</tbody></table></td>");

		sb.append("<td style=\"vertical-align: top;\">Navigation<br>");
		sb.append("<table style=\"text-align: left; width: 100%;\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\"><tbody>");
		sb.append("<tr><td style=\"vertical-align: top;\">"+ cbxLat +"</td>");
		sb.append("<td style=\"vertical-align: top;\">Latitude<br></td></tr>");
		sb.append("<tr><td style=\"vertical-align: top;\">"+ cbxLon +"</td>");
		sb.append("<td style=\"vertical-align: top;\">Longitude<br></td></tr>");
		sb.append("<tr><td style=\"vertical-align: top;\">"+ cbxHei +"</td>");
		sb.append("<td style=\"vertical-align: top;\">Height<br></td></tr>");
		sb.append("<tr><td style=\"vertical-align: top;\">"+ cbxSpd +"<br></td>");
		sb.append("<td style=\"vertical-align: top;\">Speed<br></td></tr>");
		sb.append("<tr><td style=\"vertical-align: top;\">"+ cbxHed +"<br></td>");
		sb.append("<td style=\"vertical-align: top;\">Heading<br></td></tr>");
		sb.append("<tr><td style=\"vertical-align: top;\">"+ cbxDis +"<br></td>");
		sb.append("<td style=\"vertical-align: top;\">Distance<br></td></tr>");
		sb.append("</tbody></table></td>");

		sb.append("<td>Sat info");
		sb.append("<table style=\"text-align: left; width: 100%;\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\"><tbody>");
		sb.append("<tr><td>"+ cbxNsat +"<br></td>");
		sb.append("<td colspan=\"2\">NSAT<br></td></tr>");
		sb.append("<tr><td>"+ cbxSID +"<br></td>");
		sb.append("<td colspan=\"2\">SID<br></td></tr>");
		sb.append("<tr><td><br></td>");
		sb.append("<td>"+ cbxEle +"<br>	</td>");
		sb.append("<td>Elevation<br></td></tr>");
		sb.append("<tr><td><br></td>");
		sb.append("<td>"+ cbxAzi +"<br></td>");
		sb.append("<td>Azimuth<br></td></tr>");
		sb.append("<tr><td><br></td>");
		sb.append("<td>"+ cbxSNR +"<br></td>");
		sb.append("<td>SNR<br></td></tr>");
		sb.append("</tbody></table></td></tr>");

		sb.append("<tr><td style=\"vertical-align: top;\"> UTC");
		sb.append("<table style=\"text-align: left; width: 100%;\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\"><tbody>");
		sb.append("<tr><td style=\"vertical-align: top;\">"+ cbxDate +"</td>");
		sb.append("<td style=\"vertical-align: top;\">Date/time<br></td></tr>");
		sb.append("<tr><td style=\"vertical-align: top;\">"+ cbxMili +"<br></td>");
		sb.append("<td style=\"vertical-align: top;\">Mili-second<br></td></tr>");
		sb.append("</tbody></table></td>");

		sb.append("<td style=\"vertical-align: top;\">Differential<br>");
		sb.append("<table style=\"text-align: left; width: 100%;\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\"><tbody>");
		sb.append("<tr><td style=\"vertical-align: top;\">"+ cbxDSTA +"<br></td>");
		sb.append("<td style=\"vertical-align: top;\">DSTA<br></td></tr>");
		sb.append("<tr><td style=\"vertical-align: top;\">"+ cbxDAGE +"<br></td>");
		sb.append("<td style=\"vertical-align: top;\">DAGE<br></td></tr>");
		sb.append("</tbody></table></td>");

		sb.append("<td style=\"vertical-align: top;\">DOP<br>");
		sb.append("<table style=\"text-align: left; width: 100%;\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\"><tbody>");
		sb.append("<tr><td style=\"vertical-align: top;\">"+ cbxPDOP +"<br></td>");
		sb.append("<td style=\"vertical-align: top;\">PDOP<br></td></tr>");
		sb.append("<tr><td style=\"vertical-align: top;\">"+ cbxHDOP +"<br></td>");
		sb.append("<td style=\"vertical-align: top;\">HDOP<br></td></tr>");
		sb.append("<tr><td style=\"vertical-align: top;\">"+ cbxVDOP +"<br></td>");
		sb.append("<td style=\"vertical-align: top;\">VDOP<br></td></tr>");
		sb.append("</tbody></table></td></tr>");

		sb.append("<tr align=\"center\"><td colspan=\"3\" style=\"vertical-align: top;\" >Recording frequency<br></td></tr>");
		sb.append("<tr><td style=\"vertical-align: top;\">time:" + String.format(Locale.CANADA,"%.1f", txtByTd) + " sec<br></td>");
		sb.append("<td style=\"vertical-align: top;\">dist:" + Integer.toString(txtByDi) +" m.<br></td>");
		sb.append("<td style=\"vertical-align: top;\">speed:"+ Integer.toString(txtBySi) + " km/h<br></td></tr>");
		sb.append("<tr align=\"center\"><td colspan=\"3\" style=\"vertical-align: top;\">"+ lrStrng +"<br></td></tr>");
		sb.append("<tr align=\"center\" border=\"1px\"><td colspan=\"3\" style=\"font-size:"+htmFSs+"px; vertical-align: top;\">"+ bmStrng +"<br></td></tr>");
		sb.append("</tbody></table></body>");

		return sb.toString();
	}//newHTML()
}