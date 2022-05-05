package com.example.creditcardlimit;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.Manifest;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import androidx.biometric.BiometricPrompt;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.Executor;

public class MainActivity extends AppCompatActivity {
    private static final String[] CAMERA_PERMISSION = new String[]{Manifest.permission.CAMERA};
    private static final int CAMERA_REQUEST_CODE = 10;

    private float credit = 10000;
    private Executor executor;
    private BiometricPrompt biometricPrompt;
    private BiometricPrompt.PromptInfo promptInfo;
    Toolbar toolbar;
    EditText amountEntered;
    TextView remainingCreditText;
    SharedPreferences preferences;
    List<String> cards;
    Spinner cardsSpinner;

    private int openCount;
    List<String> history;
    private static final String CARDS_TABLE_NAME = "Cards";
    private static final String HISTORY_TABLE_NAME = "History";
    DatabaseHelper databaseHelper;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        amountEntered = findViewById(R.id.amountEntered);
        Button submitButton = findViewById(R.id.submitButton);
        remainingCreditText = findViewById(R.id.remainingCreditText);
        FloatingActionButton floatingActionButton = findViewById(R.id.floatingActionButton);

        cardsSpinner = (Spinner) findViewById(R.id.cardsSpinner);

        databaseHelper = new DatabaseHelper(this);
        Button enableCamera = findViewById(R.id.camera);
        enableCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (hasCameraPermission()) {
                    enableCamera();
                } else {
                    requestPermission();
                }
            }
        });

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        history= new ArrayList<String>();



        switch (getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK) {
            case Configuration.UI_MODE_NIGHT_YES:
                toolbar.setTitleTextColor(Color.BLACK);
                floatingActionButton.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor(("#A5D6A7"))));
                floatingActionButton.setColorFilter(Color.BLACK);
                break;
            case Configuration.UI_MODE_NIGHT_NO:
                toolbar.setTitleTextColor(Color.WHITE);
                floatingActionButton.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor(("#1B5E20"))));
                floatingActionButton.setColorFilter(Color.WHITE);
                break;
        }









        preferences = PreferenceManager.getDefaultSharedPreferences(this);
        openCount = preferences.getInt("openCount",0);



        Calendar currentDateAndTime = Calendar.getInstance();

        if (currentDateAndTime.get(Calendar.DAY_OF_MONTH) == 20 && credit != 10000){
            credit = 10000;
            Cursor cardsData = databaseHelper.getCardsData();

            while (cardsData.moveToNext()){

                credit = Float.parseFloat(cardsData.getString(2));

            }
        }
        cards = new ArrayList<String>();
        Cursor dataCards = databaseHelper.getCardsData();
        while (dataCards.moveToNext()){
            cards.add(dataCards.getString(1));
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, cards);

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        cardsSpinner.setAdapter(adapter);

        cardsSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String currentCard = cardsSpinner.getSelectedItem().toString();
                Cursor cardsData = databaseHelper.getCardsData();

                while (cardsData.moveToNext()){
                    if (cardsData.getString(1).equals(currentCard)){
                        credit = Float.parseFloat(cardsData.getString(2));
                        remainingCreditText.setText("Remaining credit is: " + credit + " EGP");
                    }

                }
                Cursor dataHistory = databaseHelper.getHistoryData();
                history.clear();
                while (dataHistory.moveToNext()){
                    if (dataHistory.getString(0).equals(currentCard))
                        history.add(0,dataHistory.getString(1));
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });




        remainingCreditText.setText("Remaining credit is: " + credit + " EGP");
        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                amountHandling();
                
            }
        });
        amountEntered.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                amountHandling();
                return true;
            }
        });

        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        LayoutInflater inflater = MainActivity.this.getLayoutInflater();
        builder.setTitle("Add Card");

        builder.setView(inflater.inflate(R.layout.add_dialog,null))
                .setPositiveButton("Done", null);
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Dialog dialogView = (Dialog) dialog;
                EditText cardNum = dialogView.findViewById(R.id.editTextNumber);
                cardNum.setText("");
                dialog.dismiss();
            }
        });

        AlertDialog alertDialog = builder.create();

        alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                Button positveButton = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
                positveButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Dialog dialogView = (Dialog) dialog;
                        EditText cardNum = dialogView.findViewById(R.id.editTextNumber);
                        if (cardNum.getText().length() > 4 | cardNum.getText().length() < 4)
                            Toast.makeText(MainActivity.this,"Must enter 4 digits",Toast.LENGTH_SHORT).show();
                        else if (cards.contains(cardNum.getText().toString())) {
                            Toast.makeText(MainActivity.this,"Card already exists",Toast.LENGTH_SHORT).show();
                        }else{
                            //cards.add(cardNum.getText().toString());
                            databaseHelper.addData(cardNum.getText().toString(),"",CARDS_TABLE_NAME);
                            cards.clear();
                            Cursor dataCards = databaseHelper.getCardsData();
                            while (dataCards.moveToNext()){
                                cards.add(dataCards.getString(1));
                            }
                            dialog.dismiss();
                            Toast.makeText(MainActivity.this,"Card Added",Toast.LENGTH_SHORT).show();
                            adapter.notifyDataSetChanged();
                        }
                        cardNum.setText("");
                    }
                });
            }
        });


        if (openCount == 0){


            alertDialog.show();
            openCount += 1;
            SharedPreferences.Editor editor = preferences.edit();
            editor.putInt("openCount", openCount);
            editor.apply();

        }else {
            String currentCard = cardsSpinner.getSelectedItem().toString();
            Cursor cardsData = databaseHelper.getCardsData();
            Cursor dataHistory = databaseHelper.getHistoryData();
            while (dataHistory.moveToNext()){
                if (dataHistory.getString(0).equals(cardsSpinner.getSelectedItem().toString()))
                    history.add(0,dataHistory.getString(1));
            }
            while (cardsData.moveToNext()){
                if (cardsData.getString(1).equals(currentCard)){
                    credit = Float.parseFloat(cardsData.getString(2));
                    remainingCreditText.setText("Remaining credit is: " + credit + " EGP");
                }

            }
        }




        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                alertDialog.show();


            }
        });




    }
//#############  BIOMETRIC PROMPT  ########################################
//        executor = ContextCompat.getMainExecutor(this);
//        biometricPrompt = new BiometricPrompt(MainActivity.this,
//                executor, new BiometricPrompt.AuthenticationCallback() {
//            @Override
//            public void onAuthenticationError(int errorCode,
//                                              @NonNull CharSequence errString) {
//                super.onAuthenticationError(errorCode, errString);
//                Toast.makeText(getApplicationContext(),
//                        "Authentication error: " + errString, Toast.LENGTH_SHORT)
//                        .show();
//                if (errorCode == BiometricPrompt.ERROR_USER_CANCELED)
//                    biometricPrompt.authenticate(promptInfo);
//            }
//
//            @Override
//            public void onAuthenticationSucceeded(
//                    @NonNull BiometricPrompt.AuthenticationResult result) {
//                super.onAuthenticationSucceeded(result);
//                Toast.makeText(getApplicationContext(),
//                        "Authentication succeeded!", Toast.LENGTH_SHORT).show();
//            }
//
//            @Override
//            public void onAuthenticationFailed() {
//                super.onAuthenticationFailed();
//                Toast.makeText(getApplicationContext(), "Authentication failed",
//                        Toast.LENGTH_SHORT)
//                        .show();
//            }
//        });
//
//        promptInfo = new BiometricPrompt.PromptInfo.Builder()
//                .setTitle("Biometric login for my app")
//                .setSubtitle("Log in using your biometric credential")
//                .setNegativeButtonText("Use account password")
//                .build();
//        biometricPrompt.authenticate(promptInfo);



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            Cursor dataHistory = databaseHelper.getHistoryData();
            history.clear();
            while (dataHistory.moveToNext()){
                if (dataHistory.getString(0).equals(cardsSpinner.getSelectedItem().toString()))
                    history.add(0,dataHistory.getString(1));
            }
            Intent intent = new Intent(MainActivity.this,HistoryActivity.class);
            intent.putStringArrayListExtra("history", (ArrayList<String>) history);
            startActivity(intent);


            return true;

    }
    public void amountHandling(){
        try {
            if (cards.isEmpty())
                Snackbar.make(findViewById(R.id.ConstraintLayout), "Must add a card first. press '+' to add a card", Snackbar.LENGTH_LONG).show();
            else{

                credit -= Float.parseFloat(String.valueOf(amountEntered.getText()));
                databaseHelper.updateCardsTable(cardsSpinner.getSelectedItem().toString(),String.valueOf(credit));
                remainingCreditText.setText("Remaining credit is: " + credit + " EGP");
                databaseHelper.addData("- " + amountEntered.getText().toString(),cardsSpinner.getSelectedItem().toString(),HISTORY_TABLE_NAME);



            }

        }
        catch (Exception e){
            Toast.makeText(getApplicationContext(),"Must enter a number",Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
        finally {
            amountEntered.requestFocus();
            amountEntered.selectAll();
            InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(MainActivity.INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(amountEntered.getWindowToken(),0);
        }
    }
    private boolean hasCameraPermission() {
        return ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED;
    }
    private void requestPermission() {
        ActivityCompat.requestPermissions(
                this,
                CAMERA_PERMISSION,
                CAMERA_REQUEST_CODE
        );
        ActivityCompat.requestPermissions(
                this,new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},1024);
    }
    private void enableCamera() {
        Intent intent = new Intent(this, CameraActivity.class);
        startActivity(intent);
    }


}