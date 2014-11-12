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
import java.util.Set;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.os.Environment;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.MultiSelectListPreference;
import android.preference.Preference;
import android.preference.PreferenceScreen;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import android.util.Log;
import android.widget.Toast;

public class PreferencesFragment extends PreferenceFragment
implements OnSharedPreferenceChangeListener {

	private BluetoothAdapter mBluetoothAdapter = null;
	private String PathName = "";
	private final int REQUEST_CODE_BIN_DIR = 1;
	private final int REQUEST_CODE_GPX_DIR = 2;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		MTKutility.debugWrite(132, "PreferencesFragment-onCreate()");
		addPreferencesFromResource(R.xml.pref_epo_source);

		// Populate the listPreference with all the bluetooth devices
		ListPreference customPref = (ListPreference) findPreference("bluetoothListPref");
		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
		// If there are paired devices
		if (pairedDevices.size() > 0) {
			// Loop through paired devices
			CharSequence[] entries = new CharSequence[pairedDevices.size()];
			CharSequence[] entrieValues = new CharSequence[pairedDevices.size()];
			int i = 0;
			for (BluetoothDevice device : pairedDevices) {
				// Add the name and address to an array adapter to show in a
				// ListView
				entries[i] = device.getName();
				entrieValues[i] = device.getAddress();
				if (entries[i] == null) {
					entries[i] = String.format("Device%02d", i);} 
				i++;}
			customPref.setEntries(entries);
			customPref.setEntryValues(entrieValues);
		}else {
			customPref.setEnabled(false);}

		PathName = MTKutility.getSharedPreferences().getString("BINpath", Environment.getExternalStorageDirectory().toString() );
		Preference pathPref = findPreference("BINpath");
		pathPref.setSummary(PathName);

		PathName = MTKutility.getSharedPreferences().getString("GPXpath", Environment.getExternalStorageDirectory().toString() );
		pathPref = findPreference("GPXpath");
		pathPref.setSummary(PathName);
	}	//onCreate()

	@Override
	public void onResume() {
		super.onResume();
		MTKutility.debugWrite(132, "PreferencesFragment-onResume()");

		// Set up a listener whenever a key changes
		getPreferenceScreen().getSharedPreferences()
		.registerOnSharedPreferenceChangeListener(this);

		initSummary();
	}	//onResume()

	@Override
	public void onPause() {
		super.onPause();
		MTKutility.debugWrite(132, "PreferencesFragment-onPause()");

		// Unregister the listener whenever a key changes
		getPreferenceScreen().getSharedPreferences()
		.unregisterOnSharedPreferenceChangeListener(this);
		//if (mRingtoneOnListener!=null) mRingtoneOnListener.setOnPreferenceChangeListener(null); //Pay attention: it is just an example
	}	//onPause()

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
		MTKutility.debugWrite(132, "PreferencesFragment-onSharedPreferenceChanged()");
		// Update summary
		updatePrefsSummary(sharedPreferences, findPreference(key));
	}	//onSharedPreferenceChanged()

	protected void updatePrefsSummary(SharedPreferences sharedPreferences, Preference pref) {
//		MTKutility.debugWrite(132, "PreferencesFragment-updatePrefsSummary()");
		if (pref == null)
			return;

		if (pref instanceof ListPreference) {
			// List Preference
			ListPreference listPref = (ListPreference) pref;
			listPref.setSummary(listPref.getEntry());

		} else if (pref instanceof EditTextPreference) {
			// EditPreference
			EditTextPreference editTextPref = (EditTextPreference) pref;
			editTextPref.setSummary(editTextPref.getText());

		} else if (pref instanceof MultiSelectListPreference) {
			// MultiSelectList Preference
			MultiSelectListPreference mlistPref = (MultiSelectListPreference) pref;
			String summaryMListPref = "";
			String and = "";

			// Retrieve values
			Set<String> values = mlistPref.getValues();
			for (String value : values) {
				// For each value retrieve index
				int index = mlistPref.findIndexOfValue(value);
				// Retrieve entry from index
				CharSequence mEntry = index >= 0
						&& mlistPref.getEntries() != null ? mlistPref
								.getEntries()[index] : null;
								if (mEntry != null) {
									// add summary
									summaryMListPref = summaryMListPref + and + mEntry;
									and = ";";}
			}
			// set summary
			mlistPref.setSummary(summaryMListPref);
		}
	}	//updatePrefsSummary()

	protected void initSummary() {
		MTKutility.debugWrite(132, "PreferencesFragment-initSummary()");
		int pcsCount=getPreferenceScreen().getPreferenceCount();
		for (int i = 0; i < pcsCount; i++) {
			initPrefsSummary(getPreferenceManager().getSharedPreferences(),
					getPreferenceScreen().getPreference(i));}
	}	//initSummary()

	protected void initPrefsSummary(SharedPreferences sharedPreferences, Preference p) {
//		MTKutility.debugWrite(132, "PreferencesFragment-initPrefsSummary()");
		if (p instanceof PreferenceCategory) {
			PreferenceCategory pCat = (PreferenceCategory) p;
			int pcCatCount= pCat.getPreferenceCount();
			for (int i = 0; i < pcCatCount; i++) {
				initPrefsSummary(sharedPreferences, pCat.getPreference(i));}
		} else {
			updatePrefsSummary(sharedPreferences, p);}

	}	//initPrefsSummary()

	@Override
	public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
		MTKutility.debugWrite(132, "PreferencesFragment-onPreferenceTreeClick()");
		String LOGTAG = "sharedPref";
		Log.d(LOGTAG, "Clicked on preference tree: "+preference.getKey());
		boolean foldersOnly;

		if (preference.getKey().compareTo("BINpath") == 0) {
			// Start the file chooser here
			Log.d(LOGTAG, "Start browsing button pressed");
			foldersOnly = MTKutility.getSharedPreferences().getBoolean("foldersOnly", false);
			Intent fileExploreIntent = new Intent(getActivity(), FilePickerActivity.class);
			fileExploreIntent.putExtra(FilePickerActivity.FullPath, MTKutility.rootDirectory);
			fileExploreIntent.putExtra(FilePickerActivity.ShowHidden, false);
			fileExploreIntent.putExtra(FilePickerActivity.FoldersOnly, foldersOnly);
			startActivityForResult(fileExploreIntent, REQUEST_CODE_BIN_DIR);
			return true;
		}

		if (preference.getKey().compareTo("GPXpath") == 0) {
			// Start the file chooser here
			Log.d(LOGTAG, "Start browsing button pressed");
			foldersOnly = false;
			Intent fileExploreIntent = new Intent(getActivity(), FilePickerActivity.class);
			fileExploreIntent.putExtra(FilePickerActivity.FullPath, MTKutility.rootDirectory);
			fileExploreIntent.putExtra(FilePickerActivity.ShowHidden, false);
			fileExploreIntent.putExtra(FilePickerActivity.FoldersOnly, foldersOnly);
			startActivityForResult(fileExploreIntent, REQUEST_CODE_GPX_DIR);
			return true;
		}

		return false;
	}	//onPreferenceTreeClick()

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		MTKutility.debugWrite(132, "PreferencesFragment-onActivityResult()");
		//		Log.d(LOGTAG, "onActivityResult("+requestCode+","+resultCode+",...)");
		SharedPreferences sharedPreferences = MTKutility.getSharedPreferences();
		SharedPreferences.Editor editor = sharedPreferences.edit();
		
		if (requestCode == REQUEST_CODE_BIN_DIR) {
			if (resultCode == Activity.RESULT_OK) {
				String newDir = data.getStringExtra(FilePickerActivity.FullPath);
				if (newDir != null){
					Toast.makeText(getActivity(), "New BIN path:\n" + newDir, Toast.LENGTH_LONG).show();
					editor.putString("BINpath", newDir);
					editor.commit();
					Preference pathPref = findPreference("BINpath");
					pathPref.setSummary(newDir);}
			} else {
				Toast.makeText(getActivity(), "No Changes Made", Toast.LENGTH_LONG).show();
			}
		}
		
		if (requestCode == REQUEST_CODE_GPX_DIR) {
			if (resultCode == Activity.RESULT_OK) {
				String newDir = data.getStringExtra(FilePickerActivity.FullPath);
				if (newDir != null){
					Toast.makeText(getActivity(), "New GPX path:\n" + newDir, Toast.LENGTH_LONG).show();
					editor.putString("GPXpath", newDir);
					editor.commit();
					Preference pathPref = findPreference("GPXpath");
					pathPref.setSummary(newDir);}
			} else {
				Toast.makeText(getActivity(), "No Changes Made", Toast.LENGTH_LONG).show();
			}
		}

		super.onActivityResult(requestCode, resultCode, data);
	}	//onActivityResult()
}
