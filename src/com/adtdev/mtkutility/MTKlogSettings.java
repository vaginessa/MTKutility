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
import java.util.Locale;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

public class MTKlogSettings extends Fragment {
	private final static String BR = System.getProperty("line.separator");

	//layout inflater values
	private View mV;
	private Button btnDflt;  //default settings
	private Button btnClrA;  //clear all
	private Button btnRefr;  //refresh - read MTK logger fields bitmap
	private Button btnRefr2; //refresh - read MTK logger EditText values
	private Button btnSet;   //write fields bitmap to MTK logger
	private Button btnSave;  //write EditText values to MTK logger
	private int epoinfofont = 15;
	private int btnBARfont = 20;

	//views that have the width set by code
	private View frame1;
	private View frame2;
	private View frame3;

	//checkboxes
	private static CheckBox cbxDate; //date / time
	private static CheckBox cbxMili; //milliseconds
	private static CheckBox cbxLat; //latitude
	private static CheckBox cbxLon; //longitude
	private static CheckBox cbxHei; //height
	private static CheckBox cbxSpd; //speed
	private static CheckBox cbxHed; //heading
	private static CheckBox cbxDis; //distance
	private static CheckBox cbxRCR; //reason for recorded track point
	private static CheckBox cbxVal; //fix mode - SPS, DGPS, etc.
	private static CheckBox cbxFxo; //only record track points that have a satellite fix
	private static CheckBox cbxNsat; //number of satellites
	private static CheckBox cbxSID; //satellite id
	private static CheckBox cbxEle; //satellite elevation 
	private static CheckBox cbxAzi; //satellite azimuth
	private static CheckBox cbxSNR; //satellite number
	private static CheckBox cbxDSTA; //differential reference station id
	private static CheckBox cbxDAGE; //age of differential data in seconds 
	private static CheckBox cbxPDOP; //position dilution of precision (m)
	private static CheckBox cbxHDOP; //horizontal dilution of precision (m)
	private static CheckBox cbxVDOP; //vertical dilution of precision (m)

	//textviews
	private static TextView logMSG;  //message field to display record size and logger capacity
	private static TextView txvBitm; //message field to display log settings bitmask

	//EditText
	private static EditText txtByT;
	private static EditText txtByD;
	private static EditText txtByS;

	//RadioButtons
	private static RadioButton rbnOvr; //record mode radio button - overwrite (wrap recording)
	private static RadioButton rbnStp; //record mode radio button - stop when memory is full

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

	static int logFormat = 0x00000000;
	static int logRecLen = 0;
	static int flashSize;
	static int maxRecords;
	static double maxTime;
	static int logRecCount;
	static int intVal;
	static double dVal;
	static int MTKlogFormat;
	static String MTKtxtByT;
	static String MTKtxtByD;
	static String MTKtxtByS;
	static int MTKrbtnVal;
	private String cmd;
	static String txtByTstr;
	static String txtByDstr;
	static String txtBySstr;
	private String msg;
	private String[] parms;

	static DecimalFormat TimeFmt = new DecimalFormat("#0.00");
	static String tag = "MTKlogSettings";

	NumberFormat nf = NumberFormat.getInstance(Locale.US);

	GPSrxtx gpsdev;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		MTKutility.debugWrite(132, "MTKlogSettings-onCreate()");
		gpsdev = MTKutility.getBTconnect();
	}	//onCreate()

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		MTKutility.debugWrite(132, "MTKlogSettings-onCreateView()");
		SharedPreferences sharedPref = MTKutility.getSharedPreferences();
		epoinfofont = Integer.valueOf(sharedPref.getString("mainMSGblock", "15"));
		btnBARfont = Integer.valueOf(sharedPref.getString("mainBTNbar", "20"));		

		// Inflate the layout for this fragment
		mV =  inflater.inflate(R.layout.mtklogsettings, container, false);

		btnDflt = (Button) mV.findViewById(R.id.btnDflt);
		btnDflt.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				MTKutility.debugWrite(132, "MTKlogSettings -button " + btnDflt.getText() + " pressed");
				setDefaultSettings();
			}
		});

		btnClrA = (Button) mV.findViewById(R.id.btnClrA);
		btnClrA.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				MTKutility.debugWrite(132, "MTKlogSettings -button " + btnClrA.getText() + " pressed");
				clearAllSettings();
			}
		});

		btnRefr = (Button) mV.findViewById(R.id.btnRefr);
		btnRefr.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				MTKutility.debugWrite(132, "MTKlogSettings -button " + btnRefr.getText() + " pressed");
				refreshCheckboxes();
			}
		});

		btnRefr2 = (Button) mV.findViewById(R.id.btnRefr2);
		btnRefr2.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				MTKutility.debugWrite(132, "MTKlogSettings -button " + btnRefr2.getText() + " pressed");
				refreshTextValues();
			}
		});

		btnSet = (Button) mV.findViewById(R.id.btnSet);
		btnSet.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				MTKutility.debugWrite(132, "MTKlogSettings -button " + btnSet.getText() + " pressed");
				saveBitmap();
			}
		});

		btnSave = (Button) mV.findViewById(R.id.btnSave);
		btnSave.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				MTKutility.debugWrite(132, "MTKlogSettings -button " + btnSave.getText() + " pressed");
				saveTexts();
			}
		});
		frame1 = mV.findViewById(R.id.frame1);
		frame2 = mV.findViewById(R.id.frame2);
		frame3 = mV.findViewById(R.id.frame3);

		cbxDate = (CheckBox) mV.findViewById(R.id.cbxDate);
		cbxMili = (CheckBox) mV.findViewById(R.id.cbxMili);
		cbxLat  = (CheckBox) mV.findViewById(R.id.cbxLat);
		cbxLon  = (CheckBox) mV.findViewById(R.id.cbxLon);
		cbxHei  = (CheckBox) mV.findViewById(R.id.cbxHei);
		cbxSpd  = (CheckBox) mV.findViewById(R.id.cbxSpd);
		cbxHed  = (CheckBox) mV.findViewById(R.id.cbxHed);
		cbxDis  = (CheckBox) mV.findViewById(R.id.cbxDis);
		cbxRCR  = (CheckBox) mV.findViewById(R.id.cbxRCR);
		cbxVal  = (CheckBox) mV.findViewById(R.id.cbxVal);
		cbxFxo  = (CheckBox) mV.findViewById(R.id.cbxFxo);
		rbnOvr  = (RadioButton) mV.findViewById(R.id.rbnOvr);
		rbnStp  = (RadioButton) mV.findViewById(R.id.rbnStp);
		cbxNsat = (CheckBox) mV.findViewById(R.id.cbxNsat);
		cbxSID  = (CheckBox) mV.findViewById(R.id.cbxSID);
		cbxEle  = (CheckBox) mV.findViewById(R.id.cbxEle); 
		cbxAzi  = (CheckBox) mV.findViewById(R.id.cbxAzi);
		cbxSNR  = (CheckBox) mV.findViewById(R.id.cbxSNR);
		cbxDSTA = (CheckBox) mV.findViewById(R.id.cbxDSTA);
		cbxDAGE = (CheckBox) mV.findViewById(R.id.cbxDAGE); 
		cbxPDOP = (CheckBox) mV.findViewById(R.id.cbxPDOP);
		cbxHDOP = (CheckBox) mV.findViewById(R.id.cbxHDOP);
		cbxVDOP = (CheckBox) mV.findViewById(R.id.cbxVDOP);
		logMSG  = (TextView) mV.findViewById(R.id.logMSG);
		txvBitm = (TextView) mV.findViewById(R.id.txvBitm);
		txtByT  = (EditText) mV.findViewById(R.id.txtByT);
		txtByD  = (EditText) mV.findViewById(R.id.txtByD);
		txtByS  = (EditText) mV.findViewById(R.id.txtByS);

		//use width to size frames so that screen is evenly filled
		int framewidth = (MTKutility.screenWidth - 30)/3;
		frame1.setMinimumWidth(framewidth);
		frame2.setMinimumWidth(framewidth);
		frame3.setMinimumWidth(framewidth);

		logMSG.setTextSize(TypedValue.COMPLEX_UNIT_SP, epoinfofont-2);
		btnDflt.setTextSize(TypedValue.COMPLEX_UNIT_SP, btnBARfont);
		btnClrA.setTextSize(TypedValue.COMPLEX_UNIT_SP, btnBARfont);
		btnRefr.setTextSize(TypedValue.COMPLEX_UNIT_SP, btnBARfont);
		btnSet.setTextSize(TypedValue.COMPLEX_UNIT_SP, btnBARfont);
		btnSave.setTextSize(TypedValue.COMPLEX_UNIT_SP, btnBARfont-2);
		btnRefr2.setTextSize(TypedValue.COMPLEX_UNIT_SP, btnBARfont-2);

		return mV;
	}	//onCreateView()

	@Override
	public void onPause() {
		super.onPause();
		MTKutility.debugWrite(132, "MTKlogSettings-onPause()");
	}

	@Override
	public void onResume() {
		super.onResume();
		MTKutility.debugWrite(132, "MTKlogSettings-onResume()");
		gpsdev = MTKutility.getBTconnect();
		//check for BlueTooth connection
		if (!gpsdev.sock.isConnected()) {
			//re-establish the MTK logger connection
			if (!gpsdev.connect()){
				msg = "BlueTooth connection is missing";
				Toast.makeText(getActivity(), msg, Toast.LENGTH_LONG).show();
				MTKutility.debugWrite(132, "**** "+msg);
				return;
			}
		}
		refreshTextValues();			
		refreshCheckboxes();
		setMsgFields();
	}

	private void refreshTextValues() {
		MTKutility.debugWrite(132, "MTKlogSettings -refreshTextValues()");
		int val;
		double dVal;

		//get recording by time value
		parms = MTKutility.mtkCmd("PMTK182,2,3", "PMTK182,3,3", MTKutility.cmdTimeOut*2);
		if (parms != null){
			dVal = Double.parseDouble(parms[3]) / 10.0;
			MTKtxtByT = String.format(Locale.CANADA,"%.1f", dVal);
			txtByT.setText(MTKtxtByT);}
		//get recording by distance value
		parms = MTKutility.mtkCmd("PMTK182,2,4", "PMTK182,3,4", MTKutility.cmdTimeOut*2);
		if (parms != null){
			val = Integer.valueOf(parms[3]) / 10;
			MTKtxtByD = Integer.toString(val);
			txtByD.setText(MTKtxtByD);}
		//get recording by speed value
		parms = MTKutility.mtkCmd("PMTK182,2,5", "PMTK182,3,5", MTKutility.cmdTimeOut*2);
		if (parms != null){
			val = Integer.valueOf(parms[3]) / 10;
			MTKtxtByS = Integer.toString(val);
			txtByS.setText(MTKtxtByS);}
	}	//refreshTextValues()

	private void refreshCheckboxes() {
		MTKutility.debugWrite(132, "MTKlogSettings -refreshCheckboxes()");
		//get recording mode - stop/overwrite
		parms = MTKutility.mtkCmd("PMTK182,2,6", "PMTK182,3,6", MTKutility.cmdTimeOut*2);
		if (parms != null){
			MTKrbtnVal = Integer.parseInt(parms[3]);
			if (MTKrbtnVal == 1) {
				rbnOvr.setChecked(true);
			}else{
				rbnStp.setChecked(true);}}
		//get fields logged bitmask and set checkboxes
		parms = MTKutility.mtkCmd("PMTK182,2,2", "PMTK182,3,2", MTKutility.cmdTimeOut*2);
		if (parms != null){
			clearAllSettings();
			logFormat = (int) Long.parseLong(parms[3], 16);
			MTKlogFormat = logFormat;

			if ((logFormat & cbxDateMask) == cbxDateMask) {
				cbxDate.setChecked(true); cbxDate();}

			if ((logFormat & cbxMiliMask) == cbxMiliMask) {
				cbxMili.setChecked(true); cbxMili();}

			if ((logFormat & cbxNsatMask) == cbxNsatMask) {
				cbxNsat.setChecked(true); cbxNsat();}

			if ((logFormat & cbxSIDMask) == cbxSIDMask) {
				cbxSID.setChecked(true); cbxSID();}

			if ((logFormat & cbxEleMask) == cbxEleMask) {
				cbxEle.setChecked(true); cbxEle();}

			if ((logFormat & cbxAziMask) == cbxAziMask) {
				cbxAzi.setChecked(true); cbxAzi();}

			if ((logFormat & cbxEleMask) == cbxEleMask) {
				cbxEle.setChecked(true); cbxEle();}

			if ((logFormat & cbxLatMask) == cbxLatMask) {
				cbxLat.setChecked(true); cbxLat();}

			if ((logFormat & cbxLonMask) == cbxLonMask) {
				cbxLon.setChecked(true); cbxLon();}

			if ((logFormat & cbxHeiMask) == cbxHeiMask) {
				cbxHei.setChecked(true); cbxHei();}

			if ((logFormat & cbxSpdMask) == cbxSpdMask) {
				cbxSpd.setChecked(true); cbxSpd();}

			if ((logFormat & cbxHedMask) == cbxHedMask) {
				cbxHed.setChecked(true); cbxHed();}

			if ((logFormat & cbxDisMask) == cbxDisMask) {
				cbxDis.setChecked(true); cbxDis();}

			if ((logFormat & cbxDSTAMask) == cbxDSTAMask) {
				cbxDSTA.setChecked(true); cbxDSTA();}

			if ((logFormat & cbxDAGEMask) == cbxDAGEMask) {
				cbxDAGE.setChecked(true); cbxDAGE();}

			if ((logFormat & cbxRCRMask) == cbxRCRMask) {
				cbxRCR.setChecked(true); cbxRCR();}

			if ((logFormat & cbxValMask) == cbxValMask) {
				cbxVal.setChecked(true); cbxVal();}

			if ((logFormat & cbxFxoMask) == cbxFxoMask) {
				cbxFxo.setChecked(true); cbxFxo();}

			if ((logFormat & cbxPDOPMask) == cbxPDOPMask) {
				cbxPDOP.setChecked(true); cbxPDOP();}

			if ((logFormat & cbxHDOPMask) == cbxHDOPMask) {
				cbxHDOP.setChecked(true); cbxHDOP();}

			if ((logFormat & cbxVDOPMask) == cbxVDOPMask) {
				cbxVDOP.setChecked(true); cbxVDOP();}
		}	
	}

	private void saveBitmap() {
		MTKutility.debugWrite(132, "MTKlogSettings -saveBitmap()");
		int rbtnVal = 2;
		if (rbnOvr.isChecked()){rbtnVal = 1;}
		final int rbtnValParm = rbtnVal;
		if ((MTKlogFormat == logFormat)&&(MTKrbtnVal == rbtnVal)){
			msg = "no changes - nothing to save";
			Toast.makeText(getActivity(), msg, Toast.LENGTH_LONG).show();
		}else {
			final deleteLog task = new deleteLog(getActivity());
			//let user abandon log delete when MTK logger has log records
			if (logRecCount != 0){
				AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
				msg = String.format("MTK logger has %s log records", Integer.toString(logRecCount));
				alertDialogBuilder.setTitle(msg);
				alertDialogBuilder
				.setMessage("Click yes proceed with log deletion")
				.setCancelable(false)
				.setPositiveButton("Yes",
						new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog,
							int id) {
						task.execute(logRecCount, logFormat, rbtnValParm);
						dialog.cancel();
					}
				})
				.setNegativeButton("No",
						new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog,
							int id) { 
						// do nothing
					}
				});
				//show alert dialog
				AlertDialog alertDialog = alertDialogBuilder.create();
				alertDialog.show();
			}else{
				task.execute(logRecCount, logFormat, rbtnValParm);}
		}
	}

	private void saveTexts() {
		MTKutility.debugWrite(132, "MTKlogSettings -saveTexts()");
		txtByTstr = txtByT.getText().toString().replace(",",".");
		double dtxtByT;
		dtxtByT = Double.parseDouble(txtByTstr);
		if ((dtxtByT != 0.0)&&((dtxtByT < 0.1)||(dtxtByT > 999.0))){
			msg = "by time is invald: must be <=999.0 & >=0.1";
			Toast.makeText(getActivity(), msg, Toast.LENGTH_LONG).show();
			return;}

		txtByDstr = txtByD.getText().toString();
		intVal = Integer.parseInt(txtByDstr);
		if ((intVal != 0)&&((intVal < 1)||(intVal > 9999))){
			msg = "by distance is invald: must be <=9999 & >=10";
			Toast.makeText(getActivity(), msg, Toast.LENGTH_LONG).show();
			return;}

		txtBySstr = txtByS.getText().toString();
		intVal = Integer.parseInt(txtBySstr);
		if ((intVal != 0)&&((intVal < 1)||(intVal > 999))){
			msg = "speed over is invald: must be <=999 & >=1";
			Toast.makeText(getActivity(), msg, Toast.LENGTH_LONG).show();
			return;}
		/*		double dtxtByS;
		dtxtByS = Double.parseDouble(txtBySstr);
		if ((dtxtByS != 0.0)&&((dtxtByS < 0.1)||(intVal > 999.0))){
			msg = "by time is invald: must be <=999.0 & >=0.1";
			Toast.makeText(getActivity(), msg, Toast.LENGTH_LONG).show();
			return;}*/

		dtxtByT = dtxtByT * 10.0;
		txtByTstr = Integer.toString((int)(dtxtByT));
		if (txtByTstr != MTKtxtByT){
			cmd = String.format("PMTK182,1,3,%s", txtByTstr);
			parms = MTKutility.mtkCmd(cmd, "PMTK001,182", MTKutility.cmdTimeOut);
			MTKtxtByT = txtByTstr;
			if (dtxtByT < 10){
				cmd = String.format("PMTK300,%s00,0,0,0,0", Integer.toString((int) dtxtByT)); 
			}else{
				cmd = "PMTK300,1000,0,0,0,0";}
			parms = MTKutility.mtkCmd(cmd, "PMTK001,300", MTKutility.cmdTimeOut);
		}

		if (txtByDstr != MTKtxtByD){
			cmd = String.format("PMTK182,1,4,%s0", txtByDstr);
			parms = MTKutility.mtkCmd(cmd, "PMTK001,182", MTKutility.cmdTimeOut);
			MTKtxtByD = txtByDstr;}

		/*		dtxtByS = dtxtByS * 10.0;
		txtBySstr = Integer.toString((int)(dtxtByS));*/
		if (txtBySstr != MTKtxtByS){
			cmd = String.format("PMTK182,1,5,%s", txtBySstr);
			parms = MTKutility.mtkCmd(cmd, "PMTK001,182", MTKutility.cmdTimeOut);
			MTKtxtByS = txtBySstr;}

		setMsgFields();
	}

	// the following methods are called when from MTKutility.onCheckboxClicked
	// they adjust the settings bitmask and log record length to reflect checked settings 
	static void cbxDate(){
		MTKutility.debugWrite(132, "MTKlogSettings -cbxDate()");
		//date / time - size I4
		if (cbxDate.isChecked()){
			logFormat |= cbxDateMask;
			logRecLen += 6;
		}else {
			logFormat ^= cbxDateMask;
			logRecLen -= 6;}
	}

	static void cbxMili(){
		MTKutility.debugWrite(132, "MTKlogSettings -cbxMili()");
		//milliseconds - size U2
		if (cbxMili.isChecked()){
			logFormat |= cbxMiliMask;
			logRecLen += 2;
		}else {
			logFormat ^= cbxMiliMask;
			logRecLen -= 2;}
	}

	static void cbxLat(){
		MTKutility.debugWrite(132, "MTKlogSettings -cbxLat()");
		//latitude - size R8
		if (cbxLat.isChecked()){
			logFormat |= cbxLatMask;
			logRecLen += 8;
		}else {
			logFormat ^= cbxLatMask;
			logRecLen -= 8;}
	}

	static void cbxLon(){
		MTKutility.debugWrite(132, "MTKlogSettings -cbxLon()");
		//longitude - size R8
		if (cbxLon.isChecked()){
			logFormat |= cbxLonMask;
			logRecLen += 8;
		}else {
			logFormat ^= cbxLonMask;
			logRecLen -= 8;}
	}

	static void cbxHei(){
		MTKutility.debugWrite(132, "MTKlogSettings -cbxHei()");
		//height - size R4
		if (cbxHei.isChecked()){
			logFormat |= cbxHeiMask;
			logRecLen += 4;
		}else {
			logFormat ^= cbxHeiMask;
			logRecLen -= 4;}
	}

	static void cbxSpd(){
		MTKutility.debugWrite(132, "MTKlogSettings -cbxSpd()");
		//speed - size R4
		if (cbxSpd.isChecked()){
			logFormat |= cbxSpdMask;
			logRecLen += 4;
		}else {
			logFormat ^= cbxSpdMask;
			logRecLen -= 4;}
	}

	static void cbxHed(){
		MTKutility.debugWrite(132, "MTKlogSettings -cbxHed()");
		//heading - size R4
		if (cbxHed.isChecked()){
			logFormat |= cbxHedMask;
			logRecLen += 4;
		}else {
			logFormat ^= cbxHedMask;
			logRecLen -= 4;}
	}

	static void cbxDis(){
		MTKutility.debugWrite(132, "MTKlogSettings -cbxDis()");
		//distance - size 8??
		if (cbxDis.isChecked()){
			logFormat |= cbxDisMask;
			logRecLen += 8;
		}else {
			logFormat ^= cbxDisMask;
			logRecLen -= 8;}
	}

	static void cbxRCR(){
		MTKutility.debugWrite(132, "MTKlogSettings -cbxRCR()");
		//reason for recorded track point - size U2
		if (cbxRCR.isChecked()){
			logFormat |= cbxRCRMask;
			logRecLen += 2;
		}else {
			logFormat ^= cbxRCRMask;
			logRecLen -= 2;}
	}

	static void cbxVal(){
		MTKutility.debugWrite(132, "MTKlogSettings -cbxVal()");
		//fix mode - SPS, DGPS, etc. - size U2
		if (cbxVal.isChecked()){
			logFormat |= cbxValMask;
			logRecLen += 2;
		}else {
			logFormat ^= cbxValMask;
			logRecLen -= 2;}
	}

	static void cbxFxo(){
		MTKutility.debugWrite(132, "MTKlogSettings -cbxFxo()");
		//only record track points that have a satellite fix
		if (cbxFxo.isChecked()){
			logFormat |= cbxFxoMask;
			//does not affect log record length
		}else {
			logFormat ^= cbxFxoMask;}
	}

	static void cbxNsat(){
		MTKutility.debugWrite(132, "MTKlogSettings -cbxNsat()");
		//number of satellites - size U2
		if (cbxNsat.isChecked()){
			logFormat |= cbxNsatMask;
			logRecLen += 2;
		}else {
			logFormat ^= cbxNsatMask;
			logRecLen -= 2;}
	}

	static void cbxSID(){
		MTKutility.debugWrite(132, "MTKlogSettings -cbxSID()");
		//satellite id - size U4
		if (cbxSID.isChecked()){
			logFormat |= cbxSIDMask;
			logRecLen += 4;
			cbxEle.setAlpha((float) 1.0);
			cbxEle.setClickable(true);
			cbxAzi.setAlpha((float) 1.0);
			cbxAzi.setClickable(true);
			cbxSNR.setAlpha((float) 1.0);
			cbxSNR.setClickable(true);
		}else {
			logFormat ^= cbxSIDMask;
			logRecLen -= 4;
			if (cbxEle.isChecked()){
				cbxEle.setChecked(false);
				cbxEle();}
			cbxEle.setAlpha((float) 0.5);
			cbxEle.setClickable(false);
			if (cbxAzi.isChecked()){
				cbxAzi.setChecked(false);
				cbxAzi();}
			cbxAzi.setAlpha((float) 0.5);
			cbxAzi.setClickable(false);
			if (cbxSNR.isChecked()){
				cbxSNR.setChecked(false);
				cbxSNR();}
			cbxSNR.setAlpha((float) 0.5);
			cbxSNR.setClickable(false);
		}
	}

	static void cbxEle(){
		MTKutility.debugWrite(132, "MTKlogSettings -cbxEle()");
		//satellite elevation - size I2
		if (cbxEle.isChecked()){
			logFormat |= cbxEleMask;
			logRecLen += 2;
		}else {
			logFormat ^= cbxEleMask;
			logRecLen -= 2;}
	}

	static void cbxAzi(){
		MTKutility.debugWrite(132, "MTKlogSettings -cbxAzi()");
		//satellite azimuth - size U2
		if (cbxAzi.isChecked()){
			logFormat |= cbxAziMask;
			logRecLen += 2;
		}else {
			logFormat ^= cbxAziMask;
			logRecLen -= 2;}
	}

	static void cbxSNR(){
		MTKutility.debugWrite(132, "MTKlogSettings -cbxSNR()");
		//satellite number - size U2
		if (cbxSNR.isChecked()){
			logFormat |= cbxSNRMask;
			logRecLen += 2;
		}else {
			logFormat ^= cbxSNRMask;
			logRecLen -= 2;}
	}

	static void cbxDSTA(){
		MTKutility.debugWrite(132, "MTKlogSettings -cbxDSTA()");
		//differential reference station id - size U2
		if (cbxDSTA.isChecked()){
			logFormat |= cbxDSTAMask;
			logRecLen += 2;
		}else {
			logFormat ^= cbxDSTAMask;
			logRecLen -= 2;}
	}

	static void cbxDAGE(){
		MTKutility.debugWrite(132, "MTKlogSettings -cbxDAGE()");
		//age of differential data in seconds - size R4
		if (cbxDAGE.isChecked()){
			logFormat |= cbxDAGEMask;
			logRecLen += 4;
		}else {
			logFormat ^= cbxDAGEMask;
			logRecLen -= 4;}
	}

	static void cbxPDOP(){
		MTKutility.debugWrite(132, "MTKlogSettings -cbxPDOP()");
		//position dilution of precision (m) - size U2
		if (cbxPDOP.isChecked()){
			logFormat |= cbxPDOPMask;
			logRecLen += 4;
		}else {
			logFormat ^= cbxPDOPMask;
			logRecLen -= 4;}
	}

	static void cbxHDOP(){
		MTKutility.debugWrite(132, "MTKlogSettings -cbxHDOP()");
		//horizontal dilution of precision (m) - size U2
		if (cbxHDOP.isChecked()){
			logFormat |= cbxHDOPMask;
			logRecLen += 4;
		}else {
			logFormat ^= cbxHDOPMask;
			logRecLen -= 4;}
	}

	static void cbxVDOP(){
		MTKutility.debugWrite(132, "MTKlogSettings -cbxVDOP()");
		//vertical dilution of precision (m) - size U2
		if (cbxVDOP.isChecked()){
			logFormat |= cbxVDOPMask;
			logRecLen += 4;
		}else {
			logFormat ^= cbxVDOPMask;
			logRecLen -= 4;}
	}
	// end of settings methods

	static void setMsgFields(){
		MTKutility.debugWrite(132, "MTKlogSettings -setMsgFields()");
		String s1;
		int days;
		int hours;
		if ((logRecLen > 0) && (MTKutility.flashSize > 0)){
			maxRecords = MTKutility.flashSize / logRecLen;
			txtByTstr = txtByT.getText().toString().replace(",",".");
			dVal = Double.parseDouble(txtByTstr);
			maxTime = maxRecords * dVal;
			hours = (int) maxTime / 3600;
			days = hours / 24;
			hours = hours - (days * 24);
			//			maxTime = maxTime / (60.0 * 60.0 * 24.0);
			s1 = Integer.toString(logRecLen) + " Bytes/record  Max:" +
					NumberFormat.getInstance().format(maxRecords) +
					" records  Time:" + Integer.toString(days) + " days " +
					Integer.toString(hours) + " hours";			
		}else {
			s1 = Integer.toString(logRecLen) + " Bytes/record";}

		logMSG.setText(s1);
		String s2 = "Bitmask: " + String.format("%8S", Integer.toHexString(logFormat)).replace(' ', '0');
		txvBitm.setText(s2);
	}

	private void setDefaultSettings() {
		MTKutility.debugWrite(132, "MTKlogSettings -setDefaultSettings()");
		clearAllSettings();
		cbxRCR.setChecked(true); cbxRCR();
		cbxVal.setChecked(true); cbxVal();
		cbxFxo.setChecked(true); cbxFxo();
		cbxDate.setChecked(true); cbxDate();
		cbxLat.setChecked(true); cbxLat();
		cbxLon.setChecked(true); cbxLon();
		cbxHei.setChecked(true); cbxHei();
		cbxSpd.setChecked(true); cbxSpd();
		rbnStp.setChecked(true);
	}

	private void clearAllSettings() {
		MTKutility.debugWrite(132, "MTKlogSettings -clearAllSettings()");
		if (cbxDate.isChecked()){
			cbxDate.setChecked(false);
			cbxDate();}
		if (cbxMili.isChecked()){
			cbxMili.setChecked(false);
			cbxMili();}
		if (cbxNsat.isChecked()){
			cbxNsat.setChecked(false);
			cbxNsat();}
		if (cbxSID.isChecked()){
			cbxSID.setChecked(false);
			cbxSID();}
		if (cbxLat.isChecked()){
			cbxLat.setChecked(false);
			cbxLat();}
		if (cbxLon.isChecked()){
			cbxLon.setChecked(false);
			cbxLon();}
		if (cbxHei.isChecked()){
			cbxHei.setChecked(false);
			cbxHei();}
		if (cbxSpd.isChecked()){
			cbxSpd.setChecked(false);
			cbxSpd();}
		if (cbxHed.isChecked()){
			cbxHed.setChecked(false);
			cbxHed();}
		if (cbxDis.isChecked()){
			cbxDis.setChecked(false);
			cbxDis();}
		if (cbxDSTA.isChecked()){
			cbxDSTA.setChecked(false);
			cbxDSTA();}
		if (cbxDAGE.isChecked()){
			cbxDAGE.setChecked(false);
			cbxDAGE();}
		if (cbxRCR.isChecked()){
			cbxRCR.setChecked(false);
			cbxRCR();}
		if (cbxVal.isChecked()){
			cbxVal.setChecked(false);
			cbxVal();}
		if (cbxFxo.isChecked()){
			cbxFxo.setChecked(false);
			cbxFxo();}
		if (cbxPDOP.isChecked()){
			cbxPDOP.setChecked(false);
			cbxPDOP();}
		if (cbxHDOP.isChecked()){
			cbxHDOP.setChecked(false);
			cbxHDOP();}
		if (cbxVDOP.isChecked()){
			cbxVDOP.setChecked(false);
			cbxVDOP();}
		logRecLen = 0;
		logFormat = 0x00000000;
	}

	class deleteLog extends AsyncTask<Integer, Void, Boolean> {
		//parameters passed are:logRecCount, logFormat, RbtnValParm
		private Context mContext;
		ProgressDialog pDialog;
		boolean loop = true;

		public deleteLog(Context context){
			this.mContext = context;
			MTKutility.debugWrite(132, "MTKlogSettings - deleteLog.deleteLog()");
		}

		@Override
		protected void onPreExecute(){
			MTKutility.debugWrite(132, "MTKlogSettings - deleteLog.onPreExecute()");
			pDialog = new ProgressDialog(mContext);
			pDialog.setTitle("Set MTK logging options");
			pDialog.setMessage("updating");
			pDialog.setCancelable(false);
			pDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
			pDialog.setIndeterminate(true);
			pDialog.show();
		}
		@Override
		protected void onPostExecute(Boolean result) {
			MTKutility.debugWrite(132, "MTKlogSettings - deleteLog.onPostExecute()");
			pDialog.dismiss();
			if (result){
				msg = "new settings have been saved";
			}else {
				msg = "Log delete failed" + BR + "new settings have not been saved";}
			MTKutility.debugWrite(132, "+++ "+msg);
			Toast.makeText(getActivity(), msg, Toast.LENGTH_LONG).show();
		}

		@Override
		protected Boolean doInBackground(Integer... parms) {
			MTKutility.debugWrite(132, "MTKlogSettings - deleteLog.doInBackground()");
			String cmd;
			String[] result = null;
			if (parms[0] != 0){
				//delete log
				result = MTKutility.mtkCmd("PMTK182,6,1", "PMTK001,182,6", MTKutility.cmdTimeOut+40.0);
				if (result == null){
					//delete failed
					MTKutility.debugWrite(132, "**** delete failed");
					return false;}}

			//send bitmask
			cmd = String.format("PMTK182,1,2,%S", Integer.toHexString(parms[1]));
			result = MTKutility.mtkCmd(cmd, "PMTK001,182,1", MTKutility.cmdTimeOut);

			//send overwrite mode setting
			cmd = String.format("PMTK182,1,6,%S", Integer.toString(parms[2]));
			result = MTKutility.mtkCmd(cmd, "PMTK001,182,1", MTKutility.cmdTimeOut);
			return true;			
		}
	}
}