package com.example.simpleui;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private EditText inputText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        inputText = (EditText)findViewById(R.id.inputText);
        inputText.setText("1234");
    }

    public void submit(View view){
        String text = inputText.getText().toString();
        Toast.makeText(this, text, Toast.LENGTH_LONG).show();
    }
}
