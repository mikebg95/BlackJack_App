package com.example.blackjack;

import java.util.ArrayList;

public class Player {
    private String name;
    private ArrayList<Card> cards = new ArrayList<Card>();
    private int chips;
    private int bet;

    public Player(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setBet(int bet) {
        this.bet = bet;
    }

    public int getBet() {
        return bet;
    }

    public int getChips() {
        return chips;
    }

    public void setChips(int chips) {
        this.chips = chips;
    }

    public void addCard(Card card) {
        cards.add(card);
    }

    public ArrayList<Card> getCards() {
        return cards;
    }

    public void resetCards() {
        this.cards.clear();
    }

    public String cardText(boolean dealer) {

        String result = "";
        if (dealer && cards.size() == 2) {
            result += cards.get(0).getName() + " XX";
        }
        else {
            for (int i = 0; i < cards.size(); i++) {
                result += cards.get(i).getName();
                result += " ";
            }
        }
        return result;
    }

    public int calculateScore() {
        int score = 0;
        for (int i = 0; i < this.cards.size(); i++) {
            score += this.cards.get(i).getScore();
        }

        while (score > 21) {
            for (int i = 0; i < this.cards.size(); i++) {
                if (this.cards.get(i).getScore() == 11) {
                    cards.get(i).setScore(1);
                    score -= 10;
                }
            }
            if (score > 21) {
                break;
            }
        }
        return score;
    }
}
