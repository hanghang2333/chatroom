import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Scanner;
 //receive message thread,handling the message from the server
    class ReadMessage extends Thread
      { private BufferedReader sin; //buffer，IOstream
        private boolean loginornot=false;   //a flag save the status of login or not
	    public  ReadMessage(BufferedReader sin) //init
	    {   this.sin=sin;}
	    public void unlogined(String str) throws IOException
		{   //when unlogin,only these message can be display,contains the quit.
			 if(str.equals("please login")||str.equals("Name exist, please choose anthoer name.")||str.equals("Invalid command")||(!loginornot&&str.equals("/quit")))
	          { if(!(str.equals("/quit"))){
		             System.out.println(str);}
	             else{                                         //when status=unlogin,quit->close socket 
	    	          System.out.println("you are quit");
	    	          if(ThreadClient.socket!=null)
	                  ThreadClient.socket.close();
	                  return;
	    	          }}
		}
		public void logined(String str) throws IOException
		{
			if(!(str.equals("OK"))&&!(str.contains("/quit")))
            {                     //do not dsiplay these command,just give them to server to handle.
                System.out.println(str);  }                 
           else if(str.contains("/quit"))
           {                  //status=logined,quit
               System.out.println("you are quit");
               if(ThreadClient.socket!=null)
               ThreadClient.socket.close();
               return;                }	
		}
	    public void run()
        {try { 
		     while(true){
		       String str=null;
		       str=sin.readLine();                                 
		       unlogined(str);
		       if(str.equals("OK"))                                
		    	   //when the server tell login success,it will give a OK as a flag
		           { loginornot=true;}
		       if(loginornot)
		            {      //status=logined
			            logined(str); }
	           }}catch(IOException e)
	               {e.printStackTrace();System.out.println("Something is wrong in client-ReadMessage");}
               finally{
            	   try{
            		   if(ThreadClient.socket!=null)
            	       ThreadClient.socket.close(); return;
            	         }catch(Exception e){e.printStackTrace();}
                   }
            }}         
//the thread responsble for send message
 class SendMessage extends Thread
      { 
 	    private PrintWriter sout;     
 	    public  SendMessage(PrintWriter sout)   
 	        {this.sout=sout;}
		public void run()
              {try{
 	           while(true){   //keyboard,send messgae to server
	                      String test=ThreadClient.input.nextLine();
	                      if(test!=null){        //not null
	        	          sout.println(test);}	 }
 	                  }catch(Exception e){
	        	         e.printStackTrace();System.out.println("Something is wrong in client-WriteMessage");
	                                 }finally{
	        	                             try {
	        	                            	 if(ThreadClient.socket!=null)
						                         ThreadClient.socket.close();
	        	                            	 if(ThreadClient.socket!=null)
						                         ThreadClient.input.close();return;
					                             } catch (IOException e) {e.printStackTrace();}
	                                         }	
               }}
//ThreadClient Class 
 public class ThreadClient{                                   
	    public static Socket socket;
	    public static Scanner input=new Scanner(System.in);
	    public static void main(String[] args)throws IOException,InterruptedException
            {     InetAddress addr=InetAddress.getByName("localhost");//just test,given the hostname to get ip
    		      socket=new Socket(addr,12345);    
        	      BufferedReader sin;                         
        	      PrintWriter sout;                           
	    	try{          	  
            	  sin=new BufferedReader(new InputStreamReader(socket.getInputStream()));
          	      sout=new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())),true);
            	  new ReadMessage(sin).start();
            	  new SendMessage(sout).start();           		 
            	     } catch (IOException e) {e.printStackTrace();}}
                        }
