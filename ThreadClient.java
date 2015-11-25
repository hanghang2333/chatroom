import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Scanner;
 //接收信息线程，即从服务器传过来的信息在这里处理
    class ReadMessage extends Thread
      { private BufferedReader sin;                                //buffer,缓冲区，io流
        private boolean loginornot=false;                             //一个记录是否登录成功的标识符
	    public  ReadMessage(BufferedReader sin)                    //初始化，入口参数为一个io缓冲区
	    {   this.sin=sin;}
	    public void unlogined(String str) throws IOException
		{   //在未登录情况下：只允许显示这些信息，包括未登录情况下的quit处理
			 if(str.equals("please login")||str.equals("Name exist, please choose anthoer name.")||str.equals("Invalid command")||(!loginornot&&str.equals("/quit")))
	          { if(!(str.equals("/quit"))){
		             System.out.println(str);}
	             else{                                         //未login就quit，关闭socket线程，return退出客户端
	    	          System.out.println("you are quit");
	    	          if(ThreadClient.socket!=null)
	                  ThreadClient.socket.close();
	                  return;
	    	          }}
		}
		public void logined(String str) throws IOException
		{
			if(!(str.equals("OK"))&&!(str.contains("/quit")))
            {                     //不显示这些命令类的消息，进行单独处理
                System.out.println(str);  }                 
           else if(str.contains("/quit"))
           {                  //已经登录了之后输入/quit。
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
		    	   //服务端端检测到登录成功后会发一个OK过来，这里用这个作为标志来进行状态转换
		           { loginornot=true;}
		       if(loginornot)
		            {      //已登录情况下：
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
//发送给服务器端的线程。
 class SendMessage extends Thread
      { 
 	    private PrintWriter sout;     
 	    public  SendMessage(PrintWriter sout)    //初始化函数
 	        {this.sout=sout;}
		public void run()
              {try{
 	           while(true){   //检测键盘输入，发送到服务器端再进行检测处理
	                      String test=ThreadClient.input.nextLine();
	                      if(test!=null){        //不为空
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
//ThreadClient类，客户端
 public class ThreadClient{                                   
	    public static Socket socket;
	    public static Scanner input=new Scanner(System.in);
	    public static void main(String[] args)throws IOException,InterruptedException
            {     InetAddress addr=InetAddress.getByName("localhost");//给定主机名获取ip
    		      socket=new Socket(addr,12345);              //到服务器的套接字
        	      BufferedReader sin;                         //读入缓存
        	      PrintWriter sout;                           //发出缓存
	    	try{          	  
            	  sin=new BufferedReader(new InputStreamReader(socket.getInputStream()));
          	      sout=new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())),true);
            	  new ReadMessage(sin).start();
            	  new SendMessage(sout).start();           		 
            	     } catch (IOException e) {e.printStackTrace();}}//在命令行打印出异常出现在程序中出错的位置
                        }