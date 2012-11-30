package org.no9.r2993;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button; 
import android.widget.RadioGroup;

public class RelayManager extends Activity {

	
	private static final int PID_UNO = 0x0043;
	private static final int PID_MEGA = 0x0044;
	
	private int PID = 0x0043;
	private static final int VID = 0x2341;
	private SocketController skt;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_relay_manager);

        Button start = (Button)findViewById(R.id.btnStart);
        Button stop = (Button)findViewById(R.id.btnStop);

        start.setOnClickListener(startListener);
        stop.setOnClickListener(stopListener);
        L.info("application starting");
		    
    }
    
    private OnClickListener startListener = new OnClickListener() {
        public void onClick(View v){
            
            RadioGroup radioDeviceType = (RadioGroup) findViewById(R.id.radioBoardType);;
			
			int selectedId = radioDeviceType.getCheckedRadioButtonId();
			switch(selectedId){
			    case 0 :
			    	PID = PID_UNO;
			    	break;
			    
			    case 1 :
			    	PID = PID_MEGA;
			    	break;
			    
			    default :
			    	PID = PID_UNO;
			    	break;
			}  
		    
			skt = new SocketController(RelayManager.this, mSocketConnectionHandler, VID, PID);
		    
        }
       };
    
       private OnClickListener stopListener = new OnClickListener() {
            public void onClick(View v){
               
            	if(skt != null)
            	skt.stop();
            	
           }               
       };
          
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_relay_manager, menu);
        return true;
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
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
    		
    	}
    	
    	@Override
    	public void onClientClosed(){
    		L.info("Client Closed\n");
    	}
    };
  }

