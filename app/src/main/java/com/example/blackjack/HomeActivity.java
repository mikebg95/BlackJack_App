package com.example.blackjack;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class HomeActivity extends AppCompatActivity {

    EditText name_text;
    EditText chips_text;
    Button play_btn;
    Intent intent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        name_text = findViewById(R.id.name);
        chips_text = findViewById(R.id.chips);
        play_btn = findViewById(R.id.play);

        play_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    int chips = Integer.parseInt(chips_text.getText().toString());

                    if (isEmpty(name_text)) {
                        Toast.makeText(getApplicationContext(), "Please insert a name", Toast.LENGTH_SHORT).show();
                    }
                    else {
                        // go to playActivity, add name and chips to intent
                        intent = new Intent(HomeActivity.this, PlayActivity.class);
                        intent.putExtra("name", name_text.getText().toString());
                        intent.putExtra("chips", chips);
                        startActivity(intent);
                    }
                }
                catch (NumberFormatException e) {
                    Toast.makeText(getApplicationContext(), "Please insert number of chips", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public boolean isEmpty(EditText et) {
        String text = et.getText().toString();
        if (text.trim().equals("")) {
            return true;
        }
        else {
            return false;
        }
    }
}