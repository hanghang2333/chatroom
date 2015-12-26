import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.concurrent.*;
import java.util.concurrent.locks.Lock;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
class ServerThreadCode  extends Thread
{   //Server，1.handling login/quit，broadcast,personal，command
	//2.chat history :list 3.user-list:map
	 boolean a=false;     //when status=logined,a=true;
	private Socket clientSocket;
	private PrintWriter sout;
	private BufferedReader sin;
	CopyOnWriteArrayList<Socket> clients;                                     
	public static Lock lock=new java.util.concurrent.locks.ReentrantLock(); //Thread Lock
	private String name;
	private static HashMap<String,Socket> users = new HashMap<String,Socket>();
	//map:user-name user-Socket
	public ServerThreadCode(Socket client,CopyOnWriteArrayList<Socket> socketList)throws IOException
	{   name=null;
		this.clientSocket=client;
		this.clients=socketList;
	}
	@SuppressWarnings("unchecked")
	public  void history(String str,String str1,String str2)
	{     //history
		ThreadServer.jilu.get(0).add(str);
        ThreadServer.jilu.get(1).add(str1);
        ThreadServer.jilu.get(2).add(str2);
      }
	public  void qunfa(CopyOnWriteArrayList<Socket> clients,Socket clientSocket,String str)
	{ //qunfa
		for(Socket socket:clients) {                            
             if(clientSocket!=socket){            
		           PrintWriter pw = null;
				try {pw = new PrintWriter(socket.getOutputStream(), true);}
				    catch (IOException e) {
					e.printStackTrace();} 
   	           pw.println(str);}    }	
	 }
	public void dupname()
	{   
		boolean dupornot=false;;
	    lock.lock(); 
      try{
        Iterator <String> iterator =users.keySet().iterator();//提取map的key放到keyset集合，并实例化一个keyset的集合
        while(iterator.hasNext()){                       
	        String key=iterator.next();
	         if(name.equals(key))   {
	            sout.println("Name exist, please choose anthoer name.");
	           dupornot=true;    }	    				    			
        } }
      finally{lock.unlock();	}					
        if(!dupornot){
	          users.put(name,clientSocket);a=true;
	          sout.println("OK");
	          sout.println("You have logined,your name is "+name);
	          history("You have logined", name, "true");
	          lock.lock();
            try{
		      qunfa(clients,clientSocket,name+" has logined ");
		      history(name+" has logined", name,"false");}
          finally{lock.unlock();	}  		
	                 }    
	}
	public void deletesocket()
	{
		 lock.lock();  //delete user frome socket list
     try{
	       for(int i=0;i<clients.size();i++) {   
			   if(clientSocket.equals(clients.get(i))) {     
				   clients.remove(i);               }  
		   } }
     finally{lock.unlock();
	}}
	public void deletemap()
	{
		 lock.lock();         //delete user form user and lists
	   try{  
     Iterator <String> iterator =users.keySet().iterator();
		   while(iterator.hasNext())  {
			    String key=iterator.next();
			    if(name.equals(key)){
			             iterator.remove(); } }	}
     finally{lock.unlock();}  	
	}
	public void closesocket() throws IOException
	{
		   clientSocket.close();sin.close();sout.close();  //close the Thread and set it=null
		   clientSocket=null;sin=null;sout=null; return;
	}
	public void quit()
	{
		 lock.lock();   
     try{
		   qunfa(clients, clientSocket, name+" has quit");
		   history(name+" has quit ",name,"false");}
     finally{lock.unlock();}
	       sout.println("/quit");
	       a=false; 
	}
   public void yushe(String str1,String str2)
	    { lock.lock();
        try{
		  qunfa(clients, clientSocket, str1);
          history(str1,name,"false");}
        finally{lock.unlock();	}   					 		    					 
		  sout.println(str2); 
          history(str2, name, "true");
	    }
   public void secert(String temp) throws IOException
   {
	   lock.lock();
     boolean cunzai=false;
     try{
	   Iterator <String> iterator =users.keySet().iterator();
	   while(iterator.hasNext()){
			String key=iterator.next();
			if(temp.contains(key)){
			     cunzai=true;
			     if(!(key.equals(name))){
			          Socket value=users.get(key);
			          PrintWriter pw1 = new PrintWriter(value.getOutputStream(),true);
			          temp=temp.replace(key,"");
			          String siliao1=name+"said to you："+temp;
			          String siliao2="you said to"+key+"："+temp;
			          pw1.println(siliao1);	 history(siliao1, key, "true");
			          sout.println(siliao2);history(siliao2, name, "true");
			                            }	
		      else{
		    	sout.println("Stop talking to yourself!");}            
			                      }}}
     finally{lock.unlock();}
	if(!cunzai){sout.println("user_name is not online.");	}    		
   }
   public void hisomeone(String temp) throws IOException
   {
	   boolean qunfa=true;
	   lock.lock();
     try{
	   Iterator <String> iterator =users.keySet().iterator();
	   while(iterator.hasNext()){
			String key=iterator.next();
			if(temp.contains(key)){
				@SuppressWarnings("resource")
				Socket sockettmp=new Socket();
			    sockettmp=users.get(key);
			    String hi1=name+" said to "+key+" say hi",hi2=" you said to "+key+" say hi";
				for(Socket socket:clients){  
                       if((clientSocket!=socket)&&(socket!=sockettmp)){            
  				          PrintWriter pw = new PrintWriter(socket.getOutputStream(), true); 
  				          pw.println(hi1);}      
                       if(socket==sockettmp){
                      	  PrintWriter pw = new PrintWriter(socket.getOutputStream(), true); 
  				          pw.println(name+"say hi to you");}
                       }	  history(hi1, name, "false");   				    				
		    	    sout.println(hi2);history(hi2, name, "true");     
				qunfa=false;	    				    			
		                          }		    				    		
	                            }	}
     finally{lock.unlock();}
	  if(qunfa){
		         String hi3=name+"say hi to everyone",hi4="you say hi to everyone";
		         lock.lock();
             try{
                qunfa(clients, clientSocket,hi3);}
             finally{lock.unlock();}
		        sout.println(hi4);
		        history(hi3,name,"false");history(hi4, name, "true");
		       }
   }
   public void history(String content)
   {
	   int k=ThreadServer.jilu.get(0).size()-1;int count=0;//k：the sum-value of message，count：the value of user need
       for(int j=0;j<=k;j++){if(((ThreadServer.jilu.get(1)).get(j)==name&&(ThreadServer.jilu.get(2).get(j)=="true"))||(((ThreadServer.jilu.get(1)).get(j)!=name)&&((ThreadServer.jilu.get(2)).get(j)=="false")))
       	                    count++;}   //if:get the message which the user need
   	if(content.equals("/history"))      //without value,MAX=50
            {   for(int j=0;j<=k;j++){
           	 if(((ThreadServer.jilu.get(1)).get(j)==name&&(ThreadServer.jilu.get(2).get(j)=="true"))||(((ThreadServer.jilu.get(1)).get(j)!=name)&&((ThreadServer.jilu.get(2)).get(j)=="false"))){
           	   if(count<=50){sout.println(count+":"+ThreadServer.jilu.get(0).get(j));}	count--;   }     
           	                      }
            }
   	else{                               //用正则表达式匹配摘取出两个参数
              int[] d=new int[2];int i=0;
              Pattern p=Pattern.compile("[0-9]{1,}");//生存pattern对象并且编译表达式
              Matcher m=p.matcher(content);//用pattern类的mathcher的matcher方法来生存一个对象
              while(m.find())              //尝试在目标字符串中查找下一个匹配子串
               { d[i++]=Integer.parseInt(m.group()); 
                 if(i==2)break;}            //返回查找获得的所有子串内容,并防止越界
              int tmp,tmp1;                 //this user need display the tmp1-st to tmp-st
              if(d[0]>count)tmp=1;else tmp=count+1-d[0];
              if((d[0]+d[1])>count+1)tmp1=1;else tmp1=count-d[0]-d[1]+2;
              int counttmp=0;              //counttmp，find the message form tmp1 to tmp。
              int counttmp2=tmp-tmp1+1;    
              for(int j=0;j<=k;j++)
               {   
           	   if(((ThreadServer.jilu.get(1)).get(j)==name&&(ThreadServer.jilu.get(2).get(j)=="true"))
           			||(((ThreadServer.jilu.get(1)).get(j)!=name)&&((ThreadServer.jilu.get(2)).get(j)=="false"))){
               	    counttmp++;
               	    if(counttmp>=tmp1&&counttmp<=tmp){sout.println(counttmp2+":"+ThreadServer.jilu.get(0).get(j));	counttmp2--; }}     
            }  }						               
                                
   }
   @SuppressWarnings("unused")
public void who()
   {
	   
	int count=0;               
     lock.lock();
     try{
	    Iterator <String> iterator =users.keySet().iterator();
	    qunfa(clients,null,"mlp");
	    while(iterator.hasNext()){
		      String key=iterator.next();
		      //sout.println("plm"+"++++"+key);
		      qunfa(clients,null,"plm"+"++++"+key);
		      count++;
	                             }}
     finally{lock.unlock();	}    				    		
	    //sout.println("Welcome!Now there are "+count+" users online" );		    			
   }
   public void run()//login/quit，command，broadcast，personal
	{ try {
		    sout=new PrintWriter(new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream())),true);
		    sin=new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));		    
		    sout.println("please login");
		    String content=null;
		    while(true){
		    	  if(!a){
		    		     if((content=sin.readLine()).equals("/quit"))            
		    		    	 //status=unlogin,close the Thread
					          {      sout.println("/quit");
					                 deletesocket();
					                  closesocket(); 
					                  }
		    	        else if(content.contains("/login "))                    
		    	        	
		    	              {    name=content.substring(7);
					                dupname();				 
					                 //no dupname
					                  }
		    		    else{sout.println("Invalid command");   }		      
		    		     
		    	             }
		         if(a){                //status=logined：/quit，normal message，sercert，command/map,/history [a1],[a2];
		    		    if((content=sin.readLine()).equals("/quit"))
		    		        {    quit();
		    			         deletemap();
		    			         deletesocket();
		    			         closesocket();
		    			        who();
	                             return;  }	
		    		    else if(content.contains("/login ")){}
		    		    else{          
		    				   if(content.equals("//smile")){               
                                        //预设消息//smile
		    					      String yushe1=name+" smiled";
		    					      String yushe2="you smiled";
		    					      yushe(yushe1,yushe2);}
		    				   else if(content.equals("//hehe")){            
		    					      //预设消息//hehe
		    					      String yushe1=name+"said hehe";
		    					      String yushe2="you said hehe";
                                       yushe(yushe1,yushe2);}
		    			        else if(content.contains("/to"))            
		    			        	 //sercert
		    				       {   String temp=content.substring(4);
		    				            secert(temp);   }	
		    				    else if(content.contains("//hi")){       
		    				    	 //预设消息 //hi [somebody]
		    				    	   String temp=content.substring(4);
		    				    	   hisomeone(temp);   }	
		    				    else if(content.contains("/history")){  
		    				    	   String temp=content;
		    				            history(temp);	  }					 	    
		    				    else if(content.equals("/who")){      
		    				    	   	    	who();     }		    				    
		    				    else{   lock.lock();
                            try{
		    				    	          qunfa(clients, clientSocket, name+"said："+content);}
                            finally{
		    				    	          lock.unlock();}
		    				    	          sout.println("you said： "+content);
		    				    	          history(name+"said："+content, name, "false");
		    				    	          history("you said： "+content, name, "true");	}	    			      
		    			      }  }
		    		  }}catch(IOException e)
		                      {e.printStackTrace();
		                      System.out.println("Something is wrong in server");}
		                finally{System.out.println("Close the Server socket and the io.");
			                     try{if(clientSocket!=null)clientSocket.close();
			                         if(sin!=null)sin=null;
			                         if(sout!=null)sout=null;}catch(IOException e)
			                                                    {e.printStackTrace();}
		                       }} }

public class ThreadServer
{   
	public static ArrayList<String> xiaoxi=new ArrayList<String>(),shuyu=new ArrayList<String>(),flag=new ArrayList<String>();;       
	@SuppressWarnings("rawtypes")
	public static ArrayList<ArrayList> jilu=new ArrayList<ArrayList>();    //归属用两个标志，shuyu和flag.其中name---0为除name外广播，name---1为私有。       
	public static final int portNo=12345;       
	public volatile static  CopyOnWriteArrayList<Socket> socketList=new CopyOnWriteArrayList<Socket>();
	public static void main(String[]args)throws IOException
	{   jilu.add(xiaoxi);jilu.add(shuyu);jilu.add(flag);                 //message history :list
		ServerSocket serversocket=new ServerSocket(portNo);
		System.out.println("The Server is start:"+serversocket);         //start success
		try {while(true)
			   {  Socket socket=serversocket.accept();                   //wait client
				  socketList.add(socket);                                //save this Thread
				  new ServerThreadCode(socket,socketList).start();//run();
			   }
		    }catch(IOException e){e.printStackTrace();}
		          finally{ if(serversocket!=null)serversocket.close();}
	}
}
