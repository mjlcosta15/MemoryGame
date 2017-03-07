package pt.isec.pem.memory;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.util.ArrayList;

public class CreateLevelActivity extends AppCompatActivity {

    private SeekBar seekBar;
    private SeekBar seekBarIntruders;
    private TextView textViewSeekBar;
    private TextView textViewSeekBarIntruders;
    private Switch intruders;
    private LinearLayout layout;
    private int fillDefault;
    private EditText editText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_level);

        editText = (EditText) findViewById(R.id.editTextLevelName);
        seekBar = (SeekBar) findViewById(R.id.seekBar);
        seekBarIntruders = (SeekBar) findViewById(R.id.seekBar2);
        textViewSeekBar = (TextView) findViewById(R.id.textView1);
        textViewSeekBarIntruders = (TextView) findViewById(R.id.textView2);
        intruders = (Switch) findViewById(R.id.switch1);
        layout = (LinearLayout) findViewById(R.id.group);

        fillDefault = 2;
        seekBar.setProgress(fillDefault);
        seekBarIntruders.setProgress(fillDefault);
        textViewSeekBar.setText(getString(R.string.tv1CreateLevel) + " " + seekBar.getProgress() + "/" + seekBar.getMax());
        textViewSeekBarIntruders.setText(getString(R.string.checkbox1title) + " " + seekBarIntruders.getProgress() + "/" + seekBar.getMax());

        layout.setVisibility(View.GONE);

        defineListeners();
    }

    private void defineListeners() {

        intruders.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    layout.setVisibility(View.VISIBLE);
                } else {
                    layout.setVisibility(View.GONE);
                }
            }
        });

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            int progressValue = 2;

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                progressValue = progress;

                if (progress < fillDefault) {
                    seekBar.setProgress(fillDefault);
                }
                textViewSeekBar.setText(getString(R.string.tv1CreateLevel) + " " + progressValue + "/" + seekBar.getMax());
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        seekBarIntruders.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            int progressValue = 2;

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                progressValue = progress;

                if (progress < fillDefault) {
                    seekBarIntruders.setProgress(fillDefault);
                }
                textViewSeekBarIntruders.setText(getString(R.string.checkbox1title) + " " + progressValue + "/" + seekBar.getMax());
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
    }

    public void onButtonProceed(View v) {

        String levelName = editText.getText().toString();

        if(levelName.compareToIgnoreCase("") == 0) {
            Toast.makeText(this, getString(R.string.toastEmptyTextName), Toast.LENGTH_SHORT).show();
            return;
        }

        if((seekBar.getProgress() + seekBarIntruders.getProgress() >= 17) && intruders.isChecked()){
            Toast.makeText(this, getString(R.string.toast15Pairs), Toast.LENGTH_SHORT).show();
            return;
        }

        ArrayList<Level> levels = getArrayLevels();

        for(Level lvl : levels){
            if(lvl.getLevelName().compareToIgnoreCase(levelName) == 0){
                Toast.makeText(this, getString(R.string.toastNameInUse), Toast.LENGTH_SHORT).show();
                return;
            }
        }

        int numIntruders = 0;
        if (intruders.isChecked())
            numIntruders = seekBarIntruders.getProgress();

        Intent intent = new Intent(this, MakeLevelActivity.class);
        intent.putExtra("level_name", levelName);
        intent.putExtra("num_pairs", seekBar.getProgress());
        intent.putExtra("num_intruders", numIntruders);
        startActivity(intent);
    }

    private ArrayList<Level> getArrayLevels(){

        ArrayList<Level> array;

        try{

            FileInputStream fin = new FileInputStream(getFilesDir() + "/array_levels");
            ObjectInputStream ois = new ObjectInputStream(fin);
            array = (ArrayList<Level>) ois.readObject();
            ois.close();

            return array;

        }catch(Exception ex){
            ex.printStackTrace();
            return null;
        }
    }
}
