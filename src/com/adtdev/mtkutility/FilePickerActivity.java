/*
 * Copyright 2014 Al Tauber
 * 
 * Code modified to enable selection with a long press. Icons changed
 * to make it easier to differentiate files and folders.
 *
 * Copyright 2011 Anders Kalør
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.adtdev.mtkutility;

import com.adtdev.mtkutility.R;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
//import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
//import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
//import android.widget.LinearLayout;
//import android.widget.RelativeLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class FilePickerActivity extends ListActivity {

	public final static String FullPath    = "fullpath";
	public final static String FileName    = "filename";
	public final static String FoldersOnly = "foldersonly";
	public final static String ShowHidden  = "showhidden";
	public final static String LimitParent = "limitparent";
	public final static String ExtensionsFilter = "acceptedextensions";

	protected File fullPath;
	protected File fileName;
	protected String StartDirectory;
	protected ArrayList<File> mFiles;
	protected FilePickerListAdapter FPadapter;
	protected String[] acceptedFileExtensions;

	protected boolean ShowHiddenFiles = false;
	protected boolean ShowFoldersOnly = false;
	protected boolean LimitParentSlct = true;

	static File newFile;
	static String absPath;

	private TextView fpPath;
	private Button   fpExit;
	private Button   fpUp;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Set initial directory
		fullPath = new File("/");
		// Initialize the extensions array to allow any file extensions
		acceptedFileExtensions = new String[] {};

		// Get intent extras
		if(getIntent().hasExtra(FullPath)) {
			fullPath = new File(getIntent().getStringExtra(FullPath));}
		StartDirectory = fullPath.getPath();

		if(getIntent().hasExtra(LimitParent)) {
			LimitParentSlct = getIntent().getBooleanExtra(LimitParent, false);}

		if(getIntent().hasExtra(FoldersOnly)) {
			ShowFoldersOnly = getIntent().getBooleanExtra(FoldersOnly, false);}

		if(getIntent().hasExtra(ShowHidden)) {
			ShowHiddenFiles = getIntent().getBooleanExtra(ShowHidden, false);}

		if(getIntent().hasExtra(ExtensionsFilter)) {
			ArrayList<String> collection = getIntent().getStringArrayListExtra(ExtensionsFilter);
			acceptedFileExtensions = collection.toArray(new String[collection.size()]);}

		//set up list view header
		View header = View.inflate(getApplication(), R.layout.file_picker_header, null);

		fpExit = (Button) header.findViewById(R.id.fpExit);
		fpExit.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
//				getIntent().removeExtra(FileName);
//				getIntent().putExtra(FileName, "");
				setResult(RESULT_CANCELED, getIntent());
				finish();
			}
		});

		fpUp = (Button) header.findViewById(R.id.fpUp);
		fpUp.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				goParent();}
		});

		fpPath = (TextView)header.findViewById(R.id.fpPath); 
		fpPath.setText(fullPath.toString());

		ListView list = getListView();
		list.addHeaderView(header);
//		list.setChoiceMode(ListView.CHOICE_MODE_SINGLE);

		// Initialize the ArrayList
		mFiles = new ArrayList<File>();
		// Set the ListAdapter
		FPadapter = new FilePickerListAdapter(this, mFiles);
		setListAdapter(FPadapter);

		list.setOnItemLongClickListener(new OnItemLongClickListener() {
			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view,
					int position, long id) {
				File newFile = (File) parent.getItemAtPosition(position);
				Intent extra = new Intent();
				if (newFile.isFile()){
					extra.putExtra(FileName, newFile.getName());
				}else{
					extra.putExtra(FileName, "");
				}
				extra.putExtra(FullPath, newFile.getAbsolutePath());
				setResult(RESULT_OK, extra);
				finish();
				return true;
			}
		});

//		fpExit.setOnClickListener(new View.OnClickListener() {
//			@Override
//			public void onClick(View v){
//				Intent extra = new Intent();
//				setResult(RESULT_CANCELED, extra);
//				finish();
//				return;
//			}
//		});
	}

	@Override
	protected void onResume() {
		refreshFilesList();
		super.onResume();
	}

	/**
	 * Updates the list view to the current directory
	 */
	protected void refreshFilesList() {
		// Clear the files ArrayList
		mFiles.clear();

		// Set the extension file filter
		ExtensionFilenameFilter filter = new ExtensionFilenameFilter(acceptedFileExtensions);

		// Get the files in the directory
		fpPath.setText(fullPath.getAbsolutePath().toString());
		File[] files = fullPath.listFiles(filter);

		if(files != null && files.length > 0) {
			for(File f : files) {
				if(f.isHidden() && !ShowHiddenFiles) {
					// Don't add the file
					continue;}
				if (f.isFile() && ShowFoldersOnly){
					// Don't add the file
					continue;}				
				// Add the file the ArrayAdapter
				mFiles.add(f);
			}
			Collections.sort(mFiles, new FileComparator());
		}
		if (mFiles.isEmpty()){
			Toast.makeText(getApplication(), "fullPath is empty",Toast.LENGTH_SHORT).show();}
		FPadapter.notifyDataSetChanged();
	}

	@Override
	public void onBackPressed() {
		if((StartDirectory.matches(fullPath.getPath()))
				||(fullPath.getParentFile() == null)){
			Intent extra = new Intent();
			setResult(RESULT_CANCELED, extra);
			finish();
			return;
		}else {
			// Go to parent directory
			fullPath = fullPath.getParentFile();
			refreshFilesList();
			return;}
	}

	public void goParent() {
		if (LimitParentSlct && StartDirectory.matches(fullPath.getPath())){return;}
		if(fullPath.getParentFile() != null) {
			// Go to parent directory
			fullPath = fullPath.getParentFile();
			refreshFilesList();}
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		File newFile = (File) l.getItemAtPosition(position);

		if(newFile.isDirectory()) {
			fullPath = newFile;
			// Update the files list
			refreshFilesList();}

		super.onListItemClick(l, v, position, id);
	}

	private class FilePickerListAdapter extends ArrayAdapter<File> {

		private List<File> mObjects;

		public FilePickerListAdapter(Context context, List<File> objects) {
			super(context, R.layout.file_picker_listview, android.R.id.text1, objects);
			mObjects = objects;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View row = null;
			if(convertView == null) { 
				LayoutInflater inflater = (LayoutInflater)getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				row = inflater.inflate(R.layout.file_picker_listview, parent, false);
			} else {
				row = convertView;}

			File object = mObjects.get(position);
			ImageView imageView = (ImageView)row.findViewById(R.id.file_picker_image);
			TextView textView = (TextView)row.findViewById(R.id.file_picker_text);
			// Set single line
			textView.setSingleLine(true);

			textView.setText(object.getName());
			if(object.isFile()) {
				// Show the file icon
				imageView.setImageResource(R.drawable.file);
			} else {
				// Show the folder icon
				imageView.setImageResource(R.drawable.folder);}
			return row;
		}

	}

	private class FileComparator implements Comparator<File> {
		@Override
		public int compare(File f1, File f2) {
			if(f1 == f2) {
				return 0;}
			if(f1.isDirectory() && f2.isFile()) {
				// Show directories above files
				return -1;}
			if(f1.isFile() && f2.isDirectory()) {
				// Show files below directories
				return 1;}
			// Sort the directories alphabetically
			return f1.getName().compareToIgnoreCase(f2.getName());
		}
	}

	private class ExtensionFilenameFilter implements FilenameFilter {
		private String[] mExtensions;

		public ExtensionFilenameFilter(String[] extensions) {
			super();
			mExtensions = extensions;
		}

		@Override
		public boolean accept(File dir, String filename) {
			if(new File(dir, filename).isDirectory()) {
				// Accept all directory names
				return true;}
			if(mExtensions != null && mExtensions.length > 0) {
				for(int i = 0; i < mExtensions.length; i++) {
					if(filename.endsWith(mExtensions[i])) {
						// The filename ends with the extension
						return true;}
				}
				// The filename did not match any of the extensions
				return false;
			}
			// No extensions has been set. Accept all file extensions.
			return true;
		}
	}
}
