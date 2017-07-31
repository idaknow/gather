package part4project.uoa.gather;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ToggleButton;

import java.util.LinkedList;
import java.util.List;

public class FitnessSummaryActivity extends AppCompatActivity {

    private static final String TAG = "Fitness";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fitness_summary);
        ToggleButton toggle = (ToggleButton) findViewById(R.id.toggleButton);
        toggle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                displayData((ToggleButton) v);
            }
        });
        displayData(toggle);
    }

    public void intentMainActivity(View view) {
        startActivity(new Intent(this,MainActivity.class));
    }

    private void displayData(ToggleButton toggle){
        if (toggle.isChecked()){
            Log.d(TAG, "Toggle Button is checked");
            setListViewContent(true);
        } else {
            Log.d(TAG, "Toggle Button is NOT checked");
            setListViewContent(false);
        }
    }

    private void setListViewContent(boolean isSocial){
        ListView view = (ListView) findViewById(R.id.listView);
        List<Data> list;
        if (isSocial) {
            list = MainActivity.fitnessSocial;
        } else {
            list = MainActivity.fitnessGeneral;
        }

        List<String> listToPrint = new LinkedList<>();
        for (int i = 0; i < list.size(); i++) {
            listToPrint.add(list.get(i).getType() + list.get(i).getValue());
        }

        ArrayAdapter adapter = new ArrayAdapter<>(getApplicationContext(), R.layout.activity_listview, listToPrint);
        view.setAdapter(adapter);
    }

}
