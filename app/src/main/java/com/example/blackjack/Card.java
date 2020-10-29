package com.example.blackjack;

public class Card {
    private int value;
    private String coat;
    private int score;
    private String name;
    private String val_name;


    public String getName() {
        return name;
    }

    public int getScore() {
        return score;
    }



    public void setScore(int score) {
        this.score = score;
    }

    public Card(int value, String coat) {
        this.value = value;
        this.coat = coat;
        this.score = 0;

        if (value == 11) {
            val_name = "J";
        }
        else if (value == 12) {
            val_name = "Q";
        }
        else if (value == 13) {
            val_name = "K";
        }
        else if (value == 14) {
            val_name = "A";
        }
        else {
            val_name = Integer.toString(value);
        }

        if (value == 11 || value == 12 || value == 13) {
            this.score = 10;
        }
        else if (value == 14) {
            this.score = 11;
        }
        else {
            this.score = value;
        }

        this.name = coat + val_name;
    }


}
