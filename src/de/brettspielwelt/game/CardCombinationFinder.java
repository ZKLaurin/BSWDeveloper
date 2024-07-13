package de.brettspielwelt.game;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import de.brettspielwelt.tools.IntVector;

public class CardCombinationFinder {

	public static final int FIXED=0;
	public static final int THREE_SUM=1; // SUM
	public static final int X_TEN=2;
	public static final int STREET=3; // LENGTH
	public static final int TWO_PAIRS=4; 
	public static final int EQUALS=5; // LENGTH
	public static final int TWO_PAIRS_66=6; // LENGTH
	public static final int EVEN=7; // 0= ODD 1=EVEN
	public static final int FIXED_OR=8; // a << 10 | b
	public static final int 
			CAN_REDUCE=512,ONE_EIGHT=513,THREE_JOKER=514,TWO_PERL=515,REDRAW_ANY=516,
			ONE_MORE=517,EXTRA_ACTION=518,PEEK_CHAR=519,SWAP_CHAR=520,
			I_3_ACTIONS=521,I_NEXT_1_ACTION=522,I_BACK_ONE=523,I_STEAL_HAND=524,I_STEAL_CHAR=525,WILL_O=526;
	
	static int[][] charKosten = { {}, { 0, 11, 1 }, { 0, 22, 2 }, { 0, 33, 3 }, { 0, 44, 4 }, { 0, 55, 5 }, { 0, 66, 6 }, { 0, 77, 7 }, { 0, 12, 256 },
			{ 0, 1111, 0 }, { 0, 357, CAN_REDUCE }, { 1, 10, ONE_EIGHT }, { 0, 333, THREE_JOKER }, { 0, 2, TWO_PERL }, { 0, 18, REDRAW_ANY }, { 2, 10, ONE_MORE }, { 0, 45678, EXTRA_ACTION }, { 3, 3, PEEK_CHAR },
			{ 3, 5, SWAP_CHAR }, { 0, 1357, I_3_ACTIONS }, { 0, 2468, I_3_ACTIONS }, { 4, 0, I_NEXT_1_ACTION }, { 0, 345, I_BACK_ONE }, { 0, 567, I_STEAL_HAND}, { 1, 7, I_STEAL_CHAR }, { 8, 333 << 10 |666, WILL_O }, { 8, 444<<10|555, WILL_O },
			{ 5, 2, 0 }, { 5, 3, 0 }, { 5, 4, 0 }, { 0, 88, 0 }, { 1, 20, 0 }, { 0, 7777, 0 }, { 0, 8888, 0 }, { 0, 6688, 0 }, { 0, 7788, 0 },
			{ 0, 222, 0 }, { 6, 0, 0 }, { 0, 1234, 0 }, { 7, 1, 0 }, { 7, 0, 0 } };
	
	public static void main(String[] args) {
		IntVector outside=new IntVector(new int[] {1,3,11},1);
		IntVector handKarten=new IntVector(new int[] {4,1,3},1);
		int gems=2;
		
		IntVector resultV=check(10, outside, handKarten, gems);
		
		for(int i=0; i<resultV.size(); i++) {
			int w=resultV.elementAt(i);
			System.out.println(" : "+(w>>20&15)+" Gem "+(w>>16&15)+" Cards "+(w&0xffff)+" selC");
		}

	}

	public static List<List<Integer>> findCombinationsWithType(List<Integer> hand, int points, int minMax, int checkType, int checkValue ) {
		List<List<Integer>> result = new ArrayList<>();

		// Erstelle eine Liste der Karten ohne Joker
		List<Integer> nonJokers = new ArrayList<>();
		int jokerCount = 0;

		for (int card : hand) {
			if ((card&15) == 0) {
				jokerCount++;
			} else {
				nonJokers.add(card);
			}
		}

		// Suche nach Kombinationen von drei Karten
		findCombinations(nonJokers, jokerCount, points, new ArrayList<>(), 0, minMax, checkType, checkValue, result);

		return result;
	}

	private static boolean isFixed(List<Integer> cards, int num, int len) {
		int compDiv=1;
		for (int i = 0; i<len-1; i++) compDiv *= 10;
		
		List<Integer> cardsC = new ArrayList<>(cards);  // Step 1: Create a copy
		Collections.sort(cardsC);
		
		for (int i = 0; i < cardsC.size(); i++) {
			if (cardsC.get(i) != ((num/compDiv)%10) ) 
				return false; // Nicht passend
			compDiv/=10;
		}
		return true;
	}
	private static boolean isTwoPairs(List<Integer> cards, boolean sechs) {
		List<Integer> cardsC = new ArrayList<>(cards);  // Step 1: Create a copy
		Collections.sort(cardsC);
		if(sechs)
			return (cardsC.get(0)==cardsC.get(1) && cardsC.get(2)==cardsC.get(3) && (cardsC.get(0)==6||cardsC.get(2)==6));
		return (cardsC.get(0)==cardsC.get(1) && cardsC.get(2)==cardsC.get(3));
	}
	private static boolean isEquals(List<Integer> cards) {
		int v=cards.get(0);
		for(int i=1; i<cards.size(); i++)
			if(cards.get(i)!=v) return false;
		return true;
	}
	private static boolean isStreet(List<Integer> cards) {
		List<Integer> cardsC = new ArrayList<>(cards);  // Step 1: Create a copy
		Collections.sort(cardsC);
		for (int i = 1; i < cards.size(); i++) {
			if (cardsC.get(i) != cardsC.get(i - 1) + 1) {
				return false; // Nicht aufeinanderfolgende Karten, keine Straße
			}
		}
		return true; // Alle Karten sind aufeinanderfolgend, Straße gefunden
	}
	private static boolean isSum(List<Integer> cards, int targetSum) {
	  	int sum=0;
		for(Integer v: cards)
			sum+=v;
		return sum == targetSum;
	}
	private static boolean isEven(List<Integer> cards, boolean even) {
		for(Integer v: cards) {
			if(even && (v&1)==1) return false;
			if(!even && (v&1)==0) return false;
		}
		return true;
	}
	
	private static void findCombinations(List<Integer> hand, int jokerCount, int points, List<Integer> currentCombination, int start, int minMax, int checkType, int checkValue, List<List<Integer>> result) {
		if (currentCombination.size() >= (minMax&15) && currentCombination.size() <= (minMax>>4) ) {
			List<Integer> conCheck=new ArrayList<>();
			for(Integer v: currentCombination)
				conCheck.add(v&15);
			boolean condition=false;
			switch(checkType) {
			case FIXED: condition=isFixed(conCheck,checkValue,minMax&15); break;
			case STREET: condition=isStreet(conCheck); break;
			case THREE_SUM: condition=isSum(conCheck,checkValue); break;
			case X_TEN: condition=isSum(conCheck,checkValue); break;
			case EQUALS: condition=isEquals(conCheck); break;
			case TWO_PAIRS: condition=isTwoPairs(conCheck,false); break;
			case TWO_PAIRS_66: condition=isTwoPairs(conCheck,true); break;
			case EVEN: condition=isEven(conCheck,checkValue==1); break;
			case FIXED_OR: condition=isFixed(conCheck,checkValue&0xffff,3)||isFixed(conCheck,checkValue>>16&0xffff,3); break;
			}
			//System.out.println("Check "+conCheck+" "+condition);
			
			if (condition) {
				result.add(new ArrayList<>(currentCombination));
			} else if (points > 0) {
				for (int i = 0; i < currentCombination.size(); i++) {
					int card = currentCombination.get(i);
					if (card>0 && card<8) { // Assuming cards are in range [0, 8]
						currentCombination.set(i, card + 1 |16);
						findCombinations(hand, jokerCount, points - 1, currentCombination, start, minMax, checkType, checkValue, result);
						currentCombination.set(i, card); // Backtrack
					}
					if (card>1 && card<9) { // Verringere den Kartenwert
						currentCombination.set(i, card - 1 | 32);
						findCombinations(hand, jokerCount, points - 1, currentCombination, start, minMax, checkType, checkValue, result);
						currentCombination.set(i, card); // Backtrack
					}
				}
			}
			return;
		}

		for (int i = start; i < hand.size(); i++) {
			currentCombination.add(hand.get(i));
			findCombinations(hand, jokerCount, points, currentCombination, i + 1, minMax, checkType, checkValue, result);
			currentCombination.remove(currentCombination.size() - 1);
		}

		// Verwende Joker, falls verfügbar
		if (jokerCount > 0) {
			for (int j = 1; j <= 8; j++) { // Joker kann jeden Wert von 1 bis 8 annehmen
				currentCombination.add(j|64);
				findCombinations(hand, jokerCount - 1, points, currentCombination, start, minMax, checkType, checkValue, result);
				currentCombination.remove(currentCombination.size() - 1);
			}
		}
	}

	private static int getMinMax(int checkType, int checkValue) {
		int minMax = checkValue<<4 | checkValue;
		if(checkType==FIXED) {
			if(checkValue<10) minMax=1|1<<4;
			else if(checkValue<100) minMax=2|2<<4;
			else if(checkValue<1000) minMax=3|3<<4;
			else if(checkValue<10000) minMax=4|4<<4;
			else if(checkValue<100000) minMax=5|5<<4;
		}
		if(checkType==THREE_SUM) minMax=3|3<<4;
		if(checkType==X_TEN) minMax=1|15<<4;
		if(checkType==TWO_PAIRS) minMax=4|4<<4;
		if(checkType==TWO_PAIRS_66) minMax=4|4<<4;
		if(checkType==EVEN) minMax=3|3<<4;
		if(checkType==FIXED_OR) minMax=3|3<<4;
		return minMax;
	}

	
	public static IntVector check(int cardId, IntVector outside, IntVector handKarten, int points) {
		// Beispielaufruf der Methode
		
		long start=System.currentTimeMillis();
		
		int outsideJoker=0;
		boolean hasThreeJoker=false, hasOneToEight=false;
		for(int i=0; i<outside.size(); i++) {
			int act=charKosten[outside.elementAt(i)][2];
			if(act==THREE_JOKER) hasThreeJoker=true;
			if(act==ONE_EIGHT) hasOneToEight=true;
		}

		List<Integer> hand = new ArrayList<>();
		for(int i=0; i<handKarten.size(); i++) {
			int c=handKarten.elementAt(i);
			hand.add(hasThreeJoker&&c==3?256:c);
		}
		for(int i=0; i<outside.size(); i++) {
			int act=charKosten[outside.elementAt(i)][2];
			if(act>0 && act<=8)
				hand.add(act|256);
			if(act==256) { // Joker
				outsideJoker++;
				hand.add(256);
			}
		}
		
		int checkType=charKosten[cardId][0];
		int checkValue=charKosten[cardId][1];
		int minMax = getMinMax(checkType, checkValue);
		
		if(cardId==36) points--; // Bei der 222Edelstein karte einen weniger
		IntVector resultV=new IntVector();
		boolean nochEinser=false;
		int oneEChange=0;
		do {
			nochEinser=false;
			List<List<Integer>> combinations = findCombinationsWithType(hand,  points, minMax,   checkType, checkValue);
			//System.out.println("Folgende Kombinationen gefunden:");
			for (List<Integer> combination : combinations) {
				int selC=0;
				int karten=0, edelsteine=0, jokerUsed=0;
				for(Integer com:combination) {
					//System.out.print((com&15)+"/"+(com>>4)+" ");
					int woher=com>>4; // 1 - mit edelstein up 2 - mit Edelstein down   4-joker-draussen   16-draussen
					if(woher==4) jokerUsed++;
					if(com>>4==1 || com>>4==2) edelsteine++;
					if(com>>4==0 || com>>4==1 || com>>4==2) {
						karten++;
						int orginalWert=com&15;
						if(com>>4==1) orginalWert--;
						if(com>>4==2) orginalWert++;
						for(int i=0; i<handKarten.size(); i++) {
							if((selC>>i&1)==1) continue;
							int hWert=handKarten.elementAt(i);
							if((oneEChange>>i&1)==1) hWert=8;
							if(hWert==orginalWert) selC|=1<<i;
						}
					}
				}
				if(jokerUsed>outsideJoker && hasThreeJoker) {
					int diff=jokerUsed-outsideJoker;
					for(int i=0; i<handKarten.size(); i++) {
						if(handKarten.elementAt(i)==3) { 
							diff--; selC|=1<<i;
							karten++;
							if(diff==0) break;
						}
					}
				}
				if(cardId==36) edelsteine++;
				int res=edelsteine<<20 | karten<<16 | selC;
				if(!resultV.contains(res))
					resultV.addElement(res);
				
				//System.out.println(" --> "+karten+" Karten "+edelsteine+" Gems "+selC+" selC");
			}
			if(hasOneToEight) {
				for(int i=0; i<handKarten.size(); i++) {
					if(hand.get(i)==1) {
						hand.set(i,8);
						oneEChange|=1<<i;
						nochEinser=true;
						break;
					}
				}
			}
		} while (nochEinser);
		
		resultV.sort();
 
		System.err.println("Time: "+(System.currentTimeMillis()-start));
		return resultV;
	}
}
