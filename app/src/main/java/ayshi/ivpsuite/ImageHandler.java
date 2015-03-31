package ayshi.ivpsuite;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.SurfaceTexture;
import android.os.Bundle;
import android.os.Environment;
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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by alexa_000 on 2015-03-08.
 */
public class ImageHandler extends android.app.Fragment implements View.OnClickListener{
    static String ARG_ITEM_ID;
    File imageSourceFile;
    String imageSourcePath;

    ImageView imagePreview;
    Button saveButton;
    Button exportButton;
    Bitmap previewBitmap;
    private int[] byteArray;
    private int imageWidth;
    private int imageHeight;
    private Bitmap.Config config;
    ProgressBar progressBar;
    final int HISTOGRAM_HEIGHT = 450;
    final int HISTOGRAM_WIDTH = 800;

    private final double GAMMA_CONSTANT = 1;

    //TODO: export bitmap as JPEG to file system
    //TODO: only call saveBitmap when destroying fragment
    //TODO: add method for Otsu's thresholding here

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
        //TODO: replace all save buttons with export buttons after making saveBitmap get called with onDestroy (or other fragment eq.)
//        else if (view.getId() == R.id.button_export){
//            exportBitmapToJPEG();
//            Log.e(ARG_ITEM_ID, "export");
//        }
    }

    public File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "IVP_" + timeStamp;
        File storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        //TODO: saving to custom directory (ayshi.ivpsuite)
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );
        return image;
    }

    public void saveBitmap(){
        //TODO: get image parameters from Bundle, reducing dependence on an ItemListActivity instance
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

    public void loadBitmap(){
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
        saveBitmap();
    }

    public void exportBitmapToJPEG(){
        Bitmap tempBitmap = Bitmap.createBitmap(byteArray, imageWidth, imageHeight, config);
        OutputStream stream = null;
        try{
            stream = new FileOutputStream(createImageFile().getAbsolutePath());
            tempBitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
        }
        catch (IOException e){
            e.printStackTrace();
        }
    }

    public void generateAverageIntensityHistogram(){
        Bitmap mutableBitmap = Bitmap.createBitmap(HISTOGRAM_WIDTH, HISTOGRAM_HEIGHT, Bitmap.Config.ARGB_8888);
        Canvas mCanvas = new Canvas(mutableBitmap);
        Paint axisPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        axisPaint.setStrokeWidth(axisPaint.getStrokeWidth()*(float)1.5);
        Paint histogramPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        histogramPaint.setARGB(255,200,200,200);


        //create a collector for intensity frequency
        int[] collectorArray = new int[256];

        for (int i = 0; i < byteArray.length; i++){
            int red = ((byteArray[i] >> 16) & 0xff);
            int green = ((byteArray[i] >> 8) & 0xff);
            int blue = (byteArray[i] & 0xff);
            int newValue = (red+green+blue)/3;
            collectorArray[newValue]++;
        }

        //TODO: set maximum y axis value based on the total number of pixels, making the amounts absolute instead of relative
        int max = -1;
        //find max value
        for (int i = 0; i <255; i++){
            max = collectorArray[i] > max ? collectorArray[i] : max;
        }

        //TODO: add smaller lines to help with frequency / bin estimation
        //bin line
        mCanvas.drawLine((float)10, (float)HISTOGRAM_HEIGHT - 1, (float)10+255*2, (float)HISTOGRAM_HEIGHT - 1, axisPaint);
        //frequency line
        mCanvas.drawLine((float)1, (float)HISTOGRAM_HEIGHT - 10, (float)1, (float)HISTOGRAM_HEIGHT - (10+400), axisPaint);


        for (int i = 0; i <255; i++){
            float startX = (float)(10 + 2*i);
            float startY = (float) (HISTOGRAM_HEIGHT - 10);
            float stopX = (float)(10 + 2*i);
            float stopY = (float) (HISTOGRAM_HEIGHT - (10 + collectorArray[i] * 400.0 / max));
            mCanvas.drawLine(startX, startY, stopX, stopY, histogramPaint);
        }
        imagePreview.setImageBitmap(mutableBitmap);
    }

    public void generateColourIntensityHistogram(String colour){
        Bitmap mutableBitmap = Bitmap.createBitmap(HISTOGRAM_WIDTH, HISTOGRAM_HEIGHT, Bitmap.Config.ARGB_8888);
        Canvas mCanvas = new Canvas(mutableBitmap);
        Paint axisPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        axisPaint.setStrokeWidth(axisPaint.getStrokeWidth()*(float)1.5);
        Paint histogramPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

        int offset = -1;
        if (colour.equals("red")) {
            offset = 16;
            histogramPaint.setARGB(255,200,100,100);
        }if (colour.equals("green")) {
            offset = 8;
            histogramPaint.setARGB(255,100,200,100);
        }if (colour.equals("blue")) {
            offset = 0;
            histogramPaint.setARGB(255,100,100,200);
        }

        if (offset==-1){
            Toast.makeText(getActivity().getBaseContext(), "Colour offset not received properly", Toast.LENGTH_LONG).show();
            return;
        }
        //create a collector for intensity frequency
        int[] collectorArray = new int[256];
        for (int i = 0; i < byteArray.length; i++){
            int value = ((byteArray[i] >> offset) & 0xff);
            collectorArray[value]++;
        }

        int max = -1;
        //find max value
        for (int i = 0; i <255; i++){
            max = collectorArray[i] > max ? collectorArray[i] : max;
        }

        //TODO: add smaller lines to help with frequency / bin estimation
        //bin line
        mCanvas.drawLine((float)10, (float)HISTOGRAM_HEIGHT - 1, (float)10+255*2, (float)HISTOGRAM_HEIGHT - 1, axisPaint);
        //frequency line
        mCanvas.drawLine((float)1, (float)HISTOGRAM_HEIGHT - 10, (float)1, (float)HISTOGRAM_HEIGHT - (10+400), axisPaint);


        for (int i = 0; i <255; i++){
            float startX = (float)(10 + 2*i);
            float startY = (float) (HISTOGRAM_HEIGHT - 10);
            float stopX = (float)(10 + 2*i);
            float stopY = (float) (HISTOGRAM_HEIGHT - (10 + collectorArray[i] * 400.0 / max));
            mCanvas.drawLine(startX, startY, stopX, stopY, histogramPaint);
        }
        imagePreview.setImageBitmap(mutableBitmap);
    }

    public Bitmap threshold(int levels){
        Log.e("ImageHandler", "thresholding down " + levels + " levels");
        if(levels>0){
            int[] tempByteArray = new int[byteArray.length];
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
                tempByteArray[i] = 0xff000000 | (red << 16) | (green << 8) | blue;
            }
            return Bitmap.createBitmap(tempByteArray, imageWidth, imageHeight, config);
        }
        return previewBitmap;
    }

    public Bitmap ARGBtoGrayScale(){
        Log.e("ImageHandler", "creating pseudo grayscale");
        //TODO: investigate null crash - may need to change fragments to recieve value properly
        int[] tempByteArray = new int[byteArray.length];
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
            tempByteArray[i] = 0xff000000 | (red << 16) | (green << 8) | blue;
        }
        saveBitmap();
        //TODO: saved grayscale image does not transfer between fragments
        return Bitmap.createBitmap(tempByteArray, imageWidth, imageHeight, config);
    }

    public Bitmap gammaCorrect(double gammaLevel){
        //TODO: function is taxing the processor heavily
        Log.e("ImageHandler", "gamma correct by: " + gammaLevel);
        int[] tempByteArray = new int[byteArray.length];
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
                tempByteArray[i] = 0xff000000 | (red << 16) | (green << 8) | blue;
                progressBar.setProgress((int)(i/100.0*byteArray.length));
            }
            return Bitmap.createBitmap(tempByteArray, imageWidth, imageHeight, config);
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
