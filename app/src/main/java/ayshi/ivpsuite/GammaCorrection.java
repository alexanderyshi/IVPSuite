package ayshi.ivpsuite;

import android.os.Bundle;
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
        saveButton = (Button) rootView.findViewById(R.id.button_save);

        progressBar = (ProgressBar) rootView.findViewById(R.id.progress_bar);
        progressBar.setMax(100);

        gammaCorrectionButton.setOnClickListener(this);
        saveButton.setOnClickListener(this);

        numberPicker = (NumberPicker) rootView.findViewById(R.id.numberPicker);
        setGammaValues();
        return rootView;
    }

    public void onClick(View view) {
        super.onClick(view);
        if (view.getId() == R.id.button_gamma_correction){
            previewBitmap = gammaCorrect(mapToDouble(numberPicker.getValue()));
            imagePreview.setImageBitmap(previewBitmap);
            saveBitmap();
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
}
