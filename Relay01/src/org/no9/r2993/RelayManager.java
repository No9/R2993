package org.no9.r2993;

import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;
import android.view.View;
import android.widget.Button; 
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;

public class RelayManager extends Activity {

	private static final int VID = 0x2341;
	private static final int PID_UNO = 0x0043;
	private static final int PID_MEGA = 0x0044;
	private static SocketController sSocketController;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_relay_manager);
        appendToTextField("\n");
		
        ((Button)findViewById(R.id.btnStart)).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				
				int PID = 0x0043;
				RadioGroup radioDeviceType = (RadioGroup) findViewById(R.id.radioBoardType);;
				
				int selectedId = radioDeviceType.getCheckedRadioButtonId();
				RadioButton radioDeviceButton = (RadioButton) findViewById(selectedId);
			    appendToTextField("Trying to Find: " + radioDeviceButton.getText() + "\n"); 
				
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
			
				if(sSocketController == null){
			    	sSocketController = new SocketController(RelayManager.this, mSocketConnectionHandler, VID, PID);
			    }
			}
		});
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_relay_manager, menu);
        return true;
    }
    
    private void appendToTextField(String text){
    	EditText edt = (EditText)findViewById(R.id.editText1);
		edt.append(text);
    }
    
    private final ISocketConnectionHandler mSocketConnectionHandler = new ISocketConnectionHandler() {
    	
    	@Override
    	public void onConnected(){
    		appendToTextField("TCP Server Running\n");
    	}
    	
    	@Override
    	public void onError(String msg){
    		appendToTextField("Error in TCPIP server\n");
    		appendToTextField(msg + "\n");
    	}
    	
    	@Override
    	public void onData(String data){
    		appendToTextField("Data Arrived:" + data + "\n");
    	}
    	
    	@Override
    	public void onClientClosed(){
    		appendToTextField("Client Closed\n");
    	}
    };
}