package com.example.simpleui;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private EditText inputText;
    private CheckBox hideCheckBox;
    private ListView historyListView;

    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sharedPreferences = getSharedPreferences("settings", Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();

        inputText = (EditText)findViewById(R.id.inputText);
//        inputText.setText("1234");
        inputText.setText(sharedPreferences.getString("inputText", ""));
        inputText.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_DOWN) {
                    if (keyCode == KeyEvent.KEYCODE_ENTER) {
                        submit(v);
                        return true;
                    }
                }
                return false;
            }
        });

        hideCheckBox = (CheckBox) findViewById(R.id.hideCheckBox);
        hideCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                editor.putBoolean("hideCheckBox", isChecked);
                editor.commit();
            }
        });
        hideCheckBox.setChecked(sharedPreferences.getBoolean("hideCheckBox", false));

        historyListView = (ListView) findViewById(R.id.historyListView);
        setHistory();

    }

    private void setHistory() {
        String[] data = new String[]{"1", "2", "3", "4", "5", "6", "7", "8", "9", "10"};
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, data);
        historyListView.setAdapter(adapter);
    }

    public void submit(View view){
        String text = inputText.getText().toString();
        editor.putString("inputText", text);
        editor.commit();
        Utils.writeFile(this, "history.txt", text + "\n");
        if(hideCheckBox.isChecked()){
            text = "**********";
            inputText.setText("***********");
        }
        String fileContent = Utils.readFile(this, "history.txt");
        Toast.makeText(this, fileContent, Toast.LENGTH_LONG).show();
    }
}
