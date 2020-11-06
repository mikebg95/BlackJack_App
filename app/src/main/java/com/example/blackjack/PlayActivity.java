package com.example.blackjack;



import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.app.AlertDialog.Builder;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import static android.view.View.VISIBLE;
import static java.lang.Thread.sleep;

public class PlayActivity extends Activity {

    // static variables
    static String TAG = "GAMEPLAY";
    static int NUM_CARDS = 2;

    // UI views
    ListView options_lv;
    TextView dealer_cards, player_cards, info_text, bet_text, score_text, dealer_score_text;
    Button deal_btn;
    EditText insert_bet;

    // needed variables
    Intent intent;
    String name, dialogText;
    int chips;
    Player user, dealer;
    ArrayList<String> options;
    ArrayAdapter<String> arrayAdapter;
    Context context;
    ArrayList<Card> deck;
    boolean dealerTurnBool, playing;
    private Handler handler = new Handler();
    Timer timer = new Timer();
    AlertDialog dialog;

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
        score_text = findViewById(R.id.score_text);
        dealer_score_text = findViewById(R.id.dealer_score_text);

        // set needed variables
        options = new ArrayList<String>();
        arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, options);
        context = getApplicationContext();
        dialogText = "";
        dealerTurnBool = false;

        insert_bet.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    insert_bet.setText("");
                }
            }
        });

        // get name and chips from intent
        intent = getIntent();
        name = intent.getStringExtra("name");
        chips = (intent.getIntExtra("chips", 999));

        // create player objects for player and dealer and set players chips
        user = new Player(name);
        dealer = new Player("Dealer");
        user.setChips(chips);

        // show player info (name and chips)
        info_text.setText("Name: " + user.getName() + "\nChips: " + Integer.toString(user.getChips()));

        // start background thread
        startBackgroundThread();

        // when 'deal' button is clicked, make sure all is well
        deal_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    int bet = Integer.parseInt(insert_bet.getText().toString());
                    if (bet > user.getChips()) {
                        Toast.makeText(context, "Not enough chips!", Toast.LENGTH_SHORT).show();
                    }
                    else {
                        // set user bet and chips
                        user.setBet(bet);
                        user.setChips(user.getChips() - user.getBet());
                        info_text.setText("Name: " + user.getName() + "\nChips: " + Integer.toString(user.getChips()));

                        // play round
                        playRound();
                    }
                }
                catch (NumberFormatException e) {
                    Toast.makeText(context, "Please insert a bet", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public void playRound() {

        playing = true;

        // show bet info
        bet_text.setText("Bet: " + Integer.toString(user.getBet()));

        // set bet entry and deal button invisible
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

        // show cards
        setCards(user, true);
        setCards(dealer, false);

        // check if player has blackjack
        checkBlackjack();

        // present move options based on turn and cards
        presentMoves();

        // show move options in listview
        options_lv.setAdapter(arrayAdapter);
        arrayAdapter.notifyDataSetChanged();
        options_lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                String option = (String) parent.getItemAtPosition(position); // get chosen move option

                // if user chose 'hit'
                if (option.equals("Hit")) {
                    hit(user);
                }

                // if user chose 'double'
                else if (option.equals("Double")) {

                    // double bet and update chips
                    user.setChips(user.getChips() - user.getBet());
                    user.setBet(user.getBet() * 2);
                    bet_text.setText(Integer.toString(user.getBet()));
                    info_text.setText("Name: " + user.getName() + "\nChips: " + Integer.toString(user.getChips()));

                    hit(user);
                    dealerTurnBool = true;
                    dealerTurn();
                    checkWinner();
                }

                // if user chose 'stand'
                else if (option.equals("Stand")) {

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            dealer_cards.setText(dealer.cardText(false));
                        }
                    });

                    try {
                        sleep(1000);
                    } catch (Exception e) {

                    }


                    // TODO: dealer cards don't update until after entire program finishes
                    // TODO: maybe put in dealerTurn() ??
//                    runOnUiThread(new Runnable() {
//                        @Override
//                        public void run() {
//                            setCards(dealer, true);
//                            timer.schedule(new SmallDelay(), 100);
//                        }
//                    });

                    dealerTurnBool = true;
                    dealerTurn();
                    checkWinner();

                }

                // Shows players cards after each move
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        setCards(user, true);
                    }
                });
            }
        });

    }

    public void newRound() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                // clear options
                options.clear();
                arrayAdapter.notifyDataSetChanged();

                // clear all views
                dealer_cards.setText("");
                player_cards.setText("");
                score_text.setText("");
                dealer_score_text.setText("");
                bet_text.setText("");

                // clear dialog text
                dialogText = "";

                // make bet entry and deal button visible again
                deal_btn.setVisibility(VISIBLE);
                insert_bet.setVisibility(VISIBLE);

                // update info text
                info_text.setText("Name: " + user.getName() + "\nChips: " + Integer.toString(user.getChips()));
            }
        });

        // clear cards of player and dealer
        user.resetCards();
        dealer.resetCards();

        // when deal button clicked, start new round
        deal_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    int bet = Integer.parseInt(insert_bet.getText().toString());
                    if (bet > user.getChips()) {
                        Toast.makeText(context, "Not enough chips!", Toast.LENGTH_SHORT).show();
                    }
                    else {
                        // set user bet and chips
                        user.setBet(bet);
                        user.setChips(user.getChips() - user.getBet());
                        info_text.setText("Name: " + user.getName() + "\nChips: " + Integer.toString(user.getChips()));

                        // play round
                        playRound();
                    }
                }
                catch (NumberFormatException e) {
                    Toast.makeText(context, "Please insert a bet", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    // TODO: Doesn't show dealer cards until after program is finished!!
    public void dealerTurn() {
//        dealerTurnBool = true;
        while (true) {
//
//            runOnUiThread(new Runnable() {
//                @Override
//                public void run() {
//                    setCards(dealer, true);
//                    Log.d(TAG, "SET CARDS DEALER");
//                }
//            });

//            timer.schedule(new SmallDelay(), 100);
//            Log.d(TAG, "DEALER CARDS: " + dealer.cardText(false));

            if (dealer.getCards().size() == 2 && dealer.calculateScore() == 21) {
                Log.d(TAG, "DEALER HAS BLACKJACK");
                return;
            }
            if (dealer.calculateScore() > 21) {
                Log.d(TAG, "DEALER OVER 21");
//                Log.d(TAG, "DEALER CARDS: " + dealer.cardText(false));
                return;
            }
            if (dealer.calculateScore() < 17) {
                Log.d(TAG, "DEALER TAKES ANOTHER CARD");
                hit(dealer);
                dealer_cards.setText(dealer.cardText(false));
//                Log.d(TAG, "DEALER CARDS: " + dealer.cardText(false));
            }
            if (dealer.calculateScore() >= 17) {
                Log.d(TAG, "DEALER NO MORE CARDS");
//                Log.d(TAG, "DEALER CARDS: " + dealer.cardText(false));
                return;
            }
        }
    }

    // TODO: program crashes when attempting to show dealer score in background thread, but not with player score
    // TODO: EVEN THOUGH THEY ARE EXACTLY THE SAME
    public void startBackgroundThread() {
        Thread bgThread = new Thread() {
            public void run() {
                while (true) {
                    if (playing) {

                        int score = user.calculateScore();
                        int dealerScore = dealer.calculateScore();

                        if (score == 0) {
                            score_text.setText("");
                        } else if (score > 21) {
                            Log.d(TAG, "OVER 21");
                            score_text.setText("Score: " + Integer.toString(score));
                            setCards(user, true);
                            done("loss");
                            Log.d(TAG, "Score over 21! You lose.");
                        } else {
                            score_text.setText("Score: " + Integer.toString(score));
                        }
                    }
                    else {
//                        Log.d(TAG, "NOT PLAYING");
                    }
                }
            }
        };
        bgThread.start();
    }

    public void presentMoves() {
        options.clear(); // clear previous moves in list

        // show move options based on situation
        options.add("Hit");
        options.add("Stand");
        if (user.getChips() > user.getBet() && user.getCards().size() == 2) {
            options.add("Double");
        }
        if (user.getCards().get(0).getScore() == user.getCards().get(1).getScore() && user.getCards().size() == 2) {
            options.add("Split");
        }
    }

    // TODO: setCards() works, but only until after round finished
    public void setCards(Player player, boolean open) {
        String text;
        if (player.getName().equals("Dealer")) {
            if (player.getCards().size() == 2 && !open) {
                text = dealer.cardText(true);
            }
            else {
                text = dealer.cardText(false);
            }
            dealer_cards.setText(text);
        }
        else {
            text = user.cardText(false);
            player_cards.setText(text);
        }
        timer.schedule(new SmallDelay(), 100);
    }

    public void done(String result) {
        playing = false; // so that background thread stops loop

        // number of times user wins back bet
        double times;
        if (result.equals("tie")) {
            Log.d(TAG, "TIE");
            dialogText += "It's a TIE!";
            times = 1;
        }
        else if (result.equals("win")) {
            Log.d(TAG, "WIN");
            dialogText += "You WIN!";
            times = 2;
        }
        else if (result.equals("blackjack")) {
            Log.d(TAG, "BLACKJACK");
            dialogText += "You have BLACKJACK!";
            times = 2.5;
        }
        else {
            Log.d(TAG, "LOSS");
            dialogText += "You LOSE!";
            times = 0;
        }
        // make calculations
        int currentChips = user.getChips();
        int currentBet = user.getBet();
        long nb = Math.round(currentBet * times);
        int newBet = (int) nb;
        int newChips = currentChips + newBet;
        user.setChips(newChips);

        // present user with dialoginterface with info and asking for new round
        dialogText += "\nYou gained " + newBet + " new chips.\n\n";
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                dialog = new MyDialogBuilder().getMyDialog(PlayActivity.this, dialogText + "New round?");
                dialog.show();
            }
        });
    }

    class SmallDelay extends TimerTask {
        public void run() {
            handler.sendEmptyMessage(0);
        }
    }

    public void checkWinner() {

        // calculate scores and check winner
        int userScore = user.calculateScore();
        int dealerScore = dealer.calculateScore();
        Log.d(TAG, "USER CARDS: " + user.cardText(false));
        Log.d(TAG, "DEALER CARDS: " + dealer.cardText(false));
        Log.d(TAG, "USER SCORE: " + Integer.toString(userScore));
        Log.d(TAG, "DEALER SCORE: " + Integer.toString(dealerScore));


        if (userScore > 21) {
            Log.d(TAG, "USERSCORE > 21");
            done("loss");
        }
        else if (dealerScore > 21) {
            Log.d(TAG, "DEALERSCORE > 21");
            done("win");
        }
        else {
            Log.d(TAG, "NOBODY > 21");
            if (userScore > dealerScore) {
                Log.d(TAG, "USERSCORE > DEALERSCORE -> WIN");
                done("win");
            }
            else if (dealerScore > userScore) {
                Log.d(TAG, "DEALERSCORE > USERSCORE -> LOSS");
                done("loss");
            }
            else {
                Log.d(TAG, "USERSCORE = DEALERSCORE -> TIE");
                done("tie");
            }
        }
    }

    public void checkBlackjack() {
        if (hasBlackjack(user)) {
            Toast.makeText(context, "You have BlackJack!", Toast.LENGTH_SHORT).show();

            dealer_cards.setText(dealer.cardText(false)); // show dealers cards

            // check if dealer has blackjack
            if (hasBlackjack(dealer)) {
                Toast.makeText(context, "Dealer also Blackjack! TIE!", Toast.LENGTH_SHORT).show();
                done("tie");
            } else {
                done("blackjack");
            }
        }
    }

    public void hit(Player player) {
        // deal decks top card to player
        player.addCard(deck.get(0));
        deck.remove(0);

        // present move options
        presentMoves();
        arrayAdapter.notifyDataSetChanged();
    }

    static boolean hasBlackjack(Player player) {

        // checks if player has blackjack
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

    public class MyDialogBuilder {
        private Context context;

        public AlertDialog getMyDialog(Context c, String message) {
            this.context = c;

            // create alert dialog
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setMessage(message).setPositiveButton("Yes", dialogClickListener).
                    setNegativeButton("No", dialogClickListener);

            AlertDialog dialog = builder.create();
            return dialog;
        }

        // when clicked on one of the options
        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which){

                    // start new round
                    case DialogInterface.BUTTON_POSITIVE:
                        newRound();
                        break;

                    // go back to home screen
                    case DialogInterface.BUTTON_NEGATIVE:
                        intent = new Intent(PlayActivity.this, HomeActivity.class);
                        startActivity(intent);
                        break;
                }
            }
        };
    }
}