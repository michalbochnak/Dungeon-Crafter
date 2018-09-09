package edu.uic.cs440.group1.dungeon_crafter.Activities;


import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import edu.uic.cs440.group1.dungeon_crafter.R;

public class GameOverActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_over);

        DisplayMetrics ds = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(ds);

        int width = ds.widthPixels;
        int heigth = ds.heightPixels;

        getWindow().setLayout((int)(width*.6), (int)(heigth*.6));

        TextView textView = (TextView) findViewById(R.id.gameOverTxtBox);

        Intent i = getIntent();

        textView.setText(i.getStringExtra("msg"));

        Button ok = (Button) findViewById(R.id.okButton);

        ok.setOnClickListener(displayStatistics);


    }


    private View.OnClickListener displayStatistics = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Intent intent = getIntent();
            setResult(RESULT_OK);
            finish();


        }
    };
}
