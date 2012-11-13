package org.no9.r2993;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;

import android.R.integer;
import android.app.Activity;
import android.util.Log;


public class SocketController implements Runnable{

	
	public static final String SERVERIP = "127.0.0.1";
	public static final int SERVERPORT = 4444;
	private final ISocketConnectionHandler mSocketConnectionHandler;
	private UsbController sUsbController = null;
	private final Thread mThread;
	
	public SocketController(Activity parentActivity, ISocketConnectionHandler handler, int vid, int pid){
		
		mSocketConnectionHandler = handler;
		if(sUsbController == null)
			sUsbController = new UsbController(parentActivity, mConnectionHandler, vid, pid);
		else{
			sUsbController.stop();
			sUsbController = new UsbController(parentActivity, mConnectionHandler, vid, pid);
		}
		mThread = new Thread(this);
		mThread.start();
	}

	OutputStream out = null;
	
	public void run() {
		 
        try {
        	
            ServerSocket serverSocket = new ServerSocket(SERVERPORT);
            mSocketConnectionHandler.onConnected();
        	
            while (true) {
            	
                Socket client = serverSocket.accept();
                out = client.getOutputStream();
                
                try {
                	InputStream is = client.getInputStream();
                	ByteArrayOutputStream buffer = new ByteArrayOutputStream();
                	int nRead;
                	byte[] data = new byte[256];
                	while ((nRead = is.read(data, 0, data.length)) != -1) {
                		  buffer.write(data, 0, nRead);
                		}

                	buffer.flush();
                	if(buffer.size() > 0){
                		sUsbController.send(buffer.toByteArray());
                		//l("Socket Data: " + buffer.toString("UTF-8"));
                	}

                } catch(Exception e) {

                	l("Error in buffer reader: " + e.getMessage() + "\n");
                	e(e);
                	mSocketConnectionHandler.onError("Error in buffer reader: " + e.getMessage() + "\n");
                    
                } finally {

                       client.close();
                       l("S: Done.");
                       }
            }      

        } catch (Exception e) {
        	mSocketConnectionHandler.onError("Error in connection : " + e.getMessage() + "\n");
            l("S: Error");
            e(e);
        }
   }
	
	public final static String TAG = "SocketController";
    
    private void l(Object msg) {
		Log.d(TAG, ">==< " + msg.toString() + " >==<");
	}
    
    private void e(Object msg) {
		Log.e(TAG, ">==< " + msg.toString() + " >==<");
	}
	
private final IUsbConnectionHandler mConnectionHandler = new IUsbConnectionHandler() {
		
		@Override
		public void onUsbStopped() {
			String msg = "Usb stopped!\n"; 
			L.e(msg);
		}
		
		@Override
		public void onErrorLooperRunningAlready() {
			String msg = "Looper already running!\n";
			L.e(msg);
		}
		
		@Override
		public void onUsbStarted() {
			String msg = "Usb Started!\n";
			L.e(msg);
		}
		
		byte[] msg = new byte[200];
		boolean isWriting = false;
		int currentByte = 0;
		
		@Override
		public void onUsbData(byte[] data) {
			
			
				try{
					//if there is a connection then write
					if(out != null){
						out.write(data);
					}
				}catch(IOException e){
					e.printStackTrace();
				}
	
		}
		
		
		@Override
		public void onDeviceNotFound() {
			String msg = "Device Not Found!\n";
			L.e(msg);
			
			if(sUsbController != null){
				sUsbController.stop();
				sUsbController = null;
				String usbmsg = "UsbController Stopped!\n";
				L.e(msg);
			}
		}
	};
}
