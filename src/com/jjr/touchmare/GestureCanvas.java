package com.jjr.touchmare;
import java.io.IOException;
import java.io.InputStream;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;
import javax.microedition.lcdui.game.GameCanvas;
import javax.microedition.lcdui.game.LayerManager;
import javax.microedition.lcdui.game.Sprite;
import javax.microedition.lcdui.game.TiledLayer;
import javax.microedition.media.Manager;
import javax.microedition.media.MediaException;
import javax.microedition.media.Player;
import com.nokia.mid.ui.DeviceControl;

public class GestureCanvas extends GameCanvas implements Runnable { 
	static final int TOTALIENSHIPS = 100;
	static final int INITIALALIENSHIPS = 5;
	
	static final int MAXSHELTERHITS = 5;
	
	static final int MAXOWNSHIPS = 10;
	static final int MAXOWNAMMO = 150; // was 150
	
	static final int ALIENHITPOINTS = 10;
	static final int OWNAMMOPOINTS = -2;
	static final int OWNHITPOINTS = -5;
	
	static final int MAXSKILLLEVEL = 10;
	static final int MINSKILLLEVEL = 1;

	static final int ALIENAMMOW = 8; /* WIDTH */
	static final int ALIENAMMOH = 8; /* HEIGTH */
	static final int OWNAMMOW = 7; /* WIDTH */
	static final int OWNAMMOH = 7; /* HEIGTH */
	static final int MENUX = 20;
	static final int MENUY = 70; /* was 50 */
	static final int MENUXWIDTH = 200;
	static final int MENUYHEIGTH = 160;
	static final int MENUYSLOTHEIGTH = 40;
	
	private TouchMare midlet;

    private boolean isQuit = false;
    private Player midiPlayer = null;
    private boolean playerStoppedByUser = false;
	private Sprite ownShip;
	
	private Sprite alienShip[] = new Sprite[Constant.MAXALIENSHIPS];
	private AlienShipMover alienshipMover[] = new AlienShipMover[Constant.MAXALIENSHIPS];
	
	private LayerManager manager;
	private int xOwnShip = 0, yOwnShip = 0;
	private OwnAmmo ownAmmo = null;
	private OwnShipMover ownShipMover = null;
	private int speed = 0, dir = 0;
	
	private int gamePoints = 0;
	private int alienShipsLeft = TOTALIENSHIPS;
	private int ownShipsLeft = MAXOWNSHIPS;
	private int ownAmmoLeft = MAXOWNAMMO;
	private int skillLevel = MINSKILLLEVEL;
	private int shelterHits = 0; /* per frame */
	private Sprite shelterSprite = null;
	
	private Player alienHitPlayer = null;
	private InputStream alienHitStreamSound = null;	
	private Player ownHitPlayer = null;
	private InputStream ownHitStreamSound = null;
	private Player shelterHitPlayer = null;
	private InputStream shelterHitStreamSound = null;
	
	private Image ownShipImage;
	private Image alienshipImage;
	private boolean ownShipHit = false;
	
	public GestureCanvas(TouchMare midlet) {
		/* Gamecanvas constructor */
	    super(true);	    
	    this.midlet = midlet;	    
	    setFullScreenMode(true);	    
	    manager = new LayerManager();
	    DeviceControl.setLights(0, 100); /* lights on */
	}

    public void quit() {
        isQuit = true;
    }
    
    public synchronized void updateOwnShipHit(boolean value) {
    	ownShipHit = value;
    }
 
    public synchronized boolean getOwnShipHit() {
    	return ownShipHit;
    }
    
    public synchronized void updateAlienShip(int index, Sprite sprite) {
    	if (index < Constant.MAXALIENSHIPS) {
    		alienShip[index] = sprite;
    	}
    }
    
    private synchronized Sprite getAlienShip(int index) {
    	Sprite sprite = null;
    	
    	if (index < Constant.MAXALIENSHIPS) {
    		sprite = alienShip[index];
    	}
    	return sprite;
    }
    
    private void updateGamePoints(int points) {
    	gamePoints = gamePoints + points;
    	if (gamePoints < 0) {
    		gamePoints = 0;
    	}
    }
        
    private Sprite makeOwnShip(int x, int y) {
    	Sprite ship;

	    ship = new Sprite(ownShipImage,20,20);
	    ship.defineReferencePixel(10,0);
	    ship.setFrame(0);
	    ship.setRefPixelPosition(x,y);
	    manager.insert(ship, 0); 
	    return ship;
    }
    
    private Sprite makeShelter() {
    	Sprite shelter;
    	Image shelterImage;
    	int x,y;

		try {
			shelterImage = Image.createImage("/shelter5fr.png");
	    } catch(Exception e) {
	    	return null; 
	    }
	    shelter = new Sprite(shelterImage,100,20);
	    shelter.defineReferencePixel(50,19);
	    shelter.setFrame(0);
	    x=getWidth()/2;
	    y=getHeight()-60; /* was 55 */	    
	    shelter.setRefPixelPosition(x,y);    
	    return shelter;
    }
    
    private void makeAlienShip(int i,int x,int y) {
    	Sprite sprite;
    	
	    sprite = new Sprite(alienshipImage,20,20);
	    updateAlienShip(i, sprite);
	    
	    getAlienShip(i).defineReferencePixel(10,19); 
	    getAlienShip(i).setFrame(0);
	    getAlienShip(i).setRefPixelPosition(x,y);
	    manager.insert(getAlienShip(i), 0);	
		alienshipMover[i] = new AlienShipMover(manager, skillLevel, getAlienShip(i), getWidth(), getHeight()-40, x, y);
		alienshipMover[i].start();
    }

	public void run() {		
		int screenW = getWidth();
		int screenH = getHeight();
		Graphics g = getGraphics();
		ShipDestroy ownShipDestroy = null;
		boolean newOwnShip = false;		
		final TiledLayer tiledBackground;

		showIntro();
				
		shelterSprite = makeShelter();
		if (shelterSprite != null) {
			manager.append(shelterSprite);
		}

		tiledBackground = createBackground();
		manager.append(tiledBackground);
			
    	for (int i=0;i < Constant.MAXALIENSHIPS;i++) {
    		updateAlienShip(i, null );
    		alienshipMover[i] = null;
    	}
		
		try {
			alienshipImage = Image.createImage("/alien4fr.png");
	    } catch(Exception e) { 
	    }
    
    	for (int j=0;j < INITIALALIENSHIPS;j++) {
    		makeAlienShip(j, j*20, j*21+40); 		
    	}
    	
    	try {
			ownShipImage = Image.createImage("/ownship4fr.png");
	    } catch(Exception e) { 
	    }
	    
    	xOwnShip = screenW/2;
    	yOwnShip = screenH-50;
    	ownShip = makeOwnShip(xOwnShip,yOwnShip);

		try	{
			alienHitStreamSound = getClass().getResourceAsStream("/alienhit.wav");
			alienHitPlayer = Manager.createPlayer(alienHitStreamSound, "audio/X-wav");
		}
		catch (IOException ioe){}
		catch (MediaException me){}
			
    	try	{
			ownHitStreamSound = getClass().getResourceAsStream("/ownhit.wav");				
			ownHitPlayer = Manager.createPlayer(ownHitStreamSound, "audio/X-wav");
		}
		catch (IOException ioe){}
		catch (MediaException me){}
			

    	try	{
			shelterHitStreamSound = getClass().getResourceAsStream("/shelterhit.wav");
			shelterHitPlayer = Manager.createPlayer(shelterHitStreamSound, "audio/X-wav");
		}
		catch (IOException ioe){}
		catch (MediaException me){}
			
    	
		while(isQuit == false) {
			/* first check if game is over */
			if ((alienShipsLeft == 0) || (ownShipsLeft == 0)) {
				gameOver();
			}
			if ((ownAmmoLeft == 0) && !checkOwnAmmoAlive()) {
				gameOver();
			}			
			/* check collision */
			checkAlienShipCollision();			
			/* check collision */
			checkShelterCollision();
			
			if (!getOwnShipHit() && !newOwnShip) {
				/* check collision */
				if (checkOwnShipCollision()) {
					updateOwnShipHit(true);
					newOwnShip = true;
					ownShipsLeft--;
					if (ownShipsLeft > 0) {
						newOwnShip = true;
					}
					updateGamePoints(OWNHITPOINTS);
					if(ownShipMover != null && ownShipMover.isAlive() == true) {
						ownShipMover.quit();
		                try {
		                	ownShipMover.join();
		                } catch(InterruptedException exc) {
		                    // This thread was interrupted, do nothing.
		                }
		            }	
					ownShipDestroy = new ShipDestroy(this, manager, ownShip, Constant.NOTALIENSHIP, ownShip.getFrame(), 
							ownShip.getFrameSequenceLength());
					ownShipDestroy.start();
					playOwnHit();				
				}
				/* check move control */
				if (midlet.isMovementUpdated()) {
					if(ownShipMover != null && ownShipMover.isAlive() == true) {
						ownShipMover.quit();
		                try {
		                	ownShipMover.join();
		                } catch(InterruptedException exc) {
		                    // This thread was interrupted, do nothing.
		                }
		            }				
					speed = midlet.MovementSpeed();
					dir = midlet.MovementDir();
					ownShipMover = new OwnShipMover(skillLevel, ownShip, screenW, speed, dir);
					ownShipMover.start();
				}
				/* check tap */
				if (midlet.istapUpdated()) {
					if (!getOwnShipHit() && !newOwnShip) {
						if (ownAmmoLeft > 0) {
							ownAmmo = new OwnAmmo(manager, skillLevel, ownShip.getRefPixelX(), ownShip.getRefPixelY());
							ownAmmo.start();	
							ownAmmoLeft--;
							updateGamePoints(OWNAMMOPOINTS);
						}
					}
				}				
			}
			/* check long press */
			if (midlet.isLongPressUpdated()) {
				pauseAlienShips();
				displayMenu(null, false);
			}
			/* check own ship status */
			if (!getOwnShipHit()) {
				if (newOwnShip) {
					xOwnShip = screenW/2;;
			    	yOwnShip = getHeight()-50;
			    	ownShip = makeOwnShip(xOwnShip,yOwnShip);
			    	newOwnShip = false;
				}
			}
			/* check alien ships */
			if (alienShipsLeft > 0) {
				createAlienShips();
			}
			/* update display */
			drawDisplay(g, null);
			
			/* delay */
			try {
				Thread.sleep(20-skillLevel);
			} catch(InterruptedException ex) {
				return;
			}		
		}
	}

	private void createAlienShips() {
		int i, shipCount = 0;
		
	   	for (i=0;i < Constant.MAXALIENSHIPS;i++) {
	   		if(getAlienShip(i) != null) {
	   			shipCount++;
	   		}
    	}
	   	if (shipCount >= Constant.MAXALIENSHIPS) {
	   		/* do nothing */
	   	} else if (shipCount < INITIALALIENSHIPS + skillLevel/2) {
	   		/* create now only one ship more */
	   		for (i=0;i < Constant.MAXALIENSHIPS;i++) {
	   			if (getAlienShip(i) == null) {
	   				/* unused, free slot */
	   				makeAlienShip(i, i*20, i*21+40);
	   				break;	 
	   			}
	    	}		   		
	   	}			
	}

	private void pauseAlienShips() {
		int i;
		
	   	for (i=0;i < Constant.MAXALIENSHIPS;i++) {
	   		if((alienshipMover[i] != null) && (alienshipMover[i].isAlive())) {
	  			alienshipMover[i].pause();
	   		}
    	}		
	}
	
	private void resumeAlienShips() {
		int i;
		
	   	for (i=0;i < Constant.MAXALIENSHIPS;i++) {
	   		if((alienshipMover[i] != null) && (alienshipMover[i].isAlive())) {
	  			alienshipMover[i].resume();
	   		}
    	}		
	}
	
	private boolean checkOwnAmmoAlive() {
		int i;
		boolean retVal = false;
		int hitAmmoW = 0, hitAmmoH = 0;
		
		for(i = 0; i < (manager.getSize()-2); i++) { /* last is bg, shelter */
			/* check own ammo */
			hitAmmoW = manager.getLayerAt(i).getWidth();
			hitAmmoH = manager.getLayerAt(i).getHeight();			
			if ((hitAmmoW == OWNAMMOW) && (hitAmmoH == OWNAMMOH)) {
				/* own ammo */
				retVal = true;
				break;
			}
		}
		return retVal;
	}
	
	private void checkAlienShipCollision() {
		int loops, innerLoop;
		ShipDestroy shipDestroy = null;
		int hitAmmoW = 0, hitAmmoH = 0;
		
		for(loops = 0; loops < Constant.MAXALIENSHIPS; loops++) {
			if (alienshipMover[loops] != null && alienshipMover[loops].isAlive() == true) {
				for(innerLoop = 0; innerLoop < (manager.getSize()-2); innerLoop++) { /* last is bg, shelter */
					if(getAlienShip(loops) != null && getAlienShip(loops)==(Sprite)manager.getLayerAt(innerLoop)){
						/* alien hits alien, do nothing */
					} else if(getAlienShip(loops) != null && getAlienShip(loops).collidesWith((Sprite)manager.getLayerAt(innerLoop), true)) {	
						/* check alien ammo */
						hitAmmoW = manager.getLayerAt(innerLoop).getWidth();
						hitAmmoH = manager.getLayerAt(innerLoop).getHeight();
						
						if ((hitAmmoW == ALIENAMMOW) && (hitAmmoH == ALIENAMMOH)) {
							/* alien ammo */
						} else if (getAlienShip(loops) != null && getAlienShip(loops).getFrame() > 0) {
							/* already hit, do nothing */	
						} else if ((hitAmmoW == OWNAMMOW) && (hitAmmoH == OWNAMMOH)) {
							manager.remove(manager.getLayerAt(innerLoop)); /* own ammo */
							shipDestroy = new ShipDestroy(this, manager, getAlienShip(loops), 
									loops, getAlienShip(loops).getFrame(), getAlienShip(loops).getFrameSequenceLength());
							shipDestroy.start();
					  		updateGamePoints(ALIENHITPOINTS);
					  		alienShipsLeft--;
					  		skillLevel = MAXSKILLLEVEL-alienShipsLeft/10;
					  		if(alienshipMover[loops] != null && alienshipMover[loops].isAlive() == true) {
					  			alienshipMover[loops].quit();
					  			try {
					                alienshipMover[loops].join();
					  			} catch(InterruptedException exc) {
					                    // This thread was interrupted, do nothing.
					            }
					  			alienshipMover[loops] = null;
					  		}
					  		playAlienHit();
					  		return; /* only one detection for round */
						}
					}
				}
			}
		}
	}
						
	private boolean checkOwnShipCollision() {
		boolean retVal = false;
		int hitAmmoW, hitAmmoH;
		
		for(int i = 0; i < (manager.getSize()-2); i++) { /* last is bg, shelter*/
			if(ownShip==(Sprite)manager.getLayerAt(i)){
				/* own ship */
			}
			else if(ownShip.collidesWith((Sprite)manager.getLayerAt(i), false)) {
				if(ownShip.collidesWith((Sprite)manager.getLayerAt(i), true)) {
					/* check alien ammo */
					hitAmmoW = manager.getLayerAt(i).getWidth();
					hitAmmoH = manager.getLayerAt(i).getHeight();					
					if ((hitAmmoW == ALIENAMMOW) && (hitAmmoH == ALIENAMMOH)) {
						/* alien ammo */
						manager.remove(manager.getLayerAt(i));
						retVal = true;
						break; /* indexes are changed */
					}
				}
			}			  	
		}
		return retVal;
	}
	
	private void checkShelterCollision() {
		int lastFrame = shelterSprite.getFrameSequenceLength()-1;
		int hitAmmoW, hitAmmoH;
		Sprite sprite = null;
		int layerCount;
		
		layerCount = (manager.getSize()-2); /* last is bg, 2nd last shelter */
		if (layerCount < 1) {
			return; /* fail */
		}
		for(int i = 0; i < layerCount; i++) {			
			try	{
				sprite = (Sprite)manager.getLayerAt(i);
			}
			catch (IndexOutOfBoundsException ex){
				break;
			}
			
			if (shelterSprite.collidesWith(sprite, false)) {
				if (shelterSprite.collidesWith(sprite, true)) {
					hitAmmoW = sprite.getWidth();
					hitAmmoH = sprite.getHeight();						
					if ((hitAmmoW == OWNAMMOW) && (hitAmmoH == OWNAMMOH)) {
						/* own ammo */
						manager.remove(sprite);
					}
					else if ((hitAmmoW == ALIENAMMOW) && (hitAmmoH == ALIENAMMOH)) {
						if (shelterSprite.getFrame() == lastFrame ) {
							/* last visible frame, do nothing */
						} else {
							shelterHits++;
							if (shelterHits < MAXSHELTERHITS ) {
								/* not yet */
							} else {
								shelterSprite.nextFrame();
								shelterHits = 0;
							}
						}
						manager.remove(sprite);
						playShelterHit();
					} else {
						/* what is this */
					}
					break;
				}							
			}
		}
	}
	
	private void drawDisplay(Graphics g, Image menuImage)
	{
		manager.paint(g, 0, 0);
		
		if (menuImage != null) {
			g.drawImage(menuImage, MENUX, MENUY, 0);
		}
				
        g.setColor(0x0000ff); /* blue, r g, b */
        
        g.drawString(""+ownShipsLeft,30, 0, Graphics.TOP | Graphics.LEFT);
        g.drawString(""+ownAmmoLeft,80, 0, Graphics.TOP | Graphics.LEFT);
        g.drawString(""+alienShipsLeft,140, 0, Graphics.TOP | Graphics.LEFT);
        g.drawString(""+gamePoints,190, 0, Graphics.TOP | Graphics.LEFT);
                          
	    flushGraphics();
	  }

	private void drawEndDisplay(Graphics g, Image menuImage, boolean newHiScore)
	{
		String gameStatus = "";
		    
		manager.paint(g, 0, 0);
		
		if (menuImage != null) {
			g.drawImage(menuImage, MENUX, MENUY, 0);
		}
				
        g.setColor(0x0000ff); /* blue, r g, b */

        g.drawString(""+ownShipsLeft,30, 0, Graphics.TOP | Graphics.LEFT);
        g.drawString(""+ownAmmoLeft,80, 0, Graphics.TOP | Graphics.LEFT);
        g.drawString(""+alienShipsLeft,140, 0, Graphics.TOP | Graphics.LEFT);
        g.drawString(""+gamePoints,190, 0, Graphics.TOP | Graphics.LEFT);
                   
        if (alienShipsLeft == 0) {
        	if (newHiScore) {
        		gameStatus = "You won, new HiScore";
        	} else {
        		gameStatus = "You won";
        	}      	
        } else {
        	if (newHiScore) {
        		gameStatus = "You lost, new HiScore";
        	} else {
        		gameStatus = "You lost";
        	} 
        }
        	
        g.drawString(gameStatus, MENUX+10, MENUY+60, Graphics.TOP | Graphics.LEFT);
        g.drawString("Score "+gamePoints, MENUX+10, MENUY+80, Graphics.TOP | Graphics.LEFT);
        g.drawString("Use long press to Exit", MENUX+10, MENUY+100, Graphics.TOP | Graphics.LEFT);
    
	    flushGraphics();
	  }
	
	public void start() {
	    Thread t = new Thread(this);
	    t.start();
	}

	private void playAlienHit() {			
		try {
			alienHitPlayer.start();
		} catch (MediaException e) {}
	}
	
	private void playOwnHit() {	
		try {
			ownHitPlayer.start();
		} catch (MediaException e) {}
	} 

	private void playShelterHit() {
		try {
			shelterHitPlayer.start();
		} catch (MediaException e) {
		}
	}		
			
	private TiledLayer createBackground() {

		Image backgroundImg = null;
		TiledLayer tiledLayer;
		int screenW = getWidth();
		int screenH = getHeight();

		byte mapData_1[][] = {
				{5, 6, 8, 7, 8, 8},
				{4, 4, 4, 4, 4, 4},
				{4, 4, 4, 4, 4, 4},
				{4, 4, 4, 4, 4, 4},
				{4, 4, 4, 4, 4, 4},
				{4, 4, 4, 4, 4, 4},
				{3, 3, 3, 3, 3, 3},
				{1, 2, 2, 1, 1, 2}
		};
		
		byte mapData_2[][] = {				
				{5, 6, 8, 7, 8, 8},
				{4, 4, 4, 4, 4, 4},
				{4, 4, 4, 4, 4, 4},
				{4, 4, 4, 4, 4, 4},
				{4, 4, 4, 4, 4, 4},
				{4, 4, 4, 4, 4, 4},
				{4, 4, 4, 4, 4, 4},
				{4, 4, 4, 4, 4, 4},
				{3, 3, 3, 3, 3, 3},
				{1, 2, 2, 1, 1, 2}
		};
		
		try {
			backgroundImg = Image.createImage("/tiledall.png");
		} catch (IOException e) {
			return null;
		}

		if ((screenW == 240) && (screenH == 320)) {
			tiledLayer = new TiledLayer(6, 8, backgroundImg, 40, 40);
			int rows = mapData_1.length;
			int columns = mapData_1[0].length;
			
			for(int y = 0;  y < rows; y++) {
				for(int x = 0; x < columns; x++) {
					int tileIndex = mapData_1[y][x];
					if(tileIndex > 0)
						tiledLayer.setCell(x, y, tileIndex);
				}
			}
		} else if ((screenW == 240) && (screenH == 400)) {
			tiledLayer = new TiledLayer(6, 10, backgroundImg, 40, 40);
			int rows = mapData_2.length;
			int columns = mapData_2[0].length;
			
			for(int y = 0;  y < rows; y++) {
				for(int x = 0; x < columns; x++) {
					int tileIndex = mapData_2[y][x];
					if(tileIndex > 0)
						tiledLayer.setCell(x, y, tileIndex);
				}
			}
		} else {
			return null;
		}
		tiledLayer.setPosition(0, 0);
		mapData_1 = null;
		mapData_2 = null;
		return tiledLayer;
	}

	private void drawDisplayIntro(Graphics g, Image image, Image menuImage)
	{       
		if (image != null) {
			g.drawImage(image, 0, 0, 0);
		}
 
		if (menuImage != null) {
			g.drawImage(menuImage, MENUX, MENUY, 0);
		}
		
        g.setColor(0x000000); /* black, r g, b */
        g.drawString("tap screen to start Game", 5, 5, Graphics.TOP | Graphics.LEFT);
        g.drawString("Use long press to get Menu", 5, 25, Graphics.TOP | Graphics.LEFT);
        g.drawString("HiScore: "+midlet.getHiScoreRS(), 5, 45, Graphics.TOP | Graphics.LEFT);
        
        flushGraphics();
	}
	
	private void showIntro() {
		int screenW = getWidth();
		int screenH = getHeight();
		Image backgroundImg;
		boolean introIsQuit = false;
		
		Graphics g = getGraphics();

		try {
			midiPlayer = Manager.createPlayer(getClass().getResourceAsStream("/intro.mid"), "audio/midi");
			midiPlayer.addPlayerListener(midlet);
			midiPlayer.setLoopCount(-1);
			midiPlayer.start();
		} catch (Exception e) {
			/* just continue, fails if silent mode */
		}
		
		if ((screenW == 240) && (screenH == 320)) {
			try {
				backgroundImg = Image.createImage("/intro240x320.png");
			} catch (IOException e) {
				return;
			}			
		} else if ((screenW == 240) && (screenH == 400)) {
			try {
				backgroundImg = Image.createImage("/intro240x400.png");
			} catch (IOException e) {
				return;
			}				
		} else {
			backgroundImg = null;
		}
	
		while(introIsQuit == false) {			
			if (midlet.istapUpdated()) {		
				midiPlayer.removePlayerListener(midlet);
				midiPlayer.close();
				midiPlayer = null;
				introIsQuit = true;
			} else if (midlet.isLongPressUpdated()) {				
				try {
					midiPlayer.stop();
					playerStoppedByUser = true;
				} catch (MediaException e) {
					/* do nothing */
				}
				displayMenu(backgroundImg, true);
			}
			drawDisplayIntro(g, backgroundImg, null);			
			try {
				Thread.sleep(40);
			} catch(InterruptedException ex) {
		            return;
			}				
		}
	}

	private void gameOver() {
		Image backgroundImg = null;
		boolean gameOverIsQuit = false;
		boolean newHiScore = false;
		
		Graphics g = getGraphics();
		
		try {
			backgroundImg = Image.createImage("/gameover.png");
		} catch (IOException e) {
			/* do nothing */
		}
		
		if (midlet.getHiScoreRS() < gamePoints ) {
			midlet.updateHiScoreRS(gamePoints);
			newHiScore = true;
		}

		/* wait to complete sounds and animations */
		try {
			Thread.sleep(250);
		} catch(InterruptedException ex) {
	            return;
		}
		
		/* clean all players */
		if (alienHitPlayer != null) {
			alienHitPlayer.deallocate();
		}
		if (ownHitPlayer != null) {
			ownHitPlayer.deallocate();
		}
		if (shelterHitPlayer != null) {
			shelterHitPlayer.deallocate();
		}		
		if (midiPlayer != null) {
			midiPlayer.deallocate();
		}
		
		try {
			midiPlayer = Manager.createPlayer(getClass().getResourceAsStream("/gameover.mid"), "audio/midi");
			midiPlayer.addPlayerListener(midlet);
			midiPlayer.setLoopCount(-1);
			midiPlayer.start();
		} catch (Exception e) {
			/* just continue, fails if silent mode */
		}
			
		while(gameOverIsQuit == false) {
			if (midlet.isLongPressUpdated()) {	
				midiPlayer.removePlayerListener(midlet);
				midiPlayer.close();
				midiPlayer = null;
				/* exit */
				midlet.destroyApp(false);
			}
			
			drawEndDisplay(g, backgroundImg, newHiScore);			
			try {
				Thread.sleep(20);
			} catch(InterruptedException ex) {
		            return;
			}				
		}
	}
	
	private void displayMenu(Image backgroundImg, boolean intro) {
		Image menuImage;
		boolean menuIsQuit = false;
		boolean subMenuOn = false;
		int x,y;
		
		Graphics g = getGraphics();
		
		try {
			menuImage = Image.createImage("/menu.png");
		} catch (IOException e) {
			return;
		}

		while(menuIsQuit == false) {			
			if (midlet.istapUpdated()) {
				x = midlet.tapX();
				y = midlet.tapY();				
				if (subMenuOn){
					subMenuOn = false;
					try {
						menuImage = Image.createImage("/menu.png");
					} catch (IOException e) {
						return;
					}					
				} else {
					if ((x >= MENUX) && (x <= MENUX+MENUXWIDTH)) {
						if ((y >= MENUY) && (y <= MENUY+MENUYSLOTHEIGTH)) {
							/* return */
							if (intro==false) {
								resumeAlienShips();								
							}
							menuImage = null;
							menuIsQuit = true;
							if (playerStoppedByUser) {
								playerStoppedByUser = false;
								try {
									midiPlayer.start();
								} catch (MediaException e) {
									/* do nothing */
								}
							}							
						}
						if ((y >= MENUY+MENUYSLOTHEIGTH) && (y <= MENUY+MENUYSLOTHEIGTH*2)) {
							/* help */
							try {
								menuImage = Image.createImage("/help.png");
							} catch (IOException e) {
								return;
							}
							subMenuOn = true;						
						}
						if ((y >= MENUY+MENUYSLOTHEIGTH*2) && (y <= MENUY+MENUYSLOTHEIGTH*3)) {
							/* info */
							try {
								menuImage = Image.createImage("/info.png");
							} catch (IOException e) {
								return;
							}
							subMenuOn = true;
						
						}
						if ((y >= MENUY+MENUYSLOTHEIGTH*3) && (y <= MENUY+MENUYSLOTHEIGTH*4)) {
							/* exit */
							midlet.destroyApp(false);
						}
					}	
				}				
			} /* midlet.istapUpdated() */
			if (intro) {
				drawDisplayIntro(g, backgroundImg, menuImage);
			} else {
				drawDisplay(g, menuImage);
			}				
			try {
				Thread.sleep(40);
			} catch(InterruptedException ex) {
				return;
			}	
		} /* while */
	}	
} /* end of GestureCanvas */
