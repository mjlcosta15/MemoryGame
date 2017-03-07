package pt.isec.pem.memory;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

public class MakeLevelActivity extends AppCompatActivity {

    private Intent intent;
    private GridView gvPairs;
    private GridView gvIntruders;
    private ImageView v;

    private ArrayList<String> mImagePaths;
    private ArrayList<String> mIntrudersPaths;

    private String levelName;
    private int numIntruders;
    private int numPairs;
    private int totalImgs;
    private boolean allOk;


    private String selectedImagePath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_make_level);

        intent = getIntent();
        levelName = intent.getStringExtra("level_name");
        numPairs = intent.getIntExtra("num_pairs", 10);
        numIntruders = intent.getIntExtra("num_intruders", 0);
        totalImgs = 0;

        mImagePaths = new ArrayList<>();
        mIntrudersPaths = new ArrayList<>();
        allOk = true;

        createView();

        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1){

            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
                requestPermissions(new String[]{ Manifest.permission.READ_EXTERNAL_STORAGE }, 1);
        }



        setClickOnGridVew();
    }

    private void createView(){

        gvPairs = (GridView) findViewById(R.id.gridViewCards);
        gvIntruders = (GridView) findViewById(R.id.gridViewIntruders);

        gvPairs.setAdapter(new ImageViewAdapter(this, numPairs));

        if(numIntruders != 0){
            gvIntruders.setAdapter(new ImageViewAdapter(this,numIntruders));
        }else{
            findViewById(R.id.textViewIntruders).setVisibility(View.GONE);
            gvIntruders.setVisibility(View.GONE);
        }
    }

    private void setClickOnGridVew() {
        gvPairs.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_PICK);
                startActivityForResult(intent, 1);
                v = (ImageView) view;

            }
        });

        gvIntruders.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_PICK);
                startActivityForResult(intent, 2);
                v = (ImageView) view;
            }
        });
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (resultCode == RESULT_OK) {
            if (requestCode == 1) {

                Uri selectedImageUri = data.getData();

                selectedImagePath = getRealPathFromURI(selectedImageUri);

                v.setImageBitmap(
                        DecodeBitmapHelper.decodeSampledBitmapFromResource(selectedImagePath, 100, 100));

                //v.setImageURI(selectedImageUri);

                if (!mImagePaths.contains(selectedImagePath)) {
                    mImagePaths.add(selectedImagePath);
                    totalImgs++;
                }

            } else if (requestCode == 2) {

                Uri selectedImageUri = data.getData();

                selectedImagePath = getRealPathFromURI(selectedImageUri);

                v.setImageBitmap(
                        DecodeBitmapHelper.decodeSampledBitmapFromResource(selectedImagePath, 100, 100));
                //v.setImageURI(selectedImageUri);

                if (!mIntrudersPaths.contains(selectedImagePath)) {
                    mIntrudersPaths.add(selectedImagePath);
                    totalImgs++;
                }
            }
        }
    }

    private String getRealPathFromURI(Uri contentUri){

        String path = null;

        if(contentUri != null){
            Cursor c = getContentResolver().query(contentUri,
                    new String[]{android.provider.MediaStore.Images.ImageColumns.DATA},
                    null, null, null);

            c.moveToFirst();
            path = c.getString(0);
            c.close();
        }
        return path;
    }


    public void onMakingLevel(View v) {

        if( totalImgs < (numPairs + numIntruders)){
            Toast.makeText(this, getString(R.string.toastFewImgs), Toast.LENGTH_SHORT).show();
            return;
        }

        Level lvl = new Level(levelName,false);
        File myDir = this.getFilesDir();
        File folder = new File(myDir, "levels\\" + levelName);
        folder.mkdir();

        for( int i = 0; i < mImagePaths.size(); i++){
            copyFileToLocalStorage("img" + i, mImagePaths.get(i), folder, lvl, false);
        }

        if(numIntruders != 0){
            File intrudersFolder = new File(myDir, "levels\\" + levelName + "\\intruders");
            intrudersFolder.mkdirs();

            for( int i = 0; i < mIntrudersPaths.size(); i++){
                copyFileToLocalStorage("img" + i, mIntrudersPaths.get(i), intrudersFolder, lvl, true);
            }
        }

        lvl.setColums(4);
        lvl.setnPairs(lvl.getImagesPath().size() + lvl.getIntruders().size());

        ArrayList<Level> levels = getArrayLevels();

        levels.add(lvl);
        if(allOk)
            writeFileFromLevelsArrayObject(levels);
        else
            Toast.makeText(MakeLevelActivity.this, getString(R.string.toastPermissionsDenied), Toast.LENGTH_SHORT).show();

        Intent intent;
        intent = new Intent(this,MainActivity.class);
        startActivity(intent);
    }

    private void copyFileToLocalStorage(String fileToCopy, String from, File destFolder, Level lvl, boolean intruders){

        try {

            File newFile = new File(destFolder,fileToCopy);
            File fromFile = new File(from);

            InputStream myInput = new FileInputStream(fromFile);
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

        }catch (FileNotFoundException e) {
            allOk = false;
            e.printStackTrace();
        }catch (IOException e){
            allOk = false;
            e.printStackTrace();
        }
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

    private void writeFileFromLevelsArrayObject(List<Level> array){

        try{
            FileOutputStream fout = new FileOutputStream(getFilesDir() + "/array_levels");
            ObjectOutputStream oos = new ObjectOutputStream(fout);
            oos.writeObject(array);
            oos.close();

        }catch(Exception ex){
            ex.printStackTrace();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults)
    {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

}
