package pt.isec.pem.memory;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class HighscoresActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_highscores);

        addRowToTableLayout(getString(R.string.id) + "     ", getString(R.string.user) + "     ", getString(R.string.score));

        MyDBHelper db = new MyDBHelper(getApplicationContext());

        List<Score> scores = db.getScores();

        int i = 1;
        for(Score score : scores){
            addRowToTableLayout(i+"     ",score.getUser(),"     " + score.getScore());
            i++;
        }
        db.closeDB();
    }

    private void addRowToTableLayout(String pos1, String pos2, String pos3){

        TableLayout layout = (TableLayout) findViewById(R.id.tableLayoutHighscores);
        TableRow tbrow0 = new TableRow(this);

        TextView tv0 = new TextView(this);
        tv0.setText(pos1);
        tbrow0.addView(tv0);

        TextView tv1 = new TextView(this);
        tv1.setText(pos2);
        tbrow0.addView(tv1);

        TextView tv2 = new TextView(this);
        tv2.setText(pos3);
        tbrow0.addView(tv2);

        layout.addView(tbrow0);
    }
}
