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
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.locks.Lock;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
//服务器端线程
//多线程方面的改进：加了一些volatile修饰，CopyorWriteArrayList的使用
//                 使用了lock（）和unlock()；主要是防止用户名单之间信息的修改不同步造成异常。
//用户信息存储在两个地方clients和users。各有用途吧，也没有很大必要去统一。
//为防止异常每个try后都有catch and finally部分，或者开始就throws.
import java.util.regex.Matcher;
import java.util.regex.Pattern;
class ServerThreadCode extends Thread
{   //Server线程，1.从Clien收到后处理login/quit，broadcast,personal，command
	//2.聊天记录list 3.用户列表map
	 boolean a=false;     //用来标识是否登录成功的flag,登录后a=true;
	private Socket clientSocket;
	private PrintWriter sout;
	private BufferedReader sin;
	CopyOnWriteArrayList<Socket> clients;                                     
	//socket链表，用来遍历群发
	private String name;
	private static HashMap<String,Socket> users = new HashMap<String,Socket>();
	//map用来存储每个连接的客户端的用户名和socket
	public ServerThreadCode(Socket client,CopyOnWriteArrayList<Socket> socketList)throws IOException
	{   name=null;
		this.clientSocket=client;
		this.clients=socketList;
	}
	public  void history(String str,String str1,String str2)
	{       //记录聊天记录的函数
		ThreadServer.jilu.get(0).add(str);
        ThreadServer.jilu.get(1).add(str1);
        ThreadServer.jilu.get(2).add(str2);
      }
	public  void qunfa(CopyOnWriteArrayList<Socket> clients,Socket clientSocket,String str)
	{ //群发函数，除了自己
		for(Socket socket:clients) {                            
             if(clientSocket!=socket){            
		           PrintWriter pw = null;
				try {pw = new PrintWriter(socket.getOutputStream(), true);}
				    catch (IOException e) {
					e.printStackTrace();} 
   	           pw.println(str);}    }	
	 }
	public  void delete()
	{
		
	}
	public void dupname()
	{   
		boolean dupornot=false;;
	    ThreadServer.lock.lock(); 
        Iterator <String> iterator =users.keySet().iterator();//提取map的key放到keyset集合，并实例化一个keyset的集合
        while(iterator.hasNext()){                       
	        String key=iterator.next();
	         if(name.equals(key))   {
	            sout.println("Name exist, please choose anthoer name.");
	           dupornot=true;    }	    				    			
                                                          }
        ThreadServer.lock.unlock();						
        if(!dupornot){
	          users.put(name,clientSocket);a=true;
	          sout.println("OK");
	          sout.println("You have logined");
	          history("You have logined", name, "true");
	          ThreadServer.lock.lock();
		      qunfa(clients,clientSocket,name+" has logined ");
		      history(name+" has logined", name,"false");
		      ThreadServer.lock.unlock();	  		
	                 }    
	}
	public void deletesocket()
	{
		 ThreadServer.lock.lock();  //从socket表中删除,写成函数
	       for(int i=0;i<clients.size();i++) {   
			   if(clientSocket.equals(clients.get(i))) {     
				   clients.remove(i);               }  
		   }                     
	       ThreadServer.lock.unlock();
	}
	public void deletemap()
	{
		 ThreadServer.lock.lock();         //删除,从users和list中都删除该用户,写成函数
	       Iterator <String> iterator =users.keySet().iterator();
		   while(iterator.hasNext())  {
			    String key=iterator.next();
			    if(name.equals(key)){
			             iterator.remove(); } }	
		   ThreadServer.lock.unlock();	    	
	}
	public void closesocket() throws IOException
	{
		   clientSocket.close();sin.close();sout.close();  //关闭线程并置为null
		   clientSocket=null;sin=null;sout=null; return;
	}
	public void quit()
	{
		 ThreadServer.lock.lock();   
		   qunfa(clients, clientSocket, name+" has quit");
		   history(name+" has quit ",name,"false");
		   ThreadServer.lock.unlock();	  
	       sout.println("/quit");
	       a=false; 
	}
   public void yushe(String str1,String str2)
	    { ThreadServer.lock.lock();
		  qunfa(clients, clientSocket, str1);
          history(str1,name,"false");
		  ThreadServer.lock.unlock();		    					 		    					 
		  sout.println(str2); 
          history(str2, name, "true");
	    }
   public void secert(String temp) throws IOException
   {
	   ThreadServer.lock.lock();
	   Iterator <String> iterator =users.keySet().iterator();
	   boolean cunzai=false;
	   while(iterator.hasNext()){
			String key=iterator.next();
			if(temp.contains(key)){
			     cunzai=true;
			     if(!(key.equals(name))){
			          Socket value=(Socket)users.get(key);
			          PrintWriter pw1 = new PrintWriter(value.getOutputStream(),true);
			          temp=temp.replace(key,"");
			          String siliao1=name+"对你说 ："+temp;
			          String siliao2="你对"+key+"说："+temp;
			          pw1.println(siliao1);	 history(siliao1, key, "true");
			          sout.println(siliao2);history(siliao2, name, "true");
			                            }	
		      else{
		    	sout.println("Stop talking to yourself!");}            
			                      }	    				    			
		     }ThreadServer.lock.unlock();
	if(!cunzai){
		sout.println("user_name is not online.");}		    		
   }
   public void hisomeone(String temp) throws IOException
   {
	   boolean qunfa=true;
	   ThreadServer.lock.lock();
	   Iterator <String> iterator =users.keySet().iterator();
	   while(iterator.hasNext()){
			String key=iterator.next();
			if(temp.contains(key)){
				@SuppressWarnings("resource")
				Socket sockettmp=new Socket();
			    sockettmp=users.get(key);
			    String hi1=name+"向"+key+"打招呼，“Hi，你好啊~”",hi2="你向"+key+"打招呼 ：“Hi,你好啊~”";
				for(Socket socket:clients){  
                       if((clientSocket!=socket)&&(socket!=sockettmp)){            
  				          PrintWriter pw = new PrintWriter(socket.getOutputStream(), true); 
  				          pw.println(hi1);}      
                       if(socket==sockettmp){
                      	  PrintWriter pw = new PrintWriter(socket.getOutputStream(), true); 
  				          pw.println(name+"向你打招呼，“Hi，你好啊~”");}
                       }	  history(hi1, name, "false");   				    				
		    	    sout.println(hi2);history(hi2, name, "true");     
				qunfa=false;	    				    			
		                          }		    				    		
	                            }	ThreadServer.lock.unlock();
	  if(qunfa){
		         String hi3=name+"向大家打招呼：“Hi，大家好！我来咯~”",hi4="你向大家打招呼 :“Hi，大家好！我来咯~”";
		         ThreadServer.lock.lock();
                qunfa(clients, clientSocket,hi3);
			    ThreadServer.lock.unlock();
		        sout.println(hi4);
		        history(hi3,name,"false");history(hi4, name, "true");
		       }
   }
   public void history(String content)
   {
	   int k=ThreadServer.jilu.get(0).size()-1;int count=0;//k：总的消息数目，count：用户需要显示消息数目
       for(int j=0;j<=k;j++){if(((ThreadServer.jilu.get(1)).get(j)==name&&(ThreadServer.jilu.get(2).get(j)=="true"))||(((ThreadServer.jilu.get(1)).get(j)!=name)&&((ThreadServer.jilu.get(2)).get(j)=="false")))
       	                    count++;}   //if中判断出对于本用户来说需要显示的消息记录是哪些
   	if(content.equals("/history"))      //无参，最多五十条
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
              int tmp,tmp1;                 //几个式子，从本用户需要显示消息的第tmp1到tmp。。。
              if(d[0]>count)tmp=1;else tmp=count+1-d[0];
              if((d[0]+d[1])>count+1)tmp1=1;else tmp1=count-d[0]-d[1]+2;
              int counttmp=0;              //counttmp计数，找到编号为从tmp1到tmp的消息。
              int counttmp2=tmp-tmp1+1;    //显示条目编号，总共这么多条
              for(int j=0;j<=k;j++)
               {   
           	   if(((ThreadServer.jilu.get(1)).get(j)==name&&(ThreadServer.jilu.get(2).get(j)=="true"))
           			||(((ThreadServer.jilu.get(1)).get(j)!=name)&&((ThreadServer.jilu.get(2)).get(j)=="false"))){
               	    counttmp++;
               	    if(counttmp>=tmp1&&counttmp<=tmp){sout.println(counttmp2+":"+ThreadServer.jilu.get(0).get(j));	counttmp2--; }}     
            }  }						               
                                
   }
   public void who()
   {
	   int count=0;               ThreadServer.lock.lock();
	    Iterator <String> iterator =users.keySet().iterator();
	    while(iterator.hasNext()){
		      String key=iterator.next();
		      sout.println(key);count++;
	                             }	ThreadServer.lock.unlock();	    				    		
	    sout.println("Total online user: "+count );		    			
   }
   public void run()//完成大部分任务，login/quit，command，broadcast，personal
	{ try {
		    sout=new PrintWriter(new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream())),true);
		    sin=new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));		    
		    sout.println("please login");
		    String content=null;
		    while(true){
		    	  if(!a){
		    		     if((content=sin.readLine()).equals("/quit"))            
		    		    	 //未登录就退出，close线程，return
					          {      sout.println("/quit");
					                 deletesocket();
					                  closesocket(); }
		    	        else if(content.contains("/login "))                    
		    	        	//登录：获取姓名，包括检查重名，以及通知其他客户端登陆
		    	              {    name=content.substring(7);
					                dupname();				 
					                 //先检测是否重名,若无重名则加入到列表中
					                  }
		    		    else{sout.println("Invalid command");   }		      
		    		     //无效命令    		
		    	             }
		         if(a){                //登录之后：退出，预设消息，普通消息，私聊，命令/map,/history [a1],[a2];
		    		    if((content=sin.readLine()).equals("/quit"))
		    		        {    quit();
		    			         deletemap();
		    			         deletesocket();
		    			         closesocket();
	                             return;  }		    		
		    		    else{          //非退出命令即进入
		    				   if(content.equals("//smile")){               
                                        //预设消息//smile
		    					      String yushe1=name+" 的脸上浮起微笑";
		    					      String yushe2="你的脸上浮起微笑";
		    					      yushe(yushe1,yushe2);}
		    				   else if(content.equals("//hehe")){            
		    					      //预设消息//hehe
		    					      String yushe1=name+"表示呵呵";
		    					      String yushe2="你表示呵呵";
                                       yushe(yushe1,yushe2);}
		    			        else if(content.contains("/to"))            
		    			        	 //私聊
		    				       {   String temp=content.substring(4);
		    				            secert(temp);   }	
		    				    else if(content.contains("//hi")){       
		    				    	 //预设消息 //hi [somebody],写成函数
		    				    	   String temp=content.substring(4);
		    				    	   hisomeone(temp);   }	
		    				    else if(content.contains("/history")){  
		    				    	   String temp=content;
		    				            history(temp);	  }					 	    
		    				    else if(content.equals("/who")){      
		    				    	   	    	who();     }		    				    
		    				    else{   ThreadServer.lock.lock();
		    				    	          qunfa(clients, clientSocket, name+"说："+content);
		    				    	          ThreadServer.lock.unlock();
		    				    	          sout.println("你说： "+content);
		    				    	          history(name+"说："+content, name, "false");
		    				    	          history("你说： "+content, name, "true");	}	    			      
		    			      }  }
		    		  }}catch(IOException e)
		                      {e.printStackTrace();
		                      System.out.println("Something is wrong in server");}
		                finally{System.out.println("Close the Server socket and the io.");//提示关闭服务器并关闭线程
			                     try{if(clientSocket!=null)clientSocket.close();
			                         if(sin!=null)sin.close();
			                         if(sout!=null)sout.close();}catch(IOException e)
			                                                    {e.printStackTrace();}
		                       }} }

public class ThreadServer
{   
	public static Lock lock=new java.util.concurrent.locks.ReentrantLock(); //线程锁
	public static ArrayList<String> xiaoxi=new ArrayList<String>(),shuyu=new ArrayList<String>(),flag=new ArrayList<String>();;       
	@SuppressWarnings("rawtypes")
	public static ArrayList<ArrayList> jilu=new ArrayList<ArrayList>();    //归属用两个标志，shuyu和flag.其中name---0为除name外广播，name---1为私有。       
	public static final int portNo=12345;       
	public volatile static  CopyOnWriteArrayList<Socket> socketList=new CopyOnWriteArrayList<Socket>();
	public static void main(String[]args)throws IOException
	{   jilu.add(xiaoxi);jilu.add(shuyu);jilu.add(flag);                 //历史消息list
		ServerSocket serversocket=new ServerSocket(portNo);
		System.out.println("The Server is start:"+serversocket);         //正常启动提示
		try {while(true)
			   {  Socket socket=serversocket.accept();                   //阻塞，等待客户端的到来
				  socketList.add(socket);                                //将这个线程存储起来便于后面群发
				  new ServerThreadCode(socket,socketList).start();//run();
			   }
		    }catch(IOException e){e.printStackTrace();}
		          finally{ if(serversocket!=null)serversocket.close();}
	}
}