package pt.isec.pem.memory;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class SinglePlayerActivity extends AppCompatActivity {

    private Timer myTimer;
    private Handler h = null;
    private AlertDialog backPressedDialog;
    private TextView tvHeader;
    private GridView gridView;
    private ArrayList<String> arrayGridView;
    private int score;
    private boolean canGo;
    private boolean firstFlip;

    private ImageView imgFirstFlipped;
    private String firstCardFlipped;
    private int firstCardFlippedPosition;
    private int pairsDiscovered;

    private boolean doubleClick;
    private boolean doubleClickGo;
    private int pos = -1;
    private int clickTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single_player);

        Intent intent = getIntent();

        String levelName = intent.getStringExtra("level_name");

        backPressedDialog = dialogCreator(0, getString(R.string.dialogMessage1), "");
        score = 0;
        firstFlip = true;
        canGo = false;
        doubleClickGo = false;
        h = new Handler();

        tvHeader = (TextView) findViewById(R.id.spAreaHeader);
        gridView = (GridView) findViewById(R.id.gvSinglePlayer);
        tvHeader.setText(getString(R.string.score) + ": " + score);

        createLevel(levelName);
    }

    private void createLevel(String levelName){

        final Level lvl = getLevelFromFile(levelName);

        if(lvl == null)
            Toast.makeText(this, "Error Loading Level", Toast.LENGTH_SHORT).show();

        // - fill gridView with imageView
        final MyAdapter myAdapter = new MyAdapter(this, lvl);
        gridView.setAdapter(myAdapter);
        gridView.setNumColumns(lvl.getColums());

        //Configura o jogo com as opcoes atuais
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);

        boolean intruders = prefs.getBoolean("pref_intruders", true);

        if(intruders)
            pairsDiscovered = lvl.getnPairs();
        else{
            pairsDiscovered = lvl.getnPairs() - lvl.getIntruders().size();
        }

        int clickMode = Integer.parseInt(prefs.getString("click_mode", "1"));
        clickTime = Integer.parseInt(prefs.getString("click_time", "0"));

        defineClickMode(clickMode, lvl, myAdapter);

        boolean showTable = prefs.getBoolean("pref_show", true);

        if(showTable){

            myAdapter.setFaceUp(true);
            myAdapter.notifyDataSetChanged();

            myTimer = new Timer();
            myTimer.schedule(new TimerTask() {
                @Override
                public void run() {

                    h.post(new Runnable() {
                        @Override
                        public void run() {
                            myAdapter.setFaceUp(false);
                            myAdapter.notifyDataSetChanged();
                        }
                    });

                    canGo = true;

                }
            }, 2000);
        }else
            canGo = true;

    }

    private void defineClickMode(int clickMode, Level lvl, MyAdapter myAdapter){

        switch (clickMode){
            case 1:
                doubleClick = false;
                setOnClick(lvl, myAdapter);
                break;
            case 2:
                setOnLongClick(lvl, myAdapter);
                break;
            case 3:
                doubleClick = true;
                setOnClick(lvl, myAdapter);
                break;
        }
    }

    private void setOnClick(Level level, MyAdapter adapter){

        final MyAdapter myAdapter = adapter;
        final Level lvl = level;

        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {

                if(doubleClick) {
                    if (!doubleClickGo) {
                        pos = position;
                        doubleClickGo = true;

                        if(clickTime > 0) {
                            myTimer = new Timer();
                            myTimer.schedule(new TimerTask() {
                                @Override
                                public void run() { //caso passe o tempo definido para o doubleclick
                                    doubleClickGo = false;
                                }
                            }, clickTime);
                        }

                        return;
                    } else {
                        doubleClickGo = false;
                        if (pos != position)
                            return;
                    }
                }

                if (firstFlip && canGo) {
                    imgFirstFlipped = (ImageView) v;
                    firstCardFlippedPosition = position;
                    firstCardFlipped = myAdapter.getSelectedImagePath(position);
                    ((ImageView) v).setImageBitmap(
                            DecodeBitmapHelper.decodeSampledBitmapFromResource(myAdapter.getSelectedImagePath(position), 100, 100));
                    firstFlip = false;
                } else {
                    if (!canGo)
                        return;
                    else
                        canGo = false;

                    if (firstCardFlippedPosition == position) {
                        canGo = true;
                        return;
                    }

                    ((ImageView) v).setImageBitmap(
                            DecodeBitmapHelper.decodeSampledBitmapFromResource(myAdapter.getSelectedImagePath(position), 100, 100));
                    firstFlip = true;

                    if (myAdapter.getSelectedImagePath(position).compareToIgnoreCase(firstCardFlipped) == 0 &&
                            firstCardFlippedPosition != position) {

                        if (!isIntruder(firstCardFlipped, lvl))
                            score += 5;

                        canGo = true;
                        v.setOnClickListener(null); //dsabilita o click daquela posicao
                        imgFirstFlipped.setOnClickListener(null);
                        tvHeader.setText(getString(R.string.score) + ": " + score);
                        pairsDiscovered--;
                        if (pairsDiscovered == 0) {

                            MyDBHelper db = new MyDBHelper(getApplicationContext());

                            List<Score> scores = db.getScores();

                            if (score > getMax(scores)) { //Novo HighScore
                                dialogCreator(1, getString(R.string.gameOverDialogMsg) + " " + score,
                                        getString(R.string.gameOverDialogHeader)).show();
                            } else
                                dialogCreator(2, getString(R.string.score) + " " + score,
                                        getString(R.string.gameOverDialogHeader)).show();
                        }
                    } else {

                        final View v2 = v;

                        myTimer = new Timer();
                        myTimer.schedule(new TimerTask() {
                            @Override
                            public void run() {

                                h.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        imgFirstFlipped.setImageResource(R.drawable.back);
                                        ((ImageView) v2).setImageResource(R.drawable.back);
                                    }
                                });
                                canGo = true;
                            }
                        }, 1000);

                        score -= 2;
                        if(score < 0)
                            score = 0;
                        tvHeader.setText(getString(R.string.score) + ": " + score);
                    }
                }
            }
        });
    }

    private void setOnLongClick(Level level, MyAdapter adapter){

        final MyAdapter myAdapter = adapter;
        final Level lvl = level;

        gridView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {

                if (firstFlip && canGo) {
                    imgFirstFlipped = (ImageView) view;
                    firstCardFlippedPosition = position;
                    firstCardFlipped = myAdapter.getSelectedImagePath(position);
                    /*Bitmap b = BitmapFactory.decodeFile(firstCardFlipped);
                    ((ImageView) view).setImageBitmap(b);*/
                    ((ImageView) view).setImageBitmap(
                            DecodeBitmapHelper.decodeSampledBitmapFromResource(myAdapter.getSelectedImagePath(position), 100, 100));
                    firstFlip = false;
                } else {
                    if (!canGo)
                        return false;
                    else
                        canGo = false;

                    if (firstCardFlippedPosition == position) {
                        canGo = true;
                        return false;
                    }

                    /*Bitmap b = BitmapFactory.decodeFile(myAdapter.getSelectedImagePath(position));
                    ((ImageView) view).setImageBitmap(b);*/
                    ((ImageView) view).setImageBitmap(
                            DecodeBitmapHelper.decodeSampledBitmapFromResource(myAdapter.getSelectedImagePath(position), 100, 100));

                    firstFlip = true;

                    if (myAdapter.getSelectedImagePath(position).compareToIgnoreCase(firstCardFlipped) == 0 &&
                            firstCardFlippedPosition != position) {

                        if (!isIntruder(firstCardFlipped, lvl))
                            score += 5;

                        canGo = true;
                        view.setOnClickListener(null); //dsabilita o click daquela posicao
                        imgFirstFlipped.setOnClickListener(null);
                        tvHeader.setText(getString(R.string.score) + ": " + score);
                        pairsDiscovered--;
                        if (pairsDiscovered == 0) {

                            MyDBHelper db = new MyDBHelper(getApplicationContext());

                            List<Score> scores = db.getScores();

                            if (score > getMax(scores)) { //Novo HighScore
                                dialogCreator(1, getString(R.string.gameOverDialogMsg) + " " + score,
                                        getString(R.string.gameOverDialogHeader)).show();
                            } else
                                dialogCreator(2, getString(R.string.score) + " " + score,
                                        getString(R.string.gameOverDialogHeader)).show();
                        }
                    } else {

                        score -= 2;
                        if(score < 0)
                            score = 0;

                        final View v2 = view;

                        myTimer = new Timer();
                        myTimer.schedule(new TimerTask() {
                            @Override
                            public void run() {

                                h.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        imgFirstFlipped.setImageResource(R.drawable.back);
                                        ((ImageView) v2).setImageResource(R.drawable.back);
                                    }
                                });
                                canGo = true;
                            }
                        }, 1000);
                        tvHeader.setText(getString(R.string.score) + ": " + score);
                    }
                }
                return true;
            }
        });
    }

    private boolean isIntruder(String pair, Level lvl){
        for ( String str : lvl.getIntruders()){
            if(str.compareToIgnoreCase(pair)==0)
                return true;
        }
        return false;
    }

    public void onBackPressed(){ backPressedDialog.show(); }

    private Level getLevelFromFile(String levelName){

        ArrayList<Level> array;

        try{

            FileInputStream fin = new FileInputStream(getFilesDir() + "/array_levels");
            ObjectInputStream ois = new ObjectInputStream(fin);
            array = (ArrayList<Level>) ois.readObject();
            ois.close();

            for(Level lvl : array)
                if(lvl.getLevelName().compareToIgnoreCase(levelName) == 0)
                    return lvl;

        }catch(Exception ex){
            ex.printStackTrace();
            return null;
        }

        return null;
    }

    private int getMax(List<Score> list){
        int max = Integer.MIN_VALUE;

        for(Score score : list)
            if(score.getScore() > max)
                max = score.getScore();

        return max;
    }

    private AlertDialog dialogCreator(int id, String msg, String title){

        AlertDialog.Builder builder = new AlertDialog.Builder(SinglePlayerActivity.this);
        builder.setCancelable(false);
        builder.setTitle(title);
        builder.setMessage(msg);

        switch (id){
            case 0:
                builder.setPositiveButton(this.getString(R.string.yes), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        SinglePlayerActivity.this.finish();
                    }
                });
                builder.setNegativeButton(this.getString(R.string.no), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

                return builder.create();

            case 1:
                final LayoutInflater inflater = SinglePlayerActivity.this.getLayoutInflater(); // Get the layout inflater

                // Inflate and set the layout for the dialog
                // Pass null as the parent view because its going in the dialog layout
                builder.setView(inflater.inflate(R.layout.high_score_dialog, null))
                        // Add action buttons
                        .setPositiveButton(R.string.submit, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int id) {
                                Dialog f = (Dialog) dialog;
                                EditText uName = (EditText) f.findViewById(R.id.tfUsername);
                                String user = uName.getText().toString();

                                if(user.compareToIgnoreCase("") == 0)
                                    user = "John Doe";

                                MyDBHelper db = new MyDBHelper(getApplicationContext());

                                db.addScore(user,score);
                                db.closeDB();
                                finish();
                            }
                        })
                        .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                                finish();
                            }
                        });

                return builder.create();
            case 2:
                builder.setNeutralButton(this.getString(R.string.ok), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        SinglePlayerActivity.this.finish();
                    }
                });
                return builder.create();
        }
        return null;
    }
}
