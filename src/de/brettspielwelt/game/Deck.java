package de.brettspielwelt.game;

import java.io.Console;
import java.util.ArrayList;
import java.util.Collections;

public class Deck {
	
	ArrayList<Card> deck = new ArrayList<Card>();
		
	public void initDeck() {
		deck.clear();
		for (int i = 0; i < 13; i++) {
			for (int j = 0; j < 4; j++) {
				if(i == 0) {
					deck.add(new Card(i+1,j,"A"));
				}else if(i > 9) {
					if(i == 10) {
						deck.add(new Card(10,j,"J"));
					}else if(i == 11) {
						deck.add(new Card(10,j,"Q"));
					}else if(i == 12) {
						deck.add(new Card(10,j,"K"));
					}
				}else {
					deck.add(new Card(i+1,j,Integer.toString(i+1)));
				}
			}
		}
		shuffle();
	}
	
	public void shuffle() {
		Collections.shuffle(deck);
	}
	
	public void addCard(Card newCard) {
		deck.add(newCard);
	}
	
	public void removeCard(Card newCard) {
		int index = deck.indexOf(newCard);
		deck.remove(index);
	}
	
	public void logDeck() {
		System.err.println("---------------------------------------------------------------- ");
		for (Card card : deck) {
			System.err.println(card.displayValue+" "+card.symbol);
		}
	}
	
	public int getTotalValue() {
		int ret = 0;
		int assCount = 0;
		for (Card card : deck) {
			if (card.cardValue == 1)
				assCount++;
			ret += card.cardValue;
		}
		for (int i = 0; i < assCount; i++) {
			if (ret+10 <= 21) {
				ret+=10;
			}
		}
		return ret;
	}
}