package com.jjr.touchmare;
import javax.microedition.lcdui.game.LayerManager;
import javax.microedition.lcdui.game.Sprite;

public class ShipDestroy extends Thread {

	private GestureCanvas parent;
    private LayerManager manager;
	private Sprite ship;
	private int index,curFrame,totFrame;

public ShipDestroy(GestureCanvas parent, LayerManager manager, Sprite ship, int index, int curFrame, int totFrame) {
	this.parent = parent;
	this.manager = manager;
	this.ship = ship;
	this.index = index;
	this.curFrame = curFrame;
	this.totFrame = totFrame;
}

	public void run() {
		int i;
		
		if (curFrame < totFrame) {
			for(i=curFrame+1;i<totFrame;i++) {
				ship.setFrame(i);
				try {
					Thread.sleep(Constant.DESTROYDELAY);
				} catch(InterruptedException ex) {
					return;
				}
			}
		}
		manager.remove(ship);
		if (index < Constant.MAXALIENSHIPS) {
			parent.updateAlienShip(index, null);
		} else if (index == Constant.NOTALIENSHIP) {
			parent.updateOwnShipHit(false);
		}
	}	
}
