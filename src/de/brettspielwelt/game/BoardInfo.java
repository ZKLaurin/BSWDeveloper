package de.brettspielwelt.game;

import java.util.ArrayList;

public class BoardInfo {
	
	ArrayList<Deck> playerDecks = new ArrayList<Deck>();
	
	Deck dealerDeck = new Deck();
	
	public void initBoard(int playerCount) {
		playerDecks.clear();
		dealerDeck.initDeck();
		for (int i = 0; i < playerCount; i++) {
			playerDecks.add(new Deck());
			hit(i);
			hit(i);
		}
	}
	
	public void hit(int playerId) {
		playerDecks.get(playerId).addCard(dealerDeck.deck.get(0));
		dealerDeck.deck.remove(0);
	}
	
	public int getDeckValue(int playerId) {
		int ret = 0;
		for (Card card :playerDecks.get(playerId).deck) {
			ret += card.cardValue;
		}
		return ret;
	}
	
	public String[][] exportBoard(){
		if (dealerDeck.deck.size() == 0) {
			String[][] ret = new String[1][1];
			ret[0][0] = "no";
			return ret;
		}
		int tmpSize = 0;
		
		for (int i = 0; i <= playerDecks.size();i++) {
			Deck deck;
			if(i < playerDecks.size()) {
				deck = playerDecks.get(i);
			} else {
				deck = dealerDeck;
			}
			if (tmpSize < deck.deck.size()) {
				tmpSize = deck.deck.size();
			}
        } 
		String[][] ret = new String[playerDecks.size()+1][tmpSize*3];
		for (int i = 0; i <= playerDecks.size();i++) {
			Deck deck;
			if(i < playerDecks.size()) {
				deck = playerDecks.get(i);
			} else {
				deck = dealerDeck;
			}
            for (int cardIndex = 0; cardIndex < deck.deck.size();cardIndex++) {
            	Card card = deck.deck.get(cardIndex);
                ret[i][(3*cardIndex)+0] = Integer.toString(card.cardValue);
                ret[i][(3*cardIndex)+1] = Integer.toString(card.symbol);
                ret[i][(3*cardIndex)+2] = card.displayValue;
            }
        }
		
		for (int i = 0; i < ret.length; i++) {
	        for (int j = 0; j < ret[i].length; j++) {
	            if (ret[i][j] == null) {
	                ret[i][j] = "";
	            }
	        }
	    }
		
//		System.out.println("length: "+ret.length + " ");
//	    for (int i = 0; i < ret.length; i++) {
//	        for (int j = 0; j < ret[i].length; j++) {
//	            System.out.print(ret[i][j] + " ");
//	        }
//	        System.out.println();
//	    }
		return ret;
	}
	
	public void importBoard(String[][] boardState) {
		if (boardState[0][0].equals("no") )
			return;
		
		
	    playerDecks.clear();
	    dealerDeck.deck.clear();

	    // Determine number of players based on boardState length
	    int numPlayers = boardState.length - 1; // Subtract 1 for dealer

	    // Populate playerDecks and dealerDeck
	    for (int i = 0; i < numPlayers; i++) {
	        Deck deck = new Deck();
	        for (int j = 0; j < boardState[i].length; j += 3) {

	            if(!boardState[i][j + 2].isEmpty()) {
		            int cardValue = Integer.parseInt(boardState[i][j]);
		            int symbol = Integer.parseInt(boardState[i][j + 1]);
		            String displayValue = boardState[i][j + 2];
		            Card card = new Card(cardValue, symbol, displayValue);
		            deck.deck.add(card);
	            }
	        }
	        playerDecks.add(deck);
	    }

	    // Populate dealerDeck
	    for (int j = 0; j < boardState[numPlayers].length; j += 3) {
	    	if(!boardState[numPlayers][j + 2].isEmpty()) {
		        int cardValue = Integer.parseInt(boardState[numPlayers][j]);
		        int symbol = Integer.parseInt(boardState[numPlayers][j + 1]);
		        String displayValue = boardState[numPlayers][j + 2];
		        Card card = new Card(cardValue, symbol, displayValue);
		        dealerDeck.deck.add(card);
	        }
	    }
	}
}
