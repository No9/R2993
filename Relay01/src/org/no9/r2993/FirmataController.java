package org.no9.r2993;

import java.io.UnsupportedEncodingException;

import android.app.Activity;

public class FirmataController {

	private SocketController skt;
	private UsbController usb;
	
	public FirmataController(Activity parentActivity, int vid, int pid){
		skt = new SocketController(parentActivity.getApplicationContext(), mSocketConnectionHandler, vid, pid);
		//usb = new UsbController(parentActivity.getApplicationContext(), usbHandler, vid, pid);
	}
	
	public void stop(){
		skt.stop();
		usb.stop();
	}

private final ISocketConnectionHandler mSocketConnectionHandler = new ISocketConnectionHandler() {
    	
    	@Override
    	public void onConnected(){
    	   L.info("TCP Server Running\n");
    	}
    	
    	@Override
    	public void onError(String msg){
    		L.error("Error in TCPIP server\n" + msg);
    	}
    	
    	@Override
    	public void onData(String data){
    		L.info("Data Arrived:" + data + "\n");
    		
    	}
    	
    	@Override 
    	public void Emit(byte[] buf){
    		usb.send(buf);
    	}
    	
    	@Override
    	public void onClientClosed(){
    		L.info("Client Closed\n");
    	}
    };
    private final IUsbConnectionHandler usbHandler = new IUsbConnectionHandler() {

		@Override
		public void onUsbStopped() {
			String msg = "Usb stopped!\n"; 
			L.error(msg);
		}

		@Override
		public void onErrorLooperRunningAlready() {
			String msg = "Looper already running!\n";
			L.error(msg);
		}

		@Override
		public void onUsbStarted() {
			String msg = "Usb Started!\n";
			L.error(msg);
		}

		@Override
		public void onUsbData(byte[] data) {
				skt.send(data);
				
				try {
					String decoded = new String(data, "UTF-8");
					L.error(decoded);
				} catch (UnsupportedEncodingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} 
		}


		@Override
		public void onDeviceNotFound() {
			String msg = "Device Not Found!\n";
			L.error(msg);

			if(usb != null){
				usb.stop();
				usb = null;
				String usbmsg = "UsbController Stopped!\n";
				L.error(usbmsg);
			}
		}
	};
}


