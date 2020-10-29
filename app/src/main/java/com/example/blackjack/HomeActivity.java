package com.example.blackjack;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

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
                int chips = Integer.parseInt(chips_text.getText().toString());

                // go to playActivity, add name and chips to intent
                intent = new Intent(HomeActivity.this, PlayActivity.class);
                intent.putExtra("name", name_text.getText());
                intent.putExtra("chips", chips);
                startActivity(intent);
            }
        });
    }
}
