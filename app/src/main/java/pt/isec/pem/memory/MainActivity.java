package pt.isec.pem.memory;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import java.io.File;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {

    private SharedPreferences sharedPreferences;

    private Timer myTimer;
    private Button singlePlayerButton;
    private Button twoPlayersButton;
    private Button multiplayerButton;
    private ImageButton optionsButton;
    private ImageButton trophiesButton;
    private boolean quit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        quit = false;

        //Ligacao entre a aplicacao e o layout
        singlePlayerButton = (Button) findViewById(R.id.singlePlayerButton);
        twoPlayersButton = (Button) findViewById(R.id.twoPlayersButton);
        multiplayerButton = (Button) findViewById(R.id.multiplayerButton);
        optionsButton = (ImageButton) findViewById(R.id.optionsButton);
        trophiesButton = (ImageButton) findViewById(R.id.trophiesButton);

        optionsButton.setImageResource(R.drawable.ic_settings);
        trophiesButton.setImageResource(R.drawable.ic_trophy);

        sharedPreferences = getPreferences(MODE_PRIVATE);

        if (sharedPreferences.getBoolean("firstrun", true)) { //First run load images to localStorage

            List<Level> arrayLevels = new ArrayList<>();
            File myDir = this.getFilesDir();
            File levelsFolder = new File(myDir, "levels");
            levelsFolder.mkdir();

            try {

                Level lvl;
                File folder;
                File intrudersFolder;
                List<String> myLevels = new ArrayList<>();
                List<String> levelPictures;

                //Tive que criar este array, porque haviam pastas ocultas
                myLevels.add("Animais");
                myLevels.add("Frutos");
                myLevels.add("Formas");
                myLevels.add("Futebol");
                myLevels.add("Barbas");
                myLevels.add("Caracteres Chineses");

                for (String level : myLevels) {

                    lvl = new Level(level, true);

                    folder = new File(levelsFolder, level);
                    folder.mkdir();

                    intrudersFolder = new File(myDir, level + "/intruders");
                    boolean ups = intrudersFolder.mkdirs();

                    ups = intrudersFolder.exists();

                    levelPictures = Arrays.asList(getAssets().list(level));

                    for (String picture : levelPictures) {
                        if (picture.compareToIgnoreCase("icon.png") == 0)
                            continue;
                        copyFileToLocalStorage(picture, level, folder, lvl, false);
                    }

                    //CRIA INTRUDERS
                    levelPictures = Arrays.asList(getAssets().list(level + "/intruders"));

                    for (String picture : levelPictures) {
                        copyFileToLocalStorage(picture, level + "/intruders", intrudersFolder, lvl, true);
                    }

                    lvl.setColums(4);
                    lvl.setnPairs(lvl.getImagesPath().size() + lvl.getIntruders().size());

                    copyLevelIcon(level, lvl);
                    arrayLevels.add(lvl);
                }

                createFileFromLevelsArrayObject(arrayLevels);

            } catch (IOException e) {
                e.printStackTrace();
            }

            sharedPreferences.edit().putBoolean("firstrun", false).commit();
        }
    }

    public void onBackPressed(){ //Sai da aplicacao evitando que va para  a activity anterior necessario dois cliques

        if(!quit){
            quit = true;

            myTimer = new Timer();
            myTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    quit = false;
                }
            }, 1000);

            Toast.makeText(this, getString(R.string.toastQuit), Toast.LENGTH_SHORT).show();

            return;
        }

        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    public void onSinglePlayerClick(View v) {

        Intent intent;
        intent = new Intent(this,SelectLevelActivity.class);
        intent.putExtra("gameMode", "SinglePlayer");
        startActivity(intent);
    }

    public void onTwoPlayersClick(View v) {

        Intent intent;
        intent = new Intent(this,SelectLevelActivity.class);
        intent.putExtra("gameMode", "twoPlayers");
        startActivity(intent);
    }

    public void onMultiplayerClick(View v){

        Intent intent;
        intent = new Intent(this,MultiplayerChooserActivity.class);
        startActivity(intent);
    }

    public void onHighscoresClick(View v){
        Intent intent;
        intent = new Intent(this,HighscoresActivity.class);
        startActivity(intent);
    }

    public void onSettings( View v){
        Intent intent = new Intent(this,SettingsActivity.class);
        startActivity(intent);
    }

    public void onCreateLevel(View v){
        Intent intent = new Intent(this,CreateLevelActivity.class);
        startActivity(intent);
    }

    private void copyFileToLocalStorage(String fileToCopy, String level, File destFolder, Level lvl, boolean intruders){

        try {

            File newFile = new File(destFolder,fileToCopy);

            InputStream myInput = this.getAssets().open(level+"/" + fileToCopy);
            OutputStream output = new FileOutputStream(newFile);

            byte[] buf = new byte[1024];
            int bytesRead;

            while ((bytesRead = myInput.read(buf)) > 0) {
                output.write(buf, 0, bytesRead);
            }

            if(intruders)
                lvl.getIntruders().add(newFile.getAbsolutePath());
            else
                lvl.getImagesPath().add(newFile.getAbsolutePath());
        }
        catch (FileNotFoundException e) {
            e.printStackTrace();
        }catch ( IOException e){
            e.printStackTrace();
        }
    }

    private void copyLevelIcon(String level, Level lvl){

        try {

            File newFile = new File(getFilesDir() + "/levels/" + level,"icon");
            InputStream myInput = this.getAssets().open(level + "/icon.png");

            OutputStream output = new FileOutputStream(newFile);

            byte[] buf = new byte[1024];
            int bytesRead;

            while ((bytesRead = myInput.read(buf)) > 0) {
                output.write(buf, 0, bytesRead);
            }

            lvl.setPathImageLevel(newFile.getAbsolutePath());

            myInput.close();
            output.close();
        }
        catch (FileNotFoundException e) {
            e.printStackTrace();
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    private void createFileFromLevelsArrayObject(List<Level> array){

        try{
            FileOutputStream fout = new FileOutputStream(getFilesDir() + "/array_levels");
            ObjectOutputStream oos = new ObjectOutputStream(fout);
            oos.writeObject(array);
            oos.close();

        }catch(Exception ex){
            ex.printStackTrace();
        }
    }


}
