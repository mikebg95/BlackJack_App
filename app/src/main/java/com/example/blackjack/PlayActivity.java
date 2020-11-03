package com.example.blackjack;


import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.app.AlertDialog.Builder;
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
import java.util.Timer;
import java.util.TimerTask;

import javax.security.auth.callback.Callback;

import static android.view.View.VISIBLE;
import static java.lang.Thread.currentThread;
import static java.lang.Thread.sleep;

public class PlayActivity extends Activity {

    ListView options_lv;
    TextView dealer_cards, player_cards, info_text, bet_text, score_text, dealer_score_text;
    Button deal_btn;
    EditText insert_bet;
    Intent intent;
    String name;
    int chips;
    Player user, dealer;
    ArrayList<String> options;
    ArrayAdapter<String> arrayAdapter;
    Context context;
    ArrayList<String> option;
    boolean optionClicked;
    ArrayList<Card> deck;
    private Handler handler = new Handler();
    Timer timer = new Timer();
    boolean dealerTurnBool;
//    BackgroundRunnable backgroundRunnable;
    Thread bgThread;
    boolean playing;
    String dialogText;
    AlertDialog dialog;

    static String TAG = "GAMEPLAY";

    int round_number = 1;

    static int NUM_CARDS = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play);

        dealerTurnBool = false;
        playing = true;

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

        options = new ArrayList<String>();
        arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, options);
        context = getApplicationContext();

        dialogText = "";

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
//        backgroundRunnable = new BackgroundRunnable();
//        new Thread(backgroundRunnable).start();

        // when 'deal' button is clicked
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

        // start background thread
        startBackgroundThread();

        // show bet info
        bet_text.setText("Bet: " + Integer.toString(user.getBet()));

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

        // show cards
        setCards(user, true);
        setCards(dealer, false);

        checkBlackjack();

        presentMoves();

        options_lv.setAdapter(arrayAdapter);
        arrayAdapter.notifyDataSetChanged();
        options_lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // get chosen move option
                String option = (String) parent.getItemAtPosition(position);

                // if user chose 'hit'
                if (option.equals("Hit")) {
                    hit(user);
                }

                // if user chose 'double'
                else if (option.equals("Double")) {

                    // double bet and update chips
                    user.setBet(user.getBet() * 2);
                    bet_text.setText(Integer.toString(user.getBet()));
                    user.setChips(user.getChips() - user.getBet());

                    hit(user);
                }

                // if user chose 'stand'
                else if (option.equals("Stand")) {
//                    playing = false;

                    dealer_cards.setText(dealer.cardText(false));

                    // TODO: dealer cards don't update until after entire program finishes
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

                // TODO
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
                options.clear();
                arrayAdapter.notifyDataSetChanged();

                // clear all views
                dealer_cards.setText("");
                player_cards.setText("");
                score_text.setText("");
                dealer_score_text.setText("");
                bet_text.setText("");

                // make bet insertion and deal button visible again
                deal_btn.setVisibility(VISIBLE);
                insert_bet.setVisibility(VISIBLE);

                info_text.setText("Name: " + user.getName() + "\nChips: " + Integer.toString(user.getChips()));
            }
        });

        // clear cards of player and dealer
        user.resetCards();
        dealer.resetCards();

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
            Log.d(TAG, "DEALER CARDS: " + dealer.cardText(false));

            if (dealer.getCards().size() == 2 && dealer.calculateScore() == 21) {
                Log.d(TAG, "DEALER HAS BLACKJACK");
                return;
            }
            if (dealer.calculateScore() > 21) {
                Log.d(TAG, "DEALER OVER 21");
                Log.d(TAG, "DEALER CARDS: " + dealer.cardText(false));
                return;
            }
            if (dealer.calculateScore() < 17) {
                Log.d(TAG, "DEALER TAKES ANOTHER CARD");
                hit(dealer);
                dealer_cards.setText(dealer.cardText(false));
                Log.d(TAG, "DEALER CARDS: " + dealer.cardText(false));
            }
            if (dealer.calculateScore() >= 17) {
                Log.d(TAG, "DEALER NO MORE CARDS");
                Log.d(TAG, "DEALER CARDS: " + dealer.cardText(false));
                return;
            }
        }
    }

    // TODO: Background thread only works first round!!
    public void startBackgroundThread() {
        Thread bgThread = new Thread() {
            public void run() {
                Looper.prepare();
                while (playing) {
                    Log.d(TAG, "...STARTED BG THREAD LOOP...");

                    int score = user.calculateScore();
                    int dealerScore = dealer.calculateScore();

                    if (score == 0) {
                        score_text.setText("");
                    }
                    else if (score > 21) {
                        score_text.setText("Score: " + Integer.toString(score));
                        setCards(user, true);
                        done("loss");
                        Log.d(TAG, "Score over 21! You lose.");
                    }
                    else {
                        score_text.setText("Score: " + Integer.toString(score));
                    }


                    // TODO: program crashes when attempting to show dealer score in background thread, but not with player score
                    // TODO: EVEN THOUGH THEY ARE EXACTLY THE SAME
//                    if (dealerScore == 0) {
//                        dealer_score_text.setText("");
//                    }
//                    else if (dealerScore > 21) {
//                        dealer_score_text.setText("Score: " + Integer.toString(score));
////                        setCards(user, true);
//                        done("loss");
////                        Log.d(TAG, "Score over 21! You lose.");
//                    }
//                    else {
//                        dealer_score_text.setText("Score: " + Integer.toString(score));
//                    }
                }
                Looper.loop();
            }
        };
        bgThread.start();
    }

    public void presentMoves() {
        options.clear();

        // show move options
        options.add("Hit");
        options.add("Stand");
        if (user.getChips() > user.getBet() && user.getCards().size() == 2) {
            options.add("Double");
        }
        if (user.getCards().get(0).getScore() == user.getCards().get(1).getScore() && user.getCards().size() == 2) {
            options.add("Split");
        }
    }

    public void setCards(Player player, boolean open) {
        String text;
        if (player.getName() == "Dealer") {
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
        playing = false;
        Log.d("USER_CARDS", "PLAYER CARDS: " + user.cardText(false));
        Log.d(TAG, "DEALER CARDS: " + dealer.cardText(false));

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
        int currentChips = user.getChips();
        int currentBet = user.getBet();
        long nb = Math.round(currentBet * times);
        int newBet = (int) nb;
        int newChips = currentChips + newBet;
        user.setChips(newChips);

        dialogText += "\nYou gained " + newBet + " new chips.\n\n";
        dialog = new MyDialogBuilder().getMyDialog(PlayActivity.this, dialogText + "New round?");
        dialog.show();

        Log.d("GAMEPLAY", "You gained " + newBet + " new chips!");
    }

    class SmallDelay extends TimerTask {
        public void run() {
            handler.sendEmptyMessage(0);
        }
    }

    public void checkWinner() {
        int userScore = user.calculateScore();
        int dealerScore = dealer.calculateScore();
        if (userScore > 21) {
            Log.d(TAG, "PLAYER OVER 21! YOU LOSE");
            done("loss");
        }
        else if (dealerScore > 21) {
            Toast.makeText(context, "Dealer over 21! You win!", Toast.LENGTH_SHORT).show();
            done("win");
        }
        else {
            if (userScore > dealerScore) {
                Toast.makeText(context, "You win!", Toast.LENGTH_SHORT).show();
                done("win;");
            }
            else if (dealerScore > userScore) {
                Toast.makeText(context, "Dealer wins!", Toast.LENGTH_SHORT).show();
                done("loss");
            }
            else {
                Toast.makeText(context, "It's a tie!", Toast.LENGTH_SHORT).show();
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
//        setCards(player);
        player.addCard(deck.get(0));
        deck.remove(0);
        presentMoves();
        arrayAdapter.notifyDataSetChanged();
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

    public class MyDialogBuilder {
        private Context context;

        public AlertDialog getMyDialog(Context c, String message) {
            this.context = c;

            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setMessage(message).setPositiveButton("Yes", dialogClickListener).
                    setNegativeButton("No", dialogClickListener);

            AlertDialog dialog = builder.create();
            return dialog;
        }

        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which){
                    case DialogInterface.BUTTON_POSITIVE:
                        playing = false;
                        newRound();
                        break;

                    case DialogInterface.BUTTON_NEGATIVE:
                        intent = new Intent(PlayActivity.this, HomeActivity.class);
                        startActivity(intent);
                        break;
                }
            }
        };
    }
}