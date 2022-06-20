package dev.bestia.guitaraokeleader;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class StartupActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_startup);

        Button button_continue = findViewById(R.id.button_continue);
        button_continue.setOnClickListener(v -> startActivity(new Intent(StartupActivity.this,MainActivity.class)));
    }

    /// disable back button
    @Override
    public void onBackPressed() {

    }

}