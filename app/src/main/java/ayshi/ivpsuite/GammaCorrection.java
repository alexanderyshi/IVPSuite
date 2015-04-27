package ayshi.ivpsuite;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.NumberPicker;
import android.widget.ProgressBar;
import android.widget.TextView;

/**
 * A fragment representing a single Item detail screen.
 * This fragment is either contained in a {@link ayshi.ivpsuite.ItemListActivity}
 * in two-pane mode (on tablets) or a {@link ayshi.ivpsuite.ItemDetailActivity}
 * on handsets.
 */
public class GammaCorrection extends ImageHandler {
    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */

    NumberPicker numberPicker;
    String[] gammaValues;
    private final double GAMMA_CONSTANT = 1;


    public GammaCorrection() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        ARG_ITEM_ID = "gamma_correction_fragment";
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View rootView = inflater.inflate(R.layout.gamma_correction_item_detail, container, false);

        ((TextView) rootView.findViewById(R.id.item_detail_text)).setText(ARG_ITEM_ID);

        imagePreview = (ImageView) rootView.findViewById(R.id.image_preview);
        if (previewBitmap!=null){
            imagePreview.setImageBitmap(previewBitmap);
        }

        final Button gammaCorrectionButton = (Button) rootView.findViewById(R.id.button_gamma_correction);
        exportButton = (Button) rootView.findViewById(R.id.button_export);

        progressBar = (ProgressBar) rootView.findViewById(R.id.progress_bar);
        progressBar.setMax(100);

        gammaCorrectionButton.setOnClickListener(this);
        exportButton.setOnClickListener(this);

        numberPicker = (NumberPicker) rootView.findViewById(R.id.numberPicker);
        setGammaValues();
        return rootView;
    }

    public void onClick(View view) {
        super.onClick(view);
        progressBar.setProgress(0);
        if (view.getId() == R.id.button_gamma_correction){
            previewBitmap = gammaCorrect(mapToDouble(numberPicker.getValue()));
            imagePreview.setImageBitmap(previewBitmap);
        }
    }

    private double mapToDouble(int id){
        return Double.parseDouble(gammaValues[id]);
    }

    private void setGammaValues(){
        gammaValues = new String[]{".125",".25",".5",".75",".9","1.1","1.25","1.5","2.0"};
        numberPicker.setMaxValue(8);
        numberPicker.setMinValue(0);
        //allows 9 distinct value types
        numberPicker.setDisplayedValues(gammaValues);
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
}
