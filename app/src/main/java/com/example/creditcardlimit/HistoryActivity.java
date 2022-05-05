package com.example.creditcardlimit;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Parcelable;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.List;

public class HistoryActivity extends AppCompatActivity {
    Toolbar toolbar;
    ListView listView;

    //String[] history = {"1","2","3","4","5","6","7","8","9","10","1","2","3","4","5","6","7","8","9","10","1","2","3","4","5","6","7","8","9","10","1","2","3","4","5","6","7","8","9","10","1","2","3","4","5","6","7","8","9","10"};
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        Bundle extras = getIntent().getExtras();
        List<String> history = extras.getStringArrayList("history");

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        switch (getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK) {
            case Configuration.UI_MODE_NIGHT_YES:
                toolbar.setTitleTextColor(Color.BLACK);
                break;
            case Configuration.UI_MODE_NIGHT_NO:
                toolbar.setTitleTextColor(Color.WHITE);
                break;
        }


        listView = findViewById(R.id.listView);
        ArrayAdapter<String> arr;
        arr = new ArrayAdapter<String>(this,R.layout.support_simple_spinner_dropdown_item,history);
        listView.setAdapter(arr);
        arr.notifyDataSetChanged();
    }
}