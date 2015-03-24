package ayshi.ivpsuite;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.NumberPicker;
import android.widget.TextView;

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
        saveButton = (Button) rootView.findViewById(R.id.button_save);

        histogramTransformButton.setOnClickListener(this);
        saveButton.setOnClickListener(this);
        generateHistogram();
        return rootView;
    }

    public void onClick(View view) {
        super.onClick(view);
        if (view.getId() == R.id.button_gamma_correction){
            //TODO:histogram transformer function
            generateHistogram();
            saveBitmap();
        }
    }
}
