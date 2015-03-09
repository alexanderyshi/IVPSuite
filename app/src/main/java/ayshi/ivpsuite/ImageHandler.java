package ayshi.ivpsuite;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by alexa_000 on 2015-03-08.
 */
public class ImageHandler extends android.app.Fragment implements View.OnClickListener{
    static String ARG_ITEM_ID;
    ImageView imagePreview;
    File imageSourceFile;
    String imageSourcePath;
    Bitmap imageSource;

    //TODO: method to save current Bitmap in ItemListActivity instance into a non-volatile file

    public ImageHandler(){};

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.e(ARG_ITEM_ID, "onCreate");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.e(ARG_ITEM_ID, "onCreateView");
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onClick(View v) {

    }


    public Bitmap threshold(Bitmap image, int levels){
        Log.e("ImageHandler", "thresholding down " + levels + " levels");
        if(levels>0){
            int[] byteArray = new int[image.getWidth() * image.getHeight()];
            image.getPixels(byteArray, 0, image.getWidth(), 0, 0, image.getWidth(), image.getHeight());

            int divisor = (int)Math.pow(2,levels);
            for (int i = 0; i<byteArray.length; i++){
                byteArray[i] = byteArray[i]/divisor*divisor;
            }
            return Bitmap.createBitmap(byteArray, image.getWidth(), image.getHeight(), Bitmap.Config.ARGB_8888);
        }
        return image;
    }

}
