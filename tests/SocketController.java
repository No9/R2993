

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
	
public class SocketController implements Runnable{

	 public static void main(String[] args) {
        SocketController socket = new SocketController();
		
    }
	
	public static final String SERVERIP = "127.0.0.1";
	public static final int SERVERPORT = 4444;
	private final Thread mThread;
	public SocketController(){
		mThread = new Thread(this);
		mThread.start();
	}

	public void run() {
		 
        try {
        	
        	
            ServerSocket serverSocket = new ServerSocket(SERVERPORT);
            l("Socket Started");
            while (true) {              

                Socket client = serverSocket.accept();
                try {
                     BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));
					 PrintStream out = new PrintStream(client.getOutputStream());
                     String str = in.readLine();
                     l("Socket Data: " + str);
					 out.println("Recieved" + str);
              
                } catch(Exception e) {

                	l("Error in buffer reader: " + e.getMessage() + "\n");
                	e(e);
                    
                } finally {

                       //client.close();
                       //mConnectionHandler.onClientClosed();
                       l("S: Done.");
                   }
            }      

        } catch (Exception e) {
            l("S: Error");
            e(e);
        }
   }
	
	public final static String TAG = "SocketController";
    
    private void l(Object msg) {
		System.out.println(">==< " + msg.toString() + " >==<");
	}
    
    private void e(Object msg) {
		System.out.println(">==< " + msg.toString() + " >==<");
	}
	
}
