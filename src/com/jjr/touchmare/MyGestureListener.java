package com.jjr.touchmare;
import com.nokia.mid.ui.gestures.GestureEvent;
import com.nokia.mid.ui.gestures.GestureInteractiveZone;
import com.nokia.mid.ui.gestures.GestureListener;

public class MyGestureListener implements GestureListener {

	private TouchMare midlet;

	public MyGestureListener(TouchMare midlet) {
		this.midlet = midlet;
	}
	
	public void gestureAction(Object container,
        GestureInteractiveZone gestureInteractiveZone,
        GestureEvent gestureEvent) {

		int speed = 0;
		int x = 0, y = 0;	
		float dir = 0;
		
		// retrieve the tap or long tap details and store them
		switch (gestureEvent.getType()) {
		    case GestureInteractiveZone.GESTURE_TAP:
		    	x = gestureEvent.getStartX();
		    	y = gestureEvent.getStartY();		    	
		    	midlet.updatetap(x,y);
		    	break;
		    case GestureInteractiveZone.GESTURE_LONG_PRESS:
		    	midlet.updateLongPress();
		        break;
		    case GestureInteractiveZone.GESTURE_LONG_PRESS_REPEATED:
		        break;
		    case GestureInteractiveZone.GESTURE_DRAG:
		        break;
		    case GestureInteractiveZone.GESTURE_DROP:
		        break;
		    case GestureInteractiveZone.GESTURE_FLICK:
		    	dir = gestureEvent.getFlickDirection();
		    	if ((dir < Math.PI/4) && (dir > -Math.PI/4))
		    	{
		    		speed = Math.abs(gestureEvent.getFlickSpeedX());
			    	midlet.updateMovement(speed, Direction.DIR_RIGHT);
		    	}
		    	else if ((dir > Math.PI*0.75) || (dir < -Math.PI*0.75))
		    	{
		    		speed = Math.abs(gestureEvent.getFlickSpeedX());
			    	midlet.updateMovement(speed, Direction.DIR_LEFT);
		    	}
		    	else {
		    		/* no direction, do nothing*/		  
		    	}
		        break;    		        
		}
	}
}