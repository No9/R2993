/*
 * UsbController.java
 * This file is part of UsbController
 *
 * Copyright (C) 2012 - Manuel Di Cerbo
 *
 * UsbController is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * UsbController is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with UsbController. If not, see <http://www.gnu.org/licenses/>.
 */
package org.no9.r2993;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Iterator;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbConstants;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbManager;
import android.hardware.usb.UsbRequest;
import android.util.Log;

/**
 * (c) Neuxs-Computing GmbH Switzerland
 * @author Manuel Di Cerbo, 02.02.2012
 *
 */
public class UsbController {

	private final Context mApplicationContext;
	private final UsbManager mUsbManager;
	private final IUsbConnectionHandler mConnectionHandler;
	private final int VID;
	private final int PID;
	protected static final String ACTION_USB_PERMISSION = "org.no9.r2993.USB";

	/**
	 * Activity is needed for onResult
	 * 
	 * @param parentActivity
	 */
	public UsbController(Activity parentActivity,
			IUsbConnectionHandler connectionHandler, int vid, int pid) {
		mApplicationContext = parentActivity.getApplicationContext();
		mConnectionHandler = connectionHandler;
		mUsbManager = (UsbManager) mApplicationContext
				.getSystemService(Context.USB_SERVICE);
		VID = vid;
		PID = pid;
		init();
	}

	private void init() {
		enumerate(new IPermissionListener() {
			@Override
			public void onPermissionDenied(UsbDevice d) {
				UsbManager usbman = (UsbManager) mApplicationContext
						.getSystemService(Context.USB_SERVICE);
				PendingIntent pi = PendingIntent.getBroadcast(
						mApplicationContext, 0, new Intent(
								ACTION_USB_PERMISSION), 0);
				mApplicationContext.registerReceiver(mPermissionReceiver,
						new IntentFilter(ACTION_USB_PERMISSION));
				usbman.requestPermission(d, pi);
			}
		});
	}

	public void stop() {
		mStop = true;
		synchronized (sSendLock) {
			sSendLock.notify();
		}
		try {
			if(mUsbThread != null)
				mUsbThread.join();
		} catch (InterruptedException e) {
			e(e);
		}
		mStop = false;
		mLoop = null;
		mUsbThread = null;
		
		try{
			mApplicationContext.unregisterReceiver(mPermissionReceiver);
		}catch(IllegalArgumentException e){};//bravo
	}

	private UsbRunnable mLoop;
	private Thread mUsbThread;

	private void startHandler(UsbDevice d) {
		if (mLoop != null) {
			mConnectionHandler.onErrorLooperRunningAlready();
			return;
		}
		 
		mLoop = new UsbRunnable(d);
		mUsbThread = new Thread(mLoop);
		mUsbThread.start();
	}

	public void send(byte[] data) {
		mData = data;
		l(">>> USB_DATA_SENT >>>");
		l(data);
		synchronized (sSendLock) {
			sSendLock.notify();
		}
	}

	private void enumerate(IPermissionListener listener) {
		l("enumerating");
		HashMap<String, UsbDevice> devlist = mUsbManager.getDeviceList();
		Iterator<UsbDevice> deviter = devlist.values().iterator();
		while (deviter.hasNext()) {
			UsbDevice d = deviter.next();
			l("Found device: "
					+ String.format("%04X:%04X", d.getVendorId(),
							d.getProductId()));
			
			if (d.getVendorId() == VID && d.getProductId() == PID) {
				l("Device under: " + d.getDeviceName());
				if (!mUsbManager.hasPermission(d))
					listener.onPermissionDenied(d);
				else{
					startHandler(d);
					return;
				}
				break;
			}
		}
		l("no more devices found");
		mConnectionHandler.onDeviceNotFound();
	}

	private class PermissionReceiver extends BroadcastReceiver {
		private final IPermissionListener mPermissionListener;

		public PermissionReceiver(IPermissionListener permissionListener) {
			mPermissionListener = permissionListener;
		}

		@Override
		public void onReceive(Context context, Intent intent) {
			mApplicationContext.unregisterReceiver(this);
			if (intent.getAction().equals(ACTION_USB_PERMISSION)) {
				if (!intent.getBooleanExtra(
						UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
					mPermissionListener.onPermissionDenied((UsbDevice) intent
							.getParcelableExtra(UsbManager.EXTRA_DEVICE));
				} else {
					l("Permission granted");
					UsbDevice dev = (UsbDevice) intent
							.getParcelableExtra(UsbManager.EXTRA_DEVICE);
					if (dev != null) {
						if (dev.getVendorId() == VID
								&& dev.getProductId() == PID) {
							startHandler(dev);// has new thread
						}
					} else {
						e("device not present!");
					}
				}
			}
		}

	}

	// MAIN LOOP
	private static final Object[] sSendLock = new Object[]{};//learned this trick from some google example :)
	//basically an empty array is lighter than an  actual new Object()...
	private boolean mStop = false;
	private byte[] mData = new byte[] { 0x00 };

	private class UsbRunnable implements Runnable {
		private final UsbDevice mDevice;
	
		UsbRunnable(UsbDevice dev) {
			mDevice = dev;
		}
	
		@Override
		public void run() {//here the main USB functionality is implemented
			UsbDeviceConnection conn = mUsbManager.openDevice(mDevice);
			
			//mConnectionHandler.onUsbStarted();
			if (!conn.claimInterface(mDevice.getInterface(1), true)) {
				return;
			}
			// Arduino Serial usb Conv
			conn.controlTransfer(0x21, 34, 0, 0, null, 0, 0);
			conn.controlTransfer(0x21, 32, 0, 0, new byte[] { (byte) 0x80,
					0x25, 0x00, 0x00, 0x00, 0x00, 0x08 }, 7, 0);
	
			UsbEndpoint epIN = null;
			UsbEndpoint epOUT = null;
	
			UsbInterface usbIf = mDevice.getInterface(1);
			for (int i = 0; i < usbIf.getEndpointCount(); i++) {
				if (usbIf.getEndpoint(i).getType() == UsbConstants.USB_ENDPOINT_XFER_BULK) {
					if (usbIf.getEndpoint(i).getDirection() == UsbConstants.USB_DIR_IN)
						epIN = usbIf.getEndpoint(i);
					else
						epOUT = usbIf.getEndpoint(i);
				}
			}
	
			//ByteBuffer buffer = ByteBuffer.allocate(1);
			byte[] buffer = new byte[256];
			
		    UsbRequest request = new UsbRequest();
		    request.initialize(conn, epIN);
		    StringBuilder str = new StringBuilder();
		    boolean send = false;
		    
			for (;;) {// this is the main loop for transferring
				synchronized (sSendLock) {
					sSendLock.notify();
				}
				
				int ibuftosend = 0;
				byte[] buffertosend = new byte[20];
				
				if (conn.bulkTransfer(epIN, buffer, 20, 0) >= 0) {
					for (int i = 2; i < 20; i++) {
						if (buffer[i] != 0) {
							buffertosend[ibuftosend] = buffer[i];
							ibuftosend++;
							if(buffer[i] == 36)
								send = true;
						}
//                        	mConnectionHandler.onUsbData(buffer[i]);
//                        	//L.e((char) buffer[i]);
//                            str.append((char) buffer[i]);
//                        } else {
//                            //L.e(str);
//                            break;
//                        }
					}
					if(send){
						String ls = new String(buffer);
						L.e("SENDING:" + ls);
						mConnectionHandler.onUsbData(buffer);
						
					}
                }
				
				conn.bulkTransfer(epOUT, mData, mData.length, 0);
//				if (conn.requestWait() == request) {
//	                byte newStatus = buffer.get(0);
//	                L.d(newStatus);
////	                if (newStatus != status) {
////	                    Log.d(TAG, "got status " + newStatus);
////	                    status = newStatus;
////	                    if ((status & COMMAND_FIRE) != 0) {
////	                        // stop firing
////	                        sendCommand(COMMAND_STOP);
////	                    }
////	                }
//	                try {
//	                    Thread.sleep(100);
//	                } catch (InterruptedException e) {
//	                }
//	            } else {
//	                Log.e(TAG, "requestWait failed, exiting");
//	                break;
//	            }
				
				
				
				
				
				if (mStop) {
					mConnectionHandler.onUsbStopped();
					return;
				}
			}
		}
	}

	// END MAIN LOOP
	private BroadcastReceiver mPermissionReceiver = new PermissionReceiver(
			new IPermissionListener() {
				@Override
				public void onPermissionDenied(UsbDevice d) {
					l("Permission denied on " + d.getDeviceId());
				}
			});

	private static interface IPermissionListener {
		void onPermissionDenied(UsbDevice d);
	}

	public final static String TAG = "USBController";

	private void l(Object msg) {
		Log.d(TAG, ">==< " + msg.toString() + " >==<");
	}

	private void e(Object msg) {
		Log.e(TAG, ">==< " + msg.toString() + " >==<");
	}
}
