package com.jjr.touchmare;
import javax.microedition.media.MediaException;
import javax.microedition.media.Player;
import javax.microedition.media.PlayerListener;
import javax.microedition.midlet.MIDlet;
import javax.microedition.midlet.MIDletStateChangeException;
import javax.microedition.rms.RecordStore;
import javax.microedition.rms.RecordStoreException;
import javax.microedition.lcdui.*;
import com.nokia.mid.ui.gestures.*;

public class TouchMare extends MIDlet implements PlayerListener {

	private RecordStore rs = null;
	private int speed = 0;
	private int dir = 0;
	private int tapX = 0, tapY= 0;
	
	private boolean movementUpdated = false;
	private boolean tapUpdated = false;
	private boolean longPressUpdated = false;
	
	private Display display;
	private GestureCanvas myCanvas;
	private MyGestureListener myGestureListener;

	private Player midiPlayer = null;
	private boolean midiPlayerOn = false;
	private boolean playerSuspended  = false;

	public TouchMare() {	
		rs = createStore();
		display=Display.getDisplay(this);		
		myCanvas = new GestureCanvas(this);
		display.setCurrent(myCanvas);
		myCanvas.start();
	
		GestureInteractiveZone giz = new GestureInteractiveZone( 
				GestureInteractiveZone.GESTURE_LONG_PRESS | 
				GestureInteractiveZone.GESTURE_TAP | GestureInteractiveZone.GESTURE_FLICK );	
		GestureRegistrationManager.register( myCanvas, giz );		
		myGestureListener = new MyGestureListener(this);
		GestureRegistrationManager.setListener(myCanvas, myGestureListener);	
	}

    public void updatetap(int x, int y) {
    	tapX = x;
    	tapY = y;
    	tapUpdated = true;    	
    }

    public boolean istapUpdated() {
    	boolean tem;
    	
    	tem = tapUpdated;
    	tapUpdated = false;
    	return tem;    	
    }

    public int tapX() {
    	int tem;
    	
    	tem = tapX;
    	tapX = 0;
    	return tem;    	
    }
 
    public int tapY() {
    	int tem;
    	
    	tem = tapY;
    	tapY = 0;
    	return tem;    	
    }
    
    public void updateLongPress() {
    	longPressUpdated = true;    	
    }

    public boolean isLongPressUpdated() {
    	boolean tem;
    	
    	tem = longPressUpdated;
    	longPressUpdated = false;
    	return tem;    	
    }
    
    public void updateMovement(int speed, int dir) {

    	this.speed = speed;
    	this.dir = dir;
    	movementUpdated = true;   	
    }

    public boolean isMovementUpdated() {
    	boolean tem;
    	
    	tem = movementUpdated;
    	movementUpdated = false;
    	return tem;    	
    }

    public int MovementSpeed() {
    	int tem;
    	
    	tem = speed;
    	speed = 0;
    	return tem;    	
    }
    
    public int MovementDir() {
    	return dir;    	
    }
    
	protected void destroyApp(boolean unconditional) {
		notifyDestroyed();
	}

	protected void pauseApp() {
	}

	protected void startApp() throws MIDletStateChangeException {
	}

	public void playerUpdate(Player player, 
		String event, Object data) {
		
		midiPlayer = player;

		if(event == PlayerListener.STARTED) {
			midiPlayer = player;
			midiPlayerOn = true;	
		}
		if(event == PlayerListener.STOPPED) {
			midiPlayerOn = false;
			playerSuspended = false;
		}
		if(event == PlayerListener.DEVICE_UNAVAILABLE) {
			if (midiPlayerOn) {
				try {
					midiPlayer.stop();
				} catch (MediaException e) {
					/* do nothing */
				}
				midiPlayerOn = false;
				playerSuspended  = true;
			}
		}
		if(event == PlayerListener.DEVICE_AVAILABLE) {
			if (playerSuspended) {
				playerSuspended = false;
				try {
					midiPlayer.start();
				} catch (MediaException e) {
					/* do nothing */
				}
			}
		}
		if(event == PlayerListener.CLOSED) {
			midiPlayerOn = false;
			playerSuspended = false;
			midiPlayer = null;
		} else {
			/* do nothing */
		}	
	}

    public void updateHiScoreRS(int points) {
    	if (rs == null) {
    		/* do nothing */
    	} else {
    		try {
				byte[] data = rs.getRecord(1);
				data[0] = (byte)(points/100);
				data[1] = (byte)(points % 100);
				rs.setRecord( 1, data, 0, data.length );				
    		} catch( RecordStoreException e ){
    			// fail
    		}
    	}   	
    }
    
    public int getHiScoreRS() {
    	int ret = 0;
    	if (rs == null) {
    		/* do nothing */
    	} else {
    		try {
				byte[] data = rs.getRecord(1);
				ret = data[0]*100+data[1];
    		} catch( RecordStoreException e ){
    			// couldn't open it or create it
    		}
    	}
    	return ret;
    }
    	
	private RecordStore createStore() {
		RecordStore rsTem = null;
		
		byte[] data = new byte[]{ 0, 0 }; /* hiscore hi, lo */

		try {
			rsTem = RecordStore.openRecordStore( "TMareHiScore", true );
			if( rsTem.getNumRecords() == 0 ){
				rsTem.addRecord( data, 0, data.length );
			    // record store is empty, re-initialize				
			}
		} catch( RecordStoreException e ){
			// couldn't open it or create it
		}
		return rsTem;
	}
}
