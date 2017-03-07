package pt.isec.pem.memory;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SelectLevelActivity extends AppCompatActivity {

    ArrayList<HashMap<String,Object>> levelsTab;
    private String gameMode;

    void addLevelToTab(String imagePath, String level_name) {

        HashMap<String,Object> hm = new HashMap<>();

        hm.put("img_id",imagePath);
        hm.put("level_name",level_name);

        levelsTab.add(hm);
    }

    void fillWithLevels() {

        levelsTab = new ArrayList<>();

        ArrayList<Level> array = getArrayLevels();

        if(array == null) {
            Toast.makeText(this, "Error Loading Levels", Toast.LENGTH_SHORT).show();
        }

        if(gameMode.compareToIgnoreCase("Multiplayer") == 0){
            for (Level lvl : array) {
                if (lvl.isDefaultLevel())
                    addLevelToTab(lvl.getPathImageLevel(), lvl.getLevelName());
            }
        }else {
            for (Level lvl : array) {
                if (lvl.isDefaultLevel())
                    addLevelToTab(lvl.getPathImageLevel(), lvl.getLevelName());
                else
                    addLevelToTab("default_icon", lvl.getLevelName());
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_level);

        gameMode = getIntent().getStringExtra("gameMode");

        fillWithLevels();

        ListView lv = (ListView) findViewById(R.id.SinglePlayerListView);

        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (id<0)
                    return;
                levelSelected((String) levelsTab.get((int)id).get("level_name"));
            }
        });

        String [] data = {"img_id","level_name"};
        int ids[] = { R.id.lvImageViewSinglePlayer,R.id.lvTextViewSinglePlayer};

        MySimpleAdapter sa= new MySimpleAdapter(this,levelsTab,R.layout.levels_listview,data,ids);
        lv.setAdapter(sa);
    }

    private void levelSelected(String level_name){

        Intent intent;

        if(gameMode.compareToIgnoreCase("SinglePlayer") == 0)
            intent = new Intent(this,SinglePlayerActivity.class);
        else if (gameMode.compareToIgnoreCase("twoPlayers") == 0)
            intent = new Intent(this,TwoPlayersActivity.class);
        else {
            intent = new Intent(this, MultiplayerActivity.class);
            intent.putExtra("server", getIntent().getBooleanExtra("server",true));
        }

        intent.putExtra("level_name",level_name);
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

    public class MySimpleAdapter extends SimpleAdapter{

        /**
         * Constructor
         *
         * @param context  The context where the View associated with this SimpleAdapter is running
         * @param data     A List of Maps. Each entry in the List corresponds to one row in the list. The
         *                 Maps contain the data for each row, and should include all the entries specified in
         *                 "from"
         * @param resource Resource identifier of a view layout that defines the views for this list
         *                 item. The layout file should include at least those named views defined in "to"
         * @param from     A list of column names that will be added to the Map associated with each
         *                 item.
         * @param to       The views that should display column in the "from" parameter. These should all be
         *                 TextViews. The first N views in this list are given the values of the first N columns
         */
        public MySimpleAdapter(Context context, List<? extends Map<String, ?>> data, int resource, String[] from, int[] to) {
            super(context, data, resource, from, to);
        }

        @Override
        public void setViewImage(ImageView v, String path){
            if(path.compareToIgnoreCase("default_icon") == 0) {
                v.setImageResource(R.drawable.default_icon);
            }else{
                v.setImageBitmap(
                        DecodeBitmapHelper.decodeSampledBitmapFromResource(path, 100, 100));
            }
        }
    }

}
