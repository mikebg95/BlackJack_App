package com.example.blackjack;

import java.util.ArrayList;
import java.util.Collections;

public class Deck {
    private String[] COATS = {"♠", "♥", "♦", "♣"};
    private int NUM_CARDS = 2;

    private ArrayList<Card> deck = new ArrayList<Card>();

    private int value;
    private String coat;

    public Deck() {
        for (int i = 0; i < COATS.length; i++) {
            for (int val = 2; val < 15; val++) {
                deck.add(new Card(val, COATS[i]));
            }
        }
        Collections.shuffle(deck);
    }

    public ArrayList<Card> getDeck() {
        return deck;
    }

    public Card dealTop() {
        Card card = deck.get(0);
        deck.remove(0);
        return card;
    }
}
