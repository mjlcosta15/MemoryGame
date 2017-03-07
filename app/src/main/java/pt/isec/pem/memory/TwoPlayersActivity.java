package pt.isec.pem.memory;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

public class TwoPlayersActivity extends AppCompatActivity {

    private AlertDialog backPressedDialog;
    private TextView tvScore1,tvScore2;
    private int score1, score2;
    private GridView gridView;
    private boolean canGo;
    private boolean firstFlip;
    private boolean playerOneTurn;
    private ImageView imgFirstFlipped;
    private String firstCardFlipped;
    private int firstCardFlippedPosition;
    private int pairsDiscovered;
    private Timer myTimer;
    private Handler h = null;

    private boolean doubleClick;
    private boolean doubleClickGo;
    private int pos = -1;
    private int clickTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_two_players);

        Intent intent = getIntent();

        String levelName = intent.getStringExtra("level_name");

        backPressedDialog = dialogCreator(0,getString(R.string.dialogMessage1),"");

        score1 = score2 = 0;
        playerOneTurn = true;
        firstFlip = true;
        canGo = false;
        h = new Handler();

        gridView = (GridView) findViewById(R.id.gvTwoPlayers);
        tvScore1 = (TextView) findViewById(R.id.tpScore1);
        tvScore2 = (TextView) findViewById(R.id.tpScore2);

        tvScore1.setText(getString(R.string.player1) + ": " + score1);
        tvScore2.setText(getString(R.string.player2) + ": " + score2);

        createLevel(levelName);

    }

    private void createLevel(String levelName){

        final Level lvl = getLevelFromFile(levelName);

        if(lvl == null) {
            Toast.makeText(this, "Error Loading Level", Toast.LENGTH_SHORT).show();
            finish();
        }

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

    private void setOnClick(Level level, MyAdapter adapter) {

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
                            DecodeBitmapHelper.decodeSampledBitmapFromResource(firstCardFlipped, 100, 100));
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

                        if (!isIntruder(firstCardFlipped, lvl)) {
                            if (playerOneTurn) {
                                score1++;
                                tvScore1.setText(getString(R.string.player1) + ": " + score1);
                            } else {
                                score2++;
                                tvScore2.setText(getString(R.string.player2) + ": " + score2);
                            }
                        }

                        canGo = true;
                        v.setOnClickListener(null); //dsabilita o click daquela posicao

                        imgFirstFlipped.setOnClickListener(null);
                        pairsDiscovered--;
                        if (pairsDiscovered == 0) { //FIM DE JOGO

                            if (score1 > score2)
                                dialogCreator(1, getString(R.string.player1Won) + ": " + score1,
                                        getString(R.string.gameOverDialogHeader)).show();
                            else if (score1 < score2)
                                dialogCreator(1, getString(R.string.player2Won) + ": " + score2,
                                        getString(R.string.gameOverDialogHeader)).show();
                            else
                                dialogCreator(1, getString(R.string.draw),
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

                        if (playerOneTurn) {
                            playerOneTurn = false;
                            Toast.makeText(TwoPlayersActivity.this, getString(R.string.player2Turns), Toast.LENGTH_SHORT).show();
                        } else {
                            playerOneTurn = true;
                            Toast.makeText(TwoPlayersActivity.this, getString(R.string.player1Turns), Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            }
        });
    }

    private void setOnLongClick(Level level, MyAdapter adapter) {

        final MyAdapter myAdapter = adapter;
        final Level lvl = level;

        gridView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {

                if (firstFlip && canGo) {
                    imgFirstFlipped = (ImageView) view;
                    firstCardFlippedPosition = position;
                    firstCardFlipped = myAdapter.getSelectedImagePath(position);

                    ((ImageView) view).setImageBitmap(
                            DecodeBitmapHelper.decodeSampledBitmapFromResource(firstCardFlipped, 100, 100));
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

                    ((ImageView) view).setImageBitmap(
                            DecodeBitmapHelper.decodeSampledBitmapFromResource(myAdapter.getSelectedImagePath(position), 100, 100));

                    firstFlip = true;

                    if (myAdapter.getSelectedImagePath(position).compareToIgnoreCase(firstCardFlipped) == 0 &&
                            firstCardFlippedPosition != position) {

                        if (!isIntruder(firstCardFlipped, lvl)) {
                            if (playerOneTurn) {
                                score1++;
                                tvScore1.setText(getString(R.string.player1) + ": " + score1);
                            } else {
                                score2++;
                                tvScore2.setText(getString(R.string.player2) + ": " + score2);
                            }
                        }

                        canGo = true;
                        view.setOnClickListener(null); //dsabilita o click daquela posicao
                        imgFirstFlipped.setOnClickListener(null);
                        pairsDiscovered--;
                        if (pairsDiscovered == 0) { //FIM DE JOGO

                            if (score1 > score2)
                                dialogCreator(1, getString(R.string.player1Won) + ": " + score1,
                                        getString(R.string.gameOverDialogHeader)).show();
                            else if (score1 < score2)
                                dialogCreator(1, getString(R.string.player2Won) + ": " + score2,
                                        getString(R.string.gameOverDialogHeader)).show();
                            else
                                dialogCreator(1, getString(R.string.draw),
                                        getString(R.string.gameOverDialogHeader)).show();
                        }
                    } else {

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

                        if (playerOneTurn) {
                            playerOneTurn = false;
                            Toast.makeText(TwoPlayersActivity.this, getString(R.string.player2Turns), Toast.LENGTH_SHORT).show();
                        } else {
                            playerOneTurn = true;
                            Toast.makeText(TwoPlayersActivity.this, getString(R.string.player1Turns), Toast.LENGTH_SHORT).show();
                        }
                    }
                }
                return true;
            }
        });
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

    private boolean isIntruder(String pair, Level lvl){
        for ( String str : lvl.getIntruders()){
            if(str.compareToIgnoreCase(pair)==0)
                return true;
        }
        return false;
    }

    private AlertDialog dialogCreator(int id, String msg, String title){

        AlertDialog.Builder builder = new AlertDialog.Builder(TwoPlayersActivity.this);
        builder.setCancelable(false);
        builder.setTitle(title);
        builder.setMessage(msg);

        switch (id){
            case 0:
                builder.setPositiveButton(this.getString(R.string.yes), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        TwoPlayersActivity.this.finish();
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
                builder.setNeutralButton(this.getString(R.string.ok), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        TwoPlayersActivity.this.finish();
                    }
                });
                return builder.create();
        }
        return null;
    }
}
