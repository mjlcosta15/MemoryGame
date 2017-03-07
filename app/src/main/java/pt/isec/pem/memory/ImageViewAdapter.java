package pt.isec.pem.memory;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;

import java.util.ArrayList;

/**
 * Created by ASUS on 04/01/2016.
 */
public class ImageViewAdapter extends BaseAdapter {

    private Context mContext;
    private int numButtons;

    public ImageViewAdapter(Context c, int num){
        mContext = c;
        numButtons = num;
    }

    @Override
    public int getCount() {
        return numButtons;
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ImageView imageView;
        if (convertView == null) {
            // if it's not recycled, initialize some attributes
            imageView = new ImageView(mContext);
            imageView.setAdjustViewBounds(true);
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            imageView.setPadding(3, 3, 3, 3);
        }
        else {
            imageView = (ImageView) convertView;
        }
        imageView.setImageResource(R.drawable.default_img);
        imageView.setId(position);

        return imageView;
    }
}
