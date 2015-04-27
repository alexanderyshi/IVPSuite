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
    Button exportButton;

    File imageSourceFile;
    String imageSourcePath;
    Bitmap previewBitmap;
    public int[] byteArray;
    public int imageWidth;
    public int imageHeight;
    public Bitmap.Config config;




    //TODO: custom JPEG exporter
    //TODO: Gaussian blur, Hough transform, Wiener filter, histogram equalization/AHE/CLAHE, histogram matching, locla neighbourhood averaging, median filter
    //TODO: stretch goal - non local means, image restoration
    //TODO: add new Activity for viewing and importing JPEGs from history
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

    public int[] generateHistogramArray(char colour){
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


    //TODO: image transforms should be put in a different class - static referenced ImageTransformer with passed values, or a child Transformer that inherits from the ImageHandler

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