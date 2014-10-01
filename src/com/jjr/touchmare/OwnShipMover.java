package com.jjr.touchmare;
import javax.microedition.lcdui.game.Sprite;

public class OwnShipMover extends Thread {
	static final int DELTA = 2; /* pixels per one step */
	private boolean isQuit = false;
	private int skillLevel;
	private Sprite ownShip;
	private int maxX = 0, x = 0, y = 0, speed = 0, dir = 0;
		
	public OwnShipMover(int skillLevel, Sprite ownShip, int maxX, int speed, int dir) {
		this.skillLevel = skillLevel;
		this.ownShip = ownShip;
		this.maxX = maxX;
		x = this.ownShip.getRefPixelX();
		y = this.ownShip.getRefPixelY();
		this.speed = speed;
		this.dir = dir;
	}

	public void quit() {
		isQuit = true;	        
	}	

	public void run() { 
		int delay, loops, delta;
		
		delay = 11-skillLevel; /* ms */
		loops  = (int)(speed/20);
		if (loops > 100 ) {
			loops = 100;
		}
		delta = DELTA;
		loops = loops/delta;
		
		while(isQuit == false) {				
			if (dir == Direction.DIR_LEFT ) { /* DIR_LEFT */
				x  = x - delta;
				if (x < 0 ) {
					x = 0;
				}
				ownShip.setRefPixelPosition(x,y);
				if (x <= 0) {
					break;
				}
			} else if (dir == Direction.DIR_RIGHT ) { /* DIR_RIGHT */
				x  = x + delta;
				if (x > (maxX-1) ) {
					x = (maxX-1);
				}
				ownShip.setRefPixelPosition(x,y);
				if (x >= (maxX-1)) {
					break;
				}
			}
							
			try {
		        Thread.sleep(delay);
			} catch(InterruptedException ex) {
		        return;
		    }
		
			loops--;
			if (loops == 0) {
				break;
			}
		}
	}	
}
