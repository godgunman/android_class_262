package com.example.simpleui;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SimpleAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.parse.FindCallback;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.SaveCallback;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_CODE_MENU_ACTIVITY = 1;
    private static final int REQUEST_TAKE_PHOTO = 2;

    private EditText inputText;
    private CheckBox hideCheckBox;
    private ListView historyListView;
    private Spinner storeInfoSpinner;
    private ImageView photoImageView;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private ProgressDialog progressDialog;
    private ProgressBar progressBar;
    private String menuResult;
    private boolean hasPhoto = false;
    private List<ParseObject> queryResult;

    private CallbackManager callbackManager;
    private LoginButton loginButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        storeInfoSpinner = (Spinner) findViewById(R.id.storeInfoSpinner);
        sharedPreferences = getSharedPreferences("settings", Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();

        photoImageView = (ImageView) findViewById(R.id.photo);
        inputText = (EditText) findViewById(R.id.inputText);
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
        historyListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                goToOrderDetail(position);
            }
        });
        progressDialog = new ProgressDialog(this);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        setHistory();
        setStoreInfo();
        setupFacebook();
    }

    private void setupFacebook() {
        callbackManager = CallbackManager.Factory.create();
        loginButton = (LoginButton) findViewById(R.id.login_button);
        loginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                AccessToken token = loginResult.getAccessToken();
                GraphRequest request = GraphRequest.newGraphPathRequest(token
                        , "/v2.5/me",
                        new GraphRequest.Callback() {
                            @Override
                            public void onCompleted(GraphResponse response) {
                                JSONObject object = response.getJSONObject();
                                try {
                                    String name = object.getString("name");
                                    Toast.makeText(MainActivity.this, "Hello " + name, Toast.LENGTH_SHORT).show();
                                    Log.d("debug", object.toString());

                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        });
                request.executeAsync();
            }

            @Override
            public void onCancel() {

            }

            @Override
            public void onError(FacebookException error) {

            }
        });
    }

    private void goToOrderDetail(int position) {
        Intent intent = new Intent();
        intent.setClass(this, OrderDetailActivity.class);
        ParseObject object = queryResult.get(position);
        intent.putExtra("storeInfo", object.getString("storeInfo"));
        intent.putExtra("note", object.getString("note"));
        startActivity(intent);
    }

    private void setStoreInfo() {
        ParseQuery<ParseObject> query =
                new ParseQuery<>("StoreInfo");
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                String[] stores = new String[objects.size()];
                for (int i = 0; i < stores.length; i++) {
                    ParseObject object = objects.get(i);
                    stores[i] = object.getString("name") + "," +
                            object.getString("address");
                }
                ArrayAdapter<String> storeAdapter =
                        new ArrayAdapter<>(MainActivity.this, android.R.layout.simple_spinner_dropdown_item, stores);
                storeInfoSpinner.setAdapter(storeAdapter);
            }
        });
    }

    private void setHistory() {

        ParseQuery<ParseObject> query = new ParseQuery<>("Order");
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                if (e != null) {
                    Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                    progressBar.setVisibility(View.GONE);
                    return;
                }
                queryResult = objects;
                List<Map<String, String>> data = new ArrayList<>();
                for (int i = 0; i < objects.size(); i++) {
                    ParseObject object = objects.get(i);
                    String note = object.getString("note");
                    String storeInfo = object.getString("storeInfo");
                    JSONArray array = object.getJSONArray("menu");

                    Map<String, String> item = new HashMap<>();
                    item.put("note", note);
                    item.put("drinkNum", "15");
                    item.put("storeInfo", storeInfo);

                    data.add(item);
                }

                String[] from = {"note", "drinkNum", "storeInfo"};
                int[] to = {R.id.note, R.id.drinkNum, R.id.storeInfo};

                SimpleAdapter adapter = new SimpleAdapter(MainActivity.this,
                        data, R.layout.listview_item, from, to);

                historyListView.setAdapter(adapter);
                historyListView.setVisibility(View.VISIBLE);
                progressBar.setVisibility(View.GONE);
            }
        });
    }

    /*
    {
        "note": "this is a note",
        "menu": [...]
    }
    */
    public void submit(View view) {

        progressDialog.setTitle("Loading...");
        progressDialog.show();
        String text = inputText.getText().toString();
        editor.putString("inputText", text);
        editor.commit();

        try {
            JSONObject orderData = new JSONObject();
            if (menuResult == null)
                menuResult = "[]";
            JSONArray array = new JSONArray(menuResult);
            orderData.put("note", text);
            orderData.put("menu", array);
            Utils.writeFile(this, "history.txt", orderData.toString() + "\n");

            ParseObject orderObject = new ParseObject("Order");
            orderObject.put("note", text);
            orderObject.put("storeInfo", storeInfoSpinner.getSelectedItem());
            orderObject.put("menu", array);
            if (hasPhoto == true) {
                Uri uri = Utils.getPhotoUri();
                ParseFile parseFile = new ParseFile("photo.png", Utils.uriToBytes(this, uri));
                orderObject.put("photo", parseFile);
            }
            orderObject.saveInBackground(new SaveCallback() {
                @Override
                public void done(ParseException e) {
                    progressDialog.dismiss();
                    if (e == null) {
                        Toast.makeText(MainActivity.this,
                                "[SaveCallback] ok", Toast.LENGTH_SHORT).show();
                    } else {
                        e.printStackTrace();
                        Toast.makeText(MainActivity.this,
                                "[SaveCallback] fail", Toast.LENGTH_SHORT).show();
                    }
                    setHistory();
                }
            });

        } catch (JSONException e) {
            e.printStackTrace();
        }

        if (hideCheckBox.isChecked()) {
            text = "**********";
            inputText.setText("***********");
        }
    }

    public void goToMenu(View view) {
        Intent intent = new Intent();
        intent.setClass(this, DrinkMenuActivity.class);
        startActivityForResult(intent, REQUEST_CODE_MENU_ACTIVITY);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode,
                                    Intent data) {
        callbackManager.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_MENU_ACTIVITY) {
            if (resultCode == RESULT_OK) {
                menuResult = data.getStringExtra("result");
            }
        } else if (requestCode == REQUEST_TAKE_PHOTO) {
            if (resultCode == RESULT_OK) {
                Uri uri = Utils.getPhotoUri();
                photoImageView.setImageURI(uri);
                hasPhoto = true;
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();
        if (id == R.id.action_take_photo) {
            Toast.makeText(this, "take photo", Toast.LENGTH_SHORT).show();
            goToCamera();
        }

        return super.onOptionsItemSelected(item);
    }

    private void goToCamera() {
        Intent intent = new Intent();
        intent.setAction(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, Utils.getPhotoUri());
        startActivityForResult(intent, REQUEST_TAKE_PHOTO);
    }


}
