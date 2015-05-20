package ayshi.ivpsuite;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

/**
 * A fragment representing a single Item detail screen.
 * This fragment is either contained in a {@link ItemListActivity}
 * in two-pane mode (on tablets) or a {@link ItemDetailActivity}
 * on handsets.
 */
public class HistogramTransformer extends ImageHandler {
    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    final int HISTOGRAM_HEIGHT = 450;
    final int HISTOGRAM_WIDTH = 800;
//TODO: histogram equalization/AHE/CLAHE, histogram matching
    public HistogramTransformer() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        ARG_ITEM_ID = "histogram_transformer_fragment";
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View rootView = inflater.inflate(R.layout.histogram_transformer_item_detail, container, false);

        ((TextView) rootView.findViewById(R.id.item_detail_text)).setText(ARG_ITEM_ID);

        imagePreview = (ImageView) rootView.findViewById(R.id.image_preview);
        if (previewBitmap!=null){
            imagePreview.setImageBitmap(previewBitmap);
        }

        final Button histogramTransformButton = (Button) rootView.findViewById(R.id.button_histogram_transform);
        final Button redButton = (Button) rootView.findViewById(R.id.button_histogram_red);
        final Button greenButton = (Button) rootView.findViewById(R.id.button_histogram_green);
        final Button blueButton = (Button) rootView.findViewById(R.id.button_histogram_blue);
        exportButton = (Button) rootView.findViewById(R.id.button_export);

        histogramTransformButton.setOnClickListener(this);
        exportButton.setOnClickListener(this);
        redButton.setOnClickListener(this);
        greenButton.setOnClickListener(this);
        blueButton.setOnClickListener(this);
        return rootView;
    }

    public void onClick(View view) {
        super.onClick(view);
        if (view.getId() == R.id.button_histogram_transform){
            generateIntensityHistogram("average");
        }else if (view.getId() == R.id.button_histogram_red){
            generateIntensityHistogram("red");
        }else if (view.getId() == R.id.button_histogram_green){
            generateIntensityHistogram("green");
        }else if (view.getId() == R.id.button_histogram_blue){
            generateIntensityHistogram("blue");
        }


    }

    public void generateIntensityHistogram(String colour){
        Bitmap mutableBitmap = Bitmap.createBitmap(HISTOGRAM_WIDTH, HISTOGRAM_HEIGHT, Bitmap.Config.ARGB_8888);
        Canvas mCanvas = new Canvas(mutableBitmap);
        Paint axisPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        axisPaint.setStrokeWidth(axisPaint.getStrokeWidth()*(float)1.5);
        Paint histogramPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        Paint maxValuePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        maxValuePaint.setARGB(255,100,100,100);
        maxValuePaint.setTextSize(24);



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
        //max fraction
        mCanvas.drawText("" + Math.round((float)max/(imageHeight*imageWidth)*10000)/10000.0, (float)20, (float)HISTOGRAM_HEIGHT - (10+400), maxValuePaint);


        for (int i = 0; i <255; i++){
            float startX = (float)(10 + 2*i);
            float startY = (float) (HISTOGRAM_HEIGHT - 10);
            float stopX = (float)(10 + 2*i);
            float stopY = (float) (HISTOGRAM_HEIGHT - (10 + collectorArray[i] * 400.0 / max));
//            float stopY = (float) (HISTOGRAM_HEIGHT - (10 + collectorArray[i] * 400.0 / (imageWidth*imageHeight)));
            mCanvas.drawLine(startX, startY, stopX, stopY, histogramPaint);
        }
        //TODO: method should be returning Bitmap instead of directly reassigning the ImageView
        imagePreview.setImageBitmap(mutableBitmap);
    }

    public Bitmap histogramEqualization(){

        return null;
    }
}
