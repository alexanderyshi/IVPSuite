package ayshi.ivpsuite;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;

/**
 * Created by alexa_000 on 2015-03-08.
 */
public class ImageHandler extends android.app.Fragment implements View.OnClickListener{
    static String ARG_ITEM_ID;
    File imageSourceFile;
    String imageSourcePath;

    ImageView imagePreview;
    Button saveButton;
    Bitmap previewBitmap;
    private int[] byteArray;
    private int imageWidth;
    private int imageHeight;
    private Bitmap.Config config;

    private final double GAMMA_CONSTANT = 1;

    //TODO: method to save current Bitmap in ItemListActivity instance into a non-volatile file
    //TODO: histogram generation
    //TODO: export as JPEG to file system
    //TODO: http://stackoverflow.com/questions/3528735/failed-binder-transaction

    public ImageHandler(){};

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.e(ARG_ITEM_ID, "onCreate");
        // get the byteArray to use for the live preview
        if (getArguments().containsKey("byteArray")){
            byteArray = getArguments().getIntArray("byteArray");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.e(ARG_ITEM_ID, "onCreateView");

        if (byteArray != null) {
            imageWidth = ((ItemListActivity) getActivity()).getImageWidth();
            imageHeight = ((ItemListActivity) getActivity()).getImageHeight();
            config = ((ItemListActivity) getActivity()).getBitmapConfig();
            previewBitmap = Bitmap.createBitmap(byteArray, imageWidth, imageHeight, config);
        }
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.button_save){
            saveBitmap();
            Log.e(ARG_ITEM_ID, "save");
        }
    }

    public void saveBitmap(){
        try{
            ((ItemListActivity) getActivity()).setImageWidth(previewBitmap.getWidth());
            ((ItemListActivity) getActivity()).setImageHeight(previewBitmap.getHeight());
            ((ItemListActivity) getActivity()).setBitmapConfig(previewBitmap.getConfig());

            int[] tempByteArray = new int[previewBitmap.getWidth() * previewBitmap.getHeight()];
            previewBitmap.getPixels(tempByteArray, 0, previewBitmap.getWidth(), 0, 0,
                    previewBitmap.getWidth(), previewBitmap.getHeight());
            ((ItemListActivity) getActivity()).setByteArray(tempByteArray);
            Toast.makeText(getActivity().getBaseContext(), "Bitmap saved successfully", Toast.LENGTH_LONG).show();
        }
        catch(Exception e){
            e.printStackTrace();
            Toast.makeText(getActivity().getBaseContext(), "Bitmap could not be saved", Toast.LENGTH_LONG).show();
        }
    }

    public void decodeFile(){
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        previewBitmap = BitmapFactory.decodeFile(
                ((ItemListActivity) getActivity()).getSourceImagePath(), options);
    }

    //TODO: change all the IVP methods to take byte Arrays/dim/config and create a static method to create a Bitmap object for the return field

    public Bitmap threshold(Bitmap image, int levels){
        Log.e("ImageHandler", "thresholding down " + levels + " levels");
        if(levels>0){

            int[] byteArray = new int[image.getWidth() * image.getHeight()];
            image.getPixels(byteArray, 0, image.getWidth(), 0, 0, image.getWidth(), image.getHeight());

            int divisor = (int)Math.pow(2,levels);
            for (int i = 0; i<byteArray.length; i++){
                //http://www.developer.com/ws/android/programming/Working-with-Images-in-Googles-Android-3748281-2.htm
                //pointer* >> 16 shifts the value to the right by 16 bits, i.e.
                    //11000000 10101000 00000001 00000010 becomes
                    //00000000 00000000 11000000 10101000
                //http://www.mkyong.com/java/java-and-0xff-example/ - & 0xff grabs last 8 bits from the 32 bit signed int (2^8 values)
                int red = ((byteArray[i] >> 16) & 0xff)/divisor*divisor;
                int green = ((byteArray[i] >> 8) & 0xff)/divisor*divisor;
                int blue = (byteArray[i] & 0xff)/divisor*divisor;
                byteArray[i] = 0xff000000 | (red << 16) | (green << 8) | blue;
            }
            return Bitmap.createBitmap(byteArray, image.getWidth(), image.getHeight(), Bitmap.Config.ARGB_8888);
        }
        return image;
    }

    public Bitmap ARGBtoGrayScale(Bitmap image){
        Log.e("ImageHandler", "creating pseudo grayscale");

        int[] byteArray = new int[image.getWidth() * image.getHeight()];
        image.getPixels(byteArray, 0, image.getWidth(), 0, 0, image.getWidth(), image.getHeight());

        for (int i = 0; i<byteArray.length; i++){
            //http://www.developer.com/ws/android/programming/Working-with-Images-in-Googles-Android-3748281-2.htm
            //http://www.mkyong.com/java/java-and-0xff-example/ - & 0xff grabs last 8 bits from the 32 bit signed int (2^8 values)
            //pointer* >> 16 shifts the value to the right by 16 bits, i.e.
            //11000000 10101000 00000001 00000010 becomes
            //00000000 00000000 11000000 10101000
            int red = ((byteArray[i] >> 16) & 0xff);
            int green = ((byteArray[i] >> 8) & 0xff);
            int blue = (byteArray[i] & 0xff);
            int newValue = (red+green+blue)/3;
            red = green = blue = newValue;
            byteArray[i] = 0xff000000 | (red << 16) | (green << 8) | blue;
        }
        return Bitmap.createBitmap(byteArray, image.getWidth(), image.getHeight(), Bitmap.Config.ARGB_8888);
    }

    public Bitmap gammaCorrect(Bitmap image, double gammaLevel){
        Log.e("ImageHandler", "gamma correct by: " + gammaLevel);
        if (gammaLevel != 0){
            int[] byteArray = new int[image.getWidth() * image.getHeight()];
            image.getPixels(byteArray, 0, image.getWidth(), 0, 0, image.getWidth(), image.getHeight());

            for (int i = 0; i<byteArray.length; i++){
                //http://www.developer.com/ws/android/programming/Working-with-Images-in-Googles-Android-3748281-2.htm
                //http://www.mkyong.com/java/java-and-0xff-example/ - & 0xff grabs last 8 bits from the 32 bit signed int (2^8 values)
                //pointer* >> 16 shifts the value to the right by 16 bits, i.e.
                //11000000 10101000 00000001 00000010 becomes
                //00000000 00000000 11000000 10101000
                int red = ((byteArray[i] >> 16) & 0xff);
                int green = ((byteArray[i] >> 8) & 0xff);
                int blue = (byteArray[i] & 0xff);
                red  = (int)(GAMMA_CONSTANT * Math.pow(red/255.0, gammaLevel)*255.0);
                green  = (int)(GAMMA_CONSTANT * Math.pow(green/255.0, gammaLevel)*255.0);
                blue  = (int)(GAMMA_CONSTANT * Math.pow(blue/255.0, gammaLevel)*255.0);
                byteArray[i] = 0xff000000 | (red << 16) | (green << 8) | blue;
            }
        }
        return Bitmap.createBitmap(byteArray, image.getWidth(), image.getHeight(), Bitmap.Config.ARGB_8888);
    }
}
