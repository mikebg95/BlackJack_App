package com.example.blackjack;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

import static java.lang.Thread.currentThread;

public class PlayActivity extends Activity {

    ListView options_lv;
    TextView dealer_cards, player_cards, info_text, bet_text;
    Button deal_btn;
    EditText insert_bet;
    Intent intent;
    String name;
    int chips;
    Player user, dealer;
    ArrayList<String> options = new ArrayList<String>();
    ArrayAdapter<String> arrayAdapter;
    Context context;
    ArrayList<String> option;
    boolean optionClicked;
    ArrayList<Card> deck;
    private Handler handler = new Handler();

    static int NUM_CARDS = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play);

        // layout views
        options_lv = findViewById(R.id.options_lv);
        dealer_cards = findViewById(R.id.dealer_cards);
        player_cards = findViewById(R.id.player_cards);
        info_text = findViewById(R.id.info_text);
        bet_text = findViewById(R.id.bet_text);
        deal_btn = findViewById(R.id.deal_button);
        insert_bet = findViewById(R.id.insert_bet);

        arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, options);
        context = getApplicationContext();

        // get name and chips from intent
        intent = getIntent();
        name = intent.getStringExtra("name");
        chips = (intent.getIntExtra("chips", 999));

        // create player objects for player and dealer and set players chips
        user = new Player(name);
        dealer = new Player("Dealer");
        user.setChips(chips);

        // background thread to check if users score is over 21
        new Thread( new Runnable() {
            @Override public void run() {
                while (true) {
                    int score = user.calculateScore();
                    info_text.setText(Integer.toString(score));
                    if (score > 21) {
                        currentThread().interrupt();
                        user.resetCards();
                        player_cards.setText(user.cardText(false));
                    }
                }
            }
        }).start();

        // when 'deal' button is clicked
        deal_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                optionClicked = false;

                playRound();
            }
        });
    }

    public void playRound() {

        // get inserted bet and set bet textview to it
        int bet = Integer.parseInt(insert_bet.getText().toString());
        user.setBet(bet);
        bet_text.setText(Integer.toString(user.getBet()));

        // set bet edittext and deal button invisible
        insert_bet.setVisibility(View.INVISIBLE);
        deal_btn.setVisibility(View.INVISIBLE);

        // create shuffled deck
        deck = new Deck().getDeck();

        // deal cards
        for (int i = 0; i < NUM_CARDS; i++) {
            user.addCard(deck.get(0));
            deck.remove(0);
            dealer.addCard(deck.get(0));
            deck.remove(0);
        }

        // show cards in game
        player_cards.setText(user.cardText(false));
        dealer_cards.setText(dealer.cardText(true));


        // check for blackjack
        if (hasBlackjack(user)) {
            Toast.makeText(context, "You have BlackJack!", Toast.LENGTH_SHORT).show();

            dealer_cards.setText(dealer.cardText(false)); // show dealers cards

            // check if dealer has blackjack
            if (hasBlackjack(dealer)) {
                Toast.makeText(context, "Dealer also has BlackJack!", Toast.LENGTH_SHORT).show();

                // add bet back to player chips
                user.setChips(user.getChips() + user.getBet());
            } else {
                // add 2.5 * bet to player chips (& round)
                user.setChips((int) (user.getBet() * 2.5));
            }
        }

        // show move options
        options.add("Hit");
        options.add("Stand");
        if (user.getChips() > user.getBet() && user.getCards().size() == 2) {
            options.add("Double");
        }
        if (user.getCards().get(0).getScore() == user.getCards().get(1).getScore() && user.getCards().size() == 2) {
            options.add("Split");
        }

        options_lv.setAdapter(arrayAdapter);
        options_lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // get chosen move option
                String option = (String) parent.getItemAtPosition(position);

                // if user chose 'hit'
                if (option.equals("Hit")) {
                    user.addCard(deck.get(0));
                    deck.remove(0);
                    player_cards.setText(user.cardText(false));
                }

                // if user chose 'double'
                else if (option.equals("Double")) {
                    user.setBet(user.getBet() * 2);
                    bet_text.setText(Integer.toString(user.getBet()));
                    user.setChips(user.getChips() - user.getBet());
                    user.addCard(deck.get(0));
                    deck.remove(0);
                    player_cards.setText(user.cardText(false));
                }

                // if user chose 'stand'
                else if (option.equals("Stand")) {
                    dealer_cards.setText(dealer.cardText(false));
                    dealerTurn();
                    checkWinner();

                    DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            switch (which){
                                case DialogInterface.BUTTON_POSITIVE:
                                    //Yes button clicked
                                    break;

                                case DialogInterface.BUTTON_NEGATIVE:
                                    intent = new Intent(PlayActivity.this, HomeActivity.class);
                                    startActivity(intent);
                                    break;
                            }
                        }
                    };

                    AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());
                    builder.setMessage("Another round?").setPositiveButton("Yes", dialogClickListener)
                            .setNegativeButton("No", dialogClickListener).show();

                    newRound();
                }
            }
        });

    }

    public void newRound() {

    }

    public void dealerTurn() {
        while (true) {
            if (dealer.getCards().size() == 2 && dealer.calculateScore() == 21) {
                Toast.makeText(context, "DEALER HAS BLACKJACK!", Toast.LENGTH_SHORT).show();
                break;
            }
            if (dealer.calculateScore() > 21) {
                Toast.makeText(context, "Dealer over 21!", Toast.LENGTH_SHORT).show();
                break;
            }
            if (dealer.calculateScore() < 17) {
                Toast.makeText(context, "Dealer takes another card", Toast.LENGTH_SHORT).show();
                dealer.addCard(deck.get(0));
                deck.remove(0);
                dealer_cards.setText(dealer.cardText(false));
            }
            if (dealer.calculateScore() >= 17) {
                Toast.makeText(context, "Dealer no more cards", Toast.LENGTH_SHORT).show();
                break;
            }
        }
        return;
    }

    public void checkWinner() {
        int userScore = user.calculateScore();
        int dealerScore = dealer.calculateScore();
        if (userScore > 21) {
            Toast.makeText(context, "Player over 21! You lose!", Toast.LENGTH_SHORT).show();
        }
        else if (dealerScore > 21) {
            Toast.makeText(context, "Dealer over 21! You win!", Toast.LENGTH_SHORT).show();
            int newChips = user.getChips() + user.getBet() * 2;
            user.setChips(newChips);
        }
        else {
            if (userScore > dealerScore) {
                Toast.makeText(context, "You win!", Toast.LENGTH_SHORT).show();
                int newChips = user.getChips() + user.getBet() * 2;
                user.setChips(newChips);
            }
            else if (dealerScore > userScore) {
                Toast.makeText(context, "Dealer wins!", Toast.LENGTH_SHORT).show();
            }
            else {
                Toast.makeText(context, "It's a tie!", Toast.LENGTH_SHORT).show();
                int newChips = user.getChips() + user.getBet();
                user.setChips(newChips);
            }
        }
    }


    static boolean hasBlackjack(Player player) {
        if (player.getCards().size() != 2) {
            return false;
        }
        int total = 0;
        for (int i = 0; i < player.getCards().size(); i++) {
            total += player.getCards().get(i).getScore();
        }
        if (total == 21) {
            return true;
        }
        else {
            return false;
        }
    }
}