package com.jjr.touchmare;
import javax.microedition.lcdui.Image;
import javax.microedition.lcdui.game.LayerManager;
import javax.microedition.lcdui.game.Sprite;

public class OwnAmmo extends Thread {
	private LayerManager manager;
	private int skillLevel;
    private boolean isQuit = false;
	private Image ammoImage;
	private Sprite ownAmmo = null;
	private int xOwnAmmo = 0, yOwnAmmo = 0;
	
	public OwnAmmo(LayerManager manager, int skillLevel, int x, int y) {
		this.manager = manager;
		this.skillLevel = skillLevel;
		xOwnAmmo = x;
		yOwnAmmo = y-2; /* -2 to make some room */
		
		try {
			ammoImage = Image.createImage("/ownammo.png");
	    } catch(Exception e) { 
	    	return; 
	    }
	    ownAmmo = new Sprite(ammoImage,7,7);
	    ownAmmo.defineReferencePixel(3,6);
	    ownAmmo.setRefPixelPosition(xOwnAmmo,yOwnAmmo);	    
		manager.insert(ownAmmo, 0);
	}

	public void quit() {
		isQuit = true;	        
	}	

	public void run() {
        while(isQuit == false) { 
        	yOwnAmmo = yOwnAmmo-2;
        	if (yOwnAmmo <= 25 ) {
        		break;
        	}
        	ownAmmo.setRefPixelPosition(xOwnAmmo,yOwnAmmo);        	
            try {
                Thread.sleep(11-skillLevel);
            } catch(InterruptedException ex) {
                return;
            }
        }
		if (ownAmmo.isVisible())
		{
			try {
				manager.remove(ownAmmo);
			} catch(Exception ex) {
        		return;
        	}
		}    
	}	   	   
}
