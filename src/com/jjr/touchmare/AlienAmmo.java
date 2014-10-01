package com.jjr.touchmare;
import javax.microedition.lcdui.Image;
import javax.microedition.lcdui.game.LayerManager;
import javax.microedition.lcdui.game.Sprite;

public class AlienAmmo extends Thread {
	private AlienShipMover parent;
	private int skillLevel;
	private LayerManager manager;
    private boolean isQuit = false;
    private boolean ammoPause = false;
	private Image ammoImage;
	private Sprite alienAmmo;
	private int x = 0, y = 0, maxY = 0;
	
	public AlienAmmo(AlienShipMover parent, int skillLevel, LayerManager manager, int x, int y, int maxY) {
		this.parent = parent;
		this.skillLevel = skillLevel;
		this.manager = manager;
		this.x = x;
		this.y = y;
		this.maxY = maxY;
	}

	public void quit() {
		isQuit = true;	        
	}	

	public void pause() {
		ammoPause = true;
	}
	
	public void resume() {
		ammoPause = false;
	}
	
	public void run() {  		
		try {
			ammoImage = Image.createImage("/alienammo.png");
	    } catch(Exception e) { 
	    	return; 
	    }
	    alienAmmo = new Sprite(ammoImage,ammoImage.getWidth(),ammoImage.getHeight());
	    alienAmmo.defineReferencePixel(ammoImage.getWidth()/2,0);
	    alienAmmo.setRefPixelPosition(x,y);
	    
		try {
	    	manager.insert(alienAmmo, 0);
	    } catch(Exception e) { 
	    	return; /* oops */
	    }	    
		while(isQuit == false) {
			while (ammoPause) {
				try {
					Thread.sleep(40);
				} catch(InterruptedException ex) {
					   return;
				}	   	
			}		
			y = y+2;
	        if (y >= maxY ) {
	        	quit();
	        } else {
	        	alienAmmo.setRefPixelPosition(x,y);	        	
		        try {
		        	Thread.sleep(15-skillLevel);
		        } catch(InterruptedException ex) {
		        	return;
		        }
	        }
		}
		
		if (alienAmmo.isVisible())
		{
			try {
				manager.remove(alienAmmo);
			} catch(Exception ex) {
        		return;
        	}
		}
		
	    if (parent.isAlive()) {
	    	parent.ammoEnd();
	    }
	}	   	   
}
