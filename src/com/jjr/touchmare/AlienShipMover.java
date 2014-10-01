package com.jjr.touchmare;

import java.util.Random;
import javax.microedition.lcdui.game.LayerManager;
import javax.microedition.lcdui.game.Sprite;

public class AlienShipMover extends Thread {

	private boolean isQuit = false;
	private boolean spritePause = false;
	private LayerManager manager;
	private int skillLevel;
	private Sprite alienShip;
	private int maxX = 0, maxY = 0, dir = 0, x = 0, y = 0;
	private AlienAmmo alienAmmo = null;
	private Random number99;
		
	public AlienShipMover(LayerManager manager, int skillLevel, Sprite alienShip, int maxX, int maxY, int x, int y) {
		this.manager = manager;
		this.skillLevel = skillLevel;
		this.alienShip = alienShip;
		this.maxX = maxX;
		this.maxY = maxY;
		this.x = x;
		this.y = y;
	}

	public void ammoEnd() {
		alienAmmo = null;
	}
	
	public void pause() {
		spritePause = true;
		if ((alienAmmo != null) && (alienAmmo.isAlive())) {
			alienAmmo.pause();
		}
	}
	
	public void resume() {
		spritePause = false;
		if ((alienAmmo != null) && (alienAmmo.isAlive())) {
			alienAmmo.resume();
		}
	}
	
	public void quit() {
		isQuit = true;	        
	}	

	public void run() {
		int dir99;
		
		number99 = new Random();
		dir99 = random99();
		
		if (dir99 < 50) {
			dir = -5;
		} else {
			dir = 5;
		}
			
		while(isQuit == false) { 
			while (spritePause) {
				try {
					Thread.sleep(40);
				} catch(InterruptedException ex) {
					return;
				}	   	
			}
			x = x + dir;
			if (x >= maxX ) {
				x=0;
			} else if (x < 0) {
				x=maxX;
			}			
			alienShip.setRefPixelPosition(x,y);			    
			if (random99() < skillLevel +3) {
				if (alienAmmo == null) {
					alienAmmo = new AlienAmmo(this,skillLevel, manager, x, y, maxY);
					alienAmmo.start();
				}
			}				
			try {
				Thread.sleep(200-skillLevel*15);
			} catch(InterruptedException ex) {
				return;
			}	   				
		}	
	}

	/* ret is 0-99 */
	private int random99(){
		int retVal = 0;
		
		retVal = number99.nextInt(100);
		return retVal;
	}	
}
