package ayshi.ivpsuite;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.SurfaceTexture;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
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
    ProgressBar progressBar;

    private final double GAMMA_CONSTANT = 1;

    //TODO: method to save current Bitmap in ItemListActivity instance into a non-volatile file
    //TODO: histogram generation
    //TODO: export as JPEG to file system
    //TODO: only call saveBitmap when destroying fragment
    //TODO: investigate bug upon regression http://stackoverflow.com/questions/3528735/failed-binder-transaction

    public ImageHandler(){};

    //TODO: get image parameters from Bundle, reducing dependence on an ItemListActivity instance

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
        //TODO: reuse the save button as an export button
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

        int[] tempByteArray = new int[previewBitmap.getWidth() * previewBitmap.getHeight()];
        previewBitmap.getPixels(tempByteArray, 0, previewBitmap.getWidth(), 0, 0,
                previewBitmap.getWidth(), previewBitmap.getHeight());
        byteArray = tempByteArray;
        imageHeight = previewBitmap.getHeight();
        imageWidth = previewBitmap.getWidth();
        config = previewBitmap.getConfig();
    }

    public void generateHistogram(){
        //TODO: use SurfaceView's SurfaceHolder to handle the thread, use Canvas to draw onto a custom bitmap
    }

    public Bitmap threshold(int levels){
        Log.e("ImageHandler", "thresholding down " + levels + " levels");
        if(levels>0){

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
            return Bitmap.createBitmap(byteArray, imageWidth, imageHeight, config);
        }
        return previewBitmap;
    }

    public Bitmap ARGBtoGrayScale(){
        Log.e("ImageHandler", "creating pseudo grayscale");
        //TODO: investigate null crash - may need to change fragments to recieve value properly

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
        return Bitmap.createBitmap(byteArray, imageWidth, imageHeight, config);
    }

    public Bitmap gammaCorrect(double gammaLevel){
        //TODO: function is taxing the processor heavily
        Log.e("ImageHandler", "gamma correct by: " + gammaLevel);
        if (gammaLevel != 0){
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
                progressBar.setProgress((int)(i/100.0*byteArray.length));
            }
            return Bitmap.createBitmap(byteArray, imageWidth, imageHeight, config);
        }
        return previewBitmap;
    }

    public void setByteArray(int[] _byteArray){
        byteArray = _byteArray;
    }

    public int[] getByteArray(){
        return byteArray;
    }

    public void setImageWidth(int _imageWidth){
        imageWidth = _imageWidth;
    }

    public int getImageWidth(){
        return imageWidth;
    }

    public void setImageHeight(int _imageHeight){
        imageHeight = _imageHeight;
    }

    public int getImageHeight(){
        return imageHeight;
    }

    public void setBitmapConfig(Bitmap.Config _config){
        config = _config;
    }

    public Bitmap.Config getBitmapConfig(){
        return config;
    }
}
