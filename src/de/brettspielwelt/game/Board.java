package de.brettspielwelt.game;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Vector;

import de.Data;
import de.Vect;
import de.brettspielwelt.tools.IntVector;


public class Board extends ElementBoard
{
	Graphics2D backG;

	private int oldSize;	//,spacing,pix2;
	private Font fontDefault,threeFont,fourFont;
	private Font fontLarge;	//,bigFont,fontSmaller;
	private boolean scaling;
	private String[] localStrings,kartenTexte;
	int[] score=new int[0];
	int[] platz=new int[0];
	int[] auslage=new int[5];
	
	private String[] spielerName={"","","","",""};
	boolean iAmPlaying = false;
	int anzSpieler = 0;
	int iAmId = -1;
	
	boolean hideCards = false;
	
	
	int currentPlayer=-1,startSpieler=-1,subRound=0;
	int phase=0,round=0,cont=0;
	
	Image[] baseImg,baseOrgImg;
	private Image[] wOrgImg=new Image[WIMG_ANZ];
	private Image[] wImg=new Image[WIMG_ANZ];
	static final int WIMG_ANZ=(6)*3;
	
	
	int[][] würfelStapel=new int[3][9]; 
	int[][][] spielerBoards = new int[4][3][6];
	int[] spielerPlättchenCount = new int[4];
	int verdecktPlättchenStapel=0;
	IntVector spielerPlättchenStapel=new IntVector(); 
	
	
	int[] info=new int[5];

	@Override
	public void init(int lev, Runnable run) {
		if(lev==0){
			localInitH();
			initPics();
		}
		if(lev==1){
			float scale=1;
			baseImg=new Image[baseOrgImg.length];
			for(int i=0; i<baseOrgImg.length; i++){
				baseImg[i]=getScaledInstance(baseOrgImg[i], 1);
			}
			for(int i=0; i<WIMG_ANZ; i++){
				wImg[i]=wOrgImg[i]; //getScaledInstance(wOrgImg[i][j], scale);
			}
			
			fourFont = getFontLocal("fourFont", scale);
			threeFont = getFontLocal("threeFont", scale);
			fontDefault = getFontLocal("defaultFont", scale);
			fontLarge = getFontLocal("largeFont", scale);
		}
	}
	
	static final int N_CARDS=10,REST=20,PHASEN=31,BUTTONS=34,PORTAL=37;
	
	public void initPics(){
		String[] imageNames = {
				"playerBoard.png"
		};
		
		baseOrgImg = new Image[imageNames.length];
		for(int i=0; i<imageNames.length; i++){
			baseOrgImg[i] = getImageLocal(imageNames[i]);
		}
		
		registerFont("BradBunR.ttf");
		int[] wp={0,15,22,7,31,39};
		String prefix = "gruen";
		for(int co=0; co<WIMG_ANZ; co++){
			if (co ==6) {
				prefix = "gelb";
			}else if(co == 12) {
				prefix = "blau";
			}
				
			wOrgImg[co]=getImageLocal(prefix+"."+(wp[co%6]+1)+".png");
		}
		
		oldSize = 0;
	}
	
	
	public synchronized void scalePics(){
		
		if(true || oldSize==getWidth())return;
		System.err.println("Scaling");
		double scale = ((double)getWidth()) / ((double)1220);
		
		
		baseImg=new Image[baseOrgImg.length];
		for(int i=0; i<baseOrgImg.length; i++){
			baseImg[i]=getScaledInstance(baseOrgImg[i], scale);
		}
		for(int i=0; i<WIMG_ANZ; i++){
			wImg[i]=wOrgImg[i]; //getScaledInstance(wOrgImg[i][j], scale);
		}
		
		// Scaled fonts...
		fourFont = getFontLocal("fourFont", scale);
		threeFont = getFontLocal("threeFont", scale);
		fontDefault = getFontLocal("defaultFont", scale);
		fontLarge = getFontLocal("largeFont", scale);
		
		oldSize=getWidth();
		scaling=false;
	}
	
	public int co(int co) {
		return co; //*getWidth()/1220;
	}
	public int rco(int co) {
		return co*1220/getWidth();
	}
	
	protected boolean localInitH(){
		localStrings = getStringsLocal("t", 1);
		
		// Scaled fonts...
		double scale = ((double)getWidth()) / ((double)1220);
		
		fourFont = getFontLocal("fourFont", scale);
		threeFont = getFontLocal("threeFont", scale);
		fontDefault = getFontLocal("defaultFont", scale);
		fontLarge = getFontLocal("largeFont", scale);
		
		initPics();
		
		return getSoundPack(null, new String[] {
		}); // 14
	}
	
	public void drawDice(Graphics2D g, int ca) {
		
	}
	
	public void drawNumberCard(Graphics2D g, int ca) {
		boolean austausch=(ca&64)==64;
		if(austausch) ca&=15;
		g.setColor(new Color(0x17495c));
		g.fillRoundRect(-105, -160, 210, 320, 20, 20);
		if(ca==0)
			g.drawImage(baseImg[N_CARDS+9], -100, -155, null);
		else g.drawImage(baseImg[ca+N_CARDS-1], -100, -155, null);
		if(austausch)
			g.drawImage(baseImg[REST+7],-100,-155,null);
	}
	public void drawC(Graphics2D g, int ca, int x, int y, int w) {
		save(g);
		g.translate(x, y);
		g.scale(w/210.0, w/210.0);
		drawNumberCard(g, ca);
		restore(g);
	}
	
	public void importPlayerBoards(int[][] inputBoard){
		int[][] ret = new int[4][18];
		
		for (int i = 0; i < inputBoard.length; i++) {
			for (int j = 0; j < inputBoard[i].length; j++) {
					spielerBoards[i][j/6][j%6] = inputBoard[i][j];				
			}
		}
		printBoards();
	}
	
	public void importPlättchenCount(int[] input) {
		verdecktPlättchenStapel =input[0];
		for (int i = 0; i < input.length-1; i++) {
			spielerPlättchenCount[i] = input[i+1];
		}
	}
	public void printBoards() {
        for (int i = 0; i < spielerBoards.length; i++) {
            System.out.println("Player " + (i + 1) + ":");
            for (int j = 0; j < spielerBoards[i].length; j++) {
                System.out.print("Row " + (j + 1) + ": ");
                for (int k = 0; k < spielerBoards[i][j].length; k++) {
                    System.out.print(spielerBoards[i][j][k] + " ");
                }
                System.out.println();
            }
            System.out.println();
        }
	}
	
	
	
	public void getBoard(Vect v){
		if(v.size()>3) {
			int c=0;
			int aPhase=phase;
			int aPl=currentPlayer;
			
			anzSpieler=((Integer)v.elementAt(c++)).intValue();
			iAmId=((Integer)v.elementAt(c++)).intValue();
			phase=((Integer)v.elementAt(c++)).intValue();
			round=((Integer)v.elementAt(c++)).intValue();
			currentPlayer=((Integer)v.elementAt(c++)).intValue();
			startSpieler=((Integer)v.elementAt(c++)).intValue();
			spielerPlättchenStapel = (IntVector) v.elementAt(c++);
			importPlayerBoards((int[][]) v.elementAt(c++));
			importPlättchenCount((int[]) v.elementAt(c++));
			würfelStapel = (int[][]) v.elementAt(c++);
			auslage = (int[]) v.elementAt(c++);
			info=(int[])v.elementAt(c++);
			
			
			int[] anim=((int[])v.elementAt(c++));
			handleAnim(anim);
			
			iAmPlaying=(currentPlayer==iAmId);
			
//			if(currentPlayer!=aPl && iAmPlaying)
//				playSound(4);
			
			repaint();
		}else {
			if(v.size()==1){
				int[] anim=((int[])v.elementAt(0));
				handleAnim(anim);
			}
		}
	}
	
	private void handleAnim(int[] anim) {
		if(anim==null || anim.length==0) return;
		animationArr=anim;
		animation=1;
	}
	
	public void run() {
		super.run();
	}
	
	public void getNioData(int typ, Data dat){
		int c=0;
		
		switch(typ){
		case 700:
			history.addElement(dat.v);
			break;
		case 701:
			history.addElement(dat.v);
			break;
			
		case 702:
			score=(int[])dat.v.elementAt(c++);
			platz=(int[])dat.v.elementAt(c++);
			for(int i=0; i<5; i++)
				spielerName[i]=(String)dat.v.elementAt(c++);
			repaint();
			break;
			
		case 703:
			history.addElement(dat.v);
			break;
		}
	}
	
	public synchronized void sendAction(int power, int act){
		//System.err.println("Sending Action: "+act);
		Data dat=makeData(700);
		dat.v.addElement(new Integer((power<<28)|act));
		sendDataObject(dat);
	}
	public synchronized void sendAction(int power, int act, IntVector marks){
		//System.err.println("Sending Action: "+act);0�
		Data dat=makeData(700);
		dat.v.addElement(new Integer((power<<27)|act));
		dat.v.addElement(marks);
		sendDataObject(dat);
	}
	

	
	
	// --------------------------  Mouse INteraction Stuff  --------------------------
	
	int mouseMoveX=0, mouseMoveY=0;
	int mouseDownX=0, mouseDownY=0;
	int auslagePick=-1;
	IntVector possibilities=new IntVector();
	int possibleSel=-1,possiblePortal=-1,possibleCommit=-1;
	int gesammeltPick=-1,grabHand=-1;
	int portalPick=-1;
	
	int pickedChooser=-1, chooserShow=3;
	int chooserX=0, chooserY=0;
	double chooserScale=1;
	IntVector chooser=new IntVector();
	
	int selCenterX, selCenterY;
	
	Rectangle[] buttons=new Rectangle[8];
	
	@Override
	public void mouseMoved(MouseEvent ev) {
		int x = ev.getX();
		int y = ev.getY();
		mouseMoveX=x; mouseMoveY=y;
		ev.consume();
	}
	
	public void mouseReleased(MouseEvent ev) {
		int x = rco(ev.getX());
		int y = rco(ev.getY());
		
		if(y<200 && auslagePick==2 && x>900) { // austausch alle 4 
			sendAction(8,1);
		}
		if(portalPick>-1 && phase==12 && mouseMoveX<850) {
			sendAction(7,portalPick); // steal/destroy
		}
		if(grabHand>-1 && phase==3) {
			sendAction(6,grabHand);
		}
		if(pickedChooser>-1 && mouseMoveY>600) {
			sendAction(5,pickedChooser);
		}
		if(auslagePick>-1 && mouseMoveY>300) {
			int target=3; // undefined target
			if(hitLocObject(x, y, 20+iAmId, 0, DICE<<16|0)) target=0;
			if(hitLocObject(x, y, 20+iAmId, 1, DICE<<16|0)) target=1;
			sendAction(1,auslagePick|target<<3);
		}
		if(possibleSel>-1) {
		//	sendAction(2+possiblePortal,possibilities.elementAt(possibleSel));
			possibleCommit=possibleSel;
		}
		if(hideCards) {
			hideCards = false;
		}
		//possibilities=new IntVector();
		// possibleSel=-1;
		auslagePick=-1;
		gesammeltPick=-1;
		pickedChooser=-1;
		grabHand=-1;
		portalPick=-1;
		repaint();
	}
	
	public void mousePressed(MouseEvent ev) {
		int x = rco(ev.getX());
		int y = rco(ev.getY());
		
		System.err.print(x+","+y+", ");

		mouseDownX=x; mouseDownY=y;
		mouseMoveX=x; mouseMoveY=y;
		if(phase==11 && chooser.size()==0) {
			for(int i=0; i<anzSpieler; i++) {
				if(buttons[i]!=null && hitLocObject(x, y, 20+i, 0, BUTTON<<16|1)) {
					sendAction(5,i);
				}
			}
		}

		if(phase==1 ) {
			
			//Würfeln
			for (int i = 0; i < 3; i++) {
				
				 if(new Rectangle( 580+ 220*i, 400 , 3 * 65,  3 * 65).contains(x,y))
						sendAction(1,i);
			}
			
			//Werten
			if(new Rectangle(395, 620, 200, 60).contains(x,y))
				sendAction(2,0);
			
		}
		
		if(phase==2) {
			for(int i=1; i<auslage.length; i++) {
				int imageIndex = auslage[0]*6+auslage[i];
				if(hitLocObject(x, y, 4, i-1, DICE<<16|imageIndex)) {
					System.out.println("hit auslage spot: "+i+ " with Wert: "+auslage[i]);
					sendAction(3, i);
				}
			}
			return;
		}
		
		for(int i=0; i<anzSpieler; i++) {
			if(i!=iAmId) {
//				for(int j=0; j<2; j++)
//					if(portale[i][j]>0 && hitLocObject(x, y, 20+i, j, HEROES<<16|0)) {
//						System.err.println("Hit on other "+i+","+j);
//						portalPick=i<<4|j;
//					}
			}
		}
//		for(int i=gesammelt[iAmId].size()-1; i>=0;  i--)
//			if(hitLocObject(x, y, 30+iAmId, i, HEROES<<16|0)) {
//				gesammeltPick=i;
//				break;
//			}

		// Auslage
//		for(int i=0; i<6; i++)
//			if(hitLocObject(x, y, 1, i, HEROES<<16|0))
//				auslagePick=i;
		if(hitLocObject(x, y, 2, 0, DICE<<16|0))
			auslagePick=6;
		if(hitLocObject(x, y, 2, 1, DICE<<16|0))
			auslagePick=7;
		
		possibleSel=-1;
//		for(int i=0; i<2; i++)
//			if(hitLocObject(x, y, 20+iAmId, i, HEROES<<16|0))
//				if(portale[iAmId][i]!=0) {
//					possiblePortal=i;
//					possibleSel=0;
//					//System.err.println(" "+res);
//				}

		System.err.println("\nHIT:"+hitLocObject(x, y, 1, 2, DICE<<16|0));
		repaint();
	}
	
	@Override
	public void mouseDragged(MouseEvent ev){
		int x = rco(ev.getX());
		int y = rco(ev.getY());
		mouseMoveX=x; mouseMoveY=y;

		if(possibilities.size()>0) {
			if(getDistance(new Point(x,y), mouseDownX,mouseDownY+20)>250*250) {
				possibleSel=-1;
				possibilities=new IntVector();
				possibleCommit=-1;
			}
		}
		
//		for(int i=gesammelt[iAmId].size()-1; i>=0;  i--)
//			if(hitLocObject(x, y, 30+iAmId, i, HEROES<<16|0)) {
//				gesammeltPick=i;
//				break;
//			}

		repaint();
	}
	
	public boolean immediateDrag(){
		return true;
	}
	
	public int getDistance(Point p, int x, int y){
		int vx=x-co(p.x), vy=y-co(p.y);
		return vx*vx+vy*vy;
	}
	
	
	// Animation part ---------------- Copy here ----------------------
	static int DICE=0, PLAYERBOARD=1, BUTTON=2;

	public Place getPlace(int a, int b) {
		if(a>=1 && a <=3) { // Würfel Stapel
			
			 	int rowIndex = b / 3;
	            int colIndex = b % 3;
			
			return new Place(605+ 220*(a-1)+ colIndex * 65,420 + rowIndex * 65,0.5);
		}
		if(a==4) { // Auslage
				return new Place(735+110*b,690, 1);
		}
		if(a==5) { // Würfel auf Playerboards
			int[] boardOffset= {197,631, 40,45, 592,153, 986,153};
			int playerBoard = (b>>8)&255;
			int diceInfo = b&255;
			int rowIndex = diceInfo / 6;
            int colIndex = diceInfo % 6;
			int offsetX=-157, offsetY=-103;
			Place pp =new Place(offsetX+colIndex*60,offsetY+rowIndex*70, 0.6).setOnPlace(getPlace(10, playerBoard));
			return pp;
		}
		if (a==10) { //Playerboards 395x306 px  /2 = 197x153  1220, 784
			int[] auslagePos= {197,631, 197,153, 592,153, 986,153};
			return new Place(auslagePos[b*2],auslagePos[b*2+1], 1);
			
		}

		return null;
	}
	public Rectangle drawObj(Graphics2D g, int obj, int ti) {
		int objKind=(obj>>16)&255;
		int objTyp=obj&255;
		if(objKind==DICE) {  // Card
			if(g!=null) {				
				int imageIndex = objTyp-1;
				g.drawImage(wImg[imageIndex],-50,-50,100,100, null);
				
			}				
			return new Rectangle(-50,-50,100,100);
		}
		if(objKind==PLAYERBOARD) {  // Card
			if(g!=null) g.drawImage(baseImg[0], -197,-153,null);
			return new Rectangle(-197,-153,395,306);
		}
		if(objKind==BUTTON) { // Button 395x306 px  /2 = 197x153  1220, 784
			if(g!=null) g.drawImage(baseImg[BUTTONS+objTyp], -197,-153,null);
			return new Rectangle(-197,-153,395,306);
		}
		return null;
	}
	
	
	// -------------------------------------------------
	
	public void paintp(Graphics g) {
		try {
			if(scaling || oldSize!=getWidth()){
				Graphics gl=getOffScreenGraphics();
				gl.setColor(Color.black);
				gl.fillRect(0,0,getWidth(),getHeight());
				gl.setColor(Color.white);
				FontMetrics fm=gl.getFontMetrics(fontDefault);
				gl.drawString(localStrings[0],getWidth()/2-fm.stringWidth(localStrings[0]),getHeight()/2);	// "Standby..." ("Scaling Pics")
				if(oldSize==getWidth()) return;
			}
			scaling=false;
			later=new ArrayList<Place>();
			int iId=iAmId>-1?iAmId:0;
			backG=(Graphics2D)getOffScreenGraphics();
			backG.setColor(Color.black);
			backG.fillRect(0, 0, 1220, 784);
			if (phase>=1) {
			
			//Drawing Board Start
			int iAmIdentifier = 0;
			for (int i = 0; i < anzSpieler; i++) {
				drawLocObject(backG, 10, i, PLAYERBOARD<<16|0);
			}
			for (int i = 0; i < anzSpieler; i++) {
				if (i == iAmId) {
					iAmIdentifier++;
				}
				
				
				for (int j = 0; j < spielerBoards[i].length; j++) {
					for (int k = 0; k < spielerBoards[i][j].length; k++) {
						if(spielerBoards[i][j][k] > 0) {
							if (i == iAmId) {
								drawLocObject(backG, 5, 0<<8|j*6+k, DICE<<16|j*6+spielerBoards[i][j][k]);
							} else {
								drawLocObject(backG, 5, (i-iAmIdentifier+1)<<8|j*6+k, DICE<<16|j*6+spielerBoards[i][j][k]);
							}
						}
					}
				}
			}
			for (int i = 0; i < würfelStapel.length; i++) {
		        for (int j = 0; j < würfelStapel[i].length; j++) {

		            if (würfelStapel[i][j] != -1) {
		            	drawLocObject(backG, i+1, j, DICE<<16|i*6+würfelStapel[i][j]);
		            }
		        }
		    }
			if(phase==2)
			for (int i = 1; i < auslage.length; i++) {
				int imageIndex = auslage[0]*6+auslage[i];
				if (auslage[i] != -1) {
					drawLocObject(backG, 4, i-1, DICE<<16|imageIndex);
	            }
			}
		}
		//Drawing Board End
		if(phase == 1) {
			backG.setColor(new Color(0xeabd09));
			backG.fillRoundRect(395, 620, 200, 60, 20, 20);
			backG.setFont(fontLarge);
			drawSizedBorderdString(backG, new Color(0xbd09ea), new Color(0x09eabd), "Werten", 395+100, 620+50, 600, true );
		}
		}catch(Exception ex) {ex.printStackTrace();}
	}
	
}
