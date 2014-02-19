package com.blueocean.jnsinput;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import android.util.Log;

public class JNSInputServer {

	public static final String TAG = "JNSInputServer";
	private List<Socket> socketlist = new ArrayList<Socket>();
	private List<JNSInput> thlist = new ArrayList<JNSInput>();
	public static void main(String[] args) {
		Log.d(TAG, "a new server run");
		JNSInputServer mInputServer = new JNSInputServer();
		mInputServer.createServer();
	}

	public void createServer() {
		try 
		{
			ServerSocket mServerSocket = new ServerSocket(44444);

			while (true) {
				Socket socket = mServerSocket.accept();
				Log.d(TAG, "a new socket connect");
				// 关闭之前之前的socket与线程
				for(int i = 0; i < socketlist.size(); i++)
				{
					socketlist.get(i).close();
				//	thlist.get(i).stop();
				}
				socketlist.clear();
				//thlist.clear();
				// 开始新的服务线程
				JNSInput jnsinput = new JNSInput(socket);
				jnsinput.start();
				socketlist.add(socket);
				//thlist.add(jnsinput);
			}
		} catch(Exception e) 
		{
			e.printStackTrace();
			try {
				Thread.sleep(1000);
			} 
			catch (InterruptedException e1)
			{
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}

		}
	}	
}
