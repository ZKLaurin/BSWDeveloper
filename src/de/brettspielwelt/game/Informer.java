package de.brettspielwelt.game;

import java.io.Serializable;

import de.Data;
import de.brettspielwelt.client.boards.games.GameReceiver;
import de.brettspielwelt.client.boards.games.PlainInformer;

public class Informer extends PlainInformer implements Serializable{

	int phase=0; 

	int[] score=new int[0];
	int[] platz=new int[0];
	int[] punkte=new int[6];
	
	BoardInfo boardinfo = new BoardInfo();
	
	int[][] board = new int[3][3];
	
	int[] playerActions = new int[anzMitSpieler];

	public Informer(){
		baseInit();
	}
	
	public void initBoard() {
		for (int x = 0; x < 3; x++) {
			for (int y = 0; y < 3; y++) {
				board[x][y]=-1;
			}
		}
	}
	
	public int checkRoundWin() {
		int[] retArr = new int[anzMitSpieler];
		int ret = -1;
		
		for (int i = 0; i < boardinfo.playerDecks.size(); i++) {
			Deck deck = boardinfo.playerDecks.get(i);
			for (int b = 0; b < deck.deck.size(); b++) {
				retArr[i] =deck.getTotalValue();
			}
		}
		
		for (int i = 0; i < retArr.length; i++) {
			if(retArr[i] <=21) {
				if(ret == -1 || retArr[i] > retArr[ret]) {
					ret = i;
				}else if(retArr[i] == retArr[ret]) {
					return -1;
				}
				
			}
		}
		
		return ret;
	}
	
	public boolean checkWin() {
		boolean ret = false;
		for (int i = 0; i < punkte.length; i++) {
			if(punkte[i]==3) {
				ret = true;
			}
		}
		return ret;
	}
	
	
	// ----------------------- Init and Starting of Game: reset() / spielStart()  
	public void spielStart() {
		baseInit();
		
		phase=1;
		currentPlayer=startSpieler;
		sendBoard();

		super.spielStart();
	}
	
	@Override
	public void reset() {
		baseInit();
		super.reset();
	}

	public void baseInit(){
		punkte=new int[4];
		platz=new int[0];
		score=new int[0];
		phase=0;
		initBoard();
		boardinfo.initBoard(anzMitSpieler);
		playerActions = new int[anzMitSpieler];
		for (int i = 0; i < playerActions.length; i++) {
			playerActions[i] = -1;
		}
	}

	// ------------- Game End ---------------------------
	
	
	@Override
	public void spielEnde() {
		phase=15;

		calcScorePlatz();

		sendGameStatus();
		sendBoard();

		super.spielEnde();
	}

	private void calcScorePlatz() {
		int[] tie=new int[anzMitSpieler];
		score=new int[anzMitSpieler];
		platz=new int[anzMitSpieler];

		for (int u=0; u<anzMitSpieler; u++) {
			platz[u]=1;
			score[u]=1;
			tie[u]=1;  // Tiebreaker 
		}
		
		for (int u=0; u<anzMitSpieler-1; u++) {
			for (int v=u+1; v<anzMitSpieler; v++) {
				if (score[u] < score[v]) {
					platz[u]++;
				} else if (score[u] < score[v]) {
					platz[v]++;
				}
			}
		}

		for(int u=0; u<anzMitSpieler-1; u++) {
			for(int v=u+1; v<anzMitSpieler; v++) {
				if(score[u]==score[v]) {
					if(tie[v]<tie[u]){
						platz[v]++;
					}else{ if(tie[u]<tie[v])
						platz[u]++;
					}
				}
			}
		}
	}
	
	// --------------- Input received from the Boards -----------------

	@Override
	public void doAnswer(int command,int pl,Data dat){
		switch(command){
		case 700:
			execAction(pl,((Integer)dat.v.elementAt(0)).intValue());
			break;
		}
	}

	private void execAction(int curPl, int action) {
		int act=action>>28&7;
		if(!isRunningGame()) return;
		

		if(currentPlayer==curPl) {
			if(phase==1) {
				if( act == 1) {
					boardinfo.hit(curPl);
					playerActions[curPl] = 1;
				}else if(act == 2) {
					playerActions[curPl] = 2;
				}
				if (currentPlayer!=anzMitSpieler-1){
					currentPlayer++;
				}else {
					currentPlayer = 0;
				}
				
				if(currentPlayer == startSpieler) {
					boolean allReady = true;
					for (int i = 0; i < playerActions.length; i++) {
						if(playerActions[i] <2) {
							allReady = false;
						}
					}
					if (allReady) {
						if (startSpieler!=anzMitSpieler-1){
							startSpieler++;
						}else {
							startSpieler = 0;
						}
						currentPlayer = startSpieler;
						if(checkRoundWin() > -1) {
							punkte[checkRoundWin()]++;
						}
						boardinfo.initBoard(anzMitSpieler);
					}else {
					}

					for (int i = 0; i < playerActions.length; i++) {
						playerActions[i] = -1;
					}
				}
				
//				currentPlayer =(currentPlayer!=(anzMitSpieler-1)?currentPlayer++:0);
				if(checkWin()){
					//Gewonnen
					spielEnde();
				}
			}
			sendBoard();
		}
	}


	// --------------  Sending Stuff  --------------------- 
	public void sendAnim(int sp, int[] anim) {
		if(spieler[sp]!=null)
			sendAnim(spieler[sp],anim);
	}
	
	public int[] appendAnim(int[] arr, int wh, int fr1, int fr2, int to1, int to2) {
		int[] ret;
		int le=arr==null?0:arr.length;
		if(arr!=null) {
			ret=new int[arr.length+5];
			System.arraycopy(arr,0,ret,0,arr.length);
		} else ret=new int[5];
		ret[le]=wh;
		ret[le+1]=fr1;
		ret[le+2]=fr2;
		ret[le+3]=to1;
		ret[le+4]=to2;
		return ret;
	}

	public void sendBoard(){
		for (GameReceiver playerInfo : getReceiverArray()) {
			sendBoard(playerInfo);
		}
	}
	public void sendBoard(int[] anim){
		for (GameReceiver playerInfo : getReceiverArray()) {
			sendBoard(playerInfo,anim);
		}
	}
	
	public void sendBoard(GameReceiver st){
		sendBoard(st,null);
	}
	
	// ------------------ The informations for all the Boards connected ---------------
	
	public void sendBoard(GameReceiver st, int[] anim){
		int id=st.getPlaying();
		
		Data dat=st.makeData(700,getSpielClient());
		dat.v.addElement(new Integer(anzMitSpieler));
		dat.v.addElement(new Integer(st.getPlaying()));
		dat.v.addElement(new Integer(phase));
		dat.v.addElement(new Integer(currentPlayer));
		dat.v.addElement(new Integer(startSpieler));
		dat.v.addElement(punkte);
		dat.v.addElement(boardinfo.exportBoard());
		
		if(anim!=null)
			dat.v.addElement(anim);
		else
			dat.v.addElement(new int[0]);
		st.sendDataObject(dat);
		sendGameStatus(st);
	}

	public void sendGameStatus(GameReceiver st) {
		Data dat=st.makeData(702,getSpielClient());

		for(int i=0; i<4; i++) {
			if(spieler[i]!=null) {
				dat.v.addElement(spieler[i].getPName());
			} else {
				dat.v.addElement("");
			}
		}
		st.sendDataObject(dat);
	}

}
