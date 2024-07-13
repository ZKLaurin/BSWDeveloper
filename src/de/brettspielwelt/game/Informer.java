package de.brettspielwelt.game;

import java.util.ArrayList;
import java.util.Iterator;

import de.Data;
import de.brettspielwelt.client.boards.games.GameReceiver;
import de.brettspielwelt.client.boards.games.PlainInformer;
import de.brettspielwelt.tools.IntVector;

public class Informer extends PlainInformer {

	int phase = 0;
	int round = 0, nextRound = 0;
	boolean lastRound = false;

	int[] score = new int[0];
	int[] platz = new int[0];

	int[][] würfelStapel = new int[3][9]; // Alle 9 Würfel pro 3 Farben
	int[][][] spielerBoards = new int[4][3][6];
	IntVector verdecktPlättchenStapel = new IntVector(); // Verdeckte Chips: 26x Mistkäfer, 13x DoppeltMistkäfer, 9x
															// Kleeblätter
	IntVector[] spielerPlättchenStapel = new IntVector[4]; // Plättchenstapel der Spieler
	int[] auslage = new int[5];
	int[] stolenInfo = new int[4];

//	int aktionen=3,nextAktionen=0,stealPlayer=0;

	public void sendAnim(int sp, int[] anim) {
		if (spieler[sp] != null)
			sendAnim(spieler[sp], anim);
	}

	public int[] appendAnim(int[] arr, int wh, int fr1, int fr2, int to1, int to2) {
		int[] ret;
		int le = arr == null ? 0 : arr.length;
		if (arr != null) {
			ret = new int[arr.length + 5];
			System.arraycopy(arr, 0, ret, 0, arr.length);
		} else
			ret = new int[5];
		ret[le] = wh;
		ret[le + 1] = fr1;
		ret[le + 2] = fr2;
		ret[le + 3] = to1;
		ret[le + 4] = to2;
		return ret;
	}

	public Informer() {
		baseInit();
	}

	public void construct(int nr) {
//		super.construct(nr);
		initGame();
		baseInit();
	}

	public int getMinMaxPlayer() {
		return (2 << 16) | 5;
	}

	public int getSpielID() {
		return 170;
	}

	public String getSpielClient() {
		return "PasstNichtBoard";
	}

	public void sendComplete() {
		sendGameStatus();
		sendBoard();
	}

	public void sendComplete(GameReceiver pl) {
		sendGameStatus(pl);
		sendBoard(pl);
	}

	@Override
	public void spielStart() {
		initGame();
		super.spielStart();

		los();
	}

	@Override
	public void reset() {
		initGame();
		super.reset();
	}

	// Boilerplate ends ---------------------------

	public void initGame() {
		baseInit();
	}

	@Override
	public void spielEnde() {
		phase = 15;

		calcScorePlatz();

		sendGameStatus();
		sendBoard();

		// insertGame(getSpielID(), score, platz);
		super.spielEnde();
	}

	private void calcScorePlatz() {
		score = new int[anzMitSpieler];
		platz = new int[anzMitSpieler];

		for (int u = 0; u < anzMitSpieler; u++) {
//			score[u]=punkte[u];
			platz[u] = 1;
		}

		for (int u = 0; u < anzMitSpieler - 1; u++) {
			for (int v = u + 1; v < anzMitSpieler; v++) {
				if (score[u] < score[v]) {
					platz[u]++;
				} else if (score[u] > score[v]) {
					platz[v]++;
				}
			}
		}

	}

	public void los() {
		baseInit();
		currentPlayer = startSpieler;

		phase = 1;
		sendBoard();
	}

	public void drawPlättchen(int sp) {
		spielerPlättchenStapel[sp].addElement(verdecktPlättchenStapel.removeLast());
	}

	public void insertDiceIntoBoard(int sp, int row, int diceValue) {
		for (int i = 0; i < spielerBoards[sp][row].length; i++) {
			if (spielerBoards[sp][row][i] == -1) {
				spielerBoards[sp][row][i] = diceValue;
				return;
			}
		}
	}

	public int removeLastDiceFromBoard(int sp, int row) {
		int ret = -1;
		for (int i = spielerBoards[sp][row].length - 1; i >= 0; i--) {
			if (spielerBoards[sp][row][i] != -1) {
				ret = spielerBoards[sp][row][i];
				spielerBoards[sp][row][i] = -1;
				return ret;
			}
		}
		return ret;
	}

	public int rollDice() {
		return rnd(6) + 1;
	}

	// Function to move the last dice from würfelStapel to auslage
	public void moveLastDiceFromWürfelStapelToAuslage(int diceColor, int auslageIndex) {
		for (int i = würfelStapel[diceColor].length - 1; i >= 0; i--) {
			if (würfelStapel[diceColor][i] != -1) {
				auslage[auslageIndex] = würfelStapel[diceColor][i];
				würfelStapel[diceColor][i] = -1;
				return;
			}
		}
	}

	// Function to move dice from auslage to spielerBoards
	public void moveDiceFromAuslageToBoard(int auslageIndex, int sp, int row) {
		if (auslage[auslageIndex] != -1) {
			insertDiceIntoBoard(sp, row, auslage[auslageIndex]);
			auslage[auslageIndex] = -1;
		}
	}

	public int countNonNegativeOneEntries(int[] array) {
		int count = 0;
		for (int value : array) {
			if (value == -1) {
				break;
			}
			count++;
		}
		return count;
	}

	public void rollAuslage() {
		for (int i = 1; i < auslage.length; i++) {
			if (auslage[i] > 0) {
				auslage[i] = rollDice();
			} else {
				return;
			}
		}
	}
	
	public void insertDiceIntoWürfelStapel(int row, int diceValue) {
		for (int j = 0; j < würfelStapel[row].length; j++) {
			if (würfelStapel[row][j] == -1) {
				würfelStapel[row][j] = diceValue;
				break;
			}
		}
	}

	public void clearAuslage() {
		int diceColor = auslage[0];
		for (int i = 1; i < auslage.length; i++) {
			insertDiceIntoWürfelStapel(diceColor, auslage[i]);
			auslage[i] = -1;
		}
		auslage[0] = -1;

	}

	public int findDiceFromPlayerBoard(int player, int row, int diceValue) {
		for (int i = spielerBoards[player][row].length - 1; i >= 0; i--) {
			if (spielerBoards[player][row][i] == diceValue) {
				spielerBoards[player][row][i] = -1;
				return diceValue;
			}
		}
		return -1; // If no matching dice is found
	}

	public void stealFromPlayer(int currentPlayer, int targetPlayer, int diceValue) {
		if (stolenInfo[targetPlayer] == -1) {
			int row = auslage[0];
			stolenInfo[targetPlayer] = 1;
			if (findDiceFromPlayerBoard(targetPlayer, row, diceValue) > 0)
				insertDiceIntoBoard(currentPlayer, row, diceValue);
		}
	}
	
	public int findLastDiceFromPlayerBoard(int player, int row, int diceValue) {
		for (int i = spielerBoards[player][row].length - 1; i >= 0; i--) {
			if (spielerBoards[player][row][i] > -1) 
				if (spielerBoards[player][row][i] == diceValue) {
					return i;
				} else {
					break;
				}
		}
		return -1; // If no matching dice is found
	}
	
	public int findLastDiceValueFromPlayerBoard(int player, int row) {
		for (int i = spielerBoards[player][row].length - 1; i >= 0; i--) {
			if (spielerBoards[player][row][i] != -1) {
				return spielerBoards[player][row][i];
			}
		}
		return -1; // If no matching dice is found
	}
	
	public int[] getArrOfPlayerDice(int player, int row, int lastDiceIndex) {
		int[] ret = new int[6];
		int counter = 0;
		
		for (int i = spielerBoards[player][row].length - 1; i >= lastDiceIndex; i--) {
			if (spielerBoards[player][row][i] > -1) {
				ret[counter] =spielerBoards[player][row][i];
				spielerBoards[player][row][i] = -1;
				counter++;
			}
		}
		return ret;
	}

	public void clearPlayerBoardToIndex(int player, int lastDiceIndex) {
		int[] ret = new int[6];
		for (int i = 0; i < 3; i++) {
			ret = getArrOfPlayerDice(player,i, lastDiceIndex);
			for (int j = 0; j < ret.length; j++) {
				if (ret[j] > 0)
					insertDiceIntoWürfelStapel(i, ret[j]);
			}
		}
		
	}
	
	public int[] exportSpielerPlättchen() {
		int[] ret = new int[5];
		ret[0] = verdecktPlättchenStapel.size();
		for (int i = 0; i < spielerPlättchenStapel.length; i++) {
			ret[i + 1] = spielerPlättchenStapel[i].size();
		}

		return ret;
	}

	public void initPlättchen() {
		verdecktPlättchenStapel.removeAllElements();
		for (int i = 0; i < 48; i++) {
			int tempNum = 1;
			if (i >= 26 && i < 39) {
				tempNum = 2;
			} else if (i >= 39) {
				tempNum = 3;
			}

			verdecktPlättchenStapel.addElement(tempNum);
		}
		verdecktPlättchenStapel.mix();
	}

	public void baseInit() {
		round = 0;
		phase = 14;
		info = new int[4];
		score = new int[0];
		platz = new int[0];
		spielerPlättchenStapel = new IntVector[4];

		for (int i = 0; i < 4; i++) {
			spielerPlättchenStapel[i] = new IntVector();
			stolenInfo[i] = -1;
			for (int j = 0; j < spielerBoards[0].length; j++) {
				for (int j2 = 0; j2 < spielerBoards[0][0].length; j2++) {
					spielerBoards[i][j][j2] = -1;
				}
			}
		}

		for (int i = 0; i < würfelStapel.length; i++) {
			for (int j = 0; j < würfelStapel[i].length; j++) {
				würfelStapel[i][j] = rollDice();
			}
		}

		for (int i = 0; i < 5; i++) {
			auslage[i] = -1;
		}

		initPlättchen();

		platz = new int[0];
		score = new int[0];
		nextRound = 0;
		currentPlayer = 0;
	}

	@Override
	public void doAnswer(int command, int pl, Data dat) {
		switch (command) {
		case 700:
//		    if(!pl.kiebitz){
			execAction(pl, ((Integer) dat.v.elementAt(0)).intValue());
			break;
//			}
		}
	}

	private void execAction(int curPl, int action) {
		int act = action >> 28 & 15;
		if (!isRunningGame())
			return;

		if (phase == 1 && currentPlayer == curPl) {
			if (act == 1) {
				int diceColor = action & 3;
				auslage[0] = diceColor;
				int diceLeft = countNonNegativeOneEntries(würfelStapel[diceColor]);
				if (diceLeft == 0||countNonNegativeOneEntries(spielerBoards[currentPlayer][auslage[0]]) == 6) {
					return;
				}
				phase = 2;
				if (diceLeft >= 4) {
					for (int i = 0; i < 4; i++) {
						moveLastDiceFromWürfelStapelToAuslage(diceColor, i + 1);
					}
				} else {
					for (int i = 0; i < diceLeft; i++) {
						moveLastDiceFromWürfelStapelToAuslage(diceColor, i + 1);
					}
				}
				rollAuslage();
				boolean chooseDicePossible = false;
				for (int i = 1; i < auslage.length; i++) {
					if (findLastDiceValueFromPlayerBoard(curPl, auslage[0]) < auslage[i]) {
						chooseDicePossible = true;
						break;
					}
				}
				if(!chooseDicePossible) {
					clearPlayerBoardToIndex(curPl, 2);
					clearAuslage();
					weiter();
				}
			} else if (act == 2) {
				// Hier auswerten
			}
			sendBoard();
		} else if (phase == 2&& currentPlayer == curPl) {
			if (act == 3) {
				
				int auslageIndex = action & 31;
				int chosenDiceValue = auslage[auslageIndex];
				int ableToSteal = -1;
				
				if(findLastDiceValueFromPlayerBoard(curPl, auslage[0]) >= chosenDiceValue) {
					return;
				}
				
				if (countNonNegativeOneEntries(spielerBoards[currentPlayer][auslage[0]]) < 6 ) {
					stolenInfo[currentPlayer] = chosenDiceValue;
					moveDiceFromAuslageToBoard(auslageIndex, currentPlayer, auslage[0]);

					for (int i = 1; i < auslage.length; i++) {
						if (countNonNegativeOneEntries(spielerBoards[currentPlayer][auslage[0]]) < 6 && auslage[i] == chosenDiceValue)
							moveDiceFromAuslageToBoard(i, currentPlayer, auslage[0]);
					}
					
					for (int i = 0; i < anzMitSpieler; i++) {
						if(i==currentPlayer)
							continue;
						ableToSteal = findLastDiceFromPlayerBoard(i,auslage[0], chosenDiceValue);
						if(ableToSteal>-1)
							break;
					}

				} else {
					// TODO add losing dice function
				}
				if(ableToSteal>-1&& countNonNegativeOneEntries(spielerBoards[currentPlayer][auslage[0]]) < 6) {
					phase = 3;
					sendBoard();
				}else {
					clearAuslage();
					weiter();
					sendBoard();
				}
			}
		} else if (phase == 3&& currentPlayer == curPl) {
			if (act == 4) {
				int targetPlayer = action & 31;
				boolean canSteal = false;
				int ableToSteal = -1;
				
				
				if (countNonNegativeOneEntries(spielerBoards[currentPlayer][auslage[0]]) < 6) {
					stealFromPlayer(curPl, targetPlayer, stolenInfo[currentPlayer]);
				}
				
				for (int i = 0; i < anzMitSpieler; i++) {
					if (stolenInfo[i]==-1) {
						canSteal=true;
						break;
					}
				}
				for (int i = 0; i < anzMitSpieler; i++) {
					if(i==curPl|| stolenInfo[i] > -1)
						continue;
					ableToSteal= findLastDiceFromPlayerBoard(i,auslage[0], stolenInfo[curPl]);
					if ( ableToSteal > -1) {
						break;
					}
				}
				
				if (canSteal && ableToSteal > -1 && countNonNegativeOneEntries(spielerBoards[curPl][auslage[0]]) < 6) {
				} else {
					clearAuslage();
					weiter();
				}

				sendBoard();
			}
		}
//
//		if(phase==0  && currentPlayer==curPl) {
//			if(gesammelt[curPl].contains(18)) {
//				if(act==1) { // nimm karte in hand (2-4)    oder auf Portal (0-1)
//					int place=action&7;
//					int target=action>>3&3;
//					if(target<2 && place<2 && portale[curPl][target]>0) {
//						int saveP=portale[curPl][target];
//						int[] anim=null;
//						anim=appendAnim(null, 0<<16|auslage[place], 1,place, 20+curPl,target);
//						portale[curPl][target]=auslage[place];
//						auslage[place]=saveP;
//						anim=appendAnim(anim, 0<<16|saveP, 20+curPl,target, 1,place);
////						phase=1;
//						sendBoard(anim);
//					}
//				}
//			}
//		}
//		if((phase==0||phase==1) && currentPlayer==curPl) {
//			if(act==8) {
//				int[] animt=null;
//				for(int i=3; i>=0; i--) {
//					animt=appendAnim(animt, 1<<16|auslage[i+2], 1,i+2, 2,0);
//					ablageNum.addElement(auslage[i+2]);
//				}
//				sendBoard(animt);
//				animt=null;
//				for(int i=0; i<4; i++) {
//					auslage[i+2]=drawNumberCard(curPl);
//					animt=appendAnim(animt, 1<<16|auslage[i+2], 2,0, 1,i+2);
//				}
//				aktionen--;
//				phase=1;
//				sendBoard(animt);
//			}
//			if((act==2||act==3)) { // karte aus portal 2/3==0/1 aktivieren
//				int getCard=portale[curPl][act-2];
//				chooser.removeAllElements();
//				for(int i=15; i>=0; i--) {
//					if((action>>i&1)==1) {
//						int w=hand[curPl].removeElementAt(i);
//						ablageNum.addElement(w);
//						if(getCard==22) chooser.addElement(w); // Die kann ich zur�ck bekommen
//					}
//				}
//				gesammelt[curPl].addElement(getCard);
//				punkte[curPl]+=charKosten[getCard][3]&15;
//				perlen[curPl]+=charKosten[getCard][3]>>4&15;
//				int[] anim=null;
//				anim=appendAnim(null,0<<16|getCard, 20+curPl,act-2, 30+curPl,gesammelt[curPl].size());
//				portale[curPl][act-2]=-1;
//				aktionen--;
//				phase=1;
//				gainCard(getCard);
//				sendBoard(anim);
//			}
//			if(act==1) { // nimm karte in hand (2-4)    oder auf Portal (0-1)
//				int place=action&7;
//				int target=action>>3&3;
//		
//				if(place>1 && place<7) { // zahlenkarten
//					// achtung 6=nazistapel
//					
//					int[] anim=null;
//					int[] animt=null;
//					int neuCard=-1;
//					if(place==6) { // von nazistapel
//						hand[curPl].addElement(drawNumberCard(curPl));
//						anim=appendAnim(null, 1<<16|0, 2,0, 10+curPl,hand[curPl].size()-1);
//					}else {
//						// Grundprinzip --- Server daten �nderung -- sendBoard(anim)  
//						hand[curPl].addElement(auslage[place]);
//						anim=appendAnim(null, 1<<16|auslage[place], 1,place, 10+curPl,hand[curPl].size()-1);
//		
//						neuCard=drawNumberCard(curPl);
//						auslage[place]=neuCard;
//						// Bei Animationorten muss der Zielplatz existieren (richtig berechenbar sein) 
//						// Alle flying places werden nicht gezeichnet nur durch die animation
//						anim=appendAnim(anim, 1<<16|neuCard, 2,0, 1,place);
//
//
//					}
//					aktionen--;
//					phase=1;
//					sendBoard(anim);
//					if((neuCard&64)==64) { // Charaustausch
//						for(int i=1; i>=0; i--) {
//							animt=appendAnim(animt, 0<<16|auslage[i], 1,i, 2,1);
//							ablageChar.addElement(auslage[i]);
//						}
//						sendBoard(animt);
//						animt=null;
//						for(int i=0; i<2; i++) {
//							auslage[i]=drawCharCard(curPl);
//							animt=appendAnim(animt, 0<<16|auslage[i], 2,1, 1,i);
//						}
//						sendBoard(animt);
//					}
//
//					
//				}else {
//					if(target==3 && portale[curPl][0]==-1) target=0;
//					if(target==3 && portale[curPl][1]==-1) target=1;
//					
//					if(target<2 && portale[curPl][target]==-1) {
//						int[] anim=null;
//						if(place==7) { // von Nazistapel
//							int drawnCard=drawCharCard(curPl);;
//							anim=appendAnim(null, 0<<16|drawnCard, 2,1, 20+curPl,target);
//							portale[curPl][target]=drawnCard;
//						}else {
//							anim=appendAnim(null, 0<<16|auslage[place], 1,place, 20+curPl,target);
//							portale[curPl][target]=auslage[place];
//							int neuCard=drawCharCard(curPl);
//							auslage[place]=neuCard;
//							anim=appendAnim(anim, 0<<16|neuCard, 2,1, 1,place);
//						}
//						aktionen--;
//						phase=1;
//						sendBoard(anim);
//					}				}
//			}
//			if(aktionen<=0 && phase==1) {
//				weiter();
//				sendBoard();
//			}
//		}
	}

	public void weiter() {		
		currentPlayer = (currentPlayer + 1) % anzMitSpieler;
		if (lastRound && currentPlayer == startSpieler)
			spielEnde();
		else {
			for (int i = 0; i < anzMitSpieler; i++) {
				stolenInfo[i] = -1;
			}
			if (currentPlayer == startSpieler) {
			}
			phase = 1; // phase 0 wird autiomatisch beendet wenn erste aktion ausgef�hrt.
		}
	}

	public void sendBoard() {
		for (GameReceiver playerInfo : getReceiverArray()) {
			sendBoard(playerInfo);
		}
	}

	public void sendBoard(int[] anim) {
		for (GameReceiver playerInfo : getReceiverArray()) {
			sendBoard(playerInfo, anim);
		}
	}

	public void sendBoard(GameReceiver st) {
		sendBoard(st, null);
	}

	public int[][] exportPlayerBoards() {
		int[][] ret = new int[4][18];
		for (int i = 0; i < spielerBoards.length; i++) {
			for (int j = 0; j < spielerBoards[i].length; j++) {
				for (int j2 = 0; j2 < spielerBoards[i][j].length; j2++) {
					ret[i][j * 6 + j2] = spielerBoards[i][j][j2];
				}
			}
		}
		return ret;
	}

	IntVector empty = new IntVector();
	private boolean noMoreTopChar;
	int[] info = new int[5];

	public void sendBoard(GameReceiver st, int[] anim) {
		int id = st.getPlaying();
//		for(int i=0; i<anzMitSpieler; i++) info[i]=hand[i].size()|punkte[i]<<5|perlen[i]<<10|portalGraph[i]<<15;
		Data dat = st.makeData(700, getSpielClient());
		dat.v.addElement(new Integer(anzMitSpieler));
		dat.v.addElement(new Integer(st.getPlaying()));
		dat.v.addElement(new Integer(phase));
		dat.v.addElement(new Integer(round));
		dat.v.addElement(new Integer(currentPlayer));
		dat.v.addElement(new Integer(startSpieler));
		dat.v.addElement(id > -1 ? spielerPlättchenStapel[id] : empty);
		dat.v.addElement(exportPlayerBoards());
		dat.v.addElement(exportSpielerPlättchen());
		dat.v.addElement(würfelStapel);
		dat.v.addElement(auslage);

		dat.v.addElement(info);

		if (anim != null)
			dat.v.addElement(anim);
		else
			dat.v.addElement(new int[0]);
		st.sendDataObject(dat);
		sendGameStatus(st);
	}

	public void sendGameStatus(GameReceiver st) {
		Data dat = st.makeData(702, getSpielClient());

		dat.v.addElement(score);
		dat.v.addElement(platz);
		for (int i = 0; i < 5; i++) {
			if (spieler[i] != null) {
				dat.v.addElement(spieler[i].getPName());
			} else {
				dat.v.addElement("");
			}
		}
		st.sendDataObject(dat);
	}

	public void sendGameStatus() {
		for (GameReceiver playerInfo : getReceiverArray()) {
			sendGameStatus(playerInfo);
		}
	}
}
