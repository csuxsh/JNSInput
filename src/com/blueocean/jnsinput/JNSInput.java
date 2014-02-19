package com.blueocean.jnsinput;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;


import android.app.Instrumentation;
import android.os.SystemClock;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.MotionEvent.PointerCoords;  
import android.view.MotionEvent.PointerProperties;  
import android.os.Build;

class JNSInput extends Thread{
	private static final String TAG = "JNSInput";
	private Socket socket = null;
	private BufferedReader is = null;
	private PrintWriter pw;
	static List<Pos> pos_list = new ArrayList<Pos>();
	private final static int POS_X_INDEX = 0x01;
	private final static int POS_Y_INDEX = 0x02;
	private final static int POS_TAG_INDEX = 0x03;
	private final static int POS_VALUE_INDEX = 0x04;
	public static final int RIGHT_JOYSTICK_TAG = 1;
	public static final int LEFT_JOYSTICK_TAG = 2;

	JNSInput(Socket socket)
	{
		super();
		this.socket = socket;
	}

	public void run() {
		System.err.println("monitorRunnable");
		try {
			is = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			pw = new PrintWriter(socket.getOutputStream());
			System.err.println("connect address = " + socket.getInetAddress());
			System.err.println("socket.isclosed = " + socket.isConnected());
			while (!socket.isClosed()) {
				try {
					//System.err.println("is.readline");
					String line = is.readLine();
					//Log.d(TAG, "line = " + line);
					if (line != null) {
						processData(line);
					}
				} catch (IOException e) {
					e.printStackTrace();
					Log.d(TAG, "IO EXCEPTION ");
				}
			}
			Log.d(TAG, "this sokect close");
			socket.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}


private void processData(String data) {
	String[] listData = data.split(":");
	//if (debug) System.err.println("listData = " + listData);
	//Log.d(TAG, "current time is "+System.currentTimeMillis());
	if (listData[0].equals("injectKey")) {
		injectKeyProcess(listData);
	} else if (listData[0].equals("injectTouch")) {
		injectTouchProcess(listData);
	}else if(listData[0].equals("geteventlock")) {
		if(pos_list.size() > 0)
			pw.print("lock:true\n");
		else
			pw.print("lock:false\n");
		pw.flush();
	}
}

private void injectKeyProcess(String[] listD) {
	if (listD.length < 5) {
		System.err.println("Invalid content = " + listD);
		return;
	}
	int eventCode = Integer.parseInt(listD[1]);
	int state = Integer.parseInt(listD[3]);
	int eventDeviceId = Integer.parseInt(listD[4]);
	long now = SystemClock.uptimeMillis();
	KeyEvent event = new KeyEvent(now, now, (state == 0) ? KeyEvent.ACTION_DOWN : KeyEvent.ACTION_UP, eventCode, 0, 0,eventDeviceId, 0xffff);
	injectKeyEvent(event);
}

public static void sendMotionDownByInstrumentation(Pos pos_in) 
{

//	pos_list.add(pos_in);
	for(Pos pos : pos_list)
	{
		if(((pos.x == pos_in.x) && (pos.y == pos_in.y)) ||((pos.tag == pos_in.tag) && ((pos.tag == RIGHT_JOYSTICK_TAG)|| (pos.tag ==LEFT_JOYSTICK_TAG))))
			return ;
	}
	pos_list.add(pos_in);
	final PointerProperties[] properties = new PointerProperties[pos_list.size()];  
	final PointerCoords[] pointerCoords = new PointerCoords[pos_list.size()];  
	//Log.d(TAG,"pointcount="+pos_list.size());

	//Log.d(TAG, "add pos x ="+pos_in.x+"y="+pos_in.y );
	for(int i = 0; i <pos_list.size(); i++)
	{
		Pos pos  = pos_list.get(i);
	
		
		PointerProperties pp = new PointerProperties();  
		pp.id= i;  
		pp.toolType = MotionEvent.TOOL_TYPE_FINGER;  
		properties[i] = pp;  

		PointerCoords pc = new PointerCoords();  
		pc.x = pos.x;  
		pc.y = pos.y;  
		pc.pressure = 1;  
		pc.size = 1;  
		pointerCoords[i] = pc;  
	}
	//Log.d(TAG, "itroter list ok");
	//add by steven
	//Log.d(TAG,"x ="+pointerCoords[0].x+",y="+pointerCoords[0].y);
	if(pos_list.size() == 1)
	{
		//Log.d(TAG, "listsize = "+pos_list.size());
		injectTouchEvent(MotionEvent.obtain(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(),  
				MotionEvent.ACTION_DOWN, 1, properties,  
				pointerCoords, 0, 0, 1, 1, 0, 0, 0, 0));
	}
	else 
	{
		injectTouchEvent(MotionEvent.obtain(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(),  
				(MotionEvent.ACTION_POINTER_DOWN | ((pos_list.size()-1) << 8)), pos_list.size(), properties, pointerCoords, 0, 0, 1, 1, 0, 0, 0, 0));
		//Log.d(TAG, "-----------------------------the "+pos_list.size()+"th pos---------------------------");			
	}	

}
public static void sendMotionMoveByInstrumentation(Pos pos_in)
{

	final PointerProperties[] properties = new PointerProperties[pos_list.size()];  
	final PointerCoords[] pointerCoords = new PointerCoords[pos_list.size()];  
	//Log.d(TAG,"pointcount="+pos_list.size());
	if(pos_list.size() == 0)
	{
		return;
	}

	//Log.d("testdownandup", "add pos x ="+tx+"y="+ty );
	for(int i = 0; i <pos_list.size(); i++)
	{
		Pos pos  = pos_list.get(i);
		PointerProperties pp = new PointerProperties();   
		pp.id= i;  
		pp.toolType = MotionEvent.TOOL_TYPE_FINGER;  
		properties[i] = pp;  

		PointerCoords pc = new PointerCoords();  
		pc.x = pos.x;  
		pc.y = pos.y;  
		pc.pressure = 1;  
		pc.size = 1;  
		if((pos.tag == pos_in.tag) && ((pos.tag == RIGHT_JOYSTICK_TAG)|| (pos.tag ==LEFT_JOYSTICK_TAG)))
		{
			pc.x = pos_in.x;  
			pc.y = pos_in.y;
			pos.x = pos_in.x;
			pos.y = pos_in.y;
			pos_list.set(i,pos);
		}
		pointerCoords[i] = pc;  
	}
	//Log.d("testdownandup", "itroter list ok");
	//add by steven
	if(pos_list.size() == 1)

		injectTouchEvent(MotionEvent.obtain(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(),  
				MotionEvent.ACTION_MOVE, 1, properties,  
				pointerCoords, 0, 0, 1, 1, 0, 0, 0, 0));
	else 
	{
		injectTouchEvent(MotionEvent.obtain(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(),  
				(MotionEvent.ACTION_MOVE | ((pos_list.size()-1) << 8)), pos_list.size(), properties, pointerCoords, 0, 0, 1, 1, 0, 0, 0, 0));
		//Log.d(TAG, "-----------------------------the "+pos_list.size()+"th pos moved---------------------------");			
	}

}
public static void sendMotionUpByInstrumentation(Pos pos_in) {


	final PointerProperties[] properties = new PointerProperties[pos_list.size()];  
	final PointerCoords[] pointerCoords = new PointerCoords[pos_list.size()];  
	int rm_index = 0;

	if(pos_list.size() == 0)
	{	
		return ;
	}
	//Log.d(TAG, "del pos x ="+pos_in.x+"y="+pos_in.y );
	for(int i =0; i<pos_list.size(); i++)
	{
		Pos pos = pos_list.get(i);
		//	if(pos.x == gx && pos.y == gy)
		//	{
		//		
		//		Log.d(TAG,"remove x="+gx+ ", y="+gy);
		PointerProperties pp = new PointerProperties();
		//long downTime = SystemClock.uptimeMillis();  
		//long eventTime = SystemClock.uptimeMillis();     
		pp.id= i;  
		pp.toolType = MotionEvent.TOOL_TYPE_FINGER;  
		properties[i] = pp;  

		PointerCoords pc = new PointerCoords();  
		pc.x = pos.x;  
		pc.y = pos.y;  
		pc.pressure = 1;  
		pc.size = 1;  

		if(pos.x == pos_in.x && pos.y == pos_in.y) 
			rm_index = i;
		if((pos.tag == pos_in.tag) && ((pos.tag == RIGHT_JOYSTICK_TAG)|| (pos.tag ==LEFT_JOYSTICK_TAG)))
		{
			pc.x = pos_in.x;
			pc.y = pos_in.y;
			rm_index = i;
		}

		pointerCoords[i] = pc;  
		//Log.d(TAG, "tag = "+pos_in.tag );

		//break;
		//}
		//instrumentation.sendPointerSync(MotionEvent.obtain(SystemClock.uptimeMillis(),SystemClock.uptimeMillis(), MotionEvent.ACTION_UP, tx, ty, 0));			
	}

	//add by steven
	//Log.d(TAG,"remove x="+pos_in.x+ ", y="+pos_in.y);
	//Log.d(TAG,"remove x="+pointerCoords[rm_index].x+", y="+pointerCoords[rm_index].y);

	PointerCoords temp =  pointerCoords[rm_index];
	pointerCoords[rm_index] = pointerCoords[pos_list.size()-1];
	pointerCoords[pos_list.size()-1] = temp;
	//Log.d(TAG,"change ok");

	if(pos_list.size() == 1)
		injectTouchEvent(MotionEvent.obtain(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(),  
				MotionEvent.ACTION_UP, 1, properties,  
				pointerCoords, 0, 0, 1, 1, 0, 0, 0, 0));
	else
		injectTouchEvent(MotionEvent.obtain(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(),  
				(MotionEvent.ACTION_POINTER_UP | ((pos_list.size()-1) << 8)), pos_list.size(), properties,  
				pointerCoords, 0, 0, 1, 1, 0, 0, 0, 0));

	pos_list.remove(rm_index);

}

private void injectTouchProcess(String[] listD) {

	if(listD == null)
		System.err.println("listD is null");
	Pos pos = new Pos(Float.valueOf(listD[POS_X_INDEX]), Float.valueOf(listD[POS_Y_INDEX]),
			Integer.parseInt(listD[POS_VALUE_INDEX]),Integer.parseInt(listD[POS_TAG_INDEX]));
	switch(pos.action)
	{
	case MotionEvent.ACTION_DOWN:
		sendMotionDownByInstrumentation(pos);
		System.err.println("send a postion down");
		break;
	case MotionEvent.ACTION_MOVE:
		sendMotionMoveByInstrumentation(pos);
		System.err.println("send a postion move");
		break;
	case MotionEvent.ACTION_UP:
		sendMotionUpByInstrumentation(pos);
		System.err.println("send a postion up");
		break;
	}
}

private void injectKeyEvent(final KeyEvent event) {
	if (event == null) {
		System.err.println("KeyEvent event == null");
		return;
	}
	//Log.d(TAG, "key "+event.getKeyCode()+"; action ="+event.getAction());
	new Thread(new Runnable() {
		public void run() {
			try {
				/*
				(IWindowManager.Stub
						.asInterface(ServiceManager.getService("window")))
						.injectKeyEvent(event, true);
						*/
				if(Build.VERSION.SDK_INT > 17)
					(new MyInstrumentation()).sendKeySync(event);
				else
					(new Instrumentation()).sendKeySync(event);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}).start();
}

private static void injectTouchEvent(final MotionEvent event) {
	if (event == null) {
		System.err.println(" MotionEvent  event == null");
		return;
	}
	new Thread(new Runnable() {
		public void run() {
			try {
				/*
				(IWindowManager.Stub
						.asInterface(ServiceManager.getService("window")))
						.injectPointerEvent(event, true);
						*/
				if(Build.VERSION.SDK_INT > 17)
					(new MyInstrumentation()).sendPointerSync(event);
				else
					(new Instrumentation()).sendPointerSync(event);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}).start();
}
}
