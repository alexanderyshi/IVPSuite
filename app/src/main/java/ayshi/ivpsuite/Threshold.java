package ayshi.ivpsuite;

import android.os.Bundle;
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
}
