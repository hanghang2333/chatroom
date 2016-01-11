import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
 //receive message thread,handling the message from the server
    class ClientThread extends JFrame implements Runnable
      { 
		private static final long serialVersionUID = 1L;
	private BufferedReader sin; //buffer，IOstream
        private PrintWriter sout;  
        private boolean loginornot=false;   //a flag save the status of login or not
	    //添加的
       private  static Socket client;
        private static final int DEFAULT_WIDTH = 400;
    	private static final int DEFAULT_HEIGHT = 400;
      JButton sendButton;
      JButton quitButton;
      JButton loginButton;
      JTextArea chatSpace;
      JTextArea composeMessageSpace;
      JTextArea userSpace;
      JPanel composeMessageSpacePanel;
      JPanel buttonPanel;
     
        public  ClientThread() throws IOException 
	    { 
        	 InetAddress addr;
			addr = InetAddress.getByName("192.168.1.103");
			client=new Socket(addr,12345);
		    sin=new BufferedReader(new InputStreamReader(client.getInputStream()));
     	    sout=new PrintWriter(new BufferedWriter(new OutputStreamWriter(client.getOutputStream())),true);
  
            setSize(DEFAULT_WIDTH,DEFAULT_HEIGHT);
            setLocationByPlatform(true);
            Image img=new ImageIcon("icon.jpg").getImage();
            setIconImage(img);
		   // setLayout(new GridLayout(2,2)); 
		    
          chatSpace=new JTextArea();
          chatSpace.setLineWrap(true);
          chatSpace.setEditable(false);
          chatSpace.setBackground(new Color(44,0,30));
          Font sansbold14=new Font("SansSerif",Font.BOLD,14);
          chatSpace.setFont(sansbold14);
          chatSpace.setForeground(Color.white);
          JScrollPane chatScroll=new JScrollPane(chatSpace);
          
          userSpace=new JTextArea();
          userSpace.setLineWrap(true);
          userSpace.setEditable(false);
         // userSpace.setBackground(Color.lightGray);
          userSpace.setBackground(new Color(44,0,30));
          Font sansbold10=new Font("SansSerif",Font.BOLD,11);
          userSpace.setFont(sansbold10);
          userSpace.setForeground(Color.white);
         JScrollPane userScroll=new JScrollPane(userSpace);
         
          composeMessageSpace=new JTextArea(5,29);
          composeMessageSpace.setLineWrap(true);
          Font sansbold12=new Font("SansSerif",Font.BOLD,11);
          composeMessageSpace.setFont(sansbold12);
          composeMessageSpace.setForeground(Color.white);
         // composeMessageSpace.setBackground(Color.lightGray);
          composeMessageSpace.setBackground(new Color(44,0,30));
          JScrollPane enterScroll=new JScrollPane(composeMessageSpace);
          
          sendButton=new JButton("Send");
          sendButton.addActionListener(new SendAction());
          quitButton=new JButton("Quit");
          quitButton.addActionListener(new QuitAction());
          loginButton=new JButton("Login");
          loginButton.addActionListener(new LoginAction());
          buttonPanel=new JPanel(new GridLayout(3,1));
          buttonPanel.add(sendButton);
          buttonPanel.add(quitButton);
          buttonPanel.add(loginButton);
          
       
          
          composeMessageSpacePanel=new JPanel();
          composeMessageSpacePanel.add(enterScroll);
        composeMessageSpacePanel.add(buttonPanel);

         // getContentPane().add(buttonPanel);   
         add(chatScroll,BorderLayout.CENTER);
         add(userScroll,BorderLayout.EAST);
         add(composeMessageSpacePanel,BorderLayout.SOUTH);
        // add(buttonPanel,BorderLayout.EAST);
      }
	    public void unlogined(String str) throws IOException
		{   //when unlogin,only these message can be display,contains the quit.
			 if(str.equals("please login")||str.equals("Name exist, please choose anthoer name.")||str.equals("Invalid command")||(!loginornot&&str.equals("/quit")))
	          { if(!(str.equals("/quit"))){
                chatSpace.append(str+"\n");}
	             else{                                         //when status=unlogin,quit->close socket 
	            	 chatSpace.append("you are quit"+"\n");sin=null;sout=null;
	    	          if(ClientThread.client!=null)
	                //  ClientThread.client.close();
	                  return;
	    	          }}
		}
		public void logined(String str) throws IOException
		{
			if(!(str.equals("OK"))&&!(str.contains("/quit")))
            {                     //do not dsiplay these command,just give them to server to handle.
             String temp1=str.substring(0,3);
             String temp2=str.substring(3);
             if(str.equals("mlp")){userSpace.setText("");}
             else  if(temp1.equals("plm")){userSpace.append(temp2+"\n");}
             else  chatSpace.append(str+"\n");}     
           else if(str.contains("/quit"))
           {                  //status=logined,quit
              chatSpace.append("you are quit"+"\n");
               if(ClientThread.client!=null)
            	  // ClientThread.client.close();
               return;                }	
		}
	//rivate class  ThreadClient  implements Runnable {
			
			  public void run()
		        {try { 
				     while(true){
				       String str=null;
				       str=sin.readLine(); 
				       if(str.equals("/quit"))sin=null;
				       unlogined(str);
				       if(str.equals("OK"))                                
				           { loginornot=true;sout.println("/who");}
				       if(loginornot)
				            {    logined(str); }
			           }}catch(IOException e)
			               {e.printStackTrace();
			               }
		               finally{
		            	   try{
		            		   if(ClientThread.client!=null)
		            			   ClientThread.client.close(); return;
		            	         }catch(Exception e){e.printStackTrace();}
		                   }
		            }
	  
      
      private class SendAction implements ActionListener{
        public void actionPerformed(ActionEvent arg){
        	try{
          String message=composeMessageSpace.getText().trim();
          if(message!=null){sout.println(message);}
                               }catch(Exception e){
                            	   e.printStackTrace();System.out.println("Something is wrong in client-WriteMessage");}
        	composeMessageSpace.setText("");
        }}
      private class LoginAction implements ActionListener{
          public void actionPerformed(ActionEvent arg){
        	  String myname=composeMessageSpace.getText().trim();
              sout.println("/login "+myname);
              composeMessageSpace.setText("");
       
          }}
      private class QuitAction implements ActionListener{
          public void actionPerformed(ActionEvent arg){
        	   sout.println("/quit");
          }}
	    public static void main(String[] args)
            { 
	    	EventQueue.invokeLater(new Runnable() {
				public void run() {
					ClientThread c = null;
					try {c = new ClientThread();
					} catch (IOException e) {
						e.printStackTrace();
					}
					c.setTitle("chatroom");
					c.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
					
					c.setVisible(true);
					Thread thread=new Thread(c) ;
					thread.start();
					
				}
			});	}}
           
