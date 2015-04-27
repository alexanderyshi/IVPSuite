package ayshi.ivpsuite;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.NumberPicker;

/**
 * A fragment representing a single Item detail screen.
 * This fragment is either contained in a {@link ItemListActivity}
 * in two-pane mode (on tablets) or a {@link ItemDetailActivity}
 * on handsets.
 */
public class Threshold extends ImageHandler {
    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */

    NumberPicker numberPicker;

    public Threshold() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        ARG_ITEM_ID = "threshold_detail_fragment";
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View rootView = inflater.inflate(R.layout.threshold_item_detail, container, false);

        ((TextView) rootView.findViewById(R.id.item_detail_text)).setText(ARG_ITEM_ID);

        imagePreview = (ImageView) rootView.findViewById(R.id.image_preview);
        if (previewBitmap!=null){
            imagePreview.setImageBitmap(previewBitmap);
        }

        final Button thresholdButton = (Button) rootView.findViewById(R.id.button_threshold);
        final Button otsuButton = (Button) rootView.findViewById(R.id.button_otsu);
        exportButton = (Button) rootView.findViewById(R.id.button_export);

        thresholdButton.setOnClickListener(this);
        otsuButton.setOnClickListener(this);
        exportButton.setOnClickListener(this);

        numberPicker = (NumberPicker) rootView.findViewById(R.id.numberPicker);
        numberPicker.setMaxValue(8);
        numberPicker.setMinValue(0);
        return rootView;
    }

    public void onClick(View view) {
        super.onClick(view);
        if (view.getId() == R.id.button_threshold){
            previewBitmap = threshold(numberPicker.getValue());
            imagePreview.setImageBitmap(previewBitmap);
        }
        else if (view.getId() == R.id.button_otsu){
            previewBitmap = otsuThreshold();
            imagePreview.setImageBitmap(previewBitmap);
        }
    }

    public Bitmap otsuThreshold(){
        int[] collectorArray = generateHistogramArray('a');
        double[] probArray = new double[256];
        for (int i = 0; i<256; i++){
            probArray[i] = collectorArray[i]/(double)(imageHeight*imageWidth);
        }
        int threshold, otsuThreshold = 0;
        double mean1, mean2, prob1  = probArray[0], prob2 = 1.0, var1, var2, intraClassVar = 0;
        //TODO: optimize calculation of means and variance with additive calls instead of recomputing, resulting in just one loop
        for (int i = 0; i<256; i++){
            threshold = i;
            mean1 = mean2 = var1 = var2 = 0;

            //get the class probabilties
            prob1 += probArray[threshold];
            prob2 -= probArray[threshold];

            //get the means
            for (int j = 0; j < threshold; j++){
                mean1 += j*collectorArray[j]*probArray[j];
            }
            for (int k = threshold; k<256; k++){
                mean2 += k*collectorArray[k]*probArray[k];
            }

            //get the variances
            for(int j = 0; j <threshold; j++){
                var1 += Math.pow(j - mean1, 2) * probArray[j];
            }
            for(int k = threshold; k <256; k++){
                var2 += Math.pow(k - mean2, 2) * probArray[k];
            }

            //save the threshold for minimum variance
            if (i != 1){
                if (intraClassVar > prob1*var1 + prob2*var2){
                    intraClassVar = prob1*var1 + prob2*var2;
                    otsuThreshold = threshold;
                    Log.e("otsu", i + "new threshold");
                }
            }
            else{
                intraClassVar = prob1*var1 + prob2*var2;
                Log.e("otsu", i+"new threshold");
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
            if (average>otsuThreshold){
                //assign white
                tempByteArray[i] = 0xffffffff;
            }
            else{
                //assign black
                tempByteArray[i] = 0xff000000;
            }
        }
        byteArray = tempByteArray;
        return Bitmap.createBitmap(tempByteArray, imageWidth, imageHeight, config);
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
}
