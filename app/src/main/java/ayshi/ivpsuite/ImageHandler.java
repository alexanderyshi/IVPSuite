package ayshi.ivpsuite;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by alexa_000 on 2015-03-08.
 */
public class ImageHandler extends android.app.Fragment implements View.OnClickListener{
    static String ARG_ITEM_ID;
    ImageView imagePreview;
    ProgressBar progressBar;
    Button saveButton;
    Button exportButton;

    File imageSourceFile;
    String imageSourcePath;
    Bitmap previewBitmap;
    private int[] byteArray;
    private int imageWidth;
    private int imageHeight;
    private Bitmap.Config config;

    final int HISTOGRAM_HEIGHT = 450;
    final int HISTOGRAM_WIDTH = 800;
    private final double GAMMA_CONSTANT = 1;

    //TODO: custom JPEG exporter
    //TODO: Gaussian blur, Hough transform, Wiener filter, histogram equalization, histogram matching, locla neighbourhood averaging, median filter
    //TODO: stretch goal - non local means, image restoration
    //TODO: add new Activity for viewing and importing JPEGs from history (save alls new Bitmaps to a custom directory)
    //TODO: use material design themes and buttons, try cards?

    //constructors and overridden classes
    public ImageHandler(){};

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.e(ARG_ITEM_ID, "onCreate");
        // get the byteArray to use for the live preview
        if (getArguments().containsKey("byteArray")){
            byteArray = getArguments().getIntArray("byteArray");
        }
        if (getArguments().containsKey("imageWidth")){
            imageWidth = getArguments().getInt("imageWidth");
        }
        if (getArguments().containsKey("imageHeight")){
            imageHeight = getArguments().getInt("imageHeight");
        }if (getArguments().containsKey("config")){
            String configString = getArguments().getString("config");
            switch (configString){
                case "ARGB_8888":
                    config = Bitmap.Config.ARGB_8888;
                    break;
                case "":
                    config = Bitmap.Config.ARGB_8888;
                    Toast.makeText(getActivity().getBaseContext(), "config not found - using default", Toast.LENGTH_LONG).show();
                    break;
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.e(ARG_ITEM_ID, "onCreateView");

        if (byteArray != null) {
            previewBitmap = Bitmap.createBitmap(byteArray, imageWidth, imageHeight, config);
        }
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.button_export) {
            if (previewBitmap != null){
                exportBitmapToJPEG();
            }
            else{
                Toast.makeText(getActivity().getBaseContext(), "No bitmap to export", Toast.LENGTH_LONG).show();
            }
            Log.e(ARG_ITEM_ID, "export");
        }
    }

    //bitmap handling
    public File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "IVP_" + timeStamp;
        String storageDirPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).getAbsolutePath() + "/ayshi.ivpsuite";
        File storageDir = new File(storageDirPath);
        if( !storageDir.isDirectory() && storageDir.canWrite() ){
            storageDir.delete();
            storageDir.mkdirs();
        }
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir       /* directory */
        );

        Log.e("createImageFile()", image.getAbsolutePath());
        return image;
    }

    //TODO: find a less hacky way to do this - parent ItemListActivity can use the accessors on this class?
    public void saveBitmap(){
        if (byteArray != null){
            try{
                ((ItemListActivity) getActivity()).setImageWidth(previewBitmap.getWidth());
                ((ItemListActivity) getActivity()).setImageHeight(previewBitmap.getHeight());
                ((ItemListActivity) getActivity()).setBitmapConfig(previewBitmap.getConfig());

                byteArray = new int[previewBitmap.getWidth() * previewBitmap.getHeight()];
                previewBitmap.getPixels(byteArray, 0, previewBitmap.getWidth(), 0, 0,
                        previewBitmap.getWidth(), previewBitmap.getHeight());
                ((ItemListActivity) getActivity()).setByteArray(byteArray);
                Toast.makeText(getActivity().getBaseContext(), "Bitmap saved successfully", Toast.LENGTH_LONG).show();
            }
            catch(Exception e){
                e.printStackTrace();
                Toast.makeText(getActivity().getBaseContext(), "Bitmap could not be saved", Toast.LENGTH_LONG).show();
            }
        }
    }

    public void loadBitmap(){
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        previewBitmap = BitmapFactory.decodeFile(
                ((ItemListActivity) getActivity()).getSourceImagePath(), options);

        byteArray = new int[previewBitmap.getWidth() * previewBitmap.getHeight()];
        previewBitmap.getPixels(byteArray, 0, previewBitmap.getWidth(), 0, 0,
                previewBitmap.getWidth(), previewBitmap.getHeight());
        imageHeight = previewBitmap.getHeight();
        imageWidth = previewBitmap.getWidth();
        config = previewBitmap.getConfig();
        saveBitmap();
    }

    public void exportBitmapToJPEG(){
        FileOutputStream stream = null;
        try{
            File file = createImageFile();
            stream = new FileOutputStream(file);
            previewBitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
            MediaScannerConnection.scanFile(getActivity(), new String[]{file.toString()}, null, null);
            Log.e("ImageHandler", "bitmap written to external at" + file.getAbsolutePath());
        }
        catch (IOException e){
            e.printStackTrace();
        }
        finally {
            if (stream != null){
                try{
                    stream.flush();
                    stream.close();
                }
                catch(IOException e){
                    e.printStackTrace();
                }
            }
        }
    }

    private int[] generateHistogramArray(char colour){
        int[] collectorArray = new int[256];
        int offset = -1;

        if (colour == 'a'){
            for (int i = 0; i < byteArray.length; i++){
                int red = ((byteArray[i] >> 16) & 0xff);
                int green = ((byteArray[i] >> 8) & 0xff);
                int blue = (byteArray[i] & 0xff);
                int newValue = (red+green+blue)/3;
                collectorArray[newValue]++;
            }
        }
        else{
            switch(colour){
                case 'r':
                    offset = 16;
                    break;
                case 'g':
                    offset = 8;
                    break;
                case 'b':
                    offset = 0;
                    break;
            }

            if (offset != -1){
                for (int i = 0; i < byteArray.length; i++){
                    int value = ((byteArray[i] >> offset) & 0xff);
                    collectorArray[value]++;
                }
            }
            else{
                Toast.makeText(getActivity().getBaseContext(), "Histogram Array could not be generated", Toast.LENGTH_LONG).show();
            }

        }
        return collectorArray;
    }

    public void generateIntensityHistogram(String colour){
        Bitmap mutableBitmap = Bitmap.createBitmap(HISTOGRAM_WIDTH, HISTOGRAM_HEIGHT, Bitmap.Config.ARGB_8888);
        Canvas mCanvas = new Canvas(mutableBitmap);
        Paint axisPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        axisPaint.setStrokeWidth(axisPaint.getStrokeWidth()*(float)1.5);
        Paint histogramPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

        int[] collectorArray = new int[256];
        if(colour.equals("average")){
            collectorArray = generateHistogramArray('r');
            histogramPaint.setARGB(255,200,200,200);
        }else if (colour.equals("red")) {
            collectorArray = generateHistogramArray('r');
            histogramPaint.setARGB(255,200,100,100);
        }else if (colour.equals("green")) {
            collectorArray = generateHistogramArray('g');
            histogramPaint.setARGB(255,100,200,100);
        }else if (colour.equals("blue")) {
            collectorArray = generateHistogramArray('b');
            histogramPaint.setARGB(255,100,100,200);
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
        //TODO: method should be returning Bitmap instead of directly reassigning the ImageView
        imagePreview.setImageBitmap(mutableBitmap);
    }

    //TODO: image transforms should be put in a different class - static referenced ImageTransformer with passed values, or a child Transformer that inherits from the ImageHandler
    //image transforms
    public Bitmap otsuThreshold(){
        int[] collectorArray = generateHistogramArray('a');
        double[] probArray = new double[256];
        int threshold = 0;
        double mean1 = 0, mean2 = 0, prob1, prob2, var1, var2, minVar1 = 1e12, minVar2 = 1e12;
        for (int i = 0; i<256; i++){
            probArray[i] = collectorArray[i]/(double)(imageHeight*imageWidth);
        }

        //TODO: optimize mean and probability calculation by making additive statements instead of running a loop
        for (int i = 0; i<256; i++){
            threshold++;
            mean1 = mean2 = prob1 = prob2 = var1 = var2 = 0;

            //get the class probabilties
            for (int j = 0; j < threshold; j++){
                prob1 += probArray[j];
            }
            for (int k = threshold; k<256; k++){
                prob2 += probArray[k];
            }
            prob1 /= (imageHeight*imageWidth);
            prob2 /= (imageHeight*imageWidth);

            //get the means
            for (int j = 0; j < threshold; j++){
                mean1 += j*probArray[j];
            }
            for (int k = threshold; k<256; k++){
                mean2 += k*probArray[k];
            }

            mean1 /= prob1;
            mean2 /= prob2;

            //get the variances
            for(int j = 0; i <threshold; j++){
                var1 += Math.pow(j - mean1, 2) * probArray[j];
            }
            for(int k = threshold; i <256; k++){
                var2 += Math.pow(k - mean2, 2) * probArray[k];
            }

            var1 /= prob1;
            var2 /= prob2;
            //save the threshold for minimum variance
            if (i != 0){
                minVar1 = Math.max(var1, minVar1);
                minVar2 = Math.max(var2, minVar2);
            }
            else{
                minVar1 = var1;
                minVar2 = var2;
            }
        }
        int[] tempByteArray = new int[byteArray.length];
        //threshold the byteArray with the otsu threshold
        for (int i = 0; i<byteArray.length; i++){
            //http://www.developer.com/ws/android/programming/Working-with-Images-in-Googles-Android-3748281-2.htm
            //pointer* >> 16 shifts the value to the right by 16 bits, i.e.
            //11000000 10101000 00000001 00000010 becomes
            //00000000 00000000 11000000 10101000
            //http://www.mkyong.com/java/java-and-0xff-example/ - & 0xff grabs last 8 bits from the 32 bit signed int (2^8 values)
            int red = ((byteArray[i] >> 16) & 0xff);
            int green = ((byteArray[i] >> 8) & 0xff);
            int blue = (byteArray[i] & 0xff);
            double average = (red+green+blue)/3;
            if (average>threshold){
                //assign white
                tempByteArray[i] = 0xff000000 | (256 << 16) | (256 << 8) | 256;
            }
            else{
                //assign black
                tempByteArray[i] = 0xff000000 | (0 << 16) | (0 << 8) | 0;
            }
        }
        return Bitmap.createBitmap(byteArray, imageWidth, imageHeight, config);
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
        //TODO: saved grayscale image does not transfer between fragments, find better solution
        byteArray = tempByteArray;
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

    //accessors and mutators

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