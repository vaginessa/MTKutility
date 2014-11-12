/**
 * @author Bastiaan Naber (AndroidMTK app - GNU GPL v3 license)
 * https://code.google.com/p/androidmtk/
 * 
 * modified by Al Tauber
 * - rewrote waitForReply to fix reply not caught when it starts the block read
 * - added readString, sendBytes,
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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.util.Log;

public class GPSrxtx {
	boolean debug = android.os.Debug.isDebuggerConnected();
	private static final String TAG = "== GPSrxtx";
	private final static String BR = System.getProperty("line.separator");
	private String logString;
	private String msg;

	public InputStream in = null;
	public OutputStream out = null;
	public String btName;

	public BluetoothAdapter mBluetoothAdapter = null;
	public BluetoothSocket sock = null;

	private String dev_id;
	private StringBuilder buffer = new StringBuilder();

	public GPSrxtx(BluetoothAdapter adapter, String gpsdev) {
		mBluetoothAdapter = adapter;
		dev_id = gpsdev;
	}

	public boolean connect() {
		debugLog(TAG, "++++ connect()");
		BluetoothDevice zee = mBluetoothAdapter.getRemoteDevice(dev_id);
		btName = zee.getName();
		Method m = null;

		try {m = zee.getClass().getMethod("createRfcommSocket", new Class[] { int.class });
		} catch (SecurityException e1) {
			e1.printStackTrace();
			return false;
		} catch (NoSuchMethodException e1) {
			e1.printStackTrace();
			return false;}

		try {sock = (BluetoothSocket)m.invoke(zee, Integer.valueOf(1));
		} catch (IllegalArgumentException e1) {
			e1.printStackTrace();
			return false;
		} catch (IllegalAccessException e1) {
			e1.printStackTrace();
			return false;
		} catch (InvocationTargetException e1) {
			e1.printStackTrace();
			return false;}

		try {sock.connect();
		} catch (IOException e1) {
			e1.printStackTrace();
			return false;}
		debugLog(TAG, "++++ Connected");

		try {in = sock.getInputStream();
		} catch (IOException e1) {
			e1.printStackTrace();
			return false;}

		try {out = sock.getOutputStream();
		} catch (IOException e1) {
			e1.printStackTrace();
			return false;}

		return true;
	}

	public void sendCommand(String command) throws IOException {
		int i = command.length();
		byte checksum = 0;
		while (--i >= 0) {
			checksum ^= (byte) command.charAt(i);
		}
		StringBuilder rec = new StringBuilder(256);
		rec.setLength(0);
		rec.append('$');
		rec.append(command);
		rec.append('*');
		rec.append(String.format("%02X", checksum));
		rec.append("\r\n");
		debugLog(TAG, "++++ Writing: " + rec.toString() );

		try {out.write(rec.toString().getBytes());
		} catch (Exception e) {
			e.printStackTrace();
			throw new IOException(e);
		}
	}

	public void sendBytes(byte[] byteArray) throws IOException {
		debugLog(TAG, "sendBytes.out.write:" + byteArray.toString());
		try {out.write(byteArray);
		} catch (Exception e) {
			e.printStackTrace();
			throw new IOException(e);
		}
	}

	public String readString(double timeout) throws IOException, InterruptedException {
		int bytes_available = 0;

		while((bytes_available = in.available()) == 0 && timeout > 0) {
			debugLog(TAG, "++++ readString waiting for input ---"+ Double.toString(timeout));
			Thread.sleep(250);
			timeout -= 0.25;
		}
		byte[] buf = new byte[bytes_available];
		in.read(buf);

		debugLog(TAG, "++++ readString read "+bytes_available+" bytes from GPS");
		buffer.setLength(0);
		for (int k = 0; k < buf.length; k++) {
			char c = (char)(buf[k] & 0xff);
			buffer.append(c);
		}

		debugLog(TAG, "++++ Read "+bytes_available+" bytes from GPS");
		return buffer.toString();
	}

	public byte[] readBytes(double timeout) throws IOException, InterruptedException {
		int bytes_available = 0;

		while((bytes_available = in.available()) == 0 && timeout > 0) {
//			debugLog(TAG, "++++ readBytes waiting for input ---"+ Double.toString(timeout));
			Thread.sleep(250);
			timeout -= 0.25;
		}
		
		byte[] buf = new byte[bytes_available];
		in.read(buf);

		debugLog(TAG, "++++ readBytes timeout:"+Double.toString(timeout)+" read:"+bytes_available+" bytes");
		return buf;
	}

	public String waitForReply(String reply, double timeout) throws IOException, InterruptedException {
		byte[] buf;
		char b;
		boolean doAppend = false;
		int retries = (int) (timeout*3);
		int idx = 0;
		String subCmd = "0";
		// Read from the device until we get the reply we are looking for
		debugLog(TAG, "++++ reading from device, waiting for: " +reply+", timeout: "+ timeout);

		int i = 0;
		buffer.replace(0, buffer.capacity(),"");
		String mtkCmd = reply.substring(4,7);
		if (mtkCmd.equals("182")){
			subCmd = reply.substring(8, 9);
		}
		while (i < retries) {
			buf = readBytes(timeout);
			if (buf.length == 0) {
				msg = "no bytes read from device!";
				debugLog(TAG, "###### "+msg);
				throw new IOException(msg);
			}
			for (int j = 0; j < buf.length; j++) {
				b = (char)(buf[j] & 0xff);
				// Check if this is the start of a new message
				if (b == '$') {
					doAppend = true;
					debugLog(TAG, "++## $ found - starting append");
				}
				if (b == '*'){
					doAppend = false;
					retries--;
					debugLog(TAG, "++## * found - stopping append -- retries left=" + retries);
					buffer.append(b);
					String message = buffer.toString();
					debugLog(TAG, "++++ Received: " + message);
					/* ############## command failure debug lines #####################*/
//					if (message.contains("PMTK182,3,9")) {
//						message = "$PMTK001,182,1*";
//					}
//					if (message.contains("PMTK707")) {
//						message = "$PMTK001,607,1*";
//					}
					/* ############## command failure debug lines #####################*/
					if (message.indexOf(reply, 0) > 0) {
						debugLog(TAG, "++++ Breaking because we received:" + reply);
						return message;
					}
					idx = message.indexOf("PMTK001", 0);
					if (idx > 0){
						String sA[] = message.split(",");
						if ((sA.length == 3)&&(!sA[2].equals("3*"))){
							debugLog(TAG, "++++ Breaking because we received:" + message);
							return message;	
						}
						if (mtkCmd.equals(sA[1])){
							if (sA.length > 3){
								if ((mtkCmd == "182")&&(sA[2].equals(subCmd))&&(!sA[3].equals("3*"))){
									debugLog(TAG, "++++ Breaking because we received:" + message);
									return message;									
								}
							}
						}
					}
					buffer.replace(0, buffer.capacity(),"");
				}
				if (doAppend) {buffer.append(b);}
			}
		}
		// We did not receive the message we where waiting for after 100 messages! Return empty string.
		debugLog(TAG, "++++ did not receive " + reply + " after " + retries + " reads!");
		return null;
	}

	public void close() {
		debugLog(TAG, "++++ close()");
		try {sock.close();
		} catch (IOException e) {e.printStackTrace();}
	}

	public void debugLog(String tag, String msg){
		logString = tag + ": " + msg;
		MTKutility.debugGPSrxts(logString);
		if (debug) {Log.d(tag, msg +BR);}
	}
}
