/**
 * @author Alex Tauber
 * 
 * This file is part of the Android app MTK logger utility.
 * 
 * MTK logger utility is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, version 3 of the License. This extends to files
 * included that were authored by others and modified to make them suitable for
 * MTK logger utility. All files included were subject to open source licensing.
 * 
 * MTK logger utility is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You can review a copy of the GNU General Public License
 * at http://www.gnu.org/licenses.
 *
 */
package com.adtdev.mtkutility;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;

import com.adtdev.mtkutility.R;

import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentTransaction;
//import android.app.ProgressDialog;
//import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
//import android.net.ConnectivityManager;
//import android.net.NetworkInfo;
//import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
//import android.os.Handler;
//import android.os.Message;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
//import android.provider.Settings;
//import android.provider.Settings.SettingNotFoundException;
import android.util.DisplayMetrics;
import android.util.Log;
//import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
//import android.widget.CheckBox;
//import android.view.WindowManager;
import android.widget.Toast;

public class MTKutility extends ActionBarActivity {

	public static final String TAG = "MTKutility";

	static Fragment activeFragment;

	private static BluetoothAdapter mBluetoothAdapter = null;
	private static String GPS_bluetooth_id;
	static GPSrxtx gpsdev;
	static boolean GPSrxtxSet = false;

	private static final int REQUEST_CODE_PICK_DIR = 1;
	private static final int REQUEST_ENABLE_BT = 2;
	private static final int REQUEST_CONFIG_PREFS = 3;

	static final String TAB_KEY_INDEX = "tab_key";
	static SharedPreferences sharedPrefs;
	static SharedPreferences appPrefs;
	static SharedPreferences.Editor sharedPrefEditor;
	static SharedPreferences.Editor appPrefEditor;
	static String aboutXMLfile;
	private static boolean debug;
	private static boolean debugCore = false;
	private static String debugFile = "/MTKutilityDebug.txt";
	static boolean debugFileIsOpen = false;
	private File fileOut;
	private static FileOutputStream fOut;
	private static OutputStreamWriter OutWriter;
	static int epoMSGfont;
	static int mainSPNRfont;
	static int mainSetMsgfont;
	static int mainBtnSVEfont;
	static int mainbtnBARfont;
	static int dnldButtons;
	static int dnldTexts;
	static boolean useHTMLsettings;
	static int htmlStngsBtns;
	static int htmlStngsTxts;
	static int agpsButtons;
	static int agpsTexts;
	static String NMEAsettings = null;


	private final static String BR = System.getProperty("line.separator");
	private static final int DISP_CHAR = 0;
	private static long back_pressed;

	// Linefeed Code Settings
	private static final int LINEFEED_CODE_CR = 0;
	private static final int LINEFEED_CODE_CRLF = 1;
	private static final int LINEFEED_CODE_LF = 2;
	private static int mReadLinefeedCode = LINEFEED_CODE_LF;

	// binary command to switch to NMEA mode
	final static byte[] binPMTK253 = new byte[] {
		(byte)0x04, 0x24, 0x0E, 0x00, (byte) 0xFD, 0x00, 0x00, 0x00,
		(byte)0xC2, 0x01, 0x00, 0x30, 0x0D, 0x0A};

	private static String msg;
	private static String reply;
	static int rbufSize = 4096;
	static int EPOblks;
	static long BINrecs;
	static String EPOinfo = "";
	static int screenWidth;
	static int screenHeight;
	static int screenDPI;
	static String[] parms;
	static double cmdTimeOut;
	static boolean connected = false;
	static boolean hasLOG = false;
	static boolean hasAGPS = false;
	static boolean renewAGPS = true;
	static boolean aboutIsActive;
	static Activity mContext = null;
	static String rootDirectory = "/storage";
	static int flashSize;


	static ActionBar actionbar;
	static ActionBar.Tab tabGPX;
	static ActionBar.Tab tabMain;
	static ActionBar.Tab tabDNLD;
	static ActionBar.Tab tabLset;
	static ActionBar.Tab tabHset;
	static ActionBar.Tab tabAGPS;

	private Thread.UncaughtExceptionHandler androidDefaultUEH;
	private Thread.UncaughtExceptionHandler handler = new Thread.UncaughtExceptionHandler() {
		@Override
		public void uncaughtException(Thread thread, Throwable ex) {
			if (!debugFileIsOpen){
				debuglogOpen();
			}
			debugWrite(999, "@@@@ "+ thread.getName() +" uncaught exception: " + ex);
			ex.printStackTrace();
			readLogCat();
			abortApp(ex.toString());
			androidDefaultUEH.uncaughtException(thread, ex);
		}
	};


	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		androidDefaultUEH = Thread.getDefaultUncaughtExceptionHandler();
		Thread.setDefaultUncaughtExceptionHandler(handler);

		mContext = this;
		setContentView(R.layout.main);
		sharedPrefs = PreferenceManager.getDefaultSharedPreferences(mContext);
		sharedPrefEditor = sharedPrefs.edit();
		appPrefs = mContext.getSharedPreferences("otherprefs", Context.MODE_PRIVATE);
		appPrefEditor = appPrefs.edit();
		getPreferences();

		//get screen size
		DisplayMetrics metrics = this.getResources().getDisplayMetrics();
		screenDPI = metrics.densityDpi;
		screenWidth = metrics.widthPixels;
		screenHeight = metrics.heightPixels;

		checkForDebugLog();

		//this app will not work without a BlueTooth connection to an MTK logger  
		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

		if (mBluetoothAdapter == null) {
			// Device does not support BlueTooth
			AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(mContext);
			alertDialogBuilder.setTitle("BlueTooth capability not found");

			// set dialog message
			alertDialogBuilder
			.setMessage("This app talks to your MTK logger via BlueTooth." + BR +
					"Your Android device does not seem to support BlueTooth.")
					.setCancelable(false)
					.setPositiveButton("OK",
							new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog,
								int id) {MTKutility.this.finish();}
					});
			//show alert dialog
			AlertDialog alertDialog = alertDialogBuilder.create();
			alertDialog.show();
			return;
		}

		// ActionBar
		actionbar = getActionBar();
		actionbar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

		// create new tabs and and set up the titles of the tabs
		tabGPX  = actionbar.newTab().setText(
				getString(R.string.ui_tabname_gpx));
		tabMain = actionbar.newTab().setText(
				getString(R.string.ui_tabname_main));
		tabDNLD = actionbar.newTab().setText(
				getString(R.string.ui_tabname_dnld));
		tabLset = actionbar.newTab().setText(
				getString(R.string.ui_tabname_logset));
		tabHset = actionbar.newTab().setText(
				getString(R.string.ui_tabname_htmset));
		tabAGPS = actionbar.newTab().setText(
				getString(R.string.ui_tabname_agps));

		// create the fragments
		Fragment createGPXFragment = new createGPXFragment();
		Fragment MainFragment      = new MainFragment();
		Fragment logdnldfragment   = new logDnldFragment();
		Fragment EPOFragment       = new EPOFragment();
		Fragment LogSetFragment    = new MTKlogSettings();
		Fragment HTMSetFragment    = new MTKhtmlSettings();

		// bind the fragments to the tabs - set up tabListeners for each tab
		tabGPX.setTabListener(new MyTabsListener(createGPXFragment,
				getApplicationContext()));
		tabMain.setTabListener(new MyTabsListener(MainFragment,
				getApplicationContext()));
		tabDNLD.setTabListener(new MyTabsListener(logdnldfragment,
				getApplicationContext()));
		tabLset.setTabListener(new MyTabsListener(LogSetFragment,
				getApplicationContext()));
		tabHset.setTabListener(new MyTabsListener(HTMSetFragment,
				getApplicationContext()));
		tabAGPS.setTabListener(new MyTabsListener(EPOFragment,
				getApplicationContext()));

		// add the main fragment tab to the action bar
		//the rest of the tabs are added when the GPS logger is connected
		actionbar.addTab(tabMain);
		actionbar.addTab(tabGPX);

		//settings screen will not fit on a screen that is less than 480 pixels wide
		if (screenWidth < 480){screenSizeAbort();}

		// restore to navigation
		if (savedInstanceState != null) {
			msg = "tab is " + savedInstanceState.getInt(TAB_KEY_INDEX, 0);
			debugWrite(132,"+++ savedInstanceState: "+msg);
			Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
			actionbar.setSelectedNavigationItem(savedInstanceState.getInt(
					TAB_KEY_INDEX, 0));}

		//		checkForBTdevice();
		//		throw new RuntimeException("This is a crash");
	}	//onCreate()

	@Override
	public void onResume() {
		super.onResume();
		//		sharedPrefs = PreferenceManager.getDefaultSharedPreferences(mContext);
		checkForDebugLog();
		debugWrite(132, "MTKutility - onResume()");
	}

	static void getPreferences(){
		cmdTimeOut = Double.parseDouble(sharedPrefs.getString("cmdTimeOut", "10"));
		cmdTimeOut = cmdTimeOut * 3.0;
		debugFile = sharedPrefs.getString("Path",Environment.getExternalStorageDirectory().toString()) + debugFile;

		//MAIN tab 
		epoMSGfont = Integer.valueOf(sharedPrefs.getString("epoMSGfont", "14"));
		mainSPNRfont = Integer.valueOf(sharedPrefs.getString("mainSPNRfont", "16"));
		mainSetMsgfont = Integer.valueOf(sharedPrefs.getString("mainSetMsgfont", "13"));
		mainBtnSVEfont = Integer.valueOf(sharedPrefs.getString("mainBtnSVEfont", "18"));
		mainbtnBARfont  = Integer.valueOf(sharedPrefs.getString("mainbtnBARfont", "18"));

		//DOWNLAOD tab 
		dnldButtons = Integer.valueOf(sharedPrefs.getString("dnldButtons", "16"));
		dnldTexts   = Integer.valueOf(sharedPrefs.getString("dnldTexts", "15"));

		//HTML SETTINGS tab
		useHTMLsettings = sharedPrefs.getBoolean("useHTMLsettings", false);
		htmlStngsBtns = Integer.valueOf(sharedPrefs.getString("htmlStngsBtns", "16"));
		htmlStngsTxts = Integer.valueOf(sharedPrefs.getString("htmlStngsTxts", "14"));

		//AGPS tab
		agpsButtons = Integer.valueOf(sharedPrefs.getString("agpsButtons", "16"));
		agpsTexts = Integer.valueOf(sharedPrefs.getString("agpsTexts", "17"));

		NMEAsettings = appPrefs.getString("NMEAsettings", "");
	}

	private void checkForDebugLog() {
		//		debugWrite(132, "MTKutility - checkForDebugLog()");
		if (debugFileIsOpen){
			debugWrite(132, "+++ debug file is already open");
			return;}

		debug = sharedPrefs.getBoolean("debugPref", false);

		if (debug){
			AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(mContext);
			alertDialogBuilder.setTitle("Debug logging is turned on");

			// set dialog message
			alertDialogBuilder
			.setMessage("You have debug logging set in Preferences.\n\n" +
					"This can create a large file.\n\n" +
					"press Trace for a regular log\n" +
					"press Verbose for all I/O log\n" +
					"press Cancel to turn debug off")
					.setCancelable(false)
					.setNeutralButton("Trace",
							new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog,int id) {
							debuglogOpen();
						}
					})
					.setNegativeButton("Cancel",
							new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog,int id) {
							sharedPrefEditor.putBoolean("debugPref", false);
							sharedPrefEditor.commit();
						}
					})
					.setPositiveButton("Verbose",
							new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog,int id) {
							debugCore = true;
							debuglogOpen();
						}
					});
			//show alert dialog
			AlertDialog alertDialog = alertDialogBuilder.create();
			alertDialog.show();}
	}

	private void debuglogOpen() {
		String state = Environment.getExternalStorageState();
		if (!Environment.MEDIA_MOUNTED.equals(state)) {
			msg = "SD card is not availble - cannot create debug log";
			Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_LONG).show();
			return;}

		fileOut = new File(debugFile);
		if (fileOut.exists()){
			fileOut.delete();}

		try {fileOut.createNewFile();
		} catch (IOException e) {
			e.printStackTrace();
			msg = "create debug log file failed";
			Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_LONG).show();
			return;}

		try {
			String currentDateTimeString = DateFormat.getDateTimeInstance().format(new Date());
			fOut = new FileOutputStream(fileOut);
			OutWriter = new OutputStreamWriter(fOut);
			debugFileIsOpen = true;
			debugWrite(132, "+++++++ debug file opened\n");
			debugWrite(132, "=== "+currentDateTimeString);

			debugWrite(132, "=== "+Build.MANUFACTURER);
			debugWrite(132, "=== "+Build.MODEL);
			debugWrite(132, "=== "+Build.VERSION.RELEASE);
			debugWrite(132, "=== Screen DPI:"+screenDPI);
			debugWrite(132, "=== Screen width:"+screenWidth);
			debugWrite(132, "=== Screen height:"+screenHeight);
			debugWrite(132, "\n");

			debugWrite(132, "=== MAIN tab text sizes");
			debugWrite(132, "===   EPO info font: "+Integer.toString(epoMSGfont));
			debugWrite(132, "===   NMEA sel font: "+Integer.toString(mainSPNRfont));
			debugWrite(132, "===   tiny msg font: "+Integer.toString(mainSetMsgfont));
			debugWrite(132, "===   SAVE btn font: "+Integer.toString(mainBtnSVEfont));
			debugWrite(132, "===   btns bar font: "+Integer.toString(mainbtnBARfont));

			debugWrite(132, "=== DOWNLOAD tab text sizes");
			debugWrite(132, "===    buttons font: "+Integer.toString(dnldButtons));
			debugWrite(132, "===      texts font: "+Integer.toString(dnldTexts));

			debugWrite(132, "=== HTML SETTINGS tab text sizes");
			debugWrite(132, "===    buttons font: "+Integer.toString(htmlStngsBtns));
			debugWrite(132, "===      texts font: "+Integer.toString(htmlStngsTxts));

			debugWrite(132, "=== AGPS tab text sizes");
			debugWrite(132, "===    buttons font: "+Integer.toString(agpsButtons));
			debugWrite(132, "===      texts font: "+Integer.toString(agpsTexts));
			debugWrite(132, "\n");


			//AGPS tab
			agpsButtons = Integer.valueOf(sharedPrefs.getString("agpsButtons", "16"));
			agpsTexts = Integer.valueOf(sharedPrefs.getString("agpsTexts", "17"));

			debugWrite(132, "=== CmdTimeout: "+Double.toString(cmdTimeOut));
			debugWrite(132, "=== NMEA default: "+NMEAsettings+"\n");

			msg = "debug log file " + fileOut + " will be created";
			Toast.makeText(mContext, msg, Toast.LENGTH_LONG).show();
		} catch (FileNotFoundException e) {
			e.printStackTrace();}
	}

	static void debugWrite(int len, String dMsg){
		if (debugFileIsOpen){
			if (!dMsg.contains("uncaught")){
				if (dMsg.length() > len ){
					dMsg = dMsg.substring(0, len-1) + " ...";
				}
			}
			Log.d("#### ", dMsg);
			try {
				OutWriter.append(dMsg+"\n");
				OutWriter.flush();
			} catch (IOException e) {
				msg = "error writting to the debug log";
				Toast.makeText(mContext, msg, Toast.LENGTH_LONG).show();
				e.printStackTrace();}}
	}

	static void debugGPSrxts(String dMsg){
		if (!debugCore){
			return;}

		if (debugFileIsOpen){
			Log.d("#### ", dMsg);
			if (dMsg.length() > 80){
				dMsg = dMsg.substring(0, 79) + " ...";
			}
			try {
				OutWriter.append(dMsg+"\n");
				OutWriter.flush();
			} catch (IOException e) {
				msg = "error writting to the debug log";
				Toast.makeText(mContext, msg, Toast.LENGTH_LONG).show();
				e.printStackTrace();}}
	}

	private void screenSizeAbort(){
		debugWrite(132, "MTKutility - screenSizeAbort()");
		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(mContext);
		alertDialogBuilder.setTitle("Phone is not suitable for app");

		// set dialog message
		alertDialogBuilder
		.setMessage("Your phone's screen size is too small. " +
				"The MTK settings screen will not fit\n\n" +
				"press OK to leave this app")
				.setCancelable(false)
				.setPositiveButton("OK",
						new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog,
							int id) {
						MTKutility.this.finish();}
				});
		//show alert dialog
		AlertDialog alertDialog = alertDialogBuilder.create();
		alertDialog.show();
		return;
	}

	private static boolean BTisEnabled(){
		debugWrite(132, "MTKutility - BTisEnabled()");
		// Check if BlueTooth is enabled
		if (!mBluetoothAdapter.isEnabled()) {
			debugWrite(132, "+++ starting BluetoothAdapter");
			Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
			mContext.startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
			return false;}

		return true;
	}

	static boolean btDeviceAvailable(){
		debugWrite(132, "MTKutility - btDeviceAvailable()");
		if (!BTisEnabled()){return false;}

		GPS_bluetooth_id = sharedPrefs.getString("bluetoothListPref", "-1");
		if ("-1".equals(GPS_bluetooth_id) || GPS_bluetooth_id.length() == 0) {
			debugWrite(132, "+++ GPS device isn't set in Preferences");

			AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(mContext);
			alertDialogBuilder.setTitle("Configuration required");

			// set dialog message
			alertDialogBuilder
			.setMessage("MTK device needs to be selected in GPS device configuration\n\n" +
					"Click OK to open app sharedPref\n          or\n" +
					"Exit to leave MTK logger utility")
					.setCancelable(false)
					.setPositiveButton("OK",
							new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog,
								int id) {
							debugWrite(132, "+++ starting PreferencesActivity");
							Intent intent = new Intent(mContext, PreferencesActivity.class);
							mContext.startActivityForResult(intent, REQUEST_CONFIG_PREFS);
							dialog.cancel();}
					})
					.setNegativeButton("Exit",
							new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog,
								int id) {
							mContext.finish();}
						//							MTKutility.mContext.finish();}
					});
			//show alert dialog
			AlertDialog alertDialog = alertDialogBuilder.create();
			alertDialog.show();
			return GPSrxtxSet;
		}

		if (GPSrxtxSet){return true;}

		gpsdev = new GPSrxtx(getmBluetoothAdapter(), GPS_bluetooth_id);
		GPSrxtxSet = true;
		return GPSrxtxSet;
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		debugWrite(132, "MTKutility - onActivityResult("
				+ Integer.toString(requestCode)
				+ "," + Integer.toString(resultCode)
				+ "," + data +")");
		GPS_bluetooth_id = sharedPrefs.getString("bluetoothListPref", "-1");
		if ("-1".equals(GPS_bluetooth_id) || GPS_bluetooth_id.length() == 0) {
			btDeviceAvailable();
		}else{
			gpsdev = new GPSrxtx(getmBluetoothAdapter(), GPS_bluetooth_id);}
		if (requestCode == REQUEST_CODE_PICK_DIR) {
			if (resultCode == Activity.RESULT_OK) {
				String newDir = data.getStringExtra(FilePickerActivity.FullPath);
				if (newDir != null){
					Toast.makeText(this, "New Export Path:\n" + newDir, Toast.LENGTH_LONG).show();
					SharedPreferences sharedPreferences = MTKutility.getSharedPreferences();
					SharedPreferences.Editor editor = sharedPreferences.edit();
					editor.putString("Path", newDir);
					editor.commit();}
			} else {
				Toast.makeText(this, "No Changes Made", Toast.LENGTH_LONG).show();}
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		debugWrite(132, "MTKutility - onCreateOptionsMenu("+menu+")");
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		debugWrite(132, "MTKutility - onOptionsItemSelected("+item+")");
		switch (item.getItemId()) {
		case R.id.menuitem_preferences:
			if (!BTisEnabled()){return false;}
			Intent prefIntent = new Intent(this, PreferencesActivity.class);
			startActivity(prefIntent);
			return true;
		case R.id.menuitem_about:    // Add the fragment to the activity, pushing this transaction
			// on to the back stack.
			aboutIsActive = true;
			aboutXMLfile = "file:///android_asset/MTKutilityHelp.html";
			FragmentTransaction ft = getFragmentManager().beginTransaction();
			ft.replace(android.R.id.content, new aboutFragment());
			ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
			ft.addToBackStack(null);
			ft.commit();
			return true;}
		//		case R.id.menuitem_exit:
		//			closeApp();
		//			return true;}

		return false;
	}

	// onSaveInstanceState() is used to "remember" the current state when a
	// configuration change occurs such screen orientation change. This
	// is not meant for "long term persistence". We store the tab navigation

	private void closeApp(){ 
		NMEAoutStart();
		if (debugFileIsOpen){
			debugWrite(132, "MTKutility - closeApp()");
			debugWrite(132, "+++++++ closing debug file");
			sharedPrefEditor.putBoolean("debugPref", false);
			sharedPrefEditor.commit();
			try {OutWriter.close(); fOut.close();
			} catch (IOException e) {e.printStackTrace();}}

		//		Toast.makeText(this, getString(R.string.ui_menu_exit),
		//				Toast.LENGTH_SHORT).show();
		System.exit(0);
	}

	static void readLogCat(){
		try {
			Process process = Runtime.getRuntime().exec("logcat -d *:E");
			BufferedReader bufferedReader = new BufferedReader(
					new InputStreamReader(process.getInputStream()));

			String line = "";
			while ((line = bufferedReader.readLine()) != null) {
				debugWrite(999, line);
			}
		} 
		catch (IOException e) {}
	}	//readLogCat()

	static void abortApp(String msg){ 
		if (debugFileIsOpen){
			debugWrite(132, "MTKutility - closeApp()");
			debugWrite(132, "+++++++ closing debug file");
			sharedPrefEditor.putBoolean("debugPref", false);
			sharedPrefEditor.commit();
			try {
				OutWriter.close();
				fOut.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		AlertDialog.Builder alert = new AlertDialog.Builder(mContext);
		alert.setTitle("fatal error")
		.setMessage(msg + "\napp execution is being aborted" +
				"\npress OK to leave this app")
				.setCancelable(false)
				.setPositiveButton("OK", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int id) {
						System.exit(0);}})
						.create()
						.show();
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		debugWrite(132, "MTKutility - onSaveInstanceState("+outState+")");
		super.onSaveInstanceState(outState);
		outState.putInt(TAB_KEY_INDEX, getActionBar()
				.getSelectedNavigationIndex());
	}

	static void showTabs(){
		debugWrite(132, "MTKutility - showTabs()");
		actionbar.removeTab(tabGPX);

		if (hasLOG){
			actionbar.addTab(tabDNLD);
		}

		if(hasAGPS){
			actionbar.addTab(tabAGPS);		
		}

		if (hasLOG){
			if (useHTMLsettings) {
				actionbar.addTab(tabHset);
			}else {
				actionbar.addTab(tabLset);
			}
		}
	}

	static void hidetabs(){
		debugWrite(132, "MTKutility - hidetabs()");
		if(hasAGPS){
			actionbar.removeTab(tabAGPS);
		}

		if (hasLOG){
			actionbar.removeTab(tabDNLD);
			if (useHTMLsettings) {
				actionbar.removeTab(tabHset);
			}else {
				actionbar.removeTab(tabLset);
			}
		}

		actionbar.addTab(tabGPX);
	}

	static SharedPreferences getSharedPreferences() {
		debugWrite(132, "MTKutility - getSharedPreferences()");
		return sharedPrefs;
	}

	static void notconnected(Context context){
		debugWrite(132, "MTKutility - notconnected()");
		msg = String.format("Not connected to %s", gpsdev.btName);
		Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
	}

	static String getEPOinfo(){
		debugWrite(132, "MTKutility - getEPOinfo()");
		SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy HH:mm:ss", Locale.CANADA);
		StringBuilder txtOut = new StringBuilder() ;
		String[] prms = new String[10];
		if (renewAGPS){
			if (hasAGPS){
				prms = mtkCmd("PMTK607", "PMTK707", cmdTimeOut);
				if (prms == null){
					EPOblks = -1;
				} else {
					EPOblks = Integer.valueOf(prms[1]);
					txtOut.append(prms[1] + " blocks AGPS data");
				}
				if (EPOblks > 0) {
					Date dd = calcDate(Integer.valueOf(prms[4]),Integer.valueOf(prms[5]));
					txtOut.append(" expires " + sdf.format(dd));
				}
				renewAGPS = false;
			}

			if (hasLOG){
				if (txtOut.length() > 0){
					txtOut.append(BR);
				}
				prms = mtkCmd("PMTK182,2,10", "PMTK182,3,10", cmdTimeOut);
				if (prms != null){
					BINrecs = Long.parseLong(prms[3], 16);
					txtOut.append(Long.toString(BINrecs) + " log records");
				}
				renewAGPS = false;
			}
		}
		if (txtOut.length() > 0){
			EPOinfo = txtOut.toString();	
		}
		debugWrite(132, ">>>>>> " + EPOinfo);
		return EPOinfo;
	}

	static GPSrxtx getBTconnect(){
		debugWrite(132, "MTKutility - getBTconnect()");
		return gpsdev;
	}

	public static BluetoothAdapter getmBluetoothAdapter() {
		debugWrite(132, "MTKutility - getmBluetoothAdapter()");
		return mBluetoothAdapter;
	}

	static void connectBT(Context context) {
		debugWrite(132, "MTKutility - connectBT()");
		connected = false;
		if (!gpsdev.connect()) {
			msg = String.format("Connect to GPS device: %s failed", gpsdev.btName);
			debugWrite(132, "*** "+msg);
			Toast.makeText(mContext, msg, Toast.LENGTH_LONG).show();
			return;
		}else {
			//send binary command to cancel MTK Binary Protocol - restores normal command
			//mode when an AGPS data upload fails and leaves the MTK in binary mode
			debugWrite(132, "*** sending: binPMTK253");
			try { gpsdev.sendBytes(binPMTK253);}
			catch (IOException e2) {
				e2.printStackTrace();
				msg = String.format("Communication with GPS device: %s failed", gpsdev.btName);
				debugWrite(132, "*** "+msg);
				Toast.makeText(mContext, msg, Toast.LENGTH_LONG).show();
				return;}
			goSleep(300);
			connected = true;
			msg = String.format("Connected to GPS device: %s", gpsdev.btName);
			debugWrite(132, "*** "+msg);
			Toast.makeText(mContext, msg, Toast.LENGTH_LONG).show();
			determineGPSfunctions();
			NMEAoutStart();
			//			refreshEPOinfo();
		}
	}

	private static void determineGPSfunctions() {
		debugWrite(132, "MTKutility - determineGPSfunctions()");

		// request flash ID 
		hasLOG = true;
		parms = mtkCmd("PMTK182,2,9","PMTK182,3,9,", cmdTimeOut);
//		debugWrite(132, ">>>> MTKutility PMTK182,2,9 parms: " + Arrays.toString(parms));
		flashSize = 0;
		if (parms == null) {
			hasLOG = false;
			debugWrite(132, ">>>>>> PMTK182,2,9 returned null");
			msg = "Command to determine if this is a GPS logger failed";
			Toast.makeText(mContext, msg, Toast.LENGTH_LONG).show();
		}else{
			if ((parms[0].equals("PMTK001")) && (parms[2].equals("1"))){
				hasLOG = false;
				debugWrite(132, ">>>>>> GPS device does not have Logging");
			}
		}
		

		// check for AGPS
		hasAGPS = true;
		parms = mtkCmd("PMTK607", "PMTK707", cmdTimeOut);
		debugWrite(132, ">>>>>> PMTK707 parms: " + Arrays.toString(parms));
		if (parms == null) {
			hasAGPS = false;
			debugWrite(132, ">>>>>> PMTK607 returned null");
			msg = "Command to determine if this GPS has AGPS failed";
			Toast.makeText(mContext, msg, Toast.LENGTH_LONG).show();
		}else{
			if ((parms[0].equals("PMTK001")) && (parms[2].equals("1"))){
				hasAGPS = false;
				debugWrite(132, ">>>>>> GPS device does not have AGPS");
			}
		}

		if (hasLOG){
			// get GPS device info
			flashSize = 32 * 1024 * 1024 / 8; //a safe default flash size
			parms = mtkCmd("PMTK605","PMTK705", cmdTimeOut);
//			debugWrite(132, ">>>> PMTK705 parms: " + Arrays.toString(parms));
			if (parms == null){
				debugWrite(132, ">>>>>> PMTK605 returned null");
			}else{
				if (parms[0].contains("PMTK705")){
					flashSize = getFlashSize((int) Long.parseLong(parms[2],16));
				}
			}
			debugWrite(132, ">>>>>> flashsize is " + flashSize);
		}
	}

	static void NMEAoutStop(){
		debugWrite(132, "MTKutility - NMEAoutStop()");
		int loops = 0;
		String[] parms = null;
		//stop NMEA output
		loops = 50;
		do {loops--;
		parms = mtkCmd("PMTK314,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0","PMTK001,314", cmdTimeOut);
		} while (parms == null && loops > 0);
	}

	static void NMEAoutStart(){
		debugWrite(132, "MTKutility - NMEAoutStart()");
		//		sharedPrefs = PreferenceManager.getDefaultSharedPreferences(mContext);
		NMEAsettings = appPrefs.getString("NMEAsettings", "");
		if (NMEAsettings == "") {
			//get current NMEA output settings
			parms = mtkCmd("PMTK414","PMTK514", cmdTimeOut);
			if (parms == null) {
				NMEAsettings = "0,1,0,1,1,1,0,0,0,0,0,0,0,0,0,0,0,0,0";
				sendPMTK314();
			} else {
				StringBuilder builder = new StringBuilder();
				int ix = 0;
				for(String s : parms) {
					if (ix != 0){
						builder.append(",");
						builder.append(s);}
					ix++;}
				NMEAsettings = builder.toString();
				if (NMEAsettings.matches(",0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0")) {
					NMEAsettings = ",0,1,0,1,1,1,0,0,0,0,0,0,0,0,0,0,0,0,0";
					sendPMTK314();
				}
			}
			appPrefEditor.putString("NMEAsettings", NMEAsettings);
			appPrefEditor.commit();}
	}

	private static void sendPMTK314() {
		debugWrite(132, "*** sending: PMTK314" + NMEAsettings);
		try {gpsdev.sendCommand("PMTK314" + NMEAsettings);
		} catch (IOException e) {
			debugWrite(132, "*** PMTK314 failed: "+e);
			e.printStackTrace();}
	}

	static String[] mtkCmd(String mtkCmd, String mtkRep, double timeout){
		debugWrite(132, "MTKutility - mtkCmd("+mtkCmd+"|"+mtkRep+"|"+Double.toString(timeout)+")");
		String[] sArray = new String[99];
		boolean ok = true;

		debugWrite(132, "*** sending: " + mtkCmd);
		try { gpsdev.sendCommand(mtkCmd);} 
		catch (IOException e2) {ok = false;
		debugWrite(132, "IOException: " + e2);
		e2.printStackTrace();}

		if (ok){
			debugWrite(132, "*** waiting for: " + mtkRep);
			try { reply = gpsdev.waitForReply(mtkRep, timeout);}
			catch (IOException e2) {
				ok = false;
				debugWrite(132, "IOException: " + e2);
				e2.printStackTrace();
			}
			catch (InterruptedException e2) {
				ok = false;
				debugWrite(132, "InterruptedException: " + e2);
				e2.printStackTrace();
			}
			if (reply == null){
				ok = false;
			} else {
				// is replay a command failure response?
				if (reply.contains("PMTK001")) {
					mtkRep = "PMTK001";
				}
			}
		}

		if (ok){
			int bgn = reply.indexOf(mtkRep);
			int end = reply.indexOf("*", bgn);
			reply = reply.substring(bgn, end);
			debugWrite(132, "*** received: " + reply);
			sArray = reply.split(",");
			return sArray;
		} else {
			debugWrite(132, "*** no response for: " + mtkRep);
			return null;}
	}


	static int getFlashSize (int model) {
		debugWrite(132, "MTKutility - getFlashSize()");
		// 8 Mbit = 1 Mb
		if (model == 0x1388) return( 8 * 1024 * 1024 / 8); // 757/ZI v1
		if (model == 0x5202) return( 8 * 1024 * 1024 / 8); // 757/ZI v2
		// 32 Mbit = 4 Mb
		if (model == 0x0000) return(32 * 1024 * 1024 / 8); // Holux M-1200E
		if (model == 0x0001) return(32 * 1024 * 1024 / 8); // Qstarz BT-Q1000X
		if (model == 0x0004) return(32 * 1024 * 1024 / 8); // 747 A+ GPS Trip Recorder
		if (model == 0x0005) return(32 * 1024 * 1024 / 8); // Qstarz BT-Q1000P
		if (model == 0x0006) return(32 * 1024 * 1024 / 8); // 747 A+ GPS Trip Recorder
		if (model == 0x0008) return(32 * 1024 * 1024 / 8); // Pentagram PathFinder P 3106
		if (model == 0x000F) return(32 * 1024 * 1024 / 8); // 747 A+ GPS Trip Recorder
		if (model == 0x005C) return(32 * 1024 * 1024 / 8); // Holux M-1000C
		if (model == 0x8300) return(32 * 1024 * 1024 / 8); // Qstarz BT-1200
		// 16Mbit -> 2Mb
		// 0x0051    i-Blue 737, Qstarz 810, Polaris iBT-GPS, Holux M1000
		// 0x0002    Qstarz 815
		// 0x001B    i-Blue 747
		// 0x001d    BT-Q1000 / BGL-32
		// 0x0131    EB-85A
		return(16 * 1024 * 1024 / 8);
	}

	private static Date calcDate(int weeks, int secs){
		debugWrite(132, "MTKutility - calcDate()");
		Calendar wrkCal = new GregorianCalendar(1980,0,6,0,0,0);
		wrkCal.add(Calendar.DATE, weeks*7);
		wrkCal.add(Calendar.SECOND,secs);
		return wrkCal.getTime();
	}

	static StringBuilder setSerialDataToTextView(int disp, byte[] rbuf, int len, String sCr, String sLf) {
		debugWrite(132, "MTKutility - setSerialDataToTextView()");
		StringBuilder sText = new StringBuilder();
		boolean lastDataIs0x0D = false;

		for (int i = 0; i < len; ++i) {
			// "\r":CR(0x0D) "\n":LF(0x0A)
			if ((mReadLinefeedCode == LINEFEED_CODE_CR) && (rbuf[i] == 0x0D)) {
				sText.append(sCr);
				sText.append(BR);
			} else if ((mReadLinefeedCode == LINEFEED_CODE_LF) && (rbuf[i] == 0x0A)) {
				sText.append(sLf);
				sText.append(BR);
			} else if ((mReadLinefeedCode == LINEFEED_CODE_CRLF) && (rbuf[i] == 0x0D)
					&& (rbuf[i + 1] == 0x0A)) {
				sText.append(sCr);
				if (disp != DISP_CHAR) {
					sText.append(" ");}
				sText.append(sLf);
				sText.append(BR);
				++i;
			} else if ((mReadLinefeedCode == LINEFEED_CODE_CRLF) && (rbuf[i] == 0x0D)) {
				// case of rbuf[last] == 0x0D and rbuf[0] == 0x0A
				sText.append(sCr);
				lastDataIs0x0D = true;
			} else if (lastDataIs0x0D && (rbuf[0] == 0x0A)) {
				if (disp != DISP_CHAR) {
					sText.append(" ");}
				sText.append(sLf);
				sText.append(BR);
				lastDataIs0x0D = false;
			} else if (lastDataIs0x0D && (i != 0)) {
				// only disable flag
				lastDataIs0x0D = false;
				--i;
			} else {
				sText.append((char) rbuf[i]);
			}
		}
		return sText;
	}

	static void goSleep (int mSec) {
		debugWrite(132, "MTKutility - goSleep(" + Integer.toString(mSec) + ")");
		try {Thread.sleep(mSec);
		} catch (InterruptedException e) {e.printStackTrace();}
	}

	@Override
	public void onBackPressed() {
		debugWrite(132, "MTKutility - onBackPressed()");
		if (aboutIsActive){
			aboutIsActive = false;
			FragmentTransaction ft = getFragmentManager().beginTransaction();
			ft.replace(R.id.fragment_container, activeFragment);
			ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
			ft.addToBackStack(null);
			ft.commit();
			//			super.onBackPressed(); 
		}else{
			if (back_pressed + 2000 > System.currentTimeMillis()){
				closeApp();
				super.onBackPressed();
			}else{
				Toast.makeText(getBaseContext(), "press back again to exit MTKutility", Toast.LENGTH_SHORT).show();
			}
			back_pressed = System.currentTimeMillis();
		}
	}

	public void onCheckboxClicked(View view) {
		debugWrite(132, "MTKutility - onCheckboxClicked()");
		//MTKlogSettings layout check box handler
		switch(view.getId()) {
		case R.id.cbxDate: MTKlogSettings.cbxDate(); break;
		case R.id.cbxMili: MTKlogSettings.cbxMili(); break;
		case R.id.cbxLat:  MTKlogSettings.cbxLat(); break;
		case R.id.cbxLon:  MTKlogSettings.cbxLon(); break;
		case R.id.cbxHei:  MTKlogSettings.cbxHei(); break;
		case R.id.cbxSpd:  MTKlogSettings.cbxSpd(); break;
		case R.id.cbxHed:  MTKlogSettings.cbxHed(); break;
		case R.id.cbxDis:  MTKlogSettings.cbxDis(); break;
		case R.id.cbxRCR:  MTKlogSettings.cbxRCR(); break;
		case R.id.cbxVal:  MTKlogSettings.cbxVal(); break;
		case R.id.cbxFxo:  MTKlogSettings.cbxFxo(); break;
		case R.id.cbxNsat: MTKlogSettings.cbxNsat(); break;
		case R.id.cbxSID:  MTKlogSettings.cbxSID(); break;
		case R.id.cbxEle:  MTKlogSettings.cbxEle(); break;
		case R.id.cbxAzi:  MTKlogSettings.cbxAzi(); break;
		case R.id.cbxSNR:  MTKlogSettings.cbxSNR(); break;
		case R.id.cbxDSTA: MTKlogSettings.cbxDSTA(); break;
		case R.id.cbxDAGE: MTKlogSettings.cbxDAGE(); break;
		case R.id.cbxPDOP: MTKlogSettings.cbxPDOP(); break;
		case R.id.cbxHDOP: MTKlogSettings.cbxHDOP(); break;
		case R.id.cbxVDOP: MTKlogSettings.cbxVDOP(); break;
		}
		MTKlogSettings.setMsgFields();
	}
}

// TabListenr class for managing user interaction with the ActionBar tabs. The
// application context is passed in pass it in constructor, needed for the
// toast.

class MyTabsListener implements ActionBar.TabListener {
	public Fragment fragment;
	public Context context;

	public MyTabsListener(Fragment fragment, Context context) {
		this.fragment = fragment;
		this.context = context;
	}

	@Override
	public void onTabReselected(Tab tab, FragmentTransaction ft) {
		//		Toast.makeText(context, "Reselected!", Toast.LENGTH_SHORT).show();
	}

	@Override
	public void onTabSelected(Tab tab, FragmentTransaction ft) {
		//		Toast.makeText(context, "Selected!", Toast.LENGTH_SHORT).show();
		MTKutility.activeFragment = fragment;
		ft.replace(R.id.fragment_container, fragment);
	}

	@Override
	public void onTabUnselected(Tab tab, FragmentTransaction ft) {
		//		Toast.makeText(context, "Unselected!", Toast.LENGTH_SHORT).show();
		ft.remove(fragment);
	}
}