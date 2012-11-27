package org.no9.r2993;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.widget.Toast;


public class SocketService extends Service {

	public static final String SERVERIP = "127.0.0.1";
	public static final int SERVERPORT = 4445;
	private OutputStream out = null;
	private UsbController sUsbController = null;

    @Override
    public IBinder onBind(Intent arg0) {
        // TODO Auto-generated method stub
        return myBinder;
    }

    private final IBinder myBinder = new LocalBinder();

    public class LocalBinder extends Binder {
        public SocketService getService() {
            return SocketService.this;
        }
    }


    @Override
    public void onCreate() {
        super.onCreate();
    }

    public void IsBoundable(){
        L.info("I bind like butter");
    }

    @SuppressWarnings("deprecation")
	public void onStart(Intent intent, int startId){
        super.onStart(intent, startId);
       
        Toast.makeText(this,"Service created ...", Toast.LENGTH_LONG).show();
        Runnable connect = new connectSocket();
        int pid = intent.getIntExtra("PID", 0);
        int vid = intent.getIntExtra("VID", 0);
        
        if(sUsbController == null)
			sUsbController = new UsbController(this, mConnectionHandler, vid, pid);
		else{
			sUsbController.stop();
			sUsbController = new UsbController(this, mConnectionHandler, vid, pid);
		}
        
        new Thread(connect).start();
    }

    class connectSocket implements Runnable {

        @Override
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

                    	L.error("Error in buffer reader: " + e.getMessage() + "\n");
                    	
                    	mSocketConnectionHandler.onError("Error in buffer reader: " + e.getMessage() + "\n");
                        
                    } finally {

                           client.close();
                           L.info("S: Done.");
                           }
                    Thread.sleep(5);
                }      

            } catch (Exception e) {
            	mSocketConnectionHandler.onError("Error in connection : " + e.getMessage() + "\n");
            	L.error("S: Error");
                //L.e(e);
            }

        }

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
//        try {
//            //s.close();
//        } catch (IOException e) {
//            // TODO Auto-generated catch block
//            e.printStackTrace();
//        }
        //s = null;
    }
    
    
    
private final IUsbConnectionHandler mConnectionHandler = new IUsbConnectionHandler() {
		
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
					L.error(e.getMessage());
				    e.printStackTrace();
				}
	
		}
		
		
		@Override
		public void onDeviceNotFound() {
			String msg = "Device Not Found!\n";
			L.error(msg);
			
			if(sUsbController != null){
				sUsbController.stop();
				sUsbController = null;
				String usbmsg = "UsbController Stopped!\n";
				L.info(usbmsg);
			}
		}
	};
	
private final ISocketConnectionHandler mSocketConnectionHandler = new ISocketConnectionHandler() {
    	
    	@Override
    	public void onConnected(){
    		L.info("Socket Connected");
    	}
    	
    	@Override
    	public void onError(String msg){
    		L.info("Socket Error : " + msg);
    	}
    	
    	@Override
    	public void onData(String data){
    		L.info("Socket Connected");
    	}
    	
    	@Override
    	public void onClientClosed(){
    		L.info("Client Closed");
    	}
    	
    	public void Emit(byte[] buff){}
    	
    };
}
