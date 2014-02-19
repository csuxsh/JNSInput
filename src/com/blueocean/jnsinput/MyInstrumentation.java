package com.blueocean.jnsinput;

import java.lang.reflect.Method;

import android.app.Instrumentation;
import android.hardware.input.InputManager;
import android.util.Log;
import android.view.InputDevice;
import android.view.MotionEvent;
import android.os.SystemClock;
import android.view.KeyEvent;

public class MyInstrumentation extends Instrumentation {
	
	public MyInstrumentation()
	{
		super();
	}
		@Override
	public void sendKeySync(KeyEvent event)
	{
		long downTime = event.getDownTime();
        	long eventTime = event.getEventTime();
        	int action = event.getAction();
        	int code = event.getKeyCode();
        	int repeatCount = event.getRepeatCount();
        	int metaState = event.getMetaState();
        	int deviceId = event.getDeviceId();
        	int scancode = event.getScanCode();
        	int source = event.getSource();
        	int flags = event.getFlags();
       	 if (source == InputDevice.SOURCE_UNKNOWN) {
           		 source = InputDevice.SOURCE_KEYBOARD;
        	}
       	 if (eventTime == 0) {
         	   eventTime = SystemClock.uptimeMillis();
        	}
        	if (downTime == 0) {
        	    downTime = eventTime;
       	 	}
        	KeyEvent newEvent = new KeyEvent(downTime, eventTime, action, code, repeatCount, metaState,
                deviceId, scancode, flags | KeyEvent.FLAG_FROM_SYSTEM, source);
        	InputManager.getInstance().injectInputEvent(newEvent,
                InputManager.INJECT_INPUT_EVENT_MODE_WAIT_FOR_FINISH);
	}
	@Override
	public void sendPointerSync(MotionEvent event) {;
        if ((event.getSource() & InputDevice.SOURCE_CLASS_POINTER) == 0) {
            event.setSource(InputDevice.SOURCE_TOUCHSCREEN);
        }
        InputManager.getInstance().injectInputEvent(event,
                InputManager.INJECT_INPUT_EVENT_MODE_WAIT_FOR_FINISH);
        /*
        try{
        Class<InputManager> class1 = (Class<InputManager>) InputManager.class;
        Method  getinstance = class1.getMethod("getInstance");
        Method  inject = class1.getMethod("injectInputEvent", new Class[]{MotionEvent.class, Integer.class});
        Log.d("JNSInput", "reflect ok!");
        InputManager im = (InputManager) getinstance.invoke(InputManager.class);
        if(im == null)
        	Log.d("JNSInput", "im instance is null!");
        Log.d("JNSInput", "inject event");
        inject.invoke(im, event,2);
        }
        catch(Exception e)
        {
        	e.printStackTrace();
        }
        */
     }
}
