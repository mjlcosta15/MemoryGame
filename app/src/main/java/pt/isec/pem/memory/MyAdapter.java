package pt.isec.pem.memory;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collections;

public class MyAdapter extends BaseAdapter{

    private Context mContext;
    private ArrayList<String> mImagePaths;
    private ArrayList<ImageView> imgViews;
    private boolean faceUp;
    private boolean pos0 = false;

    public MyAdapter(Context c, Level lvl) {

        faceUp = false;
        mContext = c;
        mImagePaths = new ArrayList<>();
        imgViews = new ArrayList<>();

        for(String str : lvl.getImagesPath()) {
            mImagePaths.add(str);
            mImagePaths.add(str);
            imgViews.add(null);
            imgViews.add(null);
        }

        //Ver se os pares intrusos estao ativados
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(c);

        boolean intruders = prefs.getBoolean("pref_intruders", true);

        if(intruders) {
            for (String str : lvl.getIntruders()) {
                mImagePaths.add(str);
                mImagePaths.add(str);
                imgViews.add(null);
                imgViews.add(null);
            }
        }

        Collections.shuffle(mImagePaths);
    }

    public ArrayList<String> getmImagePaths(){
        return mImagePaths;
    }

    public int getCount() {
        return mImagePaths.size();
    }

    public Object getItem(int position) {
        return imgViews.get(position);
    }

    public long getItemId(int position) {
        return 0;
    }

    public String getSelectedImagePath(int position){
        return mImagePaths.get(position);
    }

    // create a new ImageView for each item referenced by the Adapter
    public View getView(int position, View convertView, ViewGroup parent) {

        ImageView imageView;
        if (convertView == null) {
            // if it's not recycled, initialize some attributes
            imageView = new ImageView(mContext);
            imageView.setAdjustViewBounds(true);
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            imageView.setPadding(3,3, 3,3);
        } else {
            imageView = (ImageView) convertView;
        }

        if(!(pos0 && position == 0)) {
            imgViews.set(position, imageView);
            pos0 = true;
        }

        if(faceUp){
            imageView.setImageBitmap(
                    DecodeBitmapHelper.decodeSampledBitmapFromResource(mImagePaths.get(position), 100, 100));
        } else
            imageView.setImageResource(R.drawable.back);

        return imageView;
    }

    public boolean isFaceUp() {
        return faceUp;
    }

    public void setFaceUp(boolean faceUp) {
        this.faceUp = faceUp;
    }

    public void setmImagePaths(ArrayList<String> mImagePaths) {
        this.mImagePaths = mImagePaths;
    }
}
