package de.brettspielwelt.game;

public class Card {
	
	// cardValue ist der Wert der Karte: 2 = 2, König = 10
	// symbol 0 = Karo, 1 = Herz, 2 = Kreuz, 3 = Spaten
	
	int cardValue= 0;
	int symbol = 0;
	String displayValue = "";
	
	public Card(int initCardValue, int initSymbol, String initDisplayValue) {
		cardValue = initCardValue;
		symbol = initSymbol;	
		displayValue = initDisplayValue;
	}

}
